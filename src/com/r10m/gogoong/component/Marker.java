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

/** ȭ�鿡 ǥ�õǴ� ��Ŀ�� ���õ� �ڵ��� ��κ��� ���
 * ȭ�鿡 ��Ŀ�� ���̴��� ���� ���
 * �����ϰ� �̹����� ���ڸ� ȭ�鿡 �׸� */
public class Marker implements Comparable<Marker> {
	/** ���̴��� �������� �Ÿ��� ������ ���� */
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("@#");
    /** ��ȣ�� ��ġ�� ȸ����Ʈ������ �̿��ؼ� ã���� ��� */
    private static final Vector symbolVector = new Vector(0, 0, 0);
    /** ������ ��ġ�� ȸ����Ʈ������ �̿��ؼ� ã���� ��� */
    private static final Vector textVector = new Vector(0, 1, 0);
    /** ��Ŀ��ȣ�� �׿� ���� ���ڸ� �׸��� ��ġ�� ��µ� ��� */
    private final Vector screenPositionVector = new Vector();
    private final Vector tmpSymbolVector = new Vector();
    private final Vector tmpVector = new Vector();
    private final Vector tmpTextVector = new Vector();
    private final float[] distanceArray = new float[1];
    private final float[] locationArray = new float[3];
    private final float[] screenPositionArray = new float[3];
    
    /** �� ��Ŀ�� �ʱ� Y�� ��ġ */
    private float initialY = 0.0f;

    private volatile static CameraModel cam = null;

    private volatile PaintableBoxedText textBox = null;
    private volatile PaintablePosition textContainer = null;
    
    /** ��ȣ�� ���ڸ� �׸��� ��� */
    protected final float[] symbolArray = new float[3];
    protected final float[] textArray = new float[3];
    /** GPS ��ȣ */
    protected volatile PaintableObject gpsSymbol = null;
    /** GPS ��ȣ�� �����ϴ� ����� */
    protected volatile PaintablePosition symbolContainer = null;
    /** �� ��Ŀ�� ���� ���� �ĺ����̸� ���̽����� �޾ƿ� ������ �̿��ؼ� ���� */
    protected String name = null;
    /** �� ��Ŀ�� ���� �ڼ��� ���� */
    protected String detail = null;
    /** ��Ŀ�� ���� ���迡���� �������� ��ġ */
    protected volatile PhysicalLocationUtility physicalLocation = new PhysicalLocationUtility();
    /** ������� ��ġ���� ��ǥ �Ÿ��� ���ʹ����� ���� */
    protected volatile double distance = 0.0;
    /** ��Ŀ�� ���ü� �÷��� */
    protected volatile boolean isOnRadar = false;
    protected volatile boolean isInView = false;
    /** ī�޶� ȭ�鿡�� ��Ŀ ��ȣ�� ������ ��ġ�� �����ϴµ� ���
     * ������ ��ġ�� �����ؼ� ������� ��ġ�� �����ϴµ� ��� 
     * x�� ����, y�� �¿�, z�� ������� ������ ���͸� �ϼ��ϱ� ���� ���*/
    protected final Vector symbolXyzRelativeToCameraView = new Vector();
    protected final Vector textXyzRelativeToCameraView = new Vector();
    protected final Vector locationXyzRelativeToPhysicalLocation = new Vector();
    /** ��Ŀ�� �⺻ ���� */
    protected int color = Color.WHITE;
    
    /** TouchZone Ȱ�� ��Ȱ�� �÷��� �� ������Ҷ� ������ �������� ���� �׸��µ� �̿�Ǵ� �ʵ� */
    private static boolean debugTouchZone = false;
    private static PaintableBox touchBox = null;
    private static PaintablePosition touchPosition = null;
    
    /** CollisionZone Ȱ�� ��Ȱ�� �÷��� �� ������Ҷ� ������ �������� ���� �׸��µ� �̿�Ǵ� �ʵ� */
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
    /** ī�޶� �信 ���õ� ��ȣ�� ������ ��ġ�� �̿��ؼ� ȭ����� ��Ŀ�� ��ġ�� ��� */
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
    /** ȭ�� ������Ʈ �� ��Ʈ���� �����Ҷ� ��� */
    public synchronized void update(Canvas canvas, float addX, float addY) {
    	if (canvas==null) throw new NullPointerException();
    	
    	if (cam==null) cam = new CameraModel(canvas.getWidth(), canvas.getHeight(), true);
    	cam.set(canvas.getWidth(), canvas.getHeight(), false);
        //�þ߰� ����
    	cam.setViewAngle(CameraModel.DEFAULT_VIEW_ANGLE);
        populateMatrices(cam, addX, addY);
        updateRadar();
        updateView();
    }
    /** ARData���� ���� ȸ�� ��Ʈ������ �̿��� ��Ŀ�� ��ȭ�� ������ ��ġ�� ã�´� */
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
	/** ���̴� ���� ��Ŀ�� ��ġ�� ������Ʈ */
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
	/** ��Ŀ�� ���� ���̴��� �˻� */
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
    /** ���ڷ� �Ѱܹ��� ��ġ�� ����� ���ο� ������� ��ġ�� ��� */
    public synchronized void calcRelativePosition(Location location) {
		if (location==null) throw new NullPointerException();
		
	    updateDistance(location);
	    
		if (physicalLocation.getAltitude()==0.0) physicalLocation.setAltitude(location.getAltitude());
		 
		PhysicalLocationUtility.convLocationToVector(location, physicalLocation, locationXyzRelativeToPhysicalLocation);
		this.initialY = locationXyzRelativeToPhysicalLocation.getY();
		updateRadar();
    }
    /** ��Ŀ�� �������� ��ġ�� ������� ��ġ������ ���ο� �Ÿ��� ��� */
    private synchronized void updateDistance(Location location) {
        if (location==null) throw new NullPointerException();

        Location.distanceBetween(physicalLocation.getLatitude(), physicalLocation.getLongitude(), location.getLatitude(), location.getLongitude(), distanceArray);
        distance = distanceArray[0];
    }
    /** Ŭ���� x,y���� ���ڷ� �Ѱ� ��Ŀ�� ���̴��� ��� ���� �ʴٸ� false */
    public synchronized boolean handleClick(float x, float y) {
    	if (!isOnRadar || !isInView) return false;
    	return isPointOnMarker(x,y,this);
    }
    /** ���ڷ� ���� ��Ŀ�� ���� ��Ŀ�� ������������ �˻� */
    public synchronized boolean isMarkerOnMarker(Marker marker) {
        return isMarkerOnMarker(marker,true);
    }
    /** ���ڷ� ���� ��Ŀ�� ���� ��Ŀ�� ������������ �˻� */
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
    /** ���޹��� ��ǥ�� ��Ŀ���� �ִ��� �˻� */
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
	/** ��Ŀ�� �������� �Ǵ�
	 * �ʿ信 �� ����� ���� �׸��� */
    public synchronized void draw(Canvas canvas) {
        if (canvas==null) throw new NullPointerException();

        if (!isOnRadar || !isInView) return;
        
        if (debugTouchZone) drawTouchZone(canvas);
        if (debugCollisionZone) drawCollisionZone(canvas);
        drawIcon(canvas);
        drawText(canvas);
    }
    /** �浹������ �׷����� �� ��쿡 �θ�Ŀ ������ �浹������ �׸��� */
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
    /** ��Ŀ�� ��ġ���� �� �ش� ���� ���� ������ �簢���� �׸��� */
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
    /** ������ �׸��� */
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
    /** ���õ� ���� ǥ�� */
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
    /** �� ��Ŀ�� �̸��� �� */
    public synchronized int compareTo(Marker another) {
        if (another==null) throw new NullPointerException();
        
        return name.compareTo(another.getName());
    }
    /** �� ��Ŀ�� �̸��� ������ �˻� */
    @Override
    public synchronized boolean equals(Object marker) {
        if(marker==null || name==null) throw new NullPointerException();
        
        return name.equals(((Marker)marker).getName());
    }
}
