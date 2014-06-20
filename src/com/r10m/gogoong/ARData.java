package com.r10m.gogoong;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import com.r10m.gogoong.component.Marker;
import com.r10m.gogoong.util.Matrix;

import android.location.Location;
import android.util.Log;
/** app이 구동되는데 필수적인 데이터를 저장하는 저장 클래스
 * 전역 컨트롤 클래스 */
public class ARData {
	 private static final String TAG = "ARData";
	    /** 마커의 Hashmap과 이름을 저장 */
		private static final Map<String,Marker> markerList = new ConcurrentHashMap<String,Marker>();
		/** 캐쉬 역할 */
	    private static final List<Marker> cache = new CopyOnWriteArrayList<Marker>();
	    /** 상태가 깨끗한지 판별 */
	    private static final AtomicBoolean dirty = new AtomicBoolean(false);
	    /** 위치데이터를 저장하는 배열 */
	    private static final float[] locationArray = new float[3];
	    /** ATL 마커와 같은 기본 위치를 저장 */
	    public static final Location hardFix = new Location("ATL");
	    static {
	        hardFix.setLatitude(0);
	        hardFix.setLongitude(0);
	        hardFix.setAltitude(1);
	    }
	    /** Lock가 추가된 변수는 이 변수를 동기화 하기 위한 변수 */
	    private static final Object radiusLock = new Object();
	    /** 레이더의 반경 */
	    private static float radius = new Float(20);
	    /** 현재의 줌 레벨 */
	    private static String zoomLevel = new String();
	    private static final Object zoomProgressLock = new Object();
	    /** 앱에서 줌 과정 */
	    private static int zoomProgress = 0;
	    /** 현재 위치 */
	    private static Location currentLocation = hardFix;
	    /** 회전 매트릭스를 저장 */
	    private static Matrix rotationMatrix = new Matrix();
	    
	    private static final Object azimuthLock = new Object();
	    private static float azimuth = 0;
	    private static final Object pitchLock = new Object();
	    private static float pitch = 0;
	    private static final Object rollLock = new Object();
	    private static float roll = 0;
		private static int count;

	    
	    /** 모든 메서드는 데이터가 앱의 다른부분에 의해 변경되지 않았는지 검사하는 
	     * 동기화 블록을 사용해서 메소드의 이름과 같은 변수를 설정하거나 값을 얻어오는 역할을 한다. */
	    
	    
	    public static void setZoomLevel(String zoomLevel) {
	    	if (zoomLevel==null) throw new NullPointerException();
	    	
	    	synchronized (ARData.zoomLevel) {
	    	    ARData.zoomLevel = zoomLevel;
	    	}
	    }
	    
	    public static void setZoomProgress(int zoomProgress) {
	        synchronized (ARData.zoomProgressLock) {
	            if (ARData.zoomProgress != zoomProgress) {
	                ARData.zoomProgress = zoomProgress;
	                if (dirty.compareAndSet(false, true)) {
	                    Log.v(TAG, "Setting DIRTY flag!");
	                    cache.clear();
	                }
	            }
	        }
	    }
	    
	    public static void setRadius(float radius) {
	        synchronized (ARData.radiusLock) {
	            ARData.radius = radius;
	        }
	    }

	    public static float getRadius() {
	        synchronized (ARData.radiusLock) {
	            return ARData.radius;
	        }
	    }

	    public static void setCurrentLocation(Location currentLocation) {
	    	if (currentLocation==null) throw new NullPointerException();
	    	
	    	Log.d(TAG, "current location. location="+currentLocation.toString());
	    	synchronized (currentLocation) {
	    	    ARData.currentLocation = currentLocation;
	    	}
	        onLocationChanged(currentLocation);
	    }
	    
	    public static Location getCurrentLocation() {
	        synchronized (ARData.currentLocation) {
	            return ARData.currentLocation;
	        }
	    }

	    public static void setRotationMatrix(Matrix rotationMatrix) {
	        synchronized (ARData.rotationMatrix) {
	            ARData.rotationMatrix = rotationMatrix;
	        }
	    }

	    public static Matrix getRotationMatrix() {
	        synchronized (ARData.rotationMatrix) {
	            return rotationMatrix;
	        }
	    }
	    /** 마커를 모두 돌려서 모든 마커의 값을 반환시킨다. */
	    public static List<Marker> getMarkers() {
	        if (dirty.compareAndSet(true, false)) {
	            Log.v(TAG, "DIRTY flag found, resetting all marker heights to zero.");
	            for(Marker ma : markerList.values()) {
	                ma.getLocation().get(locationArray);
	                locationArray[1]=ma.getInitialY();
	                ma.getLocation().set(locationArray);
	            }

	            Log.v(TAG, "Populating the cache.");
	            List<Marker> copy = new ArrayList<Marker>();
	            copy.addAll(markerList.values());
	            Collections.sort(copy,comparator);
	            cache.clear();
	            cache.addAll(copy);
	        }
	        count = Collections.unmodifiableList(cache).size();
	        return Collections.unmodifiableList(cache);
	    }

	    public static int getMarkersSize(){
	    	return count;
	    }
	    
	    public static void setAzimuth(float azimuth) {
	        synchronized (azimuthLock) {
	            ARData.azimuth = azimuth;
	        }
	    }

	    public static float getAzimuth() {
	        synchronized (azimuthLock) {
	            return ARData.azimuth;
	        }
	    }

	    public static void setPitch(float pitch) {
	        synchronized (pitchLock) {
	            ARData.pitch = pitch;
	        }
	    }

	    public static float getPitch() {
	        synchronized (pitchLock) {
	            return ARData.pitch;
	        }
	    }

	    public static void setRoll(float roll) {
	        synchronized (rollLock) {
	            ARData.roll = roll;
	        }
	    }

	    public static float getRoll() {
	        synchronized (rollLock) {
	            return ARData.roll;
	        }
	    }
	    
	    /** 마커간의 거리를 비교할때 사용 */
	    private static final Comparator<Marker> comparator = new Comparator<Marker>() {
	        public int compare(Marker arg0, Marker arg1) {
	            return Double.compare(arg0.getDistance(),arg1.getDistance());
	        }
	    };
	    
	    public static void clearMarkers(){
	    	markerList.clear();
	    }
	    
	    
	    /** 전달받은 컬렉션으로부터 새로운 마커를 추가 */
	    public static void addMarkers(Collection<Marker> markers) {
	    	if (markers==null) throw new NullPointerException();

	    	if (markers.size()<=0) return;
	    	
	    	Log.d(TAG, "New markers, updating markers. new markers="+markers.toString());
	    	for(Marker marker : markers) {
	    	    if (!markerList.containsKey(marker.getName())) {
	    	        marker.calcRelativePosition(ARData.getCurrentLocation());
	    	        markerList.put(marker.getName(),marker);
	    	    }
	    	}

	    	if (dirty.compareAndSet(false, true)) {
	    	    Log.v(TAG, "Setting DIRTY flag!");
	    	    cache.clear();
	    	}
	    }
	    /** 새로운 위치에 대한 마커의 상대적인 위치를 업데이트 */
	    private static void onLocationChanged(Location location) {
	        Log.d(TAG, "New location, updating markers. location="+location.toString());
	        for(Marker ma: markerList.values()) {
	            ma.calcRelativePosition(location);
	        }

	        if (dirty.compareAndSet(false, true)) {
	            Log.v(TAG, "Setting DIRTY flag!");
	            cache.clear();
	        }
	    }
}
