package com.way.doughnut.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.way.doughnut.R;

public class HelpActivity extends Activity {
    public static final String DEVICE_ADMIN_ACTION = "device_admin_action";
    public static final String ACCESSIBILITY_ACTION = "accessibility_action";
    public static final String FIRST_TUTORIAL_ACTION = "first_tutorial_action";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent() == null) {
            finish();
            return;
        }
        getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        setContentView(R.layout.help_activity_layout);
        ImageView iv = (ImageView) findViewById(R.id.tutorial_float_view);
        TextView tv = (TextView) findViewById(R.id.desc_text);

        String action = getIntent().getAction();
        switch (action) {
            case DEVICE_ADMIN_ACTION:
                iv.setVisibility(View.GONE);
                tv.setText(R.string.activity_device_admin_reason);
                break;
            case ACCESSIBILITY_ACTION:
                iv.setVisibility(View.GONE);
                tv.setText(R.string.accesibility_service);
                break;
            case FIRST_TUTORIAL_ACTION:
                tv.setVisibility(View.GONE);
                iv.setVisibility(View.VISIBLE);
                break;

            default:
                finish();
                break;
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN)
            finish();
        return super.onTouchEvent(event);
    }
}
