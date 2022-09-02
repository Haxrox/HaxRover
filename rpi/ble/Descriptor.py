import dbus
import dbus.service

from ble.Exceptions import *
from ble.Constants import *

class Descriptor(dbus.service.Object):
    """
    org.bluez.GattDescriptor1 interface implementation
    """
    def __init__(self, bus, index, uuid, flags, characteristic):
        self.path = characteristic.path + '/desc' + str(index)
        self.bus = bus
        self.uuid = uuid
        self.flags = flags
        self.characteristic = characteristic
        self.callbacks = {}
        dbus.service.Object.__init__(self, bus, self.path)

    def get_properties(self):
        return {
                GATT_DESC_IFACE: {
                        'Characteristic': self.characteristic.get_path(),
                        'UUID': self.uuid,
                        'Flags': self.flags,
                }
        }

    def get_uuid(self):
        return self.uuid

    def on(self, event, callback):
        self.callbacks[event] = callback
    
    def on_read(self, callback):
        self.callbacks["read"] = callback

    def on_write(self, callback):
        self.callbacks["write"] = callback

    def get_path(self):
        return dbus.ObjectPath(self.path)

    @dbus.service.method(DBUS_PROP_IFACE,
                         in_signature='s',
                         out_signature='a{sv}')
    def GetAll(self, interface):
        if interface != GATT_DESC_IFACE:
            raise InvalidArgsException()

        return self.get_properties()[GATT_DESC_IFACE]

    @dbus.service.method(GATT_DESC_IFACE,
                        in_signature='a{sv}',
                        out_signature='ay')
    def ReadValue(self, options):
        if "read" in self.callbacks:
            return self.callbacks["read"](self, options)
        else:
            print('Default ReadValue called, returning error')
            raise NotSupportedException()

    @dbus.service.method(GATT_DESC_IFACE, in_signature='aya{sv}')
    def WriteValue(self, value, options):
        if "write" in self.callbacks:
            return self.callbacks["write"](self, value, options)
        else:
            print('Default WriteValue called, returning error')
            raise NotSupportedException()