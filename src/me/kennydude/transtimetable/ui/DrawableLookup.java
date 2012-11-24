package me.kennydude.transtimetable.ui;

import me.kennydude.transtimetable.R;
import me.kennydude.transtimetable.TransitItem.TransitType;

public class DrawableLookup {

	public static int getTransitDrawable(TransitType tt){
		switch(tt){
		case TRAIN:
			return R.drawable.ic_train_icon;
		case BUS:
			return R.drawable.ic_bus_icon;
		default:
			// Default, should have an unknown icon
			return R.drawable.ic_train_icon;
		}
	}
	
}
