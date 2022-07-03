import dbus
import dbus.service
from gi.repository import GLib

from ble.Constants import *
from ble.Exceptions import *

from services.RoverService import RoverService

class RoverApplication(dbus.service.Object):
    """
    org.bluez.GattApplication1 interface implementation
    """
    def __init__(self, bus, rover, camera):
        self.path = '/haxrover'
        self.services = []
        dbus.service.Object.__init__(self, bus, self.path)
        self.add_service(RoverService(bus, 0, rover, camera))

    def get_path(self):
        return dbus.ObjectPath(self.path)

    def add_service(self, service):
        self.services.append(service)

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

    def register_error_callback(self, mainloop, error):
        mainloop.quit()
        raise ErrorException(f"Failed to register RoverApplication @ {self.get_path()} - {error}")
