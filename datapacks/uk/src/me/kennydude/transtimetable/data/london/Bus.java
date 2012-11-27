package me.kennydude.transtimetable.data.london;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;

import android.net.Uri;

import me.kennydude.transtimetable.Station;
import me.kennydude.transtimetable.TransitItem;

/**
 * London Bus Service
 * @author kennydude
 *
 */
public class Bus extends LondonTransitService {
	public static final String stationReturnList = "&StopAlso=True&ReturnList=StopPointName,StopID,Latitude,Longitude,StopPointType";
	public static final String baseURL = "http://countdown.api.tfl.gov.uk/interfaces/ura/instant_V1";
	
	JSONArray getJSON(String url){
		try{
			HttpGet hg = new HttpGet(url);
			DefaultHttpClient dhc = new DefaultHttpClient();
			HttpResponse r = dhc.execute(hg);
			if(r.getStatusLine().getStatusCode() != 200){
				throw new Exception("Non 200 Response ;_;");
			}
			
			// TfL, why return invalid JSON?
			String lines = EntityUtils.toString(r.getEntity());
			JSONArray ja = new JSONArray();
			for(String line : lines.split("\n")){
				ja.put( new JSONArray( line ) );
			}
			return ja;
		} catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}

	Station getStationFromArr(JSONArray ja) throws JSONException{
		Station r = new Station();
		
		r.name = ja.getString(1);
		r.id = ja.getString(2);
		r.type = TransitItem.TransitType.BUS;
		r.where = new Station.Location(ja.getDouble(4), ja.getDouble(5));
		
		return r;
	}
	
	@Override
	public List<TransitItem> getArrivals(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<TransitItem> getDepartures(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Station> getMatchingStations(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Station> getNearbyStations(double lat, double lon) {
		if(!isInLondon(lat, lon)){ return null; }
		try{
			JSONArray nearby = getJSON(baseURL + "?Circle=" + lat + "," + lon +
					"&StopPointState=0" + stationReturnList);
			
			List<Station> r = new ArrayList<Station>();
			for(int i = 0; i < nearby.length(); i++){
				JSONArray station = nearby.getJSONArray(i);
				if(station.getInt(0) == 0){
					r.add( getStationFromArr(station) );
				}
			}
			return r;
			
		} catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Station getStation(String id) {
		try{
			JSONArray nearby = getJSON(baseURL + "?StopID=" + Uri.encode(id) + stationReturnList);
			for(int i = 0; i < nearby.length(); i++){
				JSONArray station = nearby.getJSONArray(i);
				if(station.getInt(0) == 0){
					return getStationFromArr(station);
				}
			}
		} catch(Exception e){
			e.printStackTrace();
			return null;
		}
		return null;
	}

	@Override
	public TransitItem getTransitDetails(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getTravelUpdateForStation(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}
