package com.haxtech.haxrover;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.haxtech.haxrover.central.CentralActivity;
import com.haxtech.haxrover.haxrover.RoverActivity;
import com.haxtech.haxrover.peripheral.PeripheralActivity;

public class MainActivity extends AppCompatActivity {

    private Button btnCentral, btnPeripheral, btnRover;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        initView();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void initView() {
        btnRover = (Button) findViewById(R.id.btnRover);
        btnCentral = (Button) findViewById(R.id.btnCentral);
        btnPeripheral = (Button) findViewById(R.id.btnPeripheral);

        btnRover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, RoverActivity.class));
            }
        });

        btnCentral.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, CentralActivity.class));
            }
        });

        btnPeripheral.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, PeripheralActivity.class));
            }
        });


    }
}