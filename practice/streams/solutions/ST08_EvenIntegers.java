package streams.solutions;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

/*
 * ST08 — Extract even integers from mixed strings
 *
 * QUESTION:
 * From list = ["one","5","ab12","abc","8","20"], extract only the even integers using the Stream API.
 *
 * EXAMPLES:
 *   Input:    ["one", "5", "ab12", "abc", "8", "20"]
 *   Expected: [8, 20]
 *
 * WHY / TRAPS:
 *   - Integer.parseInt("ab12") throws NumberFormatException — filter first.
 *   - Negative numbers? use "-?\\d+".
 */
public class ST08_EvenIntegers {

    static List<Integer> solve(List<String> list) {
        return list.stream()
                .filter(s -> s != null && s.matches("\\d+"))                  // "ab12" is NOT a number
                .map(Integer::parseInt)
                .filter(n -> n % 2 == 0)
                .collect(Collectors.toList());
    }

    public static void main(String[] args) {
        System.out.println("ST08 — Extract even integers from mixed strings");
        check("mixed", solve(Arrays.asList("one", "5", "ab12", "abc", "8", "20")), Arrays.asList(8, 20));
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
