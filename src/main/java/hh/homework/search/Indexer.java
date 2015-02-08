package hh.homework.search;

import com.google.common.collect.Lists;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by fib on 08/02/15.
 */
class Indexer<T extends Comparable<T>> {
    private final ConcurrentHashMap<String, SortedSet<T>> invertedIndex = new ConcurrentHashMap<>();

    public void put(final T id, final List<String> words) {
        for (String word : words) {
            if (invertedIndex.containsKey(word)) {
                invertedIndex.get(word).add(id);
            } else {
                SortedSet<T> indexValue = new TreeSet<>();
                indexValue.add(id);
                invertedIndex.put(word, indexValue);
            }
        }
    }

    public List<T> andOperation(final List<String> terms) {
        List<Iterator<T>> indexValues = new ArrayList<>();
        terms.stream().filter(invertedIndex::containsKey).forEach(term -> {
            final Iterator<T> iterator = invertedIndex.get(term).iterator();
            if (iterator.hasNext()) {
                indexValues.add(iterator);
            }
        });
        Iterator<Iterator<T>> indexValuesIterator = indexValues.iterator();

        if (indexValuesIterator.hasNext()) {
            Iterator<T> current = indexValuesIterator.next();
            while (current.hasNext() && indexValuesIterator.hasNext()) {
                current = intersect(current, indexValuesIterator.next());
            }
            return Lists.newArrayList(current);
        }

        return new ArrayList<>();
    }

    public List<T> orOperation(final List<String> terms) {
        if (terms == null || terms.size() == 0)
            return new ArrayList<>();

        final PriorityQueue<Node<T>> pq = new PriorityQueue<>(terms.size());
        terms.stream().filter(invertedIndex::containsKey).forEach(term -> {
            final Iterator<T> iterator = invertedIndex.get(term).iterator();
            if (iterator.hasNext()) {
                pq.add(new Node<>(iterator.next(), iterator));
            }
        });
        final List<T> documents = new ArrayList<>();
        T previous = null;
        while (!pq.isEmpty()) {
            final Node<T> node = pq.poll();
            if (previous == null || previous.compareTo(node.current) != 0) {
                previous = node.current;
                documents.add(node.current);
            }
            if (node.iterator.hasNext()) {
                pq.add(new Node<>(node.iterator.next(), node.iterator));
            }
        }
        return documents;
    }

    private Iterator<T> intersect(final Iterator<T> a, final Iterator<T> b) {
        //todo переписать без создания ArrayList
        final List<T> c = new ArrayList<>();
        if (a.hasNext() && b.hasNext()) {
            T currentA = a.next();
            T currentB = b.next();
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

    private static class Node<T extends Comparable<T>> implements Comparable<Node<T>> {
        private final T current;
        private final Iterator<T> iterator;

        Node(final T current, final Iterator<T> iterator) {
            this.current = current;
            this.iterator = checkNotNull(iterator);
        }

        @Override
        public int compareTo(final Node<T> o) {
            if (o == null)
                return -1;
            return current.compareTo(o.current);
        }
    }
}
