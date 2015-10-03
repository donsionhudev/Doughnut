package com.way.doughnut;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.widget.ImageView;

import com.way.doughnut.util.MyLog;

/**
 * @author way
 */
public class DoughtnutImageView extends ImageView {
    public static final int SWIPE_NONE = 0;
    public static final int SWIPE_TO_TOP = 1;
    public static final int SWIPE_TO_BOTTOM = 2;
    public static final int SWIPE_TO_RIGHT = 3;
    public static final int SWIPE_TO_LEFT = 4;
    private static final String TAG = "DoughtnutImageView";
    private static final long SWIPE_TIMEOUT_MS = 500;
    private static final int MAX_TRACKED_POINTERS = 32; // max per input system
    private static final int UNTRACKED_POINTER = -1;
    private final Callbacks mCallbacks;
    private final int[] mDownPointerId = new int[MAX_TRACKED_POINTERS];
    private final float[] mDownX = new float[MAX_TRACKED_POINTERS];
    private final float[] mDownY = new float[MAX_TRACKED_POINTERS];
    private final long[] mDownTime = new long[MAX_TRACKED_POINTERS];
    private int mSwipeDistanceThreshold;
    private int mDownPointers;
    private boolean mSwipeFireable;
    private boolean mDebugFireable;
    private Context mContext;
    private WindowManager mWindowManager;

    public DoughtnutImageView(Context context, Callbacks callbacks) {
        super(context);
        mContext = context;
        mCallbacks = checkNull("callbacks", callbacks);
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        final DisplayMetrics metrics = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(metrics);
        //mSwipeDistanceThreshold = (int) (25 * metrics.density);
        //MyLog.d(TAG, "mSwipeDistanceThreshold = " + mSwipeDistanceThreshold);

        // mWindowManager.addView(this, createLayoutParams(getContext()));
        getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                getViewTreeObserver().removeOnGlobalLayoutListener(this);
                mSwipeDistanceThreshold = getMeasuredWidth() / 4;
                MyLog.d(TAG, "onPreDraw... mSwipeDistanceThreshold = " + mSwipeDistanceThreshold);
            }
        });
    }

    private static <T> T checkNull(String name, T arg) {
        if (arg == null) {
            throw new IllegalArgumentException(name + " must not be null");
        }
        return arg;
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        MyLog.i(TAG, "configuration changed: " + mContext.getResources().getConfiguration());
    }

    /**
     * 获取手机屏幕高度
     *
     * @return screen height
     */
    public int getDisplayHeight() {
        DisplayMetrics dm = new DisplayMetrics();
        // 获取屏幕信息
        mWindowManager.getDefaultDisplay().getMetrics(dm);
        return dm.heightPixels;
    }

    /**
     * 获取手机屏幕宽度
     *
     * @return screen width
     */
    public int getDisplayWidth() {
        DisplayMetrics dm = new DisplayMetrics();
        // 获取屏幕信息
        mWindowManager.getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
    }

    public WindowManager.LayoutParams createLayoutParams(Context context) {
        int width = WindowManager.LayoutParams.WRAP_CONTENT;
        int height = WindowManager.LayoutParams.WRAP_CONTENT;

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(width, height,
                WindowManager.LayoutParams.TYPE_TOAST,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                        | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = getDisplayWidth() / 2;
        params.y = getDisplayHeight() / 2;
        return params;
    }

    public void removeFromWindow() {
        if (mWindowManager != null) {
            mWindowManager.removeView(this);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled())
            return super.onTouchEvent(event);
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mSwipeFireable = true;
                mDebugFireable = true;
                mDownPointers = 0;
                captureDown(event, 0);
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                captureDown(event, event.getActionIndex());
                if (mDebugFireable) {
                    mDebugFireable = event.getPointerCount() < 5;
                    if (!mDebugFireable) {
                        MyLog.d(TAG, "Firing debug");
                        mCallbacks.onDebug();
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mSwipeFireable) {
                    final int swipe = detectSwipe(event);
                    mSwipeFireable = swipe == SWIPE_NONE;
                    mCallbacks.onSwipeToDirection(swipe);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mSwipeFireable = false;
                mDebugFireable = false;
                break;
            default:
                MyLog.d(TAG, "Ignoring " + event);
        }
        return true;
    }

    private void captureDown(MotionEvent event, int pointerIndex) {
        final int pointerId = event.getPointerId(pointerIndex);
        final int i = findIndex(pointerId);
        MyLog.d(TAG, "pointer " + pointerId + " down pointerIndex=" + pointerIndex + " trackingIndex=" + i);
        if (i != UNTRACKED_POINTER) {
            mDownX[i] = event.getX(pointerIndex);
            mDownY[i] = event.getY(pointerIndex);
            mDownTime[i] = event.getEventTime();
            MyLog.d(TAG, "pointer " + pointerId + " down x=" + mDownX[i] + " y=" + mDownY[i]);
        }
    }

    private int findIndex(int pointerId) {
        for (int i = 0; i < mDownPointers; i++) {
            if (mDownPointerId[i] == pointerId) {
                return i;
            }
        }
        if (mDownPointers == MAX_TRACKED_POINTERS || pointerId == MotionEvent.INVALID_POINTER_ID) {
            return UNTRACKED_POINTER;
        }
        mDownPointerId[mDownPointers++] = pointerId;
        return mDownPointers - 1;
    }

    private int detectSwipe(MotionEvent move) {
        final int historySize = move.getHistorySize();
        final int pointerCount = move.getPointerCount();
        for (int p = 0; p < pointerCount; p++) {
            final int pointerId = move.getPointerId(p);
            final int i = findIndex(pointerId);
            if (i != UNTRACKED_POINTER) {
                for (int h = 0; h < historySize; h++) {
                    final long time = move.getHistoricalEventTime(h);
                    final float x = move.getHistoricalX(p, h);
                    final float y = move.getHistoricalY(p, h);
                    final int swipe = detectSwipe(i, time, x, y);
                    if (swipe != SWIPE_NONE) {
                        return swipe;
                    }
                }
                final int swipe = detectSwipe(i, move.getEventTime(), move.getX(p), move.getY(p));
                if (swipe != SWIPE_NONE) {
                    return swipe;
                }
            }
        }
        return SWIPE_NONE;
    }

    private int detectSwipe(int i, long time, float x, float y) {
        final float fromX = mDownX[i];
        final float fromY = mDownY[i];
        final long elapsed = time - mDownTime[i];
        MyLog.d(TAG, "pointer " + mDownPointerId[i] + " moved (" + fromX + "->" + x + "," + fromY + "->" + y + ") in "
                + elapsed);
        if ((fromY - y > mSwipeDistanceThreshold) && (Math.abs(x - fromX) < mSwipeDistanceThreshold)
                && elapsed < SWIPE_TIMEOUT_MS) {
            return SWIPE_TO_TOP;
        }
        if ((y - fromY > mSwipeDistanceThreshold) && (Math.abs(x - fromX) < mSwipeDistanceThreshold)
                && elapsed < SWIPE_TIMEOUT_MS) {
            return SWIPE_TO_BOTTOM;
        }
        if ((x - fromX > mSwipeDistanceThreshold) && (Math.abs(y - fromY) < mSwipeDistanceThreshold)
                && elapsed < SWIPE_TIMEOUT_MS) {
            return SWIPE_TO_RIGHT;
        }
        if ((fromX - x > mSwipeDistanceThreshold) && (Math.abs(y - fromY) < mSwipeDistanceThreshold)
                && elapsed < SWIPE_TIMEOUT_MS) {
            return SWIPE_TO_LEFT;
        }
        return SWIPE_NONE;
    }

    public interface Callbacks {
        void onSwipeToDirection(int direction);

        void onDebug();
    }
}
