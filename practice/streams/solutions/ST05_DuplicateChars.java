package streams.solutions;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

/*
 * ST05 — Find duplicate characters
 *
 * QUESTION:
 * List every duplicate character in s = "programming".
 *
 * WHY / TRAPS:
 *   - .entrySet().stream() after groupingBy — one of your 3 recurring mistakes.
 *   - Set-based alternative: Set<Character> seen = new HashSet<>(); filter(c -> !seen.add(c)) (stateful lambda, fine in an interview if you say so).
 */
public class ST05_DuplicateChars {

    static Set<Character> solve(String s) {
        return s.chars()
                .mapToObj(c -> (char) c)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet().stream()                                        // Map -> Stream<Entry>
                .filter(e -> e.getValue() > 1)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    public static void main(String[] args) {
        System.out.println("ST05 — Find duplicate characters");
        check("programming", solve("programming"), setOf('r', 'g', 'm'));
        check("abc (none)", solve("abc"), setOf());
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
