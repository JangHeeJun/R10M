package com.r10m.gogoong.component;

import java.text.DecimalFormat;

import android.graphics.Canvas;
import android.graphics.Color;
import android.location.Location;
import android.util.Log;

import com.r10m.gogoong.ARData;
import com.r10m.gogoong.camera.CameraModel;
import com.r10m.gogoong.locate.PhysicalLocationUtility;
import com.r10m.gogoong.ui.PaintableBox;
import com.r10m.gogoong.ui.PaintableBoxedText;
import com.r10m.gogoong.ui.PaintableGps;
import com.r10m.gogoong.ui.PaintableObject;
import com.r10m.gogoong.ui.PaintablePosition;
import com.r10m.gogoong.util.Utilities;
import com.r10m.gogoong.util.Vector;

/** 화면에 표시되는 마커와 관련된 코드의 대부분을 담당
 * 화면에 마커가 보이는지 유무 계산
 * 적절하게 이미지와 글자를 화면에 그림 */
public class Marker implements Comparable<Marker> {
	/** 레이더에 보여지는 거리의 형식을 지정 */
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("@#");
    /** 기호의 위치를 회전매트릭스를 이용해서 찾을때 사용 */
    private static final Vector symbolVector = new Vector(0, 0, 0);
    /** 글자의 위치를 회전매트릭스를 이용해서 찾을때 사용 */
    private static final Vector textVector = new Vector(0, 1, 0);
    /** 마커기호와 그에 따른 글자를 그리고 위치를 잡는데 사용 */
    private final Vector screenPositionVector = new Vector();
    private final Vector tmpSymbolVector = new Vector();
    private final Vector tmpVector = new Vector();
    private final Vector tmpTextVector = new Vector();
    private final float[] distanceArray = new float[1];
    private final float[] locationArray = new float[3];
    private final float[] screenPositionArray = new float[3];
    
    /** 각 마커의 초기 Y축 위치 */
    private float initialY = 0.0f;

    private volatile static CameraModel cam = null;

    private volatile PaintableBoxedText textBox = null;
    private volatile PaintablePosition textContainer = null;
    
    /** 기호와 글자를 그릴때 사용 */
    protected final float[] symbolArray = new float[3];
    protected final float[] textArray = new float[3];
    /** GPS 기호 */
    protected volatile PaintableObject gpsSymbol = null;
    /** GPS 기호를 저장하는 저장소 */
    protected volatile PaintablePosition symbolContainer = null;
    /** 각 마커에 대한 고유 식별자이며 제이슨에서 받아온 샤싣을 이용해서 설정 */
    protected String name = null;
    /** 각 마커에 대한 자세한 설명 */
    protected String detail = null;
    /** 마커의 실제 세계에서의 물리적인 위치 */
    protected volatile PhysicalLocationUtility physicalLocation = new PhysicalLocationUtility();
    /** 사용자의 위치부터 목표 거리를 미터단위로 저장 */
    protected volatile double distance = 0.0;
    /** 마커의 가시성 플래그 */
    protected volatile boolean isOnRadar = false;
    protected volatile boolean isInView = false;
    /** 카메라 화면에서 마커 기호와 글자의 위치를 결정하는데 사용
     * 물리적 위치와 관련해서 사용자의 위치를 결정하는데 사용 
     * x는 상하, y는 좌우, z는 사용하지 않지만 벡터를 완성하기 위해 사용*/
    protected final Vector symbolXyzRelativeToCameraView = new Vector();
    protected final Vector textXyzRelativeToCameraView = new Vector();
    protected final Vector locationXyzRelativeToPhysicalLocation = new Vector();
    /** 마커의 기본 색상 */
    protected int color = Color.WHITE;
    
    /** TouchZone 활성 비활성 플래그 및 디버깅할때 도와줄 불투명한 상자 그리는데 이용되는 필드 */
    private static boolean debugTouchZone = false;
    private static PaintableBox touchBox = null;
    private static PaintablePosition touchPosition = null;
    
    /** CollisionZone 활성 비활성 플래그 및 디버깅할때 도와줄 불투명한 상자 그리는데 이용되는 필드 */
    private static boolean debugCollisionZone = false;
    private static PaintableBox collisionBox = null;
    private static PaintablePosition collisionPosition = null;

    public Marker(String name, double latitude, double longitude, double altitude, int color) {
		set(name, latitude, longitude, altitude, color, "no infomation");
	}
    
	public Marker(String name, double latitude, double longitude, double altitude, int color, String detail) {
		set(name, latitude, longitude, altitude, color, detail);
	}

	public synchronized void set(String name, double latitude, double longitude, double altitude, int color, String detail) {
		if (name==null) throw new NullPointerException();

		this.name = name;
		this.physicalLocation.set(latitude,longitude,altitude);
		this.color = color;
		this.detail = detail;
		this.isOnRadar = false;
		this.isInView = false;
		this.symbolXyzRelativeToCameraView.set(0, 0, 0);
		this.textXyzRelativeToCameraView.set(0, 0, 0);
		this.locationXyzRelativeToPhysicalLocation.set(0, 0, 0);
		this.initialY = 0.0f;
	}

	public synchronized String getName(){
		return this.name;
	}
	
	public synchronized String getDetail(){
		return this.detail;
	}

    public synchronized int getColor() {
    	return this.color;
    }

    public synchronized double getDistance() {
        return this.distance;
    }

    public synchronized float getInitialY() {
        return this.initialY;
    }

    public synchronized boolean isOnRadar() {
        return this.isOnRadar;
    }

    public synchronized boolean isInView() {
        return this.isInView;
    }
    /** 카메라 뷰에 관련된 기호와 글자의 위치를 이용해서 화면상의 마커의 위치를 계산 */
    public synchronized Vector getScreenPosition() {
        symbolXyzRelativeToCameraView.get(symbolArray);
        textXyzRelativeToCameraView.get(textArray);
        float x = (symbolArray[0] + textArray[0])/2;
        float y = (symbolArray[1] + textArray[1])/2;
        float z = (symbolArray[2] + textArray[2])/2;

        if (textBox!=null) y += (textBox.getHeight()/2);

        screenPositionVector.set(x, y, z);
        return screenPositionVector;
    }

    public synchronized Vector getLocation() {
        return this.locationXyzRelativeToPhysicalLocation;
    }

    public synchronized float getHeight() {
        if (symbolContainer==null || textContainer==null) return 0f;
        return symbolContainer.getHeight()+textContainer.getHeight();
    }
    
    public synchronized float getWidth() {
        if (symbolContainer==null || textContainer==null) return 0f;
        float w1 = textContainer.getWidth();
        float w2 = symbolContainer.getWidth();
        return (w1>w2)?w1:w2;
    }
    /** 화면 업데이트 및 매트릭스 생성할때 사용 */
    public synchronized void update(Canvas canvas, float addX, float addY) {
    	if (canvas==null) throw new NullPointerException();
    	
    	if (cam==null) cam = new CameraModel(canvas.getWidth(), canvas.getHeight(), true);
    	cam.set(canvas.getWidth(), canvas.getHeight(), false);
        //시야각 설정
    	cam.setViewAngle(CameraModel.DEFAULT_VIEW_ANGLE);
        populateMatrices(cam, addX, addY);
        updateRadar();
        updateView();
    }
    /** ARData에서 얻은 회전 매트릭스를 이용해 마커의 기화와 문자의 위치를 찾는다 */
	private synchronized void populateMatrices(CameraModel cam, float addX, float addY) {
		if (cam==null) throw new NullPointerException();
		
		tmpSymbolVector.set(symbolVector);
		tmpSymbolVector.add(locationXyzRelativeToPhysicalLocation);        
        tmpSymbolVector.prod(ARData.getRotationMatrix());
		
		tmpTextVector.set(textVector);
		tmpTextVector.add(locationXyzRelativeToPhysicalLocation);
		tmpTextVector.prod(ARData.getRotationMatrix());

		cam.projectPoint(tmpSymbolVector, tmpVector, addX, addY);
		symbolXyzRelativeToCameraView.set(tmpVector);
		cam.projectPoint(tmpTextVector, tmpVector, addX, addY);
		textXyzRelativeToCameraView.set(tmpVector);
	}
	/** 레이더 상의 마커의 위치를 업데이트 */
	private synchronized void updateRadar() {
		isOnRadar = false;

		float range = ARData.getRadius() * 1000;
		float scale = range / Radar.RADIUS;
		locationXyzRelativeToPhysicalLocation.get(locationArray);
        float x = locationArray[0] / scale;
        float y = locationArray[2] / scale; // z==y Switched on purpose 
        symbolXyzRelativeToCameraView.get(symbolArray);
		if ((symbolArray[2] < -1f) && ((x*x+y*y)<(Radar.RADIUS*Radar.RADIUS))) {
			isOnRadar = true;
		}
	}
	/** 마커가 현재 보이는지 검사 */
    private synchronized void updateView() {
        isInView = false;

        symbolXyzRelativeToCameraView.get(symbolArray);
        float x1 = symbolArray[0] + (getWidth()/2);
        float y1 = symbolArray[1] + (getHeight()/2);
        float x2 = symbolArray[0] - (getWidth()/2);
        float y2 = symbolArray[1] - (getHeight()/2);
        if (x1>=-1 && x2<=(cam.getWidth()) 
            &&
            y1>=-1 && y2<=(cam.getHeight())
        ) {
            isInView = true;
        }
    }
    /** 인자로 넘겨받은 위치를 사용해 새로운 상대적인 위치를 계산 */
    public synchronized void calcRelativePosition(Location location) {
		if (location==null) throw new NullPointerException();
		
	    updateDistance(location);
	    
		if (physicalLocation.getAltitude()==0.0) physicalLocation.setAltitude(location.getAltitude());
		 
		PhysicalLocationUtility.convLocationToVector(location, physicalLocation, locationXyzRelativeToPhysicalLocation);
		this.initialY = locationXyzRelativeToPhysicalLocation.getY();
		updateRadar();
    }
    /** 마커의 물리적인 위치와 사용자의 위치사이의 새로운 거리를 계산 */
    private synchronized void updateDistance(Location location) {
        if (location==null) throw new NullPointerException();

        Location.distanceBetween(physicalLocation.getLatitude(), physicalLocation.getLongitude(), location.getLatitude(), location.getLongitude(), distanceArray);
        distance = distanceArray[0];
    }
    /** 클릭의 x,y값을 인자로 넘겨 마커가 레이더와 뷰상에 있지 않다면 false */
    public synchronized boolean handleClick(float x, float y) {
    	if (!isOnRadar || !isInView) return false;
    	return isPointOnMarker(x,y,this);
    }
    /** 인자로 받은 마커가 현재 마커와 겹쳐지는지를 검사 */
    public synchronized boolean isMarkerOnMarker(Marker marker) {
        return isMarkerOnMarker(marker,true);
    }
    /** 인자로 받은 마커가 현재 마커와 겹쳐지는지를 검사 */
    private synchronized boolean isMarkerOnMarker(Marker marker, boolean reflect) {
        marker.getScreenPosition().get(screenPositionArray);
        float x = screenPositionArray[0];
        float y = screenPositionArray[1];        
        boolean middleOfMarker = isPointOnMarker(x,y,this);
        if (middleOfMarker) return true;

        float halfWidth = marker.getWidth()/2;
        float halfHeight = marker.getHeight()/2;

        float x1 = x - halfWidth;
        float y1 = y - halfHeight;
        boolean upperLeftOfMarker = isPointOnMarker(x1,y1,this);
        if (upperLeftOfMarker) return true;

        float x2 = x + halfWidth;
        float y2 = y1;
        boolean upperRightOfMarker = isPointOnMarker(x2,y2,this);
        if (upperRightOfMarker) return true;

        float x3 = x1;
        float y3 = y + halfHeight;
        boolean lowerLeftOfMarker = isPointOnMarker(x3,y3,this);
        if (lowerLeftOfMarker) return true;

        float x4 = x2;
        float y4 = y3;
        boolean lowerRightOfMarker = isPointOnMarker(x4,y4,this);
        if (lowerRightOfMarker) return true;

        return (reflect)?marker.isMarkerOnMarker(this,false):false;
    }
    /** 전달받은 좌표가 마커위에 있는지 검사 */
	private synchronized boolean isPointOnMarker(float x, float y, Marker marker) {
	    marker.getScreenPosition().get(screenPositionArray);
        float myX = screenPositionArray[0];
        float myY = screenPositionArray[1];
        float adjWidth = marker.getWidth()/2;
        float adjHeight = marker.getHeight()/2;

        float x1 = myX-adjWidth;
        float y1 = myY-adjHeight;
        float x2 = myX+adjWidth;
        float y2 = myY+adjHeight;

        if (x>=x1 && x<=x2 && y>=y1 && y<=y2) return true;
        
        return false;
	}
	/** 마커가 보여질지 판단
	 * 필요에 딸 디버그 상자 그리기 */
    public synchronized void draw(Canvas canvas) {
        if (canvas==null) throw new NullPointerException();

        if (!isOnRadar || !isInView) return;
        
        if (debugTouchZone) drawTouchZone(canvas);
        if (debugCollisionZone) drawCollisionZone(canvas);
        drawIcon(canvas);
        drawText(canvas);
    }
    /** 충돌영역이 그려져야 할 경우에 두마커 사이의 충돌영역을 그린다 */
    protected synchronized void drawCollisionZone(Canvas canvas) {
        if (canvas==null) throw new NullPointerException();
        
        getScreenPosition().get(screenPositionArray);
        float x = screenPositionArray[0];
        float y = screenPositionArray[1];        

        float width = getWidth();
        float height = getHeight();
        float halfWidth = width/2;
        float halfHeight = height/2;

        float x1 = x - halfWidth;
        float y1 = y - halfHeight;

        float x2 = x + halfWidth;
        float y2 = y1;

        float x3 = x1;
        float y3 = y + halfHeight;

        float x4 = x2;
        float y4 = y3;

        Log.w("collisionBox", "ul (x="+x1+" y="+y1+")");
        Log.w("collisionBox", "ur (x="+x2+" y="+y2+")");
        Log.w("collisionBox", "ll (x="+x3+" y="+y3+")");
        Log.w("collisionBox", "lr (x="+x4+" y="+y4+")");
        
        if (collisionBox==null) collisionBox = new PaintableBox(width,height,Color.WHITE,Color.RED);
        else collisionBox.set(width,height);

        float currentAngle = Utilities.getAngle(symbolArray[0], symbolArray[1], textArray[0], textArray[1])+90;
        
        if (collisionPosition==null) collisionPosition = new PaintablePosition(collisionBox, x1, y1, currentAngle, 1);
        else collisionPosition.set(collisionBox, x1, y1, currentAngle, 1);
        collisionPosition.paint(canvas);
    }
    /** 마커를 터치했을 때 해당 영역 위에 빨간색 사각형을 그린다 */
    protected synchronized void drawTouchZone(Canvas canvas) {
        if (canvas==null) throw new NullPointerException();
        
        if (gpsSymbol==null) return;
        
        symbolXyzRelativeToCameraView.get(symbolArray);
        textXyzRelativeToCameraView.get(textArray);        
        float x1 = symbolArray[0];
        float y1 = symbolArray[1];
        float x2 = textArray[0];
        float y2 = textArray[1];
        float width = getWidth();
        float height = getHeight();
        float adjX = (x1 + x2)/2;
        float adjY = (y1 + y2)/2;
        float currentAngle = Utilities.getAngle(symbolArray[0], symbolArray[1], textArray[0], textArray[1])+90;
        adjX -= (width/2);
        adjY -= (gpsSymbol.getHeight()/2);
        
        Log.w("touchBox", "ul (x="+(adjX)+" y="+(adjY)+")");
        Log.w("touchBox", "ur (x="+(adjX+width)+" y="+(adjY)+")");
        Log.w("touchBox", "ll (x="+(adjX)+" y="+(adjY+height)+")");
        Log.w("touchBox", "lr (x="+(adjX+width)+" y="+(adjY+height)+")");
        
        if (touchBox==null) touchBox = new PaintableBox(width,height,Color.WHITE,Color.GREEN);
        else touchBox.set(width,height);

        if (touchPosition==null) touchPosition = new PaintablePosition(touchBox, adjX, adjY, currentAngle, 1);
        else touchPosition.set(touchBox, adjX, adjY, currentAngle, 1);
        touchPosition.paint(canvas);
    }
    /** 아이콘 그리기 */
    protected synchronized void drawIcon(Canvas canvas) {
    	if (canvas==null) throw new NullPointerException();

        if (gpsSymbol==null) gpsSymbol = new PaintableGps(36, 36, true, getColor());

        textXyzRelativeToCameraView.get(textArray);
        symbolXyzRelativeToCameraView.get(symbolArray);

        float currentAngle = Utilities.getAngle(symbolArray[0], symbolArray[1], textArray[0], textArray[1]);
        float angle = currentAngle + 90;

        if (symbolContainer==null) symbolContainer = new PaintablePosition(gpsSymbol, symbolArray[0], symbolArray[1], angle, 1);
        else symbolContainer.set(gpsSymbol, symbolArray[0], symbolArray[1], angle, 1);

        symbolContainer.paint(canvas);
    }
    /** 관련된 문구 표시 */
    protected synchronized void drawText(Canvas canvas) {
		if (canvas==null) throw new NullPointerException();
		
	    String textStr = null;
	    if (distance<1000.0) {
	        textStr = name + " ("+ DECIMAL_FORMAT.format(distance) + "m)";          
	    } else {
	        double d=distance/1000.0;
	        textStr = name + " (" + DECIMAL_FORMAT.format(d) + "km)";
	    }

	    textXyzRelativeToCameraView.get(textArray);
	    symbolXyzRelativeToCameraView.get(symbolArray);

	    float maxHeight = Math.round(canvas.getHeight() / 10f) + 1;
	    if (textBox==null) textBox = new PaintableBoxedText(textStr, Math.round(maxHeight / 2f) + 1, 300);
	    else textBox.set(textStr, Math.round(maxHeight / 2f) + 1, 300);

	    float currentAngle = Utilities.getAngle(symbolArray[0], symbolArray[1], textArray[0], textArray[1]);
        float angle = currentAngle + 90;

	    float x = textArray[0] - (textBox.getWidth() / 2);
	    float y = textArray[1] + maxHeight;

	    if (textContainer==null) textContainer = new PaintablePosition(textBox, x, y, angle, 1);
	    else textContainer.set(textBox, x, y, angle, 1);
	    textContainer.paint(canvas);
	}
    /** 두 마커의 이름을 비교 */
    public synchronized int compareTo(Marker another) {
        if (another==null) throw new NullPointerException();
        
        return name.compareTo(another.getName());
    }
    /** 두 마커의 이름이 같은지 검사 */
    @Override
    public synchronized boolean equals(Object marker) {
        if(marker==null || name==null) throw new NullPointerException();
        
        return name.equals(((Marker)marker).getName());
    }
}
