package com.r10m.gogoong.camera;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.view.Display;
import android.view.WindowManager;
/** app이 모든 버전과 호환성을 유지하고 예전 API의 제약을 회피하도록 하는 클래스 */
public class CameraCompatibility {
	private static Method getSupportedPreviewSizes = null;
	private static Method mDefaultDisplay_getRotation = null;
	
	static {
		initCompatibility();
	};
	/** 카메라 기본 프리뷰 크기를 480*320 으로 적절하게 설정 */
	private static void initCompatibility() {
		try {
			getSupportedPreviewSizes = Camera.Parameters.class.getMethod("getSupportedPreviewSizes", new Class[] { } );
			mDefaultDisplay_getRotation = Display.class.getMethod("getRotation", new Class[] { } );
		} catch (NoSuchMethodException nsme) {
		}
	}
	/** 기기의 회전각을 구하는데 사용 */
	public static int getRotation(Activity activity) {
	     int result = 1;
	     try {
    	     Display display = ((WindowManager) activity.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
    	     Object retObj = mDefaultDisplay_getRotation.invoke(display);
    	     if(retObj != null) result = (Integer) retObj;
	     } catch (Exception ex) {
	         ex.printStackTrace();
	     }
	     return result;
	}
	/** 기기에서 사용 가능한 미리보기 화면의 크기 목록을 반환 */
	public static List<Camera.Size> getSupportedPreviewSizes(Camera.Parameters params) {
		List<Camera.Size> retList = null;

		try {
			Object retObj = getSupportedPreviewSizes.invoke(params);
			if (retObj != null) {
				retList = (List<Camera.Size>)retObj;
			}
		} catch (InvocationTargetException ite) {
			Throwable cause = ite.getCause();
			if (cause instanceof RuntimeException) {
				throw (RuntimeException) cause;
			} else if (cause instanceof Error) {
				throw (Error) cause;
			} else {
				throw new RuntimeException(ite);
			}
		} catch (IllegalAccessException ie) {
			ie.printStackTrace();
		}
		return retList;
	}
}