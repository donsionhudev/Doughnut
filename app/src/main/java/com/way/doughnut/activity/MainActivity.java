package com.way.doughnut.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;

import com.way.doughnut.R.id;
import com.way.doughnut.R.layout;
import com.way.explosionfield.ExplosionField;
import com.way.view.SwipeHelper;
import com.way.view.SwipeVerticalLayout;
import com.way.view.SwipeVerticalLayout.Callback;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    int pickerValue;
    private ExplosionField mExplosionField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_main);
        mExplosionField = ExplosionField.attach2Window(this);
        findViewById(id.enable_device_admin_btn).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                vibrationPicker(MainActivity.this);
            }
        });
        final SwipeVerticalLayout mLayout = (SwipeVerticalLayout) findViewById(id.swipe_layout);
        final ImageView icon = (ImageView) findViewById(id.icon);
        mLayout.setCallback(new Callback() {

            @Override
            public boolean updateSwipeProgress(View animView, boolean dismissable, float swipeProgress) {
                // TODO Auto-generated method stub
                Log.i("way", "swipeProgress = " + swipeProgress);
                return false;
            }

            @Override
            public void onChildDismissed(View v, int direction) {
                // TODO Auto-generated method stub
                switch (direction) {
                    case SwipeHelper.SWIPE_TO_TOP:
                        mExplosionField.explode(icon);
                        mLayout.setVisibility(View.GONE);
                        break;

                    default:
                        break;
                }
            }
        });

    }

    private void vibrationPicker(final Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final SharedPreferences.Editor editor = prefs.edit();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        View view = LayoutInflater.from(context).inflate(layout.number_picker, new LinearLayout(context));
        NumberPicker numberPicker = (NumberPicker) view.findViewById(id.numberPicker1);
        numberPicker.setDescendantFocusability(393216);
        numberPicker.setMaxValue(9);
        pickerValue = prefs.getInt("vibrate_strength", 20);
        numberPicker.setValue(pickerValue / 5);
        numberPicker.setOnValueChangedListener(new OnValueChangeListener() {

            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                pickerValue = newVal * 5;
                vibrator.vibrate(pickerValue);
            }
        });
        builder.setTitle("振动");
        builder.setView(view);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                editor.putInt("vibrate_strength", pickerValue).commit();
                if (pickerValue == 0) {
                    editor.putBoolean("vibrate_feedback", false).commit();
                } else
                    editor.putBoolean("vibrate_feedback", true).commit();
            }
        });
        builder.setNeutralButton("默认", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                editor.putInt("vibrate_strength", 20).commit();
                editor.putBoolean("vibrate_feedback", true).commit();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }
}
