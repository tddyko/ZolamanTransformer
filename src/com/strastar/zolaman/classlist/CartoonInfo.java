package com.strastar.zolaman.classlist;

public class CartoonInfo {
	public static final String DEF_PREF_BOOKLIST="1:0:false:_:_:_:_:_:,2:_:false:_:_:_:_:_:,3:_:false:_:_:_:_:_:,4:_:false:_:_:_:_:_:,5:_:false:_:_:_:_:_:,6:_:false:_:_:_:_:_:,7:_:false:_:_:_:_:_:,8:_:false:_:_:_:_:_:,9:_:false:_:_:_:_:_:,10:_:false:_:_:_:_:_:,11:_:false:_:_:_:_:_:,12:_:false:_:_:_:_:_:";
	
    private String m1No="_";	 			// 책 번호
    private String m2Price="_"; 			// 금액, 무료는 0원, 구매안하면 "_", 구매하면 "2000".
    private String m3DownloadType="_"; 		// 컨텐츠 다운로드유무 : true / false
    private String m4ContentPath="_"; 	// 컨텐츠 경로
    private String m5SelectedType="_"; 	// 책선택 유무 17권중 한권만 선택..
    private String m6CurrPage="_";	 	// 현재페이지..
    private String m7TotalPage="_";		// 전체페이지..
    private String m8BookMarkTitle="_"; 	// 북마크 타이틀
    private String m9BookMarkPage="_"; 	// 북마크 페이지 
    
    public String getm1No() { return m1No; }
    public String getm2Price() { return m2Price; }
    public String getm3DownloadType() { return m3DownloadType; }
    public String getm4ContentPath() { return m4ContentPath; }
    public String getm5SelectedType() { return m5SelectedType; }
    public String getm6CurrPage() { return m6CurrPage; }
    public String getm7TotalPage() { return m7TotalPage; }
    public String getm8BookMarkTitle() { return m8BookMarkTitle; }
    public String getm9BookMarkPage() { return m9BookMarkPage; }
    //public String getm() { return m; }
    
    
    //public void setmUsername(String _mUsername){ mUsername = _mUsername; }
    public void setm1No(String _mNo) { m1No = _mNo; }
    public void setm2Price(String _mPrice) { m2Price = _mPrice; }
    public void setm3DownloadType(String _mDownloadType) { m3DownloadType = _mDownloadType; }
    public void setm4ContentPath(String _mContentPath) { m4ContentPath = _mContentPath; }
    public void setm5SelectedType(String _mSelectedType) { m5SelectedType = _mSelectedType; }
    public void setm6CurrPage(String _mCurrPage) { m6CurrPage = _mCurrPage; }
    public void setm7TotalPage(String _mTotalPage) { m7TotalPage = _mTotalPage; }
    public void setm8BookMarkTitle(String _mBookMarkTitle) { m8BookMarkTitle = _mBookMarkTitle; }
    public void setm9BookMarkPage(String _mBookMarkPage) { m9BookMarkPage = _mBookMarkPage; }
    
}
/*
 
1:0:false:_:_:_:_:_:,2:2000:false:_:_:_:_:_:,3:2000:false:_:_:_:_:_:,4:2000:false:_:_:_:_:_:,5:2000:false:_:_:_:_:_:,6:2000:false:_:_:_:_:_:,7:2000:false:_:_:_:_:_:,8:2000:false:_:_:_:_:_:,9:2000:false:_:_:_:_:_:,10:2000:false:_:_:_:_:_:,11:2000:false:_:_:_:_:_:,12:2000:false:_:_:_:_:_:,

*/