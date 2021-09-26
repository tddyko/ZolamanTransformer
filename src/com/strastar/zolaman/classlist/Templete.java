package com.strastar.zolaman.classlist;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

public class Templete extends Activity {
    private final String TAG="Templete";
    private Context mContext = Templete.this;
    private Handler mHandler = new Handler();
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.main);
        Log.d(TAG,"onCreate invoked");
    }
    
    @Override
	protected void onStart() {
    	super.onStart();
    	Log.d(TAG, "onStart() invoked");
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	Log.d(TAG, "onResume() invoked");
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	Log.d(TAG, "onPause() invoked");
    }
    
    @Override
    protected void onStop() {
    	super.onStop();
    	Log.d(TAG, "onStop() invoked");
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	Log.d(TAG, "onDestroy() invoked");
    }   
    
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
    	super.onRestoreInstanceState(savedInstanceState);
    	Log.d(TAG, "onRestoreInstanceState(Bundle) invoked");
    }
    
    @Override
    protected void onSaveInstanceState(Bundle b) {
    	super.onSaveInstanceState(b);
    	Log.d(TAG, "onSaveInstanceState(Bundle) invoked");
    }
}