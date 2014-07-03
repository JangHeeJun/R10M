package com.r10m.gogoong;

import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
/** Marker를 click했을시 텍스트 및 음성 출력 */
public class DetailPopUp extends Activity implements OnClickListener,TextToSpeech.OnInitListener{
	private SharedPreferences mainPreference;
	private TextToSpeech mTts;
	private String name; 
	private String detail;
	private String locale;
	private String kind;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
                WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        
        setContentView(R.layout.detail);
    	
    	Locale systemLocale = getResources().getConfiguration().locale;
	    String strLanguage = systemLocale.getLanguage();
		
		mainPreference = PreferenceManager.getDefaultSharedPreferences(this);
		locale = mainPreference.getString("LanguageList", strLanguage);
        
        // 텍스트 출력 준비
        TextView textName = (TextView)findViewById(R.id.textView_detail_name);
        TextView textDetail = (TextView)findViewById(R.id.textView_detail_detail);
        ImageButton imgBtn = (ImageButton)findViewById(R.id.imageButton_detail);
        imgBtn.setOnClickListener(this);
        
        Intent intent = getIntent();
        name = intent.getExtras().get("name").toString();
        detail = intent.getExtras().get("detail").toString();
        kind = intent.getExtras().getString("kind");
        textName.setText(name);
        textDetail.setText(detail);
        
        //TTS 준비
        mTts = new TextToSpeech(this, this  // TextToSpeech.OnInitListener
                );
            setVolumeControlStream(AudioManager.STREAM_MUSIC);
            AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
            int amStreamMusicMaxVol = am.getStreamMaxVolume(am.STREAM_MUSIC);
            am.getStreamVolume(am.STREAM_MUSIC);
            
//            int iAlertVolume=1;
//    		am.setStreamVolume(am.STREAM_MUSIC, (iAlertVolume*amStreamMusicMaxVol)/100,0 );
            // The button is disabled in the layout.
            // It will be enabled upon initialization of the TTS engine.
            say();        
    }
	
	//TTS 자원 해제
	@Override
	protected void onDestroy() {
		if (mTts != null) {
            mTts.stop();
            mTts.shutdown();
        }
		super.onDestroy();
	}
	
	// 음성출력
	private void say() {
        mTts.speak(detail,
            TextToSpeech.QUEUE_FLUSH,  // Drop all pending entries in the playback queue.
            null);
	}
	
	// video 출력 
	@Override
	public void onClick(View v) {
		if(v.getId()==R.id.imageButton_detail){
			Intent intent = new Intent(DetailPopUp.this, VideoActivity.class);
			intent.putExtra("name", name);
			intent.putExtra("kind", kind);
			
			if (mTts != null) {
	            mTts.stop();
	            mTts.shutdown();
	        }
			
			startActivity(intent);
		}
	}
	
	//TTS 초기화
	@Override
	public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = 0;
        	if(locale.equals("ko")){
        		result = mTts.setLanguage(Locale.KOREA);
    		}else if(locale.equals("en")){
    			result = mTts.setLanguage(Locale.ENGLISH);
    		}else if(locale.equals("ja")){
    			result = mTts.setLanguage(Locale.JAPAN);
    		}
            if (result == TextToSpeech.LANG_MISSING_DATA ||
                result == TextToSpeech.LANG_NOT_SUPPORTED) {
               // Lanuage data is missing or the language is not supported.
                Toast.makeText(this, "Language is not available.", Toast.LENGTH_SHORT).show();
            } else {
                say();
            }
        } else {
            Log.e("TTS", "Could not initialize TextToSpeech.");
        }
	}

}
