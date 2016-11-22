package com.radoslavdosev.userstories.syncservice;

import net.yslibrary.simplepreferences.annotation.Key;
import net.yslibrary.simplepreferences.annotation.Preferences;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Rado on 19.8.2016 г..
 */
@Preferences
public class SyncPreferences {

    @Key(omitGetterPrefix = true)
    protected boolean isLocalDataSynced = false;

    @Key
    protected Set<String> deletedUnSyncedProjectIds = new HashSet<>();

    @Key
    protected Set<String> updatedUnSyncedProjectIds = new HashSet<>();

    @Key
    protected Set<String> deletedUnSyncedUserStoryIds = new HashSet<>();

    @Key
    protected Set<String> updatedUnSyncedUserStoryIds = new HashSet<>();
}
