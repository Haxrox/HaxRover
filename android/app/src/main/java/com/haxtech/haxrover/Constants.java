package com.haxtech.haxrover;

import java.util.UUID;

public class Constants {
    // used to identify adding bluetooth names
    public final static int REQUEST_ENABLE_BT = 3054;
    // used to request fine location permission
    public final static int REQUEST_FINE_LOCATION = 3055;
    // scan period in milliseconds
    public final static int SCAN_PERIOD = 5000;

//    public static String SERVICE_STRING = "12345678-1234-5678-1234-56789abcdef0";
//    public static final UUID SERVICE_UUID = UUID.fromString(SERVICE_STRING);
//    public static String CHARACTERISTIC_UUID = "12345678-1234-5678-1234-56789abcdef1";
//    public static String CONFIG_UUID = "12345678-1234-5678-1234-56789abcdef2";

    public static String SERVICE_STRING = "5b71d7d1-f766-47b2-96ba-27055173f50e";
    public static final UUID SERVICE_UUID = UUID.fromString("5d9c0da5-75be-4140-94b0-3473b5715c18"); // UUID.fromString("565c04aa-3baf-46b6-8167-faa588db850e"); - hmni
    public static String CHARACTERISTIC_UUID = "5ccb12aa-b402-4bd0-a67c-85cc70240a1a";
    public static String CONFIG_UUID = "05f1a622-beb2-4edb-b62f-f5ed0f2226d3";
}
