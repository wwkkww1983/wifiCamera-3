package com.icatch.sbcapp.Presenter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.icatch.sbcapp.Adapter.SettingListAdapter;
import com.icatch.sbcapp.AppDialog.AppDialog;
import com.icatch.sbcapp.AppDialog.AppToast;
import com.icatch.sbcapp.AppInfo.AppInfo;
import com.icatch.sbcapp.BaseItems.SlowMotion;
import com.icatch.sbcapp.BaseItems.TimeLapseInterval;
import com.icatch.sbcapp.BaseItems.TimeLapseMode;
import com.icatch.sbcapp.BaseItems.Tristate;
import com.icatch.sbcapp.BaseItems.Upside;
import com.icatch.sbcapp.Beans.GoogleToken;
import com.icatch.sbcapp.Beans.SettingMenu;
import com.icatch.sbcapp.Beans.StreamInfo;
import com.icatch.sbcapp.CustomException.NullPointerException;
import com.icatch.sbcapp.DataConvert.StreamInfoConvert;
import com.icatch.sbcapp.ExtendComponent.MyProgressDialog;
import com.icatch.sbcapp.ExtendComponent.MyToast;
import com.icatch.sbcapp.Function.PhotoCapture;
import com.icatch.sbcapp.Function.ZoomInOut;
import com.icatch.sbcapp.GlobalApp.GlobalInfo;
import com.icatch.sbcapp.Listener.OnDecodeTimeListener;
import com.icatch.sbcapp.Listener.OnSettingCompleteListener;
import com.icatch.sbcapp.Log.AppLog;
import com.icatch.sbcapp.Message.AppMessage;
import com.icatch.sbcapp.Mode.PreviewMode;
import com.icatch.sbcapp.Model.Implement.SDKEvent;
import com.icatch.sbcapp.Model.UIDisplaySource;
import com.icatch.sbcapp.MyCamera.MyCamera;
import com.icatch.sbcapp.Oauth.CreateBroadcast;
import com.icatch.sbcapp.Oauth.GoogleAuthTool;
import com.icatch.sbcapp.Oauth.YoutubeCredential;
import com.icatch.sbcapp.Presenter.Interface.BasePresenter;
import com.icatch.sbcapp.PropertyId.PropertyId;
import com.icatch.sbcapp.R;
import com.icatch.sbcapp.SdkApi.CameraAction;
import com.icatch.sbcapp.SdkApi.CameraProperties;
import com.icatch.sbcapp.SdkApi.CameraState;
import com.icatch.sbcapp.SdkApi.FileOperation;
import com.icatch.sbcapp.SdkApi.PreviewStream;
import com.icatch.sbcapp.Setting.OptionSetting;
import com.icatch.sbcapp.ThumbnailGetting.ThumbnailOperation;
import com.icatch.sbcapp.Tools.BitmapTools;
import com.icatch.sbcapp.Tools.ConvertTools;
import com.icatch.sbcapp.Tools.FileOpertion.FileOper;
import com.icatch.sbcapp.Tools.FileOpertion.FileTools;
import com.icatch.sbcapp.Tools.QRCode;
import com.icatch.sbcapp.Tools.StorageUtil;
import com.icatch.sbcapp.Tools.TimeTools;
import com.icatch.sbcapp.View.Activity.LoginGoogleActivity;
import com.icatch.sbcapp.View.Interface.PreviewView;
import com.icatch.wificam.customer.ICatchWificamConfig;
import com.icatch.wificam.customer.ICatchWificamPreview;
import com.icatch.wificam.customer.type.ICatchCameraProperty;
import com.icatch.wificam.customer.type.ICatchDateStamp;
import com.icatch.wificam.customer.type.ICatchEventID;
import com.icatch.wificam.customer.type.ICatchFile;
import com.icatch.wificam.customer.type.ICatchH264StreamParam;
import com.icatch.wificam.customer.type.ICatchMJPGStreamParam;
import com.icatch.wificam.customer.type.ICatchMode;
import com.icatch.wificam.customer.type.ICatchPreviewMode;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.icatch.sbcapp.Mode.PreviewMode.APP_STATE_TIMELAPSE_VIDEO_PREVIEW;

/**
 * Created by zhang yanhu C001012 on 2015/12/4 14:22.
 */
public class PreviewPresenter extends BasePresenter {
    private static final String TAG = "PreviewPresenter";
    private MediaPlayer videoCaptureStartBeep;
    private MediaPlayer modeSwitchBeep;
    private MediaPlayer stillCaptureStartBeep;
    private MediaPlayer continuousCaptureBeep;
    private Activity activity;
    private PreviewView previewView;
    private CameraProperties cameraProperties;
    private CameraAction cameraAction;
    private CameraState cameraState;
    private PreviewStream previewStream;
    private FileOperation fileOperation;
    private ICatchWificamPreview cameraPreviewStreamClint;
    private MyCamera currentCamera;
    private PreviewHandler previewHandler;
    private SDKEvent sdkEvent;
    private int curMode = PreviewMode.APP_STATE_NONE_MODE;
    private Timer videoCaptureButtomChangeTimer;
    public boolean videoCaptureButtomChangeFlag = true;
    private Timer recordingLapseTimeTimer;
    private int lapseTime = 0;
    private List<SettingMenu> settingMenuList;
    private SettingListAdapter settingListAdapter;
    private boolean allowClickButtoms = true;
    private int currentSettingMenuMode;
    private WifiSSReceiver wifiSSReceiver;
    private Boolean supportStreaming = true;
    private long lastCilckTime = 0;
    private long lastRecodeTime;
    private Tristate ret;
    private int curCacheTime = 0;
    private boolean isYouTubeLiving = false;
    private boolean needShowSBCHint = true;

    public PreviewPresenter(Activity activity) {
        super(activity);
        this.activity = activity;
    }

    public void setView(PreviewView previewView) {
        this.previewView = previewView;
        initCfg();
//        initData();
    }

    public void initUI() {
        if (AppInfo.youtubeLive) {
            previewView.setYouTubeLiveLayoutVisibility(View.VISIBLE);
        } else {
            previewView.setYouTubeLiveLayoutVisibility(View.GONE);
        }
    }


    public void initData() {
        cameraProperties = CameraProperties.getInstance();
        cameraAction = CameraAction.getInstance();
        cameraState = CameraState.getInstance();
        previewStream = PreviewStream.getInstance();
        fileOperation = FileOperation.getInstance();
        currentCamera = GlobalInfo.getInstance().getCurrentCamera();
        cameraPreviewStreamClint = currentCamera.getpreviewStreamClient();

        videoCaptureStartBeep = MediaPlayer.create(activity, R.raw.camera_timer);
        stillCaptureStartBeep = MediaPlayer.create(activity, R.raw.captureshutter);
        continuousCaptureBeep = MediaPlayer.create(activity, R.raw.captureburst);
        modeSwitchBeep = MediaPlayer.create(activity, R.raw.focusbeep);

        previewHandler = new PreviewHandler();
        sdkEvent = new SDKEvent(previewHandler);

        if (cameraProperties.hasFuction(0xD7F0)) {
            cameraProperties.setCaptureDelayMode(1);
        }
//JIRA BUG delete IC-591
//        int cacheTime = cameraProperties.getPreviewCacheTime();
//        if (cacheTime < 200) {
//            cacheTime = 200;
//        }
//        ICatchWificamConfig.getInstance().setPreviewCacheParam(cacheTime, 200);
        AppLog.i(TAG, "cameraProperties.getMaxZoomRatio() =" + cameraProperties.getMaxZoomRatio());

//        GetCurrentImageSizeTask task = new GetCurrentImageSizeTask();
//		getImageSizeTimer = new Timer(true);
//		getImageSizeTimer.schedule(task, 0,5000);
    }

    public void initStatus() {
        int resid = ThumbnailOperation.getBatteryLevelIcon();
        if (resid > 0) {
            previewView.setBatteryIcon(resid);
            if (resid == R.drawable.ic_battery_charging_green24dp) {
                AppDialog.showLowBatteryWarning(activity);
            }
        }
        IntentFilter wifiSSFilter = new IntentFilter(WifiManager.RSSI_CHANGED_ACTION);
        wifiSSReceiver = new WifiSSReceiver();
        activity.registerReceiver(wifiSSReceiver, wifiSSFilter);
        AppLog.i(TAG, "initStatus start showDialogWarn: Only for iCatch SBC");
        if(needShowSBCHint){
            AppDialog.showDialogWarn(activity, R.string.text_preview_hint_info);
            needShowSBCHint = false;
        }
        GlobalInfo.getInstance().startConnectCheck();
        if (AppInfo.displayDecodeTime) {
            previewView.setDecodeTimeLayoutVisibility(View.VISIBLE);
            previewView.setOnDecodeTimeListener(new OnDecodeTimeListener() {
                @Override
                public void decodeTime(final long time) {
                    previewHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            previewView.setDecodeTimeTxv((time / 1000.0) + "ms");
                        }
                    });
                }
            });
        } else {
            previewView.setDecodeTimeLayoutVisibility(View.GONE);
            previewView.setOnDecodeTimeListener(null);
        }
    }


    public void changeCameraMode(final int previewMode, final ICatchPreviewMode ichVideoPreviewMode) {
        AppLog.i(TAG, "start changeCameraMode ichVideoPreviewMode=" + ichVideoPreviewMode);
        AppLog.i(TAG, "start changeCameraMode previewMode=" + previewMode + " isStreaming=" + currentCamera.isStreaming);
        if (currentCamera.isStreaming) {
            AppLog.d(TAG, "changeCameraMode currnet streaming has started,do not need to start again!");
            return;
        }
        ret = Tristate.FALSE;
        previewView.setmPreviewVisibility(View.GONE);
        MyProgressDialog.showProgressDialog(activity, "processing..");
        new Thread(new Runnable() {
            @Override
            public void run() {
                //add by b.jiang 兼容旧版Firmware
                if (previewMode == PreviewMode.APP_STATE_STILL_PREVIEW ||
                        previewMode == PreviewMode.APP_STATE_STILL_CAPTURE ||
                        previewMode == PreviewMode.APP_STATE_TIMELAPSE_STILL_PREVIEW ||
                        previewMode == PreviewMode.APP_STATE_TIMELAPSE_STILL_CAPTURE) {
                    cameraProperties.getRemainImageNum();
                } else {
                    cameraProperties.getRecordingRemainTime();
                }
                //end add

                ret = startMediaStream(ichVideoPreviewMode);
                if (ret == Tristate.NORMAL) {
                    currentCamera.isStreaming = true;
                    previewHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            curMode = previewMode;
                            previewView.startMPreview(currentCamera);
                            String info = "Current cache time=" + curCacheTime + "  enableSoftwareDecoder=" + GlobalInfo.enableSoftwareDecoder;
                            previewView.setDecodeInfo(info);
//                            createUIByMode(curMode);
                            AppLog.i(TAG, "startMPreview");
                            supportStreaming = true;
                            MyProgressDialog.closeProgressDialog();
                        }
                    });
                    previewHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            createUIByMode(curMode);
                        }
                    }, 1000);
                } else if (ret == Tristate.ABNORMAL) {
                    previewHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            curMode = previewMode;
                            createUIByMode(curMode);
                            previewView.setmPreviewVisibility(View.GONE);
                            previewView.setSupportPreviewTxvVisibility(View.VISIBLE);
                            supportStreaming = false;
                            MyProgressDialog.closeProgressDialog();
                        }
                    });
                } else if (ret == Tristate.FALSE) {
                    previewHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            MyToast.show(activity, R.string.stream_set_error);
                            curMode = previewMode;
                            createUIByMode(curMode);
                            supportStreaming = false;
                            MyProgressDialog.closeProgressDialog();
                        }
                    });
                }
            }
        }).start();
    }


    public boolean stopMediaStream() {
        if (!currentCamera.isStreaming) {
            AppLog.d(TAG, "stopMediaStream currnet Streaming has stopped,do not need to stop again!");
            return true;
        }
        boolean ret = previewStream.stopMediaStream(cameraPreviewStreamClint);
//        try {
//            Thread.sleep(500);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        currentCamera.isStreaming = false;
        return ret;
    }

    public Tristate startMediaStream(ICatchPreviewMode ichVideoPreviewMode) {
        AppLog.d(TAG, "start startMediaStream");
        Tristate ret = Tristate.FALSE;
        String streamUrl = cameraProperties.getCurrentStreamInfo();
//        String streamUrl = "H264?W=1280&H=720&BR=2000000&FPS=15&";
//        String streamUrl =  "MJPG?W=720&H=540&BR=4000000";
        AppLog.d(TAG, "1123 start startMediaStream streamUrl=[" + streamUrl + "]");
        if (streamUrl == null) {
            //JIRA IC-591
            int cacheTime = cameraProperties.getPreviewCacheTime();
            if (cacheTime > 0 && cacheTime < 200) {
                cacheTime = 200;
            }
//            int cacheTime = 0;
            Boolean setCacheRet = ICatchWificamConfig.getInstance().setPreviewCacheParam(cacheTime, 200);
            AppLog.d(TAG, "start startMediaStream setCacheRet=" + setCacheRet + " cacheTime=" + cacheTime);
            curCacheTime = cacheTime;
            ICatchMJPGStreamParam param = new ICatchMJPGStreamParam();
            ret = previewStream.startMediaStream(cameraPreviewStreamClint, param,
                    ichVideoPreviewMode, AppInfo.disableAudio);
            return ret;
        }

        StreamInfo streamInfo = StreamInfoConvert.convertToStreamInfoBean(streamUrl);
        GlobalInfo.curFps = streamInfo.fps;
        if (streamInfo.mediaCodecType.equals("MJPG")) {
            //JIRA IC-591
            int cacheTime = cameraProperties.getPreviewCacheTime();
            if (cacheTime > 0 && cacheTime < 200) {
                cacheTime = 200;
            }
//            int cacheTime = 0;
            ICatchWificamConfig.getInstance().setPreviewCacheParam(cacheTime, 200);
            AppLog.d(TAG, "start startMediaStream MJPG cacheTime=" + cacheTime);
            curCacheTime = cacheTime;
            ICatchMJPGStreamParam param = new ICatchMJPGStreamParam(streamInfo.width, streamInfo.height, streamInfo.bitrate, 50);
            AppLog.i(TAG, "begin startMediaStream MJPG");
            ret = previewStream.startMediaStream(cameraPreviewStreamClint, param,
                    ichVideoPreviewMode, AppInfo.disableAudio);
        } else if (streamInfo.mediaCodecType.equals("H264")) {
            //JIRA IC-591
            int cacheTime = cameraProperties.getPreviewCacheTime();
            if (cacheTime > 0 && cacheTime < 200) {
                cacheTime = 500;
            }
//            int cacheTime = 0;
            ICatchWificamConfig.getInstance().setPreviewCacheParam(cacheTime, 200);
            AppLog.d(TAG, "start startMediaStream start H264 startMediaStream cacheTime=" + cacheTime);
            curCacheTime = cacheTime;
//            if (GlobalInfo.enableSoftwareDecoder) {
//                streamUrl = ConvertTools.resolutionConvert(streamUrl);
//            }
//            ICatchCustomerStreamParam param = new ICatchCustomerStreamParam(554, streamUrl);

            ICatchH264StreamParam param = new ICatchH264StreamParam(streamInfo.width, streamInfo.height, streamInfo.bitrate, streamInfo.fps);
            ret = previewStream.startMediaStream(cameraPreviewStreamClint, param, ichVideoPreviewMode, AppInfo.disableAudio);
        }
        AppLog.i(TAG, "end startMediaStream ret = " + ret);
        return ret;
    }

    public void startOrStopCapture() {
        AppLog.d(TAG, "begin startOrStopCapture curMode=" + curMode);
        if (isYouTubeLiving) {
            MyToast.show(activity, R.string.stop_live_hint);
            return;
        }
        if (TimeTools.isFastClick()) {
            return;
        }
        if (curMode == PreviewMode.APP_STATE_VIDEO_PREVIEW) {

            if (cameraProperties.isSDCardExist() == false) {
                AppDialog.showDialogWarn(activity, R.string.dialog_card_not_exist);
                return;
            }
            int remainTime = cameraProperties.getRecordingRemainTime();
            if (remainTime == 0) {
                AppDialog.showDialogWarn(activity, R.string.dialog_sd_card_is_full);
                return;
            } else if (remainTime < 0) {
                AppDialog.showDialogWarn(activity, R.string.text_get_data_exception);
                return;
            }
            videoCaptureStartBeep.start();
            lastRecodeTime = System.currentTimeMillis();
            if (cameraAction.startMovieRecord()) {
                AppLog.i(TAG, "startRecordingLapseTimeTimer(0)");
                curMode = PreviewMode.APP_STATE_VIDEO_CAPTURE;
                startVideoCaptureButtomChangeTimer();
                startRecordingLapseTimeTimer(0);
            }
        } else if (curMode == PreviewMode.APP_STATE_VIDEO_CAPTURE) {
            videoCaptureStartBeep.start();
            if (System.currentTimeMillis() - lastRecodeTime < 2000) {
                MyToast.show(activity, "Operation Frequent!");
                return;
            }
            if (cameraAction.stopVideoCapture()) {
                curMode = PreviewMode.APP_STATE_VIDEO_PREVIEW;
                stopVideoCaptureButtomChangeTimer();
                stopRecordingLapseTimeTimer();
                String info = currentCamera.getVideoSize().getCurrentUiStringInPreview() + "/" + ConvertTools.secondsToMinuteOrHours(cameraProperties
                        .getRecordingRemainTime());
                previewView.setPreviewInfo(info);
            }
        } else if (curMode == PreviewMode.APP_STATE_STILL_PREVIEW) {

            if (cameraProperties.isSDCardExist() == false) {
                AppDialog.showDialogWarn(activity, R.string.dialog_card_not_exist);
                return;
            }
            int remainImageNum = cameraProperties.getRemainImageNum();
            if (remainImageNum == 0) {
                AppDialog.showDialogWarn(activity, R.string.dialog_sd_card_is_full);
                return;
            } else if (remainImageNum < 0) {
                AppDialog.showDialogWarn(activity, R.string.text_get_data_exception);
                return;
            }
//            stillCaptureStartBeep.start();
            curMode = PreviewMode.APP_STATE_STILL_CAPTURE;
            startPhotoCapture();
        } else if (curMode == PreviewMode.APP_STATE_TIMELAPSE_STILL_PREVIEW) {

            if (cameraProperties.isSDCardExist() == false) {
                AppDialog.showDialogWarn(activity, R.string.dialog_card_not_exist);
                return;
            }
            int remainImageNum = cameraProperties.getRemainImageNum();
            if (remainImageNum == 0) {
                AppDialog.showDialogWarn(activity, R.string.dialog_sd_card_is_full);
                return;
            } else if (remainImageNum < 0) {
                AppDialog.showDialogWarn(activity, R.string.text_get_data_exception);
                return;
            }
            if (cameraProperties.getCurrentTimeLapseInterval() == TimeLapseInterval.TIME_LAPSE_INTERVAL_OFF) {
                AppDialog.showDialogWarn(activity, R.string.timeLapse_not_allow);
                return;
            }
            continuousCaptureBeep.start();
            if (cameraAction.startTimeLapse() == false) {
                AppLog.e(TAG, "failed to start startTimeLapse");
                return;
            }
            previewView.setCaptureBtnBackgroundResource(R.drawable.still_capture_btn_off);
            curMode = PreviewMode.APP_STATE_TIMELAPSE_STILL_CAPTURE;
        } else if (curMode == PreviewMode.APP_STATE_TIMELAPSE_STILL_CAPTURE) {
            AppLog.d(TAG, "curMode == PreviewMode.APP_STATE_TIMELAPSE_STILL_CAPTURE");
            if (cameraAction.stopTimeLapse() == false) {
                AppLog.e(TAG, "failed to stopTimeLapse");
                return;
            }
            stopRecordingLapseTimeTimer();
            curMode = PreviewMode.APP_STATE_TIMELAPSE_STILL_PREVIEW;
        } else if (curMode == APP_STATE_TIMELAPSE_VIDEO_PREVIEW) {
            AppLog.d(TAG, "curMode == PreviewMode.APP_STATE_TIMELAPSE_PREVIEW_VIDEO");
            if (cameraProperties.isSDCardExist() == false) {
                AppDialog.showDialogWarn(activity, R.string.dialog_card_not_exist);
                return;
            }

            int remainImageNum = cameraProperties.getRemainImageNum();
            if (remainImageNum == 0) {
                AppDialog.showDialogWarn(activity, R.string.dialog_sd_card_is_full);
                return;
            } else if (remainImageNum < 0) {
                AppDialog.showDialogWarn(activity, R.string.text_get_data_exception);
                return;
            }
            if (cameraProperties.getCurrentTimeLapseInterval() == TimeLapseInterval.TIME_LAPSE_INTERVAL_OFF) {
                AppLog.d(TAG, "time lapse is not allowed because of timelapse interval is OFF");
                AppDialog.showDialogWarn(activity, R.string.timeLapse_not_allow);
                return;
            }

            videoCaptureStartBeep.start();
            if (cameraAction.startTimeLapse() == false) {
                AppLog.e(TAG, "failed to start startTimeLapse");
                return;
            }
            curMode = PreviewMode.APP_STATE_TIMELAPSE_VIDEO_CAPTURE;
            startVideoCaptureButtomChangeTimer();
            startRecordingLapseTimeTimer(0);

        } else if (curMode == PreviewMode.APP_STATE_TIMELAPSE_VIDEO_CAPTURE) {
            AppLog.d(TAG, "curMode == PreviewMode.APP_STATE_TIMELAPSE_VIDEO_CAPTURE");
            videoCaptureStartBeep.start();
            if (cameraAction.stopTimeLapse() == false) {
                AppLog.e(TAG, "failed to stopTimeLapse");
                return;
            }
            stopVideoCaptureButtomChangeTimer();
            stopRecordingLapseTimeTimer();
            curMode = APP_STATE_TIMELAPSE_VIDEO_PREVIEW;
        }
        AppLog.d(TAG, "end processing for responsing captureBtn clicking");
    }

    public void createUIByMode(int previewMode) {
        AppLog.i(TAG, "start createUIByMode previewMode=" + previewMode);
        if (cameraProperties.cameraModeSupport(ICatchMode.ICH_MODE_VIDEO)) {
            if (previewMode == PreviewMode.APP_STATE_VIDEO_PREVIEW ||
                    previewMode == PreviewMode.APP_STATE_VIDEO_CAPTURE) {
                previewView.setPvModeBtnBackgroundResource(R.drawable.video_toggle_btn_on);
            }
        }
        if (previewMode == PreviewMode.APP_STATE_STILL_PREVIEW ||
                previewMode == PreviewMode.APP_STATE_STILL_CAPTURE) {
            previewView.setPvModeBtnBackgroundResource(R.drawable.capture_toggle_btn_on);
        }
        if (cameraProperties.cameraModeSupport(ICatchMode.ICH_MODE_TIMELAPSE)) {
            if (previewMode == PreviewMode.APP_STATE_TIMELAPSE_STILL_CAPTURE ||
                    previewMode == PreviewMode.APP_STATE_TIMELAPSE_STILL_PREVIEW ||
                    previewMode == PreviewMode.APP_STATE_TIMELAPSE_VIDEO_CAPTURE ||
                    previewMode == APP_STATE_TIMELAPSE_VIDEO_PREVIEW) {
                previewView.setPvModeBtnBackgroundResource(R.drawable.timelapse_toggle_btn_on);
            }
        }

        if (previewMode == PreviewMode.APP_STATE_STILL_CAPTURE ||
                previewMode == PreviewMode.APP_STATE_TIMELAPSE_STILL_CAPTURE ||
                previewMode == PreviewMode.APP_STATE_TIMELAPSE_STILL_PREVIEW ||
                previewMode == PreviewMode.APP_STATE_STILL_PREVIEW) {
            previewView.setCaptureBtnBackgroundResource(R.drawable.still_capture_btn);
        } else if (previewMode == PreviewMode.APP_STATE_VIDEO_CAPTURE ||
                previewMode == PreviewMode.APP_STATE_TIMELAPSE_VIDEO_CAPTURE ||
                previewMode == APP_STATE_TIMELAPSE_VIDEO_PREVIEW ||
                previewMode == PreviewMode.APP_STATE_VIDEO_PREVIEW) {
            previewView.setCaptureBtnBackgroundResource(R.drawable.video_recording_btn_on);
        }
        if (currentCamera.getCaptureDelay().needDisplayByMode(previewMode)) {
            previewView.setDelayCaptureLayoutVisibility(View.VISIBLE);
            previewView.setDelayCaptureTextTime(currentCamera.getCaptureDelay().getCurrentUiStringInPreview());
        } else {
            previewView.setDelayCaptureLayoutVisibility(View.GONE);
        }

        if (currentCamera.getImageSize().needDisplayByMode(previewMode)) {
            String info = currentCamera.getImageSize().getCurrentUiStringInPreview() + "/" + cameraProperties.getRemainImageNum();
            previewView.setPreviewInfo(info);
        }

        if (currentCamera.getVideoSize().needDisplayByMode(previewMode)) {
            String info = currentCamera.getVideoSize().getCurrentUiStringInPreview() + "/" + ConvertTools.secondsToMinuteOrHours(cameraProperties
                    .getRecordingRemainTime());
            previewView.setPreviewInfo(info);
        }

        if (currentCamera.getBurst().needDisplayByMode(previewMode)) {
            previewView.setBurstStatusVisibility(View.VISIBLE);
            try {
                previewView.setBurstStatusIcon(currentCamera.getBurst().getCurrentIcon());
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        } else {
            previewView.setBurstStatusVisibility(View.GONE);
        }

        if (currentCamera.getWhiteBalance().needDisplayByMode(previewMode)) {
            previewView.setWbStatusVisibility(View.VISIBLE);
            try {
                previewView.setWbStatusIcon(currentCamera.getWhiteBalance().getCurrentIcon());
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        } else {
            previewView.setWbStatusVisibility(View.GONE);
        }

        if (currentCamera.getUpside().needDisplayByMode(previewMode) &&
                cameraProperties.getCurrentUpsideDown() == Upside.UPSIDE_ON) {
            previewView.setUpsideVisibility(View.VISIBLE);
        } else {
            previewView.setUpsideVisibility(View.GONE);
        }

        if (currentCamera.getSlowMotion().needDisplayByMode(previewMode) &&
                cameraProperties.getCurrentSlowMotion() == SlowMotion.SLOW_MOTION_ON) {
            previewView.setSlowMotionVisibility(View.VISIBLE);
        } else {
            previewView.setSlowMotionVisibility(View.GONE);
        }

        if (currentCamera.getTimeLapseMode().needDisplayByMode(previewMode)) {
            previewView.settimeLapseModeVisibility(View.VISIBLE);
            try {
                previewView.settimeLapseModeIcon(currentCamera.getTimeLapseMode().getCurrentIcon());
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        } else {
            previewView.settimeLapseModeVisibility(View.GONE);
        }

    }

    public void startVideoCaptureButtomChangeTimer() {
        AppLog.d(TAG, "startVideoCaptureButtomChangeTimer videoCaptureButtomChangeTimer=" + videoCaptureButtomChangeTimer);
        TimerTask task = new TimerTask() {

            @Override
            public void run() {
                if (videoCaptureButtomChangeFlag) {
                    videoCaptureButtomChangeFlag = false;
                    previewHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (curMode == PreviewMode.APP_STATE_VIDEO_CAPTURE ||
                                    curMode == PreviewMode.APP_STATE_TIMELAPSE_VIDEO_CAPTURE) {
                                previewView.setCaptureBtnBackgroundResource(R.drawable.video_recording_btn_on);
                            }
                        }
                    });

                } else {
                    videoCaptureButtomChangeFlag = true;
                    previewHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (curMode == PreviewMode.APP_STATE_VIDEO_CAPTURE ||
                                    curMode == PreviewMode.APP_STATE_TIMELAPSE_VIDEO_CAPTURE) {
                                previewView.setCaptureBtnBackgroundResource(R.drawable.video_recording_btn_off);
                            }
                        }
                    });
                }
            }
        };

        videoCaptureButtomChangeTimer = new Timer(true);
        videoCaptureButtomChangeTimer.schedule(task, 0, 1000);
    }

    public void initPreview() {
        AppLog.i(TAG, "initPreview curMode=" + curMode);
        previewView.setMaxZoomRate(cameraProperties.getMaxZoomRatio());
        previewView.updateZoomViewProgress(cameraProperties.getCurrentZoomRatio());
        if (cameraState.isMovieRecording()) {
            AppLog.i(TAG, "camera is recording...");
            //JIRA ICOM-3537 Start Modify by b.jiang 20160726
//            if (changeCameraMode(PreviewMode.APP_STATE_VIDEO_CAPTURE,
//                    ICatchPreviewMode.ICH_VIDEO_PREVIEW_MODE) == Tristate.FALSE) {
//                return;
//            }
            changeCameraMode(PreviewMode.APP_STATE_VIDEO_CAPTURE, ICatchPreviewMode.ICH_VIDEO_PREVIEW_MODE);
            //JIRA ICOM-3537 End Modify by b.jiang 20160726
            //start recording buttom,need a period timer
            curMode = PreviewMode.APP_STATE_VIDEO_CAPTURE;
            startVideoCaptureButtomChangeTimer();
            //start recording time timer
            startRecordingLapseTimeTimer(cameraProperties.getVideoRecordingTime());

        } else if (cameraState.isTimeLapseVideoOn()) {
            AppLog.i(TAG, "camera is TimeLapseVideoOn...");
            currentCamera.timeLapsePreviewMode = TimeLapseMode.TIME_LAPSE_MODE_VIDEO;
//            if (changeCameraMode(PreviewMode.APP_STATE_TIMELAPSE_VIDEO_CAPTURE,
//                    ICatchPreviewMode.ICH_TIMELAPSE_VIDEO_PREVIEW_MODE) == Tristate.FALSE) {
//                return;
//            }
            GlobalInfo.getInstance().getCurrentCamera().resetTimeLapseVideoSize();
            changeCameraMode(PreviewMode.APP_STATE_TIMELAPSE_VIDEO_CAPTURE, ICatchPreviewMode.ICH_TIMELAPSE_VIDEO_PREVIEW_MODE);
            curMode = PreviewMode.APP_STATE_TIMELAPSE_VIDEO_CAPTURE;
            //default set
            //start recording buttom,need a period timer
            startVideoCaptureButtomChangeTimer();
            //start recording time timer
            startRecordingLapseTimeTimer(cameraProperties.getVideoRecordingTime());

        } else if (cameraState.isTimeLapseStillOn()) {
            AppLog.i(TAG, "camera is TimeLapseStillOn...");
            currentCamera.timeLapsePreviewMode = TimeLapseMode.TIME_LAPSE_MODE_STILL;
//            if (changeCameraMode(PreviewMode.APP_STATE_TIMELAPSE_STILL_CAPTURE,
//                    ICatchPreviewMode.ICH_TIMELAPSE_STILL_PREVIEW_MODE) == Tristate.FALSE) {
//                return;
//            }
            GlobalInfo.getInstance().getCurrentCamera().resetTimeLapseVideoSize();
            changeCameraMode(PreviewMode.APP_STATE_TIMELAPSE_STILL_CAPTURE, ICatchPreviewMode.ICH_TIMELAPSE_STILL_PREVIEW_MODE);
            //default set
            //start recording buttom,need a period timer
            curMode = PreviewMode.APP_STATE_TIMELAPSE_STILL_CAPTURE;
            startVideoCaptureButtomChangeTimer();
            //start recording time timer
            startRecordingLapseTimeTimer(cameraProperties.getVideoRecordingTime());

        } else if (curMode == PreviewMode.APP_STATE_NONE_MODE) {
            if (cameraProperties.cameraModeSupport(ICatchMode.ICH_MODE_VIDEO)) {
                previewStream.changePreviewMode(cameraPreviewStreamClint, ICatchPreviewMode.ICH_VIDEO_PREVIEW_MODE);
                changeCameraMode(PreviewMode.APP_STATE_VIDEO_PREVIEW, ICatchPreviewMode.ICH_VIDEO_PREVIEW_MODE);
            } else {
                changeCameraMode(PreviewMode.APP_STATE_STILL_PREVIEW, ICatchPreviewMode.ICH_VIDEO_PREVIEW_MODE);
            }
        } else if (curMode == PreviewMode.APP_STATE_VIDEO_PREVIEW) {
            AppLog.i(TAG, "initPreview curMode == PreviewMode.APP_STATE_VIDEO_PREVIEW");
            previewStream.changePreviewMode(cameraPreviewStreamClint, ICatchPreviewMode.ICH_VIDEO_PREVIEW_MODE);
            changeCameraMode(curMode, ICatchPreviewMode.ICH_VIDEO_PREVIEW_MODE);
            // normal state, app show preview
        } else if (curMode == APP_STATE_TIMELAPSE_VIDEO_PREVIEW) {
            AppLog.i(TAG, "initPreview curMode == PreviewMode.APP_STATE_TIMELAPSE_PREVIEW_VIDEO");
            currentCamera.timeLapsePreviewMode = TimeLapseMode.TIME_LAPSE_MODE_VIDEO;
            previewStream.changePreviewMode(cameraPreviewStreamClint, ICatchPreviewMode.ICH_TIMELAPSE_VIDEO_PREVIEW_MODE);
            changeCameraMode(curMode, ICatchPreviewMode.ICH_TIMELAPSE_VIDEO_PREVIEW_MODE);
            // normal state, app show preview
        } else if (curMode == PreviewMode.APP_STATE_TIMELAPSE_STILL_PREVIEW) {
            AppLog.i(TAG, "initPreview curMode == PreviewMode.APP_STATE_TIMELAPSE_PREVIEW_STILL");
            previewStream.changePreviewMode(cameraPreviewStreamClint, ICatchPreviewMode.ICH_TIMELAPSE_STILL_PREVIEW_MODE);
            currentCamera.timeLapsePreviewMode = TimeLapseMode.TIME_LAPSE_MODE_STILL;
            changeCameraMode(curMode, ICatchPreviewMode.ICH_TIMELAPSE_STILL_PREVIEW_MODE);
            // normal state, app show preview
        } else if (curMode == PreviewMode.APP_STATE_STILL_PREVIEW) {
            AppLog.i(TAG, "initPreview curMode == ICH_STILL_PREVIEW_MODE");
            changeCameraMode(curMode, ICatchPreviewMode.ICH_STILL_PREVIEW_MODE);
        }
        //do not start preview,
    }

    public void stopVideoCaptureButtomChangeTimer() {
        AppLog.d(TAG, "stopVideoCaptureButtomChangeTimer videoCaptureButtomChangeTimer=" + videoCaptureButtomChangeTimer);
        if (videoCaptureButtomChangeTimer != null) {
            videoCaptureButtomChangeTimer.cancel();
        }
        previewView.setCaptureBtnBackgroundResource(R.drawable.video_recording_btn_on);
    }

    private void startRecordingLapseTimeTimer(int startTime) {
        if (cameraProperties.hasFuction(PropertyId.VIDEO_RECORDING_TIME) == false) {
            return;
        }
        AppLog.i(TAG, "startRecordingLapseTimeTimer curMode=" + curMode);
        if (curMode == PreviewMode.APP_STATE_VIDEO_CAPTURE || curMode == PreviewMode.APP_STATE_TIMELAPSE_VIDEO_CAPTURE
                || curMode == PreviewMode.APP_STATE_TIMELAPSE_STILL_CAPTURE) {
            AppLog.i(TAG, "startRecordingLapseTimeTimer");
            if (recordingLapseTimeTimer != null) {
                recordingLapseTimeTimer.cancel();
            }

            lapseTime = startTime;
            recordingLapseTimeTimer = new Timer(true);
            previewView.setRecordingTimeVisibility(View.VISIBLE);

            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    previewHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            previewView.setRecordingTime(ConvertTools.secondsToHours(lapseTime++));
                        }
                    });
                }
            };
            recordingLapseTimeTimer.schedule(timerTask, 0, 1000);
        }
    }

    private void stopRecordingLapseTimeTimer() {
        if (recordingLapseTimeTimer != null) {
            recordingLapseTimeTimer.cancel();
        }
        previewView.setRecordingTime("00:00:00");
        previewView.setRecordingTimeVisibility(View.GONE);
    }

    public void changePreviewMode(int previewMode) {
        AppLog.d(TAG, "changePreviewMode previewMode=" + previewMode);
        AppLog.d(TAG, "changePreviewMode curMode=" + curMode);
        long timeInterval = System.currentTimeMillis() - lastCilckTime;
        AppLog.d(TAG, "repeat click: timeInterval=" + timeInterval);
        if (System.currentTimeMillis() - lastCilckTime < 2000) {
            AppLog.d(TAG, "repeat click: timeInterval < 2000");
            return;
        } else {
            lastCilckTime = System.currentTimeMillis();
        }
        modeSwitchBeep.start();
        previewView.dismissPopupWindow();
        if (previewMode == PreviewMode.APP_STATE_VIDEO_MODE) {
            if (curMode == PreviewMode.APP_STATE_STILL_CAPTURE ||
                    curMode == PreviewMode.APP_STATE_TIMELAPSE_VIDEO_CAPTURE ||
                    curMode == PreviewMode.APP_STATE_TIMELAPSE_STILL_CAPTURE) {
                if (curMode == PreviewMode.APP_STATE_STILL_CAPTURE || curMode == PreviewMode.APP_STATE_TIMELAPSE_STILL_CAPTURE) {
                    MyToast.show(activity, R.string.stream_error_capturing);
                } else if (curMode == PreviewMode.APP_STATE_VIDEO_CAPTURE || curMode == PreviewMode.APP_STATE_TIMELAPSE_VIDEO_CAPTURE) {
                    MyToast.show(activity, R.string.stream_error_recording);
                }
                return;
            } else if (curMode == PreviewMode.APP_STATE_STILL_PREVIEW ||
                    curMode == PreviewMode.APP_STATE_TIMELAPSE_STILL_PREVIEW ||
                    curMode == APP_STATE_TIMELAPSE_VIDEO_PREVIEW) {
                //createUIByMode(PreviewMode.APP_STATE_VIDEO_PREVIEW);
                previewView.stopMPreview(currentCamera);
                stopMediaStream();
                GlobalInfo.getInstance().getCurrentCamera().resetVideoSize();
                previewStream.changePreviewMode(cameraPreviewStreamClint, ICatchPreviewMode.ICH_VIDEO_PREVIEW_MODE);
                changeCameraMode(PreviewMode.APP_STATE_VIDEO_PREVIEW, ICatchPreviewMode.ICH_VIDEO_PREVIEW_MODE);
            }

        } else if (previewMode == PreviewMode.APP_STATE_STILL_MODE) {
            if (curMode == PreviewMode.APP_STATE_VIDEO_CAPTURE ||
                    curMode == PreviewMode.APP_STATE_TIMELAPSE_STILL_CAPTURE ||
                    curMode == PreviewMode.APP_STATE_TIMELAPSE_VIDEO_CAPTURE) {
                if (curMode == PreviewMode.APP_STATE_STILL_CAPTURE || curMode == PreviewMode.APP_STATE_TIMELAPSE_STILL_CAPTURE) {
                    MyToast.show(activity, R.string.stream_error_capturing);
                } else if (curMode == PreviewMode.APP_STATE_VIDEO_CAPTURE || curMode == PreviewMode.APP_STATE_TIMELAPSE_VIDEO_CAPTURE) {
                    MyToast.show(activity, R.string.stream_error_recording);
                }
                return;
            } else if (curMode == PreviewMode.APP_STATE_VIDEO_PREVIEW ||
                    curMode == PreviewMode.APP_STATE_TIMELAPSE_STILL_PREVIEW ||
                    curMode == APP_STATE_TIMELAPSE_VIDEO_PREVIEW) {
                previewView.stopMPreview(currentCamera);
                stopMediaStream();
                previewStream.changePreviewMode(cameraPreviewStreamClint, ICatchPreviewMode.ICH_STILL_PREVIEW_MODE);
                changeCameraMode(PreviewMode.APP_STATE_STILL_PREVIEW, ICatchPreviewMode.ICH_STILL_PREVIEW_MODE);
            }
        } else if (previewMode == PreviewMode.APP_STATE_TIMELAPSE_MODE) {
            if (curMode == PreviewMode.APP_STATE_STILL_CAPTURE || curMode == PreviewMode.APP_STATE_VIDEO_CAPTURE) {
                if (curMode == PreviewMode.APP_STATE_STILL_CAPTURE) {
                    MyToast.show(activity, R.string.stream_error_capturing);
                } else if (curMode == PreviewMode.APP_STATE_VIDEO_CAPTURE) {
                    MyToast.show(activity, R.string.stream_error_recording);
                }
                return;
            } else if (curMode == PreviewMode.APP_STATE_STILL_PREVIEW || curMode == PreviewMode.APP_STATE_VIDEO_PREVIEW) {
                previewView.stopMPreview(currentCamera);
                stopMediaStream();
                GlobalInfo.getInstance().getCurrentCamera().resetTimeLapseVideoSize();
                if (currentCamera.timeLapsePreviewMode == TimeLapseMode.TIME_LAPSE_MODE_VIDEO) {
                    previewStream.changePreviewMode(cameraPreviewStreamClint, ICatchPreviewMode.ICH_TIMELAPSE_VIDEO_PREVIEW_MODE);
                    changeCameraMode(APP_STATE_TIMELAPSE_VIDEO_PREVIEW, ICatchPreviewMode.ICH_TIMELAPSE_VIDEO_PREVIEW_MODE);
                } else if (currentCamera.timeLapsePreviewMode == TimeLapseMode.TIME_LAPSE_MODE_STILL) {
                    previewStream.changePreviewMode(cameraPreviewStreamClint, ICatchPreviewMode.ICH_TIMELAPSE_STILL_PREVIEW_MODE);
                    changeCameraMode(PreviewMode.APP_STATE_TIMELAPSE_STILL_PREVIEW, ICatchPreviewMode.ICH_TIMELAPSE_STILL_PREVIEW_MODE);
                }
            }
        }
    }

    private void startPhotoCapture() {
        previewView.setCaptureBtnEnability(false);
        previewView.setCaptureBtnBackgroundResource(R.drawable.still_capture_btn_off);
        if (cameraProperties.hasFuction(0xD7F0)) {
            PhotoCapture.getInstance().addOnStopPreviewListener(new PhotoCapture.OnStopPreviewListener() {
                @Override
                public void onStop() {
                    if (!cameraProperties.hasFuction(0xd704)) {
                        stopPreview();
                        if (stopMediaStream() == false) {
                            return;
                        }
                    }

                }
            });
            PhotoCapture.getInstance().startCapture();
        } else {
            stillCaptureStartBeep.start();
            if (!cameraProperties.hasFuction(0xd704)) {
                stopPreview();
                if (stopMediaStream() == false) {
                    return;
                }
            }
            CameraAction.getInstance().capturePhoto();
        }
    }

    public boolean destroyCamera() {
        return currentCamera.destroyCamera();
    }

    public void unregisterWifiSSReceiver() {
        if (wifiSSReceiver != null) {
            activity.unregisterReceiver(wifiSSReceiver);
        }
    }

    public void zoomIn() {
        if (curMode == PreviewMode.APP_STATE_STILL_CAPTURE || curMode == PreviewMode.APP_STATE_TIMELAPSE_STILL_CAPTURE) {
            return;
        }
        ZoomInOut.getInstance().zoomIn();
        previewView.updateZoomViewProgress(cameraProperties.getCurrentZoomRatio());
    }

    public void zoomOut() {
        if (curMode == PreviewMode.APP_STATE_STILL_CAPTURE || curMode == PreviewMode.APP_STATE_TIMELAPSE_STILL_CAPTURE) {
            return;
        }
        ZoomInOut.getInstance().zoomOut();
        previewView.updateZoomViewProgress(cameraProperties.getCurrentZoomRatio());
    }

    public void zoomBySeekBar() {
        ZoomInOut.getInstance().startZoomInOutThread(this);
        ZoomInOut.getInstance().addZoomCompletedListener(new ZoomInOut.ZoomCompletedListener() {
            @Override
            public void onCompleted(final int currentZoomRate) {
                previewHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        MyProgressDialog.closeProgressDialog();
                        previewView.updateZoomViewProgress(currentZoomRate);
                    }
                });
            }
        });
        MyProgressDialog.showProgressDialog(activity, null);
    }

    public void showZoomView() {
        if (curMode == PreviewMode.APP_STATE_STILL_CAPTURE
                || curMode == PreviewMode.APP_STATE_TIMELAPSE_STILL_CAPTURE
                || curMode == PreviewMode.APP_STATE_TIMELAPSE_VIDEO_CAPTURE
                || (cameraProperties.hasFuction(ICatchCameraProperty.ICH_CAP_DATE_STAMP) == true &&
                ICatchDateStamp.ICH_DATE_STAMP_OFF != cameraProperties.getCurrentDateStamp())) {
            return;
        }
        previewView.showZoomView();
    }

    public int getMaxZoomRate() {
        return previewView.getZoomViewMaxZoomRate();
    }

    public int getZoomViewProgress() {
        AppLog.d(TAG, "getZoomViewProgress value=" + previewView.getZoomViewProgress());
        return previewView.getZoomViewProgress();
    }

    public void showSettingDialog(int position) {
        if (settingMenuList != null && settingMenuList.size() > 0) {
            OptionSetting.getInstance().addSettingCompleteListener(new OnSettingCompleteListener() {
                @Override
                public void onOptionSettingComplete() {
                    settingMenuList = UIDisplaySource.getinstance().getList(currentSettingMenuMode, currentCamera);
                    settingListAdapter.notifyDataSetChanged();
                }

                @Override
                public void settingVideoSizeComplete() {
                    AppLog.d(TAG, "settingVideoSizeComplete curMode=" + curMode);
                    if (curMode == PreviewMode.APP_STATE_TIMELAPSE_VIDEO_PREVIEW) {
                        previewStream.changePreviewMode(cameraPreviewStreamClint, ICatchPreviewMode.ICH_TIMELAPSE_VIDEO_PREVIEW_MODE);
                    } else if (curMode == PreviewMode.APP_STATE_VIDEO_PREVIEW) {
                        previewStream.changePreviewMode(cameraPreviewStreamClint, ICatchPreviewMode.ICH_VIDEO_PREVIEW_MODE);
                    }
                    cameraProperties.refreshSupportedProperties();
                }

                @Override
                public void settingTimeLapseModeComplete(int timeLapseMode) {
                    if (timeLapseMode == TimeLapseMode.TIME_LAPSE_MODE_STILL) {
                        boolean ret = previewStream.changePreviewMode(cameraPreviewStreamClint, ICatchPreviewMode.ICH_TIMELAPSE_STILL_PREVIEW_MODE);
                        if (ret) {
                            curMode = PreviewMode.APP_STATE_TIMELAPSE_STILL_PREVIEW;
                        }
                    } else if (timeLapseMode == TimeLapseMode.TIME_LAPSE_MODE_VIDEO) {
                        boolean ret = previewStream.changePreviewMode(cameraPreviewStreamClint, ICatchPreviewMode.ICH_TIMELAPSE_VIDEO_PREVIEW_MODE);
                        if (ret) {
                            curMode = PreviewMode.APP_STATE_TIMELAPSE_VIDEO_PREVIEW;
                        }
                    }
                }
            });

            OptionSetting.getInstance().showSettingDialog(settingMenuList.get(position).name, activity);
        }
    }

    public void showPvModePopupWindow() {
        AppLog.d(TAG, "showPvModePopupWindow curMode=" + curMode);
        if (isYouTubeLiving) {
            MyToast.show(activity, R.string.stop_live_hint);
            return;
        }
        if (curMode == PreviewMode.APP_STATE_STILL_CAPTURE ||
                curMode == PreviewMode.APP_STATE_TIMELAPSE_VIDEO_CAPTURE ||
                curMode == PreviewMode.APP_STATE_TIMELAPSE_STILL_CAPTURE ||
                curMode == PreviewMode.APP_STATE_VIDEO_CAPTURE) {
            //IC-754 Begin modify 20161212 BY b.jiang
            if (curMode == PreviewMode.APP_STATE_STILL_CAPTURE || curMode == PreviewMode.APP_STATE_TIMELAPSE_STILL_CAPTURE) {
                MyToast.show(activity, R.string.stream_error_capturing);
            } else if (curMode == PreviewMode.APP_STATE_VIDEO_CAPTURE || curMode == PreviewMode.APP_STATE_TIMELAPSE_VIDEO_CAPTURE) {
                MyToast.show(activity, R.string.stream_error_recording);
            }
            //IC-754 End modify 20161212 BY b.jiang
            return;
        }
        previewView.showPopupWindow(curMode);
        if (cameraProperties.cameraModeSupport(ICatchMode.ICH_MODE_VIDEO)) {
            previewView.setVideoRadioBtnVisibility(View.VISIBLE);
        }
        if (cameraProperties.cameraModeSupport(ICatchMode.ICH_MODE_TIMELAPSE)) {
            previewView.setTimepLapseRadioBtnVisibility(View.VISIBLE);
        }
        if (curMode == PreviewMode.APP_STATE_STILL_PREVIEW) {
            previewView.setCaptureRadioBtnChecked(true);
        } else if (curMode == PreviewMode.APP_STATE_VIDEO_PREVIEW) {
            previewView.setVideoRadioBtnChecked(true);
        } else if (curMode == PreviewMode.APP_STATE_TIMELAPSE_STILL_PREVIEW || curMode == PreviewMode.APP_STATE_TIMELAPSE_VIDEO_PREVIEW) {
            previewView.setTimepLapseRadioChecked(true);
        }
    }

    public void refresh() {
        previewHandler.post(new Runnable() {
            @Override
            public void run() {
                initData();
                initPreview();
                addEvent();
            }
        });
    }

    public void stopStream() {
        AppLog.i(TAG, "start stopStream");
        stopMediaStream();
        delEvent();
        previewView.stopMPreview(currentCamera);
        destroyCamera();
    }

    public void stopConnectCheck() {
        GlobalInfo.getInstance().stopConnectCheck();
    }

    public void resetState() {
        cameraProperties.resetCameraProperties();
    }

    private class PreviewHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            //super.handleMessage(msg);
            Tristate ret = Tristate.FALSE;
            switch (msg.what) {
                case SDKEvent.EVENT_BATTERY_ELETRIC_CHANGED:
                    AppLog.i(TAG, "receive EVENT_BATTERY_ELETRIC_CHANGED power =" + msg.arg1);
                    //need to update battery eletric
                    int resid = ThumbnailOperation.getBatteryLevelIcon();
                    if (resid > 0) {
                        previewView.setBatteryIcon(resid);
                        if (resid == R.drawable.ic_battery_charging_green24dp) {
                            AppDialog.showLowBatteryWarning(activity);
                        }
                    }
                    break;
                case SDKEvent.EVENT_CONNECTION_FAILURE:
                    AppLog.i(TAG, "receive EVENT_CONNECTION_FAILURE");
//                    previewView.stopMPreview(currentCamera);
                    stopMediaStream();
                    delEvent();
//                    unregisterWifiSSReceiver();
                    previewView.stopMPreview(currentCamera);
                    destroyCamera();
                    break;

                case SDKEvent.EVENT_SD_CARD_FULL:
                    AppLog.i(TAG, "receive EVENT_SD_CARD_FULL");
                    sdkEvent.delEventListener(ICatchEventID.ICH_EVENT_SDCARD_FULL);  //IC-966
                    AppDialog.showDialogWarn(activity, R.string.dialog_card_full);
                    break;
                case SDKEvent.EVENT_VIDEO_OFF://only receive if fw request to stopMPreview video recording
                    AppLog.i(TAG, "receive EVENT_VIDEO_OFF:curMode=" + curMode);
                    if (curMode == PreviewMode.APP_STATE_VIDEO_CAPTURE || curMode == PreviewMode.APP_STATE_TIMELAPSE_VIDEO_CAPTURE) {
                        if (curMode == PreviewMode.APP_STATE_VIDEO_CAPTURE) {
                            curMode = PreviewMode.APP_STATE_VIDEO_PREVIEW;
                        } else {
                            curMode = PreviewMode.APP_STATE_TIMELAPSE_VIDEO_PREVIEW;
                        }
                        stopRecordingLapseTimeTimer();
                        String info = currentCamera.getVideoSize().getCurrentUiStringInPreview() + "/" + ConvertTools.secondsToMinuteOrHours(cameraProperties
                                .getRecordingRemainTime());
                        previewView.setPreviewInfo(info);
                    }
                    break;
                case SDKEvent.EVENT_VIDEO_ON:
                    AppLog.i(TAG, "receive EVENT_VIDEO_ON:curMode =" + curMode);
                    // video from camera when file exceeds 4g
                    if (curMode == PreviewMode.APP_STATE_VIDEO_PREVIEW) {
                        curMode = PreviewMode.APP_STATE_VIDEO_CAPTURE;
                        startVideoCaptureButtomChangeTimer();
                        startRecordingLapseTimeTimer(0);
                    } else if (curMode == APP_STATE_TIMELAPSE_VIDEO_PREVIEW) {
                        curMode = PreviewMode.APP_STATE_TIMELAPSE_VIDEO_CAPTURE;
                        startVideoCaptureButtomChangeTimer();
                        startRecordingLapseTimeTimer(0);
                    }
                    break;
                case SDKEvent.EVENT_CAPTURE_START:
                    AppLog.i(TAG, "receive EVENT_CAPTURE_START:curMode=" + curMode);
                    if (curMode != PreviewMode.APP_STATE_TIMELAPSE_STILL_CAPTURE) {
                        return;
                    }
                    continuousCaptureBeep.start();
                    MyToast.show(activity, R.string.capture_start);
                    break;
                case SDKEvent.EVENT_CAPTURE_COMPLETED:
                    AppLog.i(TAG, "receive EVENT_CAPTURE_COMPLETED:curMode=" + curMode);
                    if (curMode == PreviewMode.APP_STATE_STILL_CAPTURE) {
                        //ret = changeCameraMode(PreviewMode.APP_STATE_STILL_MODE, ICatchPreviewMode.ICH_STILL_PREVIEW_MODE);
                        if (!cameraProperties.hasFuction(0xd704)) {
                            ret = startMediaStream(ICatchPreviewMode.ICH_STILL_PREVIEW_MODE);
                            if (ret == Tristate.FALSE) {
                                return;
                            }
                            if (ret == Tristate.NORMAL) {
                                currentCamera.isStreaming = true;
                                previewView.startMPreview(currentCamera);
                            }
                        }

                        previewView.setCaptureBtnEnability(true);
                        previewView.setCaptureBtnBackgroundResource(R.drawable.still_capture_btn);
                        String info = currentCamera.getImageSize().getCurrentUiStringInPreview() + "/" + cameraProperties.getRemainImageNum();
                        previewView.setPreviewInfo(info);
                        curMode = PreviewMode.APP_STATE_STILL_PREVIEW;
                        return;
                    }
                    if (curMode == PreviewMode.APP_STATE_TIMELAPSE_STILL_CAPTURE) {
                        previewView.setCaptureBtnEnability(true);
                        previewView.setCaptureBtnBackgroundResource(R.drawable.still_capture_btn);
                        String info = currentCamera.getImageSize().getCurrentUiStringInPreview() + "/" + cameraProperties.getRemainImageNum();
                        previewView.setPreviewInfo(info);
                        MyToast.show(activity, R.string.capture_completed);
                    }

                    break;
                case SDKEvent.EVENT_FILE_ADDED:
                    AppLog.i(TAG, "EVENT_FILE_ADDED");
//                    if (curMode == PreviewMode.APP_STATE_TIMELAPSE_STILL_CAPTURE) {
//                        lapseTime = 0;
//                    }
                    break;

                case SDKEvent.EVENT_TIME_LAPSE_STOP:
                    AppLog.i(TAG, "receive EVENT_TIME_LAPSE_STOP:curMode=" + curMode);
                    if (curMode == PreviewMode.APP_STATE_TIMELAPSE_VIDEO_CAPTURE) {
                        //BSP-1419 收到 Event 時，就表示FW 已經自己停止了，APP 不需要再去執行 stopTimeLapse
//                        if (cameraAction.stopTimeLapse()) {
                        stopVideoCaptureButtomChangeTimer();
                        stopRecordingLapseTimeTimer();
                        String info = currentCamera.getImageSize().getCurrentUiStringInPreview() + "/" + cameraProperties.getRemainImageNum();
                        previewView.setPreviewInfo(info);
                        curMode = APP_STATE_TIMELAPSE_VIDEO_PREVIEW;
                    } else if (curMode == PreviewMode.APP_STATE_TIMELAPSE_STILL_CAPTURE) {
                        //BSP-1419 收到 Event 時，就表示FW 已經自己停止了，APP 不需要再去執行 stopTimeLapse
//                        if (cameraAction.stopTimeLapse()) {
                        stopRecordingLapseTimeTimer();
                        String info = currentCamera.getImageSize().getCurrentUiStringInPreview() + "/" + cameraProperties.getRemainImageNum();
                        previewView.setPreviewInfo(info);
                        curMode = PreviewMode.APP_STATE_TIMELAPSE_STILL_PREVIEW;
//                        }
                    }
                    break;
                case SDKEvent.EVENT_VIDEO_RECORDING_TIME:
                    AppLog.i(TAG, "receive EVENT_VIDEO_RECORDING_TIME");
                    startRecordingLapseTimeTimer(0);
                    break;
                case SDKEvent.EVENT_FILE_DOWNLOAD:
                    AppLog.i(TAG, "receive EVENT_FILE_DOWNLOAD");
                    AppLog.d(TAG, "receive EVENT_FILE_DOWNLOAD  msg.arg1 =" + msg.arg1);
                    if (AppInfo.autoDownloadAllow == false) {
                        AppLog.d(TAG, "GlobalInfo.autoDownload == false");
                        return;
                    }
                    final String path = StorageUtil.getDownloadPath(activity);
//                    if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
//                        path = Environment.getExternalStorageDirectory().toString() + AppInfo.AUTO_DOWNLOAD_PATH;
//                    } else {
//                        return;
//                    }
                    File directory = new File(path);

                    if (FileTools.getFileSize(directory) / 1024 >= AppInfo.autoDownloadSizeLimit * 1024 * 1024) {
                        AppLog.d(TAG, "can not download because size limit");
                        return;
                    }
                    final ICatchFile file = (ICatchFile) msg.obj;
                    FileOper.createDirectory(path);
                    new Thread() {
                        @Override
                        public void run() {
                            AppLog.d(TAG, "receive downloadFile file =" + file);
                            AppLog.d(TAG, "receive downloadFile path =" + path);
                            boolean retvalue = fileOperation.downloadFile(file, path + file.getFileName());
                            if (retvalue == true) {
                                previewHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        String path1 = path + file.getFileName();
                                        Bitmap bitmap = BitmapTools.getImageByPath(path1, BitmapTools.THUMBNAIL_WIDTH, BitmapTools.THUMBNAIL_HEIGHT);
                                        previewView.setAutoDownloadBitmap(bitmap);
                                    }
                                });
                            }
                            AppLog.d(TAG, "receive downloadFile retvalue =" + retvalue);
                        }
                    }.start();
                    break;
                case AppMessage.SETTING_OPTION_AUTO_DOWNLOAD:
                    AppLog.d(TAG, "receive SETTING_OPTION_AUTO_DOWNLOAD");
                    Boolean switcher = (Boolean) msg.obj;
                    if (switcher == true) {
                        // AutoDownLoad
                        AppInfo.autoDownloadAllow = true;
                        previewView.setAutoDownloadVisibility(View.VISIBLE);
                    } else {
                        AppInfo.autoDownloadAllow = false;
                        previewView.setAutoDownloadVisibility(View.GONE);
                    }
                    break;
                case SDKEvent.EVENT_SDCARD_INSERT:
                    AppLog.i(TAG, "receive EVENT_SDCARD_INSERT");
                    AppDialog.showDialogWarn(activity, R.string.dialog_card_inserted);
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    }

    public void addEvent() {
        sdkEvent.addEventListener(ICatchEventID.ICH_EVENT_SDCARD_FULL);
        sdkEvent.addEventListener(ICatchEventID.ICH_EVENT_BATTERY_LEVEL_CHANGED);
        sdkEvent.addEventListener(ICatchEventID.ICH_EVENT_VIDEO_OFF);
        sdkEvent.addEventListener(ICatchEventID.ICH_EVENT_VIDEO_ON);
        sdkEvent.addEventListener(ICatchEventID.ICH_EVENT_CAPTURE_START);
        sdkEvent.addEventListener(ICatchEventID.ICH_EVENT_CAPTURE_COMPLETE);
        sdkEvent.addEventListener(ICatchEventID.ICH_EVENT_FILE_ADDED);
//        sdkEvent.addEventListener(ICatchEventID.ICH_EVENT_CONNECTION_DISCONNECTED);
        sdkEvent.addEventListener(ICatchEventID.ICH_EVENT_TIMELAPSE_STOP);
        sdkEvent.addCustomizeEvent(0x5001);// video recording event
        sdkEvent.addEventListener(ICatchEventID.ICH_EVENT_FILE_DOWNLOAD);
        sdkEvent.addCustomizeEvent(0x3701);// Insert SD card event
    }

    public void delEvent() {
        sdkEvent.delEventListener(ICatchEventID.ICH_EVENT_SDCARD_FULL);
        sdkEvent.delEventListener(ICatchEventID.ICH_EVENT_BATTERY_LEVEL_CHANGED);
        sdkEvent.delEventListener(ICatchEventID.ICH_EVENT_CAPTURE_COMPLETE);
        sdkEvent.delEventListener(ICatchEventID.ICH_EVENT_CAPTURE_START);
        sdkEvent.delEventListener(ICatchEventID.ICH_EVENT_VIDEO_OFF);
        sdkEvent.delEventListener(ICatchEventID.ICH_EVENT_FILE_ADDED);
        sdkEvent.delEventListener(ICatchEventID.ICH_EVENT_VIDEO_ON);
//        sdkEvent.delEventListener(ICatchEventID.ICH_EVENT_CONNECTION_DISCONNECTED);
        sdkEvent.delEventListener(ICatchEventID.ICH_EVENT_TIMELAPSE_STOP);
        sdkEvent.delCustomizeEventListener(0x5001);
        sdkEvent.delEventListener(ICatchEventID.ICH_EVENT_FILE_DOWNLOAD);
        sdkEvent.delCustomizeEventListener(0x3701);// Insert SD card event
    }

    public void stopPreview() {
        previewView.stopMPreview(currentCamera);
    }

    public void loadSettingMenuList() {
        AppLog.i(TAG, "setupBtn is clicked:allowClickButtoms=" + allowClickButtoms);
        if (isYouTubeLiving) {
            MyToast.show(activity, R.string.stop_live_hint);
            return;
        }
        if (allowClickButtoms == false) {
            return;
        }
        allowClickButtoms = false;
        if (curMode == PreviewMode.APP_STATE_VIDEO_CAPTURE) {
            AppToast.show(activity, R.string.stream_error_recording, Toast.LENGTH_SHORT);
        } else if (curMode == PreviewMode.APP_STATE_STILL_CAPTURE) {
            AppToast.show(activity, R.string.stream_error_capturing, Toast.LENGTH_SHORT);
        } else if (curMode == PreviewMode.APP_STATE_STILL_PREVIEW) {
            previewView.setSetupMainMenuVisibility(View.VISIBLE);
            currentSettingMenuMode = UIDisplaySource.CAPTURE_SETTING_MENU;
            if (settingMenuList != null) {
                settingMenuList.clear();
            }
            if (settingListAdapter != null) {
                settingListAdapter.notifyDataSetChanged();
            }
            previewView.setSettingBtnVisible(false);
            previewView.setBackBtnVisibility(true);
            previewView.setActionBarTitle(R.string.title_setting);
            stopPreview();
            MyProgressDialog.showProgressDialog(activity, R.string.action_processing);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    previewHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            settingMenuList = UIDisplaySource.getinstance().getList(UIDisplaySource.CAPTURE_SETTING_MENU, currentCamera);
                            settingListAdapter = new SettingListAdapter(activity, settingMenuList, previewHandler, new SettingListAdapter.OnItemClickListener
                                    () {
                                @Override
                                public void onItemClick(int position) {
                                    showSettingDialog(position);
                                }
                            });
                            previewView.setSettingMenuListAdapter(settingListAdapter);
                            MyProgressDialog.closeProgressDialog();
                        }
                    });
                }
            }).start();

        } else if (curMode == PreviewMode.APP_STATE_VIDEO_PREVIEW) {
            previewView.setSetupMainMenuVisibility(View.VISIBLE);
            currentSettingMenuMode = UIDisplaySource.VIDEO_SETTING_MENU;
            if (settingMenuList != null) {
                settingMenuList.clear();
            }
            if (settingListAdapter != null) {
                settingListAdapter.notifyDataSetChanged();
            }
            previewView.setSettingBtnVisible(false);
            previewView.setBackBtnVisibility(true);
            previewView.setActionBarTitle(R.string.title_setting);
            MyProgressDialog.showProgressDialog(activity, R.string.action_processing);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    previewHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            stopPreview();
                            stopMediaStream();
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            settingMenuList = UIDisplaySource.getinstance().getList(UIDisplaySource.VIDEO_SETTING_MENU, currentCamera);
                            settingListAdapter = new SettingListAdapter(activity, settingMenuList, previewHandler, new SettingListAdapter.OnItemClickListener
                                    () {
                                @Override
                                public void onItemClick(int position) {
                                    showSettingDialog(position);
                                }
                            });
                            previewView.setSettingMenuListAdapter(settingListAdapter);
                            MyProgressDialog.closeProgressDialog();
                        }
                    }, 200);
                }
            }).start();
        } else if (curMode == PreviewMode.APP_STATE_TIMELAPSE_STILL_PREVIEW || curMode == APP_STATE_TIMELAPSE_VIDEO_PREVIEW) {
            previewView.setSetupMainMenuVisibility(View.VISIBLE);
            currentSettingMenuMode = UIDisplaySource.TIMELAPSE_SETTING_MENU;
            if (settingMenuList != null) {
                settingMenuList.clear();
            }
            if (settingListAdapter != null) {
                settingListAdapter.notifyDataSetChanged();
            }
            previewView.setSettingBtnVisible(false);
            previewView.setBackBtnVisibility(true);
            previewView.setActionBarTitle(R.string.title_setting);
            MyProgressDialog.showProgressDialog(activity, R.string.action_processing);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    previewHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            stopPreview();
                            stopMediaStream();
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            settingMenuList = UIDisplaySource.getinstance().getList(UIDisplaySource.TIMELAPSE_SETTING_MENU, currentCamera);
                            settingListAdapter = new SettingListAdapter(activity, settingMenuList, previewHandler, new SettingListAdapter.OnItemClickListener
                                    () {
                                @Override
                                public void onItemClick(int position) {
                                    showSettingDialog(position);
                                }
                            });
                            previewView.setSettingMenuListAdapter(settingListAdapter);
                            MyProgressDialog.closeProgressDialog();
                        }
                    }, 200);
                }
            }).start();
        } else if (curMode == PreviewMode.APP_STATE_TIMELAPSE_VIDEO_CAPTURE) {
            AppToast.show(activity, R.string.stream_error_recording, Toast.LENGTH_SHORT);
        } else if (curMode == PreviewMode.APP_STATE_TIMELAPSE_STILL_CAPTURE) {
            AppToast.show(activity, R.string.stream_error_capturing, Toast.LENGTH_SHORT);
        }
        allowClickButtoms = true;
    }

    @Override
    public void finishActivity() {
        if (previewView.getSetupMainMenuVisibility() == View.VISIBLE) {
            AppLog.i(TAG, "onKeyDown curMode==" + curMode);
            previewView.setSetupMainMenuVisibility(View.GONE);
            previewView.setSettingBtnVisible(true);
            previewView.setBackBtnVisibility(false);
            previewView.setActionBarTitle(R.string.title_preview);
            if (curMode == PreviewMode.APP_STATE_VIDEO_PREVIEW) {
                AppLog.i(TAG, "onKeyDown curMode == APP_STATE_VIDEO_PREVIEW");
//                changePreviewMode(curMode);
                changeCameraMode(curMode, ICatchPreviewMode.ICH_VIDEO_PREVIEW_MODE);
                // normal state, app show preview
            } else if (curMode == APP_STATE_TIMELAPSE_VIDEO_PREVIEW) {
//                changePreviewMode(curMode);
                AppLog.i(TAG, "onKeyDown curMode == APP_STATE_TIMELAPSE_PREVIEW_VIDEO");
                currentCamera.timeLapsePreviewMode = TimeLapseMode.TIME_LAPSE_MODE_VIDEO;
                changeCameraMode(curMode, ICatchPreviewMode.ICH_TIMELAPSE_VIDEO_PREVIEW_MODE);
                // normal state, app show preview
            } else if (curMode == PreviewMode.APP_STATE_TIMELAPSE_STILL_PREVIEW) {
//                changePreviewMode(curMode);
                AppLog.i(TAG, "onKeyDown curMode == APP_STATE_TIMELAPSE_PREVIEW_STILL");
                currentCamera.timeLapsePreviewMode = TimeLapseMode.TIME_LAPSE_MODE_STILL;
                changeCameraMode(curMode, ICatchPreviewMode.ICH_TIMELAPSE_STILL_PREVIEW_MODE);
                // normal state, app show preview
            } else {
                if (supportStreaming) {
                    previewView.startMPreview(currentCamera);
                }
                createUIByMode(curMode);
            }
            AppLog.i(TAG, "finishActivity start showDialogWarn: Only for iCatch SBC");
            AppDialog.showDialogWarn(activity, R.string.text_preview_hint_info);
        } else {
            if (isYouTubeLiving) {
                MyToast.show(activity, R.string.stop_live_hint);
                return;
            }
            super.finishActivity();
        }
    }

    @Override
    public void redirectToAnotherActivity(final Context context, final Class<?> cls) {
        AppLog.i(TAG, "pbBtn is clicked curMode=" + curMode);
        if (isYouTubeLiving) {
            MyToast.show(activity, R.string.stop_live_hint);
            return;
        }
        if (allowClickButtoms == false) {
            AppLog.i(TAG, "do not allow to response button clicking");
            return;
        }
        allowClickButtoms = false;
        if (cameraProperties.isSDCardExist() == false) {
            AppDialog.showDialogWarn(activity, R.string.dialog_card_lose);
            allowClickButtoms = true;
            return;
        }
        AppLog.i(TAG, "curMode =" + curMode);
        if (curMode == PreviewMode.APP_STATE_STILL_PREVIEW || curMode == PreviewMode.APP_STATE_VIDEO_PREVIEW || curMode == APP_STATE_TIMELAPSE_VIDEO_PREVIEW
                || curMode == PreviewMode.APP_STATE_TIMELAPSE_STILL_PREVIEW) {
            stopPreview();
            if (supportStreaming) {
                if (stopMediaStream() == false) {
                    AppLog.i("[Error] -- Main: ", "failed to stopMediaStream");
                    allowClickButtoms = true;
                    return;
                }
            }
            delEvent();
            allowClickButtoms = true;
            needShowSBCHint = true;
            //BSP-1209
            MyProgressDialog.showProgressDialog(context, R.string.action_processing);
            previewHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    MyProgressDialog.closeProgressDialog();
                    Intent intent = new Intent();
                    AppLog.i(TAG, "intent:start PbMainActivity.class");
                    intent.setClass(context, cls);
                    context.startActivity(intent);
                    AppLog.i(TAG, "intent:end start PbMainActivity.class");
                }
            }, 1000);
//            //BSP-1209

            //ICOM-3812
//            Intent intent = new Intent();
//            AppLog.i(TAG, "intent:start PbMainActivity.class");
//            intent.setClass(context, cls);
//            context.startActivity(intent);
//            AppLog.i(TAG, "intent:end start PbMainActivity.class");
            return;
        } else if (curMode == PreviewMode.APP_STATE_VIDEO_CAPTURE || curMode == PreviewMode.APP_STATE_TIMELAPSE_VIDEO_CAPTURE) {
            MyToast.show(activity, R.string.stream_error_recording);
        } else if (curMode == PreviewMode.APP_STATE_STILL_CAPTURE || curMode == PreviewMode.APP_STATE_TIMELAPSE_STILL_CAPTURE) {
            MyToast.show(activity, R.string.stream_error_capturing);
        }
        allowClickButtoms = true;
        AppLog.i(TAG, "end processing for responsing pbBtn clicking");
    }

    public void showVideoSizeOptionDialog(final Context context, final OnSettingCompleteListener settingCompleteListener) {
        // TODO Auto-generated method stub
        final MyCamera currCamera = GlobalInfo.getInstance().getCurrentCamera();
        CharSequence title = context.getResources().getString(R.string.stream_set_res_vid);
        final String[] videoSizeUIString = currCamera.getVideoSize().getValueArrayString();
        final List<String> videoSizeValueString = currCamera.getVideoSize().getValueList();
        if (videoSizeUIString == null) {
            AppLog.e(TAG, "videoSizeUIString == null");
            return;
        }
        int length = videoSizeUIString.length;
        int curIdx = 0;
        for (int i = 0; i < length; i++) {
            if (videoSizeUIString[i].equals(currCamera.getVideoSize().getCurrentUiStringInSetting())) {
                curIdx = i;
            }
        }
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                final String value = videoSizeValueString.get(arg1);
                AppLog.d(TAG, " begin stopMPreview preview!!!!!");
                stopPreview();

                if (cameraProperties.getVideoSizeFlow() == 1) {
                    stopMediaStream();

                }
                boolean ret = cameraProperties.setVideoSize(value);
                if (!ret) {
                    ProgressDialog progressDialog = new ProgressDialog(activity);
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progressDialog.setMessage(activity.getResources().getString(R.string.text_operation_failed));
                    progressDialog.setCancelable(false);
                    progressDialog.show();// 提示设备发生异常;
                    // return;
                }
                if (cameraProperties.hasFuction(ICatchCameraProperty.ICH_CAP_SLOW_MOTION)
                        && cameraProperties.getCurrentSlowMotion() == SlowMotion.SLOW_MOTION_ON && curMode == PreviewMode.APP_STATE_VIDEO_PREVIEW) {
                    previewView.setSlowMotionVisibility(View.VISIBLE);
                } else {
                    previewView.setSlowMotionVisibility(View.GONE);
                }
//                currCamera.getVideoSize().setValue(value);
                arg0.dismiss();
                settingCompleteListener.onOptionSettingComplete();
            }
        };
        AlertDialog.Builder optionDialog = new AlertDialog.Builder(GlobalInfo.getInstance().getCurrentApp());
        optionDialog.setTitle(title).setSingleChoiceItems(videoSizeUIString, curIdx, listener).create();
        optionDialog.setCancelable(true);
        optionDialog.show();
    }

    private class WifiSSReceiver extends BroadcastReceiver {
        private WifiManager wifi;

        public WifiSSReceiver() {
            super();

            wifi = (WifiManager) activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            changeWifiStatusIcon();
        }

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            changeWifiStatusIcon();
        }

        private void changeWifiStatusIcon() {
            WifiInfo info = wifi.getConnectionInfo();
            String ssid = info.getSSID().replaceAll("\"", "");
            if (info.getBSSID() != null && ssid != null && ssid.equals(GlobalInfo.getInstance().getSsid())) {
                int strength = WifiManager.calculateSignalLevel(info.getRssi(), 5);

                AppLog.d(TAG, "change Wifi Status：" + strength);
                switch (strength) {
                    case 0:
                        previewView.setWifiIcon(R.drawable.ic_signal_wifi_0_bar_green_24dp);
                        break;
                    case 1:
                        previewView.setWifiIcon(R.drawable.ic_signal_wifi_1_bar_green_24dp);
                        break;
                    case 2:
                        previewView.setWifiIcon(R.drawable.ic_signal_wifi_2_bar_green_24dp);
                        break;
                    case 3:
                        previewView.setWifiIcon(R.drawable.ic_signal_wifi_3_bar_green_24dp);
                        break;
                    case 4:
                        previewView.setWifiIcon(R.drawable.ic_signal_wifi_4_bar_green_24dp);
                        break;
                    default:
                        break;
                }
            }
        }
    }


    public void startOrStopYouTubeLive() {
        if (curMode == PreviewMode.APP_STATE_STILL_CAPTURE || curMode == PreviewMode.APP_STATE_TIMELAPSE_STILL_CAPTURE) {
            MyToast.show(activity, R.string.capturing_cannot_live);
            return;
        }
        if (curMode == PreviewMode.APP_STATE_VIDEO_CAPTURE || curMode == PreviewMode.APP_STATE_TIMELAPSE_VIDEO_CAPTURE) {
            MyToast.show(activity, R.string.recording_cannot_live);
            return;
        }
        if (!isYouTubeLiving) {
            final String directoryPath = activity.getExternalCacheDir() + AppInfo.PROPERTY_CFG_DIRECTORY_PATH;
            final String fileName = AppInfo.FILE_GOOGLE_TOKEN;
            final GoogleToken googleToken = (GoogleToken) FileTools.readSerializable(directoryPath + fileName);
            AppLog.d(TAG, "refreshAccessToken googleToken=" + googleToken);

//            final GoogleToken googleToken = null;
            if (googleToken != null && googleToken.getRefreshToken() != null && googleToken.getRefreshToken() != "") {
                final String refreshToken = googleToken.getRefreshToken();
                MyToast.show(activity, "readSerializable RefreshToken=" + refreshToken);
                MyProgressDialog.showProgressDialog(activity, R.string.action_processing);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String accessToken = null;
                        try {
                            accessToken = GoogleAuthTool.refreshAccessToken(activity, refreshToken);
                        } catch (IOException e) {
                            previewHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    MyProgressDialog.closeProgressDialog();
                                    MyToast.show(activity, "refreshAccessToken IOException");
                                }
                            });
                            e.printStackTrace();
                        }
                        if (accessToken != null) {
                            AppLog.d(TAG, "refreshAccessToken accessToken=" + accessToken);
                            googleToken.setCurrentAccessToken(accessToken);
                            FileTools.saveSerializable(directoryPath + fileName, googleToken);
                            previewHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    MyProgressDialog.closeProgressDialog();
                                    MyToast.show(activity, "start live");
                                    startYoutubeLive();
                                }
                            }, 1000);
                        } else {
                            previewHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    MyProgressDialog.closeProgressDialog();
                                    MyToast.show(activity, "Failed to get accessToken , Please enter the google account click disconnect and re-login!");
                                }
                            });
                        }
                    }
                }).start();

            } else {
                MyToast.show(activity, "You are not logged in, please login to google account!");
            }

        } else {
            AppLog.d(TAG, "stop push publish...");
            MyProgressDialog.showProgressDialog(activity, R.string.action_processing);
            stopYoutubeLive();
        }
    }

    public void startYoutubeLive() {
        String directoryPath = activity.getExternalCacheDir() + AppInfo.PROPERTY_CFG_DIRECTORY_PATH;
        String fileName = AppInfo.FILE_GOOGLE_TOKEN;
        GoogleToken googleToken = (GoogleToken) FileTools.readSerializable(directoryPath + fileName);
        String accessToken = googleToken.getAccessToken();
        String refreshToken = googleToken.getRefreshToken();
//        String accessToken  = AppInfo.accessToken;
//        String refreshToken = AppInfo.refreshToken;
        final GoogleClientSecrets clientSecrets = YoutubeCredential.readClientSecrets(activity);
        AppLog.d(TAG, "readSerializable accessToken=" + accessToken);
        AppLog.d(TAG, "readSerializable refreshToken=" + refreshToken);
        if (accessToken == null) {
            MyToast.show(activity, "Failed to Youtube live,OAuth2AccessToken is null!");
            return;
        }
        previewHandler.post(new Runnable() {
            @Override
            public void run() {
                MyProgressDialog.showProgressDialog(activity, R.string.action_processing);
            }
        });
        final Credential credential;
        try {
            credential = YoutubeCredential.authorize(clientSecrets, accessToken, refreshToken);
        } catch (IOException e) {
            AppLog.d(TAG, "authorize IOException");
            e.printStackTrace();
            return;
        }
        AppLog.d(TAG, "success credential=" + credential);
        new Thread(new Runnable() {
            @Override
            public void run() {
                final String pushUrl = CreateBroadcast.createLive(activity, credential);
                AppLog.d(TAG, "push url..." + pushUrl);
                if (pushUrl == null) {
                    previewHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            MyProgressDialog.closeProgressDialog();
                            previewView.setYouTubeBtnTxv(R.string.start_youtube_live);
                            MyToast.show(activity, "Failed to Youtube live,pushUrl is null!");
                        }
                    });
                    return;
                }
                final boolean ret = previewStream.startPublishStreaming(cameraPreviewStreamClint, pushUrl);
                if (ret == false) {
                    previewHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            MyProgressDialog.closeProgressDialog();
                            previewView.setYouTubeBtnTxv(R.string.start_youtube_live);
                            MyToast.show(activity, "Failed to start publish streaming!");
                        }
                    });
                    return;
                }
                final String shareUrl = CreateBroadcast.startLive();
                AppLog.d(TAG, "shareUrl =" + shareUrl);
                if (shareUrl == null) {
                    previewStream.stopPublishStreaming(cameraPreviewStreamClint);
                    previewHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            MyProgressDialog.closeProgressDialog();
                            previewView.setYouTubeBtnTxv(R.string.start_youtube_live);
                            MyToast.show(activity, "Failed to YouTube live,shareUrl is null!");
                        }
                    });
                    return;
                } else {
                    previewHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            MyProgressDialog.closeProgressDialog();
                            isYouTubeLiving = true;
                            previewView.setYouTubeBtnTxv(R.string.end_youtube_live);
                            showSharedUrlDialog(activity, shareUrl);
                        }
                    });
                }
            }
        }).start();
    }

    private void stopYoutubeLive() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final boolean ret = previewStream.stopPublishStreaming(cameraPreviewStreamClint);
                try {
                    CreateBroadcast.stopLive();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                previewHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        MyProgressDialog.closeProgressDialog();
                        if (ret == false) {
                            MyToast.show(activity, "Failed to stop living publish!");
                        } else {
                            MyToast.show(activity, "Succed to stop living publish!");
                        }
                        isYouTubeLiving = false;
                        previewView.setYouTubeBtnTxv(R.string.start_youtube_live);
                    }
                });
            }
        }).start();
    }

    public void gotoGoogleAccountManagement() {
        if (isYouTubeLiving) {
            MyToast.show(activity, R.string.stop_live_hint);
        } else {
            if (curMode == PreviewMode.APP_STATE_STILL_PREVIEW || curMode == PreviewMode.APP_STATE_VIDEO_PREVIEW || curMode == APP_STATE_TIMELAPSE_VIDEO_PREVIEW
                    || curMode == PreviewMode.APP_STATE_TIMELAPSE_STILL_PREVIEW) {
                stopPreview();
                if (supportStreaming) {
                    if (stopMediaStream() == false) {
                        AppLog.i("[Error] -- Main: ", "failed to stopMediaStream");
                        return;
                    }
                }

                delEvent();
                Intent intent = new Intent();
                intent.setClass(activity, LoginGoogleActivity.class);
                activity.startActivity(intent);
                AppLog.i(TAG, "intent:end start PbMainActivity.class");
                return;
            } else if (curMode == PreviewMode.APP_STATE_VIDEO_CAPTURE || curMode == PreviewMode.APP_STATE_TIMELAPSE_VIDEO_CAPTURE) {
                MyToast.show(activity, R.string.stream_error_recording);
            } else if (curMode == PreviewMode.APP_STATE_STILL_CAPTURE || curMode == PreviewMode.APP_STATE_TIMELAPSE_STILL_CAPTURE) {
                MyToast.show(activity, R.string.stream_error_capturing);
            }
//            destroyPreview();

        }
    }

    public void showSharedUrlDialog(final Context context, final String shareUrl) {
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.live_shared_url, null);
        final EditText resetTxv = (EditText) view.findViewById(R.id.shared_url);
        final ImageView qrcodeImage = (ImageView) view.findViewById(R.id.shared_url_qrcode);
        Bitmap bitmap = QRCode.createQRCodeWithLogo(shareUrl, QRCode.WIDTH, BitmapFactory.decodeResource(context.getResources(), R.drawable.logo_wificamera));
        qrcodeImage.setImageBitmap(bitmap);

        resetTxv.setText(shareUrl);
        builder.setTitle("Success, share url is:");
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.setView(view);
        builder.setCancelable(false);
        builder.create().show();
    }

}
