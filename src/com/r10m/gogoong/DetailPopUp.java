package com.r10m.gogoong;

import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class DetailPopUp extends Activity implements OnClickListener,TextToSpeech.OnInitListener{
	
	private TextToSpeech mTts;
    private String detail; 
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
                WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        
        setContentView(R.layout.detail);
        
        TextView textView = (TextView)findViewById(R.id.Popup);
        Intent intent = getIntent();
        detail = intent.getExtras().get("detail").toString();
        textView.setText(detail);
        
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
	
	@Override
	protected void onDestroy() {
		if (mTts != null) {
            mTts.stop();
            mTts.shutdown();
        }
		super.onDestroy();
	}

	private void say() {
        mTts.speak(detail,
            TextToSpeech.QUEUE_FLUSH,  // Drop all pending entries in the playback queue.
            null);
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onInit(int status) {
		// status can be either TextToSpeech.SUCCESS or TextToSpeech.ERROR.
        if (status == TextToSpeech.SUCCESS) {
            // Set preferred language to US english.
            // Note that a language may not be available, and the result will indicate this.
            int result = mTts.setLanguage(Locale.KOREA);
            // Try this someday for some interesting results.
            // int result mTts.setLanguage(Locale.FRANCE);
            if (result == TextToSpeech.LANG_MISSING_DATA ||
                result == TextToSpeech.LANG_NOT_SUPPORTED) {
               // Lanuage data is missing or the language is not supported.
                Log.e("TTS", "Language is not available.");
            } else {
                // Check the documentation for other possible result codes.
                // For example, the language may be available for the locale,
                // but not for the specified country and variant.

                // The TTS engine has been successfully initialized.
                // Allow the user to press the button for the app to speak again.
                // mAgainButton.setEnabled(true);
                // Greet the user.
                say();
            }
        } else {
            // Initialization failed.
            Log.e("TTS", "Could not initialize TextToSpeech.");
        }
	}
	
	

}
