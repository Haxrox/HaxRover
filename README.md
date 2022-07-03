# TODO:
- [ ] Rewrite Android application to use [Nordic Android BLE Library](https://github.com/NordicSemiconductor/Android-BLE-Library) with [Nordic Scanner Compay Library](https://github.com/NordicSemiconductor/Android-Scanner-Compat-Library) for BLE connections and scanning, respectively
- [ ] Redesign RPI BLE to use callbacks for `ReadValue` `WriteValue` `PropertiesChanged` methods
- [x] Figure out why descriptors cannot be added to services
- [x] Stream video from HaxRover to Android application (still a bit buggy - might migrate to network streaming instead, where ble will be used to establish network credentials)

# Resources
[Android BLE Exam](https://github.com/rhalwls/BleExam/tree/662fd3a2074e23e04c867f31ba1386b81fd18123/app/src/main/java/com/exam/ble)

[BlueZ](https://github.com/bluez/bluez)

[gpiozero](https://github.com/gpiozero/gpiozero)

[PiCamera](https://picamera.readthedocs.io/en/release-1.13/index.html)