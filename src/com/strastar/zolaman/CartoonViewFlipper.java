package com.strastar.zolaman;

import java.io.File;
import java.util.ArrayList;

import com.strastar.zolaman.classlist.BookmarkInfo;
import com.strastar.zolaman.classlist.Common;
import com.strastar.zolaman.classlist.CurlView;
import com.strastar.zolaman.classlist.FileDecoder;
import com.strastar.zolaman.classlist.ZoomView;
import com.strastar.zolamantransformer.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout.LayoutParams;

public class CartoonViewFlipper extends Activity {
    private final String TAG="Z_CartoonViewFlipper";
    private Context mContext = CartoonViewFlipper.this;
    private Handler mHandler = new Handler();

	private CurlView mCurlView;
	//private CurlView v_cur;
	public static LinearLayout mll_footer, mll_header;
//////////////////////////pyw add :  추가된 소스입니다.
	private ZoomView mZoomView;
    public LinearLayout zoomlayout;
    private boolean mapstart = true;
    
////////////////////////////////////
	public ImageView btn_prev;
	ImageView btn_slideshow;
	ImageView btn_info;
	ImageView btn_next;
	public static TextView page_count;
	
	public static int page_index;
	public static int photosize;
	public static SeekBar seekbar;
	
	public static Animation animDownShow, animDownClose;
	public static Animation animUpShow, animUpClose;
	
	int seekbar_number, cartoonfiles_count=190;
	private TextView mTv_start;
	public static String mBitmapIds[];
	Resources res;
	private ProgressDialog mpd_waiting;
	private ArrayList<BookmarkInfo> mBookmarkList = new ArrayList<BookmarkInfo>();
    private BookmarkArrayAdapter mBookmarkArrayAdapter=null;
    private ListView mlv_bookmark;
    private ProgressBar mPb_waiting;
	private boolean mCreateFlag=false;
	private ViewFlipper flipper;
	
	private class BookmarkArrayAdapter extends ArrayAdapter<BookmarkInfo> {
		Activity context;
		//int mPos=0;
		BookmarkArrayAdapter(Activity context){
			super(context, R.layout.row_bookmark, mBookmarkList);
			this.context=context;
		}
		
		// item이 화면에 표시될때 마다 호출되는 callback
		public View getView(int position, View convertView,ViewGroup parent) {
			//final int mPos = position;
			//if(Common.DEBUG) Log.d(TAG,"mPos="+position);
			View row=convertView;
			ViewWrapper wrapper=null;			
			if (row==null) {
				LayoutInflater inflater=context.getLayoutInflater();	
				row=inflater.inflate(R.layout.row_bookmark, null);		
				wrapper=new ViewWrapper(row);				
				row.setTag(wrapper);
			}
			else {
				wrapper=(ViewWrapper)row.getTag();
			}
			
			final int mPos = position;
			final BookmarkInfo b = (BookmarkInfo)mBookmarkList.get(mPos);
			final String bookmarkstring = String.format("%s %d 권 - %s 페이지"
					,getString(R.string.app_name),Integer.parseInt(b.getm1BookIdx())+1, b.getm2BookPage());
			wrapper.getmTv_bookmark().setText(bookmarkstring);
			
			wrapper.getmBtn_delete().setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					//final BookmarkInfo b = (BookmarkInfo)mBookmarkList.get(mPos);
					
					new AlertDialog.Builder(mContext).setIcon(R.drawable.icon)
	    			.setTitle("경고")
	    			.setMessage(String.format("북마크를 삭제하시겠습니까?\n(%s)", bookmarkstring))
	    			.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		                public void onClick(DialogInterface dialog, int whichButton) {
		                    mBookmarkList.remove(mPos);
		                    mBookmarkArrayAdapter.notifyDataSetChanged();
		                    savePrefBookmarkList(getIntent().getStringExtra("book_idx"));
		                }
		            })
		            .setNegativeButton("No", new DialogInterface.OnClickListener() {
		                public void onClick(DialogInterface dialog, int whichButton) { }
		            })
		            .show();
				}
			});
			
			//row.setOnClickListener(new OnItemClickListener(position));
			return(row);
		}
	}
		
	@Override
    protected Dialog onCreateDialog(int id) {
	    Dialog dialog;
	    switch(id) {
	    case Common.DIALOG_LOADING_ID:
	            dialog = ProgressDialog.show(mContext, "", "Loading...", true);
	            dialog.setCancelable(true);
	            break;
	
	    default:
	        dialog = null;
	    }
	    return dialog;
	}
	
	public static void showPopupToggle(int visibleflag){
		mll_header.setVisibility(visibleflag);
		mll_footer.setVisibility(visibleflag);
		if(visibleflag==View.VISIBLE){
			mll_header.startAnimation(animDownShow);
			mll_footer.startAnimation(animUpShow);
		}
		else{
			mll_header.startAnimation(animDownClose);
			mll_footer.startAnimation(animUpClose);
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cartoonviewflipper);
		setResult(RESULT_CANCELED);
		
//		LinearLayout l = (LinearLayout)findViewById(R.id.ll_preview);
//		if(Common.DEBUG) Log.d(TAG,"book_page="+getIntent().getStringExtra("book_page"));
//		if(!getIntent().getStringExtra("book_page").equals("0")){
//			l.setVisibility(View.GONE);
//		}
		zoomlayout = (LinearLayout) findViewById(R.id.zoomView);
		zoomlayout.setVisibility(View.INVISIBLE);
		mapstart = true;
		
		
		if(Common.DEBUG) Log.d(TAG,"book_page="+getIntent().getStringExtra("book_page"));
		if(getIntent().getStringExtra("book_page").equals("0")){
			((FrameLayout)findViewById(R.id.fl_preview)).setVisibility(View.VISIBLE);
			if(getIntent().getIntExtra("display", 0)==Common.DENSITY_MEDIUM){
				((TextView)findViewById(R.id.tv_prev)).setText(String.format("%s\n%s","이전페이지","(클릭)"));
				((TextView)findViewById(R.id.tv_menu)).setText(String.format("%s\n%s","메뉴페이지","(클릭)"));
				((TextView)findViewById(R.id.tv_next)).setText(String.format("%s\n%s","다음페이지","(클릭)"));
			}
		}
		else {
			((FrameLayout)findViewById(R.id.fl_preview)).setVisibility(View.GONE);
		}
		
		showDialog(Common.DIALOG_LOADING_ID);
		mHandler.postDelayed(new Runnable() { 
		       @Override
		       public void run() { 
		    	   dismissDialog(Common.DIALOG_LOADING_ID);
		    	   Toast.makeText(mContext,String.format("[%d/%d]", page_index,cartoonfiles_count), Toast.LENGTH_SHORT).show();
		       }
		}, 2000);        
		
		//res = getResources();
		//Intent intent = getIntent();
		
	}
	
	private void showCartoonView(String book_idx, String book_page, String path){
		mBitmapIds = new String[cartoonfiles_count];
		photosize = cartoonfiles_count;
		for(int i=0; i<cartoonfiles_count;i++){
			mBitmapIds[i]=String.format("%s/%03d.jpg", path,i+1);
			if(Common.DEBUG) Log.d(TAG,"mBitmapId="+mBitmapIds[i]);
		}

		TextView tv = (TextView)findViewById(R.id.tv_title);
		tv.setText(String.format("%s %d권",getString(R.string.app_name), Integer.parseInt(book_idx)+1));
		
//		int index = 0;
//		if (getLastNonConfigurationInstance() != null) {
//			index = (Integer) getLastNonConfigurationInstance();
//		}
		mCurlView.setBitmapProvider(new BitmapProvider());
		mCurlView.setSizeChangedObserver(new SizeChangedObserver());
		Integer page = Integer.parseInt(book_page);
		page_index = page+1;
		if(Common.DEBUG) Log.d(TAG,String.format("currpgae(%d)", page));
		mCurlView.setCurrentIndex(page);
	}
	
	private void addHook(){
		flipper = (ViewFlipper)findViewById(R.id.vf_main);

		mCurlView = (CurlView) findViewById(R.id.curl);
		
		//////////////////추가된 소스입니다. ////////
		mZoomView = new ZoomView(mContext);
		zoomlayout.addView(mZoomView,
        		new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		
		mCurlView.setzoomclass(mZoomView);
		mZoomView.setLayout(zoomlayout, mCurlView);
		//////////////////////////////////
		
		mll_header = (LinearLayout)findViewById(R.id.ll_header);
		Drawable d = mll_header.getBackground();
		d.setAlpha(220);
		mll_footer = (LinearLayout)findViewById(R.id.ll_footer);
		d = mll_footer.getBackground();
		d.setAlpha(220);
		animUpShow = AnimationUtils.loadAnimation(this, R.anim.fromdowntoup);
		animUpClose = AnimationUtils.loadAnimation(this, R.anim.fromdowntoupclose);
		animDownShow = AnimationUtils.loadAnimation(this, R.anim.fromuptodown);
		animDownClose = AnimationUtils.loadAnimation(this, R.anim.fromuptodownclose);
		mTv_start = (TextView)findViewById(R.id.tv_start);
		seekbar = (SeekBar)findViewById(R.id.sb_currentpage);
		seekbar.setOnSeekBarChangeListener(soundcontrolListener);
		seekbar.setMax(cartoonfiles_count-1);
		
		
		showPopupToggle(View.GONE);
		Button b = (Button)findViewById(R.id.btn_cancel);
		b.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				returnActivityValue();
				finish();
			}
		});
		
		b = (Button)findViewById(R.id.btn_bookmark);
		b.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				flipper.showNext();
				loadBookmarks(getIntent().getStringExtra("book_idx"));
			}
		});
		//031-718-6114
		((FrameLayout)findViewById(R.id.fl_preview)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				((FrameLayout)v).setVisibility(View.GONE);
			}
		});
	}
	
	@Override
	protected void onStart(){
		super.onStart();
		if(!mCreateFlag) {
			mCreateFlag=true;
			mHandler.post(new Runnable() {
	            public void run() {
	            	init();
	            	addHook();
	    			showCartoonView(getIntent().getStringExtra("book_idx"), getIntent().getStringExtra("book_page"), getIntent().getStringExtra("path"));
			    }
			});
		}
	}
	
	private void init(){
//    	mPb_waiting = (ProgressBar)findViewById(R.id.pb_waiting);
//    	mPb_waiting.setVisibility(View.GONE);
    	
    	mlv_bookmark = (ListView)findViewById(R.id.lv_bookmarks);
//        LayoutAnimationController layoutAni;
//        layoutAni = AnimationUtils.loadLayoutAnimation(mContext, R.anim.list_layout_controller);
//        mlv_bookmark.setLayoutAnimation(layoutAni);
        mBookmarkArrayAdapter = new BookmarkArrayAdapter(this);
		mlv_bookmark.setAdapter(mBookmarkArrayAdapter);
		mlv_bookmark.setOnItemClickListener(new OnItemClickListener() {
			@Override
            public void onItemClick(AdapterView<?> a, View v, final int position, long id) { 
				if(Common.DEBUG) Log.d(TAG,"[mlv_bookmark]onClick : " + position);
//				flipper.setInAnimation(AnimationUtils.loadAnimation(mContext,R.anim.push_right_in));
//				flipper.setOutAnimation(AnimationUtils.loadAnimation(mContext,R.anim.push_right_out));
				flipper.showPrevious();
				final BookmarkInfo b = (BookmarkInfo)mBookmarkList.get(position); 
				seekbar.setProgress(Integer.parseInt(b.getm2BookPage())-1);
		        seekbar_number = seekbar.getProgress();
		        if(seekbar_number>=cartoonfiles_count){
					seekbar_number=cartoonfiles_count-1;
				}
				mCurlView.setRepage(seekbar_number);
				page_index = seekbar_number+1;
				Toast.makeText(mContext,String.format("[%d/%d]", page_index,cartoonfiles_count), Toast.LENGTH_SHORT).show();
	        	
            }
		});
		
		Button b = (Button)findViewById(R.id.btn_add);
		Drawable d = b.getBackground();
		d.setAlpha(200);
		b.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
//				Intent i = new Intent(mContext, BookmarksView.class);
//				i.putExtra("book_idx", getIntent().getStringExtra("book_idx"));
//				Integer page = mCurlView.getCurrentIndex()+1;
//				i.putExtra("book_page", page.toString());
//				i.putExtra("path", getIntent().getStringExtra("path"));
//				startActivityForResult(i, R.id.ll_bookmarks);
				
				BookmarkInfo b = new BookmarkInfo();
				b.setm1BookIdx(getIntent().getStringExtra("book_idx"));
				Integer page = mCurlView.getCurrentIndex()+1;
				b.setm2BookPage(page.toString());
				b.setm3BookPath(getIntent().getStringExtra("path"));
				
				for(int i=0;i<mBookmarkList.size();i++){
					BookmarkInfo bi = mBookmarkList.get(i);
					if(bi.getm1BookIdx().equals(b.getm1BookIdx()) && 
							bi.getm2BookPage().equals(b.getm2BookPage())){
						Toast.makeText(mContext, String.format("%s","이미 추가된 북마크입니다."), Toast.LENGTH_SHORT).show();
						return;
					}
				}
				mBookmarkList.add(b);
				mBookmarkArrayAdapter.notifyDataSetChanged();
				savePrefBookmarkList(getIntent().getStringExtra("book_idx"));
			}
		});
		
		b = (Button)findViewById(R.id.btn_cancel2);
		d = b.getBackground();
		d.setAlpha(200);
		b.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//finish();
				if(Common.DEBUG) Log.d(TAG,"onClick : flipper.showPrevious()");
//				flipper.setInAnimation(AnimationUtils.loadAnimation(mContext,R.anim.push_right_in));
//				flipper.setOutAnimation(AnimationUtils.loadAnimation(mContext,R.anim.push_right_out));
				flipper.showPrevious();
			}
		});
    }
	
	private void loadBookmarks(String bookidx){
    	SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(mContext);
    	String bookmarklist = p.getString(String.format("%s_%s",Common.PREF_KEY_BOOKMARKLIST,bookidx), "");
    	if(mBookmarkArrayAdapter!=null) mBookmarkArrayAdapter.clear();
    	if(!bookmarklist.equals("")){
    		String[] arr = bookmarklist.split(",");
    		for(int i=0;i<arr.length;i++){
    			String[] a = arr[i].split(":");
    			BookmarkInfo b = new BookmarkInfo();
    			b.setm1BookIdx(a[0]);
    			b.setm2BookPage(a[1]);
    			b.setm3BookPath(a[2]);
    			mBookmarkList.add(b);
    		}
    		mBookmarkArrayAdapter.notifyDataSetChanged();
    	}
    }

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mCurlView.destroyDrawingCache();
	}

	@Override
	public void onPause() {
		super.onPause();
		Log.v(TAG,"onPause() ");
		//mCurlView.onPause();
	}
	
	@Override
	protected void onStop() {
    	super.onStop();
    	if(Common.DEBUG) Log.d(TAG, "onStop() invoked");
    	//savePrefBookmarkList();
    }
    
    private void savePrefBookmarkList(String bookidx){
    	String str="";
    	for(int i=0;i<mBookmarkList.size();i++){
    		BookmarkInfo b = mBookmarkList.get(i);
    		str+=String.format("%s:%s:%s:,"
    				,b.getm1BookIdx(),b.getm2BookPage(),b.getm3BookPath());
    	}
    	if(Common.DEBUG) Log.d(TAG,"Pref="+str);
    	PreferenceManager.getDefaultSharedPreferences(mContext).edit().putString(String.format("%s_%s",Common.PREF_KEY_BOOKMARKLIST,bookidx), str).commit();
    }
	
	public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
                // *** DO ACTION HERE ***
                if(Common.DEBUG) Log.d(TAG, "onKeyUp : KEYCODE_BACK =" + keyCode);
                returnActivityValue();
                //return false;
        }
        return super.onKeyUp(keyCode, event);
	}
	
	private void returnActivityValue(){
		Intent it = getIntent();
		Integer currpage = mCurlView.getCurrentIndex(); // seekbar.getProgress();
		it.putExtra("book_page", currpage.toString());
        setResult(RESULT_OK,it);
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.v(TAG,"onResume() ");
		//mCurlView.onResume();
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		Log.v(TAG,"index = "+mCurlView.getCurrentIndex());
		return mCurlView.getCurrentIndex();
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(Common.DEBUG) Log.d(TAG,String.format("[onActivityResult] requestcode(%d) resultCode(%d)",requestCode,resultCode));
        if(requestCode==R.id.ll_bookmarks){
        	if(resultCode == RESULT_OK){
	        	if(Common.DEBUG) Log.d(TAG,String.format("[onActivityResult] book_idx(%s) book_page(%s)"
	        			,data.getStringExtra("book_idx"), data.getStringExtra("book_page")));
	        	seekbar.setProgress(Integer.parseInt(data.getStringExtra("book_page"))-1);
	        	seekbar_number = seekbar.getProgress();
	        	if(seekbar_number>=cartoonfiles_count){
					seekbar_number=cartoonfiles_count-1;
				}
				mCurlView.setRepage(seekbar_number);
				page_index = seekbar_number+1;
				Toast.makeText(mContext,String.format("[%d/%d]", page_index,cartoonfiles_count), Toast.LENGTH_SHORT).show();
	        	//showCartoonView(data.getStringExtra("book_idx"),data.getStringExtra("book_page"),data.getStringExtra("path"));
	        }
        }
    }
	
	private SeekBar.OnSeekBarChangeListener soundcontrolListener = new SeekBar.OnSeekBarChangeListener() {
		@Override
		public void onStopTrackingTouch(SeekBar paramSeekBar) {
			// TODO Auto-generated method stub
			Log.v(TAG,"onStopTrackingTouch = "+paramSeekBar.getProgress());
			if(Common.DEBUG) Log.d(TAG,"seekbar_number = "+seekbar_number);
			if(seekbar_number>=cartoonfiles_count){
				seekbar_number=cartoonfiles_count-1;
			}
			mCurlView.setRepage(seekbar_number);
			page_index = seekbar_number+1;
			Toast.makeText(mContext,String.format("[%d/%d]", page_index,cartoonfiles_count), Toast.LENGTH_SHORT).show();
		}
		
		@Override
		public void onStartTrackingTouch(SeekBar paramSeekBar) {
			// TODO Auto-generated method stub
			Log.v(TAG,"onStartTrackingTouch "+paramSeekBar);
			
		}
		
		@Override
		public void onProgressChanged(SeekBar paramSeekBar, int paramInt,
				boolean paramBoolean) {
			// TODO Auto-generated method stub
			Log.v(TAG,"paramInt = "+paramInt +"   "+"paramBoolean = "+paramBoolean );
			//Toast.makeText(mContext,String.format("%d/%d", paramInt+1,cartoonfiles_count), Toast.LENGTH_SHORT).show();
			seekbar_number = paramInt;
			if(paramInt<cartoonfiles_count){
				mTv_start.setText(String.format("%d", paramInt+1));
			}
		}
	};
	
	
	/**
	 * Bitmap provider.
	 */
	static Bitmap page_bitmap=null;
	private class BitmapProvider implements CurlView.BitmapProvider {

		//private int[] mBitmapIds = {};
		
		/*R.drawable.book001,R.drawable.book002,R.drawable.book003,R.drawable.book004,R.drawable.book005,R.drawable.book006,
		R.drawable.book007,R.drawable.book008,R.drawable.book009,R.drawable.book010,R.drawable.book011,R.drawable.book012,R.drawable.book013,
		R.drawable.book014,R.drawable.book015,R.drawable.book016,R.drawable.book017,R.drawable.book018,R.drawable.book019,R.drawable.book020,
		R.drawable.book021,R.drawable.book022,R.drawable.book023,R.drawable.book024,R.drawable.book025*/
		private FileDecoder fd;
		
		public BitmapProvider(){
			this.fd = new FileDecoder();
		}
		
		@Override
		public Bitmap getBitmap(int width, int height, final int index) {
			Bitmap b = Bitmap.createBitmap(width, height,
					Bitmap.Config.ARGB_8888);
			
			//Bitmap page_bitmap;
			if(page_bitmap!=null){
				page_bitmap.recycle();
				page_bitmap=null;
				System.gc();
			}
			//page_bitmap = BitmapFactory.decodeFile(mBitmapIds[index]);
			page_bitmap = fd.getDecodeBitmap(new File(mBitmapIds[index]));
			if(Common.DEBUG) Log.d(TAG,"[getBitmap]fd.getDecodeBitmap path="+mBitmapIds[index]);
		
			b.eraseColor(0xFFFFFFFF);
			Canvas c = new Canvas(b);
			Drawable d = (Drawable)(new BitmapDrawable(page_bitmap));

			Log.v(TAG,"mBitmapIds index = "+index);
			Log.v(TAG,"width = "+width);
			Log.v(TAG,"height  = "+height);
			
			int margin = 0; // 1;
			int border = 0; //2;
			Rect r = new Rect(margin, margin, width - margin, height - margin);
			//Rect r = new Rect(margin-10, margin-10, (width - margin)+10, (height - margin)+10);

			int imageWidth = r.width() - (border * 2);
			int imageHeight = imageWidth * d.getIntrinsicHeight()
					/ d.getIntrinsicWidth();
			if (imageHeight > r.height() - (border * 2)) {
				imageHeight = r.height() - (border * 2);
				imageWidth = imageHeight * d.getIntrinsicWidth()
						/ d.getIntrinsicHeight();
			}

			r.left += ((r.width() - imageWidth) / 2) - border;
			r.right = r.left + imageWidth + border + border;
			r.top += ((r.height() - imageHeight) / 2) - border;
			r.bottom = r.top + imageHeight + border + border;

			Paint p = new Paint();
			p.setColor(0xFFFFFFFF); //0xFFC0C0C0 //배경색..
			c.drawRect(r, p);
			r.left += border;
			r.right -= border;
			r.top += border;
			r.bottom -= border;

			d.setBounds(r);
			d.draw(c);
//////////////////////////pyw add :  추가된 소스입니다
			Log.v("zoomtest", "index = "+index+"   mCurlView.getCurrentIndex() = "+mCurlView.getCurrentIndex());
			if(index==mCurlView.getCurrentIndex()){
				mZoomView.SetBitmap(b,index);
				if(mapstart)
				{
					mHandler.post(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							seekbar.setProgress(index);//
							zoomlayout.setVisibility(View.VISIBLE);
						}
					});
					mapstart = false;
				}
				
			}
//////////////////////////////		
			return b;
		}

		@Override
		public int getBitmapCount() {
			Log.v(TAG,"length = "+mBitmapIds.length);
			return mBitmapIds.length;
		}
	}

	/**
	 * CurlView size changed observer.
	 */
	private class SizeChangedObserver implements CurlView.SizeChangedObserver {
		@Override
		public void onSizeChanged(int w, int h) {
			
			Log.v(TAG,"SizeChangedObserver ");
			
			if (w > h) {
				mCurlView.setViewMode(CurlView.SHOW_TWO_PAGES);
			
			} else {
				mCurlView.setViewMode(CurlView.SHOW_ONE_PAGE);
			
			}
		}
	}
}