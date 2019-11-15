import numpy as np

def readData(dataFile,maxSequenceLength,embeddings):
	labels=[]
	sources=[]
	wordsD={'<PAD>':0,'<UNK>':1}
	wordsL=['<PAD>','<UNK>']
	contents=[]
	nums=[]
	documentCVs=[]
	topicCVs=[]
	sourceCVs=[]
	fileIn=open(dataFile,'r')
	while True:
		line=fileIn.readline()
		if line=='':
			break
		parts=line.strip().split('\t')
		label=parts[0]
		source=parts[1]
		num=parts[2]
		documentCV=parts[3]
		topicCV=parts[4]
		sourceCV=parts[5]
		contentWords=parts[6:]
		if len(contentWords)>maxSequenceLength:
			contentWords=contentWords[0:maxSequenceLength]
		content=[]
		for contentWord in contentWords:
			if not contentWord in embeddings:
				contentWord='<UNK>'
			elif not contentWord in wordsD:
				wordsD[contentWord]=len(wordsD)
				wordsL.append(contentWord)
			content.append(wordsD[contentWord])
		if len(content)<maxSequenceLength:
			content=content+[0]*(maxSequenceLength-len(content))	
		labels.append(label)
		sources.append(source)
		contents.append(content)
		nums.append(num)
		documentCVs.append(documentCV)
		topicCVs.append(topicCV)
		sourceCVs.append(sourceCV)
	fileIn.close()
	return(labels,sources,nums,wordsL,contents,documentCVs,topicCVs,sourceCVs)


def readDocuments(dataFile,maxSequenceLength,embeddings,maxDocumentLength):
	(labels,sources,nums,wordsL,contents,documentCVs,topicCVs,sourceCVs)=readData(dataFile,maxSequenceLength,embeddings)
	labelsD=[]
	sourcesD=[]
	numsD=[]
	contentsD=[]
	documentCVsD=[]
	topicCVsD=[]
	sourceCVsD=[]
	lengthsD=[]
	i=0
	contentHere=[]
	while(True):
		contentHere.append(contents[i])
		if i==len(nums)-1 or nums[i+1]!=nums[i]:
			labelsD.append(labels[i])
			sourcesD.append(sources[i])
			numsD.append(nums[i])
			documentCVsD.append(documentCVs[i])
			topicCVsD.append(topicCVs[i])
			sourceCVsD.append(sourceCVs[i])
			if len(contentHere)<maxDocumentLength:
				lengthHere=len(contentHere)
				for j in range(maxDocumentLength-len(contentHere)):
					contentHere.append([0]*maxSequenceLength)
			else:
				lengthHere=maxDocumentLength
				contentHere=contentHere[0:maxDocumentLength]
			lengthsD.append(lengthHere)
			contentsD.append(contentHere)
			contentHere=[]
		i=i+1
		if i==len(nums):
			break
	return(labelsD,sourcesD,numsD,wordsL,contentsD,documentCVsD,topicCVsD,sourceCVsD,lengthsD)


def readEmbeddings(vectorsFile):
	embeddings={}
	for line in open(vectorsFile):
		parts=line.split()
		if len(parts)!=301:
			continue
		coefs=np.asarray(parts[1:],dtype='float32')
		embeddings[parts[0]]=coefs
	return(embeddings)

def prepareEmbeddings(embeddings,wordsL):
	embeddingMatrix = np.zeros((len(wordsL), 300))
	for i in range(len(wordsL)):
		row=np.zeros(300)
		word=wordsL[i]
		if word!='<PAD>' and word!='<UNK>':
			row=embeddings[word]
			row=row/np.sqrt(sum(row*row))
		embeddingMatrix[i]=row
	return(embeddingMatrix)

def lengthToAverageMask(lengthD,maxDocumentLength,binary=False):
	result=[]
	for i in range(len(lengthD)):
		if binary:
			multiplier=1.0
		else:
			multiplier=maxDocumentLength*1.0/lengthD[i]
		vec=[multiplier,multiplier]
		vec0=[0.0,0.0]
		vector=[vec]*lengthD[i]+[vec0]*(maxDocumentLength-lengthD[i])
		result.append(vector)
	return(np.array(result))

	
def evaluateD(resultPred,resultTrue):
	return(np.mean((resultPred[:,1]>0.5)==(resultTrue[:,1]==1)))

