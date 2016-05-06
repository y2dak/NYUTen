package com.nyuten.nyuten;


import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceIDListenerService;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;
import java.util.ArrayList;

public class MyInstanceIDService extends InstanceIDListenerService{

    public void onTokenRefresh(){
        Log.d("token refresh", "token changed");
        Intent intent=new Intent(this,GcmIntentService.class);
        startService(intent);
    }

}
