package com.nyuten.nyuten;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yatin_000 on 4/15/2016.
 */
public class StatusAdapter extends BaseAdapter {
    Context context;
    List<Status> parseObjectList;

    public StatusAdapter(Context context,  ArrayList<Status> items) {
        super();
        this.context = context;
        parseObjectList = items;
    }

    @Override
    public int getCount(){
        return parseObjectList.size();
    }

    @Override
    public Object getItem(int position) {
        return parseObjectList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            v = LayoutInflater.from(context).inflate(R.layout.status_view, parent, false);
        }

        Status p = parseObjectList.get(position);
        System.out.println("GOT PARSEOBJECT");
        System.out.println("status: " + p.getStatus());
        System.out.println("time: " + p.getTime());
        if (p != null) {
            TextView tt1 = (TextView) v.findViewById(R.id.statustxt);
            TextView tt2 = (TextView) v.findViewById(R.id.timeTxt);

            if (tt1 != null) {
                tt1.setText(p.getStatus());
            }

            if (tt2 != null) {
                tt2.setText(p.getTime());
            }

        }

        return v;
    }
}
