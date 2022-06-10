import dbus
from queue import Queue
from gi.repository import GLib

from ble.Service import Service
from ble.Characteristic import Characteristic
from ble.Descriptor import Descriptor
from ble.Constants import *

class RoverService(Service):
    def __init__(self, bus, index, rover, camera):
        Service.__init__(self, bus, index, ROVER_SERVICE_UUID, True)
        self.add_characteristic(RoverCharacteristic(bus, 0, self, rover, camera))

class RoverCharacteristic(Characteristic):
    def __init__(self, bus, index, service, rover, camera):
        Characteristic.__init__(self, bus, index, ROVER_CHARACTERISTIC_UUID, ['read', 'write', 'notify'], service)
        self.add_descriptor(RoverDescriptor(bus, 0, self))
        self.bus = bus
        self.notifying = False
        self.value = []
        self.rover = rover
        self.camera = camera
    
    def ReadValue(self, options):
        print('RoverCharacteristic Read: ' + repr(self.value))
        print("RoverCharacteristic Read Options: " + repr(options))
        print("RoverCharacteristic Read Device: " + repr(options['device']))
        
        # if self.notifying:
        #     print("Notify characteristic changed")
            # try:
            #     object = self.bus.get_object(BLUEZ_SERVICE_NAME, options['device'] + "/")
            #     print("Object: " + repr(object))
            #     # self.PropertiesChanged(GATT_CHRC_IFACE, {'Value': self.value})
            #     # self.PropertiesChanged(GATT_CHRC_IFACE, {'Value': dbus.Array("Test", 's')})
            #     # self.PropertiesChanged(GATT_CHRC_IFACE, {'Value': [dbus.Byte(84), dbus.Byte(101), dbus.Byte(115), dbus.Byte(116)]}, [])
            #     object.PropertiesChanged(GATT_CHRC_IFACE, {'Value': dbus.Array([dbus.Byte(84), dbus.Byte(101), dbus.Byte(115), dbus.Byte(116)], "y")}, dbus.Array([], "y"))

            # except:
            #     print("failed to notify characteristic changed")

        return self.value

    def WriteValue(self, value, options):
        print('RoverCharacteristic Write: ' + repr(value))
        print("RoverCharacteristic Write Options: " + repr(options))
        print("RoverCharacteristic Write Device: " + repr(options['device']))
 
        direction = 'go_' + (''.join([str(v).lower() for v in value]))
        print("value: %s" % direction)

        try:
            self.value = value
            getattr(self.rover, direction)()
        except:
            print("Invalid command: " + direction)

        # try:
        #     # self.PropertiesChanged(GATT_CHRC_IFACE, {'Value': self.value}, []) # WORKS given self.value is correct
        # self.PropertiesChanged(GATT_CHRC_IFACE, {'Value': dbus.Array("Test", 's')}, []) # DOES NOT WORK - must convert to dbus.Byte ig
        # self.PropertiesChanged(GATT_CHRC_IFACE, {'Value': [dbus.Byte(84), dbus.Byte(101), dbus.Byte(115), dbus.Byte(116)]}, []) # WORKS
        # except:
        #     print("failed to notify characteristic changed")

    def notify(self):
        print("Notify")
        if self.notifying:
            try:
                pdu = [dbus.Byte(byte) for byte in self.camera.get()]
                self.PropertiesChanged(GATT_CHRC_IFACE, {'Value': pdu}, [])
                GLib.timeout_add(0, self.notify)
            except Exception as e: 
                print("Failed to notify: " + repr(e))
        else:
            pass

    def StartNotify(self):
        print("StartNotify")
        
        if self.notifying:
            print('Already notifying, nothing to do')
            return

        self.notifying = True
        
        self.notify()

    def StopNotify(self):
        print("StopNotify")

        if not self.notifying:
            print('Not notifying, nothing to do')
            return

        self.notifying = False
        self.camera.stop()

class RoverDescriptor(Descriptor):
    def __init__(self, bus, index, characteristic):
        Descriptor.__init__(self, bus, index, ROVER_DESCRIPTOR_UUID, ['read'], characteristic)

    def ReadValue(self, options):
        return [
                dbus.Byte(84), dbus.Byte(101), dbus.Byte(115), dbus.Byte(116)
        ]