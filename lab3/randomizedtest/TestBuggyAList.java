package randomizedtest;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by hug.
 */
public class TestBuggyAList {
  // YOUR TESTS HERE
  @Test
  public void testThreeAddThreeRemove() {
      AListNoResizing<Integer> Correct_one = new AListNoResizing<>();
      AListNoResizing<Integer> Buggy_one = new AListNoResizing<>();

      Correct_one.addLast(4);
      Correct_one.addLast(5);
      Correct_one.addLast(6);

      Buggy_one.addLast(4);
      Buggy_one.addLast(5);
      Buggy_one.addLast(6);

      assertEquals(Correct_one.removeLast(), Buggy_one.removeLast());
      assertEquals(Correct_one.removeLast(), Buggy_one.removeLast());
      assertEquals(Correct_one.removeLast(), Buggy_one.removeLast());
  }

  @Test
  public void randomizedTest(){
      AListNoResizing<Integer> L = new AListNoResizing<>();
      BuggyAList<Integer> B = new BuggyAList<>();

      int N = 5000;
      for (int i = 0; i < N; i += 1) {
          int operationNumber = StdRandom.uniform(0, 4);
          if (operationNumber == 0) {
              // addLast
              int randVal = StdRandom.uniform(0, 100);
              L.addLast(randVal);
              B.addLast(randVal);
              System.out.println("addLast(" + randVal + ")");

          } else if (operationNumber == 1) {
              // size
              int size1 = L.size();
              int size2 = B.size();
              System.out.println("L_size: " + size1);
              System.out.println("B_size: " + size2);

          } else if (operationNumber == 2 && L.size() != 0 && B.size() != 0){
              int last1 = L.getLast();
              int last2 = B.getLast();
              System.out.println("L_Last item: " + last1);
              System.out.println("B_Last item: " + last2);

          } else if (operationNumber == 3 && L.size() != 0 && B.size() != 0){
              int remove_last_L = L.removeLast();
              int remove_last_B = B.removeLast();
              System.out.println("L Last item removed: " + remove_last_L);
              System.out.println("B Last item removed: " + remove_last_B);
          }
      }
  }
}
