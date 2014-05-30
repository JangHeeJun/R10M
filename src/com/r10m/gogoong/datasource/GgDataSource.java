package com.r10m.gogoong.datasource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import com.r10m.gogoong.R;
import com.r10m.gogoong.component.IconMarker;
import com.r10m.gogoong.component.Marker;

public class GgDataSource extends NetworkDataSource {
	private static final String URL = "http://192.168.200.13:8080/app/json/gangnam";

	private static Bitmap icon = null;

	public GgDataSource(Resources res) {
		if (res==null) throw new NullPointerException();
		
		createIcon(res);
	}
	
	protected void createIcon(Resources res) {
		if (res==null) throw new NullPointerException();
		
		icon=BitmapFactory.decodeResource(res, R.drawable.ic_marker);
	}
	
	@Override
	public String createRequestURL(double lat, double lon, double alt,
			float radius, String locale) {
		return URL;
	}
	
	@Override
	public List<Marker> parse(String url) {
		if (url==null) throw new NullPointerException();
		
		
		
		
		InputStream is = null;
		String result = "";
		
		try {
			HttpClient httpClient = new DefaultHttpClient();
			HttpGet httpGet = new HttpGet(url);
			HttpResponse httpResponse = httpClient.execute(httpGet);
			HttpEntity httpEntity = httpResponse.getEntity();
			
			is = httpEntity.getContent();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			StringBuilder sb = new StringBuilder();
			
			String line = null;
			while((line = br.readLine()) != null){
				sb.append(line+"\n");
			}
			
			is.close();
			result = sb.toString();	
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
		
		
		
    	JSONObject json = null;
    	try {
    		json = new JSONObject(result);
    	} catch (JSONException e) {
    	    e.printStackTrace();
    	}
    	if (json==null) throw new NullPointerException();
    	
    	return parse(json);
	}
	
	@Override
	public List<Marker> parse(JSONObject root) {
		if (root==null) throw new NullPointerException();
		
		JSONObject jo = null;
		JSONArray dataArray = null;
    	List<Marker> markers=new ArrayList<Marker>();

		try {
			//if(root.has("gangnam")) 
				dataArray = root.getJSONArray("gangnam");
			//if (dataArray == null) return markers;
			int top = Math.min(MAX, dataArray.length());
			for (int i = 0; i < top; i++) {					
				jo = dataArray.getJSONObject(i);
				Marker ma = processJSONObject(jo);
				//Marker ma = processJSONObject(root);
				//if(ma!=null) 
					markers.add(ma);
			}
		} catch (JSONException e) {
		    e.printStackTrace();
		}
		return markers;
	}
	
	private Marker processJSONObject(JSONObject jo) {
		//if (jo==null) throw new NullPointerException();
		
		//if (!jo.has("gangnam")) throw new NullPointerException();
		
		Marker ma = null;
		try {
			ma = new IconMarker(
//					jo.getJSONObject("gangnam").getString("locationName"), 
//					Double.parseDouble(jo.getJSONObject("gangnam").getString("lat")), 
//					Double.parseDouble(jo.getJSONObject("gangnam").getString("lng")), 
//					Double.parseDouble(jo.getJSONObject("gangnam").getString("alt")),
					jo.getString("locationName"), 
					Double.parseDouble(jo.getString("lat")), 
					Double.parseDouble(jo.getString("lng")), 
					Double.parseDouble(jo.getString("altitude")),
					Color.RED,
					icon,
					jo.getString("locationDetail"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ma;
	}

}
