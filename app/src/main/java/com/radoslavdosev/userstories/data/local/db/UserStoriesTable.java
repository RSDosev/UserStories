package com.radoslavdosev.userstories.data.local.db;

import com.radoslavdosev.userstories.data.model.Project;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.annotation.Unique;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.container.ForeignKeyContainer;

/**
 * Created by Rado on 16.8.2016 г..
 */
@Table(database = AppDatabase.class)
public class UserStoriesTable extends BaseModel {

    @Column
    @Unique
    @PrimaryKey
    int id;

    @Column
    int remoteId;

    @Column
    String who;

    @Column
    String what;

    @Column
    String why;

    @ForeignKey(references =
            {@ForeignKeyReference(columnName = "projectId",
                                columnType = Integer.class,
                                foreignKeyColumnName = "id")},
            saveForeignKeyModel = false)
    ForeignKeyContainer<ProjectsTable> project;


    public void associateProject(ProjectsTable project) {
        this.project = FlowManager.getContainerAdapter(ProjectsTable.class)
                .toForeignKeyContainer(project);
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public int getRemoteId() {
        return remoteId;
    }

    public void setRemoteId(int remoteId) {
        this.remoteId = remoteId;
    }

    public String getWho() {
        return who;
    }

    public void setWho(String who) {
        this.who = who;
    }

    public String getWhat() {
        return what;
    }

    public void setWhat(String what) {
        this.what = what;
    }

    public String getWhy() {
        return why;
    }

    public void setWhy(String why) {
        this.why = why;
    }


    public ForeignKeyContainer<ProjectsTable> getProject() {
        return project;
    }

    public void setProject(ForeignKeyContainer<ProjectsTable> project) {
        this.project = project;
    }

}
