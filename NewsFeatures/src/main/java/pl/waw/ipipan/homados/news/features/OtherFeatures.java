package pl.waw.ipipan.homados.news.features;

import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import pl.waw.ipipan.homados.news.ArchivedPage;

public class OtherFeatures {

	private GeneralInquirerRecogniserExtended gi;

	public OtherFeatures(GeneralInquirerRecogniserExtended gi) {
		this.gi = gi;
	}

	public SortedMap<String, Double> compute(ArchivedPage page) {
		SortedMap<String, Double> result = new TreeMap<String, Double>();
		int lengthAll = 0;
		int wordsNum = 0;
		int sentenceNum = 0;

		int letterWords = 0;
		int wordsCased = 0;
		int wordsCASED = 0;
		int wordscased = 0;
		int wordsCaSeD = 0;
		for (String[] sentence : page.getTokens()) {
			sentenceNum++;
			for (String token : sentence) {
				wordsNum++;
				lengthAll += token.length();
				if (isWord(token) && token.length() > 1) {
					letterWords++;
					if (token.equals(token.toLowerCase()))
						wordscased++;
					else if (token.equals(token.toUpperCase()))
						wordsCASED++;
					else if (token.substring(1).equals(token.substring(1).toLowerCase()))
						wordsCased++;
					else
						wordsCaSeD++;
				}
			}
		}
		result.put("sentenceNum", sentenceNum * 1.0);
		result.put("meanWordLength", lengthAll * 1.0 / wordsNum);
		result.put("meanSentenceLength", wordsNum * 1.0 / sentenceNum);
		result.put("wordscased", wordscased * 1.0 / letterWords);
		result.put("wordsCased", wordsCased * 1.0 / letterWords);
		result.put("wordsCASED", wordsCASED * 1.0 / letterWords);
		result.put("wordsCaSeD", wordsCaSeD * 1.0 / letterWords);
		List<String> giFeatures=gi.featureNames();
		double[] giValues=gi.featureValues(page);
		for (int i=0;i<giFeatures.size();++i)
			result.put(giFeatures.get(i), giValues[i]);
		return result;
	}

	private static boolean isWord(String token) {
		token = token.replaceAll("’", "'");
		if (token.equals("'s") || token.equals("n't") || token.equals("'ve"))
			return true;
		for (int i = 0; i < token.length(); ++i)
			if (!Character.isAlphabetic(token.charAt(i)))
				return false;
		return true;
	}

	public List<String> names() {
		List<String> result=new LinkedList<String>();
		result.add("sentenceNum");
		result.add("meanWordLength");
		result.add("meanSentenceLength");
		result.add("wordscased");
		result.add("wordsCased");
		result.add("wordsCASED");
		result.add("wordsCaSeD");
		result.addAll(gi.featureNames());
		return result;
	}

}
