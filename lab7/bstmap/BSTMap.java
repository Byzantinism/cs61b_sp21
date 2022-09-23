package bstmap;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

public class BSTMap<K extends Comparable<K>, V> implements Map61B<K, V>{
    private Node root;//BST root
    private Set<K> keySets;
    private Node currentRemove;
    //BST node
    private class Node{
        public K key;
        public V val;
        public Node left, right;
        public int size;

        public Node(K key, V val, int size){
            this.key = key;
            this.val = val;
            this.size = size;
        }
    }

    public BSTMap(){
            this.root = null;
            this.keySets = new TreeSet<>();
            this.currentRemove = null;
    }
    /** Removes all of the mappings from this map. */
    @Override
    public void clear(){
        this.root = null;
        this.keySets = new TreeSet<>();
        this.currentRemove = null;
    }

    /* Returns true if this map contains a mapping for the specified key. */
    @Override
    public boolean containsKey(K key){
        if (key == null){ throw new IllegalArgumentException("argument to contains() is null");}
        return get(root,key) != null;
    }

    /* Returns the value to which the specified key is mapped, or null if this
     * map contains no mapping for the key.
     */
    @Override
    public V get(K key){
        Node temp = get(root,key);
        if (temp == null){ return null;}
        return temp.val;
    }
    private Node get(Node x, K key) {
        if (key == null) {throw new IllegalArgumentException("argument to get() is null");}
        if (x == null){ return null;}
        int temp = key.compareTo(x.key);
        if (temp < 0){
            return get(x.left, key);
        }else if (temp > 0){
            return get(x.right, key);
        }else{
            return x;
        }

    }
    /* Returns the number of key-value mappings in this map. */
    @Override
    public int size(){
        return size(root);
    }
    private int size(Node x){
        if (x == null){ return 0;}
        return x.size;
    }
    /* Associates the specified value with the specified key in this map. */
    @Override
    public void put(K key, V value){
        if (key == null) {throw new IllegalArgumentException("key argument to put() is null");}
        root = put(root, key, value);
    }
    private Node put(Node x, K key, V value){
        if (x == null){
            this.keySets.add(key);
            return new Node(key, value, 1);}
        int temp = key.compareTo(x.key);
        if (temp < 0){
            x.left = put(x.left, key, value);
        }else if (temp > 0){
            x.right = put(x.right, key, value);
        }else{
            x.val = value;
        }
        x.size = size(x.left) + size(x.right) + 1;
        return x;
    }
    /* Returns a Set view of the keys contained in this map. Not required for Lab 7.
     * If you don't implement this, throw an UnsupportedOperationException. */
    @Override
    public Set<K> keySet(){
        return this.keySets;
    }

    /* Removes the mapping for the specified key from this map if present.
     * Not required for Lab 7. If you don't implement this, throw an
     * UnsupportedOperationException. */
    @Override
    public V remove(K key){
        this.currentRemove = null;
        if (get(root, key) != null){
            remove(root, key);
            return this.currentRemove.val;}
        return null;
    }

    /* Removes the entry for the specified key only if it is currently mapped to
     * the specified value. Not required for Lab 7. If you don't implement this,
     * throw an UnsupportedOperationException.*/
    @Override
    public V remove(K key, V value){
        this.currentRemove = null;
        if (value == get(key)){
            remove(root, key);
            return this.currentRemove.val;
        }
        return null;
    }

    private Node remove(Node x, K key){
        if (key == null){ throw new IllegalArgumentException("key argument to remove() is null");}
        if (x == null){ return null;}

        int temp = key.compareTo(x.key);
        if (temp < 0){
            x.left = remove(x.left, key);
        }else if (temp > 0){
            x.right = remove(x.right, key);
        }else{
            this.currentRemove = x;
            if (x.left == null){
                if (root == x){ root = x.right;}
                this.keySets.remove(key);
                return x.right;}
            if (x.right == null){
                if (root == x){ root = x.left;}
                this.keySets.remove(key);
                return x.left;}
            //x has 2 children.
            //Node y = x;
            x = max(x.left);
            x.left = deleteMax(this.currentRemove.left);
            x.right = this.currentRemove.right;
            if (root == this.currentRemove){ root = x;}
            this.keySets.remove(key);
        }
        x.size = size(x.left) + size(x.right) + 1;
        return x;
    }
    private Node max(Node x){
        if (x.right == null){ return x;}
        return max(x.right);
    }

    private Node deleteMax(Node x){
        if (x.right == null){ return x.left;}
        x.right = deleteMax(x.right);
        x.size = size(x.left) + size(x.right) + 1;
        return x;
    }

    @Override
    public Iterator<K> iterator(){
        return new bstIterator();
    }
    private class bstIterator implements Iterator<K>{
        private int pos;
        private K[] keyArray;
        public bstIterator(){
            pos = 0;
            keyArray = (K[]) keySets.toArray();
            Arrays.sort(keyArray);
        }
        @Override
        public boolean hasNext(){
            return pos < keyArray.length;
        }
        @Override
        public K next(){
            K returnItem = keyArray[pos];
            pos += 1;
            return returnItem;
        }
    }
    /*printInOrder prints out your BSTMap in order of increasing Key.
    public void printInOrder(){
        printInOrder(root);
    }
    private void printInOrder(Node x){
        if (x == null){ return;}
        System.out.print("["+x.key+": "+x.val+"]");
        printInOrder(x.left);
        printInOrder(x.right);
    }

     */
}
