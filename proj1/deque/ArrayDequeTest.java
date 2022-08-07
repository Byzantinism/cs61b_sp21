package deque;
import org.junit.Test;
import static org.junit.Assert.*;
import edu.princeton.cs.algs4.StdRandom;

public class ArrayDequeTest {
    //TESTCASES HERE
    @Test
    public void testThreeAddThreeRemove() {
        AList<Integer> reference = new AList<>();
        ArrayDeque<Integer> result = new ArrayDeque<>();

        reference.addLast(4);
        reference.addLast(5);
        reference.addLast(6);

        result.addLast(4);
        result.addLast(5);
        result.addLast(6);

        assertEquals(reference.removeLast(), result.removeLast());
        assertEquals(reference.removeLast(), result.removeLast());
        assertEquals(reference.removeLast(), result.removeLast());
    }

    @Test
    public void randomizedTest(){
        LinkedListDeque<Integer> reference = new LinkedListDeque<>();
        ArrayDeque<Integer> result = new ArrayDeque<>();

        int N = 5000;
        for (int i = 0; i < N; i += 1) {
            int operationNumber = StdRandom.uniform(0, 7);
            if (operationNumber == 0) {
                // size
                int size1 = reference.size();
                int size2 = result.size();
                //System.out.println("reference_size: " + size1);
                //System.out.println("result_size: " + size2);
                assertEquals("Should have the same size", size1, size2);

            } else if (operationNumber == 1) {
                // && result.size() != 8
                // addLast
                int randVal = StdRandom.uniform(0, 100);
                reference.addLast(randVal);
                result.addLast(randVal);
                //System.out.println("addLast(" + randVal + ")");

            } else if (operationNumber == 2 && reference.size() != 0 && result.size() != 0){
                // getLast
                int last1 = reference.getLast();
                int last2 = result.getLast();
                //System.out.println("reference_Last item: " + last1);
                //System.out.println("result_Last item: " + last2);
                assertEquals("Should have the same last item", last1, last2);
                //assertEquals("Should have the same last item", reference.getLast(), result.getLast());

            } else if (operationNumber == 3 && reference.size() != 0 && result.size() != 0){
                // remove Last
                int remove_last_1 = reference.removeLast();
                int remove_last_2 = result.removeLast();
                //System.out.println("reference Last item removed: " + remove_last_1);
                //System.out.println("result Last item removed: " + remove_last_2);
                assertEquals("Should remove the same last item", remove_last_1, remove_last_2);
                //assertEquals("Should remove the same last item", reference.removeLast(), result.removeLast());

            } else if (operationNumber == 4){
                //&& result.size() != 8
                //addFirst
                int randVal = StdRandom.uniform(0, 100);
                reference.addFirst(randVal);
                result.addFirst(randVal);

            } else if (operationNumber == 5 && reference.size() != 0 && result.size() != 0){
                //getFirst
                int first1 = reference.getFirst();
                int first2 = result.getFirst();
                assertEquals("Should have the same first item", first1, first2);
                //assertEquals("Should have the same first item", reference.getFirst(), result.getFirst());

            } else if (operationNumber == 6 && reference.size() != 0 && result.size() != 0){
                //removeFirst
                int remove_first_1 = reference.removeFirst();
                int remove_first_2 = result.removeFirst();
                assertEquals("Should remove the same first item", remove_first_1, remove_first_2);
                //assertEquals("Should remove the same first item", reference.removeFirst(), result.removeFirst());

            } else if (operationNumber == 7 && reference.size() != 0 && result.size() != 0){
                //get(i)
                int randVal = StdRandom.uniform(0, 100);
                int ith1 = reference.get(randVal);
                int ith2 = result.get(randVal);
                assertEquals("Should have the same i-th item", ith1, ith2);
                //assertEquals("Should have the same i-th item", reference.get(randVal), result.get(randVal));
            }
        }
    }
}
