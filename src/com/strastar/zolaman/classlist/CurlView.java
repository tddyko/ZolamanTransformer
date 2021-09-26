package com.strastar.zolaman.classlist;


import com.strastar.zolaman.CartoonViewFlipper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.RectF;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

/**
 * OpenGL ES View.
 * 
 * @author harism
 */
public class CurlView extends GLSurfaceView implements View.OnTouchListener,
		View.OnClickListener,
		CurlRenderer.Observer {
	private String TAG="Z_CurlView";
	public  ImageView btn_prev;
	private Handler mHandler = new Handler();
	public  Context mContext;
	
	private ZoomView mZoomView;
	boolean b_sideTouch = false;

	// Shows one page at the center of view.
	
	public static final int SHOW_ONE_PAGE = 1;
	// Shows two pages side by side.
	public static final int SHOW_TWO_PAGES = 2;
	// One page is the default.
	private int mViewMode = SHOW_ONE_PAGE;

	private boolean mRenderLeftPage = true;
	private boolean mAllowLastPageCurl = true;

	// Page meshes. Left and right meshes are 'static' while curl is used to
	// show page flipping.
	private CurlMesh mPageCurl;
	private CurlMesh mPageLeft;
	private CurlMesh mPageRight;

	// Curl state. We are flipping none, left or right page.
	private static final int CURL_NONE = 0;
	private static final int CURL_LEFT = 1;
	private static final int CURL_RIGHT = 2;
	private int mCurlState = CURL_NONE;

	// Current page index. This is always showed on right page.
	private int mCurrentIndex = 0;

	// Bitmap size. These are updated from renderer once it's initialized.
	private int mPageBitmapWidth = -1;
	private int mPageBitmapHeight = -1;

	// Start position for dragging.
	private PointF mDragStartPos = new PointF();
	private PointerPosition mPointerPos = new PointerPosition();
	private PointF mCurlPos = new PointF();
	private PointF mCurlDir = new PointF();

	private boolean mAnimate = false;
	private PointF mAnimationSource = new PointF();
	private PointF mAnimationTarget = new PointF();
	private long mAnimationStartTime;
	private long mAnimationDurationTime = 300;
	private int mAnimationTargetEvent;
	
	// Constants for mAnimationTargetEvent.
	private static final int SET_CURL_TO_LEFT = 1;
	private static final int SET_CURL_TO_RIGHT = 2;

	private CurlRenderer mRenderer;
	private BitmapProvider mBitmapProvider;
	private SizeChangedObserver mSizeChangedObserver;
	private boolean mEnableTouchPressure = false;

	private boolean b_centerToggle = false;
	private boolean b_rl;
	private long realTime; 
	private float oldX; 
	
	public static boolean b_touch;
	
	public  static boolean success = true;
	/**
	 * Default constructor.
	 */
	public CurlView(Context ctx) {
		super(ctx);
		b_touch = true;
		init(ctx);
		Log.v(TAG,"every12");
		mContext = ctx;
	}

	/**
	 * Default constructor.
	 */
	public CurlView(Context ctx, AttributeSet attrs) {
		super(ctx, attrs);
		b_touch = true;
		init(ctx);
		Log.v(TAG,"every12");
		mContext = ctx;
	}


	/**
	 * Default constructor.
	 */
	public CurlView(Context ctx, AttributeSet attrs, int defStyle) {
		this(ctx, attrs);
		Log.v(TAG,"every12");
		b_touch = true;
		mContext = ctx;
	}

	/**
	 * Set current page index.
	 */
	public int getCurrentIndex() {
		Log.v("zoomtest","mCurrentIndex = "+mCurrentIndex);
		return mCurrentIndex;
	}

	
	
////////////////////////////////	
	
	
	

	public void setzoomclass(ZoomView zoomView)
	{
		mZoomView = zoomView;
	}

	public void showpopup()
	{
		CartoonViewFlipper.showPopupToggle(View.VISIBLE);
		//CartoonViewFlipper.popup.setVisibility(View.VISIBLE);
		b_centerToggle = true;
	}
	
	
	@Override
	public void onDrawFrame() {
		// We are not animating.
		if (mAnimate == false) {
			return;
		}

		long currentTime = System.currentTimeMillis();
		// If animation is done.
		if (currentTime >= mAnimationStartTime + mAnimationDurationTime) {
		
			if (mAnimationTargetEvent == SET_CURL_TO_RIGHT) {
				// Switch curled page to right.
				CurlMesh right = mPageCurl;
				CurlMesh curl = mPageRight;
				right.setRect(mRenderer.getPageRect(CurlRenderer.PAGE_RIGHT));
				right.setFlipTexture(false);
				right.reset();
				mRenderer.removeCurlMesh(curl);
				mPageCurl = curl;
				mPageRight = right;
				
				// If we were curling left page update current index.
				if (mCurlState == CURL_LEFT) {
					mCurrentIndex--;
					CartoonViewFlipper.page_index = mCurrentIndex;
					Log.d(TAG,String.format("bjc : %d %d",mPageBitmapWidth,mPageBitmapHeight));
					mZoomView.SetBitmap(mBitmapProvider.getBitmap(mPageBitmapWidth,
							mPageBitmapHeight, mCurrentIndex),mCurrentIndex);
//					mZoomView.SetBitmapId(mCurrentIndex);
					
					Log.v(TAG,"CURL_LEFT = "+mCurrentIndex);
					mHandler.post(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							Toast.makeText(mContext,String.format("[%d/%d]", 
									mCurrentIndex+1, mBitmapProvider.getBitmapCount()), Toast.LENGTH_SHORT).show();
							CartoonViewFlipper.seekbar.setProgress(mCurrentIndex);
							mZoomView.setTouchControl(true);
							b_touch = true;
						}
					});
				}else{
					b_touch = true;
				}
			} else if (mAnimationTargetEvent == SET_CURL_TO_LEFT) {
				// Switch curled page to left.
				CurlMesh left = mPageCurl;
				CurlMesh curl = mPageLeft;
				left.setRect(mRenderer.getPageRect(CurlRenderer.PAGE_LEFT));
				left.setFlipTexture(true);
				left.reset();
				mRenderer.removeCurlMesh(curl);
				if (!mRenderLeftPage) {
					mRenderer.removeCurlMesh(left);
				}
				mPageCurl = curl;
				mPageLeft = left;
				
				// If we were curling right page update current index.
				if (mCurlState == CURL_RIGHT) {
					mCurrentIndex++;
					CartoonViewFlipper.page_index = mCurrentIndex;
//					mZoomView.SetBitmap(mPageRight.getBitmap());
					mZoomView.SetBitmap(mBitmapProvider.getBitmap(mPageBitmapWidth,
							mPageBitmapHeight, mCurrentIndex),mCurrentIndex);
//					mZoomView.SetBitmapId(mCurrentIndex);
					Log.v(TAG,"CURL_RIGHT = "+CURL_RIGHT);
					mHandler.post(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							Toast.makeText(mContext,String.format("[%d/%d]", 
									mCurrentIndex+1, mBitmapProvider.getBitmapCount()), Toast.LENGTH_SHORT).show();
							CartoonViewFlipper.seekbar.setProgress(mCurrentIndex);
							mZoomView.setTouchControl(true);
							b_touch = true;
						}
					});
				}else{
					b_touch = true;
				}
			}
			mCurlState = CURL_NONE;
			mAnimate = false;
			requestRender();
			
			
			Log.v(TAG,"TAG  mAnimate");
			success = true;
			
			if(!b_centerToggle)
				mZoomView.setZoomViewPageVisible(true);
//			mZoomView.setEnabled(true);
			
		} else {
			mPointerPos.mPos.set(mAnimationSource);
			float t = (float) Math
					.sqrt((double) (currentTime - mAnimationStartTime)
							/ mAnimationDurationTime);
			mPointerPos.mPos.x += (mAnimationTarget.x - mAnimationSource.x) * t;
			mPointerPos.mPos.y += (mAnimationTarget.y - mAnimationSource.y) * t;
			updateCurlPos(mPointerPos);
			mZoomView.setTouchControl(true);
			Log.v("end","a");
		}
	}

	@Override
	public void onPageSizeChanged(int width, int height) {
		mPageBitmapWidth = width;
		mPageBitmapHeight = height;
		updateBitmaps();
		requestRender();
	}

	@Override
	public void onSizeChanged(int w, int h, int ow, int oh) {
		super.onSizeChanged(w, h, ow, oh);
		requestRender();
		if (mSizeChangedObserver != null) {
			mSizeChangedObserver.onSizeChanged(w, h);
		}
	}

	@Override
	public void onSurfaceCreated() {
		// In case surface is recreated, let page meshes drop allocated texture
		// ids and ask for new ones. There's no need to set textures here as
		// onPageSizeChanged should be called later on.
		mPageLeft.resetTexture();
		mPageRight.resetTexture();
		mPageCurl.resetTexture();
	}
	
	
	public CurlView getclass() {
		return this;
	}
	
	@Override
	public void onClick(View view){
		if(!b_touch)
			return;
		Log.d(TAG, "onClick ");
		mAnimate = false;
	}

//	private int mClickEventPoint=0;
	public void onTouch(MotionEvent even){
		onTouch(this, even);
	}
	
	@Override
	public boolean onTouch(View view, MotionEvent me) {
		
		if(!b_touch)
			return true;
		
		if(me.getAction() == MotionEvent.ACTION_UP)
			b_touch = false;
		
		if (mAnimate || mBitmapProvider == null) {
			Log.v(TAG,"2");
			mAnimate = false;
			return true;
			
		}
		
		Log.v("test", "onTouch");
		// We need page rects quite extensively so get them for later use.
		RectF rightRect = mRenderer.getPageRect(CurlRenderer.PAGE_RIGHT);
		RectF leftRect = mRenderer.getPageRect(CurlRenderer.PAGE_LEFT);

		// Store pointer position.
	
		mPointerPos.mPos.set(me.getX(), me.getY());
		mRenderer.translate(mPointerPos.mPos);
		if (mEnableTouchPressure) {
			mPointerPos.mPressure = me.getPressure();
			Log.v(TAG,"3");
		} else {
			mPointerPos.mPressure = 0f;
			Log.v(TAG,"4");
		}
		
		switch (me.getAction()) {
		
		case MotionEvent.ACTION_DOWN: 
			//mMoveFlag=false;
			if(!b_touch)
				return true;
			
//			if(  ((me.getX()<view.getWidth()/3 || me.getX() >(view.getWidth()/3)*2) )){
			if(me.getX()<getWidth()/Common.DIVISION_SECTION || 
					me.getX() >(getWidth()/Common.DIVISION_SECTION)*(Common.DIVISION_SECTION-1) ){	
				mDragStartPos.set(mPointerPos.mPos);

				// First we make sure it's not over or below page. Pages are
				// supposed to be same height so it really doesn't matter do we use
				// left or right one.
				if (mDragStartPos.y > rightRect.top) {
					mDragStartPos.y = rightRect.top;
					Log.v(TAG,"6");
				} else if (mDragStartPos.y < rightRect.bottom) {
					mDragStartPos.y = rightRect.bottom;
					Log.v(TAG,"7");
				}

				Log.v("value","11 mBitmapProvider.getBitmapCount() =  "+ mBitmapProvider.getBitmapCount()+
						"  \nmCurrentIndex = "+mCurrentIndex);
				float halfX = (rightRect.right + rightRect.left) / 2;
				if (mDragStartPos.x < halfX && mCurrentIndex > 0) {
					mDragStartPos.x = rightRect.left;
					startCurl(CURL_LEFT);
					Log.v(TAG,"12");
				} else if (mDragStartPos.x >= halfX
						&& mCurrentIndex < mBitmapProvider.getBitmapCount()-1) {
					mDragStartPos.x = rightRect.right;
					Log.v(TAG,"13");
					if (!mAllowLastPageCurl
							&& mCurrentIndex >= mBitmapProvider
									.getBitmapCount() - 1) {
						Log.v(TAG,"14");
						return false;
					}
					Log.v("value","11 CartoonViewFlipper.photosize =  "+ CartoonViewFlipper.photosize+
							"   CartoonViewFlipper.page_index = "+CartoonViewFlipper.page_index);
					
					if(CartoonViewFlipper.page_index<CartoonViewFlipper.photosize){
						Log.v(TAG,"startCurl(CURL_RIGHT)");
						startCurl(CURL_RIGHT);
					}
					
				}
				// If we have are in curl state, let this case clause flow through
				// to next one. We have pointer position and drag position defined
				// and this will create first render request given these points.
				if (mCurlState == CURL_NONE) {
					Log.v(TAG,"15aa");
					return false;
				}
				b_sideTouch = true;
				
			}
			oldX = me.getX();
			realTime = System.currentTimeMillis();
			
			Log.v(TAG,"b_sideTouch = "+String.valueOf(b_sideTouch));
//			updateCurlPos(mPointerPos);
			break;
			
		case MotionEvent.ACTION_MOVE: {
//			b_sideTouch = false;
			
			
			if(b_sideTouch)
				updateCurlPos(mPointerPos);
			//mMoveFlag=true;
			Log.v(TAG,"[ACTION_MOVE]16 me.getX() = "+me.getX());
			break;
		}
		
		case MotionEvent.ACTION_CANCEL:
			Log.v(TAG,"17");
		case MotionEvent.ACTION_UP: 
			Log.v("test", "ACTION_UP");
			
//			mClickEventPoint = (int)me.getX();
//			Log.v(TAG,"[ACTION_UP] me.getX() = "+me.getX() + " mClickEventPoint="+mClickEventPoint);
//			Log.d(TAG,"Math.abs(mClickEventPoint-(int)me.getX() =" + Math.abs(mClickEventPoint-(int)me.getX()));
			if(System.currentTimeMillis()-realTime < 150){
				if(oldX<view.getWidth()/Common.DIVISION_SECTION || 
						oldX >(view.getWidth()/Common.DIVISION_SECTION)*(Common.DIVISION_SECTION-1)) {
					Log.v("test", "page move");
//					||b_sideTouch){
					if(oldX<view.getWidth()/2){ // 왼쪽 클릭한 경우 
						//startCurl(CURL_LEFT);
						b_rl = false;
						setnextpage(mCurrentIndex-1);
					}
					else{
						// 오른쪽 클릭한 경우
						b_rl = true;
						setnextpage(mCurrentIndex+1);
					}
					return false;
				}else{
					Log.v(TAG,"here here ");
					
					if(b_centerToggle == false){
						CartoonViewFlipper.showPopupToggle(View.VISIBLE);
						b_centerToggle = true;
					}else{
						mZoomView.setZoomViewPageVisible(true);
						b_centerToggle = false;
						CartoonViewFlipper.showPopupToggle(View.GONE);
					}
					b_touch = true;
				}
				
			}
//			
			//pagerun(false);
			if (mCurlState == CURL_LEFT || mCurlState == CURL_RIGHT) {
				// Animation source is the point from where animation starts.
				// Also it's handled in a way we actually simulate touch events
				// meaning the output is exactly the same as if user drags the
				// page to other side. While not producing the best looking
				// result (which is easier done by altering curl position and/or
				// direction directly), this is done in a hope it made code a
				// bit more readable and easier to maintain.
				mAnimationSource.set(mPointerPos.mPos);
				mAnimationStartTime = System.currentTimeMillis();
				
				if (mPointerPos.mPos.x > (rightRect.left + rightRect.right) / 2) {
					Log.v(TAG,"1");
					if(mCurlState != 2){
						CartoonViewFlipper.page_index--;
						
					}
					// On right side target is always right page's right border.
					mAnimationTarget.set(mDragStartPos);
					mAnimationTarget.x = mRenderer
							.getPageRect(CurlRenderer.PAGE_RIGHT).right;
					mAnimationTargetEvent = SET_CURL_TO_RIGHT;
					
				} else {
					Log.v(TAG,"21");
					// On left side target depends on visible pages.
					mAnimationTarget.set(mDragStartPos);
					if (mCurlState == CURL_RIGHT || mViewMode == SHOW_TWO_PAGES) {
						mAnimationTarget.x = leftRect.left;
						CartoonViewFlipper.page_index++;
						
						Log.v(TAG,"2");
					} else {
						mAnimationTarget.x = rightRect.left;
						Log.v(TAG,"3");
					}
					
					Log.v(TAG,"4");
					mAnimationTargetEvent = SET_CURL_TO_LEFT;
					
				
				}
				b_touch = false;
				mAnimate = true;
				requestRender();
			}else{
				Log.v("test", "else");
				b_touch = true;
			}
//			if(CartoonViewFlipper.page_index <CartoonViewFlipper.photosize+1){
//				CartoonViewFlipper.seekbar.setProgress(CartoonViewFlipper.page_index-1);
//				//CartoonViewFlipper.page_count.setText(CartoonViewFlipper.page_index+"/"+CartoonViewFlipper.photosize);
//			}
			
			int pageidx = CartoonViewFlipper.page_index-1;
			
			Log.d(TAG,String.format("seekbar pageidx(%d) CartoonViewFlipper.seekbar.getProgress()=%d"
									,pageidx,CartoonViewFlipper.seekbar.getProgress()));
			break;
		
		}
		return true;
	}

//	public void setRepage(boolean lr_move)
//	{
//		if(lr_move){ // 왼쪽 클릭한 경우 
//			//startCurl(CURL_LEFT);
//			b_rl = false;
//			setRepage(mCurrentIndex-1);
//		}
//		else{
//			// 오른쪽 클릭한 경우
//			b_rl = true;
//			setRepage(mCurrentIndex+1);
//		}
//	}
	
	public boolean setnextpage(int index)
	{
		Log.v("setnextpage","setnextpage start");
				
		if(mCurrentIndex == index){
			b_touch = true;
			return false;
		}
		mZoomView.setTouchControl(false);
		Log.v("setnextpage","setnextpage next");
		// Animation source is the point from where animation starts.
		// Also it's handled in a way we actually simulate touch events
		// meaning the output is exactly the same as if user drags the
		// page to other side. While not producing the best looking
		// result (which is easier done by altering curl position and/or
		// direction directly), this is done in a hope it made code a
		// bit more readable and easier to maintain.
		
		RectF rightRect = mRenderer.getPageRect(CurlRenderer.PAGE_RIGHT);
		RectF leftRect = mRenderer.getPageRect(CurlRenderer.PAGE_LEFT);
		
		mAnimationSource.set(mPointerPos.mPos);
		mAnimationStartTime = System.currentTimeMillis();
		
		Log.v(TAG,"19>>>  "+ mRenderer
				.getPageRect(CurlRenderer.PAGE_RIGHT).right+"  "+leftRect.left+"   "+rightRect.left);
		
		// Given the explanation, here we decide whether to simulate
		// drag to left or right end.
		if (!b_rl) {
			Log.v(TAG,"1");
//			if(mCurlState != 2){
//				CartoonViewFlipper.page_index--;
//				
//			}
			// On right side target is always right page's right border.
			mAnimationTarget.set(mDragStartPos);
			mAnimationTarget.x = mRenderer
					.getPageRect(CurlRenderer.PAGE_RIGHT).right;
			mAnimationTargetEvent = SET_CURL_TO_RIGHT;
			
			Log.v("setnextpage","setnextpage next1");
		} else {
			Log.v(TAG,"21");
			// On left side target depends on visible pages.
			mAnimationTarget.set(mDragStartPos);
//			if (mCurlState == CURL_RIGHT || mViewMode == SHOW_TWO_PAGES) {
				mAnimationTarget.x = leftRect.left;
				CartoonViewFlipper.page_index++;
//				
//				Log.v(TAG,"2");
//			} else {
//				mAnimationTarget.x = rightRect.left;
//				Log.v(TAG,"3");
//			}
			
			Log.v(TAG,"4");
			mAnimationTargetEvent = SET_CURL_TO_LEFT;
			Log.v("setnextpage","setnextpage next2");
		
		}
		mAnimate = true;
		requestRender();
	
//		if(CartoonViewFlipper.page_index <CartoonViewFlipper.photosize+1){
//			CartoonViewFlipper.seekbar.setProgress(CartoonViewFlipper.page_index-1);
//			//CartoonViewFlipper.page_count.setText(CartoonViewFlipper.page_index+"/"+CartoonViewFlipper.photosize);
//		}
		
//		int pageidx = CartoonViewFlipper.page_index-1;
//		
//		Log.d(TAG,String.format("seekbar pageidx(%d) CartoonViewFlipper.seekbar.getProgress()=%d"
//								,pageidx,CartoonViewFlipper.seekbar.getProgress()));
		
//		boolean lr = mCurrentIndex > index;
//		
//		if(mCurrentIndex+1 < index || mCurrentIndex-1 > index){
//			mCurrentIndex = index+(lr?1:-1);
//			CartoonViewFlipper.page_index = index+1;
//			mRenderer.removeCurlMesh(mPageLeft);
//			mRenderer.removeCurlMesh(mPageRight);
//			mRenderer.removeCurlMesh(mPageCurl);
//		
//			Log.v(TAG,"4");
//			// We are curling left page.
//	
//			// If there is new/previous bitmap available load it to left page.
//			if (mCurrentIndex > 0 && lr) {
//				Log.v(TAG,"5");
//				Bitmap bitmap = mBitmapProvider.getBitmap(mPageBitmapWidth,
//						mPageBitmapHeight, mCurrentIndex-1);
//				mPageLeft.setBitmap(bitmap);
//				mPageLeft
//						.setRect(mRenderer.getPageRect(CurlRenderer.PAGE_LEFT));
//				mPageLeft.setFlipTexture(false);
//				mPageLeft.reset();
//	
//				mRenderer.addCurlMesh(mPageLeft);
//			}
//		
//			// If there is something to show on right page add it to renderer.
//			if (mCurrentIndex < mBitmapProvider.getBitmapCount() - 1 && !lr) {
//				Bitmap bitmap = mBitmapProvider.getBitmap(mPageBitmapWidth,
//						mPageBitmapHeight, mCurrentIndex);
//				mPageRight.setBitmap(bitmap);
//				mPageRight
//						.setRect(mRenderer.getPageRect(CurlRenderer.PAGE_RIGHT));
//				mPageRight.setFlipTexture(false);
//				mPageRight.reset();
//	
//				mRenderer.addCurlMesh(mPageRight);
//			}
//	
//			Log.v("CURL", ">>"+(mCurrentIndex+(lr?1:-1))+"    "+mBitmapProvider.getBitmapCount());
////			if (mCurrentIndex < mBitmapProvider.getBitmapCount() - 1 +(lr?1:-1)&& 
////					mCurrentIndex > 0 +(lr?-1:1))
//			{
//				Bitmap bitmap = mBitmapProvider.getBitmap(mPageBitmapWidth,
//						mPageBitmapHeight, mCurrentIndex+(lr?-1:1));
//				Log.v("CURL", ">>"+(mCurrentIndex+(lr?1:-1))+"    "+mBitmapProvider.getBitmapCount());
//
//				mPageCurl.setBitmap(bitmap);
//				
//				mPageCurl.setRect(mRenderer.getPageRect(CurlRenderer.PAGE_RIGHT));
//				mPageCurl.setFlipTexture(false);
//				
//				mPageCurl.reset();
//				mRenderer.addCurlMesh(mPageCurl);
//			}
//			mCurlState = CURL_NONE;
//			
//		}
//		pagerun(lr);
		
		return true;
	}
	
	public boolean setRepage(int index)
	{
		if(mCurrentIndex == index)
			return false;
		boolean lr = mCurrentIndex > index;
		
		if(mCurrentIndex+1 < index || mCurrentIndex-1 > index){
			mZoomView.setZoomViewPageVisible(false);
//			mZoomView.setpageview();
			mCurrentIndex = index+(lr?1:-1);
			CartoonViewFlipper.page_index = index+1;
			mRenderer.removeCurlMesh(mPageLeft);
			mRenderer.removeCurlMesh(mPageRight);
			mRenderer.removeCurlMesh(mPageCurl);
		
			Log.v(TAG,"4");
			// We are curling left page.
	
			// If there is new/previous bitmap available load it to left page.
			if (mCurrentIndex > 0 && lr) {
				Log.v(TAG,"5");
				Bitmap bitmap = mBitmapProvider.getBitmap(mPageBitmapWidth,
						mPageBitmapHeight, mCurrentIndex-1);
				mPageLeft.setBitmap(bitmap);
				mPageLeft
						.setRect(mRenderer.getPageRect(CurlRenderer.PAGE_LEFT));
				mPageLeft.setFlipTexture(false);
				mPageLeft.reset();
	
				mRenderer.addCurlMesh(mPageLeft);
			}
		
			// If there is something to show on right page add it to renderer.
			if (mCurrentIndex < mBitmapProvider.getBitmapCount() - 1 && !lr) {
				Bitmap bitmap = mBitmapProvider.getBitmap(mPageBitmapWidth,
						mPageBitmapHeight, mCurrentIndex);
				mPageRight.setBitmap(bitmap);
				mPageRight
						.setRect(mRenderer.getPageRect(CurlRenderer.PAGE_RIGHT));
				mPageRight.setFlipTexture(false);
				mPageRight.reset();
	
				mRenderer.addCurlMesh(mPageRight);
			}
	
			Log.v("CURL", ">>"+(mCurrentIndex+(lr?1:-1))+"    "+mBitmapProvider.getBitmapCount());
//			if (mCurrentIndex < mBitmapProvider.getBitmapCount() - 1 +(lr?1:-1)&& 
//					mCurrentIndex > 0 +(lr?-1:1))
			{
				Bitmap bitmap = mBitmapProvider.getBitmap(mPageBitmapWidth,
						mPageBitmapHeight, mCurrentIndex+(lr?-1:1));
				Log.v("CURL", ">>"+(mCurrentIndex+(lr?1:-1))+"    "+mBitmapProvider.getBitmapCount());

				mPageCurl.setBitmap(bitmap);
//				mZoomView.SetBitmap(bitmap);
				mPageCurl.setRect(mRenderer.getPageRect(CurlRenderer.PAGE_RIGHT));
				mPageCurl.setFlipTexture(false);
				
				mPageCurl.reset();
				mRenderer.addCurlMesh(mPageCurl);
			}
			mCurlState = CURL_NONE;
			
		}
		pagerun(lr);
		
		return true;
	}
	
	
	public void pagerun(boolean LR_select)
	{
		
		
		RectF rightRect = mRenderer.getPageRect(CurlRenderer.PAGE_RIGHT);
		
		// Store pointer position.
	

		//mDragStartPos.set(mPointerPos.mPos);
		
		  if (LR_select) {
			   mPointerPos.mPos.set(0.0f, (float)getHeight()/2);
			   mRenderer.translate(mPointerPos.mPos);


			   mPointerPos.mPressure = 1f;
			  
			   mDragStartPos.set(mPointerPos.mPos);
			   
			   mDragStartPos.x = rightRect.left;
			   updateCurlPos(mPointerPos);
			   startCurl(CURL_LEFT);
			   
			  } else  {
			   mPointerPos.mPos.set((float)getWidth(), (float)getHeight()/2);
			   mRenderer.translate(mPointerPos.mPos);


			   mPointerPos.mPressure = 0f;
			  
			   mDragStartPos.set(mPointerPos.mPos);
			   
			   mDragStartPos.x = rightRect.right;

			   startCurl(CURL_RIGHT);
			  }
		
		
		mAnimationSource.set(mPointerPos.mPos);
		mAnimationStartTime = System.currentTimeMillis();

		// Given the explanation, here we decide whether to simulate
		// drag to left or right end.
		if(LR_select){
			// On right side target is always right page's right border.
			mAnimationTarget.set(mDragStartPos);
			mAnimationTarget.x = 1;
			mAnimationTargetEvent = SET_CURL_TO_RIGHT;
//			if(com.strastar.album.CurlActivity.page_index>1)
//			{
			CartoonViewFlipper.page_index--;
//			}
			
		
			
		} else {
			
			// On left side target depends on visible pages.
			mAnimationTarget.set(mDragStartPos);
//			if (mCurlState == CURL_RIGHT || mViewMode == SHOW_TWO_PAGES) {
//				mAnimationTarget.x = leftRect.left;
//			} else {
				mAnimationTarget.x = -1;
//			}
			mAnimationTargetEvent = SET_CURL_TO_LEFT;
//			if(com.strastar.album.CurlActivity.page_index!=8)
//			{
			CartoonViewFlipper.page_index++;
//			}
			
		
		}
		mAnimate = true;
		requestRender();
	}
	
	/**
	 * Allow the last page to curl.
	 */
	public void setAllowLastPageCurl(boolean allowLastPageCurl) {
		mAllowLastPageCurl = allowLastPageCurl;
	}

	/**
	 * Sets background color - or OpenGL clear color to be more precise. Color
	 * is a 32bit value consisting of 0xAARRGGBB and is extracted using
	 * android.graphics.Color eventually.
	 */
	@Override
	public void setBackgroundColor(int color) {
		mRenderer.setBackgroundColor(color);
		requestRender();
	}

	/**
	 * Update/set bitmap provider.
	 */
	public void setBitmapProvider(BitmapProvider bitmapProvider) {
		mBitmapProvider = bitmapProvider;
		mCurrentIndex = 0;
		updateBitmaps();
		requestRender();
		Log.v(TAG,"setBitmapProvider");
		mHandler.post(new Runnable() {
            public void run() {
            	//CartoonViewFlipper.mpb_waiting.setVisibility(View.GONE);
		    }
		});
	}

	/**
	 * Set page index.
	 */
	public void setCurrentIndex(int index) {
		if (mBitmapProvider == null || index <= 0) {
			mCurrentIndex = 0;
		} else {
			mCurrentIndex = Math.min(index,
					mBitmapProvider.getBitmapCount() - 1);
		}
		Log.v("number","setCurrentIndex = " +index);
		updateBitmaps();
		requestRender();
	}

	/**
	 * If set to true, touch event pressure information is used to adjust curl
	 * radius. The more you press, the flatter the curl becomes. This is
	 * somewhat experimental and results may vary significantly between devices.
	 * On emulator pressure information seems to be flat 1.0f which is maximum
	 * value and therefore not very much of use.
	 */
	public void setEnableTouchPressure(boolean enableTouchPressure) {
		mEnableTouchPressure = enableTouchPressure;
	}

	/**
	 * Set margins (or padding). Note: margins are proportional. Meaning a value
	 * of .1f will produce a 10% margin.
	 */
	public void setMargins(float left, float top, float right, float bottom) {
		mRenderer.setMargins(left, top, right, bottom);
	}

	/**
	 * Setter for whether left side page is rendered. This is useful mostly for
	 * situations where right (main) page is aligned to left side of screen and
	 * left page is not visible anyway.
	 */
	public void setRenderLeftPage(boolean renderLeftPage) {
		mRenderLeftPage = renderLeftPage;
	}

	/**
	 * Sets SizeChangedObserver for this View. Call back method is called from
	 * this View's onSizeChanged method.
	 */
	public void setSizeChangedObserver(SizeChangedObserver observer) {
		mSizeChangedObserver = observer;
	}

	/**
	 * Sets view mode. Value can be either SHOW_ONE_PAGE or SHOW_TWO_PAGES. In
	 * former case right page is made size of display, and in latter case two
	 * pages are laid on visible area.
	 */
	public void setViewMode(int viewMode) {
		switch (viewMode) {
		case SHOW_ONE_PAGE:
			mViewMode = viewMode;
			mRenderer.setViewMode(CurlRenderer.SHOW_ONE_PAGE);
			break;
		case SHOW_TWO_PAGES:
			mViewMode = viewMode;
			mRenderer.setViewMode(CurlRenderer.SHOW_TWO_PAGES);
			break;
		}
	}

	/**
	 * Initialize method.
	 */
	private void init(Context ctx) {
		mRenderer = new CurlRenderer(this);
		setRenderer(mRenderer);
		setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
		setOnClickListener(this);
		setOnTouchListener(this);
		

		// Even though left and right pages are static we have to allocate room
		// for curl on them too as we are switching meshes. Another way would be
		// to swap texture ids only.
		mPageLeft = new CurlMesh(10);
		mPageRight = new CurlMesh(10);
		mPageCurl = new CurlMesh(10);
		mPageLeft.setFlipTexture(true);
		mPageRight.setFlipTexture(false);
	}

	/**
	 * Sets mPageCurl curl position.
	 */
	private void setCurlPos(PointF curlPos, PointF curlDir, double radius) {

		// First reposition curl so that page doesn't 'rip off' from book.
		if (mCurlState == CURL_RIGHT
				|| (mCurlState == CURL_LEFT && mViewMode == SHOW_ONE_PAGE)) {
			RectF pageRect = mRenderer.getPageRect(CurlRenderer.PAGE_RIGHT);
			if (curlPos.x >= pageRect.right) {
				mPageCurl.reset();
				requestRender();
				return;
			}
			if (curlPos.x < pageRect.left) {
				curlPos.x = pageRect.left;
			}
			if (curlDir.y != 0) {
				float diffX = curlPos.x - pageRect.left;
				float leftY = curlPos.y + (diffX * curlDir.x / curlDir.y);
				if (curlDir.y < 0 && leftY < pageRect.top) {
					curlDir.x = curlPos.y - pageRect.top;
					curlDir.y = pageRect.left - curlPos.x;
				} else if (curlDir.y > 0 && leftY > pageRect.bottom) {
					curlDir.x = pageRect.bottom - curlPos.y;
					curlDir.y = curlPos.x - pageRect.left;
				}
			}
		} else if (mCurlState == CURL_LEFT) {
			RectF pageRect = mRenderer.getPageRect(CurlRenderer.PAGE_LEFT);
			if (curlPos.x <= pageRect.left) {
				mPageCurl.reset();
				requestRender();
				return;
			}
			if (curlPos.x > pageRect.right) {
				curlPos.x = pageRect.right;
			}
			if (curlDir.y != 0) {
				float diffX = curlPos.x - pageRect.right;
				float rightY = curlPos.y + (diffX * curlDir.x / curlDir.y);
				if (curlDir.y < 0 && rightY < pageRect.top) {
					curlDir.x = pageRect.top - curlPos.y;
					curlDir.y = curlPos.x - pageRect.right;
				} else if (curlDir.y > 0 && rightY > pageRect.bottom) {
					curlDir.x = curlPos.y - pageRect.bottom;
					curlDir.y = pageRect.right - curlPos.x;
				}
			}
		}

		// Finally normalize direction vector and do rendering.
		double dist = Math.sqrt(curlDir.x * curlDir.x + curlDir.y * curlDir.y);
		if (dist != 0) {
			curlDir.x /= dist;
			curlDir.y /= dist;
			mPageCurl.curl(curlPos, curlDir, radius);
		} else {
			mPageCurl.reset();
		}

		requestRender();
	}

	/**
	 * Switches meshes and loads new bitmaps if available.
	 */
	private void startCurl(int page) {
		switch (page) {

		// Once right side page is curled, first right page is assigned into
		// curled page. And if there are more bitmaps available new bitmap is
		// loaded into right side mesh.
		case CURL_RIGHT: {
			// Remove meshes from renderer.
			mRenderer.removeCurlMesh(mPageLeft);
			mRenderer.removeCurlMesh(mPageRight);
			mRenderer.removeCurlMesh(mPageCurl);

			// We are curling right page.
			CurlMesh curl = mPageRight;
			mPageRight = mPageCurl;
			mPageCurl = curl;
			
			
			// If there is something to show on left page, simply add it to
			// renderer.
			if (mCurrentIndex > 0) {
				Log.v("matrix","1aa");
				mPageLeft
						.setRect(mRenderer.getPageRect(CurlRenderer.PAGE_LEFT));
				mPageLeft.reset();
				if (mRenderLeftPage) {
					mRenderer.addCurlMesh(mPageLeft);
					Log.v("matrix","2aa");
				}
				
				
			}

			// If there is new/next available, set it to right page.
			if (mCurrentIndex < mBitmapProvider.getBitmapCount() - 1) {
				Log.v("matrix","3aa");
				
				Bitmap bitmap = mBitmapProvider.getBitmap(mPageBitmapWidth,
						mPageBitmapHeight, mCurrentIndex + 1);
				mPageRight.setBitmap(bitmap);
				mPageRight.setRect(mRenderer
						.getPageRect(CurlRenderer.PAGE_RIGHT));
				mPageRight.setFlipTexture(false);
				mPageRight.reset();
				mRenderer.addCurlMesh(mPageRight);
			}

			// Add curled page to renderer.
			mPageCurl.setRect(mRenderer.getPageRect(CurlRenderer.PAGE_RIGHT));
			mPageCurl.setFlipTexture(false);
			mPageCurl.reset();
			mRenderer.addCurlMesh(mPageCurl);

			mCurlState = CURL_RIGHT;
			break;
		}

			// On left side curl, left page is assigned to curled page. And if
			// there are more bitmaps available before currentIndex, new bitmap
			// is loaded into left page.
		case CURL_LEFT: {
			// Remove meshes from renderer.
			mRenderer.removeCurlMesh(mPageLeft);
			mRenderer.removeCurlMesh(mPageRight);
			mRenderer.removeCurlMesh(mPageCurl);

			Log.v(TAG,"4");
			// We are curling left page.
			CurlMesh curl = mPageLeft;
			mPageLeft = mPageCurl;
			mPageCurl = curl;
//			mZoomView.SetBitmap(curl.getBitmap());
			// If there is new/previous bitmap available load it to left page.
			if (mCurrentIndex > 1) {
				Log.v(TAG,"5");
				Bitmap bitmap = mBitmapProvider.getBitmap(mPageBitmapWidth,
						mPageBitmapHeight, mCurrentIndex - 2);
				mPageLeft.setBitmap(bitmap);
				mPageLeft
						.setRect(mRenderer.getPageRect(CurlRenderer.PAGE_LEFT));
				mPageLeft.setFlipTexture(true);
				mPageLeft.reset();
				if (mRenderLeftPage) {
					Log.v(TAG,"6");
					mRenderer.addCurlMesh(mPageLeft);
				}
			}

			// If there is something to show on right page add it to renderer.
			if (mCurrentIndex < mBitmapProvider.getBitmapCount()) {
				mPageRight.setRect(mRenderer
						.getPageRect(CurlRenderer.PAGE_RIGHT));
				mPageRight.reset();
				mRenderer.addCurlMesh(mPageRight);
				Log.v(TAG,"7");
			}

			// How dragging previous page happens depends on view mode.
			if (mViewMode == SHOW_ONE_PAGE) {
				mPageCurl.setRect(mRenderer
						.getPageRect(CurlRenderer.PAGE_RIGHT));
				mPageCurl.setFlipTexture(false);
				Log.v(TAG,"8");
			} else {
				mPageCurl
						.setRect(mRenderer.getPageRect(CurlRenderer.PAGE_LEFT));
				mPageCurl.setFlipTexture(true);
				Log.v(TAG,"9");
			}
			mPageCurl.reset();
			mRenderer.addCurlMesh(mPageCurl);

			mCurlState = CURL_LEFT;
			break;
		}

		}
	}

	/**
	 * Updates bitmaps for left and right meshes.
	 */
	private void updateBitmaps() {
		if (mBitmapProvider == null || mPageBitmapWidth <= 0
				|| mPageBitmapHeight <= 0) {
			return;
		}

		// Remove meshes from renderer.
		mRenderer.removeCurlMesh(mPageLeft);
		mRenderer.removeCurlMesh(mPageRight);
		mRenderer.removeCurlMesh(mPageCurl);

		
		
		
		int leftIdx = mCurrentIndex - 1;
		int rightIdx = mCurrentIndex;
		int curlIdx = -1;
		
		Log.v("number","A leftIdx = "+leftIdx);
		Log.v("number","A rightIdx = "+rightIdx);
		
		
		if (mCurlState == CURL_LEFT) {
			curlIdx = leftIdx;
			leftIdx--;
		} else if (mCurlState == CURL_RIGHT) {
			curlIdx = rightIdx;
			rightIdx++;
		}
		
		Log.v("number","B leftIdx = "+leftIdx);
		Log.v("number","B rightIdx = "+rightIdx);
		
		
		

		if (rightIdx >= 0 && rightIdx < mBitmapProvider.getBitmapCount()) {
			Bitmap bitmap = mBitmapProvider.getBitmap(mPageBitmapWidth,
					mPageBitmapHeight, rightIdx);
			mPageRight.setBitmap(bitmap);
			mPageRight.setRect(mRenderer.getPageRect(CurlRenderer.PAGE_RIGHT));
			mPageRight.reset();
			mRenderer.addCurlMesh(mPageRight);
		}
		if (leftIdx >= 0 && leftIdx < mBitmapProvider.getBitmapCount()) {
			Bitmap bitmap = mBitmapProvider.getBitmap(mPageBitmapWidth,
					mPageBitmapHeight, leftIdx);
			mPageLeft.setBitmap(bitmap);
			mPageLeft.setRect(mRenderer.getPageRect(CurlRenderer.PAGE_LEFT));
			mPageLeft.reset();
			if (mRenderLeftPage) {
				mRenderer.addCurlMesh(mPageLeft);
			}
		}
		if (curlIdx >= 0 && curlIdx < mBitmapProvider.getBitmapCount()) {
			Bitmap bitmap = mBitmapProvider.getBitmap(mPageBitmapWidth,
					mPageBitmapHeight, curlIdx);
			mPageCurl.setBitmap(bitmap);
			
			if (mCurlState == CURL_RIGHT
					|| (mCurlState == CURL_LEFT && mViewMode == SHOW_TWO_PAGES)) {
				mPageCurl.setRect(mRenderer
						.getPageRect(CurlRenderer.PAGE_RIGHT));
			} else {
				mPageCurl
						.setRect(mRenderer.getPageRect(CurlRenderer.PAGE_LEFT));
			}
			mPageCurl.reset();
			mRenderer.addCurlMesh(mPageCurl);
		}
		
		Log.v("number","C leftIdx = "+leftIdx);
		Log.v("number","C rightIdx = "+rightIdx);
		
	}

	/**
	 * Updates curl position.
	 */
	private void updateCurlPos(PointerPosition pointerPos) {

		// Default curl radius.
		double radius = mRenderer.getPageRect(CURL_RIGHT).width() / 3;
		// TODO: This is not an optimal solution. Based on feedback received so
		// far; pressure is not very accurate, it may be better not to map
		// coefficient to range [0f, 1f] but something like [.2f, 1f] instead.
		// Leaving it as is until get my hands on a real device. On emulator
		// this doesn't work anyway.
		radius *= Math.max(1f - pointerPos.mPressure, 0f);
		// NOTE: Here we set pointerPos to mCurlPos. It might be a bit confusing
		// later to see e.g "mCurlPos.x - mDragStartPos.x" used. But it's
		// actually pointerPos we are doing calculations against. Why? Simply to
		// optimize code a bit with the cost of making it unreadable. Otherwise
		// we had to this in both of the next if-else branches.
		mCurlPos.set(pointerPos.mPos);

		// If curl happens on right page, or on left page on two page mode,
		// we'll calculate curl position from pointerPos.
		if (mCurlState == CURL_RIGHT
				|| (mCurlState == CURL_LEFT && mViewMode == SHOW_TWO_PAGES)) {

			mCurlDir.x = mCurlPos.x - mDragStartPos.x;
			mCurlDir.y = mCurlPos.y - mDragStartPos.y;
			float dist = (float) Math.sqrt(mCurlDir.x * mCurlDir.x + mCurlDir.y
					* mCurlDir.y);

			// Adjust curl radius so that if page is dragged far enough on
			// opposite side, radius gets closer to zero.
			float pageWidth = mRenderer.getPageRect(CurlRenderer.PAGE_RIGHT)
					.width();
			double curlLen = radius * Math.PI;
			if (dist > (pageWidth * 2) - curlLen) {
				curlLen = Math.max((pageWidth * 2) - dist, 0f);
				radius = curlLen / Math.PI;
			}

			// Actual curl position calculation.
			if (dist >= curlLen) {
				double translate = (dist - curlLen) / 2;
				mCurlPos.x -= mCurlDir.x * translate / dist;
				mCurlPos.y -= mCurlDir.y * translate / dist;
			} else {
				double angle = Math.PI * Math.sqrt(dist / curlLen);
				double translate = radius * Math.sin(angle);
				mCurlPos.x += mCurlDir.x * translate / dist;
				mCurlPos.y += mCurlDir.y * translate / dist;
			}

			setCurlPos(mCurlPos, mCurlDir, radius);
		}
		// Otherwise we'll let curl follow pointer position.
		else if (mCurlState == CURL_LEFT) {

			// Adjust radius regarding how close to page edge we are.
			float pageLeftX = mRenderer.getPageRect(CurlRenderer.PAGE_RIGHT).left;
			radius = Math.max(Math.min(mCurlPos.x - pageLeftX, radius), 0f);

			float pageRightX = mRenderer.getPageRect(CurlRenderer.PAGE_RIGHT).right;
			mCurlPos.x -= Math.min(pageRightX - mCurlPos.x, radius);
			mCurlDir.x = mCurlPos.x + mDragStartPos.x;
			mCurlDir.y = mCurlPos.y - mDragStartPos.y;

			setCurlPos(mCurlPos, mCurlDir, radius);
		}
	}

	/**
	 * Provider for feeding 'book' with bitmaps which are used for rendering
	 * pages.
	 */
	public interface BitmapProvider {

		/**
		 * Called once new bitmap is needed. Width and height are in pixels
		 * telling the size it will be drawn on screen and following them
		 * ensures that aspect ratio remains. But it's possible to return bitmap
		 * of any size though.<br/>
		 * <br/>
		 * Index is a number between 0 and getBitmapCount() - 1.
		 */
		public Bitmap getBitmap(int width, int height, int index);

		/**
		 * Return number of pages/bitmaps available.
		 */
		public int getBitmapCount();
	}

	/**
	 * Observer interface for handling CurlView size changes.
	 */
	public interface SizeChangedObserver {

		/**
		 * Called once CurlView size changes.
		 */
		public void onSizeChanged(int width, int height);
	}

	/**
	 * Simple holder for pointer position.
	 */
	private class PointerPosition {
		PointF mPos = new PointF();
		float mPressure;
	}

}
