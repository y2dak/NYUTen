package com.nyuten.nyuten;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.nyuten.nyuten.GPS.LocationReceiver;
import com.nyuten.nyuten.GPS.RunManager;
import com.nyuten.nyuten.Model.mLocation;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class UpdateLocationActivity extends AppCompatActivity {
    mLocation mLocCurrent ;
    private Location mLastLocation;

    String name;
    String userId;
    RadioButton crowded;
    RadioButton notBad;
    RadioButton empty;
    String status;
    TextView atLocationtTxt;
    boolean atLocation = false;


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
            if (distance[0] < 50){
                atLocation = true;
                atLocationtTxt.setText("You are at this location!");
            }
            System.out.println("in the UpdateLocation class, the distance between from current to is " + StringDistance + "\n");

            //Toast.makeText(getApplication(), "Current distance from " + mLocCurrent.getLocation() + " is\n" + StringDistance + " meters", Toast.LENGTH_LONG).show();
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
        setContentView(R.layout.activity_update_location);

        crowded = (RadioButton)findViewById(R.id.radioButton);
        notBad = (RadioButton)findViewById(R.id.radioButton2);
        empty = (RadioButton)findViewById(R.id.radioButton3);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        atLocationtTxt = (TextView) findViewById(R.id.atLocationTxt);
        setSupportActionBar(toolbar);


        this.registerReceiver(mLocationReceiver,
                new IntentFilter(RunManager.ACTION_LOCATION));


            name = getIntent().getStringExtra("name");
            mLocCurrent = getIntent().getParcelableExtra("mLocation");

            /*location part*/
            mRunManager = RunManager.get(this);
            mRunManager.startLocationUpdates();

            userId = getIntent().getStringExtra("userId");
            setTitle("Update " + name + "'s status");
            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    tryToVote(view);
                }
            });
        }

    public void tryToVote(final View view){
        if(atLocation) {
            ParseQuery<ParseObject> queryZero = ParseQuery.getQuery("Status");
            queryZero.whereEqualTo("user", userId);
            queryZero.whereEqualTo("location", name);
            Date currentDate = new Date();
            Calendar cal = Calendar.getInstance();
            cal.setTime(currentDate);
            cal.add(Calendar.HOUR, -1);
            Date date = cal.getTime();
            queryZero.whereGreaterThanOrEqualTo("updatedAt", date);
            queryZero.addDescendingOrder("updatedAt");
            queryZero.findInBackground(new FindCallback<ParseObject>() {
                public void done(List<ParseObject> statusList, ParseException e) {
                    if (e == null) {
                        Log.d("statuses", "Retrieved " + statusList.size() + " statuses");
                        if (statusList.size() != 0) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(UpdateLocationActivity.this);
                            builder.setMessage("You already voted in the past hour!")
                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {

                                        }
                                    });
                            builder.create();
                            builder.show();
                        } else {
                            if (!crowded.isChecked() && !notBad.isChecked() && !empty.isChecked()) {
                                Snackbar.make(view, "Must choose one!", Snackbar.LENGTH_LONG)
                                        .setAction("Action", null).show();
                            } else {
                                ParseObject statusUpdate = new ParseObject("Status");
                                statusUpdate.put("location", name);
                                if (crowded.isChecked()) {
                                    statusUpdate.put("status", "Crowded");
                                    status = "Crowded";
                                } else if (notBad.isChecked()) {
                                    statusUpdate.put("status", "Manageable");
                                    status = "Manageable";
                                } else {
                                    statusUpdate.put("status", "Empty");
                                    status = "Empty";
                                }
                                statusUpdate.put("user", userId);
                                statusUpdate.saveInBackground();
                                //sendNotification();
                                mRunManager.stopLocationUpdates();
                                finish();
                            }
                        }
                    }
                }
            });
        }
        else{
            AlertDialog.Builder builder = new AlertDialog.Builder(UpdateLocationActivity.this);
            builder.setMessage("You are not at this location!")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    });
            builder.create();
            builder.show();
        }
    }
    @Override
    public void onStop () {
        this.unregisterReceiver(mLocationReceiver);
        super.onStop();
    }

}
