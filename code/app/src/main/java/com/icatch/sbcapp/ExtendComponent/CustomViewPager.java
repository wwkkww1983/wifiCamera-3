package com.icatch.sbcapp.ExtendComponent;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by b.jiang on 2016/1/13.
 */
public class CustomViewPager extends ViewPager {

    private boolean isCanScroll = true;
    public CustomViewPager(Context context) {
        super(context);
    }

    public CustomViewPager(Context context, AttributeSet attrs) {

        super(context, attrs);

    }

    public void setScanScroll(boolean isCanScroll){

        this.isCanScroll = isCanScroll;

    }





    @Override

    public void scrollTo(int x, int y){

        if (isCanScroll){

            super.scrollTo(x, y);

        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if(isCanScroll){
            return super.onTouchEvent(ev);
        }else{
            return true;
        }

    }


}