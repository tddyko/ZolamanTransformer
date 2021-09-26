package com.strastar.zolaman.classlist;

public class Common {
	public static final boolean DEBUG=false; //false; //true; //false; //true; 
//	public static final String DEF_PREF_BOOKLIST="1:0:false::::::,2:2000:false::::::,3:2000:false::::::,4:2000:false::::::,5:2000:false::::::,6:2000:false::::::,7:2000:false::::::,";
	public static final String CARTOON_NAME = "transformer";
	// host
	public static final int DENSITY_MEDIUM=160;
	public static final String CARTOON_DOWNLOAD_HOST = "http://update3.strastar.com/cartoon/zolaman/"+CARTOON_NAME;
//	public static final String HOST_COMMON_URI = "encode";
//	public static final String HOST_320_480_URI = "encode_320_480";
	public static final String HOST_COMMON_URI = "600x800";
	public static final String HOST_320_480_URI = "320x480";
	
	// contents path
	public static final String CONTENT_SDCARD_PATH = "."+CARTOON_NAME;
	
	public static final int MAX_BOOK_COUNT = 12;
	// Book Price
	public static final String CartoonPrice = "2000";
	
	// preference values;
	public static final String PREF_KEY_IAB_PAIDLIST="pref_key_iab_paidlist";
	public static final String PREF_KEY_BOOKLIST="pref_key_booklist";
	public static final String PREF_KEY_BOOKMARKLIST="pref_key_bookmarklist";
	public static final String PREF_KEY_BOOKREADING_IDX="pref_key_bookreading_idx";
	
	public static final String PREF_CURR_BOOKPAGE="X:X"; // 권:페이지 
	
	// dialog
	public static final int DIALOG_LOADING_ID = 1;
	public static final int DIALOG_PROGRESS_ID = 2;
	public static final int DIALOG_WAITTING_ID = 3;
	public static final int DIALOG_PAYMENT = 4;
	public static final int DIALOG_MAIN_ID = 5;
    
	// 부분유료화
	public static final int BILL_RESULTCODE_SUCCESS = 10000; // 부분유료 결재 성공
	public static final int BILL_REDOWNLOAD_SUCCESS = 15;
	
	// etc
	public static final int MAX_BUFFER_SIZE = 4096; // 2048;
	public static final int DIVISION_SECTION = 6;
}
/*
 
1:0:false::::::,2:2000:false::::::,3:2000:false::::::,4:2000:false::::::,5:2000:false::::::,6:2000:false::::::,7:2000:false::::::,8:2000:false::::::,9:2000:false::::::,10:2000:false::::::,11:2000:false::::::,12:2000:false::::::,

*/