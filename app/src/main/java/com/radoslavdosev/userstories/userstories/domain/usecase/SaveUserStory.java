package com.radoslavdosev.userstories.userstories.domain.usecase;

import android.support.annotation.NonNull;

import com.radoslavdosev.userstories.base.domain.UseCase;
import com.radoslavdosev.userstories.data.DataSource;
import com.radoslavdosev.userstories.data.Repository;
import com.radoslavdosev.userstories.data.model.UserStory;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by Rado on 25.8.2016 г..
 */
public class SaveUserStory extends UseCase<SaveUserStory.RequestValues, SaveUserStory.ResponseValue> {

    private final Repository mProjectsRepository;

    public SaveUserStory(@NonNull final Repository projectsRepository) {
        mProjectsRepository = checkNotNull(projectsRepository, "projectsRepository cannot be null!");
    }

    @Override
    protected void executeUseCase(final RequestValues values) {
        final int projectId = values.getProjectId();
        final int remoteProjectId = values.getRemoteProjectId();
        final UserStory userStory = values.getUserStory();
        userStory.setRemoteSynced(false);
        final boolean toRemoteSync = values.toRemoteSync();

        mProjectsRepository.saveUserStory(projectId, remoteProjectId, userStory, new DataSource.SaveDataCallback<UserStory>() {
            @Override
            public void onDataSaved(@NonNull final UserStory data) {
                getUseCaseCallback().onSuccess(new ResponseValue(data));
            }

            @Override
            public void onError(String message) {
                getUseCaseCallback().onError(message);
            }
        }, toRemoteSync);
    }

    public static final class RequestValues implements UseCase.RequestValues {
        private final UserStory mUserStory;
        private boolean mToRemoteSync;
        private final int mProjectId;
        private final int mRemoteProjectId;

        public RequestValues(final int projectId, final int remoteProtectId, @NonNull final UserStory userStory, final boolean toRemoteSync) {
            checkArgument(projectId > 0, "projectId cannot be 0 or negative!");
            mUserStory = checkNotNull(userStory, "userStory cannot be null!");
            mProjectId = projectId;
            mRemoteProjectId = remoteProtectId;
            mToRemoteSync = toRemoteSync;
        }

        public UserStory getUserStory() {
            return mUserStory;
        }

        public int getProjectId() {
            return mProjectId;
        }

        public int getRemoteProjectId() {
            return mRemoteProjectId;
        }

        public boolean toRemoteSync() {
            return mToRemoteSync;
        }
    }

    public static final class ResponseValue implements UseCase.ResponseValue {
        private UserStory mUserStory;

        public ResponseValue(@NonNull final UserStory userStory) {
            this.mUserStory = checkNotNull(userStory, "userStory cannot be null!");
        }

        public UserStory getUserStory() {
            return mUserStory;
        }
    }
}
