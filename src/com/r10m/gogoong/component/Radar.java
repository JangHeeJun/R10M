package com.r10m.gogoong.component;

import android.graphics.Canvas;
import android.graphics.Color;

import com.r10m.gogoong.ARData;
import com.r10m.gogoong.camera.CameraModel;
import com.r10m.gogoong.locate.ScreenPositionUtility;
import com.r10m.gogoong.ui.PaintableCircle;
import com.r10m.gogoong.ui.PaintableLine;
import com.r10m.gogoong.ui.PaintablePosition;
import com.r10m.gogoong.ui.PaintableRadarPoints;
import com.r10m.gogoong.ui.PaintableText;
import com.r10m.gogoong.util.PitchAzimuthCalculator;

/** 레이더를 그리는 클래스 , 마커를 표시하는 점,선들을 그리기도 함 */
public class Radar {
	// 레이더의 색상,지름,글자색,간격등 설정
    public static final float RADIUS = 100;
    private static final int LINE_COLOR = Color.argb(150,0,0,220);
    private static final float PAD_X = 10;
    private static final float PAD_Y = 20;
    private static final int RADAR_COLOR = Color.argb(100, 0, 0, 200);
    private static final int TEXT_COLOR = Color.rgb(255,255,255);
    private static final int TEXT_SIZE = 16;
    
    private static ScreenPositionUtility leftRadarLine = null;
    private static ScreenPositionUtility rightRadarLine = null;
    private static PaintablePosition leftLineContainer = null;
    private static PaintablePosition rightLineContainer = null;
    private static PaintablePosition circleContainer = null;
    
    private static PaintableRadarPoints radarPoints = null;
    private static PaintablePosition pointsContainer = null;
    
    private static PaintableText paintableText = null;
    private static PaintablePosition paintedContainer = null;
    // 레이더에 선이 있는지 확인
    public Radar() {
        if (leftRadarLine==null) leftRadarLine = new ScreenPositionUtility();
        if (rightRadarLine==null) rightRadarLine = new ScreenPositionUtility();
    }
    
    public void draw(Canvas canvas) {
    	if (canvas==null) throw new NullPointerException();
    	// 피치와 방위각을 얻어온다
    	PitchAzimuthCalculator.calcPitchBearing(ARData.getRotationMatrix());
        ARData.setAzimuth(PitchAzimuthCalculator.getAzimuth());
        ARData.setPitch(PitchAzimuthCalculator.getPitch());
        
        drawRadarCircle(canvas);
        drawRadarPoints(canvas);
        drawRadarLines(canvas);
        drawRadarText(canvas);
    }
    /** 레이더의 기본이 되는 원 */
    private void drawRadarCircle(Canvas canvas) {
    	if (canvas==null) throw new NullPointerException();
    	
        if (circleContainer==null) {
            PaintableCircle paintableCircle = new PaintableCircle(RADAR_COLOR,RADIUS,true);
            circleContainer = new PaintablePosition(paintableCircle,PAD_X+RADIUS,PAD_Y+RADIUS,0,1);
        }
        circleContainer.paint(canvas);
    }

    /** 레이더 상에서 마커를 나타내는 모든 점 */
    private void drawRadarPoints(Canvas canvas) {
    	if (canvas==null) throw new NullPointerException();
    	
        if (radarPoints==null) radarPoints = new PaintableRadarPoints();
        
        if (pointsContainer==null) 
        	pointsContainer = new PaintablePosition( radarPoints, 
                                                     PAD_X, 
                                                     PAD_Y, 
                                                     -ARData.getAzimuth(), 
                                                     1);
        else 
        	pointsContainer.set(radarPoints, 
                    			PAD_X, 
                    			PAD_Y, 
                    			-ARData.getAzimuth(), 
                    			1);
        
        pointsContainer.paint(canvas);
    }

    /** 현재 카메라의 시야를 보여주는 두줄의 선 */
    private void drawRadarLines(Canvas canvas) {
    	if (canvas==null) throw new NullPointerException();
    	
        if (leftLineContainer==null) {
            leftRadarLine.set(0, -RADIUS);
            leftRadarLine.rotate(-CameraModel.DEFAULT_VIEW_ANGLE / 2);
            leftRadarLine.add(PAD_X+RADIUS, PAD_Y+RADIUS);

            float leftX = leftRadarLine.getX()-(PAD_X+RADIUS);
            float leftY = leftRadarLine.getY()-(PAD_Y+RADIUS);
            PaintableLine leftLine = new PaintableLine(LINE_COLOR, leftX, leftY);
            leftLineContainer = new PaintablePosition(  leftLine, 
                                                        PAD_X+RADIUS, 
                                                        PAD_Y+RADIUS, 
                                                        0, 
                                                        1);
        }
        leftLineContainer.paint(canvas);
        
        if (rightLineContainer==null) {
            rightRadarLine.set(0, -RADIUS);
            rightRadarLine.rotate(CameraModel.DEFAULT_VIEW_ANGLE / 2);
            rightRadarLine.add(PAD_X+RADIUS, PAD_Y+RADIUS);
            
            float rightX = rightRadarLine.getX()-(PAD_X+RADIUS);
            float rightY = rightRadarLine.getY()-(PAD_Y+RADIUS);
            PaintableLine rightLine = new PaintableLine(LINE_COLOR, rightX, rightY);
            rightLineContainer = new PaintablePosition( rightLine, 
                                                        PAD_X+RADIUS, 
                                                        PAD_Y+RADIUS, 
                                                        0, 
                                                        1);
        }
        rightLineContainer.paint(canvas);
    }

    /** radarText()메소드 호출하여 레이더에 글씨를 그리기 전에 형식을 정렬 */
    private void drawRadarText(Canvas canvas) {
    	if (canvas==null) throw new NullPointerException();
        int range = (int) (ARData.getAzimuth() / (360f / 16f)); 
        String  dirTxt = "";
        if (range == 15 || range == 0) dirTxt = "N"; 
        else if (range == 1 || range == 2) dirTxt = "NE"; 
        else if (range == 3 || range == 4) dirTxt = "E"; 
        else if (range == 5 || range == 6) dirTxt = "SE";
        else if (range == 7 || range == 8) dirTxt= "S"; 
        else if (range == 9 || range == 10) dirTxt = "SW"; 
        else if (range == 11 || range == 12) dirTxt = "W"; 
        else if (range == 13 || range == 14) dirTxt = "NW";
        int bearing = (int) ARData.getAzimuth(); 
        radarText(  canvas, 
                    ""+bearing+((char)176)+" "+dirTxt, 
                    (PAD_X + RADIUS), 
                    (PAD_Y - 5), 
                    true
                 );
        
        radarText(  canvas, 
                    formatDist(ARData.getRadius() * 1000), 
                    (PAD_X + RADIUS), 
                    (PAD_Y + RADIUS*2 -10), 
                    false
                 );
    }
    
    private void radarText(Canvas canvas, String txt, float x, float y, boolean bg) {
    	if (canvas==null || txt==null) throw new NullPointerException();
    	
        if (paintableText==null) paintableText = new PaintableText(txt,TEXT_COLOR,TEXT_SIZE,bg);
        else paintableText.set(txt,TEXT_COLOR,TEXT_SIZE,bg);
        
        if (paintedContainer==null) paintedContainer = new PaintablePosition(paintableText,x,y,0,1);
        else paintedContainer.set(paintableText,x,y,0,1);
        
        paintedContainer.paint(canvas);
    }

    private static String formatDist(float meters) {
        if (meters < 1000) {
            return ((int) meters) + "m";
        } else if (meters < 10000) {
            return formatDec(meters / 1000f, 1) + "km";
        } else {
            return ((int) (meters / 1000f)) + "km";
        }
    }

    private static String formatDec(float val, int dec) {
        int factor = (int) Math.pow(10, dec);

        int front = (int) (val);
        int back = (int) Math.abs(val * (factor) ) % factor;

        return front + "." + back;
    }
}
