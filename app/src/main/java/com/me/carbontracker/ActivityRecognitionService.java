// ActivityRecognitionService.java
package com.me.carbontracker;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.ArrayList;

public class ActivityRecognitionService extends Service {
    private static final String TAG = "ActivityRecognitionSvc";
    private boolean isDriving = false;
    private long driveStartTime = 0;

    private LocationMapper mapper;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (ActivityRecognitionResult.hasResult(intent)) {
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            handleDetectedActivities(result);
        }

        return START_STICKY;
    }

    private void handleDetectedActivities(ActivityRecognitionResult result) {
        DetectedActivity mostProbableActivity = result.getMostProbableActivity();
        int confidence = mostProbableActivity.getConfidence();
        int activityType = mostProbableActivity.getType();

        String activityString = getActivityString(activityType);

        if (activityType == DetectedActivity.IN_VEHICLE && confidence >= 50) {
            Log.v(TAG, "isDriving: " + isDriving);
            if (!isDriving) {
                // Started driving
                mapper = new LocationMapper(this);
                Toast.makeText(this, "Started Driving", Toast.LENGTH_SHORT).show();
                isDriving = true;
                driveStartTime = System.currentTimeMillis();
                Log.d(TAG, "Driving started at: " + driveStartTime);
            }
        } else {
            if (isDriving) {
                // Stopped driving
                mapper.stopLocationUpdates();
                float kmTraveled = totalDistanceFromPoints(mapper.getLocationList());
                Toast.makeText(this, "Stopped Driving", Toast.LENGTH_SHORT).show();
                isDriving = false;
                long driveEndTime = System.currentTimeMillis();
                Log.d(TAG, "Driving ended at: " + driveEndTime);

                saveDriveLog(driveStartTime, driveEndTime, kmTraveled);
            }
        }

        Intent broadcastIntent = new Intent("ACTIVITY_RECOGNITION_DATA");
        broadcastIntent.putExtra("activity", activityString);
        broadcastIntent.putExtra("confidence", confidence);

        sendBroadcast(broadcastIntent);
    }

    private void saveDriveLog(long startTime, long endTime, float kmTraveled) {
        AppDatabase db = AppDatabase.getDatabase(this);
        DriveLogDAO dao = db.driveLogDao();
        DriveLog driveLog = new DriveLog(startTime, endTime, kmTraveled);
        // Background thread so UI doesn't lock
        new Thread(() -> dao.insertDriveLog(driveLog)).start();
    }

    private String getActivityString(int activityType) {
        switch (activityType) {
            case DetectedActivity.IN_VEHICLE:
                return "In Vehicle";
            case DetectedActivity.ON_BICYCLE:
                return "On Bicycle";
            case DetectedActivity.ON_FOOT:
                return "On Foot";
            case DetectedActivity.RUNNING:
                return "Running";
            case DetectedActivity.STILL:
                return "Still";
            case DetectedActivity.WALKING:
                return "Walking";
            case DetectedActivity.TILTING:
                return "Tilting";
            case DetectedActivity.UNKNOWN:
                return "Unknown";
            default:
                return "Unidentifiable Activity";
        }
    }

    private float totalDistanceFromPoints(ArrayList<Location> points) {
        float total = 0.0F;

        for (int i = 0; i < points.size() - 1; i++) {
            total += points.get(i).distanceTo(points.get(i + 1)) / 1000;
            Log.d(TAG, "Total: " + String.valueOf(total));
        }

        return total;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}