package com.way.doughnut;

import android.app.Application;
import android.content.Intent;
import android.preference.PreferenceManager;

import com.way.doughnut.fragment.SettingsFragment;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(SettingsFragment.KEY_FLOAT_VIEW_TOGGLE,
                true))
            startService(new Intent(this, MainService.class));
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }
}
