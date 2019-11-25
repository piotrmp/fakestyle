package pl.waw.ipipan.homados.news.features;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import pl.waw.ipipan.homados.news.ArchivedPage;

public class GeneralInquirerRecogniserExtended{
	private SortedMap<String, Integer> categories;
	private Map<String, boolean[]> words;

	public GeneralInquirerRecogniserExtended(Path file) throws IOException {
		categories = new TreeMap<String, Integer>();
		words = new HashMap<String, boolean[]>();
		BufferedReader reader = new BufferedReader(new FileReader(file.toFile()));
		String line = null;
		int counter = 0;
		while ((line = reader.readLine()) != null) {
			String[] parts = line.split("\t", -1);
			String category = parts[0];
			categories.put(category, counter++);
		}
		reader.close();
		reader = new BufferedReader(new FileReader(file.toFile()));
		while ((line = reader.readLine()) != null) {
			String[] parts = line.split("\t", -1);
			String category = parts[0];
			for (int i = 1; i < parts.length; ++i) {
				String word = parts[i].toLowerCase();
				if (word.equals(""))
					break;
				if (!words.containsKey(word))
					words.put(word, new boolean[categories.size()]);
				words.get(word)[categories.get(category)] = true;
			}
		}
		reader.close();
		System.out.println("Have " + words.size() + " words of " + categories.size() + " categories.");
	}

	public List<String> featureNames() {
		List<String> result = new LinkedList<String>();
		for (String category : categories.keySet())
			result.add("GI_" + category);
		result.add("GI_total");
		return result;
	}

	public double[] featureValues(ArchivedPage page) {
		int totalCount = 0;
		int[] counts = new int[categories.size() + 1];
		for (int a1 = 0; a1 < page.getTokens().length; ++a1) {
			for (int a2 = 0; a2 < page.getTokens()[a1].length; ++a2) {
				totalCount++;
				String lemma = page.getLemmata()[a1][a2];
				boolean found = false;	
				String word;
				word = lemma.toLowerCase();
				if (words.containsKey(word)) {
					for (int i = 0; i < words.get(word).length; ++i)
						if (words.get(word)[i]) {
							counts[i]++;
							found = true;
						}
				}
				if (found)
					counts[counts.length - 1]++;
			}
		}
		if (counts[counts.length - 1] == 0)
			counts[counts.length - 1]++;

		double[] freqs = new double[counts.length];
		int i = 0;
		for (String category : categories.keySet()) {
			freqs[i] = counts[categories.get(category)] * 1.0 / counts[counts.length - 1];
			i++;
		}
		freqs[counts.length - 1] = counts[counts.length - 1] * 1.0 / totalCount;
		return freqs;
	}

	public List<String> forWord(String word) {
		List<String> result = new LinkedList<String>();
		if (words.containsKey(word)) {
			boolean[] bools = words.get(word);
			for (int i = 0; i < bools.length; ++i)
				if (bools[i])
					for (String key : categories.keySet())
						if (categories.get(key) == i)
							result.add(key);
		}
		return result;
	}
}
