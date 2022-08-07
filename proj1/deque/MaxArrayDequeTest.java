package deque;
import org.junit.Test;
import static org.junit.Assert.*;
import edu.princeton.cs.algs4.StdRandom;

import java.util.Comparator;

/* You’ll likely be creating multiple Comparator<T> classes to test your code: this is the point!
 * To get practice using Comparator objects to do something useful (find the maximum element)
 * and to get practice writing your own Comparator classes.
 */
public class MaxArrayDequeTest {
    Comparator<String> comp1 = new Comparator<String>() {
        @Override
        public int compare(String o1, String o2) {
            return o1.compareTo(o2);
        }
    };

    Comparator<Integer> comp2 = new Comparator<Integer>() {
        @Override
        public int compare(Integer o1, Integer o2) {
            return o1.compareTo(o2);
        }
    };
    @Test
    public void addIsEmptySizeTest() {

        MaxArrayDeque<String> lld1 = new MaxArrayDeque<>(comp1);

        assertTrue("A newly initialized LLDeque should be empty", lld1.isEmpty());
        lld1.addFirst("front");

        // The && operator is the same as "and" in Python.
        // It's a binary operator that returns true if both arguments true, and false otherwise.
        assertEquals(1, lld1.size());
        assertFalse("lld1 should now contain 1 item", lld1.isEmpty());

        lld1.addLast("middle");
        assertEquals(2, lld1.size());

        lld1.addLast("back");
        assertEquals(3, lld1.size());

        System.out.println("Printing out deque: ");
        lld1.printDeque();
        assertEquals("The max item should be middle","middle", lld1.max());

    }

    @Test
    /** Adds an item, then removes an item, and ensures that dll is empty afterwards. */
    public void addRemoveTest() {

        MaxArrayDeque<Integer> lld1 = new MaxArrayDeque<Integer>(comp2);
        // should be empty
        assertTrue("lld1 should be empty upon initialization", lld1.isEmpty());

        lld1.addFirst(10);
        // should not be empty
        assertFalse("lld1 should contain 1 item", lld1.isEmpty());

        lld1.removeFirst();
        // should be empty
        assertTrue("lld1 should be empty after removal", lld1.isEmpty());
        assertEquals("Empty list don't have max item",null, lld1.max());

    }
}
