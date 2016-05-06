package com.nyuten.nyuten;
import com.nyuten.nyuten.GPS.LocationReceiver;
import com.nyuten.nyuten.GPS.RunManager;
import com.nyuten.nyuten.Model.*;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;

import android.content.Context;
import android.location.Location;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;


public class UpdateLocationActivity extends AppCompatActivity {

    //mLocation mLocCurrent = new mLocation();
    mLocation mLocCurrent ;
    private Location mLastLocation;


    String name;
    RadioButton crowded;
    RadioButton notBad;
    RadioButton empty;

    float[] distance=new float[5];
    private RunManager mRunManager;

    String StringDistance;

    private BroadcastReceiver mLocationReceiver = new LocationReceiver()
    {
        @Override
        protected void onLocationReceived(Context context, Location loc)
        {
            System.out.println("?????????????\n");
            mLastLocation = loc;
            System.out.println("Latitude:" + mLastLocation.getLatitude() + '\n');
            System.out.println("Longitude:" + mLastLocation.getLongitude() + '\n');
            System.out.println("Altitude:" + mLastLocation.getAltitude() + '\n');

            Location.distanceBetween(loc.getLatitude(), loc.getLongitude(), mLocCurrent.getLat(), mLocCurrent.getLng(), distance);
            StringDistance =Float.toString(distance[0]);
            System.out.println("in the UpdateLocation class, the distance between from current to is " + StringDistance + "\n");

            Toast.makeText(getApplication(), "Current distance from " + mLocCurrent.getLocation()+"is\n" + StringDistance + " meters", Toast.LENGTH_LONG).show();
        }
        @Override
        protected void onProviderEnabledChanged(boolean enabled)
        {
            //int toastText = enabled ? R.string.gps_enabled : R.string.gps_disabled;
            //Toast.makeText(getActivity(), toastText, Toast.LENGTH_LONG).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        this.registerReceiver(mLocationReceiver,
                new IntentFilter(RunManager.ACTION_LOCATION));


        setContentView(R.layout.activity_update_location);
        crowded = (RadioButton)findViewById(R.id.radioButton);
        notBad = (RadioButton)findViewById(R.id.radioButton2);
        empty = (RadioButton)findViewById(R.id.radioButton3);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        name = getIntent().getStringExtra("name");

        mLocCurrent = getIntent().getParcelableExtra("mLocation");
        /*
        if(mLocCurrent!=null)
        System.out.println(mLocCurrent);
        else
        System.out.println("!!!!!!mLocCurrent is null!\n");
        */

        /*location part*/
        mRunManager = RunManager.get(this);
        mRunManager.startLocationUpdates();

        setTitle("Update " + name + "'s status");
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        //.make(view, "Distance is" + StringDistance + "meters", Snackbar.LENGTH_LONG)
          //      .setAction("Action", null).show();
        //Toast.makeText(this, "Distance is" + StringDistance + "meters", Toast.LENGTH_LONG).show();



        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {



                if(!crowded.isChecked() && !notBad.isChecked() && !empty.isChecked()){
                    Snackbar.make(view, "Must choose one!", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();


                }
                else {
                    System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!\n");



                    mRunManager.stopLocationUpdates();
                    finish();
                }
            }
        });
    }
    /*???*/



    @Override
    public void onStop() {
        this.unregisterReceiver(mLocationReceiver);
        super.onStop();
    }
}
