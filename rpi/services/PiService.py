import dbus

from ble.Service import Service
from ble.Characteristic import Characteristic
from ble.Descriptor import Descriptor
from ble.Constants import *

class PiService(Service):
    def __init__(self, bus, index):
        Service.__init__(self, bus, index, PI_SERVICE_UUID, True)
        self.add_characteristic(PiCharacteristic(bus, 0, self))

class PiCharacteristic(Characteristic):
    def __init__(self, bus, index, service):
        Characteristic.__init__(self, bus, index, PI_CHARACTERISTIC_UUID, ['read', 'write'], service)
        self.value = []
        # self.add_descriptor(PiDescriptor(bus, 0, self))
    
    def ReadValue(self, options):
        print('PiCharacteristic Read: ' + repr(self.value))
        return self.value

    def WriteValue(self, value, options):
        print('PiCharacteristic Write: ' + repr(value))
        self.value = value

class PiDescriptor(Descriptor):
    def __init__(self, bus, index, characteristic):
        Descriptor.__init__(self, bus, index, PI_DESCRIPTOR_UUID, ['read', 'write'], characteristic)

    def ReadValue(self, options):
        return [
                dbus.Byte('T'), dbus.Byte('e'), dbus.Byte('s'), dbus.Byte('t')
        ]