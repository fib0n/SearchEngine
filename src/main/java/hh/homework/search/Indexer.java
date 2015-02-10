package hh.homework.search;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by fib on 08/02/15.
 */
class Indexer {
    private final ConcurrentHashMap<String, SortedSet<IndexElement>> invertedIndex;
    private final Set<Long> documentsIds;
    private long documentsCount;

    public Indexer() {
        invertedIndex = new ConcurrentHashMap<>();
        documentsIds = new CopyOnWriteArraySet<>();
    }

    private static Map<String, Integer> getWordCounts(List<String> words) {
        final Map<String, Integer> counts = new HashMap<>();
        for (final String word : words) {
            final Integer current = counts.get(word);
            if (current != null) {
                counts.put(word, current + 1);
            } else counts.put(word, 1);
        }
        return counts;
    }

    public void put(final Long id, final List<String> words) {
        final Map<String, Integer> wordCounts = getWordCounts(words);
        final int documentSize = words.size();
        documentsIds.add(id);
        documentsCount = documentsIds.size();
        for (final String word : words) {
            if (invertedIndex.containsKey(word)) {
                invertedIndex.get(word).add(new IndexElement(id, (double) wordCounts.get(word) / documentSize));
            } else {
                final SortedSet<IndexElement> indexElements = new TreeSet<>();
                indexElements.add(new IndexElement(id, (double) wordCounts.get(word) / documentSize));
                invertedIndex.put(word, indexElements);
            }
        }
    }

    public List<Long> andOperation(final List<String> terms) {
        return rank(and(terms));
    }

    public List<Long> orOperation(final List<String> terms) {
        return rank(or(terms));
    }

    private List<Long> rank(final List<Pair> ids) {
        return ids.stream()
                .sorted((a, b) -> -Double.compare(a.score, b.score))
                .map(e -> e.id)
                .collect(Collectors.toList());
    }

    private List<Pair> intersect(final List<Pair> documents,
                                 final double idf,
                                 final Iterator<IndexElement> iterator) {
        //todo переписать без создания результирующего ArrayList на каждом шаге
        final List<Pair> result = new ArrayList<>();
        final Iterator<Pair> documentsIterator = documents.iterator();
        if (documentsIterator.hasNext() && iterator.hasNext()) {
            Pair currentA = documentsIterator.next();
            IndexElement currentB = iterator.next();
            while (true) {
                int compareResult = currentA.id.compareTo(currentB.id);
                if (compareResult < 0) {
                    if (documentsIterator.hasNext())
                        currentA = documentsIterator.next();
                    else
                        break;
                } else if (compareResult > 0) {
                    if (iterator.hasNext())
                        currentB = iterator.next();
                    else
                        break;
                } else {
                    result.add(new Pair(currentA.id, currentA.score + currentB.tf * idf));
                    if (documentsIterator.hasNext() && iterator.hasNext()) {
                        currentA = documentsIterator.next();
                        currentB = iterator.next();
                    } else
                        break;
                }
            }
        }
        return result;
    }

    private List<Pair> and(final List<String> terms) {
        List<Pair> documents = null;
        for (final String term : terms) {
            if (!invertedIndex.containsKey(term)) {
                return new ArrayList<>();
            }
            final SortedSet<IndexElement> documentsByTerm = invertedIndex.get(term);
            if (documentsByTerm == null || documentsByTerm.isEmpty()) {
                return new ArrayList<>();
            }
            final double idf = Math.log((double) documentsCount / documentsByTerm.size());
            if (documents == null) {
                documents = documentsByTerm
                        .stream()
                        .map(e -> new Pair(e.id, e.tf * idf))
                        .collect(Collectors.toList());
            } else {
                final Iterator<IndexElement> iterator = documentsByTerm.iterator();
                documents = intersect(documents, idf, iterator);
            }
        }
        return documents;
    }

    private List<Pair> or(final List<String> terms) {
        if (terms == null || terms.size() == 0)
            return new ArrayList<>();

        final PriorityQueue<QueueElement> pq = new PriorityQueue<>(terms.size());
        terms.stream().filter(invertedIndex::containsKey).forEach(term -> {
            final SortedSet<IndexElement> documentsByTerm = invertedIndex.get(term);
            final Iterator<IndexElement> iterator = documentsByTerm.iterator();
            if (iterator.hasNext()) {
                pq.add(new QueueElement(
                        iterator.next(),
                        Math.log((double) documentsCount / documentsByTerm.size()),
                        iterator));
            }
        });
        final List<Pair> documents = new ArrayList<>();
        Long previousDocumentId = null;
        while (!pq.isEmpty()) {
            final QueueElement queueElement = pq.poll();
            if (previousDocumentId != null && queueElement.current.id.compareTo(previousDocumentId) == 0) {
                final Pair last = documents.get(documents.size() - 1);
                last.score += queueElement.current.tf * queueElement.idf;
            } else {
                previousDocumentId = queueElement.current.id;
                documents.add(new Pair(queueElement.current.id, queueElement.current.tf * queueElement.idf));
            }
            if (queueElement.iterator.hasNext()) {
                pq.add(new QueueElement(queueElement.iterator.next(), queueElement.idf, queueElement.iterator));
            }
        }
        return documents;
    }

    private static class IndexElement implements Comparable<IndexElement> {
        private final Long id;
        private final double tf;

        IndexElement(final Long id, final double tf) {
            this.id = checkNotNull(id);
            this.tf = tf;
        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }

        @Override
        public int compareTo(IndexElement o) {
            if (o == null)
                return -1;
            return id.compareTo(o.id);
        }
    }

    private static class Pair {
        private final Long id;
        public double score;

        Pair(final Long id, final double initialScore) {
            this.id = id;
            score = initialScore;
        }
    }

    private static class QueueElement implements Comparable<QueueElement> {
        private final IndexElement current;
        private final double idf;
        private final Iterator<IndexElement> iterator;

        QueueElement(final IndexElement current, final double idf, final Iterator<IndexElement> iterator) {
            this.current = checkNotNull(current);
            this.idf = idf;
            this.iterator = checkNotNull(iterator);
        }

        @Override
        public int hashCode() {
            return current.id.hashCode();
        }


        @Override
        public int compareTo(final QueueElement o) {
            if (o == null)
                return -1;
            return current.id.compareTo(o.current.id);
        }
    }
}
