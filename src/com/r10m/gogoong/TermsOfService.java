package com.r10m.gogoong;

import com.r10m.gogoong.R;
import com.r10m.gogoong.R.id;
import com.r10m.gogoong.R.layout;
import com.r10m.gogoong.R.menu;
import com.r10m.gogoong.SettingActivity;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.os.Build;

public class TermsOfService extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.termsofservice);
		
		//설정_뒤로
	       Button btn_back = (Button) findViewById(R.id.btn_back);
	       btn_back.setOnClickListener(new OnClickListener(){
		       @Override
		       public void onClick(View v) {
					Intent intent = new Intent(TermsOfService.this, SettingActivity.class);
		            startActivity(intent);
		       }
	       });   

	}


}
