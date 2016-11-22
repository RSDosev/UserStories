package com.radoslavdosev.userstories.userstories;


import android.support.annotation.NonNull;

import com.radoslavdosev.userstories.base.mvp.MvpView;
import com.radoslavdosev.userstories.data.model.UserStory;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Rado on 22.7.2016 г..
 */
public final class UserStoriesMvpContract {
    public interface View extends MvpView {

        void showLoading();

        void hideLoading();

        void showSyncing();

        void hideSyncing();

        void showAllUserStories(List<UserStory> userStories);

        void addUserStoryToUI(UserStory userStory);

        void removeUserStoryFromUI(UserStory userStory);

        void showNoUserStories();

        void showUndoDeletedUserStory(UserStory userStory);

        void showNoNetwork();

        void showError(String message);

    }

    public interface Presenter<V extends MvpView> extends com.radoslavdosev.userstories.base.mvp.Presenter<V> {
        void loadUserStories(boolean toForceUpdate);

        void addNewUserStory();

        void addUserStory(UserStory userStory);

        void saveUserStory(UserStory userStory);

        void deleteUserStory(@NonNull UserStory userStory);

        void scheduleSyncIfNeeded();

        boolean isWithRemoteSync();

        int getProjectId();

        int getRemoteProjectId();
    }
}
