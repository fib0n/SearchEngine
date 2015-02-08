package hh.homework.search;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by fib on 08/02/15.
 */
public class Manager {
    private final Tokenizer tokenizer;
    private final Normalizer normalizer;
    private final StopWordsAnalyzer stopWordsAnalyzer;
    private final Indexer indexer;

    public Manager(final Set<String> stopWords) throws IOException {
        tokenizer = new Tokenizer();
        normalizer = new Normalizer();
        stopWordsAnalyzer = new StopWordsAnalyzer(stopWords);
        indexer = new Indexer();
    }

    public void insertDocument(final Long id, final String text) {
        final List<String> terms = stopWordsAnalyzer.execute(normalizer.execute(tokenizer.execute(text)));
        indexer.put(id, terms);
    }

    public List<Long> searchDocuments(final String query, final String logic, final int count) {
        final List<String> terms =  getTerms(query);
        final List<Long> documents = (logic.equalsIgnoreCase("AND")
                ? indexer.andOperation(terms)
                : indexer.orOperation(terms));
        return documents.stream().limit(count).collect(Collectors.toList());
    }

    private List<String> getTerms(final String text){
        return stopWordsAnalyzer.execute(normalizer.execute(tokenizer.execute(text)));
    }
}
