package me.kennydude.transtimetable.data;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.json.JSONException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import me.kennydude.transtimetable.Station;
import me.kennydude.transtimetable.TransService;
import me.kennydude.transtimetable.TransitItem;
import me.kennydude.transtimetable.Utils;
import me.kennydude.transtimetable.TransitItem.TransitType;

/**
 * National Rail Provider
 * 
 * TODO: Push out to external download
 * 
 * @author kennydude
 *
 */
public class NationalRail extends TransService {
	public static final String USER_AGENT = "Mozilla/5.0 (Linux; U; Android 3.0; en-us; Xoom Build/HRI39) AppleWebKit/534.13 (KHTML, like Gecko) Version/4.0 Safari/534.13";
	
	class NREDBHelper extends SQLiteOpenHelper{
		public static final int DATABASE_VERSION = 1;
		public static final String DATABASE_NAME = "nre";
		
		public NREDBHelper(Context context) throws Exception {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
			if(!context.getDatabasePath(DATABASE_NAME).exists()){
				// Copy database
				try{
					context.getDatabasePath(DATABASE_NAME).getParentFile().mkdirs();
					context.getDatabasePath(DATABASE_NAME).createNewFile();
					
					int bufSizeHint = 8 * 1024;
					BufferedInputStream in = new BufferedInputStream(context.getAssets().open("nrl.db"));
					BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream( context.getDatabasePath(DATABASE_NAME) ));
					int read = -1;
					byte[] buf = new byte[bufSizeHint];
					while ((read = in.read(buf, 0, bufSizeHint)) >= 0) {
						out.write(buf, 0, read);
					}
					out.flush();
					
					out.close();
					in.close();
				} catch(Exception e){
					e.printStackTrace();
					throw new Exception("Database was not copied ;__;");
				}
			}
	    }

		@Override public void onCreate(SQLiteDatabase arg0) {}
		@Override public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {}
	}
	SQLiteDatabase stations;
	
	/**
	 * We need to have a asset of the stations because of the lack of API
	 */
	void ensureStationsLoaded(){
		if(stations == null){
			try{
				stations = new NREDBHelper(this).getReadableDatabase();
			} catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	Station fromCursor(Cursor jo) throws JSONException{
		Station s = new Station();
		s.type = TransitItem.TransitType.TRAIN;
		s.name = jo.getString(0);
		s.id = jo.getString(1);
		s.where = new Station.Location( Double.parseDouble(jo.getString(2)), Double.parseDouble(jo.getString(3)) );
		return s;
	}
	
	Document getDOM(String url){
		try{
			return Jsoup.connect(url).header("User-Agent", USER_AGENT).get();
		} catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public List<TransitItem> getDepartures(String stationID) {
		return scrapNRLBoard(stationID, "dep");
	}
	
	List<TransitItem> scrapNRLBoard(String stationID, String page){
		Document dom = getDOM("http://m.nationalrail.co.uk/pj/ldbboard/"+ page +"/" + stationID.toUpperCase(Locale.UK));
		if(dom == null) return null;
		
		ArrayList<TransitItem> r = new ArrayList<TransitItem>();
		
		// Go to h2s which are the TransitTypes
		Elements hl = dom.select("h2.hList");
		for( Element heading : hl ){
			String hV = heading.text().toLowerCase(Locale.UK);
			TransitType tt = TransitType.UNKNOWN;
			
			if(hV.contains("train")){
				tt = TransitType.TRAIN;
			} else if(hV.contains("bus")){ 
				tt = TransitType.BUS;
			} else{
				System.out.print("Unknown NRL Type: " + hV);
			}
			
			// Now get the list for that place
			Element list = heading.nextElementSibling();
			if(list.nodeName().equals("ul")){
				for( Element item : list.children() ){
					for( Element i : item.children() ) {
						if( i.nodeName().equals("a") ){
							item = i;
							break;
						}
					}
					
					TransitItem ti = new TransitItem();
					ti.type = tt;
					ti.id = item.attributes().get("href");
					
					ti.from = stationID;
					ti.to = item.select(".station").text();
					
					TransitItem.Stop thisStop = new TransitItem.Stop();
					
					String[] time = item.select(".time").get(0).ownText().split(":");
					Calendar when = Calendar.getInstance();
					when.setTime(new Date());
					when.set(Calendar.HOUR_OF_DAY, Integer.parseInt( time[0] ) );
					when.set(Calendar.MINUTE, Integer.parseInt( time[1] ) );
					thisStop.when = when;
					
					String status = item.select(".time small").text().toLowerCase(Locale.UK);
					if(status.equals("on time")){
						thisStop.status = TransitItem.Stop.Status.ON_TIME;
					} else if(status.equals("starts here")){
						thisStop.status = TransitItem.Stop.Status.STARTS_HERE;
					} else if(status.contains(":")){
						// Late
						thisStop.status = TransitItem.Stop.Status.LATE;
					} else if(status.contains("cancelled")){
						thisStop.status = TransitItem.Stop.Status.CANCELED;
					}
					
					if( item.select(".platform").size() > 0 ){
						thisStop.where = item.select(".platform").get(0).ownText();
					}
					
					ti.stops.add(thisStop);
					ti.preferedStop = 0;
					
					r.add(ti);
				}
			}
		}
		
		return r;
	}

	@Override
	public List<TransitItem> getArrivals(String stationID) {
		return scrapNRLBoard(stationID, "arr");
	}

	@Override
	public Station getStation(String stationID) {
		ensureStationsLoaded();
		try{
			Cursor t = stations.rawQuery("SELECT * FROM stations WHERE code = ?", new String[] { stationID } );
			t.moveToFirst();
			return fromCursor(t);
		} catch(Exception e){ e.printStackTrace(); }
		return null;
	}

	@Override
	public List<Station> getNearbyStations(double lat, double lon) {
		ensureStationsLoaded();
		try{
			List<Station> r = new ArrayList<Station>();
			String sql = "SELECT * FROM stations ORDER BY " + Utils.getOrderBySQL(lat, lon) + " LIMIT 0, 10";
			Cursor t = stations.rawQuery(sql, new String[]{});
			
			while(t.moveToNext() == true){
				r.add(fromCursor(t));
			}
			
			stations.close();
			
			return r;
		} catch(Exception e){ e.printStackTrace(); } 
		return null;
	}

	@Override
	public List<Station> getMatchingStations(String key) {
		ensureStationsLoaded();
		try{
			List<Station> r = new ArrayList<Station>();
			String sql = "SELECT * FROM `stations` WHERE name LIKE '%"+key+"%' OR code LIKE '%"+key+"%' LIMIT 0, 10";
			Cursor t = stations.rawQuery(sql, new String[]{});
			
			while(t.moveToNext() == true){
				r.add(fromCursor(t));
			}
			
			stations.close();
			
			return r;
		} catch(Exception e){ e.printStackTrace(); } 
		return null;
	}

}
