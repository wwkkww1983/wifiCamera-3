package com.icatch.sbcapp.Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.icatch.sbcapp.Listener.OnAddAsytaskListener;
import com.tonicartos.widget.stickygridheaders.StickyGridHeadersSimpleAdapter;
import com.icatch.sbcapp.BaseItems.FileType;
import com.icatch.sbcapp.BaseItems.MultiPbItemInfo;
import com.icatch.sbcapp.Log.AppLog;
import com.icatch.sbcapp.Mode.OperationMode;
import com.icatch.sbcapp.R;
import com.icatch.sbcapp.SystemInfo.SystemInfo;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by b.jiang on 2015/12/18.
 */

public class MultiPbPhotoWallGridAdapter extends BaseAdapter implements StickyGridHeadersSimpleAdapter {
    private String TAG = "MultiPbPhotoWallGridAdapter";
    private Context context;
    private List<MultiPbItemInfo> list;
    private LayoutInflater mInflater;
    private int width;
    private OperationMode operationMode = OperationMode.MODE_BROWSE;
    private FileType fileType;
    LruCache<Integer, Bitmap> mLruCache;
    private OnAddAsytaskListener listener;

    public MultiPbPhotoWallGridAdapter(Context context, List<MultiPbItemInfo> list, int width, LruCache<Integer, Bitmap> mLruCache,FileType fileType,OnAddAsytaskListener listener) {
        this.context = context;
        mInflater = LayoutInflater.from(context);
        this.list = list;
        this.mLruCache = mLruCache;
        this.fileType = fileType;
        this.width = SystemInfo.getMetrics().widthPixels;
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        AppLog.d(TAG, "getView position=" + position);
        ViewHolder mViewHolder;
        if (convertView == null) {
            mViewHolder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.item_local_photo_wall_grid, parent, false);
            mViewHolder.mImageView = (ImageView) convertView.findViewById(R.id.local_photo_wall_grid_item);
            mViewHolder.mCheckImageView = (ImageView) convertView.findViewById(R.id.local_photo_wall_grid_edit);
            mViewHolder.videoSignImageView = (ImageView)convertView.findViewById(R.id.video_sign);
            ViewGroup.LayoutParams photoLayoutParams = mViewHolder.mImageView.getLayoutParams();
            photoLayoutParams.width = (width - 3 * 1) / 4;
            photoLayoutParams.height = (width - 3 * 1) / 4;

            mViewHolder.mImageView.setLayoutParams(photoLayoutParams);
            convertView.setTag(mViewHolder);
        } else {

            mViewHolder = (ViewHolder) convertView.getTag();
            ViewGroup.LayoutParams photoLayoutParams = mViewHolder.mImageView.getLayoutParams();
            photoLayoutParams.width = (width - 3 * 1) / 4;
            photoLayoutParams.height = (width - 3 * 1) / 4;
            mViewHolder.mImageView.setLayoutParams(photoLayoutParams);
        }
        if (fileType == FileType.FILE_PHOTO){
            mViewHolder.videoSignImageView.setVisibility(View.GONE);
        }else {
            mViewHolder.videoSignImageView.setVisibility(View.VISIBLE);
        }
        if (operationMode == OperationMode.MODE_EDIT) {
            mViewHolder.mCheckImageView.setVisibility(View.VISIBLE);
//            mViewHolder.mImageView.showBorder(true);
            if (list.get(position).isItemChecked) {
                mViewHolder.mCheckImageView.setImageResource(R.drawable.ic_check_box_blue);
            } else {
                mViewHolder.mCheckImageView.setImageResource(R.drawable.ic_check_box_blank_grey);
            }
        } else {
            mViewHolder.mCheckImageView.setVisibility(View.GONE);
//            mViewHolder.mImageView.showBorder(false);
        }

        int fileHandle = list.get(position).getFileHandle();
        mViewHolder.mImageView.setTag(fileHandle);

        Bitmap bitmap = mLruCache.get(fileHandle);
        if (bitmap != null){
            AppLog.d(TAG, "position=" + position + " bitmap.isRecycled()=" + bitmap.isRecycled());
        }

        if (bitmap != null && !bitmap.isRecycled()) {
            mViewHolder.mImageView.setImageBitmap(bitmap);
        }
        else {
            if(listener != null){
                listener.addAsytask(position);
            }
            mViewHolder.mImageView.setImageResource(R.drawable.pictures_no);
        }
        return convertView;
    }

    @Override
    public long getHeaderId(int i) {
        return list.get(i).section;
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        HeaderViewHolder mHeaderHolder;
        if (convertView == null) {
            mHeaderHolder = new HeaderViewHolder();
            convertView = mInflater.inflate(R.layout.item_local_wall_grid_header, parent, false);
            mHeaderHolder.mTextView = (TextView) convertView.findViewById(R.id.photo_wall_header);
            convertView.setTag(mHeaderHolder);
        } else {
            mHeaderHolder = (HeaderViewHolder) convertView.getTag();
        }
        mHeaderHolder.mTextView.setText(list.get(position).getFileDate());
        return convertView;
    }


    @Override
    public void notifyDataSetChanged() {
        width = SystemInfo.getMetrics().widthPixels;
        super.notifyDataSetChanged();

    }

    public static class ViewHolder {
        public ImageView mImageView;
        public ImageView mCheckImageView;
        public ImageView videoSignImageView;
    }

    public static class HeaderViewHolder {
        public TextView mTextView;
    }

    public void changeCheckBoxState(int position, OperationMode operationMode) {
        this.operationMode = operationMode;
        list.get(position).isItemChecked = list.get(position).isItemChecked == true ? false : true;
        this.notifyDataSetChanged();
    }

    public List<MultiPbItemInfo> getCheckedItemsList() {
        LinkedList<MultiPbItemInfo> checkedList = new LinkedList<MultiPbItemInfo>();

        for (int ii = 0; ii < list.size(); ii++) {
            if (list.get(ii).isItemChecked) {
                checkedList.add(list.get(ii));
            }
        }
        return checkedList;
    }

    public void quitEditMode() {
        this.operationMode = OperationMode.MODE_BROWSE;
        for (int ii = 0; ii < list.size(); ii++) {
            list.get(ii).isItemChecked = false;
        }
        this.notifyDataSetChanged();
    }

    public void selectAllItems() {
        for (int ii = 0; ii < list.size(); ii++) {
            list.get(ii).isItemChecked = true;
        }
        this.notifyDataSetChanged();
    }

    public void cancelAllSelections() {
        for (int ii = 0; ii < list.size(); ii++) {
            list.get(ii).isItemChecked = false;
        }
        this.notifyDataSetChanged();
    }

    public int getSelectedCount() {
        int checkedNum = 0;
        for (int ii = 0; ii < list.size(); ii++) {
            if (list.get(ii).isItemChecked) {
                checkedNum++;
            }
        }
        return checkedNum;
    }
}


