package streams.questions;

import java.time.LocalDate;
import java.time.Month;
import java.util.*;
import java.util.stream.*;

/*
 * ST14 — Vehicle that travelled the most in August
 *
 * QUESTION:
 * Given a vehicle table (id, name, model) and a journey table (vehicleId, dateOfJourney, distanceTravelled),
 * find the NAME of the vehicle that travelled the most (total distance) in August.
 *
 * EXAMPLES:
 *   Input:    vehicles: (1, Swift, VXi), (2, City, ZX)
 *             journeys: (1, 03-Aug, 120 km), (2, 09-Aug, 300 km), (1, 20-Aug, 250 km), (2, 01-Jul, 900 km)
 *   Expected: "Swift"   (August totals: Swift 120+250 = 370, City 300; City's 900 km was in July)
 *
 *   Input:    journeys: only (1, 01-Jul, 10 km)
 *   Expected: "No vehicles travelled in August"
 *
 * HINTS (try without them first):
 *   - filter August -> groupingBy vehicleId with summingDouble.
 *   - entrySet().stream().max(Map.Entry.comparingByValue())
 *   - Look the name up from the vehicle list (id -> name map).
 *
 * HOW TO PRACTISE: fill in the method(s) marked TODO, then run it:
 *   IntelliJ: click the green Run arrow next to main()
 *   Terminal (from the practice/ folder):  ./run-streams.sh 14
 * Answer: streams/solutions/ST14_MostTravelledInAugust.java
 */
public class ST14_MostTravelledInAugust {

    static class Vehicle {
        private final int id; private final String name; private final String model;
        Vehicle(int id, String name, String model) { this.id = id; this.name = name; this.model = model; }
        int id() { return id; }
        String name() { return name; }
    }
    static class Journey {
        private final int vehicleId; private final LocalDate dateOfJourney; private final double distanceTravelled;
        Journey(int vehicleId, LocalDate dateOfJourney, double distanceTravelled) {
            this.vehicleId = vehicleId; this.dateOfJourney = dateOfJourney; this.distanceTravelled = distanceTravelled;
        }
        int vehicleId() { return vehicleId; }
        LocalDate dateOfJourney() { return dateOfJourney; }
        double distanceTravelled() { return distanceTravelled; }
    }

    static String solve(List<Vehicle> vehicles, List<Journey> journeys) {
        // TODO: write your Stream API solution here
        Map<Integer, String> vehicleMap = vehicles.stream()
                .collect(Collectors.toMap(
                        Vehicle::id,
                        Vehicle::name
                ));

        return journeys.stream()
                .filter(j -> j.dateOfJourney().getMonth() == Month.AUGUST)
                .collect(Collectors.groupingBy(
                        Journey::vehicleId,
                        Collectors.summingDouble(Journey::distanceTravelled)
                )).entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(entry -> vehicleMap.get(entry.getKey()))
                .orElse("No vehicles travelled in August");
    }

    public static void main(String[] args) {
        System.out.println("ST14 — Vehicle that travelled the most in August");
        List<Vehicle> vehicles = Arrays.asList(new Vehicle(1, "Swift", "VXi"), new Vehicle(2, "City", "ZX"));
        List<Journey> journeys = Arrays.asList(
                new Journey(1, LocalDate.of(2026, 8, 3), 120), new Journey(2, LocalDate.of(2026, 8, 9), 300),
                new Journey(1, LocalDate.of(2026, 8, 20), 250), new Journey(2, LocalDate.of(2026, 7, 1), 900));
        check("Swift 370 km vs City 300 km in August (City's 900 km was July)", solve(vehicles, journeys), "Swift");
        check("no August trips", solve(vehicles, Arrays.asList(new Journey(1, LocalDate.of(2026, 7, 1), 10))), "No vehicles travelled in August");
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
