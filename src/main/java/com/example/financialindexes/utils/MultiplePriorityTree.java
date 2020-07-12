package com.example.financialindexes.utils;

// Taken from:
//
// Red Black Tree implementation in Java
// Author: Algorithm Tutor
// Tutorial URL: https://algorithmtutor.com/Data-Structures/Tree/Red-Black-Trees/

import java.util.*;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

// data structure that represents a node in the tree
class Node<T> {
    static final Node<?> TNULL = new Node<>(null);

    T data; // holds the key
    final Map<String, Node<T>> parent = new HashMap<>(); // pointer to the parent
    final Map<String, Node<T>> left = new HashMap<>(); // pointer to left child
    final Map<String, Node<T>> right = new HashMap<>(); // pointer to right child
    final Map<String, Boolean> color = new HashMap<>(); // 1 . Red, 0 . Black

    public Node(T data) {
        this.data = data;
    }

    @SuppressWarnings("unchecked")
    private Node<T> TNULL() {
        return (Node<T>) TNULL;
    }

    public Node<T> left(String alias) {
        return left.getOrDefault(alias, TNULL());
    }

    public Node<T> left(String alias, Node<T> n) {
        return left.put(alias, n);
    }

    public Node<T> right(String alias) {
        return right.getOrDefault(alias, TNULL());
    }

    public Node<T> right(String alias, Node<T> n) {
        return right.put(alias, n);
    }

    public Node<T> parent(String alias) {
        return parent.get(alias);
    }

    public Node<T> parent(String alias, Node<T> n) {
        return parent.put(alias, n);
    }

    public int color(String alias) {
        return color.getOrDefault(alias, false) ? 1 : 0;
    }

    public Boolean color(String alias, int n) {
        return color.put(alias, n == 1);
    }
}

// class RedBlackTree implements the operations in Red Black Tree
public class MultiplePriorityTree<T> {
    private Node<T> TNULL = (Node<T>) Node.TNULL;
    private Map<String, Node<T>> root = new HashMap<>();
    private Map<String, Node<T>> first = new HashMap<>();
    private Map<String, Node<T>> last = new HashMap<>();
    private Map<String, Comparator<T>> cmp;
    private List<String> aliases;
    private int size = 0;

    @SafeVarargs
    public static <T> MultiplePriorityTree<T> of(Map.Entry<String, Comparator<T>> ...entries) {
        return new MultiplePriorityTree<T>(Map.ofEntries(entries));
    }

    public MultiplePriorityTree(Map<String, Comparator<T>> cmp) {
        this.cmp = cmp;
        aliases = new ArrayList<>(cmp.keySet());
    }

    public int size() {
        return size;
    }

    private long cmp(String alias, T val1, T val2) {
        return cmp.get(alias).compare(val1, val2);
    }

    private Node<T> root(String alias) {
        return root.getOrDefault(alias, TNULL);
    }

    private Node<T> root(String alias, Node<T> n) {
        return root.put(alias, n);
    }

    private void preOrderHelper(String alias, Node<T> node) {
        if (node != TNULL) {
            System.out.print(node.data + " ");
            preOrderHelper(alias, node.left(alias));
            preOrderHelper(alias, node.right(alias));
        }
    }

    private void inOrderHelper(String alias, Node<T> node) {
        if (node != TNULL) {
            inOrderHelper(alias, node.left(alias));
            System.out.print(node.data + " ");
            inOrderHelper(alias, node.right(alias));
        }
    }

    private void postOrderHelper(String alias, Node<T> node) {
        if (node != TNULL) {
            postOrderHelper(alias, node.left(alias));
            postOrderHelper(alias, node.right(alias));
            System.out.print(node.data + " ");
        }
    }

    private Node<T> searchTreeHelper(String alias, Node<T> node, T key) {
        if (node == TNULL || key.equals(node.data)) {
            return node;
        }

        if (cmp(alias, key, node.data) < 0) {
            return searchTreeHelper(alias, node.left(alias), key);
        }
        return searchTreeHelper(alias, node.right(alias), key);
    }

    // fix the rb tree modified by the delete operation
    private void fixDelete(String alias, Node<T> x) {
        Node<T> s;
        while (x != root(alias) && x.color(alias) == 0) {
            var xParent = x.parent(alias);
            if (x == xParent.left(alias)) {
                s = xParent.right(alias);
                if (s.color(alias) == 1) {
                    // case 3.1
                    s.color(alias, 0);
                    xParent.color(alias, 1);
                    leftRotate(alias, xParent);
                    xParent = x.parent(alias);
                    s = xParent.right(alias);
                }

                if (s.left(alias).color(alias) == 0 && s.right(alias).color(alias) == 0) {
                    // case 3.2
                    s.color(alias, 1);
                    x = xParent;
                } else {
                    if (s.right(alias).color(alias) == 0) {
                        // case 3.3
                        s.left(alias).color(alias, 0);
                        s.color(alias, 1);
                        rightRotate(alias, s);
                        xParent = x.parent(alias);
                        s = xParent.right(alias);
                    }

                    // case 3.4
                    s.color(alias, xParent.color(alias));
                    xParent.color(alias,  0);
                    s.right(alias).color(alias, 0);
                    leftRotate(alias, xParent);
                    x = root(alias);
                }
            } else {
                s = xParent.left(alias);
                if (s.color(alias) == 1) {
                    // case 3.1
                    s.color(alias, 0);
                    xParent.color(alias, 1);
                    rightRotate(alias, xParent);
                    xParent = x.parent(alias);
                    s = xParent.left(alias);
                }

                if (s.right(alias).color(alias) == 0 && s.right(alias).color(alias) == 0) {
                    // case 3.2
                    s.color(alias, 1);
                    x = xParent;
                } else {
                    if (s.left(alias).color(alias) == 0) {
                        // case 3.3
                        s.right(alias).color(alias,  0);
                        s.color(alias, 1);
                        leftRotate(alias, s);
                        xParent = x.parent(alias);
                        s = xParent.left(alias);
                    }

                    // case 3.4
                    s.color(alias, xParent.color(alias));
                    xParent.color(alias,  0);
                    s.left(alias).color(alias, 0);
                    rightRotate(alias, xParent);
                    x = root(alias);
                }
            }
        }
        x.color(alias, 0);
    }


    private void rbTransplant(String alias, Node<T> u, Node<T> v){
        var uParent = u.parent(alias);
        if (uParent == null) {
            root(alias, v);
        } else if (u == uParent.left(alias)){
            uParent.left(alias, v);
        } else {
            uParent.right(alias, v);
        }
        v.parent(alias, uParent);
    }

    private void deleteNodeHelper(String alias, Node<T> node, T key) {
        // find the node containing key
        Node<T> z = TNULL;
        Node<T> x, y;
        while (node != TNULL){
            if (cmp(alias, node.data, key) == 0) {
                z = node;
            }

            if (cmp(alias, node.data, key) <= 0) {
                node = node.right(alias);
            } else {
                node = node.left(alias);
            }
        }

        if (z == TNULL) {
            System.out.println("Couldn't find key in the tree");
            return;
        }

        y = z;
        var zLeft = z.left(alias);
        var zRight = z.right(alias);
        int yOriginalColor = y.color(alias);
        if (zLeft == TNULL) {
            x = zRight;
            rbTransplant(alias, z, zRight);
        } else if (zRight == TNULL) {
            x = zLeft;
            rbTransplant(alias, z, zLeft);
        } else {
            y = minimum(alias, zRight);
            yOriginalColor = y.color(alias);
            x = y.right(alias);
            if (y.parent(alias) == z) {
                x.parent(alias, y);
            } else {
                rbTransplant(alias, y, y.right(alias));
                y.right(alias, zRight);
                y.right(alias).parent(alias, y);
            }

            rbTransplant(alias, z, y);
            y.left(alias, zLeft);
            y.left(alias).parent(alias, y);
            y.color(alias, z.color(alias));
        }
        if (yOriginalColor == 0){
            fixDelete(alias, x);
        }
    }

    // fix the red-black tree
    private void fixInsert(String alias, Node<T> k){
        Node<T> u;
        while (k.parent(alias).color(alias) == 1) {
            var kParent = k.parent(alias);
            var kGParent = kParent.parent(alias);
            var rUncle = kGParent.right(alias);
            var lUncle = kGParent.left(alias);

            if (kParent == rUncle) {
                u = lUncle; // uncle
                if (u.color(alias) == 1) {
                    // case 3.1
                    u.color(alias,  0);
                    kParent.color(alias, 0);
                    kGParent.color(alias, 1);
                    k = kGParent;
                } else {
                    if (k == kParent.left(alias)) {
                        // case 3.2.2
                        k = kParent;
                        rightRotate(alias, k);
                        kParent = k.parent(alias);
                        kGParent = kParent.parent(alias);
                    }
                    // case 3.2.1
                    kParent.color(alias, 0);
                    kGParent.color(alias, 1);
                    leftRotate(alias, kGParent);
                }
            } else {
                u = rUncle; // uncle

                if (u.color(alias) == 1) {
                    // mirror case 3.1
                    u.color(alias, 0);
                    kParent.color(alias, 0);
                    kGParent.color(alias, 1);
                    k = kGParent;
                } else {
                    if (k == kParent.right(alias)) {
                        // mirror case 3.2.2
                        k = kParent;
                        leftRotate(alias, k);
                        kParent = k.parent(alias);
                        kGParent = kParent.parent(alias);
                    }
                    // mirror case 3.2.1
                    kParent.color(alias, 0);
                    kGParent.color(alias, 1);
                    rightRotate(alias, kGParent);
                }
            }
            if (k == root(alias)) {
                break;
            }
        }
        root(alias).color(alias, 0);
    }

    private void printHelper(String alias, Node<T> root, String indent, boolean last) {
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

            String sColor = root.color(alias) == 1?"RED":"BLACK";
            System.out.println(root.data + "(" + sColor + ")");
            printHelper(alias, root.left(alias), indent, false);
            printHelper(alias, root.right(alias), indent, true);
        }
    }

    // Pre-Order traversal
    // Node.Left Subtree.Right Subtree
    public void preorder(String alias) {
        preOrderHelper(alias, root(alias));
    }

    // In-Order traversal
    // Left Subtree . Node . Right Subtree
    public void inorder(String alias) {
        inOrderHelper(alias, root(alias));
    }

    // Post-Order traversal
    // Left Subtree . Right Subtree . Node
    public void postorder(String alias) {
        postOrderHelper(alias, root(alias));
    }

    // search the tree for the key k
    // and return the corresponding node
    public Node<T> searchTree(String alias, T k) {
        return searchTreeHelper(alias, root(alias), k);
    }

    public T first(String alias) {
        return first.get(alias).data;
    }

    // find the node with the minimum key
    private Node<T> minimum(String alias, Node<T> node) {
        while (node.left(alias) != TNULL) {
            node = node.left(alias);
        }
        return node;
    }

    public T last(String alias) {
        return last.get(alias).data;
    }

    // find the node with the maximum key
    private Node<T> maximum(String alias, Node<T> node) {
        while (node.right(alias) != TNULL) {
            node = node.right(alias);
        }
        return node;
    }

    // find the successor of a given node
    private Node<T> successor(String alias, Node<T> x) {
        // if the right subtree is not null,
        // the successor is the leftmost node in the
        // right subtree
        if (x.right(alias) != TNULL) {
            return minimum(alias, x.right(alias));
        }

        // else it is the lowest ancestor of x whose
        // left child is also an ancestor of x.
        Node<T> y = x.parent(alias);
        while (y != null && x == y.right(alias)) {
            x = y;
            y = y.parent(alias);
        }
        return y;
    }

    // find the predecessor of a given node
    private Node<T> predecessor(String alias, Node<T> x) {
        // if the left subtree is not null,
        // the predecessor is the rightmost node in the
        // left subtree
        if (x.left(alias) != TNULL) {
            return maximum(alias, x.left(alias));
        }

        Node<T> y = x.parent(alias);
        while (y != null && x == y.left(alias)) {
            x = y;
            y = y.parent(alias);
        }

        return y;
    }

    // rotate left at node x
    private void leftRotate(String alias, Node<T> x) {
        Node<T> y = x.right(alias);
        var yLeft = y.left(alias);
        var xParent = x.parent(alias);

        x.right(alias, yLeft);
        if (yLeft != TNULL) {
            yLeft.parent(alias, x);
        }
        y.parent(alias, xParent);
        if (xParent == null) {
            root(alias, y);
        } else if (x == xParent.left(alias)) {
            xParent.left(alias, y);
        } else {
            xParent.right(alias, y);
        }
        y.left(alias, x);
        x.parent(alias, y);
    }

    // rotate right at node x
    private void rightRotate(String alias, Node<T> x) {
        Node<T> y = x.left(alias);
        var yRight = y.right(alias);
        var xParent = x.parent(alias);

        x.left(alias, yRight);
        if (yRight != TNULL) {
            yRight.parent(alias, x);
        }
        y.parent(alias, xParent);
        if (xParent == null) {
            root(alias, y);
        } else if (x == xParent.right(alias)) {
            xParent.right(alias, y);
        } else {
            xParent.left(alias, y);
        }
        y.right(alias, x);
        x.parent(alias, y);
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public synchronized void add(T key) {
        var node = new Node<>(key);
        aliases.forEach(alias -> {
            add(alias, node);

            if (isEmpty()) {
                first.put(alias, node);
                last.put(alias, node);
            } else {
                var firstNode = first.getOrDefault(alias, node);
                if (cmp(alias, key, firstNode.data) < 0)
                    first.put(alias, node);
                else {
                    var lastNode = last.getOrDefault(alias, node);
                    if (cmp(alias, key, lastNode.data) > 0)
                        last.put(alias, node);
                }
            }
        });

        size++;
    }

        // insert the key to the tree in its appropriate position
    // and fix the tree
    private void add(String alias, Node<T> node) {
        // Ordinary Binary Search Insertion
        node.color(alias, 1); // new node must be red

        Node<T> y = null;
        Node<T> x = root(alias);

        while (x != TNULL) {
            y = x;
            if (cmp(alias, node.data, x.data) < 0) {
                x = x.left(alias);
            } else {
                x = x.right(alias);
            }
        }

        // y is parent of x
        node.parent(alias, y);
        if (y == null) {
            root(alias, node);
        } else if (cmp(alias, node.data, y.data) < 0) {
            y.left(alias, node);
        } else {
            y.right(alias, node);
        }

        // if new node is a root node, simply return
        if (node.parent(alias) == null){
            node.color(alias, 0);
            return;
        }

        // if the grandparent is null, simply return
        if (node.parent(alias).parent(alias) == null) {
            return;
        }

        // Fix the tree
        try {
            fixInsert(alias, node);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                deleteNodeHelper(theAlias, firstNode, firstValue);

                if (isNull(predecessor) && nonNull(successor))
                    first.put(theAlias, successor);

                if (!alias.equals(theAlias) && isNull(successor))
                    last.put(theAlias, predecessor);
            });
        }

        return firstValue;
    }

    // print the tree structure on the screen
    public void prettyPrint() {
        aliases.stream()
                .peek(System.out::println)
                .forEach(alias ->
                printHelper(alias, root(alias), "", true));
    }
}