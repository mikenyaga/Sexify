package com.anonymous.sexify.sexify.historyRecylerVIew;

/**
 * Created by bitsoko on 3/16/18.
 */

public class HistoryObject {
    private String rideId;
    private String time;

    public HistoryObject(String rideId,String time){
        this.rideId = rideId;
        this.time = time;
    }

    public String getRideId(){
        return rideId;
    }

    public String getTime() {
        return time;
    }
}
