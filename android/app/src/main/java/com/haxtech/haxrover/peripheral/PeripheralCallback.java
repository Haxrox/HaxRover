package com.haxtech.haxrover.peripheral;

public interface PeripheralCallback {

    void requestEnableBLE();

    void onStatusMsg(final String message);

    void onToast(final String message);
}
