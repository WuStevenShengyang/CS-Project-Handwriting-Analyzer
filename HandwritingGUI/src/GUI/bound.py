from PIL import Image
import pytesseract
from pytesseract import Output
import cv2
import sys

class FilePaths:
	"filenames and paths to data"
	fnImageFolder = 'C:/Users/wsywu/git/CS-Project-Handwriting-Analyzer-/OCR_Model/Sources/SplitedImages'

def detect(filename):
    bound = pytesseract.image_to_data(Image.open(filename), output_type=Output.DICT)
    return bound

# Setup imagesE
test_image = sys.argv[1]
bound = detect(test_image)
n_boxes = len(bound['level'])
boxes = []

for i in range(n_boxes):
    (t, x, y, w, h) = (bound['text'][i], bound['left'][i], bound['top'][i], bound['width'][i], bound['height'][i])
    if t == '':
        t = "NULL"
    boxes.append((t, x, y, w, h))

for box in boxes:
    for element in box:
        print(element, end=' ')
    print()
