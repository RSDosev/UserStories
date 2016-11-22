package com.radoslavdosev.userstories.projects;

import android.content.Context;
import android.support.annotation.NonNull;

import com.radoslavdosev.userstories.base.domain.UseCase;
import com.radoslavdosev.userstories.base.domain.UseCaseRunner;
import com.radoslavdosev.userstories.base.mvp.BasePresenter;
import com.radoslavdosev.userstories.data.model.Project;
import com.radoslavdosev.userstories.projects.domain.usecase.DeleteProject;
import com.radoslavdosev.userstories.projects.domain.usecase.GetProjects;
import com.radoslavdosev.userstories.projects.domain.usecase.SaveProject;
import com.radoslavdosev.userstories.syncservice.SyncGcmTaskService;
import com.radoslavdosev.userstories.syncservice.SyncService;
import com.radoslavdosev.userstories.util.NetworkUtil;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by Rado on 22.7.2016 г..
 */
public class ProjectsPresenter extends BasePresenter<ProjectsMvpContract.View>
        implements ProjectsMvpContract.Presenter<ProjectsMvpContract.View> {

    private final Context mContext;
    private final UseCaseRunner mUseCaseRunner;
    private final GetProjects mGetProjects;
    private final SaveProject mSaveProject;
    private final DeleteProject mDeleteProject;
    private final boolean mWithRemoteSync;

    public ProjectsPresenter(@NonNull final Context context,
                             @NonNull final UseCaseRunner useCaseRunner,
                             @NonNull final GetProjects getProjects,
                             @NonNull final SaveProject saveProject,
                             @NonNull final DeleteProject deleteProject, final boolean withRemoteSync) {
        this.mContext = checkNotNull(context, "context cannot be null!").getApplicationContext();
        this.mUseCaseRunner = checkNotNull(useCaseRunner, "useCaseRunner cannot be null!");
        this.mGetProjects = checkNotNull(getProjects, "getProjects cannot be null!");
        this.mSaveProject = checkNotNull(saveProject, "saveProject cannot be null!");
        this.mDeleteProject = checkNotNull(deleteProject, "deleteProject cannot be null!");
        this.mWithRemoteSync = withRemoteSync;
    }

    @Override
    public void loadProjects(final boolean toForceUpdate) {
        getMvpView().showLoading();
        final boolean isNetworkAvailable = NetworkUtil.isNetworkConnected(mContext);
        if (toForceUpdate && mWithRemoteSync && isNetworkAvailable) {
            getMvpView().showSyncing();
        }

        mUseCaseRunner.execute(mGetProjects, new GetProjects.RequestValues(toForceUpdate && mWithRemoteSync && isNetworkAvailable),
                new UseCase.UseCaseCallback<GetProjects.ResponseValue>() {
                    @Override
                    public void onSuccess(@NonNull final GetProjects.ResponseValue response) {
                        if (response.getProjects().size() == 0) {
                            getMvpView().showNoProjects();
                        } else {
                            getMvpView().showAllProjects(response.getProjects());
                        }
                        getMvpView().hideSyncing();
                        getMvpView().hideLoading();
                    }

                    @Override
                    public void onError(String message) {
                        getMvpView().hideSyncing();
                        getMvpView().hideLoading();
                        getMvpView().showError(message);
                    }
                });
    }

    @Override
    public void addNewProject() {
        getMvpView().showNewEditProjectUI(null);
    }

    @Override
    public void editProject(Project project) {
        getMvpView().showNewEditProjectUI(project);
    }

    @Override
    public void saveNewProject(@NonNull final String projectName) {
        final Project newProject = new Project(projectName, null);
        saveProject(newProject);
    }

    @Override
    public void saveEditedProject(@NonNull final Project project) {
        saveProject(project);
    }

    private void saveProject(@NonNull final Project project) {
        final boolean toRemoteSync = NetworkUtil.isNetworkConnected(mContext) && mWithRemoteSync;
        if (toRemoteSync) {
            getMvpView().showSyncing();
        } else {
            // save for sync later if project will be saved only locally
            saveForUpdateLaterEventually(project.getRemoteId());
        }

        mUseCaseRunner.execute(mSaveProject, new SaveProject.RequestValues(project, toRemoteSync),
                new UseCase.UseCaseCallback<SaveProject.ResponseValue>() {

                    @Override
                    public void onSuccess(@NonNull final SaveProject.ResponseValue response) {
                        if (response.getProject().isRemoteSynced()) {
                            getMvpView().hideSyncing();
                        } else {
                            getMvpView().addProjectToUI(project);
                        }
                    }

                    @Override
                    public void onError(String message) {
                        getMvpView().hideSyncing();
                        getMvpView().showError(message);
                        saveForUpdateLaterEventually(project.getRemoteId());
                    }
                });
    }

    private void saveForUpdateLaterEventually(final int remoteProjectId) {
        // if remoteProjectId is 0, this means the project is new
        if (remoteProjectId != 0) {
            SyncService.addUpdatedUnsyncedProjectRemoteId(mContext, remoteProjectId);
        }
    }

    @Override
    public void deleteProject(@NonNull final Project project) {
        final boolean toRemoteSync = NetworkUtil.isNetworkConnected(mContext) && mWithRemoteSync;
        if (toRemoteSync) {
            getMvpView().showSyncing();
        } else {
            // save for sync later if project will be deleted only locally
            saveForDeleteLaterEventually(project.getRemoteId());
        }

        mUseCaseRunner.execute(mDeleteProject, new DeleteProject.RequestValues(project, toRemoteSync),
                new UseCase.UseCaseCallback<DeleteProject.ResponseValue>() {

                    @Override
                    public void onSuccess(@NonNull final DeleteProject.ResponseValue response) {
                        if (response.isRemoteSynced()) {
                            getMvpView().hideSyncing();
                        } else {
                            getMvpView().removeProjectFromUI(project);
                            getMvpView().showUndoDeletedProject(project);
                        }
                    }

                    @Override
                    public void onError(String message) {
                        getMvpView().hideSyncing();
                        getMvpView().showError(message);
                        saveForDeleteLaterEventually(project.getRemoteId());
                    }
                });
    }

    private void saveForDeleteLaterEventually(final int remoteProjectId) {
        // if remoteProjectId is 0, this means the project is new
        if (remoteProjectId != 0) {
            SyncService.addDeletedUnsyncedProjectRemoteId(mContext, remoteProjectId);
        }
    }

    @Override
    public void loadProjectUserStories(@NonNull final Project project) {
        getMvpView().showProjectUserStories(project);
    }

    @Override
    public void scheduleSyncIfNeeded() {
        if (SyncService.isSyncNeeded(mContext)) {
            SyncGcmTaskService.schedule(mContext);
        }
    }

    @Override
    public void detachView() {
        super.detachView();
    }

    @Override
    public boolean isWithRemoteSync() {
        return mWithRemoteSync;
    }
}
