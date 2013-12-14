package com.valge.champchat.gcm_package;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.google.android.gms.gcm.GoogleCloudMessaging;


public class GCMBroadcastReceiver extends WakefulBroadcastReceiver{
        BroadcastReceiver passBroadCast;

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            // Explicitly specify that GcmIntentService will handle the intent.
            System.out.println("Do something woi");
            System.out.println("NAKED INTENT : " + intent.getExtras().toString());
            ComponentName comp = new ComponentName(context.getPackageName(), GCMIntentService.class.getName());
            Intent gcmIntent = new Intent(context, GCMIntentService.class);
            gcmIntent.putExtras(intent.getExtras());
            GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);


            // Start the service, keeping the device awake while it is launching.
            startWakefulService(context, (intent.setComponent(comp)));
            setResultCode(Activity.RESULT_OK);
	    }

    private void processReceivedMessage() {

    }
}
