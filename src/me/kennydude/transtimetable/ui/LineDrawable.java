package me.kennydude.transtimetable.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;

/**
 * Line for the item
 * 
 * @author kennydude
 *
 */
public class LineDrawable extends Drawable {
	Paint linePaint, trainPaint;
	boolean train = false;
	
	public LineDrawable(Context c){
		super();
		
		linePaint = new Paint();
		linePaint.setColor( c.getResources().getColor( android.R.color.holo_blue_dark ) );
		linePaint.setStrokeWidth( TabBackgroundDrawable.LINE_HEIGHT_SELECTED );
		
		trainPaint = new Paint();
		trainPaint.setColor( c.getResources().getColor( android.R.color.holo_blue_light ) );
	}
	
	@Override
	public void draw(Canvas cnv) {
		int mid = (cnv.getWidth() / 2) - ( TabBackgroundDrawable.LINE_HEIGHT_SELECTED / 2 ) ;
		cnv.drawLine( mid , 0, mid, cnv.getHeight(), linePaint);
	
		int radius = train ? 15 : 10;
		cnv.drawCircle( mid , (cnv.getHeight() / 2) , radius, train ? trainPaint : linePaint);
	}

	@Override
	public int getOpacity() {
		return 1;
	}

	@Override public void setAlpha(int arg0) {}
	@Override public void setColorFilter(ColorFilter arg0) {}

	public void setDrawTrain(boolean b) {
		train = true;
	}

}
