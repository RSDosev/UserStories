package com.radoslavdosev.userstories.userstories;

import android.content.Context;
import android.support.annotation.NonNull;

import com.radoslavdosev.userstories.base.domain.UseCase;
import com.radoslavdosev.userstories.base.domain.UseCaseRunner;
import com.radoslavdosev.userstories.base.mvp.BasePresenter;
import com.radoslavdosev.userstories.data.model.UserStory;
import com.radoslavdosev.userstories.syncservice.SyncGcmTaskService;
import com.radoslavdosev.userstories.syncservice.SyncService;
import com.radoslavdosev.userstories.userstories.domain.usecase.DeleteUserStory;
import com.radoslavdosev.userstories.userstories.domain.usecase.GetProjectUserStories;
import com.radoslavdosev.userstories.userstories.domain.usecase.SaveUserStory;
import com.radoslavdosev.userstories.util.NetworkUtil;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by Rado on 22.7.2016 г..
 */
public class UserStoriesPresenter extends BasePresenter<UserStoriesMvpContract.View>
        implements UserStoriesMvpContract.Presenter<UserStoriesMvpContract.View> {

    private Context mContext;
    private final UseCaseRunner mUseCaseRunner;
    private final GetProjectUserStories mGetProjectUserStories;
    private final SaveUserStory mSaveUserStory;
    private final DeleteUserStory mDeleteUserStory;
    private final boolean mWithRemoteSync;
    private final int mProjectId, mRemoteProjectId;

    public UserStoriesPresenter(@NonNull Context context,
                                @NonNull UseCaseRunner useCaseRunner,
                                @NonNull GetProjectUserStories getProjectUserStories,
                                @NonNull SaveUserStory saveUserStory,
                                @NonNull DeleteUserStory deleteUserStory,
                                final boolean withRemoteSync,
                                final int projectId,
                                final int remoteProjectId) {
        checkArgument(projectId != 0, "saveUserStory cannot be null!");
        checkArgument(remoteProjectId != 0, "saveUserStory cannot be null!");
        this.mContext = checkNotNull(context, "context cannot be null!");
        this.mUseCaseRunner = checkNotNull(useCaseRunner, "useCaseRunner cannot be null!");
        this.mGetProjectUserStories = checkNotNull(getProjectUserStories, "getProjectUserStories cannot be null!");
        this.mSaveUserStory = checkNotNull(saveUserStory, "saveUserStory cannot be null!");
        this.mDeleteUserStory = checkNotNull(deleteUserStory, "deleteUserStory cannot be null!");
        this.mWithRemoteSync = withRemoteSync;
        this.mProjectId = projectId;
        this.mRemoteProjectId = remoteProjectId;
    }

    @Override
    public void loadUserStories(final boolean toForceUpdate) {
        final boolean toRemoteSync = toForceUpdate && toRemoteSync();
        if (toRemoteSync) {
            getMvpView().showSyncing();
        }
        getMvpView().showLoading();

        final GetProjectUserStories.RequestValues requestValues = new GetProjectUserStories.RequestValues(
                mProjectId,
                mRemoteProjectId,
                toRemoteSync);

        mUseCaseRunner.execute(mGetProjectUserStories, requestValues,
                new UseCase.UseCaseCallback<GetProjectUserStories.ResponseValue>() {
                    @Override
                    public void onSuccess(@NonNull final GetProjectUserStories.ResponseValue response) {
                        if (response.getUserStories().size() == 0) {
                            getMvpView().showNoUserStories();
                        } else {
                            getMvpView().showAllUserStories(response.getUserStories());
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
    public void addNewUserStory() {
        getMvpView().addUserStoryToUI(new UserStory.Builder().build());
    }

    @Override
    public void addUserStory(@NonNull final UserStory userStory) {
        getMvpView().addUserStoryToUI(userStory);
    }

    @Override
    public void saveUserStory(@NonNull final UserStory userStory) {
        final boolean toRemoteSync = toRemoteSync();
        if (toRemoteSync) {
            getMvpView().showSyncing();
        } else {
            // save for sync later if userStory will be saved only locally
            saveForUpdateLaterEventually(userStory.getRemoteId());
        }

        final SaveUserStory.RequestValues requestValues = new SaveUserStory.RequestValues(mProjectId,
                mRemoteProjectId, userStory, toRemoteSync);

        mUseCaseRunner.execute(mSaveUserStory, requestValues, new UseCase.UseCaseCallback<SaveUserStory.ResponseValue>() {
            @Override
            public void onSuccess(@NonNull final SaveUserStory.ResponseValue response) {
                if (response.getUserStory().isRemoteSynced()) {
                    getMvpView().hideSyncing();
                }
            }

            @Override
            public void onError(String message) {
                getMvpView().hideSyncing();
                getMvpView().showError(message);
                saveForUpdateLaterEventually(userStory.getRemoteId());
            }
        });
    }

    private void saveForUpdateLaterEventually(final int remoteUserStoryId) {
        // if remoteUserStoryId is 0, this means the userStory is new
        if (remoteUserStoryId != 0) {
            SyncService.addUpdatedUnsyncedUserStoryRemoteId(mContext, remoteUserStoryId);
        }
    }

    @Override
    public void deleteUserStory(@NonNull final UserStory userStory) {
        final boolean toRemoteSync = toRemoteSync();
        if (toRemoteSync) {
            getMvpView().showSyncing();
        } else {
            // save for sync later if userStory will be deleted only locally
            saveForDeleteLaterEventually(userStory.getRemoteId());
        }

        mUseCaseRunner.execute(mDeleteUserStory, new DeleteUserStory.RequestValues(userStory, toRemoteSync),
                new UseCase.UseCaseCallback<DeleteUserStory.ResponseValue>() {
                    @Override
                    public void onSuccess(@NonNull final DeleteUserStory.ResponseValue response) {
                        if (response.isRemoteSynced()) {
                            getMvpView().hideSyncing();
                        } else {
                            getMvpView().removeUserStoryFromUI(userStory);
                            getMvpView().showUndoDeletedUserStory(userStory);
                        }
                    }

                    @Override
                    public void onError(String message) {
                        getMvpView().hideSyncing();
                        getMvpView().showError(message);
                        saveForDeleteLaterEventually(userStory.getRemoteId());
                    }
                });
    }


    private void saveForDeleteLaterEventually(final int remoteUserStoryId) {
        // if remoteProjectId is 0, this means the userStory is new
        if (remoteUserStoryId != 0) {
            SyncService.addDeletedUnsyncedUserStoryRemoteId(mContext, remoteUserStoryId);
        }
    }

    private boolean toRemoteSync() {
        // mRemoteProjectId != 0 means that the project the userStory belongs is synced
        return mWithRemoteSync && NetworkUtil.isNetworkConnected(mContext) && mRemoteProjectId != 0;
    }

    @Override
    public void scheduleSyncIfNeeded() {
        if (SyncService.isSyncNeeded(mContext)) {
            SyncGcmTaskService.schedule(mContext);
        }
    }

    @Override
    public void detachView() {
        mContext = null;
        super.detachView();
    }

    @Override
    public boolean isWithRemoteSync() {
        return false;
    }

    @Override
    public int getProjectId() {
        return mProjectId;
    }

    @Override
    public int getRemoteProjectId() {
        return mRemoteProjectId;
    }
}
