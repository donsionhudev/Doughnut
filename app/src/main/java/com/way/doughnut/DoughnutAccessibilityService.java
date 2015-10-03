package com.way.doughnut;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import com.way.doughnut.util.ConstantValues;

public class DoughnutAccessibilityService extends AccessibilityService {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (TextUtils.equals(ConstantValues.ACTION_BACK, action))
                performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
            else if (TextUtils.equals(ConstantValues.ACTION_HOME, action))
                performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);
            else if (TextUtils.equals(ConstantValues.ACTION_RECENTS, action))
                performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS);
            else if (TextUtils.equals(ConstantValues.ACTION_OPEN_NOTIFICATIONS, action))
                performGlobalAction(AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS);
            else if (TextUtils.equals(ConstantValues.ACTION_POWER_MENU, action))
                performGlobalAction(AccessibilityService.GLOBAL_ACTION_POWER_DIALOG);
            else if (TextUtils.equals(ConstantValues.ACTION_QUICK_SETTINGS, action))
                performGlobalAction(AccessibilityService.GLOBAL_ACTION_QUICK_SETTINGS);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, R.string.accesibility_service_close, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onInterrupt() {
        // TODO Auto-generated method stub

    }

}
