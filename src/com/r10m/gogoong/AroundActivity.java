package com.r10m.gogoong;

import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.LinearLayout;

import com.nhn.android.maps.NMapActivity;
import com.nhn.android.maps.NMapController;
import com.nhn.android.maps.NMapOverlay;
import com.nhn.android.maps.NMapOverlayItem;
import com.nhn.android.maps.NMapView;
import com.nhn.android.maps.NMapView.OnMapStateChangeListener;
import com.nhn.android.maps.NMapView.OnMapViewTouchEventListener;
import com.nhn.android.maps.maplib.NGeoPoint;
import com.nhn.android.maps.nmapmodel.NMapError;
import com.nhn.android.maps.overlay.NMapPOIdata;
import com.nhn.android.maps.overlay.NMapPOIitem;
import com.nhn.android.mapviewer.overlay.NMapCalloutOverlay;
import com.nhn.android.mapviewer.overlay.NMapOverlayManager;
import com.nhn.android.mapviewer.overlay.NMapPOIdataOverlay;
import com.nhn.android.mapviewer.overlay.NMapPOIdataOverlay.OnStateChangeListener;

public class AroundActivity extends NMapActivity implements OnMapStateChangeListener, OnStateChangeListener, OnMapViewTouchEventListener {

	// API-KEY
	public static final String API_KEY = "94cbae417dfa6aa0f0b9f103e04dd903";
	// 네이버 맵 객체
	NMapView mMapView = null;
	// 맵 컨트롤러
	NMapController mMapController = null;
	// 맵을 추가할 레이아웃
	LinearLayout MapContainer;
	
	// 오버레이의 리소스를 제공하기 위한 객체
	AroundMapViewerResourceProvider mMapViewerResourceProvider = null;
	// 오버레이 관리자
	NMapOverlayManager mOverlayManager;
	private OnStateChangeListener onPOIdataStateChangeListener;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.aroundmap);
		
		/******************* 지도 초기화 시작 ********************/
		// 네이버 지도를 넣기 위한 LinearLayout 컴포넌트
		MapContainer = (LinearLayout) findViewById(R.id.MapContainer);

		// 네이버 지도 객체 생성
		mMapView = new NMapView(this);
		
		// 지도 객체로부터 컨트롤러 추출
		mMapController = mMapView.getMapController();

		// 네이버 지도 객체에 APIKEY 지정
		mMapView.setApiKey(API_KEY);

		// 생성된 네이버 지도 객체를 LinearLayout에 추가시킨다.
		MapContainer.addView(mMapView);

		// 지도를 터치할 수 있도록 옵션 활성화
		mMapView.setClickable(true);
		
		// 확대/축소를 위한 줌 컨트롤러 표시 옵션 활성화
		mMapView.setBuiltInZoomControls(true, null);	

		// 지도에 대한 상태 변경 이벤트 연결
		mMapView.setOnMapStateChangeListener(this);
		/******************* 지도 초기화 끝 ********************/
		
		
		/******************* 오버레이 관련 코드 시작 ********************/
		// 오버레이 리소스 관리객체 할당
		mMapViewerResourceProvider = new AroundMapViewerResourceProvider(this);

		// 오버레이 관리자 추가
		mOverlayManager = new NMapOverlayManager(this, mMapView, mMapViewerResourceProvider);
		
		// 오버레이들을 관리하기 위한 id값 생성
		int markerId = AroundMapPOIflagType.PIN;

		// 표시할 위치 데이터를 지정한다. -- 마지막 인자가 오버레이를 인식하기 위한 id값
		NMapPOIdata poiData = new NMapPOIdata(2, mMapViewerResourceProvider);
		poiData.beginPOIdata(2);
		poiData.addPOIitem(126.976916, 37.575997, getString(R.string.map1_key), markerId, 0);
		poiData.addPOIitem(126.976930, 37.578588, getString(R.string.map2_key), markerId, 0);
		poiData.addPOIitem(126.991003, 37.579476, getString(R.string.map3_key), markerId, 0);
		poiData.addPOIitem(126.983696, 37.575003, getString(R.string.map4_key), markerId, 0);
		
		poiData.addPOIitem(126.993932, 37.574910, getString(R.string.map5_key), markerId, 0);
		// 종묘
		poiData.addPOIitem(126.985874, 37.577321, getString(R.string.map6_key), markerId, 0);
		//북촌로
		poiData.addPOIitem(126.967915, 37.575876, getString(R.string.map7_key), markerId, 0);
		//사직공원
		poiData.addPOIitem(126.956435, 37.573886, getString(R.string.map8_key), markerId, 0);
		//서대문독립공원
		poiData.addPOIitem(126.968215, 37.571335, getString(R.string.map9_key), markerId, 0);
		//경희궁
		poiData.addPOIitem(126.970570, 37.570417, getString(R.string.map10_key), markerId, 0);
		//서울역사박물관
		poiData.addPOIitem(126.988294, 37.571263, getString(R.string.map11_key), markerId, 0);
		//탑골공원
		poiData.addPOIitem(127.001811, 37.582331, getString(R.string.map12_key), markerId, 0);
		//대학로
		poiData.addPOIitem(126.974839, 37.565767, getString(R.string.map13_key), markerId, 0);
		//덕수궁
		poiData.addPOIitem(126.977843, 37.569169, getString(R.string.map14_key), markerId, 0);
		//청계광장
		poiData.addPOIitem(126.977870, 37.566167, getString(R.string.map15_key), markerId, 0);
		//서울시청 및 서울광장
		poiData.addPOIitem(126.976768, 37.574028, getString(R.string.map16_key), markerId, 0);
		//광화문광장
		
		poiData.endPOIdata();
		//getString(R.string.map4_key) .java에서 프리퍼런스에 있는 스트링을 불러올 때는 이와 같이 사용한다.
		//@string/name 은 xml에서 불러올때 사용한다.

		// 위치 데이터를 사용하여 오버레이 생성
		NMapPOIdataOverlay poiDataOverlay = mOverlayManager.createPOIdataOverlay(poiData, null);
		
		// id값이 0으로 지정된 모든 오버레이가 표시되고 있는 위치로 지도의 중심과 ZOOM을 재설정
		poiDataOverlay.showAllPOIdata(0);
		
		
		
		// 오버레이 이벤트 등록(마커 클릭후 말풍선 크기 조절)
		poiDataOverlay.setOnStateChangeListener(onPOIdataStateChangeListener);
		/******************* 오버레이 관련 코드 끝 ********************/
	}
	
	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
	}
		

	/**
	 * 지도가 초기화된 후 호출된다.
	 * 정상적으로 초기화되면 errorInfo 객체는 null이 전달되며,
	 * 초기화 실패 시 errorInfo객체에 에러 원인이 전달된다
	 */
	@Override
	public void onMapInitHandler(NMapView mapview, NMapError errorInfo) {
		if (errorInfo == null) { // success
			mMapController.setMapCenter(new NGeoPoint(126.976916, 37.575997), 13);
		} else { // fail
			android.util.Log.e("NMAP", "onMapInitHandler: error=" + errorInfo.toString());
		}
	}

	/**
	 * 지도 레벨 변경 시 호출되며 변경된 지도 레벨이 파라미터로 전달된다.
	 */
	@Override
	public void onZoomLevelChange(NMapView mapview, int level) {}

	/**
	 * 지도 중심 변경 시 호출되며 변경된 중심 좌표가 파라미터로 전달된다.
	 */
	@Override
	public void onMapCenterChange(NMapView mapview, NGeoPoint center) {}

	/**
	 * 지도 애니메이션 상태 변경 시 호출된다.
	 * animType : ANIMATION_TYPE_PAN or ANIMATION_TYPE_ZOOM
	 * animState : ANIMATION_STATE_STARTED or ANIMATION_STATE_FINISHED
	 */
	@Override
	public void onAnimationStateChange(NMapView arg0, int animType, int animState) {}

	@Override
	public void onMapCenterChangeFine(NMapView arg0) {}

	/** 오버레이가 클릭되었을 때의 이벤트 */
//	@Override
//	public NMapCalloutOverlay onCreateCalloutOverlay(NMapOverlay arg0,
//			NMapOverlayItem arg1, Rect arg2) {
//		Toast.makeText(this, arg1.getTitle(), Toast.LENGTH_SHORT).show();
//		return null;
//	}

	public void onFocusChanged(NMapPOIdataOverlay poiDataOverlay, NMapPOIitem item) {
		if (item != null) {
			Log.i("NMAP", "onFocusChanged: " + item.toString());
		} else {
			Log.i("NMAP", "onFocusChanged: ");
		}
	}

	public NMapCalloutOverlay onCreateCalloutOverlay(NMapOverlay itemOverlay, NMapOverlayItem overlayItem, Rect itemBounds) {
		// set your callout overlay
		return new AroundMapCalloutBasicOverlay(itemOverlay, overlayItem, itemBounds);
	}
	
	/**OnMapViewTouchEventListener*/
	@Override
	public void onLongPress(NMapView arg0, MotionEvent arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onLongPressCanceled(NMapView arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onScroll(NMapView arg0, MotionEvent arg1, MotionEvent arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSingleTapUp(NMapView arg0, MotionEvent arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTouchDown(NMapView arg0, MotionEvent arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onCalloutClick(NMapPOIdataOverlay arg0, NMapPOIitem arg1) {
		// TODO Auto-generated method stub
		
	}
}