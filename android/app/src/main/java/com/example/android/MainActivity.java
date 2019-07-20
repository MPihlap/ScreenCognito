package com.example.android;

import androidx.appcompat.app.AppCompatActivity;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.utils.UrlBeaconUrlCompressor;

import java.util.Collection;

public class MainActivity extends AppCompatActivity implements BeaconConsumer, MonitorNotifier, RangeNotifier {

    protected static final String TAG = "BeaconsEverywhere";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        enableAdmin();
        //turnOffScreen();

        BeaconManager beaconManager = BeaconManager.getInstanceForApplication(this.getApplicationContext());
// Detect the main identifier (UID) frame:
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout(BeaconParser.EDDYSTONE_UID_LAYOUT));
// Detect the telemetry (TLM) frame:
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout(BeaconParser.EDDYSTONE_TLM_LAYOUT));
// Detect the URL frame:
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout(BeaconParser.EDDYSTONE_URL_LAYOUT));
    }

    private void enableAdmin() {
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, new ComponentName(this, DeviceAdminReceiverImpl.class));
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "You need to activate device admin to lock screen.");
        startActivity(intent);
    }

    private void turnOffScreen() {
        Intent serviceIntent = new Intent(this, ScreenService.class);
        ServiceConnection serviceConnection = createServiceConnection();
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        startService(serviceIntent);
    }

    private ServiceConnection createServiceConnection() {
        return new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder binder) {
                Log.d("test", "onServiceConnected");
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.d("test", "onServiceDisconnected");
            }
        };
    }

    private BeaconManager mBeaconManager;



    public void didEnterRegion(Region region) {
        Log.d(TAG, "I detected a beacon in the region with namespace id " + region.getId1() +
                " and instance id: " + region.getId2());
    }

    public void didExitRegion(Region region) {
    }

    public void didDetermineStateForRegion(int state, Region region) {
    }

    @Override
    public void onPause() {
        super.onPause();
        mBeaconManager.unbind(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        mBeaconManager = BeaconManager.getInstanceForApplication(this.getApplicationContext());
        // Detect the URL frame:
        mBeaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout(BeaconParser.EDDYSTONE_URL_LAYOUT));
        mBeaconManager.bind(this);
    }

    public void onBeaconServiceConnect() {
        Region region = new Region("all-beacons-region", null, null, null);
        try {
            mBeaconManager.startRangingBeaconsInRegion(region);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        mBeaconManager.setRangeNotifier(this);
    }

    @Override
    public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
        for (Beacon beacon: beacons) {
            if (beacon.getServiceUuid() == 0xfeaa && beacon.getBeaconTypeCode() == 0x10) {
                // This is a Eddystone-URL frame
                String url = UrlBeaconUrlCompressor.uncompress(beacon.getId1().toByteArray());
                Log.d(TAG, "I see a beacon transmitting a url: " + url +
                        " approximately " + beacon.getDistance() + " meters away.");
            }
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
