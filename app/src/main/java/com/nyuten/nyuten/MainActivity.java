package com.nyuten.nyuten;

import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.nyuten.nyuten.Model.mLocation;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

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
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements GoogleMap.OnMapClickListener, GoogleMap.OnMarkerClickListener, OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, View.OnClickListener, GoogleApiClient.ConnectionCallbacks{
    GoogleApiClient mGoogleApiClient;
    String TAG = "MainActivity";
    private int RC_SIGN_IN = 1;
    GoogleSignInOptions gso;
    String email;
    String userId;
    boolean loggedIn;
    SharedPreferences sharedPreferences;
    SharedPreferences statuses;
    GoogleMap mGoogleMap = null;
    boolean dibnerFirst = true;
    boolean chipotleFirst = true;
    boolean starbucksFirst = true;
    boolean gymFirst = true;
    ProgressDialog dialog;

    /*
    TODO
    * April 18- Anderson Lin
    * */
    mLocation mLocDibner= new mLocation("Dibner",40.694575, -73.985779);
    mLocation mLocChipotle= new mLocation("Chipotle",40.693651, -73.986403);
    mLocation mLocGym= new mLocation("Gym",40.694399, -73.986746);
    mLocation mLocCurrent = new mLocation();
    mLocation mLocStarbucks = new mLocation("Starbucks", 40.694087, -73.986818);

    MarkerOptions dibner = new MarkerOptions().position(new LatLng(40.694575, -73.985779)).title("Dibner");
    MarkerOptions chipotle = new MarkerOptions().position(new LatLng(40.693651, -73.986403)).title("Chipotle");
    MarkerOptions gym = new MarkerOptions().position(new LatLng(40.694399, -73.986746)).title("Gym");
    MarkerOptions starbucks = new MarkerOptions().position(new LatLng(40.694087, -73.986818)).title("Starbucks");
    String dibnerStat = "No Recent Status";
    String prevDibnerStat = "No Recent Status";

    String chipotleStat = "No Recent Status";
    String prevChipotleStat = "No Recent Status";

    String gymStat = "No Recent Status";
    String prevGymStat = "No Recent Status";

    String starbucksStat = "No Recent Status";
    String prevStarbucksStat = "No Recent Status";

    boolean dDone = true;
    boolean cDone = true;
    boolean sDone = true;
    boolean gDone = true;

    Marker dibnerMarker;
    Marker chipotleMarker;
    Marker gymMarker;
    Marker starbucksMarker;
    View mainActivity;
    View loginActivity;
    Menu myMenu;
    TextView markerTitle;
    RelativeLayout markerInfo;
    Button view;
    Button update;
    String currentLocation;
    Marker currentMarker = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getSharedPreferences("nyutencredz", MODE_PRIVATE);
        statuses = getSharedPreferences("nyutenstatuses", MODE_PRIVATE);
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        try {
            Parse.initialize(this, "M1xTL6HzyegVSmshU1NgFbai5VTn07aOMSFmFgvP", "f0MCDv8IMKdKZTMpnV6oXELDwTG12wSv8F4y6q8T");
        }
        catch (IllegalStateException e){
            e.printStackTrace();
        }
        //ParseObject testObject = new ParseObject("TestObject");
        //testObject.put("foo", "bar");
        //testObject.saveInBackground();
        loggedIn = sharedPreferences.getBoolean("loggedIn", false);
        mainActivity = getLayoutInflater().inflate(R.layout.activity_main, null);
        loginActivity = getLayoutInflater().inflate(R.layout.activity_login, null);
        //setStats();
        if(loggedIn){
            setContentView(mainActivity);
            //findViewById(R.id.signOut).setOnClickListener(this);
            dialog = ProgressDialog.show(MainActivity.this, "", "Loading. Please wait...", true);
            email = sharedPreferences.getString("email", "");
            userId = email.substring(0, email.length() - 8);
            System.out.println("USERID: " + userId);
            markerInfo = (RelativeLayout)findViewById(R.id.marker_info);
            markerInfo.setVisibility(View.INVISIBLE);
            markerTitle = (TextView)findViewById(R.id.marker_title);
            ((Button)findViewById(R.id.refreshBtn)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    currentMarker = null;
                    markerInfo.setVisibility(View.INVISIBLE);
                    dialog.show();
                    setStats();
                }
            });
            view = (Button)findViewById(R.id.viewbtn);
            view.setOnClickListener(this);
            update = (Button)findViewById(R.id.updatebtn);
            update.setOnClickListener(this);
            ((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);
            ((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMap().setOnMapClickListener(this);
            ((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(40.694575, -73.985779), 17.0f));
        }
        else {
            setContentView(loginActivity);
            SignInButton signInButton = (SignInButton) findViewById(R.id.sign_in_button);
            signInButton.setSize(SignInButton.SIZE_STANDARD);
            signInButton.setScopes(gso.getScopeArray());
            findViewById(R.id.sign_in_button).setOnClickListener(this);
        }


        if(checkPlayServices()){
            Intent intent=new Intent(this,GcmIntentService.class);
            startService(intent);
        }
//        LocationServices.GeofencingApi.addGeofences(
//                mGoogleApiClient,
//                getGeofencingRequest(),
//                getGeofencePendingIntent()
//        ).setResultCallback(this);
    }


    private boolean checkPlayServices(){
        int resultCode= GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if(resultCode!= ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)){
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, 9000).show();
            }
            else{
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    public void setStats(){
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Status");
        query.whereEqualTo("location", "Dibner");
        Date currentDate = new Date();
        Calendar cal = Calendar.getInstance();
//        cal.set(Calendar.HOUR_OF_DAY,0);
//        cal.set(Calendar.MINUTE,0);
//        cal.set(Calendar.SECOND,0);
//        cal.set(Calendar.MILLISECOND,0);
        cal.setTime(currentDate);
        cal.add(Calendar.HOUR, -1);
        Date date = cal.getTime();
        query.whereGreaterThanOrEqualTo("updatedAt", date);
        query.addDescendingOrder("updatedAt");
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> status, ParseException e) {
                if (e == null) {
                    dDone = false;
                    Log.d("statuses", "Retrieved " + status.size() + " statuses");
                    if (status.size() != 0) {
                        if(status.size() < 4) {
                            dibnerStat = status.get(0).getString("status");
                            System.out.println("dibnerStat: " + dibnerStat);
                            if (dibnerStat.equals("Crowded")) {
                                dibner.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                            } else if (dibnerStat.equals("Manageable")) {
                                dibner.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
                            } else if (dibnerStat.equals("Empty")) {
                                dibner.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                            }
                        }
                        else{
                            int crowded = 0;
                            int manage = 0;
                            int empty = 0;
                            for(int i = 0; i < status.size(); i++){
                                if(status.get(i).getString("status").equals("Crowded")){
                                    crowded++;
                                }
                                else if(status.get(i).getString("status").equals("Manageable")){
                                    manage++;
                                }
                                else if(status.get(i).getString("status").equals("Empty")){
                                    empty++;
                                }
                            }
                            if(crowded >= manage && crowded >= empty){
                                dibnerStat = "Crowded";
                                dibner.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                            }
                            else if(manage >= crowded && manage >= empty){
                                dibnerStat = "Manageable";
                                dibner.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
                            }
                            else{
                                dibnerStat = "Empty";
                                dibner.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                            }
                        }
                    } else {
                        dibner.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                        dibnerStat = "No Recent Status";
                    }
                    if(dibnerMarker != null){
                        dibnerMarker.remove();
                    }
                    dibnerMarker = mGoogleMap.addMarker(dibner);
                    if(!dibnerFirst) {
                        if (!prevDibnerStat.equals(dibnerStat)) {
                            //notify
                            //notifyServerOfChange(1);
                        }
                    }
                    else{
                        dibnerFirst = false;
                        prevDibnerStat = dibnerStat;
                    }
                    dDone = true;
                    if(dDone && cDone && sDone && gDone){
                        dialog.hide();
                    }
                }
            }
        });
        ParseQuery<ParseObject> query1 = ParseQuery.getQuery("Status");
        query1.whereEqualTo("location", "Chipotle");
        query1.whereGreaterThanOrEqualTo("updatedAt", date);
        query1.addDescendingOrder("updatedAt");
        query1.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> status, ParseException e) {
                if (e == null) {
                    cDone = false;
                    Log.d("statuses", "Retrieved " + status.size() + " statuses");
                    if (status.size() != 0) {
                        if(status.size() < 4) {
                            chipotleStat = status.get(0).getString("status");
                            if (chipotleStat.equals("Crowded")) {
                                chipotle.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                            } else if (chipotleStat.equals("Manageable")) {
                                chipotle.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
                            } else if (chipotleStat.equals("Empty")) {
                                chipotle.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                            }
                        }
                        else{
                            int crowded = 0;
                            int manage = 0;
                            int empty = 0;
                            for(int i = 0; i < status.size(); i++){
                                if(status.get(i).getString("status").equals("Crowded")){
                                    crowded++;
                                }
                                else if(status.get(i).getString("status").equals("Manageable")){
                                    manage++;
                                }
                                else if(status.get(i).getString("status").equals("Empty")){
                                    empty++;
                                }
                            }
                            if(crowded >= manage && crowded >= empty){
                                chipotleStat = "Crowded";
                                chipotle.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                            }
                            else if(manage >= crowded && manage >= empty){
                                chipotleStat = "Manageable";
                                chipotle.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
                            }
                            else{
                                chipotleStat = "Empty";
                                chipotle.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                            }
                        }
                    } else {
                        chipotle.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                        chipotleStat = "No Recent Status";
                    }
                    if(chipotleMarker != null){
                        chipotleMarker.remove();
                    }
                    chipotleMarker = mGoogleMap.addMarker(chipotle);
                    if(!chipotleFirst) {
                        if (!prevChipotleStat.equals(chipotleStat)) {
                            //notify
                            //notifyServerOfChange(0);
                        }
                    }
                    else{
                        chipotleFirst = false;
                        prevChipotleStat = chipotleStat;
                    }
                    cDone = true;
                    if(dDone && cDone && sDone && gDone){
                        dialog.hide();
                    }
                }
            }
        });
        ParseQuery<ParseObject> query2 = ParseQuery.getQuery("Status");
        query2.whereEqualTo("location", "Gym");
        query2.whereGreaterThanOrEqualTo("updatedAt", date);
        query2.addDescendingOrder("updatedAt");
        query2.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> status, ParseException e) {
                if (e == null) {
                    gDone = false;
                    Log.d("statuses", "Retrieved " + status.size() + " statuses");
                    if (status.size() != 0) {
                        if(status.size() < 4) {
                            gymStat = status.get(0).getString("status");
                            System.out.println("gymStatCheck:(" + gymStat + ")");
                            if (gymStat.trim().toUpperCase().equals("CROWDED")) {
                                System.out.println("1");
                                gym.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                            } else if (gymStat.trim().toUpperCase().equals("MANAGEABLE")) {
                                System.out.println("2");
                                gym.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
                            } else if (gymStat.trim().toUpperCase().equals("EMPTY")) {
                                System.out.println("3");
                                gym.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                            }
                        }
                        else{
                            int crowded = 0;
                            int manage = 0;
                            int empty = 0;
                            for(int i = 0; i < status.size(); i++){
                                if(status.get(i).getString("status").equals("Crowded")){
                                    crowded++;
                                }
                                else if(status.get(i).getString("status").equals("Manageable")){
                                    manage++;
                                }
                                else if(status.get(i).getString("status").equals("Empty")){
                                    empty++;
                                }
                            }
                            System.out.println("Crowded: " + crowded);
                            System.out.println("Manageable: " + manage);
                            System.out.println("Empty: " + empty);
                            if(crowded >= manage && crowded >= empty){
                                gymStat = "Crowded";
                                gym.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                            }
                            else if(manage >= crowded && manage >= empty){
                                gymStat = "Manageable";
                                gym.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
                            }
                            else{
                                gymStat = "Empty";
                                gym.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                            }
                        }
                    } else {
                        gym.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                        gymStat = "No Recent Status";
                    }
                    if(gymMarker != null){
                        gymMarker.remove();
                    }
                    gymMarker = mGoogleMap.addMarker(gym);
                    if(!gymFirst) {
                        if (!prevGymStat.equals(gymStat)) {
                            //notify
                            //notifyServerOfChange(2);
                        }
                    }
                    else{
                        gymFirst = false;
                        prevGymStat = gymStat;
                    }
                    gDone = true;
                    if(dDone && cDone && sDone && gDone){
                        dialog.hide();
                    }
                }
            }
        });
        ParseQuery<ParseObject> query3 = ParseQuery.getQuery("Status");
        query3.whereEqualTo("location", "Starbucks");
        query3.whereGreaterThanOrEqualTo("updatedAt", date);
        query3.addDescendingOrder("updatedAt");
        query3.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> status, ParseException e) {
                if (e == null) {
                    sDone = false;
                    Log.d("statuses", "Retrieved " + status.size() + " statuses");
                    if (status.size() != 0) {
                        if(status.size() < 4) {
                            starbucksStat = status.get(0).getString("status");
                            if (starbucksStat.equals("Crowded")) {
                                starbucks.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                            } else if (starbucksStat.equals("Manageable")) {
                                starbucks.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
                            } else if (starbucksStat.equals("Empty")) {
                                starbucks.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                            }
                        }
                        else{
                            int crowded = 0;
                            int manage = 0;
                            int empty = 0;
                            for(int i = 0; i < status.size(); i++){
                                if(status.get(i).getString("status").equals("Crowded")){
                                    crowded++;
                                }
                                else if(status.get(i).getString("status").equals("Manageable")){
                                    manage++;
                                }
                                else if(status.get(i).getString("status").equals("Empty")){
                                    empty++;
                                }
                            }
                            if(crowded >= manage && crowded >= empty){
                                starbucksStat = "Crowded";
                                starbucks.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                            }
                            else if(manage >= crowded && manage >= empty){
                                starbucksStat = "Manageable";
                                starbucks.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
                            }
                            else{
                                starbucksStat = "Empty";
                                starbucks.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                            }
                        }
                    } else {
                        starbucks.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                        starbucksStat = "No Recent Status";
                    }
                    if(starbucksMarker != null){
                        starbucksMarker.remove();
                    }
                    starbucksMarker = mGoogleMap.addMarker(starbucks);
                    if(!starbucksFirst) {
                        if (!prevStarbucksStat.equals(starbucksStat)) {
                            //notify
                            //notifyServerOfChange(3);
                        }
                    }
                    else{
                        starbucksFirst = false;
                        prevStarbucksStat = starbucksStat;
                    }
                    sDone = true;
                    if(dDone && cDone && sDone && gDone){
                        dialog.hide();
                    }
                }
            }
        });
        mGoogleMap.setOnMarkerClickListener(MainActivity.this);
//        if(firstTime){
//            prevDibnerStat = dibnerStat;
//            prevGymStat = gymStat;
//            prevStarbucksStat = starbucksStat;
//            prevChipotleStat = chipotleStat;
//        }
    }

    private void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        // ...
                    }
                });
        updateUI(false);
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            //mStatusTextView.setText(getString(R.string.signed_in_fmt, acct.getDisplayName()));
            updateUI(true);
            //signIn();
        } else {
            // Signed out, show unauthenticated UI.
            updateUI(false);
        }
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
        //getMenuInflater().inflate(R.menu.menu_main, null);
;    }
    private void updateUI(boolean signedIn){
        if(signedIn){
            setContentView(mainActivity);
            markerInfo = (RelativeLayout)findViewById(R.id.marker_info);
            markerInfo.setVisibility(View.INVISIBLE);
            markerTitle = (TextView)findViewById(R.id.marker_title);
            dialog = ProgressDialog.show(MainActivity.this, "", "Loading. Please wait...", true);
            ((Button)findViewById(R.id.refreshBtn)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    currentMarker = null;
                    markerInfo.setVisibility(View.INVISIBLE);
                    dialog.show();
                    setStats();
                }
            });
            view = (Button)findViewById(R.id.viewbtn);
            view.setOnClickListener(this);
            update = (Button)findViewById(R.id.updatebtn);
            update.setOnClickListener(this);
            ((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);
            ((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMap().setOnMapClickListener(this);
            ((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(40.694575, -73.985779), 17.0f));
            sharedPreferences.edit().putBoolean("loggedIn", true).apply();
            sharedPreferences.edit().putString("email", email).apply();
            email = sharedPreferences.getString("email", "");
            userId = email.substring(0, email.length() - 8);
            myMenu.findItem(R.id.sign_out).setVisible(true);
        }
        else{
            setContentView(loginActivity);
            SignInButton signInButton = (SignInButton) findViewById(R.id.sign_in_button);
            signInButton.setSize(SignInButton.SIZE_STANDARD);
            signInButton.setScopes(gso.getScopeArray());
            findViewById(R.id.sign_in_button).setOnClickListener(this);
            getMenuInflater().inflate(R.menu.menu_login, myMenu);
            sharedPreferences.edit().putBoolean("loggedIn", false).apply();
            myMenu.findItem(R.id.sign_out).setVisible(false);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            GoogleSignInAccount acct = result.getSignInAccount();
            String personName = acct.getDisplayName();
            System.out.println(personName);
            String personEmail = acct.getEmail();
            System.out.println(personEmail);
            String personId = acct.getId();
            System.out.println(personId);
            Uri personPhoto = acct.getPhotoUrl();
            System.out.println(personPhoto);
            if(personEmail.endsWith("@nyu.edu")) {
                email = personEmail;
                handleSignInResult(result);
            }
            else{
                Toast.makeText(MainActivity.this, "Must sign in with NYU email!", Toast.LENGTH_SHORT).show();
                signOut();
            }
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
            case R.id.viewbtn:
                Intent i = new Intent(MainActivity.this, ViewLocationActivity.class);
                i.putExtra("name", currentLocation);
                if(currentLocation.equals("Dibner")){
                    i.putExtra("status", dibnerStat);
                }
                else if(currentLocation.equals("Starbucks")){
                    i.putExtra("status", starbucksStat);
                }
                else if(currentLocation.equals("Chipotle")){
                    i.putExtra("status", chipotleStat);
                }
                else{
                    i.putExtra("status", gymStat);
                }
                i.putExtra("userId", userId);
                i.putExtra("mLocation", mLocCurrent);
                startActivity(i);
                break;
            case R.id.updatebtn:
                Intent j = new Intent(MainActivity.this, UpdateLocationActivity.class);
                j.putExtra("name", currentLocation);
                j.putExtra("userId", userId);
                j.putExtra("mLocation", mLocCurrent);
                startActivity(j);
                break;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        System.out.println("onMapReady");
        mGoogleMap = googleMap;
        dialog.show();
        setStats();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        myMenu = menu;
        getMenuInflater().inflate(R.menu.menu_main, myMenu);
        if(loggedIn){
            myMenu.findItem(R.id.sign_out).setVisible(true);
        }
        else{
            myMenu.findItem(R.id.sign_out).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.sign_out) {
            signOut();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        currentMarker = marker;
        //System.out.println(marker.getId() + " " + marker.getTitle());
        if(marker.getTitle().equals("Dibner")){
            mLocCurrent.setmLocation(mLocDibner);
            currentLocation = "Dibner";
            markerInfo.setVisibility(View.VISIBLE);
            markerTitle.setText("Dibner: " + dibnerStat);
        }
        else if(marker.getTitle().equals("Chipotle")){
            mLocCurrent.setmLocation(mLocChipotle);
            currentLocation = "Chipotle";
            markerInfo.setVisibility(View.VISIBLE);
            markerTitle.setText("Chipotle: " + chipotleStat);
        }
        else if(marker.getTitle().equals("Gym")){
            mLocCurrent.setmLocation(mLocGym);
            currentLocation = "Gym";
            markerInfo.setVisibility(View.VISIBLE);
            markerTitle.setText("Gym: " + gymStat);
        }
        else{
            mLocCurrent.setmLocation(mLocStarbucks);
            currentLocation = "Starbucks";
            markerInfo.setVisibility(View.VISIBLE);
            markerTitle.setText("Starbucks: " + starbucksStat);
        }
        return false;
    }

    @Override
    public void onMapClick(LatLng latLng) {
        currentMarker = null;
        markerInfo.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onResume(){
        super.onResume();
        if(currentMarker != null){
            if(currentMarker.getTitle().equals("Dibner")){
                currentLocation = "Dibner";
                markerInfo.setVisibility(View.VISIBLE);
                markerTitle.setText("Dibner: " + dibnerStat);
            }
            else if(currentMarker.getTitle().equals("Chipotle")){
                currentLocation = "Chipotle";
                markerInfo.setVisibility(View.VISIBLE);
                markerTitle.setText("Chipotle: " + chipotleStat);
            }
            else if(currentMarker.getTitle().equals("Gym")){
                currentLocation = "Gym";
                markerInfo.setVisibility(View.VISIBLE);
                markerTitle.setText("Gym: " + gymStat);
            }
            else{
                currentLocation = "Starbucks";
                markerInfo.setVisibility(View.VISIBLE);
                markerTitle.setText("Starbucks: " + starbucksStat);
            }
        }
        if(markerInfo != null) {
            markerInfo.setVisibility(View.INVISIBLE);
        }
//        System.out.println("BEFORE setStats()");
//        System.out.println("dibnerStat: " + dibnerStat);
//        System.out.println("prevDibnerStat: " + prevDibnerStat);
//        System.out.println("chipotleStat: " + chipotleStat);
//        System.out.println("prevChipotleStat: " + prevChipotleStat);
//        System.out.println("gymStat: " + gymStat);
//        System.out.println("prevGymStat: " + prevGymStat);
//        System.out.println("starbucksStat: " + starbucksStat);
//        System.out.println("prevStarbucksStat: " + prevStarbucksStat);
        if(mGoogleMap != null){
            dialog.show();
            setStats();
        }
//        prevChipotleStat = chipotleStat;
//        prevDibnerStat = dibnerStat;
//        prevStarbucksStat = starbucksStat;
//        prevGymStat = gymStat;
    }

    public void notifyServerOfChange(int id) {
        System.out.println("STATUS CHANGE!");
        String message;
        if (id == 0) {
            message = "Chipotle is now " + chipotleStat;
        } else if (id == 1) {
            message = "Dibner is now " + dibnerStat;
        } else if (id == 2) {
            message = "Gym is now " + gymStat;
        } else {
            message = "Starbucks is now " + starbucksStat;
        }
        new sendMessageTask().execute(message);
    }

    private class sendMessageTask extends AsyncTask<String,Integer,String> {

        @Override
        protected String doInBackground(String... params) {
            URL url= null;
            String message = params[0];
            try {
                url = new URL("http://nyu-ten.appspot.com/");
                HttpURLConnection c= (HttpURLConnection) url.openConnection();
                c.setReadTimeout(15000);
                c.setConnectTimeout(15000);
                c.setRequestMethod("POST");
                c.setDoInput(true);
                c.setDoOutput(true);

                JSONObject obj=new JSONObject();
                obj.put("command","SEND_MESSAGE");
                obj.put("message", message);
                Log.d("OBJ IS",obj.toString());
                OutputStream os=c.getOutputStream();
                BufferedWriter writer=new BufferedWriter(new OutputStreamWriter(os,"UTF-8"));
                writer.write(obj.toString());
                writer.flush();
                writer.close();
                os.close();
                c.connect();
                InputStream is= c.getInputStream();
                BufferedReader br=new BufferedReader(new InputStreamReader(is,"UTF-8"));
                String line = "";
                while ((line = br.readLine()) != null) {
                    Log.d("REG ID RESULT", line);
                }


            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

    }
}
