package com.geefox.geefoxptr;

import android.content.Context;
import android.support.v4.widget.NestedScrollView;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created on 16/7/12.
 *
 * @author JM
 * @version v1.0
 * @discrition TODO
 */

public class NoScrollScrollView extends NestedScrollView {
    public NoScrollScrollView(Context context) {
        super(context);
    }

    public NoScrollScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NoScrollScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return super.onTouchEvent(ev);
    }
}
