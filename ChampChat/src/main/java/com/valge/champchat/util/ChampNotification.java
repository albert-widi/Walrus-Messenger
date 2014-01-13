package com.valge.champchat.util;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.valge.champchat.MessagingActivity;
import com.valge.champchat.R;

/**
 * Created by Albert Widiatmoko on 09/12/13.
 */
public class ChampNotification {

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void setNotification(int friendId, String friendName, String friendPhoneNumber, byte[] friendPublicKey, Context context) {
        System.out.println("Set notification from intentservice");
        SharedPrefsUtil sharedPrefsUtil = new SharedPrefsUtil(context);
        Intent intent = new Intent(context, MessagingActivity.class);
        //Intent upIntent = new Intent(context, ChatActivity.class);
        intent.putExtra(IntentExtrasUtil.XTRAS_FRIEND_USER_ID, friendId);
        intent.putExtra(IntentExtrasUtil.XTRAS_FRIEND_NAME, friendName);
        intent.putExtra(IntentExtrasUtil.XTRAS_FRIEND_PHONENUMBER, friendPhoneNumber);
        intent.putExtra(IntentExtrasUtil.XTRAS_FRIEND_PUBLICKEY, friendPublicKey);

        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(context);
        //taskStackBuilder.addNextIntentWithParentStack(upIntent);
        taskStackBuilder.addParentStack(MessagingActivity.class);
        taskStackBuilder.addNextIntent(intent);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.my_app_icon)
                        .setContentTitle("New Message")
                        .setContentText("New message from " + friendName);

        PendingIntent resultPendingIntent = taskStackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT );

        mBuilder.setContentIntent(resultPendingIntent);
        mBuilder.setAutoCancel(true);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(101, mBuilder.build());

        if(sharedPrefsUtil.isNotificationSoundOn()) {
            MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.notif);
            mediaPlayer.start();
        }
    }
}
