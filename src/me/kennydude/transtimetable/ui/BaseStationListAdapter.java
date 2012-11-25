package me.kennydude.transtimetable.ui;

import java.util.ArrayList;
import java.util.List;

import com.emilsjolander.components.stickylistheaders.StickyListHeadersBaseAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import me.kennydude.transtimetable.R;
import me.kennydude.transtimetable.ui.StationList.StationListItem;

public abstract class BaseStationListAdapter extends StickyListHeadersBaseAdapter {
	public List<StationListItem> items;

	public BaseStationListAdapter(Context c){
		super(c);
		items = new ArrayList<StationListItem>();
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
	
	public abstract String getHeaderTitle(int position);
	
	@Override
	public View getHeaderView(int position, View convertView) {
		if(convertView == null){
			convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_station_header, null);
		}
		
		position = items.get(position).headerId;
		
		((TextView) convertView).setText(getHeaderTitle(position));
		
		
		return convertView;
	}
	
}
