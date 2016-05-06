package com.nyuten.nyuten;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.app.Activity;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("GCMBROADCASERECEIVER onReceive");
        ComponentName comp = new ComponentName(context.getPackageName(),
                GcmIntentService2.class.getName());
        startWakefulService(context, (intent.setComponent(comp)));
        setResultCode(Activity.RESULT_OK);
    }
}