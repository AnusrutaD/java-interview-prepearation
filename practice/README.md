# Practice code — Streams & Concurrency

Every file here is **self-contained** and was **compiled and run on JDK 17** (outputs below).

## How to run

No build tool needed (JDK 11+ single-file launch):

```bash
cd practice
java streams/StreamsPractice.java
java concurrency/MT14_RoundRobinPrinter.java
```

Or compile everything: `javac -d out streams/*.java concurrency/*.java`

**Practice tip:** cover the code, solve the question yourself, then run the file and compare.

---

## Streams — `streams/StreamsPractice.java` (ST1–ST17 in one file)

| Q | Question | Expected output |
|---|---|---|
| ST1 | Count each character in `"abcdabcghsk"` | `{a=2, b=2, c=2, d=1, ...}` |
| ST2 | Merge `[1,2,3,4]` + `[5,6,7,8]` | `[1..8]` (`Stream.concat` or `flatMap`) |
| ST3 | Reverse `"Stream"` | `maertS` |
| ST4 | Remove every `'c'` from `"occurance"` | `ourane` |
| ST5 | Duplicate characters in `"programming"` | `[r, g, m]` |
| ST6 | Count each word | `{apple=2, banana=2, orange=1}` |
| ST7 | Second-highest **distinct** word length | `9` |
| ST8 | Even integers from mixed strings | `[8, 20]` |
| ST9 | Names ending / not ending with "lal", null-safe | `[Mohanlal, Jawaharlal]` / `[Ram, Vinitha]` |
| ST10 | Second-largest number | `15` |
| ST11 | Unique characters in `"hello world"` | set of chars |
| ST12 | `List<Employee>` → `Map<id, name>` | `toMap(id, name)` |
| ST13 | Same, insertion order | 4-arg `toMap(..., (a,b)->a, LinkedHashMap::new)` |
| ST14 | Vehicle that travelled most in August | `Swift` |
| ST15 | Duplicated integers | `[2, 3]` |
| ST16 | `.stream()` on `int[]`? | No → `Arrays.stream(arr)` / `IntStream.of` |
| ST17 | Name starts with 'A' and city Mumbai/Pune | `Predicate.and(...)` |

### The 3 recurring Streams mistakes
1. `chars()` gives an `IntStream` → use `mapToObj(c -> (char) c)` (or `String.valueOf((char) c)` to get Strings).
2. After `groupingBy(...)` you get a **Map** → call `.entrySet().stream()` before filtering/sorting entries.
3. Nothing runs without a **terminal operation** (`collect`, `toList`, `findFirst`, `forEach`, ...).

Traps also shown in the file: `distinct()` before "second highest" (tie test), `substring` on short strings, `null` in the list.

---

## Concurrency — `concurrency/`

| File | Question | What it shows |
|---|---|---|
| `MT01_OddEvenPrinter.java` | MT1 — two threads print odd / even | wait/notifyAll turn template + clean exit for both threads |
| `MT14_RoundRobinPrinter.java` | MT14 — N threads print 1..N round-robin | turn check `(num - 1) % n != id`, exit branch |
| `MT12_CustomBlockingQueue.java` | MT12 — blocking queue with wait/notify | `while` + `wait()`, `notifyAll()`; stress test 2×2 threads |
| `MT12_PubSubReentrantLock.java` | MT12 variant — pub-sub with ReentrantLock | one shared lock + `notFull` / `notEmpty` conditions |
| `MT10_Deadlock.java` | MT10 — deadlock | creates one, detects it with `ThreadMXBean`, fixes by lock ordering |
| `MT02_ThreadLocal.java` | MT2 — ThreadLocal | per-thread value; leak in thread pools; `remove()` in `finally` |
| `MT06_MT07_Locks.java` | MT6 + MT7 | object vs class lock run in parallel; reentrancy (hold count); `tryLock` |
| `MT09_ThreadPools.java` | MT9 — fixed vs cached pool | 998 queued tasks vs 1000 threads; bounded pool + rejection |
| `MT11_Join.java` | MT11 — join | the caller waits for the other thread |

### Turn-taking template (MT1 / MT14) — memorise this shape
```java
while (true) {
    synchronized (lock) {
        while (!done && !myTurn) lock.wait();   // WHILE, never if
        if (done) { lock.notifyAll(); return; } // let the other threads exit too
        print(); advance();
        lock.notifyAll();
    }
}
```

### Not here yet
MT8 (thread safety overview) and MT13 (Fork/Join + work stealing): theory, see the revision notes.
