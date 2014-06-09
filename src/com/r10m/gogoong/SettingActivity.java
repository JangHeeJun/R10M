package com.r10m.gogoong;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationBuilder;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

/** 설정 정보를 저장 및 제어하는 Activity */
public class SettingActivity extends PreferenceActivity {

	SharedPreferences mainPreference;
	
	//facebook - 귀찮아서 걍 메인에 있던거 끌고와서 하드하게 코딩.. ㅠㅠ
	private Bundle mSavedInstanceState;
	private boolean bProgressLogin;
	private String userId;
	private String userName;
	private Session session;
	private String facebookUrl;

	private Session.StatusCallback statusCallback = new Session.StatusCallback() {

		@Override
        public void call(Session session, SessionState state, Exception exception) {
        	if (state == SessionState.OPENED
        			|| state == SessionState.OPENED_TOKEN_UPDATED) {
	            // log in
	            checkFacebookLogin();
	        }else if (state == SessionState.CLOSED){
	        	 // log out
	        }else if (state == SessionState.CLOSED_LOGIN_FAILED) {
	            // 여러 이유로 인증실패.
	            bProgressLogin = false;
	        }
        }
    };
	    
	
	//twitter
    private Handler mHandler = new Handler();
    String twitUrl;
    ProgressBar progBar;
	
	
	
	@Override
	public void onBackPressed() {
		Intent intent = new Intent(SettingActivity.this, MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
		super.onBackPressed();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		
		
		
		// 1. \res\xml\preferences.xml로 부터 Preference 계층구조를 읽어와
		// 2. 이 PreferenceActivity의 계층구조로 지정/표현 하고
		// 3. \data\data\패키지이름\shared_prefs\패키지이름_preferences.xml 생성
		// 4. 이 후 Preference에 변경 사항이 생기면 파일에 자동 저장
		
		Locale systemLocale = getResources().getConfiguration().locale;
	    String strLanguage = systemLocale.getLanguage();
		
		mainPreference = PreferenceManager.getDefaultSharedPreferences(this);
		setLocale(mainPreference.getString("LanguageList", strLanguage));
		setContentView(R.layout.setting);
		addPreferencesFromResource(R.xml.settings);
		
	   	ListPreference searchEngineSettings = (ListPreference)findPreference("LanguageList");
	    searchEngineSettings.setSummary(searchEngineSettings.getValue());
	    searchEngineSettings.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
	         
	         @Override
	         public boolean onPreferenceChange(Preference preference, Object newValue) {
	        	 Intent intent = getIntent();
	        	 overridePendingTransition(0, 0);
	        	 intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	            startActivity(intent);
	       }
       });   

       // progress bar
       progBar = (ProgressBar)findViewById(R.id.progressBar_setting);
       
       // facebook 설정창
       Preference facebook = (Preference)findPreference("Facebook");      
       facebook.setOnPreferenceClickListener(new OnPreferenceClickListener() {
		
			@Override
			public boolean onPreferenceClick(Preference preference) {
				facebookLogin();
				if (session != null && session.isOpened()) {
			        // If the session is open, make an API call to get user data
			        // and define a new callback to handle the response
			        Request request = Request.newMeRequest(session, new Request.GraphUserCallback() {
			            @Override
			            public void onCompleted(GraphUser user, Response response) {
			                // If the response is successful
			                if (session == Session.getActiveSession()) {
			                    if (user != null) {
			                        userId = user.getId();//user id
			                        userName = user.getName();//user's profile name
			                        new FacebookProfileImageURLTask().execute();
			                    }   
			                }   
			            }   
			        }); 
			        Request.executeBatchAsync(request);
					return true;
				}	    		
				return false;
			}
		});
       
       
       // twitter 설정창
       Preference twitter = (Preference)findPreference("Twitter");
       twitter.setOnPreferenceClickListener(new OnPreferenceClickListener() {
		
			@Override
			public boolean onPreferenceClick(Preference preference) {
				twitLogin();
				if (BasicInfo.TwitLogin) {
					new TwitProfileImageURLTask().execute();
					return true;
		    	 }
				return false;
			}
		});
       
       Intent intent2 = new Intent(SettingActivity.this, TermsOfService.class);
       Preference serviceterms = findPreference("Serviceterms");
       serviceterms.setIntent(intent2);
       
       Intent intent3 = new Intent(SettingActivity.this, ContactUs.class);
       Preference contactus = findPreference("Contactus");
       contactus.setIntent(intent3);
	}
	
   	public void setLocale(String character) {
    	Locale locale = new Locale(character); 
    	Locale.setDefault(locale);
    	Configuration config = new Configuration();
    	config.locale = locale;
    	getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
    }
   	
   	
   	
   	
   	//facebook 연동 시작 -----------------------------------------------------------------
   	class FacebookProfileImageURLTask extends AsyncTask<Void, Void, String>{

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progBar.setVisibility(View.VISIBLE);
		}

		@Override
		protected String doInBackground(Void... params) {
				facebookUrl = parse("http://graph.facebook.com/" +userId + "/picture?type=large&redirect=false");
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			try {
				Intent intent = new Intent(SettingActivity.this,SettingSNSLoginActivity.class);
				intent.putExtra("target", "facebook");
				intent.putExtra("name", userName);
				intent.putExtra("url", facebookUrl);
				progBar.setVisibility(View.GONE);
				startActivity(intent);
				overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
			} catch (Exception e) {
				e.printStackTrace();
			}
			super.onPostExecute(result);
		}	
	}
   	
   	
   	public String parse(String url) {
		if (url==null) throw new NullPointerException();
		InputStream is = null;
		JSONObject jsonArray = null;
		JSONObject json = null;
		String result = "";
		String imgurl = "";
			
		try {
			HttpClient httpClient = new DefaultHttpClient();
			HttpGet httpGet = new HttpGet(url);
			HttpResponse httpResponse = httpClient.execute(httpGet);
			HttpEntity httpEntity = httpResponse.getEntity();
				
			is = httpEntity.getContent();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			StringBuilder sb = new StringBuilder();				
			String line = null;
			while((line = br.readLine()) != null){
				sb.append(line+"\n");
			}
			
			is.close();
			result = sb.toString();	
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    	try {
    		jsonArray = new JSONObject(result);
    		json = jsonArray.getJSONObject("data");
    		imgurl = json.getString("url");
    	} catch (JSONException e) {
    	    e.printStackTrace();
    	}		
    	return imgurl;
	}
   
   	private void facebookLogin() {
    	bProgressLogin = true;
    	session = Session.getActiveSession();
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
    	    //Toast.makeText(this, getString(R.string.facebookLogin_main), Toast.LENGTH_SHORT).show();
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
  	//facebook 연동 끝 -----------------------------------------------------------------
    
    //tiwtter 연동 시작 -----------------------------------------------------------------
   	
   	class TwitProfileImageURLTask extends AsyncTask<Void, Void, String>{

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progBar.setVisibility(View.VISIBLE);
		}

		@Override
		protected String doInBackground(Void... params) {
			try {
				if (BasicInfo.TwitLogin) {
					twitUrl = BasicInfo.TwitInstance.showUser(BasicInfo.TwitScreenName).getProfileImageURLHttps();
				}
			} catch (TwitterException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			try {
				Intent intent = new Intent(SettingActivity.this,SettingSNSLoginActivity.class);
				intent.putExtra("target", "twitter");
				intent.putExtra("name", BasicInfo.TwitScreenName);
				intent.putExtra("url", twitUrl);
				progBar.setVisibility(View.GONE);
				startActivity(intent);
				overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
			} catch (Exception e) {
				e.printStackTrace();
			}
			super.onPostExecute(result);
		}
		
		
	}
   	
    private void twitLogin() {
		// 인증 되어있을때
		if (BasicInfo.TwitLogin) {
			
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
	//twitter 연동 끝----------------------------------------------------------------
    
   	
   	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
	}
   	
   	@Override
    public void onStart() {
        super.onStart();
        if (Session.getActiveSession() != null)
        	Session.getActiveSession().addCallback(statusCallback);
        twitLoadProperties();
    }
   
    @Override
    public void onStop() {
        super.onStop();
        if (Session.getActiveSession() != null)
        	Session.getActiveSession().removeCallback(statusCallback);
		twitSaveProperties();
    }

	@Override
	protected void onResume() {
		super.onResume();
        progBar.setVisibility(View.INVISIBLE);
	}

   	
}
