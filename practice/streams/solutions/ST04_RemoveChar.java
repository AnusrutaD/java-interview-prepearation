package streams.solutions;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

/*
 * ST04 — Delete every occurrence of a character
 *
 * QUESTION:
 * Delete every occurrence of 'c' from s = "occurance" and print the result.
 *
 * EXAMPLES:
 *   Input:    s = "occurance", remove = 'c'
 *   Expected: "ourane"
 *
 * WHY / TRAPS:
 *   - joining() only works on Stream<String> — convert with String.valueOf((char) c) first.
 *   - No terminal operation = nothing runs.
 */
public class ST04_RemoveChar {

    static String solve(String s, char remove) {
        return s.chars()
                .filter(c -> c != remove)
                .mapToObj(c -> String.valueOf((char) c))
                .collect(Collectors.joining());
    }

    public static void main(String[] args) {
        System.out.println("ST04 — Delete every occurrence of a character");
        check("occurance without c", solve("occurance", 'c'), "ourane");
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
