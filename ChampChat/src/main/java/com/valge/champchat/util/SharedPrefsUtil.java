package com.valge.champchat.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;

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
    public static final String KEY_SECRET_KEY = "SECRETKEY";
    public static final String KEY_ACTIVITY_LOCATION = "ACTIVITYLOCATION";

    //receiver
    public static final String KEY_NOTIFICATION_MODE = "NOTIFICATIONMODE";
    public static final String KEY_TESTER_MODE = "TESTERMODE";

    //settings
    public static final String KEY_HISTORY = "HISTORY";
    public static final String KEY_SOUND_NOTIFICATION = "NOTIFSOUND";
    public static final String KEY_SOUND_MESSAGE = "MESSAGESOUND";

    private Context context;

    public int userId;
    public String userName;
    public String phoneNumber;
    public byte[] publicKey;
    public byte[] privateKey;
    public String secretKey;
    public boolean appActivated = false;

    public SharedPrefsUtil(Context context) {
        this.context = context;
    }

    public void saveApplicationPrefs() {

    }

    public void loadApplicationPrefs() {
        SharedPreferences sprefs = context.getSharedPreferences(SharedPrefsUtil.PREFS_NAME, context.MODE_PRIVATE);
        appActivated = sprefs.getBoolean(SharedPrefsUtil.KEY_APP_ACTIV, false);
        userId = sprefs.getInt(SharedPrefsUtil.KEY_USER_ID, 0);
        userName = sprefs.getString(SharedPrefsUtil.KEY_USER_NAME, "");
        phoneNumber = sprefs.getString(SharedPrefsUtil.KEY_PHONE_NUMBER, "");
        privateKey = Base64.decode(sprefs.getString(SharedPrefsUtil.KEY_PRIVATE_KEY, ""), Base64.DEFAULT);
        secretKey = sprefs.getString(SharedPrefsUtil.KEY_SECRET_KEY, "");
    }

    public PrivateKey getUserPrivateKey() {
        SharedPreferences sprefs = context.getSharedPreferences(SharedPrefsUtil.PREFS_NAME, context.MODE_PRIVATE);
        byte[] privateKey = Base64.decode(sprefs.getString(SharedPrefsUtil.KEY_PRIVATE_KEY, ""), Base64.DEFAULT);
        PrivateKey userPrivateKey = null;
        try{
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKey);
            return KeyFactory.getInstance("RSA").generatePrivate(keySpec);
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        return userPrivateKey;
    }

    public String getUserSecretKey() {
        SharedPreferences sprefs = context.getSharedPreferences(SharedPrefsUtil.PREFS_NAME, context.MODE_PRIVATE);
        String secretKey = sprefs.getString(SharedPrefsUtil.KEY_SECRET_KEY, "");
        return secretKey;
    }

    public void setToReceiveMode() {
        System.out.println("This is receive mode");
        SharedPreferences sprefs = context.getSharedPreferences(SharedPrefsUtil.PREFS_NAME, context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sprefs.edit();
        editor.putBoolean(SharedPrefsUtil.KEY_NOTIFICATION_MODE, false);
        editor.commit();
    }

    public void setToNotificationMode() {
        System.out.println("Set to notif mode");
        SharedPreferences sprefs = context.getSharedPreferences(SharedPrefsUtil.PREFS_NAME, context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sprefs.edit();
        editor.putBoolean(SharedPrefsUtil.KEY_NOTIFICATION_MODE, true);
        editor.commit();
    }

    public boolean isNotificationModeOn() {
        SharedPreferences sprefs = context.getSharedPreferences(SharedPrefsUtil.PREFS_NAME, context.MODE_PRIVATE);
        boolean isNotifMode = sprefs.getBoolean(SharedPrefsUtil.KEY_NOTIFICATION_MODE, true);

        if(isNotifMode) {
            System.out.println("This is notif mode");
        }
        else {
            System.out.println("This is receive mode");
        }
        return isNotifMode;
    }

    public void setToTesterModeOn() {
        System.out.println("Set to tester mode");
        SharedPreferences sprefs = context.getSharedPreferences(SharedPrefsUtil.PREFS_NAME, context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sprefs.edit();
        editor.putBoolean(SharedPrefsUtil.KEY_TESTER_MODE, true);
        editor.commit();
    }

    public void setTesterModeOff() {
        System.out.println("Set to non tester mode");
        SharedPreferences sprefs = context.getSharedPreferences(SharedPrefsUtil.PREFS_NAME, context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sprefs.edit();
        editor.putBoolean(SharedPrefsUtil.KEY_TESTER_MODE, false);
        editor.commit();
    }

    public boolean isTesterMode() {
        SharedPreferences sprefs = context.getSharedPreferences(SharedPrefsUtil.PREFS_NAME, context.MODE_PRIVATE);
        boolean isTesterMode = sprefs.getBoolean(SharedPrefsUtil.KEY_TESTER_MODE, true);

        return isTesterMode;
    }

    public void setHistoryOn() {
        SharedPreferences sprefs = context.getSharedPreferences(SharedPrefsUtil.PREFS_NAME, context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sprefs.edit();
        editor.putBoolean(SharedPrefsUtil.KEY_HISTORY, true);
        editor.commit();
    }

    public void setHistoryOff() {
        SharedPreferences sprefs = context.getSharedPreferences(SharedPrefsUtil.PREFS_NAME, context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sprefs.edit();
        editor.putBoolean(SharedPrefsUtil.KEY_HISTORY, false);
        editor.commit();
    }

    public boolean isMessageHistoryOn() {
        SharedPreferences sprefs = context.getSharedPreferences(SharedPrefsUtil.PREFS_NAME, context.MODE_PRIVATE);
        boolean status = sprefs.getBoolean(SharedPrefsUtil.KEY_HISTORY, true);

        return status;
    }

    public void setNotificationSoundOn() {
        SharedPreferences sprefs = context.getSharedPreferences(SharedPrefsUtil.PREFS_NAME, context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sprefs.edit();
        editor.putBoolean(SharedPrefsUtil.KEY_SOUND_NOTIFICATION, true);
        editor.commit();
    }

    public void setNotificationSoundOff() {
        SharedPreferences sprefs = context.getSharedPreferences(SharedPrefsUtil.PREFS_NAME, context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sprefs.edit();
        editor.putBoolean(SharedPrefsUtil.KEY_SOUND_NOTIFICATION, false);
        editor.commit();
    }

    public boolean isNotificationSoundOn() {
        SharedPreferences sprefs = context.getSharedPreferences(SharedPrefsUtil.PREFS_NAME, context.MODE_PRIVATE);
        boolean status = sprefs.getBoolean(SharedPrefsUtil.KEY_SOUND_NOTIFICATION, true);

        return status;
    }

    public void setMessagingSoundOn() {
        SharedPreferences sprefs = context.getSharedPreferences(SharedPrefsUtil.PREFS_NAME, context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sprefs.edit();
        editor.putBoolean(SharedPrefsUtil.KEY_SOUND_MESSAGE, true);
        editor.commit();
    }

    public void setMessagingSoundOff() {
        SharedPreferences sprefs = context.getSharedPreferences(SharedPrefsUtil.PREFS_NAME, context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sprefs.edit();
        editor.putBoolean(SharedPrefsUtil.KEY_SOUND_MESSAGE, false);
        editor.commit();
    }

    public boolean isMessagingSoundOn() {
        SharedPreferences sprefs = context.getSharedPreferences(SharedPrefsUtil.PREFS_NAME, context.MODE_PRIVATE);
        boolean status = sprefs.getBoolean(SharedPrefsUtil.KEY_SOUND_MESSAGE, true);

        return status;
    }
}
