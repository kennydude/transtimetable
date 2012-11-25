package me.kennydude.transtimetable.ui;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class AboutView extends Activity {

	@Override
	public void onCreate(Bundle bis){
		super.onCreate(bis);
		
		WebView wv = new WebView(this);
		setContentView(wv);
		
		wv.loadUrl("file:///android_asset/about.html");
	}
}
