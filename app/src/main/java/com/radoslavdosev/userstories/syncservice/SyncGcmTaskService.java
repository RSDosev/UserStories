package com.radoslavdosev.userstories.syncservice;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.os.ResultReceiver;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.OneoffTask;
import com.google.android.gms.gcm.Task;
import com.google.android.gms.gcm.TaskParams;

/**
 * Created by Rado on 7.9.2016 г..
 */
public class SyncGcmTaskService extends GcmTaskService {
    public static final String TAG = "SYNC_PROJECTS_SERVICE";

    @Override
    public int onRunTask(TaskParams taskParams) {
        startTheSyncService();
        return GcmNetworkManager.RESULT_SUCCESS;
    }

    private void startTheSyncService() {
        final Intent intent = new Intent(this, SyncService.class);
        intent.putExtra(SyncService.EXTRA_RECEIVER, new SyncResultReceiver(new Handler(getMainLooper())));
        startService(intent);
    }

    private class SyncResultReceiver extends ResultReceiver {
        public SyncResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            // if something bad happened reschedule the sync task
            if (resultCode == SyncService.RESULT_ERROR) {
                schedule(SyncGcmTaskService.this);
            }
        }
    }

    /**
     * Schedule the service if Google Play Services are available. If they are missing,
     * the {@link #onInitializeTasks() onInitializeTasks} will be called when the are installed.
     */
    public static boolean schedule(@NonNull final Context context) {
        int result = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context);
        if (result == ConnectionResult.SUCCESS) {
            final Task syncTask = new OneoffTask.Builder()
                    .setTag(SyncGcmTaskService.TAG)
                    .setService(SyncGcmTaskService.class)
                    .setExecutionWindow(0, 60)
                    .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
                    .setRequiresCharging(false)
                    .setUpdateCurrent(true)
                    .build();

            GcmNetworkManager.getInstance(context).schedule(syncTask);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onInitializeTasks() {
        schedule(this);
        super.onInitializeTasks();
    }
}
