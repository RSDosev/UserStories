package com.radoslavdosev.userstories.userstories;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.ViewGroup;

import com.radoslavdosev.userstories.data.model.Project;
import com.radoslavdosev.userstories.data.model.User;
import com.radoslavdosev.userstories.data.model.UserStory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rado on 22.7.2016 г..
 */
public class UserStoriesAdapter extends RecyclerView.Adapter<UserStoriesAdapter.ViewHolder> {

    private List<UserStory> mData;
    private OnUserStoryItemListener mOnItemListener;

    public UserStoriesAdapter(@NonNull final List<UserStory> mData) {
        this.mData = new ArrayList<>(mData);
    }

    public void refresh(@NonNull final List<UserStory> data) {
        this.mData = data;
        notifyDataSetChanged();
    }

    public void addItem(@NonNull final UserStory userStory) {
        mData.add(userStory);
        notifyItemInserted(getItemPosition(userStory));
    }

    public void removeItem(@NonNull final UserStory userStory) {
        mData.remove(userStory);
        notifyDataSetChanged();
    }

    public void initOnSwipeAndMoveCallbacks(@NonNull final RecyclerView recyclerView) {
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull final RecyclerView.ViewHolder viewHolder, final int swipeDir) {
                final UserStoriesAdapter.ViewHolder vh = (UserStoriesAdapter.ViewHolder)viewHolder;
                mOnItemListener.onUserStoryItemDeleted(getUserStoryById(vh.getUserStoryId()));
            }

        }).attachToRecyclerView(recyclerView);
    }

    private UserStory getUserStoryById(final int userStoryId) {
        for(UserStory userStory: mData) {
            if (userStory.getId() == userStoryId){
                return userStory;
            }
        }
        return null;
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        final UserStoryView userStoryView = new UserStoryView(parent.getContext());
        return new ViewHolder(userStoryView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int position) {
        final UserStory userStory = mData.get(position);

        viewHolder.setUserStoryId(userStory.getId());
        viewHolder.update(userStory.getWho(), userStory.getWhat(), userStory.getWhy());
        viewHolder.setActionsListener(new UserStoryView.OnUserStoryViewActionsListener() {
            @Override
            public void onModified(String who, String what, String why) {
                userStory.setWho(who);
                userStory.setWhat(what);
                userStory.setWhy(why);
                mOnItemListener.onUserStoryItemModified(userStory);
            }

            @Override
            public void onDeleteRequested() {
                mOnItemListener.onUserStoryItemDeleted(userStory);
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

    public int getItemPosition(final @NonNull UserStory userStory){
        return mData.indexOf(userStory);
    }

    public void setOnUserStoryItemListener(final OnUserStoryItemListener mOnItemListener) {
        this.mOnItemListener = mOnItemListener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final UserStoryView userStoryView;
        private int userStoryId;

        public ViewHolder(@NonNull final UserStoryView userStoryView) {
            super(userStoryView);
            this.userStoryView = userStoryView;
        }

        public void update(final String who, final String what, final String why) {
            userStoryView.setWho(who);
            userStoryView.setWhat(what);
            userStoryView.setWhy(why);
        }

        public void setActionsListener(@NonNull final UserStoryView.OnUserStoryViewActionsListener listener) {
            userStoryView.setActionsListener(listener);
        }

        public int getUserStoryId() {
            return userStoryId;
        }

        public void setUserStoryId(int userStoryId) {
            this.userStoryId = userStoryId;
        }
    }

    public interface OnUserStoryItemListener {
        void onUserStoryItemModified(UserStory userStory);

        void onUserStoryItemDeleted(UserStory userStory);
    }
}
