package com.radoslavdosev.userstories.userstories.domain.usecase;

import android.support.annotation.NonNull;

import com.radoslavdosev.userstories.base.domain.UseCase;
import com.radoslavdosev.userstories.data.DataSource;
import com.radoslavdosev.userstories.data.Repository;
import com.radoslavdosev.userstories.data.model.UserStory;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by Rado on 25.8.2016 г..
 */
public class DeleteUserStory extends UseCase<DeleteUserStory.RequestValues, DeleteUserStory.ResponseValue> {
    private final Repository mProjectsRepository;

    public DeleteUserStory(@NonNull final Repository projectsRepository) {
        mProjectsRepository = checkNotNull(projectsRepository, "projectsRepository cannot be null!");
    }

    @Override
    protected void executeUseCase(final RequestValues values) {
        final UserStory userStoryToDelete = values.getUserStory();
        userStoryToDelete.setRemoteSynced(false);
        final boolean toRemoteSync = values.toRemoteSync();

        mProjectsRepository.deleteUserStory(userStoryToDelete, new DataSource.DeleteDataCallback<UserStory>() {
            @Override
            public void onDataDeleted(@NonNull final UserStory deletedUserStory) {
                getUseCaseCallback().onSuccess(new ResponseValue(deletedUserStory.isRemoteSynced()));
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

        public RequestValues(@NonNull final UserStory userStory, final boolean toRemoteSync) {
            mUserStory = checkNotNull(userStory, "userStory cannot be null!");
            this.mToRemoteSync = toRemoteSync;
        }

        public UserStory getUserStory() {
            return mUserStory;
        }

        public boolean toRemoteSync() {
            return mToRemoteSync;
        }
    }

    public static final class ResponseValue implements UseCase.ResponseValue {
        private boolean isRemoteSynced;

        public ResponseValue(final boolean isRemoteSynced) {
            this.isRemoteSynced = isRemoteSynced;
        }

        public boolean isRemoteSynced() {
            return isRemoteSynced;
        }
    }
}
