package hh.homework.search;

import com.google.common.collect.Lists;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by fib on 08/02/15.
 */
class Indexer {
    private final ConcurrentHashMap<String, NavigableSet<IndexElement>> invertedIndex;
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
                final NavigableSet<IndexElement> indexElements = new TreeSet<>();
                indexElements.add(new IndexElement(id, (double) wordCounts.get(word) / documentSize));
                invertedIndex.put(word, indexElements);
            }
            //System.out.println("tf:" + (double) wordCounts.get(word) / documentSize);
        }
    }

    public List<Long> andOperation(final List<String> terms) {
        return rank(and(terms), terms);
    }

    public List<Long> orOperation(final List<String> terms) {
        return rank(or(terms), terms);
    }

    private List<Long> rank(final List<Long> ids, final List<String> terms) {
        return ids.stream()
                .sorted((a, b) -> -Double.compare(getScore(a, terms), getScore(b, terms)))
                .collect(Collectors.toList());
    }

    private double getScore(final Long id, final List<String> terms) {
        double score = 0.0;
        for (final String term : terms) {
            final NavigableSet<IndexElement> set = invertedIndex.get(term);
            if (set != null) {
                final IndexElement virtualElement = new IndexElement(id, 0);
                if (set.contains(virtualElement)) {
                    double idf = Math.log((double) documentsCount / set.size());
                    score += set.floor(virtualElement).tf * idf;
                }
            }
        }
        return score;
    }


    private Iterator<IndexElement> intersect(final Iterator<IndexElement> a, final Iterator<IndexElement> b) {
        //todo переписать без создания ArrayList
        final List<IndexElement> c = new ArrayList<>();
        if (a.hasNext() && b.hasNext()) {
            IndexElement currentA = a.next();
            IndexElement currentB = b.next();
            while (true) {
                int compareResult = currentA.compareTo(currentB);
                if (compareResult < 0) {
                    if (a.hasNext())
                        currentA = a.next();
                    else
                        break;
                } else if (compareResult > 0) {
                    if (b.hasNext())
                        currentB = b.next();
                    else
                        break;
                } else {
                    c.add(currentA);
                    if (a.hasNext() && b.hasNext()) {
                        currentA = a.next();
                        currentB = b.next();
                    } else
                        break;
                }
            }
        }

        return c.iterator();
    }

    private List<Long> and(final List<String> terms) {
        final List<Iterator<IndexElement>> indexValues = new ArrayList<>();
        for(final String term: terms){
            if (!invertedIndex.containsKey(term)){
                return new ArrayList<>();
            }

            final Iterator<IndexElement> iterator = invertedIndex.get(term).iterator();
            if (iterator.hasNext()) {
                indexValues.add(iterator);
            }
        }
        final Iterator<Iterator<IndexElement>> indexValuesIterator = indexValues.iterator();

        if (indexValuesIterator.hasNext()) {
            Iterator<IndexElement> current = indexValuesIterator.next();
            while (current.hasNext() && indexValuesIterator.hasNext()) {
                current = intersect(current, indexValuesIterator.next());
            }
            return Lists.newArrayList(current).stream().map(t -> t.id).collect(Collectors.toList());
        }

        return new ArrayList<>();
    }

    private List<Long> or(final List<String> terms) {
        if (terms == null || terms.size() == 0)
            return new ArrayList<>();

        final PriorityQueue<QueueElement> pq = new PriorityQueue<>(terms.size());
        terms.stream().filter(invertedIndex::containsKey).forEach(term -> {
            final Iterator<IndexElement> iterator = invertedIndex.get(term).iterator();
            if (iterator.hasNext()) {
                pq.add(new QueueElement(iterator.next().id, iterator));
            }
        });
        final List<Long> documents = new ArrayList<>();
        Long previous = null;
        while (!pq.isEmpty()) {
            final QueueElement queueElement = pq.poll();
            if (previous == null || previous.compareTo(queueElement.currentId) != 0) {
                previous = queueElement.currentId;
                documents.add(queueElement.currentId);
            }
            if (queueElement.iterator.hasNext()) {
                pq.add(new QueueElement(queueElement.iterator.next().id, queueElement.iterator));
            }
        }
        return documents;
    }

    private static class IndexElement implements Comparable<IndexElement> {
        private final Long id;
        private final double tf;

        IndexElement(final Long id, final double tf) {
            this.id = id;
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

    private static class QueueElement implements Comparable<QueueElement> {
        private final Long currentId;
        private final Iterator<IndexElement> iterator;

        QueueElement(final Long currentId, final Iterator<IndexElement> iterator) {
            this.currentId = currentId;
            this.iterator = checkNotNull(iterator);
        }

        @Override
        public int hashCode() {
            return currentId.hashCode();
        }


        @Override
        public int compareTo(final QueueElement o) {
            if (o == null)
                return -1;
            return currentId.compareTo(o.currentId);
        }
    }
}
