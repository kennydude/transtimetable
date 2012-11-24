package me.kennydude.transtimetable.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.StateSet;

public class TabBackgroundDrawable extends Drawable {
	public static final int LINE_HEIGHT_SELECTED = 9, LINE_HEIGHT_UNSELECTED = 2;
	
	Paint bluePaint, backPaint;
	int LINE_HEIGHT = LINE_HEIGHT_UNSELECTED;
	boolean background = false;
	
	@Override
	public boolean isStateful() {
		return true;
	}
	
	@Override
    protected boolean onStateChange(int[] stateSet) {
		String s = StateSet.dump(stateSet);
		
		LINE_HEIGHT = s.contains("S") ? LINE_HEIGHT_SELECTED : LINE_HEIGHT_UNSELECTED;
		bluePaint.setStrokeWidth(LINE_HEIGHT);
		
		background = s.contains("P");
		
		this.invalidateSelf();
		
		return super.onStateChange(stateSet);
	}
	
	public TabBackgroundDrawable(Context c){
		super();
		
		bluePaint = new Paint();
		bluePaint.setStrokeWidth(LINE_HEIGHT);
		bluePaint.setColor( c.getResources().getColor(android.R.color.holo_blue_dark) );
		
		backPaint = new Paint();
		backPaint.setColor( c.getResources().getColor(android.R.color.holo_blue_light) );
	}

	@Override
	public void draw(Canvas cnv) {
		if(background){
			cnv.drawRect(0, 0, cnv.getWidth(), cnv.getHeight(), backPaint);
		}
		
		cnv.drawLine(0, cnv.getHeight() - LINE_HEIGHT / 2, cnv.getWidth(), cnv.getHeight() - LINE_HEIGHT / 2, bluePaint);
	}

	@Override
	public int getOpacity() {
		return 1;
	}

	@Override public void setAlpha(int arg0) {}
	@Override public void setColorFilter(ColorFilter arg0) {}

}
