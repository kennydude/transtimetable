package me.kennydude.transtimetable.ui;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import com.emilsjolander.components.stickylistheaders.StickyListHeadersBaseAdapter;
import com.emilsjolander.components.stickylistheaders.StickyListHeadersListView;

import me.kennydude.transtimetable.R;
import me.kennydude.transtimetable.Station;
import me.kennydude.transtimetable.Utils;
import me.kennydude.transtimetable.ui.TransitActivity.LocateStationHelperCallback;
import android.app.Activity;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
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
	public int hasLocation = 1;
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
		
		PackageManager pm = getPackageManager();
		Intent intent = new Intent(Utils.TRANSIT_SERVICE_ACTION);
		if(pm.queryIntentServices(intent, 0).size() == 0){
			Intent i = new Intent(this, PackList.class);
			startActivity(i);
			finish();
		}
		
		setupStationListView(getListView(), sla, this);
	}
	
	public static void setupStationListView(ListView p, final BaseStationListAdapter sla, final Activity me){
		p.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
					long arg3) {
				StationListItem sli = sla.items.get(pos);
				
				Intent i = new Intent(me, StationView.class);
				i.putExtra("pname", sli.pname);
				i.putExtra("class", sli.cls);
				i.putExtra("station", sli.id);
				me.startActivity(i);
			}
		});
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu u){
		new MenuInflater(this).inflate(R.menu.activity_stationlist, u);
		
		if(hasLocation == 2){
			u.findItem(R.id.relocate).setIcon(R.drawable.ic_action_location_on);
		} else if(hasLocation == 1){
			ProgressBar pv = new ProgressBar(this, null,
					android.R.attr.progressBarStyleSmall);
			u.findItem(R.id.relocate).setEnabled(false).setActionView(pv);
		}
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected (MenuItem item){
		switch(item.getItemId()){
		case R.id.about:
			Intent ai = new Intent(this, AboutView.class);
			startActivity(ai);
			break;
		case R.id.packs:
			Intent i = new Intent(this, PackList.class);
			i.putExtra("homeOK", true);
			startActivity(i);
			break;
		case R.id.search:
			onSearchRequested();
			break;
		case R.id.relocate:
			hasLocation = 1;
			invalidateOptionsMenu();
			
			LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
			lm.removeUpdates(mLocationListener);
			requestLocationUpdates();
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	LocationListener mLocationListener = new LocationListener(){

		@Override
		public void onLocationChanged(final Location whereAmI) {
			// We have a new location! :D
			new Thread(new Runnable(){

				@Override
				public void run() {
					runOnUiThread(new Runnable(){

						@Override
						public void run() {
							hasLocation = 1;
							invalidateOptionsMenu();
						}
					});
					TransitActivity.locateStationHelper(StationList.this, new LocateStationHelperCallback<StationListItem>(){

						@Override
						public List<StationListItem> getStations(TransServiceBinder tsb, ComponentName cn) {
							List<Station> items = tsb.getStationsNearby( whereAmI.getLatitude(), whereAmI.getLongitude() );
							if(items == null) return null;
							return stationsToLI(items, cn, NEARBY_ID);
						}

						@Override
						public void finishLocation(final List<StationListItem> results) {
							// publish results
							runOnUiThread(new Runnable(){

								@Override
								public void run() {
									hasLocation = 2;
									invalidateOptionsMenu();
									
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
		@Override public void onStatusChanged(String arg0, int status, Bundle arg2) {
			if(status != LocationProvider.AVAILABLE){
				runOnUiThread(new Runnable(){

					@Override
					public void run() {
						hasLocation = 0;
						invalidateOptionsMenu();
					}
					
				});
			}
		}
		
	};
	
	public void onPause(){
		super.onPause();
		LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		lm.removeUpdates(mLocationListener);
	}
	
	void requestLocationUpdates(){
		LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		lm.requestLocationUpdates( 1000 * 60 * 10, 10, new Criteria(), mLocationListener, null );
	}
	
	public void onResume(){
		super.onResume();
		
		requestLocationUpdates();
		
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
	
	public static class StationListItem {
		public Station station;
		
		public String id;
		public String pname;
		public String cls;
		
		public int headerId;
	}
	
	public static List<StationListItem> stationsToLI(List<Station> items, ComponentName cn, int hI){
		ArrayList<StationListItem> r = new ArrayList<StationListItem>();
		for(Station item : items){
			StationListItem sli = new StationListItem();
			sli.headerId = hI;
			sli.id = item.id;
			sli.station = item;
			sli.pname = cn.getPackageName();
			sli.cls = cn.getClassName();
			r.add(sli);
		}
		return r;
	}
	
	public class StationListAdapter extends BaseStationListAdapter {

		public StationListAdapter(Context context) {
			super(context);
		}
		
		public void removeAllUnderHeader(int id){
			for(int i = 0; i < items.size(); i++){
				if(items.get(i).headerId == id){
					items.remove(i);
				}
			}
		}

		@Override
		public String getHeaderTitle(int position) {
			if(position == FAVOURITES_ID){
				return getString(R.string.favourites);
			} else if(position == NEARBY_ID){
				return getString(R.string.nearby);
			}
			return "";
		}
		
	}
	
}
