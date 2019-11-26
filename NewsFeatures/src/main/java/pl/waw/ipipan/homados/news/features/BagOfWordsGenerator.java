package pl.waw.ipipan.homados.news.features;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import pl.waw.ipipan.homados.news.ArchivedPage;
import pl.waw.ipipan.homados.news.ArchivedSite;
import pl.waw.ipipan.homados.news.Corpus;

public class BagOfWordsGenerator {
	private static final int MIN = 5;

	private Map<String, Integer> lexicon;
	private String[] lexemes;

	private OtherFeatures features;

	public BagOfWordsGenerator(OtherFeatures features) {
		lexicon = new HashMap<String, Integer>();
		this.features = features;
	}

	public void consume(Corpus corpus) throws IOException {
		collectLexemes(corpus);
		System.out.println("Have " + lexicon.size() + " lexemes.");
		filterLexemes();
		System.out.println("Have " + lexicon.size() + " lexemes.");
	}

	private void filterLexemes() {
		int counter = 0;
		Map<String, Integer> newLexicon = new HashMap<String, Integer>();
		for (String key : lexicon.keySet())
			if (lexicon.get(key) >= MIN)
				newLexicon.put(key, ++counter);
		lexicon = newLexicon;
		lexemes = new String[lexicon.size() + 1];
		for (String key : lexicon.keySet())
			lexemes[lexicon.get(key)] = key;
	}

	private void collectLexemes(Corpus corpus) throws IOException {
		for (ArchivedSite site : corpus.getSites()) {
			System.out.println("Previewing contents of " + site.getDomain());
			corpus.readContents(site);
			for (ArchivedPage page : corpus.getTrainSet())
				if (page.getSite() == site) {
					Set<String> lexemesHere = countLexemes(page, null);
					for (String lexeme : lexemesHere)
						if (!lexicon.containsKey(lexeme))
							lexicon.put(lexeme, 1);
						else
							lexicon.put(lexeme, lexicon.get(lexeme) + 1);
				}
			site.removeContent();
		}
	}

	private static Set<String> countLexemes(ArchivedPage page, Map<String, Integer> counts) {
		Set<String> result = new HashSet<String>();
		List<String> lexemes;
		lexemes = posTrigrams(page);
		for (String lexeme : lexemes) {
			result.add(lexeme);
			if (counts != null)
				if (!counts.containsKey(lexeme))
					counts.put(lexeme, 1);
				else
					counts.put(lexeme, counts.get(lexeme) + 1);
		}
		return result;
	}

	private static List<String> posTrigrams(ArchivedPage page) {
		List<String> result = new LinkedList<String>();
		for (String[] tagSentence : page.getTags()) {
			for (int i = 0; i < tagSentence.length; ++i) {
				String thisTag = tagSentence[i];
				result.add("TAG:" + thisTag);
				String prevTag = null;
				if (i > 0) {
					prevTag = tagSentence[i - 1];
					result.add("TAG:" + prevTag + "_" + thisTag);
				}
				String prevprevTag = null;
				if (i > 1) {
					prevprevTag = tagSentence[i - 2];
					result.add("TAG:" + prevprevTag + "_" + prevTag + "_" + thisTag);
				}
			}

		}
		return result;
	}

	public void output(Corpus corpus, File dir, Path inputFile) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(Paths.get(dir.getPath(), "words.tsv").toFile()));
		for (int i = 1; i < lexemes.length; ++i)
			bw.write(lexemes[i] + "\t" + i + "\n");
		bw.close();
		List<String> otherFeatureNames = new LinkedList<String>();
		if (features != null)
			otherFeatureNames = features.names();
		@SuppressWarnings("rawtypes")
		List[] sets = { corpus.getTrainSet(), corpus.getTestSet(), corpus.getDevSet() };
		Path[] pathsSparse = { Paths.get(dir.getPath(), "train.csr"), Paths.get(dir.getPath(), "test.csr"), Paths.get(dir.getPath(), "dev.csr") };
		Path[] pathsDense = { Paths.get(dir.getPath(), "train.tsv"), Paths.get(dir.getPath(), "test.tsv"), Paths.get(dir.getPath(), "dev.tsv") };
		BufferedWriter writerSparse[] = new BufferedWriter[pathsSparse.length];
		BufferedWriter writerDense[] = new BufferedWriter[pathsDense.length];
		for (int i = 0; i < sets.length; ++i) {
			writerSparse[i] = new BufferedWriter(new FileWriter(pathsSparse[i].toFile()));
			writerDense[i] = new BufferedWriter(new FileWriter(pathsDense[i].toFile()));
			writerDense[i].write("y\tsource\ttopic");
			for (int j = 0; j < otherFeatureNames.size(); ++j)
				writerDense[i].write("\t" + otherFeatureNames.get(j));
			writerDense[i].write("\n");
		}
		ArchivedSite currentSite = null;
		for (String line : Files.readAllLines(inputFile)) {
			String[] parts = line.split("\t");
			String domain = parts[1].strip();
			if (!domain.contains("."))
				continue;
			if (currentSite == null || !currentSite.getDomain().equals(domain)) {
				if (currentSite != null) {
					currentSite.removeContent();
					//System.out.println("Finished with " + currentSite.getDomain());
					currentSite = null;
				}
				for (ArchivedSite site : corpus.getSites())
					if (site.getDomain().equals(domain)) {
						currentSite = site;
						break;
					}
				if (currentSite == null) {
					System.out.println("Skipping unknown domain " + domain);
					continue;
				}
				corpus.readContents(currentSite);
				System.out.println("Printing features of " + currentSite.getDomain());
			}
			String pageNo = parts[2];
			ArchivedPage page = currentSite.getNumberedPage(pageNo);
			for (int i = 0; i < sets.length; ++i)
				if (sets[i].contains(page)) {
					Map<String, Integer> counts = new HashMap<String, Integer>();
					countLexemes(page, counts);
					SortedMap<Integer, Integer> sortedCounts = sortCounts(counts);
					int length = getLength(page);
					writerSparse[i].write(page.getFake() ? "1" : "0");
					for (Integer key : sortedCounts.keySet())
						writerSparse[i].write(" " + key + ":" + (sortedCounts.get(key) * 1.0 / length));
					writerSparse[i].newLine();
					Map<String, Double> otherFeatures = null;
					if (features != null)
						otherFeatures = features.compute(page);
					writerDense[i].write(page.getFake() ? "1" : "0");
					writerDense[i].write("\t" + page.getDomain());
					writerDense[i].write("\t" + page.getTopic());
					for (int j = 0; j < otherFeatureNames.size(); ++j)
						writerDense[i].write("\t" + otherFeatures.get(otherFeatureNames.get(j)));
					writerDense[i].newLine();
				}
		}
		if (currentSite != null)
			currentSite.removeContent();
		for (int i = 0; i < sets.length; ++i) {
			writerSparse[i].close();
			writerDense[i].close();
		}
	}

	private SortedMap<Integer, Integer> sortCounts(Map<String, Integer> counts) {
		SortedMap<Integer, Integer> result = new TreeMap<Integer, Integer>();
		for (String key : counts.keySet())
			if (lexicon.containsKey(key))
				result.put(lexicon.get(key), counts.get(key));
		return result;
	}

	private static int getLength(ArchivedPage page) {
		int result = 0;
		for (String[] poss : page.getTags())
			result += poss.length;
		return result;
	}

}
