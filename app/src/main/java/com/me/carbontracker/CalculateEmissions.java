package com.me.carbontracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.List;

public class CalculateEmissions {
    private AppDatabase appDb;
    private float totalDistance;
    private List<DriveLog> allLogs;
    private SharedPreferences sharedPreferences;
    private long savedCo2;

    private static final String PREFS_NAME = "app_settings";
    private static final String KEY_CO2 = "co2"; // g/km

    private final String TAG = "CalculateEmissions";

    public CalculateEmissions(Context context, AppDatabase db) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        new Thread(() -> {
            appDb = db;
            allLogs = db.driveLogDao().getAllDriveLogs();
            totalDistance = 0.0F;
            savedCo2 = sharedPreferences.getLong(KEY_CO2, 1);

            for (DriveLog log : allLogs) {
                float distance = log.kmTraveled;
                totalDistance += distance;
            }
            Log.d(TAG, "Total distance: " + totalDistance);
            Log.d(TAG + ":36", "Shared Preferences: " + sharedPreferences.getAll().toString());
        }).start();
    }

    public float totalCO2() {
        savedCo2 = sharedPreferences.getLong(KEY_CO2, 190);
        Log.d(TAG, "CO2 emissions: " + savedCo2 + "// Total Distance: " + totalDistance + "// Math: " + savedCo2 * totalDistance);
        return (savedCo2 * totalDistance); // kg
    }

    public float getTotalDistance() {
        return totalDistance;
    }
}
