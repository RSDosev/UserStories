package com.radoslavdosev.userstories.data.model;

import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by Rado on 15.8.2016 г..
 */
public class User implements Serializable{

    @NonNull
    @SerializedName("tokenId")
    private String id;
    @NonNull
    private String displayName;
    @NonNull
    private String givenName;
    @NonNull
    private String familyName;
    @NonNull
    private String email;
    @NonNull
    private String photoUrl;


    public User(@NonNull String id, @NonNull String displayName, @NonNull String givenName,
                @NonNull String familyName, @NonNull String email, @NonNull String photoUrl) {
        this.id = id;
        this.displayName = displayName;
        this.givenName = givenName;
        this.familyName = familyName;
        this.email = email;
        this.photoUrl = photoUrl;
    }


    @NonNull
    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(@NonNull String givenName) {
        this.givenName = givenName;
    }

    @NonNull
    public String getId() {
        return id;
    }

    @NonNull
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(@NonNull String displayName) {
        this.displayName = displayName;
    }

    @NonNull
    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(@NonNull String familyName) {
        this.familyName = familyName;
    }

    @NonNull
    public String getEmail() {
        return email;
    }

    public void setEmail(@NonNull String email) {
        this.email = email;
    }

    @NonNull
    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(@NonNull String photoUrl) {
        this.photoUrl = photoUrl;
    }


}
