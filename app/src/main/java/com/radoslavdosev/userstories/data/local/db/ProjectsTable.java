package com.radoslavdosev.userstories.data.local.db;

import com.radoslavdosev.userstories.data.model.Project;
import com.radoslavdosev.userstories.data.model.UserStory;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.annotation.ModelContainer;
import com.raizlabs.android.dbflow.annotation.OneToMany;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.annotation.Unique;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.container.ForeignKeyContainer;

import java.util.List;

/**
 * Created by Rado on 16.8.2016 г..
 */

@Table(database = AppDatabase.class)
@ModelContainer
public class ProjectsTable extends BaseModel {
    @Column
    @Unique
    @PrimaryKey
    int id;

    @Column
    int remoteId;

    @Column
    String name;

    List<UserStoriesTable> userStories;

    @OneToMany(methods = {OneToMany.Method.ALL}, variableName = "userStories")
    public List<UserStoriesTable> getUserStories() {
        if (userStories == null || userStories.isEmpty()) {
            userStories = SQLite.select()
                    .from(UserStoriesTable.class)
                    .where(UserStoriesTable_Table.projectId.eq(id))
                    .queryList();
        }
        return userStories;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRemoteId() {
        return remoteId;
    }

    public void setRemoteId(int remoteId) {
        this.remoteId = remoteId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
