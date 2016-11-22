package com.radoslavdosev.userstories.data.model;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rado on 4.8.2016 г..
 */
public class Project {

    private int id;

    @SerializedName("projectId")
    private int remoteId;
    private boolean isRemoteSynced;

    @NonNull
    private String name;
    @Nullable
    private List<UserStory> userStories;


    public Project(final @NonNull String name, final @Nullable List<UserStory> userStories) {
        this(generateProjectId(), name, userStories);
    }

    public Project(final @NonNull int id, final @NonNull String name, final @Nullable List<UserStory> userStories) {
        this.id = id;
        this.name = name;
        this.userStories = userStories;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public int getRemoteId() {
        return remoteId;
    }

    public void setRemoteId(int remoteId) {
        this.remoteId = remoteId;
    }

    public boolean isRemoteSynced() {
        return isRemoteSynced;
    }

    public void setRemoteSynced(boolean remoteSynced) {
        isRemoteSynced = remoteSynced;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @NonNull
    public List<UserStory> getUserStories() {
        if (userStories == null) {
            return new ArrayList<>();
        }
        return userStories;
    }

    public void setUserStories(final @Nullable List<UserStory> userStories) {
        this.userStories = userStories;
    }

    public void update(final Project otherProject) {
        this.name = otherProject.getName();
    }

    @Override
    public boolean equals(Object object) {
        // self check
        if (this == object)
            return true;
        // null check
        if (object == null)
            return false;
        // type check and cast
        if (getClass() != object.getClass())
            return false;
        final Project otherProject = (Project) object;
        // field comparison
        return id == otherProject.getId()
                && name.equals(otherProject.getName());
    }

    @Override
    public int hashCode() {
        return id * 27 + (name.hashCode() * 19);
    }

    public static int generateProjectId(){
        return ObjectsIdCounter.get().getNewProjectId();
    }
}
