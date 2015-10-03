package com.way.doughnut;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;

import com.way.doughnut.fragment.SettingsFragment;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(SettingsFragment.KEY_AUTO_BOOT, true))
            context.startService(new Intent(context, MainService.class));
    }

}
