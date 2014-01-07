package com.valge.champchat.util;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.valge.champchat.ChatActivity;
import com.valge.champchat.MessagingActivity;
import com.valge.champchat.R;

/**
 * Created by Albert Widiatmoko on 09/12/13.
 */
public class ChampNotification {

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void setNotification(int friendId, String friendName, String friendPhoneNumber, byte[] friendPublicKey, Context context) {
        System.out.println("Set notification from intentservice");
        Intent intent = new Intent(context, MessagingActivity.class);
        intent.putExtra(IntentExtrasUtil.XTRAS_FRIEND_USER_ID, friendId);
        intent.putExtra(IntentExtrasUtil.XTRAS_FRIEND_NAME, friendName);
        intent.putExtra(IntentExtrasUtil.XTRAS_FRIEND_PHONENUMBER, friendPhoneNumber);
        intent.putExtra(IntentExtrasUtil.XTRAS_FRIEND_PUBLICKEY, friendPublicKey);


        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(context);
        taskStackBuilder.addParentStack(ChatActivity.class);
        taskStackBuilder.addNextIntent(intent);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("New Message")
                        .setContentText("New message from " + friendName);

        PendingIntent resultPendingIntent =
                taskStackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        mBuilder.setContentIntent(resultPendingIntent);
        mBuilder.setAutoCancel(true);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(101, mBuilder.build());
    }
}
