# HOMADOS: Capturing the Style of Fake News

This repository contains resources for the article *[Capturing the Style of Fake News](https://home.ipipan.waw.pl/p.przybyla/bib/capturing.pdf)* presented at the *AI for Social Impact* track at the [AAAI 2020](https://aaai.org/Conferences/AAAI-20/) in New York. The research was done within the [HOMADOS](https://homados.ipipan.waw.pl/) project at the [Institute of Computer Science](https://ipipan.waw.pl/), Polish Academy of Sciences.

The resources available here are the following:
* a corpus including credible and non-credible (*fake*) news documents,
* a code for credibility classifier based on stylometric features,
* a code for credibility classifier based on neural networks.

If you need any more information consult [the paper](https://home.ipipan.waw.pl/p.przybyla/bib/capturing.pdf) or contact the author! 

## News Style Corpus
The corpus generated in this research contains 103,219 documents from 18 credible and 205 non-credible sources selected based on work of [PolitiFact](https://www.politifact.com/punditfact/article/2017/apr/20/politifacts-guide-fake-news-websites-and-what-they/) and [Pew Research Center](https://www.journalism.org/2014/10/21/political-polarization-media-habits/).

The folder [NewsStyleCorpus](NewsStyleCorpus) contains the following files necessary to retrieve the pages constituting the corpus from the *[WayBackMachine](https://web.archive.org/)* archive:
* `corpusSources.tsv`: tab-separated list of all documents in the corpus, each with the website (domain) it comes from and its credibility label, original page URL and the address, under which the document is currently available at the archive,
* `CredibilityCorpusDownloader.java`: a sample code in Java that retrieves HTML documents from the given address list and converts them to plain text, following the procedure described in the article,
* `foldsCV.tsv`: a list of fold identifiers for the documents from `corpusSources.tsv` (in the same order) for three CV scenarios described in the paper: document-based, topic-based and source-based.

Downloading the whole corpus takes several hours. In order to limit the load on the *WayBackMachine* infrastructure and retrieve all the pages (some may be temporarily unavailable), you should perform the process in stages. You can select just part of the corpus for download by modifying the address list.

## Stylometric Classifier
The implementation of the stylometric classifier is available in three folders:
* `NewsFeatures` generating the stylometric features described in the paper for a given text corpus, 


## BiLSTMAvg
The folder [BiLSTMAvg](BiLSTMAvg) contains source code of the document-averaged BiLSTM neural network. The following files are included:
* `model.py` with the BiLSTMAvg model implemented in Keras,
* `functions.py` with utility functions,
* `run.py` showing how to use the above to replicate the cross-validation evaluation as shown in the article.

The code was tested on Python 3.6.8 with TensorFlow 1.14. Java code for converting the News Style Corpus to a format used by BiLSTMAvg or BERT baseline is uploaded as `DataConversion.java`.

The model uses word2vec embeddings [trained on Google News corpus](https://code.google.com/archive/p/word2vec/). You can download them [here](https://home.ipipan.waw.pl/p.przybyla/GoogleNewsUnigrams.zip) or use your own method for token representation.





