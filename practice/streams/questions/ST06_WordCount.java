package streams.questions;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

/*
 * ST06 — Count each word
 *
 * QUESTION:
 * Given words = ["apple","banana","apple","orange","banana"], count how many times each word appears.
 *
 * HINTS (try without them first):
 *   - Same pattern as ST01, but the elements are already Strings.
 *
 * HOW TO PRACTISE: fill in the method(s) marked TODO, then run it:
 *   IntelliJ: click the green Run arrow next to main()
 *   Terminal (from the practice/ folder):  ./run-streams.sh 06
 * Answer: streams/solutions/ST06_WordCount.java
 */
public class ST06_WordCount {

    static Map<String, Long> solve(List<String> words) {
        // TODO: write your Stream API solution here
        return null;
    }

    public static void main(String[] args) {
        System.out.println("ST06 — Count each word");
        check("words", solve(Arrays.asList("apple", "banana", "apple", "orange", "banana")),
              mapOf("apple", 2L, "banana", 2L, "orange", 1L));
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
