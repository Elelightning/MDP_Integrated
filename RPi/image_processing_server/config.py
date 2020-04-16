MODEL_NAME = 'model'
INFERENCE_GRAPH = 'frozen_inference_graph.pb'
LABEL_MAP = 'labelmap.pbtxt'

# Number of classes the object detector can identify
NUM_CLASSES = 15

IMAGE_ENCODING = '.png'
STOPPING_IMAGE = 'stop_image_processing.png'

MAX_NUM_SYMBOLS = 3

IMAGE_WIDTH = 1920  # 400
IMAGE_HEIGHT = 1080  # 225

DISPLAY_IMAGE_WIDTH = 400

# red colour symbols tend to have lower confidence scores
MIN_CONFIDENCE_THRESHOLD = 0.50

# usually for non-red symbols, confidence of > 90%.
# however, once in a blue moon, confidence score may drop very low.
# no false positive with confidence higher than 70% though
# therefore, set confidence score this low
NON_RED_CONFIDENCE_THRESHOLD = 0.70

# used for filtering symbols that are 5 grids away
# sitution: [S]    [ ]  <R
# where [ ] be obstacle,
#       S be symbol
#       R be robot
#       < be camera direction
# 3 grids - extreme case (correct): ~750
# 5 grids (wrong; as shown in situation): ~695
YMAX_THRESHOLD = 775

SYMBOL_ON_LEFT_OF_IMAGE_THRESHOLD = 780  # left xmax compared to middle xmin
SYMBOL_ON_RIGHT_OF_IMAGE_THRESHOLD = 1090  # right xmin compared to middle xmax

MAIN_IMAGE_DIR = 'frames'
RAW_IMAGE_DIR = 'raw'
PROCESSED_IMAGE_DIR = 'processed'

RAW_IMAGE_PREFIX = 'frame'
PROCESSED_IMAGE_PREFIX = 'processed'

DISPLAY_DURATION_MILLISECONDS = 3000

LEFT_OBSTACLE = 'left_obstacle'
MIDDLE_OBSTACLE = 'middle_obstacle'
RIGHT_OBSTACLE = 'right_obstacle'

NO_SYMBOL = '-1'
