package com.radoslavdosev.userstories.userstories;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.radoslavdosev.userstories.R;
import com.radoslavdosev.userstories.data.model.UserStory;
import com.radoslavdosev.userstories.di.DI;
import com.radoslavdosev.userstories.userstories.UserStoriesMvpContract.Presenter;
import com.radoslavdosev.userstories.userstories.domain.usecase.DeleteUserStory;

import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jp.wasabeef.recyclerview.animators.LandingAnimator;

/**
 * An activity representing a list of UserStories. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link UserStoriesListActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class UserStoriesListActivity extends AppCompatActivity implements UserStoriesMvpContract.View {
    private final static String EXTRA_PROJECT_ID = "EXTRA_PROJECT_ID";
    private final static String EXTRA_REMOTE_PROJECT_ID = "EXTRA_PROJECT_ID";
    private static final String EXTRA_WITH_REMOTE_SYNC = "EXTRA_WITH_REMOTE_SYNC";

    @Bind(R.id.refresh_layout)
    protected SwipeRefreshLayout mSwipeRefreshLayout;
    @Bind(R.id.userstory_list)
    protected RecyclerView mRecyclerView;
    @Bind(R.id.layoutNoUserStories)
    protected View mNoProjectsLayout;
    @Bind(R.id.progressBarSyncing)
    protected ProgressBar mSyncIndicator;

    private Presenter<UserStoriesMvpContract.View> mPresenter;
    private UserStoriesAdapter mUserStoriesAdapter;

    public static Intent getIntent(final Context context, final int projectId, final int remoteProjectId, final boolean withSync) {
        final Intent intent = new Intent(context.getApplicationContext(), UserStoriesListActivity.class);
        intent.putExtra(EXTRA_PROJECT_ID, projectId);
        intent.putExtra(EXTRA_REMOTE_PROJECT_ID, remoteProjectId);
        intent.putExtra(EXTRA_WITH_REMOTE_SYNC, withSync);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userstory_list);
        ButterKnife.bind(this);

        mPresenter = new UserStoriesPresenter(this, DI.provideUseCaseRunner(),
                DI.provideGetProjectUserStoriesUseCase(),
                DI.provideSaveUserStoryUseCase(),
                DI.provideDeleteUserStoryUseCase(),
                extractWithRemoteSyncExtra(savedInstanceState),
                extractTheProjectId(savedInstanceState),
                extractTheRemoteProjectId(savedInstanceState));

        setupTheUI();
    }

    private int extractTheProjectId(@Nullable final Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            return savedInstanceState.getInt(EXTRA_PROJECT_ID, 0);
        } else {
            return getIntent().getIntExtra(EXTRA_PROJECT_ID, 0);
        }
    }

    private int extractTheRemoteProjectId(@Nullable final Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            return savedInstanceState.getInt(EXTRA_REMOTE_PROJECT_ID, 0);
        } else {
            return getIntent().getIntExtra(EXTRA_REMOTE_PROJECT_ID, 0);
        }
    }

    private boolean extractWithRemoteSyncExtra(@Nullable final Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            return savedInstanceState.getBoolean(EXTRA_WITH_REMOTE_SYNC, false);
        } else {
            return getIntent().getBooleanExtra(EXTRA_WITH_REMOTE_SYNC, false);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(EXTRA_PROJECT_ID, mPresenter.getProjectId());
        outState.putInt(EXTRA_PROJECT_ID, mPresenter.getRemoteProjectId());
        outState.putBoolean(EXTRA_WITH_REMOTE_SYNC, mPresenter.isWithRemoteSync());
        super.onSaveInstanceState(outState);
    }

    private void setupTheUI() {
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getTitle());
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mSwipeRefreshLayout = ButterKnife.findById(this, R.id.refresh_layout);
        mSwipeRefreshLayout.setColorSchemeColors(
                ContextCompat.getColor(this, R.color.colorPrimary),
                ContextCompat.getColor(this, R.color.colorAccent),
                ContextCompat.getColor(this, R.color.colorPrimaryDark)
        );
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mPresenter.loadUserStories(true);
            }
        });
        if (!mPresenter.isWithRemoteSync()) {
            mSwipeRefreshLayout.setEnabled(false);
        }

        mUserStoriesAdapter = new UserStoriesAdapter(Collections.EMPTY_LIST);
        mUserStoriesAdapter.setOnUserStoryItemListener(new UserStoriesAdapter.OnUserStoryItemListener() {
            @Override
            public void onUserStoryItemModified(UserStory userStory) {
                mPresenter.saveUserStory(userStory);
            }

            @Override
            public void onUserStoryItemDeleted(UserStory userStory) {
                mPresenter.deleteUserStory(userStory);
            }
        });
        mUserStoriesAdapter.initOnSwipeAndMoveCallbacks(mRecyclerView);
        mRecyclerView = ButterKnife.findById(this, R.id.userstory_list);
        mRecyclerView.setAdapter(mUserStoriesAdapter);
        mRecyclerView.setItemAnimator(new LandingAnimator());
    }

    @OnClick(R.id.fab)
    protected void addNewUserStory() {
        mPresenter.addNewUserStory();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return item.getItemId() == R.id.action_settings || super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mPresenter.attachView(this);
        mPresenter.loadUserStories(false);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mPresenter.scheduleSyncIfNeeded();
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
        mSyncIndicator.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideSyncing() {
        mSyncIndicator.setVisibility(View.GONE);
    }

    @Override
    public void showAllUserStories(@NonNull final List<UserStory> userStories) {
        mUserStoriesAdapter.refresh(userStories);
    }

    @Override
    public void addUserStoryToUI(@NonNull final UserStory userStory) {
        if (mUserStoriesAdapter.isEmpty()) {
            toggleNoUserStoriesView(false);
        }
        mUserStoriesAdapter.addItem(userStory);
    }

    @Override
    public void removeUserStoryFromUI(@NonNull final UserStory userStory) {
        mUserStoriesAdapter.removeItem(userStory);
        if (mUserStoriesAdapter.isEmpty()) {
            toggleNoUserStoriesView(true);
        }
    }

    @Override
    public void showNoUserStories() {
        toggleNoUserStoriesView(true);
    }

    @Override
    public void showUndoDeletedUserStory(@NonNull final UserStory userStory) {
        Snackbar.make(mRecyclerView, "The user story was deleted", Snackbar.LENGTH_LONG)
                .setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mPresenter.addUserStory(userStory);
                        mPresenter.saveUserStory(userStory);
                    }
                }).show();
    }

    @Override
    public void showNoNetwork() {
        Toast.makeText(this, "No network", Toast.LENGTH_LONG).show();
    }

    @Override
    public void showError(@NonNull final String message) {
        Snackbar.make(mRecyclerView, message, Snackbar.LENGTH_LONG).show();
    }

    private void toggleNoUserStoriesView(final boolean toShow) {
        if (toShow) {
            mNoProjectsLayout.setVisibility(View.VISIBLE);
        } else {
            mNoProjectsLayout.setVisibility(View.GONE);
        }
    }

}
