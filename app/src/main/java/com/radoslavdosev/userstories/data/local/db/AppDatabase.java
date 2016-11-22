package com.radoslavdosev.userstories.data.local.db;

import com.raizlabs.android.dbflow.annotation.Database;

/**
 * Created by Rado on 16.8.2016 г..
 */

@Database(name = AppDatabase.NAME, version = AppDatabase.VERSION)
public class AppDatabase {
    public static final String NAME = "AppDatabase";
    public static final int VERSION = 1;
}
