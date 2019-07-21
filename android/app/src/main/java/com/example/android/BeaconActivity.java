package com.example.android;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;

public class BeaconActivity extends AppCompatActivity implements BeaconConsumer, MonitorNotifier, RangeNotifier {

    protected static final String TAG = "BeaconsEverywhere";
    private static final String CHANNEL_ID = "12345";
    private static final String CHANNEL_NAME = "VEEBEL";
    private BeaconManager beaconManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createNotificationChannel();
        setContentView(R.layout.activity_beacon);
        beaconManager = BeaconManager.getInstanceForApplication(this.getApplicationContext());
        //beaconManager.setBackgroundBetweenScanPeriod(0);
        //beaconManager.setBackgroundScanPeriod(100);
        //beaconManager.setForegroundBetweenScanPeriod(0);
        //beaconManager.setForegroundScanPeriod(100);

        String iBeaconPattern = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24";
        beaconManager.getBeaconParsers().add(new BeaconParser()
                .setBeaconLayout(iBeaconPattern));

        beaconManager.bind(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.removeAllRangeNotifiers();
        beaconManager.removeAllMonitorNotifiers();
        beaconManager.unbind(this);
    }

    @Override
    public void onBeaconServiceConnect() {
        Region region = new Region("all-beacons-region", null, null, null);
        try {
            beaconManager.startRangingBeaconsInRegion(region);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        beaconManager.addRangeNotifier(this);
    }

    @Override
    public void didEnterRegion(Region region) {
        Log.d(TAG, "I detected a beacon in the region with namespace id " + region.getId1() +
                " and instance id: " + region.getId2());
    }

    @Override
    public void didExitRegion(Region region) {
        //
    }

    @Override
    public void didDetermineStateForRegion(int i, Region region) {
        //
    }

    @Override
    public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
        if (!beacons.isEmpty()) {
            for (Beacon beacon : beacons) {
                if (beacon.getBluetoothName().contains("VEEBEL") && beacon.getIdentifiers().size() >= 2) {
                    if ("1".equals(beacon.getIdentifier(2).toString())) {
                        Bitmap icon = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.ic_launcher_foreground);
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                                .setSmallIcon(R.drawable.ic_launcher_foreground)
                                .setContentTitle("Hoiatus!")
                                .setContentText("Oht läheduses, peagi lülitatakse ekraan välja!")
                                .setStyle(new NotificationCompat.BigPictureStyle().bigPicture(icon))
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
                        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
                        notificationManager.notify(1, builder.build());
                    } else if ("2".equals(beacon.getIdentifier(2).toString())) {
                        turnOffScreen();
                    }
                }
            }
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance);
            channel.setDescription("DESCRIPTION");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void turnOffScreen() {
        DevicePolicyManager systemService = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName devAdminReceiver = new ComponentName(getApplicationContext(), DeviceAdminReceiverImpl.class);
        if (systemService.isAdminActive(devAdminReceiver)) {
            systemService.lockNow();
        }
    }
}
