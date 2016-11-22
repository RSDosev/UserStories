package com.radoslavdosev.userstories.projects;

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
import com.radoslavdosev.userstories.base.domain.UseCaseRunner;
import com.radoslavdosev.userstories.data.Repository;
import com.radoslavdosev.userstories.data.local.LocalDataSource;
import com.radoslavdosev.userstories.data.model.Project;
import com.radoslavdosev.userstories.data.remote.RemoteDataSource;
import com.radoslavdosev.userstories.di.DI;
import com.radoslavdosev.userstories.projects.ProjectsMvpContract.Presenter;
import com.radoslavdosev.userstories.projects.domain.usecase.DeleteProject;
import com.radoslavdosev.userstories.projects.domain.usecase.GetProjects;
import com.radoslavdosev.userstories.projects.domain.usecase.SaveProject;
import com.radoslavdosev.userstories.userstories.UserStoriesListActivity;
import com.radoslavdosev.userstories.userstories.UserStoriesListFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jp.wasabeef.recyclerview.animators.LandingAnimator;

/**
 * An activity representing a list of Projects. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link UserStoriesListActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class ProjectsListActivity extends AppCompatActivity implements ProjectsMvpContract.View {
    private static final String EXTRA_WITH_REMOTE_SYNC = "EXTRA_WITH_REMOTE_SYNC";

    @Bind(R.id.refresh_layout)
    protected SwipeRefreshLayout mSwipeRefreshLayout;
    @Bind(R.id.project_list)
    protected RecyclerView mRecyclerView;
    @Bind(R.id.layoutNoProjects)
    protected View mNoProjectsLayout;
    @Bind(R.id.progressBarSyncing)
    protected ProgressBar mSyncIndicator;

    private Presenter<ProjectsMvpContract.View> mPresenter;
    private ProjectsAdapter projectsAdapter;
    private boolean mIsTwoPane;

    public static Intent getIntent(@NonNull final Context context, final boolean withRemoteSync) {
        final Intent intent = new Intent(context.getApplicationContext(), ProjectsListActivity.class);
        intent.putExtra(EXTRA_WITH_REMOTE_SYNC, withRemoteSync);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_list);
        ButterKnife.bind(this);

        mPresenter = new ProjectsPresenter(this, DI.provideUseCaseRunner(),
                DI.provideGetProjectsUseCase(), DI.provideSaveProjectUseCase(),
                DI.provideDeleteProjectUseCase(), extractWithRemoteSyncExtra(savedInstanceState));

        setupTheUI();

        if (findViewById(R.id.project_users_stories) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mIsTwoPane = true;
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
        outState.putBoolean(EXTRA_WITH_REMOTE_SYNC, mPresenter.isWithRemoteSync());
        super.onSaveInstanceState(outState);
    }

    private void setupTheUI() {
        final Toolbar toolbar = ButterKnife.findById(this, R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        mSwipeRefreshLayout.setColorSchemeColors(
                ContextCompat.getColor(this, R.color.colorPrimary),
                ContextCompat.getColor(this, R.color.colorAccent),
                ContextCompat.getColor(this, R.color.colorPrimaryDark)
        );
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mPresenter.loadProjects(true);
            }
        });
        if (!mPresenter.isWithRemoteSync()) {
            mSwipeRefreshLayout.setEnabled(false);
        }

        projectsAdapter = new ProjectsAdapter(Collections.EMPTY_LIST);
        projectsAdapter.setOnProjectClickListener(new ProjectsAdapter.OnProjectActionsListener() {
            @Override
            public void onProjectClick(final Project project) {
                mPresenter.loadProjectUserStories(project);
            }

            @Override
            public void onProjectSwipe(Project project) {
                mPresenter.deleteProject(project);
            }
        });
        projectsAdapter.setOnSwipeCallback(mRecyclerView);
        mRecyclerView = ButterKnife.findById(this, R.id.project_list);
        mRecyclerView.setAdapter(projectsAdapter);
        mRecyclerView.setItemAnimator(new LandingAnimator());
    }

    @Override
    public void showProjectUserStories(@NonNull final Project project) {
        final int projectId = project.getId();
        final int remoteProjectId = project.getId();

        if (mIsTwoPane) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.project_users_stories, UserStoriesListFragment.getNewInstance(projectId,
                            remoteProjectId, mPresenter.isWithRemoteSync()))
                    .commit();
        } else {
            startActivity(UserStoriesListActivity.getIntent(this, projectId, remoteProjectId,
                    mPresenter.isWithRemoteSync()));
        }
    }

    @OnClick(R.id.fab)
    protected void addNewProject() {
        mPresenter.addNewProject();
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
        mPresenter.loadProjects(false);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mPresenter.scheduleSyncIfNeeded();
        mPresenter.detachView();
    }

    @Override
    public void showLoading() {
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void hideLoading() {
        mSwipeRefreshLayout.setRefreshing(false);
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
    public void showAllProjects(final List<Project> projects) {
        projectsAdapter.refresh(projects);
    }

    @Override
    public void addProjectToUI(@NonNull final Project project) {
        if (projectsAdapter.isEmpty()) {
            toggleNoProjectsView(false);
        }
        projectsAdapter.update(project);
    }

    @Override
    public void removeProjectFromUI(@NonNull final Project project) {
        projectsAdapter.remove(project);
        if (projectsAdapter.isEmpty()) {
            toggleNoProjectsView(true);
        }
    }

    @Override
    public void showNewEditProjectUI(@Nullable final Project project) {
        final String projectName = project == null ? null : project.getName();

        NewEditProjectFragment.getInstance(projectName, new NewEditProjectFragment.OnSubmitListener() {
            @Override
            public void onSubmit(@NonNull String name, boolean isNewProject) {
                if (isNewProject) {
                    mPresenter.saveNewProject(name);
                } else {
                    project.setName(name);
                    mPresenter.saveEditedProject(project);
                }
            }
        }).show(getSupportFragmentManager(), NewEditProjectFragment.TAG);
    }

    @Override
    public void showNoProjects() {
        projectsAdapter.refresh(new ArrayList<Project>());
        toggleNoProjectsView(true);
    }

    @Override
    public void showUndoDeletedProject(@NonNull final Project project) {
        Snackbar.make(mRecyclerView, "Project \"" + project.getName() + "\" deleted", Snackbar.LENGTH_LONG)
                .setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mPresenter.saveEditedProject(project);
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

    private void toggleNoProjectsView(final boolean toShow) {
        if (toShow) {
            mNoProjectsLayout.setVisibility(View.VISIBLE);
        } else {
            mNoProjectsLayout.setVisibility(View.GONE);
        }
    }
}
