package com.radoslavdosev.userstories.login;

import android.content.Context;
import android.support.annotation.NonNull;

import com.radoslavdosev.userstories.base.domain.UseCase;
import com.radoslavdosev.userstories.base.domain.UseCaseRunner;
import com.radoslavdosev.userstories.base.mvp.BasePresenter;
import com.radoslavdosev.userstories.data.model.User;
import com.radoslavdosev.userstories.login.domain.usecase.DoLogin;
import com.radoslavdosev.userstories.projects.domain.usecase.DeleteProject;
import com.radoslavdosev.userstories.projects.domain.usecase.GetProjects;
import com.radoslavdosev.userstories.projects.domain.usecase.SaveProject;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by Rado on 15.8.2016 г..
 */
public class LoginPresenter extends BasePresenter<LoginMvpContract.View> implements LoginMvpContract.Presenter<LoginMvpContract.View> {

    private final DoLogin mDoLogin;
    private final UseCaseRunner mUseCaseRunner;

    public LoginPresenter(@NonNull UseCaseRunner useCaseRunner,
                             @NonNull DoLogin doLogin) {
        this.mUseCaseRunner = checkNotNull(useCaseRunner, "useCaseRunner cannot be null!");
        this.mDoLogin = checkNotNull(doLogin, "doLogin cannot be null!");
    }


    @Override
    public void doLoginRegister(User user) {
        mUseCaseRunner.execute(mDoLogin, new DoLogin.RequestValues(user), new UseCase.UseCaseCallback<DoLogin.ResponseValue>() {
            @Override
            public void onSuccess(DoLogin.ResponseValue response) {
               getMvpView().showUserLoggedInSuccessfully();
            }

            @Override
            public void onError(String message) {
                getMvpView().showLoginError(message);
            }
        });
    }

    @Override
    public void skipLogin() {
        getMvpView().showContinueWithoutUser();
    }

}
