package com.haxtech.haxrover.peripheral;

import static com.haxtech.haxrover.Constants.REQUEST_ENABLE_BT;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.haxtech.haxrover.R;

import java.util.Calendar;

public class PeripheralActivity extends AppCompatActivity {

    private final String TAG = PeripheralActivity.class.getSimpleName();

    private TextView tvStatus;
    private Button btnSend, btnClose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_peripheral);

        initView();
        initServer();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ENABLE_BT)
            initServer();
    }

    @Override
    public void onBackPressed() {
        showStatusMsg("Close Server");
        PeripheralManager.getInstance(PeripheralActivity.this).close();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 500);
    }

    private void initView() {
        tvStatus = (TextView) findViewById(R.id.tv_status);
        btnSend = (Button) findViewById(R.id.btnSend);
        btnClose = (Button) findViewById(R.id.btnClose);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Calendar calendar = Calendar.getInstance();
                String todayTime = (calendar.get(Calendar.MONTH) + 1)
                        + "/" + calendar.get(Calendar.DAY_OF_MONTH)
                        + " " + calendar.get(Calendar.HOUR_OF_DAY)
                        + ":" + calendar.get(Calendar.MINUTE)
                        + ":" + calendar.get(Calendar.SECOND);

                PeripheralManager.getInstance(PeripheralActivity.this).sendData(todayTime);
            }
        });

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PeripheralManager.getInstance(PeripheralActivity.this).close();
            }
        });
    }

    private void initServer() {
        PeripheralManager.getInstance(PeripheralActivity.this).setCallBack(peripheralCallback);
        PeripheralManager.getInstance(PeripheralActivity.this).initServer();
    }

    private void requestEnableBLE() {
        Intent ble_enable_intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(ble_enable_intent, REQUEST_ENABLE_BT);
    }

    /**
     * @param message
     */
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
                Toast.makeText(PeripheralActivity.this, message, Toast.LENGTH_SHORT).show();
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

    /**
     * Peripheral Callback
     */
    PeripheralCallback peripheralCallback = new PeripheralCallback() {
        @Override
        public void requestEnableBLE() {
            PeripheralActivity.this.requestEnableBLE();
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