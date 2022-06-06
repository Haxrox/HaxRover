package com.haxtech.haxrover.central;

import static com.haxtech.haxrover.Constants.REQUEST_ENABLE_BT;
import static com.haxtech.haxrover.Constants.REQUEST_FINE_LOCATION;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.haxtech.haxrover.R;

import java.util.Calendar;


public class CentralActivity extends AppCompatActivity {

    //// GUI variables
    // text view for status
    private TextView tvStatus;
    // button for start scan
    private Button btnScan;
    // button for stop connection
    private Button btnStop;
    // button for send data
    private Button btnSend;
    private EditText dataField;

    private Button btnRead;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_central);

        initView();
        initBle();
        CentralManager.getInstance(this).initBle();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // finish app if the BLE is not supported
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        CentralManager.getInstance(this).disconnectGattServer();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 500);
    }

    /**
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_FINE_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(CentralActivity.this,
                            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    private void initView() {
        //// get instances of gui objects
        // status textview
        tvStatus = findViewById(R.id.tv_status);
        // scan button
        btnScan = findViewById(R.id.btnScan);
        // stop button
        btnStop = findViewById(R.id.btnStop);
        // send button
        btnSend = findViewById(R.id.btnSend);

        btnRead = findViewById(R.id.btnRead);

        dataField = findViewById(R.id.dataField);

        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CentralManager.getInstance(CentralActivity.this).startScan();
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CentralManager.getInstance(CentralActivity.this).disconnectGattServer();
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar calendar = Calendar.getInstance();
                String todayTime = (calendar.get(Calendar.MONTH) + 1)
                        + calendar.get(Calendar.DAY_OF_MONTH)
                        + calendar.get(Calendar.HOUR_OF_DAY)
                        + ":" + calendar.get(Calendar.MINUTE)
                        + ":" + calendar.get(Calendar.SECOND);

                Editable data = dataField.getText();

                CentralManager.getInstance(CentralActivity.this).sendData(data.length() > 0 ? data.toString() : todayTime);
            }
        });

        btnRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CentralManager.getInstance(CentralActivity.this).readData();
            }
        });
    }

    private void initBle() {
        CentralManager.getInstance(this).setCallBack(centralCallback);
    }

    private void requestEnableBLE() {
        Intent ble_enable_intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(ble_enable_intent, REQUEST_ENABLE_BT);
    }

   private void requestLocationPermission() {
        ActivityCompat.requestPermissions(CentralActivity.this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION);
    }

    private void showStatusMsg(final String message) {
        Handler handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                String oldMsg = tvStatus.getText().toString();
                tvStatus.setText(oldMsg + "\n" + message);

                scrollToBottom();
            }
        };
        handler.sendEmptyMessage(1);
    }

    private void showToast(final String message) {
        Handler handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                Toast.makeText(CentralActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        };
        handler.sendEmptyMessage(1);
    }

    private void scrollToBottom() {
        final ScrollView scrollview = ((ScrollView) findViewById(R.id.scrollview));
        scrollview.post(new Runnable() {
            @Override public void run() {
                scrollview.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    CentralCallback centralCallback = new CentralCallback() {
        @Override
        public void requestEnableBLE() {
            CentralActivity.this.requestEnableBLE();
        }

        @Override
        public void requestLocationPermission() {
            CentralActivity.this.requestLocationPermission();
        }

        @Override
        public void onStatusMsg(String message) {
            showStatusMsg(message);
        }

        @Override
        public void onToast(String message) {
            showToast(message);
        }
    };
}