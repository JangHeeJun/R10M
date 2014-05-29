package com.r10m.gogoong;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class LogoActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);       
        setContentView(R.layout.logo);
        
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(LogoActivity.this, MainActivity.class));
            	overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();       // 3 초후 이미지를 닫아버림
            }
        }, 3000);		
    }

}
