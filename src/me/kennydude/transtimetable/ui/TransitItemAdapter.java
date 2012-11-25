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
		
		String h = it.to;
		
		setStopViewData(convertView, thisStop, h, getContext());
		((ImageView)convertView.findViewById(R.id.icon)).setImageResource( DrawableLookup.getTransitDrawable( it.type ) );
		
		return convertView;
	}
	
	public static void setStopViewData(View convertView, TransitItem.Stop thisStop, String h, Context c){
		
		if(thisStop.status == TransitItem.Stop.Status.LATE && thisStop.delayedUntil != null){
			h += "<br/><small>" +
					c.getString(R.string.arrives_at_late)
						.replace("{time}", Utils.friendlyTimeShort(thisStop.delayedUntil))
						.replace("{brick}", Utils.calendarTime(thisStop.delayedUntil))
						.replace("{meant}", Utils.calendarTime(thisStop.when))
					+ "</small>";
		} else{
			h += "<br/><small>" +
				c.getString(R.string.arrives_at)
					.replace("{time}", Utils.friendlyTimeShort(thisStop.when))
					.replace("{brick}", Utils.calendarTime(thisStop.when))
				+ "</small>";
		}
		((TextView)convertView.findViewById(R.id.destination)).setText( Html.fromHtml( h ) );
		
		if(thisStop.where != null && !thisStop.where.isEmpty()){
			TextView tv = (TextView)convertView.findViewById(R.id.where);
			tv.setVisibility(View.VISIBLE);
			String text = "";
			switch(thisStop.whereType){
			case PLATFORM:
				text = "<small>" + c.getString(R.string.platform) + "</small><br/>";
				break;
			case STOP:
				text = "<small>" + c.getString(R.string.stop) + "</small><br/>";
			default: break;
			}
			text += thisStop.platform;
			
			tv.setText(Html.fromHtml(text));
		} else{
			convertView.findViewById(R.id.where).setVisibility(View.GONE);
		}
		
		TextView status = (TextView) convertView.findViewById(R.id.status);
		switch(thisStop.status){
		case CANCELED:
			status.setText(R.string.canceled);
			status.setBackgroundColor(c.getResources().getColor(android.R.color.darker_gray));
			break;
		case LATE:
			String s = "";
			if(thisStop.delayedUntil != null){
				s += "\n" + Utils.friendlyTimeShort(thisStop.delayedUntil, thisStop.when);
			}
			status.setText(c.getString(R.string.late) + s);
			status.setBackgroundColor(c.getResources().getColor(android.R.color.holo_red_dark));
			break;
		case ON_TIME:
			status.setText(R.string.on_time);
			status.setBackgroundColor(c.getResources().getColor(android.R.color.holo_green_dark));
			break;
		case PAST:
			status.setText(R.string.gone);
			status.setBackgroundColor(c.getResources().getColor(android.R.color.darker_gray));
			break;
		case STARTS_HERE:
			status.setText(R.string.starts_here);
			status.setBackgroundColor(c.getResources().getColor(android.R.color.holo_green_dark));
			break;
		case UNKNOWN:
			break;
		}
	}
	
}