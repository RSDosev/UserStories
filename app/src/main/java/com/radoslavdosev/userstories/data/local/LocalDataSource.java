package com.radoslavdosev.userstories.data.local;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.radoslavdosev.userstories.data.DataSource;
import com.radoslavdosev.userstories.data.local.db.ProjectsTable;
import com.radoslavdosev.userstories.data.local.db.ProjectsTable_Table;
import com.radoslavdosev.userstories.data.local.db.UserStoriesTable;
import com.radoslavdosev.userstories.data.local.db.UserStoriesTable_Table;
import com.radoslavdosev.userstories.data.model.Project;
import com.radoslavdosev.userstories.data.model.User;
import com.radoslavdosev.userstories.data.model.UserStory;
import com.radoslavdosev.userstories.util.Mapper;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.sql.language.property.Property;

import java.util.List;

/**
 * Created by Rado on 8.8.2016 г..
 */
public class LocalDataSource implements DataSource {
    private static LocalDataSource sInstance;

    private LocalDataSource() {
    }

    public static LocalDataSource get() {
        if (sInstance == null) {
            sInstance = new LocalDataSource();
        }
        return sInstance;
    }


    @Override
    public void loginRegister(User user, LoadDataCallback<List<Project>> onProjectsLoaded) {
        // not implemented yet
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
        final List<ProjectsTable> projects = new Select(Property.ALL_PROPERTY)
                .from(ProjectsTable.class).queryList();

        if (projects.size() == 0) {
            callback.onDataNotAvailable();
        } else {
            callback.onDataLoaded(Mapper.toProjectList(projects));
        }
    }

    @Override
    public void getProject(final int projectId,@NonNull final LoadDataCallback<Project> callback) {
        final ProjectsTable project = new Select(Property.ALL_PROPERTY)
                .from(ProjectsTable.class).where(ProjectsTable_Table.id.eq(projectId)).querySingle();
        if (project == null) {
            callback.onDataNotAvailable();
        } else {
            callback.onDataLoaded(Mapper.toProject(project));
        }
    }

    @Override
    public void saveProject(@NonNull final Project project, @Nullable final SaveDataCallback<Project> callback) {
        final ProjectsTable projectRecord = Mapper.toProjectTableRecord(project);

        if (projectRecord.exists()) {
            projectRecord.update();
        } else {
            if (project.getUserStories().size() != 0) {
                for (UserStory userStory : project.getUserStories()) {
                    saveUserStory(project.getId(), userStory, null);
                }
            }
            projectRecord.insert();
        }
        if (callback != null) {
            callback.onDataSaved(project);
        }
    }

    @Override
    public void saveAndReplaceProjects(@NonNull final List<Project> projects, @Nullable final SaveDataCallback<List<Project>> callback) {
        for (Project project : projects) {
            saveProject(project, null);
        }
        if (callback != null) {
            callback.onDataSaved(projects);
        }
    }

    @Override
    public void deleteProject(@NonNull final Project project, @Nullable final DeleteDataCallback<Project> callback) {
        deleteAllUserStoriesPerProject(project.getId(), null);
        final ProjectsTable projectRecord = Mapper.toProjectTableRecord(project);
        projectRecord.delete();
        if (callback != null) {
            callback.onDataDeleted(project);
        }
    }

    @Override
    public void deleteAllProjects(@Nullable DeleteDataCallback callback) {
        SQLite.delete(ProjectsTable.class).execute();
        if (callback != null) {
            callback.onDataDeleted(null);
        }
    }

    @Override
    public void saveUserStory(final int projectId, @NonNull final UserStory userStory, @Nullable final SaveDataCallback<UserStory> callback) {
        final UserStoriesTable userStoryRecord = Mapper.toUserStoryTableRecord(projectId, userStory);
        if (userStoryRecord.exists()) {
            userStoryRecord.update();
        } else {
            userStoryRecord.insert();
        }
        if (callback != null) {
            callback.onDataSaved(userStory);
        }
    }

    @Override
    public void deleteUserStory(@NonNull final UserStory userStory, @Nullable final DeleteDataCallback<UserStory> callback) {
        final UserStoriesTable userStoryRecord = Mapper.toUserStoryTableRecord(0, userStory);
        userStoryRecord.delete();
        if (callback != null) {
            callback.onDataDeleted(userStory);
        }
    }

    @Override
    public void deleteAllUserStoriesPerProject(final int projectId, @Nullable final DeleteDataCallback callback) {
        SQLite.delete(UserStoriesTable.class).where(UserStoriesTable_Table.projectId.eq(projectId)).execute();
        if (callback != null) {
            callback.onDataDeleted(null);
        }
    }
}
