package me.kennydude.transtimetable;

import java.util.Calendar;
import java.util.Date;

public class Utils {
	public static final String TRANSIT_SERVICE_ACTION = "me.kennydude.TRANSIT_INFORMATION";
	
	public static String getOrderBySQL( double lat, double lon ){
		// Thanks http://stackoverflow.com/a/7472230/230419
		double fudge = Math.pow( lat, 2 );
		return "(( "+ lat+ " - lat) * ("+lat+" - lat) + ("+lon+" - lon) * ("+lon+" - lon) * "+fudge+")";
	}
	
	static String dp(int in){
		String r = Integer.toString(in);
		if(r.length() == 1){
			r = "0" + r;
		}
		return r;
	}
	
	public static String calendarTime(Calendar when){
		return dp(when.get(Calendar.HOUR_OF_DAY)) + ":" + dp(when.get(Calendar.MINUTE));
	}
	
	public static String friendlyTimeShort(Calendar when){
		return friendlyTimeShort(when.getTime());
	}
	
	// Time until when
	public static String friendlyTimeShort(Date when) {
		Date now = new Date();
		long diff =  when.getTime() - now.getTime();
		if (diff <= 60000) {
			long seconds = (diff / 6000);
			return Long.toString(seconds) + "s";
		} else if (diff <= 3600000) {
			return Long.toString(diff / 60000) + "m";
		} else if (diff <= 86400000) {
			return Long.toString(diff / 3600000) + "h";
		} else if (diff <= 604800000) {
			return Long.toString(diff / 86400000) + "d";
		} else
			return Long.toString(diff / 604800000) + "w";
	}

	public static String saveCalendar(Calendar when) {
		if(when == null) return "";
		return when.get(Calendar.HOUR_OF_DAY) + ":" + when.get(Calendar.MINUTE) + ":" + when.get(Calendar.SECOND)
				+ " " + when.get(Calendar.DAY_OF_MONTH) + "/" + when.get(Calendar.MONTH) + "/" + when.get(Calendar.YEAR);
	}
	
	public static Calendar readCalendar(String when){
		Calendar c = Calendar.getInstance();
		String[] parts = when.split(" ");
		
		String[] time = parts[0].split(":");
		c.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time[0]));
		c.set(Calendar.MINUTE, Integer.parseInt(time[1]));
		c.set(Calendar.SECOND, Integer.parseInt(time[2]));
		
		time = parts[1].split("/");
		c.set(Calendar.DAY_OF_MONTH, Integer.parseInt(time[0]));
		c.set(Calendar.MONTH, Integer.parseInt(time[1]));
		c.set(Calendar.YEAR, Integer.parseInt(time[2]));
		
		return c;
	}
}
