package com.radoslavdosev.userstories.projects.domain.usecase;

import android.support.annotation.NonNull;

import com.radoslavdosev.userstories.base.domain.UseCase;
import com.radoslavdosev.userstories.data.DataSource;
import com.radoslavdosev.userstories.data.Repository;
import com.radoslavdosev.userstories.data.model.Project;
import com.radoslavdosev.userstories.syncservice.SyncService;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by Rado on 25.8.2016 г..
 */
public class SaveProject extends UseCase<SaveProject.RequestValues, SaveProject.ResponseValue> {

    private final Repository mProjectsRepository;

    public SaveProject(@NonNull final Repository projectsRepository) {
        mProjectsRepository = checkNotNull(projectsRepository, "projectsRepository cannot be null!");
    }

    @Override
    protected void executeUseCase(final RequestValues values) {
        final Project projectToSave = values.getProject();
        projectToSave.setRemoteSynced(false);

        mProjectsRepository.saveProject(projectToSave, new DataSource.SaveDataCallback<Project>() {
            @Override
            public void onDataSaved(@NonNull final Project data) {
                getUseCaseCallback().onSuccess(new ResponseValue(data));
            }

            @Override
            public void onError(String message) {
                getUseCaseCallback().onError(message);
            }
        }, values.toRemoteSync());
    }

    public static final class RequestValues implements UseCase.RequestValues {
        private boolean mToRemoteSync;
        private Project mProject;

        public RequestValues(@NonNull final Project project, final boolean toRemoteSync) {
            this.mProject = checkNotNull(project, "project cannot be null!");
            this.mToRemoteSync = toRemoteSync;
        }
        public Project getProject() {
            return mProject;
        }

        public boolean toRemoteSync() {
            return mToRemoteSync;
        }
    }

    public static final class ResponseValue implements UseCase.ResponseValue {
        private Project mProject;

        public ResponseValue(@NonNull final Project project) {
            this.mProject = checkNotNull(project, "project cannot be null!");
        }
        public Project getProject() {
            return mProject;
        }
    }
}
