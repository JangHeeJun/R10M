package com.r10m.gogoong;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {

	/** Called when the activity is first created. */
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.main);

		//카메라start
		Button btn_start = (Button) findViewById(R.id.btn_start);
		btn_start.setOnClickListener(new OnClickListener(){
		
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, CameraActivity.class);
			    startActivity(intent); 
			}  
		}); 
		
		//주변지도
       Button btn_AroundView = (Button) findViewById(R.id.btn_AroundView);
       btn_AroundView.setOnClickListener(new OnClickListener(){

          @Override
          public void onClick(View v) {
        	  Intent intent = new Intent(MainActivity.this, AroundActivity.class);
              startActivity(intent);
          }
       });  
       
	   //홈페이지 연결
	   Button btn_homepage = (Button) findViewById(R.id.btn_homepage);
	   btn_homepage.setOnClickListener(new OnClickListener(){
	
	   @Override
	       public void onClick(View v) {
	          Intent intent=new Intent(Intent.ACTION_VIEW,Uri.parse("https://www.google.com/"));
	          startActivity(intent);
	       }     
	   });
	   
	   //설정
       Button setting = (Button) findViewById(R.id.btn_setting);
       setting.setOnClickListener(new OnClickListener(){

          @Override
          public void onClick(View v) {
        	  Intent intent = new Intent(MainActivity.this, SettingActivity.class);
              startActivity(intent);
          }
       });
       
     
   
	   //페이스북 연결
	   Button ibtn_fb = (Button) findViewById(R.id.btn_fb);
	   ibtn_fb.setOnClickListener(new OnClickListener(){
	
	     @Override
	     public void onClick(View v) {
	        Intent intent=new Intent(Intent.ACTION_VIEW,Uri.parse("https://www.facebook.com/"));
	        startActivity(intent);  
	     } 
	   });
	   
	   //트위터 연결
	   Button ibtn_tw = (Button) findViewById(R.id.btn_tw);
	   ibtn_tw.setOnClickListener(new OnClickListener(){
	
	     @Override
	     public void onClick(View v) {
	        Intent intent=new Intent(Intent.ACTION_VIEW,Uri.parse("https://www.twitter.com/"));
	        startActivity(intent);  
	     } 
	   });
    }
}
