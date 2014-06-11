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
/** Marker�� click������ �ؽ�Ʈ �� ���� ��� */
public class DetailPopUp extends Activity implements OnClickListener,TextToSpeech.OnInitListener{
	private SharedPreferences mainPreference;
	private TextToSpeech mTts;
	private String name; 
	private String detail;
	private String locale;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
                WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        
        setContentView(R.layout.detail);
        
        mainPreference = PreferenceManager.getDefaultSharedPreferences(this);	//���������о��
    	locale = mainPreference.getString("LanguageList", "ko");
        
        // �ؽ�Ʈ ��� �غ�
        TextView textName = (TextView)findViewById(R.id.textView_detail_name);
        TextView textDetail = (TextView)findViewById(R.id.textView_detail_detail);
        ImageButton imgBtn = (ImageButton)findViewById(R.id.imageButton_detail);
        imgBtn.setOnClickListener(this);
        
        Intent intent = getIntent();
        name = intent.getExtras().get("name").toString();
        detail = intent.getExtras().get("detail").toString();
        textName.setText(name);
        textDetail.setText(detail);
        
        //TTS �غ�
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
	
	//TTS �ڿ� ����
	@Override
	protected void onDestroy() {
		if (mTts != null) {
            mTts.stop();
            mTts.shutdown();
        }
		super.onDestroy();
	}
	
	// �������
	private void say() {
        mTts.speak(detail,
            TextToSpeech.QUEUE_FLUSH,  // Drop all pending entries in the playback queue.
            null);
	}
	
	// video ��� 
	@Override
	public void onClick(View v) {
		if(v.getId()==R.id.imageButton_detail){
			Intent intent = new Intent(DetailPopUp.this, VideoActivity.class);
			intent.putExtra("name", name);
			
			if (mTts != null) {
	            mTts.stop();
	            mTts.shutdown();
	        }
			
			startActivity(intent);
		}
	}
	
	//TTS �ʱ�ȭ
	@Override
	public void onInit(int status) {
		// status can be either TextToSpeech.SUCCESS or TextToSpeech.ERROR.
        if (status == TextToSpeech.SUCCESS) {
            // Set preferred language to US english.
            // Note that a language may not be available, and the result will indicate this.
            int result = mTts.setLanguage(locale.equals("kr")?Locale.KOREA:Locale.ENGLISH);
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
