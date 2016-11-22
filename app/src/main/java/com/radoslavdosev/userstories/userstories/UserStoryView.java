package com.radoslavdosev.userstories.userstories;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.radoslavdosev.userstories.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Rado on 22.8.2016 г..
 */
public class UserStoryView extends CardView {
    @Bind(R.id.editTextWho)
    protected EditText who;
    @Bind(R.id.editTextWhat)
    protected EditText what;
    @Bind(R.id.editTextWhy)
    protected EditText why;
    @Bind(R.id.buttonDelete)
    protected ImageButton delete;
    @Bind(R.id.buttonEditSave)
    protected ImageButton editSave;

    private boolean isInEditMode;
    private String[] currentState;
    private OnUserStoryViewActionsListener mListener;

    public UserStoryView(Context context) {
        super(context);
        initView();
    }

    public UserStoryView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public UserStoryView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        inflate(getContext(), R.layout.card_user_story, this);
        ButterKnife.bind(this);
        this.isInEditMode = false;
        this.editSave.setBackgroundResource(android.R.drawable.ic_menu_edit);
        this.currentState = new String[3];
    }

    @OnClick(R.id.buttonDelete)
    protected void onDeleteButtonClick(final View deleteButton) {
        if (mListener != null) {
            mListener.onDeleteRequested();
        }
    }

    @OnClick(R.id.buttonEditSave)
    protected void onEditSaveButtonClick(final View buttonEditSave) {
        if (isInEditMode) {
            editSave.setBackgroundResource(android.R.drawable.ic_menu_edit);
            delete.setVisibility(View.GONE);
            who.setEnabled(false);
            what.setEnabled(false);
            why.setEnabled(false);
            isInEditMode = false;
            editCompleted();
        } else {
            editSave.setBackgroundResource(android.R.drawable.ic_menu_save);
            delete.setVisibility(View.VISIBLE);
            who.setEnabled(true);
            what.setEnabled(true);
            why.setEnabled(true);
            isInEditMode = true;
            saveCurrentState();
        }
    }

    private void editCompleted() {
        if (isViewChanged() && mListener != null) {
            mListener.onModified(who.getText().toString(), what.getText().toString(), why.getText().toString());
        }
    }

    private boolean isViewChanged() {
        if (!currentState[0].equals(who.getText().toString())
                || !currentState[1].equals(what.getText().toString())
                || !currentState[2].equals(why.getText().toString())) {
            return true;
        }
        return false;
    }

    private void saveCurrentState() {
        currentState[0] = who.getText().toString();
        currentState[1] = what.getText().toString();
        currentState[2] = why.getText().toString();
    }

    public void setWho(@NonNull final String who) {
        this.who.setText(who);
    }

    public void setWhat(@NonNull final String what) {
        this.what.setText(what);
    }

    public void setWhy(@NonNull final String why) {
        this.why.setText(why);
    }

    public void setActionsListener(OnUserStoryViewActionsListener mListener) {
        this.mListener = mListener;
    }

    public interface OnUserStoryViewActionsListener {
        void onModified(String who, String what, String why);

        void onDeleteRequested();
    }

}
