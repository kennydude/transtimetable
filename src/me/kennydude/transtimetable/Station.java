package me.kennydude.transtimetable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A station
 * @author kennydude
 *
 */
public class Station {
	public String id;
	public String name;
	public Location where;
	public TransitItem.TransitType type;
	
	public static class Location {
		public Location(float lat, float lon){
			this.lat = lat;
			this.lon = lon;
		}
		public Location(double lat, double lon){
			this.lat = lat;
			this.lon = lon;
		}
		
		public double lat, lon;

		public JSONObject toJSONObject() throws JSONException {
			JSONObject jo = new JSONObject();
			jo.put("lat", lat);
			jo.put("lon", lon);
			return jo;
		}

		public static Location fromJSONObject(JSONObject jsonObject) throws JSONException {
			Location r = new Location( jsonObject.getDouble("lat"), jsonObject.getDouble("lon") );
			return r;
		}
	}
	
	public JSONObject toJSONObject(){
		try{
			JSONObject jo = new JSONObject();
			jo.put("id", id);
			jo.put("name", name);
			jo.put("type", type.toString());
			jo.put("location", where.toJSONObject());
			
			return jo;
		} catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}

	public static Station fromJSONObject(JSONObject jsonObject) throws JSONException {
		Station r = new Station();
		r.id = jsonObject.getString("id");
		r.name = jsonObject.getString("name");
		r.where = Location.fromJSONObject( jsonObject.getJSONObject("location") );
		r.type = TransitItem.TransitType.valueOf( jsonObject.getString("type") );
		
		return r;
	}
}
