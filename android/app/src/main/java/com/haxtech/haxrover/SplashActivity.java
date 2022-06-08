package com.haxtech.haxrover;

import static com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.Task;
import com.haxtech.haxrover.haxrover.RoverActivity;

import java.util.ArrayList;
import java.util.Arrays;

public class SplashActivity extends AppCompatActivity {

    private final String TAG = SplashActivity.class.getSimpleName();
    private static final int ENABLE_LOCATION_CODE = 1;
    private static final int REQUEST_PERMISSIONS_CODE = 100;
    private static final int ENABLE_BLE_CODE = 1001;

    private static String[] permissions = {
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_splash);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ArrayList<String> permissionsList = new ArrayList<>();
            permissionsList.addAll(Arrays.asList(permissions));
            permissionsList.add(Manifest.permission.BLUETOOTH_SCAN);
            permissionsList.add(Manifest.permission.BLUETOOTH_CONNECT);
            permissions = permissionsList.toArray(permissions);
        }

        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            boolean permissionsGranted = checkPermission();

            new Handler().postDelayed((Runnable) () -> {
                if (permissionsGranted) {
                    enableBluetooth();
                } else {
                    Log.i(TAG, "Requesting permissions");
                    requestPermissions(permissions, REQUEST_PERMISSIONS_CODE);
                }
            }, 1000);
        } else {
            Toast.makeText(this, "Your device does not support BLE. Exiting ...", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSIONS_CODE) {
            boolean granted = Arrays.stream(grantResults).allMatch(grantResult -> grantResult == PackageManager.PERMISSION_GRANTED);

            if (granted) {
                Toast.makeText(this, "Permissions Granted", Toast.LENGTH_LONG).show();
                enableBluetooth();
            } else {
                AlertDialog.Builder altDlgBuilder = new AlertDialog.Builder(this);
                altDlgBuilder.setMessage("Please grant: " + Arrays.stream(permissions).reduce("- ", (initial, permission) -> initial + "\n- " + permission) + " for HaxRover to work properly!");
                altDlgBuilder.setPositiveButton("Ok", (dialogInterface, i) -> requestPermissions(permissions, REQUEST_PERMISSIONS_CODE));
                altDlgBuilder.setNegativeButton("No", (dialogInterface, i) -> finish());
                AlertDialog altDlg = altDlgBuilder.create();
                altDlg.show();
            }
        } else {
            Log.e(TAG, "Request code: " + requestCode);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == ENABLE_BLE_CODE) {
                enableLocation();
            } else if (requestCode == ENABLE_LOCATION_CODE) {
                showNextScreen();
            } else {
                enableBluetooth();
            }
        } else {
            AlertDialog.Builder altDlgBuilder = new AlertDialog.Builder(this);
            altDlgBuilder.setMessage("Please turn on " + (requestCode == ENABLE_BLE_CODE ? "Bluetooth" : "Location") + " for HaxRover to work properly!");
            altDlgBuilder.setPositiveButton("Ok", (dialogInterface, i) -> {
                if (requestCode == ENABLE_BLE_CODE) {
                    enableBluetooth();
                } else if (requestCode == ENABLE_LOCATION_CODE) {
                    enableLocation();
                } else {
                    showNextScreen();
                }
            });
            altDlgBuilder.setNegativeButton("No", (dialogInterface, i) -> finish());
            AlertDialog altDlg = altDlgBuilder.create();
            altDlg.show();
        }
    }

    private BluetoothAdapter enableBluetooth() {
        final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null) {
            if (!bluetoothAdapter.isEnabled()) {
                startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), ENABLE_BLE_CODE);
            } else {
                enableLocation();
            }
        } else {
            Toast.makeText(this, "Your device does not support Bluetooth. Exiting ...", Toast.LENGTH_LONG).show();
            finish();
        }
        return bluetoothAdapter;
    }

    private void enableLocation() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(LocationRequest.create().setPriority(PRIORITY_HIGH_ACCURACY))
                .setNeedBle(true);
        Task<LocationSettingsResponse> result =
                LocationServices.getSettingsClient(this).checkLocationSettings(builder.build());
        result.addOnCompleteListener(task -> {
            try {
                task.getResult(ApiException.class);
                showNextScreen();
            } catch (ApiException exception) {
                switch (exception.getStatusCode()) {
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the
                        // user a dialog.
                        try {
                            // Cast to a resolvable exception.
                            ResolvableApiException resolvable = (ResolvableApiException) exception;
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            resolvable.startResolutionForResult(SplashActivity.this, ENABLE_LOCATION_CODE);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        } catch (ClassCastException e) {
                            // Ignore, should be an impossible error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:

                        break;
                }
            }
        });
    }

    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Arrays.stream(permissions).allMatch(permission -> checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
        }
        return false;
    }

    private void showNextScreen() {
        startActivity(new Intent(SplashActivity.this, RoverMainActivity.class));

//        startActivity(new Intent(SplashActivity.this, RoverActivity.class));
    }
}