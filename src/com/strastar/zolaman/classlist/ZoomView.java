package com.strastar.zolaman.classlist;

import java.io.File;

import com.strastar.zolaman.CartoonViewFlipper;

import android.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Paint.Style;
import android.graphics.drawable.BitmapDrawable;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.util.FloatMath;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import android.util.Log;


public class ZoomView extends ImageView {
	private final int COUNTDOWN_INTERVAL = 300;
	private final int MENU_TOUCH_INTERVAL = 300;
	private final int DOUBLE_TOUCH_INTERVAL = MENU_TOUCH_INTERVAL+30;
	
	private LinearLayout zoomlayout;
	private CurlView mCurlView;
	private Context mContext;
	
	private Handler mHandler = new Handler();
	
	private Matrix matrix;
	private Matrix save_matrix;
	
	private Bitmap main_bitmap;

	private float zoom_size = 3f;
	
	private static final int NONE = 0;
	private static final int DRAG = 1;
	private static final int ZOOM = 2;
	private int mode = NONE;

	private boolean move_line_up;
	
	private boolean mCurlViewTouchEventFlag=false;
	private boolean untouch = false;
	//PointF image_size = new PointF();
	private PointF mStartTouchPoint = new PointF();
	private PointF real_point = new PointF();
	private PointF mid = new PointF();
	private PointF mRealMovePoint = new PointF();

	private float saveDist, img_rate, init_scale;
	private float save_scale;
	private float oldDist = 1f;
	private long realTime;
	private boolean mDoubleTouchFlag = false;
	private boolean autozoomON = false;
	private boolean augmentation_and_diminution;//false = 확대   true = 축소

	private MotionEvent mEventZoomview;
	private int mImage_index;
	
//	public CountDownTimer mZoomViewDisableTimer = new CountDownTimer
//										(COUNTDOWN_INTERVAL+10, COUNTDOWN_INTERVAL) {
//		@Override
//		public void onTick(long millisUntilFinished) {
//			// TODO Auto-generated method stub
//			Log.v("mTimer", "onTick");
//		}
//		
//		@Override
//		public void onFinish() {
//			// TODO Auto-generated method stub
//			Log.v("mTimer", "onFinish");
//			pageDownZoomViewDisable();
//		}
//	};
	
//	public void pageDownZoomViewDisable()
//	{
//		mHandler.post(new Runnable() {
//			
//			@Override
//			public void run() {
//				// TODO Auto-generated method stub
//				setZoomViewPageVisible(false);
//			}
//		});
//		Log.v("mTimer", "test pageDown");
//		mEventZoomview.setAction(MotionEvent.ACTION_DOWN);
//		mEventZoomview.setLocation(mStartTouchPoint.x, mStartTouchPoint.y);
//		
//		CurlView.b_touch = true;
//		mCurlView.onTouch(mEventZoomview);
//		mCurlViewTouchEventFlag = true;
//
//	}
	
	
	public ZoomView(Context context, Bitmap btmap, int index) {
		super(context);
		
		mContext = context;
		mEventZoomview = MotionEvent.obtain(0, 0, 0, 0, 0, 0);
		untouch = false;
		matrix = new Matrix();
		save_matrix = new Matrix();
		move_line_up = false;
		
		
		SetBitmap(btmap, index);
	}
	
	public ZoomView(Context context) {
		super(context);
		
		mContext = context;
		mEventZoomview = MotionEvent.obtain(0, 0, 0, 0, 0, 0);
		untouch = false;
		matrix = new Matrix();
		save_matrix = new Matrix();
		move_line_up = false;
		
		main_bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.alert_dark_frame);
	}
	
	public void setLayout(LinearLayout layout, CurlView _curlView)
	{
		zoomlayout = layout;
		mCurlView = _curlView;
	}
	
	public void setTouchControl(boolean touch)
	{
		untouch = !touch;
	}
	
	public void setZoomViewPageVisible(final boolean on)
	{
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if(on){
					zoomlayout.setVisibility(View.VISIBLE);
					zoomlayout.refreshDrawableState();
				
					mCurlViewTouchEventFlag = false;
				}
				else{
					zoomlayout.setVisibility(View.INVISIBLE);
					
				}
			}
		});

	}
	
//	public void SetBitmap
	
	public void SetBitmapId(int index) {
		mImage_index = index;
		
//		String contentfile = CartoonViewFlipper.mBitmapIds[index].substring(0,CartoonViewFlipper.mBitmapIds[index].indexOf("."));
//		int contentfile_id = getResources().getIdentifier(contentfile, "raw", mContext.getPackageName());
//		SetBitmap(BitmapFactory.decodeResource(getResources(), contentfile_id));

		FileDecoder fd=new FileDecoder();
		//fd.getDecodeBitmap(new File(CartoonViewFlipper.mBitmapIds[mImage_index]));
		BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPurgeable = true;
          options.inDither = true;
          options.inJustDecodeBounds = false;
          options.inScaled = true;
          options.inPreferredConfig = Bitmap.Config.ARGB_8888;
          options.inSampleSize = 1;//img_inSampleSize[i];
          
		Bitmap b = fd.getDecodeBitmapOptions(new File(CartoonViewFlipper.mBitmapIds[mImage_index]), options);
		
		//Bitmap b = fd.getDecodeBitmap(new File(CartoonViewFlipper.mBitmapIds[mImage_index]));
		SetBitmap(b, index);
	
	
	}
	public void SetBitmap(Bitmap btmap, int index) {
		mImage_index = index;
		main_bitmap = btmap;
		post(new Runnable() {
	  		@Override
	  		public void run() {
	  			init_image();
	  		}
		});
		init_image();
	}
	
	private void init_image(){
		float x = main_bitmap.getWidth();
		float y = main_bitmap.getHeight();
		
		if(getWidth()/x>getHeight()/(float)y)
		{
			img_rate = getHeight()/((float)y);
			real_point.set((getWidth()-x)/2,
					(getHeight()-(float)y)/2);

			save_matrix.setTranslate(real_point.x-getWidth(), real_point.y);
			
			save_matrix.postScale(img_rate, img_rate,
					getWidth()/2,
					getHeight()/2);
		}
		else{
			img_rate = getWidth()/(x);
			real_point.set((getWidth()-x)/2,
					(getHeight()-(float)y)/2);

			save_matrix.setTranslate(real_point.x-getWidth(), real_point.y);
			//setImageMatrix(matrix);

			save_matrix.postScale(img_rate, img_rate,
					getWidth()/2,
					getHeight()/2);
		}
		
		
			init_scale = img_rate;
			//zoom_size = 1f;
			//zoom_size = 1.5f;
			zoom_size = 2f; //1.5f;
		

		float[] value = new float[9];
		save_matrix.getValues(value);
		float image_x=(value[2]/value[0]);
		float image_y=(value[5]/value[4]);

		save_matrix.postTranslate((getWidth() -	x*img_rate)/2-image_x*img_rate, 
				(getHeight() - (float)y*img_rate)/2-image_y*img_rate);
		matrix.set(save_matrix);
	}


	
	private float dist_m(float image_m)
	{
		float dist = 0;
		if(image_m<0f){
			if(image_m < -2.5f)
				dist = 0.4f;
			else if(image_m < -1.3f)
				dist = 0.2f;
			else if(image_m < -0.5f)
				dist = 0.1f;
			else if(image_m < -0.1f)
				dist = 0.05f;
			else dist = 0.01f;
		}
		else{
			if(image_m > 2.5f)
				dist = 0.4f;
			else if(image_m > 1.3f)
				dist = 0.2f;
			else if(image_m > 0.5f)
				dist = 0.1f;
			else if(image_m > 0.1f)
				dist = 0.05f;
			else dist = 0.01f;
		}
		return dist;
	}

	@Override
	protected void onDraw(final Canvas canvas) {
		super.onDraw(canvas);
		final Paint paint = new Paint();
	
		canvas.drawColor(Color.TRANSPARENT);
		
		paint.setTextSize(10);
		paint.setStyle(Style.STROKE);
		paint.setColor(Color.WHITE);
		
		short move_check = 0;
		if(move_line_up){
			float[] value = new float[9];
			float dist = 0;
		
			matrix.getValues(value);
			
			
			Log.v("matrix", "ondraw value[0] = "+value[0]+"   zoom_size = "+zoom_size);
			if(value[0] > zoom_size || value[0] < init_scale){
				
				Log.v("matrix", "ondraw value[0] = "+value[0]);
				
				if(value[0] > zoom_size){

					matrix.postScale(0.9f, 0.9f, mid.x, mid.y);
					
				}else{
					value[4] = value[0]+= dist_m(init_scale - value[0]);
					if(value[0] >= init_scale){
						value[4] = value[0] = init_scale;
					}
					value[2] = 0f;
					value[5] = (getHeight()-value[0]*main_bitmap.getHeight())/2;
					matrix.setValues(value);
					matrix.postScale(1f, 1f);
					
					
					
//					scale = init_scale/value[0]/1.2f;
				}
				
				move_check++;
			}
			
			if(autozoomON){
				if(augmentation_and_diminution){//축소
					matrix.postScale(0.9f, 0.9f, mid.x, mid.y);
					if(value[0] <= init_scale){
						value[4] = value[0] = init_scale;
						autozoomON = false;
					}
				}else{//확대
					if(value[0]*1.1f >= zoom_size){
						value[4] = value[0] = zoom_size;
						autozoomON = false;
					}else{
						matrix.postScale(1.1f, 1.1f, mid.x, mid.y);
					}
					matrix.getValues(value);

					float move_x = getWidth()/2 - mRealMovePoint.x;
					float move_y = getHeight()/2 - mRealMovePoint.y;
					
					if(move_x>0){
						move_x = move_x>20?20:move_x;
					}else{
						move_x = move_x<-20?-20:move_x;
					}
					if(move_y>0){
						move_y = move_y>20?20:move_y;
					}else{
						move_y = move_y<-20?-20:move_y;
					}
					
					if(value[2]+move_x>0)move_x = -value[2];
					if(value[5]+move_y>0)move_y = -value[5];

					if(value[2] + value[0]*main_bitmap.getWidth() +
							move_x < getWidth())
						move_x = getWidth()-(value[2]+value[0]*main_bitmap.getWidth());
					if((value[5]-(getHeight()-value[0]*main_bitmap.getHeight())/2) +
							value[0]*main_bitmap.getHeight() +
							move_y < getHeight())
						move_y = getHeight()-(getHeight()-value[0]*main_bitmap.getHeight())/2
							-(value[5]+value[4]*main_bitmap.getHeight());

					mRealMovePoint.set(mRealMovePoint.x+move_x, mRealMovePoint.y+move_y);
					Log.v("test", "move_x, move_y = "+move_x+", "+ move_y);
					matrix.postTranslate(move_x, move_y);
					
					
//					matrix.setValues(value);
//					matrix.postScale(1f, 1f, mid.x, mid.y);
				}
				if(autozoomON)move_check++;
				else{
					matrix.getValues(value);
					
					float move_x = 0;
					float move_y = 0;
					
					if(value[2]>0)move_x = -value[2];
					if(value[5]>0)move_y = -value[5];

					if(value[2] + value[0]*main_bitmap.getWidth() < getWidth())
						move_x = getWidth()-(value[2]+value[0]*main_bitmap.getWidth());
					if(value[5] + value[0]*main_bitmap.getHeight() < getHeight())
						move_y = getHeight()-(value[5]+value[4]*main_bitmap.getHeight());

					if(getHeight()>main_bitmap.getHeight()*value[0])
						move_y = 0;
					
					matrix.postTranslate(move_x, move_y);
				}
			}
			
		}
		canvas.drawBitmap(main_bitmap, matrix, paint);
	
		if(move_check>0)
			invalidate();
		else{
			move_line_up = false;
			Log.v("zoom", "ondraw return");
		}
	}



	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if(untouch)return true;
		
		
		if(mCurlViewTouchEventFlag){
			mCurlView.onTouch(event);
			return true;
		}
		// Handle touch events here...
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_DOWN:
				Log.v("realTime", "ACTION_DOWN = "+(System.currentTimeMillis()-realTime));
				if(DOUBLE_TOUCH_INTERVAL < System.currentTimeMillis()-realTime)
					mDoubleTouchFlag = false;
				if(mDoubleTouchFlag && 250 > System.currentTimeMillis()-realTime){
					mDoubleTouchFlag = true;
					realTime = System.currentTimeMillis();
					return true;
				}
				realTime = System.currentTimeMillis();
				{
					float[] value = new float[9];
					matrix.getValues(value);
					if(value[0] == init_scale){
						if((event.getX()<getWidth()/Common.DIVISION_SECTION && mImage_index> 0)||
								(event.getX() >(getWidth()/Common.DIVISION_SECTION)*(Common.DIVISION_SECTION-1)
										&& mImage_index < CartoonViewFlipper.mBitmapIds.length-1)){
							
							mHandler.post(new Runnable() {
								
								@Override
								public void run() {
									// TODO Auto-generated method stub
									setZoomViewPageVisible(false);
								}
							});
							Log.v("mTimer", "test pageDown");
//							mEventZoomview.setAction(MotionEvent.ACTION_DOWN);
//							mEventZoomview.setLocation(mStartTouchPoint.x, mStartTouchPoint.y);
//							
//							CurlView.b_touch = true;
							mCurlView.onTouch(event);
							mCurlViewTouchEventFlag = true;
//							mZoomViewDisableTimer.start();
						}
					}
				}
				
				move_line_up = false;
				save_matrix.set(matrix);
				mStartTouchPoint.set(event.getX(), event.getY());
				mode = DRAG;
				invalidate();
				break;
			case MotionEvent.ACTION_POINTER_DOWN:
				oldDist = spacing(event);
				
				if (oldDist > 5f) {
					save_matrix.set(matrix);
		            midPoint(mid, event);
		            save_scale = 1f;
		            mode = ZOOM;
		            
		            mHandler.post(new Runnable() {
						@Override
						public void run() {
							zoomlayout.refreshDrawableState();
							
						}
					});
				}
				break;
			case MotionEvent.ACTION_POINTER_UP:
//				mZoomViewDisableTimer.cancel();
				mode = NONE;
				
				move_line_up = true;
				mDoubleTouchFlag = false;
				invalidate();
				break;
			case MotionEvent.ACTION_UP:
//				mZoomViewDisableTimer.cancel();
				Log.v("realTime", "ACTION_UP = "+(System.currentTimeMillis()-realTime));
				if(DOUBLE_TOUCH_INTERVAL < System.currentTimeMillis()-realTime)
					mDoubleTouchFlag = false;
				else{
					if(mDoubleTouchFlag){//더블 클릭했을 경우
						mDoubleTouchFlag = false;
						Log.v("realTime", "ACTION_UP double click");
						realTime = System.currentTimeMillis();
						
						float[] value = new float[9];
						matrix.getValues(value);
	
						mid.set(event.getX(), event.getY());
						mRealMovePoint.set(event.getX(), event.getY());
						if((zoom_size - init_scale)/2+init_scale>value[0]){
							//확대
							mHandler.post(new Runnable() {
								
								@Override
								public void run() {
									// TODO Auto-generated method stub
									zoomlayout.refreshDrawableState();
								}
							});
							augmentation_and_diminution = false;
						}
						else{
							//축소
							mHandler.post(new Runnable() {
								
								@Override
								public void run() {
									// TODO Auto-generated method stub
									zoomlayout.refreshDrawableState();
								}
							});
							augmentation_and_diminution = true;
						}
						autozoomON = true;
					}
					else {//원클릭 경우
						mDoubleTouchFlag = true;
						Log.v("realTime", "ACTION_UP          pastTouch");
						realTime = System.currentTimeMillis();
						mHandler.postDelayed(new Runnable() {
							@Override
							public void run() {
								// TODO Auto-generated method stub
								if(mDoubleTouchFlag){// 원클릭 후 DOUBLE_TOUCH_INTERVAL 초 안에 한번더 클릭이 안들어 오는 경우
//									if((mStartTouchPoint.x<getWidth()/3 || mStartTouchPoint.x >(getWidth()/3)*2)){
//										pageDownZoomViewDisable();
//										mEventZoomview.setLocation(mStartTouchPoint.x, mEventZoomview.getY());
//										mEventZoomview.setAction(MotionEvent.ACTION_UP);
//										mCurlView.onTouch(mEventZoomview);
//									}else{
									float[] value = new float[9];
									matrix.getValues(value);
									if(value[0] == init_scale){
										setZoomViewPageVisible(false);
										mCurlView.showpopup();
									}
//									}
								}
							}
						}, MENU_TOUCH_INTERVAL);
					}
					
				}
				mode = NONE;
				
				move_line_up = true;
				invalidate();
				break;
			case MotionEvent.ACTION_MOVE:
				float[] value = new float[9];
				

				if (mode == DRAG ){
					matrix.set(save_matrix);
					matrix.getValues(value);
					float main_scale = value[0];
					
					float move_x = event.getX() - mStartTouchPoint.x;
					float move_y = event.getY() - mStartTouchPoint.y;
					
					if(value[2]+move_x>0)move_x = -value[2];
					if(value[5]+move_y>0)move_y = -value[5];

					if(value[2] + value[0]*main_bitmap.getWidth() +
							move_x < getWidth())
						move_x = getWidth()-(value[2]+value[0]*main_bitmap.getWidth());
					if(value[5] + value[0]*main_bitmap.getHeight() +
							move_y < getHeight())
						move_y = getHeight()-(value[5]+value[4]*main_bitmap.getHeight());

					if(getHeight()>main_bitmap.getHeight()*value[0])
						move_y = 0;
					
					if(getHeight() > (float)main_bitmap.getHeight() * main_scale)
						matrix.postTranslate(move_x, (move_y)/10);
					else
						matrix.postTranslate(move_x, move_y);

				}
				else if (mode == ZOOM) {
					matrix.set(save_matrix);
					matrix.getValues(value);
					float main_scale = value[0];
					
					float newDist = spacing(event);
					
					if (newDist > 1f) {
			
						float scale = newDist / oldDist;
			
						if(value[0]*scale < init_scale){

							matrix.postScale(save_scale+(scale-save_scale)/3, 
									save_scale+(scale-save_scale)/3, mid.x, mid.y);
						}else{
							save_scale = scale;
							matrix.postScale(scale, scale, mid.x, mid.y);
						}
						
					}
				}
				invalidate();
				break;
		}
	
		return true; // indicate event was handled
	}

	private float spacing(MotionEvent event) {
		// ...
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return FloatMath.sqrt(x * x + y * y);
	}
	private void midPoint(PointF point, MotionEvent event) {
		// ...
		float x = event.getX(0) + event.getX(1);
		float y = event.getY(0) + event.getY(1);
		point.set(x / 2, y / 2);
	}
}