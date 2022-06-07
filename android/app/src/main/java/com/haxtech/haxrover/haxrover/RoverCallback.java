package com.haxtech.haxrover.haxrover;

public interface RoverCallback {

    void requestEnableBLE();

    void requestLocationPermission();

    void onStatusMsg(final String message);

    void onToast(final String message);
}
