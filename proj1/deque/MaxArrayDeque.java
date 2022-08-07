package deque;

import java.util.Comparator;

public class MaxArrayDeque<T> extends ArrayDeque<T> {
    private Comparator<T> comp;
    // creates a MaxArrayDeque with the given Comparator.
    public MaxArrayDeque(Comparator<T> c){
        super();
        comp = c;
    }
    // returns the maximum element in the deque as governed by the previously given Comparator.
    // If the MaxArrayDeque is empty, simply return null.
    public T max(){
        if (isEmpty()){ return null;}
        T maxitem = this.get(0);
        for (T x: this){
            if (comp.compare(x, maxitem) > 0){
                maxitem = x;
            }
        }
        return maxitem;
    }
    // returns the maximum element in the deque as governed by the parameter Comparator c.
    // If the MaxArrayDeque is empty, simply return null.
    public T max(Comparator<T> c){
        if (isEmpty()){return null;}
        T maxitem = this.get(0);
        for (T x: this){
            if (c.compare(x, maxitem) > 0){
                maxitem = x;
            }
        }
        return maxitem;
    }
}

