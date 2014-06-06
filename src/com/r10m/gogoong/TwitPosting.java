package com.r10m.gogoong;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import twitter4j.TwitterFactory;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.media.ImageUpload;
import twitter4j.media.ImageUploadFactory;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class TwitPosting extends Activity {
	TextView nameText;
	ImageView img;
	Button btnWrite;
	EditText writeTwit;
	Configuration config;
	Bitmap cImage;
	ProgressBar progBar;
	Handler mHandler = new Handler();
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.twit_posting);
        
        // 이름 텍스트 뷰
        nameText 	= (TextView) findViewById(R.id.textView_twit_posting);
        nameText.setText(BasicInfo.TwitScreenName);
        
        //프로그레스바
        progBar=(ProgressBar)findViewById(R.id.progressBar_twit);
		progBar.setVisibility(View.INVISIBLE);
		
        // 이미지뷰
        img 		= (ImageView) findViewById(R.id.imageView_twit);
        Intent intentFb = getIntent();
		cImage=(Bitmap)intentFb.getExtras().get("cImage");
		
		img.setImageBitmap(cImage);
	
        // 글쓰기 버튼
        btnWrite 	= (Button) findViewById(R.id.btn_twit_write);
        // 글 에디트박스
        writeTwit 	= (EditText) findViewById(R.id.write_twit);
        
        btnWrite.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		String statusText = writeTwit.getText().toString();
        		// 글을 안썼을 때
        		if (statusText.length() < 1) {
        			Toast.makeText(getApplicationContext(), "글을 입력하세요.", 1000).show();
        			return;
        		}
        		//loadProperties();
    			connect();
        		updateStatus(statusText);
        	}
        });
    }
	
	 private void connect() {
		 progBar.setVisibility(View.VISIBLE);
		// 인증 되어있을때
		if (BasicInfo.TwitLogin) {
			try {
				ConfigurationBuilder builder = new ConfigurationBuilder();

				builder.setOAuthAccessToken(BasicInfo.TWIT_KEY_TOKEN);
				builder.setOAuthAccessTokenSecret(BasicInfo.TWIT_KEY_TOKEN_SECRET);
				builder.setOAuthConsumerKey(BasicInfo.TWIT_CONSUMER_KEY);
				builder.setOAuthConsumerSecret(BasicInfo.TWIT_CONSUMER_SECRET);

				config = builder.build();
				TwitterFactory tFactory = new TwitterFactory(config);
				BasicInfo.TwitInstance = tFactory.getInstance();
				
	    	} catch (Exception ex) {
				ex.printStackTrace();
			}
		} 
    }
	 
	// 게시물 올리기 메소드
    private void updateStatus(String statusText) {
    	
    	UpdateStatusThread thread = new UpdateStatusThread(statusText);
    	thread.start();
    
    }
    // 게시물 올리기 스레드
    class UpdateStatusThread extends Thread {
    	String statusText;
    	
    	public UpdateStatusThread(String inText) {
    		statusText = inText;
    	}
    	
    	public void run() {
    		try {    			
        		//BasicInfo.TwitInstance.updateStatus(statusText);
        		
				ImageUpload imageUpload = new ImageUploadFactory(config).getInstance();
				imageUpload.upload(SaveBitmapToFileCache(cImage), statusText);
				
        		mHandler.post(new Runnable() {
        			public void run() {
        				progBar.setVisibility(View.INVISIBLE);
        				Toast.makeText(getApplicationContext(), "글을 업데이트했습니다 : ", Toast.LENGTH_SHORT).show();
        			}
        		});
        		finish();
        	} catch(Exception ex) {
        		progBar.setVisibility(View.INVISIBLE);
        		ex.printStackTrace();
        	}

    	}
    }
    
    // Bitmap to File
    public File SaveBitmapToFileCache(Bitmap bitmap) {
  
        OutputStream out = null;
        File img = null;
        
        try {
        	img = File.createTempFile("img", "png");
            out = new FileOutputStream(img);
            bitmap.compress(CompressFormat.JPEG, 100, out);
           
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return img;
    }
}
