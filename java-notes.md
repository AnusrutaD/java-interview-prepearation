# Java — Revision Notes (Q&A)

← [Back to README](README.md) · [Java](java-notes.md) · [Spring Boot](spring-boot-notes.md) · [Microservices](microservices-notes.md)

Core Java (CJ1–CJ50), Streams (ST1–ST17), Testing block, Multithreading & Concurrency (MT1–MT14).
Each entry: **Keywords** → **Model answer** → **Traps**.

## CJ1 — == vs equals() vs hashCode()

**Keywords:** `==` operator: primitives → value, objects → reference · `equals()` = logical equality, default uses `==` · `hashCode()` = bucket index, identity hash (not the address) · equal ⇒ same hash, not the reverse · override both or HashMap breaks

**Model answer:** "`==` is an operator. For primitives it compares values; for objects it compares references — whether both variables point to the same object. `equals()` is a method from `Object` for logical equality; its default implementation just uses `==`, so classes like `String` override it to compare content. `hashCode()` returns an int that hash-based collections use to pick a bucket. The contract: equal objects must have equal hash codes, but equal hash codes don't imply equal objects. So whenever you override `equals()`, you must override `hashCode()` too — otherwise HashMap and HashSet won't find your keys."

**Traps:**

- The default `hashCode()` is not the memory address. It is an identity hash stored in the object header; the GC moves objects, but the hash must never change.
- `new String("a") == new String("a")` is false; `.equals()` is true.
- Overriding only `equals()` → two equal keys go to different buckets → duplicates in a HashSet, `get()` returns null.

```java
class Emp {
    int id;
    Emp(int id) { this.id = id; }
    @Override
    public boolean equals(Object o) { return o instanceof Emp e && e.id == id; }
    // no hashCode() override
}
Set<Emp> set = new HashSet<>();
set.add(new Emp(1));
set.add(new Emp(1));
System.out.println(set.size());   // 2 — add hashCode() and it becomes 1
```

## CJ2 — String pool and String immutability

**Keywords:** literal → pool (in heap since Java 7), reused · `new` → separate heap object, `==` false · `intern()` → pooled reference · why immutable: pool sharing, security, thread safety, cached hashCode · final class + private final byte\[\] + no mutators

**Model answer:** "String literals are stored in the String pool, a special area of the heap. When I write a literal, the JVM checks the pool and reuses an existing object if there is one, so identical literals share one object and save memory. `new String()` always creates a separate heap object, so `==` is false between them; `intern()` returns the pooled reference. Strings are immutable because the pool depends on it — shared references must not see each other's changes. It also gives security, since strings hold paths, URLs and credentials; thread safety without locks; and a cached hashCode, which makes Strings ideal HashMap keys. It is achieved by a final class, a private final array, and no methods that modify it."

```java
String a = "Anusruta";
String b = "Anusruta";
String c = new String("Anusruta");
a == b;           // true  — same pooled object
a == c;           // false — separate heap object
a == c.intern();  // true
```

**Traps:**

- "What is immutable" is not the answer to "why immutable". Give the four reasons.
- `substring()`, `concat()`, `replace()` return new Strings; the original never changes.
- Only local variables live on the stack; a String field lives in the heap with its object.

## CJ3 — How do you design an immutable class?

**Keywords:** final class · private final fields · no setters · constructor sets every field · copy mutable fields IN (constructor) and OUT (getter) · `List.copyOf` for collections · or make the inner type immutable / a record

**Model answer:** "Five rules. Make the class `final` so no subclass can add mutable behaviour. Make every field `private final` so each is set once, in the constructor. Provide no setters. For any mutable field, like an `Address` or a `List`, make a defensive copy in the constructor — otherwise the caller still holds the original reference and can change it — and return a copy from the getter. For collections I use `List.copyOf`, which copies and makes it unmodifiable. If I control the inner class, the cleanest option is to make it immutable too, for example a record."

```java
public final class Student {
    private final String name;
    private final Address address;          // mutable type
    private final List<String> subjects;

    public Student(String name, Address address, List<String> subjects) {
        this.name = name;
        this.address = new Address(address.getCity());   // copy IN
        this.subjects = List.copyOf(subjects);           // copy + unmodifiable
    }
    public String getName() { return name; }
    public Address getAddress() { return new Address(address.getCity()); }  // copy OUT
    public List<String> getSubjects() { return subjects; }
}
```

**Traps:**

- A getter copy alone is not enough: the caller who passed `addr` into the constructor can still call `addr.setCity(...)`.
- Without `final` on the class, a subclass can override getters and break immutability.

## CJ4 — final vs finally vs finalize

**Keywords:** final: class → no subclass, method → no override, variable → assign once (reference fixed, object can still change) · finally: always runs except System.exit / JVM crash · finalize: called before GC reclaims, not guaranteed, deprecated Java 9, use try-with-resources / Cleaner

**Model answer:** "`final` is a modifier: a final class can't be extended, a final method can't be overridden, and a final variable can be assigned only once — for a reference that means the reference is fixed, but the object it points to can still change. `finally` is a block after try/catch that runs whether or not an exception occurs, used for cleanup; it is skipped only if the JVM stops, for example `System.exit()`. Today try-with-resources is preferred for closing resources. `finalize()` is an Object method the GC may call before reclaiming an object. It is unpredictable, not guaranteed to run, deprecated since Java 9 and marked for removal in Java 18; use try-with-resources or `Cleaner` instead."

```java
final List<String> list = new ArrayList<>();
list.add("Java");           // allowed — object changes
list = new ArrayList<>();   // compile error — reference is final
```

**Traps:**

- `final` ≠ immutable (see above).
- A `return` inside `finally` overrides the try's return value and swallows exceptions. Never do it.
- `System.gc()` is only a request; it does not guarantee GC or `finalize()`.

## CJ5 — Overloading vs overriding

**Keywords:** overloading: same class, same name, different params, return type alone ✗, compile-time, decided by reference type · overriding: subclass, same signature, covariant return OK, access not narrower, no broader checked exceptions, static/final/private ✗, runtime, decided by actual object · @Override → compiler check

**Model answer:** "Both are polymorphism. Overloading is several methods with the same name in one class but different parameter lists — number, type or order. It's compile-time polymorphism: the compiler picks the method from the reference type and arguments. Changing only the return type is not allowed. Overriding is a subclass giving its own implementation of an inherited method with the same signature. It's runtime polymorphism: the actual object decides which version runs. The return type can be the same or a subtype, access can't be narrower, and it can't throw broader checked exceptions. Static, final and private methods can't be overridden. I always use `@Override` so the compiler catches signature mistakes."

|  | Overloading | Overriding |
| --- | --- | --- |
| Polymorphism | Compile-time (static) | Runtime (dynamic) |
| Where | Same class | Parent → child |
| Parameters | Must differ | Must be the same |
| Return type | Can differ, but not alone | Same or subtype (covariant) |
| Decided by | Reference type + arguments | Actual object type |
| static / final / private | Can be overloaded | Cannot be overridden |

```java
Animal a = new Dog();
a.speak();   // Dog's version — runtime dispatch on the actual object
```

**Trap:** a static method with the same signature in the child is method hiding, not overriding — the reference type decides which runs.

## CJ6 — Abstract class vs interface

**Keywords:** abstract class: can have 0 abstract methods, state + constructor, extend one, is-a, shared code · interface: implement many, only constants, no constructor, can-do · Java 8 default/static, Java 9 private · prefer interface; abstract class when sharing state/code · never `new` an abstract class

**Model answer:** "An interface defines a capability — a contract that unrelated classes can implement, and a class can implement many. It has no instance state, only constants, and no constructor; since Java 8 it can have default and static methods, and since Java 9 private helpers. An abstract class is a partial base class for closely related types: it can hold instance fields, constructors and concrete methods, and a class can extend only one. I default to interfaces, and add an abstract class when implementations share state or code — for example a `PaymentProcessor` interface with an `AbstractPaymentProcessor` holding common retry logic and timeouts."

|  | Abstract class | Interface |
| --- | --- | --- |
| Inheritance | Extend one | Implement many |
| Fields | Any instance fields | Only `public static final` constants |
| Constructor | Yes, called via `super()` | No |
| Methods | Any modifier, abstract or concrete | Abstract, default, static (Java 8), private (Java 9) |
| Relationship | is-a, shared identity | can-do, capability |

**Can you create an object of an abstract class with no abstract methods?** No. `new Base()` is a compile error whatever methods it has — the `abstract` keyword exists to prevent instantiation. `new Base() {}` compiles, but it creates an anonymous subclass, not a `Base`. The constructor exists for subclasses to call via `super()`. Real example: `HttpServlet` is abstract but every method has a body.

**Traps:**

- "Abstract class must have at least one abstract method" is false. The rule is the reverse: a class with an abstract method must be abstract.
- "Interface is 100% abstract" is only true before Java 8.

## CJ7 — Default and static methods in interfaces

**Keywords:** default = added in Java 8 to evolve interfaces without breaking implementors (`Collection.stream()`, `forEach`) · can be overridden · two interfaces, same default → compile error → override and pick with `I1.super.hello()` · class method wins over default · static = utility, not inherited, call via `Interface.method()`

**Model answer:** "Default methods were added in Java 8 so that existing interfaces could get new methods without breaking every class that implements them — that's how `Collection` got `stream()` and `Iterable` got `forEach()`. Implementing classes inherit the default and can override it. If a class implements two interfaces that both define the same default method, the code doesn't compile; the class must override the method, and it can call a specific version with `InterfaceName.super.method()`. A method in a class always wins over an interface default. Static interface methods are helper methods: they belong to the interface, aren't inherited by implementing classes, and are called as `InterfaceName.method()`, so they can't be overridden."

```java
interface I1 { default void hello() { System.out.println("I1"); } }
interface I2 { default void hello() { System.out.println("I2"); } }

class C implements I1, I2 {
    @Override
    public void hello() {        // required — otherwise compile error
        I1.super.hello();        // pick one, or write your own logic
    }
}
```

**Resolution rules:**

1. A class or superclass method beats any interface default.
2. A more specific interface (child) beats its parent interface.
3. Still ambiguous → the class must override.

**Trap:** static interface methods are not inherited, so `c.staticMethod()` or `C.staticMethod()` does not compile — only `I1.staticMethod()` works.

## CJ8 + CJ9 — Exception hierarchy; checked vs unchecked

**Keywords:** Throwable → Error + Exception · RuntimeException under Exception · unchecked = RuntimeException + Error (and subclasses), everything else checked · checked = compiler forces catch or `throws`, not "compile-time" · checked → recoverable (IO, SQL); unchecked → programming bug (NPE, IllegalArgument); Error → don't catch · Spring wraps checked into unchecked

**Model answer:** "Everything thrown in Java extends `Throwable`, which has two branches: `Error` for serious JVM problems like OutOfMemoryError and StackOverflowError, which we shouldn't catch, and `Exception`. Under `Exception` sits `RuntimeException`. Exceptions extending `RuntimeException`, plus all Errors, are unchecked; everything else is checked. All of them occur at runtime — 'checked' just means the compiler forces you to either catch it or declare it with `throws`. I use checked exceptions when the caller can realistically recover, like `IOException` or `SQLException`, and unchecked for programming errors like `NullPointerException` or `IllegalArgumentException`. In Spring, checked exceptions are usually wrapped into unchecked ones, like `DataAccessException`, and `@Transactional` rolls back only on unchecked exceptions by default."

```
Throwable
├── Error                      (unchecked — don't catch)
│   ├── OutOfMemoryError
│   └── StackOverflowError
└── Exception                  (checked)
    ├── IOException, SQLException
    └── RuntimeException       (unchecked)
        ├── NullPointerException
        ├── ArithmeticException
        └── IllegalArgumentException
```

**Traps:**

- Calling checked exceptions "compile-time exceptions". They are thrown at runtime; only the handling is checked at compile time.
- `Throwable` and `Exception` themselves are checked — `throw new Exception()` must be caught or declared.

## CJ10 — Serialization, deserialization and serialVersionUID

**Keywords:** object ↔ byte stream · `Serializable` = marker interface · ObjectOutputStream / ObjectInputStream · transient + static skipped · constructor not called · serialVersionUID = class version, mismatch → InvalidClassException, declare it or any class change breaks old data · prefer JSON/Avro in practice

**Model answer:** "Serialization converts an object into a byte stream so it can be stored or sent over a network; deserialization rebuilds the object from those bytes. The class must implement `Serializable`, a marker interface, and we use `ObjectOutputStream.writeObject()` and `ObjectInputStream.readObject()`. Transient and static fields are not serialized, every field must itself be serializable, and the constructor isn't called on deserialization. `serialVersionUID` is a version number for the class: on deserialization Java compares it with the stored one and throws `InvalidClassException` on a mismatch. If I don't declare it, the JVM computes it from the class structure, so adding a single field breaks previously saved data — so I always declare it. In practice I prefer JSON, Avro or Protobuf over Java native serialization, which is slow, Java-only and a known security risk with untrusted data."

```java
public class Employee implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private transient String password;   // not serialized → null after read
}
```

**Traps:**

- A non-serializable field → `NotSerializableException` at runtime.
- In the general sense, JSON/XML conversion (Jackson) is also serialization, but a Core Java question means the byte-stream mechanism.
- Serialization can break a Singleton — fixed with `readResolve()` (CJ36).

## CJ11 — transient, volatile and static

**Keywords:** transient → skipped in serialization, default value on read · volatile → visibility through main memory + no reordering, NOT atomic (count++ unsafe → AtomicInteger), used in DCL singleton and stop flags · static → class-level, one shared copy, created at class load, no `this`, can't touch instance members directly, hiding not overriding, static block / nested class, not allowed on local variables

**Model answer:** "`transient` marks a field to be skipped during serialization; after deserialization it has its default value — useful for passwords or derived data. `volatile` is for multithreading: reads and writes go to main memory instead of a thread's cached copy, so every thread sees the latest value, and it also prevents instruction reordering. It does not make compound operations atomic — `count++` on a volatile is still unsafe, so for counters I use `AtomicInteger`. Typical uses are a stop flag and the instance field in double-checked locking. `static` makes a member belong to the class rather than an object: one copy shared by all instances, created when the class loads, accessed by class name. A static method has no `this`, so it can't use instance members directly, and it can't be overridden — a same-signature static in a subclass hides it."

```java
private volatile boolean running = true;   // stop flag: visibility is enough
private volatile int count;                // count++ still NOT thread-safe
private final AtomicInteger safe = new AtomicInteger();  // safe.incrementAndGet()
```

**Traps:**

- volatile = visibility, not atomicity.
- `static int x;` inside a method body is a compile error.
- Instance methods can use static members; static methods can't use instance members without an object.

## CJ12 — Records, sealed classes and pattern matching

**Keywords:** record (Java 16): immutable data carrier, private final fields, `name()` accessors, auto constructor / equals / hashCode / toString, NO setters, implicitly final, can implement interfaces, compact constructor · sealed (Java 17): `permits`, subclasses final / sealed / non-sealed, enables exhaustive switch · pattern matching: `instanceof String s` (16) removes the cast · switch patterns + record patterns (21)

**Model answer:** "Records, final in Java 16, are concise immutable data carriers: `record Employee(int id, String name)` generates private final fields, a canonical constructor, accessors like `name()`, and equals, hashCode and toString. There are no setters; the class is implicitly final but can implement interfaces, and I can add a compact constructor for validation. I use them for DTOs and event payloads. Sealed classes, final in Java 17, restrict which classes can extend a type using `permits`; each permitted subclass must be final, sealed or non-sealed. Pattern matching removes manual casts: `if (obj instanceof String s)` binds a typed variable (Java 16), and Java 21 added pattern matching in switch and record deconstruction. Combined with sealed types, a switch becomes exhaustive with no default case."

```java
public record Employee(int id, String name) {
    public Employee {                                   // compact constructor
        if (id <= 0) throw new IllegalArgumentException("bad id");
    }
}

public sealed interface Payment permits Card, Upi, Wallet {}

String fee = switch (payment) {                        // Java 21
    case Card c   -> "2%";
    case Upi u    -> "0%";
    case Wallet w -> "1%";
};                                                      // no default needed

if (obj instanceof Employee(int id, String name)) { }  // record pattern, Java 21
```

**Traps:**

- Records have no setters; accessors are `name()`, not `getName()`.
- The keyword is `permits`, not `permit`.
- Plain `instanceof` (since Java 1.0) is not pattern matching; the binding variable is.

## CJ13 — Covariant return types

**Keywords:** overriding method may return a subtype of the parent's return type (Java 5) · avoids casts · e.g. `clone()` returning `Employee`, fluent builders · reference types only, primitives must match

**Model answer:** "Since Java 5, when a subclass overrides a method it can narrow the return type to a subtype of the original. For example, if `Animal.create()` returns `Animal`, `Dog` can override it to return `Dog`. Callers then get the specific type without casting. A common real case is overriding `clone()` to return the class's own type instead of `Object`. It only applies to reference types; a primitive return type must match exactly."

```java
class Animal { Animal create() { return new Animal(); } }
class Dog extends Animal {
    @Override
    Dog create() { return new Dog(); }   // covariant return
}
Dog d = new Dog().create();   // no cast
```

## CJ14 — String vs StringBuilder vs StringBuffer

**Keywords:** all three are classes · String immutable · Builder + Buffer mutable · Buffer synchronized (thread-safe, slower), Builder not (faster) · loops → StringBuilder (`+=` creates a new String each iteration) · Buffer rarely used today

**Model answer:** "`String` is immutable, so every modification creates a new object. `StringBuilder` and `StringBuffer` are mutable and modify the same buffer. The difference is that `StringBuffer`'s methods are synchronized, so it's thread-safe but slower, while `StringBuilder` isn't synchronized and is faster. For building strings in a loop I use `StringBuilder`, because `+=` on a String creates a new object every iteration. In practice a builder is almost always local to one method, so `StringBuilder` is the default and `StringBuffer` is rarely needed."

|  | String | StringBuilder | StringBuffer |
| --- | --- | --- | --- |
| Mutable | No | Yes | Yes |
| Thread-safe | Yes (immutable) | No | Yes (synchronized) |
| Speed | Slow for repeated changes | Fastest | Slower than Builder |
| Use | Fixed text, map keys | Loops, building output | Shared across threads (rare) |

## CJ15 + CJ27 — Composition vs inheritance; association, aggregation, composition

**Keywords:** inheritance = is-a, composition = has-a · favour composition: loose coupling, swap at runtime via interface / DI, no single-inheritance limit, easy to mock, avoids fragile base class · inheritance only for a true, stable is-a · association = has-a → composition (strong, child dies with owner: Car–Engine) vs aggregation (weak, child survives: Department–Employee)

**Model answer (CJ15):** "Inheritance models an is-a relationship and composition a has-a relationship. I favour composition: my class holds a reference to another object, usually through an interface, so it's loosely coupled — I can swap the implementation at runtime or inject a mock in tests, and I'm not limited to a single parent. Inheritance couples the child to the parent's internals, so a change in the parent can break subclasses. I use inheritance only for a genuine, stable is-a relationship where I want polymorphism. Spring DI is composition in practice — a `PaymentService` has a `PaymentGateway` injected."

**Model answer (CJ27):** "Association is any has-a relationship between classes. It has two forms. Composition is strong ownership: the part is created and owned by the whole and doesn't exist without it — a `Car` and its `Engine`. Aggregation is weak: the part exists independently and can be shared or moved — a `Department` and its `Employee`s."

|  | Inheritance | Composition | Aggregation |
| --- | --- | --- | --- |
| Relationship | is-a | has-a (strong) | has-a (weak) |
| Lifecycle | — | Part dies with owner | Part survives owner |
| Example | `Dog extends Animal` | Car → Engine | Department → Employee |
| Coupling | Tight, fixed at compile time | Loose, swappable | Loose |

**Trap:** don't flip them under pressure — inheritance is is-a, composition is has-a.

## CJ16 + CJ17 — try-with-resources; AutoCloseable vs Closeable

**Keywords:** try-with-resources (Java 7) → resources in `try(...)` auto-closed at block end, no finally · resource must implement AutoCloseable · closed in reverse order · close exception attached as suppressed (`getSuppressed()`) · Java 9: effectively final outside variable allowed · Closeable (java.io, Java 5, throws IOException, idempotent) extends AutoCloseable (java.lang, Java 7, throws Exception)

**Model answer:** "Try-with-resources, added in Java 7, automatically closes any resource declared in the `try` parentheses when the block finishes, normally or with an exception, so I don't need a finally block for cleanup. The resource must implement `AutoCloseable`. Multiple resources are closed in reverse order of declaration. If both the body and `close()` throw, the body's exception wins and the close exception is added as a suppressed exception, instead of hiding the original as it could with finally. `Closeable` is the older `java.io` interface: it extends `AutoCloseable`, narrows the exception to `IOException`, and requires `close()` to be idempotent. `AutoCloseable` was introduced as the general parent so non-IO resources like JDBC connections and locks could be used too."

```java
try (Connection con = ds.getConnection();
     PreparedStatement ps = con.prepareStatement(sql)) {
    // use ps
}   // ps closed first, then con — automatically
```

**Traps:**

- It is Java 7, not 17.
- The whole point is the automatic close; closing in finally is what it replaces.

## CJ18 — Shallow copy vs deep copy

**Keywords:** shallow → new outer object, references copied, inner objects shared (`clone()` default) · deep → inner objects copied too, nothing shared · copy constructor / custom clone / serialisation · immutable class defensive copy = deep copy

**Model answer:** "A shallow copy creates a new object and copies each field's value; for reference fields that means copying the reference, so both objects share the same nested objects — changing the copy's Address changes the original's. `Object.clone()` is shallow by default. A deep copy also creates new copies of every referenced object, recursively, so the two are fully independent. I usually deep copy with a copy constructor; overriding `clone()` or serialising also works but is clumsier. The defensive copies in an immutable class are exactly this."

```java
Student s1 = new Student("Anusruta", new Address("Marathahalli"));

Student shallow = new Student(s1.name, s1.address);                         // shared Address
Student deep    = new Student(s1.name, new Address(s1.address.getCity()));  // own Address

shallow.address.setCity("Kolkata");   // s1's city is now Kolkata too
deep.address.setCity("Pune");         // s1 unaffected
```

## CJ34a — HashMap internal working

**Keywords:** array of Node buckets, default 16, created lazily · hash = h ^ (h >>> 16), index = (n-1) & hash, capacity power of 2 · collision → compare hash then `equals()`: match → replace value, else append at tail · ≥ 8 nodes AND capacity ≥ 64 → red-black tree (back to list at 6), else resize · load factor 0.75, threshold 12 → double + rehash · one null key in bucket 0 · avg O(1), worst O(log n) since Java 8

**Model answer:** "HashMap is backed by an array of buckets, default capacity 16, created on the first put. For `put(k, v)` it computes `k.hashCode()`, spreads the high bits into the low bits, and gets the index with `(n-1) & hash` — capacity is always a power of two, so that equals hash mod n. If the bucket is empty it stores a node. If not, that's a collision: it walks the nodes comparing hash and then `equals()`; if the key already exists it replaces the value, otherwise it appends a new node. Since Java 8, when a bucket reaches 8 nodes and capacity is at least 64, the list is converted to a red-black tree, so the worst case is O(log n) instead of O(n). When size exceeds capacity × load factor 0.75 — 12 by default — the table doubles and entries are rehashed. `get()` follows the same path: hash → bucket → equals. Average get and put are O(1)."

```java
hash  = h ^ (h >>> 16);   // spread
index = (n - 1) & hash;   // same as hash % n when n is a power of 2
```

| Case | get / put |
| --- | --- |
| Average | O(1) |
| Worst before Java 8 (one long list) | O(n) |
| Worst Java 8+ (treeified bucket) | O(log n) |

**Traps:**

- Red-black tree, not B-tree.
- Treeify needs both conditions: bucket ≥ 8 and capacity ≥ 64; below 64 it resizes instead.
- Uniqueness of keys comes from `equals()`, not from hashCode alone.
- Java 8 appends at the tail; Java 7 inserted at the head (which could cause an infinite loop during concurrent resize).

## CJ34c + CJ34e + CJ34f — Forcing one bucket; default buckets and load factor; why multiple buckets

**Keywords:** constant `hashCode()` (e.g. return 1) + correct `equals()` → all keys in one bucket · pre-Java 8 O(n); Java 8 tree → \~O(log n), best when keys are Comparable · hash-flooding DoS is why tree bins were added · many buckets keep chains \~0–1 long → O(1); one bucket = a linked list, O(n) · default 16 buckets, load factor 0.75 → resize above 12 entries, capacity doubles (16 → 32 → 64) and entries are rehashed

**Model answer:** "To force every entry into one bucket, I override `hashCode()` on the key class to return a constant, while keeping `equals()` correct so keys are still distinct. Every key then maps to the same index. Before Java 8 that bucket is a linked list, so get and put degrade to O(n). Since Java 8 the bucket turns into a red-black tree once it has 8 entries and capacity is at least 64, so it's roughly O(log n) — most effective when keys implement Comparable. This is exactly the hash-flooding attack Java 8 defends against. HashMap needs many buckets because spreading keys keeps each chain about one entry long, which is what gives O(1); with a single bucket it is just a linked list with O(n) lookups. The 0.75 load factor triggers a resize before buckets get crowded."

**Model answer (CJ34e):** "A HashMap starts with 16 buckets and a load factor of 0.75, so it resizes once it holds more than 12 entries (16 × 0.75). Each resize doubles the capacity — 16, 32, 64 — and rehashes every entry into the new table. If I know the size in advance, I pass an initial capacity to avoid repeated resizing."

```java
class BadKey {
    int id;
    @Override
    public int hashCode() { return 1; }          // every key → same bucket
    @Override
    public boolean equals(Object o) { return o instanceof BadKey b && b.id == id; }
}
```

## CJ34b + CJ34h — synchronizedMap vs ConcurrentHashMap; bucket-level locking

**Keywords:** synchronizedMap = one lock for everything, reads locked too · CHM Java 7 = 16 segments (lock striping) · CHM Java 8 = CAS on empty bucket, `synchronized` on the bucket's first node, lock-free volatile reads · no null keys/values · weakly consistent iterators, no CME · atomic putIfAbsent / compute / merge

**Model answer:** "`Collections.synchronizedMap` wraps a HashMap with a single lock, so every read and write — even on unrelated keys — waits for that one lock. ConcurrentHashMap locks at a much finer level. In Java 7 it used 16 segments, each with its own lock. Since Java 8 there are no segments: inserting into an empty bucket uses a lock-free CAS, and updating a non-empty bucket synchronizes only on that bucket's first node, so threads writing to different buckets run in parallel. Reads take no lock at all, because node fields are volatile. It doesn't allow null keys or values, its iterators are weakly consistent and never throw ConcurrentModificationException, and it offers atomic compound operations like `putIfAbsent`, `compute` and `merge`."

|  | synchronizedMap | ConcurrentHashMap |
| --- | --- | --- |
| Lock | One lock on the whole map | CAS + per-bucket lock |
| Reads | Locked | Lock-free |
| Concurrent writers | One at a time | Many, on different buckets |
| Null key / value | Allowed | Not allowed |
| Iteration | Lock manually or get CME | Weakly consistent, no CME |
| Compound ops | Not atomic | putIfAbsent, compute, merge atomic |

```java
if (!map.containsKey(k)) map.put(k, v);   // race even on synchronizedMap
map.putIfAbsent(k, v);                    // atomic on CHM
map.merge(word, 1, Integer::sum);         // atomic counter
```

**Traps:**

- Segments are the Java 7 answer; say CAS + per-bin synchronized for Java 8+.
- Why no nulls: `get()` returning null would be ambiguous (missing vs null value) and can't be safely re-checked under concurrency.

## CJ34g + CJ34i — Fail-fast vs fail-safe; triggering ConcurrentModificationException

**Keywords:** fail-fast (ArrayList, HashMap): modCount vs expectedModCount → CME on next(), best-effort, even single-threaded · trigger: modify the collection directly inside for-each · fix: `iterator.remove()`, `removeIf` · map: put NEW key while iterating → CME; update existing key's value → no CME · fail-safe: CopyOnWriteArrayList iterates a snapshot (never sees changes); CHM weakly consistent (may see changes); neither throws CME

**Model answer:** "Fail-fast iterators, used by ArrayList, LinkedList and HashMap, detect structural changes made outside the iterator. The collection keeps a `modCount` that every add or remove increments; the iterator stores the expected value and checks it on each `next()`, throwing ConcurrentModificationException on a mismatch. It happens even in a single thread — for example removing from a list inside a for-each loop. The safe ways are `iterator.remove()` or `removeIf`. For a HashMap, putting a new key while iterating throws CME, but updating an existing key's value doesn't, because that isn't a structural change. Fail-safe iterators don't throw: CopyOnWriteArrayList iterates over a snapshot, so it never sees changes made during iteration and pays for a full array copy on every write, while ConcurrentHashMap's iterators are weakly consistent — they work on the live table and may or may not reflect concurrent updates."

```java
List<Integer> list = new ArrayList<>(List.of(1, 2, 3, 4));
for (Integer x : list) if (x == 2) list.remove(x);   // CME
list.removeIf(x -> x == 2);                          // safe

Map<String, Integer> map = new HashMap<>(Map.of("a", 1, "b", 2));
for (String k : map.keySet()) map.put("c", 3);      // new key → CME
for (String k : map.keySet()) map.put(k, 99);       // existing key → no CME
```

**Traps:**

- CME is not a multithreading-only exception.
- Fail-fast is best-effort, not a guarantee — don't rely on it for correctness.
- Snapshot (CopyOnWrite) and weakly consistent (CHM) are different behaviours.

## CJ34j + CJ34k — Array vs ArrayList; why use an array

**Keywords:** array fixed, ArrayList dynamic · ArrayList = backed by an array, default 10 (allocated lazily), grows 1.5× (old + old >> 1), NO load factor, amortised O(1) add · choose array for primitives (no boxing, \~4–5× less memory), raw speed, fixed-size data, 2D grids · arrays covariant (ArrayStoreException), generics invariant

**Model answer:** "An array has a fixed length set at creation; an ArrayList is a resizable list backed by an internal array. ArrayList's default capacity is 10, allocated on the first add, and when it's full it grows by 1.5× and copies the elements, so add is amortised O(1). I choose an array mainly for primitives: an `int[]` stores raw values, while `ArrayList<Integer>` boxes every element into an object, costing several times the memory and extra time. Arrays also suit truly fixed-size data, like the four direction vectors in a grid problem, and multi-dimensional data. For everything else — especially when the size changes or I want the Collections API — I use ArrayList, pre-sized if I know the count."

|  | Array | ArrayList |
| --- | --- | --- |
| Size | Fixed at creation | Grows 1.5× automatically |
| Primitives | Yes (`int[]`) | No — boxed (`Integer`) |
| Length | `arr.length` | `list.size()` |
| Type safety | Covariant (runtime ArrayStoreException) | Generics, checked at compile time |
| API | Basic | add, remove, contains, streams, Collections utilities |

**Trap:** ArrayList has no load factor — that's HashMap.

## CJ34l — map() vs flatMap()

**Keywords:** map = one-to-one, `Function<T, R>` · flatMap = one-to-many then flatten, `Function<T, Stream<R>>`, avoids `Stream<Stream<R>>` · nested lists → `flatMap(List::stream)` · sentences → words · `Optional.flatMap` avoids `Optional<Optional<T>>`

**Model answer:** "`map` transforms each element into exactly one new element — a one-to-one function. `flatMap` is for when each element produces several elements: the function returns a stream for each element, and `flatMap` merges all those streams into a single flat stream. With `map` you would end up with a stream of streams. Typical uses are flattening a list of lists with `flatMap(List::stream)` or splitting sentences into words. `Optional.flatMap` follows the same idea, avoiding a nested Optional."

```java
List<List<Integer>> nested = List.of(List.of(1, 2), List.of(3, 4));
nested.stream().map(List::stream);       // Stream<Stream<Integer>>
nested.stream().flatMap(List::stream);   // Stream<Integer>: 1, 2, 3, 4

sentences.stream().flatMap(s -> Arrays.stream(s.split(" "))).toList();   // all words
```

## CJ19 — Generics and type erasure

**Keywords:** generics (Java 5) = compile-time type safety, no casts, ClassCastException caught early · generic classes/methods, bounded `<T extends X>` · type erasure: types removed after compile, T → Object or its bound, casts inserted, `List<String>` and `List<Integer>` are the same class at runtime, for backward compatibility · so: no primitives, no `new T()`, no `instanceof List<String>`, no generic arrays, no overloads differing only by type argument

**Model answer:** "Generics, added in Java 5, let classes and methods work with a type parameter, so a `List<String>` only accepts Strings. The benefit is compile-time type safety: wrong types are rejected by the compiler instead of failing with ClassCastException at runtime, and no casts are needed. I can write generic classes, generic methods, and bounded types like `<T extends Number>`. Generics are implemented by type erasure: the compiler checks types, then removes them — `T` becomes `Object` or its bound and the compiler inserts casts — so at runtime `List<String>` and `List<Integer>` are the same class. This kept compatibility with pre-Java 5 code. The consequences are that primitives can't be type arguments, I can't do `new T()` or create generic arrays, `instanceof List<String>` isn't allowed, and two overloads differing only by type argument clash."

```java
List raw = new ArrayList();          // raw type
raw.add(42);
String s = (String) raw.get(0);      // ClassCastException at runtime

List<String> typed = new ArrayList<>();
// typed.add(42);                    // compile error

new ArrayList<String>().getClass() == new ArrayList<Integer>().getClass();   // true — erasure
```

**Trap:** wrapper classes are `Integer`, `Character`, `Double` — `int`, `char`, `double` are primitives.

## CJ20 — PECS: ? extends vs ? super

**Keywords:** PECS = Producer Extends, Consumer Super · `? extends T` → read as T, can't add (only null) · `? super T` → can add T, read only as Object · `sum(List<? extends Number>)`, `Collections.copy(dest super, src extends)` · generics invariant: `List<Integer>` is not a `List<Number>` — that's why wildcards exist

**Model answer:** "Generics are invariant — a `List<Integer>` is not a `List<Number>` — so wildcards make methods flexible. PECS means Producer Extends, Consumer Super. If a collection only produces values for me to read, I use `? extends T`: I can read elements as T, but I can't add, because the actual list might be a `List<Double>` and an Integer would corrupt it. If a collection only consumes values I put in, I use `? super T`: I can safely add T, because any list of T or its supertypes can hold it, but reading only gives me Object. The JDK's `Collections.copy(List<? super T> dest, List<? extends T> src)` shows both."

|  | `List<? extends Number>` | `List<? super Integer>` |
| --- | --- | --- |
| Accepts | List\<Integer>, List\<Double>, List\<Number> | List\<Integer>, List\<Number>, List\<Object> |
| Read | Yes, as Number | Only as Object |
| Add | No (only null) | Yes, Integer |
| Role | Producer | Consumer |

```java
double sum(List<? extends Number> nums) {     // producer: read only
    double s = 0;
    for (Number n : nums) s += n.doubleValue();
    return s;
}
void fill(List<? super Integer> out) {        // consumer: add only
    out.add(1);
    out.add(2);
}
```

## CJ21 / CJ34m — Comparator vs Comparable

**Keywords:** Comparable (java.lang) → `compareTo(other)`, this vs other, natural order, implemented inside the class, one ordering · Comparator (java.util) → `compare(a, b)`, external, many orderings, lambdas / `comparing().thenComparing().reversed()` · returns negative / 0 / positive · used in sort, TreeMap/TreeSet, PriorityQueue · memory trick: comparABLE = object is able to compare itself; comparATOR = outside judge of two

**Model answer:** "`Comparable`, in `java.lang`, defines a class's natural ordering: the class implements `compareTo(T other)` and compares itself with another object — `String` and `Integer` do this — so there's exactly one ordering. `Comparator`, in `java.util`, is an external strategy with `compare(T a, T b)`; I can create as many orderings as I need without touching the class, which matters for library classes I can't modify. Both return negative, zero or positive. Comparator is a functional interface, so I usually write it with `Comparator.comparing(...).thenComparing(...).reversed()`. Both are used by `Collections.sort`, `List.sort`, TreeMap, TreeSet and PriorityQueue."

|  | Comparable | Comparator |
| --- | --- | --- |
| Package | java.lang | java.util |
| Method | `compareTo(T other)` | `compare(T a, T b)` |
| Compares | this vs other | two objects passed in |
| Lives | Inside the class | Separate object / lambda |
| Orderings | One (natural) | Many |

```java
class Employee implements Comparable<Employee> {
    int id;
    public int compareTo(Employee o) { return Integer.compare(this.id, o.id); }
}
Collections.sort(list);                                        // natural order

list.sort(Comparator.comparing(Employee::getSalary).reversed()
                    .thenComparing(Employee::getName));        // custom order
PriorityQueue<Employee> pq = new PriorityQueue<>(Comparator.comparing(Employee::getSalary));
```

**Trap:** don't swap them — Comparable has `compareTo` (one argument), Comparator has `compare` (two arguments).

## CJ34d — hashCode and equals contracts

**Keywords:** equals: reflexive, symmetric, transitive, consistent, `x.equals(null)` false · hashCode: consistent while fields unchanged; equal objects → same hash; unequal may share a hash · override both, use the same fields · don't use mutable fields as keys

**Model answer:** "The `equals()` contract has five rules: reflexive — `x.equals(x)` is true; symmetric — if `x.equals(y)` then `y.equals(x)`; transitive — if x equals y and y equals z, then x equals z; consistent — repeated calls give the same result if nothing changed; and `x.equals(null)` is false. The `hashCode()` contract: it must return the same value while the fields used in equals don't change; equal objects must have equal hash codes; unequal objects may share a hash code, though fewer collisions means better performance. So I override both together, based on the same fields — usually with `Objects.equals` and `Objects.hash`, or a record, which generates both. And I avoid mutable fields in keys: if a key's field changes after insertion, its hash changes and the map can't find it."

```java
@Override
public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Employee e)) return false;
    return id == e.id && Objects.equals(name, e.name);
}
@Override
public int hashCode() { return Objects.hash(id, name); }   // same fields as equals
```

**Trap:** a mutable key whose field changes after `put()` is effectively lost — `get()` looks in the wrong bucket.

## CJ22 — How does HashSet work internally?

**Keywords:** HashSet wraps a HashMap · element = key, value = dummy `PRESENT` object (not null) · add → `map.put(e, PRESENT) == null` · uniqueness via hashCode + equals · one null allowed · no order · LinkedHashSet → LinkedHashMap, TreeSet → TreeMap · same buckets, 16, 0.75, treeify rules as HashMap

**Model answer:** "HashSet is built on a HashMap. Each element I add is stored as a key in that internal map, and every value is the same dummy object called `PRESENT`. `add(e)` calls `map.put(e, PRESENT)`; if that returns null the key was new and add returns true, otherwise it was a duplicate and add returns false. So uniqueness comes from the HashMap's keys, which means it relies on correct hashCode and equals. Everything else follows HashMap: 16 buckets, load factor 0.75, treeified buckets in Java 8, one null element, and no ordering. LinkedHashSet uses a LinkedHashMap for insertion order and TreeSet uses a TreeMap for sorted order."

```java
// Inside java.util.HashSet
private transient HashMap<E, Object> map;
private static final Object PRESENT = new Object();

public boolean add(E e)          { return map.put(e, PRESENT) == null; }
public boolean contains(Object o) { return map.containsKey(o); }
```

**Trap:** the value is a shared dummy `Object`, not null.

## CJ23 — Exception handling with method overriding

**Keywords:** rules apply to checked exceptions only · child may throw the same, a narrower subtype, or none · never broader or new checked · parent throws nothing → child no checked, unchecked still OK · why: a caller holding a Parent reference only catches what Parent declared (child may promise less, never more)

**Model answer:** "When overriding, the child can throw the same checked exception as the parent, a subclass of it, or nothing at all — but never a broader or additional checked exception. If the parent method declares no checked exceptions, the override can't declare any either. Unchecked exceptions are unrestricted in both cases, since the compiler doesn't track them. The reason is polymorphism: code that calls the method through a Parent reference was compiled to handle only the exceptions Parent declared, so a child throwing something new would escape that handling."

| Parent declares | Child override may throw |
| --- | --- |
| `IOException` (checked) | `IOException`, a subclass like `FileNotFoundException`, or nothing — not `Exception` or `SQLException` |
| Nothing | No checked exceptions; any unchecked is fine |
| Either | Unchecked (RuntimeException) always allowed |

```java
Parent p = new Child();
try {
    p.read();                   // compiler sees Parent.read() throws IOException
} catch (IOException e) { }     // a new checked exception from Child would escape this
```

## CJ24 — wait() vs sleep() vs yield()

**Keywords:** wait → Object, RELEASES the lock, only inside synchronized (else IllegalMonitorStateException), woken by notify/notifyAll · sleep → static Thread, KEEPS the lock, wakes after timeout · yield → static Thread, hint only (may be ignored), keeps lock, running → runnable · wait inside a while loop (spurious wakeups) · wait/sleep throw InterruptedException

**Model answer:** "`wait()` is an Object method used for coordination between threads. It must be called inside a synchronized block on the same object; it releases that lock and the thread waits until another thread calls `notify()` or `notifyAll()`, or a timeout or interrupt occurs. I always call it in a while loop that re-checks the condition, because of spurious wakeups. `sleep()` is a static Thread method that pauses the current thread for a fixed time but keeps any locks it holds. `yield()` is also static; it only hints to the scheduler that the thread is willing to give up the CPU, moving it from running to runnable, and the scheduler may ignore it. The key difference is that wait releases the lock and sleep doesn't."

|  | wait() | sleep(ms) | yield() |
| --- | --- | --- | --- |
| Class | Object (instance) | Thread (static) | Thread (static) |
| Releases lock | Yes | No | No |
| Call from | Inside synchronized only | Anywhere | Anywhere |
| Wakes by | notify / notifyAll / timeout / interrupt | Timeout / interrupt | Immediately runnable |
| State | WAITING / TIMED\_WAITING | TIMED\_WAITING | RUNNING → RUNNABLE |
| Purpose | Coordination | Pause | Scheduler hint |

```java
synchronized (lock) {
    while (!condition) {   // while, not if
        lock.wait();       // releases lock, waits for notify
    }
    // proceed
}
```

**Trap:** yield has nothing to do with acquiring locks.

## CJ25 — Future vs CompletableFuture

**Keywords:** Future (Java 5) = result of an async task, but `get()` blocks; no callbacks, chaining, combining, manual completion or error handling · CompletableFuture (Java 8) = Future + CompletionStage · thenApply / thenAccept (callback), thenCompose (chain dependent), thenCombine / allOf / anyOf (combine), complete() (manual), exceptionally / handle (errors) · default commonPool — pass own executor for I/O · same model as Vert.x futures

**Model answer:** "A `Future`, from Java 5, represents the result of a task submitted to an ExecutorService, but the only way to get the result is `get()`, which blocks. It also can't be completed manually, can't take callbacks, can't be chained or combined with other futures, and has no exception-handling API. `CompletableFuture`, added in Java 8, implements both Future and CompletionStage and fixes all of that: `thenApply` and `thenAccept` attach callbacks, `thenCompose` chains a dependent async call, `thenCombine` and `allOf` combine results, `complete()` finishes it manually, and `exceptionally` or `handle` deal with errors — all without blocking. By default async methods run on the common ForkJoinPool, so for I/O I pass my own executor. It's the same compose-don't-block model we use with Vert.x futures."

| Future limitation | CompletableFuture fix |
| --- | --- |
| `get()` blocks | thenApply / thenAccept / thenRun |
| Can't complete manually | complete(), completeExceptionally() |
| Can't chain | thenCompose |
| Can't combine | thenCombine, allOf, anyOf |
| No error handling | exceptionally, handle, whenComplete |

```java
CompletableFuture.supplyAsync(() -> fetchUser(id), ioExecutor)
    .thenCompose(user -> fetchOrders(user))      // dependent async call
    .thenCombine(fetchLimits(id), this::merge)   // combine with an independent call
    .exceptionally(ex -> fallback());            // error handling
```

**Trap:** Future itself is not non-blocking — its `get()` blocks.

## CJ26 + CJ49 — Garbage collection and collectors

**Keywords:** `new` allocates in Eden · garbage = unreachable from GC roots (thread stacks, statics, active threads); cycles collected · minor GC = Eden full → copy live objects to the empty survivor, age++, promote to old at 15 · major/full GC = old gen, slower · old gen: mark → sweep → compact; young gen: copying · stop-the-world pauses · CMS removed in 14 · G1 default since 9, regions, pause target · ZGC sub-ms pauses, generational since 21 · System.gc() only a hint

**Model answer:** "Garbage collection frees objects that are no longer reachable. The GC starts from GC roots — local variables on thread stacks, static fields, active threads — and anything it can't reach is garbage, even objects that reference each other in a cycle. The heap is generational. New objects are allocated in Eden. When Eden fills, a minor GC copies the live objects from Eden and the current survivor space into the other survivor space and discards the rest; each survival increases an object's age, and after a threshold, 15 by default, it's promoted to the old generation. Since most objects die young, this is fast. The old generation is collected less often with mark, sweep and compact: mark reachable objects, free the rest, and compact to avoid fragmentation. Collections involve stop-the-world pauses. G1 has been the default since Java 9; it splits the heap into regions and targets a pause time. ZGC gives sub-millisecond pauses even on very large heaps and is generational since Java 21. CMS was deprecated in Java 9 and removed in 14. `System.gc()` is only a request."

|  | Minor GC | Major / Full GC |
| --- | --- | --- |
| Area | Young (Eden + Survivor) | Old (Full = whole heap) |
| Trigger | Eden full | Old gen full |
| Cost | Fast, frequent | Slow, longer pauses |

| Collector | Key point |
| --- | --- |
| Serial | Single-threaded, small apps |
| Parallel | Multi-threaded, maximum throughput |
| CMS | Low pause, fragmentation; deprecated 9, removed 14 |
| G1 | Default since 9; regions, garbage-first, pause target (MaxGCPauseMillis, default 200 ms) |
| ZGC | Sub-millisecond pauses, TB heaps, mostly concurrent; generational since 21 |

**Traps:**

- The GC doesn't place objects in Eden — `new` allocates there.
- Mark-and-sweep (old gen) and copying (young gen) are different algorithms.
- Reachability, not reference counting — cycles are collected.

## CJ32 (+ SB13, SB36) — Constructor vs setter injection; types of DI

**Keywords:** DI = Spring creates beans in the IoC container and injects them · 3 types: constructor, setter, field · constructor recommended: final fields, required deps guaranteed, testable with `new`, circular deps fail fast, `@Autowired` optional with one constructor (4.3+) · setter → optional/changeable deps · field → avoid: hides deps, can't be final, needs reflection to test

**Model answer:** "Dependency injection means my class doesn't create its own dependencies; Spring creates beans in the IoC container and supplies them. There are three styles: constructor, setter and field injection. Spring recommends constructor injection: dependencies can be final, so the object is immutable; it can't be created without its required dependencies, so there are no half-initialised objects; I can unit test it with plain `new` and mocks, without a Spring context; and circular dependencies fail fast at startup. With a single constructor, `@Autowired` isn't even needed. Setter injection suits optional dependencies that may change. Field injection is concise but hides dependencies, prevents final fields, and needs reflection to test, so I avoid it."

|  | Constructor | Setter | Field |
| --- | --- | --- | --- |
| final fields | Yes | No | No |
| Best for | Required deps | Optional deps | Avoid |
| Test without Spring | Yes | Yes | No (reflection) |
| Dependencies visible | Yes | Yes | Hidden |

```java
@Service
public class PaymentService {
    private final PaymentGateway gateway;

    public PaymentService(PaymentGateway gateway) {   // @Autowired optional
        this.gateway = gateway;
    }
}
```

## CJ33 — Marker interface

**Keywords:** no methods at all, only tags a class · `implements` Serializable / Cloneable / RandomAccess · checked with `instanceof`: NotSerializableException, CloneNotSupportedException · modern alternative: annotations (@Entity, @Component) · marker interfaces still give compile-time type checking

**Model answer:** "A marker interface has no methods; implementing it just tags a class with a capability that other code checks with `instanceof`. `Serializable` is checked by ObjectOutputStream, which throws NotSerializableException if it's missing; `Cloneable` is checked by `Object.clone()`, which otherwise throws CloneNotSupportedException; and `RandomAccess` tells algorithms a list like ArrayList supports fast index access. Today annotations are usually used for this kind of metadata because they're more flexible and can carry values, but a marker interface still has one advantage: it's a type, so a method can require it at compile time, like `save(Serializable obj)`."

**Trap:** a class implements a marker interface; it doesn't extend it.

## CJ35 — OOP concepts group

Letters follow the order on the printed question sheet.

| Sub-question | Where it's covered |
| --- | --- |
| 35a — Overriding with a static method → method hiding | CJ5 trap, CJ11 |
| 35b — Overriding scenarios with exceptions, static, final | CJ23 (exceptions), CJ5 (static, final) |
| 35c — Abstract methods in a functional interface | Below |
| 35d — Functional interface vs lambda | Below |
| 35e — Predicate, Supplier, Consumer, Function | Below (pending) |
| 35f — Diamond problem | CJ7 |

### CJ35a — Static method in child → method hiding

**Keywords:** static = class-level, bound at compile time by the REFERENCE type · child static with same signature hides parent's · `Parent p = new Child(); p.show()` → Parent's version · `Child.show()` / Child reference → Child's version · no runtime polymorphism · `@Override` on static = compile error

**Model answer:** "Static methods belong to the class, so calls are resolved at compile time from the reference type, not the actual object. If a child declares a static method with the same signature, it hides the parent's version rather than overriding it: with `Parent p = new Child()`, `p.show()` still calls Parent's method, while `Child.show()` or a Child reference calls Child's. Because there's no runtime dispatch, it isn't overriding — and putting `@Override` on a static method is a compile error."

**Verified by running (JDK 17):**

```
Parent p = new Child(); p.show() -> Parent.show
Child  c = new Child(); c.show() -> Child.show
Child.show()                     -> Child.show
Parent.show()                    -> Parent.show
@Override on static -> error: static methods cannot be annotated with @Override
```

**Trap:** the child's static method is not uncallable — it's the reference type that decides which one runs.

### CJ35b — Overriding scenarios: exceptions, static, final

**Keywords:** checked exceptions: child may throw same, NARROWER (subclass) or none — never broader or new · unchecked: no rule · static: can't override, only hide — reference type decides · final: can't override ("overridden method is final"), CAN overload · private: not inherited, so not overridable

**Model answer:** "Three rules. Exceptions: an overriding method may throw the same checked exception as the parent, a narrower subclass of it, or none — never a broader or new checked exception — while unchecked exceptions are unrestricted. Static: static methods can't be overridden, only hidden; which version runs depends on the reference type, and `@Override` on them won't compile. Final: a final method can't be overridden, but it can still be overloaded, because final only blocks overriding. Private methods aren't inherited, so they can't be overridden either."

**Verified by compiling (JDK 17):**

```
child throws Exception, parent IOException  -> error: overridden method does not throw Exception
child throws FileNotFoundException / none   -> compiles
child overrides final pay()                 -> error: overridden method is final
final pay() + final pay(int) (overload)     -> compiles
```

**Traps:**

- Never say "higher" or "broader" exception for the child — it's narrower.
- Static: reference type decides; instance: the actual object decides.

### CJ35c — How many abstract methods can a functional interface have?

**Keywords:** exactly one abstract method · any number of default and static methods · `@FunctionalInterface` makes the compiler enforce it, optional · redeclared `Object` public methods (equals, toString) don't count — e.g. Comparator declares compare + equals · examples: Runnable, Callable, Comparator

**Model answer:** "A functional interface has exactly one abstract method, plus any number of default and static methods. `@FunctionalInterface` is optional, but it makes the compiler reject the interface if it has zero or more than one abstract method. Abstract methods that redeclare Object's public methods don't count — that's why `Comparator` declares both `compare` and `equals` and is still functional. Examples are Runnable, Callable and Comparator."

**Verified by compiling (JDK 17):** `Comparator`'s abstract methods are `compare` and `equals`, and it carries `@FunctionalInterface`. An interface with two real abstract methods fails with "multiple non-overriding abstract methods found".

### CJ35d — Functional interface vs lambda expression

**Keywords:** functional interface = the type, lambda = the value · lambda implements its single abstract method · lambda has no type of its own — target typing · replaces anonymous inner classes · inside a lambda `this` = enclosing object, captured locals effectively final

**Model answer:** "The functional interface is the type and the lambda is the value: a lambda is a concise implementation of that interface's single abstract method. A lambda has no type of its own — it can only be used where a functional interface is expected, and the compiler infers which one from context. Before Java 8 we'd write an anonymous inner class. Inside a lambda, `this` refers to the enclosing object, and any local variables it uses must be effectively final."

```java
PriorityQueue<int[]> pq = new PriorityQueue<>((a, b) -> Integer.compare(a[0], b[0]));
// the lambda is a Comparator<int[]>: it implements compare(a, b)
```

**Trap:** use `Integer.compare(a[0], b[0])`, not `a[0] - b[0]` — subtraction can overflow.

### CJ35e — Predicate, Supplier, Consumer, Function

**Keywords:** Predicate `test` T → boolean (filter) · Function `apply` T → R (map), two type params, one input · Consumer `accept` T → void (forEach, peek) · Supplier `get` () → T (orElseGet, generate) · Bi-versions, UnaryOperator / BinaryOperator, primitive IntPredicate etc. · compose with and / or / negate / andThen

**Model answer:** "They're the core functional interfaces in `java.util.function`, added in Java 8. `Predicate<T>` has `test`, takes a T and returns a boolean — used by `filter`. `Function<T, R>` has `apply`, takes a T and returns an R — used by `map`. `Consumer<T>` has `accept`, takes a T and returns nothing — used by `forEach` and `peek`. `Supplier<T>` has `get`, takes nothing and returns a T — used for lazy values like `Optional.orElseGet` and `Stream.generate`. There are two-argument versions such as BiFunction and BiConsumer, UnaryOperator and BinaryOperator for same-type input and output, and primitive versions like IntPredicate that avoid boxing. They can be combined with `and`, `or`, `negate` and `andThen`."

| Interface | Method | In → Out | Typical use |
| --- | --- | --- | --- |
| Predicate\<T> | test | T → boolean | filter |
| Function\<T, R> | apply | T → R | map |
| Consumer\<T> | accept | T → void | forEach, peek |
| Supplier\<T> | get | () → T | orElseGet, generate |

```java
Predicate<Employee> inMumbai = e -> e.getCity().equals("Mumbai");
Predicate<Employee> startsA  = e -> e.getName().startsWith("A");
list.stream().filter(startsA.and(inMumbai)).toList();
```

**Trap:** Function has two type parameters but takes one input value.

## CJ36 — Singleton (a: double-checked locking, b: Serializable impact, c: fix)

**Keywords:** one instance · private constructor + static `getInstance()` · eager (create at class load) vs lazy (create on first call) · 36a DCL: check → synchronized → check again, field `volatile` (prevents reordering → half-constructed object) · 36b deserialization creates a NEW object → two instances · 36c `readResolve()` returns the instance · best: enum singleton (safe against serialization + reflection) · also breakable by reflection and cloning

**Model answer:** "A singleton allows only one instance: the constructor is private and a static `getInstance()` returns the single instance. Eager initialisation creates it when the class loads; lazy initialisation creates it on first use. For thread-safe lazy initialisation I use double-checked locking: check for null, synchronize, check again, then create. The second check is needed because two threads can pass the first check together. The field must be volatile, because `new` is allocate, construct, assign, and without volatile those can be reordered so another thread sees a non-null but half-constructed object. If the singleton is Serializable, deserialisation creates a brand-new object without calling the constructor, so there are two instances. The fix is a `readResolve()` method returning the existing instance. The most robust option is an enum singleton, which the JVM guarantees against both serialisation and reflection."

```java
class Singleton implements Serializable {
    private static volatile Singleton instance;
    private Singleton() {}

    static Singleton getInstance() {
        if (instance == null) {                    // 1st check: no lock once created
            synchronized (Singleton.class) {
                if (instance == null) {            // 2nd check: two threads may pass the 1st
                    instance = new Singleton();
                }
            }
        }
        return instance;
    }
    private Object readResolve() { return getInstance(); }   // 36c fix
}

enum EnumSingleton { INSTANCE }                    // best: serialization + reflection safe
```

**Verified by running (JDK 17):**

```
Without readResolve: same object after deserialize? false
With readResolve:    same object after deserialize? true
Enum singleton:      same object after deserialize? true
```

**Traps:**

- Without `volatile`, DCL is broken (reordering).
- The serialization problem is a second object, not lost data.
- Reflection (`setAccessible`) and cloning can also break it; an enum blocks both.

## CJ37 — Immutability in Java (a: why Strings immutable, b: immutable Employee + Address)

**37a** — same as CJ2. Start from "one String object is shared by many references", then derive: pool sharing → a change would leak to every reference; security → a checked path or password can't change before use; thread safety → no locks needed; hash caching → safe, fast HashMap keys.

**37b** — same as CJ3 (full code there). Five rules: final class · private final fields · no setters · constructor sets everything · copy mutable fields in and out (`List.copyOf` for collections), or make Address immutable / a record.

**Trap:** the fields are `private final` instance fields — never `static`. A static field is shared by every Employee, so all employees would have the same name and address.

## CJ38 — Why are wait(), notify(), notifyAll() in Object, not Thread?

**Keywords:** every object can be a lock (monitor) · threads wait on an object's lock, not on a thread · notifier says "wake whoever waits on THIS lock" — no need to know which thread · must hold the lock (synchronized) else IllegalMonitorStateException · notify wakes one (arbitrary), notifyAll wakes all, then they compete

**Model answer:** "In Java every object can act as a lock, called a monitor. Threads coordinate by waiting on a specific object's lock, not on a particular thread. So wait and notify belong to whatever can be a lock — which is every Object. When a thread calls `lock.wait()` it releases that lock and waits; another thread holding the same lock calls `lock.notify()` to wake one waiting thread, or `notifyAll()` to wake all of them, which then compete for the lock. The notifying thread doesn't need to know which threads are waiting — they only share the lock object. They must be called while holding that object's lock, otherwise IllegalMonitorStateException is thrown."

**Trap:** if these were Thread methods, the notifier would have to know exactly which thread to wake — that's the design reason they live on Object.

## CJ40 — Java 8 / 11 / 17 / 21 / 25 features (a: records, b: Optional, c: sealed, d: virtual threads)

**Keywords:** 8 lambdas, streams, Optional, default methods, java.time, CompletableFuture · 11 HttpClient, String isBlank/strip/lines · 17 records, sealed, instanceof patterns, text blocks, switch expressions · 21 virtual threads, switch patterns, record patterns, sequenced collections · 25 scoped values, compact source files, flexible constructors, compact object headers

| Version | Top features |
| --- | --- |
| 8 | Lambdas, Streams, functional interfaces, Optional, default/static interface methods, java.time, CompletableFuture |
| 11 (LTS) | Standard HttpClient, String isBlank / strip / lines / repeat, `var` in lambda params, `java Main.java` |
| 17 (LTS) | Records, sealed classes, pattern matching for instanceof, text blocks, switch expressions |
| 21 (LTS) | Virtual threads, pattern matching for switch, record patterns, sequenced collections, generational ZGC |
| 25 (LTS) | Scoped values, compact source files / instance main, flexible constructor bodies, module imports, compact object headers |

### 40a — Records

See CJ12. Fields are private final; methods you add follow normal rules (not private by default); no setters; accessors `name()`; final in Java 16.

### 40b — Optional

**Model answer:** "Optional, from Java 8, is a container that may or may not hold a value. It's meant as a return type that makes absence explicit, so callers handle it instead of hitting a NullPointerException — Spring Data's `findById` returns one. I create it with `of`, `ofNullable` or `empty`, and use it with `map`, `filter`, `orElse`, `orElseGet`, `orElseThrow` and `ifPresent`. `orElse` always evaluates its argument while `orElseGet` only runs the supplier when empty. I avoid calling `get()` without a check, and I don't use Optional for fields or parameters."

```java
Employee e = repo.findById(id)
                 .filter(Employee::isActive)
                 .orElseThrow(() -> new NotFoundException(id));
```

### 40c — Sealed classes

See CJ12. Keyword is `permits`; permitted subclasses must be final, sealed or non-sealed; enables exhaustive switch.

### 40d — Virtual threads

**Model answer:** "Virtual threads, final in Java 21, are lightweight threads managed by the JVM rather than mapped one-to-one to OS threads. They run on a small pool of carrier threads; when a virtual thread blocks on I/O it unmounts and the carrier runs another one. So I can have millions of them and write simple blocking, thread-per-request code that still scales. They help I/O-bound work, not CPU-bound work, and shouldn't be pooled — I create one per task with `Executors.newVirtualThreadPerTaskExecutor()`, or enable them in Spring Boot 3.2+ with `spring.threads.virtual.enabled=true`. Vert.x solves the same scaling problem with an event loop and callbacks; virtual threads let you keep blocking-style code."

**Traps:**

- Records: methods are not private by default — only the fields are private final.
- Optional: `orElse` evaluates eagerly; use `orElseGet` for expensive defaults.
- Virtual threads: don't pool them; no gain for CPU-heavy work.

## CJ41 — Why is main() static? (a: pass-by-value, b: ways to create objects, c: Object superclass)

**Keywords:** main static → JVM calls it before any object exists, via the class · 41a Java is ALWAYS pass-by-value; for objects the value is a copy of the reference → mutate visible, reassign not · 41b new, reflection, clone, deserialization, factory/builder · 41c Object is the root; A extends B → still extends Object indirectly; methods equals, hashCode, toString, getClass, clone, finalize, wait/notify/notifyAll · cyclic inheritance = compile error

**Model answer:** "`main` is static because the JVM calls it through the class to start the program, before any object exists. Java is always pass-by-value: primitives pass a copy of the value, and objects pass a copy of the reference. Both copies point to the same object, so changes to the object are visible to the caller, but reassigning the parameter isn't. Objects can be created with `new`, through reflection with `getDeclaredConstructor().newInstance()`, with `clone()`, through deserialization — the last two skip the constructor — and via factory or builder methods. `Object` is the root of every class: if A extends B, A still inherits from Object indirectly. It defines equals, hashCode, toString, getClass, clone, finalize, and wait, notify and notifyAll."

**Verified by running (JDK 17):**

```
primitive after change():   5          // value copied
object after reassign():    Anusruta   // caller's reference unchanged
object after mutate():      Ravi       // same object changed
A extends B, B extends A -> error: cyclic inheritance involving A
```

**Traps:**

- "Objects are pass-by-reference" is wrong — it's pass-by-value of the reference.
- clone() and deserialization create objects without calling a constructor.

## CJ42 + CJ43 — Annotations, custom annotations, built-in annotations

**Keywords:** annotation = metadata, does nothing alone; compiler / framework / reflection reads it · custom: `@interface` + `@Retention` (SOURCE / CLASS default / RUNTIME) + `@Target` (TYPE, METHOD, FIELD, PARAMETER) + elements with defaults · validation: `@Constraint(validatedBy=...)` + `ConstraintValidator<A, T>.isValid()` · AOP: annotation + `@Aspect @Around` · built-in: @Override, @Deprecated, @SuppressWarnings, @FunctionalInterface, @SafeVarargs · meta: @Retention, @Target, @Documented, @Inherited, @Repeatable

**Model answer:** "Annotations are metadata attached to code; they do nothing on their own — the compiler, a framework or reflection reads them and acts. `@Override` is checked by the compiler, `@Transactional` is read by Spring at runtime. To create one I declare an `@interface`, set `@Retention` — RUNTIME if frameworks or reflection must read it — and `@Target` for where it can be placed, and add elements with defaults. In Spring I use custom annotations two ways: for validation with `@Constraint` and a `ConstraintValidator` that implements `isValid`, and for cross-cutting logic with an aspect that intercepts methods carrying the annotation. The built-in ones are @Override, @Deprecated, @SuppressWarnings, @FunctionalInterface and @SafeVarargs, plus meta-annotations like @Retention, @Target, @Documented, @Inherited and @Repeatable."

```java
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Constraint(validatedBy = CurrencyValidator.class)
public @interface ValidCurrency {
    String message() default "Unsupported currency";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

public class CurrencyValidator implements ConstraintValidator<ValidCurrency, String> {
    private static final Set<String> ALLOWED = Set.of("INR", "USD", "EUR");
    @Override
    public boolean isValid(String value, ConstraintValidatorContext ctx) {
        return value != null && ALLOWED.contains(value);
    }
}
```

| Retention | Kept until | Example |
| --- | --- | --- |
| SOURCE | Compilation only | @Override |
| CLASS (default) | .class file, not at runtime | — |
| RUNTIME | Runtime, via reflection | @Transactional, custom Spring annotations |

**Traps:**

- The keyword is `@interface`, not a class.
- Without `RetentionPolicy.RUNTIME`, Spring and reflection can't see the annotation.
- The validator method is `isValid`, not `validate`.

## CJ44 — Exception handling (a: hierarchy, b: checked vs unchecked, c: throw vs throws, d: try-with-resources)

44a → see CJ8 · 44b → see CJ9 · 44d → see CJ16 (Java 7, resource implements AutoCloseable, closed in reverse order).

### 44c — throw vs throws

**Keywords:** throw = inside the body, one exception object, actually raises it · throws = method signature, exception types, declares and passes to caller (mainly checked) · catch order specific → general, else compile error · multi-catch `catch (A | B e)` Java 7

**Model answer:** "`throw` is a statement inside a method body that actually raises one exception object, like `throw new IllegalArgumentException("bad id")`. `throws` goes in the method signature and declares the exception types the method may pass to its caller, which is required for checked exceptions it doesn't handle itself. When catching, I order catch blocks from most specific to most general — the reverse doesn't compile — and since Java 7 I can catch several types in one block with `catch (IOException | SQLException e)`."

|  | throw | throws |
| --- | --- | --- |
| Where | Inside method body | Method signature |
| Followed by | One exception object | One or more exception types |
| Purpose | Raise an exception | Declare / delegate to caller |

```java
void load(String path) throws IOException {           // declare
    if (path == null) throw new IllegalArgumentException("path");   // raise
    try (BufferedReader r = Files.newBufferedReader(Path.of(path))) {
        r.readLine();
    } catch (FileNotFoundException | NoSuchFileException e) {        // multi-catch
        log.warn("missing {}", path);
    }
}
```

**Trap:** `catch (Exception e)` before `catch (IOException e)` is a compile error — the second block is unreachable.

## CJ45 — Java Memory Model (a: OutOfMemoryError, b: memory leaks)

**Keywords:** memory areas: heap (shared, objects), stack (per thread, frames, locals, references), Metaspace (class metadata, Java 8+, replaced PermGen) · the JMM proper = how threads see each other's writes: visibility, ordering, happens-before via volatile / synchronized / final / start / join · 45a OOM = heap still full AFTER GC because objects are reachable; it's an Error · types: heap space, Metaspace, GC overhead limit, native thread · diagnose: HeapDumpOnOutOfMemoryError → MAT / VisualVM / JProfiler dominator tree → fix code, raise -Xmx last · 45b leak = reachable but unused: static collections, caches without eviction, unclosed resources, ThreadLocal in pools, listeners

**Model answer:** "The term covers two things. The JVM's memory areas: the heap, shared by all threads, holds objects; each thread has its own stack holding method frames, local primitives and references; Metaspace holds class metadata outside the heap. The Java Memory Model itself defines how threads see each other's writes — visibility and ordering — through happens-before rules created by volatile, synchronized, final fields, and thread start and join. An OutOfMemoryError is thrown when the heap is still full after garbage collection, because objects are still reachable — either a leak or a genuinely larger working set. I'd enable a heap dump on OOM, analyse it in MAT or VisualVM to find which objects retain the most memory, fix the code, and only then consider raising -Xmx. A memory leak in Java is objects that are still referenced but no longer needed — typically ever-growing static collections, caches without eviction, unclosed resources, ThreadLocals left in pooled threads, or listeners never deregistered. I prevent them with bounded caches with TTL, try-with-resources, `ThreadLocal.remove()` in finally, and deregistering listeners."

| Area | Shared? | Holds |
| --- | --- | --- |
| Heap | All threads | Objects (Eden, Survivor, Old) |
| Stack | Per thread | Frames, local primitives, references |
| Metaspace | All threads | Class metadata (outside heap) |
| PC register / native stack | Per thread | Current instruction, native calls |

| Leak cause | Fix |
| --- | --- |
| Static Map/List that only grows | Remove entries, bound it |
| Cache without eviction | Size limit + TTL (Caffeine, LRU, Hazelcast eviction) |
| Unclosed streams/connections | try-with-resources |
| ThreadLocal in a thread pool | `remove()` in finally |
| Listeners never deregistered | Deregister or weak references |

**Traps:**

- OOM is not caused by "GC not running" — GC runs first; the objects are still reachable.
- OutOfMemoryError is an Error, not an Exception.
- Raising -Xmx without finding the leak only delays the crash.

## CJ46 — Iterate over a String without its methods

**Keywords:** puzzle — `length()` and `charAt()` are String methods, so they're banned · clarify what's allowed · `StringReader.read()` until -1 (cleanest) · `StringCharacterIterator` first/next until DONE · if only length() banned: charAt until StringIndexOutOfBoundsException (puzzle only) · if toCharArray allowed: for-each over it

**Model answer:** "First I'd confirm which methods are off-limits. If I can't call any String method, I wrap it in a `StringReader` and read one character at a time until it returns -1, or use a `StringCharacterIterator` with first and next until DONE. If only `length()` is banned, I could call `charAt` until it throws StringIndexOutOfBoundsException, though using exceptions for flow control is bad practice. If `toCharArray()` is allowed, a for-each over the array is the normal approach."

```java
try (Reader r = new StringReader(s)) {
    int c;
    while ((c = r.read()) != -1) System.out.print((char) c);
}

CharacterIterator it = new StringCharacterIterator(s);
for (char c = it.first(); c != CharacterIterator.DONE; c = it.next()) System.out.print(c);
```

**Verified by running (JDK 17):** all three approaches print `J a v a`.

**Trap:** a `for` loop with `length()` and `charAt()` uses exactly the methods the question bans.

## CJ47 — Reactive programming: Mono and Flux

**Keywords:** Project Reactor / Spring WebFlux · Mono = 0..1, Flux = 0..N · implement Reactive Streams `Publisher` · LAZY — nothing runs until subscribe · backpressure: subscriber `request(n)` · operators map / flatMap / filter / zip / timeout / onErrorResume · non-blocking: Netty event loop, few threads, thread freed while waiting, callback continues · never `.block()` on the event loop · WebClient, R2DBC · same idea as Vert.x

**Model answer:** "Mono and Flux are the two publisher types in Project Reactor, which Spring WebFlux is built on. A Mono emits zero or one value, a Flux zero to many. Both implement the Reactive Streams Publisher interface, so they're lazy — nothing happens until something subscribes — and they support backpressure, where the subscriber requests only as many items as it can handle. I build pipelines with operators like map, flatMap, filter, zip and timeout. The I/O is non-blocking because WebFlux runs on Netty with a small event loop: while a call waits on the network or database the thread isn't held, it serves other requests, and the pipeline continues when data arrives. So a few threads handle many concurrent requests — as long as nothing blocks the event loop. Compared with CompletableFuture, a Mono is lazy, can represent streams, and supports backpressure. It's the same principle as Vert.x's event loop."

```java
webClient.get().uri("/users/{id}", id)
         .retrieve().bodyToMono(User.class)         // Mono<User> — nothing sent yet
         .flatMap(u -> orderClient.findOrders(u))   // chain next async call
         .timeout(Duration.ofSeconds(2))
         .onErrorResume(e -> Mono.empty());         // runs when subscribed
```

|  | CompletableFuture | Mono / Flux |
| --- | --- | --- |
| Starts | Immediately | On subscribe (lazy) |
| Values | One | Mono 0..1, Flux 0..N |
| Backpressure | No | Yes |

**Traps:**

- Calling `.block()` inside a WebFlux handler stalls the event loop.
- A Mono that nobody subscribes to never executes.

## Streams

### ST3 / CJ48 — Reverse a String with the Stream API

**Keywords:** `chars()` → `mapToObj` to String → `reduce((s1, s2) -> s2 + s1)` puts each char in front · associative, so safe in parallel · O(n²) concatenation — production: `new StringBuilder(s).reverse()` · alternative: IntStream index from the end + `joining()`

**Your solution (verified: Anusruta → atursunA, works on empty input and in parallel):**

```java
String reversed = name.chars()
        .mapToObj(c -> String.valueOf((char) c))
        .reduce((s1, s2) -> s2 + s1)
        .orElse(name);
```

**Alternative:**

```java
String reversed = IntStream.range(0, name.length())
        .mapToObj(i -> String.valueOf(name.charAt(name.length() - 1 - i)))
        .collect(Collectors.joining());
```

**Say in the interview:** "With streams I'd reduce by prepending each character; in production I'd use `StringBuilder.reverse()`, because repeated string concatenation is O(n²)."

### The 3 recurring Streams mistakes (from the handwritten sheet)

1. `String.valueOf((char) c)` gives a String. For Character keys, sets or comparisons use `mapToObj(c -> (char) c)`; use String + `joining()` only to build a String.
2. `collect(groupingBy(...))` returns a Map — add `.entrySet().stream()` before filter/map.
3. A pipeline must end with a terminal operation (`collect`, `toList`, `joining`). `.toString()` on a stream prints the stream object.

### ST1–ST17 — corrected solutions (all verified by running, JDK 17)

```java
// ST1 — character counts
Map<Character, Long> st1 = str.chars().mapToObj(c -> (char) c)
    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

// ST2 — merge lists
List<Integer> st2 = Stream.concat(list1.stream(), list2.stream()).toList();
// or Stream.of(list1, list2).flatMap(List::stream).toList();   NOT Stream.of(list1, list2).collect(...)

// ST3 — reverse (see above)

// ST4 — remove 'c'
String st4 = s.chars().filter(c -> c != 'c')
    .mapToObj(c -> String.valueOf((char) c)).collect(Collectors.joining());   // "ourane"

// ST5 — duplicate characters
List<Character> st5 = s.chars().mapToObj(c -> (char) c)
    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
    .entrySet().stream()
    .filter(e -> e.getValue() > 1)
    .map(Map.Entry::getKey)
    .toList();                                                                // [r, g, m]

// ST6 — word counts
Map<String, Long> st6 = words.stream()
    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

// ST7 — second-highest DISTINCT word length
int st7 = Arrays.stream(sentence.split(" "))      // arrays have no .stream()
    .map(String::length).distinct()
    .sorted(Comparator.reverseOrder()).skip(1).findFirst().orElse(0);    // 9

// ST8 — even integers from strings
List<Integer> st8 = list.stream().filter(s -> s != null && s.matches("\\d+"))
    .map(Integer::parseInt).filter(n -> n % 2 == 0).toList();            // [8, 20]

// ST9 — names ending with "lal" (null-safe)
List<String> st9a = list.stream().filter(s -> s != null && s.endsWith("lal")).toList();
List<String> st9c = list.stream().filter(s -> s != null && !s.endsWith("lal")).toList();

// ST10 — second largest
Integer st10 = numbers.stream().distinct().sorted(Comparator.reverseOrder())
    .skip(1).findFirst().orElse(null);                                      // 15

// ST11 — unique characters
Set<Character> st11 = input.chars().mapToObj(c -> (char) c).collect(Collectors.toSet());

// ST12 — List<Employee> → Map<id, name>
Map<Integer, String> st12 = employees.stream()
    .collect(Collectors.toMap(Employee::getEmpId, Employee::getEmpName));

// ST13 — same, insertion order
Map<Integer, String> st13 = employees.stream()
    .collect(Collectors.toMap(Employee::getEmpId, Employee::getEmpName,
                              (v1, v2) -> v1, LinkedHashMap::new));

// ST14 — vehicle with most distance in August
Map<Integer, String> nameById = vehicleList.stream()
    .collect(Collectors.toMap(Vehicle::getId, Vehicle::getName));
String st14 = journeyList.stream()
    .filter(j -> j.getDateOfJourney().getMonthValue() == 8)
    .collect(Collectors.groupingBy(Journey::getVehicleId,
             Collectors.summingDouble(Journey::getDistanceTravelled)))
    .entrySet().stream()
    .max(Map.Entry.comparingByValue())
    .map(e -> nameById.get(e.getKey()))
    .orElse("No vehicles travelled in August");

// ST15 — duplicated integers
List<Integer> st15 = nums.stream()
    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
    .entrySet().stream().filter(e -> e.getValue() > 1).map(Map.Entry::getKey).toList();
// or: Set<Integer> seen = new HashSet<>(); nums.stream().filter(n -> !seen.add(n)).distinct().toList();

// ST16 — int[]: not a Collection, so no .stream()
IntStream st16 = Arrays.stream(arr);          // or IntStream.of(arr); .boxed() for Stream<Integer>

// ST17 — two Predicates combined
Predicate<Employee> startsWithA  = e -> e.getName() != null && e.getName().startsWith("A");
Predicate<Employee> mumbaiOrPune = e -> "Mumbai".equalsIgnoreCase(e.getCity())
                                     || "Pune".equalsIgnoreCase(e.getCity());
List<Employee> st17 = employees.stream().filter(startsWithA.and(mumbaiOrPune)).toList();
```

**Traps found on the sheet:**

- ST2: `Stream.of(list1, list2)` is a stream of two lists, not of eight numbers.
- ST7: without `distinct()`, "aaaa bbbb cc" gives 4 instead of 2.
- ST9: `substring(length - 3)` throws StringIndexOutOfBoundsException on short strings — use `endsWith`.
- ST10: without `distinct()`, \[20, 20, 15\] returns 20; `orElse("")` doesn't compile for Integer.
- ST12: `toMap(keyFn, valueFn)` throws IllegalStateException on duplicate keys — add a merge function if keys can repeat.

## Testing block — JUnit 5 + Mockito (CJ28–31, CJ39, CJ50) — to study

Not yet answered in a session. Code written for JUnit 5 + Mockito 5 (included in spring-boot-starter-test); not run here, so confirm it in your IDE.

```java
@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {
    @Mock PaymentGateway gateway;          // fake dependency
    @Mock Notifier notifier;
    @InjectMocks PaymentService service;   // real class under test, mocks injected

    @Test
    void pay_returnsTxId_andNotifies() {
        when(gateway.charge(100)).thenReturn("TX1");
        assertEquals("TX1", service.pay(100));
        verify(notifier).send("Paid 100");                 // void method was called
    }

    @Test
    void pay_rejectsNegative() {
        IllegalArgumentException ex =
            assertThrows(IllegalArgumentException.class, () -> service.pay(-5));
        assertEquals("amount must be positive", ex.getMessage());
        verifyNoInteractions(gateway);
    }

    @Test
    void pay_whenNotifierFails() {
        when(gateway.charge(50)).thenReturn("TX2");
        doThrow(new RuntimeException("SMS down")).when(notifier).send(anyString());
        assertThrows(RuntimeException.class, () -> service.pay(50));
    }
}
```

| Q | Answer |
| --- | --- |
| CJ28 @Mock vs @InjectMocks | @Mock = fake dependency returning defaults unless stubbed; @InjectMocks = real class under test with the mocks injected (constructor, then setter, then field). Needs @ExtendWith(MockitoExtension.class) |
| CJ29 Mock vs Spy | Mock = fully fake; Spy wraps a real object and runs real methods unless stubbed (partial mocking) |
| CJ30 doReturn vs thenReturn | `when(spy.x()).thenReturn(v)` calls the real x() while stubbing; `doReturn(v).when(spy).x()` does not. thenReturn for mocks (type-checked), doReturn for spies |
| CJ31 Mock a void method | `doNothing()` / `doThrow(...)` / `doAnswer(...)` `.when(mock).method(...)`; check with `verify(mock).method(...)`, ArgumentCaptor for arguments |
| CJ39 Exception in a JUnit test | JUnit 5: `assertThrows(Type.class, () -> code)` returns the exception so you can check the message; JUnit 4: `@Test(expected = ...)` |
| CJ50 Unit vs integration | Unit = one class, dependencies mocked, milliseconds; integration = real components (Spring context, DB, Kafka), seconds. Spring: @SpringBootTest; slices @WebMvcTest, @DataJpaTest; Testcontainers for a real DB. Test pyramid: many unit, fewer integration |

### Streams — second handwritten attempt (Tue 17 Nov): new traps

Fixed since Sunday: ST6, ST8, ST9a/b, ST12, ST16, ST10 (compiles), ST17 (getters). Still recurring: `String.valueOf` where a Character is needed (ST1, ST5, ST11).

| Q | Wrote | Problem | Fix |
| --- | --- | --- | --- |
| ST1 | Map\<Character, Integer> | counting() returns Long; String.valueOf gives String keys | Map\<Character, Long> + mapToObj(c -> (char) c) |
| ST2 | Stream.concat(list1, list2) | concat takes streams | Stream.concat(list1.stream(), list2.stream()) |
| ST3 | reduce(...).toString() | reduce returns Optional → "Optional\[maertS\]" | reduce(...).orElse("") |
| ST4 | filter(s -> ...) inside a method that has s | lambda parameter can't reuse a local variable name | rename the parameter; end with collect(joining()) |
| ST5 / ST15 | .toEntrySet() / .entrySet().filter | no toEntrySet(); a Set can't be filtered | .entrySet().stream().filter(...).map(Map.Entry::getKey).toList() |
| ST7 | split(" ").sorted(...) | arrays have no stream methods; no distinct | Arrays.stream(...).map(String::length).distinct()... |
| ST9c | s == null \|\| ... | keeps null in the result | s != null && ... |
| ST10 | no distinct() | \[20, 20, 15\] → 20 | add .distinct() |
| ST13 | toMap(k, v, LinkedHashMap::new) | 3-arg toMap is (key, value, MERGE) | toMap(k, v, (a, b) -> a, LinkedHashMap::new) |
| ST17 | one big lambda | question asks for two Predicate variables | startsWithA.and(mumbaiOrPune) |

**Drill:** write these three lines five times by hand.

```java
Map<Character, Long> m = s.chars().mapToObj(c -> (char) c).collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
Set<Character> set   = s.chars().mapToObj(c -> (char) c).collect(Collectors.toSet());
String joined        = s.chars().filter(c -> c != 'x').mapToObj(c -> String.valueOf((char) c)).collect(Collectors.joining());
```

## Multithreading & Concurrency

### MT2 — ThreadLocal

**Keywords:** one variable, separate copy per thread · no locks, nothing shared · per-request context without passing parameters · uses: SecurityContextHolder, @Transactional connection, MDC correlation IDs, SimpleDateFormat · DANGER: thread pools reuse threads → value leaks to next request (security bug + memory leak) → `remove()` in finally · doesn't work in reactive / Vert.x (thread hopping) · ScopedValue (Java 25) is the safer successor

**Model answer:** "ThreadLocal gives each thread its own independent copy of a variable — one ThreadLocal object, but `get()` returns the value set by the calling thread, so no synchronization is needed. It's used to carry per-request context through many calls without passing it as a parameter: Spring Security keeps the current user in a ThreadLocal, Spring transactions bind the connection to the thread, and logging MDC holds correlation IDs. The danger is thread pools: threads are reused, so a value left behind by one request is visible to the next — a security and memory-leak problem. I always clear it with `remove()` in a finally block. It also doesn't fit reactive frameworks like Vert.x or WebFlux, where a request can move between threads; there you use the framework's context. Java 25's ScopedValue is a safer, immutable alternative."

```java
private static final ThreadLocal<String> CURRENT_USER = new ThreadLocal<>();

try {
    CURRENT_USER.set(user);
    handleRequest();
} finally {
    CURRENT_USER.remove();      // never skip on pooled threads
}
```

**Verified by running (JDK 17):**

```
t2 sees: Ravi
t1 sees: Anusruta
Request B sees (no remove):   Request-A user   <- leak
Request D sees (with remove): null
```

### MT3 — volatile vs Atomic classes

**Keywords:** volatile = visibility + no reordering, NOT atomicity — fine for a flag, not for `count++` · `i++` = read, add, write → lost update · AtomicInteger / Long / Boolean / Reference use CAS (compare-and-swap), lock-free · incrementAndGet (++i), getAndIncrement (i++), addAndGet(delta), compareAndSet · LongAdder for very high contention

**Model answer:** "`volatile` guarantees that every read sees the latest write from main memory and prevents reordering, so it's enough for something like a stop flag. It doesn't make compound operations atomic: `count++` is read, increment, write, so two threads can both read 1 and both write 2, losing an update. Atomic classes such as AtomicInteger, AtomicLong, AtomicBoolean and AtomicReference fix that with compare-and-swap — a lock-free CPU instruction that updates the value only if it still holds the expected value, otherwise it retries. I use `incrementAndGet`, `addAndGet` or `compareAndSet`. Under very heavy contention, LongAdder scales better than AtomicLong."

|  | volatile | Atomic classes |
| --- | --- | --- |
| Visibility | Yes | Yes |
| Atomic compound ops (count++) | No | Yes (CAS) |
| Locking | None | None (lock-free) |
| Use for | Flags, published references | Counters, sequence numbers, CAS updates |

**Trap:** don't say volatile is unrelated to thread safety — it provides visibility, just not atomicity.

### MT9 — ExecutorService; fixed vs cached thread pool

**Keywords:** ExecutorService = interface (java.util.concurrent), separates task submission from thread management · submit() → Future, execute(), shutdown() / shutdownNow() / awaitTermination · fixed(n): n threads + UNBOUNDED queue → OOM under load · cached: unlimited threads, SynchronousQueue, 60 s idle → thread explosion · single, scheduled, virtual-per-task · production: ThreadPoolExecutor with core, max, bounded queue, rejection policy (CallerRuns) · flow: core → queue → max → reject

**Model answer:** "ExecutorService is an interface in java.util.concurrent that separates submitting tasks from managing threads: I submit Runnables or Callables, get Futures back, and the pool reuses its threads; I shut it down when done. `newFixedThreadPool(n)` keeps n threads but uses an unbounded queue, so under sustained load tasks pile up until memory runs out. `newCachedThreadPool()` has no queue and creates a new thread whenever none is idle, so a traffic spike can create thousands of threads. In production I build a ThreadPoolExecutor explicitly with a core size, a max size, a bounded queue and a rejection policy such as CallerRunsPolicy, which applies back-pressure. New tasks go to a core thread first, then the queue, then extra threads up to max, and are rejected only when everything is full."

```java
ExecutorService pool = new ThreadPoolExecutor(
    2, 4,                                        // core, max
    60, TimeUnit.SECONDS,                        // idle timeout for extra threads
    new ArrayBlockingQueue<>(10),                // bounded queue
    new ThreadPoolExecutor.CallerRunsPolicy());  // back-pressure when full
```

| Pool | Threads | Queue | Risk |
| --- | --- | --- | --- |
| newFixedThreadPool(n) | n | Unbounded LinkedBlockingQueue | Queue grows → OOM |
| newCachedThreadPool() | 0 → unlimited, 60 s idle | SynchronousQueue | Thread explosion |
| newSingleThreadExecutor() | 1 | Unbounded | Sequential order |
| newScheduledThreadPool(n) | n | Delay queue | Delayed / periodic tasks |
| newVirtualThreadPerTaskExecutor() | 1 virtual per task | — | I/O-bound work (Java 21) |

**Verified by running (JDK 17), 1000 slow tasks:**

```
FIXED(2):  threads=2,    queued=998
CACHED:    threads=1000, queued=0
BOUNDED(core 2, max 4, queue 10), 20 tasks: threads=4, queued=10, rejected=6
```

### MT10 — Deadlock

**Keywords:** threads blocked forever, each holding a lock the other needs · 4 conditions: mutual exclusion, hold-and-wait, no preemption, circular wait — break one to prevent · fixes: consistent lock order, `tryLock(timeout)`, fewer nested locks / smaller scope, higher-level concurrency utilities · detect: jstack thread dump ("Found one Java-level deadlock"), JConsole / VisualVM, `ThreadMXBean.findDeadlockedThreads()`

**Model answer:** "A deadlock is when two or more threads are blocked forever because each holds a lock the other needs — thread 1 holds A and waits for B, thread 2 holds B and waits for A. It needs four conditions at once: mutual exclusion, hold-and-wait, no preemption and circular wait, so preventing it means breaking one. The most common fix is acquiring locks in the same global order everywhere, which removes the circular wait. Another is ReentrantLock's `tryLock` with a timeout, so a thread that can't get the second lock releases the first and retries. I also keep lock scopes small, avoid calling external code while holding a lock, and prefer concurrent collections and atomics. In production I'd take a thread dump with jstack, which reports the deadlock and the locks involved, or use ThreadMXBean's findDeadlockedThreads in a health check."

```java
// Deadlock: opposite order
Thread t1 = new Thread(() -> { synchronized (A) { synchronized (B) { } } });
Thread t2 = new Thread(() -> { synchronized (B) { synchronized (A) { } } });

// Fix: same order everywhere
Thread f1 = new Thread(() -> { synchronized (A) { synchronized (B) { } } });
Thread f2 = new Thread(() -> { synchronized (A) { synchronized (B) { } } });
```

**Verified by running (JDK 17):**

```
Deadlocked threads found: 2
  T1 is BLOCKED waiting for lock held by T2
  T2 is BLOCKED waiting for lock held by T1
fixed t1 done
fixed t2 done
```

**Trap:** "atomicity" doesn't prevent deadlock — lock ordering and timeouts do. A deadlocked thread keeps its locks forever, so other work needing those locks freezes too.

### MT6 — Object-level lock vs class-level lock

**Keywords:** instance `synchronized` / `synchronized(this)` → locks that object · `static synchronized` / `synchronized(X.class)` → locks the single Class object · independent: a static and an instance synchronized method run at the same time · different instances don't block each other

**Model answer:** "A synchronized instance method or `synchronized(this)` locks that particular object, so only threads using the same instance are serialised. A static synchronized method or `synchronized(MyClass.class)` locks the Class object, of which there's one per class, so it serialises all callers regardless of instance. The two are separate locks: a thread in a static synchronized method doesn't block a thread in an instance synchronized method."

|  | Object-level | Class-level |
| --- | --- | --- |
| Syntax | synchronized instance method, synchronized(this / obj) | static synchronized, synchronized(X.class) |
| Lock object | That instance | X.class |
| Blocks | Threads on the same instance | All threads entering it |

### MT7 — ReentrantLock

**Keywords:** `Lock` = interface, `ReentrantLock` = implementation · lock() + unlock() in finally · extras over synchronized: tryLock / tryLock(timeout), lockInterruptibly, fairness, multiple Conditions, lock and unlock in different methods · reentrant = same thread re-acquires, hold count, unlock as many times · synchronized is also reentrant

**Model answer:** "ReentrantLock implements the Lock interface and is an explicit alternative to synchronized. I call `lock()` and always `unlock()` in a finally block. It adds what synchronized can't do: `tryLock`, optionally with a timeout, so a thread can give up instead of blocking forever — useful against deadlocks; `lockInterruptibly`; an optional fairness policy; and multiple Condition objects for separate wait queues. Reentrant means the thread that holds the lock can acquire it again without blocking itself; it keeps a hold count and must unlock the same number of times. I use synchronized for simple cases and ReentrantLock when I need timeouts, interruptibility, fairness or conditions."

```java
private final Lock lock = new ReentrantLock();

if (lock.tryLock(200, TimeUnit.MILLISECONDS)) {
    try { /* critical section */ }
    finally { lock.unlock(); }
} else {
    // couldn't get the lock in time — back off or retry
}
```

**Verified by running (JDK 17):**

```
0ms    instance lock (this) START
0ms    class lock (Locks.class) START     <- run concurrently
hold count = 2                            <- reentrant
other thread tryLock(200ms) -> false
```

**Trap:** Lock is the interface and ReentrantLock the class — not the other way round.

### MT4 — CountDownLatch vs CyclicBarrier

**Keywords:** latch: waiting threads `await()` until count hits 0 via `countDown()` (any thread, any number of times) · ONE-SHOT · barrier: N parties each `await()` until all arrive, optional barrier action runs once, then all released together · REUSABLE (resets) · "latch: one waits for many, once; barrier: many wait for each other, repeatedly"

**Model answer:** "A CountDownLatch lets one or more threads wait until a count of events reaches zero: workers call `countDown()`, waiters call `await()`, and once it reaches zero it stays there — it can't be reused. I'd use it to make the main thread wait until several services have started or several parallel calls have finished. A CyclicBarrier makes a fixed number of threads wait for each other: each calls `await()` and blocks until all have arrived, then an optional barrier action runs once and all are released together. It resets automatically, so it suits work done in rounds, where each worker finishes its chunk, the results are merged, and the next round starts."

|  | CountDownLatch | CyclicBarrier |
| --- | --- | --- |
| Idea | Waiters wait for N events | N threads wait for each other |
| Methods | countDown() + await() | await() by every party |
| Reusable | No (one-shot) | Yes (resets) |
| Barrier action | No | Optional Runnable |
| Use case | Wait for 3 services / 3 parallel calls | Multi-phase parallel computation |

```java
CountDownLatch ready = new CountDownLatch(3);
for (int i = 0; i < 3; i++) pool.submit(() -> { startService(); ready.countDown(); });
ready.await();                                   // main continues when all 3 are up

CyclicBarrier barrier = new CyclicBarrier(3, () -> mergeResults());
Runnable worker = () -> { for (int round = 0; round < 5; round++) { computeChunk(); barrier.await(); } };
```

**Trap:** with a barrier, threads don't hand over one by one — they all block in await() and are released together when the last arrives.

### MT5 — CompletableFuture: runAsync() vs supplyAsync()

See CJ25 for Future vs CompletableFuture.

**Keywords:** runAsync(Runnable) → CompletableFuture\<Void>, no result · supplyAsync(Supplier\<T>) → CompletableFuture\<T> · default ForkJoinPool.commonPool · overloads take your own Executor — use it for I/O · chain with thenApply / thenCompose / exceptionally

**Model answer:** "Both start a task asynchronously. `runAsync` takes a Runnable and returns `CompletableFuture<Void>` because there's no result — for fire-and-forget work like audit logging. `supplyAsync` takes a `Supplier<T>` and returns `CompletableFuture<T>` — for work that produces a value, like calling a payment gateway. Both run on the common ForkJoinPool by default, and both have overloads that take an Executor, which I use for blocking I/O so the common pool isn't starved. The result can then be chained with thenApply, thenCompose or exceptionally instead of blocking on get()."

```java
CompletableFuture<Void>   audit = CompletableFuture.runAsync(() -> auditLog.write(tx), ioPool);
CompletableFuture<String> txId  = CompletableFuture.supplyAsync(() -> gateway.charge(100), ioPool);
txId.thenApply(String::toUpperCase).exceptionally(ex -> "FAILED");
```

### MT11 — sleep() vs wait() vs join()

See CJ24 for sleep vs wait vs yield.

**Keywords:** sleep = static Thread, milliseconds, keeps locks · wait = Object, releases lock, needs synchronized, woken by notify · join = Thread instance method: the CALLING thread waits until that thread finishes · join(ms) timeout · join uses wait() internally

**Model answer:** "`sleep(ms)` is a static Thread method that pauses the current thread for a time and keeps any locks it holds. `wait()` is an Object method used inside synchronized code; it releases the lock and waits for notify or notifyAll. `join()` is an instance method on Thread: calling `t2.join()` makes the current thread wait until t2 has finished, optionally with a timeout. A typical use is the main thread starting several workers and joining each one before combining their results."

|  | sleep(ms) | wait() | join() |
| --- | --- | --- | --- |
| Class | Thread (static) | Object | Thread (instance) |
| Caller waits for | Time | notify / notifyAll | That thread to finish |
| Releases lock | No | Yes | Yes (on the thread object) |
| Needs synchronized | No | Yes | No |
| Timeout | Itself | wait(ms) | join(ms) |

**Verified by running (JDK 17):**

```
main: calling t2.join()  -> main will now WAIT
t2 finished its work
main: resumed after t2 finished
```

**Trap:** `t2.join()` doesn't make t2 wait — it makes the caller wait for t2.

### MT1 — Odd/even printer with two threads

**Keywords:** one shared lock · `while (not my turn) wait()` · ONE shared stop condition for both threads · `notifyAll()` after each print AND before exiting · one Runnable parameterised by parity · plain int is enough inside synchronized · restore interrupt flag

**Your first version (Tue 17 Nov):** printed 1–9, never printed 10, and hung. The odd loop used `num <= MAX` and the even loop `num < MAX`, so the even thread exited at 10 without printing or notifying, leaving the odd thread waiting forever (thread dump: WAITING in Object.wait). The ExecutorService version hangs the same way — `shutdown()` doesn't stop a running task.

```java
private static int num = 1;
private static final int MAX = 10;
private static final Object lock = new Object();

static Runnable printer(int parity) {               // 1 = odd, 0 = even
    return () -> {
        while (true) {
            synchronized (lock) {
                while (num <= MAX && num % 2 != parity) {
                    try { lock.wait(); }
                    catch (InterruptedException e) { Thread.currentThread().interrupt(); return; }
                }
                if (num > MAX) { lock.notifyAll(); return; }   // wake the other thread, then exit
                System.out.println(num + " printed by " + Thread.currentThread().getName());
                num++;
                lock.notifyAll();
            }
        }
    };
}

ExecutorService pool = Executors.newFixedThreadPool(2);
pool.execute(printer(1));
pool.execute(printer(0));
pool.shutdown();
```

**Verified by running (JDK 21):** prints 1–10 alternating pool-1-thread-1 / thread-2, then `finished cleanly? true`; also correct with MAX = 7.

**Traps:**

- Different stop conditions in the two threads → one skips the last number and the other waits forever.
- Exiting without notifyAll() strands the other thread.
- AtomicInteger inside synchronized is redundant.

### MT14 — N threads printing 1..N in round-robin

**Keywords:** same template as MT1, parameterised by thread id · start at 1, turn check `(num - 1) % n == id` · ALWAYS an exit branch: `if (done) { notifyAll(); return; }` · without it: busy-spin at full CPU forever · print "THREAD-" + (id + 1)

**Template for any "threads in turn" question:**

```
while (true) {
    synchronized (lock) {
        while (!done && !myTurn) wait();
        if (done) { notifyAll(); return; }
        print; advance; notifyAll();
    }
}
```

**Your first version (Tue 17 Nov):** started at 0 (printed 0–10, eleven numbers) and had no exit branch — after 10 all three threads spun in `while (true)` holding and releasing the lock: \~190% CPU, all threads RUNNABLE, never finished.

```java
Object lock = new Object();
int[] num = {1};
int max = 10, n = 3;
ExecutorService pool = Executors.newFixedThreadPool(n);
for (int i = 0; i < n; i++) pool.execute(print(lock, num, max, n, i));
pool.shutdown();

static Runnable print(Object lock, int[] num, int max, int n, int id) {
    return () -> {
        while (true) {
            synchronized (lock) {
                while (num[0] <= max && (num[0] - 1) % n != id) {
                    try { lock.wait(); }
                    catch (InterruptedException e) { Thread.currentThread().interrupt(); return; }
                }
                if (num[0] > max) { lock.notifyAll(); return; }
                System.out.println("THREAD-" + (id + 1) + " : " + num[0]);
                num[0]++;
                lock.notifyAll();
            }
        }
    };
}
```

**Verified by running (JDK 21):** THREAD-1 : 1, THREAD-2 : 2, THREAD-3 : 3, THREAD-1 : 4 … THREAD-1 : 10, then `finished cleanly? true`.

**Traps:**

- No exit branch → infinite busy loop, worse than a hang because it burns CPU.
- `new int[1]` starts at 0, not 1.
- notifyAll (not notify) with 3+ threads, so the right thread is guaranteed to wake.

### MT12 — Blocking queue with wait()/notify()

**Keywords:** put blocks while full, take blocks while empty — nothing dropped or rejected (back-pressure) · synchronized methods · `while`, not `if`, around wait() · notifyAll() after every change (several producers and consumers) · declare throws InterruptedException · follow-up: ReentrantLock + two Conditions (notFull, notEmpty) · JDK: add throws, offer returns false, put waits

**Your solution (Tue 17 Nov) — correct first time.** Stress test (JDK 21): 2 producers × 5000 items, 2 consumers, capacity 2 → 10000 / 10000 consumed, 0 duplicates, 0 left, no hang.

```java
public class CustomBlockingQueue<T> {
    private final int capacity;
    private final Queue<T> queue = new LinkedList<>();

    public CustomBlockingQueue(int capacity) { this.capacity = capacity; }

    public synchronized void put(T item) throws InterruptedException {
        while (queue.size() == capacity) wait();
        queue.offer(item);
        notifyAll();
    }

    public synchronized T take() throws InterruptedException {
        while (queue.isEmpty()) wait();
        T item = queue.poll();
        notifyAll();
        return item;
    }
}
```

**Follow-up — ReentrantLock with two Conditions** (producers wake only consumers and vice versa):

```java
private final Lock lock = new ReentrantLock();
private final Condition notFull = lock.newCondition(), notEmpty = lock.newCondition();

public void put(T item) throws InterruptedException {
    lock.lock();
    try {
        while (queue.size() == capacity) notFull.await();
        queue.offer(item);
        notEmpty.signal();
    } finally { lock.unlock(); }
}

public T take() throws InterruptedException {
    lock.lock();
    try {
        while (queue.isEmpty()) notEmpty.await();
        T item = queue.poll();
        notFull.signal();
        return item;
    } finally { lock.unlock(); }
}
```

| Method (java.util.concurrent.BlockingQueue) | When full |
| --- | --- |
| add(e) | Throws IllegalStateException |
| offer(e) | Returns false |
| put(e) | Waits for space |

**Traps:** `if` instead of `while` breaks on spurious wakeups; `notify()` with several producers and consumers can wake the wrong kind of thread and stall.
