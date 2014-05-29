package com.r10m.gogoong;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.r10m.gogoong.util.LowPassFilter;
import com.r10m.gogoong.util.Matrix;

import android.app.Activity;
import android.content.Context;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

/** 1. 센서 및 위치정보를 얻기위해 확장하는 Activity*/
public class SensorsActivity extends Activity implements SensorEventListener, LocationListener {
	/** 단순한 클래스 이름*/
	private static final String TAG = "SensorsActivity";
	/** 플래그처럼 작업이 현재 진행중인지 확인하는데 사용*/
    private static final AtomicBoolean computing = new AtomicBoolean(false); 
    
    /** 위치업데이트가 이루어지는 최소 시간 및 거리*/
    private static final int MIN_TIME = 30*1000;
    private static final int MIN_DISTANCE = 10;
    
    /** 회전하는 동안 사용되는 임시 배열*/
    private static final float temp[] = new float[9];
    /** 최종적으로 회전된 행렬*/
    private static final float rotation[] = new float[9];
    /** 중력 값*/
    private static final float grav[] = new float[3];
    /** 자기장 값*/
    private static final float mag[] = new float[3];

    /** 기기의 위치*/
    private static final Matrix worldCoord = new Matrix();
    /** 진북과 자북의 차이를 보정하는데 사용*/
    private static final Matrix magneticCompensatedCoord = new Matrix();
    private static final Matrix magneticNorthCompensation = new Matrix();
    /** X축으로 부터 90도 회전되었을 때 행렬*/
    private static final Matrix xAxisRotation = new Matrix();
    
    /** GeomagneticField의 인스턴스 저장*/
    private static GeomagneticField gmf = null;
    /** grav와 mag의 값에 로우패스 필터 적용시 사용*/
    private static float smooth[] = new float[3];
    /** SensorManager*/
    private static SensorManager sensorMgr = null;
    /** sensor의 목록*/
    private static List<Sensor> sensors = null;
    /** 기본 설정된 중력가속도 센서와 자기 센서를 저장*/
    private static Sensor sensorGrav = null;
    private static Sensor sensorMag = null;
    /** LocationManager*/
    private static LocationManager locationMgr = null;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

	@Override
    public void onStart() {
        super.onStart();
        
        double angleX = Math.toRadians(-90);
        double angleY = Math.toRadians(-90);

        xAxisRotation.set( 	1f, 
			                0f, 
			                0f, 
			                0f, 
			                (float) Math.cos(angleX), 
			                (float) -Math.sin(angleX), 
			                0f, 
			                (float) Math.sin(angleX), 
			                (float) Math.cos(angleX));

        try {//시스템으로부터 센서 레퍼런스를 얻어 저장
            sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);
            //중력가속도 센서 저장
            sensors = sensorMgr.getSensorList(Sensor.TYPE_ACCELEROMETER);
            
            if (sensors.size() > 0) 
            {// xAxis 얻기
            	sensorGrav = sensors.get(0);
            }
            //자기 센서 저장
            sensors = sensorMgr.getSensorList(Sensor.TYPE_MAGNETIC_FIELD);
            
            if (sensors.size() > 0) 
            {// ??? 얻기
            	sensorMag = sensors.get(0);
            }
            // 각 센서 리스너 등록
            sensorMgr.registerListener(this, sensorGrav, SensorManager.SENSOR_DELAY_NORMAL);
            sensorMgr.registerListener(this, sensorMag, SensorManager.SENSOR_DELAY_NORMAL);
            // LocationManager 시스템에서 얻기 및 GPS Location 정보 얻기
            locationMgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, this);

            try {

                try {//gps, network 를 통해 location 저장된 정보 얻기
                    Location gps=locationMgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    Location network=locationMgr.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if(gps!=null)
                    {//최신 정보 얻기
                        onLocationChanged(gps);
                    }
                    else if (network!=null)
                    {//최신 정보 얻기
                        onLocationChanged(network);
                    }
                    else
                    {// ???
                        onLocationChanged(ARData.hardFix);
                    }
                } catch (Exception ex2) {
                    onLocationChanged(ARData.hardFix);
                }
                
                // ???
                gmf = new GeomagneticField((float) ARData.getCurrentLocation().getLatitude(), 
                                           (float) ARData.getCurrentLocation().getLongitude(),
                                           (float) ARData.getCurrentLocation().getAltitude(), 
                                           System.currentTimeMillis());
                
                // 음의 편차로 재설정 - 라디안 값 - gmf의 진북(실제 북극점)과 
                // 자북(그린랜드 해안가쪽 해마다 40마일씩 시베리아로 이동)의 차이를 의미함
                angleY = Math.toRadians(-gmf.getDeclination());
                
                // magneticNorthCompensation를 최초 설정
                synchronized (magneticNorthCompensation) {

                    magneticNorthCompensation.toIdentity();

                    magneticNorthCompensation.set( (float) Math.cos(angleY), 
                                                  0f, 
                                                  (float) Math.sin(angleY), 
                                                  0f, 
                                                  1f, 
                                                  0f, 
                                                  (float) -Math.sin(angleY), 
                                                  0f, 
                                                  (float) Math.cos(angleY));
                    // 곱하기
                    magneticNorthCompensation.prod(xAxisRotation);
                }
            } catch (Exception ex) {
            	ex.printStackTrace();
            }
        } catch (Exception ex1) {
            try {//센서 및 로케이션 자원 해제
                if (sensorMgr != null) {
                    sensorMgr.unregisterListener(this, sensorGrav);
                    sensorMgr.unregisterListener(this, sensorMag);
                    sensorMgr = null;
                }
                if (locationMgr != null) {
                    locationMgr.removeUpdates(this);
                    locationMgr = null;
                }
            } catch (Exception ex2) {
            	ex2.printStackTrace();
            }
        }
    }
	
	@Override
    protected void onStop() {
        super.onStop();

        try {
            try {
            	//센서 해제
                sensorMgr.unregisterListener(this, sensorGrav);
                sensorMgr.unregisterListener(this, sensorMag);
            } catch (Exception ex) {
            	ex.printStackTrace();
            }
            sensorMgr = null;

            try {
            	//gps 해제
                locationMgr.removeUpdates(this);
            } catch (Exception ex) {
            	ex.printStackTrace();
            }
            locationMgr = null;
        } catch (Exception ex) {
        	ex.printStackTrace();
        }
    }


    public void onProviderDisabled(String provider) {
        //Not Used
    }
	
    public void onProviderEnabled(String provider) {
        //Not Used
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
        //Not Used
    }

	@Override
	public void onSensorChanged(SensorEvent event) {
if (!computing.compareAndSet(false, true)) return;
    	
    	// 중력가속도 와 자기 센서로 부터 로우패스필터를 거쳐 값을 받아와 저장
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            smooth = LowPassFilter.filter(0.5f, 1.0f, event.values, grav);
            grav[0] = smooth[0];
            grav[1] = smooth[1];
            grav[2] = smooth[2];
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            smooth = LowPassFilter.filter(2.0f, 4.0f, event.values, mag);
            mag[0] = smooth[0];
            mag[1] = smooth[1];
            mag[2] = smooth[2];
        }

        // 실제 좌표를 얻어서 temp에 저장
        SensorManager.getRotationMatrix(temp, null, grav, mag);
        // 좌표를 재배치, 가로모드로
        SensorManager.remapCoordinateSystem(temp, SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X, rotation);
        // 좌표 변환
        worldCoord.set(rotation[0], rotation[1], rotation[2], rotation[3], rotation[4], rotation[5], rotation[6], rotation[7], rotation[8]);

        magneticCompensatedCoord.toIdentity();
        synchronized (magneticNorthCompensation) {
            magneticCompensatedCoord.prod(magneticNorthCompensation);
        }
        // 곱하기
        magneticCompensatedCoord.prod(worldCoord);
        // 반전
        magneticCompensatedCoord.invert(); 
        // 회전된 좌표 저장 - 위도와 경도를 기기의 디스플레이에서 X,Y좌표로 변환하는데 사용
        ARData.setRotationMatrix(magneticCompensatedCoord);

        computing.set(false);
	}
	// 정확도???
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// 센서 데이터가 널인지 확인
    	if (sensor==null) throw new NullPointerException();
		// 나침반이 부정확 할때 로그 출력
        if(sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD && accuracy==SensorManager.SENSOR_STATUS_UNRELIABLE) {
            Log.e(TAG, "Compass data unreliable");
        }
	}

	@Override
	public void onLocationChanged(Location location) {
		// 우선 위치를 업데이트
        ARData.setCurrentLocation(location);
        // 새로운 데이터로 다시 계산
        gmf = new GeomagneticField((float) ARData.getCurrentLocation().getLatitude(), 
                (float) ARData.getCurrentLocation().getLongitude(),
                (float) ARData.getCurrentLocation().getAltitude(), 
                System.currentTimeMillis());
        
        double angleY = Math.toRadians(-gmf.getDeclination());

        synchronized (magneticNorthCompensation) {
            magneticNorthCompensation.toIdentity();
            
            magneticNorthCompensation.set((float) Math.cos(angleY), 
                                         0f, 
                                         (float) Math.sin(angleY), 
                                         0f, 
                                         1f, 
                                         0f, 
                                         (float) -Math.sin(angleY), 
                                         0f, 
                                         (float) Math.cos(angleY));
    
            magneticNorthCompensation.prod(xAxisRotation);
        }
	}
}
