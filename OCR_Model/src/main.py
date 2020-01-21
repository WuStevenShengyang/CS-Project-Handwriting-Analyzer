from __future__ import division
from __future__ import print_function

import sys
import argparse
import cv2
import editdistance
from DataLoader import DataLoader, Batch
from Model import Model, DecoderType
from SamplePreprocessor import preprocess
import os
import pickle
import tensorflow as tf
from SpellCheck import SpellCheck
import shutil
from pathlib import Path
import os

class FilePaths:
	"filenames and paths to data"
	
	cwd = Path(os.getcwd())
	fnCharList = os.path.join(cwd.parent, "OCR_Model/model/charList.txt")
	fnAccuracy = os.path.join(cwd.parent, "OCR_Model/model/accuracy.txt")
	fnTrain = os.path.join(cwd.parent, "OCR_Model/data")
	fnCorpus = os.path.join(cwd.parent, "OCR_Model/datacorpus.txt")
	fnResult = os.path.join(cwd.parent, "OCR_Model/runtimeData/recognized.txt")
	fnImageFolder = os.path.join(cwd.parent, "OCR_Model/runtimeData/Blobs")
	fnModel = os.path.join(cwd.parent, "OCR_Model/model")

def train(model, loader):
	"train NN"
	epoch = 0  # number of training epochs since start
	bestCharErrorRate = float('inf')  # best valdiation character error rate
	noImprovementSince = 0  # number of epochs no improvement of character error rate occured
	earlyStopping = 5  # stop training after this number of epochs without improvement
	while True:
		epoch += 1
		print('Epoch:', epoch)

		# train
		print('Train NN')
		loader.trainSet()
		while loader.hasNext():
			iterInfo = loader.getIteratorInfo()
			batch = loader.getNext()
			loss = model.trainBatch(batch)
			print('Batch:', iterInfo[0], '/', iterInfo[1], 'Loss:', loss)

		# validate
		charErrorRate = validate(model, loader)

		# if best validation accuracy so far, save model parameters
		if charErrorRate < bestCharErrorRate:
			print('Character error rate improved, save model')
			bestCharErrorRate = charErrorRate
			noImprovementSince = 0
			model.save()
			open(FilePaths.fnAccuracy, 'w').write(	
			    'Validation character error rate of saved model: %f%%' % (charErrorRate*100.0))
		else:
			print('Character error rate not improved')
			noImprovementSince += 1

		# stop training if no more improvement in the last x epochs
		if noImprovementSince >= earlyStopping:
			print('No more improvement since %d epochs. Training stopped.' % earlyStopping)
			break


def validate(model, loader):
	"validate NN"
	print('Validate NN')
	loader.validationSet()
	numCharErr = 0
	numCharTotal = 0
	numWordOK = 0
	numWordTotal = 0
	while loader.hasNext():
		iterInfo = loader.getIteratorInfo()
		print('Batch:', iterInfo[0], '/', iterInfo[1])
		batch = loader.getNext()
		(recognized, _) = model.inferBatch(batch)
  
		print('Ground truth -> Recognized')
		for i in range(len(recognized)):
			numWordOK += 1 if batch.gtTexts[i] == recognized[i] else 0
			numWordTotal += 1
			dist = editdistance.eval(recognized[i], batch.gtTexts[i])
			numCharErr += dist
			numCharTotal += len(batch.gtTexts[i])
			print('[OK]' if dist == 0 else '[ERR:%d]' % dist, '"' +
			      batch.gtTexts[i] + '"', '->', '"' + recognized[i] + '"')

	# print validation result
	charErrorRate = numCharErr / numCharTotal
	wordAccuracy = numWordOK / numWordTotal
	print('Character error rate: %f%%. Word accuracy: %f%%.' %
	      (charErrorRate*100.0, wordAccuracy*100.0))
	return charErrorRate


def infer(model, fnImg):
	"recognize text in image provided by file path"
	img = preprocess(cv2.imread(fnImg, cv2.IMREAD_GRAYSCALE), Model.imgSize)
	batch = Batch(None, [img])
	(recognized, probability) = model.inferBatch(batch, True)

	return recognized[0]

def main():
	"main function"
	# optional command line args
	parser = argparse.ArgumentParser()
	parser.add_argument('--train', help='train the NN', action='store_true')
	parser.add_argument('--validate', help='validate the NN', action='store_true')
	parser.add_argument(
	    '--beamsearch', help='use beam search instead of best path decoding', action='store_true')
	parser.add_argument(
	    '--wordbeamsearch', help='use word beam search instead of best path decoding', action='store_true')
	parser.add_argument(
	    '--dump', help='dump output of NN to CSV file(s)', action='store_true')

	args = parser.parse_args()

	decoderType = DecoderType.BestPath
	if args.beamsearch:
		decoderType = DecoderType.BeamSearch
	elif args.wordbeamsearch:
		decoderType = DecoderType.WordBeamSearch

	# train or validate on IAM dataset
	if args.train or args.validate:
		# load training data, create TF model
		loader = DataLoader(FilePaths.fnTrain, Model.batchSize,
		                    Model.imgSize, Model.maxTextLen)

		# save characters of model for inference mode
		open(FilePaths.fnCharList, 'w').write(str().join(loader.charList))

		# save words contained in dataset into file
		open(FilePaths.fnCorpus, 'w').write(str(' ').join(
		    loader.trainWords + loader.validationWords))

		# execute training or validation
		if args.train:
			model = Model(loader.charList, decoderType)
			train(model, loader)
		elif args.validate:
			model = Model(loader.charList, decoderType, mustRestore=True)
			validate(model, loader)

	# infer text on test image
	else:
		pass
		model = Model(open(FilePaths.fnCharList).read(),
		              decoderType, mustRestore=True, dump=args.dump)

		infered = recognize(model)
		corrected = correctSpell(infered)
		#corrected = infered
		
		with open(FilePaths.fnResult, "w") as file:
			for word in corrected:
				file.write(word + "\n")


def correctSpell(word_list):
	checker = SpellCheck(word_list) 
	word_list = checker.correction()
	return word_list

def recognize(model):
	infered_words = []
	
	dir = os.listdir(FilePaths.fnImageFolder)
	dir.sort(key=int)
	for image in dir:
		test_img = os.path.join(FilePaths.fnImageFolder, image)
		infered_words.append(infer(model, test_img))

	for filename in os.listdir(FilePaths.fnImageFolder):
		file_path = os.path.join(FilePaths.fnImageFolder, filename)
		try:
			if os.path.isfile(file_path) or os.path.islink(file_path):
				os.unlink(file_path)
			elif os.path.isdir(file_path):
				shutil.rmtree(file_path)
		except Exception as e:
			print('Failed to delete %s. Reason: %s' % (file_path, e))


	
	return infered_words

if __name__ == '__main__':
	main()

