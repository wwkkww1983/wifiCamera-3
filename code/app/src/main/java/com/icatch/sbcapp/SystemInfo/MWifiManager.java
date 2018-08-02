package com.icatch.sbcapp.SystemInfo;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.icatch.sbcapp.AppInfo.AppInfo;
import com.icatch.sbcapp.AppInfo.AppSharedPreferences;
import com.icatch.sbcapp.Log.AppLog;
import com.icatch.sbcapp.Tools.HotSpot;

/**
 * Created by zhang yanhu C001012 on 2015/11/24 17:57.
 */
public class MWifiManager {
    private static String TAG = "MWifiManager";
    public static String getSsid(Context context){
        if(HotSpot.isApEnabled(context)){
            String ssid = HotSpot.getWifiApSSID(context);
            return ssid;
        }
        WifiManager mWifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = mWifi.getConnectionInfo();
        if(wifiInfo.getSSID() == null){
            return null;
        }
        return wifiInfo.getSSID().replaceAll("\"", "");
    }

    public static String getIp(Context context){
        String ip = "192.168.1.1";
        if(HotSpot.isApEnabled(context)){
            String value = HotSpot.getFirstConnectedHotIP();
            if(value != null){
                ip = value;
            }
        }else if(AppInfo.youtubeLive){
            String value = AppSharedPreferences.readIp(context);
            if(value != null){
                ip = value;
            }
        }
//        AppLog.d(TAG,"getIp ip=" + ip);
        return ip;
    }
}
