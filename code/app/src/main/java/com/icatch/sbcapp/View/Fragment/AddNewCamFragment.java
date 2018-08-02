package com.icatch.sbcapp.View.Fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import com.icatch.sbcapp.CustomException.*;
import com.icatch.sbcapp.GlobalApp.GlobalInfo;
import com.icatch.sbcapp.Listener.OnFragmentInteractionListener;
import com.icatch.sbcapp.Log.AppLog;
import com.icatch.sbcapp.Message.AppMessage;
import com.icatch.sbcapp.R;
import com.icatch.sbcapp.test.test;

public class AddNewCamFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private String TAG = "AddNewCamFragment";
    private Button connectCamBtn;
    private Button BTPairBtn;
    private Handler appStartHandler;
    private ImageButton backBtn;

    public AddNewCamFragment() {
        // Required empty public constructor
        this.appStartHandler = GlobalInfo.getInstance().getAppStartHandler();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        AppLog.d(TAG, "onCreateView");
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_new_cam, container, false);
        BTPairBtn = (Button) view.findViewById(R.id.bt_pair);
        connectCamBtn = (Button) view.findViewById(R.id.bt_connect_camera);
        connectCamBtn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                appStartHandler.obtainMessage(AppMessage.MESSAGE_CAMERA_CONNECTING_START).sendToTarget();
            }
        });

        BTPairBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                BTPairBeginFragment btPairBegin = new BTPairBeginFragment();
                FragmentManager fm = getFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.launch_setting_frame, btPairBegin);
                ft.addToBackStack("BTPairBeginFragment");
                ft.commit();
            }
        });
        backBtn = (ImageButton) view.findViewById(R.id.back_btn);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mListener != null){
                    mListener.removeFragment();
                }
            }
        });
        return view;
    }


    @Override
    public void onAttach(Context context) {
        AppLog.d(TAG, "onAttach");
        super.onAttach(context);
        try {
            mListener = (OnFragmentInteractionListener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        AppLog.d(TAG, "onDetach");
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onResume() {
        AppLog.d(TAG, "onResume");
        super.onResume();
        if(mListener != null){
            mListener.submitFragmentInfo(AddNewCamFragment.class.getSimpleName(),R.string.title_activity_add_new_cam);
        }
    }

    @Override
    public void onStart() {
        AppLog.d(TAG, "onStart");
        super.onStart();
    }

    @Override
    public void onStop() {
        AppLog.d(TAG, "onStop");
        super.onStop();
    }

    @Override
    public void onDestroy() {
        AppLog.d(TAG, "onDestroy");
        super.onDestroy();
    }
}
