package com.nyuten.nyuten.GPS;

/*
Context
Class Overview

Interface to global information about an application environment. This is an abstract class whose
implementation is provided by the Android system. It allows access to application-specific resources
 and classes, as well as up-calls for application-level operations such as launching activities,
 broadcasting and receiving intents, etc.

 http://developer.android.com/reference/android/content/Context.html

*
*
*
* */

/*

what is PendingIntent
A PendingIntent is a token that you give to a foreign application (e.g. NotificationManager, AlarmManager,
 Home Screen AppWidgetManager, or other 3rd party applications), which allows the foreign application to
 use your application's permissions to execute a predefined piece of code.

If you give the foreign application an Intent, and that application sends/broadcasts the Intent you gave,
 they will execute the Intent with their own permissions. But if you instead give the foreign application
 a PendingIntent you created using your own permission, that application will execute the contained Intent
  using your application's permission.

http://stackoverflow.com/questions/2808796/what-is-an-android-pendingintent
http://developer.android.com/reference/android/app/PendingIntent.html
* */

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;

//import android.support.v4.content.ContextCompat.checkSelfPermission;


public class RunManager {
    private static final String TAG = "RunManager";

    /*store the disdance*/
    float[] distance=new float[5];
    String StringDistance = new String();

    /*
    how does this string works?
    * */
    public static final String ACTION_LOCATION =
            //"com.bignerdranch.android.runtracker.ACTION_LOCATION";
            "com.example.nyuten.nyuten.ACTION_LOCATION";
    private static RunManager sRunManager;
    private Context mAppContext;
    /*something like fragmentManager*/
    private LocationManager mLocationManager;
    // The private constructor forces users to use RunManager.get(Context)




    private RunManager(Context appContext)
    {
        /**

         You do not instantiate this class directly; instead,
         the method returns a handle to a new LocationManager instance.

         retrieve it through Context.getSystemService(Context.LOCATION_SERVICE)
         getSystemService

         * public final T getSystemService (Class<T> serviceClass)
         * Return the handle to a system-level service by class.
         http://developer.android.com/guide/topics/location/index.html
         *
         * */
        mAppContext = appContext;
        mLocationManager = (LocationManager)mAppContext
                .getSystemService(Context.LOCATION_SERVICE);

    }
    //-----------------------------------------------------------------
    public static RunManager get(Context c) {
        if (sRunManager == null) {
            // Use the application context to avoid leaking activities
            /*??*/
            sRunManager = new RunManager(c.getApplicationContext());
        }
        return sRunManager;
    }
    //-----------------------------------------------------------------
    private PendingIntent getLocationPendingIntent(boolean shouldCreate) {
        /*
        Retrieve a PendingIntent that will perform a broadcast, like calling Context.sendBroadcast().

         http://developer.android.com/reference/android/app/PendingIntent.html#getBroadcast%28android.content.Context,%20int,%20android.content.Intent,%20int%29
        * */
        Intent broadcast = new Intent(ACTION_LOCATION);
        int flags = shouldCreate ? 0 : PendingIntent.FLAG_NO_CREATE;
        return PendingIntent.getBroadcast(mAppContext, 0, broadcast, flags);
    }
    //-----------------------------------------------------------------
    public void startLocationUpdates() {
        /**
         *
         public static final String GPS_PROVIDER
         Added in API level 1

         Name of the GPS location provider.

         This provider determines location using satellites. Depending on conditions, this provider
         may take a while to return a location fix. Requires the permission ACCESS_FINE_LOCATION.

         The extras Bundle for the GPS location provider can contain the following key/value pairs:

         satellites - the number of satellites used to derive the fix

         Constant Value: "gps"



         *
         public static final String NETWORK_PROVIDER
         Added in API level 1

         Name of the network location provider.

         This provider determines location based on availability of cell tower and WiFi access points.
         Results are retrieved by means of a network lookup.
         Constant Value: "network"

         *
         *
         *
         * */
        String provider = LocationManager.GPS_PROVIDER;

        /*added page 559*/
        // Get the last known location and broadcast it if you have one
        if (ContextCompat.checkSelfPermission(mAppContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(mAppContext,Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {

            // Get the last known location and broadcast it if you have one
            /**
             *
             *
             *  public Location getLastKnownLocation (String provider)
             Added in API level 1

             Returns a Location indicating the data from the last known location fix obtained from
             the given provider.

             This can be done without starting the provider. Note that this location could be
             out-of-date, for example if the device was turned off and moved to another location.

             If the provider is currently disabled, null is returned.
             http://developer.android.com/reference/android/location/LocationManager.html#getLastKnownLocation%28java.lang.String%29

             * */
            Location lastKnown = mLocationManager.getLastKnownLocation(provider);
            System.out.println("in the RunManager class, startLocationUpdates\n");
            if (lastKnown != null) {
                // Reset the time to now
                lastKnown.setTime(System.currentTimeMillis());
                broadcastLocation(lastKnown);

                /*
                *
                * 假設在gps位置
                * 華盛頓公園西南角
                  40.731033, -73.999531
                  與這裡的距離
                  華盛頓公園東北角
                  40.730683, -73.995615
                * */
                Location.distanceBetween(lastKnown.getLatitude(),lastKnown.getLongitude(),40.730683,-73.995615,distance);
                String StringDistance =Float.toString(distance[0]);
                //Toast.makeText(, "Distance is " + StringDistance, 5);
                System.out.println("in the RunManager class, the distance between from Bobst Lib to is " + StringDistance + "\n");
                Log.i(TAG, "!!!hello!!!\n");
            }
            else
            {
                System.out.println("last known is Null\n");

            }


            // Start updates from the location manager
            PendingIntent pi = getLocationPendingIntent(true);
            /*how often a disatance and time to update*/
            /**
             *
             http://developer.android.com/reference/android/location/LocationManager.html#requestLocationUpdates%28long,%20float,%20android.location.Criteria,%20android.app.PendingIntent%29
             * */
            mLocationManager.requestLocationUpdates(provider, 0, 0, pi);
        }
    }
    //-----------------------------------------------------------------
    /*added paged 559*/
    private void broadcastLocation(Location location) {
        Intent broadcast = new Intent(ACTION_LOCATION);
        /* ??: but this is not pending intent right?*/
        /*
        seems that pending intent use with broadcast
        * */
        /**
         *
         If the caller supplied a pending intent, then location updates are sent with a
         key of KEY_LOCATION_CHANGED and a Location value.


         public static final String KEY_LOCATION_CHANGED
         Added in API level 3

         Key used for a Bundle extra holding a Location value when a location change is broadcast using a PendingIntent.
         Constant Value: "location"

         http://developer.android.com/reference/android/location/LocationManager.html
         * */
        /*
        System.out.println("in the RunManager class, braodcastLocation:\n");
        System.out.println("Latitude:" + location.getLatitude() + '\n');
        System.out.println("Longitude:" + location.getLongitude() + '\n');
        System.out.println("Altitude:" + location.getAltitude() + '\n');
        */
        broadcast.putExtra(LocationManager.KEY_LOCATION_CHANGED, location);

        mAppContext.sendBroadcast(broadcast);
    }

    //-----------------------------------------------------------------
    public void stopLocationUpdates() {
        PendingIntent pi = getLocationPendingIntent(false);
        if (pi != null) {
            /**

             The logic of deciding when new fixes are no longer necessary might range from very
             simple to very complex depending on your application. A short gap between when the location
             is acquired and when the location is used, improves the accuracy of the estimate. Always
             beware that listening for a long time consumes a lot of battery power, so as soon as you
             have the information you need, you should stop listening for updates by calling
             removeUpdates(PendingIntent):

             http://developer.android.com/guide/topics/location/strategies.html
             * */
            mLocationManager.removeUpdates(pi);
            pi.cancel();
        }
    }
    //-----------------------------------------------------------------
    public boolean isTrackingRun() {
        return getLocationPendingIntent(false) != null;
    }
}
