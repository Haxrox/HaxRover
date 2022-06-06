import dbus
from gi.repository import GLib

from ble.Service import Service
from ble.Characteristic import Characteristic
from ble.Descriptor import Descriptor
from ble.Constants import *

class RoverService(Service):
    def __init__(self, bus, index, rover):
        Service.__init__(self, bus, index, ROVER_SERVICE_UUID, True)
        self.add_characteristic(RoverCharacteristic(bus, 0, self, rover))

class RoverCharacteristic(Characteristic):
    def __init__(self, bus, index, service, rover):
        Characteristic.__init__(self, bus, index, ROVER_CHARACTERISTIC_UUID, ['read', 'write', 'notify'], service)
        # self.add_descriptor(RoverDescriptor(bus, 0, self))
        self.notifying = False
        self.value = []
        self.rover = rover
    
    def ReadValue(self, options):
        print('RoverCharacteristic Read: ' + repr(self.value))
        
        return self.value

    def WriteValue(self, value, options):
        print('RoverCharacteristic Write: ' + repr(value))
        print("RoverCharacteristic options: " + repr(options))
        print("Device: " + repr(options['device']))
        # print("ToString [" + len(value) + "]: " + repr(value[0]))
        # print("2 bytes: " + str(value[0:2]))
 
        direction = 'go_' + (''.join([str(v).lower() for v in value]))
        print("value: %s" % direction)

        try:
            self.value = value
            getattr(self.rover, direction)()
        except:
            print("Invalid command: " + direction)

    def StartNotify(self):
        print("StartNotify")
        
        if self.notifying:
            print('Already notifying, nothing to do')
            return

        self.notifying = True

    def StopNotify(self):
        print("StopNotify")
        if not self.Notifying:
            print('Not notifying, nothing to do')
            return

        self.notifying = False

class RoverDescriptor(Descriptor):
    def __init__(self, bus, index, characteristic):
        Descriptor.__init__(self, bus, index, ROVER_DESCRIPTOR_UUID, ['read', 'write', 'notify'], characteristic)

    def ReadValue(self, options):
        return [
                dbus.Byte('T'), dbus.Byte('e'), dbus.Byte('s'), dbus.Byte('t')
        ]