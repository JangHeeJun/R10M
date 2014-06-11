package com.r10m.gogoong;

import java.util.Locale;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationBuilder;

import com.facebook.Session;
import com.facebook.SessionState;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener{

	SharedPreferences mainPreference;
	public static final String TAG = "MainActivity";

	private static final int REQUEST_ENABLE_BT = 0;
	
	//facebook
	Session mSession;
	private Session.StatusCallback statusCallback = new SessionStatusCallback();
    
	//twitter
    private Handler mHandler = new Handler();
    ProgressBar progBar;
	
    @Override
    public void onBackPressed() {
    	mainPreference = PreferenceManager.getDefaultSharedPreferences(this);
		setLocale(mainPreference.getString("LanguageList", "ko"));
    	super.onBackPressed();
    }
    
	/** Called when the activity is first created. */
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.main);
        
        Locale systemLocale = getResources().getConfiguration().locale;
        String strLanguage = systemLocale.getLanguage();
        
        mainPreference = PreferenceManager.getDefaultSharedPreferences(this);	//�ㅼ젙�댁슜�쎌뼱��
    	setLocale(mainPreference.getString("LanguageList", strLanguage));		//�몄뼱�ㅼ젙
		
        //bluetooth
        BluetoothAdapter mBTAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBTAdapter == null) {
        // device does not support Bluetooth
        }
        if (!mBTAdapter.isEnabled()) {
        	Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        	startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        	}
      
        // GPS
        String context = Context.LOCATION_SERVICE;
        LocationManager locationManager = (LocationManager)getSystemService(context);
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            alertCheckGPS();
        }
		
        Button btn_start = (Button) findViewById(R.id.btn_start);
        Button btn_AroundView = (Button) findViewById(R.id.btn_AroundView);
        Button btn_homepage = (Button) findViewById(R.id.btn_homepage);
        Button setting = (Button) findViewById(R.id.btn_setting);
        Button ibtn_fb = (Button) findViewById(R.id.btn_fb);
        Button ibtn_tw = (Button) findViewById(R.id.btn_tw);
		
		btn_start.setOnClickListener(this);
		btn_AroundView.setOnClickListener(this);
		btn_homepage.setOnClickListener(this);
		setting.setOnClickListener(this);
		ibtn_fb.setOnClickListener(this);
		ibtn_tw.setOnClickListener(this);

        progBar = (ProgressBar)findViewById(R.id.progressBar_main);
	}
	
	
	@Override
	public void onClick(View v) {
		Intent intent = null;
		
		switch(v.getId()){
		//移대찓�펣tart
		case R.id.btn_start :
			progBar.setVisibility(View.VISIBLE);
			intent = new Intent(MainActivity.this, CameraActivity.class);
		    startActivity(intent); 
		    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
			break;
		//二쇰�吏�룄
		case R.id.btn_AroundView :
			progBar.setVisibility(View.VISIBLE);
      	  	intent = new Intent(MainActivity.this, AroundActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
			break;
		//�덊럹�댁� �곌껐
		case R.id.btn_homepage :
			progBar.setVisibility(View.VISIBLE);
			intent=new Intent(Intent.ACTION_VIEW,Uri.parse("https://www.google.com/"));
			startActivityForResult(intent, -1);
			break;
		//�ㅼ젙
		case R.id.btn_setting :
			intent = new Intent(MainActivity.this, SettingActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
			break;
		//�섏씠�ㅻ턿 �곌껐
		case R.id.btn_fb :
			progBar.setVisibility(View.VISIBLE);
	    	facebookLogin(MainActivity.this);
			break;
		//�몄쐞���곌껐
		case R.id.btn_tw :
			progBar.setVisibility(View.VISIBLE);
	    	twitLogin(); 
			break;
		}
		
	}
	
	// GPS alertDialog 李��꾩슦湲�
	private void alertCheckGPS() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.gps1_key))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.gps2_key),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                moveConfigGPS();
                            }
                    })
                .setNegativeButton(getString(R.string.gps3_key),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                    });
        AlertDialog alert = builder.create();
        alert.show();
    }
    // GPS �ㅼ젙�붾㈃�쇰줈 �대룞
    private void moveConfigGPS() {
        Intent gpsOptionsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(gpsOptionsIntent);
    }
    
    //�몄뼱 �ㅼ젙
    public void setLocale(String character) {
    	Locale locale = new Locale(character); 
    	Locale.setDefault(locale);
    	Configuration config = new Configuration();
    	config.locale = locale;
    	getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
    }
    
  	//facebook �곕룞 �쒖옉 -----------------------------------------------------------------
    private void facebookLogin(Context context) {
    	
    	
    	Session session = Session.getActiveSession();
    	
    	if(session == null){
    		session = Session.openActiveSessionFromCache(context);
    		if(session == null){
    			Log.e("init", "罹먯떆�먮룄 �몄뀡���놁쓬");
    			Session.openActiveSession(this, true, statusCallback);
    		}else{
    			Log.e("init", "罹먯떆���몄뀡 �덉쓬");
    			checkFacebookLogin(session);
    		}
    	}else if(session.isClosed()){
    		Session.openActiveSession(this, true, statusCallback);
    		checkFacebookLogin(session);
    	}else{
			Log.e("init", "�몄뀡 �덉쓬");
			Toast.makeText(this, getString(R.string.facebookLogin_main), Toast.LENGTH_SHORT).show();
			checkFacebookLogin(session);
		}
    	progBar.setVisibility(View.GONE);
    }
    
    
    // 沅뚰븳 �붿껌
    private void checkFacebookLogin(Session session) {
    	
    	String permission = "publish_actions";
    	
    	boolean isContainPermit = true;
    	if(session.isOpened()){
    		if(!session.getPermissions().contains(permission)){
    			isContainPermit = false;
    		}			
    		if(!isContainPermit){
    			// 沅뚰븳 �붿껌 �섎뒗 遺�텇
    			Session.NewPermissionsRequest newPermissionsRequest = 
    					new Session.NewPermissionsRequest(MainActivity.this, permission);
    			session.requestNewPublishPermissions(newPermissionsRequest);
    		}
		}
    }
    
    
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (Session.getActiveSession() != null) {
            Session session = Session.getActiveSession();
            Session.saveSession(session, outState);
        }
    }
    
    
	private class SessionStatusCallback implements Session.StatusCallback {
		@Override
		public void call(Session session, SessionState state, Exception exception) {
			checkFacebookLogin(session);
		}
	}
  	//facebook �곕룞 ��-----------------------------------------------------------------
    
    //tiwtter �곕룞 �쒖옉 -----------------------------------------------------------------
    private void twitLogin() {
		Log.d(TAG, "connect() called.");
		// �몄쬆 �섏뼱�덉쓣��
		if (BasicInfo.TwitLogin) {
			Log.d(TAG, "twitter already logged in.");
			Toast.makeText(getBaseContext(), getString(R.string.twitLogin_main), Toast.LENGTH_LONG).show();
	        progBar.setVisibility(View.GONE);
			try {
				ConfigurationBuilder builder = new ConfigurationBuilder();

				builder.setOAuthAccessToken(BasicInfo.TWIT_KEY_TOKEN);
				builder.setOAuthAccessTokenSecret(BasicInfo.TWIT_KEY_TOKEN_SECRET);
				builder.setOAuthConsumerKey(BasicInfo.TWIT_CONSUMER_KEY);
				builder.setOAuthConsumerSecret(BasicInfo.TWIT_CONSUMER_SECRET);

				twitter4j.conf.Configuration config = builder.build();
				TwitterFactory tFactory = new TwitterFactory(config);
				BasicInfo.TwitInstance = tFactory.getInstance();

	    	} catch (Exception ex) {
				ex.printStackTrace();
			}

	        //�덈줈 �몄쬆 諛쏆쓣��
		} else {
			
			RequestTokenThread thread = new RequestTokenThread();
			thread.start();
			
		}

    }

    /**
     * RequestToken �붿껌 �ㅻ젅��
     */
    class RequestTokenThread extends Thread {
    	public void run() {

	    	try {
				ConfigurationBuilder builder = new ConfigurationBuilder();
				builder.setDebugEnabled(true);
				builder.setOAuthConsumerKey(BasicInfo.TWIT_CONSUMER_KEY);
				builder.setOAuthConsumerSecret(BasicInfo.TWIT_CONSUMER_SECRET);

				TwitterFactory factory = new TwitterFactory(builder.build());
				Twitter mTwit = factory.getInstance();
				final RequestToken mRequestToken = mTwit.getOAuthRequestToken();
				String outToken = mRequestToken.getToken();
				String outTokenSecret = mRequestToken.getTokenSecret();

				Log.d(TAG, "Request Token : " + outToken + ", " + outTokenSecret);
				Log.d(TAG, "AuthorizationURL : " + mRequestToken.getAuthorizationURL());

				BasicInfo.TwitInstance = mTwit;
				BasicInfo.TwitRequestToken = mRequestToken;

				
				mHandler.post(new Runnable() {
					public void run() {

						Intent intent = new Intent(getApplicationContext(), TwitLogin.class);
						intent.putExtra("authUrl", mRequestToken.getAuthorizationURL());
						startActivityForResult(intent, BasicInfo.REQ_CODE_TWIT_LOGIN);

					}
				});
				
	    	} catch (Exception ex) {
				ex.printStackTrace();
			}

    	}
    }
    
    /**
     * �ㅻⅨ �≫떚鍮꾪떚濡쒕��곗쓽 �묐떟 泥섎━
     */
	protected void onActivityResult(int requestCode, int resultCode, Intent resultIntent) {
		super.onActivityResult(requestCode, resultCode, resultIntent);
		progBar.setVisibility(View.VISIBLE);
		boolean processed = false;
	    if (Session.getActiveSession() != null) {
	        processed = Session.getActiveSession().onActivityResult(this, requestCode, 
	                                                resultCode, resultIntent);
	    }

	    if (!processed) {
	    }
	    
		if (resultCode == RESULT_OK) {
			if (requestCode == BasicInfo.REQ_CODE_TWIT_LOGIN) {
				
				OAuthAccessTokenThread thread = new OAuthAccessTokenThread(resultIntent);
				thread.start();
				
			}
		}
	}

	
	class OAuthAccessTokenThread extends Thread {
		Intent resultIntent;
		
		public OAuthAccessTokenThread(Intent intent) {
			resultIntent = intent;
		}
		
		public void run() {
			try {
				Twitter mTwit = BasicInfo.TwitInstance;

				AccessToken mAccessToken = mTwit.getOAuthAccessToken(BasicInfo.TwitRequestToken, resultIntent.getStringExtra("oauthVerifier"));

				BasicInfo.TwitLogin = true;
				BasicInfo.TWIT_KEY_TOKEN = mAccessToken.getToken();
				BasicInfo.TWIT_KEY_TOKEN_SECRET = mAccessToken.getTokenSecret();

				BasicInfo.TwitAccessToken = mAccessToken;

				BasicInfo.TwitScreenName = mTwit.getScreenName();

				mHandler.post(new Runnable() {
					public void run() {
						progBar.setVisibility(View.GONE);
						Toast.makeText(getBaseContext(), getString(R.string.twitLoginsuccess_main), Toast.LENGTH_LONG).show();
					}
				});

			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	
	private void twitSaveProperties() {
		SharedPreferences pref = getSharedPreferences("TWIT", MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();

		editor.putBoolean("TwitLogin", BasicInfo.TwitLogin);
		editor.putString("TWIT_KEY_TOKEN", BasicInfo.TWIT_KEY_TOKEN);
		editor.putString("TWIT_KEY_TOKEN_SECRET", BasicInfo.TWIT_KEY_TOKEN_SECRET);
		editor.putString("TwitScreenName", BasicInfo.TwitScreenName);

		editor.commit();
	}

	private void twitLoadProperties() {
		SharedPreferences pref = getSharedPreferences("TWIT", MODE_PRIVATE);

		BasicInfo.TwitLogin = pref.getBoolean("TwitLogin", false);
		BasicInfo.TWIT_KEY_TOKEN = pref.getString("TWIT_KEY_TOKEN", "");
		BasicInfo.TWIT_KEY_TOKEN_SECRET = pref.getString("TWIT_KEY_TOKEN_SECRET", "");
		BasicInfo.TwitScreenName = pref.getString("TwitScreenName", "");
	}
	//twitter �곕룞 ��---------------------------------------------------------------
    
   
	@Override
	protected void onResume() {
		super.onResume();
		twitLoadProperties();
        progBar.setVisibility(View.GONE);
	}

	@Override
	protected void onPause() {
		super.onPause();
		twitSaveProperties();
	}

}
