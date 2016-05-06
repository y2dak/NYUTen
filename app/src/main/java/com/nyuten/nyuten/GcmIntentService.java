package com.nyuten.nyuten;


import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Base64;
import android.util.Log;



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
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.Buffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class GcmIntentService extends IntentService {
    private String token;
    public static final String REGISTRATION_SUCCESS="RegistrationSuccess";
    public static final String REGISTRATION_ERROR="RegistrationError";
    public GcmIntentService(){
        super("");
    }
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public GcmIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        System.out.println("NEW GcmIntentService onHandleIntent");
        registerGCM();

    }

    private void registerGCM(){
        Intent regComplete=null;
        token=null;
        try {
                InstanceID instanceID = InstanceID.getInstance(this);
                token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                        GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
                Log.d("token is ", token);
                Log.d("sender id", getString(R.string.gcm_defaultSenderId));
                regComplete=new Intent(REGISTRATION_SUCCESS);
                regComplete.putExtra("token",token);
                sendRegistrationToServer(token);

        } catch (IOException e) {
            e.printStackTrace();
            regComplete = new Intent(REGISTRATION_ERROR);
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(regComplete);
    }

    private void sendRegistrationToServer(String token){
        System.out.println("sendRegistrationToServer");
        String stringUrl="http://nyu-ten.appspot.com/";;
        ConnectivityManager connMgr= (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo=connMgr.getActiveNetworkInfo();
        if(networkInfo!=null && networkInfo.isConnected()){
            Log.d("NETWORK CHECK", "correct");
            new sendRegIDTask().execute(stringUrl);
        }
        else{
            Log.d("NETWORK CHECK", "No network connection");
        }


    }
    private class sendRegIDTask extends AsyncTask<String,Integer,String> {

        @Override
        protected String doInBackground(String... params) {
            URL url= null;
            try {
                url = new URL("http://nyu-ten.appspot.com/");
                HttpURLConnection c= (HttpURLConnection) url.openConnection();
                c.setReadTimeout(15000);
                c.setConnectTimeout(15000);
                c.setRequestMethod("POST");
                c.setDoInput(true);
                c.setDoOutput(true);

                JSONObject obj=new JSONObject();
                obj.put("command","SEND_REG_ID");
                obj.put("regID", token);
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