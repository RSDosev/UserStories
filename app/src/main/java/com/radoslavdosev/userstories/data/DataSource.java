package com.radoslavdosev.userstories.data;

import com.radoslavdosev.userstories.data.model.Project;
import com.radoslavdosev.userstories.data.model.User;
import com.radoslavdosev.userstories.data.model.UserStory;

import java.util.List;

/**
 * Created by Rado on 8.8.2016 г..
 */
public interface DataSource {
    interface DataErrorCallback {
        void onError(String message);
    }

    interface LoadDataCallback<T> extends DataErrorCallback {

        void onDataLoaded(T data);

        void onDataNotAvailable();
    }

    interface SaveDataCallback<T> extends DataErrorCallback{

        void onDataSaved(T data);
    }

    interface DeleteDataCallback<T> extends DataErrorCallback{

        void onDataDeleted(T data);
    }

    void loginRegister(User user, LoadDataCallback<List<Project>> onProjectsLoaded);

    void logout();

    User getLoggedUser();

    void getProjects(LoadDataCallback<List<Project>> callback);

    void getProject(int projectId, LoadDataCallback<Project> callback);

    void saveProject(Project project, SaveDataCallback<Project> callback);

    void saveAndReplaceProjects(List<Project> projects, SaveDataCallback<List<Project>> callback);

    void deleteProject(Project project, DeleteDataCallback<Project> callback);

    void deleteAllProjects(DeleteDataCallback callback);

    void saveUserStory(int projectId, UserStory userStory, SaveDataCallback<UserStory> callback);

    void deleteUserStory(UserStory userStory, DeleteDataCallback<UserStory> callback);

    void deleteAllUserStoriesPerProject(int projectId, DeleteDataCallback callback);
}
