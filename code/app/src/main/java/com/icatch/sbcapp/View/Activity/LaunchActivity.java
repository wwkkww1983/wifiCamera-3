package com.icatch.sbcapp.View.Activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.icatch.sbcapp.Adapter.CameraSlotAdapter;
import com.icatch.sbcapp.AppDialog.AppDialog;
import com.icatch.sbcapp.AppInfo.ConfigureInfo;
import com.icatch.sbcapp.GlobalApp.GlobalInfo;
import com.icatch.sbcapp.Listener.OnFragmentInteractionListener;
import com.icatch.sbcapp.Log.AppLog;
import com.icatch.sbcapp.Presenter.LaunchPresenter;
import com.icatch.sbcapp.R;
import com.icatch.sbcapp.Tools.GlideUtils;
import com.icatch.sbcapp.Tools.LruCacheTool;
import com.icatch.sbcapp.Tools.PermissionTools;
import com.icatch.sbcapp.Tools.StorageUtil;
import com.icatch.sbcapp.View.Interface.LaunchView;
import com.icatch.wificam.customer.type.ICatchEventID;

public class LaunchActivity extends BaseActivity implements View.OnClickListener, LaunchView, OnFragmentInteractionListener {
    private static String TAG = "LaunchActivity";
    private TextView noPhotosFound, noVideosFound;
    private ImageView localVideo, localPhoto;
    private ListView camSlotListView;
    private LaunchPresenter presenter;
    private LinearLayout launchLayout;
    private FrameLayout launchSettingFrame;
    private String currentFragment;
    private ActionBar actionBar;
    private final String tag = "LaunchActivity";
    private AppBarLayout appBarLayout;
    private String TEST = "LaunchActivityTEST";
    private MenuItem menuSetIp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        appBarLayout = (AppBarLayout) findViewById(R.id.appbar);
        launchLayout = (LinearLayout) findViewById(R.id.launch_view);
        launchSettingFrame = (FrameLayout) findViewById(R.id.launch_setting_frame);

        noPhotosFound = (TextView) findViewById(R.id.no_local_photos);
        noVideosFound = (TextView) findViewById(R.id.no_local_videos);

        localVideo = (ImageView) findViewById(R.id.local_video);
        localVideo.setOnClickListener(this);

        localPhoto = (ImageView) findViewById(R.id.local_photo);
        localPhoto.setOnClickListener(this);

        presenter = new LaunchPresenter(LaunchActivity.this);
        presenter.setView(this);
        GlobalInfo.getInstance().addEventListener(ICatchEventID.ICH_EVENT_SDCARD_REMOVED, false);
//        presenter.addGlobalLisnter(ICatchEventID.ICH_EVENT_SDCARD_REMOVED, false);

        camSlotListView = (ListView) findViewById(R.id.cam_slot_listview);
        camSlotListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                presenter.removeCamera(position);
                return false;
            }
        });

        camSlotListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FragmentManager fm = getSupportFragmentManager();
                getSupportFragmentManager();
                presenter.launchCamera(position, fm);
            }
        });
        LruCacheTool.getInstance().initLruCache();
        presenter.submitAppInfo();

        if (Build.VERSION.SDK_INT < 23 || PermissionTools.CheckSelfPermission(this)) {
            ConfigureInfo.getInstance().initCfgInfo(this.getApplicationContext());
            presenter.showLicenseAgreementDialog();
        } else {
            PermissionTools.RequestPermissions(LaunchActivity.this);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        AppLog.d(tag, "onStart");
    }

    @Override
    protected void onResume() {
        AppLog.i(tag, "Start onResume");
        super.onResume();
        presenter.registerWifiReceiver();
        presenter.loadListview();
        if (Build.VERSION.SDK_INT < 23 || PermissionTools.CheckSelfPermission(this)) {
            presenter.loadLocalThumbnails();
        }
        GlobalInfo.getInstance().setCurrentApp(LaunchActivity.this);
        AppLog.i(tag, "End onResume");
        StorageUtil.sdCardExist(LaunchActivity.this);
    }


    @Override
    protected void onPause() {
        super.onPause();
        presenter.unregisterWifiReceiver();
    }

    @Override
    protected void onStop() {
        super.onStop();
        AppLog.d(tag, "onStop");

//        presenter.isAppBackground();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_HOME:
                AppLog.d("AppStart", "home");
                break;
            case KeyEvent.KEYCODE_BACK:
                AppLog.d("AppStart", "back");
//                finish();
                removeFragment();
                break;
            case KeyEvent.KEYCODE_MENU:
                AppLog.d("AppStart", "KEYCODE_MENU");
                break;
            default:
                return super.onKeyDown(keyCode, event);
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        AppLog.d(tag, "onDestroy");
        super.onDestroy();
        GlobalInfo.getInstance().delEventListener(ICatchEventID.ICH_EVENT_SDCARD_REMOVED, false);
//        GlobalInfo.getInstance().endSceenListener();
        LruCacheTool.getInstance().clearCache();
        presenter.removeActivity();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_launch, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        AppLog.d(TAG, "onPrepareOptionsMenu");
        super.onPrepareOptionsMenu(menu);
        menuSetIp = menu.findItem(R.id.action_set_ip);
        presenter.initMenu();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        AppLog.i(tag, "id =" + id);
        AppLog.i(tag, "R.id.home =" + R.id.home);
        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_search) {
//            //return true;
//            presenter.startSearchCamera();
//        } else
        if (id == android.R.id.home) {
//            finish();
            removeFragment();
            return true;
        } else if (id == R.id.action_set_ip) {
            presenter.showSettingIpDialog(LaunchActivity.this);
        } else if (id == R.id.action_about) {
            AppDialog.showAPPVersionDialog(LaunchActivity.this);
        } else if (id == R.id.action_license) {
            Intent mainIntent = new Intent(LaunchActivity.this, LicenseAgreementActivity.class);
            LaunchActivity.this.startActivity(mainIntent);
        } else if (id == R.id.action_help) {
            Intent mainIntent = new Intent(LaunchActivity.this, LaunchHelpActivity.class);
            LaunchActivity.this.startActivity(mainIntent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        AppLog.i(tag, "click info:::v.getId() =" + v.getId());
        AppLog.i(tag, "click info:::R.id.local_photo =" + R.id.local_photo);
        AppLog.i(tag, "click info:::R.id.local_video =" + R.id.local_video);
        switch (v.getId()) {
            case R.id.local_photo:
                AppLog.i(tag, "click the local photo");
                presenter.redirectToAnotherActivity(LaunchActivity.this, LocalPhotoWallActivity.class);
                //presenter.redirectToAnotherActivity(LaunchActivity.this, MultiPbActivity.class);
                break;
            case R.id.local_video:
                presenter.redirectToAnotherActivity(LaunchActivity.this, LocalVideoWallActivity.class);
                break;
            default:
                break;
        }
    }

    @Override
    public void setLocalPhotoThumbnail(String filePath) {
        GlideUtils.loadImageViewLodingSize(this, filePath, 500, 500, localPhoto, R.drawable.local_default_thumbnail, R.drawable.local_default_thumbnail);
    }

    @Override
    public void setLocalVideoThumbnail(Bitmap bitmap) {
//        GlideUtils.loadImageViewLodingSize(this,filePath,500,500,localVideo,R.drawable.local_default_thumbnail,R.drawable.local_default_thumbnail);
        localVideo.setImageBitmap(bitmap);
    }

    @Override
    public void loadDefaultLocalPhotoThumbnail() {
        //ICOM-3906 20161104
        localPhoto.setImageResource(R.drawable.local_default_thumbnail);
//        localPhoto.setBackgroundResource(R.drawable.local_default_thumbnail);
    }

    @Override
    public void loadDefaultLocalVideooThumbnail() {
        localVideo.setImageResource(R.drawable.local_default_thumbnail);
//        localVideo.setBackgroundResource(R.drawable.local_default_thumbnail);
    }

    @Override
    public void setNoPhotoFilesFoundVisibility(int visibility) {
        noPhotosFound.setVisibility(visibility);
    }

    @Override
    public void setNoVideoFilesFoundVisibility(int visibility) {
        noVideosFound.setVisibility(visibility);
    }

    @Override
    public void setPhotoClickable(boolean clickable) {
        localPhoto.setEnabled(clickable);
    }

    @Override
    public void setVideoClickable(boolean clickable) {
        localVideo.setEnabled(clickable);
    }

    @Override
    public void setListviewAdapter(CameraSlotAdapter cameraSlotAdapter) {
        camSlotListView.setAdapter(cameraSlotAdapter);
    }

    @Override
    public void setBackBtnVisibility(boolean visibility) {
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(visibility);
        }
    }

    @Override
    public void setNavigationTitle(int resId) {
        if (actionBar != null) {
            actionBar.setTitle(resId);
        }
    }

    @Override
    public void setNavigationTitle(String res) {
        if (actionBar != null) {
            actionBar.setTitle(res);
        }
    }

    @Override
    public void setLaunchLayoutVisibility(int visibility) {
        launchLayout.setVisibility(visibility);
        appBarLayout.setVisibility(visibility);
    }

    @Override
    public void setLaunchSettingFrameVisibility(int visibility) {
        launchSettingFrame.setVisibility(visibility);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PermissionTools.WRITE_OR_READ_EXTERNAL_STORAGE_REQUEST_CODE:
                AppLog.i(tag, "permissions.length = " + permissions.length);
                AppLog.i(tag, "grantResults.length = " + grantResults.length);
                boolean retValue = false;
                for (int i = 0; i < permissions.length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "Request write storage ", Toast.LENGTH_SHORT).show();
                        retValue = true;
                    } else {
                        retValue = false;
                    }
                }
                if (retValue) {
                    presenter.loadLocalThumbnails();
                    presenter.showLicenseAgreementDialog();
                    ConfigureInfo.getInstance().initCfgInfo(this.getApplicationContext());
                } else {
                    AppDialog.showDialogQuit(this, R.string.permission_is_denied_info);
//                    Toast.makeText(this, "Request write storage failed!", Toast.LENGTH_SHORT).show();
                }

                break;
            default:
        }
    }

    @Override
    public void submitFragmentInfo(String fragment, int resId) {
        currentFragment = fragment;
//        setNavigationTitle(resId);
    }

    @Override
    public void removeFragment() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            finish();
        } else {
            if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
                setNavigationTitle(R.string.app_name);
                launchSettingFrame.setVisibility(View.GONE);
                launchLayout.setVisibility(View.VISIBLE);
                appBarLayout.setVisibility(View.VISIBLE);
                setBackBtnVisibility(false);
            }
            getSupportFragmentManager().popBackStack();
        }
    }

    // 将所有的fragment 出栈;

    @Override
    public void fragmentPopStackOfAll() {
        int fragmentBackStackNum = getSupportFragmentManager().getBackStackEntryCount();
        for (int i = 0; i < fragmentBackStackNum; i++) {
            getSupportFragmentManager().popBackStack();
        }
        setBackBtnVisibility(false);
        setNavigationTitle(R.string.app_name);
        launchSettingFrame.setVisibility(View.GONE);
        launchLayout.setVisibility(View.VISIBLE);
        appBarLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void setMenuSetIpVisibility(boolean visible) {
        AppLog.i(TAG, "setMenuSetIpVisibility visible=" + visible + " menuSetIp=" + menuSetIp);
        if (menuSetIp != null) {
            menuSetIp.setVisible(visible);
        }
    }

    public void startScreenListener() {
        GlobalInfo.getInstance().startScreenListener();
    }
}
