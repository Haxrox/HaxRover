try:
    from gpiozero import Robot
except ImportError:
    print("Robot module not found")
    
from enum import Enum, auto
import time

NANOSECOND = 10**9
ROTATION_CONSTANT = 1/2
DISTANCE_CONSTANT = (1/25)
INFINITE_CONSTANT = 10*NANOSECOND

class State(Enum):
    ROTATE_LEFT = auto(),
    ROTATE_RIGHT = auto(),
    UP = auto(),
    DOWN = auto(),
    LEFT = auto(),
    RIGHT = auto(),
    STOP = auto()

class Rover:
    def __init__(self):
        try:
            self.robot = Robot(left=(21, 16, 12), right=(26, 19, 13))
        except:
            print("Cannot create Rover.robot")

        self.state = State.STOP
        self.target = time.time_ns()
        self.distance = 0
        self.running = False
    
    def run(self):
        try:
            self.running = True
            # state machine for robot movement
            while self.running:
                if (time.time_ns() >= self.target and self.state != State.STOP):
                    self.state = State.STOP
                if (self.state == State.STOP):
                    self.robot.stop()
                elif (self.state == State.UP):
                    self.robot.forward()
                elif (self.state == State.DOWN):
                    self.robot.backward()
                elif (self.state == State.LEFT or self.state == State.ROTATE_LEFT):
                    self.robot.left()
                    if (self.state == State.ROTATE_LEFT and time.time_ns() >= self.target):
                        self.forward(self.distance)
                elif (self.state == State.RIGHT or self.state == State.ROTATE_RIGHT):
                    self.robot.right()
                    if (self.state == State.ROTATE_RIGHT and time.time_ns() >= self.target):
                        self.forward(self.distance)
                elif (self.state == State.STOP):
                    self.robot.stop()
        except:
            print("Run error")
    
    def rotate_left(self, angle, distance):     
        # changes robot state
        self.state = State.ROTATE_LEFT
        # maps rotation angle to a amount of time for robot to rotate for
        self.target = time.time_ns() + (-1*angle / 360) * ROTATION_CONSTANT * NANOSECOND        
        self.distance = distance

    def rotate_right(self, angle, distance):
        self.state = State.ROTATE_RIGHT
        # maps rotation angle to a amount of time for robot to rotate for
        self.target = time.time_ns() + (angle / 360) * ROTATION_CONSTANT * NANOSECOND
        self.distance = distance

    def forward(self, meters):
        self.state = State.UP
        # maps the distance to a amount of time for robot to move forward for (for a click)
        self.target = time.time_ns() + meters * NANOSECOND * DISTANCE_CONSTANT

    def go_up(self):
        self.state = State.UP
        # robot move forwards (for a arrow key input)
        self.target = time.time_ns() + INFINITE_CONSTANT

    def go_down(self):
        self.state = State.DOWN
        # robot move backwards (for a arrow key input)
        self.target = time.time_ns() + INFINITE_CONSTANT

    def go_left(self):
        self.state = State.LEFT
        # robo tmove left (for a arrow key input)
        self.target = time.time_ns() + INFINITE_CONSTANT

    def go_right(self):
        self.state = State.RIGHT
        # robot move right (for a arrow key input)
        self.target = time.time_ns() + INFINITE_CONSTANT

    def go_stop(self):
        self.state = State.STOP
        # stops robot
        self.target = time.time_ns() + INFINITE_CONSTANT