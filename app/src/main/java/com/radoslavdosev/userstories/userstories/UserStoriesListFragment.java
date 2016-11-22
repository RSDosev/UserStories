package com.radoslavdosev.userstories.userstories;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.radoslavdosev.userstories.R;
import com.radoslavdosev.userstories.base.domain.UseCaseRunner;
import com.radoslavdosev.userstories.data.Repository;
import com.radoslavdosev.userstories.data.local.LocalDataSource;
import com.radoslavdosev.userstories.data.model.UserStory;
import com.radoslavdosev.userstories.data.remote.RemoteDataSource;
import com.radoslavdosev.userstories.di.DI;
import com.radoslavdosev.userstories.userstories.domain.usecase.DeleteUserStory;
import com.radoslavdosev.userstories.userstories.domain.usecase.GetProjectUserStories;
import com.radoslavdosev.userstories.userstories.domain.usecase.SaveUserStory;

import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Rado on 5.8.2016 г..
 */
// TODO: to finish
public class UserStoriesListFragment extends Fragment implements UserStoriesMvpContract.View {
    private final static String EXTRA_PROJECT_ID = "EXTRA_PROJECT_ID";
    private final static String EXTRA_REMOTE_PROJECT_ID = "EXTRA_PROJECT_ID";
    private static final String EXTRA_WITH_REMOTE_SYNC = "EXTRA_WITH_REMOTE_SYNC";

    private UserStoriesMvpContract.Presenter<UserStoriesMvpContract.View> mPresenter;

    @Bind(R.id.userstory_list)
    protected RecyclerView mRecyclerView;

    public static Fragment getNewInstance(final int projectId, final int remoteProjectId, final boolean withSync) {
        final Bundle arguments = new Bundle();
        arguments.putInt(EXTRA_PROJECT_ID, projectId);
        arguments.putInt(EXTRA_REMOTE_PROJECT_ID, remoteProjectId);
        arguments.putBoolean(EXTRA_WITH_REMOTE_SYNC, withSync);
        final UserStoriesListFragment instance = new UserStoriesListFragment();
        instance.setArguments(arguments);
        return instance;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View contentView = inflater.inflate(R.layout.list_userstories, container, false);
        setupTheUI(contentView);
        return contentView;
    }

    private void setupTheUI(final View contentView) {
        final UserStoriesAdapter adapter = new UserStoriesAdapter(Collections.EMPTY_LIST);
        adapter.setOnUserStoryItemListener(new UserStoriesAdapter.OnUserStoryItemListener() {
            @Override
            public void onUserStoryItemModified(UserStory userStory) {

            }

            @Override
            public void onUserStoryItemDeleted(UserStory userStory) {

            }
        });
        mRecyclerView = ButterKnife.findById(contentView, R.id.userstory_list);
        mRecyclerView.setAdapter(adapter);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        extractTheProjectId(savedInstanceState);

        mPresenter = new UserStoriesPresenter(getContext(), DI.provideUseCaseRunner(),
                DI.provideGetProjectUserStoriesUseCase(),
                DI.provideSaveUserStoryUseCase(),
                DI.provideDeleteUserStoryUseCase(),
                extractWithRemoteSyncExtra(savedInstanceState),
                extractTheProjectId(savedInstanceState),
                extractTheRemoteProjectId(savedInstanceState));
    }

    private int extractTheProjectId(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            return savedInstanceState.getInt(EXTRA_PROJECT_ID, 0);
        } else {
            return getArguments().getInt(EXTRA_PROJECT_ID, 0);
        }
    }

    private int extractTheRemoteProjectId(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            return savedInstanceState.getInt(EXTRA_REMOTE_PROJECT_ID, 0);
        } else {
            return getArguments().getInt(EXTRA_REMOTE_PROJECT_ID, 0);
        }
    }

    private boolean extractWithRemoteSyncExtra(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            return savedInstanceState.getBoolean(EXTRA_WITH_REMOTE_SYNC, false);
        } else {
            return getArguments().getBoolean(EXTRA_WITH_REMOTE_SYNC, false);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mPresenter.attachView(this);
        mPresenter.loadUserStories(false);
    }

    @Override
    public void onStop() {
        super.onStop();
        mPresenter.detachView();
    }

    @Override
    public void showLoading() {

    }

    @Override
    public void hideLoading() {

    }

    @Override
    public void showSyncing() {

    }

    @Override
    public void hideSyncing() {

    }

    @Override
    public void showAllUserStories(List<UserStory> userStories) {
        ((UserStoriesAdapter) mRecyclerView.getAdapter()).refresh(userStories);
    }

    @Override
    public void addUserStoryToUI(UserStory userStory) {

    }

    @Override
    public void removeUserStoryFromUI(UserStory userStory) {

    }

    @Override
    public void showNoUserStories() {

    }

    @Override
    public void showUndoDeletedUserStory(UserStory userStory) {

    }

    @Override
    public void showNoNetwork() {

    }

    @Override
    public void showError(String message) {

    }
}
