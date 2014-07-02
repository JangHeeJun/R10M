package com.r10m.gogoong;

import java.text.DecimalFormat;

import com.facebook.AppEventsLogger;
import com.r10m.gogoong.camera.CameraSurface;
import com.r10m.gogoong.component.Marker;
import com.r10m.gogoong.resource.ARData;
import com.r10m.gogoong.wizet.VerticalSeekBar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

/** 3. SensorsActivity를 확장, 터치 구현한 액티비티 */
public class AugmentedActivity extends SensorsActivity {
	
	//manual activity 설정 저장
	SharedPreferences preferences;
	
	
    private static final String TAG = "AugmentedActivity";
    
    private static final DecimalFormat FORMAT = new DecimalFormat("#.##");
    private static final int ZOOMBAR_BACKGROUND_COLOR = Color.argb(125,55,55,55);
    private static final String END_TEXT = FORMAT.format(AugmentedActivity.MAX_ZOOM)+" km";
    private static final int END_TEXT_COLOR = Color.WHITE;

    protected static WakeLock wakeLock = null;
    protected static CameraSurface camScreen = null;
    protected static VerticalSeekBar myZoomBar = null;
    protected static TextView endLabel = null;
    protected static LinearLayout zoomLayout = null;
    protected static AugmentedView augmentedView = null;

    public static final float MAX_ZOOM = 100; //in KM
    public static final float ONE_PERCENT = MAX_ZOOM/100f;
    public static final float TEN_PERCENT = 10f*ONE_PERCENT;
    public static final float TWENTY_PERCENT = 2f*TEN_PERCENT;
    public static final float EIGHTY_PERCENTY = 4f*TWENTY_PERCENT;

    public static boolean useCollisionDetection = true;
    public static boolean showRadar = true;
    public static boolean showZoomBar = true;
    
    //카메라 프리뷰에서 터치 인식
    private int startX;
    private int startY;
    private int endX;
    private int endY;
    SurfaceHolder mpHolder;
    
    //카메라 연동하여 이미지 받기
    ImageView mImage;
    
    private Button btn;
    private ProgressBar prog;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //layout 없이 .자바로만
        //camScreen = new CameraSurface(this);
        //setContentView(camScreen);
        
        //layout 적용시 SurfaceHolder 사용

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.camera);
        
        camScreen = (CameraSurface)findViewById(R.id.surface_camera);
        mpHolder = camScreen.getHolder();
        mpHolder.addCallback(camScreen);
        mpHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS); 
        
        // 마커 띄우기 위한 augmentedView
        augmentedView = new AugmentedView(this);
        LayoutParams augLayout = new LayoutParams(  LayoutParams.WRAP_CONTENT, 
                                                    LayoutParams.WRAP_CONTENT);
        addContentView(augmentedView,augLayout);
        
        // 시크바 줌 인 아웃시 레이아웃
        zoomLayout = new LinearLayout(this);
        zoomLayout.setVisibility((showZoomBar)?LinearLayout.VISIBLE:LinearLayout.GONE);
        zoomLayout.setOrientation(LinearLayout.VERTICAL);
        zoomLayout.setPadding(5, 5, 5, 5);
        zoomLayout.setBackgroundColor(ZOOMBAR_BACKGROUND_COLOR);

        endLabel = new TextView(this);
        endLabel.setText(END_TEXT);
        endLabel.setTextColor(END_TEXT_COLOR);
        LinearLayout.LayoutParams zoomTextParams =  new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        zoomLayout.addView(endLabel, zoomTextParams);

        myZoomBar = new VerticalSeekBar(this);
        myZoomBar.setMax(100);
        myZoomBar.setProgress(50);
        myZoomBar.setOnSeekBarChangeListener(myZoomBarOnSeekBarChangeListener);
        LinearLayout.LayoutParams zoomBarParams =  new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.MATCH_PARENT);
        zoomBarParams.gravity = Gravity.CENTER_HORIZONTAL;
        zoomLayout.addView(myZoomBar, zoomBarParams);

        FrameLayout.LayoutParams frameLayoutParams = new FrameLayout.LayoutParams(  LayoutParams.WRAP_CONTENT, 
                                                                                    LayoutParams.MATCH_PARENT, 
                                                                                    Gravity.RIGHT);
        addContentView(zoomLayout,frameLayoutParams);
        
        updateDataOnZoom();

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "DimScreen");
        
        //openCV 
        btn = (Button)findViewById(R.id.button_camera);
        btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(v.getId() == R.id.button_camera){
		        	Intent intent = new Intent(AugmentedActivity.this,CvCameraActivity.class);
		        	camScreen.surfaceDestroyed(mpHolder);
		        	startActivity(intent);
		        	overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
				}
			}
		});
        
        prog = (ProgressBar)findViewById(R.id.progressBar_camera);
        prog.setVisibility(View.INVISIBLE);
    }

	@Override
	public void onResume() {
		super.onResume();
		AppEventsLogger.activateApp(this);
		wakeLock.acquire();
		
		
		// ManualActivity intent start!!!
		preferences = PreferenceManager.getDefaultSharedPreferences(this);

		String turn= preferences.getString("on/off", "on");
			
		if(turn.equals("on")){
			Intent ManualIntent = new Intent(AugmentedActivity.this,ManualActivity.class);
			startActivity(ManualIntent);
			   
			SharedPreferences.Editor editor = preferences.edit();
			editor.putString("on/off","off");
			editor.commit();  	
			   
			 //설명창 완료 부분
		}		
	}

	@Override
	public void onPause() {
		super.onPause();
		wakeLock.release();
	}	
	
	
	@Override
    public void onSensorChanged(SensorEvent evt) {
        super.onSensorChanged(evt);

        if (    evt.sensor.getType() == Sensor.TYPE_ACCELEROMETER || 
                evt.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
        {
            augmentedView.postInvalidate();
        }
    }
    
    private OnSeekBarChangeListener myZoomBarOnSeekBarChangeListener = new OnSeekBarChangeListener() {
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            updateDataOnZoom();
            camScreen.invalidate();
        }

        public void onStartTrackingTouch(SeekBar seekBar) {
            //Not used
        }

        public void onStopTrackingTouch(SeekBar seekBar) {
            updateDataOnZoom();
            camScreen.invalidate();
        }
    };

    private static float calcZoomLevel(){
        int myZoomLevel = myZoomBar.getProgress();
        float out = 0;

        float percent = 0;
        if (myZoomLevel <= 25) {
            percent = myZoomLevel/25f;
            out = ONE_PERCENT*percent;
        } else if (myZoomLevel > 25 && myZoomLevel <= 50) {
            percent = (myZoomLevel-25f)/25f;
            out = ONE_PERCENT+(TEN_PERCENT*percent);
        } else if (myZoomLevel > 50 && myZoomLevel <= 75) {
            percent = (myZoomLevel-50f)/25f;
            out = TEN_PERCENT+(TWENTY_PERCENT*percent);
        } else {
            percent = (myZoomLevel-75f)/25f;
            out = TWENTY_PERCENT+(EIGHTY_PERCENTY*percent);
        }
        return out;
    }

    protected void updateDataOnZoom() {
        float zoomLevel = calcZoomLevel();
        ARData.setRadius(zoomLevel);
        ARData.setZoomLevel(FORMAT.format(zoomLevel));
        ARData.setZoomProgress(myZoomBar.getProgress());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
    	//마커 터치시
    	for (Marker marker : ARData.getMarkers()) {
	        if (marker.handleClick(event.getX(), event.getY())) {
	            if (event.getAction() == MotionEvent.ACTION_UP) markerTouched(marker);
	            return true;
	        }
	    }
		
		//카메라 연동하기
		if(event.getAction()==MotionEvent.ACTION_DOWN){	
			startX=(int) event.getX();	
			startY=(int) event.getY();
		}
		if(event.getAction()==MotionEvent.ACTION_UP){
			endX=(int) event.getX();
			endY=(int) event.getY();
			
	    	if(startY>endY+200 && Math.abs(startX-endX)<50){
	    		//카메라 리소스 반환
	    		camScreen.surfaceDestroyed(mpHolder);
	    		// 기본 카메라 앱 이동
    			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); 
    			startActivityForResult(intent, 0);
    		}
	    	startX=endX;
	    	startY=endY;
	    }
    	return super.onTouchEvent(event);
    }
    
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (resultCode == RESULT_OK) {
			if(requestCode==0){
				// 저장된 사진을 비트맵으로 변환
				Bitmap cImage=(Bitmap)data.getExtras().get("data");
				// SNS 선택 이동
				Intent intent=new Intent(this,PostingPopup.class);
				intent.putExtra("cImage", cImage);
				startActivityForResult(intent, 0);
			}
		}
	}

	// 구현 필요
	protected void markerTouched(Marker marker) {
		Log.w(TAG,"markerTouched() not implemented.");
	}
}
