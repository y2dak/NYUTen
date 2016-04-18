package com.nyuten.nyuten.GPS;

import com.nyuten.nyuten.*;

/**
 * Created by andylin on 16/4/18.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

/**
 * Created by andylin on 16/4/15.
 */
public class LocationReceiver extends BroadcastReceiver {
    private static final String TAG = "LocationReceiver";
    /*
    This method is called when the BroadcastReceiver is receiving an Intent broadcast.
    http://developer.android.com/reference/android/content/BroadcastReceiver.html#onReceive%28android.content.Context,%20android.content.Intent%29
    * */

    /*
    n Overview of Broadcast Receivers

    An application listens for specific broadcast intents by registering a broadcast receiver. Broadcast re
    ceivers are implemented by extending the Android BroadcastReceiver class and overriding the onReceive() method.
    The broadcast receiver may then be registered, either within code (for example within an activity), or within a
    manifest file.

    http://www.techotopia.com/index.php/Android_Broadcast_Intents_and_Broadcast_Receivers
    * */
    /*
    Part of the registration implementation involves the creation of intent filters to indicate the specific
    broadcast intents the receiver is required to listen for. This is achieved by referencing the action string
    of the broadcast intent. When a matching broadcast is detected, the onReceive() method of the broadcast receiver
     is called, at which point the method has 5 seconds within which to perform any necessary tasks before returning
     . It is important to note that a broadcast receiver does not need to be running all the time. In the event that
     a matching intent is detected, the Android runtime system will automatically start up the broadcast receiver
     before calling the onReceive() method.

     http://www.techotopia.com/index.php/Android_Broadcast_Intents_and_Broadcast_Receivers
     ??: so the application is closed, but its manifest is checked, if matched, application is launched??
    * */
    @Override
    public void onReceive(Context context, Intent intent)
    {
        System.out.println("in the onReceive\n");
        // If you got a Location extra, use it
        Location loc = (Location)intent
                .getParcelableExtra(LocationManager.KEY_LOCATION_CHANGED);
        if (loc != null)
        {
            onLocationReceived(context, loc);
            System.out.println("in the onReceive, onLocationReceived is called\n");
            return;
        }
        else
        {
            System.out.println("in the onReceive, onLocationReceived is not called\n");

        }
        // If you get here, something else has happened
        if (intent.hasExtra(LocationManager.KEY_PROVIDER_ENABLED))
        {
            boolean enabled = intent
                    .getBooleanExtra(LocationManager.KEY_PROVIDER_ENABLED, false);
            onProviderEnabledChanged(enabled);
        }
    }

    protected void onLocationReceived(Context context, Location loc)
    {
        Log.d(TAG, this + " Got location from " + loc.getProvider() + ": "
                + loc.getLatitude() + ", " + loc.getLongitude());
        Log.i(TAG,"onLocationRecived in LocationReceiver is called!!");
    }
    protected void onProviderEnabledChanged(boolean enabled)
    {
        Log.d(TAG, "Provider " + (enabled ? "enabled" : "disabled"));

    }

}