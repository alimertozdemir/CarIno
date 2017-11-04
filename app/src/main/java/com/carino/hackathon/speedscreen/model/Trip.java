package com.carino.hackathon.speedscreen.model;

import com.google.gson.Gson;

import java.util.Date;

/**
 * Created by alimertozdemir on 4.11.2017.
 */

public class Trip {

    private Date startDate;
    private Date finishDate;
    private String startLocation;
    private String stoppedLocation;
    private double fuelConsumption;

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getFinishDate() {
        return finishDate;
    }

    public void setFinishDate(Date finishDate) {
        this.finishDate = finishDate;
    }

    public String getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(String startLocation) {
        this.startLocation = startLocation;
    }

    public String getStoppedLocation() {
        return stoppedLocation;
    }

    public void setStoppedLocation(String stoppedLocation) {
        this.stoppedLocation = stoppedLocation;
    }

    public double getFuelConsumption() {
        return fuelConsumption;
    }

    public void setFuelConsumption(double fuelConsumption) {
        this.fuelConsumption = fuelConsumption;
    }
}
