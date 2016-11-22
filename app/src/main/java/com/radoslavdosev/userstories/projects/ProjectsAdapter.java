package com.radoslavdosev.userstories.projects;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.radoslavdosev.userstories.R;
import com.radoslavdosev.userstories.data.model.Project;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rado on 22.7.2016 г..
 */
public class ProjectsAdapter extends RecyclerView.Adapter<ProjectsAdapter.ViewHolder> {

    private List<Project> mData;
    private OnProjectActionsListener mOnActionsListener;

    public ProjectsAdapter(@NonNull final List<Project> mData) {
        this.mData = new ArrayList<>(mData);
    }

    public void refresh(@NonNull final List<Project> data) {
        this.mData = data;
        notifyDataSetChanged();
    }

    public void remove(@NonNull final Project project) {
        final int itemPosition = getItemPosition(project);
        mData.remove(itemPosition);
        notifyDataSetChanged();
    }

    public void update(@NonNull final Project project) {
        if (mData.contains(project)) {
            for (Project pr : mData) {
                if (pr.equals(project)) {
                    pr.update(project);
                    notifyItemChanged(getItemPosition(pr));
                }
            }
        } else {
            mData.add(project);
            notifyItemInserted(mData.size());
        }
    }

    public void setOnSwipeCallback(@NonNull final RecyclerView recyclerView) {
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull final RecyclerView.ViewHolder viewHolder, final int swipeDir) {
                final ProjectsAdapter.ViewHolder vh = (ProjectsAdapter.ViewHolder)viewHolder;
                mOnActionsListener.onProjectSwipe(getProjectById(vh.projectId));
            }

            @Override
            public int getBoundingBoxMargin() {
                return super.getBoundingBoxMargin();
            }
        }).attachToRecyclerView(recyclerView);
    }

    private Project getProjectById(final int projectId){
       for (Project project : mData) {
           if (project.getId() == projectId) {
               return project;
           }
       }
        return null;
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        final View cardView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_projects, parent, false);
        return new ViewHolder(cardView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int position) {
        final Project project = mData.get(position);

        viewHolder.projectId = project.getId();
        viewHolder.projectName.setText(project.getName());
        viewHolder.rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOnActionsListener.onProjectClick(project);
            }
        });
    }

    public boolean isEmpty() {
        return mData.isEmpty();
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public int getItemPosition(final @NonNull Project project){
        return mData.indexOf(project);
    }

    public void setOnProjectClickListener(final OnProjectActionsListener onActionsListener) {
        this.mOnActionsListener = onActionsListener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final View rootView;
        public final TextView projectName;
        public int projectId;

        public ViewHolder(final View cardView) {
            super(cardView);
            rootView = cardView;
            projectName = (TextView) cardView.findViewById(R.id.textViewProjectName);
        }

    }

    public interface OnProjectActionsListener {
        void onProjectClick(Project project);
        void onProjectSwipe(Project project);
    }
}
