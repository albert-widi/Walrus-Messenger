package com.valge.champchat;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.valge.champchat.util.ActivityLocationSharedPrefs;
import com.valge.champchat.util.SharedPrefsUtil;

public class SettingsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
        Context context = getApplicationContext();
        ActivityLocationSharedPrefs activityLocationSharedPrefs = new ActivityLocationSharedPrefs(context);
        activityLocationSharedPrefs.saveLastActivityToNonChat();
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_settings, container, false);
            return rootView;
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        final SharedPrefsUtil sharedPrefsUtil = new SharedPrefsUtil(getApplicationContext());
        CheckBox historyCheckBox = (CheckBox) findViewById(R.id.settings_message_history);
        CheckBox notifSoundCheckBox = (CheckBox) findViewById(R.id.settings_notification_sound);
        CheckBox messageSoundCheckBox = (CheckBox) findViewById(R.id.settings_messaging_sound);

        if(sharedPrefsUtil.isMessageHistoryOn()) {
            historyCheckBox.setChecked(true);
        }
        else {
            historyCheckBox.setChecked(false);
        }
        if(sharedPrefsUtil.isNotificationSoundOn()) {
            notifSoundCheckBox.setChecked(true);
        }
        else {
            notifSoundCheckBox.setChecked(false);
        }
        if(sharedPrefsUtil.isMessagingSoundOn()) {
            messageSoundCheckBox.setChecked(true);
        }
        else {
            messageSoundCheckBox.setChecked(false);
        }

        historyCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    sharedPrefsUtil.setHistoryOn();
                }
                else {
                    sharedPrefsUtil.setHistoryOff();
                }
            }
        });

        notifSoundCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    sharedPrefsUtil.setNotificationSoundOn();
                }
                else {
                    sharedPrefsUtil.setNotificationSoundOff();
                }
            }
        });

        messageSoundCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    sharedPrefsUtil.setMessagingSoundOn();
                }
                else {
                    sharedPrefsUtil.setMessagingSoundOff();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("This is on resume on Settings activity");
    }

    @Override
    protected void onPause() {
        super.onPause();
        System.out.println("This is on pause on Settings activity");
    }

    @Override
    protected void onStop() {
        super.onStop();
        System.out.println("This is on stop on Settings activity");
    }
}
