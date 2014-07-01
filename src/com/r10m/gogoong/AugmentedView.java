package com.r10m.gogoong;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;

import android.content.Context;
import android.graphics.Canvas;
import android.view.View;

import com.r10m.gogoong.component.Marker;
import com.r10m.gogoong.component.Radar;
import com.r10m.gogoong.resource.ARData;

/** 2. 마커와 레이더 그리는 뷰 */
public class AugmentedView extends View {
	/** 현재 화면이 그려지고 있는지 검사하는 플래그 */
    private static final AtomicBoolean drawing = new AtomicBoolean(false);

    /** 레이더 */
    private static final Radar radar = new Radar();
    /** 마커의 위치를 저장 */
    private static final float[] locationArray = new float[3];
    /** 화면을 그리는 동안 임시로 사용되는 캐시 */
    private static final List<Marker> cache = new ArrayList<Marker>();
    /** 데이터 소스가 업데이트 되었을때 사용 */
    private static final TreeSet<Marker> updated = new TreeSet<Marker>();
    /** 화면상에서 마커의 위치를 조절해서 겹치지 않도록 배치하는데 사용 */
    private static final int COLLISION_ADJUSTMENT = 100;

    public AugmentedView(Context context) {
        super(context);
    }

	@Override
    protected void onDraw(Canvas canvas) {
    	if (canvas==null) return;
    	// 모든 마커 캐쉬에 저장
        if (drawing.compareAndSet(false, true)) { 
	        List<Marker> collection = ARData.getMarkers();

            cache.clear();
            for (Marker m : collection) {
                m.update(canvas, 0, 0);
                if (m.isOnRadar()) cache.add(m);
	        }
            // 사본 저장
            collection = cache;
            // 위치 조정
	        if (AugmentedActivity.useCollisionDetection) adjustForCollisions(canvas,collection);
	        // 마커 표시
	        ListIterator<Marker> iter = collection.listIterator(collection.size());
	        while (iter.hasPrevious()) {
	            Marker marker = iter.previous();
	            marker.draw(canvas);
	        }
	        // 레이더에 마커 업데이트
	        if (AugmentedActivity.showRadar) radar.draw(canvas);
	        drawing.set(false);
        }
    }
	
	/** 마커들이 겹치지 않도록 위치를 수정하는 메서드 */
	private static void adjustForCollisions(Canvas canvas, List<Marker> collection) {
	    updated.clear();
	    // 겹치는지 확인
        for (Marker marker1 : collection) {
        	// 안겹칠때
            if (updated.contains(marker1) || !marker1.isInView()) continue;

            int collisions = 1;
            for (Marker marker2 : collection) {
            	// 안겹칠때
                if (marker1.equals(marker2) || updated.contains(marker2) || !marker2.isInView()) continue;
                // 겹칠때
                if (marker1.isMarkerOnMarker(marker2)) {
                    marker2.getLocation().get(locationArray);
                    float y = locationArray[1];
                    float h = collisions*COLLISION_ADJUSTMENT;
                    locationArray[1] = y+h;
                    marker2.getLocation().set(locationArray);
                    marker2.update(canvas, 0, 0);
                    collisions++;
                    updated.add(marker2);
                }
            }
            updated.add(marker1);
        }
	}
}
