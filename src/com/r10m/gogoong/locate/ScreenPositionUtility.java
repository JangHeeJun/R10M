package com.r10m.gogoong.locate;
/** 레이더의 선을 그리는데 사용 */
public class ScreenPositionUtility {
    private float x = 0f;
    private float y = 0f;

	public ScreenPositionUtility() {
        set(0, 0);
    }

    public void set(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getY() {
		return y;
	}

	public void setY(float y) {
		this.y = y;
	}
	/** x,y값을 t의 각도만큼 회전 */
    public void rotate(double t) {
        float xp = (float) Math.cos(t) * x - (float) Math.sin(t) * y;
        float yp = (float) Math.sin(t) * x + (float) Math.cos(t) * y;

        x = xp;
        y = yp;
    }

    public void add(float x, float y) {
        this.x += x;
        this.y += y;
    }

    @Override
    public String toString() {
        return "x="+x+" y="+y;
    }
}