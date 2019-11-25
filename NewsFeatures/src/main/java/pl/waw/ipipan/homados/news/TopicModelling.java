package pl.waw.ipipan.homados.news;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.regex.Pattern;

import cc.mallet.pipe.CharSequence2TokenSequence;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.pipe.TokenSequenceLowercase;
import cc.mallet.pipe.TokenSequenceRemoveStopwords;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.IDSorter;
import cc.mallet.types.InstanceList;

public class TopicModelling {
	private Pipe pipe;
	private Corpus corpus;
	private InstanceList currentInstances;
	private static final String STOPWORDS = "./resources/stopListAdd.txt";
	private static final int ITERATIONS = 1000;

	public TopicModelling(Corpus corpus) {
		this.corpus = corpus;
		pipe = buildPipe();
	}

	private static Pipe buildPipe() {
		ArrayList<Pipe> pipeList = new ArrayList<Pipe>();
		Pattern tokenPattern = Pattern.compile("\\p{L}[\\p{L}\\p{N}_]*");
		pipeList.add(new CharSequence2TokenSequence(tokenPattern));
		pipeList.add(new TokenSequenceLowercase());
		pipeList.add(new TokenSequenceRemoveStopwords(new File(STOPWORDS), "UTF-8", true, false, false));
		pipeList.add(new TokenSequence2FeatureSequence());
		return new SerialPipes(pipeList);
	}

	public ParallelTopicModel createModel(int numTopics) throws IOException {
		ParallelTopicModel model = new ParallelTopicModel(numTopics);
		currentInstances = getInstances();
		model.addInstances(currentInstances);
		model.setNumThreads(2);
		model.setNumIterations(ITERATIONS);
		model.estimate();
		return model;
	}

	private InstanceList getInstances() {
		InstanceList list = new InstanceList(pipe);
		list.addThruPipe(new CorpusInstanceIterator(corpus));
		return list;
	}

	public void printTopics(ParallelTopicModel model) {
		ArrayList<TreeSet<IDSorter>> topicSortedWords = model.getSortedWords();
		for (int topic = 0; topic < model.numTopics; topic++) {
			Iterator<IDSorter> iterator = topicSortedWords.get(topic).iterator();
			int rank = 0;
			System.out.print("TOPIC " + topic + ":");
			while (iterator.hasNext() && rank < 10) {
				IDSorter idCountPair = iterator.next();
				System.out.print("\t" + currentInstances.getAlphabet().lookupObject(idCountPair.getID()));
				rank++;
			}
			System.out.println();
		}
	}

	public void printDocs(ParallelTopicModel model) {
		int[] bestDoc = new int[model.numTopics];
		double[] bestMatch = new double[model.numTopics];
		for (int i = 0; i < currentInstances.size(); ++i) {
			double[] topicDist = model.getTopicProbabilities(i);
			for (int j = 0; j < model.numTopics; ++j)
				if (topicDist[j] > bestMatch[j]) {
					bestMatch[j] = topicDist[j];
					bestDoc[j] = i;
				}
		}
		System.out.println("Best documents:");
		for (int topic = 0; topic < model.numTopics; topic++) {
			System.out.println("TOPIC " + topic + ": " + currentInstances.get(bestDoc[topic]).getName());
		}
	}

	public void outputTopics(ParallelTopicModel model,Path dir) throws IOException {
		BufferedWriter writer=new BufferedWriter(new FileWriter(Paths.get(dir.toString(),"pages.tsv").toFile()));
		for (int i = 0; i < currentInstances.size(); ++i) {
			double[] topicDist = model.getTopicProbabilities(i);
			int bestTopic = 0;
			for (int j = 1; j < topicDist.length; ++j)
				if (topicDist[j] > topicDist[bestTopic])
					bestTopic = j;
			writer.write(currentInstances.get(i).getName() + "\t" + bestTopic+"\n");
		}
		writer.close();
		
		writer=new BufferedWriter(new FileWriter(Paths.get(dir.toString(),"words.tsv").toFile()));
		ArrayList<TreeSet<IDSorter>> topicSortedWords = model.getSortedWords();
		for (int topic = 0; topic < model.numTopics; topic++) {
			Iterator<IDSorter> iterator = topicSortedWords.get(topic).iterator();
			int rank = 0;
			writer.write(""+topic);
			while (iterator.hasNext() && rank < 10) {
				IDSorter idCountPair = iterator.next();
				writer.write("\t" + currentInstances.getAlphabet().lookupObject(idCountPair.getID()));
				rank++;
			}
			writer.newLine();
		}
		writer.close();
	}

}
