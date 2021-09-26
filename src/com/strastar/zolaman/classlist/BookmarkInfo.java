package com.strastar.zolaman.classlist;

public class BookmarkInfo {
	public static final String DEF_PREF_BOOKMARKLIST="_:_:_";
	
    private String m1BookIdx="_";	 			// 북마크 책
    private String m2BookPage="_"; 			// 북마크 페이지
    private String m3BookPath="_"; 		// 북마크 책 패스. 
    
    public String getm1BookIdx() { return m1BookIdx; }
    public String getm2BookPage() { return m2BookPage; }
    public String getm3BookPath() { return m3BookPath; }
    
    //public void setmUsername(String _mUsername){ mUsername = _mUsername; }
    public void setm1BookIdx(String _m1BookIdx) { m1BookIdx = _m1BookIdx; }
    public void setm2BookPage(String _m2BookPage) { m2BookPage = _m2BookPage; }
    public void setm3BookPath(String _m3BookPath) { m3BookPath = _m3BookPath; }
	
}
/*
 
1:0:false:_:_:_:_:_:,2:2000:false:_:_:_:_:_:,3:2000:false:_:_:_:_:_:,4:2000:false:_:_:_:_:_:,5:2000:false:_:_:_:_:_:,6:2000:false:_:_:_:_:_:,7:2000:false:_:_:_:_:_:,8:2000:false:_:_:_:_:_:,9:2000:false:_:_:_:_:_:,10:2000:false:_:_:_:_:_:,11:2000:false:_:_:_:_:_:,12:2000:false:_:_:_:_:_:,

*/