import cv2
import imagezmq

image_sender = imagezmq.ImageSender(connect_to='tcp://127.0.0.1:5555')
image = cv2.imread('interstellar_blackhole.jpg')

print('Enter "q" to stop client, "e" to stop server and anything else to send an image:')
command = input()

while command != 'q':
    if command == 'e':
        image = cv2.imread('../stop_image_processing.png')

    reply = image_sender.send_image('image from client (RPi)', image)

    if reply is not None:
        reply = reply.decode('utf-8')
        
    if reply == 'End':
        print('Stopping image processing server.')
        break  # stop sending images
    else:
        print('From server:', reply)

    print('Enter "q" to stop client, "e" to stop server and anything else to send an image:')
    command = input()

print('Stopping client.')
