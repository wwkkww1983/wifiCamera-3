package com.icatch.sbcapp.View.Activity;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.icatch.sbcapp.Adapter.SettingListAdapter;
import com.icatch.sbcapp.ExtendComponent.MPreview;
import com.icatch.sbcapp.ExtendComponent.ZoomView;
import com.icatch.sbcapp.Listener.OnDecodeTimeListener;
import com.icatch.sbcapp.Log.AppLog;
import com.icatch.sbcapp.Mode.PreviewLaunchMode;
import com.icatch.sbcapp.Mode.PreviewMode;
import com.icatch.sbcapp.MyCamera.MyCamera;
import com.icatch.sbcapp.Presenter.PreviewPresenter;
import com.icatch.sbcapp.R;
import com.icatch.sbcapp.SystemInfo.SystemInfo;
import com.icatch.sbcapp.View.Interface.PreviewView;

public class PreviewActivity extends BaseActivity implements View.OnClickListener, PreviewView {

    private static final String TAG = "PreviewActivity";
    private PreviewPresenter presenter;
    private MPreview mPreview;
    private ImageView pbBtn;
    private ImageView captureBtn;
    private ImageView wbStatus;
    private ImageView burstStatus;
    private ImageView wifiStatus;
    private ImageView batteryStatus;
    private ImageView timelapseMode;
    private ImageView slowMotion;
    private ImageView carMode;
    private TextView recordingTime;
    private ImageView autoDownloadImagview;
    private TextView delayCaptureText;
    private RelativeLayout delayCaptureLayout;
    private TextView curPreviewInfoTxv;
    private ZoomView zoomView;
    private RelativeLayout setupMainMenu;
    private ListView mainMenuList;
    private MenuItem settingMenu;
    private ActionBar actionBar;
    private TextView noSupportPreviewTxv;
    private PopupWindow pvModePopupWindow;
    private RadioButton captureRadioBtn;
    private RadioButton videoRadioBtn;
    private RadioButton timepLapseRadioBtn;
    private ImageView pvModeBtn;
    private View contentView;
    private Toolbar toolbar;
    private TextView decodeTimeTxv;
    private LinearLayout decodeTimeLayout;
    private Handler handler;
    private TextView decodeInfo;
    private Button youtubeLiveBtn;
    private Button googleAccountBtn;
    private LinearLayout youTubeLiveLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppLog.d(TAG, "1122 onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(R.string.title_preview);
//        actionBar.setWindowTitle("ddddddd");
//        actionBar.setWindowTitle("teet");
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(false);

        presenter = new PreviewPresenter(PreviewActivity.this);
        presenter.setView(this);

        youTubeLiveLayout = (LinearLayout) findViewById(R.id.youtube_live_layout);
        youtubeLiveBtn = (Button) findViewById(R.id.youtube_live_btn);
        googleAccountBtn = (Button) findViewById(R.id.google_account_btn);

        decodeTimeTxv = (TextView) findViewById(R.id.decodeTimeTxv);
        decodeTimeLayout = (LinearLayout) findViewById(R.id.decode_time_layout);
        decodeInfo = (TextView) findViewById(R.id.decode_info);
        mPreview = (MPreview) findViewById(R.id.m_preview);
        mPreview.setOnClickListener(this);
        pbBtn = (ImageView) findViewById(R.id.multi_pb);
        pbBtn.setOnClickListener(this);
        captureBtn = (ImageView) findViewById(R.id.doCapture);
        captureBtn.setOnClickListener(this);

        wbStatus = (ImageView) findViewById(R.id.wb_status);
        burstStatus = (ImageView) findViewById(R.id.burst_status);
        wifiStatus = (ImageView) findViewById(R.id.wifi_status);
        batteryStatus = (ImageView) findViewById(R.id.battery_status);
        timelapseMode = (ImageView) findViewById(R.id.timelapse_mode);
        slowMotion = (ImageView) findViewById(R.id.slow_motion);
        carMode = (ImageView) findViewById(R.id.car_mode);
        recordingTime = (TextView) findViewById(R.id.recording_time);
        autoDownloadImagview = (ImageView) findViewById(R.id.auto_download_imageview);
        delayCaptureText = (TextView) findViewById(R.id.delay_capture_text);
        delayCaptureLayout = (RelativeLayout) findViewById(R.id.delay_capture_layout);
        curPreviewInfoTxv = (TextView) findViewById(R.id.preview_info_txv);
        setupMainMenu = (RelativeLayout) findViewById(R.id.setupMainMenu);
        mainMenuList = (ListView) findViewById(R.id.setup_menu_listView);
        noSupportPreviewTxv = (TextView) findViewById(R.id.not_support_preview_txv);
        pvModeBtn = (ImageView) findViewById(R.id.pv_mode);

        contentView = LayoutInflater.from(PreviewActivity.this).inflate(R.layout.camer_mode_switch_layout, null);
        pvModePopupWindow = new PopupWindow(contentView,
                GridLayout.LayoutParams.WRAP_CONTENT, GridLayout.LayoutParams.WRAP_CONTENT, true);
        //ICOM-4096 begin add by b.jiang 20170112
        pvModePopupWindow.setBackgroundDrawable(new BitmapDrawable());
        pvModePopupWindow.setFocusable(true);
        pvModePopupWindow.setOutsideTouchable(true);
        //ICOM-4096 end add by b.jiang 20170112
        captureRadioBtn = (RadioButton) contentView.findViewById(R.id.capture_radio);
        videoRadioBtn = (RadioButton) contentView.findViewById(R.id.video_radio);
        timepLapseRadioBtn = (RadioButton) contentView.findViewById(R.id.timeLapse_radio);
        contentView.setFocusable(true);
        contentView.setFocusableInTouchMode(true);
        contentView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                AppLog.d(TAG, "contentView onKey");
                switch (keyCode) {
                    case KeyEvent.KEYCODE_BACK:
                        AppLog.d("AppStart", "contentView back");
                        if (pvModePopupWindow != null && pvModePopupWindow.isShowing()) {
                            AppLog.d("AppStart", "dismiss pvModePopupWindow");
                            pvModePopupWindow.dismiss();
                        }
                        break;
                }
                return true;
            }
        });


        googleAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.gotoGoogleAccountManagement();
            }
        });

        youtubeLiveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.startOrStopYouTubeLive();
            }
        });

        zoomView = (ZoomView) findViewById(R.id.zoom_view);
        zoomView.setZoomInOnclickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.zoomIn();
            }
        });

        zoomView.setZoomOutOnclickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.zoomOut();
            }
        });

        zoomView.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                presenter.zoomBySeekBar();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                presenter.zoomBySeekBar();
            }
        });

        pvModeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.showPvModePopupWindow();
            }
        });

        captureRadioBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.changePreviewMode(PreviewMode.APP_STATE_STILL_MODE);
            }
        });

        videoRadioBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.changePreviewMode(PreviewMode.APP_STATE_VIDEO_MODE);
            }
        });
        timepLapseRadioBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.changePreviewMode(PreviewMode.APP_STATE_TIMELAPSE_MODE);
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
        presenter.initUI();
    }

    @Override
    protected void onResume() {
        AppLog.d(TAG, "1122 onResume");
        super.onResume();
        presenter.initData();
        presenter.submitAppInfo();
        presenter.initPreview();
        presenter.initStatus();
        presenter.addEvent();
        handler = new Handler();
    }

    @Override
    protected void onStop() {
        AppLog.d(TAG, "1122 onStop");
        super.onStop();
        presenter.isAppBackground();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_HOME:
                Log.d("AppStart", "home");
                break;
            case KeyEvent.KEYCODE_BACK:
                AppLog.d("AppStart", "back");
                if (pvModePopupWindow != null && pvModePopupWindow.isShowing()) {
                    AppLog.d("AppStart", "dismiss pvModePopupWindow");
                    pvModePopupWindow.dismiss();
                } else {
                    presenter.finishActivity();
                }
                break;
            default:
                return super.onKeyDown(keyCode, event);
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "1122 onDestroy");
        super.onDestroy();
        presenter.removeActivity();
        presenter.stopPreview();
        presenter.delEvent();
        presenter.stopMediaStream();
        presenter.destroyCamera();
        presenter.unregisterWifiSSReceiver();
        presenter.stopConnectCheck();
        //IC-758 Begin ADD by b.jiang 20161227
        presenter.resetState();
        //IC-758 End ADD by b.jiang 20161227
    }

    @Override
    protected boolean onPrepareOptionsPanel(View view, Menu menu) {
        return super.onPrepareOptionsPanel(view, menu);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_preview, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up ImageButton, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == android.R.id.home) {
            AppLog.d(TAG, "id == android.R.id.home");
            if (pvModePopupWindow != null && pvModePopupWindow.isShowing()) {
                pvModePopupWindow.dismiss();
            } else {
                presenter.finishActivity();
            }
//            presenter.finishActivity();
        } else if (id == R.id.action_setting) {
            settingMenu = item;
            presenter.loadSettingMenuList();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        AppLog.i(TAG, "click the v.getId() =" + v.getId());
        switch (v.getId()) {
            case R.id.multi_pb:
                AppLog.i(TAG, "click the multi_pb");
                presenter.redirectToAnotherActivity(PreviewActivity.this, MultiPbActivity.class);
                break;
            case R.id.doCapture:
                AppLog.i(TAG, "click the doCapture");
                presenter.startOrStopCapture();
                break;
            case R.id.m_preview:
                AppLog.i(TAG, "click the m_preview");
                presenter.showZoomView();
                break;
            default:
                break;
        }
    }

    @Override
    public void setmPreviewVisibility(int visibility) {
        mPreview.setVisibility(visibility);
    }

    @Override
    public void setWbStatusVisibility(int visibility) {
        wbStatus.setVisibility(visibility);
    }

    @Override
    public void setBurstStatusVisibility(int visibility) {
        burstStatus.setVisibility(visibility);
    }

    @Override
    public void setWifiStatusVisibility(int visibility) {
        wifiStatus.setVisibility(visibility);
    }

    @Override
    public void setWifiIcon(int drawableId) {
        wifiStatus.setBackgroundResource(drawableId);
    }

    @Override
    public void setBatteryStatusVisibility(int visibility) {
        batteryStatus.setVisibility(visibility);
    }

    @Override
    public void setBatteryIcon(int drawableId) {
        batteryStatus.setBackgroundResource(drawableId);
    }

    @Override
    public void settimeLapseModeVisibility(int visibility) {
        timelapseMode.setVisibility(visibility);
    }

    @Override
    public void settimeLapseModeIcon(int drawableId) {
        timelapseMode.setBackgroundResource(drawableId);
    }

    @Override
    public void setSlowMotionVisibility(int visibility) {
        slowMotion.setVisibility(visibility);
    }

    @Override
    public void setCarModeVisibility(int visibility) {
        carMode.setVisibility(visibility);
    }

    @Override
    public void setRecordingTimeVisibility(int visibility) {
        recordingTime.setVisibility(visibility);
    }

    @Override
    public void setAutoDownloadVisibility(int visibility) {
        autoDownloadImagview.setVisibility(visibility);
    }

    @Override
    public void setCaptureBtnBackgroundResource(int id) {
        captureBtn.setImageResource(id);
    }

    @Override
    public void setRecordingTime(String laspeTime) {
        recordingTime.setText(laspeTime);
    }

    @Override
    public void setDelayCaptureLayoutVisibility(int visibility) {
        delayCaptureLayout.setVisibility(visibility);
    }

    @Override
    public void setDelayCaptureTextTime(String delayCaptureTime) {
        delayCaptureText.setText(delayCaptureTime);
    }

    @Override
    public void setBurstStatusIcon(int drawableId) {
        burstStatus.setBackgroundResource(drawableId);
    }

    @Override
    public void setWbStatusIcon(int drawableId) {
        wbStatus.setBackgroundResource(drawableId);
    }

    @Override
    public void setUpsideVisibility(int visibility) {
        carMode.setVisibility(visibility);
    }

    @Override
    public void startMPreview(MyCamera myCamera) {
        AppLog.d(TAG, "startMPreview");
        if (noSupportPreviewTxv.getVisibility() == View.VISIBLE) {
            noSupportPreviewTxv.setVisibility(View.GONE);
        }
//        mPreview.setVisibility(View.GONE);
        mPreview.setVisibility(View.VISIBLE);
        mPreview.start(myCamera, PreviewLaunchMode.RT_PREVIEW_MODE);
    }

    @Override
    public void stopMPreview(MyCamera myCamera) {
        mPreview.stop();
    }

    @Override
    public void setCaptureBtnEnability(boolean enablity) {
        captureBtn.setEnabled(enablity);
    }

    @Override
    public void setPreviewInfo(String info) {
        AppLog.i(TAG, "setPreviewInfo info=" + info);
        curPreviewInfoTxv.setText(info);
    }

    @Override
    public void setDecodeInfo(String info) {
//        decodeInfo.setText(info);
    }

    @Override
    public void setYouTubeLiveLayoutVisibility(int visibility) {
        youTubeLiveLayout.setVisibility(visibility);
    }

    @Override
    public void setYouTubeBtnTxv(int resId) {
        youtubeLiveBtn.setText(resId);
    }

    @Override
    public void setOnDecodeTimeListener(OnDecodeTimeListener onDecodeTimeListener) {
        mPreview.setOnDecodeTimeListener(onDecodeTimeListener);
    }

    @Override
    public void setDecodeTimeLayoutVisibility(int visibility) {
        decodeTimeLayout.setVisibility(visibility);
    }

    @Override
    public void setDecodeTimeTxv(String value) {
        decodeTimeTxv.setText(value);
    }

    @Override
    public void showZoomView() {
        zoomView.startDisplay();
    }

    @Override
    public void setMaxZoomRate(final int maxZoomRate) {
        zoomView.setMaxValue(maxZoomRate);
    }

    @Override
    public int getZoomViewProgress() {
        return zoomView.getProgress();
    }

    @Override
    public int getZoomViewMaxZoomRate() {
        return ZoomView.MAX_VALUE;
    }

    @Override
    public void updateZoomViewProgress(int currentZoomRatio) {
        zoomView.updateZoomBarValue(currentZoomRatio);
    }

    @Override
    public int getSetupMainMenuVisibility() {
        return setupMainMenu.getVisibility();
    }

    @Override
    public void setSetupMainMenuVisibility(int visibility) {
        setupMainMenu.setVisibility(visibility);
    }

    @Override
    public void setAutoDownloadBitmap(Bitmap bitmap) {
        if (bitmap != null) {
            autoDownloadImagview.setImageBitmap(bitmap);
        }
    }

    @Override
    public void setActionBarTitle(int resId) {
        actionBar.setTitle(resId);
    }

    @Override
    public void setSettingBtnVisible(boolean isVisible) {
        settingMenu.setVisible(isVisible);
    }

    @Override
    public void setBackBtnVisibility(boolean isVisible) {
        actionBar.setDisplayHomeAsUpEnabled(isVisible);
    }

    @Override
    public void setSettingMenuListAdapter(SettingListAdapter settingListAdapter) {
        mainMenuList.setAdapter(settingListAdapter);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        dismissPopupWindow();
        AppLog.d(TAG, "onConfigurationChanged newConfig Orientation=" + newConfig.orientation);
    }

    @Override
    public void setSupportPreviewTxvVisibility(int visibility) {
        noSupportPreviewTxv.setVisibility(visibility);
    }

    @Override
    public void setPvModeBtnBackgroundResource(int drawableId) {
        pvModeBtn.setImageResource(drawableId);
    }

    @Override
    public void setTimepLapseRadioBtnVisibility(int visibility) {
        timepLapseRadioBtn.setVisibility(visibility);
    }

    @Override
    public void setCaptureRadioBtnVisibility(int visibility) {
        captureRadioBtn.setVisibility(visibility);
    }

    @Override
    public void setVideoRadioBtnVisibility(int visibility) {
        videoRadioBtn.setVisibility(visibility);
    }

    @Override
    public void setTimepLapseRadioChecked(boolean checked) {
        timepLapseRadioBtn.setChecked(checked);
    }

    @Override
    public void setCaptureRadioBtnChecked(boolean checked) {
        captureRadioBtn.setChecked(checked);
    }

    @Override
    public void setVideoRadioBtnChecked(boolean checked) {
        videoRadioBtn.setChecked(checked);
    }

    @Override
    public void showPopupWindow(int curMode) {
        if (pvModePopupWindow != null) {
            int height = SystemInfo.getMetrics().heightPixels;
            AppLog.d(TAG, "showPopupWindow height = " + height);
            AppLog.d(TAG, "showPopupWindow pvModeBtn.getWidth() = " + pvModeBtn.getWidth());
            AppLog.d(TAG, "showPopupWindow pvModeBtn.getHeight() = " + pvModeBtn.getHeight());
            AppLog.d(TAG, "showPopupWindow contentView.getHeight() = " + contentView.getHeight());
            //JIRA BUG IC-587 Start modify by b.jiang 20160719
            int contentViewH = contentView.getHeight();
            if (contentViewH == 0) {
                contentViewH = pvModeBtn.getHeight() * 5;
            }
//            pvModePopupWindow.showAsDropDown(pvModeBtn, -pvModeBtn.getWidth(), -pvModeBtn.getHeight()-contentViewH);
            pvModePopupWindow.showAsDropDown(pvModeBtn, 0, -pvModeBtn.getHeight() - contentViewH);
            //JIRA BUG IC-587 End modify by b.jiang 20160719
        }
    }

    @Override
    public void dismissPopupWindow() {
        if (pvModePopupWindow != null) {
            if (pvModePopupWindow.isShowing()) {
                pvModePopupWindow.dismiss();
            }
        }
    }

    //JIRA ICOM-3669 begin add by b.jiang 20160914
    public void refresh() {
        presenter.refresh();
    }
    //JIRA ICOM-3669 begin add by b.jiang 20160914

    public void stopStream() {
        presenter.stopStream();
    }
}
