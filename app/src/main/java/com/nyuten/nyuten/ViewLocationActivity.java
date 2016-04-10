package com.nyuten.nyuten;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class ViewLocationActivity extends AppCompatActivity {
    String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_location);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        name = getIntent().getStringExtra("name");
        setTitle(name);
        if(name.equals("Dibner")){
            ((ImageView)findViewById(R.id.view_image)).setImageResource(R.drawable.dibner);
            ((TextView)findViewById(R.id.view_content)).setText("The Bern Dibner Library of Science and Technology strives to offer exceptional service by acquiring, organizing, and providing access to information resources, specializing in the fields of science, engineering and technology management. In partnership with faculty, we employ state-of-the-art technologies to support learning, teaching, research and innovation. The Library provides an enriched cultural and social environment that  fosters interdisciplinary collaboration and creativity.");
        }
        else if (name.equals("Chipotle")){
            ((ImageView)findViewById(R.id.view_image)).setImageResource(R.drawable.chipotle);
            ((TextView)findViewById(R.id.view_content)).setText("When Chipotle opened its first restaurant in 1993, the idea was simple: show that food served fast didn't have to be a “fast-food” experience. Using high-quality raw ingredients, classic cooking techniques, and distinctive interior design, we brought features from the realm of fine dining to the world of quick-service restaurants.\n" +
                    "\n" +
                    "Over 20 years later, our devotion to finding the very best ingredients we can—with respect for animals, farmers, and the environment—is shown through our Food With Integrity commitment. And as we grow, our dedication to creating an exceptional experience for our customers is the natural result of cultivating a culture of genuine, rewarding opportunities for our employees.");
        }
        else{
            ((ImageView)findViewById(R.id.view_image)).setImageResource(R.drawable.gym);
            ((TextView)findViewById(R.id.view_content)).setText("Located at Six MetroTech Center in downtown Brooklyn, the Brooklyn Athletic Facility features a regulation-sized basketball court, an aerobic fitness room, and a physical fitness room.\n" +
                    "\n" +
                    "Located directly inside the Jacobs building, the regulation-sized full court is available for free play basketball, volleyball, and other activities. Free play basketball is based on a challenge system. The court also hosts a number of School of Engineering events, including new student orientation, admissions, and career fairs.\n" +
                    "\n" +
                    "The facility previously served as the gym for the Polytechnic Institute of NYU, which officially merged with NYU and became the Polytechnic Schoolof Engineering on January 1, 2014.\n" +
                    "\n" +
                    "Located on the lower level of the Jacobs building, this 44' x 63' room contains a diverse set of cardio machines, including upright bikes, recumbent bikes, spin bikes, elliptical cross-trainers, stair steppers, and treadmills. In addition to all of the cardio equipment, the AFR also includes multiple selectorized machines, medicine and plyometric stability balls, yoga mats, foam rollers, and an open area that can be utilized for stretching or individualized exercises.\n" +
                    "\n" +
                    "The 62' x 54' Physical Fitness Room hosts all of Brooklyn Athletic Facility's strength and weight training equipment. It includes adjustable benches, free weights ranging from 1lbs to 100lbs, plate-loaded machines, weighted barbells, and combo racks for various weightlifting exercises. The space also includes office space for NYU Athletics staff.");
        }
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                //        .setAction("Action", null).show();
                Intent j = new Intent(ViewLocationActivity.this, UpdateLocationActivity.class);
                j.putExtra("name", name);
                startActivity(j);
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
