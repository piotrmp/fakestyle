# HOMADOS: Capturing the Style of Fake News

This repository contains resources for the article *[Capturing the Style of Fake News](https://ojs.aaai.org//index.php/AAAI/article/view/5386)* presented at the *AI for Social Impact* track at the [AAAI 2020](https://aaai.org/Conferences/AAAI-20/) in New York. The research was done within the [HOMADOS](https://homados.ipipan.waw.pl/) project at the [Institute of Computer Science](https://ipipan.waw.pl/), Polish Academy of Sciences.

The resources available here are the following:
* a corpus including credible and non-credible (*fake*) news documents,
* a code for credibility classifier based on stylometric features,
* a code for credibility classifier based on neural networks.

If you need any more information consult [the paper](https://ojs.aaai.org//index.php/AAAI/article/view/5386) or contact its author! 

## News Style Corpus

**NOTE: A new and improved version (v2.0) of this corpus was developed to create the *Credibilator* browser extension and is available in [its repository](https://github.com/piotrmp/credibilator).**

The corpus generated in this research contains 103,219 documents from 18 credible and 205 non-credible sources selected based on work of [PolitiFact](https://www.politifact.com/punditfact/article/2017/apr/20/politifacts-guide-fake-news-websites-and-what-they/) and [Pew Research Center](https://www.journalism.org/2014/10/21/political-polarization-media-habits/).

The folder [NewsStyleCorpus](NewsStyleCorpus) contains the following files necessary to retrieve the pages constituting the corpus from the *[WayBackMachine](https://web.archive.org/)* archive:
* `corpusSources.tsv`: tab-separated list of all documents in the corpus, each with the website (domain) it comes from and its credibility label, original page URL and the address, under which the document is currently available at the archive,
* `CredibilityCorpusDownloader.java`: a sample code in Java that retrieves HTML documents from the given address list and converts them to plain text, following the procedure described in the article,
* `foldsCV.tsv`: a list of fold identifiers for the documents from `corpusSources.tsv` (in the same order) for three CV scenarios described in the paper: document-based, topic-based and source-based.

Downloading the whole corpus takes several hours. In order to limit the load on the *WayBackMachine* infrastructure and retrieve all the pages (some may be temporarily unavailable), you should perform the process in stages. You can select just part of the corpus for download by modifying the address list.

## Stylometric Classifier
The implementation of the stylometric classifier is available in two folders:
* `NewsFeatures` is a Java application for generating the stylometric features (through the `Main.main()` procedure) for a given text corpus. It uses [Stanford CoreNLP](https://stanfordnlp.github.io/CoreNLP/) and an extended version of [General Inquirer](http://www.wjh.harvard.edu/~inquirer/spreadsheet_guide.htm) word list, to be found in `NewsFeatures/resources`.
* `R`, including a script in R for building a glmnet model on the generated features and performing evaluation according to the CV scenarios.


## BiLSTMAvg
The folder [BiLSTMAvg](BiLSTMAvg) contains source code of the document-averaged BiLSTM neural network. The following files are included:
* `model.py` with the BiLSTMAvg model implemented in TensorFlow/Keras,
* `functions.py` with utility functions,
* `run.py` showing how to use the above to replicate the cross-validation evaluation as shown in the article.

The code was tested on Python 3.6.8 with TensorFlow 1.14. Java code for converting the News Style Corpus to a format used by BiLSTMAvg or BERT baseline is uploaded as `DataConversion.java`.

The model uses word2vec embeddings [trained on Google News corpus](https://code.google.com/archive/p/word2vec/). You can download them [here](https://home.ipipan.waw.pl/p.przybyla/GoogleNewsUnigrams.zip) or use your own method for token representation.

## Licence
* The corpus data are released under the [CC BY-NC-SA 4.0](https://creativecommons.org/licenses/by-nc-sa/4.0/) licence.
* The code is released under the [GNU GPL 3.0](https://www.gnu.org/licenses/gpl-3.0.html) licence.
* The extended GI dictionary is based on the original General Inquirer dictionary; see its [page](http://www.wjh.harvard.edu/~inquirer/spreadsheet_guide.htm) for copyright information.




