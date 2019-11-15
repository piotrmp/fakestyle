# Local setup
dataPath="<DATA_PATH>"
batch_size=64
onGPU=True	
epochs=10
dataset="all.tsv"

# Reading data
exec(open('./functions.py').read())
embeddings=readEmbeddings(dataPath+"/word2vec/GoogleNewsUnigrams.txt")
MAX_SEQUENCE_LENGTH=120
MAX_DOCUMENT_LENGTH=50
(labels,sources,nums,wordsL,contents,documentCV,topicCV,sourceCV,lengthD)=readDocuments(dataPath+"/neural/"+dataset,MAX_SEQUENCE_LENGTH,embeddings,MAX_DOCUMENT_LENGTH)
embeddingMatrix=prepareEmbeddings(embeddings,wordsL)

# Converting to numpy
y=np.asarray(labels,dtype='float32')
allY=np.concatenate((np.expand_dims(1-y,1),np.expand_dims(y,1)),axis=1)
allX=np.array(contents)

# Load models
exec(open('./model.py').read())

# Prepare CV scenarios
documentCV=np.asarray(documentCV,dtype='int32')
topicCV=np.asarray(topicCV,dtype='int32')
sourceCV=np.asarray(sourceCV,dtype='int32')
scenarioCV=sourceCV

result=np.array([[-1,-1]]*len(scenarioCV),dtype='float32')
for folda in range(max(scenarioCV)):
	fold=folda+1
	print("Evaluating on fold "+str(fold)+"...")
	whichTest=np.isin(scenarioCV,fold)
	trainY=allY[~whichTest,]
	develY=allY[whichTest,]
	trainX=allX[~whichTest,]
	develX=allX[whichTest,]
	style1=Style1(embeddingMatrix,2,MAX_SEQUENCE_LENGTH,MAX_DOCUMENT_LENGTH,onGPU)
	mask=style1.getMask(lengthD)
	trainM=np.array(mask)[~whichTest]
	develM=np.array(mask)[whichTest]
	model=style1.getModel()
	model.compile(optimizer=tf.train.AdamOptimizer(),loss="binary_crossentropy",metrics=["accuracy"])
	fit=model.fit([trainX,trainM],trainY, epochs=epochs,validation_data=([develX,develM],develY),batch_size=batch_size)
	predictions=model.predict([develX,develM])
	result[whichTest,:]=predictions

evaluateD(result,allY)

