package com.strastar.zolaman.classlist;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

public class AddViewFrame extends RelativeLayout
{
	final String TAG="AddViewFrame";
	
	public AddViewFrame(Context context, int resourceid) {
		super(context);
		// TODO Auto-generated constructor stub
		String infService = Context.LAYOUT_INFLATER_SERVICE;
        
		LayoutInflater li = (LayoutInflater) getContext().getSystemService(infService);
        View v = li.inflate(resourceid, this, false);
        addView(v);
        
        setFocusable(true);
        setFocusableInTouchMode(true);       
        //setBackgroundColor(Color.BLUE);
	}

	
}
