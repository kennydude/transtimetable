package me.kennydude.transtimetable.data.london;

import me.kennydude.transtimetable.TransService;
import me.kennydude.transtimetable.Utils;

public abstract class LondonTransitService extends TransService {

	/**
	 * Should tell you if you are in fact inside London or not
	 * 
	 * The lat/lons inside should cover 100% of London + a bit extra, but I don't know
	 * 
	 */
	public boolean isInLondon( double lat, double lon ){
		return Utils.isContained(51.765, -0.699, 51.342, 0.405, lat, lon);
	}

}
