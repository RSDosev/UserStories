package com.radoslavdosev.userstories.di;

import com.radoslavdosev.userstories.base.domain.UseCaseRunner;
import com.radoslavdosev.userstories.data.Repository;
import com.radoslavdosev.userstories.data.local.LocalDataSource;
import com.radoslavdosev.userstories.data.remote.RemoteDataSource;
import com.radoslavdosev.userstories.login.domain.usecase.DoLogin;
import com.radoslavdosev.userstories.projects.domain.usecase.DeleteProject;
import com.radoslavdosev.userstories.projects.domain.usecase.GetProjects;
import com.radoslavdosev.userstories.projects.domain.usecase.SaveProject;
import com.radoslavdosev.userstories.userstories.domain.usecase.DeleteUserStory;
import com.radoslavdosev.userstories.userstories.domain.usecase.GetProjectUserStories;
import com.radoslavdosev.userstories.userstories.domain.usecase.SaveUserStory;

/**
 * Created by Rado on 15.9.2016 г..
 */
public final class DI {
    private DI(){
        // no instances allowed
    }

    public static UseCaseRunner provideUseCaseRunner(){
        return UseCaseRunner.getInstance();
    }

    public static Repository provideProjectsRepository(){
        return Repository.get(provideLocalDataSource(), provideRemoteDataSource());
    }

    public static LocalDataSource provideLocalDataSource(){
        return LocalDataSource.get();
    }

    public static RemoteDataSource provideRemoteDataSource(){
        return RemoteDataSource.get();
    }

    public static DoLogin provideDoLoginUseCase(){
        return new DoLogin(provideProjectsRepository());
    }

    public static GetProjects provideGetProjectsUseCase(){
        return new GetProjects(provideProjectsRepository());
    }

    public static SaveProject provideSaveProjectUseCase(){
        return new SaveProject(provideProjectsRepository());
    }

    public static DeleteProject provideDeleteProjectUseCase(){
        return new DeleteProject(provideProjectsRepository());
    }

    public static GetProjectUserStories provideGetProjectUserStoriesUseCase(){
        return new GetProjectUserStories(provideProjectsRepository());
    }

    public static SaveUserStory provideSaveUserStoryUseCase(){
        return new SaveUserStory(provideProjectsRepository());
    }

    public static DeleteUserStory provideDeleteUserStoryUseCase(){
        return new DeleteUserStory(provideProjectsRepository());
    }
}
