package me.kennydude.transtimetable.ui;

import me.kennydude.transtimetable.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.ProgressBar;

@SuppressLint("SetJavaScriptEnabled")
public class AboutView extends Activity {

	@Override
	public void onCreate(Bundle bis){
		super.onCreate(bis);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		WebView wv = new WebView(this);
		setContentView(wv);
		
		wv.getSettings().setJavaScriptEnabled(true);
		wv.getSettings().setBuiltInZoomControls(false);
		wv.getSettings().setSupportZoom(false);
		wv.loadUrl("file:///android_asset/about.html");
	}
	
	@Override
	public boolean onOptionsItemSelected (MenuItem item){
		switch(item.getItemId()){
		case android.R.id.home:
			Intent intent = new Intent(this, StationList.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
			break;
		case R.id.share:
			Intent share = new Intent(android.content.Intent.ACTION_SEND);
			share.setType("text/plain");
			share.putExtra(android.content.Intent.EXTRA_TEXT, "http://kennydude.github.com/transtimetable");
			
			startActivity(Intent.createChooser(share, getString(R.string.share_app)));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu u){
		new MenuInflater(this).inflate(R.menu.activity_about, u);		
		return true;
	}
	
}
