package com.radoslavdosev.userstories.data.remote;

import com.radoslavdosev.userstories.data.model.Project;
import com.radoslavdosev.userstories.data.model.User;
import com.radoslavdosev.userstories.data.remote.entities.SaveProjectModel;
import com.radoslavdosev.userstories.data.remote.entities.SaveUserStoryModel;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Created by Rado on 24.8.2016 г..
 */
public interface ProjectsAPI {
    String BASE_URL = "https://dirigiblei302281trial.hanatrial.ondemand.com/services/js/us/services/";

    @POST("loginreg")
    Call<List<Project>> loginRegisterUser(@Body User user);

    @GET("projects")
    Call<List<Project>> getUsersProjects(@Query("userId") String userId);

    @POST("projects")
    Call<ResponseBody> saveProject(@Body SaveProjectModel body);

    @DELETE("projects")
    Call<Void> deleteProject(@Query("projectId") int projectId);

    @POST("userStories")
    Call<ResponseBody> saveUserStory(@Body SaveUserStoryModel body);

    @DELETE("userStories")
    Call<Void> deleteUserStory(@Query("userStoryId ") int userStoryId);
}
