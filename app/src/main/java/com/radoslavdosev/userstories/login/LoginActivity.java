package com.radoslavdosev.userstories.login;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.radoslavdosev.userstories.R;
import com.radoslavdosev.userstories.base.domain.UseCaseRunner;
import com.radoslavdosev.userstories.data.Repository;
import com.radoslavdosev.userstories.data.local.LocalDataSource;
import com.radoslavdosev.userstories.data.remote.RemoteDataSource;
import com.radoslavdosev.userstories.di.DI;
import com.radoslavdosev.userstories.login.domain.usecase.DoLogin;
import com.radoslavdosev.userstories.projects.ProjectsListActivity;
import com.radoslavdosev.userstories.util.Mapper;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Rado on 15.8.2016 г..
 */
public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, LoginMvpContract.View {
    private static final int RC_SIGN_IN = 55;

    private LoginPresenter mPresenter;
    private GoogleApiClient mGoogleApiClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        mPresenter = new LoginPresenter(DI.provideUseCaseRunner(), DI.provideDoLoginUseCase());
        initGoogleAPIClient();

    }

    private void initGoogleAPIClient() {
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        final GoogleSignInOptions signingOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken("531127470256-64594kv5e55tvg2la3carlrjko9df7ak.apps.googleusercontent.com")
                .build();

        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, signingOptions)
                .build();
    }

    @OnClick(R.id.buttonLogin)
    @Override
    public void showLoginDialog() {
        final Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @OnClick(R.id.buttonSkipLogin)
    protected void skipLogin() {
        mPresenter.skipLogin();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            handleSignInDialogData(data);
        }
    }

    private void handleSignInDialogData(Intent data) {
        final GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
        if (result.isSuccess()) {
            final GoogleSignInAccount signedInAccount = result.getSignInAccount();
            if (signedInAccount == null) {
                showLoginError(result.getStatus().getStatusMessage());
                return;
            }
            mPresenter.doLoginRegister(Mapper.toUser(signedInAccount));
        } else {
            showLoginError("Unsuccessful signing in!");
        }
    }

    @Override
    public void showUserLoggedInSuccessfully() {
        startActivity(ProjectsListActivity.getIntent(this, true));
    }

    @Override
    public void showContinueWithoutUser() {
        startActivity(ProjectsListActivity.getIntent(this, false));
    }

    @Override
    public void showLoginError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mPresenter.attachView(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mPresenter.detachView();
    }
}



