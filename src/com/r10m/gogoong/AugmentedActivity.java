package com.r10m.gogoong;

import java.text.DecimalFormat;

import com.r10m.gogoong.camera.CameraSurface;
import com.r10m.gogoong.component.Marker;
import com.r10m.gogoong.wizet.VerticalSeekBar;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

/** 3. SensorsActivity를 확장, 터치 구현한 액티비티 */
public class AugmentedActivity extends SensorsActivity implements OnTouchListener {
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
    
    
    private float startX;
    private float startY;
    private float endX;
    private float endY;
    

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //layout 없이 .자바로만
        //camScreen = new CameraSurface(this);
        //setContentView(camScreen);
        
        //layout 적용시 SurfaceHolder 사용
        setContentView(R.layout.camera);
        camScreen = (CameraSurface)findViewById(R.id.surface1);
        SurfaceHolder mpHolder = camScreen.getHolder();
        mpHolder.addCallback(camScreen);
        mpHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS); 
        
        augmentedView = new AugmentedView(this);
        augmentedView.setOnTouchListener(this);
        LayoutParams augLayout = new LayoutParams(  LayoutParams.WRAP_CONTENT, 
                                                    LayoutParams.WRAP_CONTENT);
        addContentView(augmentedView,augLayout);
        
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
        LinearLayout.LayoutParams zoomBarParams =  new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.FILL_PARENT);
        zoomBarParams.gravity = Gravity.CENTER_HORIZONTAL;
        zoomLayout.addView(myZoomBar, zoomBarParams);

        FrameLayout.LayoutParams frameLayoutParams = new FrameLayout.LayoutParams(  LayoutParams.WRAP_CONTENT, 
                                                                                    LayoutParams.FILL_PARENT, 
                                                                                    Gravity.RIGHT);
        addContentView(zoomLayout,frameLayoutParams);
        
        updateDataOnZoom();

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "DimScreen");
    }

	@Override
	public void onResume() {
		super.onResume();

		wakeLock.acquire();
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

	public boolean onTouch(View view, MotionEvent me) {
		switch(me.getAction()){
		    case MotionEvent.ACTION_DOWN:
		    	startX=me.getX();
		    	startY=me.getY();
		    	break;
		    case MotionEvent.ACTION_UP:
		    	endX=me.getX();
		    	endY=me.getY();
		    	break;
	    }
		
		if(Math.abs(startX-endX)<100 && Math.abs(startY-endY)>100){
			Toast t = Toast.makeText(getApplicationContext(), "카메라 성공", Toast.LENGTH_SHORT);
	        t.setGravity(Gravity.CENTER, 0, 0);
	        t.show();
	        
//			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); 
//			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
//			startActivity(intent); 
		}else{
			Toast t = Toast.makeText(getApplicationContext(), "터치 인식", Toast.LENGTH_SHORT);
	        t.setGravity(Gravity.CENTER, 0, 0);
	        t.show();
		}
		
		
		
		for (Marker marker : ARData.getMarkers()) {
	        if (marker.handleClick(me.getX(), me.getY())) {
	            if (me.getAction() == MotionEvent.ACTION_UP) markerTouched(marker);
	            return true;
	        }
	    }
	    
		return super.onTouchEvent(me);
	};
	// 구현 필요
	protected void markerTouched(Marker marker) {
		Log.w(TAG,"markerTouched() not implemented.");
	}
}
