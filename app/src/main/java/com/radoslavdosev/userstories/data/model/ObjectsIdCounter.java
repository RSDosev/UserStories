package com.radoslavdosev.userstories.data.model;

import android.content.Context;
import android.support.annotation.NonNull;


import com.radoslavdosev.userstories.UserStoriesApplication;

import net.yslibrary.simplepreferences.annotation.Key;
import net.yslibrary.simplepreferences.annotation.Preferences;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Rado on 19.8.2016 г..
 */
public class ObjectsIdCounter {
    private static ObjectsIdCounter sInstance;

    public static ObjectsIdCounter get() {
        if (sInstance == null) {
            sInstance = new ObjectsIdCounter();
        }
        return sInstance;
    }

    private AtomicInteger mProjectsIdCounter, mUserStoriesIdCounter;
    private DataPrefs mPrefs;


    private ObjectsIdCounter() {
        mPrefs = DataPrefs.create(UserStoriesApplication.get());
        mProjectsIdCounter = new AtomicInteger(mPrefs.getLastProjectId());
        mUserStoriesIdCounter = new AtomicInteger(mPrefs.getLastUserStoryId());
    }

    public int getNewProjectId() {
        final int nextId = mProjectsIdCounter.incrementAndGet();
        mPrefs.setLastProjectId(nextId);
        return nextId;
    }

    public int getNewUserStoryId() {
        final int nextId = mUserStoriesIdCounter.incrementAndGet();
        mPrefs.setLastUserStoryId(nextId);
        return nextId;
    }

    @Preferences
    public static class Data {
        @Key
        protected int lastProjectId = 0;

        @Key
        protected int lastUserStoryId = 0;
    }
}
