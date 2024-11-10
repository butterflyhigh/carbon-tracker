package com.me.carbontracker;

import android.util.Log;

public class Car {
    private final String make;
    private final String model;
    private final long emissions;

    private final String TAG = "Car";

    public Car(String[] data) {
        this.make = data[0];
        this.model = data[1];
        this.emissions = Long.parseLong(data[data.length - 1]);
    }

    public String getMake() { return make; }
    public String getModel() { return model; }
    public long getEmissions() { return emissions; }
}
