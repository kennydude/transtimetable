package me.kennydude.transtimetable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A train or bus or something
 * 
 * @author kennydude
 *
 */
public class TransitItem {
	public TransitType type = TransitType.UNKNOWN;
	
	/**
	 * Please set this to something we can request separately!
	 */
	public String id = "";
	
	public String operator = "";
	public String from = "";
	public String to = "";
	
	public int preferedStop = -1;
	
	/**
	 * This is used to signal a specific stop, for example on the departure board
	 * as it can only show 1 stop
	 * 
	 * @return
	 */
	public Stop getPreferedStop(){
		return stops.get(preferedStop);
	}
	
	public List<Stop> stops = new ArrayList<Stop>();
	public static class Stop{
		public static enum Status{
			LATE, ON_TIME, CANCELED, PAST, UNKNOWN, STARTS_HERE
		}
		
		public String where = "";
		public String platform = "";
		public Calendar when = null;
		public Status status = Status.UNKNOWN;
		
		public JSONObject toJSONObject(){
			try{
				JSONObject jo = new JSONObject();
				jo.put("where", where);
				jo.put("platform", platform);
				jo.put("when", Utils.saveCalendar(when));
				jo.put("status", status.toString());
				
				return jo;
			} catch(Exception e){
				e.printStackTrace();
				return null;
			}
		}

		public static Stop fromJSONObject(JSONObject jsonObject) throws JSONException {
			Stop r = new Stop();
			r.where = jsonObject.getString("where");
			
			// TODO: Sort out deprecation
			r.when = Utils.readCalendar( jsonObject.getString("when") );
			r.platform = jsonObject.getString("platform");
			r.status = Status.valueOf( jsonObject.getString("status") );
			
			return r;
		}
	}
	
	public enum TransitType{
		BUS, TRAIN, AIRPLANE, UNKNOWN
	}
	
	public JSONObject toJSONObject(){
		try{
			JSONObject jo = new JSONObject();
			jo.put("id", id);
			jo.put("operator", operator);
			jo.put("from", from);
			jo.put("to", to);
			jo.put("type", type.toString());
			jo.put("preferedStop", preferedStop);
			
			JSONArray jstops = new JSONArray();
			for(Stop stop : stops){
				jstops.put( stop.toJSONObject() );
			}
			jo.put("stops", jstops);
			
			return jo;
		} catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}

	public static TransitItem fromJSONObject(JSONObject jsonObject) throws JSONException {
		TransitItem ti = new TransitItem();
		ti.id = jsonObject.getString("id");
		ti.operator = jsonObject.getString("operator");
		ti.from = jsonObject.getString("from");
		ti.to = jsonObject.getString("to");
		ti.type = TransitItem.TransitType.valueOf( jsonObject.getString("type") );
		ti.preferedStop = jsonObject.getInt("preferedStop");
		
		if(jsonObject.has("stops")){
			JSONArray ja = jsonObject.getJSONArray("stops");
			ti.stops = new ArrayList<Stop>();
			if( ja.length() >= 1 ){
				for(int i = 0; i < ja.length(); i++){
					ti.stops.add( Stop.fromJSONObject( ja.getJSONObject(i) ) );
				}
			}
		}
		
		return ti;
	}
}
