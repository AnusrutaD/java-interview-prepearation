package streams.solutions;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

/*
 * ST16 — Streams on int[]
 *
 * QUESTION:
 * Can you call .stream() directly on an int[] array?  (Answer: NO — arrays have no methods.)
 * Practise: a) sum of int[] with a stream   b) int[] -> List<Integer>
 *
 * EXAMPLES:
 *   Input:    int[] arr = {3, 1, 2}
 *   Expected: a) sum = 6
 *             b) List<Integer> [3, 1, 2]
 *
 * WHY / TRAPS:
 *   - Arrays.asList(int[]) gives List<int[]> (one element!), not List<Integer>.
 *   - IntStream has sum(), average(), max() built in; Stream<Integer> needs mapToInt first.
 */
public class ST16_PrimitiveArrayStream {

    static int sum(int[] arr) {
        return Arrays.stream(arr).sum();                                  // or IntStream.of(arr).sum()
    }

    static List<Integer> toList(int[] arr) {
        return Arrays.stream(arr).boxed().collect(Collectors.toList());
    }

    public static void main(String[] args) {
        System.out.println("ST16 — Streams on int[]");
        int[] arr = {3, 1, 2};
        check("a) sum", sum(arr), 6);
        check("b) boxed list", toList(arr), Arrays.asList(3, 1, 2));
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
