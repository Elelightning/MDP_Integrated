"""
This file contains the ImageProcessingServer class.
"""
import os
import shutil
import sys
from datetime import datetime

import cv2
import imutils
import numpy as np
import tensorflow as tf

from config import *
from image_receiver import custom_imagezmq as imagezmq
from utils import label_map_util
from utils import visualization_utils as vis_util


sys.path.append("..")

# Grab path to current working directory
cwd_path = os.getcwd()


class ImageProcessingServer:
    def __init__(self):
        # Path to frozen detection graph .pb file, which contains the model that is used
        # for object detection.
        self.inference_graph_path = os.path.join(cwd_path, MODEL_NAME, INFERENCE_GRAPH)

        # Path to label map file
        self.labels_path = os.path.join(cwd_path, MODEL_NAME, LABEL_MAP)

        self._initialise_directories()

        # Load the label map.
        #
        # Label maps map indices to category names, so that when our convolution
        # network predicts `0`, we know that this corresponds to `white up arrow`.
        #
        # Here we use internal utility functions, but anything that returns a
        # dictionary mapping integers to appropriate string labels would be fine

        label_map = label_map_util.load_labelmap(self.labels_path)
        categories = label_map_util.convert_label_map_to_categories(
            label_map, 
            max_num_classes=NUM_CLASSES, 
            use_display_name=True
        )

        self.category_index = label_map_util.create_category_index(categories)

        # Load the Tensorflow model into memory.
        detection_graph = tf.Graph()

        with detection_graph.as_default():
            od_graph_def = tf.compat.v1.GraphDef()
            
            with tf.io.gfile.GFile(self.inference_graph_path, 'rb') as fid:
                serialized_graph = fid.read()
                od_graph_def.ParseFromString(serialized_graph)
                tf.import_graph_def(od_graph_def, name='')
                
            self.session = tf.compat.v1.Session(graph=detection_graph)
            
        # Define input and output tensors (i.e. data) for the object detection classifier

        # Input tensor is the image
        self.image_tensor = detection_graph.get_tensor_by_name('image_tensor:0')

        # Output tensors are the detection bounding_boxes, scores, and classes
        # Each box represents a part of the image where a particular object was detected

        # Each score represents level of confidence for each of the objects.
        # The score is shown on the result image, together with the class label.

        # Number of objects detected is also given
        self.session_params = [
            detection_graph.get_tensor_by_name('detection_boxes:0'), 
            detection_graph.get_tensor_by_name('detection_scores:0'), 
            detection_graph.get_tensor_by_name('detection_classes:0'), 
            detection_graph.get_tensor_by_name('num_detections:0'),
        ]

        # image to be sent from RPi to stop this server
        self.stopping_image = cv2.imread(STOPPING_IMAGE)
        self.stopping_image = imutils.resize(self.stopping_image, width=IMAGE_WIDTH)
        
        # initialize the ImageHub object
        self.image_hub = imagezmq.CustomImageHub()

        self.frame_list = []  # list of frames with detections
        
    def start(self):
        print('\nStarted image processing server.\n')
        
        while True:
            print('Waiting for image from RPi...')

            # receive RPi name and frame from the RPi and acknowledge the receipt
            _, frame = self.image_hub.recv_image()
            print('Connected and received frame at time: ' + str(datetime.now()))
            
            # resize the frame to have a width of IMAGE_WIDTH pixels, then
            # grab the frame dimensions and construct a blob
            frame = imutils.resize(frame, width=IMAGE_WIDTH)

            if self._is_stopping_frame(frame):
                restart = self._show_all_images()

                if restart:
                    self._initialise_directories()
                    self.frame_list.clear()
                else:
                    break  # stop image processing server
            
            # Acquire frame and expand frame dimensions to have shape: [1, None, None, 3]
            # i.e. a single-column array, where each item in the column has the pixel RGB value
            # for input into model
            frame_expanded = np.expand_dims(frame, axis=0)
            
            # form image file path for saving
            raw_image_name = RAW_IMAGE_PREFIX + str(len(self.frame_list)) + IMAGE_ENCODING
            raw_image_path = os.path.join(self.raw_image_dir_path, raw_image_name)
            
            # save raw image
            save_success = cv2.imwrite(raw_image_path, frame)
            # print('save', raw_image_name, 'successful?', save_success)
            
            frame = cv2.imread(raw_image_path)
            
            # Perform the actual detection by running the model with the image as input
            # note: detections are already sorted by confidence
            bounding_boxes, scores, classes, _ = self.session.run(
                self.session_params,
                feed_dict={self.image_tensor: frame_expanded}
            )
            
            # bounding box format: [ymin, xmin, ymax, xmax]
            bounding_boxes = np.squeeze(bounding_boxes)
            classes = np.squeeze(classes).astype(np.int32)
            scores = np.squeeze(scores)

            # convert from np array to list for getting true positive
            bbox_list = bounding_boxes.tolist()
            class_list = classes.tolist()
            score_list = scores.tolist()

            obstacle_symbol_map, bounding_boxes, classes, scores = \
                self._get_true_positives(bbox_list, class_list, score_list)
                
            # forms 'LEFT_SYMBOL|MIDDLE_SYMBOL|RIGHT_SYMBOL'
            return_string = '|'.join(obstacle_symbol_map.values())

            # convert from list to np array for visualising images
            bounding_boxes = np.array(bounding_boxes)
            classes = np.array(classes)
            scores = np.array(scores)

            # Draw the results of the detection (aka 'visualize the results')
            frame = vis_util.visualize_boxes_and_labels_on_image_array(
                frame,
                bounding_boxes,
                classes,
                scores,
                self.category_index,
                use_normalized_coordinates=True,
                line_thickness=4,
                min_score_thresh=MIN_CONFIDENCE_THRESHOLD
            )

            # All the results have been drawn on the frame, so it's time to display it.
            frame_rgb = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)

            # form image file path for saving
            processed_image_name = PROCESSED_IMAGE_PREFIX + \
                str(len(self.frame_list)) + IMAGE_ENCODING
            processed_image_path = os.path.join(
                self.processed_image_dir_path, 
                processed_image_name
            )
            
            # save processed image
            save_success = cv2.imwrite(processed_image_path, frame_rgb)
            # print('save', processed_image_name, 'successful?', save_success)

            self.image_hub.send_reply(return_string)
            # send_reply disconnects the connection
            print('Sent reply and disconnected at time: ' + str(datetime.now()) + '\n')

            if return_string != '-1|-1|-1':
                self.frame_list.append(frame_rgb)

        self.end()

    def end(self):
        print('Stopping image processing server')

        self.image_hub.send_reply('End')
        # send_reply disconnects the connection
        print('Sent reply and disconnected at time: ' + str(datetime.now()) + '\n')

    def _show_all_images(self):
        """
        return:
            whether key pressed is 'r'
        """
        print('Showing all detected images')
        # show all images with detections
        for index, frame in enumerate(self.frame_list):
            frame = imutils.resize(frame, width=DISPLAY_IMAGE_WIDTH)
            cv2.imshow('Image ' + str(index), frame)
        
        keycode = cv2.waitKey(0)

        cv2.destroyAllWindows()

        # https://stackoverflow.com/q/57690899/9171260
        return keycode & 0xFF == ord('r')

    def _initialise_directories(self):
        image_dir_path = os.path.join(cwd_path, MAIN_IMAGE_DIR)

        if os.path.exists(image_dir_path):
            shutil.rmtree(image_dir_path)

        self.raw_image_dir_path = os.path.join(image_dir_path, RAW_IMAGE_DIR)
        os.makedirs(self.raw_image_dir_path)

        self.processed_image_dir_path = os.path.join(image_dir_path, PROCESSED_IMAGE_DIR)
        os.makedirs(self.processed_image_dir_path)

    def _is_stopping_frame(self, frame):
        difference = cv2.subtract(frame, self.stopping_image)
        return not np.any(difference)

    def _get_true_positives(self, bbox_list, class_list, score_list):
        """
        params:
        - bbox_list (list): [
            [top_left_y (float), top_left_x (float), bot_right_y (float), bot_right_x (float)], 
            ..., 
        ]
        - class_list (list): [class_id (int), ]
        - score_list (list): [confidence_score (float)]

        return: (
            { LEFT_OBSTACLE: SYMBOL, MIDDLE_OBSTACLE: SYMBOL, RIGHT_OBSTACLE: SYMBOL }, 
            true positive bounding boxes (list), 
            true positive classes (list), 
            true positive confidence scores (list),
        )
        """
        bounding_boxes, classes, scores = [], [], []

        # -1 means no detection for that obstacle
        obstacle_symbol_map = {
            LEFT_OBSTACLE: NO_SYMBOL,
            MIDDLE_OBSTACLE: NO_SYMBOL,
            RIGHT_OBSTACLE: NO_SYMBOL,
        }

        num_symbols = 0
        
        left_xmax = float('-inf')
        right_xmin = float('inf')

        for bbox, class_id, score in zip(bbox_list, class_list, score_list):
            if num_symbols >= 3:
                break

            top_left_y, top_left_x, bot_right_y, bot_right_x = tuple(bbox)

            top_left_y = top_left_y * IMAGE_HEIGHT
            top_left_x = top_left_x * IMAGE_WIDTH
            bot_right_y = bot_right_y * IMAGE_HEIGHT
            bot_right_x = bot_right_x * IMAGE_WIDTH
			
            not_red = class_id != 2 and class_id != 8 and class_id != 11

            # false positive if:
            # confidence score is lower than a generic threshold (for all classes)
            # confidence score is lower than a higher threshold (for non-reds)
            # the bottom y-coordinate is lower than its repective threshold (too far)
            if ((score <= MIN_CONFIDENCE_THRESHOLD)
                or (not_red and score < NON_RED_CONFIDENCE_THRESHOLD) \
                or (bot_right_y < YMAX_THRESHOLD) \
                ):
                continue  # false positive -> skip

            if (bot_right_x < SYMBOL_ON_LEFT_OF_IMAGE_THRESHOLD):  # symbol left
                # obstacle already has a symbol of higher confidence,
                # and is directly to the left of middle
                if obstacle_symbol_map[LEFT_OBSTACLE] != NO_SYMBOL and bot_right_x < left_xmax:  
                    continue  
                
                left_xmax = bot_right_x
                obstacle_symbol_map[LEFT_OBSTACLE] = str(class_id)

            elif (top_left_x  > SYMBOL_ON_RIGHT_OF_IMAGE_THRESHOLD):  # symbol right
                # obstacle already has a symbol of higher confidence,
                # and is directly to the right of middle
                if obstacle_symbol_map[RIGHT_OBSTACLE] != NO_SYMBOL and top_left_x > right_xmin:
                    continue  
                
                right_xmin = top_left_x
                obstacle_symbol_map[RIGHT_OBSTACLE] = str(class_id)

            else:  # symbol middle
                # obstacle already has a symbol of higher confidence
                if obstacle_symbol_map[MIDDLE_OBSTACLE] != NO_SYMBOL:
                    continue  

                obstacle_symbol_map[MIDDLE_OBSTACLE] = str(class_id)

            bounding_boxes.append(bbox)
            classes.append(class_id)
            scores.append(score)
        
            print(
                'id: ', class_id,
                'confidence: ', '{:.3f}'.format(score),
                '\n',
                'xmin: ', '{:.3f}'.format(top_left_x),
                'xmax: ', '{:.3f}'.format(bot_right_x),
                'ymax: ', '{:.3f}'.format(bot_right_y),
                '\n',
            )

            num_symbols += 1

        return obstacle_symbol_map, bounding_boxes, classes, scores
