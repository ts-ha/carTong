package com.neighbor.ex.tong.common;

import java.util.List;
import java.util.regex.Pattern;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public class AppUtil {

	/**
	 * @param context
	 *            Context ��ü
	 * @param serviceName
	 *            ��Ű�� �̸��� ���񽺸�
	 * @return �������̸� true, �������� �ƴϸ� false
	 */
	public static boolean checkServieRunning(Context context, String serviceName) {
		boolean isRunning = false;
		
		try {
			ActivityManager actMng = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
			List<RunningServiceInfo> list = actMng.getRunningServices(Integer.MAX_VALUE);
			for (RunningServiceInfo rsi : list) {
				if (rsi.service.getClassName().equals(serviceName)) {
					isRunning = true;
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return isRunning;
	}

	// Mac Address�� ���´�.
	public static String getMacAddress(Context context) {
		String macAddress = "";
		try {
		    WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		    WifiInfo wifiInfo = wifiManager.getConnectionInfo();	
		    macAddress = wifiInfo == null ? "" : wifiInfo.getMacAddress();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return macAddress;
	} 
	
	/**
     * ���ڿ����� ���ڸ� ����
     * @PARAM String
     * @return
     *
     */
    public static String getIdValidation(String str){
        if(str == null || str.isEmpty())
        	return "null";

		boolean bln = Pattern.matches("^[a-zA-Z0-9]*$", str);
		if (bln == false) {
			
	        StringBuffer sb = new StringBuffer();
	        int length = str.length();
	        
	        for(int i = 0 ; i < length ; i++){
	            char curChar = str.charAt(i);

		        bln = false;
		        if( 'a' <= curChar && 'z' >= curChar){
		        	bln = true;	
		        }else if( 'A' <= curChar && 'Z' >= curChar){
		        	bln = true;	
		        }else if( '0' <= curChar && '9' >= curChar){
		        	bln = true;	
		        }
		        
	            if(bln){
	            	sb.append(curChar);
	            }
	        }

	        return sb.toString();
	        
		}else{
	        return str;
		}
    }
    
}
