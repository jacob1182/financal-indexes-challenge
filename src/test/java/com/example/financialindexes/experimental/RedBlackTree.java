package com.example.financialindexes.experimental;

// Taken from:
//
// Red Black Tree implementation in Java
// Author: Algorithm Tutor
// Tutorial URL: https://algorithmtutor.com/Data-Structures/Tree/Red-Black-Trees/

import java.util.*;

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
public class RedBlackTree<T> {
    private Node<T> TNULL = (Node<T>) Node.TNULL;
    private Map<String, Node<T>> root = new HashMap<>();
    private Map<String, Comparator<T>> cmp;
    private List<String> aliases;



    public RedBlackTree(Map<String, Comparator<T>> cmp) {
        this.cmp = cmp;
        aliases = new ArrayList<>(cmp.keySet());
    }

    public long cmp(String alias, T val1, T val2) {
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
            if (x == x.parent(alias).left(alias)) {
                s = x.parent(alias).right(alias);
                if (s.color(alias) == 1) {
                    // case 3.1
                    s.color(alias, 0);
                    x.parent(alias).color(alias, 1);
                    leftRotate(alias, x.parent(alias));
                    s = x.parent(alias).right(alias);
                }

                if (s.left(alias).color(alias) == 0 && s.right(alias).color(alias) == 0) {
                    // case 3.2
                    s.color(alias, 1);
                    x = x.parent(alias);
                } else {
                    if (s.right(alias).color(alias) == 0) {
                        // case 3.3
                        s.left(alias).color(alias, 0);
                        s.color(alias, 1);
                        rightRotate(alias, s);
                        s = x.parent(alias).right(alias);
                    }

                    // case 3.4
                    s.color(alias, x.parent(alias).color(alias));
                    x.parent(alias).color(alias,  0);
                    s.right(alias).color(alias, 0);
                    leftRotate(alias, x.parent(alias));
                    x = root(alias);
                }
            } else {
                s = x.parent(alias).left(alias);
                if (s.color(alias) == 1) {
                    // case 3.1
                    s.color(alias, 0);
                    x.parent(alias).color(alias, 1);
                    rightRotate(alias, x.parent(alias));
                    s = x.parent(alias).left(alias);
                }

                if (s.right(alias).color(alias) == 0 && s.right(alias).color(alias) == 0) {
                    // case 3.2
                    s.color(alias, 1);
                    x = x.parent(alias);
                } else {
                    if (s.left(alias).color(alias) == 0) {
                        // case 3.3
                        s.right(alias).color(alias,  0);
                        s.color(alias, 1);
                        leftRotate(alias, s);
                        s = x.parent(alias).left(alias);
                    }

                    // case 3.4
                    s.color(alias, x.parent(alias).color(alias));
                    x.parent(alias).color(alias,  0);
                    s.left(alias).color(alias, 0);
                    rightRotate(alias, x.parent(alias));
                    x = root(alias);
                }
            }
        }
        x.color(alias, 0);
    }


    private void rbTransplant(String alias, Node<T> u, Node<T> v){
        if (u.parent(alias) == null) {
            root(alias, v);
        } else if (u == u.parent(alias).left(alias)){
            u.parent(alias).left(alias, v);
        } else {
            u.parent(alias).right(alias, v);
        }
        v.parent(alias, u.parent(alias));
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
        int yOriginalColor = y.color(alias);
        if (z.left(alias) == TNULL) {
            x = z.right(alias);
            rbTransplant(alias, z, z.right(alias));
        } else if (z.right(alias) == TNULL) {
            x = z.left(alias);
            rbTransplant(alias, z, z.left(alias));
        } else {
            y = minimum(alias, z.right(alias));
            yOriginalColor = y.color(alias);
            x = y.right(alias);
            if (y.parent(alias) == z) {
                x.parent(alias, y);
            } else {
                rbTransplant(alias, y, y.right(alias));
                y.right(alias, z.right(alias));
                y.right(alias).parent(alias, y);
            }

            rbTransplant(alias, z, y);
            y.left(alias, z.left(alias));
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
            if (k.parent(alias) == k.parent(alias).parent(alias).right(alias)) {
                u = k.parent(alias).parent(alias).left(alias); // uncle
                if (u.color(alias) == 1) {
                    // case 3.1
                    u.color(alias,  0);
                    k.parent(alias).color(alias, 0);
                    k.parent(alias).parent(alias).color(alias, 1);
                    k = k.parent(alias).parent(alias);
                } else {
                    if (k == k.parent(alias).left(alias)) {
                        // case 3.2.2
                        k = k.parent(alias);
                        rightRotate(alias, k);
                    }
                    // case 3.2.1
                    k.parent(alias).color(alias, 0);
                    k.parent(alias).parent(alias).color(alias, 1);
                    leftRotate(alias, k.parent(alias).parent(alias));
                }
            } else {
                u = k.parent(alias).parent(alias).right(alias); // uncle

                if (u.color(alias) == 1) {
                    // mirror case 3.1
                    u.color(alias, 0);
                    k.parent(alias).color(alias, 0);
                    k.parent(alias).parent(alias).color(alias, 1);
                    k = k.parent(alias).parent(alias);
                } else {
                    if (k == k.parent(alias).right(alias)) {
                        // mirror case 3.2.2
                        k = k.parent(alias);
                        leftRotate(alias, k);
                    }
                    // mirror case 3.2.1
                    k.parent(alias).color(alias, 0);
                    k.parent(alias).parent(alias).color(alias, 1);
                    rightRotate(alias, k.parent(alias).parent(alias));
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
        return minimum(alias, root(alias)).data;
    }

    // find the node with the minimum key
    public Node<T> minimum(String alias, Node<T> node) {
        while (node.left(alias) != TNULL) {
            node = node.left(alias);
        }
        return node;
    }

    public T last(String alias) {
        return maximum(alias, root(alias)).data;
    }

    // find the node with the maximum key
    public Node<T> maximum(String alias, Node<T> node) {
        while (node.right(alias) != TNULL) {
            node = node.right(alias);
        }
        return node;
    }

    // find the successor of a given node
    public Node<T> successor(String alias, Node<T> x) {
        // if the right subtree is not null,
        // the successor is the leftmost node in the
        // right subtree
        if (x.right(alias) != TNULL) {
            return minimum(alias, x.right(alias));
        }

        // else it is the lowest ancestor of x whose
        // left child is also an ancestor of x.
        Node<T> y = x.parent(alias);
        while (y != TNULL && x == y.right(alias)) {
            x = y;
            y = y.parent(alias);
        }
        return y;
    }

    // find the predecessor of a given node
    public Node<T> predecessor(String alias, Node<T> x) {
        // if the left subtree is not null,
        // the predecessor is the rightmost node in the
        // left subtree
        if (x.left(alias) != TNULL) {
            return maximum(alias, x.left(alias));
        }

        Node<T> y = x.parent(alias);
        while (y != TNULL && x == y.left(alias)) {
            x = y;
            y = y.parent(alias);
        }

        return y;
    }

    // rotate left at node x
    public void leftRotate(String alias, Node<T> x) {
        Node<T> y = x.right(alias);
        x.right(alias, y.left(alias));
        if (y.left(alias) != TNULL) {
            y.left(alias).parent(alias, x);
        }
        y.parent(alias, x.parent(alias));
        if (x.parent(alias) == null) {
            root(alias, y);
        } else if (x == x.parent(alias).left(alias)) {
            x.parent(alias).left(alias, y);
        } else {
            x.parent(alias).right(alias, y);
        }
        y.left(alias, x);
        x.parent(alias, y);
    }

    // rotate right at node x
    public void rightRotate(String alias, Node<T> x) {
        Node<T> y = x.left(alias);
        x.left(alias, y.right(alias));
        if (y.right(alias) != TNULL) {
            y.right(alias).parent(alias, x);
        }
        y.parent(alias, x.parent(alias));
        if (x.parent(alias) == null) {
            root(alias, y);
        } else if (x == x.parent(alias).right(alias)) {
            x.parent(alias).right(alias, y);
        } else {
            x.parent(alias).left(alias, y);
        }
        y.right(alias, x);
        x.parent(alias, y);
    }

    public void add(T key) {
        var node = new Node<>(key);
        aliases.forEach(alias -> add(alias, node));
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
        fixInsert(alias, node);
    }

    public Node<T> getRoot(String alias){
        return root(alias);
    }

    public T pollFirst(String alias) {
        var firstNode = minimum(alias, root(alias));
        var firstValue = firstNode.data;
        aliases.forEach(theAlias  -> deleteNodeHelper(theAlias, firstNode, firstValue));

        return firstValue;
    }

    // delete the node from the tree
    public void deleteNode(T data) {
        var alias = aliases.get(0);
        deleteNodeHelper(alias, root(alias), data);
    }

    // print the tree structure on the screen
    public void prettyPrint() {
        aliases.stream()
                .peek(System.out::println)
                .forEach(alias ->
                printHelper(alias, root(alias), "", true));
    }

    public static void main(String [] args){
        RedBlackTree<Integer> bst = new RedBlackTree<>(Map.of(
                "sorted", Integer::compareTo,
                "reversed", ((Comparator<Integer>) Integer::compare).reversed()
        ));
        bst.add(8);
        bst.add(18);
        bst.add(5);
        bst.add(15);
        bst.add(17);
        bst.add(25);
        bst.add(40);
        bst.add(80);
        bst.deleteNode(25);
        bst.prettyPrint();
    }
}