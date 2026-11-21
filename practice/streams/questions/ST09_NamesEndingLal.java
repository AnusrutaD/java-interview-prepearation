package streams.questions;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

/*
 * ST09 — Names ending / not ending with "lal" (null-safe)
 *
 * QUESTION:
 * Given list = [Ram, Vinitha, Mohanlal, Jawaharlal]:
 *   a) list names ending with "lal"
 *   b) list names NOT ending with "lal"
 *   c) if null is added to the list, avoid a NullPointerException while still listing names not ending with "lal"
 *
 * EXAMPLES:
 *   Input:    ["Ram", "Vinitha", "Mohanlal", "Jawaharlal", null]
 *   Expected: a) [Mohanlal, Jawaharlal]
 *             b/c) [Ram, Vinitha]   (no NullPointerException)
 *
 * HINTS (try without them first):
 *   - endsWith("lal").
 *   - Null check FIRST in the filter (or Objects::nonNull).
 *
 * HOW TO PRACTISE: fill in the method(s) marked TODO, then run it:
 *   IntelliJ: click the green Run arrow next to main()
 *   Terminal (from the practice/ folder):  ./run-streams.sh 09
 * Answer: streams/solutions/ST09_NamesEndingLal.java
 */
public class ST09_NamesEndingLal {

    static List<String> endingWithLal(List<String> names) {
        // TODO: write your Stream API solution here
        return names.stream()
                .filter(str -> str != null && str.length() >= 3 && str.endsWith("lal"))
                .collect(Collectors.toList());
    }

    static List<String> notEndingWithLal(List<String> names) {
        // TODO: write your Stream API solution here
        return names.stream()
                .filter(str -> str != null && (str.length() < 3 || !str.endsWith("lal")))
                .collect(Collectors.toList());
    }

    public static void main(String[] args) {
        System.out.println("ST09 — Names ending / not ending with \"lal\" (null-safe)");
        List<String> names = Arrays.asList("Ram", "Vinitha", "Mohanlal", "Jawaharlal", null);   // List.of() (Java 9+) would also reject null
        check("a) ending with lal", endingWithLal(names), Arrays.asList("Mohanlal", "Jawaharlal"));
        check("b+c) not ending with lal, null-safe", notEndingWithLal(names), Arrays.asList("Ram", "Vinitha"));
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
