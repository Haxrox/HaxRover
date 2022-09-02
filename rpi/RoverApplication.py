import dbus
import dbus.service
from functools import partial
from gi.repository import GLib
from threading import Thread

from ble.Constants import *
from ble.Exceptions import *

from services.RoverService import RoverService

from Rover import Rover
from Camera import Camera

class RoverApplication(dbus.service.Object):
    """
    org.bluez.GattApplication1 interface implementation
    """
    def __init__(self, bus):
        self.path = '/haxrover'
        self.services = []
        self.bus = bus
        self.rover = Rover()
        self.camera = Camera()

        dbus.service.Object.__init__(self, bus, self.path)        
        roverService = RoverService(bus, 0)
        roverCharacteristic = roverService.find_characteristic(ROVER_CHARACTERISTIC_UUID)
        
        roverCharacteristic.on_read(partial(self.read_value))
        roverCharacteristic.on_write(partial(self.write_value))
        roverCharacteristic.on_start_notify(partial(self.start_notify))
        roverCharacteristic.on_stop_notify(partial(self.stop_notify))

        self.add_service(roverService)

    def get_path(self):
        return dbus.ObjectPath(self.path)

    def add_service(self, service):
        self.services.append(service)

    def get_service(self, uuid):
        for service in self.services:
            if service.get_uuid() == uuid:
                return service
        return None

    def read_value(self, characteristic, options):
        print('RoverCharacteristic Read: ' + repr(characteristic.value))
        print("RoverCharacteristic Read Options: " + repr(options))
        print("RoverCharacteristic Read Device: " + repr(options['device']))
        
        if characteristic.notifying:
            print("Notify characteristic changed")
            try:
                # object = self.bus.get_object(BLUEZ_SERVICE_NAME, options['device'] + "/")
                print("Object: " + repr(object))
                # characteristic.PropertiesChanged(GATT_CHRC_IFACE, {'Value': characteristic.value})
                # characteristic.PropertiesChanged(GATT_CHRC_IFACE, {'Value': dbus.Array("Test", 's')})
                characteristic.PropertiesChanged(GATT_CHRC_IFACE, {'Value': [dbus.Byte(84), dbus.Byte(101), dbus.Byte(115), dbus.Byte(116)]}, [])
                # object.PropertiesChanged(GATT_CHRC_IFACE, {'Value': dbus.Array([dbus.Byte(84), dbus.Byte(101), dbus.Byte(115), dbus.Byte(116)], "y")}, dbus.Array([], "y"))

            except:
                print("failed to notify characteristic changed")

        return characteristic.value

    def write_value(self, characteristic, value, options):
        print('RoverCharacteristic write_value: ' + repr(value))
        print("RoverCharacteristic write_value options: " + repr(options))
        print("RoverCharacteristic write_value device: " + repr(options['device']))
 
        direction = 'go_' + (''.join([str(v).lower() for v in value]))
        print("value: %s" % direction)

        try:
            characteristic.value = value
            getattr(self.rover, direction)()
        except:
            print("Invalid command: " + direction)

        # try:
        #     # characteristic.PropertiesChanged(GATT_CHRC_IFACE, {'Value': characteristic.value}, []) # WORKS given characteristic.value is correct
        # characteristic.PropertiesChanged(GATT_CHRC_IFACE, {'Value': dbus.Array("Test", 's')}, []) # DOES NOT WORK - must convert to dbus.Byte ig
        # characteristic.PropertiesChanged(GATT_CHRC_IFACE, {'Value': [dbus.Byte(84), dbus.Byte(101), dbus.Byte(115), dbus.Byte(116)]}, []) # WORKS
        # except:
        #     print("failed to notify characteristic changed")

    def notify(self, characteristic):
        if characteristic.notifying:
            try:
                pdu = self.camera.get()
                print("Notify: " + repr(len(pdu)))
                self.PropertiesChanged(GATT_CHRC_IFACE, {'Value': pdu}, [])
                GLib.timeout_add(0, self.notify)
            except Exception as e: 
                print("Failed to notify: " + repr(e))
                GLib.timeout_add(5000, self.notify)
    
    def start_notify(self, characteristic):
        print("StartNotify")
        self.camera.start()
        self.notify()
    
    def stop_notify(self):
        print("StopNotify")
        self.camera.stop()

    def close(self):
        self.camera.close()
        self.rover.close()

    @dbus.service.method(DBUS_OM_IFACE, out_signature='a{oa{sa{sv}}}')
    def GetManagedObjects(self):
        response = {}

        for service in self.services:
            response[service.get_path()] = service.get_properties()
            chrcs = service.get_characteristics()
            for chrc in chrcs:
                response[chrc.get_path()] = chrc.get_properties()
                descs = chrc.get_descriptors()
                for desc in descs:
                    response[desc.get_path()] = desc.get_properties()

        return response

    def register_callback(self):
        print("RoverApplication Registered - " + self.get_path())
        roverThread = Thread(target=self.rover.run)
        roverThread.daemon = True

        cameraThread = Thread(target=self.camera.run)
        cameraThread.daemon = True
        
        roverThread.start()
        cameraThread.start()

    def register_error_callback(self, mainloop, error):
        mainloop.quit()
        raise ErrorException(f"Failed to register RoverApplication @ {self.get_path()} - {error}")
