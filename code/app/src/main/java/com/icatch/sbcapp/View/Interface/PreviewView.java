package com.icatch.sbcapp.View.Interface;

import android.graphics.Bitmap;

import com.icatch.sbcapp.Adapter.SettingListAdapter;
import com.icatch.sbcapp.Listener.OnDecodeTimeListener;
import com.icatch.sbcapp.MyCamera.MyCamera;

/**
 * Created by zhang yanhu C001012 on 2015/12/4 15:09.
 */
public interface PreviewView {
    void setmPreviewVisibility(int visibility);

    void setWbStatusVisibility(int visibility);

    void setBurstStatusVisibility(int visibility);

    void setWifiStatusVisibility(int visibility);

    void setWifiIcon(int drawableId);

    void setBatteryStatusVisibility(int visibility);

    void setBatteryIcon(int drawableId);

    void settimeLapseModeVisibility(int visibility);

    void settimeLapseModeIcon(int drawableId);

    void setSlowMotionVisibility(int visibility);

    void setCarModeVisibility(int visibility);

    void setRecordingTimeVisibility(int visibility);

    void setAutoDownloadVisibility(int visibility);

    void setCaptureBtnBackgroundResource(int id);

    void setRecordingTime(String laspeTime);

    void setDelayCaptureLayoutVisibility(int visibility);

    void setDelayCaptureTextTime(String delayCaptureTime);

    void setBurstStatusIcon(int drawableId);

    void setWbStatusIcon(int drawableId);

    void setUpsideVisibility(int visibility);

    void startMPreview(MyCamera myCamera);

    void stopMPreview(MyCamera myCamera);

    void setCaptureBtnEnability(boolean enablity);

    void showZoomView();

    void setMaxZoomRate(int maxZoomRate);

    int getZoomViewProgress();

    int getZoomViewMaxZoomRate();

    void updateZoomViewProgress(int currentZoomRatio);

    void setSettingMenuListAdapter(SettingListAdapter settingListAdapter);

    int getSetupMainMenuVisibility();

    void setSetupMainMenuVisibility(int visibility);

    void setAutoDownloadBitmap(Bitmap bitmap);

    void setActionBarTitle(int resId);

    void setSettingBtnVisible(boolean isVisible);

    void setBackBtnVisibility(boolean isVisible);

    void setSupportPreviewTxvVisibility(int visibility);

    void setPvModeBtnBackgroundResource(int drawableId);

    void showPopupWindow(int curMode);

    void setTimepLapseRadioBtnVisibility(int visibility);

    void setCaptureRadioBtnVisibility(int visibility);

    void setVideoRadioBtnVisibility(int visibility);

    void setTimepLapseRadioChecked(boolean checked);

    void setCaptureRadioBtnChecked(boolean checked);

    void setVideoRadioBtnChecked(boolean checked);

    void dismissPopupWindow();

    void setPreviewInfo(String info);

    void setDecodeInfo(String info);

    void setYouTubeLiveLayoutVisibility(int visibility);

    void setYouTubeBtnTxv(int resId);

    void setOnDecodeTimeListener(OnDecodeTimeListener onDecodeTimeListener);

    void setDecodeTimeLayoutVisibility(int visibility);

    void setDecodeTimeTxv(String value);
}
