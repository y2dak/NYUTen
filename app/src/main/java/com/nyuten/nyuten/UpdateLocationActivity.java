package com.nyuten.nyuten;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioButton;

public class UpdateLocationActivity extends AppCompatActivity {
    String name;
    RadioButton crowded;
    RadioButton notBad;
    RadioButton empty;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_location);
        crowded = (RadioButton)findViewById(R.id.radioButton);
        notBad = (RadioButton)findViewById(R.id.radioButton2);
        empty = (RadioButton)findViewById(R.id.radioButton3);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        name = getIntent().getStringExtra("name");
        setTitle("Update " + name + "'s status");
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!crowded.isChecked() && !notBad.isChecked() && !empty.isChecked()){
                    Snackbar.make(view, "Must choose one!", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
                else {
                    finish();
                }
            }
        });
    }

}
