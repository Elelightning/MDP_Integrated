LOCALE = 'UTF-8'

# Android BT connection settings
RFCOMM_CHANNEL = 9
RPI_MAC_ADDR = 'B8:27:EB:99:A8:38'
UUID = '443559ba-b80f-4fb6-99d9-ddbcd6138fbd'
ANDROID_SOCKET_BUFFER_SIZE = 512

# Algorithm Wifi connection settings
# raspberryHotPotato: 192.168.3.1
WIFI_IP = '192.168.15.15'
WIFI_PORT = 8080
ALGORITHM_SOCKET_BUFFER_SIZE = 512

# Arduino USB connection settings
# SERIAL_PORT = '/dev/ttyACM0'
# Symbolic link to always point to the correct port that arduino is connected to
SERIAL_PORT = '/dev/serial/by-id/usb-Arduino__www.arduino.cc__0043_75232303235351F091C0-if00'
BAUD_RATE = 57600

# Image Recognition Settings
STOPPING_IMAGE = 'stop_image_processing.png'

IMAGE_WIDTH = 1920
IMAGE_HEIGHT = 1080
IMAGE_FORMAT = 'bgr'

BASE_IP = 'tcp://192.168.15.'
PORT = ':5555'

IMAGE_PROCESSING_SERVER_URLS = {
    'cheyanne': BASE_IP + '54' + PORT,
    'elbert': BASE_IP + '00' + PORT,  # don't have elbert's ip address yet
    'jason': BASE_IP + '52' + PORT,
    'joshua': BASE_IP + '93' + PORT,
    'mingyang': BASE_IP + '74' + PORT,
    'reuben': BASE_IP + '00' + PORT,  # don't have reuben's ip address yet
    'winston': BASE_IP + '55' + PORT,
    'yingting': BASE_IP + '90' + PORT,
}
