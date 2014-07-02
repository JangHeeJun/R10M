package com.r10m.gogoong;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.MediaController;
import android.widget.VideoView;

public class VideoActivity extends Activity {

	/** 톰캣 서버 주소 */
	//private String MOVIE_URL = "http://192.168.200.93:8080/app/video/";
	private static final String MOVIE_URL = "http://mycafe24kim.cafe24.com/app/";
	
	private SharedPreferences mainPreferences;
	private static String locale = "en";
	
	private String name;
	private String kind;
	private Uri videoUri;
	private VideoView vv;
	private MediaController mc;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.video);
		
		mainPreferences = PreferenceManager.getDefaultSharedPreferences(this);	//설정내용읽어옴
    	setLocale(mainPreferences.getString("LanguageList", "ko"));	//언어설정
		
		/**비디오 뷰 세팅*/
		vv = (VideoView) findViewById(R.id.videoView);
		vv.setOnCompletionListener(new OnCompletionListener() {
			
			@Override
			public void onCompletion(MediaPlayer mp) {
				finish();
				
			}
		});
		
		/**미디어 컨트롤러 세팅*/
		mc = new MediaController(this);
		mc.setAnchorView(vv);
		
		Intent intent = getIntent();
		name = intent.getExtras().getString("name");
		kind = intent.getExtras().getString("kind");
		
		
		/**스트리밍 쓰레드 실행*/
		new streamingVideoTask().execute("");
			
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
	
	private class streamingVideoTask extends AsyncTask<String, Void, Void>{
		 
		@Override
		protected void onPostExecute(Void result) {
			
			/**비디오뷰 스타트*/
			vv.setMediaController(mc);
			vv.setVideoURI(videoUri);
			vv.requestFocus();
			vv.start();
		}

		@Override
		protected Void doInBackground(String... urls) {
			
			/**스트리밍 주소 파싱*/
			//videoUri = Uri.parse( getJsonData(MOVIE_URL+name+".json") );
			videoUri = Uri.parse( getJsonData(createRequestURL(name)) );
			return null;
		}
	
	}
	
	/**스트리밍 주소 파싱 메소드*/
	private String getJsonData(String servUrl){
		
		String videoUrl = "";
		
		InputStream is = null;
		String result = "";
		
		try {
			
			/**요청 보내고 응답받기*/
			HttpClient httpClient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(servUrl);
			HttpResponse httpResponse = httpClient.execute(httpPost);
			HttpEntity httpEntity = httpResponse.getEntity();
			
			is = httpEntity.getContent();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			
			/**응답을 스트링으로 변환*/
			BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			StringBuilder sb = new StringBuilder();
			
			String line = null;
			while((line = br.readLine()) != null){
				sb.append(line+"\n");
			}
			
			result = sb.toString();	
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		try {
			
			/**제이슨을 이용해 url파싱*/
			JSONObject jo = new JSONObject(result);
			videoUrl = jo.getString("videoUrl");
			
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return videoUrl;
		
	}

	private String createRequestURL(String name) {	
		String url = "";
		if(kind.equals("location")){
			url = MOVIE_URL+"location/video/"+locale+"/"+name+".json";
		}else if(kind.equals("beacon")){
			url = MOVIE_URL+"beacon/video/"+locale+"/"+name+".json";
		}
		Log.e("Video", "==================="+url+"=====================");
		return url;
	}
	
}
