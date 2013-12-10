package com.valge.champchat.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.X509EncodedKeySpec;

public class SharedPrefsUtil {
	//prefs
    public static final String PREFS_NAME = "EAndroidIMPrefs";
    //prefs key
    public static final String KEY_APP_ACTIV = "APPACTI";
    public static final String KEY_USER_NAME = "USERNAME";
    public static final String KEY_PRIVATE_KEY = "PRIVATEKEY";
    public static final String KEY_PHONE_NUMBER = "PHONENUMBER";
    public static final String KEY_HAVE_FRIENDS = "HAVEFRIENDS";
    public static final String KEY_USER_ID = "USERID";
    public static final String PROPERTY_REG_ID = "gcm_id";
    public static final String KEY_ACTIVITY_LOCATION = "ACTIVITYLOCATION";

    private Context context;

    public String userName;
    public String phoneNumber;
    public byte[] privateKey;
    public boolean appActivated = false;

    public SharedPrefsUtil(Context context) {
        this.context = context;
    }

    public void saveApplicationPrefs() {

    }

    public void loadApplicationPrefs() {
        SharedPreferences sprefs = context.getSharedPreferences(SharedPrefsUtil.PREFS_NAME, context.MODE_PRIVATE);
        appActivated = sprefs.getBoolean(SharedPrefsUtil.KEY_APP_ACTIV, false);
        userName = sprefs.getString(SharedPrefsUtil.KEY_USER_NAME, "");
        phoneNumber = sprefs.getString(SharedPrefsUtil.KEY_PHONE_NUMBER, "");
        privateKey = Base64.decode(sprefs.getString(SharedPrefsUtil.KEY_PRIVATE_KEY, ""), Base64.DEFAULT);
    }

    public PrivateKey getUserPrivateKey() {
        SharedPreferences sprefs = context.getSharedPreferences(SharedPrefsUtil.PREFS_NAME, context.MODE_PRIVATE);
        byte[] privateKey = Base64.decode(sprefs.getString(SharedPrefsUtil.KEY_PRIVATE_KEY, ""), Base64.DEFAULT);
        PrivateKey userPrivateKey = null;
        try{
            userPrivateKey = KeyFactory.getInstance("RSA").generatePrivate(new X509EncodedKeySpec(privateKey));
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        return userPrivateKey;
    }
}
