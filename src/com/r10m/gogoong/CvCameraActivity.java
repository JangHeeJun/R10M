package com.r10m.gogoong;

import java.io.IOException;
import java.util.Locale;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.JavaCameraView;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.core.Core;
import org.opencv.core.Mat;

import com.r10m.gogoong.filter.Filter;
import com.r10m.gogoong.filter.ar.ImageDetectionFilter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

/** 3. SensorsActivity를 확장, 터치 구현한 액티비티 */
public class CvCameraActivity extends SensorsActivity implements CvCameraViewListener2{
	
	//manual activity 설정 저장
	private SharedPreferences preferences;
	private static String locale = "en";

    private static final String TAG = "CvCameraActivity";
    
    protected static WakeLock wakeLock = null;
    protected static JavaCameraView mCameraView = null;

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
    private Button btnCv;
	private LinearLayout cvLayout;
    
    // The OpenCV loader callback.
    private BaseLoaderCallback mLoaderCallback =
    		new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(final int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    Log.d(TAG, "OpenCV loaded successfully");
                    mCameraView.enableView();
                    try {
						mImageDetectionFilters=new ImageDetectionFilter(
								CvCameraActivity.this,R.drawable.hadchi1);
					} catch (IOException e) {
						e.printStackTrace();
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
  
        preferences = PreferenceManager.getDefaultSharedPreferences(this);	//설정내용읽어옴
    	setLocale(preferences.getString("LanguageList", "ko"));	//언어설정
        
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
        
        mCameraView = new JavaCameraView(this, mCameraIndex);
        mCameraView.setCvCameraViewListener(this);
        setContentView(mCameraView);
      
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "DimScreen");
        
        setCv();     
    }

	//언어 설정
    public void setLocale(String character) {
    	locale = character;
    	Locale locale = new Locale(character); 
    	Locale.setDefault(locale);
    	Configuration config = new Configuration();
    	config.locale = locale;
    	getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
    }
	
    private void setCv(){
    	
        cvLayout = new LinearLayout(this);
        cvLayout.setOrientation(LinearLayout.HORIZONTAL);
        
        btnCv = new Button(this);
        btnCv.setBackgroundResource(R.drawable.opencv2);
        cvLayout.addView(btnCv, new LayoutParams(100, 100));
        
        FrameLayout.LayoutParams frameLayoutParams = new FrameLayout.LayoutParams(
        		LayoutParams.WRAP_CONTENT, 
                LayoutParams.MATCH_PARENT, 
                Gravity.RIGHT);
        frameLayoutParams.setMarginEnd(75);
        addContentView(cvLayout, frameLayoutParams);
       
        btnCv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(v.getId() == R.id.button_camera){
		        	finish();
				}
			}
		});
	}
    
    
	@Override
    public void onPause() {
        if (mCameraView != null) {
            mCameraView.disableView();
        }
        super.onPause();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3,
                this, mLoaderCallback);
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mCameraView != null) {
            mCameraView.disableView();
        }
    }
	
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
            	intent.putExtra("name", "해치");
            	intent.putExtra("detail", "해치입니다.");
            	startActivity(intent);	
            }else{
            	 // Apply the active filters.
                flag = mImageDetectionFilters.apply(rgba, rgba);
                Log.e(TAG, "else  "+flag);
            }
        	
	        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
	            @Override
	            public void run() {
	            	mIsRunning = false;
	            }
	        }, 1000);
        }        
        
        
        if (mIsCameraFrontFacing) {
            // Mirror (horizontally flip) the preview.
            Core.flip(rgba, rgba, 1);
        }
        
        return rgba;
    }
	
	@Override
	public void finish() {
		super.finish();
    	overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
	}
	
	
	
}
