package com.radoslavdosev.userstories.data.remote.entities;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Rado on 30.8.2016 г..
 */
public class SaveProjectModel {

    @SerializedName("tokenId")
    String userId;

    @SerializedName("name")
    String projectName;

    @SerializedName("projectId")
    Integer projectRemoteId; // using wrapper class to ignore 0 values

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public int getProjectRemoteId() {
        return projectRemoteId;
    }

    public void setProjectRemoteId(int projectRemoteId) {
        this.projectRemoteId = projectRemoteId;
    }
}
