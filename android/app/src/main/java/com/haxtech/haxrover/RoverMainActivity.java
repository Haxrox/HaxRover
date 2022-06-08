package com.haxtech.haxrover;

import static com.haxtech.haxrover.Constants.CHARACTERISTIC_UUID;
import static com.haxtech.haxrover.Constants.SERVICE_STRING;
import static com.haxtech.haxrover.Constants.SERVICE_UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.haxtech.haxrover.haxrover.BluetoothUtils;
import com.haxtech.haxrover.haxrover.RoverActivity;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RoverMainActivity extends AppCompatActivity {

    private final String TAG = RoverMainActivity.class.getSimpleName();

    private TextView statusField;
    private ProgressBar progressSpinner;
    private Button haxRoverButton;
    // text view for logs
    private TextView logView;
    // button for reading data
    private Button readButton;

    private ImageButton upButton;
    private ImageButton downButton;
    private ImageButton leftButton;
    private ImageButton rightButton;
    private ScrollView scrollview;

    // BLE
    private BluetoothAdapter mBleAdapter;
    private BluetoothGatt mBleGatt;
    private BluetoothLeScanner mBleScanner;
    private List<ScanFilter> mScanFilters;
    private ScanSettings mScanSettings;
    private BluetoothGattCharacteristic mGattCharacteristic;
    private boolean isScanning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initScannerView();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    /**
     * Initializes the BLE scanner
     */
    private void initBleScanner() {
        mBleAdapter = BluetoothAdapter.getDefaultAdapter();
        mBleScanner = mBleAdapter.getBluetoothLeScanner();

        //// set scan filters
        // create scan filter list
        mScanFilters = new ArrayList<>();
        // create a scan filter with device uuid
        mScanFilters.add(new ScanFilter.Builder().setServiceUuid(new ParcelUuid(SERVICE_UUID)).build());

        mScanSettings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                .build();
    }

    /**
     * Initializes the activity view and starts LE scanning
     */
    private void initScannerView() {
        initBleScanner();

        setContentView(R.layout.activity_rover_main);
        statusField = findViewById(R.id.statusView);
        progressSpinner = findViewById(R.id.progressSpinner);
        haxRoverButton = findViewById(R.id.haxRoverButton);

        haxRoverButton.setOnClickListener(view -> {
            stopScan();
            startActivity(new Intent(RoverMainActivity.this, RoverActivity.class));
        });

        startScan();
    }

    /**
     * Initialize control view
     */

    private void initControlView() {
        setContentView(R.layout.activity_rover_control);

        scrollview = ((ScrollView) findViewById(R.id.scrollview));
        logView = findViewById(R.id.logField);
        readButton = findViewById(R.id.readButton);
        upButton = findViewById(R.id.upButton);
        downButton = findViewById(R.id.downButton);
        leftButton = findViewById(R.id.leftButton);
        rightButton = findViewById(R.id.rightButton);

        readButton.setOnClickListener(view -> readData());

        upButton.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN) {
                sendData("up");
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                sendData("stop");
            }
            return true;
        });

        downButton.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN) {
                sendData("down");
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                sendData("stop");
            }
            return true;
        });

        leftButton.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN) {
                sendData("left");
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                sendData("stop");
            }
            return true;
        });

        rightButton.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN) {
                sendData("right");
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                sendData("stop");
            }
            return true;
        });

//        upButton.setOnTouchListener(touchListener);
//        downButton.setOnTouchListener(touchListener);
//        leftButton.setOnTouchListener(touchListener);
//        rightButton.setOnTouchListener(touchListener);
    }

    private void connectError(final String message) {
        stopScan();
        Log.e(TAG, "Connect Error: " + message);
        setStatus(message + " Rescanning ...");
        new Handler(Looper.getMainLooper()).postDelayed(this::startScan, 1000);
    }
    /**
     * Sets the status message on the rover main view
     * @param message  message to set
     */
    private void setStatus(final String message) {
        Log.i(TAG, "SetStatus: " + message);
        Handler handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                statusField.setText(message);
            }
        };
        handler.sendEmptyMessage(1);
    }

    /**
     * Writes a message to the logView
     * @param message message to write
     */
    private void writeLog(final String message) {
        Handler handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                String oldMsg = logView.getText().toString();
                logView.setText(oldMsg + "\n" + message);

                scrollview.post(() -> scrollview.fullScroll(ScrollView.FOCUS_DOWN));
            }
        };
        handler.sendEmptyMessage(1);
    }

    /**
     * Starts LE Scanning
     */
    private void startScan() {
        if (mBleGatt != null) {
            mBleGatt.disconnect();
            mBleGatt.close();

            try {
                // BluetoothGatt gatt
                statusField.setText("Clearing device cache");
                Log.i(TAG, "Clearing device cache");
                final Method refresh = mBleGatt.getClass().getMethod("refresh");
                if (refresh != null) {
                    refresh.invoke(mBleGatt);
                }
            } catch (Exception e) {
                // Log it
            }
        }

        stopScan();

        isScanning = true;
        statusField.setText("Scanning for rover ...");
        mBleScanner.startScan(mScanFilters, mScanSettings, leScanCallback);
    }

    public void stopScan() {
        if (isScanning) {
            isScanning = false;
            mBleScanner.stopScan(leScanCallback);
        }
    }

    /**
     * Reads data from given characteristic
     */
    private void readData() {
        if (mBleGatt != null) {
            // disconnect if the characteristic is not found
            if (mGattCharacteristic == null) {
                writeLog("Cannot find characteristic. Disconnecting + rescanning");
                new Handler(Looper.getMainLooper()).postDelayed(this::initScannerView, 1000);
                return;
            }

            // read the characteristic
            boolean success = mBleGatt.readCharacteristic(mGattCharacteristic);
            // check the result
            if (success) {
                writeLog("Read Data");
                Log.e(TAG, "Success to read command");
            } else {
                Log.e(TAG, "Failed to read command : " + mGattCharacteristic.getUuid());
                writeLog("Failed to read command : " + mGattCharacteristic.getUuid());
                new Handler(Looper.getMainLooper()).postDelayed(this::initScannerView, 1000);
            }
        }
    }

    /**
     * Sends data to the current GATT Server
     * @param message  data to send
     */
    private void sendData(String message) {
        if (mBleGatt != null) {
            // disconnect if the characteristic is not found
            if (mGattCharacteristic == null) {
                writeLog("Cannot find characteristic. Disconnecting + rescanning");
                new Handler(Looper.getMainLooper()).postDelayed(this::initScannerView, 1000);
                return;
            }
            mGattCharacteristic.setValue(message.getBytes()); // 20byte limit
            mGattCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            // write the characteristic
            boolean success = mBleGatt.writeCharacteristic(mGattCharacteristic);
            // check the result
            if (success) {
                writeLog("Write : " + message);
                Log.e(TAG, "Success to write command");
            } else {
                Log.e(TAG, "Failed to write command : " + mGattCharacteristic.getUuid());
                writeLog("Failed to write command : " + mGattCharacteristic.getUuid());
                new Handler(Looper.getMainLooper()).postDelayed(this::initScannerView, 1000);
            }
        }
    }

    /**
     * Callback for LE Scan
     */
    ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            stopScan();

            setStatus("Rover found. Connecting ...");
            mBleGatt = result.getDevice().connectGatt(RoverMainActivity.this, false, bluetoothGattCallback, BluetoothDevice.TRANSPORT_LE);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            connectError("Scan Failed: " + errorCode + ".");
        }
    };

    /**
     * Callback for GATT Connections
     */
    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i(TAG, "Connection state change: " + status + " | " + newState);
            if (status == BluetoothGatt.GATT_FAILURE) {
                connectError("Gatt Connection Failed. ");
            } else if (status != BluetoothGatt.GATT_SUCCESS) {
                connectError("Gatt Connection Failed. ");
            } else {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    setStatus("Connected! Discovering Services...");
                    gatt.discoverServices();
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    // disconnected from the GATT Server
                    connectError("Connection Failed. ");
                }
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            // check if the discovery failed
            if (status != BluetoothGatt.GATT_SUCCESS) {
                connectError("Service discovery failed. Status: " + status);
                Log.e(TAG, "Device service discovery failed, status: " + status);
                return;
            }

            Log.e(TAG, "getDevice().getAddress() : " + gatt.getDevice().getAddress() + ", " + gatt.getDevice().getName());

            BluetoothGattService bluetoothGattService = gatt.getService(UUID.fromString(SERVICE_STRING));

            if (bluetoothGattService == null) {
                connectError("Failed to find GATT Service.");
                return;
            }

            BluetoothGattCharacteristic characteristic = bluetoothGattService.getCharacteristic(UUID.fromString(CHARACTERISTIC_UUID));

            if (characteristic == null) {
                connectError("Failed to find GattCharacteristic.");
                return;
            }

            // log for successful discovery
            Log.d(TAG, "Services discovery is successful. Setting notification...");

            gatt.setCharacteristicNotification(characteristic, true);
            characteristic.getDescriptors().forEach(descriptor -> {
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                boolean success = gatt.writeDescriptor(descriptor);
                if (success) {
                    setStatus("Services discovered!");
                    Log.i(TAG, "writeCharacteristic success");

                    progressSpinner.setIndeterminate(false);
                    progressSpinner.setProgress(100);

                    mGattCharacteristic = characteristic;
                    // successfully connected to the GATT Server
                    new Handler(Looper.getMainLooper()).postDelayed(() -> initControlView(), 2500);
                } else {
                    Log.i(TAG, "writeCharacteristic fail");
                }
            });
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);

            Log.d(TAG, "Characteristic changed: " + characteristic.getUuid().toString());
            readCharacteristic(characteristic);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Characteristic written successfully");
            } else {
                writeLog("Characteristic write unsuccessful, status: " + status + " Disconnecting + rescanning");
                new Handler(Looper.getMainLooper()).postDelayed(() -> initScannerView(), 1000);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Characteristic read successfully");
                readCharacteristic(characteristic);
            } else {
                writeLog("Characteristic read unsuccessful, status: " + status);
            }
        }

        /**
         * Log the value of the characteristic
         * @param characteristic
         */
        private void readCharacteristic(BluetoothGattCharacteristic characteristic) {
            byte[] msg = characteristic.getValue();
            String message = new String(msg);
            Log.d(TAG, "Read: " + message);
            writeLog("Read: " + message);
        }
    };
}