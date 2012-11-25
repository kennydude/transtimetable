package me.kennydude.transtimetable.ui;

import java.util.ArrayList;
import java.util.List;

import com.emilsjolander.components.stickylistheaders.StickyListHeadersListView;

import me.kennydude.transtimetable.R;
import me.kennydude.transtimetable.Station;
import me.kennydude.transtimetable.ui.StationList.StationListItem;
import me.kennydude.transtimetable.ui.TransitActivity.LocateStationHelperCallback;
import android.app.ActionBar;
import android.app.Activity;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SearchView;

public class SearchStations extends Activity {
	StationListAdapter sla;
	String query;
	
	StickyListHeadersListView getListView(){
		return (StickyListHeadersListView) findViewById(android.R.id.list);
	}
	
	@Override
	public boolean onOptionsItemSelected (MenuItem item){
		switch(item.getItemId()){
		case android.R.id.home:
			Intent intent = new Intent(this, StationList.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            return true;
		}
		return super.onOptionsItemSelected(item);
	};
	
	@Override
	public boolean onCreateOptionsMenu(Menu u){
		new MenuInflater(this).inflate(R.menu.activity_searchstations, u);
		
		SearchView sv = (SearchView) u.findItem(R.id.search).getActionView();
		sv.setIconifiedByDefault(false);
		SearchManager sm = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		sv.setSearchableInfo(sm.getSearchableInfo(getComponentName()));
		sv.setQuery(query, false);
		
		return true;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_stationlist);
		setTitle("");
		getActionBar().setDisplayOptions( ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_HOME );
		
		sla = new StationListAdapter(this);
		StationList.setupStationListView(getListView(), sla, this);
		getListView().setAdapter(sla);
		
		onNewIntent(getIntent());
	}
	
	@Override
	public void onNewIntent(Intent intent){
		// Get the intent, verify the action and get the query
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			query = intent.getStringExtra(SearchManager.QUERY);
			doSearch();
		}
	}
	
	void doSearch(){
		// TODO: Loading box?
		final PackageManager pm = getPackageManager();
		
		TransitActivity.locateStationHelper(this, new LocateStationHelperCallback<StationListItem>(){

			@Override
			public List<StationListItem> getStations(TransServiceBinder tsb, ComponentName cn) {
				List<Station> items = tsb.searchStations(query);
				if(items == null) return null;
				try{
					sla.headers.add( pm.getServiceInfo(cn, 0).loadLabel(pm).toString() );
				} catch(Exception e){ sla.headers.add("E?"); }
				return StationList.stationsToLI(items, cn, sla.headers.size() - 1);
			}

			@Override
			public void finishLocation(final List<StationListItem> results) {
				runOnUiThread(new Runnable(){

					@Override
					public void run() {
						invalidateOptionsMenu();
						
						sla.items.clear();
						sla.items.addAll(sla.items.size(), results);
						sla.notifyDataSetChanged();
					}
					
				});
			}
			
		});
	}
	
	public class StationListAdapter extends BaseStationListAdapter{
		public List<String> headers;
		
		public StationListAdapter(Context c) {
			super(c);
			headers = new ArrayList<String>();
		}

		@Override
		public String getHeaderTitle(int position) {
			return headers.get(position);
		}
		
	}
	
}
