package deque;

public class ArrayDeque<Item> {
    private Item[] items;
    private int size;
    private static int init_size = 8;
    private static int startPos = 3;
    private int nextFirst;
    private int nextLast;

    /** Creates an empty list. */
    public ArrayDeque() {
        size = 0;
        nextFirst = startPos - 1;
        nextLast = startPos;
        items = (Item[]) new Object[init_size];
    }
    /** Creates a non-empty list. */
    public ArrayDeque(Item x) {
        size = 1;
        nextFirst = startPos - 1;
        nextLast = startPos + 1;
        items = (Item[]) new Object[init_size];
        items[startPos] = x;
    }
    //Returns true if deque is empty, false otherwise.
    public boolean isEmpty(){
        return size == 0;
    }
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
    private int resize_or_not(){
        double LowerThreshold = 0.25;
        double UpperThreshold = 0.75;
        double usage =  (double) (items.length - size) / (double) items.length;
        if (items.length > 16 && usage < LowerThreshold){
            return -1;
        }
        if (usage >= UpperThreshold){
            return 1;
        }
        return 0;
    }

     /*
     private void resize(int capacity) {
        Item[] a = (Item[]) new Object[capacity];
        System.arraycopy(items, 0, a, 0, size);
        items = a;
    }*/

    // add item to as the first
    public void addFirst(Item x){
        //resize_or_not()
        size += 1;
        items[nextFirst] = x;
        if (nextFirst == 0){
            nextFirst = items.length - 1;
        }else{
            nextFirst -= 1;
        }
    }
    /** Inserts X into the back of the list.*/
    public void addLast(Item x) {
        /* wait to modify
        if (size == items.length) {
            resize(size * 2);
        }*/
        size += 1;
        items[nextLast] = x;
        if (nextLast == items.length - 1){
            nextLast = 0;
        }else{
            nextLast += 1;
        }

    }
    //the real index of i-th item.
    private int index_i(int i){
        if (startPos + i <= items.length){
            return startPos + i;
        } else {
            return startPos + i - (items.length + 1);
        }
    }
    /** Gets the ith item in the list (0 is the front). */
    public Item get(int i) {
        if (isEmpty()){
            System.out.println("This list is empty, nothing can be got");
            return null;
        } else if (i > size - 1) {
            System.out.println("Index is bigger than List size");
            return null;
        }
        return items[index_i(i)];
    }
    //get first item
    public Item getFirst() {
        if (isEmpty()) {
            System.out.println("This list is empty, nothing can be got");
            return null;
        }
        if (nextFirst == items.length - 1){
            return items[0];}
        return items[nextFirst + 1];
    }
    /** Returns the item from the back of the list. */
    public Item getLast() {
        if (isEmpty()) {
            System.out.println("This list is empty, nothing can be got");
            return null;
        }
        if (nextLast == 0){
            return items[items.length - 1];}
        return items[nextLast - 1];
    }
    //remove the first item
    public Item removeFirst(){
        if (isEmpty()){
            System.out.println("This list is empty, nothing can be removed");
            return null;
        }
        //resize_or_not()
        if (nextFirst == items.length - 1){
            nextFirst = 0;
        } else{
            nextFirst = nextFirst + 1;
        }
        Item returnItem = items[nextFirst];
        items[nextFirst] = null;
        size -= 1;
        return returnItem;
    }
    /** Deletes item from back of the list and returns deleted item. */
    public Item removeLast() {
        if (isEmpty()){
            System.out.println("This list is empty, nothing can be removed");
            return null;
        }
        //resize_or_not()
        if (nextLast == 0){
            nextLast = items.length - 1;
        } else{
            nextLast = nextLast - 1;
        }
        Item returnItem = items[nextLast];
        items[nextLast] = null;
        size -= 1;
        return returnItem;
    }
}
