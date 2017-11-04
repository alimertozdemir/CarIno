package com.carino.hackathon.speedscreen.model;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alimertozdemir on 4.11.2017.
 */

public class MyTrip implements BaseModel {

    private List<Trip> myTrips = new ArrayList<Trip>();

    public List<Trip> getMyTrips() {
        return myTrips;
    }

    public void setMyTrips(List<Trip> myTrips) {
        this.myTrips = myTrips;
    }

    @Override
    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
