package com.icatch.sbcapp.AppInfo;

import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;

import com.icatch.sbcapp.BaseItems.BluetoothAppDevice;
import com.icatch.sbcapp.BaseItems.PhotoWallPreviewType;
import com.icatch.sbcapp.Log.AppLog;
import com.icatch.wificam.customer.ICatchWificamControl;
import com.icatch.wificam.customer.ICatchWificamPlayback;
import com.icatch.wificam.customer.ICatchWificamSession;
import com.icatch.wificam.customer.ICatchWificamVideoPlayback;
import com.icatchtek.bluetooth.customer.client.ICatchBluetoothClient;

import java.util.List;

/**
 * Created by yh.zhang C001012 on 2015/10/15:13:27.
 * Fucntion:
 */
public class AppInfo {
    public static final String SDK_LOG_DIRECTORY_PATH ="/IcatchSportCamera_SDK_Log";
    public static final String PROPERTY_CFG_FILE_NAME = "netconfig.properties";
    public static final String STREAM_OUTPUT_DIRECTORY_PATH = "/SportCamResoure/Raw/";

    public static final String PROPERTY_CFG_DIRECTORY_PATH = "/SportCamResoure/";
    public static final String APP_VERSION = "R1.4.14_beta3_3";
    public static final String DOWNLOAD_PATH = "/DCIM/SBCapp/";
    public static final String AUTO_DOWNLOAD_PATH = "/DCIM/SBCapp/";
    public static final String UPDATEFW_FILENAME = "/SportCamResoure/sphost.BRN";
    public static final String FILE_GOOGLE_TOKEN = "file_googleToken.dat";
    public static final String EULA_VERSION = "1.1"; // AgreeLicenseAgreementVersion

    public static boolean isSupportAutoReconnection = false;
    public static boolean isSupportBroadcast = false;
    public static boolean isSupportSetting = false;
    public static boolean saveSDKLog = false;
    public static boolean isSdCardExist = true;
    public static boolean disableAudio = false;
    public static boolean autoDownloadAllow = false;
    public static float autoDownloadSizeLimit = 1.0f;// GB
    public static boolean isDownloading = false;
    public static boolean youtubeLive = false;
    public static boolean displayDecodeTime = false;

    public static boolean isAppSentToBackground(final Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            Log.d("1111", "appProcess.processName =" + appProcess.processName);
            if (appProcess.processName.equals(context.getPackageName())) {
				/*
				 * BACKGROUND=400 EMPTY=500 FOREGROUND=100 GONE=1000
				 * PERCEPTIBLE=130 SERVICE=300 ISIBLE=200
				 */
                if (appProcess.importance != ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;
    }

    public static void initAppdata(Context context){
        String sizeLimit = AppSharedPreferences.readDataByName(context,AppSharedPreferences.OBJECT_NAME);
        AppLog.d("dd","initAppdata sizeLimit=" + sizeLimit);
        if(sizeLimit == null){
            autoDownloadSizeLimit = 1.0f;
        }else {
            autoDownloadSizeLimit = Float.parseFloat(sizeLimit);
        }
        AppLog.d("dd","initAppdata autoDownloadSizeLimit=" + autoDownloadSizeLimit);


    }
}
