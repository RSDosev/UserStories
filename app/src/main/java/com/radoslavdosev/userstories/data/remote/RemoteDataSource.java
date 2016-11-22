package com.radoslavdosev.userstories.data.remote;

import android.support.annotation.NonNull;

import com.radoslavdosev.userstories.data.DataSource;
import com.radoslavdosev.userstories.data.model.Project;
import com.radoslavdosev.userstories.data.model.User;
import com.radoslavdosev.userstories.data.model.UserStory;
import com.radoslavdosev.userstories.data.remote.entities.SaveProjectModel;
import com.radoslavdosev.userstories.data.remote.entities.SaveUserStoryModel;
import com.radoslavdosev.userstories.util.Mapper;

import java.io.IOException;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by Rado on 15.8.2016 г..
 */
public class RemoteDataSource implements DataSource {
    private static RemoteDataSource sInstance;

    public static RemoteDataSource get() {
        if (sInstance == null) {
            sInstance = new RemoteDataSource();
        }
        return sInstance;
    }

    private final ProjectsAPI mRestAPI;
    private String userId;

    private RemoteDataSource() {
        final HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        final OkHttpClient httpClient = new OkHttpClient.Builder().addInterceptor(loggingInterceptor).build();

        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ProjectsAPI.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient)
                .build();

        mRestAPI = retrofit.create(ProjectsAPI.class);
    }


    @Override
    public void loginRegister(@NonNull final User user, @NonNull final LoadDataCallback<List<Project>> callback) {
        final Call<List<Project>> call = mRestAPI.loginRegisterUser(user);
        call.enqueue(new Callback<List<Project>>() {
                         @Override
                         public void onResponse(Call<List<Project>> call, Response<List<Project>> response) {
                             if (response.isSuccessful()) {
                                 userId = user.getId();

                                 if (response.body().size() == 0) {
                                     callback.onDataNotAvailable();
                                 } else {
                                     callback.onDataLoaded(response.body());
                                 }
                             } else {
                                 callback.onError(response.message());
                             }
                         }

                         @Override
                         public void onFailure(Call<List<Project>> call, Throwable t) {
                             callbackNetworkError(callback);
                         }
                     }
        );
    }

    @Override
    public void logout() {
        // not implemented yet
    }

    @Override
    public User getLoggedUser() {
        // not implemented yet
        return null;
    }

    @Override
    public void getProjects(@NonNull final LoadDataCallback<List<Project>> callback) {
        checkNotNull(userId, "userId must be set to make the request");

        final Call<List<Project>> call = mRestAPI.getUsersProjects(userId);
        call.enqueue(new Callback<List<Project>>() {
            @Override
            public void onResponse(Call<List<Project>> call, Response<List<Project>> response) {
                if (response.isSuccessful()) {
                    if (response.body().size() == 0) {
                        callback.onDataNotAvailable();
                    } else {
                        callback.onDataLoaded(response.body());
                    }
                } else {
                    callback.onError(response.message());
                }
            }

            @Override
            public void onFailure(Call<List<Project>> call, Throwable t) {
                callbackNetworkError(callback);
            }
        });
    }

    @Override
    public void getProject(int projectId, LoadDataCallback<Project> callback) {
        // not implemented yet
    }

    /*
        Save the project on the remote server and get a remoteId,
        which represents the id of this project on the remote server
     */
    @Override
    public void saveProject(@NonNull final Project project,
                            @NonNull final SaveDataCallback<Project> callback) {
        final SaveProjectModel body = Mapper.toSaveProjectModel(userId, project);

        mRestAPI.saveProject(body).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    final int remoteId;
                    try {
                        remoteId = Integer.valueOf(response.body().string().trim());
                    } catch (IOException e) {
                        callback.onError("There is problem with the server!");
                        return;
                    }
                    project.setRemoteId(remoteId);
                    project.setRemoteSynced(true);
                    callback.onDataSaved(project);
                } else {
                    callback.onError(response.message());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                callbackNetworkError(callback);
            }
        });
    }

    @Override
    public void saveAndReplaceProjects
            (List<Project> projects, SaveDataCallback<List<Project>> callback) {
        // not implemented yet
    }

    @Override
    public void deleteProject(@NonNull final Project project, @NonNull final DeleteDataCallback<Project> callback) {

        mRestAPI.deleteProject(project.getRemoteId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    project.setRemoteSynced(true);
                    callback.onDataDeleted(project);
                } else {
                    callback.onError(response.message());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callbackNetworkError(callback);
            }
        });
    }

    @Override
    public void deleteAllProjects(DeleteDataCallback callback) {
        // not implemented yet
    }

    @Override
    public void saveUserStory(final int remoteProjectId, @NonNull final UserStory
            userStory, @NonNull final SaveDataCallback<UserStory> callback) {
        final SaveUserStoryModel body = Mapper.toSaveUserStoryModel(remoteProjectId, userStory);

        mRestAPI.saveUserStory(body).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    final int remoteId;
                    try {
                        remoteId = Integer.valueOf(response.body().string().trim());
                    } catch (IOException e) {
                        callback.onError("There is problem with the server!");
                        return;
                    }
                    userStory.setRemoteId(remoteId);
                    userStory.setRemoteSynced(true);
                    callback.onDataSaved(userStory);
                } else {
                    callback.onError(response.message());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                callbackNetworkError(callback);
            }
        });
    }

    @Override
    public void deleteUserStory(@NonNull final UserStory userStory, @NonNull final DeleteDataCallback<UserStory> callback) {
        checkNotNull(userStory, "userStory must be set to make the request");

        mRestAPI.deleteUserStory(userStory.getRemoteId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    userStory.setRemoteSynced(true);
                    callback.onDataDeleted(userStory);
                } else {
                    callback.onError(response.message());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callbackNetworkError(callback);
            }
        });
    }

    @Override
    public void deleteAllUserStoriesPerProject(int projectId, DeleteDataCallback callback) {
        // not implemented yet
    }

    private void callbackNetworkError(@NonNull final DataErrorCallback callback) {
        callback.onError("There is a problem with the network!");
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
