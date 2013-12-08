package com.valge.champchat.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

public class SharedPrefsUtil {
	//prefs
    public static final String PREFS_NAME = "EAndroidIMPrefs";
    //prefs key
    public static final String KEY_APP_ACTIV = "APPACTI";
    public static final String KEY_USER_NAME = "USERNAME";
    public static final String KEY_PRIVATE_KEY = "PRIVATEKEY";
    public static final String KEY_HAVE_FRIENDS = "HAVEFRIENDS";
    public static final String KEY_USER_ID = "USERID";
    public static final String PROPERTY_REG_ID = "gcm_id";

    private Context context;

    public String userName;
    public byte[] privateKey;
    public boolean appActivated;

    public SharedPrefsUtil(Context context) {
        appActivated = false;
        this.context = context;
    }

    public void loadApplicationPrefs() {
        SharedPreferences sprefs = context.getSharedPreferences(SharedPrefsUtil.PREFS_NAME, context.MODE_PRIVATE);
        appActivated = sprefs.getBoolean(SharedPrefsUtil.KEY_APP_ACTIV, false);
        userName = sprefs.getString(SharedPrefsUtil.KEY_USER_NAME, "");
        privateKey = Base64.decode(sprefs.getString(SharedPrefsUtil.KEY_PRIVATE_KEY, ""), Base64.DEFAULT);
    }
}
