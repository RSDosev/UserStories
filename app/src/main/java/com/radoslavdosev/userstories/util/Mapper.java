package com.radoslavdosev.userstories.util;

import android.support.annotation.NonNull;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.radoslavdosev.userstories.data.local.db.ProjectsTable;
import com.radoslavdosev.userstories.data.local.db.UserStoriesTable;
import com.radoslavdosev.userstories.data.model.Project;
import com.radoslavdosev.userstories.data.model.User;
import com.radoslavdosev.userstories.data.model.UserStory;
import com.radoslavdosev.userstories.data.remote.entities.SaveProjectModel;
import com.radoslavdosev.userstories.data.remote.entities.SaveUserStoryModel;
import com.raizlabs.android.dbflow.structure.container.ForeignKeyContainer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rado on 16.8.2016 г..
 */
public final class Mapper {

    private Mapper(){
        // no instances allowed
    }

    public static User toUser(@NonNull final GoogleSignInAccount signInAccount) {
        return new User(signInAccount.getIdToken(), signInAccount.getDisplayName(),
                signInAccount.getGivenName(), signInAccount.getFamilyName(),
                signInAccount.getEmail(), signInAccount.getPhotoUrl().toString());
    }

    public static ProjectsTable toProjectTableRecord(@NonNull final Project project) {
        final ProjectsTable projectRecord = new ProjectsTable();
        projectRecord.setId(project.getId());
        projectRecord.setRemoteId(project.getRemoteId());
        projectRecord.setName(project.getName());

        return projectRecord;
    }

    public static Project toProject(@NonNull final ProjectsTable projectsTable) {
        final List<UserStory> userStories = toUserStoryList(projectsTable.getUserStories());
        return new Project(projectsTable.getId(), projectsTable.getName(), userStories);
    }

    public static List<Project> toProjectList(@NonNull final List<ProjectsTable> projectsTable) {
        final List<Project> projects = new ArrayList<>(projectsTable.size());
        for (ProjectsTable projectCursor : projectsTable) {
            projects.add(toProject(projectCursor));
        }
        return projects;
    }

    public static UserStoriesTable toUserStoryTableRecord(final int projectId, @NonNull final UserStory userStory) {
        final UserStoriesTable userStoryRecord = new UserStoriesTable();
        userStoryRecord.setId(userStory.getId());
        userStoryRecord.setRemoteId(userStory.getRemoteId());
        userStoryRecord.setWho(userStory.getWho());
        userStoryRecord.setWhat(userStory.getWhat());
        userStoryRecord.setWhy(userStory.getWhy());
        if (projectId != 0) {
            final ForeignKeyContainer<ProjectsTable> foreignKey = new ForeignKeyContainer<>(ProjectsTable.class);
            foreignKey.put("id", projectId);

            userStoryRecord.setProject(foreignKey);
        }
        return userStoryRecord;
    }

    public static UserStory toUserStory(@NonNull final UserStoriesTable userStoriesTable) {
        return new UserStory.Builder(userStoriesTable.getId())
                .remoteId(userStoriesTable.getRemoteId())
                .who(userStoriesTable.getWho())
                .what(userStoriesTable.getWhat())
                .why(userStoriesTable.getWhy()).build();
    }

    public static List<UserStory> toUserStoryList(@NonNull final List<UserStoriesTable> userStoriesTable) {
        final List<UserStory> userStories = new ArrayList<>(userStoriesTable.size());
        for (UserStoriesTable userStoryCursor : userStoriesTable) {
            userStories.add(toUserStory(userStoryCursor));
        }
        return userStories;
    }

    public static SaveProjectModel toSaveProjectModel(final @NonNull String userId, @NonNull final Project project) {
        final SaveProjectModel body = new SaveProjectModel();
        body.setUserId(userId);
        body.setProjectName(project.getName());
        if (project.getRemoteId() != 0) {
            body.setProjectRemoteId(project.getRemoteId());
        }
        return body;
    }

    public static SaveUserStoryModel toSaveUserStoryModel(final int projectId, @NonNull final UserStory userStory) {
        final SaveUserStoryModel saveUserStoryModel = new SaveUserStoryModel();
        saveUserStoryModel.setProjectId(projectId);
        saveUserStoryModel.setUserStoryRemoteId(userStory.getRemoteId());
        saveUserStoryModel.setWho(userStory.getWho());
        saveUserStoryModel.setWhat(userStory.getWhat());
        saveUserStoryModel.setWhy(userStory.getWhy());

        if (userStory.getRemoteId() != 0) {
            saveUserStoryModel.setUserStoryRemoteId(userStory.getRemoteId());
        }
        return saveUserStoryModel;
    }
}
