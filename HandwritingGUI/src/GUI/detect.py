from PIL import Image
import pytesseract
import cv2
import sys
import os

class FilePaths:
	"filenames and paths to data"
	fnImageFolder = 'C:/Users/wsywu/git/CS-Project-Handwriting-Analyzer-/OCR_Model/Sources/SplitedImages'

def detect():	
    infered_words = []
    dir = os.listdir(FilePaths.fnImageFolder)
    #dir.sort(key=int)

    for image in dir:
        test_img = os.path.join(FilePaths.fnImageFolder, image)

        result = pytesseract.image_to_string(Image.open(test_img))
        infered_words.append(result)

    return infered_words

text = detect()
print(text)
