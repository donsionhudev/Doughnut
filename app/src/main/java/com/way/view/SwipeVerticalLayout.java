package com.way.view;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public class SwipeVerticalLayout extends FrameLayout implements SwipeHelper.Callback {
    private static final String TAG = "SwipeVerticalLayout";
    private SwipeHelper mSwipeHelper;
    private Callback mCallback;

    public SwipeVerticalLayout(Context context) {
        this(context, null);
    }

    public SwipeVerticalLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwipeVerticalLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mSwipeHelper = new SwipeHelper(SwipeHelper.Y, this, context);
        mSwipeHelper.setMinSwipeProgress(0.3f);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        float densityScale = getResources().getDisplayMetrics().density;
        mSwipeHelper.setDensityScale(densityScale);
        float pagingTouchSlop = ViewConfiguration.get(getContext()).getScaledPagingTouchSlop();
        mSwipeHelper.setPagingTouchSlop(pagingTouchSlop);
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        Log.v(TAG, "onInterceptTouchEvent()");
        return mSwipeHelper.onInterceptTouchEvent(ev) || super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return mSwipeHelper.onTouchEvent(ev) || super.onTouchEvent(ev);
    }

    @Override
    public View getChildAtPosition(MotionEvent ev) {
        // TODO Auto-generated method stub
        return this;
    }

    @Override
    public View getChildContentView(View v) {
        if (v instanceof ViewGroup) {
            ViewGroup new_name = (ViewGroup) v;
            return new_name.getChildAt(0);
        }
        // return v.findViewById(R.id.icon);
        return null;
    }

    @Override
    public boolean canChildBeDismissed(View v) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public boolean isAntiFalsingNeeded() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void onBeginDrag(View v) {
        // We do this so the underlying ScrollView knows that it won't get
        // the chance to intercept events anymore
        requestDisallowInterceptTouchEvent(true);
    }

    @Override
    public void onChildDismissed(View v, int direction) {
        // TODO Auto-generated method stub
        Log.i("way", "onChildDismissed... direction = " + direction);
        if (mCallback != null)
            mCallback.onChildDismissed(v, direction);
    }

    @Override
    public void onDragCancelled(View v) {
        // do nothing

    }

    @Override
    public void onChildSnappedBack(View animView) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean updateSwipeProgress(View animView, boolean dismissable, float swipeProgress) {
        if (mCallback != null)
            return mCallback.updateSwipeProgress(animView, dismissable, swipeProgress);
        return false;
    }

    @Override
    public float getFalsingThresholdFactor() {
        // TODO Auto-generated method stub
        return 1.0f;
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    public interface Callback {
        void onChildDismissed(View v, int direction);

        boolean updateSwipeProgress(View animView, boolean dismissable, float swipeProgress);
    }
}
