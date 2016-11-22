package com.radoslavdosev.userstories.projects;


import android.support.annotation.NonNull;

import com.radoslavdosev.userstories.base.mvp.MvpView;
import com.radoslavdosev.userstories.data.model.Project;

import java.util.List;

/**
 * Created by Rado on 22.7.2016 г..
 */
public final class ProjectsMvpContract {
    public interface View extends MvpView {

        void showLoading();

        void hideLoading();

        void showSyncing();

        void hideSyncing();

        void showAllProjects(List<Project> projects);

        void addProjectToUI(Project project);

        void removeProjectFromUI(Project project);

        void showNewEditProjectUI(Project project);

        void showProjectUserStories(Project project);

        void showNoProjects();

        void showUndoDeletedProject(Project project);

        void showNoNetwork();

        void showError(String message);

    }

    public interface Presenter<V extends MvpView> extends com.radoslavdosev.userstories.base.mvp.Presenter<V> {
            void loadProjects(boolean forceUpdate);

            void addNewProject();

            void editProject(Project project);

            void saveNewProject(String projectName);

            void saveEditedProject(Project project);

            void deleteProject(Project project);

            void loadProjectUserStories(Project project);

            void scheduleSyncIfNeeded();

            boolean isWithRemoteSync();
    }
}
