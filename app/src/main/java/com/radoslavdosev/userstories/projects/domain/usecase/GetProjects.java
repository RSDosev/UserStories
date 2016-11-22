package com.radoslavdosev.userstories.projects.domain.usecase;

import android.support.annotation.NonNull;

import com.radoslavdosev.userstories.base.domain.UseCase;
import com.radoslavdosev.userstories.data.DataSource;
import com.radoslavdosev.userstories.data.Repository;
import com.radoslavdosev.userstories.data.model.Project;

import java.util.Collections;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by Rado on 25.8.2016 г..
 */
public class GetProjects extends UseCase<GetProjects.RequestValues, GetProjects.ResponseValue> {

    private final Repository mProjectsRepository;

    public GetProjects(@NonNull final Repository projectsRepository) {
        mProjectsRepository = checkNotNull(projectsRepository, "projectsRepository cannot be null!");
    }

    @Override
    protected void executeUseCase(final RequestValues values) {
        if (values.toForceUpdate()) {
            mProjectsRepository.refresh();
        }

        mProjectsRepository.getProjects(new DataSource.LoadDataCallback<List<Project>>() {
            @Override
            public void onDataLoaded(@NonNull final List<Project> data) {
                getUseCaseCallback().onSuccess(new ResponseValue(data));
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

        public RequestValues(final boolean toForceUpdate) {
            this.mToForceUpdate = toForceUpdate;
        }

        public boolean toForceUpdate() {
            return mToForceUpdate;
        }
    }

    public static final class ResponseValue implements UseCase.ResponseValue {
        private final List<Project> mProjects;

        public ResponseValue(@NonNull final List<Project> projects) {
            mProjects = checkNotNull(projects, "projects cannot be null!");
        }

        public List<Project> getProjects() {
            return mProjects;
        }
    }
}
