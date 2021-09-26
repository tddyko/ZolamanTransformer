/*
 * Copyright (C) 2010 Jason Fry
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @author Jason Fry - jasonfry.co.uk
 * @version 1.0
 * 
 */

package com.strastar.zolaman.classlist;

//import com.strastar.zolamangunz.PageControl.OnPageControlClickListener;


import com.strastar.zolaman.classlist.PageControl.OnPageControlClickListener;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.FrameLayout.LayoutParams;


public class SwipeView extends HorizontalScrollView
{
	private LinearLayout mLinearLayout;
	private Context mContext;
	private int SCREEN_WIDTH;
	private int motionStartX=320;
	private int distanceX;
	private int previousDirection;
	private int mCurrentPage;
	private boolean firstMotionEvent = false;
	private OnPageChangedListener mOnPageChangedListener = null;
	private SwipeOnTouchListener mSwipeOnTouchListener;
	
	private PageControl mPageControl = null;
	
//	private static int DEFAULT_SWIPE_THRESHOLD = 70;
	private static int DEFAULT_SWIPE_THRESHOLD = 60;//80; //100;//50;//60; //80;
	private static int DEFAULT_JITTER = 2;//4;//8; //4;
	

	public SwipeView(Context context) 
	{
		super(context);
		mContext = context;
		initSwipeView();
	}
	
	public SwipeView(Context context, AttributeSet attrs) 
	{
		super(context, attrs);
		mContext = context;
		initSwipeView();
	}
	
	public SwipeView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs,defStyle);
		mContext = context;
		initSwipeView();
	}
	
	private void initSwipeView()
	{
		Log.i("uk.co.jasonfry.android.tools.ui.SwipeView","Initialising SwipeView");
		mLinearLayout = new LinearLayout(mContext);
		mLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
		super.addView(mLinearLayout, -1, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		
		setSmoothScrollingEnabled(true);
		setHorizontalFadingEdgeEnabled(false);
		setHorizontalScrollBarEnabled(false);
		
		Display display = ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay(); 
		SCREEN_WIDTH = (int) (display.getWidth());
		mCurrentPage = 0;
		
		mSwipeOnTouchListener = new SwipeOnTouchListener();
		setOnTouchListener(mSwipeOnTouchListener);
		
		
	}
	
	@Override
	public boolean onTrackballEvent(MotionEvent event)
	{
		return true;
	}
	
	@Override
	public void addView(View child)
	{
		addView(child,-1);
	}
	
	@Override
	public void addView (View child, int index)
	{
		ViewGroup.LayoutParams params;
		params = new LayoutParams(SCREEN_WIDTH, LayoutParams.FILL_PARENT);
		addView(child, index, params);
	}
	
	@Override
	public void addView (View child, ViewGroup.LayoutParams params)
	{
		addView (child, -1, new LayoutParams(SCREEN_WIDTH, params.height));
	}
	
	@Override
	public void addView (View child, int index, ViewGroup.LayoutParams params)
	{
		requestLayout();
		invalidate();
		mLinearLayout.addView(child, index, params);
	}
	
	/**
	 * Get the View object that contains all the children of this SwipeView. The same as calling getChildAt(0)
	 * A SwipeView behaves slightly differently from a normal ViewGroup, all the children of a SwipeView
	 * sit within a LinearLayout, which then sits within the SwipeView object. 
	 * 
	 * @return linearLayout The View object that contains all the children of this view
	 */
	public LinearLayout getChildContainer()
	{
		return mLinearLayout;
	}
	
	
	
	/**
	 * Get the swiping threshold distance to make the screens change
	 * 
	 * @return swipeThreshold The minimum distance the finger should move to allow the screens to change
	 */
	public int getSwipeThreshold()
	{
		return DEFAULT_SWIPE_THRESHOLD;
	}
	
	/**
	 * Set the swiping threshold distance to make the screens change
	 * 
	 * @param swipeThreshold The minimum distance the finger should move to allow the screens to change
	 */
	public void setSwipeThreshold(int swipeThreshold)
	{
		DEFAULT_SWIPE_THRESHOLD = swipeThreshold;
	}
	
	/**
	 * Get the current page the SwipeView is on
	 * 
	 * @return The current page the SwipeView is on
	 */
	public int getCurrentPage()
	{
		return mCurrentPage;
	}
	
	/**
	 * Return the number of pages in this SwipeView
	 * 
	 * @return Returns the number of pages in this SwipeView
	 */
	public int getPageCount()
	{
		return mLinearLayout.getChildCount();
	}
	
	/**
	 * Go directly to the specified page
	 * 
	 * @param page The page to scroll to
	 */
	public void scrollToPage(int page)
	{
		scrollTo(page*getMeasuredWidth(),0);
		if(mOnPageChangedListener!=null)
		{
			mOnPageChangedListener.onPageChanged(mCurrentPage, page);
		}
		if(mPageControl!=null)
		{
			mPageControl.setCurrentPage(page);
		}
		mCurrentPage = page;
	}
	
	/**
	 * Animate a scroll to the specified page
	 * 
	 * @param page The page to animate to
	 */
	public void smoothScrollToPage(int page)
	{
		smoothScrollTo(page*getMeasuredWidth(),0);
		if(mOnPageChangedListener!=null)
		{
			mOnPageChangedListener.onPageChanged(mCurrentPage, page);
		}
		if(mPageControl!=null)
		{
			mPageControl.setCurrentPage(page);
		}
		mCurrentPage = page;
	}
	
	/**
	 * Assign a PageControl object to this SwipeView. Call after adding all the children
	 * 
	 * @param pageControl The PageControl object to assign
	 */
	public void setPageControl(PageControl pageControl)
	{
		mPageControl = pageControl;
		
		pageControl.setPageCount(getPageCount());
		pageControl.setCurrentPage(mCurrentPage);
		pageControl.setOnPageControlClickListener(new OnPageControlClickListener() 
		{
			
			public void goForwards() 
			{
				smoothScrollToPage(mCurrentPage+1);
				
			}
			
			public void goBackwards() 
			{
				smoothScrollToPage(mCurrentPage-1);
				
			}
		});
	}
	
	/**
	 * Return the current PageControl object
	 * 
	 * @return Returns the current PageControl object
	 */
	public PageControl getPageControl()
	{
		return mPageControl;
	}
	
	/**
	 * Implement this listener to listen for page change events
	 * 
	 * @author Jason Fry - jasonfry.co.uk
	 *
	 */
	public interface OnPageChangedListener
	{
		/**
		 * Event for when a page changes
		 * 
		 * @param oldPage The page the view was on previously
		 * @param newPage The page the view has moved to
		 */
		public abstract void onPageChanged(int oldPage, int newPage);
	}
	
	/**
	 * Set the current OnPageChangedListsner
	 * 
	 * @param onPageChangedListener The OnPageChangedListener object
	 */
	public void setOnPageChangedListener(OnPageChangedListener onPageChangedListener)
	{
		mOnPageChangedListener = onPageChangedListener;
	}
	
	/**
	 * Get the current OnPageChangeListsner
	 * 
	 * @return The current OnPageChangedListener
	 */
	public OnPageChangedListener getOnPageChangedListener()
	{
		return mOnPageChangedListener;
	}


	private class SwipeOnTouchListener implements View.OnTouchListener
	{
		public boolean onTouch(View v, MotionEvent event) 
		{
			switch(event.getAction())
			{
				case MotionEvent.ACTION_DOWN :
					Log.d("[ACTION_DOWN]",String.format("event.getX()=%d event.getY()=%d ",
							(int)event.getX(),(int)event.getY()));
					
					motionStartX = (int) event.getX();
					firstMotionEvent = false;
					
					return false;
					//break;
					
				case MotionEvent.ACTION_MOVE :
					int newDistance = motionStartX - (int) event.getX();
					//ACTION_DOWN 이벤트가 이상하게 안먹힘 따라서 
					//motionStartX 값을 주석처리함...
					//int newDistance = 0 - (int) event.getX();
					int newDirection;
					
					if(newDistance<0) //backwards
					{
						// -267
						// A 에서 B로 MOVE했을때 4 이상이 벌어지면 화면 유지 ..
						newDirection =  (distanceX+DEFAULT_JITTER <= newDistance) ? 1 : -1;  //the distance +4 is to allow for jitter
						Log.d("[ACTION_MOVE 1]backwards",String.format("distanceX=%d newDistance=%d"
								, distanceX, newDistance));
					}
					else //forwards
					{
						newDirection =  (distanceX-DEFAULT_JITTER <= newDistance) ? 1 : -1;  //the distance -4 is to allow for jitter
						Log.d("[ACTION_MOVE 1]forwards",String.format("distanceX=%d newDistance=%d"
								, distanceX, newDistance));
					}
					
					if(newDirection != previousDirection && !firstMotionEvent)//changed direction, so reset start point
					{
						motionStartX = (int) event.getX();
						distanceX = motionStartX - (int) event.getX();
						Log.d("[ACTION_MOVE 2]",String.format("motionStartX=%d", motionStartX));
					}
					else
					{
						distanceX = newDistance;
						Log.d("[ACTION_MOVE 3]",String.format("motionStartX=%d", motionStartX));
					}

					previousDirection = newDirection; //backwards -1, forwards is 1
					
					Log.d("[ACTION_MOVE 4]",String.format("previousDirection=%d distanceX=%d ",
							previousDirection,distanceX));
					
					return false;
					//break;
					
				case MotionEvent.ACTION_UP :
					// fingerUpPosition : 좌우이미지가 보이는 정도 (0 - 480)
					float fingerUpPosition = getScrollX(); 
	                float numberOfPages = mLinearLayout.getChildCount();
	                float pageWidth = getMeasuredWidth();
	                float fingerUpPage = fingerUpPosition/pageWidth;
	                float edgePosition = 0;
	                
	                Log.d("ACTION_UP",String.format("distanceX(%d) fingerUpPosition(%d) numberOfPages(%d) fingerUpPage(%f)",
	                		distanceX, (int)fingerUpPosition, (int)numberOfPages, fingerUpPage));
	                
	                if(previousDirection == 1) //forwards
	                {
	                	if(distanceX > DEFAULT_SWIPE_THRESHOLD)//if over then go forwards
		                {
	                		edgePosition = (int)(fingerUpPage+1)*pageWidth;
	                		Log.d("ACTION_UP",String.format("[if]edgePosition(%f) pageWidth(%f)",edgePosition,pageWidth));
//		                	if(mCurrentPage<(numberOfPages-1))//if not at the end of the pages, you don't want to try and advance into nothing!
//		                	{
//		                		edgePosition = (int)(fingerUpPage+1)*pageWidth;
//		                	}
//		                	else
//		                	{
//		                		edgePosition = (int)(fingerUpPage)*pageWidth;
//		                	}
		                }
		                else //return to start position
		                {
		                	if(Math.round(fingerUpPage)==numberOfPages-1)//if at the end
		                	{
		                		//need to correct for when user starts to scroll into 
		                		//nothing then pulls it back a bit, this becomes a 
		                		//kind of forwards scroll instead
		                		edgePosition = (int)(fingerUpPage+1)*pageWidth;
		                		Log.d("ACTION_UP",String.format("[else math]edgePosition(%f) fingerUpPage(%f)"
		                				,edgePosition,fingerUpPage));
//			                	
		                	}
		                	else //carry on as normal
		                	{
		                		edgePosition = (int)(fingerUpPage)*pageWidth;
		                		Log.d("ACTION_UP",String.format("[else]edgePosition(%f) fingerUpPage(%f)"
		                				,edgePosition,fingerUpPage));
		                	}
		                }
	                }
	                else //backwards
	                {
	                	if(distanceX < -DEFAULT_SWIPE_THRESHOLD)//go backwards
		                {
		                	edgePosition = (int)(fingerUpPage)*pageWidth;
		                }
		                else //return to start position
		                {
		                	//반올림(round)하여 fingerUpPage 보이는 정도가 반이하로 보이면 기존것 유지
		                	// 반이상 보이면 다음페이지 
		                	if(Math.round(fingerUpPage)==0)//if at beginning, correct
		                	{
		                		//need to correct for when user starts to scroll into 
		                		//nothing then pulls it back a bit, this becomes a 
		                		//kind of backwards scroll instead
		                		edgePosition = (int)(fingerUpPage)*pageWidth;
		                	}
		                	else //carry on as normal
		                	{
		                		edgePosition = (int)(fingerUpPage+1)*pageWidth;
		                	}
		                	
		                }
	                }
	                
	                Log.d("ACTION_UP","smoothScrollTo : " + (int)edgePosition);
	                smoothScrollTo((int)edgePosition, 0);
//	                firstMotionEvent = true;
	                
	                
	                //fire OnPageChangedListener, talk to page control
	                if(mCurrentPage!=(int)(edgePosition/pageWidth) && (int)(edgePosition/pageWidth)<getPageCount()) //if the page at the beginning of this action is not equal to page we are now on, i.e. if the page has changed
	                {
	                	Log.d("ACTION_UP",String.format("mCurrentPage=%d edgePosition=%d pageWidth=%d"
	                			,mCurrentPage, (int)edgePosition, (int)pageWidth  ));
	                	//page control
	                	if(mPageControl!=null)
	                	{
	                		mPageControl.setCurrentPage((int)(edgePosition/pageWidth));
	                	}
	               
	                	//page changed listener 
	                	if(mOnPageChangedListener!=null)
	                	{
	                		mOnPageChangedListener.onPageChanged(mCurrentPage, (int)(edgePosition/pageWidth));
	                	}
	                }
	                
	                mCurrentPage = (int)(edgePosition/pageWidth);
	                return true;
	                //break;
			}
			//return true;
			return false;
		}
		
	}

	
}	

