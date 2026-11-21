package streams.solutions;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

/*
 * ST07 — Second-highest distinct word length
 *
 * QUESTION:
 * For sentence = "I am working in motivated organization", return the second-highest DISTINCT word length.
 *
 * WHY / TRAPS:
 *   - Without distinct(), 'aaaa bbbb cc' returns 4 (the tie), not 2.
 *   - Second highest = sort descending + skip(1) + findFirst().
 */
public class ST07_SecondHighestWordLength {

    static int solve(String sentence) {
        return Arrays.stream(sentence.split("\\s+"))
                .map(String::length)
                .distinct()                                                // ties must not count twice
                .sorted(Comparator.reverseOrder())
                .skip(1)
                .findFirst()
                .orElse(-1);
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
