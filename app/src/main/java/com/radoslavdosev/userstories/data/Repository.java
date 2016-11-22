package com.radoslavdosev.userstories.data;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.radoslavdosev.userstories.data.local.LocalDataSource;
import com.radoslavdosev.userstories.data.model.Project;
import com.radoslavdosev.userstories.data.model.User;
import com.radoslavdosev.userstories.data.model.UserStory;
import com.radoslavdosev.userstories.data.remote.RemoteDataSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by Rado on 22.7.2016 г..
 */
public class Repository implements DataSource {
    private static Repository sInstance;

    private LocalDataSource mLocalDataSource;
    private RemoteDataSource mRemoteDataSource;
    private HashMap<Integer, Project> mCachedData;
    private boolean mIsCacheDirty;

    private Repository(@NonNull final LocalDataSource localDataSource, @NonNull final RemoteDataSource remoteDataSource) {
        mLocalDataSource = localDataSource;
        mRemoteDataSource = remoteDataSource;
    }

    public static Repository get(@NonNull final LocalDataSource localDataSource, @NonNull final RemoteDataSource remoteDataSource) {
        if (sInstance == null) {
            sInstance = new Repository(localDataSource, remoteDataSource);
        }
        return sInstance;
    }

    public static void destroyInstance() {
        sInstance = null;
    }

    /*
        If user is null, this means that that application continues without the cloud
     */
    @Override
    public void loginRegister(@Nullable final User user, @NonNull final LoadDataCallback<List<Project>> callback) {
        if (user == null) {

        } else {
            mRemoteDataSource.loginRegister(user, new LoadDataCallback<List<Project>>() {
                @Override
                public void onDataLoaded(final List<Project> data) {
                    refreshLocalDataSource(data);
                    getProjectsLocallyAndRefreshCache(callback);
                }

                @Override
                public void onDataNotAvailable() {
                    callback.onDataNotAvailable();
                }

                @Override
                public void onError(String message) {
                    callback.onError(message);
                }
            });
        }
    }


    @Override
    public void logout() {
        mLocalDataSource.logout();
        deleteAllProjects(null);
    }

    @Override
    public User getLoggedUser() {
        return mLocalDataSource.getLoggedUser();
    }

    public void getProject(final int projectId, final int remoteProjectId, @NonNull final LoadDataCallback<Project> callback) {
        // Respond immediately with cache if available and not dirty
        if (mCachedData != null && !mIsCacheDirty) {
            callback.onDataLoaded(mCachedData.get(projectId));
            return;
        }

        if (mIsCacheDirty) {
            mRemoteDataSource.getProject(remoteProjectId, new LoadDataCallback<Project>() {
                @Override
                public void onDataLoaded(Project data) {
                    updateCache(data);
                    updateLocalDataSource(data);
                    callback.onDataLoaded(data);
                }

                @Override
                public void onDataNotAvailable() {
                    callback.onDataNotAvailable();
                }

                @Override
                public void onError(String message) {
                    callback.onError(message);
                }
            });
        } else {
            getProjectLocallyAndUpdateCache(projectId, callback);
        }
    }

    @Override
    public void getProject(final int projectId, @NonNull final LoadDataCallback<Project> callback) {
        // Respond immediately from the cache if it is available and not dirty
        if (mCachedData != null && !mIsCacheDirty) {
            callback.onDataLoaded(mCachedData.get(projectId));
            return;
        }

        getProjectLocallyAndUpdateCache(projectId, callback);
    }

    private void getProjectLocallyAndUpdateCache(final int projectId, @NonNull final LoadDataCallback<Project> callback) {
        mLocalDataSource.getProject(projectId, new LoadDataCallback<Project>() {
            @Override
            public void onDataLoaded(final Project data) {
                updateCache(data);
                callback.onDataLoaded(data);
            }

            @Override
            public void onDataNotAvailable() {
                callback.onDataNotAvailable();
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    @Override
    public void getProjects(@NonNull final LoadDataCallback<List<Project>> callback) {
        // Respond immediately with cache if available and not dirty
        if (mCachedData != null && !mIsCacheDirty) {
            callback.onDataLoaded(new ArrayList<>(mCachedData.values()));
            return;
        }

        if (mIsCacheDirty) {
            mRemoteDataSource.getProjects(new LoadDataCallback<List<Project>>() {
                @Override
                public void onDataLoaded(final List<Project> data) {
                    refreshLocalDataSource(data);
                    getProjectsLocallyAndRefreshCache(callback);
                }

                @Override
                public void onDataNotAvailable() {
                    callback.onDataNotAvailable();
                }

                @Override
                public void onError(String message) {
                    callback.onError(message);
                }
            });
        } else {
            getProjectsLocallyAndRefreshCache(callback);
        }
    }

    private void getProjectsLocallyAndRefreshCache(@NonNull final LoadDataCallback<List<Project>> callback) {
        mLocalDataSource.getProjects(new LoadDataCallback<List<Project>>() {
            @Override
            public void onDataLoaded(final List<Project> data) {
                refreshCache(data);
                callback.onDataLoaded(data);
            }

            @Override
            public void onDataNotAvailable() {
                callback.onDataNotAvailable();
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    /*
        If "withRemote" saves the project in local db first. Then send it to the remote server.
        Then save the received remoteId in the local db.
     */
    public void saveProject(@NonNull final Project project, @NonNull final SaveDataCallback<Project> callback, final boolean withRemote) {
        saveProject(project, callback);
        if (withRemote) {
            mRemoteDataSource.saveProject(project, new SaveDataCallback<Project>() {
                @Override
                public void onDataSaved(final Project data) {
                    saveProject(data, null);
                    callback.onDataSaved(data);
                }

                @Override
                public void onError(String message) {
                    callback.onError(message);
                }
            });
        }
    }

    /*
        Saves the Project in the Local DB and in the memory
     */
    @Override
    public void saveProject(@NonNull final Project project, @Nullable final SaveDataCallback<Project> callback) {
        checkNotNull(project);
        mLocalDataSource.saveProject(project, null);

        // Do in memory cache update to keep the app UI up to date
        if (mCachedData == null) {
            mCachedData = new LinkedHashMap<>();
        }
        mCachedData.put(project.getId(), project);
        if (callback != null) {
            callback.onDataSaved(project);
        }
    }

    @Override
    public void saveAndReplaceProjects(@NonNull final List<Project> projects, @Nullable final SaveDataCallback<List<Project>> callback) {
        checkNotNull(projects, "projects cannot be null");
        if (mIsCacheDirty) {
            mRemoteDataSource.saveAndReplaceProjects(projects, callback);
        } else {
            mLocalDataSource.saveAndReplaceProjects(projects, callback);
            refreshCache(projects);
        }
    }

    public void deleteProject(@NonNull final Project project, @NonNull final DeleteDataCallback<Project> callback, final boolean withRemote) {
        deleteProject(project, callback);

        if (withRemote) {
            mRemoteDataSource.deleteProject(project, new DeleteDataCallback<Project>() {
                @Override
                public void onDataDeleted(Project data) {
                    callback.onDataDeleted(data);
                }

                @Override
                public void onError(String message) {
                    callback.onError(message);
                }
            });
        }
    }

    @Override
    public void deleteProject(@NonNull final Project project, @Nullable final DeleteDataCallback<Project> callback) {
        checkNotNull(project);
        mLocalDataSource.deleteProject(project, null);

        // Do in memory cache update to keep the app UI up to date
        if (mCachedData == null) {
            mCachedData = new LinkedHashMap<>();
        }
        mCachedData.remove(project.getId());
        if (callback != null) {
            callback.onDataDeleted(project);
        }
    }

    @Override
    public void deleteAllProjects(@Nullable final DeleteDataCallback callback) {
        mCachedData.clear();
        mCachedData = null;
        mLocalDataSource.deleteAllProjects(callback);
        if (callback != null) {
            callback.onDataDeleted(null);
        }
    }

    /*
        If "withRemote" saves the userStory in local db first. Then send it to the remote server.
        Then save the received remoteId in the local db.
     */
    public void saveUserStory(final int projectId, final int remoteProjectId, @NonNull final UserStory userStory,
                              @NonNull final SaveDataCallback<UserStory> callback, final boolean withRemote) {
        saveUserStory(projectId, userStory, callback);
        if (withRemote) {
            mRemoteDataSource.saveUserStory(remoteProjectId, userStory, new SaveDataCallback<UserStory>() {
                @Override
                public void onDataSaved(UserStory data) {
                    saveUserStory(projectId, data, null);
                    callback.onDataSaved(data);
                }

                @Override
                public void onError(String message) {
                    callback.onError(message);
                }
            });
        }
    }

    /*
        Saves the UserStory in the Local DB and in the memory
     */
    @Override
    public void saveUserStory(final int projectId, @NonNull final UserStory userStory, @Nullable final SaveDataCallback<UserStory> callback) {
        checkArgument(projectId > 0, "projectId cannot be negative number");
        checkNotNull(userStory);
        mLocalDataSource.saveUserStory(projectId, userStory, null);

        // Do in memory cache update to keep the app UI up to date
        List<UserStory> userStories = mCachedData.get(projectId).getUserStories();
        if (!userStories.contains(userStory)) {
            userStories.add(userStory);
        } else {
            userStories.set(userStories.indexOf(userStory), userStory);
        }
        mCachedData.get(projectId).setUserStories(userStories);

        if (callback != null) {
            callback.onDataSaved(userStory);
        }
    }

    public void deleteUserStory(@NonNull final UserStory userStory, @NonNull final DeleteDataCallback<UserStory> callback, final boolean withRemote) {
        deleteUserStory(userStory, callback);
        if (withRemote) {
            mRemoteDataSource.deleteUserStory(userStory, new DeleteDataCallback<UserStory>() {
                @Override
                public void onDataDeleted(UserStory data) {
                    callback.onDataDeleted(data);
                }

                @Override
                public void onError(String message) {
                    callback.onError(message);
                }
            });
        }
    }

    @Override
    public void deleteUserStory(@NonNull final UserStory userStory, @Nullable final DeleteDataCallback<UserStory> callback) {
        checkNotNull(userStory);
        mLocalDataSource.deleteUserStory(userStory, null);

        // Do in memory cache update to keep the app UI up to date
        for (Map.Entry<Integer, Project> entry : mCachedData.entrySet()) {
            if (entry.getValue().getUserStories().remove(userStory)) {
                return;
            }
        }

        if (callback != null) {
            callback.onDataDeleted(userStory);
        }
    }

    @Override
    public void deleteAllUserStoriesPerProject(final int projectId, @Nullable final DeleteDataCallback callback) {
        mLocalDataSource.deleteAllUserStoriesPerProject(projectId, callback);
        if (mCachedData != null && mCachedData.get(projectId) != null) {
            mCachedData.get(projectId).setUserStories(null);
        }
    }

    public void refresh() {
        mIsCacheDirty = true;
    }

    private void updateCache(@NonNull final Project project) {
        if (mCachedData == null) {
            mCachedData = new LinkedHashMap<>();
        }
        mCachedData.remove(project.getId());
        mCachedData.put(project.getId(), project);
        mIsCacheDirty = false;
    }

    private void refreshCache(@NonNull final List<Project> projects) {
        if (mCachedData == null) {
            mCachedData = new LinkedHashMap<>();
        }
        mCachedData.clear();
        for (Project project : projects) {
            mCachedData.put(project.getId(), project);
        }
        mIsCacheDirty = false;
    }

    private void updateLocalDataSource(@NonNull final Project project) {
        mLocalDataSource.deleteProject(project, null);
        mLocalDataSource.saveProject(project, null);
    }

    private void refreshLocalDataSource(@NonNull final List<Project> projects) {
        mLocalDataSource.deleteAllProjects(null);
        for (Project project : projects) {
            project.setId(Project.generateProjectId());
            mLocalDataSource.saveProject(project, null);
        }
    }
}
