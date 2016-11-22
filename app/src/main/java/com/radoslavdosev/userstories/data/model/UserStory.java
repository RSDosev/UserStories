package com.radoslavdosev.userstories.data.model;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by Rado on 22.7.2016 г..
 */
public class UserStory {

    private int id;

    private int remoteId;
    private boolean isRemoteSynced;

    @Nullable
    private String who;
    @Nullable
    private String what;
    @Nullable
    private String why;

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

    @Nullable
    public String getWho() {
        return who;
    }

    @Nullable
    public String getWhat() {
        return what;
    }

    @Nullable
    public String getWhy() {
        return why;
    }

    public void setWho(@Nullable final String who) {
        this.who = who;
    }

    public void setWhat(@Nullable final String what) {
        this.what = what;
    }

    public void setWhy(@Nullable final String why) {
        this.why = why;
    }

    private UserStory(@NonNull final Builder builder) {
        this.id = builder.id;
        this.remoteId = builder.remoteId;
        this.who = builder.who;
        this.what = builder.what;
        this.why = builder.why;
    }

    public static class Builder {
        private int id;
        private int remoteId;
        private String who;
        private String what;
        private String why;

        public Builder() {
            this.id = generateUserStoryId();
        }

        public Builder(@NonNull final int id) {
            this.id = id;
        }

        public Builder remoteId(final int remoteId) {
            this.remoteId = remoteId;
            return this;
        }

        public Builder who(final String who) {
            this.who = who;
            return this;
        }

        public Builder what(final String what) {
            this.what = what;
            return this;
        }

        public Builder why(final String why) {
            this.why = why;
            return this;
        }

        public UserStory build() {
            return new UserStory(this);
        }
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
        final UserStory otherStory = (UserStory) object;
        // field comparison
        return id == otherStory.getId()
                && ((who == null && otherStory.getWho() == null) || (who != null && who.equals(otherStory.getWho())))
                && ((what == null && otherStory.getWhat() == null) || (who != null && who.equals(otherStory.getWhat())))
                && ((why == null && otherStory.getWhy() == null) || (why != null && why.equals(otherStory.getWhy())));
    }

    @Override
    public int hashCode() {
        int hashCode = id * 27;
        if (who != null) {
            hashCode += (who.hashCode() * 19);
        }
        if (what != null) {
            hashCode += (what.hashCode() * 13);
        }
        if (why != null) {
            hashCode += (why.hashCode() * 7);

        }
        return hashCode;
    }

    public static int generateUserStoryId(){
       return ObjectsIdCounter.get().getNewUserStoryId();
    }
}
