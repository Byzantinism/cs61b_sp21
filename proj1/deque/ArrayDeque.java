package deque;

import java.util.Iterator;
public class ArrayDeque<T> implements Iterable<T>, Deque<T> {
    private T[] items;
    private int size;
    private static int initSize = 8;
    private static int startPos = 3;
    private int nextFirst;
    private int nextLast;
    private static double lowerThreshold = 0.25;
    private static double upperThreshold = 0.75;

    /** Creates an empty list. */
    public ArrayDeque() {
        size = 0;
        nextFirst = startPos - 1;
        nextLast = startPos;
        items = (T[]) new Object[initSize];
    }
    /** Creates a non-empty list. */
    public ArrayDeque(T x) {
        size = 1;
        nextFirst = startPos - 1;
        nextLast = startPos + 1;
        items = (T[]) new Object[initSize];
        items[startPos] = x;
    }
    //Returns true if deque is empty, false otherwise.
    //public boolean isEmpty(){return size == 0;}

    /** Returns the number of items in the list. */
    public int size() {
        return size;
    }
    /** Resizes the underlying array to the target capacity.
     *  For arrays of length 16 or more, your usage factor should always be at least 25%.
     *  This means that before performing a remove operation that will bring the number of elements in the array under 25% the length of the array,
     *  you should resize the size of the array down. For smaller arrays, your usage factor can be arbitrarily low.*/

    /* Whether list needs to resize.
     * 0 = no need.
     * 1 = need big array.
     * -1 = need small array.*/
    private void resizable(int aimSize) {
        double usage = (double) aimSize / (double) items.length;
        if (items.length > 16 && usage < lowerThreshold) {
            resize(items.length / 2);
        }
        if (usage >= upperThreshold) {
            resize(items.length * 2);
        }
    }
    private void resize(int newCapacity){
        T[] tempItem = (T[]) new Object[newCapacity];
        int tempItemNextFirst = newCapacity / 2 - size / 2 - 1;
        int tempItemNextLast = tempItemNextFirst + 1 + size;

        if (nextFirst < nextLast){
            System.arraycopy(items, nextFirst + 1, tempItem, tempItemNextFirst + 1, size);
        }else if (nextFirst > nextLast){
            System.arraycopy(items, nextFirst + 1, tempItem, tempItemNextFirst + 1, items.length - nextFirst - 1);
            System.arraycopy(items, 0, tempItem, tempItemNextFirst + items.length - nextFirst, nextLast);
        }

        items = tempItem;
        nextFirst = tempItemNextFirst;
        nextLast = tempItemNextLast;
    }
     /*
     private void resize(int capacity) {
        Item[] a = (Item[]) new Object[capacity];
        System.arraycopy(items, 0, a, 0, size);
        items = a;
    }*/

    // add item to as the first
    public void addFirst(T x){
        resizable(size + 1);
        size += 1;
        items[nextFirst] = x;
        if (nextFirst == 0){
            nextFirst = items.length - 1;
        }else{
            nextFirst -= 1;
        }
    }
    /** Inserts X into the back of the list.*/
    public void addLast(T x) {
        resizable(size + 1);
        size += 1;
        items[nextLast] = x;
        if (nextLast == items.length - 1){
            nextLast = 0;
        }else{
            nextLast += 1;
        }

    }
    //the real index of i-th item.
    private int indexI(int i){
        if (nextFirst + 1 + i < items.length){
            return nextFirst + 1 + i;
        } else {
            return nextFirst + 1 + i - items.length;
        }
    }
    /** Gets the ith item in the list (0 is the front). */
    public T get(int i) {
        if (isEmpty()){
            System.out.println("This list is empty, nothing can be got");
            return null;
        } else if (i > size - 1) {
            System.out.println("Index is bigger than List size");
            return null;
        }
        return items[indexI(i)];
    }
    //get first item
    public T getFirst() {
        return get(0);
    }
    /** Returns the item from the back of the list. */
    public T getLast() {
        return get(size - 1);
    }
    //remove the first item
    public T removeFirst(){
        if (isEmpty()){
            System.out.println("This list is empty, nothing can be removed");
            return null;
        }
        resizable(size - 1);
        if (nextFirst == items.length - 1){
            nextFirst = 0;
        } else{
            nextFirst = nextFirst + 1;
        }
        T returnItem = items[nextFirst];
        items[nextFirst] = null;
        size -= 1;
        return returnItem;
    }
    /** Deletes item from back of the list and returns deleted item. */
    public T removeLast() {
        if (isEmpty()){
            System.out.println("This list is empty, nothing can be removed");
            return null;
        }
        resizable(size - 1);
        if (nextLast == 0){
            nextLast = items.length - 1;
        } else{
            nextLast = nextLast - 1;
        }
        T returnItem = items[nextLast];
        items[nextLast] = null;
        size -= 1;
        return returnItem;
    }

    @Override
    public Iterator<T> iterator(){
        return new ArrayIterator();
    }
    private class ArrayIterator implements Iterator<T> {
        private int pos;
        public ArrayIterator(){
            pos = 0;
        }
        @Override
        public boolean hasNext(){
            return pos < size;
        }
        @Override
        public T next(){
            T returnItem =  get(pos);
            pos += 1;
            return returnItem;
        }
    }
    @Override
    public boolean equals(Object o){
        if (o == null){ return false; }
        if (this == o){ return true; }
        if (!(o instanceof Deque)){ return false; }

        Deque<T> other = (Deque<T>) o;
        if (this.size() != other.size()){ return false; }
        for (int i = 0; i < this.size(); i++){
            if (this.get(i) != other.get(i)){ return false; }
        }
        return true;
    }

    public void printDeque(){
        if (isEmpty()){
            System.out.println("This ArrayDeque list is empty");
            return;
        }
        System.out.print("[ ");
        for (T x: this){
            System.out.print(x + " ");
        }
        System.out.println("]");
    }
}
