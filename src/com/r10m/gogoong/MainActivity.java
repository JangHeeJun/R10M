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
import android.widget.Toast;

public class MainActivity extends Activity {
	
	SharedPreferences mainPreference;
	public static final String TAG = "MainActivity";

	private static final int REQUEST_ENABLE_BT = 0;
	
	//facebook
	private Bundle mSavedInstanceState;
	private boolean bProgressLogin;
	private Session.StatusCallback statusCallback = new Session.StatusCallback() {    

		@Override
        public void call(Session session, SessionState state, Exception exception) {
        	if (state == SessionState.OPENED
        	        || state == SessionState.OPENED_TOKEN_UPDATED) {
        	            // log in
        	            checkFacebookLogin();
        	        }
        	        else if (state == SessionState.CLOSED){}
        	            // log out
        	        else if (state == SessionState.CLOSED_LOGIN_FAILED) {
        	            // 여러 이유로 인증실패.
        	            bProgressLogin = false;
        	        }
        }
    };
    
	//twitter
    private Handler mHandler = new Handler();
		
	

	/** Called when the activity is first created. */
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.main);
        
    	mainPreference = PreferenceManager.getDefaultSharedPreferences(this);	//설정내용읽어옴
    	setLocale(mainPreference.getString("LanguageList", "ko"));					//언어설정

		
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

		//카메라start
		Button btn_start = (Button) findViewById(R.id.btn_start);
		btn_start.setOnClickListener(new OnClickListener(){
		
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, CameraActivity.class);
			    startActivity(intent); 
			}  
		}); 
		
		//주변지도
       Button btn_AroundView = (Button) findViewById(R.id.btn_AroundView);
       btn_AroundView.setOnClickListener(new OnClickListener(){

          @Override
          public void onClick(View v) {
        	  Intent intent = new Intent(MainActivity.this, AroundActivity.class);
              startActivity(intent);
          }
       });  
       
	   //홈페이지 연결
	   Button btn_homepage = (Button) findViewById(R.id.btn_homepage);
	   btn_homepage.setOnClickListener(new OnClickListener(){
	
	   @Override
	       public void onClick(View v) {
	          Intent intent=new Intent(Intent.ACTION_VIEW,Uri.parse("https://www.google.com/"));
	          startActivity(intent);
	       }     
	   });
	   
	   //설정
       Button setting = (Button) findViewById(R.id.btn_setting);
       setting.setOnClickListener(new OnClickListener(){

          @Override
          public void onClick(View v) {
        	  Intent intent = new Intent(MainActivity.this, SettingActivity.class);
              startActivity(intent);
          }
       });
       
     
   
	   //페이스북 연결
	   Button ibtn_fb = (Button) findViewById(R.id.btn_fb);
	   ibtn_fb.setOnClickListener(new OnClickListener(){
	
	     @Override
	     public void onClick(View v) {
	    	 onClickLogin();   
	     } 
	   });
	   
	   //트위터 연결
	   Button ibtn_tw = (Button) findViewById(R.id.btn_tw);
	   ibtn_tw.setOnClickListener(new OnClickListener(){
	
	     @Override
	     public void onClick(View v) {
	    	 connect();  
	     } 
	   });
    }
	private void alertCheckGPS() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS is disabled! Would you like to enable it?")
                .setCancelable(false)
                .setPositiveButton("Enable GPS",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                moveConfigGPS();
                            }
                    })
                .setNegativeButton("Do nothing",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                    });
        AlertDialog alert = builder.create();
        alert.show();
    }
    // GPS 설정화면으로 이동
    private void moveConfigGPS() {
        Intent gpsOptionsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(gpsOptionsIntent);
    }
   
    //언어설정
  	public void setLocale(String character) {
      	Locale locale = new Locale(character); 
      	Locale.setDefault(locale);
      	Configuration config = new Configuration();
      	config.locale = locale;
      	getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
      }
  	
  	//facebook 연동
    private void onClickLogin() {
    	bProgressLogin = true;
        Session session = Session.getActiveSession();
        if (session == null) {
            if (mSavedInstanceState != null) {
            	session = Session.restoreSession(this, null, statusCallback, mSavedInstanceState);
            }
            if (session == null) {
            	session = new Session(this);
            }
            Session.setActiveSession(session);
            session.addCallback(statusCallback);
            if (session.getState().equals(SessionState.CREATED_TOKEN_LOADED)) {
            	session.openForRead(new Session.OpenRequest(this).setCallback(statusCallback));
            }
        }
        checkFacebookLogin();
    }
    
    private void checkFacebookLogin() {
    	 Session session = Session.getActiveSession();
    	 if (session != null && bProgressLogin) {
    	  boolean logined = session.isOpened();
    	  if (logined) {
    	   String PERMISSION = "publish_actions";
    	   if (session.getPermissions().contains(PERMISSION)) {
    	    bProgressLogin = false;
    	    Toast.makeText(this, "이미 페이스북 연동하셨습니다.", Toast.LENGTH_SHORT).show();
    	    // log in 성공
    	   } else
    	    session.requestNewPublishPermissions(
    	     new Session.NewPermissionsRequest(this, PERMISSION));
    	  
    	  }
    	  else {
    	   // log in 시도
    	   if (!session.isOpened() && !session.isClosed())
    	    session.openForRead(new Session.OpenRequest(this)
    	     .setCallback(statusCallback));
    	   else{
    	    Session.openActiveSession(this, true, statusCallback);
    	   }
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
    
    //tiwtter 연동
    private void connect() {
		Log.d(TAG, "connect() called.");
		// 인증 되어있을때
		if (BasicInfo.TwitLogin) {
			Log.d(TAG, "twitter already logged in.");
			Toast.makeText(getBaseContext(), "twitter already logged in.", Toast.LENGTH_LONG).show();

			try {
				ConfigurationBuilder builder = new ConfigurationBuilder();

				builder.setOAuthAccessToken(BasicInfo.TWIT_KEY_TOKEN);
				builder.setOAuthAccessTokenSecret(BasicInfo.TWIT_KEY_TOKEN_SECRET);
				builder.setOAuthConsumerKey(BasicInfo.TWIT_CONSUMER_KEY);
				builder.setOAuthConsumerSecret(BasicInfo.TWIT_CONSUMER_SECRET);

				twitter4j.conf.Configuration config = builder.build();
				TwitterFactory tFactory = new TwitterFactory(config);
				BasicInfo.TwitInstance = tFactory.getInstance();
				
				Toast.makeText(getBaseContext(), "twitter connected.", Toast.LENGTH_LONG).show();

	    	} catch (Exception ex) {
				ex.printStackTrace();
			}

	        //새로 인증 받을때
		} else {
			
			RequestTokenThread thread = new RequestTokenThread();
			thread.start();
			
		}

    }

    /**
     * RequestToken 요청 스레드
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
     * 다른 액티비티로부터의 응답 처리
     */
	protected void onActivityResult(int requestCode, int resultCode, Intent resultIntent) {
		super.onActivityResult(requestCode, resultCode, resultIntent);
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
						Toast.makeText(getBaseContext(), "Twitter connection succeeded : " + BasicInfo.TWIT_KEY_TOKEN, Toast.LENGTH_LONG).show();
					}
				});

			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	
	private void saveProperties() {
		SharedPreferences pref = getSharedPreferences("TWIT", MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();

		editor.putBoolean("TwitLogin", BasicInfo.TwitLogin);
		editor.putString("TWIT_KEY_TOKEN", BasicInfo.TWIT_KEY_TOKEN);
		editor.putString("TWIT_KEY_TOKEN_SECRET", BasicInfo.TWIT_KEY_TOKEN_SECRET);
		editor.putString("TwitScreenName", BasicInfo.TwitScreenName);

		editor.commit();
	}

	private void loadProperties() {
		SharedPreferences pref = getSharedPreferences("TWIT", MODE_PRIVATE);

		BasicInfo.TwitLogin = pref.getBoolean("TwitLogin", false);
		BasicInfo.TWIT_KEY_TOKEN = pref.getString("TWIT_KEY_TOKEN", "");
		BasicInfo.TWIT_KEY_TOKEN_SECRET = pref.getString("TWIT_KEY_TOKEN_SECRET", "");
		BasicInfo.TwitScreenName = pref.getString("TwitScreenName", "");
	}
	    
    
    @Override
    public void onStart() {
        super.onStart();
        if (Session.getActiveSession() != null)
        	Session.getActiveSession().addCallback(statusCallback);
    }
   
    @Override
    public void onStop() {
        super.onStop();
        if (Session.getActiveSession() != null)
        	Session.getActiveSession().removeCallback(statusCallback);
    }

	@Override
	protected void onResume() {
		super.onResume();
		loadProperties();
	}

	@Override
	protected void onPause() {
		super.onPause();
		saveProperties();
	}
}
