package streams.solutions;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

/*
 * ST10 — Second-largest number
 *
 * QUESTION:
 * Find the second-largest number in numbers = [10, 5, 15, 20, 8] using the Stream API.
 *
 * WHY / TRAPS:
 *   - Without distinct(), [20, 20, 15] returns 20.
 *   - Mention complexity: sorting is O(n log n); a single pass with two variables is O(n).
 */
public class ST10_SecondLargest {

    static Integer solve(List<Integer> numbers) {
        return numbers.stream()
                .distinct()
                .sorted(Comparator.reverseOrder())
                .skip(1)
                .findFirst()
                .orElse(null);
    }

    public static void main(String[] args) {
        System.out.println("ST10 — Second-largest number");
        check("numbers", solve(Arrays.asList(10, 5, 15, 20, 8)), 15);
        check("duplicate max [20,20,15]", solve(Arrays.asList(20, 20, 15)), 15);
        check("single element", solve(Arrays.asList(7)), null);
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
