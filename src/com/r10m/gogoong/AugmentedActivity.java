package com.r10m.gogoong;

import java.text.DecimalFormat;

import com.facebook.AppEventsLogger;
import com.r10m.gogoong.camera.CameraSurface;
import com.r10m.gogoong.component.Marker;
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
import android.view.Window;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

/** 3. SensorsActivity�� Ȯ��, ��ġ ������ ��Ƽ��Ƽ */
public class AugmentedActivity extends SensorsActivity {
	
	//manual activity ���� ����
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
    
    //ī�޶� �����信�� ��ġ �ν�
    private int startX;
    private int startY;
    private int endX;
    private int endY;
    SurfaceHolder mpHolder;
    //ī�޶� �����Ͽ� �̹��� �ޱ�
    ImageView mImage;
//	String mPath;
    

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //layout ���� .�ڹٷθ�
        //camScreen = new CameraSurface(this);
        //setContentView(camScreen);
        
        //layout ����� SurfaceHolder ���

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.camera);
        camScreen = (CameraSurface)findViewById(R.id.surface1);
        mpHolder = camScreen.getHolder();
        mpHolder.addCallback(camScreen);
        mpHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS); 
        
        // ��Ŀ ���� ���� augmentedView
        augmentedView = new AugmentedView(this);
        LayoutParams augLayout = new LayoutParams(  LayoutParams.WRAP_CONTENT, 
                                                    LayoutParams.WRAP_CONTENT);
        addContentView(augmentedView,augLayout);
        
        // ��ũ�� �� �� �ƿ��� ���̾ƿ�
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
			   
			 //����â �Ϸ� �κ�
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
    	//��Ŀ ��ġ��
    	for (Marker marker : ARData.getMarkers()) {
	        if (marker.handleClick(event.getX(), event.getY())) {
	            if (event.getAction() == MotionEvent.ACTION_UP) markerTouched(marker);
	            return true;
	        }
	    }
		
		//ī�޶� �����ϱ�
		if(event.getAction()==MotionEvent.ACTION_DOWN){	
			startX=(int) event.getX();	
			startY=(int) event.getY();
		}
		if(event.getAction()==MotionEvent.ACTION_UP){
			endX=(int) event.getX();
			endY=(int) event.getY();
			
	    	if(startY>endY+200 && Math.abs(startX-endX)<50){
	    		//ī�޶� ���ҽ� ��ȯ
	    		camScreen.surfaceDestroyed(mpHolder);
	    		// �⺻ ī�޶� �� �̵�
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
				// ����� ������ ��Ʈ������ ��ȯ
				Bitmap cImage=(Bitmap)data.getExtras().get("data");
				// SNS ���� �̵�
				Intent intent=new Intent(this,PostingPopup.class);
				intent.putExtra("cImage", cImage);
				startActivityForResult(intent, 0);
			}
		}
	}

	// ���� �ʿ�
	protected void markerTouched(Marker marker) {
		Log.w(TAG,"markerTouched() not implemented.");
	}
}
