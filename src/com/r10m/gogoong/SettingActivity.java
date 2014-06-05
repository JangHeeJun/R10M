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

		// 1. \res\xml\preferences.xml�� ���� Preference ���������� �о��
		// 2. �� PreferenceActivity�� ���������� ����/ǥ�� �ϰ�
		// 3. \data\data\��Ű���̸�\shared_prefs\��Ű���̸�_preferences.xml ����
		// 4. �� �� Preference�� ���� ������ ����� ���Ͽ� �ڵ� ����
		
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
	   	 
		
	   //����_�ڷ�
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
