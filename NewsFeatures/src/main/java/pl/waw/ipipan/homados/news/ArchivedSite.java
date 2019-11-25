package pl.waw.ipipan.homados.news;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.zeroturnaround.zip.ZipUtil;

import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public class ArchivedSite implements Serializable {

	private static final long serialVersionUID = 4310790554935165605L;
	private String domain;
	private ArchivedPage mainPage;
	private Boolean fake;
	private Map<String, ArchivedPage> numberedPages;


	public ArchivedSite(String urlString) throws MalformedURLException {
		if (urlString.endsWith(":80/"))
			urlString = urlString.substring(0, urlString.length() - ":80/".length()) + "/";
		mainPage = new ArchivedPage(urlString);
		domain = mainPage.getOriginalURL().getAuthority();
		numberedPages = new HashMap<String, ArchivedPage>();
	}

	public static ArchivedSite readFromZip(File zipFile, boolean readContent) throws IOException {
		ArchivedSite result = null;
		byte[] listBytes = ZipUtil.unpackEntry(zipFile, "list.tsv");
		BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(listBytes)));
		String line = null;
		while ((line = reader.readLine()) != null) {
			String[] parts = line.split("\t");
			String id = parts[0];
			String archAddress = parts[2];
			ArchivedPage page = new ArchivedPage(archAddress);
			if (readContent) {
				byte[] txtBytes = ZipUtil.unpackEntry(zipFile, "page" + id + ".txt");
				page.setContent(new String(txtBytes));
			}
			page.setNumber(id);
			if (id.equals("1")) {
				result = new ArchivedSite(archAddress);
				result.mainPage = page;
			}
			result.numberedPages.put(id, page);
			page.setSite(result);
		}
		reader.close();
		return result;
	}

	public String getDomain() {
		return domain;
	}

	public Collection<ArchivedPage> getAllPages() {
		return numberedPages.values();
	}

	public void setFake(boolean fake) {
		this.fake = fake;
	}

	public boolean getFake() {
		return fake;
	}

	public void annotate() {
		Properties props = new Properties();
		props.setProperty("annotators", "tokenize,ssplit,pos,lemma");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
		int counter = 0;
		Collection<ArchivedPage> pages = getAllPages();
		for (ArchivedPage page : pages) {
			System.out.println("Annotating page " + (++counter) + " / " + pages.size() + " : " + page.getOriginalURL());
			CoreDocument document = new CoreDocument(page.getContent());
			pipeline.annotate(document);
			page.setAnnotation(document);
		}
	}

	public void deAnnotate() {
		for (ArchivedPage page : getAllPages()) {
			page.setAnnotation(null);
		}
	}

	public void saveContentToBinary(Path path) throws IOException {
		Map<String, PageContentStruct> contents = new HashMap<String, PageContentStruct>();
		for (ArchivedPage page : getAllPages()) {
			contents.put(page.getNumber(), page.getContentsStruct());
		}
		ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(path.toFile()));
		stream.writeObject(contents);
		stream.close();
	}

	public void removeContent() {
		for (ArchivedPage page : getAllPages())
			page.removeContent();
	}

	public void extractTagsFromAnnotation() {
		for (ArchivedPage page : getAllPages())
			page.extractTagsFromAnnotation();
	}

	@SuppressWarnings("unchecked")
	public void readContentFromBinary(Path path) throws IOException {
		ObjectInputStream stream = new ObjectInputStream(new FileInputStream(path.toFile()));
		Map<String, PageContentStruct> contents;
		try {
			contents = (Map<String, PageContentStruct>) stream.readObject();
			stream.close();
			for (String key : numberedPages.keySet()) {
				if (!contents.containsKey(key))
					System.out.println("Key " + key + " missing in archive");
				else
					numberedPages.get(key).setContentsStruct(contents.get(key));
			}
		} catch (ClassNotFoundException e) {
			stream.close();
			throw new IOException(e);
		}

	}
}
