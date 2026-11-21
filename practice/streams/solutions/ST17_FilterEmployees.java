package streams.solutions;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

/*
 * ST17 — Name starts with 'A' and city is Mumbai or Pune
 *
 * QUESTION:
 * Return names of employees whose name starts with 'A' AND whose city is Mumbai or Pune.
 *
 * EXAMPLES:
 *   Input:    [Employee(3, "Anusruta", "Pune"), Employee(1, "Ravi", "Pune"),
 *             Employee(2, "Amit", "Mumbai"), Employee(4, "Arjun", "Delhi")]
 *   Expected: [Anusruta, Amit]   (Ravi: not A; Arjun: Delhi)
 *
 * WHY / TRAPS:
 *   - Without brackets, a && b || c means (a && b) || c — Ravi from Pune would sneak in.
 *   - "Mumbai".equalsIgnoreCase(city) is null-safe; city.equals("Mumbai") is not.
 */
public class ST17_FilterEmployees {

    static class Employee {                       // a record on Java 16+; plain class keeps this JDK 11-compatible
        private final int empId; private final String empName; private final String city;
        Employee(int empId, String empName, String city) { this.empId = empId; this.empName = empName; this.city = city; }
        int empId() { return empId; }
        String empName() { return empName; }
        String city() { return city; }
    }

    static List<String> solve(List<Employee> emps) {
        Predicate<Employee> startsWithA = e -> e.empName() != null && e.empName().startsWith("A");
        Predicate<Employee> mumbaiOrPune = e -> "Mumbai".equalsIgnoreCase(e.city()) || "Pune".equalsIgnoreCase(e.city());
        return emps.stream()
                .filter(startsWithA.and(mumbaiOrPune))
                .map(Employee::empName)
                .collect(Collectors.toList());
    }

    public static void main(String[] args) {
        System.out.println("ST17 — Name starts with 'A' and city is Mumbai or Pune");
        List<Employee> emps = Arrays.asList(new Employee(3, "Anusruta", "Pune"), new Employee(1, "Ravi", "Pune"),
                new Employee(2, "Amit", "Mumbai"), new Employee(4, "Arjun", "Delhi"));
        check("A + Mumbai/Pune", solve(emps), Arrays.asList("Anusruta", "Amit"));
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
