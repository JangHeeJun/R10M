package com.r10m.gogoong.camera;

import java.util.Iterator;
import java.util.List;

import org.opencv.android.JavaCameraView;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
/** 카메라 프리뷰 */
public class CameraSurface extends JavaCameraView {
	private static final String TAG = "CameraSurface";
   // private static Camera camera = null;

//    public CameraSurface(Context context) {
//        super(context);
//    }
    
    public CameraSurface(Context context, AttributeSet attrs) {
		super(context, attrs);
		//JavaCameraSizeAccessor camSize = new JavaCameraSizeAccessor();
		Getwh gwh = new Getwh();
		this.connectCamera(gwh.getWidth(), gwh.getHeight());
	}
    
    
    
    public class Getwh{
    
    	public int bestw;
    	public int besth;
    	
    	public Getwh(){
    		int w = 480;
    		int h = 320;
    		
    		Camera.Parameters parameters = mCamera.getParameters();
		      try {
		          List<Camera.Size> supportedSizes = null;
		
		          supportedSizes = CameraCompatibility.getSupportedPreviewSizes(parameters);
		
		          float ff = (float)w/h;
		
		          float bff = 0;
		          Iterator<Camera.Size> itr = supportedSizes.iterator();
		
		          while(itr.hasNext()) {
		              Camera.Size element = itr.next();
		              float cff = (float)element.width/element.height;
		
		              if ((ff-cff <= ff-bff) && (element.width <= w) && (element.width >= bestw)) {
		                  bff=cff;
		                  bestw = element.width;
		                  besth = element.height;
		              }
		          } 
		
		          if ((bestw == 0) || (besth == 0)){
		              bestw = 480;
		              besth = 320;
		          }
		          //parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
		      } catch (Exception ex) {
		      }
    	}
    	
    	public int getWidth(){
    		return bestw;
    	}
    	
    	public int getHeight(){
    		return besth;
    	}
    }
    
    
    
    
    
    
    
    
    
    
//	public CameraSurface(Context context, AttributeSet attrs, int defStyle) {
//		super(context, attrs, defStyle);
//	}
	
//	Camera.AutoFocusCallback cb = new Camera.AutoFocusCallback() {
//		@Override
//		public void onAutoFocus(boolean success, Camera camera) {
//		}
//	};
//
//	public void surfaceCreated(SurfaceHolder holder) {
//        try {
//            if (mCamera != null) {
//                try {
//                	mCamera.stopPreview();
//                } catch (Exception ex) {
//                	ex.printStackTrace();
//                }
//                try {
//                	mCamera.release();
//                } catch (Exception ex) {
//                	ex.printStackTrace();
//                }
//                mCamera = null;
//            }
//
//            mCamera = Camera.open();
//            mCamera.setPreviewDisplay(holder);
//        } catch (Exception ex) {
//            try {
//                if (mCamera != null) {
//                    try {
//                    	mCamera.stopPreview();
//                    } catch (Exception ex1) {
//                    	ex.printStackTrace();
//                    }
//                    try {
//                    	mCamera.release();
//                    } catch (Exception ex2) {
//                    	ex.printStackTrace();
//                    }
//                    mCamera = null;
//                }
//            } catch (Exception ex3) {
//            	ex.printStackTrace();
//            }
//        }
//    }
//
//    public void surfaceDestroyed(SurfaceHolder holder) {
//        try {
//            if (mCamera != null) {
//                try {
//                	mCamera.stopPreview();
//                } catch (Exception ex) {
//                	ex.printStackTrace();
//                }
//                try {
//                	mCamera.release();
//                } catch (Exception ex) {
//                	ex.printStackTrace();
//                }
//                mCamera = null;
//            }
//        } catch (Exception ex) {
//        	ex.printStackTrace();
//        }
//    }
//
//    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
//        try {
//            Camera.Parameters parameters = mCamera.getParameters();
//            try {
//                List<Camera.Size> supportedSizes = null;
//
//                supportedSizes = CameraCompatibility.getSupportedPreviewSizes(parameters);
//
//                float ff = (float)w/h;
//
//                float bff = 0;
//                int bestw = 0;
//                int besth = 0;
//                Iterator<Camera.Size> itr = supportedSizes.iterator();
//
//                while(itr.hasNext()) {
//                    Camera.Size element = itr.next();
//                    float cff = (float)element.width/element.height;
//
//                    if ((ff-cff <= ff-bff) && (element.width <= w) && (element.width >= bestw)) {
//                        bff=cff;
//                        bestw = element.width;
//                        besth = element.height;
//                    }
//                } 
//
//                if ((bestw == 0) || (besth == 0)){
//                    bestw = 480;
//                    besth = 320;
//                }
//                parameters.setPreviewSize(bestw, besth);
//                //parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
//            } catch (Exception ex) {
//                parameters.setPreviewSize(480 , 320);
//            }
//                       
//
//            mCamera.setParameters(parameters);
//            mCamera.startPreview();
//            mCamera.autoFocus(cb);
//        } catch (Exception ex) {
//        	ex.printStackTrace();
//        }
//    }    
}