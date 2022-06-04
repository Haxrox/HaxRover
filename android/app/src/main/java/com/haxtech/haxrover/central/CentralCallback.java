package com.haxtech.haxrover.central;

public interface CentralCallback {

    void requestEnableBLE();

    void requestLocationPermission();

    void onStatusMsg(final String message);

    void onToast(final String message);
}
