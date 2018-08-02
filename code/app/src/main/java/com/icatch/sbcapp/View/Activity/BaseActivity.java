package com.icatch.sbcapp.View.Activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.icatch.sbcapp.GlobalApp.ExitApp;

public class BaseActivity extends AppCompatActivity {
    private String TAG = BaseActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ExitApp.getInstance().addActivity(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ExitApp.getInstance().setCurActivity(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ExitApp.getInstance().removeActivity(this);
    }
}
