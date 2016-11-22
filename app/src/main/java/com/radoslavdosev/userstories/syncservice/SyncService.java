package com.radoslavdosev.userstories.syncservice;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.os.ResultReceiver;

import com.radoslavdosev.userstories.data.DataSource;
import com.radoslavdosev.userstories.data.Repository;
import com.radoslavdosev.userstories.data.local.LocalDataSource;
import com.radoslavdosev.userstories.data.model.Project;
import com.radoslavdosev.userstories.data.model.UserStory;
import com.radoslavdosev.userstories.data.remote.RemoteDataSource;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

//import com.radoslavdosev.userstories.data.local.LocalDataPrefsPrefs;

/**
 * Created by Rado on 8.9.2016 г..
 */
public class SyncService extends IntentService {
    public static final String EXTRA_RECEIVER = "EXTRA_RECEIVER";
    public static final int RESULT_ALL_SYNCED = 1;
    public static final int RESULT_ERROR = 0;

    private Repository mRepository;
    private SyncPreferencesPrefs mPrefs;
    private ResultReceiver mResultReceiver;
    private boolean newPrSynced = true, newUsSycned = true,
            upAndDelPrSynced = true, upAndDelUsSynced = true;

    public SyncService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mResultReceiver = intent.getParcelableExtra(EXTRA_RECEIVER);
        checkNotNull(mResultReceiver, "No receiver provided. There is nowhere to send the results.");

        mPrefs = SyncPreferencesPrefs.create(this);
        mRepository  = Repository.get(LocalDataSource.get(), RemoteDataSource.get());

        if (!mPrefs.isLocalDataSynced()) {
            sync();
            sendTheResult();
        }
    }

    private void sync() {
        // first load all local projects
        mRepository.getProjects(new DataSource.LoadDataCallback<List<Project>>() {
            @Override
            public void onDataLoaded(@NonNull final List<Project> localProjects) {
                // then sync one at a time
                syncNewProjects(localProjects);
                synNewUserStories(localProjects);
                synUpdatedAndDeletedProjects(localProjects);
                synUpdatedAndDeletedUserStories(localProjects);
            }

            @Override
            public void onDataNotAvailable() {

            }

            @Override
            public void onError(String message) {

            }
        });
    }

    private void syncNewProjects(@NonNull final List<Project> localProjects) {
        for (final Project localProject : localProjects) {
            if (localProject.getRemoteId() == 0) { // if remoteId is 0, it means the pr is new
                mRepository.saveProject(localProject, new DataSource.SaveDataCallback<Project>() {
                    @Override
                    public void onDataSaved(Project project) {
                        localProject.setRemoteId(project.getRemoteId());
                        if (!project.isRemoteSynced()) {
                            newPrSynced = false;
                        }
                    }

                    @Override
                    public void onError(String message) {
                        newPrSynced = false;
                    }
                }, true);
            }
        }
    }

    private void synNewUserStories(@NonNull final List<Project> localProjects) {
        for (final Project project : localProjects) {
            for (final UserStory localUserStory :project.getUserStories()) {
                if (localUserStory.getRemoteId() == 0) {
                    mRepository.saveUserStory(project.getRemoteId(), localUserStory, new DataSource.SaveDataCallback<UserStory>() {
                        @Override
                        public void onDataSaved(UserStory userStory) {
                            localUserStory.setRemoteId(userStory.getRemoteId());
                            if (!userStory.isRemoteSynced()) {
                                newUsSycned = false;
                            }
                        }

                        @Override
                        public void onError(String message) {
                            newUsSycned = false;
                        }
                    });
                }
            }
        }
    }

    private void synUpdatedAndDeletedProjects(@NonNull final List<Project> localProjects) {
        // fetch updated and deleted unsynced project ids from local prefs
        final Set<String> updatedProjectRemoteIds = mPrefs.getUpdatedUnSyncedProjectIds();
        final Set<String> updatedProjectRemoteIdsResult = new LinkedHashSet<>(updatedProjectRemoteIds.size());

        final Set<String> deletedProjectsRemoteIds = mPrefs.getDeletedUnSyncedProjectIds();
        final Set<String> deletedProjectsRemoteIdsResult = new LinkedHashSet<>(deletedProjectsRemoteIds.size());

        // for every unsynced updated project id find the corresponding locally saved
        // project and send it to the remote server, if the request is ok, remove the
        // project id from the unsynced project's ids set. On error save the id in
        // "updatedProjectRemoteIdsResult"
        for (final String updatedProjectId : updatedProjectRemoteIds) {
            for (final Project project : localProjects) {
                if (Integer.parseInt(updatedProjectId) == project.getId()) {
                    mRepository.saveProject(project, new DataSource.SaveDataCallback<Project>() {
                        @Override
                        public void onDataSaved(Project data) {
                        }

                        @Override
                        public void onError(String message) {
                            updatedProjectRemoteIdsResult.add(updatedProjectId);
                        }
                    }, true);
                }
            }
        }

        // for every unsynced deleted project id send it to the remote server,
        // if the request is ok, remove the project id from the unsynced project's ids set
        // On error save the id in "deletedProjectsRemoteIdsResult"
        for (final String deletedProjectRemoteId : deletedProjectsRemoteIds) {
            final Project projectToDelete = new Project(0, null, null);
            projectToDelete.setRemoteId(Integer.parseInt(deletedProjectRemoteId));
            mRepository.deleteProject(projectToDelete, new DataSource.DeleteDataCallback<Project>() {
                @Override
                public void onDataDeleted(Project data) {
                }

                @Override
                public void onError(String message) {
                    deletedProjectsRemoteIdsResult.add(deletedProjectRemoteId);
                }
            }, true);
        }

        if (updatedProjectRemoteIdsResult.size() > 0) {
            mPrefs.setUpdatedUnSyncedProjectIds(updatedProjectRemoteIdsResult);
            upAndDelPrSynced = false;
        }

        if (deletedProjectsRemoteIdsResult.size() > 0) {
            mPrefs.setDeletedUnSyncedProjectIds(deletedProjectsRemoteIdsResult);
            upAndDelPrSynced = false;
        }
    }

    private void synUpdatedAndDeletedUserStories(@NonNull final List<Project> localProjects) {
        // fetch updated and deleted unsynced userstory ids from local prefs
        final Set<String> updatedUserStoryRemoteIds = mPrefs.getUpdatedUnSyncedUserStoryIds();
        final Set<String> updatedUserStoryRemoteIdsResult = new LinkedHashSet<>(updatedUserStoryRemoteIds.size());

        final Set<String> deletedUserStoryRemoteIds = mPrefs.getDeletedUnSyncedUserStoryIds();
        final Set<String> deletedUserStoryRemoteIdsResult = new LinkedHashSet<>(deletedUserStoryRemoteIds.size());

        // for every unsynced updated userstory id find the corresponding locally saved
        // userstory and send it to the remote server, if the request is ok, remove the
        // userstory id from the unsynced userstory's ids set. On error save the id in
        // "updatedUserStoryRemoteIdsResult"
        for (final String updatedUserStoryId : updatedUserStoryRemoteIds) {
            for (final Project project : localProjects) {
                for (final UserStory userStory : project.getUserStories()) {
                    if (Integer.parseInt(updatedUserStoryId) == userStory.getId()) {
                        mRepository.saveUserStory(project.getId(), project.getRemoteId(),
                                userStory, new DataSource.SaveDataCallback<UserStory>() {
                            @Override
                            public void onDataSaved(UserStory data) {
                            }

                            @Override
                            public void onError(String message) {
                                updatedUserStoryRemoteIdsResult.add(updatedUserStoryId);
                            }
                        }, true);
                    }
                }
            }
        }

        // for every unsynced deleted userstory id send it to the remote server,
        // if the request is ok, remove the userstory id from the unsynced userstory's ids set
        // On error save the id in "deletedUserStoryRemoteIdsResult"
        for (final String deletedUserStoryRemoteId : deletedUserStoryRemoteIds) {
            final UserStory userStoryToDelete = new UserStory.Builder(0)
                    .remoteId(Integer.parseInt(deletedUserStoryRemoteId)).build();

            mRepository.deleteUserStory(userStoryToDelete, new DataSource.DeleteDataCallback<UserStory>() {
                @Override
                public void onDataDeleted(UserStory data) {
                }

                @Override
                public void onError(String message) {
                    deletedUserStoryRemoteIdsResult.add(deletedUserStoryRemoteId);
                }
            }, true);
        }

        if (updatedUserStoryRemoteIdsResult.size() > 0) {
            mPrefs.setUpdatedUnSyncedUserStoryIds(updatedUserStoryRemoteIdsResult);
            upAndDelUsSynced = false;
        }

        if (deletedUserStoryRemoteIdsResult.size() > 0) {
            mPrefs.setDeletedUnSyncedUserStoryIds(deletedUserStoryRemoteIdsResult);
            upAndDelUsSynced = false;
        }
    }

    private void sendTheResult() {
        if (newPrSynced && newUsSycned && upAndDelPrSynced && upAndDelUsSynced) {
            mPrefs.setIsLocalDataSynced(true);
            mResultReceiver.send(RESULT_ALL_SYNCED, null);
        } else {
            mResultReceiver.send(RESULT_ERROR, null);
        }
    }

    public static boolean isSyncNeeded(@NonNull final Context context) {
        final SyncPreferencesPrefs prefs = SyncPreferencesPrefs.create(context);
        return prefs.getUpdatedUnSyncedProjectIds().size() > 0
                || prefs.getDeletedUnSyncedProjectIds().size() > 0
                || prefs.getUpdatedUnSyncedUserStoryIds().size() > 0
                || prefs.getDeletedUnSyncedUserStoryIds().size() > 0;
    }

    public static boolean isLocalDataSynced(@NonNull final Context context) {
        return SyncPreferencesPrefs.create(context).isLocalDataSynced();
    }

    public static void addUpdatedUnsyncedProjectRemoteId(@NonNull final Context context, @NonNull final int projectId) {
        final SyncPreferencesPrefs prefs = SyncPreferencesPrefs.create(context);
        final Set<String> updatedUnsyncedProjectsIds = prefs.getUpdatedUnSyncedProjectIds();
        updatedUnsyncedProjectsIds.add(String.valueOf(projectId));

        prefs.setUpdatedUnSyncedProjectIds(updatedUnsyncedProjectsIds);
    }

    public static void addDeletedUnsyncedProjectRemoteId(@NonNull final Context context, @NonNull final int projectId) {
        final SyncPreferencesPrefs prefs = SyncPreferencesPrefs.create(context);
        final Set<String> deletedUnsyncedProjectsIds = prefs.getDeletedUnSyncedProjectIds();
        deletedUnsyncedProjectsIds.add(String.valueOf(projectId));

        prefs.setDeletedUnSyncedProjectIds(deletedUnsyncedProjectsIds);
    }

    public static void addUpdatedUnsyncedUserStoryRemoteId(@NonNull final Context context, @NonNull final int userStoryId) {
        final SyncPreferencesPrefs prefs = SyncPreferencesPrefs.create(context);
        final Set<String> updatedUnsyncedUserStoryIds = prefs.getUpdatedUnSyncedUserStoryIds();
        updatedUnsyncedUserStoryIds.add(String.valueOf(userStoryId));

        prefs.setUpdatedUnSyncedUserStoryIds(updatedUnsyncedUserStoryIds);
    }

    public static void addDeletedUnsyncedUserStoryRemoteId(@NonNull final Context context, @NonNull final int userStoryId) {
        final SyncPreferencesPrefs prefs = SyncPreferencesPrefs.create(context);
        final Set<String> deletedUnsyncedUserStoryIds = prefs.getDeletedUnSyncedUserStoryIds();
        deletedUnsyncedUserStoryIds.add(String.valueOf(userStoryId));

        prefs.setDeletedUnSyncedUserStoryIds(deletedUnsyncedUserStoryIds);
    }

    public IBinder onBind(Intent intent) {
        return null;
    }
}
