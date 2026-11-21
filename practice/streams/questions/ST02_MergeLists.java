package streams.questions;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

/*
 * ST02 — Merge two lists
 *
 * QUESTION:
 * Merge list1 = [1,2,3,4] and list2 = [5,6,7,8] using the Stream API.
 *
 * EXAMPLES:
 *   Input:    list1 = [1, 2, 3, 4], list2 = [5, 6, 7, 8]
 *   Expected: [1, 2, 3, 4, 5, 6, 7, 8]
 *
 * HINTS (try without them first):
 *   - Stream has a static method that joins two streams.
 *   - Or: stream of lists + flatMap.
 *
 * HOW TO PRACTISE: fill in the method(s) marked TODO, then run it:
 *   IntelliJ: click the green Run arrow next to main()
 *   Terminal (from the practice/ folder):  ./run-streams.sh 02
 * Answer: streams/solutions/ST02_MergeLists.java
 */
public class ST02_MergeLists {

    static List<Integer> solve(List<Integer> list1, List<Integer> list2) {
        // TODO: write your Stream API solution here
        return Stream.concat(list1.stream(), list2.stream())
                .collect(Collectors.toList());
    }

    public static void main(String[] args) {
        System.out.println("ST02 — Merge two lists");
        check("merge", solve(Arrays.asList(1, 2, 3, 4), Arrays.asList(5, 6, 7, 8)), Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8));
        summary();
    }

    // ---------- checker (don't edit) ----------
    static int pass = 0, fail = 0;
    static void check(String label, Object actual, Object expected) {
        boolean ok = java.util.Objects.equals(actual, expected);
        if (ok) pass++; else fail++;
        System.out.println((ok ? "  PASS  " : "  FAIL  ") + label + "\n        expected: " + expected + "\n        actual:   " + actual);
    }
    @SafeVarargs static <T> Set<T> setOf(T... items) { return new HashSet<>(Arrays.asList(items)); }   // Java 8 has no Set.of
    @SuppressWarnings("unchecked")
    static <K, V> Map<K, V> mapOf(Object... kv) {                                                       // Java 8 has no Map.of
        Map<K, V> m = new HashMap<>();
        for (int i = 0; i < kv.length; i += 2) m.put((K) kv[i], (V) kv[i + 1]);
        return m;
    }
    static void summary() { System.out.println(pass + " passed, " + fail + " failed" + (fail == 0 ? "  \u2705" : "")); }
}
