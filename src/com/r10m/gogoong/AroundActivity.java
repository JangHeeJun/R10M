package com.r10m.gogoong;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

public class AroundActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

		// Create a RelativeLayout container that will hold a SurfaceView,
		// and set it as the content of our activity.
		MoveView moveView = new MoveView(this);
		moveView.setImageResource(R.drawable.map_around);
		setContentView(moveView);
	}



	public class MoveView extends ImageView implements OnTouchListener{
		
		// These matrices will be used to move and zoom image
		private Matrix matrix = new Matrix();
		private Matrix moveMatrix = new Matrix();
		
		// We can be in one of these 3 states
		private static final int NONE = 0;
		private static final int DRAG = 1;
		private static final int ZOOM = 2;
		private int mode = NONE;
		
		// Remember some things for zooming
		private PointF start = new PointF();
		private PointF mid = new PointF();
		private float oldDist = 1f;
		
		private static final int WIDTH = 0;
		private static final int HEIGHT = 1;
		
		//matrix constants
		private float[] value = new float[9];
		
		private Drawable drawable;
		
		//imageView size
		private int width;
		private int height;
		//image size
		private int imageWidth;
		private int imageHeight;
		//zoomIn image max size
		private int scaledImageWidth;
		private int scaledImageHeight;
		
		//Constructor
		public MoveView(Context context, AttributeSet attrs, int defStyle){
			super(context, attrs, defStyle);
		}
		
		public MoveView(Context context, AttributeSet attrs){
			this(context, attrs, 0);
		}
		
		public MoveView(Context context){
			this(context, null);
			setOnTouchListener(this);
			setScaleType(ScaleType.MATRIX);
		}
		
		//image layout 화면배치
		@Override
		protected void onLayout(boolean changed, int left, int top, int right, int bottom){
			super.onLayout(changed, left, top, right, bottom);
			init();
		}
		
		//image setter
		@Override
		public void setImageBitmap(Bitmap bm){
			super.setImageBitmap(bm);
		}
		
		@Override
		public void setImageDrawable(Drawable drawable){
			super.setImageDrawable(drawable);
		}
		
		@Override
		public void setImageResource(int resId){
			super.setImageResource(resId);
		}
		
		//initialize
		protected void init()
		{
		
			this.matrix.getValues(value); // 매트릭스 값
			// 뷰크기
			width = this.getWidth(); 
			height = this.getHeight();
			drawable = this.getDrawable();
			
			if (drawable == null)  return;
			
			imageWidth = drawable.getIntrinsicWidth(); //실제 이미지 너비
			imageHeight = drawable.getIntrinsicHeight(); //실제 이미지 높이
			
			if (imageWidth > width || imageHeight > height)	{
				setImageFitOnView();
			}
		
			setCenter();
			matrix.setValues(value);
			setImageMatrix(matrix);
		}
		
		public void initImageReal()	{
			value[0] = 1;
			value[4] = 1;
			value[2] = 0;
			value[5] = 0;
			matrix.setValues(value);
			setImageMatrix(matrix);
		}
		
		public void initImageFit()	{
		
			if (imageWidth > width || imageHeight > height)	{
				setImageFitOnView();
			}
			setCenter();
			value[2] = 0;
			value[5] = 0;
			matrix.setValues(value);
			setImageMatrix(matrix);
		
		}
		
		// 뷰에 맞게 이미지 사이즈 설정
		private void setImageFitOnView()	{// 이미지의 가로와 세로의 길이 비교하여 target 설정
			int target = WIDTH;
	//	        if (imageWidth < imageHeight)
	//	          target = HEIGHT;
		
	// 너비와 높이 중 큰값을 뷰에 맞도록 value 값 설정
			if (target == WIDTH)	{
			value[4] = (float)width / imageWidth;
			value[0] = value[4];
		
	//	        else if (target == HEIGHT)
	//	      {
	//	          value[4] = (float)height / imageHeight;
	//	          value[0] = value[4];
	//	      }
		
			scaledImageWidth = (int) (imageWidth * value[0]);
			scaledImageHeight = (int) (imageHeight * value[4]);
		// 너비(높이)가 뷰와 같지만 높이(너비)는 뷰보다 클 경우 이미지 크기를 높이(너비)에 맞게 조정
	//	        if (scaledImageWidth > width)
	//	      {
	//	          value[4] = (float) width / imageWidth;
	//	          value[0] = value[4];
	//	      }
	//	        if (scaledImageHeight > height)
	//	        {
	//	          value[4] = (float)height / imageHeight;
	//	          value[0] = value[4];
	//	        }
			}
		}
		
		private void setCenter()	{
			scaledImageWidth = (int) (imageWidth * value[0]);
			scaledImageHeight = (int) (imageHeight * value[4]);
			
			if (scaledImageWidth < width)
			{
			value[2] = (float) (width / 2) - (float) (scaledImageWidth / 2);
			}
			if (scaledImageHeight < height)	{
			value[5] = (float) (height / 2) - (float) (scaledImageHeight / 2);
			}
		}
		
		@Override
		public boolean onTouch(View v, MotionEvent event){
			ImageView view = (ImageView) v;
			switch (event.getAction() & MotionEvent.ACTION_MASK){
				case MotionEvent.ACTION_DOWN:	//first finger down only
					moveMatrix.set(matrix);
					start.set(event.getX(), event.getY());
					mode = DRAG;
					break;
				
				case MotionEvent.ACTION_POINTER_DOWN:	//second finger down
					oldDist = spacing(event);
					if (oldDist > 20f){ // 20f를 변경하여 줌을 인식하는 두 손가락의 거리값을 변경함
						moveMatrix.set(matrix);
						midPoint(mid, event);
						mode = ZOOM;
					}
					break;
				case MotionEvent.ACTION_UP:			//first finger lifted
				case MotionEvent.ACTION_POINTER_UP:	//second finger lifted
					mode = NONE;
					if (scaledImageWidth < width){
						fixView();
					}
					break;
				 
				case MotionEvent.ACTION_MOVE:
					if (mode == DRAG) {	//movement of first finger
			
					matrix.set(moveMatrix);
					matrix.postTranslate(event.getX() - start.x, event.getY() - start.y);
					}else if (mode == ZOOM) {	//pinch zooming
						float newDist = spacing(event);
						if (newDist > 5f)	{
							matrix.set(moveMatrix);
							float scale = newDist / oldDist;
							matrix.postScale(scale, scale, mid.x, mid.y);
						}
					}
					break;
				}
			
				changeMatrixValue(matrix, view);
				// Perform the transformation
				//view.setImageMatrix(matrix);
				return true;	// indicate event was handled
			}
		
		// 화면보다 작게 축소 하지 않도록
		private void fixView() {
			matrix.getValues(value);
			setImageFitOnView();
			setCenter();
			matrix.setValues(value);
			setImageMatrix(matrix);
			
		}
				
		private void changeMatrixValue(Matrix matrix, ImageView view){
			matrix.getValues(value);
			if (drawable == null)  return;
	
		// 이미지가 바깥으로 나가지 않도록.
			if (value[2] < width - scaledImageWidth)   value[2] = width - scaledImageWidth;
			if (value[5] < height - scaledImageHeight)   value[5] = height - scaledImageHeight;
			if (value[2] > 0)   value[2] = 0;
			if (value[5] > 0)   value[5] = 0;
		
		// 실제크기 2배 이상 확대 되지 않도록
			if (value[0] > 2 || value[4] > 2)	{
			value[0] = 2;
			value[4] = 2;
			}
		
			setCenter();
			matrix.setValues(value);
			setImageMatrix(matrix);
		}
		
		//pinch 시 두손가락의 거리
		private float spacing(MotionEvent event){
			float x = event.getX(0) - event.getX(1);
			float y = event.getY(0) - event.getY(1);
			return FloatMath.sqrt(x * x + y * y);
		}
		
		//zoomIn midPoint 확대 | 축소 되는 점
		private void midPoint(PointF point, MotionEvent event){
			float x = event.getX(0) + event.getX(1);
			float y = event.getY(0) + event.getY(1);
			point.set(x / 2, y / 2);
		}
	}
}
