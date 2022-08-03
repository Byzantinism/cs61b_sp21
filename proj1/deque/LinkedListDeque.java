package deque;

import java.util.Iterator;

/** ADDED 2/12: You should not have your Deque interface implement Iterable but rather just the two implementations LinkedListDeque and ArrayDeque.
 *  If you do the former, our autograder will give you API errors. */

public class LinkedListDeque<T> implements Iterable<T> {
    // Node List
    private class Node {
        public T item;
        public Node next;
        public Node prev;

        public Node (T i){
            prev = null;
            item = i;
            next = null;
        }
        public Node (T i, Node n) {
            prev = null;
            item = i;
            next = n;
        }
        public Node (Node i0, T i, Node n){
            prev = i0;
            item = i;
            next = n;
        }

    }
    //Class variables
    private Node sentinel;
    private int size;

    //Creates an empty linked list deque.
    public LinkedListDeque(){
        size = 0;
        sentinel = new Node(null);
        sentinel.next = sentinel;
        sentinel.prev = sentinel;
    }

    //List Constructor(Not empty)
    public LinkedListDeque(T item){
        size = 1;
        sentinel = new Node(null);
        sentinel.next = new Node(sentinel, item, sentinel);
        sentinel.prev = sentinel.next;
    }

    // Adds an item of type T to the front of the deque. You can assume that item is never null.
    public void addFirst(T item){
        size += 1;
        Node Temp = sentinel.next;
        sentinel.next = new Node(sentinel, item, Temp);
        Temp.prev = sentinel.next;
    }

    //Adds an item of type T to the back of the deque. You can assume that item is never null.
    public void addLast(T item){
        size += 1;
        Node Temp = sentinel.prev;
        Temp.next = new Node(Temp, item, sentinel);
        sentinel.prev = Temp.next;
    }

    //Returns true if deque is empty, false otherwise.
    public boolean isEmpty(){
        return size == 0;
    }

    //Returns the number of items in the deque.
    public int size(){
        return size;
    }

    //Prints the items in the deque from first to last, separated by a space. Once all the items have been printed, print out a new line.
    public void printDeque(){
        if (isEmpty()){
            System.out.println("This list is empty");
            return;
        }
        Node p = sentinel.next;
        System.out.print("[ ");
        while(p != sentinel){
            System.out.print(p.item + " ");
            p = p.next;
        }
        System.out.println("]");
    }

    //Removes and returns the item at the front of the deque. If no such item exists, returns null.
    public T removeFirst(){
        if (isEmpty()){
            System.out.println("This list is empty, nothing can be removed");
            return null;
        }
        size -= 1;
        Node Temp = sentinel.next;
        sentinel.next = Temp.next;
        Temp.next.prev = sentinel;
        return Temp.item;
    }

    //Removes and returns the item at the back of the deque. If no such item exists, returns null.
    public T removeLast(){
        if (isEmpty()){
            System.out.println("This list is empty, nothing can be removed");
            return null;
        }
        size -= 1;
        Node Temp = sentinel.prev;
        sentinel.prev = Temp.prev;
        Temp.prev.next = sentinel;
        return Temp.item;
    }

    //Gets the item at the given index, where 0 is the front, 1 is the next item, and so forth. If no such item exists, returns null. Must not alter the deque!
    public T get(int index){
        if (isEmpty()){
            System.out.println("This list is empty, nothing can be got");
            return null;
        } else if (index > size - 1) {
            System.out.println("Index is bigger than List size");
            return null;
        }
        Node Temp = sentinel.next;
        while(index > 0){
            Temp = Temp.next;
            index -= 1;
        }
        return Temp.item;
    }

    //Same as get, but uses recursion.
    public T getRecursive(int index){
        if (isEmpty()){
            System.out.println("This list is empty, nothing can be got");
            return null;
        } else if (index > size - 1) {
            System.out.println("Index is bigger than List size");
            return null;
        }
        return getRecursive_helper(index, sentinel.next);
    }

    //Helper function for getRecursive
    private T getRecursive_helper(int index, Node location){
        if (index == 0){
            return location.item;
        }
        return getRecursive_helper(index - 1, location.next);
    }

    @Override
    //The Deque objects we’ll make are iterable (i.e. Iterable<T>) so we must provide this method to return an iterator.
    public Iterator<T> iterator(){
        return new List_Iterator();
    }

    //Really Iterator function. The Iterator<T> returned should have a useful hasNext() and next() method.
    private class List_Iterator implements Iterator<T> {
        private int pos;

        public List_Iterator(){
            pos = 0;
        }
        @Override
        public boolean hasNext(){
            return pos < size;
        }
        @Override
        public T next(){
            T return_item =  get(pos);
            pos += 1;
            return return_item;
        }
    }

    @Override
    /** Returns whether or not the parameter o is equal to the Deque.
     *  o is considered equal if it is a Deque and if it contains the same contents (as goverened by the generic T’s equals method) in the same order.
     *  (ADDED 2/12: You’ll need to use the instance of keywords for this. Read here for more information)*/
    public boolean equals(Object o){
        if (o == null){ return false;}
        if (this == o){ return true;}
        if (this.getClass() != o.getClass()){return false;}

        LinkedListDeque<T> other = (LinkedListDeque<T>) o;
        if (this.size() != other.size()){return false;}
        for (int i = 0; i < this.size(); i++){
            if (this.get(i) != other.get(i)){return false;}
        }
        return true;
    }
}