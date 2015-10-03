package com.way.doughnut.util;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.text.TextUtils;
import android.util.Log;

import com.way.doughnut.DoughnutAccessibilityService;
import com.way.doughnut.DoughnutDeviceAdminReceiver;
import com.way.doughnut.activity.EnableDeviceAdminActivity;
import com.way.doughnut.activity.HelpActivity;

public class Utils {
    private static final String TAG = "Utils";

    public static void execLockScreen(Context context) {
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context
                .getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName deviceAdminComponentName = new ComponentName(context, DoughnutDeviceAdminReceiver.class);
        if (devicePolicyManager.isAdminActive(deviceAdminComponentName)) {
            devicePolicyManager.lockNow();
        } else {
            enableDeviceAdmin(context);
        }
    }

    private static void enableDeviceAdmin(final Context context) {
        Intent screenRecordIntent = new Intent(context, EnableDeviceAdminActivity.class);
        screenRecordIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(screenRecordIntent);
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {

            @Override
            public void run() {
                showHelp(context, HelpActivity.DEVICE_ADMIN_ACTION);

            }
        }, 500L);
    }

    private static void enableAccessibilitySettings(final Context context) {
        Log.i("way", "enableAccessibilitySettings...");
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        context.startActivity(intent);
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {

            @Override
            public void run() {
                showHelp(context, HelpActivity.ACCESSIBILITY_ACTION);

            }
        }, 500L);
    }

    public static void showHelp(Context context, String action) {
        Intent toastIntent = new Intent(context, HelpActivity.class);
        toastIntent.setAction(action);
        toastIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        context.startActivity(toastIntent);
        Log.i("way", "showHelp...");
    }

    public static void execBackButton(Context context) {
        Log.i("way", "execBackButton...");
        if (!Utils.isAccessibilitySettingsOn(context)) {
            Log.i("way", "execBackButton... isAccessibilitySettingsOn");
            enableAccessibilitySettings(context);
            return;
        }
        context.startService(
                new Intent(context, DoughnutAccessibilityService.class).setAction(ConstantValues.ACTION_BACK));
    }

    public static void execHomeButton(Context context) {
        if (!Utils.isAccessibilitySettingsOn(context)) {
            enableAccessibilitySettings(context);
            return;
        }
        context.startService(
                new Intent(context, DoughnutAccessibilityService.class).setAction(ConstantValues.ACTION_HOME));
    }

    public static void launchRecentApps(Context context) {
        if (!Utils.isAccessibilitySettingsOn(context)) {
            enableAccessibilitySettings(context);
            return;
        }
        context.startService(
                new Intent(context, DoughnutAccessibilityService.class).setAction(ConstantValues.ACTION_RECENTS));
    }

    public static void openNotifications(Context context) {
        if (!Utils.isAccessibilitySettingsOn(context)) {
            enableAccessibilitySettings(context);
            return;
        }
        context.startService(new Intent(context, DoughnutAccessibilityService.class)
                .setAction(ConstantValues.ACTION_OPEN_NOTIFICATIONS));
    }

    public static void openPowerMenu(Context context) {
        if (!Utils.isAccessibilitySettingsOn(context)) {
            enableAccessibilitySettings(context);
            return;
        }
        context.startService(
                new Intent(context, DoughnutAccessibilityService.class).setAction(ConstantValues.ACTION_POWER_MENU));
    }

    public static void openQuickSettings(Context context) {
        if (!Utils.isAccessibilitySettingsOn(context)) {
            enableAccessibilitySettings(context);
            return;
        }
        context.startService(new Intent(context, DoughnutAccessibilityService.class)
                .setAction(ConstantValues.ACTION_QUICK_SETTINGS));
    }

    // To check if accessibility service is enabled
    public static boolean isAccessibilitySettingsOn(Context mContext) {
        int accessibilityEnabled = 0;
        final String service = "com.way.doughnut/com.way.doughnut.DoughnutAccessibilityService";
        boolean accessibilityFound = false;
        try {
            accessibilityEnabled = Settings.Secure.getInt(mContext.getApplicationContext().getContentResolver(),
                    Settings.Secure.ACCESSIBILITY_ENABLED);
            MyLog.v(TAG, "accessibilityEnabled = " + accessibilityEnabled);
        } catch (SettingNotFoundException e) {
            MyLog.e(TAG, "Error finding setting, default accessibility to not found: " + e.getMessage());
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled == 1) {
            MyLog.v(TAG, "***ACCESSIBILIY IS ENABLED*** -----------------");
            String settingValue = Settings.Secure.getString(mContext.getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                TextUtils.SimpleStringSplitter splitter = mStringColonSplitter;
                splitter.setString(settingValue);
                while (splitter.hasNext()) {
                    String accessabilityService = splitter.next();

                    Log.v(TAG, "-------------- > accessabilityService :: " + accessabilityService);
                    if (accessabilityService.equalsIgnoreCase(service)) {
                        Log.v(TAG, "We've found the correct setting - accessibility is switched on!");
                        return true;
                    }
                }
            }
        } else {
            MyLog.v(TAG, "***ACCESSIBILIY IS DISABLED***");
        }

        return accessibilityFound;
    }
}
