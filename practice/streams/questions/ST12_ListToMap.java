package streams.questions;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

/*
 * ST12 — List<Employee> -> Map<empId, empName>
 *
 * QUESTION:
 * Convert a List<Employee> into a Map<empId, empName> using the Stream API.
 *
 * EXAMPLES:
 *   Input:    [Employee(3, "Anusruta", "Pune"), Employee(1, "Ravi", "Delhi"), Employee(2, "Amit", "Mumbai")]
 *   Expected: {1=Ravi, 2=Amit, 3=Anusruta}   (any order)
 *
 * HINTS (try without them first):
 *   - Collectors.toMap(keyMapper, valueMapper).
 *
 * HOW TO PRACTISE: fill in the method(s) marked TODO, then run it:
 *   IntelliJ: click the green Run arrow next to main()
 *   Terminal (from the practice/ folder):  ./run-streams.sh 12
 * Answer: streams/solutions/ST12_ListToMap.java
 */
public class ST12_ListToMap {

    static class Employee {                       // a record on Java 16+; plain class keeps this JDK 11-compatible
        private final int empId; private final String empName; private final String city;
        Employee(int empId, String empName, String city) { this.empId = empId; this.empName = empName; this.city = city; }
        int empId() { return empId; }
        String empName() { return empName; }
        String city() { return city; }
    }

    static Map<Integer, String> solve(List<Employee> emps) {
        // TODO: write your Stream API solution here
        return emps.stream()
                .collect(Collectors.toMap(
                        Employee::empId,
                        Employee::empName
                ));
    }

    public static void main(String[] args) {
        System.out.println("ST12 — List<Employee> -> Map<empId, empName>");
        List<Employee> emps = Arrays.asList(new Employee(3, "Anusruta", "Pune"), new Employee(1, "Ravi", "Delhi"), new Employee(2, "Amit", "Mumbai"));
        check("toMap", solve(emps), mapOf(1, "Ravi", 2, "Amit", 3, "Anusruta"));
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
