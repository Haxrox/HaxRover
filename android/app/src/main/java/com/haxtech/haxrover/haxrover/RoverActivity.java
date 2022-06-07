package com.haxtech.haxrover.haxrover;

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
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.haxtech.haxrover.R;
import com.haxtech.haxrover.RoverManager;

public class RoverActivity extends AppCompatActivity {

    // text view for logs
    private TextView logView;
    // button to scan
    private Button scanButton;
    // button for reading data
    private Button readButton;

    private ImageButton upButton;
    private ImageButton downButton;
    private ImageButton leftButton;
    private ImageButton rightButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rover);

        initBle();
        initView();
        RoverManager.getInstance(this).initBle();
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
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 500);
    }

    private void initView() {
        logView = findViewById(R.id.tv_status);
        scanButton = findViewById(R.id.scanButton);
        readButton = findViewById(R.id.readButton);
        upButton = findViewById(R.id.upButton);
        downButton = findViewById(R.id.downButton);
        leftButton = findViewById(R.id.leftButton);
        rightButton = findViewById(R.id.rightButton);

        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RoverManager.getInstance(RoverActivity.this).startScan();
            }
        });

        readButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RoverManager.getInstance(RoverActivity.this).readData();
            }
        });

        upButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    RoverManager.getInstance(RoverActivity.this).sendData("up");
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    RoverManager.getInstance(RoverActivity.this).sendData("stop");
                }
                return true;
            }
        });

        downButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    RoverManager.getInstance(RoverActivity.this).sendData("down");
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    RoverManager.getInstance(RoverActivity.this).sendData("stop");
                }
                return true;
            }
        });

        leftButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    RoverManager.getInstance(RoverActivity.this).sendData("left");
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    RoverManager.getInstance(RoverActivity.this).sendData("stop");
                }
                return true;
            }
        });

        rightButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    RoverManager.getInstance(RoverActivity.this).sendData("right");
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    RoverManager.getInstance(RoverActivity.this).sendData("stop");
                }
                return true;
            }
        });

//        upButton.setOnTouchListener(touchListener);
//        downButton.setOnTouchListener(touchListener);
//        leftButton.setOnTouchListener(touchListener);
//        rightButton.setOnTouchListener(touchListener);
    }

    private void initBle() {
        RoverManager.getInstance(this).setCallBack(roverCallback);
    }

    private void requestEnableBLE() {
        Intent ble_enable_intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(ble_enable_intent, REQUEST_ENABLE_BT);
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(RoverActivity.this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION);
    }

    private void showStatusMsg(final String message) {
        Handler handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                String oldMsg = logView.getText().toString();
                logView.setText(oldMsg + "\n" + message);

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
                Toast.makeText(RoverActivity.this, message, Toast.LENGTH_SHORT).show();
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

    RoverCallback roverCallback = new RoverCallback() {
        @Override
        public void requestEnableBLE() {
            RoverActivity.this.requestEnableBLE();
        }

        @Override
        public void requestLocationPermission() {
            RoverActivity.this.requestLocationPermission();
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
