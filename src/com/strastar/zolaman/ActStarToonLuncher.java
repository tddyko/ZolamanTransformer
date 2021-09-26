package com.strastar.zolaman;



import com.strastar.zolamantransformer.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
//import android.drm.DrmStore.Action;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class  ActStarToonLuncher extends Activity {
    private final String TAG="Templete";
    private Context mContext = ActStarToonLuncher.this;
    private Handler mHandler = new Handler();
    private Runnable myRunable;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actstartoonluncher);
        RelativeLayout rl_luncher = (RelativeLayout)findViewById(R.id.rl_luncher);
        rl_luncher.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mHandler.removeCallbacks(myRunable);
				startMainActivity();
			}
		});
        
        ((Button)findViewById(R.id.btn_facebook)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent i = new Intent(Intent.ACTION_VIEW,Uri.parse("http://www.facebook.com/strastar"));
				startActivity(i);
			}
		});
        
        ((Button)findViewById(R.id.btn_twitter)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent i = new Intent(Intent.ACTION_VIEW,Uri.parse("http://twitter.com/straapp"));
				startActivity(i);
			}
		});
        
        myRunable = new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				startMainActivity();
			}
		};
        
        mHandler.postDelayed(myRunable, 5000);
        Log.d(TAG,"onCreate invoked");
    }
    
    private void startMainActivity(){
    	Log.d(TAG,"startMainActivity()");
    	Intent i = new Intent(mContext, MainTable.class);
    	startActivity(i);
    	finish();
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