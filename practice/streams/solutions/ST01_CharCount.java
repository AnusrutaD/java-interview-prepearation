package streams.solutions;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

/*
 * ST01 — Count every character
 *
 * QUESTION:
 * Print the count of every character in str = "abcdabcghsk" using the Stream API.
 *
 * EXAMPLES:
 *   Input:    "abcdabcghsk"
 *   Expected: {a=2, b=2, c=2, d=1, g=1, h=1, s=1, k=1}   (map order doesn't matter)
 *
 * WHY / TRAPS:
 *   - mapToObj(c -> (char) c) — without the cast you get Integer keys (97=2, ...).
 *   - counting() returns Long, so the map is Map<Character, Long>.
 */
public class ST01_CharCount {

    static Map<Character, Long> solve(String str) {
        return str.chars()
                .mapToObj(c -> (char) c)                                   // IntStream -> Stream<Character>
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
    }

    public static void main(String[] args) {
        System.out.println("ST01 — Count every character");
        check("abcdabcghsk", solve("abcdabcghsk"),
              mapOf('a', 2L, 'b', 2L, 'c', 2L, 'd', 1L, 'g', 1L, 'h', 1L, 's', 1L, 'k', 1L));
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
