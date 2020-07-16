package com.example.financialindexes.utils;

import java.util.*;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

// class MultiplePriorityTree implements the operations in Red Black Tree
public class MultiplePriorityTree<T> {
    private final Node TNULL = new Node(null);
    private final Map<String, Node> root = new HashMap<>();
    private final Map<String, Node> first = new HashMap<>();
    private final Map<String, Node> last = new HashMap<>();
    private final Map<String, Comparator<T>> cmp;
    private final List<String> aliases;
    private int size = 0;

    private final int BLACK = 0;
    private final int RED = 1;

    // data structure that represents a node in the tree
    private class Node {
        T data; // holds the key
        final Map<String, Node> parent = new HashMap<>();
        final Map<String, Node> left = new HashMap<>();
        final Map<String, Node> right = new HashMap<>();
        final Map<String, Boolean> color = new HashMap<>();

        public Node(T data) {
            this.data = data;
        }

        public Node left(String alias) {
            return left.getOrDefault(alias, TNULL);
        }

        public void left(String alias, Node n) {
            if (n == TNULL) left.remove(alias);
            else            left.put(alias, n);
        }

        public Node right(String alias) {
            return right.getOrDefault(alias, TNULL);
        }

        public void right(String alias, Node n) {
            if (n == TNULL) right.remove(alias);
            else            right.put(alias, n);
        }

        public Node parent(String alias) {
            return parent.get(alias);
        }

        public void parent(String alias, Node n) {
            parent.put(alias, n);
        }

        public int color(String alias) {
            return color.getOrDefault(alias, false) ? RED : BLACK;
        }

        public void color(String alias, int n) {
            color.put(alias, n == RED);
        }

        public boolean isLeave(String alias) {
            return left(alias) == right(alias);
        }

        public boolean hasBothChild(String alias) {
            return left(alias) != TNULL && right(alias) != TNULL;
        }
    }

    @SafeVarargs
    public static <T> MultiplePriorityTree<T> of(Map.Entry<String, Comparator<T>> ...entries) {
        return new MultiplePriorityTree<>(Map.ofEntries(entries));
    }

    public MultiplePriorityTree(Map<String, Comparator<T>> cmp) {
        this.cmp = cmp;
        aliases = new ArrayList<>(cmp.keySet());
    }

    public int size() {
        return size;
    }

    private int cmp(String alias, T val1, T val2) {
        return cmp.get(alias).compare(val1, val2);
    }

    private Node root(String alias) {
        return root.getOrDefault(alias, TNULL);
    }

    private void root(String alias, Node n) {
        root.put(alias, n);
    }

    private void rbTransplant(String alias, Node u, Node v){
        var uParent = u.parent(alias);
        if (uParent == null) {
            root(alias, v);
        } else if (u == uParent.left(alias)){
            uParent.left(alias, v);
        } else {
            uParent.right(alias, v);
        }
        if (v != TNULL)
            v.parent(alias, uParent);
    }

    private void printHelper(String alias, Node root, String indent, boolean last) {
        // print the tree structure on the screen
        if (root != TNULL) {
            System.out.print(indent);
            if (last) {
                System.out.print("R----");
                indent += "     ";
            } else {
                System.out.print("L----");
                indent += "|    ";
            }

            String sColor = root.color(alias) == RED?"RED":"BLACK";
            System.out.println(root.data + "(" + sColor + ")");
            printHelper(alias, root.left(alias), indent, false);
            printHelper(alias, root.right(alias), indent, true);
        }
    }

    public T first(String alias) {
        return first.get(alias).data;
    }

    public T minimum(String alias) {
        return minimum(alias, root(alias)).data;
    }

    // find the node with the minimum key
    private Node minimum(String alias, Node node) {
        while (node.left(alias) != TNULL) {
            node = node.left(alias);
        }
        return node;
    }

    public T last(String alias) {
        return last.get(alias).data;
    }

    public T maximum(String alias) {
        return maximum(alias, root(alias)).data;
    }

    // find the node with the maximum key
    private Node maximum(String alias, Node node) {
        while (node.right(alias) != TNULL) {
            node = node.right(alias);
        }
        return node;
    }

    // find the successor of a given node
    private Node successor(String alias, Node node) {
        // if the right subtree is not null,
        // the successor is the leftmost node in the
        // right subtree
        if (node.right(alias) != TNULL) {
            return minimum(alias, node.right(alias));
        }

        // else it is the lowest ancestor of x whose
        // left child is also an ancestor of x.
        Node cursor = node.parent(alias);
        while (cursor != null && node == cursor.right(alias)) {
            node = cursor;
            cursor = cursor.parent(alias);
        }
        return cursor;
    }

    // find the predecessor of a given node
    private Node predecessor(String alias, Node node) {
        // if the left subtree is not null,
        // the predecessor is the rightmost node in the
        // left subtree
        if (node.left(alias) != TNULL) {
            return maximum(alias, node.left(alias));
        }

        Node cursor = node.parent(alias);
        while (cursor != null && node == cursor.left(alias)) {
            node = cursor;
            cursor = cursor.parent(alias);
        }

        return cursor;
    }

    // rotate left at node x
    private void leftRotate(String alias, Node node) {
        Node nodeRight = node.right(alias);
        var nodeParent = node.parent(alias);
        var nodeRightLeft = nodeRight.left(alias);

        node.right(alias, nodeRightLeft);
        if (nodeRightLeft != TNULL) {
            nodeRightLeft.parent(alias, node);
        }
        nodeRight.parent(alias, nodeParent);
        if (nodeParent == null) {
            root(alias, nodeRight);
        } else if (node == nodeParent.left(alias)) {
            nodeParent.left(alias, nodeRight);
        } else {
            nodeParent.right(alias, nodeRight);
        }
        nodeRight.left(alias, node);
        node.parent(alias, nodeRight);
    }

    // rotate right at node x
    private void rightRotate(String alias, Node node) {
        Node nodeLeft = node.left(alias);
        var nodeParent = node.parent(alias);
        var nodeLeftRight = nodeLeft.right(alias);

        node.left(alias, nodeLeftRight);
        if (nodeLeftRight != TNULL) {
            nodeLeftRight.parent(alias, node);
        }
        nodeLeft.parent(alias, nodeParent);
        if (nodeParent == null) {
            root(alias, nodeLeft);
        } else if (node == nodeParent.right(alias)) {
            nodeParent.right(alias, nodeLeft);
        } else {
            nodeParent.left(alias, nodeLeft);
        }
        nodeLeft.right(alias, node);
        node.parent(alias, nodeLeft);
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public synchronized void add(T e) {
        var node = new Node(e);
        aliases.forEach(alias -> {
            add(alias, node);

            if (isEmpty()) {
                first.put(alias, node);
                last.put(alias, node);
            } else {
                var firstNode = first.getOrDefault(alias, node);
                if (cmp(alias, e, firstNode.data) < 0)
                    first.put(alias, node);
                else {
                    var lastNode = last.getOrDefault(alias, node);
                    if (cmp(alias, e, lastNode.data) >= 0)
                        last.put(alias, node);
                }
            }
        });

        size++;
    }

    // and fix the tree
    private void add(String alias, Node node) {
        // Ordinary Binary Search Insertion
        if (isEmpty()) {
            root(alias, node);
            return;
        }

        node.color(alias, RED); // new node must be red

        Node parent = searchParent(alias, node);
        node.parent(alias, parent);

        if (cmp(alias, node.data, parent.data) < 0)
            parent.left(alias, node);
        else
            parent.right(alias, node);

        recolor(alias, node);
    }

    private void recolor(String alias, Node node) {
        var parent = node.parent(alias);
        if (parent.color(alias) == RED) {
            var grandParent = parent.parent(alias);
            var uncle = grandParent.left(alias) == parent
                    ? grandParent.right(alias)
                    : grandParent.left(alias);

            var uncleColor = uncle.color(alias);
            if (uncleColor == RED) {
                parent.color(alias, BLACK);
                uncle.color(alias, BLACK);

                if (grandParent != root(alias)) {
                    grandParent.color(alias, RED);
                    if (grandParent.parent(alias).color(alias) == RED)
                        recolor(alias, grandParent);
                }
            } else {
                // rebalance
                var isNodeRight = parent.right(alias) == node;
                var isParentRight = grandParent.right(alias) == parent;
                var blackNode = parent;

                if (isNodeRight != isParentRight) {
                    if (isNodeRight)
                        leftRotate(alias, parent);
                    else
                        rightRotate(alias, parent);
                    blackNode = node;
                }

                if (isParentRight)
                    leftRotate(alias, grandParent);
                else
                    rightRotate(alias, grandParent);

                grandParent.color(alias, RED);
                blackNode.color(alias, BLACK);
            }
        }
        root(alias).color(alias, BLACK);
    }

    private Node searchParent(String alias, Node node) {
        Node parent = null;
        Node cursor = root(alias);

        while (cursor != TNULL) {
            parent = cursor;
            if (cmp(alias, node.data, cursor.data) < 0) {
                cursor = cursor.left(alias);
            } else {
                cursor = cursor.right(alias);
            }
        }
        return parent;
    }

    public T pollFirst(String alias) {
        var firstNode = first.get(alias);
        var firstValue = firstNode.data;
        size--;

        if (size == 0) {
            root.clear();
            first.clear();
            last.clear();
        } else {
            aliases.forEach(theAlias -> {
                var successor = successor(theAlias, firstNode);
                var predecessor = predecessor(theAlias, firstNode);
                deleteNode(theAlias, firstNode);

                if (isNull(predecessor) && nonNull(successor))
                    first.put(theAlias, successor);

                if (!alias.equals(theAlias) && isNull(successor))
                    last.put(theAlias, predecessor);
            });
        }

        return firstValue;
    }
    public void deleteNode(String alias, Node node) {
        var rightNode = node.right(alias);
        var parentNode = node.parent(alias);
        var nodeColor = node.color(alias);
        Node dbNode = TNULL;
        Node dbParentNode = null;
        var isDbRight = false;

        var isRedDeleted = false;


        if (node.isLeave(alias)) {
            dbParentNode = parentNode;
            isDbRight = parentNode.right(alias) == node;
            isRedDeleted = nodeColor == RED;
            rbTransplant(alias, node, TNULL);
        } else if (!node.hasBothChild(alias)) {
            var singleChild = rightNode == TNULL ? node.left(alias) : rightNode;
            rbTransplant(alias, node, singleChild);
            dbNode = singleChild;
            isRedDeleted = singleChild.color(alias) == RED;
            singleChild.color(alias, node.color(alias));
        } else {
            var minNode = minimum(alias, rightNode);
            var minParent = minNode.parent(alias);

            if (minNode.isLeave(alias)) {
                isDbRight = minParent.right(alias) == minNode;
                isRedDeleted = minNode.color(alias) == RED;
                rbTransplant(alias, minNode, TNULL);
            } else {
                var minRightNode = minNode.right(alias);
                dbNode = minRightNode;
                isDbRight = false;
                isRedDeleted = minRightNode != TNULL && minRightNode.color(alias) == RED;

                rbTransplant(alias, minNode, minRightNode);
                if (minRightNode != TNULL)
                    minRightNode.color(alias, minNode.color(alias));
            }

            dbParentNode = minParent == node ? minNode : minParent;
            replaceNode(alias, node, minNode);
        }

        // Case 1
        if (!isRedDeleted) {
            // Double Black cases
            removeDoubleBlack(alias, dbNode, dbParentNode, isDbRight);
        }
    }

    private void removeDoubleBlack(String alias, Node dbNode, Node dbParentNode, boolean isDbRight) {
        while (dbNode != null) {
            // Case 2.2
            if (dbNode == root(alias))
                break;

            var dbParent = dbNode == TNULL ? dbParentNode : dbNode.parent(alias);
            isDbRight = dbNode == TNULL ? isDbRight : dbParent.right(alias) == dbNode;
            var dbSibling = isDbRight
                    ? dbParent.left(alias)
                    : dbParent.right(alias);

            // Case 2.3
            if (dbSibling.color(alias) == BLACK
                    && dbSibling.left(alias).color(alias) == BLACK
                    && dbSibling.right(alias).color(alias) == BLACK) {

                dbSibling.color(alias, RED);
                if (dbParent.color(alias) == RED) {
                    dbNode = null;
                    dbParent.color(alias, BLACK);
                } else {
                    dbNode = dbParent;
                }
                // Case 2.4
            } else if (dbSibling.color(alias) == RED) {
                swapColor(alias, dbParent, dbSibling);
                if (isDbRight)
                    rightRotate(alias, dbParent);
                else
                    leftRotate(alias, dbParent);
                // Case 2.5
            } else {
                var rightSibling = dbSibling.right(alias);
                var leftSibling = dbSibling.left(alias);
                var farSiblingChild = isDbRight ? leftSibling : rightSibling;
                var nearSiblingChild = isDbRight ? rightSibling : leftSibling;

                if (farSiblingChild.color(alias) == BLACK && nearSiblingChild.color(alias) == RED) {
                    swapColor(alias, dbSibling, nearSiblingChild);
                    if (isDbRight)
                        leftRotate(alias, dbSibling);
                    else
                        rightRotate(alias, dbSibling);

                    farSiblingChild = dbSibling;
                    dbSibling = nearSiblingChild;
                }

                swapColor(alias, dbParent, dbSibling);
                if (isDbRight)
                    rightRotate(alias, dbParent);
                else
                    leftRotate(alias, dbParent);

                farSiblingChild.color(alias, BLACK);
                dbNode = null;
            }
        }
    }

    private void replaceNode(String alias, Node oldNode, Node newNode) {
        rbTransplant(alias, oldNode, newNode);
        var rightNode = oldNode.right(alias);
        var leftNode = oldNode.left(alias);
        newNode.color(alias, oldNode.color(alias));
        newNode.right(alias, rightNode);
        newNode.left(alias, leftNode);
        rightNode.parent(alias, newNode);
        leftNode.parent(alias, newNode);
    }

    private void swapColor(String alias, Node dbParent, Node dbSibling) {
        var swapColor = dbSibling.color(alias);
        dbSibling.color(alias, dbParent.color(alias));
        dbParent.color(alias, swapColor);
    }


    // print the tree structure on the screen
    public void prettyPrint() {
        System.out.println("------------------------");
        aliases.stream()
                .peek(System.out::println)
                .forEach(alias ->
                printHelper(alias, root(alias), "", true));
    }

    public void clear() {
        root.clear();
        first.clear();
        last.clear();
        size = 0;
    }
}