package me.kennydude.transtimetable.ui;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Calendar;
import java.util.Date;

import android.content.Context;

/**
 * I manage the cache with my life! :D
 * 
 * @author kennydude
 *
 */
public class CacheManager {

	public static String getCacheItem(Context c, String key){
		return getCacheItem(c, key, -1);
	}
	
	public static String getCacheItem(Context c, String key, long expiryInSeconds){
		try{
			File cacheFile = new File(c.getCacheDir(), key.replace("/", "_") + ".cache");
			if(!cacheFile.exists()) return null;
			if(expiryInSeconds != -1 && cacheFile.lastModified() < new Date().getTime() - (expiryInSeconds * 1000)) return null;
			
			// Cache is valid! Yay!
			BufferedReader r = new BufferedReader(new FileReader( cacheFile ));
			String line = null, text = "";
			
			while( (line = r.readLine()) != null ){
				text += line + "\n";
			}
			
			r.close();
			return text;
		} catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}

	public static void saveCache(Context c, String key, String string) {
		try{
			File cacheFile = new File(c.getCacheDir(), key.replace("/", "_") + ".cache");
			if(!cacheFile.exists()){
				cacheFile.getParentFile().mkdirs();
				cacheFile.createNewFile();
			}
			
			BufferedWriter bf = new BufferedWriter( new FileWriter( cacheFile ));
			bf.write(string);
			bf.close();
			
		} catch(Exception e){
			e.printStackTrace();
		}
	}

	public static Calendar getLastSavedCached(Context c, String key) {
		File cacheFile = new File(c.getCacheDir(), key.replace("/", "_") + ".cache");
		Calendar r = Calendar.getInstance();
		r.setTimeInMillis(cacheFile.lastModified());
		return r;
	}
	
}
