package streams.questions;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

/*
 * ST16 — Streams on int[]
 *
 * QUESTION:
 * Can you call .stream() directly on an int[] array?  (Answer: NO — arrays have no methods.)
 * Practise: a) sum of int[] with a stream   b) int[] -> List<Integer>
 *
 * EXAMPLES:
 *   Input:    int[] arr = {3, 1, 2}
 *   Expected: a) sum = 6
 *             b) List<Integer> [3, 1, 2]
 *
 * HINTS (try without them first):
 *   - Arrays.stream(int[]) returns an IntStream.
 *   - IntStream -> Stream<Integer> with boxed().
 *
 * HOW TO PRACTISE: fill in the method(s) marked TODO, then run it:
 *   IntelliJ: click the green Run arrow next to main()
 *   Terminal (from the practice/ folder):  ./run-streams.sh 16
 * Answer: streams/solutions/ST16_PrimitiveArrayStream.java
 */
public class ST16_PrimitiveArrayStream {

    static int sum(int[] arr) {
        // TODO: write your Stream API solution here
        return -1;
    }

    static List<Integer> toList(int[] arr) {
        // TODO: write your Stream API solution here
        return null;
    }

    public static void main(String[] args) {
        System.out.println("ST16 — Streams on int[]");
        int[] arr = {3, 1, 2};
        check("a) sum", sum(arr), 6);
        check("b) boxed list", toList(arr), Arrays.asList(3, 1, 2));
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
