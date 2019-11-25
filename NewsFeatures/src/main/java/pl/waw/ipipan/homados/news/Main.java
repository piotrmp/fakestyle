package pl.waw.ipipan.homados.news;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import cc.mallet.topics.ParallelTopicModel;
import pl.waw.ipipan.homados.news.features.BagOfWordsGenerator;
import pl.waw.ipipan.homados.news.features.GeneralInquirerRecogniserExtended;
import pl.waw.ipipan.homados.news.features.OtherFeatures;

public class Main {
	public static void main(String[] args) throws IOException, ClassNotFoundException, ClassCastException {
		String corpusPath = args[0];
		String topicsPath = args[1];
		String annotationCachePath = args[2];
		String giExtendedPath = args[3];
		String outPath = args[4];
		mainTopics(corpusPath, topicsPath);
		mainAnnotate(corpusPath, annotationCachePath);
		mainFeatures(corpusPath, annotationCachePath, topicsPath, giExtendedPath, outPath);
	}

	private static void mainTopics(String corpusPath, String topicsPath) throws IOException {
		Corpus corpus = new Corpus(new File(corpusPath));
		corpus.allTrain();
		TopicModelling lda = new TopicModelling(corpus);
		ParallelTopicModel model = lda.createModel(100);
		lda.printTopics(model);
		lda.printDocs(model);
		lda.outputTopics(model, Paths.get(topicsPath));
	}

	public static void mainAnnotate(String corpusPath, String annotationCachePath) throws IOException, ClassNotFoundException, ClassCastException {
		Corpus corpus = new Corpus(new File(corpusPath));
		int result = corpus.annotateAndSave(Paths.get(annotationCachePath));
		System.out.println("Added tags: " + result);
	}

	public static void mainFeatures(String corpusPath, String annotationCachePath, String topicsPath, String giExtendedPath, String outPath)
			throws IOException, ClassNotFoundException, ClassCastException {
		Corpus corpus = new Corpus(new File(corpusPath), Paths.get(annotationCachePath));
		corpus.readTopics(new File(topicsPath + "pages.tsv"));
		corpus.allTrain();
		GeneralInquirerRecogniserExtended gi = new GeneralInquirerRecogniserExtended(Paths.get(giExtendedPath));
		BagOfWordsGenerator bow = new BagOfWordsGenerator(new OtherFeatures(gi));
		bow.consume(corpus);
		bow.output(corpus, new File(outPath));
	}

}
