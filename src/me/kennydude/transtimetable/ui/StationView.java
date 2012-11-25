package me.kennydude.transtimetable.ui;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.json.JSONArray;

import me.kennydude.transtimetable.R;
import me.kennydude.transtimetable.Station;
import me.kennydude.transtimetable.TransitItem;
import me.kennydude.transtimetable.Utils;
import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class StationView extends TransitActivity {
	String stationId;
	Station thisStation;
	SimpleFragmentPagerAdapter pages;
	
	void updateButtonStates(int position){
		findViewById(R.id.depart).setSelected(position == 0);
		findViewById(R.id.arrive).setSelected(position == 1);
	}
	
	@SuppressWarnings("deprecation")
	void doButton(int id, final int pos){
		Button v = (Button) findViewById(id);
		v.setBackgroundDrawable(new TabBackgroundDrawable(this));
		v.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View me) {
				ViewPager vp = (ViewPager)findViewById(R.id.viewPager);
				vp.setCurrentItem(pos);
				updateButtonStates(pos);
			}
		});
	}

	@Override
	public void onCreate(Bundle bis){
		if(getIntent().hasExtra("station") == false){
			finish();
			return;
		} else{
			stationId = getIntent().getStringExtra("station");
		}
		
		super.onCreate(bis);
		setContentView(R.layout.activity_stationview);
		
		findViewById(R.id.depart).setSelected(true);
		doButton(R.id.depart, 0);
		doButton(R.id.arrive, 1);
		
		findViewById(R.id.travel_update).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				AlertDialog.Builder ab = new AlertDialog.Builder(StationView.this);
				ab.setTitle(R.string.travel_update);
				ab.setMessage( ((TextView)v).getText());
				ab.setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dlg, int arg1) {
						dlg.dismiss();
					}
				});
				
				ab.show();
			}
		});
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		pages = new SimpleFragmentPagerAdapter(this);
		ViewPager vp = (ViewPager)findViewById(R.id.viewPager);
		vp.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int position) {
				updateButtonStates(position);
				getCurrentItem().onDisplay();
				invalidateOptionsMenu();
			}
			
			@Override public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
			@Override public void onPageScrollStateChanged(int state) {}
		});
	}
	
	FavouriteManager.Favourite getFavourite(){
		return FavouriteManager.getFavourite(this, getIntent().getStringExtra("pname"), getIntent().getStringExtra("class"), stationId);
	}
	
	BaseBoardFragment getCurrentItem(){
		ViewPager vp = (ViewPager)findViewById(R.id.viewPager);
		return ((BaseBoardFragment)pages.getLiveItem( vp.getCurrentItem() ));
	}
	
	@Override
	public boolean onOptionsItemSelected (MenuItem item){
		switch(item.getItemId()){
		case R.id.refresh:
			getCurrentItem().reloadData();
			return true;
		case android.R.id.home:
			Intent intent = new Intent(this, StationList.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            return true;
		case R.id.favourite:
			getFavourite().toggleFavourite();
			invalidateOptionsMenu();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void showTravelUpdates(final String updates){
		runOnUiThread(new Runnable(){

			@Override
			public void run() {
				if(updates.isEmpty()){
					findViewById(R.id.travel_update).setVisibility(View.GONE);
				} else{
					TextView tv = (TextView) findViewById(R.id.travel_update);
					tv.setText(updates);
					tv.setVisibility(View.VISIBLE);
					tv.setSelected(true);
				}
			}
			
		});
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu u){
		new MenuInflater(this).inflate(R.menu.activity_stationview, u);
		
		boolean refreshing = true;
		try{
			refreshing = getCurrentItem().isRefreshing;
		} catch(Exception e){} // Will failover if fragments are not loaded yet
		
		findViewById(R.id.loading).setVisibility( refreshing ? View.VISIBLE : View.GONE );
		if(refreshing){
			ProgressBar pv = new ProgressBar(this, null,
					android.R.attr.progressBarStyleSmall);
			u.findItem(R.id.refresh).setEnabled(false).setActionView(pv);
		}
		
		if(getFavourite().isFavourite()){
			u.findItem(R.id.favourite).setIcon(R.drawable.ic_action_favourite);
		}
		
		
		return true;
	}

	@Override
	public void onConnectedToBackendService() {
		new Thread(new Runnable(){

			@Override
			public void run() {
				thisStation = getTransService().getStationDetails(stationId);
				CacheManager.saveCache(StationView.this, "station-" + stationId, thisStation.toJSONObject().toString());
				
				String travel = CacheManager.getCacheItem(StationView.this, stationId + "-station-tu");
				if(travel != null)
					showTravelUpdates(travel);
				
				runOnUiThread(new Runnable(){

					@Override
					public void run() {
						setTitle(thisStation.name);
						
						// Setup ViewPager Adapter
						ViewPager vp = (ViewPager)findViewById(R.id.viewPager);
						pages.addItem(DepartureFragment.class);
						pages.addItem(ArrivalFragment.class);
						vp.setAdapter( pages );
						
						getCurrentItem().onDisplay();
					}
					
				});
			}
			
		}).start();
	}
	
	public static abstract class BaseBoardFragment extends ListFragment{
		public abstract List<TransitItem> getItems();
		public abstract String getCacheKey();
		
		TextView lastRefreshed;
		TransitItemAdapter tia;
		public boolean isRefreshing = false;
		
		void setLastRefreshed(Calendar when){
			lastRefreshed.setText( getString(R.string.last_refreshed).replace("{time}", Utils.calendarTime(when)) );
		}
		
		public void setRefreshing(boolean is){
			isRefreshing = is;
			getActivity().invalidateOptionsMenu();
		}
		
		@Override
		public void onStart(){
			super.onStart();
			if(getListAdapter() != null) return;
			
			setListShown(false);
			
			lastRefreshed = new TextView(getActivity());
			lastRefreshed.setPadding(15, 15, 15, 15);
			lastRefreshed.setGravity( Gravity.CENTER );
			getListView().addHeaderView(lastRefreshed);
			
			getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> view, View arg1,
						int pos, long arg3) {
					if(pos == 0) return; // header
					TransitItem ti = (TransitItem) view.getItemAtPosition(pos);
					
					Intent i = new Intent(getActivity(), TransitItemView.class);
					i.putExtra("id", ti.id);
					i.putExtra("transit", ti.toJSONObject().toString());
					i.putExtra("pname", getActivity().getIntent().getStringExtra("pname"));
					i.putExtra("class", getActivity().getIntent().getStringExtra("class"));
					
					startActivity(i);
				}
			});
			
			setEmptyText(getString(R.string.empty));
			tia = new TransitItemAdapter(getActivity(), 0);
			setListAdapter(tia);
		}
		
		public TransServiceBinder getTransService(){
			return ((TransitActivity)getActivity()).getTransService();
		}
		public Station getThisStation(){
			return ((StationView)getActivity()).thisStation;
		}
		
		public void onDisplay(){
			if(tia.getCount() == 0){
				new Thread(new Runnable(){

					@Override
					public void run() {
						String cache = CacheManager.getCacheItem(getActivity(), getCacheKey(), 60 * 60 * 60 * 1);
						if(cache == null){
							getActivity().runOnUiThread(new Runnable(){

								@Override
								public void run() {
									reloadData();
								}
							});
						} else{
							// Deserialize and show
							try{
								JSONArray ja = new JSONArray(cache);
								final List<TransitItem> items = new ArrayList<TransitItem>();
								for(int i = 0; i < ja.length(); i ++){
									items.add( TransitItem.fromJSONObject(ja.getJSONObject(i)) );
								}
								
								
								getActivity().runOnUiThread(new Runnable(){

									@Override
									public void run() {
										setRefreshing(false);
										setLastRefreshed( CacheManager.getLastSavedCached(getActivity(), getCacheKey()) );
										tia.addAll(items);
										tia.notifyDataSetChanged();
									}
								});
								
							} catch(Exception e){
								e.printStackTrace();
							}
						}
					}
					
				}).start();
			}
		}
		
		public void reloadData(){
			setRefreshing(true);
			new Thread(new Runnable(){

				@Override
				public void run() {
					try{
						final List<TransitItem> items = getItems();
						
						if(items == null){
							getActivity().runOnUiThread(new Runnable(){
	
								@Override
								public void run() {
									setRefreshing(false);
								}
							});
							return;
						}
						
						// Save Cache
						JSONArray ja = new JSONArray();
						for(TransitItem ti : items){
							ja.put(ti.toJSONObject());
						}
						CacheManager.saveCache( getActivity(), getCacheKey(), ja.toString() );
						
						String travel = getTransService().getStatusUpdate(getThisStation().id);
						((StationView)getActivity()).showTravelUpdates( travel );
						CacheManager.saveCache(getActivity(), getThisStation().id + "-station-tu", travel);
						
						getActivity().runOnUiThread(new Runnable(){
	
							@Override
							public void run() {
								tia.clear();
								if(items != null){
									tia.addAll(items);
								}
								setLastRefreshed( Calendar.getInstance() );
								setRefreshing(false);
								setListShown(true);
								tia.notifyDataSetInvalidated();
							}
							
						});
						
					} catch(Exception e){
						e.printStackTrace();
					}
				}
				
			}).start();
		}
	}
	
	public static class DepartureFragment extends BaseBoardFragment{

		@Override
		public List<TransitItem> getItems() {
			return getTransService().getDepartures(getThisStation().id);
		}

		@Override
		public String getCacheKey() {
			return "depart-" + getThisStation().id;
		}
		
	}
	
	public static class ArrivalFragment extends BaseBoardFragment{

		@Override
		public List<TransitItem> getItems() {
			return getTransService().getArrivals(getThisStation().id);
		}

		@Override
		public String getCacheKey() {
			return "arrive-" + getThisStation().id;
		}
		
	}
	
}
