package com.r10m.gogoong;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.r10m.gogoong.component.Marker;
import com.r10m.gogoong.datasource.GgDataSource;
import com.r10m.gogoong.datasource.LocalDataSource;
import com.r10m.gogoong.datasource.NetworkDataSource;

/** 모든 액티비티를 상속받은 CameraActivity */
public class CameraActivity extends AugmentedActivity {
	private static final String TAG = "CameraActivity";
    private static String locale = "en";
    private static final BlockingQueue<Runnable> queue = new SynchronousQueue<Runnable>();//new ArrayBlockingQueue<Runnable>(1);
    private static final ThreadPoolExecutor exeService = new ThreadPoolExecutor(1, Integer.MAX_VALUE, 20, TimeUnit.SECONDS, queue);
	private static final Map<String,NetworkDataSource> sources = new ConcurrentHashMap<String,NetworkDataSource>();
	private SharedPreferences mainPreference;
	
	/**beacon field*/
	private static final Region ALL_ESTIMOTE_BEACONS_REGION = new Region("rid", null, null, null);
	
	private BeaconManager bm;
	private Button beaconButton;
	private LinearLayout beaconLayout;
	
	private static final String URL = "http://192.168.200.93:8080/app/beacon/";
	private ArrayList<Beacon> beacons = new ArrayList<Beacon>();
	private String regionName;
	private String regionDetail;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mainPreference = PreferenceManager.getDefaultSharedPreferences(this);	//설정내용읽어옴
    	setLocale(mainPreference.getString("LanguageList", "ko"));	//언어설정

        
        LocalDataSource localData = new LocalDataSource(this.getResources());
        ARData.addMarkers(localData.getMarkers());
        
        NetworkDataSource gG = new GgDataSource(this.getResources());
        sources.put("gG",gG);
        
        Drawable alpha = ((ImageView)findViewById(R.id.imageView_camera_map)).getDrawable();
        alpha.setAlpha(50);
        
        setBeacon();
        
    }
	
	//언어 설정
    public void setLocale(String character) {
    	locale = character;
    	Locale locale = new Locale(character); 
    	Locale.setDefault(locale);
    	Configuration config = new Configuration();
    	config.locale = locale;
    	getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
    }
	
	
	@Override
    public void onStart() {
        super.onStart();
        
        Location last = ARData.getCurrentLocation();
        updateData(last.getLatitude(),last.getLongitude(),last.getAltitude());
        
        /**beacon start ranging*/
        bm.connect(new BeaconManager.ServiceReadyCallback() {
			@Override
			public void onServiceReady() {
				try {
					bm.startRanging(ALL_ESTIMOTE_BEACONS_REGION);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		});
    }
	
	@Override
	protected void onStop() {
		
		/**beacon stop ranging*/
		try {
			bm.stopRanging(ALL_ESTIMOTE_BEACONS_REGION);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		super.onStop();
	}
	
	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
		
		/**beacon finish ranging*/
		bm.disconnect();
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.v(TAG, "onOptionsItemSelected() item="+item);
        switch (item.getItemId()) {
            case R.id.showRadar:
                showRadar = !showRadar;
                item.setTitle(((showRadar)? "Hide" : "Show")+" Radar");
                break;
            case R.id.showZoomBar:
                showZoomBar = !showZoomBar;
                item.setTitle(((showZoomBar)? "Hide" : "Show")+" Zoom Bar");
                zoomLayout.setVisibility((showZoomBar)?LinearLayout.VISIBLE:LinearLayout.GONE);
                break;
            case R.id.exit:
                finish();
                break;
        }
        return true;
    }

	@Override
    public void onLocationChanged(Location location) {
        super.onLocationChanged(location);
        
        updateData(location.getLatitude(),location.getLongitude(),location.getAltitude());
    }

	@Override
	protected void markerTouched(Marker marker) {
        
        Intent intent = new Intent(this, DetailPopUp.class);
        intent.putExtra("name", marker.getName());
        intent.putExtra("detail", marker.getDetail());
		startActivity(intent);
	}

    @Override
	protected void updateDataOnZoom() {
	    super.updateDataOnZoom();
        Location last = ARData.getCurrentLocation();
        //updateData(last.getLatitude(),last.getLongitude(),last.getAltitude());
	}
    
    private void updateData(final double lat, final double lon, final double alt) {
        try {
            exeService.execute(
                new Runnable() {
                    
                    public void run() {
                    	//Marker가 적을때 다운로드 받음 - 맨 처음만 받음
                    	//if(ARData.getMarkersSize()<1)
		                    for (NetworkDataSource source : sources.values())
		                        download(source, lat, lon, alt);
                    }
                }
            );
        } catch (RejectedExecutionException rej) {
            Log.w(TAG, "Not running new download Runnable, queue is full.");
        } catch (Exception e) {
            Log.e(TAG, "Exception running download Runnable.",e);
        }
    }
    
    // 서버로 부터 데이터를 받아 마커로 변환후 ARData에 저장
    private static boolean download(NetworkDataSource source, double lat, double lon, double alt) {
		if (source==null) return false;
		
		String url = null;
		try {
			url = source.createRequestURL(lat, lon, alt, ARData.getRadius(), locale);    	
		} catch (NullPointerException e) {
			return false;
		}
    	
		List<Marker> markers = null;
		try {
			markers = source.parse(url);
		} catch (NullPointerException e) {
			return false;
		}
		ARData.clearMarkers();
    	ARData.addMarkers(markers);
    	return true;
    }
    
    /**beacon method*/
    private void setBeacon(){
    	
        beaconLayout = new LinearLayout(this);
        beaconLayout.setOrientation(LinearLayout.HORIZONTAL);
        
        beaconButton = new Button(this);
        beaconButton.setBackgroundResource(R.drawable.button_selector);
        beaconLayout.addView(beaconButton, new LayoutParams(100, 100));
        
        FrameLayout.LayoutParams frameLayoutParams = new FrameLayout.LayoutParams(
        		LayoutParams.WRAP_CONTENT, 
                LayoutParams.MATCH_PARENT, 
                Gravity.LEFT);
        frameLayoutParams.setMarginStart(300);
        addContentView(beaconLayout, frameLayoutParams);
        
        beaconButton.setVisibility(Button.INVISIBLE);
        beaconButton.setEnabled(false);
        beaconButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(!(regionName == null || regionDetail == null)){
					Intent intent = new Intent(CameraActivity.this, DetailPopUp.class);
					intent.putExtra("name", regionName);
					intent.putExtra("detail", regionDetail);
					startActivity(intent);
				}
			}
		});
        
        bm = new BeaconManager(this);
		bm.setRangingListener(new BeaconManager.RangingListener() {
			@Override
			public void onBeaconsDiscovered(Region region, final List<Beacon> beacons) {
				
				setBeaconButton(beacons);
				
			}
		});
		
	}
    
    private void setBeaconButton(List<Beacon> beacons){
		
		Log.e("beacons size ================= ", beacons.size()+"");
		
		if( !(beacons.isEmpty()) ){
			Log.e("beacons size ================= ", beacons.size()+"");
			this.beacons.clear();
			this.beacons.addAll(beacons);
			
			if(beaconButton.getVisibility() == Button.INVISIBLE){
				Log.e("", "=================VISIBLE==================");
				beaconButton.setVisibility(Button.VISIBLE);
				Log.e("getVisibility", (beaconButton.getVisibility())+"");
				beaconButton.setEnabled(true);
				
				new BeaconTask().execute();
			}
		}else{
			Log.e("", "=================INVISIBLE==================");
			beaconButton.setVisibility(Button.INVISIBLE);
			Log.e("getVisibility", (beaconButton.getVisibility())+"");
	        beaconButton.setEnabled(false);
		}
		
	}
    
    class BeaconTask extends AsyncTask<Void, Void, Void>{
		
		@Override
		protected void onPostExecute(Void result) {
			
		}

		@Override
		protected Void doInBackground(Void... params) {
			
			downloadBeaconData(beacons.get(0));

			return null;
		}
		
	}
	
	private void downloadBeaconData(Beacon beacon){
		
		InputStream is = null;
		String result = null;

		try {
			HttpClient httpClient = new DefaultHttpClient();
			HttpGet httpGet = new HttpGet(
					createRequestURL(beacon.getProximityUUID(), beacon.getMajor(), beacon.getMinor(),
							(PreferenceManager.getDefaultSharedPreferences(this)).getString("LanguageList", "ko")));
			HttpResponse httpResponse = httpClient.execute(httpGet);
			HttpEntity httpEntity = httpResponse.getEntity();

			is = httpEntity.getContent();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		if (is == null)
			throw new NullPointerException();

		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(is,
					"UTF-8"));
			StringBuilder sb = new StringBuilder();

			String line = null;
			while ((line = br.readLine()) != null) {
				sb.append(line + "\n");
			}
			result = sb.toString();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		if(result == null)
			throw new NullPointerException();
		
		try {
			
			Log.e("result", result);
			
			JSONObject jo = new JSONObject(result);
			regionName = jo.getString("regionName");
			regionDetail = jo.getString("regionDetail");
			
			Log.e("regionName", regionName);
			Log.e("regionDetail", regionDetail);
			
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	private String createRequestURL(String uuid, int major, int minor, String locale) {
		return URL+(locale.equals("ko")? 
				"kr/"+uuid+"/"+major+"/"+minor+".json"
				:"eng/"+uuid+"/"+major+"/"+minor+".json");
	}
    
}
