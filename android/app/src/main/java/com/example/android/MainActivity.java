package com.example.android;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private static final String TAG = "MAIN_ACTIVITY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        enableAdmin();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPermissions();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "onRequestPermission result: " + requestCode);
    }

    private void enableAdmin() {
        Log.d(TAG, "admin permission asked");
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, new ComponentName(this, DeviceAdminReceiverImpl.class));
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "You need to activate device admin to lock screen.");
        startActivityForResult(intent, 2);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: " + requestCode + " and " + resultCode);
        if (requestCode == 2) {
            if (resultCode == 0) {
                Log.d(TAG, "Admin permissions cancelled. Asking again.");
                enableAdmin();
            } else if (resultCode == -1) {
                Log.d(TAG, "Admin permissions granted. Asking for location permissions.");
                checkPermissions();
            }
        }
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Location permissions granted.");
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("This app needs location access");
            builder.setMessage("Please grant location access so this app can detect beacons.");
            builder.setPositiveButton(android.R.string.ok, null);

            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                }
            });
            builder.show();
        } else {
            Log.d(TAG, "All permissions granted.");
            scanBeacons();
        }
    }

    private void scanBeacons() {
        Intent intent = new Intent(this, BeaconActivity.class);
        startActivity(intent);
    }
}
