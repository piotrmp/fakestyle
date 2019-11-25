package pl.waw.ipipan.homados.news;

import java.io.Serializable;

public class PageContentStruct implements Serializable{

	private static final long serialVersionUID = -55493839057984000L;
	public String content;
	public String[][] tags;
	public String[][] lemmata;
	public String[][] tokens;

}
