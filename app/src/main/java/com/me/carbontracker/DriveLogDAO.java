package com.me.carbontracker;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface DriveLogDAO {
    @Insert
    void insertDriveLog(DriveLog driveLog);

    @Query("SELECT * FROM drive_logs")
    List<DriveLog> getAllDriveLogs();

    @Query("DELETE FROM drive_logs")
    void deleteAllDriveLogs();

    @Query("DELETE FROM drive_logs WHERE id = :id")
    void deleteDriveLog(int id);
}
