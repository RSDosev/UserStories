package com.radoslavdosev.userstories.projects;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.ViewUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.radoslavdosev.userstories.R;
import com.radoslavdosev.userstories.util.ViewUtil;

import java.io.Serializable;

/**
 * Created by Rado on 18.8.2016 г..
 */
public class NewEditProjectFragment extends DialogFragment implements TextWatcher, DialogInterface.OnClickListener {
    public static final String TAG = NewEditProjectFragment.class.getName();
    private static final String PROJECT_NAME_EXTRA = "PROJECT_NAME_EXTRA";
    private static final String LISTENER_EXTRA = "LISTENER_EXTRA";
    
    private @Nullable String mProjectName;
    private OnSubmitListener mListener;
    private EditText mNameEditText;
    private Button mPositiveButton;
    private boolean isNewProject;

    public static NewEditProjectFragment getInstance(@Nullable final String projectName, final OnSubmitListener listener) {
        final Bundle bundle = new Bundle();
        bundle.putString(PROJECT_NAME_EXTRA, projectName);
        bundle.putParcelable(LISTENER_EXTRA, listener);
        final NewEditProjectFragment newEditProjectFragment = new NewEditProjectFragment();
        newEditProjectFragment.setArguments(bundle);

        return newEditProjectFragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        extractExtras(savedInstanceState);
        setupUI();
        return createTheDialog();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(PROJECT_NAME_EXTRA, mProjectName);
        outState.putParcelable(LISTENER_EXTRA, mListener);
        super.onSaveInstanceState(outState);
    }

    private void extractExtras(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            this.mProjectName = savedInstanceState.getString(PROJECT_NAME_EXTRA, null);
            this.mListener = savedInstanceState.getParcelable(LISTENER_EXTRA);
        } else {
            this.mProjectName = getArguments().getString(PROJECT_NAME_EXTRA, null);
            this.mListener = getArguments().getParcelable(LISTENER_EXTRA);
        }
        if (mProjectName == null) {
            this.isNewProject = true;
        }
    }

    private void setupUI() {
        mNameEditText = new EditText(getActivity());
        mNameEditText.setId(android.R.id.inputExtractEditText);
        mNameEditText.addTextChangedListener(this);
        mNameEditText.setText(mProjectName);
    }

    @NonNull
    private AlertDialog createTheDialog() {
        int dialogTitleRes, dialogMessageRes, dialogPositiveButtonRes;
        if (isNewProject) {
            dialogTitleRes = R.string.fragment_title_new_project;
            dialogMessageRes = R.string.fragment_message_new_project;
            dialogPositiveButtonRes = R.string.fragment_pos_button_new_project;
        } else {
            dialogTitleRes = R.string.fragment_title_edit_project;
            dialogMessageRes = R.string.fragment_message_edit_project;
            dialogPositiveButtonRes = R.string.fragment_pos_button_edit_project;
        }
        final AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle(dialogTitleRes)
                .setMessage(dialogMessageRes)
                .setPositiveButton(dialogPositiveButtonRes, this)
                .setNegativeButton(R.string.fragment_dialog_negative_button, null)
                .setView(mNameEditText).create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface arg) {
                styleTheDialog(dialog);
            }
        });
        return dialog;
    }


    private void styleTheDialog(final AlertDialog dialog) {
        // style the buttons
        mPositiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        mPositiveButton.setEnabled(false);
//        mPositiveButton.setTextColor(getResources().getColor(android.R.color.darker_gray));
//        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(getResources().getColor(android.R.color.black));

        // style the edit text
        final FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(mNameEditText.getLayoutParams());
        final int leftAndRightMargin = ViewUtil.dpToPx(20);
        layoutParams.setMargins(leftAndRightMargin, 0, leftAndRightMargin, 0);
        mNameEditText.setLayoutParams(layoutParams);
    }

    @Override
    public void onClick(DialogInterface dialog, int position) {
        mListener.onSubmit(mNameEditText.getText().toString(), isNewProject);
        dialog.dismiss();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (mPositiveButton == null) return;
        if (s.length() <= 0) {
            mPositiveButton.setEnabled(false);
            mPositiveButton.setTextColor(getResources().getColor(android.R.color.darker_gray));
        } else {
            mPositiveButton.setEnabled(true);
            mPositiveButton.setTextColor(getResources().getColor(android.R.color.black));
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
    
    
    public void setOnSubmitListener(OnSubmitListener listener) {
        this.mListener = listener;
    }
    
    public static abstract class OnSubmitListener implements Parcelable{

        @Override
        public void writeToParcel(Parcel dest, int flags) {
        }

        @Override
        public int describeContents() {
            return 0;
        }

        abstract void onSubmit(String name, boolean isNewProject);
    }
}
