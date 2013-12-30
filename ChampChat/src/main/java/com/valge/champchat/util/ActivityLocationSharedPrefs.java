package com.valge.champchat.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Albert Widiatmoko on 09/12/13.
 */
public class ActivityLocationSharedPrefs {
    Context context;
    SharedPreferences prefs;

    public ActivityLocationSharedPrefs(Context context) {
        this.context = context;
        prefs = context.getSharedPreferences(SharedPrefsUtil.PREFS_NAME, context.MODE_PRIVATE);
    }

    public void saveLastActivityToNonChat() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(SharedPrefsUtil.KEY_ACTIVITY_LOCATION, false);
        editor.commit();
    }

    public void saveLastActivityToChat() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(SharedPrefsUtil.KEY_ACTIVITY_LOCATION, true);
        editor.commit();
    }

    public boolean isChatActivityActive() {
        if(prefs.getBoolean(SharedPrefsUtil.KEY_ACTIVITY_LOCATION, false)) {
            return true;
        }
        return false;
    }

    public void saveLastActivityToTester() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(SharedPrefsUtil.KEY_TESTER_MODE, true);
        editor.commit();
    }

    public void saveLastActivityToNonTester() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(SharedPrefsUtil.KEY_TESTER_MODE, false);
        editor.commit();
    }

    public boolean isTesterActivityActive() {
        if(prefs.getBoolean(SharedPrefsUtil.KEY_TESTER_MODE, false)) {
            return true;
        }
        return false;
    }
}
