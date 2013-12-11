package com.valge.champchat.gcm_package;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.google.android.gms.gcm.GoogleCloudMessaging;


public class GCMBroadcastReceiver extends WakefulBroadcastReceiver{
        BroadcastReceiver passBroadCast;

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            // Explicitly specify that GcmIntentService will handle the intent.
            /*System.out.println("Do something woi");
            System.out.println("NAKED INTENT : " + intent.getExtras().toString());
            ComponentName comp = new ComponentName(context.getPackageName(), GCMIntentService.class.getName());
            Intent gcmIntent = new Intent(context, GCMIntentService.class);
            gcmIntent.putExtras(intent.getExtras());*/
            GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
            Bundle extras = intent.getExtras();
            String messageType = gcm.getMessageType(intent);

            if(!extras.isEmpty()) {
                if(GoogleCloudMessaging. MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                    System.out.println("Broadcast Receiver: Get data from GCM");
                    Intent localIntent = new Intent("messageIntent");
                    String message = intent.getStringExtra("message");
                    String messageKey = intent.getStringExtra("key");
                    String messageHash = intent.getStringExtra("hash");
                    String senderId = intent.getStringExtra("whosent");

                    //debug
                    System.out.println("Broadcast Receiver: Data from GCM:");
                    System.out.println("==================================");
                    System.out.println("Message :" + message);
                    System.out.println("Message Key :" + messageKey);
                    System.out.println("Message Hash :" + messageHash);
                    System.out.println("Who Sent :" + senderId);
                    System.out.println("==================================");

                    localIntent.putExtra("message", message);
                    localIntent.putExtra("messagekey", messageKey);
                    localIntent.putExtra("messagehash", messageHash);
                    localIntent.putExtra("senderid", senderId);
                    LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent);
                }
            }
            /*// Start the service, keeping the device awake while it is launching.
            startWakefulService(context, (intent.setComponent(comp)));
            setResultCode(Activity.RESULT_OK);*/
	    }
}
