package me.kennydude.transtimetable;

import java.util.List;

import org.json.JSONArray;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

/**
 * You should extend from me, and nobody else! I manage a lot of serialisation and binding for you >:}
 * @author kennydude
 *
 */
public abstract class TransService extends Service {
	private final ITransService.Stub t = new TService() ;
	
	public abstract List<TransitItem> getDepartures(String stationID);
	public abstract List<TransitItem> getArrivals(String stationID);
	public abstract Station getStation(String stationID);
	
	public abstract List<Station> getNearbyStations(double lat, double lon);
	
	@Override
	public IBinder onBind(Intent i){
		return t;
	}
	
	public class TService extends ITransService.Stub {

		@Override
		public String getNearbyStations(double lat, double lon) throws RemoteException {
			return serializeStationList(TransService.this.getNearbyStations(lat, lon));
		}
		
		@Override
		public String getMatchingStations(String query) throws RemoteException {
			return null;
		}
		
		private String serializeTransitList(List<TransitItem> in){
			JSONArray jo = new JSONArray();
			for(TransitItem ti : in){
				jo.put(ti.toJSONObject());
			}
			return jo.toString();
		}
		
		private String serializeStationList(List<Station> in){
			JSONArray jo = new JSONArray();
			for(Station ti : in){
				jo.put(ti.toJSONObject());
			}
			return jo.toString();
		}
	
		@Override
		public String getStationDepartures(String stationID) throws RemoteException {
			return serializeTransitList(getDepartures(stationID));
		}
	
		@Override
		public String getStationArrivals(String stationID) throws RemoteException {
			return serializeTransitList(getArrivals(stationID));
		}
		
		public String getStationDetails( String id ){
			return getStation(id).toJSONObject().toString();
		}
	
		@Override
		public String getTransitItemDetails(String id) throws RemoteException {
			// TODO Auto-generated method stub
			return null;
		}
	}

}
