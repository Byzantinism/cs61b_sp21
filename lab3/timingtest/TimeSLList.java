package timingtest;
import edu.princeton.cs.algs4.Stopwatch;

/**
 * Created by hug.
 */
public class TimeSLList {
    private static void printTimingTable(AList<Integer> Ns, AList<Double> times, AList<Integer> opCounts) {
        System.out.printf("%12s %12s %12s %12s\n", "N", "time (s)", "# ops", "microsec/op");
        System.out.printf("------------------------------------------------------------\n");
        for (int i = 0; i < Ns.size(); i += 1) {
            int N = Ns.get(i);
            double time = times.get(i);
            int opCount = opCounts.get(i);
            double timePerOp = time / opCount * 1e6;
            System.out.printf("%12d %12.2f %12d %12.2f\n", N, time, opCount, timePerOp);
        }
    }

    public static void main(String[] args) {
        timeGetLast();
    }

    public static void timeGetLast() {
        // TODO: YOUR CODE HERE
        int[] row = new int[]{1000, 2000, 4000, 8000, 16000, 32000, 64000, 128000};
        int get_count = 10000;
        AList<Integer> Ns = new AList<>();
        AList<Double> times = new AList<>();
        AList<Integer> opCounts = new AList<>();

        for (int i=0; i<=7; i++){
            Ns.addLast(row[i]);
            opCounts.addLast(get_count);
            times.addLast(helper_get(row[i], get_count));
        }
        printTimingTable(Ns, times, opCounts);
    }
    public static double helper_get(int size, int get_count) {
        SLList<Integer> lst = new SLList<>();
        double time_consumed;

        for (int i = 1; i <= size; i++) {
            lst.addLast(i);
        }
        int i = 1;
        Stopwatch sw = new Stopwatch();
        while (i <= get_count) {
            lst.getLast();
            i += 1;
        }
        return sw.elapsedTime();
    }
}
