
package com.example.android;

import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class ScreenService extends Service {

    private IBinder binder = new LocalBinder();

    public ScreenService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        lockScreen();
        return binder;
    }

    public class LocalBinder extends Binder {
        ScreenService getService() {
            return ScreenService.this;
        }
    }

    public void lockScreen() {
        DevicePolicyManager systemService = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName devAdminReceiver = new ComponentName(getApplicationContext(), DeviceAdminReceiverImpl.class);
        if (systemService.isAdminActive(devAdminReceiver)) {
            systemService.lockNow();
        }
    }

}
