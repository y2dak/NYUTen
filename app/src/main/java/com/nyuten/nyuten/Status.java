package com.nyuten.nyuten;

/**
 * Created by yatin_000 on 4/15/2016.
 */
public class Status {
    String status;
    String time;

    public Status (String status, String time){
        this.status = status;
        this.time = time;
    }

    public String getStatus(){
        return status;
    }

    public String getTime(){
        return time;
    }
}
