// AppDatabase.java
package com.me.carbontracker;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;

@Database(entities = {DriveLog.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract DriveLogDAO driveLogDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "carbon_tracker_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
