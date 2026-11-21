# Practice code — Streams & Concurrency

Every file is **self-contained** and **pure Java 8**: compiled with `javac --release 8` and run on JDK 8+, 11 and 17.

## Setup in IntelliJ (once)
1. **File → Open** the `java-interview-prepearation` folder.
2. Right-click the **`practice`** folder → **Mark Directory as → Sources Root** (the Streams files use packages like `streams.questions`).
3. **File → Project Structure → Project**: SDK = **1.8**, Language level = **8**.
4. Open any file and click the green **Run ▶** arrow next to `main()`.

## Streams — practise like a test

```
streams/
├── questions/   ST01 … ST17  ← question written at the top, empty method (TODO), built-in PASS/FAIL tests
└── solutions/   ST01 … ST17  ← same file with the answer + WHY / TRAPS notes
```

**How to practise one question**
1. Open `streams/questions/ST07_SecondHighestWordLength.java` and read the question in the header comment.
2. Write your stream inside the method marked `// TODO`. Hints are in the header if you get stuck.
3. Run it: green **Run ▶** arrow in IntelliJ, or from the `practice/` folder in a terminal:
   ```bash
   ./run-streams.sh 07
   ```
   Every test prints `PASS` or `FAIL` with the expected vs actual value.
4. Only then open `streams/solutions/ST07_...java` and compare. Read the **WHY / TRAPS** section.

**Scoreboard for all 17**
```bash
./run-streams.sh             # your progress: "DONE" / "todo" for each question, then "x / 17 solved"
./run-streams.sh 07          # run just ST07 with full output
./run-streams.sh solutions   # check the reference answers (17 / 17)
```

**To practise again from scratch:** `git checkout -- streams/questions` resets your answers (commit first if you want to keep them).

The script compiles with `javac --release 8` when your JDK supports it, so even on Java 21/25 it **rejects Java 9+ APIs** (`List.of`, `var`, records...) exactly like Java 8 would. No Maven needed.

| Q | Question | Tests check |
|---|---|---|
| ST01 | Count each character in `"abcdabcghsk"` | `{a=2, b=2, c=2, d=1, g=1, h=1, s=1, k=1}` |
| ST02 | Merge `[1,2,3,4]` + `[5,6,7,8]` | `[1..8]` |
| ST03 | Reverse `"Stream"` | `maertS`, empty string |
| ST04 | Remove every `'c'` from `"occurance"` | `ourane` |
| ST05 | Duplicate characters in `"programming"` | `{r, g, m}`, none in `"abc"` |
| ST06 | Count each word | `{apple=2, banana=2, orange=1}` |
| ST07 | Second-highest **distinct** word length | `9`, tie case `2` |
| ST08 | Even integers from mixed strings | `[8, 20]` |
| ST09 | Names ending / not ending with "lal", null-safe | list contains `null` |
| ST10 | Second-largest number | `15`, `[20,20,15]` → 15, single element → null |
| ST11 | Unique characters in `"hello world"` | a) distinct set b) chars appearing exactly once, in order |
| ST12 | `List<Employee>` → `Map<id, name>` | |
| ST13 | Same, insertion order | must be a `LinkedHashMap`, keys `[3, 1, 2]` |
| ST14 | Vehicle that travelled most in August | `Swift`, and no-August case |
| ST15 | Duplicated integers | `{2, 3}`, none |
| ST16 | `.stream()` on `int[]`? | sum = 6, boxed list |
| ST17 | Name starts with 'A' AND city Mumbai/Pune | `[Anusruta, Amit]` (Ravi/Pune and Arjun/Delhi excluded) |

### The 3 recurring Streams mistakes
1. `chars()` gives an `IntStream` → `mapToObj(c -> (char) c)`, or `String.valueOf((char) c)` when you need Strings.
2. After `groupingBy(...)` you have a **Map** → `.entrySet().stream()` before filtering entries.
3. Nothing runs without a **terminal operation** (`collect`, `toList`, `findFirst`, ...).

---

## Concurrency — `concurrency/`

Run in IntelliJ (green ▶ arrow), or in a terminal from `practice/`:
```bash
javac -d out concurrency/MT14_RoundRobinPrinter.java && java -cp out concurrency.MT14_RoundRobinPrinter
```

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
