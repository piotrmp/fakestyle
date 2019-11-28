library(e1071)
library(glmnet)
library(SparseM)
library(Matrix)
library(foreach)
library(doMC)
registerDoMC(8)

foldsRunGLMFiltered=function(folds,allX,allY,thrs){
  preds=rep(NA,length(allY))
  for (i in 1:max(folds)){
    cat("Working on fold ",i,"\n")
    trainX=allX[folds!=i,]
    trainY=allY[folds!=i]
    cat("Filtering data...\n")
    cors=computeCorrelation(trainX,trainY)
    mask=(abs(cors)<thrs)
    cat("Building model...\n")
    model=cv.glmnet(trainX[,!mask],trainY,family="binomial",parallel=TRUE)
    testX=allX[folds==i,]
    cat("Applying model...\n")
    pred=predict(model,testX[,!mask],type="response",s="lambda.1se")
    cat(mean(allY[folds==i]==(pred>0.5)*1),"\n")
    preds[folds==i]=pred
  }
  return(preds)
}

computeCorrelation=function(trainX,trainY){
  cors=foreach (i=1:ncol(trainX),.combine=c)%dopar%{
    cor(trainX[,i],trainY)
  }
  cors[is.na(cors)]=0
  return(cors)
}


path="/path/to/generated/features"
dense=read.table(paste0(path,"train.tsv"),sep = "\t",header=T,quote="",comment.char="")
sparse=as(as.matrix.coo(read.matrix.csr(paste0(path,"train.csr"))$x),"dgCMatrix")
allX=cbind(Matrix(as.matrix(dense[,c(-1,-2,-3)]),sparse=T),sparse)
allY=dense[,1]
allS=dense[,2]
allT=dense[,3]
dense=NULL
sparse=NULL
foldsPath="/path/to/foldsCV.tsv"
foldsA=read.csv(foldsPath,header=TRUE,sep='\t')

k=5
seed=1
folds=foldsA$documentCV
preds1=foldsRunGLMFiltered(folds,allX,allY,0.05)
mean((preds1>0.5)*1==allY)
folds=foldsA$topicCV
preds2=foldsRunGLMFiltered(folds,allX,allY,0.05)
mean((preds2>0.5)*1==allY)
folds=foldsA$sourceCV
preds3=foldsRunGLMFiltered(folds,allX,allY,0.05)
mean((preds3>0.5)*1==allY)

