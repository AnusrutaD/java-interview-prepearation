package streams.solutions;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

/*
 * ST11 — Unique characters
 *
 * QUESTION:
 * Find the unique characters in input = "hello world" using the Stream API.
 *   a) distinct characters (each char once)
 *   b) characters that appear EXACTLY once, in original order  (interviewers often mean this one - ask!)
 *
 * EXAMPLES:
 *   Input:    "hello world"
 *   Expected: a) {h, e, l, o, ' ', w, r, d}   (any order)
 *             b) [h, e, ' ', w, r, d]          (in this order)
 *
 * WHY / TRAPS:
 *   - Ask the interviewer which meaning of 'unique' they want.
 *   - LinkedHashMap::new as the 2nd argument of groupingBy keeps first-seen order.
 */
public class ST11_UniqueChars {

    static Set<Character> distinctChars(String input) {
        return input.chars().mapToObj(c -> (char) c).collect(Collectors.toSet());
    }

    static List<Character> appearingOnce(String input) {
        return input.chars()
                .mapToObj(c -> (char) c)
                .collect(Collectors.groupingBy(Function.identity(), LinkedHashMap::new, Collectors.counting()))
                .entrySet().stream()
                .filter(e -> e.getValue() == 1)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public static void main(String[] args) {
        System.out.println("ST11 — Unique characters");
        check("a) distinct", distinctChars("hello world"), setOf('h', 'e', 'l', 'o', ' ', 'w', 'r', 'd'));
        check("b) appearing once, in order", appearingOnce("hello world"), Arrays.asList('h', 'e', ' ', 'w', 'r', 'd'));
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
