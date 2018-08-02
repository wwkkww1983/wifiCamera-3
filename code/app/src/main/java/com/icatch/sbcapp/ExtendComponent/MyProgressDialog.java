package com.icatch.sbcapp.ExtendComponent;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.icatch.sbcapp.R;


/**
 * Created by zhang yanhu C001012 on 2015/11/24 12:15.
 */

public class MyProgressDialog {
    private static Dialog mDialog = null;
    private static ProgressWheel mProgressWheel = null;
    private static boolean hasInit = false;

    public static void showProgressDialog(Context context, String text) {
        closeProgressDialog();
        //if(hasInit == false) {
        WindowManager m = ((Activity) context).getWindowManager();
        Display d = m.getDefaultDisplay(); // 获取屏幕宽、高用

        mDialog = new Dialog(context, R.style.Dialog);
        mDialog.setCancelable(false);

        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View layout = inflater.inflate(R.layout.layout_progress_wheel_small, null);
        mDialog.setContentView(layout);

        mProgressWheel = (ProgressWheel) layout.findViewById(R.id.pw_spinner);
        //hasInit = true;
        // }
        if (text != null) {
            mProgressWheel.setText(text);
        } else {
            mProgressWheel.setText("");
        }

        Window dialogWindow = mDialog.getWindow();//
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.gravity = Gravity.CENTER;

        dialogWindow.setAttributes(lp);
        mProgressWheel.startSpinning();
        mDialog.show();
    }

    public static void showProgressDialog(Context context, int stringID) {
        closeProgressDialog();
        //if(hasInit == false) {
        WindowManager m = ((Activity) context).getWindowManager();
        Display d = m.getDefaultDisplay(); // 获取屏幕宽、高用

        mDialog = new Dialog(context, R.style.Dialog);
        mDialog.setCancelable(false);

        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View layout = inflater.inflate(R.layout.layout_progress_wheel_small, null);
        mDialog.setContentView(layout);

        mProgressWheel = (ProgressWheel) layout.findViewById(R.id.pw_spinner);
        //hasInit = true;
        // }
        String text = context.getResources().getString(stringID);
        if (text != null) {
            mProgressWheel.setText(text);
        } else {
            mProgressWheel.setText("");
        }

        Window dialogWindow = mDialog.getWindow();//
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.gravity = Gravity.CENTER;

        dialogWindow.setAttributes(lp);
        mProgressWheel.startSpinning();
        mDialog.show();
    }

    public static void closeProgressDialog() {
        if (mProgressWheel != null) {
            mProgressWheel.stopSpinning();
            mProgressWheel = null;
        }

        if (!((mDialog == null) || !mDialog.isShowing())) {
            mDialog.dismiss();
            mDialog = null;
        }
    }
}