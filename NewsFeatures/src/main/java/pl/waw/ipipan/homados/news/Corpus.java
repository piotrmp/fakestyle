package pl.waw.ipipan.homados.news;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Corpus {

	private Path annotationCache;
	private List<ArchivedSite> sites;
	private List<ArchivedPage> trainSet;
	private List<ArchivedPage> devSet;
	private List<ArchivedPage> testSet;

	public Corpus(File directory) throws IOException {
		this(directory, true);
	}

	private Corpus(File directory, boolean readContent) throws IOException {
		sites = new LinkedList<ArchivedSite>();
		Path path = Paths.get(directory.getAbsolutePath(), "summary.tsv");
		int counter = 0;
		for (String line : Files.readAllLines(path)) {
			String domain = line.split("\t")[0];
			String fake = line.split("\t")[1];
			File file = Paths.get(directory.getAbsolutePath(), domain + ".zip").toFile();
			ArchivedSite site = ArchivedSite.readFromZip(file, readContent);
			if (fake.equals("1"))
				site.setFake(true);
			else if (fake.equals("0"))
				site.setFake(false);
			sites.add(site);
			counter++;
			System.out.println("Read " + site.getDomain());
		}
		System.out.println("Read " + counter + " sites.");
	}

	public Corpus(File file, Path path) throws IOException {
		this(file, false);
		annotationCache = path;
	}

	public void allTrain() {
		trainSet = new LinkedList<ArchivedPage>();
		devSet = new LinkedList<ArchivedPage>();
		testSet = new LinkedList<ArchivedPage>();
		for (ArchivedSite site : sites)
			for (ArchivedPage page : site.getAllPages())
				trainSet.add(page);
	}

	public int annotateAndSave(Path cache) throws MalformedURLException, ClassNotFoundException, ClassCastException, IOException {
		int counter = 0;
		for (ArchivedSite site : sites) {
			System.out.println("Annotating " + site.getDomain());
			site.annotate();
			site.extractTagsFromAnnotation();
			site.deAnnotate();
			site.saveContentToBinary(Paths.get(cache.toString(), site.getDomain() + ".bin"));
			site.removeContent();
		}
		return counter;
	}

	public List<ArchivedPage> getTrainSet() {
		return trainSet;
	}

	public List<ArchivedPage> getTestSet() {
		return testSet;
	}

	public List<ArchivedPage> getDevSet() {
		return devSet;
	}

	public List<ArchivedSite> getSites() {
		return sites;
	}

	public void readContents(ArchivedSite site) throws IOException {
		site.readContentFromBinary(Paths.get(annotationCache.toString(), site.getDomain() + ".bin"));
	}

	public void readTopics(File file) throws IOException {
		Map<String, Map<String, Integer>> map = new HashMap<String, Map<String, Integer>>();
		for (String line:Files.readAllLines(Paths.get(file.toString()))) {
			String[] parts=line.split("\t");
			int topic=Integer.parseInt(parts[1]);
			parts=parts[0].split("/");
			String domain=parts[0];
			String number=parts[1];
			if (!map.containsKey(domain))
				map.put(domain, new HashMap<String, Integer>());
			map.get(domain).put(number, topic);
		}
		for (ArchivedSite site:sites)
			for (ArchivedPage page:site.getAllPages())
				page.setTopic(map.get(site.getDomain()).get(page.getNumber()));
	}
}
