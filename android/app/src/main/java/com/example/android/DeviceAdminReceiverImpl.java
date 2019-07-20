package com.example.android;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class DeviceAdminReceiverImpl extends DeviceAdminReceiver {

    @Override
    public void onEnabled(@NonNull Context context, @NonNull Intent intent) {
        super.onEnabled(context, intent);
        String actionEnabled = "device_admin_action_enabled";
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(actionEnabled));
    }

    @Override
    public void onDisabled(@NonNull Context context, @NonNull Intent intent) {
        super.onDisabled(context, intent);
        String actionDisabled = "device_admin_action_disabled";
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(actionDisabled));
    }
}
