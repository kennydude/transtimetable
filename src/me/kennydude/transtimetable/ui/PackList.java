package me.kennydude.transtimetable.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import me.kennydude.transtimetable.R;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

public class PackList extends ListActivity {
	PackAdapter pa;

	@Override
	public void onCreate(Bundle bis){
		super.onCreate(bis);
		
		if(getListAdapter() != null) return;
		
		View header = LayoutInflater.from(this).inflate(R.layout.item_pack, null);
		header.findViewById(R.id.title).setVisibility(View.GONE);
		header.findViewById(R.id.install).setVisibility(View.GONE);
		((TextView)header.findViewById(R.id.description)).setText(R.string.pack_description);
		
		getListView().addHeaderView(header);
		
		pa = new PackAdapter(this);
		setListAdapter(pa);
		
		if(getIntent().hasExtra("homeOK")){
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
		
		getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
					long arg3) {
				if(pos == 0) return;
				
				Pack p = pa.getItem(pos - 1);
				
				// TODO: allow for non-play downloading
				Intent market = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + p.pname));
				startActivity(market);
			}
		});
		
		
		refresh();
	}
	
	@Override
	public boolean onOptionsItemSelected (MenuItem item){
		switch(item.getItemId()){
		case android.R.id.home:
			Intent intent = new Intent(this, StationList.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void refresh(){
		final ProgressDialog pd = new ProgressDialog(this);
		pd.setMessage(getString(R.string.loading));
		pd.show();
		
		new Thread(new Runnable(){

			@Override
			public void run() {
				try{
					HttpGet g = new HttpGet("http://kennydude.github.com/transtimetable/datapacks.json");
					DefaultHttpClient dhc = new DefaultHttpClient();
					HttpResponse r = dhc.execute(g);
					
					if(r.getStatusLine().getStatusCode() != 200)
						throw new Exception("List did not download properly");
					
					JSONArray ja = new JSONArray(EntityUtils.toString(r.getEntity()));
					final List<Pack> results = new ArrayList<Pack>();
					for(int i = 0; i < ja.length(); i++){
						Pack p = Pack.fromJSONObject(ja.getJSONObject(i));
						if(p.country.toLowerCase(Locale.UK).equals( Locale.getDefault().getISO3Country().toLowerCase(Locale.UK) )){
							results.add(0, p);
						} else
							results.add( p );
					}
					
					runOnUiThread(new Runnable(){

						@Override
						public void run() {
							pa.clear();
							pa.addAll(results);
							pa.notifyDataSetInvalidated();
						}
					});
					
				} catch(final Exception e){
					// TODO: Fix
					runOnUiThread(new Runnable(){

						@Override
						public void run() {
							Toast.makeText(PackList.this, e.getMessage(), Toast.LENGTH_SHORT).show();
						}
						
					});
					e.printStackTrace();
				}
				
				runOnUiThread(new Runnable(){

					@Override
					public void run() {
						pd.dismiss();
					}
				});
			}
			
		}).start();
	}
	
	public static class Pack{
		public String pname, name, description, country;

		public static Pack fromJSONObject(JSONObject jo) throws JSONException {
			Pack p = new Pack();
			p.pname = jo.getString("pname");
			p.name = jo.getString("title");
			p.description = jo.getString("description");
			p.country = jo.getString("country");
			return p;
		}
	}
	
	public class PackAdapter extends ArrayAdapter<Pack>{
		public PackAdapter(Context context) {
			super(context, 0);
		}
		
		@Override
		public View getView( int pos, View convertView, ViewGroup parent ){
			if(convertView == null){
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_pack, null);
			}
			Pack p = getItem(pos);
			
			((TextView)convertView.findViewById(R.id.title)).setText(p.name);
			((TextView)convertView.findViewById(R.id.description)).setText(p.description);
			
			return convertView;
		}
	}
	
}
