package pl.waw.ipipan.homados.news;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;

public class ArchivedPage implements Serializable {

	private static final long serialVersionUID = 7429684348861243845L;
	private URL archivedURL;
	private URL originalURL;
	private String date;
	private String domain;
	private String body;
	private static final String prefix = "https://web.archive.org/web/";

	private String title;

	private String content;
	private CoreDocument annotation;
	private String[][] tags;
	private String[][] lemmata;
	private String[][] tokens;

	private String number;
	private ArchivedSite site;
	
	private int topic;
	
	public ArchivedPage(String urlString) throws MalformedURLException {
		URL url = new URL(urlString);
		if (url.getRef() != null) {
			url = new URL(urlString.substring(0, urlString.length() - url.getRef().length() - 1));
		}
		archivedURL = url;
		String string = archivedURL.toString();
		if (!string.startsWith(prefix))
			throw new MalformedURLException();
		string = string.substring(prefix.length());
		date = string.split("/")[0];
		string = string.substring(date.length() + 1);
		String[] parts = string.split("/");
		if (parts.length < 3)
			throw new MalformedURLException();
		domain = parts[2];
		if (domain.endsWith(":80")) {
			string = string.replace(domain, domain.substring(0, domain.length() - ":80".length()));
			domain = domain.substring(0, domain.length() - ":80".length());
		}
		originalURL = new URL(string);
	}

	public URL getArchivedURL() {
		return archivedURL;
	}

	public URL getOriginalURL() {
		return originalURL;
	}

	public String getDomain() {
		return domain;
	}

	public String getDate() {
		return date;
	}

	public String toString() {
		return archivedURL.toString();
	}

	public void addBody(String body) {
		this.body = body;
	}

	public String getBody() {
		return body;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getTitle() {
		return title;
	}

	public void setAnnotation(CoreDocument document) {
		this.annotation = document;
	}

	public CoreDocument getAnnotation() {
		return annotation;
	}

	public int extractTagsFromAnnotation() {
		int counter = 0;
		tags = new String[annotation.sentences().size()][];
		lemmata = new String[annotation.sentences().size()][];
		tokens = new String[annotation.sentences().size()][];
		for (int i = 0; i < tags.length; ++i) {
			CoreSentence sentence = annotation.sentences().get(i);
			tags[i] = sentence.posTags().toArray(new String[sentence.posTags().size()]);
			List<CoreLabel> tokensCore = sentence.tokens();
			lemmata[i] = new String[tokensCore.size()];
			tokens[i] = new String[tokensCore.size()];
			for (int j = 0; j < tokensCore.size(); ++j) {
				lemmata[i][j] = tokensCore.get(j).lemma().toLowerCase();
				tokens[i][j] = tokensCore.get(j).originalText();
			}
			counter += tags[i].length;
		}
		return counter;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getNumber() {
		return number;
	}

	public String[][] getLemmata() {
		return lemmata;
	}

	public String[][] getTags() {
		return tags;
	}

	public String[][] getTokens() {
		return tokens;
	}

	public void setSite(ArchivedSite result) {
		this.site = result;
	}

	public ArchivedSite getSite() {
		return site;
	}

	public boolean getFake() {
		return site.getFake();
	}

	public void removeContent() {
		content = null;
		tags = null;
		lemmata = null;
		tokens = null;
	}

	public PageContentStruct getContentsStruct() {
		PageContentStruct result = new PageContentStruct();
		result.content = content;
		result.lemmata = lemmata;
		result.tags = tags;
		result.tokens = tokens;
		return result;
	}

	public void setContentsStruct(PageContentStruct pageContentStruct) {
		content=pageContentStruct.content;
		lemmata=pageContentStruct.lemmata;
		tags=pageContentStruct.tags;
		tokens=pageContentStruct.tokens;
	}

	public void setTopic(int integer) {
		topic=integer;
	}

	public int getTopic() {
		return topic;
	}
}
