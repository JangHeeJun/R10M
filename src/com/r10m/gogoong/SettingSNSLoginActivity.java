package com.r10m.gogoong;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.facebook.Session;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class SettingSNSLoginActivity extends Activity {
	ImageView profileImg;
	TextView name;
	Button logout;
	Intent intent;
	String target;
	
	Bitmap profileBitmap;
	private ProgressBar progBar;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.setting_sns);
		Log.e("SNS", "===========================");
		// 전 Activity에서 data 받기
		intent = getIntent();
		target = intent.getExtras().getString("target");
		
		//프로필 사진
		profileImg = (ImageView)findViewById(R.id.imageView_profile);
		//본인 아이디
		name = (TextView)findViewById(R.id.textView_profile);
		
		// progress bar
		progBar = (ProgressBar)findViewById(R.id.progressBar_setting_sns);
		
		//url을 받아 profile image를 bitmap으로 변환
    	new ProfileImageTask().execute();

		logout = (Button)findViewById(R.id.btn_logout);
		logout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				logout();
				finish();
			}
		});	
	}
	
	class ProfileImageTask extends AsyncTask<Void, Void, Bitmap>{

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progBar.setVisibility(View.VISIBLE);
		}
		
		@Override
		protected Bitmap doInBackground(Void... params) {
			URL ProfileImageUrl = null;
    		profileBitmap = null;
    		HttpURLConnection con = null;
    		BufferedInputStream bis=null;
            try{
               	ProfileImageUrl = new URL(intent.getExtras().getString("url"));
                
                con = (HttpURLConnection)ProfileImageUrl.openConnection();
                con.setDoInput(true);
                con.setRequestMethod("GET");
                con.connect();
                bis = new BufferedInputStream(con.getInputStream());
                profileBitmap = BitmapFactory.decodeStream(bis);
                Log.e("image", profileBitmap.toString());
                 
            }catch (MalformedURLException e){
                e.printStackTrace();
            }catch (IOException e){
                e.printStackTrace();
            } finally{
    			try {
    				if(bis!=null) bis.close();
    				if(con!=null) con.disconnect();
    			} catch (IOException e) {
    				e.printStackTrace();
    			}
    		}
			return null;
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			super.onPostExecute(result);
			progBar.setVisibility(View.GONE);
			
			// 각 뷰에 적용
			profileImg.setImageBitmap(profileBitmap);
			name.setText(intent.getExtras().getString("name"));
		}
	}
	
	
	// logout
	private void logout(){
		if (target.equals("facebook")){
            Session session = Session.getActiveSession();
            if(session!=null){
            	session.closeAndClearTokenInformation();
            }
            Session.setActiveSession(null);
        }else if (target.equals("twitter")){
        	BasicInfo.TwitLogin = false;
        	BasicInfo.TWIT_KEY_TOKEN = "";
        	BasicInfo.TWIT_KEY_TOKEN_SECRET = "";
        	BasicInfo.TwitScreenName = "";
        	twitSaveProperties();
        }
    	Toast.makeText(this, getString(R.string.logout_setting), Toast.LENGTH_SHORT).show();
	}
	
	private void twitSaveProperties() {
		SharedPreferences pref = getSharedPreferences("TWIT", MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();

		editor.putBoolean("TwitLogin", BasicInfo.TwitLogin);
		editor.putString("TWIT_KEY_TOKEN", BasicInfo.TWIT_KEY_TOKEN);
		editor.putString("TWIT_KEY_TOKEN_SECRET", BasicInfo.TWIT_KEY_TOKEN_SECRET);
		editor.putString("TwitScreenName", BasicInfo.TwitScreenName);

		editor.commit();
	}
	
	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
	}	
}
