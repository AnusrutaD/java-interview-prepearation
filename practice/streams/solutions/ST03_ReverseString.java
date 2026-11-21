package streams.solutions;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

/*
 * ST03 — Reverse a String
 *
 * QUESTION:
 * Reverse the string str = "Stream" using the Stream API.
 *
 * WHY / TRAPS:
 *   - String.valueOf((char) c) — reduce needs Strings to concatenate.
 *   - Other answers: IntStream.range(0, n).map(i -> n - 1 - i) + charAt; or new StringBuilder(str).reverse() (no stream).
 */
public class ST03_ReverseString {

    static String solve(String str) {
        return str.chars()
                .mapToObj(c -> String.valueOf((char) c))                   // String, not Character
                .reduce("", (acc, ch) -> ch + acc);
    }

    public static void main(String[] args) {
        System.out.println("ST03 — Reverse a String");
        check("Stream", solve("Stream"), "maertS");
        check("empty", solve(""), "");
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
