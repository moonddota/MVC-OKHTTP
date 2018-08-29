package com.skylin.uav.drawforterrain.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by mooon on 2016/11/15.
 */

public class ViewPager extends android.support.v4.view.ViewPager {
     private boolean noScroll  = false;
    public ViewPager(Context context) {
        super(context);
    }
    public ViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public void setNoScroll(boolean noScroll) {
        this.noScroll = noScroll;
    }
    @Override
    public void scrollTo(int x, int y) {
        super.scrollTo(x, y);
    }
    @Override    //禁止viewpager手势滑动
    public boolean onTouchEvent(MotionEvent ev) {
        if (noScroll){
            return super.onTouchEvent(ev);
        }
        else{
            return false;

        }
    }
    @Override//禁止viewpager手势滑动
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (noScroll ){
            return super.onInterceptTouchEvent(ev);
        }
        else {
            return false;
        }
    }

    @Override
    public void setCurrentItem(int item, boolean smoothScroll) {
        super.setCurrentItem(item, smoothScroll);
    }

    @Override
    public void setCurrentItem(int item) {
        super.setCurrentItem(item);
    }
}
