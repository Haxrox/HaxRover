from gi.repository import GLib

from ble.Advertisement import Advertisement
from ble.Exceptions import *
from ble.Constants import *

class RoverAdvertisement(Advertisement):
    def __init__(self, bus, index):
        Advertisement.__init__(self, bus, index, 'peripheral')
        self.add_service_uuid(SERVICE_UUID)
        self.add_local_name(LOCAL_NAME)
    
    def register_callback(self):
        print("RoverAdvertisement Registered - " + self.get_path())

    def register_error_callback(self, mainloop, error):
        mainloop.quit()
        raise ErrorException(f"Failed to register RoverAdvertisement @ {self.get_path()} - {error}")
