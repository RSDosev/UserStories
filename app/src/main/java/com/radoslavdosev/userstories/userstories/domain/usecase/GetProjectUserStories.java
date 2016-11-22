package com.radoslavdosev.userstories.userstories.domain.usecase;

import android.support.annotation.NonNull;

import com.radoslavdosev.userstories.base.domain.UseCase;
import com.radoslavdosev.userstories.data.DataSource;
import com.radoslavdosev.userstories.data.Repository;
import com.radoslavdosev.userstories.data.model.Project;
import com.radoslavdosev.userstories.data.model.UserStory;

import java.util.Collections;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by Rado on 25.8.2016 г..
 */
public class GetProjectUserStories extends UseCase<GetProjectUserStories.RequestValues, GetProjectUserStories.ResponseValue> {

    private final Repository mProjectsRepository;

    public GetProjectUserStories(@NonNull final Repository projectsRepository) {
        mProjectsRepository = checkNotNull(projectsRepository, "projectsRepository cannot be null!");
    }

    @Override
    protected void executeUseCase(final RequestValues values) {
        final int projectId = values.getProjectId();
        final int remoteProjectId = values.getRemoteProjectId();
        if (values.toForceUpdate()) {
            mProjectsRepository.refresh();
        }

        mProjectsRepository.getProject(projectId, remoteProjectId, new DataSource.LoadDataCallback<Project>() {
            @Override
            public void onDataLoaded(@NonNull final Project data) {
                getUseCaseCallback().onSuccess(new ResponseValue(data.getUserStories()));
            }

            @Override
            public void onDataNotAvailable() {
                getUseCaseCallback().onSuccess(new ResponseValue(Collections.EMPTY_LIST));
            }

            @Override
            public void onError(String message) {
                getUseCaseCallback().onError(message);
            }
        });
    }

    public static final class RequestValues implements UseCase.RequestValues {
        private boolean mToForceUpdate;
        private final int mProjectId;
        private final int mRemoteProjectId;

        public RequestValues(final int projectId, final int remoteProtectId, final boolean toForceUpdate) {
            checkArgument(projectId > 0, "projectId cannot be 0 or negative!");
            mToForceUpdate = toForceUpdate;
            mProjectId = projectId;
            mRemoteProjectId = remoteProtectId;
        }

        public boolean toForceUpdate() {
            return mToForceUpdate;
        }

        public int getProjectId() {
            return mProjectId;
        }

        public int getRemoteProjectId() {
            return mRemoteProjectId;
        }
    }

    public static final class ResponseValue implements UseCase.ResponseValue {

        private final List<UserStory> mUserStories;

        public ResponseValue(@NonNull final List<UserStory> userStories) {
            mUserStories = checkNotNull(userStories, "userStories cannot be null!");
        }

        public List<UserStory> getUserStories() {
            return mUserStories;
        }
    }
}
