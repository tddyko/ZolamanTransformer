package com.strastar.zolaman.classlist;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class FileDecoder {
    private final String TAG="Z_FileDecoder";
    private final String algorithm = "AES";
    private final String transformation = algorithm + "/ECB/PKCS5Padding";
    private Key key;
    private Bitmap mDeCodeBitmap;
    
	public FileDecoder() {
		String aeskey="s!t&r#a(s)t%r^55";
		Log.d(TAG,"aeskey="+aeskey);
		//SecretKeySpec key = new SecretKeySpec(toBytes(aeskey, 16), algorithm);
		SecretKeySpec key = new SecretKeySpec(aeskey.getBytes(), algorithm);
		this.key = key;
		Log.d(TAG,"[FileDecoder] key="+key);
	}
	
	public Bitmap getDecodeBitmapOptions(File f, BitmapFactory.Options ops){
		InputStream input = null;
		byte[] aa = new byte[1024*1024];
		int i=0;
		try{
			Cipher cipher = Cipher.getInstance(transformation);
			cipher.init(Cipher.DECRYPT_MODE, key);
			
			input = new BufferedInputStream(new FileInputStream(f));
			byte[] buffer = new byte[1024];
			
			int read = -1;
			while ((read = input.read(buffer)) != -1) {
				byte[] vbuffer = cipher.update(buffer, 0, read);
				for(int j=0 ; j<vbuffer.length; j++){
					aa[i] = vbuffer[j];
					i++;
				}
			}
			
			byte[] vbuffer =  cipher.doFinal();
			for(int j=0 ; j<vbuffer.length; j++){
				aa[i] = vbuffer[j];
				i++;
			}
			InputStream is = new ByteArrayInputStream(aa);
			//mDeCodeBitmap = BitmapFactory.decodeStream(is);
			mDeCodeBitmap = BitmapFactory.decodeStream(is, null, ops);
			
		} 
		catch (Exception e) {
			Log.e(TAG,"[getDecodeBitmap]Err="+e.toString());
		}
		finally {
			if (input != null) {
				try { input.close(); } catch(IOException e) { Log.e(TAG,"[getDecodeBitmap]Err2="+e.toString()); }
			}
		}
		return mDeCodeBitmap;
	}
	
	public Bitmap getDecodeBitmap(File f){
		InputStream input = null;
		byte[] aa = new byte[1024*1024];
		int i=0;
		try{
			Cipher cipher = Cipher.getInstance(transformation);
			cipher.init(Cipher.DECRYPT_MODE, key);
			
			input = new BufferedInputStream(new FileInputStream(f));
			byte[] buffer = new byte[1024];
			
			int read = -1;
			while ((read = input.read(buffer)) != -1) {
				byte[] vbuffer = cipher.update(buffer, 0, read);
				for(int j=0 ; j<vbuffer.length; j++){
					aa[i] = vbuffer[j];
					i++;
				}
			}
			
			byte[] vbuffer =  cipher.doFinal();
			for(int j=0 ; j<vbuffer.length; j++){
				aa[i] = vbuffer[j];
				i++;
			}
			InputStream is = new ByteArrayInputStream(aa);
			mDeCodeBitmap = BitmapFactory.decodeStream(is);
			//mDeCodeBitmap = BitmapFactory.decodeStream(is, null, opts);
			
		} 
		catch (Exception e) {
			Log.e(TAG,"[getDecodeBitmap]Err="+e.toString());
		}
		finally {
			if (input != null) {
				try { input.close(); } catch(IOException e) { Log.e(TAG,"[getDecodeBitmap]Err2="+e.toString()); }
			}
		}
		return mDeCodeBitmap;
	}
	
	public void decrypt(File source) throws Exception {
		crypt(Cipher.DECRYPT_MODE, source);
	}
	
	private void crypt(int mode, File source) throws Exception {
		Cipher cipher = Cipher.getInstance(transformation);
		cipher.init(mode, key);
		InputStream input = null;
		byte[] aa = new byte[1024*1024];
		int i=0;
		try {
			input = new BufferedInputStream(new FileInputStream(source));
			byte[] buffer = new byte[1024];
			
			int read = -1;
			while ((read = input.read(buffer)) != -1) {
				byte[] vbuffer = cipher.update(buffer, 0, read);
				for(int j=0 ; j<vbuffer.length; j++){
					aa[i] = vbuffer[j];
					i++;
				}
				
			}
			
			byte[] vbuffer =  cipher.doFinal();
			for(int j=0 ; j<vbuffer.length; j++){
				aa[i] = vbuffer[j];
				i++;
			}
			
//			i=0;
//			while(buffer[i] != -1){
//				System.out.println(aa[i]);
//				i++;
//			}
//			
//			System.out.println(aa);
			InputStream is = new ByteArrayInputStream(aa);
			mDeCodeBitmap = BitmapFactory.decodeStream(is);
		} finally {
			if (input != null) {
				try { input.close(); } catch(IOException ie) {}
			}
		}
	}
}