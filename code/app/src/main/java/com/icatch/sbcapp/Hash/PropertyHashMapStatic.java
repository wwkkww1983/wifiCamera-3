package com.icatch.sbcapp.Hash;

import android.annotation.SuppressLint;

import com.icatch.sbcapp.BaseItems.BurstNumber;
import com.icatch.sbcapp.BaseItems.WhiteBalance;
import com.icatch.wificam.customer.type.ICatchBurstNumber;
import com.icatch.wificam.customer.type.ICatchDateStamp;
import com.icatch.wificam.customer.type.ICatchLightFrequency;
import com.icatch.wificam.customer.type.ICatchWhiteBalance;
import com.icatch.sbcapp.BaseItems.SlowMotion;
import com.icatch.sbcapp.BaseItems.TimeLapseDuration;
import com.icatch.sbcapp.BaseItems.TimeLapseMode;
import com.icatch.sbcapp.BaseItems.Upside;
import com.icatch.sbcapp.Beans.ItemInfo;
import com.icatch.sbcapp.Log.AppLog;
import com.icatch.sbcapp.R;

import java.util.HashMap;

public class PropertyHashMapStatic {
    private final String tag = "PropertyHashMapStatic";
    @SuppressLint("UseSparseArrays")
    public static HashMap<Integer, ItemInfo> burstMap = new HashMap<Integer, ItemInfo>();
    @SuppressLint("UseSparseArrays")
    public static HashMap<Integer, ItemInfo> whiteBalanceMap = new HashMap<Integer, ItemInfo>();
    @SuppressLint("UseSparseArrays")
    public static HashMap<Integer, ItemInfo> electricityFrequencyMap = new HashMap<Integer, ItemInfo>();
    @SuppressLint("UseSparseArrays")
    public static HashMap<Integer, ItemInfo> dateStampMap = new HashMap<Integer, ItemInfo>();
    @SuppressLint("UseSparseArrays")
    public static HashMap<Integer, ItemInfo> timeLapseMode = new HashMap<Integer, ItemInfo>();
    @SuppressLint("UseSparseArrays")
    public static HashMap<Integer, ItemInfo> timeLapseIntervalMap = new HashMap<Integer, ItemInfo>();
    @SuppressLint("UseSparseArrays")
    public static HashMap<Integer, ItemInfo> timeLapseDurationMap = new HashMap<Integer, ItemInfo>();
    @SuppressLint("UseSparseArrays")
    public static HashMap<Integer, ItemInfo> slowMotionMap = new HashMap<Integer, ItemInfo>();
    @SuppressLint("UseSparseArrays")
    public static HashMap<Integer, ItemInfo> upsideMap = new HashMap<Integer, ItemInfo>();

    public static PropertyHashMapStatic propertyHashMap;

    public static PropertyHashMapStatic getInstance() {
        if (propertyHashMap == null) {
            propertyHashMap = new PropertyHashMapStatic();
        }
        return propertyHashMap;
    }

    public void initPropertyHashMap() {
        AppLog.i(tag, "Start initPropertyHashMap");
        initWhiteBalanceMap();
        initTimeLapseDuration();
        initSlowMotion();
        initUpside();
        initBurstMap();
        initElectricityFrequencyMap();
        initDateStampMap();
        ininTimeLapseMode();
        AppLog.i(tag, "End initPropertyHashMap");
    }

    private void ininTimeLapseMode() {
        timeLapseMode.put(TimeLapseMode.TIME_LAPSE_MODE_STILL, new ItemInfo(R.string.timeLapse_capture_mode, null, 0));
        timeLapseMode.put(TimeLapseMode.TIME_LAPSE_MODE_VIDEO, new ItemInfo(R.string.timeLapse_video_mode, null, 0));
        // TODO Auto-generated method stub

    }

    public void initWhiteBalanceMap() {
        whiteBalanceMap.put(WhiteBalance.WB_AUTO, new ItemInfo(R.string.wb_auto, null, R.drawable.awb_auto));
        whiteBalanceMap.put(WhiteBalance.WB_CLOUDY, new ItemInfo(R.string.wb_cloudy, null, R.drawable.awb_cloudy));
        whiteBalanceMap.put(WhiteBalance.WB_DAYLIGHT, new ItemInfo(R.string.wb_daylight, null, R.drawable.awb_daylight));
        whiteBalanceMap.put(WhiteBalance.WB_FLUORESCENT, new ItemInfo(R.string.wb_fluorescent, null, R.drawable.awb_fluoresecent));
        whiteBalanceMap.put(WhiteBalance.WB_TUNGSTEN, new ItemInfo(R.string.wb_incandescent, null, R.drawable.awb_incadescent)); // whiteBalanceMap.put(ICatchWhiteBalance.ICH_WB_UNDEFINED,
        //  ICOM-4219
        whiteBalanceMap.put(WhiteBalance.WB_UNDERWATER, new ItemInfo(R.string.wb_underwater, null, R.drawable.underwater));
        //  ICOM-4219
    }

    private void initTimeLapseDuration() {
        // TODO Auto-generated method stub
        timeLapseIntervalMap.put(TimeLapseDuration.TIME_LAPSE_DURATION_2MIN, new ItemInfo(R.string.setting_time_lapse_duration_2M, null, 0));
        timeLapseIntervalMap.put(TimeLapseDuration.TIME_LAPSE_DURATION_5MIN, new ItemInfo(R.string.setting_time_lapse_duration_5M, null, 0));
        timeLapseIntervalMap.put(TimeLapseDuration.TIME_LAPSE_DURATION_10MIN, new ItemInfo(R.string.setting_time_lapse_duration_10M, null, 0));
        timeLapseIntervalMap.put(TimeLapseDuration.TIME_LAPSE_DURATION_15MIN, new ItemInfo(R.string.setting_time_lapse_duration_15M, null, 0));
        timeLapseIntervalMap.put(TimeLapseDuration.TIME_LAPSE_DURATION_20MIN, new ItemInfo(R.string.setting_time_lapse_duration_20M, null, 0));
        timeLapseIntervalMap.put(TimeLapseDuration.TIME_LAPSE_DURATION_30MIN, new ItemInfo(R.string.setting_time_lapse_duration_30M, null, 0));
        timeLapseIntervalMap.put(TimeLapseDuration.TIME_LAPSE_DURATION_60MIN, new ItemInfo(R.string.setting_time_lapse_duration_60M, null, 0));
        timeLapseIntervalMap.put(TimeLapseDuration.TIME_LAPSE_DURATION_UNLIMITED, new ItemInfo(R.string.setting_time_lapse_duration_unlimit, null, 0));
    }

    private void initSlowMotion() {
        // TODO Auto-generated method stub
        slowMotionMap.put(SlowMotion.SLOW_MOTION_OFF, new ItemInfo(R.string.setting_off, null, 0));
        slowMotionMap.put(SlowMotion.SLOW_MOTION_ON, new ItemInfo(R.string.setting_on, null, 0));
    }

    private void initUpside() {
        // TODO Auto-generated method stub
        upsideMap.put(Upside.UPSIDE_OFF, new ItemInfo(R.string.setting_off, null, 0));
        upsideMap.put(Upside.UPSIDE_ON, new ItemInfo(R.string.setting_on, null, 0));
    }

    public void initBurstMap() {
        burstMap.put(BurstNumber.BURST_NUMBER_OFF, new ItemInfo(R.string.burst_off, null, 0));
        burstMap.put(BurstNumber.BURST_NUMBER_3, new ItemInfo(R.string.burst_3, null, R.drawable.continuous_shot_1));
        burstMap.put(BurstNumber.BURST_NUMBER_5, new ItemInfo(R.string.burst_5, null, R.drawable.continuous_shot_2));
        burstMap.put(BurstNumber.BURST_NUMBER_10, new ItemInfo(R.string.burst_10, null, R.drawable.continuous_shot_3));
        //  ICOM-4219
        burstMap.put(BurstNumber.BURST_NUMBER_7, new ItemInfo(R.string.burst_7, null, R.drawable.continuous_shot_7));
        burstMap.put(BurstNumber.BURST_NUMBER_15, new ItemInfo(R.string.burst_15, null, R.drawable.continuous_shot_15));
        burstMap.put(BurstNumber.BURST_NUMBER_30, new ItemInfo(R.string.burst_30, null, R.drawable.continuous_shot_30));
        //  ICOM-4219
        burstMap.put(BurstNumber.BURST_NUMBER_HS, new ItemInfo(R.string.burst_hs, null, 0));
    }

    public void initElectricityFrequencyMap() {
        electricityFrequencyMap.put(ICatchLightFrequency.ICH_LIGHT_FREQUENCY_50HZ, new ItemInfo(R.string.frequency_50HZ, null, 0));
        electricityFrequencyMap.put(ICatchLightFrequency.ICH_LIGHT_FREQUENCY_60HZ, new ItemInfo(R.string.frequency_60HZ, null, 0));
    }

    public void initDateStampMap() {
        dateStampMap.put(ICatchDateStamp.ICH_DATE_STAMP_OFF, new ItemInfo(R.string.dateStamp_off, null, 0));
        dateStampMap.put(ICatchDateStamp.ICH_DATE_STAMP_DATE, new ItemInfo(R.string.dateStamp_date, null, 0));
        dateStampMap.put(ICatchDateStamp.ICH_DATE_STAMP_DATE_TIME, new ItemInfo(R.string.dateStamp_date_and_time, null, 0));
    }
}
