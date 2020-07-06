package com.example.financialindexes.experimental;

import lombok.Getter;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.nonNull;

public class MultiplePriorityQueue<T> {

    Map<String, Comparator<T>> comparators;
    Map<String, Node<T>> first = new HashMap<>();
    Map<String, Node<T>> last = new HashMap<>();
    long size = 0;


    public MultiplePriorityQueue(Map<String, Comparator<T>> comparators) {
        this.comparators = comparators;
    }

    public synchronized void add(T e) {
        var node = new Node<>(e);
        if (first.isEmpty()) {
            comparators.keySet().forEach(alias -> {
                first.put(alias, node);
                last.put(alias, node);
            });
        } else {
            comparators.forEach((alias, comparator) -> {

                if (comparator.compare(e, last.get(alias).getValue()) >= 0) {
                    // put it at last
                    // ...
                    var lastNode = last.get(alias);
                    //  0 - 0 - 0 - X - e
                    node.insertAfter(alias, lastNode);

                    last.put(alias, node);
                } else {
                    //  0 - e - X - 0 - 0 - 0
                    var cursor = first.get(alias);
                    while (nonNull(cursor) && comparator.compare(e, cursor.getValue()) > 0) {
                        cursor = cursor.getNext(alias);
                    }

                    cursor.insertAfter(alias, node);

                    if (!node.hasBefore(alias))
                        first.put(alias, node);
                }
            });
        }

        size++;
    }

    public boolean isEmpty() {
        return first.isEmpty();
    }

    public synchronized T pollFirst(String alias) {
        var node = first.get(alias);
        node.remove(first, last);
        size--;

        return node.getValue();
    }

    public String toString() {
        StringBuilder str = new StringBuilder();
        for (String alias: comparators.keySet()) {
            var cursor = first.get(alias);
            str.append("\n").append(alias).append(":");
            while (nonNull(cursor)) {
                str.append("\n").append(cursor.getValue());
                cursor = cursor.getNext(alias);
            }
        }

        return str.toString();
    }

    public T first(String alias) {
        if (isEmpty())
            return null;
        return first.get(alias).getValue();
    }

    public T last(String alias) {
        if (isEmpty())
            return null;
        return last.get(alias).getValue();
    }

    public long size() {
        return size;
    }

    static class Node<T>  {
        @Getter
        T value;
        Map<String, Node<T>> next = new HashMap<>();
        Map<String, Node<T>> before = new HashMap<>();

        public Node(T value) {
            this.value = value;
        }

        public boolean hasNext(String alias) {
            return next.containsKey(alias);
        }

        public boolean hasBefore(String alias) {
            return before.containsKey(alias);
        }

        public Node<T> getNext(String alias) {
            return next.get(alias);
        }

        public Node<T> getBefore(String alias) {
            return before.get(alias);
        }

        public void remove(Map<String, Node<T>> first, Map<String, Node<T>> last) {

            before.forEach((alias, node) -> {
                if (!next.containsKey(alias))
                    last.put(alias, node);
            });

            next.forEach((alias, node) -> {
                if (before.containsKey(alias)) {
                    var bf = before.get(alias);
                    node.before.put(alias, bf);
                    bf.next.put(alias, node);
                } else {
                    node.before.remove(alias);
                    first.put(alias, node);
                }
            }) ;
        }

        public void insertAfter(String alias, Node<T> node) {
            var bf = before.get(alias);
            if (nonNull(bf)) {
                bf.next.put(alias, node);
                node.before.put(alias, bf);
            }

            node.next.put(alias, this);
            before.put(alias, node);
        }
    }
}



