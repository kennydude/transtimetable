package me.kennydude.transtimetable.ui;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import me.kennydude.transtimetable.ITransService;
import me.kennydude.transtimetable.Station;
import me.kennydude.transtimetable.TransitItem;

public class TransServiceBinder {
	ITransService backend;
	
	public TransServiceBinder(ITransService binder){
		this.backend = binder;
	}
	
	public Station getStationDetails(String stationID){
		try{
			return Station.fromJSONObject( new JSONObject( backend.getStationDetails(stationID) ) );
		} catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	public String getStatusUpdate(String id){
		try{
			return backend.getTravelUpdates(id);
		} catch(Exception e){
			e.printStackTrace();
			return "";
		}
	}
	
	public TransitItem getTransitItem(String id){
		try{
			return TransitItem.fromJSONObject( new JSONObject( backend.getTransitItemDetails(id) ) );
		} catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	public List<Station> searchStations(String key){
		try{
			List<Station> r = new ArrayList<Station>();
			
			JSONArray ja = new JSONArray( backend.getMatchingStations(key) );
			
			for(int i = 0; i < ja.length(); i ++){
				Station it = Station.fromJSONObject( ja.getJSONObject(i) );
				r.add(it);
			}
			return r;
			
		} catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	public List<Station> getStationsNearby(double lat, double lon){
		try{
			List<Station> r = new ArrayList<Station>();
			
			JSONArray ja = new JSONArray( backend.getNearbyStations( lat, lon ) );
			
			for(int i = 0; i < ja.length(); i ++){
				Station it = Station.fromJSONObject( ja.getJSONObject(i) );
				r.add(it);
			}
			return r;
			
		} catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}

	public List<TransitItem> getDepartures( String stationID ) {
		try{
			JSONArray ja = new JSONArray( backend.getStationDepartures( stationID ) );
			
			List<TransitItem> r = new ArrayList<TransitItem>();
			for(int i = 0; i < ja.length(); i ++){
				TransitItem it = TransitItem.fromJSONObject( ja.getJSONObject(i) );
				r.add(it);
			}
			
			return r;
		} catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}

	public List<TransitItem> getArrivals(String id) {
		try{
			JSONArray ja = new JSONArray( backend.getStationArrivals( id ) );
			
			List<TransitItem> r = new ArrayList<TransitItem>();
			for(int i = 0; i < ja.length(); i ++){
				TransitItem it = TransitItem.fromJSONObject( ja.getJSONObject(i) );
				r.add(it);
			}
			
			return r;
		} catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
}
