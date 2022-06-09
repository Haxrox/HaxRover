# import the necessary packages
from queue import Queue
import base64
import dbus

try:
    from picamera.array import PiRGBArray
    from picamera import PiCamera
    import cv2  # opencv-python
except ImportError:
    print("PiCamera or cv2 not installed")

import time

# constants
FRAME_SIZE = 510
FRAMERATE = 24
HEIGHT = 640
WIDTH = 480

class Camera:
    capturing = False
    queue = Queue(0)

    def __init__(self):

        try:
            # initialize the camera and grab a reference to the raw camera capture
            self.camera = PiCamera()
            self.camera.resolution = (HEIGHT, WIDTH)
            self.camera.framerate = FRAMERATE
            self.rawCapture = PiRGBArray(self.camera, size=(HEIGHT, WIDTH))
        except:
            print("Cannot create PiCamera")
            self.counter = 0

        # allow the camera to warmup
        time.sleep(0.1)

    def run(self):
        try:
            self.capturing = True
            # capture frames from the camera
            for frame in self.camera.capture_continuous(self.rawCapture, format="bgr", use_video_port=True):
                if self.capturing:
                    # grab the raw NumPy array representing the image, then initialize the timestamp
                    # and occupied/unoccupied text
                    image = frame.array
                    # encode the frame as jpg and send the bytes
                    buffer = cv2.imencode('.jpg', image, [cv2.IMWRITE_JPEG_QUALITY, 45])[1].tobytes()

                    parsedBytes = 0

                    while (parsedBytes < len(buffer)):
                        frame = []
                        if len(buffer) > FRAME_SIZE:
                            frame[0] = 1
                            frame[1:] = buffer[parsedBytes: (parsedBytes + FRAME_SIZE)]
                            parsedBytes += FRAME_SIZE
                        else:
                            frame[0] = 0
                            frame[1:] = buffer[parsedBytes:]
                            parsedBytes += len(buffer) - parsedBytes
                        self.queue.put(frame)
                    # clear the stream in preparation for the next frame
                    self.rawCapture.truncate(0)
                    # return buffer
                else:
                    break
        except:
            print("Camera Run Error")

    def get(self):
        if self.queue.empty():
            if (not hasattr(self, "camera")):
                self.counter = self.counter + 1
                return bytearray(str(self.counter), "utf-8")
            else:
                return []
        else:
            return self.queue.get()

    def stop(self):
        self.capturing = False

    def close(self):
        if hasattr(self, "camera"):
            self.camera.close()