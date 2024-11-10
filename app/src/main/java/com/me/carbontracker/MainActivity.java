package com.me.carbontracker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionClient;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.me.carbontracker.ui.SharedViewModel;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final int PERMISSION_REQUEST_ACTIVITY_RECOGNITION = 1001;
    private static final int PERMISSION_REQUEST_LOCATION = 1;

    private ActivityRecognitionClient activityRecognitionClient;
    private CalculateEmissions calculateEmissions;
    private PendingIntent pendingIntent;

    private AppDatabase db;
    private DriveLogDAO dao;

    private SharedViewModel sharedViewModel;
    private SharedPreferences sharedPreferences;

    private static CarData carData;

    private static final String PREFS_NAME = "app_settings";
    private static final String KEY_SELECTED_CAR = "selected_car";
    private static final String KEY_CO2 = "co2"; // g/km

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPreferences = this.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        if (!sharedPreferences.contains(KEY_SELECTED_CAR)) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(KEY_SELECTED_CAR, "ACURA IXL");
            editor.putLong(KEY_CO2, 190);
            editor.apply();
        }

        sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);

        sharedViewModel.getAddFakeLogsEvent().observe(this, unused -> {
            Toast.makeText(this, "Add Fake Logs Button Clicked", Toast.LENGTH_SHORT).show();
            insertFakeLogs();
        });

        sharedViewModel.getDestroyLogsEvent().observe(this, unused -> {
            Toast.makeText(this, "Destroyed Logs", Toast.LENGTH_SHORT).show();
            destroyLogs();
        });

        sharedViewModel.getRefreshButtonEvent().observe(this, unused -> {
            Log.d(TAG, "Refreshed");
            displayDriveLogs();
        });

        db = AppDatabase.getDatabase(this);
        dao = db.driveLogDao();

        activityRecognitionClient = ActivityRecognition.getClient(this);

        // Permissions check
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACTIVITY_RECOGNITION},
                    PERMISSION_REQUEST_ACTIVITY_RECOGNITION);
        } else {
            // Permission granted :)
            Log.d(TAG, "Activity recognition started");
            startActivityRecognition();
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQUEST_LOCATION);
        }

        BottomNavigationView navView = findViewById(R.id.nav_view);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupWithNavController(navView, navController);

        calculateEmissions = new CalculateEmissions(this, db);

        Log.d(TAG + ":139", "Shared Preferences: " + sharedPreferences.getAll().toString());
        displayDriveLogs();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_ACTIVITY_RECOGNITION || requestCode == PERMISSION_REQUEST_LOCATION) {
            Log.d(TAG, Arrays.toString(grantResults));
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted :)
                startActivityRecognition();
            } else {
                // Permission denied :(
                Toast.makeText(this, "Please make sure you have granted necessary permissions", Toast.LENGTH_LONG).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void startActivityRecognition() {
        //Toast.makeText(this, "Activity service started", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, ActivityRecognitionService.class);
        pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        activityRecognitionClient.requestActivityUpdates(500, pendingIntent)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Successfully requested activity updates"))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to request activity updates", e));
    }

    private void stopActivityRecognition() {
        if (pendingIntent != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            activityRecognitionClient.removeActivityUpdates(pendingIntent)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Successfully removed activity updates"))
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to remove activity updates", e));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopActivityRecognition();
    }

    private final BroadcastReceiver activityReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String activity = intent.getStringExtra("activity");
            int confidence = intent.getIntExtra("confidence", 0);

            displayDriveLogs();
        }
    };

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(activityReceiver, new IntentFilter("ACTIVITY_RECOGNITION_DATA"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(activityReceiver);
    }

    private void displayDriveLogs() {
        new Thread(() -> {
            Log.d(TAG, "Loading logs");
            List<DriveLog> driveLogs = dao.getAllDriveLogs();

            StringBuilder logsBuilder = new StringBuilder();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

            for (DriveLog log : driveLogs) {
                String startTime = sdf.format(new Date(log.startTime));
                String endTime = sdf.format(new Date(log.endTime));
                long durationMillis = log.endTime - log.startTime;
                float kmTraveled = log.kmTraveled;
                float durationSeconds = (float) durationMillis / 1000;
                float durationMinutes = durationSeconds / 60;
                //durationSeconds = durationSeconds % 60;
                long id = log.id;

                logsBuilder
                        .append(startTime + ": Drive with duration ")
                        .append(durationMinutes)
                        .append(" and distance ")
                        .append(kmTraveled)
                        .append("\n\n");
            }

            // Update UI
            runOnUiThread(() -> {
                if (driveLogs.isEmpty()) {
                    sharedViewModel.setDriveLogsText("No drive logs available.");
                } else {
                    sharedViewModel.setDriveLogsText(logsBuilder.toString()); // line 221
                }
            });
        }).start();

        Log.d(TAG + ":279", "Shared Preferences: " + sharedPreferences.getAll().toString());
        sharedViewModel.setTotalEm(String.valueOf(calculateEmissions.totalCO2()));
        sharedViewModel.setTotalDistance(String.valueOf(calculateEmissions.getTotalDistance()));

        Log.d(TAG, "Done loading logs");
    }

    private void insertFakeLogs() {
        new Thread(() -> {
            // Create some fake logs
            long currentTime = System.currentTimeMillis();
            for (int i = 0; i < 5; i++) {
                long startTime = currentTime - ((i + 1) * 24 * 3600000); // 1 day increments
                long endTime = startTime + 3600000; // Lasts 1 hour
                long kmTraveled = 10 * (i + 1);
                DriveLog fakeLog = new DriveLog(startTime, endTime, kmTraveled);
                dao.insertDriveLog(fakeLog);
            }
            // Update the UI
            runOnUiThread(() -> {
                displayDriveLogs();
                Toast.makeText(this, "Fake logs inserted", Toast.LENGTH_SHORT).show();
            });
        }).start();
    }

    private void destroyLogs() {
        new Thread(() -> {
            dao.deleteAllDriveLogs();

            // Update the UI
            runOnUiThread(() -> {
                displayDriveLogs();
                Toast.makeText(this, "Logs destroyed", Toast.LENGTH_SHORT).show();
            });
        }).start();
    }
}