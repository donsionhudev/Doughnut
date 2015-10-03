package com.way.doughnut.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;

import com.way.doughnut.MainService;
import com.way.doughnut.R;
import com.way.doughnut.R.drawable;
import com.way.doughnut.R.id;
import com.way.doughnut.R.layout;
import com.way.doughnut.R.string;
import com.way.view.DiscreteSeekBarPreference;

public class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {
    public static final int DEFAULT_VIBRATE_LEVEL = 20;
    public static final String KEY_FLOAT_VIEW_TOGGLE = "key_switch_float_view";
    public static final String KEY_AUTO_SIDE_MODEL = "key_switch_float_view_auto_side_model";
    public static final String KEY_SMART_HIDE = "key_switch_float_view_smart_hide";
    public static final String KEY_AUTO_BOOT = "key_switch_float_view_boot";
    public static final String KEY_VIBRATOR_LEVEL = "key_vibrator_level";
    public static final String KEY_FLOAT_VIEW_THEME = "key_float_view_theme";
    public static final String KEY_FLOAT_VIEW_ALPHA = "key_float_view_tran";
    public static final String KEY_FLOAT_VIEW_SIZE = "key_float_view_size";
    public static final String KEY_FLOAT_VIEW_QUESTION = "key_float_view_questions";
    public static final int[] THEME_ICON_RES = {drawable.theme_captain, drawable.theme_default,
            drawable.theme_doughnut, drawable.theme_halo, drawable.theme_helloketty, drawable.theme_rainbow,
            drawable.theme_smile, drawable.theme_trans_helloketty};
    private static final String KEY_VERSION = "key_float_view_version";
    AlertDialog dialog = null;
    private Activity mContext;
    private int mPickerVibrateValue;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        addPreferencesFromResource(R.xml.doughnut_settings);
        mContext = getActivity();
        PackageManager packageManager = mContext.getPackageManager();
        String packageName = mContext.getPackageName();
        // Update the version number
        try {
            final PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
            findPreference(KEY_VERSION).setSummary(packageInfo.versionName);
        } catch (final NameNotFoundException e) {
            findPreference(KEY_VERSION).setSummary("?");
        }
        DiscreteSeekBarPreference alphaPreference = (DiscreteSeekBarPreference) findPreference(KEY_FLOAT_VIEW_ALPHA);
        alphaPreference
                .setValue(PreferenceManager.getDefaultSharedPreferences(mContext).getInt(KEY_FLOAT_VIEW_ALPHA, 80));
        DiscreteSeekBarPreference sizePreference = (DiscreteSeekBarPreference) findPreference(KEY_FLOAT_VIEW_SIZE);
        sizePreference
                .setValue(PreferenceManager.getDefaultSharedPreferences(mContext).getInt(KEY_FLOAT_VIEW_SIZE, 100));
        register(alphaPreference, sizePreference);
    }

    protected void register(Preference... preferences) {
        for (Preference p : preferences) {
            p.setOnPreferenceChangeListener(this);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        String key = preference.getKey();
        switch (key) {
            case KEY_VIBRATOR_LEVEL:
                vibrationPicker(mContext);
                break;
            case KEY_FLOAT_VIEW_THEME:
                themePicker(mContext);
                break;
            case KEY_FLOAT_VIEW_TOGGLE:
                Intent intent = new Intent(mContext, MainService.class);
                boolean isOpen = PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean(key, true);
                if (isOpen)
                    mContext.startService(intent);
                else
                    mContext.stopService(intent);
                break;
            default:
                break;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private void themePicker(final Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final SharedPreferences.Editor editor = prefs.edit();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        GridView view = (GridView) LayoutInflater.from(context).inflate(layout.theme_picker, null);
        view.setAdapter(new ThemeAdapter());
        view.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (dialog != null)
                    dialog.cancel();
                editor.putInt(KEY_FLOAT_VIEW_THEME, position).apply();
            }
        });
        builder.setTitle(string.pref_float_view_theme);
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.setView(view);
        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

    private void vibrationPicker(final Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final SharedPreferences.Editor editor = prefs.edit();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        View view = LayoutInflater.from(context).inflate(layout.number_picker, null);
        NumberPicker numberPicker = (NumberPicker) view.findViewById(id.numberPicker1);
        numberPicker.setDescendantFocusability(393216);
        numberPicker.setMaxValue(9);
        mPickerVibrateValue = prefs.getInt(KEY_VIBRATOR_LEVEL, DEFAULT_VIBRATE_LEVEL);
        numberPicker.setValue(mPickerVibrateValue / 5);
        numberPicker.setOnValueChangedListener(new OnValueChangeListener() {

            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                mPickerVibrateValue = newVal * 5;
                vibrator.vibrate(mPickerVibrateValue);
            }
        });
        builder.setTitle(string.vibrator_level);
        builder.setView(view);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                editor.putInt(KEY_VIBRATOR_LEVEL, mPickerVibrateValue).commit();
            }
        });
        builder.setNeutralButton(string.vibrator_level_default, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                editor.putInt(KEY_VIBRATOR_LEVEL, DEFAULT_VIBRATE_LEVEL).commit();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        preference.getSharedPreferences().edit().putInt(preference.getKey(), (int) newValue).apply();
        return false;
    }

    private class ThemeAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return THEME_ICON_RES.length;
        }

        @Override
        public Integer getItem(int position) {
            return THEME_ICON_RES[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = LayoutInflater.from(mContext).inflate(layout.theme_item, parent, false);
            ((ImageView) convertView).setImageResource(getItem(position));
            return convertView;
        }

    }
}
