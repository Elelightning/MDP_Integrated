# CZ3004 MDP Image Processing Server

Python version: 3.7.4

## How it works

1. receives images from RPi and process them
2. detects symbols present in each image and output their bounding boxes and class ids
3. sends symbols' class ids back to RPi
4. displays all images with bounding boxes at the end of exploration

## How to set up

- change directory to 'image_processing_server': `cd image_processing server`
- create a new virtual environment named 'venv' in the 'image_processing_server' directory: `python -m venv venv` (or `python3 -m venv venv --python=python3.7.4` if your python version is different)
- activate the virtual environment: `source venv/bin/activate`
  - for Windows (inclusive of quotes): `"C:\...\image_processing_server\venv\Scripts\activate"`
- update pip and setuptools to latest version: `python -m pip install --upgrade pip setuptools`
- install dependencies: `pip install -r requirements.txt`
- once done, you can deactivate the virtual environment: `deactivate`
  - for Windows (inclusive of quotes): `"C:\...\image_processing_server\venv\Scripts\deactivate"`

## To run image processing server

- change directory to 'image_processing_server': `cd image_processing server`
- activate the virtual environment: `source venv/bin/activate`
  - for Windows (inclusive of quotes): `"C:\...\image_processing_server\venv\Scripts\activate"`
- run the server: `python main.py` (or `python -m main`)
- once done, you can deactivate the virtual environment: `deactivate`
  - for Windows (inclusive of quotes): `"C:\...\image_processing_server\venv\Scripts\deactivate"`
