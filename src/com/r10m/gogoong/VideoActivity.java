package com.r10m.gogoong;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

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
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

public class VideoActivity extends Activity {

	/** ��Ĺ ���� �ּ� */
	//private String MOVIE_URL = "http://192.168.200.93:8080/app/video/";
	private static final String MOVIE_URL = "http://192.168.200.13:8080/app/video/Geunjeongjeon.json";
	
	//private String name;
	private Uri videoUri;
	private VideoView vv;
	private MediaController mc;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.video);
		
		/**���� �� �� �̵�� ��Ʈ�ѷ� ����*/
		vv = (VideoView) findViewById(R.id.VideoView);
		mc = new MediaController(this);
		mc.setAnchorView(vv);
		
		//Intent intent = getIntent();
		//name = intent.getExtras().getString("name");
		
		/**��Ʈ���� ������ ����*/
		new streamingVideoTask().execute("");
			
	}
	
	private class streamingVideoTask extends AsyncTask<String, Void, Void>{
		 
		@Override
		protected void onPostExecute(Void result) {
			
			/**������ ��ŸƮ*/
			vv.setMediaController(mc);
			vv.setVideoURI(videoUri);
			vv.requestFocus();
			vv.start();
		}

		@Override
		protected Void doInBackground(String... urls) {
			
			/**��Ʈ���� �ּ� �Ľ�*/
			//videoUri = Uri.parse( getJsonData(MOVIE_URL+name+".json") );
			videoUri = Uri.parse( getJsonData(MOVIE_URL) );
			return null;
		}
	      
	}
	
	/**��Ʈ���� �ּ� �Ľ� �޼ҵ�*/
	private String getJsonData(String servUrl){
		
		String videoUrl = "";
		
		InputStream is = null;
		String result = "";
		
		try {
			
			/**��û ������ ����ޱ�*/
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
			
			/**������ ��Ʈ������ ��ȯ*/
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
			
			/**���̽��� �̿��� url�Ľ�*/
			JSONObject jo = new JSONObject(result);
			videoUrl = jo.getString("videoUrl");
			
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return videoUrl;
		
	}

}
