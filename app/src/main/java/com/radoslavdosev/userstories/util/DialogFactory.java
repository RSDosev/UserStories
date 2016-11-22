package com.radoslavdosev.userstories.util;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;

import com.radoslavdosev.userstories.R;


public final class DialogFactory {

    private DialogFactory(){
        // no instances allowed
    }

    public static Dialog createSimpleOkErrorDialog(@NonNull final Context context,
                                                   @NonNull final String title,
                                                   @NonNull final String message) {

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setNeutralButton(R.string.dialog_action_ok, null);
        return alertDialog.create();
    }

    public static Dialog createSimpleOkErrorDialog(@NonNull final Context context,
                                                   @StringRes final int titleResource,
                                                   @StringRes final int messageResource) {

        return createSimpleOkErrorDialog(context,
                context.getString(titleResource),
                context.getString(messageResource));
    }

    public static Dialog createGenericErrorDialog(@NonNull final Context context,
                                                  @NonNull final String message) {

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.dialog_error_title))
                .setMessage(message)
                .setNeutralButton(R.string.dialog_action_ok, null);
        return alertDialog.create();
    }

    public static Dialog createGenericErrorDialog(@NonNull final Context context,
                                                  @StringRes final int messageResource) {
        return createGenericErrorDialog(context, context.getString(messageResource));
    }

    public static ProgressDialog createProgressDialog(@NonNull final Context context,
                                                      @NonNull final String message) {
        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(message);
        return progressDialog;
    }

    public static ProgressDialog createProgressDialog(@NonNull final Context context,
                                                      @StringRes final int messageResource) {
        return createProgressDialog(context, context.getString(messageResource));
    }

}
