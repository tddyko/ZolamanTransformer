package com.strastar.zolaman;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import com.android.iabkr.plugin;
import com.android.iabkr.plugin.pluginHandler;
import com.feelingk.iap.IAPLib;
import com.strastar.zolaman.classlist.AddViewFrame;
import com.strastar.zolaman.classlist.CartoonInfo;
import com.strastar.zolaman.classlist.Common;
import com.strastar.zolaman.classlist.PageControl;
import com.strastar.zolaman.classlist.SwipeView;
import com.strastar.zolamantransformer.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainTable extends Activity implements View.OnClickListener {
	private final String TAG = "Z_MainTable";
	private Context mContext = MainTable.this;
	private Handler mHandler = new Handler();
	private SharedPreferences mPrefs;
	protected SwipeView mainSwipeView = null;
	protected PageControl mainPageControl = null;
	private ArrayList<CartoonInfo> mCartoonInfo = new ArrayList<CartoonInfo>();
	private String mNotiMsg = "";
	private ProgressDialog mProgressDialog = null;
	private ProgressThread mProgressThread = null;
	private int mContentLength = 0, mContentPos = 0, mBookIdx = 0;
	private DisplayMetrics mDisplayMetrics;
	private final int Book01 = 0;
	private final int Book02 = 1;
	private final int Book03 = 2;
	private final int Book04 = 3;
	private final int Book05 = 4;
	private final int Book06 = 5;
	private final int Book07 = 6;
	private final int Book08 = 7;
	private final int Book09 = 8;
	private final int Book10 = 9;
	private final int Book11 = 10;
	private final int Book12 = 11;

	// private String mTelcoName=null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// try {
		// ActivityInfo ai =
		// getPackageManager().getActivityInfo(this.getComponentName(),
		// PackageManager.GET_ACTIVITIES|PackageManager.GET_META_DATA);
		// Bundle bundle = ai.metaData;
		// mTelcoName = (String)bundle.get("telconame");
		// } catch (NameNotFoundException e) {
		// e.printStackTrace();
		// }
		mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		StartApplication();
	}

	pluginHandler _pluginHandler = new pluginHandler() {

		@Override
		public void onError(String result) {
			// error on purcahse or query
			Log.d(TAG, "onError=" + result);

			mHandler.post(new Runnable() {
				public void run() {
					try {
						dismissDialog(Common.DIALOG_MAIN_ID);
					} catch (Exception e) {
						Log.e(TAG, "[Common.DIALOG_MAIN_ID] err" + e.toString());
					}
				}
			});

			if (result.indexOf("Timeout") > -1) {
				if (Common.DEBUG)
					Log.d(TAG, "IAP Timeout ! so return.");
				Toast.makeText(mContext, "과금조회중 Timeout이 발생하였습니다.",
						Toast.LENGTH_SHORT).show();
				return;
			}
//			mPrefs.edit()
//					.putString(Common.PREF_KEY_BOOKLIST,
//							CartoonInfo.DEF_PREF_BOOKLIST).commit();
			checkBookList("");
			// initBookList(mPrefs);
		}

		@Override
		public void onPurchase(String PID) {
			// store item id
			Log.d(TAG, "onPurchase=" + PID);
			commandButtonText(mBookIdx, R.drawable.btn_book_download);
			mCartoonInfo.get(mBookIdx).setm2Price(Common.CartoonPrice);
			// Dialog dlg = null;
			AlertDialog.Builder adb1 = new AlertDialog.Builder(mContext);
			adb1.setTitle("알 림");
			adb1.setMessage("구매를 완료하였습니다.\n확인을 클릭하면 만화책 다운로드가 시작됩니다.\n(wi-fi 네트워크를 권장합니다.)");
			adb1.setPositiveButton("확인", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					loadCartoonView(mBookIdx); // 책 X권 .
				}
			});
			adb1.create();
			adb1.show();
		}

		@Override
		public void onQuery(String PIDs) {
			// restore item id
			if (Common.DEBUG)
				Log.d(TAG, "onQuery() pids=" + PIDs);
			// 구매이력 체크.
			checkBookList(PIDs);

			mHandler.post(new Runnable() {
				public void run() {
					try {
						dismissDialog(Common.DIALOG_MAIN_ID);
					} catch (Exception e) {
						Log.e(TAG, "[Common.DIALOG_MAIN_ID] err" + e.toString());
					}
				}
			});

		}
	};

	private void checkBookList(String PIDs) {
		// ArrayList<CartoonInfo> mCartoonInfo = new ArrayList<CartoonInfo>();
		SharedPreferences p = PreferenceManager
				.getDefaultSharedPreferences(mContext);
		String list = p.getString(Common.PREF_KEY_BOOKLIST, "");
		String[] arr = list.split(",");
		mCartoonInfo.clear();
		// 과금내역 받기 전의 과금파일리스트를 로드해서 메모리에 올리는 작업
		for (int i = 0; i < arr.length; i++) {
			if (Common.DEBUG)
				Log.d(TAG, "arr[i]=" + arr[i]);
			String[] a = arr[i].split(":");
			if (Common.DEBUG)
				Log.d(TAG, String.format("a.length=%d", a.length));
			CartoonInfo c = new CartoonInfo();
			c.setm1No(a[0]);
			c.setm2Price(a[1]);
			c.setm3DownloadType(a[2]);
			c.setm4ContentPath(a[3]);
			c.setm5SelectedType(a[4]);
			c.setm6CurrPage(a[5]);
			c.setm7TotalPage(a[6]);
			c.setm8BookMarkTitle(a[7]);
			mCartoonInfo.add(c);
		}

		for (int i = 1; i < arr.length; i++) {
			String pid = String.format("PID%d", i + 1);

			if (PIDs.indexOf(pid) > -1) {
				// 구매한 내역에 있는 경우.
				mCartoonInfo.get(i).setm2Price("2000");
				if (mCartoonInfo.get(i).getm3DownloadType().equals("true")) {
					commandButton(i, View.GONE);
				} else {
					if (mCartoonInfo.get(i).getm2Price()
							.equals(Common.CartoonPrice)) {
						commandButtonText(i, R.drawable.btn_book_download);
					}
				}
			} else {
				// 구매한 내역에 없는 경우.
				// mCartoonInfo.get(i).setm1No(a[0]);
				mCartoonInfo.get(i).setm2Price("_");
				mCartoonInfo.get(i).setm3DownloadType("false");
				mCartoonInfo.get(i).setm4ContentPath("_");
				mCartoonInfo.get(i).setm5SelectedType("_");
				mCartoonInfo.get(i).setm6CurrPage("_");
				mCartoonInfo.get(i).setm7TotalPage("_");
				mCartoonInfo.get(i).setm8BookMarkTitle("_");
				commandButtonText(i, R.drawable.btn_book_buy);
			}
		}

		checkReadingFlag(-1); // 책 읽는중 표시 초기화 체크
		int bookidx = mPrefs.getInt(Common.PREF_KEY_BOOKREADING_IDX, -1);
		if (bookidx > -1) {
			if (!mCartoonInfo.get(bookidx).getm6CurrPage().equals("_")) {
				checkReadingFlag(bookidx);
			}
		}

		String str = "";
		for (int i = 0; i < mCartoonInfo.size(); i++) {
			CartoonInfo c = mCartoonInfo.get(i);
			str += String.format("%s:%s:%s:%s:%s:%s:%s:%s:%s:,", c.getm1No(),
					c.getm2Price(), c.getm3DownloadType(),
					c.getm4ContentPath(), c.getm5SelectedType(),
					c.getm6CurrPage(), c.getm7TotalPage(),
					c.getm8BookMarkTitle(), c.getm9BookMarkPage());
		}
		if (Common.DEBUG)
			Log.d(TAG, "Pref=" + str);
		p.edit().putString(Common.PREF_KEY_BOOKLIST, str).commit();
	}

	private void StartApplication() {
		setContentView(R.layout.main);
		if (Common.DEBUG)
			Log.d(TAG, "onCreate invoked");
		init();
		addhook();

		mHandler.post(new Runnable() {
			@Override
			public void run() {
				try {
					showDialog(Common.DIALOG_MAIN_ID);
				} catch (Exception e) {
					Log.e(TAG, "[StartApplication] err" + e.toString());
				}
			}
		});

		plugin.Init(mContext, "AID", _pluginHandler);
		if (Common.DEBUG)
			Log.d(TAG, "plugin.Init()");
		plugin.Query(null, 10000);
		if (Common.DEBUG)
			Log.d(TAG, "plugin.Query(null, 10000)");
	}

	private void initBookList(SharedPreferences p) {
		String list = p.getString(Common.PREF_KEY_BOOKLIST, "");
		if (list.equals("")) {
			p.edit()
					.putString(Common.PREF_KEY_BOOKLIST,
							CartoonInfo.DEF_PREF_BOOKLIST).commit();
			list = CartoonInfo.DEF_PREF_BOOKLIST;
		}
		if (Common.DEBUG)
			Log.d(TAG, "[initBookList] list=" + list);
		mCartoonInfo.clear();
		// 1:0:false::::::,2::false::::::,
		String[] arr = list.split(",");
		for (int i = 0; i < arr.length; i++) {
			if (Common.DEBUG)
				Log.d(TAG, "arr[i]=" + arr[i]);
			String[] a = arr[i].split(":");
			if (Common.DEBUG)
				Log.d(TAG, String.format("a.length=%d", a.length));
			CartoonInfo c = new CartoonInfo();
			c.setm1No(a[0]);
			c.setm2Price(a[1]);
			c.setm3DownloadType(a[2]);
			c.setm4ContentPath(a[3]);
			c.setm5SelectedType(a[4]);
			c.setm6CurrPage(a[5]);
			// if (!a[5].equals("_")) {
			// checkReadingFlag(i);
			// }
			c.setm7TotalPage(a[6]);
			c.setm8BookMarkTitle(a[7]);
			mCartoonInfo.add(c);

			if (c.getm3DownloadType().equals("true")) {
				commandButton(i, View.GONE);
			} else {
				if (c.getm2Price().equals(Common.CartoonPrice)) {
					commandButtonText(i, R.drawable.btn_book_download);
				}
			}
		}
	}

	private void commandButtonText(final int idx, final int rlt) {
		// TextView btn = (TextView)findViewById(R.id.btn_comm1);
		// btn.setBackgroundResource(resid);
		mHandler.post(new Runnable() {
			public void run() {
				switch (idx) {
				case Book01:
					((TextView) findViewById(R.id.btn_comm1))
							.setBackgroundResource(rlt);
					break;
				case Book02:
					((TextView) findViewById(R.id.btn_comm2))
							.setBackgroundResource(rlt);
					break;
				case Book03:
					((TextView) findViewById(R.id.btn_comm3))
							.setBackgroundResource(rlt);
					break;
				case Book04:
					((TextView) findViewById(R.id.btn_comm4))
							.setBackgroundResource(rlt);
					break;
				case Book05:
					((TextView) findViewById(R.id.btn_comm5))
							.setBackgroundResource(rlt);
					break;
				case Book06:
					((TextView) findViewById(R.id.btn_comm6))
							.setBackgroundResource(rlt);
					break;
				case Book07:
					((TextView) findViewById(R.id.btn_comm7))
							.setBackgroundResource(rlt);
					break;
				case Book08:
					((TextView) findViewById(R.id.btn_comm8))
							.setBackgroundResource(rlt);
					break;
				case Book09:
					((TextView) findViewById(R.id.btn_comm9))
							.setBackgroundResource(rlt);
					break;
				case Book10:
					((TextView) findViewById(R.id.btn_comm10))
							.setBackgroundResource(rlt);
					break;
				case Book11:
					((TextView) findViewById(R.id.btn_comm11))
							.setBackgroundResource(rlt);
					break;
				case Book12:
					((TextView) findViewById(R.id.btn_comm12))
							.setBackgroundResource(rlt);
					break;
				}
			}
		});
	}

	private void commandButton(final int idx, final int rlt) {
		mHandler.post(new Runnable() {
			public void run() {
				switch (idx) {
				case Book01:
					((TextView) findViewById(R.id.btn_comm1))
							.setVisibility(rlt);
					break;
				case Book02:
					((TextView) findViewById(R.id.btn_comm2))
							.setVisibility(rlt);
					break;
				case Book03:
					((TextView) findViewById(R.id.btn_comm3))
							.setVisibility(rlt);
					break;
				case Book04:
					((TextView) findViewById(R.id.btn_comm4))
							.setVisibility(rlt);
					break;
				case Book05:
					((TextView) findViewById(R.id.btn_comm5))
							.setVisibility(rlt);
					break;
				case Book06:
					((TextView) findViewById(R.id.btn_comm6))
							.setVisibility(rlt);
					break;
				case Book07:
					((TextView) findViewById(R.id.btn_comm7))
							.setVisibility(rlt);
					break;
				case Book08:
					((TextView) findViewById(R.id.btn_comm8))
							.setVisibility(rlt);
					break;
				case Book09:
					((TextView) findViewById(R.id.btn_comm9))
							.setVisibility(rlt);
					break;
				case Book10:
					((TextView) findViewById(R.id.btn_comm10))
							.setVisibility(rlt);
					break;
				case Book11:
					((TextView) findViewById(R.id.btn_comm11))
							.setVisibility(rlt);
					break;
				case Book12:
					((TextView) findViewById(R.id.btn_comm12))
							.setVisibility(rlt);
					break;
				}
			}
		});
	}

	private void init() {

		// 해상도 구하기..
		mDisplayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
		//
		mainSwipeView = (SwipeView) findViewById(R.id.mainSwipeView);
		AddViewFrame addViewFrame = null;
		addViewFrame = new AddViewFrame(mContext, R.layout.add_chapter1);
		mainSwipeView.addView(addViewFrame);
		addViewFrame = new AddViewFrame(mContext, R.layout.add_chapter2);
		mainSwipeView.addView(addViewFrame);
		addViewFrame = new AddViewFrame(mContext, R.layout.add_chapter3);
		mainSwipeView.addView(addViewFrame);
		if (Common.DEBUG)
			Log.d(TAG, "mainSwipeView has [" + mainSwipeView.getPageCount()
					+ "] pages");
		mainPageControl = (PageControl) findViewById(R.id.mainPageControl);

		mainSwipeView.setPageControl(mainPageControl);
		mainSwipeView
				.setOnPageChangedListener(new SwipeView.OnPageChangedListener() {
					@Override
					public void onPageChanged(int oldPage, int newPage) {
						if (Common.DEBUG)
							Log.d(TAG, "Page changed From:" + oldPage
									+ " Into:" + newPage);
					}
				});
		mainSwipeView.scrollToPage(0);

		checkReadingFlag(-1);
		initBookList(mPrefs);
	}

	private void addhook() {
		//
		LinearLayout f = (LinearLayout) findViewById(R.id.ll_book1);
		f.setOnClickListener(this);
		f = (LinearLayout) findViewById(R.id.ll_book2);
		f.setOnClickListener(this);
		f = (LinearLayout) findViewById(R.id.ll_book3);
		f.setOnClickListener(this);
		f = (LinearLayout) findViewById(R.id.ll_book4);
		f.setOnClickListener(this);
		f = (LinearLayout) findViewById(R.id.ll_book5);
		f.setOnClickListener(this);
		f = (LinearLayout) findViewById(R.id.ll_book6);
		f.setOnClickListener(this);
		f = (LinearLayout) findViewById(R.id.ll_book7);
		f.setOnClickListener(this);
		f = (LinearLayout) findViewById(R.id.ll_book8);
		f.setOnClickListener(this);
		f = (LinearLayout) findViewById(R.id.ll_book9);
		f.setOnClickListener(this);
		f = (LinearLayout) findViewById(R.id.ll_book10);
		f.setOnClickListener(this);
		f = (LinearLayout) findViewById(R.id.ll_book11);
		f.setOnClickListener(this);
		f = (LinearLayout) findViewById(R.id.ll_book12);
		f.setOnClickListener(this);
	}

	private Thread requestThreadCartoonToServer(final int idx) {
		return new Thread() {
			public void run() {
				// 초기화..
				mContentPos = 0;
				mContentLength = 0;
				mHandler.post(new Runnable() {
					public void run() {
						mNotiMsg = String.format("%s %s권 다운로드중입니다.",
								getString(R.string.app_name),
								mCartoonInfo.get(idx).getm1No());
						// showDialog(Common.DIALOG_PROGRESS_ID);
						showProgressDialog();
					}
				});

				try {
					// String file = String.format("gunz%02denc.zip",
					// Integer.parseInt(mCartoonInfo.get(idx).getm1No()));
					String file = String.format("%02d_enc.zip",
							Integer.parseInt(mCartoonInfo.get(idx).getm1No()));

					URL url = null;
					String rootpath = mContext.getExternalFilesDir(
							Environment.DIRECTORY_DOWNLOADS).getPath();

					if (Common.DEBUG)
						Log.d(TAG, "rootpath=" + rootpath);

					String SDCardPath = "";
					if (mDisplayMetrics.densityDpi == Common.DENSITY_MEDIUM) {
						url = new URL(String.format("%s/%s/%s",
								Common.CARTOON_DOWNLOAD_HOST,
								Common.HOST_320_480_URI, file));
						SDCardPath = String.format("%s/%s/%s", rootpath,
								Common.CONTENT_SDCARD_PATH,
								Common.HOST_320_480_URI);
					} else {
						url = new URL(String.format("%s/%s/%s",
								Common.CARTOON_DOWNLOAD_HOST,
								Common.HOST_COMMON_URI, file));
						SDCardPath = String.format("%s/%s/%s", rootpath,
								Common.CONTENT_SDCARD_PATH,
								Common.HOST_COMMON_URI);
					}

					if (Common.DEBUG)
						Log.d(TAG, "url=" + url.toString());
					HttpURLConnection urlConnection = (HttpURLConnection) url
							.openConnection();
					// set up some things on the connection
					urlConnection.setRequestMethod("GET");
					urlConnection.setDoOutput(true);
					urlConnection.connect();
					if (Common.DEBUG)
						Log.d(TAG, String.format("response code(%d) msg(%s)",
								urlConnection.getResponseCode(),
								urlConnection.getResponseMessage()));

					File d = new File(SDCardPath);
					d.mkdirs();
					File fd = new File(d, file);

					FileOutputStream fileOutput = new FileOutputStream(fd);
					InputStream inputStream = urlConnection.getInputStream();
					mContentLength = urlConnection.getContentLength();
					if (Common.DEBUG)
						Log.d(TAG, String.format("path=%s size=%d", SDCardPath,
								mContentLength));
					byte[] buffer = new byte[Common.MAX_BUFFER_SIZE];
					int bufferLength = 0, i = 0;
					while ((bufferLength = inputStream.read(buffer)) > 0) {
						fileOutput.write(buffer, 0, bufferLength);
						// if(Common.DEBUG)
						// Log.d(TAG,"bufferLength="+bufferLength);
						mContentPos += bufferLength;
						i++;
						if ((i % 10) == 0) {
							try {
								sleep(100);
								// if(Common.DEBUG) Log.d(TAG,
								// "inputStream.read sleep(100) i="+i);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
					fileOutput.close();

					if (unzipCartoon(idx, SDCardPath,
							String.format("%s/%s", SDCardPath, file))) {
						commandButton(idx, View.GONE);
						mCartoonInfo.get(idx).setm3DownloadType("true");
					}

				} catch (MalformedURLException e) {
					Log.e(TAG, "MalformedURLException:" + e.toString());
					mHandler.post(new Runnable() {
						public void run() {
							if (mProgressDialog != null) {
								mProgressDialog.dismiss();
							}
							if (mProgressThread != null) {
								mProgressThread
										.setState(ProgressThread.STATE_DONE);
							}
							Toast.makeText(
									mContext,
									"[MalformedURLException]네트워크 장애 !\n잠시후 다시시도해 보시기 바랍니다.",
									Toast.LENGTH_SHORT).show();
						}
					});
				} catch (IOException e) {
					Log.e(TAG, "IOException:" + e.toString());
					mHandler.post(new Runnable() {
						public void run() {
							if (mProgressDialog != null) {
								mProgressDialog.dismiss();
							}
							if (mProgressThread != null) {
								if (Common.DEBUG)
									Log.d(TAG,
											"setState(ProgressThread.STATE_DONE)");
								mProgressThread
										.setState(ProgressThread.STATE_DONE);
							} else {
								if (Common.DEBUG)
									Log.d(TAG, "setState() skip");
							}
							// Toast.makeText(mContext,
							// "[IOException]네트워크 장애 !\n잠시후 다시시도해 보시기 바랍니다.",
							// Toast.LENGTH_SHORT).show();
							try {
								AlertDialog.Builder builder = new AlertDialog.Builder(
										mContext);
								builder.setMessage(
										R.string.msg_network_error_finish)
										.setTitle(R.string.app_name)
										.setCancelable(false)
										.setPositiveButton(
												"OK",
												new DialogInterface.OnClickListener() {
													public void onClick(
															DialogInterface dialog,
															int which) {
														((Activity) mContext)
																.finish();
													}
												}).show();
							} catch (Exception e) {
								Log.d(TAG,
										"[requestThreadCartoonToServer] AlertDialog.show() err="
												+ e.toString());
							}
						}
					});
				}
			}
		};
	}

	private boolean unzipCartoon(int idx, String dest_path, String zipfile) {
		boolean ret = false;
		try {
			mHandler.post(new Runnable() {
				public void run() {
					try {
						showDialog(Common.DIALOG_WAITTING_ID);
					} catch (Exception e) {
						Log.e(TAG, "[unzipCartoon] err" + e.toString());
					}
				}
			});

			FileInputStream fis = new FileInputStream(zipfile);
			ZipInputStream zis = new ZipInputStream(
					new BufferedInputStream(fis));
			ZipEntry ze;

			while ((ze = zis.getNextEntry()) != null) {
				String path = String.format("%s/%s", dest_path, ze.getName());
				if (Common.DEBUG)
					Log.d(TAG, "ze.getName()=" + path);
				File f = new File(path);
				if (!f.exists() && ze.getName().indexOf(".") == -1) {
					if (Common.DEBUG)
						Log.d(TAG, "mkdir=" + path);
					f.mkdirs();
					mCartoonInfo.get(idx).setm4ContentPath(path);
					continue;
				} else if (f.exists() && f.isDirectory()) {
					if (Common.DEBUG)
						Log.d(TAG, "isDirectory=" + path);
					mCartoonInfo.get(idx).setm4ContentPath(path);
					continue;
				}

				FileOutputStream fos = new FileOutputStream(path);
				BufferedInputStream in = new BufferedInputStream(zis);
				BufferedOutputStream out = new BufferedOutputStream(fos);

				byte b[] = new byte[8 * 1024];
				int n;
				while ((n = in.read(b, 0, (8 * 1024))) >= 0) {
					out.write(b, 0, n);
				}
				out.flush();
				out.close();
			}
			zis.close();
			fis.close();
			ret = true;
		} catch (IOException e) {
			Log.e(TAG, "IOException:" + e.toString());
		} finally {
			mHandler.post(new Runnable() {
				public void run() {
					try {
						dismissDialog(Common.DIALOG_WAITTING_ID);
					} catch (Exception e) {
						Log.e(TAG,
								"[unzipCartoon] dismissDialog()s err"
										+ e.toString());
					}
				}
			});
		}

		return ret;
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (Common.DEBUG)
			Log.d(TAG, String.format(
					"[onActivityResult] requestcode(%d) resultCode(%d)",
					requestCode, resultCode));
		if (requestCode == R.id.rl_cartoonview) {
			if (resultCode == RESULT_OK) {
				String book_page = data.getStringExtra("book_page");
				Integer book_idx = Integer.parseInt(data
						.getStringExtra("book_idx"));
				if (Common.DEBUG)
					Log.d(TAG, String.format("book_idx(%d) book_page(%s)",
							book_idx, book_page));
				mCartoonInfo.get(book_idx).setm6CurrPage(book_page);
				checkReadingFlag(book_idx); // 책 읽는중 표시하는 메서드
				mPrefs.edit().putInt(Common.PREF_KEY_BOOKREADING_IDX, book_idx)
						.commit();
			}
		}
		// else if(requestCode==R.id.rl_sktarmlauncher){
		// if(resultCode == RESULT_OK){
		// int type = data.getIntExtra("errtype", 0);
		// int ret = data.getIntExtra("errcode", 0);
		// if(type==Common.BILL_RESULTCODE_SUCCESS &&
		// ret==Common.BILL_RESULTCODE_SUCCESS){ // 최초구입 상품.
		// showDialog(Common.DIALOG_PAYMENT);
		// }
		// else if(type==IAPLib.HND_ERR_ITEMQUERY &&
		// ret==Common.BILL_REDOWNLOAD_SUCCESS){ // 이전에 이미 구매한 상품인 경우.
		// mBookIdx = data.getIntExtra("book_idx", 0);
		// mCartoonInfo.get(mBookIdx).setm2Price(Common.CartoonPrice);
		// commandButtonText(mBookIdx, R.drawable.btn_book_download);
		// // 이미 아이템 구매한 경우 처리부분..
		// AlertDialog.Builder adb1 = new AlertDialog.Builder(mContext);
		// adb1.setIcon(R.drawable.icon);
		// adb1.setTitle("알 림");
		// adb1.setMessage("이미 구매를 완료하였습니다.\n확인을 클릭하면 만화책 다운로드가 시작됩니다.\n(wi-fi 네트워크를 권장합니다.)");
		// adb1.setPositiveButton("확인", new DialogInterface.OnClickListener() {
		// public void onClick(DialogInterface dialog, int which) {
		// dialog.dismiss();
		// mCartoonInfo.get(mBookIdx).setm2Price(Common.CartoonPrice);
		// loadCartoonView(mBookIdx); // 책 X권 .
		// }
		// });
		// adb1.show();
		// }
		// }
		// }
	}

	private void checkReadingFlag(int idx) {
		// LinearLayout l = (LinearLayout)findViewById(R.id.ll_read1);
		// l.setVisibility(View.GONE);
		((LinearLayout) findViewById(R.id.ll_read1)).setVisibility(View.GONE);
		((LinearLayout) findViewById(R.id.ll_read2)).setVisibility(View.GONE);
		((LinearLayout) findViewById(R.id.ll_read3)).setVisibility(View.GONE);
		((LinearLayout) findViewById(R.id.ll_read4)).setVisibility(View.GONE);
		((LinearLayout) findViewById(R.id.ll_read5)).setVisibility(View.GONE);
		((LinearLayout) findViewById(R.id.ll_read6)).setVisibility(View.GONE);
		((LinearLayout) findViewById(R.id.ll_read7)).setVisibility(View.GONE);
		((LinearLayout) findViewById(R.id.ll_read8)).setVisibility(View.GONE);
		((LinearLayout) findViewById(R.id.ll_read9)).setVisibility(View.GONE);
		((LinearLayout) findViewById(R.id.ll_read10)).setVisibility(View.GONE);
		((LinearLayout) findViewById(R.id.ll_read11)).setVisibility(View.GONE);
		((LinearLayout) findViewById(R.id.ll_read12)).setVisibility(View.GONE);

		if (idx == 0) {
			((LinearLayout) findViewById(R.id.ll_read1))
					.setVisibility(View.VISIBLE);
		} else if (idx == 1) {
			((LinearLayout) findViewById(R.id.ll_read2))
					.setVisibility(View.VISIBLE);
		} else if (idx == 2) {
			((LinearLayout) findViewById(R.id.ll_read3))
					.setVisibility(View.VISIBLE);
		} else if (idx == 3) {
			((LinearLayout) findViewById(R.id.ll_read4))
					.setVisibility(View.VISIBLE);
		} else if (idx == 4) {
			((LinearLayout) findViewById(R.id.ll_read5))
					.setVisibility(View.VISIBLE);
		} else if (idx == 5) {
			((LinearLayout) findViewById(R.id.ll_read6))
					.setVisibility(View.VISIBLE);
		} else if (idx == 6) {
			((LinearLayout) findViewById(R.id.ll_read7))
					.setVisibility(View.VISIBLE);
		} else if (idx == 7) {
			((LinearLayout) findViewById(R.id.ll_read8))
					.setVisibility(View.VISIBLE);
		} else if (idx == 8) {
			((LinearLayout) findViewById(R.id.ll_read9))
					.setVisibility(View.VISIBLE);
		} else if (idx == 9) {
			((LinearLayout) findViewById(R.id.ll_read10))
					.setVisibility(View.VISIBLE);
		} else if (idx == 10) {
			((LinearLayout) findViewById(R.id.ll_read11))
					.setVisibility(View.VISIBLE);
		} else if (idx == 11) {
			((LinearLayout) findViewById(R.id.ll_read12))
					.setVisibility(View.VISIBLE);
		} else if (idx == 12) {
			((LinearLayout) findViewById(R.id.ll_read2))
					.setVisibility(View.VISIBLE);
		}

	}

	private void loadCartoonView(int book_idx) {
		if (mCartoonInfo.get(book_idx).getm3DownloadType().equals("true")) {
			// goto CartoonView
			// Intent i = new Intent(mContext, CartoonView.class);
			Intent i = new Intent(mContext, CartoonViewFlipper.class);
			i.putExtra("display", mDisplayMetrics.densityDpi);
			i.putExtra("book_idx", Integer.toString(book_idx)); // 책
			if (mCartoonInfo.get(book_idx).getm6CurrPage().equals("_")
					|| mCartoonInfo.get(book_idx).getm6CurrPage()
							.equals("null")) {
				i.putExtra("book_page", "0"); // 책페이지..
			} else
				i.putExtra("book_page", mCartoonInfo.get(book_idx)
						.getm6CurrPage()); // 책페이지..
			i.putExtra("path", mCartoonInfo.get(book_idx).getm4ContentPath());
			startActivityForResult(i, R.id.rl_cartoonview);
		} else { // 만화책 다운받기..
			if (Common.DEBUG)
				Log.d(TAG, "onClick= Cartoon Case=" + book_idx);
			gotoCartoonView(book_idx);
		}
	}

	private void gotoCartoonView(int idx) {
		// if(mCartoonInfo.get(idx).getm2Price().equals("0") &&
		// mCartoonInfo.get(idx).getm3DownloadType().equals("false")){
		if (mCartoonInfo.get(idx).getm3DownloadType().equals("false")) {
			requestThreadCartoonToServer(idx).start();
		} else {
			if (Common.DEBUG)
				Log.d(TAG, "gotoCartoonView idx=" + idx);
			// goto CartoonReader ..
		}
	}

	@Override
	public void onClick(View view) {
		if (Common.DEBUG)
			Log.d(TAG, "onClick=" + view.getId());
		switch (view.getId()) {
		case R.id.ll_book1:
			mBookIdx = Book01; // 책 X권 ..
			loadCartoonView(mBookIdx); // 책 X권 .
			break;
		case R.id.ll_book2:
			mBookIdx = Book02; // 책 X권 ..
			if (!mCartoonInfo.get(mBookIdx).getm2Price()
					.equals(Common.CartoonPrice)) {
				gotoPaymentProcess(mBookIdx);
			} else {
				loadCartoonView(mBookIdx); // 책 X권 .
			}
			break;
		case R.id.ll_book3:
			mBookIdx = Book03; // 책 X권 ..
			if (!mCartoonInfo.get(mBookIdx).getm2Price()
					.equals(Common.CartoonPrice)) {
				gotoPaymentProcess(mBookIdx);
			} else {
				loadCartoonView(mBookIdx); // 책 X권 .
			}
			break;
		case R.id.ll_book4:
			mBookIdx = Book04; // 책 X권 ..
			if (!mCartoonInfo.get(mBookIdx).getm2Price()
					.equals(Common.CartoonPrice)) {
				gotoPaymentProcess(mBookIdx);
			} else {
				loadCartoonView(mBookIdx); // 책 X권 .
			}
			break;
		case R.id.ll_book5:
			mBookIdx = Book05; // 책 X권 ..
			if (!mCartoonInfo.get(mBookIdx).getm2Price()
					.equals(Common.CartoonPrice)) {
				gotoPaymentProcess(mBookIdx);
			} else {
				loadCartoonView(mBookIdx); // 책 X권 .
			}
			break;
		case R.id.ll_book6:
			mBookIdx = Book06; // 책 X권 ..
			if (!mCartoonInfo.get(mBookIdx).getm2Price()
					.equals(Common.CartoonPrice)) {
				gotoPaymentProcess(mBookIdx);
			} else {
				loadCartoonView(mBookIdx); // 책 X권 .
			}
			break;
		case R.id.ll_book7:
			mBookIdx = Book07; // 책 X권 ..
			if (!mCartoonInfo.get(mBookIdx).getm2Price()
					.equals(Common.CartoonPrice)) {
				gotoPaymentProcess(mBookIdx);
			} else {
				loadCartoonView(mBookIdx); // 책 X권 .
			}
			break;
		case R.id.ll_book8:
			mBookIdx = Book08; // 책 X권 ..
			if (!mCartoonInfo.get(mBookIdx).getm2Price()
					.equals(Common.CartoonPrice)) {
				gotoPaymentProcess(mBookIdx);
			} else {
				loadCartoonView(mBookIdx); // 책 X권 .
			}
			break;
		case R.id.ll_book9:
			mBookIdx = Book09; // 책 X권 ..
			if (!mCartoonInfo.get(mBookIdx).getm2Price()
					.equals(Common.CartoonPrice)) {
				gotoPaymentProcess(mBookIdx);
			} else {
				loadCartoonView(mBookIdx); // 책 X권 .
			}
			break;
		case R.id.ll_book10:
			mBookIdx = Book10; // 책 X권 ..
			if (!mCartoonInfo.get(mBookIdx).getm2Price()
					.equals(Common.CartoonPrice)) {
				gotoPaymentProcess(mBookIdx);
			} else {
				loadCartoonView(mBookIdx); // 책 X권 .
			}
			break;
		case R.id.ll_book11:
			mBookIdx = Book11; // 책 X권 ..
			if (!mCartoonInfo.get(mBookIdx).getm2Price()
					.equals(Common.CartoonPrice)) {
				gotoPaymentProcess(mBookIdx);
			} else {
				loadCartoonView(mBookIdx); // 책 X권 .
			}
			break;
		case R.id.ll_book12:
			mBookIdx = Book12; // 책 X권 ..
			if (!mCartoonInfo.get(mBookIdx).getm2Price()
					.equals(Common.CartoonPrice)) {
				gotoPaymentProcess(mBookIdx);
			} else {
				loadCartoonView(mBookIdx); // 책 X권 .
			}
			break;
		}
	}

	private void gotoPaymentProcess(final int book_idx) {
		// 방패 아이템 구매는 3,000원이 결제됩니다. 결제하시겠습니까? (확인 / 취소)
		plugin.Init(mContext, "AID", _pluginHandler);
		String pid = String.format("PID%d", book_idx + 1);
		plugin.Purchase(getString(R.string.purchase_title), String.format(
				"%s %d권\n구매는 2,000원이 결제됩니다.\n결제하시겠습니까?",
				getString(R.string.app_name), book_idx + 1),
				getString(R.string.purchase_yes),
				getString(R.string.purchase_cancel), pid);
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (Common.DEBUG)
			Log.d(TAG, "onStart() invoked");

	}

	@Override
	protected void onResume() {
		super.onResume();
		if (Common.DEBUG)
			Log.d(TAG, "onResume() invoked");

	}

	@Override
	protected void onPause() {
		super.onPause();
		if (Common.DEBUG)
			Log.d(TAG, "onPause() invoked");
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (Common.DEBUG)
			Log.d(TAG, "onStop() invoked");
		savePrefCartoonInfo();
	}

	private void savePrefCartoonInfo() {
		String str = "";
		for (int i = 0; i < mCartoonInfo.size(); i++) {
			CartoonInfo c = mCartoonInfo.get(i);
			str += String.format("%s:%s:%s:%s:%s:%s:%s:%s:%s:,", c.getm1No(),
					c.getm2Price(), c.getm3DownloadType(),
					c.getm4ContentPath(), c.getm5SelectedType(),
					c.getm6CurrPage(), c.getm7TotalPage(),
					c.getm8BookMarkTitle(), c.getm9BookMarkPage());
		}
		if (Common.DEBUG)
			Log.d(TAG, "Pref=" + str);
		// String list = p.getString(Common.PREF_KEY_BOOKLIST, "");
		// if(list.equals("")){
		// p.edit().putString(Common.PREF_KEY_BOOKLIST,
		// CartoonInfo.DEF_PREF_BOOKLIST);
		// list = CartoonInfo.DEF_PREF_BOOKLIST;
		// }
		mPrefs.edit().putString(Common.PREF_KEY_BOOKLIST, str).commit();
	}

	private TextView mTv_Percent;

	protected Dialog onCreateDialog(int id) {
		// Dialog dialog;
		switch (id) {

		case Common.DIALOG_PROGRESS_ID:
			if (Common.DEBUG)
				Log.d(TAG, "showDialog[DIALOG_PROGRESS_ID] mNotiMsg="
						+ mNotiMsg);
			mProgressDialog = null;
			mProgressDialog = new ProgressDialog(mContext);
			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			mProgressDialog.setMessage(mNotiMsg);
			mProgressDialog.setCancelable(true);
			mProgressDialog.setProgress(0);
			mProgressDialog.setMax(100);
			if (mProgressThread == null) {
				mProgressThread = new ProgressThread(handler);
				mProgressThread.start();
			} else {
				// mProgressThread.setState(ProgressThread.STATE_RUNNING);
				if (Common.DEBUG)
					Log.d(TAG, "mProgressThread exist so start()");
				mProgressThread.start();
			}
			// mProgressDialog.show();
			break;
		case Common.DIALOG_MAIN_ID:
			mProgressDialog = ProgressDialog.show(mContext, "",
					"정보 불러오는중\n잠시기다려 주세요..", true);
			mProgressDialog.setCancelable(false);
			break;
			
		case Common.DIALOG_WAITTING_ID:
			mProgressDialog = ProgressDialog.show(mContext, "",
					"압축해제중\n잠시기다려 주세요..", true);
			mProgressDialog.setCancelable(true);
			break;

		case Common.DIALOG_PAYMENT:
			commandButtonText(mBookIdx, R.drawable.btn_book_download);
			// Dialog dlg = null;
			AlertDialog.Builder adb1 = new AlertDialog.Builder(this);
			adb1.setTitle("알 림");
			adb1.setMessage("구매를 완료하였습니다.\n확인을 클릭하면 만화책 다운로드가 시작됩니다.\n(wi-fi 네트워크를 권장합니다.)");
			adb1.setPositiveButton("확인", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					mCartoonInfo.get(mBookIdx).setm2Price(Common.CartoonPrice);
					loadCartoonView(mBookIdx); // 책 X권 .
				}
			});
			return adb1.create();
		default:
			mProgressDialog = null;
		}
		return mProgressDialog;
	}

	private void showProgressDialog() {
		mProgressDialog = null;
		mProgressDialog = new ProgressDialog(mContext);
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		mProgressDialog.setMessage(mNotiMsg);
		mProgressDialog.setCancelable(true);
		mProgressDialog.setProgress(0);
		mProgressDialog.setMax(100);
		mProgressDialog.show();
		if (mProgressThread == null) {
			mProgressThread = new ProgressThread(handler);
			mProgressThread.start();
			if (Common.DEBUG)
				Log.d(TAG,
						"showProgressDialog : mProgressThread Create & Start");
		} else {
			mProgressThread.setState(ProgressThread.STATE_RUNNING);
			// mProgressThread.resume();
			if (Common.DEBUG)
				Log.d(TAG, "showProgressDialog : mProgressThread ReStart");
		}
	}

	public static String CommaAddToString(int v) {
		NumberFormat nf = NumberFormat.getInstance();
		String RetVal = "";
		try {
			RetVal = nf.format(v);
		} catch (Exception e) {
			RetVal = "0";
			// TODO: handle exception
		}
		return RetVal;
	}

	final Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			float currpos = msg.getData().getInt("currpos");
			float totalpos = msg.getData().getInt("totalpos");
			// if(Common.DEBUG)
			// Log.d(TAG,String.format("[handler]currpos=%d totalpos=%d",msg.getData().getInt("currpos"),msg.getData().getInt("totalpos")));
			if (currpos == 0 || totalpos == 0) {
				if (Common.DEBUG)
					Log.d(TAG, String.format(
							"handleMessage currpos=%f totalpos=%f", currpos,
							totalpos));
				return;
			}
			float rate = (currpos / totalpos) * 100;
			int val = Math.round(rate);
			if (Common.DEBUG)
				Log.d(TAG, String.format("curr(%f) total(%f) rate(%f) val(%d)",
						currpos, totalpos, rate, val));
			mProgressDialog.setProgress(val);
			mProgressDialog.setMessage(String.format(
					"%s\n\n(%s byte / %s byte)", mNotiMsg,
					CommaAddToString((int) currpos),
					CommaAddToString((int) totalpos)));
			// mTv_Percent.setText(String.format("%d %s",val,"%"));

			if (val >= 100) {
				mProgressDialog.dismiss();
				// dismissDialog(Common.DIALOG_PROGRESS_ID);
				if (Common.DEBUG)
					Log.d(TAG, "handleMessage dismissDialog : val=" + val);
				mProgressThread.setState(ProgressThread.STATE_DONE);
				// if(mProgressThread!=null){
				// mProgressThread=null;
				// }
			}
		}
	};

	private class ProgressThread extends Thread {
		Handler mThreadHandler;
		final static boolean THREAD_RUNNING = true;
		final static boolean THREAD_STOPPED = false;
		final static int STATE_DONE = 0;
		final static int STATE_RUNNING = 1;
		int mState;
		boolean mThreadRunning;

		public ProgressThread(Handler h) {
			mThreadHandler = h;
			mThreadRunning = THREAD_RUNNING;
			mState = STATE_RUNNING;
		}

		public void run() {

			while (mThreadRunning) {
				try {
					Thread.sleep(1000);
					if (Common.DEBUG)
						Log.d(TAG, "sleep(1000) mState=" + mState);
					while (mState == STATE_RUNNING) {
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							Log.e(TAG, "[InterruptedException1]" + e.toString());
						}
						Message msg = mThreadHandler.obtainMessage();
						Bundle b = new Bundle();
						b.putInt("currpos", mContentPos);
						b.putInt("totalpos", mContentLength);
						// if(Common.DEBUG)
						// Log.d(TAG,String.format("[ProgressThread]currpos=%d totalpos=%d",mContentPos,mContentLength));

						msg.setData(b);
						mThreadHandler.sendMessage(msg);
					}
				} catch (InterruptedException e) {
					Log.e(TAG, "[InterruptedException2]" + e.toString());
				}
			}
			if (Common.DEBUG)
				Log.d(TAG, "Thread Stopped! mThreadRunning=" + mThreadRunning);
		}

		public void setState(int state) {
			mState = state;
		}

		public void setThreadRunning(boolean state) {
			mThreadRunning = state;
		}
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (Common.DEBUG)
			Log.d(TAG, "[onKeyDown] " + keyCode);
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			// *** DO ACTION HERE ***
			if (Common.DEBUG)
				Log.d(TAG, "onKeyDown : KEYCODE_BACK");
			if (mProgressThread != null) {
				mProgressThread.setState(ProgressThread.STATE_DONE);
				mProgressThread.setThreadRunning(ProgressThread.THREAD_STOPPED);
				if (Common.DEBUG)
					Log.d(TAG,
							"mProgressThread.setThreadRunning=THREAD_STOPPED");
			}
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (Common.DEBUG)
			Log.d(TAG, "onDestroy() invoked");
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		if (Common.DEBUG)
			Log.d(TAG, "onRestoreInstanceState(Bundle) invoked");
	}

	@Override
	protected void onSaveInstanceState(Bundle b) {
		super.onSaveInstanceState(b);
		if (Common.DEBUG)
			Log.d(TAG, "onSaveInstanceState(Bundle) invoked");
	}
}