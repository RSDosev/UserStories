package com.radoslavdosev.userstories.login.domain.usecase;

import android.support.annotation.NonNull;

import com.radoslavdosev.userstories.base.domain.UseCase;
import com.radoslavdosev.userstories.data.DataSource;
import com.radoslavdosev.userstories.data.Repository;
import com.radoslavdosev.userstories.data.model.Project;
import com.radoslavdosev.userstories.data.model.User;

import java.util.Collections;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by Rado on 31.8.2016 г..
 */
public class DoLogin extends UseCase<DoLogin.RequestValues, DoLogin.ResponseValue> {

    private final Repository mProjectsRepository;

    public DoLogin(@NonNull final Repository projectsRepository) {
        mProjectsRepository = checkNotNull(projectsRepository, "projectsRepository cannot be null!");
    }

    @Override
    protected void executeUseCase(RequestValues requestValues) {
        final User user = requestValues.getUser();

        mProjectsRepository.loginRegister(user, new DataSource.LoadDataCallback<List<Project>>() {
            @Override
            public void onDataLoaded(final List<Project> data) {
                getUseCaseCallback().onSuccess(new ResponseValue());
            }

            @Override
            public void onDataNotAvailable() {
                getUseCaseCallback().onSuccess(new ResponseValue());
            }

            @Override
            public void onError(String message) {
                getUseCaseCallback().onError(message);
            }
        });
    }

    public static final class RequestValues implements UseCase.RequestValues {
        private User user;

        public RequestValues(@NonNull final User user) {
            this.user = user;
        }

        public User getUser() {
            return user;
        }
    }

    public static final class ResponseValue implements UseCase.ResponseValue {

    }
}
