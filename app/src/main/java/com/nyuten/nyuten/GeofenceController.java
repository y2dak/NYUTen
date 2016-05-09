package com.nyuten.nyuten;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class GeofenceController {

  // region Properties

  private final String TAG = GeofenceController.class.getName();

  private Context context;
  private GoogleApiClient googleApiClient1;
  private GoogleApiClient googleApiClient2;
  private GoogleApiClient googleApiClient3;
  private GoogleApiClient googleApiClient4;

  private Gson gson;
  private SharedPreferences prefs;
  private GeofenceControllerListener listener;

  private List<NamedGeofence> namedGeofences;
  public List<NamedGeofence> getNamedGeofences() {
    return namedGeofences;
  }

  private List<NamedGeofence> namedGeofencesToRemove;

  private Geofence geofenceToAdd;
  private NamedGeofence namedGeofenceToAdd;

  // endregion

  // region Shared Instance

  private static GeofenceController INSTANCE;

  public static GeofenceController getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new GeofenceController();
    }
    return INSTANCE;
  }

  // endregion

  // region Public

  public void init(Context context) {
    this.context = context.getApplicationContext();

    gson = new Gson();
    namedGeofences = new ArrayList<>();
    namedGeofencesToRemove = new ArrayList<>();
    prefs = this.context.getSharedPreferences(Constants.SharedPrefs.Geofences, Context.MODE_PRIVATE);

    loadGeofences();
  }

  public void addGeofence(NamedGeofence namedGeofence) {
    this.namedGeofenceToAdd = namedGeofence;
    this.geofenceToAdd = namedGeofence.geofence();

    connectWithCallbacks(connectionAddListener);
  }

  // endregion

  // region Private

  private void loadGeofences() {
    // Loop over all geofence keys in prefs and add to namedGeofences
    Map<String, ?> keys = prefs.getAll();
    for (Map.Entry<String, ?> entry : keys.entrySet()) {
      String jsonString = prefs.getString(entry.getKey(), null);
      NamedGeofence namedGeofence = gson.fromJson(jsonString, NamedGeofence.class);
      namedGeofences.add(namedGeofence);
    }

    // Sort namedGeofences by name
    Collections.sort(namedGeofences);

  }


  private void connectWithCallbacks(GoogleApiClient.ConnectionCallbacks callbacks) {
    System.out.println("private void connectWithCallbacks1(GoogleApiClient.ConnectionCallbacks callbacks)");
    googleApiClient1 = new GoogleApiClient.Builder(context)
            .addApi(LocationServices.API)
            .addConnectionCallbacks(callbacks)
            .addOnConnectionFailedListener(connectionFailedListener)
            .build();
    googleApiClient1.connect();
  }

  private GeofencingRequest getAddGeofencingRequest() {
    List<Geofence> geofencesToAdd = new ArrayList<>();
    geofencesToAdd.add(geofenceToAdd);
    GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
    builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
    builder.addGeofences(geofencesToAdd);
    return builder.build();
  }

  private void saveGeofence() {
    namedGeofences.add(namedGeofenceToAdd);
    if (listener != null) {
      listener.onGeofencesUpdated();
    }

    String json = gson.toJson(namedGeofenceToAdd);
    SharedPreferences.Editor editor = prefs.edit();
    editor.putString(namedGeofenceToAdd.id, json);
    editor.apply();
  }

  private void removeSavedGeofences() {
    SharedPreferences.Editor editor = prefs.edit();

    for (NamedGeofence namedGeofence : namedGeofencesToRemove) {
      int index = namedGeofences.indexOf(namedGeofence);
      editor.remove(namedGeofence.id);
      namedGeofences.remove(index);
      editor.apply();
    }

    if (listener != null) {
      listener.onGeofencesUpdated();
    }
  }

  private void sendError() {
    if (listener != null) {
      listener.onError();
    }
  }

  // endregion

  // region ConnectionCallbacks

  private GoogleApiClient.ConnectionCallbacks connectionAddListener = new GoogleApiClient.ConnectionCallbacks() {
    @Override
    public void onConnected(Bundle bundle) {
      System.out.println("GREAT SUCCESS!");
      Intent intent = new Intent(context, GeofenceIntentService.class);
      PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
      PendingResult<Status> result = LocationServices.GeofencingApi.addGeofences(googleApiClient1, getAddGeofencingRequest(), pendingIntent);
      result.setResultCallback(new ResultCallback<Status>() {
        @Override
        public void onResult(Status status) {
          if (status.isSuccess()) {
            saveGeofence();
          } else {
            Log.e(TAG, "Registering geofence failed: " + status.getStatusMessage() + " : " + status.getStatusCode());
            sendError();
          }
        }
      });
    }

    @Override
    public void onConnectionSuspended(int i) {
      Log.e(TAG, "Connecting to GoogleApiClient suspended.");
      sendError();
    }
  };

  private GoogleApiClient.ConnectionCallbacks connectionRemoveListener = new GoogleApiClient.ConnectionCallbacks() {
    @Override
    public void onConnected(Bundle bundle) {
      System.out.println("GREAT SUCCESS!");
      List<String> removeIds = new ArrayList<>();
      for (NamedGeofence namedGeofence : namedGeofencesToRemove) {
        removeIds.add(namedGeofence.id);
      }

      if (removeIds.size() > 0) {
        PendingResult<Status> result = LocationServices.GeofencingApi.removeGeofences(googleApiClient1, removeIds);
        result.setResultCallback(new ResultCallback<Status>() {
          @Override
          public void onResult(Status status) {
            if (status.isSuccess()) {
              removeSavedGeofences();
            } else {
              Log.e(TAG, "Removing geofence failed: " + status.getStatusMessage());
              sendError();
            }
          }
        });
      }
    }

    @Override
    public void onConnectionSuspended(int i) {
      Log.e(TAG, "Connecting to GoogleApiClient suspended.");
      sendError();
    }
  };
  // endregion

  // region OnConnectionFailedListener

  private GoogleApiClient.OnConnectionFailedListener connectionFailedListener = new GoogleApiClient.OnConnectionFailedListener() {
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
      Log.e(TAG, "Connecting to GoogleApiClient failed.");
      sendError();
    }
  };

  // endregion

  // region Interfaces

  public interface GeofenceControllerListener {
    void onGeofencesUpdated();
    void onError();
  }

  // end region

}