import dbus
import dbus.service

from ble.Exceptions import *
from ble.Constants import *

class Characteristic(dbus.service.Object):
    """
    org.bluez.GattCharacteristic1 interface implementation
    """
    def __init__(self, bus, index, uuid, flags, service):
        self.path = service.path + '/char' + str(index)
        self.bus = bus
        self.uuid = uuid
        self.service = service
        self.flags = flags
        self.notifying = False
        self.descriptors = []
        self.callbacks = {}
        dbus.service.Object.__init__(self, bus, self.path)

    def get_properties(self):
        return {
                GATT_CHRC_IFACE: {
                        'Service': self.service.get_path(),
                        'UUID': self.uuid,
                        'Flags': self.flags,
                        'Descriptors': dbus.Array(
                                self.get_descriptor_paths(),
                                signature='o')
                }
        }

    def get_path(self):
        return dbus.ObjectPath(self.path)

    def add_descriptor(self, descriptor):
        self.descriptors.append(descriptor)

    def get_descriptor_paths(self):
        result = []
        for desc in self.descriptors:
            result.append(desc.get_path())
        return result

    def get_descriptors(self):
        return self.descriptors

    def find_descriptor(self, uuid):
        for descriptor in self.descriptors:
            if descriptor.get_uuid() == uuid:
                return descriptor
        return None

    def get_uuid(self):
        return self.uuid

    def on(self, event, callback):
        self.callbacks[event] = callback
    
    def on_read(self, callback):
        self.callbacks["read"] = callback

    def on_write(self, callback):
        self.callbacks["write"] = callback

    def on_start_notify(self, callback):
        self.callbacks["startNotify"] = callback
    
    def on_stop_notify(self, callback):
        self.callbacks["stopNotify"] = callback

    @dbus.service.method(DBUS_PROP_IFACE,
                         in_signature='s',
                         out_signature='a{sv}')
    def GetAll(self, interface):
        if interface != GATT_CHRC_IFACE:
            raise InvalidArgsException()

        return self.get_properties()[GATT_CHRC_IFACE]

    @dbus.service.method(GATT_CHRC_IFACE,
                        in_signature='a{sv}',
                        out_signature='ay')
    def ReadValue(self, options):
        if "read" in self.callbacks:
            return self.callbacks["read"](self, options)
        else:
            print('Default ReadValue called, returning error')
            raise NotSupportedException()

    @dbus.service.method(GATT_CHRC_IFACE, in_signature='aya{sv}')
    def WriteValue(self, value, options):
        if "write" in self.callbacks:
            return self.callbacks["write"](self, value, options)
        else:
            print('Default WriteValue called, returning error')
            raise NotSupportedException()

    # @dbus.service.method(GATT_CHRC_IFACE, in_signature='a{sv}', out_signature='hq')
    # def AcquireWrite(self, options):
    #     print('Default AcquireWrite called, returning error')
    #     raise NotSupportedException()

    # @dbus.service.method(GATT_CHRC_IFACE, in_signature='a{sv}', out_signature='hq')
    # def AcquireNotify(self, options):
    #     print('Default AcquireNotify called, returning error')
    #     raise NotSupportedException()

    @dbus.service.method(GATT_CHRC_IFACE)
    def StartNotify(self):
        if self.notifying:
            return

        self.notifying = True
        if "startNotify" in self.callbacks:
            return self.callbacks["startNotify"](self)
        else:
            print('Default StartNotify called, returning error')
            raise NotSupportedException()

    @dbus.service.method(GATT_CHRC_IFACE)
    def StopNotify(self):
        if not self.notifying:
            return

        self.notifying = False
        if "stopNotify" in self.callbacks:
            self.callbacks["stopNotify"](self)
        else:
            print('Default StopNotify called, returning error')
            raise NotSupportedException()

    @dbus.service.signal(DBUS_PROP_IFACE,
                         signature='sa{sv}as')
    def PropertiesChanged(self, interface, changed, invalidated):
        # print('Default PropertiesChanged called')
        # print("Interface: " + repr(interface))
        # print("Changed: " + repr(changed))
        # print("Invalidated: " + repr(invalidated))

        return True