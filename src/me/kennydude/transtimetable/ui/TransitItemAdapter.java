package me.kennydude.transtimetable.ui;

import me.kennydude.transtimetable.R;
import me.kennydude.transtimetable.TransitItem;
import me.kennydude.transtimetable.Utils;
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class TransitItemAdapter extends ArrayAdapter<TransitItem>{
	public TransitItemAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
	}
	
	@Override
	public View getView( int pos, View convertView, ViewGroup parent ){
		if(convertView == null){
			convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_transit, null);
		}
		TransitItem it = getItem(pos);
		TransitItem.Stop thisStop = it.getPreferedStop();
		
		String h = it.to + "<br/><small>" +
				getContext().getString(R.string.arrives_at)
					.replace("{time}", Utils.friendlyTimeShort(thisStop.when))
					.replace("{brick}", Utils.calendarTime(thisStop.when))
				+ "</small>";
		((TextView)convertView.findViewById(R.id.destination)).setText( Html.fromHtml( h ) );
		
		TextView status = (TextView) convertView.findViewById(R.id.status);
		switch(thisStop.status){
		case CANCELED:
			status.setText(R.string.canceled);
			status.setBackgroundColor(getContext().getResources().getColor(android.R.color.darker_gray));
			break;
		case LATE:
			status.setText(R.string.late);
			status.setBackgroundColor(getContext().getResources().getColor(android.R.color.holo_red_dark));
			break;
		case ON_TIME:
			status.setText(R.string.on_time);
			status.setBackgroundColor(getContext().getResources().getColor(android.R.color.holo_green_dark));
			break;
		case PAST:
			status.setText(R.string.gone);
			status.setBackgroundColor(getContext().getResources().getColor(android.R.color.darker_gray));
			break;
		case STARTS_HERE:
			status.setText(R.string.starts_here);
			status.setBackgroundColor(getContext().getResources().getColor(android.R.color.holo_green_dark));
			break;
		case UNKNOWN:
			break;
		}
		
		((ImageView)convertView.findViewById(R.id.icon)).setImageResource( DrawableLookup.getTransitDrawable( it.type ) );
		
		return convertView;
	}
	
}