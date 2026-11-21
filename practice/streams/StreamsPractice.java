import java.time.LocalDate;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

public class StreamsPractice {
    record Employee(int empId, String empName, String city) {}
    record Vehicle(int id, String name, String model) {}
    record Journey(int vehicleId, LocalDate dateOfJourney, double distanceTravelled) {}

    public static void main(String[] a) {
        // ST1
        String str = "abcdabcghsk";
        Map<Character, Long> st1 = str.chars().mapToObj(c -> (char) c)
            .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        System.out.println("ST1  " + st1);
        // ST2
        List<Integer> l1 = List.of(1,2,3,4), l2 = List.of(5,6,7,8);
        System.out.println("ST2  " + Stream.concat(l1.stream(), l2.stream()).toList()
                         + "  or " + Stream.of(l1, l2).flatMap(List::stream).toList());
        // ST3
        String s3 = "Stream";
        System.out.println("ST3  " + s3.chars().mapToObj(c -> String.valueOf((char) c)).reduce((x, y) -> y + x).orElse(s3));
        // ST4
        String s4 = "occurance";
        System.out.println("ST4  " + s4.chars().filter(c -> c != 'c')
                               .mapToObj(c -> String.valueOf((char) c)).collect(Collectors.joining()));
        // ST5
        String s5 = "programming";
        List<Character> st5 = s5.chars().mapToObj(c -> (char) c)
            .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
            .entrySet().stream()
            .filter(e -> e.getValue() > 1)
            .map(Map.Entry::getKey)
            .toList();
        System.out.println("ST5  " + st5);
        // ST6
        List<String> words = List.of("apple","banana","apple","orange","banana");
        System.out.println("ST6  " + words.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting())));
        // ST7
        String sentence = "I am working in motivated organization";
        int st7 = Arrays.stream(sentence.split(" ")).map(String::length).distinct()
            .sorted(Comparator.reverseOrder()).skip(1).findFirst().orElse(0);
        System.out.println("ST7  " + st7);
        // ST7 trap: your version (sort words, skip 1) on a tie
        String tie = "aaaa bbbb cc";
        int wrong = Arrays.stream(tie.split(" ")).sorted((x, y) -> y.length() - x.length())
            .skip(1).findFirst().get().length();
        int right = Arrays.stream(tie.split(" ")).map(String::length).distinct()
            .sorted(Comparator.reverseOrder()).skip(1).findFirst().orElse(0);
        System.out.println("     tie test \"aaaa bbbb cc\": without distinct=" + wrong + ", with distinct=" + right);
        // ST8 (yours, regex)
        List<String> l8 = List.of("one","5","ab12","abc","8","20");
        System.out.println("ST8  " + l8.stream().filter(s -> s != null && s.matches("\\d+"))
                               .map(Integer::parseInt).filter(n -> n % 2 == 0).toList());
        // ST9
        List<String> l9 = Arrays.asList("Ram","Vinitha","Mohanlal","Jawaharlal", null);
        System.out.println("ST9a " + l9.stream().filter(s -> s != null && s.endsWith("lal")).toList());
        System.out.println("ST9c " + l9.stream().filter(s -> s != null && !s.endsWith("lal")).toList());
        System.out.println("     \"Al\".substring(\"Al\".length()-3) -> " + tryIt(() -> "Al".substring("Al".length() - 3)));
        // ST10
        List<Integer> n10 = List.of(10,5,15,20,8);
        System.out.println("ST10 " + n10.stream().distinct().sorted(Comparator.reverseOrder()).skip(1).findFirst().orElse(null));
        List<Integer> dup = List.of(20,20,15);
        System.out.println("     [20,20,15] without distinct=" + dup.stream().sorted(Comparator.reverseOrder()).skip(1).findFirst().orElse(null)
                         + ", with distinct=" + dup.stream().distinct().sorted(Comparator.reverseOrder()).skip(1).findFirst().orElse(null));
        // ST11
        Set<Character> st11 = "hello world".chars().mapToObj(c -> (char) c).collect(Collectors.toSet());
        System.out.println("ST11 " + st11);
        // ST12 / ST13
        List<Employee> emps = List.of(new Employee(3,"Anusruta","Pune"), new Employee(1,"Ravi","Delhi"), new Employee(2,"Amit","Mumbai"));
        Map<Integer,String> st12 = emps.stream().collect(Collectors.toMap(Employee::empId, Employee::empName));
        Map<Integer,String> st13 = emps.stream().collect(Collectors.toMap(Employee::empId, Employee::empName,
                                       (v1, v2) -> v1, LinkedHashMap::new));
        System.out.println("ST12 " + st12 + "   ST13 (insertion order) " + st13);
        // ST14
        List<Vehicle> vehicles = List.of(new Vehicle(1,"Swift","VXi"), new Vehicle(2,"City","ZX"));
        List<Journey> journeys = List.of(
            new Journey(1, LocalDate.of(2026,8,3), 120), new Journey(2, LocalDate.of(2026,8,9), 300),
            new Journey(1, LocalDate.of(2026,8,20), 250), new Journey(2, LocalDate.of(2026,7,1), 900));
        Map<Integer,String> nameById = vehicles.stream().collect(Collectors.toMap(Vehicle::id, Vehicle::name));
        String st14 = journeys.stream()
            .filter(j -> j.dateOfJourney().getMonthValue() == 8)
            .collect(Collectors.groupingBy(Journey::vehicleId, Collectors.summingDouble(Journey::distanceTravelled)))
            .entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(e -> nameById.get(e.getKey()))
            .orElse("No vehicles travelled in August");
        System.out.println("ST14 " + st14 + "   (Swift 370 km vs City 300 km in August; City's 900 km was July)");
        // ST15
        List<Integer> nums = List.of(1,2,3,2,4,5,3,6);
        System.out.println("ST15 " + nums.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                               .entrySet().stream().filter(e -> e.getValue() > 1).map(Map.Entry::getKey).toList()
                               + "   (Set way: " + dupWithSet(nums) + ")");
        // ST16
        int[] arr = {3,1,2};
        System.out.println("ST16 sum=" + Arrays.stream(arr).sum() + ", boxed=" + Arrays.stream(arr).boxed().toList());
        // ST17
        Predicate<Employee> startsWithA = e -> e.empName() != null && e.empName().startsWith("A");
        Predicate<Employee> mumbaiOrPune = e -> "Mumbai".equalsIgnoreCase(e.city()) || "Pune".equalsIgnoreCase(e.city());
        System.out.println("ST17 " + emps.stream().filter(startsWithA.and(mumbaiOrPune)).map(Employee::empName).toList());
    }
    static List<Integer> dupWithSet(List<Integer> nums) {
        Set<Integer> seen = new HashSet<>();
        return nums.stream().filter(n -> !seen.add(n)).distinct().toList();
    }
    static String tryIt(Supplier<String> s) { try { return s.get(); } catch (Exception e) { return e.getClass().getSimpleName(); } }
}
