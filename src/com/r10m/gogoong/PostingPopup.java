package com.r10m.gogoong;

import java.util.Arrays;

import com.facebook.AppEventsLogger;
import com.facebook.FacebookRequestError;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphObject;
import com.facebook.widget.FacebookDialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;

/** fb,tw 포스팅 하기 위한 공유하기 팝업창 */
public class PostingPopup extends Activity {
	//layout
	private ImageButton twiter;
	private ImageButton facebook;
	Bitmap cImage;
	
	//facebook에서 도움주는 헬퍼클래스
	private UiLifecycleHelper uiHelper;
	//Session 관리를 위한 callback 매서드
    private Session.StatusCallback statusCallback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
        }
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        uiHelper = new UiLifecycleHelper(this, statusCallback);
        uiHelper.onCreate(savedInstanceState);
        
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
                WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        
        setContentView(R.layout.posting_popup);
        
        // image 받아오기
        Intent intentFb = getIntent();
		cImage=(Bitmap)intentFb.getExtras().get("cImage");

		//twitter
        twiter		=(ImageButton)findViewById(R.id.img_btn_twit);
        twiter.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (BasicInfo.TwitLogin) {
					Intent intentTw=new Intent(PostingPopup.this,TwitPosting.class);
					intentTw.putExtra("cImage", cImage);
					startActivityForResult(intentTw, 0);
				}else{
					Toast.makeText(PostingPopup.this, "twitter 로그인을 하셔야 합니다.", Toast.LENGTH_SHORT).show();
				}
			}
		});
        
        //facebook
        facebook	=(ImageButton)findViewById(R.id.img_btn_fb); 
        facebook.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Session session = Session.getActiveSession();
		        if (session != null && session.isOpened()) {
		        	postPhoto(cImage);
		        } else {
		        	Toast.makeText(PostingPopup.this, "facebook 로그인을 하셔야 합니다.", Toast.LENGTH_SHORT).show();
		        }
				
			}
		});
    }
	
	
	//facebook posting
	 private void postPhoto(Bitmap cImage) {
	        
        if (FacebookDialog.canPresentShareDialog(this,
                FacebookDialog.ShareDialogFeature.PHOTOS)) {
            FacebookDialog shareDialog = createShareDialogBuilderForPhoto(cImage).build();
            uiHelper.trackPendingDialogCall(shareDialog.present());
        } else if (hasPublishPermission()) {
            Request request = Request.newUploadPhotoRequest(Session.getActiveSession(), cImage, new Request.Callback() {
                @Override
                public void onCompleted(Response response) {
                    showPublishResult(getString(R.string.photo_post), response.getGraphObject(), response.getError());
                }
            });
            request.executeAsync();
        }
    }
    
    private FacebookDialog.PhotoShareDialogBuilder createShareDialogBuilderForPhoto(Bitmap... photos) {
    	return new FacebookDialog.PhotoShareDialogBuilder(this)
                .addPhotos(Arrays.asList(photos));
        
    }
    
    private boolean hasPublishPermission() {
        Session session = Session.getActiveSession();
        return session != null && session.getPermissions().contains("publish_actions");
    }
    
    private interface GraphObjectWithId extends GraphObject {
        String getId();
    }

    private void showPublishResult(String message, GraphObject result, FacebookRequestError error) {
        String title = null;
        String alertMessage = null;
        if (error == null) {
            title = getString(R.string.success);
            String id = result.cast(GraphObjectWithId.class).getId();
            alertMessage = getString(R.string.successfully_posted_post, message, id);
        } else {
            title = getString(R.string.error);
            alertMessage = error.getErrorMessage();
        }

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(alertMessage)
                .setPositiveButton(R.string.ok, null)
                .show();
    }



    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	super.onSaveInstanceState(outState);
    	uiHelper.onSaveInstanceState(outState);
    }

    @Override
	protected void onDestroy() {
		super.onDestroy();
        uiHelper.onDestroy();
	}
    
    @Override
	public void onResume() {
		super.onResume();
		uiHelper.onResume();
        AppEventsLogger.activateApp(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		uiHelper.onPause();
	}    
}
