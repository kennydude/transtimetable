package me.kennydude.transtimetable.ui;

import java.util.Calendar;

import org.json.JSONObject;

import me.kennydude.transtimetable.R;
import me.kennydude.transtimetable.TransitItem;
import me.kennydude.transtimetable.Utils;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;


public class TransitItemView extends TransitActivity {
	String tId;
	TransitItem item;
	StopAdapter sa;
	TextView lastRefreshed;
	
	void setLastRefreshed(Calendar when){
		lastRefreshed.setText( getString(R.string.last_refreshed).replace("{time}", Utils.calendarTime(when)) );
	}
	
	ListView getListView(){
		return (ListView) findViewById(R.id.list);
	}
	
	@Override
	public void onCreate(Bundle bis){
		if(getIntent().hasExtra("id") == false){
			finish();
			return;
		} else{
			tId = getIntent().getStringExtra("id");
		}
		
		super.onCreate(bis);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.activity_transititem);
		
		if(getIntent().hasExtra("transit")){
			try{
				item = TransitItem.fromJSONObject( new JSONObject( getIntent().getStringExtra("transit") ) );
				render();
			} catch(Exception e){}
		}
		
		lastRefreshed = new TextView(this);
		lastRefreshed.setPadding(15, 15, 15, 15);
		lastRefreshed.setGravity( Gravity.CENTER );
		getListView().addHeaderView(lastRefreshed);
		
		sa = new StopAdapter(this);
		getListView().setAdapter(sa);
	}
	
	// Render to controls
	void render(){
		setTitle( getString(R.string.from_to).replace("{from}", item.from).replace("{to}", item.to) );
		
		sa.clear();
		sa.addAll( item.stops );
		sa.notifyDataSetInvalidated();
		
	}

	String getCacheKey(){
		return getIntent().getStringExtra("pname") + "." + getIntent().getStringExtra("class") + "." + getIntent().getStringExtra("id");
	}
	
	@Override
	public void onConnectedToBackendService() {
		new Thread(new Runnable(){

			@Override
			public void run() {
				String cache = CacheManager.getCacheItem(TransitItemView.this, getCacheKey(), 60 * 60 * 60 * 1);
				if(cache == null){
					refresh();
				} else{
					try{
						item = TransitItem.fromJSONObject( new JSONObject( cache ) );
						runOnUiThread(new Runnable(){
	
							@Override
							public void run() {
								render();
								setLastRefreshed( CacheManager.getLastSavedCached(TransitItemView.this, getCacheKey()) );
							}
							
						});
					} catch(Exception e){
						e.printStackTrace();
						refresh();
					}
				}
			}
			
		}).start();
	}
	
	boolean refreshing = false;
	
	void setRefreshing(final boolean is){
		runOnUiThread(new Runnable(){

			@Override
			public void run() {
				refreshing = is;
				invalidateOptionsMenu();
			}
			
		});
	}
	
	@Override
	public boolean onOptionsItemSelected (MenuItem item){
		switch(item.getItemId()){
		case android.R.id.home:
			Intent intent = new Intent(this, StationList.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
			break;
		case R.id.refresh:
			refresh();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu u){
		new MenuInflater(this).inflate(R.menu.activity_transititem, u);
		if(refreshing){
			ProgressBar pv = new ProgressBar(this, null,
					android.R.attr.progressBarStyleSmall);
			u.findItem(R.id.refresh).setEnabled(false).setActionView(pv);
		}
		
		return true;
	}
	
	public void refresh(){
		setRefreshing(true);
		new Thread(new Runnable(){

			@Override
			public void run() {
				try{
					TransitItem Ttem = getTransService().getTransitItem(tId);
					if(Ttem != null){
						item = Ttem;
					}
					CacheManager.saveCache(TransitItemView.this, getCacheKey(), item.toJSONObject().toString());
					
					runOnUiThread(new Runnable(){

						@Override
						public void run() {
							setRefreshing(false);
							render();
							setLastRefreshed(Calendar.getInstance());
						}
						
					});
				} catch(Exception e){
					e.printStackTrace();
				}
			}
			
		}).start();
	}
	
	public class StopAdapter extends ArrayAdapter<TransitItem.Stop>{
		public StopAdapter(Context context) {
			super(context, 0);
		}
		
		@Override
		public View getView( int pos, View convertView, ViewGroup parent ){
			if(convertView == null){
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_transit, null);
			}
			
			TransitItem.Stop ts = getItem(pos);
			TransitItemAdapter.setStopViewData(convertView, ts, ts.where, getContext());
			
			LineDrawable d = new LineDrawable(getContext());
			if(ts.here){
				d.setDrawTrain(true);
			}
			
			((ImageView)convertView.findViewById(R.id.icon)).setLayoutParams(new LinearLayout.LayoutParams(
				Utils.convertDpToPx(getContext(), 64), LinearLayout.LayoutParams.MATCH_PARENT
			));
			((ImageView)convertView.findViewById(R.id.icon)).setImageDrawable(d);
			
			return convertView;
		}
		
	}

}
