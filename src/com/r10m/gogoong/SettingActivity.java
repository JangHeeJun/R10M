package com.r10m.gogoong;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

public class SettingActivity extends Activity {
	ListView list;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting);
		overridePendingTransition(R.anim.leftin, R.anim.leftout);
		
		
	   //설정_뒤로
       Button btn_back = (Button) findViewById(R.id.btn_back);
       btn_back.setOnClickListener(new OnClickListener(){
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(SettingActivity.this, MainActivity.class);
            startActivity(intent);
		}
       });

		list = (ListView)findViewById(R.id.listView);	
		//배열을 갖기 위해서는 adapter가 필요하다.				
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
		//이곳에 만든다, R파일의 array.settiing, 미리 정의되어 있는것 = adapter 	

		this, R.array.setting, android.R.layout.simple_list_item_1);	
		list.setAdapter(adapter);

		list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
		}

			
	});					
}
}
