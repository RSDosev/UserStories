package com.radoslavdosev.userstories.projects.domain.usecase;

import android.support.annotation.NonNull;

import com.radoslavdosev.userstories.base.domain.UseCase;
import com.radoslavdosev.userstories.data.DataSource;
import com.radoslavdosev.userstories.data.Repository;
import com.radoslavdosev.userstories.data.model.Project;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by Rado on 25.8.2016 г..
 */
public class DeleteProject extends UseCase<DeleteProject.RequestValues, DeleteProject.ResponseValue> {

    private final Repository mProjectsRepository;

    public DeleteProject(@NonNull final Repository projectsRepository) {
        mProjectsRepository = checkNotNull(projectsRepository, "projectsRepository cannot be null!");
    }

    @Override
    protected void executeUseCase(final RequestValues values) {
        final Project projectToDelete = values.getProject();
        projectToDelete.setRemoteSynced(false);
        final boolean toRemoteSync = values.toRemoteSync();

        mProjectsRepository.deleteProject(projectToDelete, new DataSource.DeleteDataCallback<Project>() {
            @Override
            public void onDataDeleted(@NonNull final Project deletedUserStory) {
                getUseCaseCallback().onSuccess(new ResponseValue(deletedUserStory.isRemoteSynced()));
            }

            @Override
            public void onError(String message) {
                getUseCaseCallback().onError(message);
            }
        }, toRemoteSync);
    }

    public static final class RequestValues implements UseCase.RequestValues {
        private Project mProject;
        private boolean mToRemoteSync;

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
        private boolean isRemoteSynced;

        public ResponseValue(final boolean isRemoteSynced) {
            this.isRemoteSynced = isRemoteSynced;
        }

        public boolean isRemoteSynced() {
            return isRemoteSynced;
        }
    }
}
