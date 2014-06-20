package com.r10m.gogoong.ui;

import android.graphics.Canvas;
/** 화면에 한개의 점을 찍을때 사용 - 레이더를 그릴때 사용하는 클래스 */
public class PaintablePoint extends PaintableObject {
    private static int width=5;
    private static int height=5;
    private int color = 0;
    private boolean fill = false;
    
    public PaintablePoint(int color, boolean fill) {
    	set(color, fill);
    }

    public void set(int color, boolean fill) {
        this.color = color;
        this.fill = fill;
    }

	@Override
    public void paint(Canvas canvas) {
    	if (canvas==null) throw new NullPointerException();
    	
        setFill(fill);
        setColor(color);
        paintRect(canvas, -1, -1, width, height);
    }

	@Override
    public float getWidth() {
        return width;
    }

	@Override
    public float getHeight() {
        return height;
    }
}