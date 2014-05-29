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

/** 1. ���� �� ��ġ������ ������� Ȯ���ϴ� Activity*/
public class SensorsActivity extends Activity implements SensorEventListener, LocationListener {
	/** �ܼ��� Ŭ���� �̸�*/
	private static final String TAG = "SensorsActivity";
	/** �÷���ó�� �۾��� ���� ���������� Ȯ���ϴµ� ���*/
    private static final AtomicBoolean computing = new AtomicBoolean(false); 
    
    /** ��ġ������Ʈ�� �̷������ �ּ� �ð� �� �Ÿ�*/
    private static final int MIN_TIME = 30*1000;
    private static final int MIN_DISTANCE = 10;
    
    /** ȸ���ϴ� ���� ���Ǵ� �ӽ� �迭*/
    private static final float temp[] = new float[9];
    /** ���������� ȸ���� ���*/
    private static final float rotation[] = new float[9];
    /** �߷� ��*/
    private static final float grav[] = new float[3];
    /** �ڱ��� ��*/
    private static final float mag[] = new float[3];

    /** ����� ��ġ*/
    private static final Matrix worldCoord = new Matrix();
    /** ���ϰ� �ں��� ���̸� �����ϴµ� ���*/
    private static final Matrix magneticCompensatedCoord = new Matrix();
    private static final Matrix magneticNorthCompensation = new Matrix();
    /** X������ ���� 90�� ȸ���Ǿ��� �� ���*/
    private static final Matrix xAxisRotation = new Matrix();
    
    /** GeomagneticField�� �ν��Ͻ� ����*/
    private static GeomagneticField gmf = null;
    /** grav�� mag�� ���� �ο��н� ���� ����� ���*/
    private static float smooth[] = new float[3];
    /** SensorManager*/
    private static SensorManager sensorMgr = null;
    /** sensor�� ���*/
    private static List<Sensor> sensors = null;
    /** �⺻ ������ �߷°��ӵ� ������ �ڱ� ������ ����*/
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

        try {//�ý������κ��� ���� ���۷����� ��� ����
            sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);
            //�߷°��ӵ� ���� ����
            sensors = sensorMgr.getSensorList(Sensor.TYPE_ACCELEROMETER);
            
            if (sensors.size() > 0) 
            {// xAxis ���
            	sensorGrav = sensors.get(0);
            }
            //�ڱ� ���� ����
            sensors = sensorMgr.getSensorList(Sensor.TYPE_MAGNETIC_FIELD);
            
            if (sensors.size() > 0) 
            {// ??? ���
            	sensorMag = sensors.get(0);
            }
            // �� ���� ������ ���
            sensorMgr.registerListener(this, sensorGrav, SensorManager.SENSOR_DELAY_NORMAL);
            sensorMgr.registerListener(this, sensorMag, SensorManager.SENSOR_DELAY_NORMAL);
            // LocationManager �ý��ۿ��� ��� �� GPS Location ���� ���
            locationMgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, this);

            try {

                try {//gps, network �� ���� location ����� ���� ���
                    Location gps=locationMgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    Location network=locationMgr.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if(gps!=null)
                    {//�ֽ� ���� ���
                        onLocationChanged(gps);
                    }
                    else if (network!=null)
                    {//�ֽ� ���� ���
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
                
                // ���� ������ �缳�� - ���� �� - gmf�� ����(���� �ϱ���)�� 
                // �ں�(�׸����� �ؾȰ��� �ظ��� 40���Ͼ� �ú����Ʒ� �̵�)�� ���̸� �ǹ���
                angleY = Math.toRadians(-gmf.getDeclination());
                
                // magneticNorthCompensation�� ���� ����
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
                    // ���ϱ�
                    magneticNorthCompensation.prod(xAxisRotation);
                }
            } catch (Exception ex) {
            	ex.printStackTrace();
            }
        } catch (Exception ex1) {
            try {//���� �� �����̼� �ڿ� ����
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
            	//���� ����
                sensorMgr.unregisterListener(this, sensorGrav);
                sensorMgr.unregisterListener(this, sensorMag);
            } catch (Exception ex) {
            	ex.printStackTrace();
            }
            sensorMgr = null;

            try {
            	//gps ����
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
    	
    	// �߷°��ӵ� �� �ڱ� ������ ���� �ο��н����͸� ���� ���� �޾ƿ� ����
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

        // ���� ��ǥ�� �� temp�� ����
        SensorManager.getRotationMatrix(temp, null, grav, mag);
        // ��ǥ�� ���ġ, ���θ���
        SensorManager.remapCoordinateSystem(temp, SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X, rotation);
        // ��ǥ ��ȯ
        worldCoord.set(rotation[0], rotation[1], rotation[2], rotation[3], rotation[4], rotation[5], rotation[6], rotation[7], rotation[8]);

        magneticCompensatedCoord.toIdentity();
        synchronized (magneticNorthCompensation) {
            magneticCompensatedCoord.prod(magneticNorthCompensation);
        }
        // ���ϱ�
        magneticCompensatedCoord.prod(worldCoord);
        // ����
        magneticCompensatedCoord.invert(); 
        // ȸ���� ��ǥ ���� - ������ �浵�� ����� ���÷��̿��� X,Y��ǥ�� ��ȯ�ϴµ� ���
        ARData.setRotationMatrix(magneticCompensatedCoord);

        computing.set(false);
	}
	// ��Ȯ��???
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// ���� �����Ͱ� ������ Ȯ��
    	if (sensor==null) throw new NullPointerException();
		// ��ħ���� ����Ȯ �Ҷ� �α� ���
        if(sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD && accuracy==SensorManager.SENSOR_STATUS_UNRELIABLE) {
            Log.e(TAG, "Compass data unreliable");
        }
	}

	@Override
	public void onLocationChanged(Location location) {
		// �켱 ��ġ�� ������Ʈ
        ARData.setCurrentLocation(location);
        // ���ο� �����ͷ� �ٽ� ���
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
