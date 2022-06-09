import signal
import sys
from functools import partial
from threading import Thread

import dbus
from dbus.mainloop.glib import DBusGMainLoop
from gi.repository import GLib

from ble.Utils import *
from ble.Constants import *
from ble.Exceptions import *

from RoverAdvertisement import RoverAdvertisement
from RoverApplication import RoverApplication

from Rover import Rover
from Camera import Camera

mainloop = None
application = None

def sigint_handler(sig, frame):
    if (sig == signal.SIGINT):
        print("")
        if (mainloop.is_running()):
            mainloop.quit()
        else:
            sys.exit("SIGINT Signal Emitted")
    else:
        raise ValueError(f"Undefined handler for {sig}")

def init_advertising(bus):
    print("Initialize Advertising")
    advertisements = []

    advertisement_adapter = find_adapter(bus, LE_ADVERTISING_MANAGER_IFACE)

    if not advertisement_adapter:
        raise NotFoundException(f"{LE_ADVERTISING_MANAGER_IFACE} interface not found")

    advertisement_props = dbus.Interface(bus.get_object(BLUEZ_SERVICE_NAME, advertisement_adapter), DBUS_PROP_IFACE)
    advertisement_props.Set(ADAPTER_IFACE, "Powered", dbus.Boolean(1))
    advertisement_props.Set(ADAPTER_IFACE, "Pairable", dbus.Boolean(0))

    advertisement_manager = dbus.Interface(bus.get_object(BLUEZ_SERVICE_NAME, advertisement_adapter), LE_ADVERTISING_MANAGER_IFACE)
    
    rover_advertisement = RoverAdvertisement(bus, 0)

    advertisement_manager.RegisterAdvertisement(rover_advertisement.get_path(), {},
        reply_handler = partial(rover_advertisement.register_callback),
        error_handler = partial(rover_advertisement.register_error_callback, mainloop)
    )

    advertisements.append(rover_advertisement)
    return advertisement_manager, advertisements

def deinit_advertising(advertisement_manager, advertisements):
    print("Deinitialize Advertising")
    for advertisement in advertisements:
        print(f"UnregisterAdvertisement: {advertisement.get_path()}")
        advertisement_manager.UnregisterAdvertisement(advertisement)
        dbus.service.Object.remove_from_connection(advertisement)

def init_gatt_server(bus, rover, camera):
    print("Initialize GATT Server")
    gatt_adapter = find_adapter(bus, GATT_MANAGER_IFACE)

    if not gatt_adapter:
        raise NotFoundException(f"{GATT_MANAGER_IFACE} interface not found")
    
    gatt_manager = dbus.Interface(bus.get_object(BLUEZ_SERVICE_NAME, gatt_adapter), GATT_MANAGER_IFACE)

    rover_application = RoverApplication(bus, rover, camera)

    gatt_manager.RegisterApplication(rover_application.get_path(), {},
        reply_handler = partial(rover_application.register_callback),
        error_handler = partial(rover_application.register_error_callback, mainloop)
    )

    return gatt_manager, rover_application

def deinit_gatt_server(gatt_manager, application):
    print("Deinitialize GATT Server")
    print(f"Unregistered Application - {application.get_path()}")
    gatt_manager.UnregisterApplication(application)

def interfaces_added_cb(object_path, interfaces):
    print("Interface added: " + object_path)
    # print("Interfaces: " + repr(interfaces))
    
    # try:
    #     print("Device: " + repr(interfaces[DEVICE_IFACE]))
    #     print("Characteristic: " + repr(application.services[0].characteristics[0]))
    #     fd, mtu = application.services[0].characteristics[0].AcquireNotify({
    #         'device': interfaces[DEVICE_IFACE]
    #     })
    #     print("Fd: " + repr(fd) + " | mtu: " + repr(mtu))
    # except:
    #     print("Failed")

def interfaces_removed_cb(object_path, interfaces):
    print("Interface removed: " + object_path)
    # print("Interfaces: " + repr(interfaces))
    
    # try:
    #     print("Device: " + repr(interfaces[DEVICE_IFACE]))
    # except:
    #     print("Failed")

def init_object_manager(bus):
    object_manager = dbus.Interface(bus.get_object(BLUEZ_SERVICE_NAME, '/'), DBUS_OM_IFACE)
    object_manager.connect_to_signal('InterfacesAdded', interfaces_added_cb)
    object_manager.connect_to_signal('InterfacesRemoved', interfaces_removed_cb)

def main():
    DBusGMainLoop(set_as_default=True)
    bus = dbus.SystemBus()
    
    global mainloop
    mainloop = GLib.MainLoop()

    rover = Rover()
    camera = Camera()

    roverThread = Thread(target=rover.run)
    roverThread.daemon = True

    cameraThread = Thread(target=camera.run)
    cameraThread.daemon = True
    
    roverThread.start()
    cameraThread.start()

    global application
    advertisement_manager, advertisements = init_advertising(bus)
    gatt_manager, application = init_gatt_server(bus, rover, camera)
    init_object_manager(bus)

    signal.signal(signal.SIGINT, sigint_handler)

    mainloop.run()

    deinit_advertising(advertisement_manager, advertisements)
    deinit_gatt_server(gatt_manager, application)
    
    rover.close()
    camera.close()

if __name__ == '__main__':
    main()