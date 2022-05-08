import dbus
from ble.Constants import *

def find_adapter(bus : dbus, adapter: str):
    remote_om = dbus.Interface(bus.get_object(BLUEZ_SERVICE_NAME, '/'),
                               DBUS_OM_IFACE)
    objects = remote_om.GetManagedObjects()

    for o, props in objects.items():
        if adapter in props.keys():
            return o

    return None