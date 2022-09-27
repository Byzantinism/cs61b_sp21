package hashmap;

import java.util.*;

/**
 *  A hash table-backed Map implementation. Provides amortized constant time
 *  access to elements via get(), remove(), and put() in the best case.
 *
 *  Assumes null keys will never be inserted, and does not resize down upon remove().
 *  @author YOUR NAME HERE
 */
public class MyHashMap<K, V> implements Map61B<K, V> {

    /**
     * Protected helper class to store key/value pairs
     * The protected qualifier allows subclass access
     */
    protected class Node {
        K key;
        V value;

        Node(K k, V v) {
            key = k;
            value = v;
        }
    }

    /* Instance Variables */

    private Collection<Node>[] buckets;
    private Set<K> keySets;
    private double loadFactor;
    private int bucketSize;
    // You should probably define some more!

    /** Constructors */
    public MyHashMap() {
        this.bucketSize = 16;
        this.loadFactor = 0.75;
        keySets = new HashSet<>();
        buckets = createTable(this.bucketSize);
    }

    public MyHashMap(int initialSize) {
        this.bucketSize = initialSize;
        this.loadFactor = 0.75;
        keySets = new HashSet<>();
        buckets = createTable(this.bucketSize);
    }

    /**
     * MyHashMap constructor that creates a backing array of initialSize.
     * The load factor (# items / # buckets) should always be <= loadFactor
     *
     * @param initialSize initial size of backing array
     * @param maxLoad maximum load factor
     */
    public MyHashMap(int initialSize, double maxLoad) {
        this.bucketSize = initialSize;
        this.loadFactor = maxLoad;
        keySets = new HashSet<>();
        buckets = createTable(this.bucketSize);
    }

    /**
     * Returns a new node to be placed in a hash table bucket
     */
    private Node createNode(K key, V value) {
        if (key == null) throw new IllegalArgumentException("key to createNode() is null");
        if (value == null) throw new IllegalArgumentException("value to createNode() is null");
        return new Node(key, value);
    }

    /**
     * Returns a data structure to be a hash table bucket
     *
     * The only requirements of a hash table bucket are that we can:
     *  1. Insert items (`add` method)
     *  2. Remove items (`remove` method)
     *  3. Iterate through items (`iterator` method)
     *
     * Each of these methods is supported by java.util.Collection,
     * Most data structures in Java inherit from Collection, so we
     * can use almost any data structure as our buckets.
     *
     * Override this method to use different data structures as
     * the underlying bucket type
     *
     * BE SURE TO CALL THIS FACTORY METHOD INSTEAD OF CREATING YOUR
     * OWN BUCKET DATA STRUCTURES WITH THE NEW OPERATOR!
     */
    protected Collection<Node> createBucket() { return new LinkedList<>();}

    /**
     * Returns a table to back our hash table. As per the comment
     * above, this table can be an array of Collection objects
     *
     * BE SURE TO CALL THIS FACTORY METHOD WHEN CREATING A TABLE SO
     * THAT ALL BUCKET TYPES ARE OF JAVA.UTIL.COLLECTION
     *
     * @param tableSize the size of the table to create
     */
    private Collection<Node>[] createTable(int tableSize) {
        Collection<Node>[] returnItem = new Collection[tableSize];
        for (int i = 0; i < tableSize; i++){
            returnItem[i] = createBucket();
        }
        return returnItem;
    }

    // TODO: Implement the methods of the Map61B Interface below
    // Your code won't compile until you do so!

    //Helper functions
    private int hashIndex(K key){
        if (key == null) throw new IllegalArgumentException("key to hashIndex() is null");
        return Math.floorMod(key.hashCode(), this.bucketSize);
    }
    //Need more buckets
    private void resize(){
        double nextLF = (double) (size() + 1) / (double) buckets.length;
        if (nextLF >= loadFactor){
            //Collection<Node>[] tempBuckets = createTable(this.bucketSize * 2);
            MyHashMap<K, V> temp = new MyHashMap<>(this.bucketSize * 2);
            for (K i: this){
                temp.put(i, get(i));
            }
            this.bucketSize = temp.bucketSize;
            this.keySets = temp.keySets;
            this.buckets = temp.buckets;
        }
    }

    //Implements Map61B below!
    /** Removes all of the mappings from this map. */
    @Override
    public void clear(){
        keySets = new HashSet<>();
        buckets = createTable(this.bucketSize);
    }

    /** Returns true if this map contains a mapping for the specified key. */
    @Override
    public boolean containsKey(K key){
        if (key == null) throw new IllegalArgumentException("key to containsKey() is null");
        return keySets.contains(key);
    }

    /**
     * Returns the value to which the specified key is mapped, or null if this
     * map contains no mapping for the key.
     */
    @Override
    public V get(K key){
        if (key == null) throw new IllegalArgumentException("key to get() is null");
        if (containsKey(key)) {
            Collection<Node> aimBucket = buckets[hashIndex(key)];
            for (Node i : aimBucket) {
                if (i.key.equals(key)) return i.value;
            }
        }
        return null;
    }

    /** Returns the number of key-value mappings in this map. */
    @Override
    public int size(){
        return keySets.size();
    }

    /**
     * Associates the specified value with the specified key in this map.
     * If the map previously contained a mapping for the key,
     * the old value is replaced.
     */
    @Override
    public void put(K key, V value){
        if (key == null) throw new IllegalArgumentException("key to createNode() is null");
        if (value == null) throw new IllegalArgumentException("value to createNode() is null");
        //Check whether need to resize.
        resize();
        //start to put
        Collection<Node> aimBucket = buckets[hashIndex(key)];
        if (containsKey(key)) {
            for (Node i : aimBucket) {
                if (i.key.equals(key)) {
                    i.value = value;
                    break;
                }
            }
        } else {
            this.keySets.add(key);
            aimBucket.add(createNode(key, value));
        }
    }

    /** Returns a Set view of the keys contained in this map. */
    @Override
    public Set<K> keySet(){
        return this.keySets;
    }

    /**
     * Removes the mapping for the specified key from this map if present.
     * Not required for Lab 8. If you don't implement this, throw an
     * UnsupportedOperationException.
     */
    @Override
    public V remove(K key){
        if (key == null) throw new IllegalArgumentException("key to remove() is null");
        if (containsKey(key)) {
            Collection<Node> aimBucket = buckets[hashIndex(key)];
            for (Node i : aimBucket) {
                if (i.key.equals(key)) {
                    aimBucket.remove(i);
                    this.keySets.remove(key);
                    return i.value;
                }
            }
        }
        return null;
    }

    /**
     * Removes the entry for the specified key only if it is currently mapped to
     * the specified value. Not required for Lab 8. If you don't implement this,
     * throw an UnsupportedOperationException.
     */
    @Override
    public V remove(K key, V value){
        if (key == null) throw new IllegalArgumentException("key to remove() is null");
        if (containsKey(key)) {
            Collection<Node> aimBucket = buckets[hashIndex(key)];
            for (Node i : aimBucket) {
                if (i.key.equals(key) && i.value.equals(value)) {
                    aimBucket.remove(i);
                    this.keySets.remove(key);
                    return i.value;
                }
            }
        }
        return null;
    }

    @Override
    public Iterator<K> iterator(){
        return new hmIterator();
    }
    private class hmIterator implements Iterator<K> {
        private int pos;
        private K[] keyArray;
        public hmIterator(){
            pos = 0;
            keyArray = (K[]) keySets.toArray();
            Arrays.sort(keyArray);
        }
        @Override
        public boolean hasNext(){
            return pos < keyArray.length;
        }
        @Override
        public K next() {
            K returnItem = keyArray[pos];
            pos += 1;
            return returnItem;
        }
    }
}
