package com.nyuten.nyuten;

import android.app.LoaderManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends AppCompatActivity implements GoogleMap.OnMapClickListener, GoogleMap.OnMarkerClickListener, OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, View.OnClickListener, GoogleApiClient.ConnectionCallbacks{
    GoogleApiClient mGoogleApiClient;
    String TAG = "MainActivity";
    private int RC_SIGN_IN = 1;
    GoogleSignInOptions gso;
    boolean loggedIn;
    SharedPreferences sharedPreferences;
    SharedPreferences statuses;
    MarkerOptions dibner = new MarkerOptions().position(new LatLng(40.694575, -73.985779)).title("Dibner");
    MarkerOptions chipotle = new MarkerOptions().position(new LatLng(40.693651, -73.986403)).title("Chipotle");
    MarkerOptions gym = new MarkerOptions().position(new LatLng(40.694399, -73.986746)).title("Gym");
    View mainActivity;
    View loginActivity;
    Menu myMenu;
    TextView markerTitle;
    RelativeLayout markerInfo;
    Button view;
    Button update;
    String currentLocation;


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
        loggedIn = sharedPreferences.getBoolean("loggedIn", false);
        mainActivity = getLayoutInflater().inflate(R.layout.activity_main, null);
        loginActivity = getLayoutInflater().inflate(R.layout.activity_login, null);
        if(loggedIn){
            setContentView(mainActivity);
            //findViewById(R.id.signOut).setOnClickListener(this);
            markerInfo = (RelativeLayout)findViewById(R.id.marker_info);
            markerInfo.setVisibility(View.INVISIBLE);
            markerTitle = (TextView)findViewById(R.id.marker_title);
            view = (Button)findViewById(R.id.viewbtn);
            view.setOnClickListener(this);
            update = (Button)findViewById(R.id.updatebtn);
            update.setOnClickListener(this);
            ((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);
            ((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMap().setOnMapClickListener(this);
            ((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(40.694575, -73.985779), 14.0f));
        }
        else {
            setContentView(loginActivity);
            SignInButton signInButton = (SignInButton) findViewById(R.id.sign_in_button);
            signInButton.setSize(SignInButton.SIZE_STANDARD);
            signInButton.setScopes(gso.getScopeArray());
            findViewById(R.id.sign_in_button).setOnClickListener(this);
        }
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
            view = (Button)findViewById(R.id.viewbtn);
            view.setOnClickListener(this);
            update = (Button)findViewById(R.id.updatebtn);
            update.setOnClickListener(this);
            ((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);
            ((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMap().setOnMapClickListener(this);
            ((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(40.694575, -73.985779), 14.0f));
            sharedPreferences.edit().putBoolean("loggedIn", true).apply();
            getMenuInflater().inflate(R.menu.menu_main, myMenu);
        }
        else{
            setContentView(loginActivity);
            SignInButton signInButton = (SignInButton) findViewById(R.id.sign_in_button);
            signInButton.setSize(SignInButton.SIZE_STANDARD);
            signInButton.setScopes(gso.getScopeArray());
            findViewById(R.id.sign_in_button).setOnClickListener(this);
            getMenuInflater().inflate(R.menu.menu_login, myMenu);
            sharedPreferences.edit().putBoolean("loggedIn", false).apply();
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
                startActivity(i);
                break;
            case R.id.updatebtn:
                Intent j = new Intent(MainActivity.this, UpdateLocationActivity.class);
                j.putExtra("name", currentLocation);
                startActivity(j);
                break;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        System.out.println("onMapReady");
        googleMap.addMarker(dibner);
        googleMap.addMarker(chipotle);
        googleMap.addMarker(gym);
        googleMap.setOnMarkerClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        myMenu = menu;
        if(loggedIn) {
            getMenuInflater().inflate(R.menu.menu_main, menu);
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
        //System.out.println(marker.getId() + " " + marker.getTitle());
        if(marker.getTitle().equals("Dibner")){
            currentLocation = "Dibner";
            markerInfo.setVisibility(View.VISIBLE);
            markerTitle.setText("Dibner");
        }
        else if(marker.getTitle().equals("Chipotle")){
            currentLocation = "Chipotle";
            markerInfo.setVisibility(View.VISIBLE);
            markerTitle.setText("Chipotle");
        }
        else{
            currentLocation = "Gym";
            markerInfo.setVisibility(View.VISIBLE);
            markerTitle.setText("Gym");
        }
        return false;
    }

    @Override
    public void onMapClick(LatLng latLng) {
        markerInfo.setVisibility(View.INVISIBLE);
    }
}