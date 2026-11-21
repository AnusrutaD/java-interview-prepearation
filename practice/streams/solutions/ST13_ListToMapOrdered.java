package streams.solutions;

import java.util.*;
import java.util.stream.*;

/*
 * ST13 — Same, preserving insertion order
 *
 * QUESTION:
 * Convert a List<Employee> into a Map<empId, empName>, but preserve the insertion order of the list.
 *
 * WHY / TRAPS:
 *   - There is no 3-arg toMap(key, value, LinkedHashMap::new) — the 3rd argument is the MERGE function.
 *   - Sorted by key instead? use TreeMap::new.
 */
public class ST13_ListToMapOrdered {

    static class Employee {                       // a record on Java 16+; plain class keeps this JDK 11-compatible
        private final int empId; private final String empName; private final String city;
        Employee(int empId, String empName, String city) { this.empId = empId; this.empName = empName; this.city = city; }
        int empId() { return empId; }
        String empName() { return empName; }
        String city() { return city; }
    }

    static Map<Integer, String> solve(List<Employee> emps) {
        return emps.stream().collect(Collectors.toMap(
                Employee::empId,
                Employee::empName,
                (first, second) -> first,                                  // merge fn is REQUIRED to reach the 4th arg
                LinkedHashMap::new));
    }

    public static void main(String[] args) {
        System.out.println("ST13 — Same, preserving insertion order");
        List<Employee> emps = Arrays.asList(new Employee(3, "Anusruta", "Pune"), new Employee(1, "Ravi", "Delhi"), new Employee(2, "Amit", "Mumbai"));
        Map<Integer, String> result = solve(emps);
        check("type is LinkedHashMap", result instanceof LinkedHashMap, true);
        check("key order = list order", result == null ? null : new ArrayList<>(result.keySet()), Arrays.asList(3, 1, 2));
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
