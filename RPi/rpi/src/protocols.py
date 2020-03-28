"""
Communication protocols.
They are defined so that all subsystems know how to communicate with each other.
"""

MESSAGE_SEPARATOR = '|'.encode()
NEWLINE = '\n'.encode()

ANDROID_HEADER = 'AND'.encode()
ARDUINO_HEADER = 'ARD'.encode()
ALGORITHM_HEADER = 'ALG'.encode()


class Status:
    IDLE = 'idle'.encode()
    EXPLORING = 'exploring'.encode()
    FASTEST_PATH = 'fastest path'.encode()


class AndroidToArduino:
    MOVE_FORWARD = 'W1|'.encode()
    MOVE_BACK = 'S1|'.encode()
    TURN_LEFT = 'A|'.encode()
    TURN_RIGHT = 'D|'.encode()
    DO_SHORTCUT_1 = 'F1|'.encode()
    DO_SHORTCUT_2 = 'F2|'.encode()

    ALL_MESSAGES = [
        MOVE_FORWARD,
        MOVE_BACK,
        TURN_LEFT,
        TURN_RIGHT,
        DO_SHORTCUT_1,
        DO_SHORTCUT_2,
    ]


class AndroidToAlgorithm:
    START_EXPLORATION = 'ES|'.encode()
    START_FASTEST_PATH = 'FS|'.encode()
    SEND_ARENA = 'SendArena'.encode()


class AndroidToRPi:
    CALIBRATE_SENSOR = 'SS|'.encode()


class AlgorithmToAndroid:
    MOVE_FORWARD = 'W'.encode()[0]
    TURN_LEFT = 'A'.encode()[0]
    TURN_RIGHT = 'D'.encode()[0]
    CALIBRATING_CORNER = 'L'.encode()[0]
    SENSE_ALL = 'Z'.encode()[0]
    ALIGN_RIGHT = 'B'.encode()[0]
    ALIGN_FRONT = 'V'.encode()[0]

    MDF_STRING = 'M'.encode()[0]


class AlgorithmToRPi:
    TAKE_PICTURE = 'C'.encode()[0]
    EXPLORATION_COMPLETE = 'N'.encode()


class RPiToAndroid:
    STATUS_EXPLORING = '{"status":"exploring"}'.encode()
    STATUS_FASTEST_PATH = '{"status":"fastest path"}'.encode()
    STATUS_TURNING_LEFT = '{"status":"turning left"}'.encode()
    STATUS_TURNING_RIGHT = '{"status":"turning right"}'.encode()
    STATUS_IDLE = '{"status":"idle"}'.encode()
    STATUS_TAKING_PICTURE = '{"status":"taking picture"}'.encode()
    STATUS_CALIBRATING_CORNER = '{"status":"calibrating corner"}'.encode()
    STATUS_SENSE_ALL = '{"status":"sense all"}'.encode()
    STATUS_MOVING_FORWARD = '{"status":"moving forward"}'.encode()
    STATUS_ALIGN_RIGHT = '{"status":"align right"}'.encode()
    STATUS_ALIGN_FRONT = '{"status":"align front"}'.encode()
    
    MOVE_UP = '{"move":[{"direction":"forward"}]}'.encode()
    TURN_LEFT = '{"move":[{"direction":"left"}]}'.encode()
    TURN_RIGHT = '{"move":[{"direction":"right"}]}'.encode()


class RPiToArduino:
    CALIBRATE_SENSOR = 'L|A|'.encode()
    START_EXPLORATION = 'E|'.encode()
    START_FASTEST_PATH = 'F|'.encode()


class RPiToAlgorithm:
    DONE_TAKING_PICTURE = 'D'.encode()
    DONE_IMG_REC = 'I'.encode()
