package com.radoslavdosev.userstories.data.remote.entities;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Rado on 30.8.2016 г..
 */
public class SaveUserStoryModel {
    String who;
    String what;
    String why;
    int projectId;

    @SerializedName("userStoryId")
    Integer userStoryRemoteId; // using wrapper class to ignore 0 values

    public String getWho() {
        return who;
    }

    public void setWho(String who) {
        this.who = who;
    }

    public String getWhat() {
        return what;
    }

    public void setWhat(String what) {
        this.what = what;
    }

    public String getWhy() {
        return why;
    }

    public void setWhy(String why) {
        this.why = why;
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public int getUserStoryRemoteId() {
        return userStoryRemoteId;
    }

    public void setUserStoryRemoteId(int userStoryRemoteId) {
        this.userStoryRemoteId = userStoryRemoteId;
    }
}
