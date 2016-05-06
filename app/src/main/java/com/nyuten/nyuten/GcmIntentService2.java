package com.nyuten.nyuten;


import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

public class GcmIntentService2 extends IntentService{
    public GcmIntentService2(){
        super("");
    }
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public GcmIntentService2(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("GcmIntentService2", "onhandle intent");
        Bundle extras=intent.getExtras();
        String message=intent.getStringExtra("message");
        GoogleCloudMessaging gcm=GoogleCloudMessaging.getInstance(this);
        String messageType=gcm.getMessageType(intent);
        if(extras!=null&&!extras.isEmpty()&& GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)){

            NotificationManager mNotificationManager = (NotificationManager)
                    getSystemService(Context.NOTIFICATION_SERVICE);

            PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                    new Intent(this, MainActivity.class), PendingIntent.FLAG_ONE_SHOT);

            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle("NYU Ten")
                            .setStyle(new NotificationCompat.BigTextStyle()
                                    .bigText(message))
                            .setContentText(message).setContentIntent(contentIntent).setAutoCancel(true);

            mBuilder.setContentIntent(contentIntent);
            mNotificationManager.notify(0, mBuilder.build());

        }
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }
}
