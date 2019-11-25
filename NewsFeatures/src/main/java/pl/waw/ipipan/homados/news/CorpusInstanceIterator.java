package pl.waw.ipipan.homados.news;

import java.util.Iterator;

import cc.mallet.types.Instance;

public class CorpusInstanceIterator implements Iterator<Instance> {

	private Iterator<ArchivedPage> pageIterator;

	public CorpusInstanceIterator(Corpus corpus) {
		pageIterator=corpus.getTrainSet().iterator();
	}

	@Override
	public boolean hasNext() {
		return pageIterator.hasNext();
	}

	@Override
	public Instance next() {
		ArchivedPage page=pageIterator.next();
		return new Instance(page.getContent(), null,page.getDomain()+"/"+page.getNumber(), null);
	}

}
