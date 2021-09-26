/***
	Copyright (c) 2008-2009 CommonsWare, LLC
	
	Licensed under the Apache License, Version 2.0 (the "License"); you may
	not use this file except in compliance with the License. You may obtain
	a copy of the License at
		http://www.apache.org/licenses/LICENSE-2.0
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
*/

package com.strastar.zolaman;



import com.strastar.zolamantransformer.R;

import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

class ViewWrapper {
	View base;
	TextView mTv_bookmark=null;
	Button mBtn_delete=null;
	
	public ViewWrapper(View base) {
		this.base=base;
	}	
	
	TextView getmTv_bookmark() {
        if (mTv_bookmark==null) {
        	mTv_bookmark=(TextView)base.findViewById(R.id.tv_bookmark);
        }
        return(mTv_bookmark);
	}
	
	Button getmBtn_delete() {
        if (mBtn_delete==null) {
        	mBtn_delete=(Button)base.findViewById(R.id.btn_delete);
        }
        return(mBtn_delete);
	}
}
