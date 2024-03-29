package com.harman.phonehealth.util;

import android.content.Context;

import androidx.room.Room;

import com.harman.phonehealth.database.AppDataBase;

public class RoomUtils {
    public static RoomUtils mInstance;
    public static AppDataBase db;

    public static AppDataBase getDataBase(Context context) {
        if (db == null) {
            db = Room.databaseBuilder(context,
                    AppDataBase.class, "database-name").build();
        }
        return db;
    }
}
