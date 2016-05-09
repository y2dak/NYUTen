package com.nyuten.nyuten;

import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nyuten.nyuten.Model.mLocation;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ViewLocationActivity extends AppCompatActivity {
    String name;
    String userId;
    String status;
    ListView statusList;
    ArrayList<Status> statusCollection = new ArrayList<>();
    StatusAdapter statusAdapter;
    SwipeRefreshLayout swipeRefreshLayout;
    mLocation mLocationCurrent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_location);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor("#512DA8"));
        }
        name = getIntent().getStringExtra("name");
        userId = getIntent().getStringExtra("userId");
        status = getIntent().getStringExtra("status");
        mLocationCurrent = getIntent().getParcelableExtra("mLocation");
        ((TextView)findViewById(R.id.location)).setText(name + "\n" + status);
        statusList = (ListView)findViewById(R.id.statusList);
        statusAdapter = new StatusAdapter(this, statusCollection);
        statusList.setAdapter(statusAdapter);
        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.activity_view_swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshStatuses();
            }
        });
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                //        .setAction("Action", null).show();
                Intent j = new Intent(ViewLocationActivity.this, UpdateLocationActivity.class);
                j.putExtra("name", name);
                j.putExtra("userId", userId);
                j.putExtra("mLocation", mLocationCurrent);
                startActivity(j);
                //finish();
            }
        });
    }

    public void refreshStatuses(){//Put in background thread - AsyncTask
        ParseQuery<ParseObject> queryZero = ParseQuery.getQuery("Status");
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
                        if (statusList.size() < 4) {
                            status = statusList.get(0).getString("status");
                        } else {
                            int crowded = 0;
                            int manage = 0;
                            int empty = 0;
                            for (int i = 0; i < statusList.size(); i++) {
                                if (statusList.get(i).getString("status").equals("Crowded")) {
                                    crowded++;
                                } else if (statusList.get(i).getString("status").equals("Manageable")) {
                                    manage++;
                                } else if (statusList.get(i).getString("status").equals("Empty")) {
                                    empty++;
                                }
                            }
                            if (crowded >= manage && crowded >= empty) {
                                status = "Crowded";
                            } else if (manage >= crowded && manage >= empty) {
                                status = "Manageable";
                            } else {
                                status = "Empty";
                            }
                        }
                    } else {
                        status = "No Recent Status";
                    }
                    ((TextView) findViewById(R.id.location)).setText(name + "\n" + status);
                }
            }
        });

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Status");
        if(name.equals("Dibner")){
            ((ImageView)findViewById(R.id.view_image)).setImageResource(R.drawable.dibner1);
            query.whereEqualTo("location", "Dibner");
        }
        else if (name.equals("Chipotle")){
            ((ImageView)findViewById(R.id.view_image)).setImageResource(R.drawable.chipotle);
            query.whereEqualTo("location", "Chipotle");
        }
        else if(name.equals("Gym")){
            ((ImageView)findViewById(R.id.view_image)).setImageResource(R.drawable.gym);
            query.whereEqualTo("location", "Gym");
        }
        else{
            ((ImageView)findViewById(R.id.view_image)).setImageResource(R.drawable.starbucks);
            query.whereEqualTo("location", "Starbucks");
        }
        Calendar cal1 = Calendar.getInstance();
        cal1.set(Calendar.HOUR_OF_DAY,0);
        cal1.set(Calendar.MINUTE,0);
        cal1.set(Calendar.SECOND,0);
        cal1.set(Calendar.MILLISECOND,0);
        Date date1 = cal1.getTime();
        //date.setTime(0);
        System.out.println(date1.toString());
        query.whereGreaterThanOrEqualTo("updatedAt", date1);
        query.addDescendingOrder("updatedAt");
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> status, ParseException e) {
                if (e == null) {
                    Log.d("statuses", "Retrieved " + status.size() + " statuses");
                    statusCollection.clear();
                    if (status.size() == 0) {
                        statusCollection.add(new Status("No updates posted yet for today", ""));
                        System.out.println("EMPTY UPDATES");
                    } else {
                        for (int i = 0; i < status.size(); i++) {
                            try {
                                SimpleDateFormat localDateFormat = new SimpleDateFormat("HH:mm");
                                SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a");
                                String time = localDateFormat.format(status.get(i).getUpdatedAt());
                                Date twentyFour = localDateFormat.parse(time);
                                //System.out.println(twentyFour);
                                //System.out.println(dateFormat.format(twentyFour));
                                String finalTime = dateFormat.format(twentyFour);
                                if (finalTime.startsWith("0")) {
                                    finalTime = finalTime.substring(1);
                                }
                                statusCollection.add(new Status(status.get(i).getString("status"), finalTime));
                                statusAdapter.notifyDataSetChanged();
                            } catch (Exception exc) {
                                exc.printStackTrace();
                            }
                        }
                    }
                } else {
                    Log.d("statuses", "Error: " + e.getMessage());
                }
            }
        });
        statusAdapter.notifyDataSetChanged();
        if(swipeRefreshLayout.isRefreshing()){
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void onResume(){
        super.onResume();
       refreshStatuses();
    }
}
