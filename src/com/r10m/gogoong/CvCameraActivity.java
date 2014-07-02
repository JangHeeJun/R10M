package com.r10m.gogoong;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.JavaCameraView;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.core.Core;
import org.opencv.core.Mat;

import com.facebook.AppEventsLogger;
import com.r10m.gogoong.component.Marker;
import com.r10m.gogoong.filter.Filter;
import com.r10m.gogoong.filter.ar.ImageDetectionFilter;
import com.r10m.gogoong.resource.ARData;
import com.r10m.gogoong.wizet.VerticalSeekBar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.Camera.CameraInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.WindowManager;
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

/** 3. SensorsActivity */
public class CvCameraActivity extends SensorsActivity implements CvCameraViewListener2{
	
	//manual activity
	SharedPreferences mainPreferences;
		
    private static final String TAG = "CvCameraActivity";
    
    protected static WakeLock wakeLock = null;
    protected static JavaCameraView camScreen = null;
  
    //opencv
    private boolean flag;
    
    // A key for storing the index of the active camera.
    private static final String STATE_CAMERA_INDEX = "cameraIndex";
    
    // Keys for storing the indices of the active filters.
    private static final String STATE_IMAGE_DETECTION_FILTER_INDEX =
            "imageDetectionFilterIndex";
    
    // The filters.
    private Filter mImageDetectionFilters;
    
    // The indices of the active filters.
    private int mImageDetectionFilterIndex;
    
    // The index of the active camera.
    private int mCameraIndex;
    
    // Whether the active camera is front-facing.
    // If so, the camera view should be mirrored.
    private boolean mIsCameraFrontFacing;
    
    // The number of cameras on the device.
    private int mNumCameras;
    private boolean mIsRunning;
    private Mat rgba;
    private Button btn;
    private ProgressBar prog;
    private Marker marker;
    private String imgName;
    
    // The OpenCV loader callback.
    private BaseLoaderCallback mLoaderCallback =
    		new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(final int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    Log.d(TAG, "OpenCV loaded successfully");
                    camScreen.enableView();
                    
					try {
						mImageDetectionFilters=new ImageDetectionFilter(
								CvCameraActivity.this,
								CvCameraActivity.this.getResources().getIdentifier(imgName,
										"drawable", CvCameraActivity.this.getPackageName()));
					} catch (IOException e) {
						e.printStackTrace();
						break;
					}
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };
    
    
    

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.cvcamera);
              
        final Window window = getWindow();
        window.addFlags(
        		WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        if (savedInstanceState != null) {
            mCameraIndex = savedInstanceState.getInt(
            		STATE_CAMERA_INDEX, 0);
            mImageDetectionFilterIndex = savedInstanceState.getInt(
            		STATE_IMAGE_DETECTION_FILTER_INDEX, 0);
        } else {
            mCameraIndex = 0;
            mImageDetectionFilterIndex = 0;
        }
        
        if (Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.GINGERBREAD) {
            CameraInfo cameraInfo = new CameraInfo();
            Camera.getCameraInfo(mCameraIndex, cameraInfo);
            mIsCameraFrontFacing = 
                    (cameraInfo.facing ==
                    CameraInfo.CAMERA_FACING_FRONT);
            mNumCameras = Camera.getNumberOfCameras();
        } else { // pre-Gingerbread
            // Assume there is only 1 camera and it is rear-facing.
            mIsCameraFrontFacing = false;
            mNumCameras = 1;
        }
        
        
        btn = (Button)findViewById(R.id.button_cv);
        btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(v.getId() == R.id.button_cv){
					finish();
				}
			}
		});
        
        prog = (ProgressBar)findViewById(R.id.progressBar_cv);
        
        camScreen = (JavaCameraView)findViewById(R.id.surface_cv);
        camScreen.setCvCameraViewListener(this);

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "DimScreen");
    }

	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
	}
	
	
	@Override
	public void onResume() {
		super.onResume();
		AppEventsLogger.activateApp(this);
		wakeLock.acquire();
		
    	marker = ARData.getMarkers().get(9);
		prog.setVisibility(View.VISIBLE);
		
		imgName = "haechi";
		Log.e(TAG, "=================="+imgName);
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3,
                this, mLoaderCallback);
	}

	@Override
	public void onPause() {
		super.onPause();
		wakeLock.release();
		if (camScreen != null) {
			camScreen.disableView();
        }
	}	
	
	//opencv
	@Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the current camera index.
        savedInstanceState.putInt(STATE_CAMERA_INDEX, mCameraIndex);
        
        // Save the current filter indices.
        savedInstanceState.putInt(STATE_IMAGE_DETECTION_FILTER_INDEX,
        		mImageDetectionFilterIndex);
        
        super.onSaveInstanceState(savedInstanceState);
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (camScreen != null) {
        	camScreen.disableView();
        }
    }
  
    @Override
    public void onCameraViewStarted(final int width,
    		final int height) {
    	flag = false;
    	Log.e(TAG, "Start  "+flag);
    }

    @Override
    public void onCameraViewStopped() {
    	flag = false;
    	Log.e(TAG, "end  "+flag);
    }

    @Override
    public Mat onCameraFrame(final CvCameraViewFrame inputFrame) {
        rgba = inputFrame.rgba();
        
        Log.e(TAG, "Frame  "+mIsRunning+"======"+mImageDetectionFilterIndex);
        
        if(!mIsRunning){
        	mIsRunning = true;
        	if(flag){
            	flag = false;
            	Log.e(TAG, "if  "+flag);
            	Intent intent = new Intent(CvCameraActivity.this,DetailPopUp.class);
            	intent.putExtra("name", marker.getName());
            	intent.putExtra("detail", marker.getDetail());
            	startActivity(intent);	
            }else{
            	 // Apply the active filters.
            	flag = mImageDetectionFilters.apply(rgba, rgba);
                Log.e(TAG, "else  "+flag);
            }
        	
	        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
	            @Override
	            public void run() {
	            	prog.setVisibility(View.GONE);
	            	mIsRunning = false;
	            	imgName = marker.getName().toLowerCase();
	            	Log.e(TAG, "=================="+imgName);
	            	//marker = ARData.getMarkers().get(0);
	            	
	            }
	        }, 1500);
        }        
        
        
        if (mIsCameraFrontFacing) {
            // Mirror (horizontally flip) the preview.
            Core.flip(rgba, rgba, 1);
        }
        
        return rgba;
    }
}
