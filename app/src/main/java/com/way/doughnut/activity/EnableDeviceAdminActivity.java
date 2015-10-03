package com.way.doughnut.activity;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;

import com.way.doughnut.DoughnutDeviceAdminReceiver;
import com.way.doughnut.R;
import com.way.doughnut.util.MyLog;

public class EnableDeviceAdminActivity extends Activity {
    private static final int REQUEST_CODE_ENABLE_ADMIN = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enableDeviceAdmin();
        finish();
    }

    private void enableDeviceAdmin() {
        // Launch the activity to have the user enable our admin.
        ComponentName deviceAdminComponentName = new ComponentName(this, DoughnutDeviceAdminReceiver.class);
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, deviceAdminComponentName);
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, this.getString(R.string.add_admin_extra_app_text));
        startActivityForResult(intent, REQUEST_CODE_ENABLE_ADMIN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ENABLE_ADMIN)
            MyLog.i("EnableDeviceAdminActivity", "onActivityResult.... requestCode == REQUEST_CODE_ENABLE_ADMIN");
        finish();
    }

    @Override
    protected void onStop() {
        if (!isFinishing()) {
            finish();
        }
        super.onStop();
    }
}
