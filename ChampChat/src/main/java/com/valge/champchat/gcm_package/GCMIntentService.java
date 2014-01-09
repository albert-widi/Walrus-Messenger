package com.valge.champchat.gcm_package;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.valge.champchat.ChatActivity;
import com.valge.champchat.MessagingActivity;
import com.valge.champchat.R;
import com.valge.champchat.util.ChampNotification;
import com.valge.champchat.util.DbAdapter;
import com.valge.champchat.util.EncryptionUtil;
import com.valge.champchat.util.IntentExtrasUtil;
import com.valge.champchat.util.SharedPrefsUtil;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by Albert Widiatmoko on 29/11/13.
 */
public class GCMIntentService extends IntentService {
    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;

    public final static String TAG = "GCMIntentServiceTag";

    public GCMIntentService() {
        super("GCMIntentService");
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // TODO Auto-generated method stub
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);

        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that GCM
             * will be extended in the future with new message types, just ignore
             * any message types you're not interested in, or that you don't
             * recognize.
             */
            if (GoogleCloudMessaging.
                    MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                //sendNotification("Send error: " + extras.toString());
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_DELETED.equals(messageType)) {
                //sendNotification("Deleted messages on server: " + extras.toString());
                // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging. MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                System.out.println("Launching notification");

                processIncomingMessage(intent);
            }
        }
    }

    private void processIncomingMessage(Intent intent) {
        System.out.println("Broadcast Receiver: Get data from GCM");
        boolean friendExists = false;
        boolean friendFound = false;
        String message = intent.getStringExtra("message");
        String messageKey = intent.getStringExtra("key");
        String messageHash = intent.getStringExtra("hash");
        int friendId = Integer.parseInt(intent.getStringExtra("whosent"));
        String friendName = " ";
        byte[] friendPublicKey = null;
        String friendPhoneNumber = " ";

        //debug
        System.out.println("Broadcast Receiver: Data from GCM:");
        System.out.println("==================================");
        System.out.println("Message :" + message);
        System.out.println("Message Key :" + messageKey);
        System.out.println("Message Hash :" + messageHash);
        System.out.println("Who Sent :" + friendId);
        System.out.println("==================================");

        DbAdapter asyncDbAdapter = new DbAdapter(getApplicationContext());
        GregorianCalendar gCalendar = new GregorianCalendar();
        EncryptionUtil encryptionUtil = new EncryptionUtil();

        //load friend info
        Cursor friendDataCursor = asyncDbAdapter.getFriendInfo(friendId);
        SharedPrefsUtil sharedPrefsUtil = new SharedPrefsUtil(getApplicationContext());
        int history = 0;

        if(sharedPrefsUtil.isMessageHistoryOn()) {
            history = 1;
        }

        if(friendDataCursor.getCount() > 0) {
            friendExists = true;
            friendDataCursor.moveToFirst();
            friendName = friendDataCursor.getString(friendDataCursor.getColumnIndex(DbAdapter.DbHelper.COLUMN_FRIEND_NAME));
            friendPublicKey = friendDataCursor.getBlob(friendDataCursor.getColumnIndex(DbAdapter.DbHelper.COLUMN_FRIEND_PUBLIC_KEY));
            friendPhoneNumber = friendDataCursor.getString(friendDataCursor.getColumnIndex(DbAdapter.DbHelper.COLUMN_FRIEND_PHONE_NUMBER));
        }
        else {
            //disable message coming-in from unknown source
            //if tester mode active, application can receive all message delivered from server
            if(!sharedPrefsUtil.isTesterMode()) {
                return;
            }
        }
        friendDataCursor.close();

        //date-time
        String date = gCalendar.get(Calendar.DATE) + "-" + gCalendar.get(Calendar.MONTH)+1 + "-" + gCalendar.get(Calendar.YEAR);
        String time = gCalendar.get(Calendar.HOUR) + ":" + gCalendar.get(Calendar.MINUTE);

        String originalMessage = encryptionUtil.decryptMessage(message, messageKey, messageHash, getApplicationContext());
        System.out.println("Process Message: Original Message = " + originalMessage);

        //save message to db
        long insertId = 0;
        if(friendExists) {
            if(!sharedPrefsUtil.isTesterMode()) {
                insertId = asyncDbAdapter.saveMessage(friendId, friendPhoneNumber, friendName, originalMessage, date, time, "", "1", history);
                System.out.println("Receive insert id : " + insertId);
                if(insertId != -1) {
                    System.out.println("Processing chat activity : Save message success");
                }
                else {
                    System.out.println("Processing chat activity : Save message failed");
                }

                //save chat thread
                asyncDbAdapter.saveChatThread(friendId);
            }
        }

        if(sharedPrefsUtil.isTesterMode()) {
            System.out.println("Goin to tester mode");
            Intent messagingIntent = new Intent("messagingtester");
            messagingIntent.putExtra("message", originalMessage);
            if(friendName.equals(friendPhoneNumber)) {
                messagingIntent.putExtra("name", String.valueOf(friendId));
            }
            else {
                messagingIntent.putExtra("name", friendName);
            }
            messagingIntent.putExtra("date", date);
            messagingIntent.putExtra("time", time);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(messagingIntent);
        }
        else if(!sharedPrefsUtil.isNotificationModeOn()) {
            Intent messagingIntent = new Intent("messagingactiv");
            messagingIntent.putExtra("message", originalMessage);
            messagingIntent.putExtra("id", friendId);
            messagingIntent.putExtra("name", friendName);
            messagingIntent.putExtra("phonenumber", friendPhoneNumber);
            messagingIntent.putExtra("publickey", friendPublicKey);
            messagingIntent.putExtra("date", date);
            messagingIntent.putExtra("time", time);
            messagingIntent.putExtra("insertid", insertId);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(messagingIntent);
        }
        else {
            System.out.println("Sending notification");
            ChampNotification champNotification = new ChampNotification();
            champNotification.setNotification(friendId, friendName, friendPhoneNumber, friendPublicKey, getApplicationContext());
            //setNotification(friendId, friendName, friendPhoneNumber, friendPublicKey);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void setNotification(int friendId, String friendName, String friendPhoneNumber, byte[] friendPublicKey ) {
        System.out.println("Set notification from intentservice");
        Intent intent = new Intent(this, MessagingActivity.class);
        intent.putExtra(IntentExtrasUtil.XTRAS_FRIEND_USER_ID, friendId);
        intent.putExtra(IntentExtrasUtil.XTRAS_FRIEND_NAME, friendName);
        intent.putExtra(IntentExtrasUtil.XTRAS_FRIEND_PHONENUMBER, friendPhoneNumber);
        intent.putExtra(IntentExtrasUtil.XTRAS_FRIEND_PUBLICKEY, friendPublicKey);


        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(this);
        taskStackBuilder.addParentStack(ChatActivity.class);
        taskStackBuilder.addNextIntent(intent);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
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
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(101, mBuilder.build());
    }
}

