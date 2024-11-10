package com.me.carbontracker;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.ArrayList;

@Entity(tableName = "drive_logs")
public class DriveLog {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public long startTime;
    public long endTime;
    public float kmTraveled;

    public DriveLog(long startTime, long endTime, float kmTraveled) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.kmTraveled = kmTraveled;
    }
}
