package com.radoslavdosev.userstories;

import android.app.Application;

import com.raizlabs.android.dbflow.config.FlowConfig;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.squareup.leakcanary.LeakCanary;

/**
 * Created by Rado on 16.8.2016 г..
 */
public class UserStoriesApplication extends Application {
    private static UserStoriesApplication thizz;

    @Override
    public void onCreate() {
        super.onCreate();
        FlowManager.init(new FlowConfig.Builder(this).openDatabasesOnInit(true).build());
        LeakCanary.install(this);
        thizz = this;
    }

    public static UserStoriesApplication get(){
        return thizz;
    }
}
