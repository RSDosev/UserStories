package com.radoslavdosev.userstories.login;


import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.radoslavdosev.userstories.base.mvp.MvpView;
import com.radoslavdosev.userstories.data.model.User;

/**
 * Created by Rado on 22.7.2016 г..
 */
public final class LoginMvpContract {
    public interface View extends MvpView {

        void showLoginDialog();

        void showUserLoggedInSuccessfully();

        void showLoginError(String message);

        void showContinueWithoutUser();
    }

    public interface Presenter<V extends MvpView> extends com.radoslavdosev.userstories.base.mvp.Presenter<V> {

        void doLoginRegister(User user);

        void skipLogin();

    }
}
