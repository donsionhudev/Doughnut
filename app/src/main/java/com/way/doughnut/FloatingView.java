/**
 * Copyright 2015 RECRUIT LIFESTYLE CO., LTD.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.way.doughnut;

import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Build;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;

import com.way.doughnut.fragment.SettingsFragment;
import com.way.doughnut.util.MyLog;

/**
 *
 * http://stackoverflow.com/questions/18503050/how-to-create-draggabble-system-
 * alert-in-android
 */
class FloatingView extends FrameLayout implements ViewTreeObserver.OnPreDrawListener {

    /**
     * 判断是否移动阀值(dp)
     */
    private static final float MOVE_THRESHOLD_DP = 8.0f;

    /**
     * 按下时缩放率
     */
    private static final float SCALE_PRESSED = 0.9f;

    /**
     * 正常状态缩放率
     */
    private static final float SCALE_NORMAL = 1.0f;

    /**
     * 动画持续时间
     */
    private static final long MOVE_TO_EDGE_DURATION = 450L;
    private static final long ICON_SCALE_DURATION_MILLIS = 200L;
    /**
     * 动画插值器系数
     */
    private static final float MOVE_TO_EDGE_OVERSHOOT_TENSION = 1.25f;

    /**
     * FloatingView
     */
    private static final float SIDE_CHANGE_THRESHOLD_MILLIS = 0.75f;
    private static final String X_POSITION = "x-position";
    private static final String Y_POSITION = "y-position";
    /**
     * WindowManager
     */
    private final WindowManager mWindowManager;
    /**
     * LayoutParams
     */
    private final WindowManager.LayoutParams mParams;
    /**
     * DisplayMetrics
     */
    private final DisplayMetrics mMetrics;
    /**
     * ステータスバーの高さ
     */
    private final int mStatusBarHeight;
    /**
     * Interpolator
     */
    private final TimeInterpolator mMoveEdgeInterpolator;
    /**
     * 移動限界を表すRect
     */
    private final Rect mMoveLimitRect;
    /**
     * 表示位置（画面端）の限界を表すRect
     */
    private final Rect mPositionLimitRect;
    /**
     * 按下时间
     */
    private long mTouchDownTime;
    /**
     * スクリーン押下X座標(移動量判定用)
     */
    private float mScreenTouchDownX;
    /**
     * スクリーン押下Y座標(移動量判定用)
     */
    private float mScreenTouchDownY;
    /**
     * 移动量阀值判断
     */
    private boolean mIsMoveAccept;
    /**
     * スクリーンのタッチX座標
     */
    private float mScreenTouchX;
    /**
     * スクリーンのタッチY座標
     */
    private float mScreenTouchY;
    /**
     * ローカルのタッチX座標
     */
    private float mLocalTouchX;
    /**
     * ローカルのタッチY座標
     */
    private float mLocalTouchY;
    /**
     * 左・右端に寄せるアニメーション
     */
    private ValueAnimator mMoveEdgeAnimator;
    private ValueAnimator mScaleAnimator;
    /**
     * ドラッグ可能フラグ
     */
    private boolean mIsDraggable;
    /**
     * 画面端をオーバーするマージン
     */
    private int mOverMargin;
    /**
     * 画面上の右側にある場合はtrue
     */
    private boolean mIsOnRight;
    /**
     * 判断是否处于长按状态
     */
    private boolean mIsLongClick = false;
    private boolean mIsAllowMoveEdge = false;
    private Vibrator mVibrator;
    private SharedPreferences mPreferences;
    private int mOriginWidth;
    private int mOriginHeight;
    private Runnable longClickRunnable = new Runnable() {
        public void run() {
            mVibrator.vibrate(PreferenceManager.getDefaultSharedPreferences(getContext())
                    .getInt(SettingsFragment.KEY_VIBRATOR_LEVEL, SettingsFragment.DEFAULT_VIBRATE_LEVEL));
            mIsLongClick = true;
        }
    };

    /**
     *
     * @param context
     *            {@link Context}
     */
    FloatingView(final Context context) {
        super(context);
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        mIsAllowMoveEdge = mPreferences.getBoolean(SettingsFragment.KEY_AUTO_SIDE_MODEL, false);
        mParams = new WindowManager.LayoutParams();
        mMetrics = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(mMetrics);
        mOriginWidth = mOriginHeight = context.getResources().getDimensionPixelOffset(R.dimen.default_float_view_size);
        // mParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        // mParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        float scale = mPreferences.getInt(SettingsFragment.KEY_FLOAT_VIEW_SIZE, 100) / 100.0f;
        mParams.width = Math.round(mOriginWidth * scale);
        mParams.height = Math.round(mOriginHeight * scale);
        boolean smartHide = mPreferences.getBoolean(SettingsFragment.KEY_SMART_HIDE, true);
        mParams.type = smartHide ? WindowManager.LayoutParams.TYPE_TOAST : WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        mParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        mParams.format = PixelFormat.TRANSLUCENT;
        float alpha = mPreferences.getInt(SettingsFragment.KEY_FLOAT_VIEW_ALPHA, 80) / 100.0f;
        mParams.alpha = alpha;
        // 左下の座標を0とする
        mParams.gravity = Gravity.LEFT | Gravity.BOTTOM;
        mMoveEdgeInterpolator = new OvershootInterpolator(MOVE_TO_EDGE_OVERSHOOT_TENSION);

        mMoveLimitRect = new Rect();
        mPositionLimitRect = new Rect();

        // ステータスバーの高さを取得
        final Resources resources = context.getResources();
        final int statusBarHeightId = resources.getIdentifier("status_bar_height", "dimen", "android");
        if (statusBarHeightId > 0) {
            mStatusBarHeight = resources.getDimensionPixelSize(statusBarHeightId);
        } else {
            mStatusBarHeight = 0;
        }

        // 初次添加时调用
        getViewTreeObserver().addOnPreDrawListener(this);
    }

    /**
     * 表示位置を決定します。
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        updateViewLayout();
    }

    /**
     * 屏幕旋转
     */
    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateViewLayout();
    }

    /**
     * 初回描画時の座標設定を行います。
     */
    @Override
    public boolean onPreDraw() {
        getViewTreeObserver().removeOnPreDrawListener(this);
        int defX = mMetrics.widthPixels / 2 - getMeasuredWidth() / 2;
        int defY = mMetrics.heightPixels / 2 + mStatusBarHeight - getMeasuredHeight() / 2;
        int x = mPreferences.getInt(X_POSITION, defX);
        int y = mPreferences.getInt(Y_POSITION, defY);
        mParams.x = x;
        mParams.y = y;
        mScreenTouchX = mParams.x;
        mScreenTouchY = mParams.y;
        mWindowManager.updateViewLayout(this, mParams);
        mIsDraggable = true;
        mIsOnRight = false;
        if (mIsAllowMoveEdge)
            moveToEdge(false);
        return true;
    }

    /**
     * 画面サイズから自位置を決定します。
     */
    private void updateViewLayout() {
        cancelAnimation();

        final int oldScreenHeight = mMetrics.heightPixels;
        final int oldScreenWidth = mMetrics.widthPixels;
        final int oldPositionLimitHeight = mPositionLimitRect.height();
        final int oldPositionLimitWidth = mPositionLimitRect.width();

        mWindowManager.getDefaultDisplay().getMetrics(mMetrics);
        final int width = getMeasuredWidth();
        final int height = getMeasuredHeight();
        final int newScreenWidth = mMetrics.widthPixels;
        final int newScreenHeight = mMetrics.heightPixels;

        mMoveLimitRect.set(-width, -height * 2, newScreenWidth + width, newScreenHeight + height);
        mPositionLimitRect.set(-mOverMargin, 0, newScreenWidth - width + mOverMargin,
                newScreenHeight - mStatusBarHeight - height);

        if (oldScreenWidth != newScreenWidth || oldScreenHeight != newScreenHeight) {
            if (mIsAllowMoveEdge) {
                if (mParams.x > (newScreenWidth - width) / 2) {
                    mParams.x = mPositionLimitRect.right;
                } else {
                    mParams.x = mPositionLimitRect.left;
                }
            } else {
                final int newX = (int) (mParams.x * mPositionLimitRect.width() / (float) oldPositionLimitWidth + 0.5f);
                mParams.x = Math.min(Math.max(mPositionLimitRect.left, newX), mPositionLimitRect.right);
            }

            final int newY = (int) (mParams.y * mPositionLimitRect.height() / (float) oldPositionLimitHeight + 0.5f);
            mParams.y = Math.min(Math.max(mPositionLimitRect.top, newY), mPositionLimitRect.bottom);
            mWindowManager.updateViewLayout(this, mParams);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onDetachedFromWindow() {
        if (mMoveEdgeAnimator != null) {
            mMoveEdgeAnimator.removeAllUpdateListeners();
        }
        if (mScaleAnimator != null)
            mScaleAnimator.removeAllUpdateListeners();
        super.onDetachedFromWindow();
    }

    private void updateViewPostion() {
        mParams.x = Math.min(Math.max(0, getXByTouch()), mMetrics.widthPixels - getMeasuredWidth());
        mParams.y = Math.min(Math.max(0, getYByTouch()), mMetrics.heightPixels - getMeasuredHeight());
        mWindowManager.updateViewLayout(this, mParams);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (!isEnabled())
            return super.dispatchTouchEvent(event);
        // Viewが表示されていなければ何もしない
        if (getVisibility() != View.VISIBLE) {
            return true;
        }

        // タッチ不能な場合は何もしない
        if (!mIsDraggable) {
            return true;
        }

        // 現在位置のキャッシュ
        mScreenTouchX = event.getRawX();
        mScreenTouchY = event.getRawY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                cancelAnimation();
                mScreenTouchDownX = mScreenTouchX;
                mScreenTouchDownY = mScreenTouchY;
                mLocalTouchX = event.getX();
                mLocalTouchY = event.getY();
                mIsMoveAccept = false;
                // setScale(SCALE_PRESSED);
                setScaleFromTo(SCALE_NORMAL, SCALE_PRESSED, ICON_SCALE_DURATION_MILLIS, new OvershootInterpolator());
                // 押下処理の通過判定のための時間保持
                // mIsDraggableやgetVisibility()のフラグが押下後に変更された場合にMOVE等を処理させないようにするため
                mTouchDownTime = event.getDownTime();
                mIsLongClick = false;
                postDelayed(longClickRunnable, ViewConfiguration.getLongPressTimeout());
                break;
            case MotionEvent.ACTION_MOVE:
                if (mTouchDownTime != event.getDownTime()) {
                    return true;
                }
                final float moveThreshold = MOVE_THRESHOLD_DP * mMetrics.density;
                // 移動受付状態でない、かつX,Y軸ともにしきい値よりも小さい場合
                if (!mIsMoveAccept && Math.abs(mScreenTouchX - mScreenTouchDownX) < moveThreshold
                        && Math.abs(mScreenTouchY - mScreenTouchDownY) < moveThreshold) {
                    return true;
                }
                removeCallbacks(longClickRunnable);
                mIsMoveAccept = true;
                if (mIsLongClick)
                    updateViewPostion();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (mTouchDownTime != event.getDownTime()) {
                    return true;
                }
                // 恢复原图大小
                // setScale(SCALE_NORMAL);
                setScaleFromTo(SCALE_PRESSED, SCALE_NORMAL, ICON_SCALE_DURATION_MILLIS, new OvershootInterpolator());

                // 動かされていれば画面端に戻す
                if (mIsMoveAccept) {
                    if (mIsAllowMoveEdge && mIsLongClick)
                        moveToEdge(true);
                }
                // 動かされていなければ、クリックイベントを発行
                else {
                    // 一番上のViewからたどって、1つ処理したら終了
                    final int size = getChildCount();
                    for (int i = size - 1; i >= 0; i--) {
                        if (getChildAt(i).performClick()) {
                            break;
                        }
                    }
                }
                mIsLongClick = false;
                removeCallbacks(longClickRunnable);
                break;
            default:
                break;
        }
        return super.dispatchTouchEvent(event);
    }

    /**
     * 画面から消す際の処理を表します。
     */
    @Override
    public void setVisibility(int visibility) {
        // 画面表示時
        if (visibility != View.VISIBLE) {
            // 画面から消す時は長押しをキャンセルし、画面端に強制的に移動します。
            cancelLongPress();
            setScale(SCALE_NORMAL);
            if (mIsMoveAccept) {
                moveToEdge(false);
            }
        }
        super.setVisibility(visibility);
    }

    /**
     * 左右の端に移動します。
     *
     * @param withAnimation
     *            アニメーションを行う場合はtrue.行わない場合はfalse
     */
    private void moveToEdge(boolean withAnimation) {
        // TODO:縦軸の速度も考慮して斜めに行くようにする
        // X・Y座標と移動方向を設定
        final int currentX = getXByTouch();
        final int currentY = getYByTouch();
        final boolean isMoveRightEdge = currentX > (mMetrics.widthPixels - getWidth()) / 2;
        final int goalPositionX = isMoveRightEdge ? mPositionLimitRect.right : mPositionLimitRect.left;
        final int goalPositionY = Math.min(Math.max(mPositionLimitRect.top, currentY), mPositionLimitRect.bottom);
        mIsOnRight = isMoveRightEdge;

        // アニメーションを行う場合
        if (withAnimation) {
            // TODO:Y座標もアニメーションさせる
            mParams.y = goalPositionY;

            mMoveEdgeAnimator = ValueAnimator.ofInt(currentX, goalPositionX);
            mMoveEdgeAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mParams.x = (Integer) animation.getAnimatedValue();
                    mWindowManager.updateViewLayout(FloatingView.this, mParams);
                }
            });
            // X軸のアニメーション設定
            mMoveEdgeAnimator.setDuration(MOVE_TO_EDGE_DURATION);
            mMoveEdgeAnimator.setInterpolator(mMoveEdgeInterpolator);
            mMoveEdgeAnimator.start();
        } else {
            // 位置が変化した時のみ更新
            if (mParams.x != goalPositionX || mParams.y != goalPositionY) {
                mParams.x = goalPositionX;
                mParams.y = goalPositionY;
                mWindowManager.updateViewLayout(FloatingView.this, mParams);
            }
        }
        // タッチ座標を初期化
        mLocalTouchX = 0;
        mLocalTouchY = 0;
        mScreenTouchDownX = 0;
        mScreenTouchDownY = 0;
        mIsMoveAccept = false;
    }

    public void setScaleFromTo(float from, float to, long duration, Interpolator interpolator) {
        canAnimate();
        mScaleAnimator = ValueAnimator.ofFloat(from, to);
        mScaleAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float scale = (float) animation.getAnimatedValue();
                setScale(scale);
            }
        });
        mScaleAnimator.setDuration(duration);
        mScaleAnimator.setInterpolator(interpolator);
        mScaleAnimator.start();
    }

    public void scaleToDimiss(boolean withAnimation) {
        if (withAnimation) {
            setScaleFromTo(1.0f, 0.0f, MOVE_TO_EDGE_DURATION, new AccelerateDecelerateInterpolator());
        } else {
            setScale(0.0f);
        }
        setEnabled(false);

    }

    public void scaleToShow(boolean withAnimation) {
        if (withAnimation) {
            setScaleFromTo(0.0f, 1.0f, MOVE_TO_EDGE_DURATION, new OvershootInterpolator());
        } else {
            setScale(1.0f);
        }
        setEnabled(true);
    }

    /**
     * アニメーションをキャンセルします。
     */
    private void cancelAnimation() {
        if (mMoveEdgeAnimator != null && mMoveEdgeAnimator.isStarted()) {
            mMoveEdgeAnimator.cancel();
            mMoveEdgeAnimator = null;
        }
        if (mScaleAnimator != null && mScaleAnimator.isStarted()) {
            mScaleAnimator.cancel();
            mScaleAnimator = null;
        }
    }

    /**
     * 拡大・縮小を行います。
     *
     * @param newScale
     *            設定する拡大率
     */
    private void setScale(float newScale) {
        // INFO:childにscaleを設定しないと拡大率が変わらない現象に対処するための修正
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            final int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                final View targetView = getChildAt(i);
                targetView.setScaleX(newScale);
                targetView.setScaleY(newScale);
            }
        } else {
            setScaleX(newScale);
            setScaleY(newScale);
        }
    }

    /**
     * ドラッグ可能フラグ
     *
     * @param isDraggable
     *            ドラッグ可能にする場合はtrue
     */
    void setDraggable(boolean isDraggable) {
        mIsDraggable = isDraggable;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (mParams == null)
            return;
        if (enabled)
            mParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                    | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        else
            mParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        mWindowManager.updateViewLayout(this, mParams);
        final int size = getChildCount();
        for (int i = 0; i < size; i++) {
            getChildAt(i).setEnabled(enabled);
        }
    }

    void setAllowMoveEdge(boolean isAllowMoveEdge) {
        mIsAllowMoveEdge = isAllowMoveEdge;
        if (mIsAllowMoveEdge)
            moveToEdge(true);
    }

    public void updateAlpha(float alpha) {
        if (mParams == null)
            return;
        mParams.alpha = alpha;
        mWindowManager.updateViewLayout(this, mParams);
    }

    public void updateSize(float scale) {
        if (mParams == null)
            return;
        mParams.width = Math.round(mOriginWidth * scale);
        mParams.height = Math.round(mOriginHeight * scale);
        MyLog.i("way", "updateSize mParams.width = " + mParams.width + ", mParams.height = " + mParams.height);
        mWindowManager.updateViewLayout(this, mParams);
    }

    public void updateType(boolean smartHide) {
        if (mParams == null)
            return;
        mParams.type = smartHide ? WindowManager.LayoutParams.TYPE_TOAST : WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        // mWindowManager.updateViewLayout(this, mParams);
        mWindowManager.removeViewImmediate(this);
        mWindowManager.addView(this, mParams);
    }

    /**
     * 画面端をオーバーするマージンです。
     *
     * @param margin
     *            マージン
     */
    void setOverMargin(int margin) {
        mOverMargin = margin;
    }

    /**
     * Window上での描画領域を取得します。
     *
     * @param outRect
     *            変更を加えるRect
     */
    void getWindowDrawingRect(Rect outRect) {
        final int currentX = getXByTouch();
        final int currentY = getYByTouch();
        outRect.set(currentX, currentY, currentX + getWidth(), currentY + getHeight());
    }

    /**
     * WindowManager.LayoutParamsを取得します。
     */
    WindowManager.LayoutParams getWindowLayoutParams() {
        return mParams;
    }

    /**
     * タッチ座標から算出されたFloatingViewのX座標
     *
     * @return FloatingViewのX座標
     */
    private int getXByTouch() {
        return (int) (mScreenTouchX - mLocalTouchX);
    }

    /**
     * タッチ座標から算出されたFloatingViewのY座標
     *
     * @return FloatingViewのY座標
     */
    private int getYByTouch() {
        return (int) (mMetrics.heightPixels - (mScreenTouchY - mLocalTouchY + getHeight()));
    }

    public void addToWindow() {
        //if (!mPreferences.getBoolean(SettingsFragment.KEY_FLOAT_VIEW_TOGGLE, true))
        //	return;
        mWindowManager.addView(this, mParams);
    }

    public void removeFromWindow() {
        //if (!mPreferences.getBoolean(SettingsFragment.KEY_FLOAT_VIEW_TOGGLE, true))
        //	return;
        mWindowManager.removeViewImmediate(this);
        mPreferences.edit().putInt(X_POSITION, mParams.x).apply();
        mPreferences.edit().putInt(Y_POSITION, mParams.y).apply();
    }

}
