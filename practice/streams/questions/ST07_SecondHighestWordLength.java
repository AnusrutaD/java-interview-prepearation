package streams.questions;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

/*
 * ST07 — Second-highest distinct word length
 *
 * QUESTION:
 * For sentence = "I am working in motivated organization", return the second-highest DISTINCT word length.
 *
 * HINTS (try without them first):
 *   - Map words to their lengths.
 *   - distinct() BEFORE sorting, then skip(1).
 *
 * HOW TO PRACTISE: fill in the method(s) marked TODO, then run it:
 *   IntelliJ: click the green Run arrow next to main()
 *   Terminal (from the practice/ folder):  ./run-streams.sh 07
 * Answer: streams/solutions/ST07_SecondHighestWordLength.java
 */
public class ST07_SecondHighestWordLength {

    static int solve(String sentence) {
        // TODO: write your Stream API solution here
        return -1;
    }

    public static void main(String[] args) {
        System.out.println("ST07 — Second-highest distinct word length");
        check("sentence", solve("I am working in motivated organization"), 9);
        check("tie: aaaa bbbb cc", solve("aaaa bbbb cc"), 2);
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
