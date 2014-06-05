package com.r10m.gogoong;

import android.graphics.Rect;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.nhn.android.maps.NMapActivity;
import com.nhn.android.maps.NMapController;
import com.nhn.android.maps.NMapOverlay;
import com.nhn.android.maps.NMapOverlayItem;
import com.nhn.android.maps.NMapView;
import com.nhn.android.maps.NMapView.OnMapStateChangeListener;
import com.nhn.android.maps.maplib.NGeoPoint;
import com.nhn.android.maps.nmapmodel.NMapError;
import com.nhn.android.maps.overlay.NMapPOIdata;
import com.nhn.android.mapviewer.overlay.NMapCalloutOverlay;
import com.nhn.android.mapviewer.overlay.NMapOverlayManager;
import com.nhn.android.mapviewer.overlay.NMapOverlayManager.OnCalloutOverlayListener;
import com.nhn.android.mapviewer.overlay.NMapPOIdataOverlay;

public class AroundActivity extends NMapActivity implements OnMapStateChangeListener, OnCalloutOverlayListener {

	// API-KEY
	public static final String API_KEY = "94cbae417dfa6aa0f0b9f103e04dd903";
	// ���̹� �� ��ü
	NMapView mMapView = null;
	// �� ��Ʈ�ѷ�
	NMapController mMapController = null;
	// ���� �߰��� ���̾ƿ�
	LinearLayout MapContainer;
	
	// ���������� ���ҽ��� �����ϱ� ���� ��ü
	AroundMapViewerResourceProvider mMapViewerResourceProvider = null;
	// �������� ������
	NMapOverlayManager mOverlayManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.aroundmap);
		
		/******************* ���� �ʱ�ȭ ���� ********************/
		// ���̹� ������ �ֱ� ���� LinearLayout ������Ʈ
		MapContainer = (LinearLayout) findViewById(R.id.MapContainer);

		// ���̹� ���� ��ü ����
		mMapView = new NMapView(this);
		
		// ���� ��ü�κ��� ��Ʈ�ѷ� ����
		mMapController = mMapView.getMapController();

		// ���̹� ���� ��ü�� APIKEY ����
		mMapView.setApiKey(API_KEY);

		// ������ ���̹� ���� ��ü�� LinearLayout�� �߰���Ų��.
		MapContainer.addView(mMapView);

		// ������ ��ġ�� �� �ֵ��� �ɼ� Ȱ��ȭ
		mMapView.setClickable(true);
		
		// Ȯ��/��Ҹ� ���� �� ��Ʈ�ѷ� ǥ�� �ɼ� Ȱ��ȭ
		mMapView.setBuiltInZoomControls(true, null);	

		// ������ ���� ���� ���� �̺�Ʈ ����
		mMapView.setOnMapStateChangeListener(this);
		/******************* ���� �ʱ�ȭ �� ********************/
		
		
		/******************* �������� ���� �ڵ� ���� ********************/
		// �������� ���ҽ� ������ü �Ҵ�
		mMapViewerResourceProvider = new AroundMapViewerResourceProvider(this);

		// �������� ������ �߰�
		mOverlayManager = new NMapOverlayManager(this, mMapView, mMapViewerResourceProvider);
		
		// �������̵��� �����ϱ� ���� id�� ����
		int markerId = AroundMapPOIflagType.PIN;

		// ǥ���� ��ġ �����͸� �����Ѵ�. -- ������ ���ڰ� �������̸� �ν��ϱ� ���� id��
		NMapPOIdata poiData = new NMapPOIdata(2, mMapViewerResourceProvider);
		poiData.beginPOIdata(2);
		poiData.addPOIitem(126.977000, 37.576866, "��ȭ���Դϴ�.", markerId, 0);
		poiData.addPOIitem(126.991023, 37.579468, "â�����Դϴ�.", markerId, 0);
		poiData.addPOIitem(126.983083, 37.575395, "�λ絿�Դϴ�.", markerId, 0);
		poiData.endPOIdata();

		// ��ġ �����͸� ����Ͽ� �������� ����
		NMapPOIdataOverlay poiDataOverlay = mOverlayManager.createPOIdataOverlay(poiData, null);
		
		// id���� 0���� ������ ��� �������̰� ǥ�õǰ� �ִ� ��ġ�� ������ �߽ɰ� ZOOM�� �缳��
		poiDataOverlay.showAllPOIdata(0);
		
		// �������� �̺�Ʈ ���
		mOverlayManager.setOnCalloutOverlayListener(this);
		/******************* �������� ���� �ڵ� �� ********************/
	}

	/**
	 * ������ �ʱ�ȭ�� �� ȣ��ȴ�.
	 * ���������� �ʱ�ȭ�Ǹ� errorInfo ��ü�� null�� ���޵Ǹ�,
	 * �ʱ�ȭ ���� �� errorInfo��ü�� ���� ������ ���޵ȴ�
	 */
	@Override
	public void onMapInitHandler(NMapView mapview, NMapError errorInfo) {
		if (errorInfo == null) { // success
			//mMapController.setMapCenter(new NGeoPoint(126.978371, 37.5666091), 11);
		} else { // fail
			android.util.Log.e("NMAP", "onMapInitHandler: error=" + errorInfo.toString());
		}
	}

	/**
	 * ���� ���� ���� �� ȣ��Ǹ� ����� ���� ������ �Ķ���ͷ� ���޵ȴ�.
	 */
	@Override
	public void onZoomLevelChange(NMapView mapview, int level) {}

	/**
	 * ���� �߽� ���� �� ȣ��Ǹ� ����� �߽� ��ǥ�� �Ķ���ͷ� ���޵ȴ�.
	 */
	@Override
	public void onMapCenterChange(NMapView mapview, NGeoPoint center) {}

	/**
	 * ���� �ִϸ��̼� ���� ���� �� ȣ��ȴ�.
	 * animType : ANIMATION_TYPE_PAN or ANIMATION_TYPE_ZOOM
	 * animState : ANIMATION_STATE_STARTED or ANIMATION_STATE_FINISHED
	 */
	@Override
	public void onAnimationStateChange(NMapView arg0, int animType, int animState) {}

	@Override
	public void onMapCenterChangeFine(NMapView arg0) {}

	/** �������̰� Ŭ���Ǿ��� ���� �̺�Ʈ */
	@Override
	public NMapCalloutOverlay onCreateCalloutOverlay(NMapOverlay arg0,
			NMapOverlayItem arg1, Rect arg2) {
		Toast.makeText(this, arg1.getTitle(), Toast.LENGTH_SHORT).show();
		return null;
	}
}