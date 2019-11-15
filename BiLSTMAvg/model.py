import numpy as np
import tensorflow as tf
from tensorflow import keras
from tensorflow.keras.models import Model


class Style1():
	def __init__(self,embeddingMatrix,labelsNum,maxSequenceLength,maxDocumentLength,onGPU):
		self.maxSequenceLength=maxSequenceLength
		self.maxDocumentLength=maxDocumentLength
		self.labelsNum=labelsNum
		self.embeddingSize=np.shape(embeddingMatrix)[1]
		self.embL=keras.layers.Embedding(np.shape(embeddingMatrix)[0],self.embeddingSize,input_length=maxSequenceLength,weights=[embeddingMatrix],trainable=False)
		self.reshape1L=keras.layers.Lambda(self.backend_reshape,output_shape=(maxSequenceLength,self.embeddingSize))
		if onGPU:
			self.LSTMforL=keras.layers.CuDNNLSTM(units=100,go_backwards=False,return_sequences=False)
			self.LSTMrevL=keras.layers.CuDNNLSTM(units=100,go_backwards=True,return_sequences=False)
		else:
			self.LSTMforL=keras.layers.LSTM(units=100,go_backwards=False,return_sequences=False)
			self.LSTMrevL=keras.layers.LSTM(units=100,go_backwards=True,return_sequences=False)
		self.conL=keras.layers.Concatenate(axis=1)
		self.denseL=keras.layers.Dense(labelsNum,activation="softmax")
		self.reshape2L=keras.layers.Lambda(self.backend_reshape2,output_shape=(maxDocumentLength,labelsNum))
		self.multiply=keras.layers.Multiply()
		self.poolingL=keras.layers.GlobalAveragePooling1D()

	def backend_reshape(self,x):
		return keras.backend.reshape(x,(-1,self.maxSequenceLength,self.embeddingSize))

	def backend_reshape2(self,x):
		return keras.backend.reshape(x,(-1,self.maxDocumentLength,self.labelsNum))

	def getMask(self,lengthD):
		return (lengthToAverageMask(lengthD,self.maxDocumentLength,binary=False))

	def getModel(self):
		inputWords = keras.layers.Input(shape=(self.maxDocumentLength,self.maxSequenceLength,))
		inputMask = keras.layers.Input(shape=(self.maxDocumentLength,self.labelsNum,))
		x=self.embL(inputWords)
		x=self.reshape1L(x)
		lstm1=self.LSTMforL(x)
		lstm2=self.LSTMrevL(x)
		x=self.conL([lstm1,lstm2])
		Ps=self.denseL(x)
		Ps=self.reshape2L(Ps)
		Ps=self.multiply([Ps,inputMask])
		Ps=self.poolingL(Ps)
		model=Model(inputs=[inputWords,inputMask], outputs=Ps)
		return(model)
