package me.kennydude.transtimetable.ui;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;

public class FavouriteManager {
	static final String PREF_NAME = "favs";
	
	static SharedPreferences getPrefs(Context c){
		return c.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
	}
	
	static String getPName(String pname, String cls, String id){
		return cls + "@" + pname + "/" + id;
	}
	
	public static Favourite getFavourite(Context c, String pname, String cls, String id){
		return new Favourite( c, pname, cls, id );
	}
	
	public static class FavId{
		public String id, cls, pname;
	}
	
	public static List< FavId > getFavourites( Context c ){
		List<FavId> r = new ArrayList<FavId>();
		SharedPreferences sp = getPrefs(c);
		
		for( String key :  sp.getAll().keySet() ){
			FavId f = new FavId();
			f.id = key.split("/")[1];
			f.cls = key.split("@")[0];
			f.pname = key.split("@")[1].split("/")[0];
			r.add(f);
		}
		
		return r;
	}
	
	public static class Favourite{
		String prefName;
		Context c;
		
		public Favourite(Context c, String pname, String cls, String id) {
			this.c = c;
			this.prefName = getPName(pname, cls, id);
		}
		
		public boolean isFavourite(){
			return getPrefs(c).contains(prefName);
		}
		
		public void toggleFavourite(){
			if(isFavourite()){
				removeFavourite();
			} else{
				addFavourite();
			}
		}
		
		public void removeFavourite(){
			getPrefs(c).edit().remove(prefName).commit();
		}
		
		public void addFavourite(){
			getPrefs(c).edit().putBoolean(prefName, true).commit();
		}
		
	}
}
