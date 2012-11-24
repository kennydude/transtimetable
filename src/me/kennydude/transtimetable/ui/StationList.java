package me.kennydude.transtimetable.ui;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import com.emilsjolander.components.stickylistheaders.StickyListHeadersBaseAdapter;
import com.emilsjolander.components.stickylistheaders.StickyListHeadersListView;

import me.kennydude.transtimetable.R;
import me.kennydude.transtimetable.Station;
import me.kennydude.transtimetable.ui.TransitActivity.LocateStationHelperCallback;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * A list of the stations you have favourited or are near you with the option
 * to search for more!
 * 
 * @author kennydude
 *
 */
public class StationList extends Activity {
	public static final int FAVOURITES_ID = 1;
	public static final int NEARBY_ID = 2;
	StationListAdapter sla;
	
	StickyListHeadersListView getListView(){
		return (StickyListHeadersListView) findViewById(android.R.id.list);
	}
	
	@Override
	public void onCreate(Bundle bis){
		super.onCreate(bis);
		
		if(getListView() != null) return;
		
		setContentView(R.layout.activity_stationlist);
		sla = new StationListAdapter(this);
		getListView().setAdapter(sla);
		
		getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
					long arg3) {
				StationListItem sli = sla.items.get(pos);
				
				Intent i = new Intent(StationList.this, StationView.class);
				i.putExtra("pname", sli.pname);
				i.putExtra("class", sli.cls);
				i.putExtra("station", sli.id);
				startActivity(i);
			}
		});
	}
	
	LocationListener mLocationListener = new LocationListener(){

		@Override
		public void onLocationChanged(final Location whereAmI) {
			// We have a new location! :D
			new Thread(new Runnable(){

				@Override
				public void run() {
					TransitActivity.locateStationHelper(StationList.this, new LocateStationHelperCallback<StationListItem>(){

						@Override
						public List<StationListItem> getStations(TransServiceBinder tsb, ComponentName cn) {
							System.out.println(cn.toShortString());
							List<Station> items = tsb.getStationsNearby( whereAmI.getLatitude(), whereAmI.getLongitude() );
							if(items == null) return null;
							
							ArrayList<StationListItem> r = new ArrayList<StationListItem>();
							for(Station item : items){
								StationListItem sli = new StationListItem();
								sli.headerId = NEARBY_ID;
								sli.id = item.id;
								sli.station = item;
								sli.pname = cn.getPackageName();
								sli.cls = cn.getClassName();
								r.add(sli);
							}
							return r;
						}

						@Override
						public void finishLocation(final List<StationListItem> results) {
							// publish results
							runOnUiThread(new Runnable(){

								@Override
								public void run() {
									sla.removeAllUnderHeader( NEARBY_ID );
									sla.items.addAll(sla.items.size(), results);
									sla.notifyDataSetChanged();
								}
								
							});
						}
						
					});
				}
				
			}).start();
		}

		@Override public void onProviderDisabled(String arg0) {}
		@Override public void onProviderEnabled(String arg0) {}
		@Override public void onStatusChanged(String arg0, int arg1, Bundle arg2) {}
		
	};
	
	public void onPause(){
		super.onPause();
		LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		lm.removeUpdates(mLocationListener);
	}
	
	public void onResume(){
		super.onResume();
		
		LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		lm.requestLocationUpdates( 1000 * 60 * 10, 10, new Criteria(), mLocationListener, null );
		
		// Get Favourites
		sla.removeAllUnderHeader(FAVOURITES_ID);
		for( FavouriteManager.FavId fid : FavouriteManager.getFavourites( this ) ){
			StationListItem sli = new StationListItem();
			sli.headerId = FAVOURITES_ID;
			sli.id = fid.id;
			sli.pname = fid.pname;
			sli.cls = fid.cls;
			
			try{
				// TODO: We need a background lookup for invalid/old data
				sli.station = Station.fromJSONObject( new JSONObject( CacheManager.getCacheItem(this, "station-"  + sli.id) ) );
			} catch(Exception e){
				e.printStackTrace();
			}
			
			sla.items.add( 0, sli );
		}
		sla.notifyDataSetInvalidated();
	}
	
	public class StationListItem {
		public Station station;
		
		public String id;
		public String pname;
		public String cls;
		
		public int headerId;
	}
	
	public class StationListAdapter extends StickyListHeadersBaseAdapter {
		public List<StationListItem> items;

		public StationListAdapter(Context context) {
			super(context);
			items = new ArrayList<StationListItem>();
		}
		
		public void removeAllUnderHeader(int id){
			for(int i = 0; i < items.size(); i++){
				if(items.get(i).headerId == id){
					items.remove(i);
				}
			}
		}

		@Override
		public View getHeaderView(int position, View convertView) {
			if(convertView == null){
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_station_header, null);
			}
			
			position = items.get(position).headerId;
			
			TextView tv = (TextView) convertView;
			if(position == FAVOURITES_ID){
				tv.setText(R.string.favourites);
			} else if(position == NEARBY_ID){
				tv.setText(R.string.nearby);
			}
			
			return convertView;
		}

		@Override
		public long getHeaderId(int position) {
			return items.get(position).headerId;
		}

		@Override
		public int getCount() {
			return items.size();
		}

		@Override
		public Object getItem(int arg0) {
			return items.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}

		@Override
		protected View getView(int position, View convertView) {
			if(convertView == null){
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_station, null);
			}
			
			StationListItem sli = items.get(position);
			
			if(sli.station != null){
				((TextView)convertView.findViewById(R.id.title)).setText(sli.station.name);
				((ImageView)convertView.findViewById(R.id.icon)).setImageResource( DrawableLookup.getTransitDrawable( sli.station.type ) );
			}
			
			return convertView;
		}
		
	}
	
}
