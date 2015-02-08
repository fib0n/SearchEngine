package hh.homework.search;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by fib on 08/02/15.
 */
class StopWordsAnalyzer {
    private final Set<String> stopWords;

    public StopWordsAnalyzer(final Set<String> stopWords) {
        this.stopWords = stopWords;
    }

    public List<String> execute(final List<String> words) {
        final List<String> result = new ArrayList<>(words.size());
        result.addAll(words.stream().filter(word -> !stopWords.contains(word)).collect(Collectors.toList()));
        return result;
    }
}
