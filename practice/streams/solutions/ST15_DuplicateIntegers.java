package streams.solutions;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

/*
 * ST15 — Duplicated integers
 *
 * QUESTION:
 * Return the duplicated integers from nums = [1, 2, 3, 2, 4, 5, 3, 6].
 *
 * WHY / TRAPS:
 *   - Frequency(list, x) inside filter is O(n^2) — avoid.
 *   - The HashSet trick uses a stateful lambda: fine sequentially, not with parallel().
 */
public class ST15_DuplicateIntegers {

    static Set<Integer> solve(List<Integer> nums) {
        return nums.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet().stream()
                .filter(e -> e.getValue() > 1)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
        // alternative: Set<Integer> seen = new HashSet<>();
        //              return nums.stream().filter(n -> !seen.add(n)).collect(Collectors.toSet());
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
