package com.anonymous.makanga.makanga.historyRecylerVIew;

/**
 * Created by bitsoko on 3/16/18.
 */

public class HistoryObject {
    private String rideId;
    private String time;
    private String destination;

    public HistoryObject(String destination, String rideId, String time){
        this.rideId = rideId;
        this.time = time;
        this.destination = destination;
    }

    public String getRideId(){
        return rideId;
    }

    public String getTime() {
        return time;
    }

    public String getDestination(){return  destination;}
}
