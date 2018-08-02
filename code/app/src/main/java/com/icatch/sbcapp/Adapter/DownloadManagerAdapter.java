package com.icatch.sbcapp.Adapter;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.icatch.sbcapp.AppInfo.AppInfo;
import com.icatch.sbcapp.BuildConfig;
import com.icatch.wificam.customer.type.ICatchFile;
import com.icatch.sbcapp.BaseItems.DownloadInfo;
import com.icatch.sbcapp.ExtendComponent.NumberProgressBar;
import com.icatch.sbcapp.Log.AppLog;
import com.icatch.sbcapp.R;
import com.icatch.sbcapp.Message.AppMessage;

/**
 * Added by zhangyanhu C01012,2014-5-28
 */
public class DownloadManagerAdapter extends BaseAdapter {
    private String TAG = "DownloadManagerAdapter";
    private Context context;
    private HashMap<ICatchFile, DownloadInfo> chooseListMap;
    private List<ICatchFile> actList;
    private Handler handler;
    private boolean isDownloadComplete = false;

    public DownloadManagerAdapter(Context context, HashMap<ICatchFile, DownloadInfo> downloadDataList,
                                  List<ICatchFile> actList, Handler handler) {
        this.context = context;
        this.chooseListMap = downloadDataList;
        this.actList = actList;
        this.handler = handler;
    }

    /*
     * (non-Javadoc)
     *
     * @see android.widget.Adapter#getCount()
     */
    @Override
    public int getCount() {
        // TODO Auto-generated method stub
//        AppLog.d(TAG,"getCount="+ actList.size());
        return actList.size();
    }

    /*
     * (non-Javadoc)
     *
     * @see android.widget.Adapter#getItem(int)
     */
    @Override
    public Object getItem(int arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see android.widget.Adapter#getItemId(int)
     */
    @Override
    public long getItemId(int arg0) {
        // TODO Auto-generated method stub
        return 0;
    }

    /*
     * (non-Javadoc)
     *
     * @see android.widget.Adapter#getView(int, android.view.View,
     * android.view.ViewGroup)
     */
    @Override
    public View getView(final int position, View convertView, ViewGroup arg2) {
        // TODO Auto-generated method stub
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.download, null);
        }
        if (position >= actList.size()) {
            return convertView;
        }
        isDownloadComplete = false;
        final ImageButton cancelImv = (ImageButton) convertView.findViewById(R.id.doAction);
        TextView fileName = (TextView) convertView.findViewById(R.id.fileName);
        TextView downloadStatus = (TextView) convertView.findViewById(R.id.downloadStatus);

        fileName.setText(actList.get(position).getFileName());
//        ProgressBar processBar = (ProgressBar) convertView.findViewById(R.id.progressBar);
        NumberProgressBar numberProgressBar = (NumberProgressBar) convertView.findViewById(R.id.numberbar);
        final ICatchFile downloadFile = actList.get(position);
        final DownloadInfo downloadInfo = chooseListMap.get(downloadFile);
//        processBar.setProgress(downloadInfo.progress);
//        numberProgressBar.incrementProgressBy(downloadInfo.progress);
        numberProgressBar.setProgress(downloadInfo.progress);
        DecimalFormat df = new DecimalFormat("#.#");
        String curFileLength = df.format(downloadInfo.curFileLength / 1024.0 / 1024) + "M";
        String fileSize = df.format(downloadInfo.fileSize / 1024.0 / 1024) + "M";
        cancelImv.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                AppLog.d(TAG,"3322ee cancelImv setOnClickListener downloadInfo.progress=" + downloadInfo.progress +" position=" + position);
                if (downloadInfo.progress < 100) {
                    handler.obtainMessage(AppMessage.MESSAGE_CANCEL_DOWNLOAD_SINGLE, 0, 0, downloadFile).sendToTarget();
                }
            }
        });

        if (downloadInfo.progress >= 100) {

            downloadStatus.setText(curFileLength + "/" + fileSize);
            cancelImv.setImageResource(R.drawable.ic_done_cyan);
            cancelImv.setClickable(false);
            isDownloadComplete = true;
        } else {
            downloadStatus.setText(curFileLength + "/" + fileSize);
            cancelImv.setImageResource(R.drawable.cancel_task);
            cancelImv.setClickable(true);
            isDownloadComplete = false;
        }

        return convertView;
    }
}
