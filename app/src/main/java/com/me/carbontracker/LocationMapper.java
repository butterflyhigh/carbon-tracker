package com.me.carbontracker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.location.*;

import java.util.ArrayList;
import java.util.List;

public class LocationMapper {

    private static final long LOCATION_REQUEST_INTERVAL = 2 * 60 * 1000; // 2 minutes in milliseconds

    private final Context context;
    private final ArrayList<Location> locationList;
    private final FusedLocationProviderClient fusedLocationClient;
    private final Handler handler;
    private final Runnable locationRunnable;

    private final String TAG = "LocationMapper";

    public LocationMapper(@NonNull Context context) {
        this.context = context.getApplicationContext();
        this.locationList = new ArrayList<>();
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(this.context);
        this.handler = new Handler(Looper.getMainLooper());

        this.locationRunnable = new Runnable() {
            @Override
            public void run() {
                requestLocation();
                handler.postDelayed(this, LOCATION_REQUEST_INTERVAL);
            }
        };
    }

    /**
     * Starts the periodic location updates.
     */
    public void startLocationUpdates() {
        Log.d(TAG, "Started logging location");
        handler.post(locationRunnable);
    }

    /**
     * Stops the periodic location updates.
     */
    public void stopLocationUpdates() {
        Log.d(TAG, "Stopped logging location");
        handler.removeCallbacks(locationRunnable);
    }

    public ArrayList<Location> getLocationList() {
        return new ArrayList<>(locationList);
    }

    @SuppressLint("MissingPermission")
    private void requestLocation() {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        locationList.add(location);
                    } else {
                        requestNewLocationData();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, e.toString());
                });
    }

    @SuppressLint("MissingPermission")
    private void requestNewLocationData() {
        LocationRequest locationRequest = new LocationRequest
                .Builder(10000)
                .build();

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private final LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationResult == null) {
                Log.d(TAG, "Location request null");
                return;
            }

            locationList.addAll(locationResult.getLocations());
            Log.d(TAG, String.valueOf(locationList));

            fusedLocationClient.removeLocationUpdates(this);
        }
    };
}
