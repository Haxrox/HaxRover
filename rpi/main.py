import signal
import sys
from functools import partial

import dbus
from dbus.mainloop.glib import DBusGMainLoop
from gi.repository import GLib

from ble.Utils import *
from ble.Constants import *
from ble.Exceptions import *

from PiAdvertisement import PiAdvertisement
from PiApplication import PiApplication

mainloop = None

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
    
    pi_advertisement = PiAdvertisement(bus, 0)

    advertisement_manager.RegisterAdvertisement(pi_advertisement.get_path(), {},
        reply_handler = partial(pi_advertisement.register_callback),
        error_handler = partial(pi_advertisement.register_error_callback, mainloop)
    )

    advertisements.append(pi_advertisement)
    return advertisement_manager, advertisements

def deinit_advertising(advertisement_manager, advertisements):
    print("Deinitialize Advertising")
    for advertisement in advertisements:
        print(f"UnregisterAdvertisement: {advertisement.get_path()}")
        advertisement_manager.UnregisterAdvertisement(advertisement)
        dbus.service.Object.remove_from_connection(advertisement)

def init_gatt_server(bus):
    print("Initialize GATT Server")
    gatt_adapter = find_adapter(bus, GATT_MANAGER_IFACE)

    if not gatt_adapter:
        raise NotFoundException(f"{GATT_MANAGER_IFACE} interface not found")
    
    gatt_manager = dbus.Interface(bus.get_object(BLUEZ_SERVICE_NAME, gatt_adapter), GATT_MANAGER_IFACE)

    pi_application = PiApplication(bus)

    gatt_manager.RegisterApplication(pi_application.get_path(), {},
        reply_handler = partial(pi_application.register_callback),
        error_handler = partial(pi_application.register_error_callback, mainloop)
    )

    return gatt_manager, pi_application

def deinit_gatt_server(gatt_manager, application):
    print("Deinitialize GATT Server")
    print(f"Unregistered Application - {application.get_path()}")
    gatt_manager.UnregisterApplication(application)

def main():
    DBusGMainLoop(set_as_default=True)
    bus = dbus.SystemBus()
    
    global mainloop
    mainloop = GLib.MainLoop()

    advertisement_manager, advertisements = init_advertising(bus)
    gatt_manager, application = init_gatt_server(bus)

    signal.signal(signal.SIGINT, sigint_handler)

    mainloop.run()

    deinit_advertising(advertisement_manager, advertisements)
    deinit_gatt_server(gatt_manager, application)

if __name__ == '__main__':
    main()