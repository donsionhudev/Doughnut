package com.way.doughnut.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewAnimationUtils;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import com.way.doughnut.R;
import com.way.doughnut.fragment.QuestionFragment;
import com.way.doughnut.fragment.SettingsFragment;
import com.way.view.ClipRevealFrame;

public class SettingsActivity extends AppCompatActivity {
    private View mRootLayout;
    private ClipRevealFrame mRevealLayout;
    private Dialog mTutorialDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_layout);
        // getActionBar().setElevation(0f);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction().replace(R.id.settings_fragment, new SettingsFragment()).commit();
            getFragmentManager().beginTransaction().replace(R.id.clip_revel_frame, new QuestionFragment()).commit();
        }

        mRootLayout = findViewById(R.id.root_layout);
        mRevealLayout = (ClipRevealFrame) findViewById(R.id.clip_revel_frame);
        mRevealLayout.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    onFabClick();
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(HelpActivity.FIRST_TUTORIAL_ACTION, true)) {
            showHelp();
            PreferenceManager.getDefaultSharedPreferences(this).edit()
                    .putBoolean(HelpActivity.FIRST_TUTORIAL_ACTION, false).apply();
        }
    }

    private void showHelp() {
        if (mTutorialDialog != null && mTutorialDialog.isShowing())
            return;
        mTutorialDialog = new Dialog(this, R.style.Theme_Dialog);
        View rootView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.tutorial_tips_layout, null);
        rootView.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (mTutorialDialog != null && mTutorialDialog.isShowing()) {
                        mTutorialDialog.cancel();
                        return true;
                    }
                }
                return false;
            }
        });
        mTutorialDialog.setContentView(rootView);
        mTutorialDialog.setCanceledOnTouchOutside(true);
        mTutorialDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_TOAST);
        mTutorialDialog.getWindow().setWindowAnimations(R.style.dialog_window_anim);
        mTutorialDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_help:
                onFabClick();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public int getDisplayWidth() {
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        // 获取屏幕信息
        windowManager.getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
    }

    private void onFabClick() {
        // int x = (v.getLeft() + v.getRight()) / 2;
        // int y = (v.getTop() + v.getBottom()) / 2;
        int x = getDisplayWidth();
        int y = 0;
        // float radiusOfFab = 1f * v.getWidth() / 2f;
        float radiusOfFab = 0f;
        float radiusFromFabToRoot = (float) Math.hypot(Math.max(x, mRootLayout.getWidth() - x),
                Math.max(y, mRootLayout.getHeight() - y));
        boolean isShow = mRevealLayout.getVisibility() == View.VISIBLE;
        if (isShow) {
            hideMenu(x, y, radiusFromFabToRoot, radiusOfFab);
        } else {
            showMenu(x, y, radiusOfFab, radiusFromFabToRoot);
        }
    }

    private void showMenu(int cx, int cy, float startRadius, float endRadius) {
        mRevealLayout.setVisibility(View.VISIBLE);
        Animator revealAnim = createCircularReveal(mRevealLayout, cx, cy, startRadius, endRadius);
        revealAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        revealAnim.setDuration(300);
        revealAnim.start();
    }

    private void hideMenu(int cx, int cy, float startRadius, float endRadius) {
        Animator revealAnim = createCircularReveal(mRevealLayout, cx, cy, startRadius, endRadius);
        revealAnim.setInterpolator(new DecelerateInterpolator());
        revealAnim.setDuration(300);
        revealAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mRevealLayout.setVisibility(View.GONE);
            }
        });
        revealAnim.start();

    }

    private Animator createCircularReveal(final ClipRevealFrame view, int x, int y, float startRadius,
                                          float endRadius) {
        final Animator reveal;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            reveal = ViewAnimationUtils.createCircularReveal(view, x, y, startRadius, endRadius);
        } else {
            view.setClipOutLines(true);
            view.setClipCenter(x, y);
            reveal = ObjectAnimator.ofFloat(view, "ClipRadius", startRadius, endRadius);
            reveal.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    view.setClipOutLines(false);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
        }
        return reveal;
    }
}
