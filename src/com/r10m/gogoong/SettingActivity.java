package com.r10m.gogoong;

import java.util.Locale;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class SettingActivity extends PreferenceActivity {

	SharedPreferences mainPreference;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		overridePendingTransition(R.anim.leftin, R.anim.leftout);

		// 1. \res\xml\preferences.xml로 부터 Preference 계층구조를 읽어와
		// 2. 이 PreferenceActivity의 계층구조로 지정/표현 하고
		// 3. \data\data\패키지이름\shared_prefs\패키지이름_preferences.xml 생성
		// 4. 이 후 Preference에 변경 사항이 생기면 파일에 자동 저장
		
		mainPreference = PreferenceManager.getDefaultSharedPreferences(this);
		setLocale(mainPreference.getString("LanguageList", "ko"));
		setContentView(R.layout.setting);
		addPreferencesFromResource(R.xml.settings);
		
	   	ListPreference searchEngineSettings = (ListPreference)findPreference("LanguageList");
	    searchEngineSettings.setSummary(searchEngineSettings.getValue());
	    searchEngineSettings.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
	         
	         @Override
	         public boolean onPreferenceChange(Preference preference, Object newValue) {
	        	 Intent intent = getIntent();
	        	 overridePendingTransition(0, 0);
	        	 intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
	        	 finish();
	        	 startActivity(intent);
	             return true;
	         }
	     });
	   	 
		
	   //설정_뒤로
       Button btn_back = (Button) findViewById(R.id.btn_back);
       btn_back.setOnClickListener(new OnClickListener(){
	       @Override
	       public void onClick(View v) {
				Intent intent = new Intent(SettingActivity.this, MainActivity.class);
	            startActivity(intent);
	       }
       });   	
	}
	
   	public void setLocale(String character) {
    	Locale locale = new Locale(character); 
    	Locale.setDefault(locale);
    	Configuration config = new Configuration();
    	config.locale = locale;
    	getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
    }
}
