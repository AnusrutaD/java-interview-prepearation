package streams.questions;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

/*
 * ST15 — Duplicated integers
 *
 * QUESTION:
 * Return the duplicated integers from nums = [1, 2, 3, 2, 4, 5, 3, 6].
 *
 * HINTS (try without them first):
 *   - Same pattern as ST05, with Integers.
 *   - Or a HashSet whose add() returns false for a repeat.
 *
 * HOW TO PRACTISE: fill in the method(s) marked TODO, then run it:
 *   IntelliJ: click the green Run arrow next to main()
 *   Terminal (from the practice/ folder):  ./run-streams.sh 15
 * Answer: streams/solutions/ST15_DuplicateIntegers.java
 */
public class ST15_DuplicateIntegers {

    static Set<Integer> solve(List<Integer> nums) {
        // TODO: write your Stream API solution here
        return null;
    }

    public static void main(String[] args) {
        System.out.println("ST15 — Duplicated integers");
        check("nums", solve(Arrays.asList(1, 2, 3, 2, 4, 5, 3, 6)), setOf(2, 3));
        check("no duplicates", solve(Arrays.asList(1, 2, 3)), setOf());
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
