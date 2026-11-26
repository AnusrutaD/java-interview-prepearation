# Microservices — Revision Notes (Q&A)

Week 2 prep for the Spring + Microservices mock. One entry per question: **Keywords**, **Model answer**, **Traps**.
Question IDs match `plans/Week2_Microservices_SpringBoot_Plan.pdf` (MS/DS = question bank, TR/REST/DEP = ProgressTracker).

Tags: 🟢 Strong · 🟡 Partial · 🔴 Weak

### TR1 — Have you worked on microservices? — 🟡 Partial (confident, but too generic)

**Keywords:** who you work for (Grid Dynamics → Visa CyberSource) · team (AS Platform, shared services) · ~7 services owned · 2 named services + what they do · sync (HTTP) + async (Kafka) · one result with numbers · hook

**Model answer (~60 sec):**
> "Yes. I'm with Grid Dynamics, working on Visa's **CyberSource payment-gateway platform**, which runs a few hundred microservices. I'm in the **AS Platform team**, the shared-services team, and I own about **seven services end to end**. Two examples: **mks-proxy**, a Vert.x reverse proxy that terminates TLS and routes requests using the service registry; and **config-loader-utility**, which loads compliance configuration from **Consul**, certificates from **Vault**, and validates incoming requests. Services talk **synchronously over HTTP** on the request path and **asynchronously over Kafka** for data sync. The piece I'm proudest of is a **Kafka-based sync pipeline with a two-tier Hazelcast cache across 50 instances**, which brought the transaction failure rate from **60% to under 5% at 15,000+ RPS**. Happy to go deeper into any of these."

**Structure:** context (company / platform / team) → what you own (named services) → how they communicate → one measurable win → stop.

**Traps:** say "with Grid Dynamics, on the Visa platform", not "in the Visa engineering team"; the interviewer may check. "Handling inter-service communication and architecture" is vague; name the services and the tech. Always end on a number, then stop talking.

### TR2 — Monolithic vs microservice application — 🟡 Partial (right idea and good verdict; two wrong claims, missing the data point, too long)

**Keywords:** one deployable unit vs many independently deployable services · organised by business capability · **database per service** · scale per service · fault isolation · team autonomy (Conway's law) · cost: network calls, distributed data, observability, ops · modular monolith first

**Model answer (~90 sec):**
> "A **monolith** is one codebase deployed as **one unit**, usually with **one shared database**. It's simple to build, test, debug and deploy, and calls between modules are in-process and fast. The problem shows up as it grows: any small change means rebuilding and redeploying the **whole app**, you can only scale the **whole app**, one bug or memory leak can take **everything** down, and many teams step on each other in one codebase.
>
> **Microservices** split the system into small services around **business capabilities** (orders, payments, users). Each is **independently deployable**, **owns its own database**, and is scaled and owned by one team. You get independent releases, **per-service scaling**, **fault isolation** and technology freedom.
>
> The price is **distributed-systems complexity**: network latency and failures between services (so timeouts, retries, circuit breakers), no single ACID transaction across services (so Saga / eventual consistency), and you need service discovery, an API gateway, distributed tracing, centralised logging and good CI/CD.
>
> **My rule:** small product, one or two teams → a **modular monolith**. Large domain, many teams, parts with very different scaling needs → microservices, ideally extracted step by step with the **strangler fig** pattern."

| | Monolith | Microservices |
|---|---|---|
| Deployment | whole app every time | one service at a time |
| Scaling | whole app (more copies of everything) | only the hot service |
| Database | one shared DB | database per service |
| Failure | one bug can take it all down | isolated (with circuit breakers) |
| Communication | in-process method calls | network calls: HTTP / gRPC / Kafka |
| Transactions | local ACID | Saga, eventual consistency |
| Teams | shared codebase | one team per service |
| Ops cost | low | high: discovery, gateway, tracing, K8s |

**Traps:**
- "Monolith = downtime on every deploy" is **wrong**: run several instances behind a load balancer and do rolling or blue-green deploys. The real problem is that you must redeploy and retest **everything** for a small change.
- "A monolith can't scale" is **wrong**: it scales horizontally too, but only **as a whole**. You can't scale just the payment module.
- A **load balancer is not microservices-specific**; monoliths use one as well. Gateway, service discovery and tracing are the microservices-specific additions.
- Don't skip **database per service**; it's the line interviewers listen for. A shared database between services = a "distributed monolith".

### TR12 — Strangler fig (monolith → microservices migration) — 🟡 Partial (correct steps, concise; missing the routing layer and the data migration)

**Keywords:** incremental, no big-bang rewrite · **routing facade in front** (API gateway / reverse proxy) · extract one capability → route its traffic → retire old code · gradual traffic shift + instant rollback · **data migration** (own DB, CDC / events during transition) · anti-corruption layer · start low-risk, loosely coupled

**Model answer (~75 sec):**
> "Strangler fig is how you migrate a monolith **incrementally** instead of a big-bang rewrite. The name comes from the fig vine that grows around a tree until it replaces it.
>
> 1. Put a **routing facade** in front of the monolith, usually an **API gateway or reverse proxy**. At first it sends everything to the monolith.
> 2. Pick one **low-risk, loosely coupled capability**, say notifications, and build it as a new service with **its own database**.
> 3. Change the routing so that capability's paths, e.g. `/notifications/**`, go to the new service. Shift traffic **gradually** (canary or feature flag); if something breaks, **flip the route back**.
> 4. Once it's stable, **delete that code** from the monolith. Repeat, capability by capability, until the monolith is gone or small enough to keep.
>
> The hardest part is the **data**: during the transition both sides may need the same data, so you sync it with **CDC (e.g. Debezium) or events**, and you add an **anti-corruption layer** so the new service's clean model isn't polluted by the monolith's.
>
> On my platform, **mks-proxy** is exactly that kind of routing layer: header- and registry-based routing in front of services."

**Traps:**
- Without naming the **routing facade**, the answer has no mechanism: "move traffic" *how*? Through the gateway or proxy.
- Don't forget the **database**. Extracting the code but keeping the shared DB = a distributed monolith.
- Start with **edge or low-risk** modules, not the core transaction flow.
- New features go into new services from day one; don't keep growing the monolith.

### MS2 — How do two microservices communicate (sync vs async)? — 🟡 Partial (concepts and example correct; ~4 min long, no trade-offs or failure handling)

**Keywords:** sync = request/response, caller waits (REST via RestTemplate / WebClient / OpenFeign, gRPC) · async = events/messages via broker (Kafka, RabbitMQ), caller doesn't wait · temporal coupling · failures: timeouts + retries + circuit breaker vs at-least-once + idempotent consumer + DLQ · eventual consistency · use sync for queries that need an answer now, async for side effects / workflows

**Model answer (~2 min):**
> "There are two styles.
>
> **Synchronous**: request/response. The caller sends a request and **waits** for the answer: REST over HTTP with **RestTemplate, WebClient or OpenFeign**, or **gRPC** for internal low-latency calls. It's simple and you get the answer immediately, but the services are **temporally coupled**: if inventory is slow or down, order is slow or down too, and latency adds up along the chain. So every sync call needs a **timeout, retries with backoff and a circuit breaker**.
>
> **Asynchronous**: the service **publishes an event or message** to a broker, **Kafka** or **RabbitMQ**, and carries on; consumers process it in their own time. That **decouples** services (the consumer can be down and catch up later), absorbs traffic spikes, and lets many consumers react to one event (pub-sub). The price is **eventual consistency**, harder debugging, and **at-least-once delivery**, so consumers must be **idempotent**, with a **dead-letter queue** for poison messages.
>
> **When to use which:** sync when the caller needs the answer **right now** to continue, e.g. checking a price or validating a card; async for **side effects and long workflows**, e.g. 'order placed' → inventory, payment, notification.
>
> On our platform the request path is synchronous HTTP through the proxy, and data sync between services goes through **Kafka**."

| | Synchronous | Asynchronous |
|---|---|---|
| Tools | RestTemplate, WebClient, OpenFeign, gRPC | Kafka, RabbitMQ, SQS/SNS |
| Caller | waits for the response | publishes and continues |
| Coupling | temporal: both must be up | decoupled in time |
| Consistency | immediate | eventual |
| Failure handling | timeout, retry + backoff, circuit breaker | at-least-once → idempotent consumer, retry topic, DLQ |
| Use for | queries needing an answer now | events, side effects, long workflows, spikes |

**Traps:**
- **Length**: don't narrate the whole order → inventory → payment flow step by step. One sentence: "e.g. order calls inventory then payment over REST, and the user waits for the whole chain."
- Kafka is a **distributed log**, not just a queue. With consumer groups it does both point-to-point and pub-sub. RabbitMQ is a classic message broker / queue.
- `RestTemplate` is in maintenance mode → prefer `WebClient` or the newer `RestClient`; OpenFeign = declarative REST client.
- The async order → inventory → payment flow you described is a **choreography Saga** (Wednesday's topic): name it.

### TR16 — Sync communication: RestTemplate vs WebClient (vs RestClient, OpenFeign) — 🟢 Strong (short and correct; refine "always WebClient")

**Keywords:** RestTemplate = blocking, thread-per-request, maintenance mode · WebClient = non-blocking reactive (Mono/Flux), needs WebFlux, few threads · RestClient (Spring 6.1) = modern fluent **blocking** client, RestTemplate's successor for MVC apps · OpenFeign = declarative interface client · always set timeouts

**Model answer (~60 sec):**
> "Both make HTTP calls in request/response style. **RestTemplate** is the old client: **blocking**, each call holds a thread until the response arrives, and it's in **maintenance mode**. **WebClient** is the reactive client from WebFlux: **non-blocking I/O**, returns **`Mono`/`Flux`**, so a few threads can handle thousands of concurrent calls, and it's easy to fire several calls in parallel and combine them.
>
> Which one I pick depends on the app: in a reactive / WebFlux service, or when I fan out to many services in parallel, **WebClient**. In a normal blocking **Spring MVC** app I'd use **RestClient** (Spring 6.1), the modern fluent replacement for RestTemplate, or **OpenFeign** for a declarative interface. Using WebClient there and calling `.block()` everywhere gives you no benefit. Whatever the client, **always configure connect/read timeouts**."

```java
// RestTemplate (legacy, blocking)
Order o = restTemplate.getForObject("http://order-service/orders/{id}", Order.class, id);

// RestClient (Spring 6.1+, blocking, fluent)
Order o = restClient.get().uri("/orders/{id}", id).retrieve().body(Order.class);

// WebClient (non-blocking)
Mono<Order> o = webClient.get().uri("/orders/{id}", id).retrieve().bodyToMono(Order.class);

// OpenFeign (declarative)
@FeignClient(name = "order-service")
interface OrderClient { @GetMapping("/orders/{id}") Order get(@PathVariable Long id); }
```

**Traps:**
- WebClient is not "non-blocking synchronous": say "**request-response style with non-blocking I/O**". It becomes blocking only if you call `.block()`.
- **Never call `.block()` inside a reactive pipeline / event-loop thread**: same rule as never blocking the Vert.x event loop.
- RestTemplate's default request factory has **no timeouts (waits forever)** → a slow downstream exhausts your threads. Always set them.
- "Always WebClient" is too absolute; in a plain MVC app RestClient / Feign is simpler.

### TR17 — Async communication via message broker: Kafka, RabbitMQ — 🔴 Weak (two factual errors about Kafka: "queue size / waits when full" and consumer-group semantics)

**Keywords:** producer → broker → consumer, producer doesn't wait · **Kafka = distributed, partitioned, append-only log**; messages **retained** (by time/size), not deleted on read; consumers **pull** and track **offsets**; replay · **consumer group: each partition → exactly ONE consumer in the group** (load-balancing); **each group gets every message** (pub-sub) · **RabbitMQ = message broker**: exchanges (direct/topic/fanout) route to **queues**, broker **pushes**, message **deleted after ack** · ordering only within a partition

**Model answer (~90 sec):**
> "In async messaging, a **producer** sends a message to a **broker** and carries on; **consumers** process it later, so the services are decoupled in time.
>
> **Kafka** is a **distributed, append-only log**. A **topic** is split into **partitions** spread across brokers and replicated. Producers append to a partition (same **key** → same partition → **ordered per key**). Messages are **retained** for a configured time or size, not deleted when read; each consumer **pulls** and tracks its own **offset**, so you can **replay**. Consumers form **consumer groups**: inside one group each partition is read by **exactly one** consumer, which spreads the load, so parallelism is limited by the number of partitions; **different groups each get every message**, which gives you pub-sub. It's built for very high throughput and event streaming.
>
> **RabbitMQ** is a classic **message broker**: producers send to an **exchange** (direct, topic, fanout), which **routes** to **queues**; the broker **pushes** to consumers and **deletes the message after ack**. It's great for task queues, per-message routing, priorities and TTLs, at lower throughput.
>
> I use Kafka at work for data sync between services: we key by entity id to keep per-entity ordering."

| | Kafka | RabbitMQ |
|---|---|---|
| Model | distributed commit log | broker with exchanges → queues |
| Delivery | consumer **pulls**, tracks offset | broker **pushes** |
| After consumption | **retained** (replay possible) | **deleted** after ack |
| Ordering | per partition (per key) | per queue |
| Scaling | partitions + consumer groups | more queues / consumers |
| Strength | throughput, streaming, replay, event sourcing | flexible routing, priorities, task queues |

**Traps (the two errors):**
- ✗ "Kafka has a queue size; when full the producer waits": Kafka is a **log with retention**, not a bounded queue. Bounded length is a RabbitMQ / `BlockingQueue` concept. (Only the producer's local send buffer, `buffer.memory`, can fill up.)
- ✗ "All consumers in the group get the data": **within a group, each message goes to ONE consumer** (one partition → one consumer). To make every service get every event, give each service **its own group**.
- RabbitMQ's "topic" is an **exchange type** (routing by pattern), not the same thing as a Kafka topic.
- More consumers than partitions → the extra consumers sit **idle**.

### TR15 / TR9 — Service mesh & sidecar proxy — 🟡 Partial (right goal: "developers focus on code, not networking"; mechanism missing, "application layer" is wrong)

**Keywords:** dedicated **infrastructure layer** for service-to-service traffic · **sidecar proxy** (Envoy) next to every service instance, intercepts all in/out traffic transparently · **data plane** (proxies) + **control plane** (Istio istiod / Linkerd) pushes config · gives discovery + load balancing, retries/timeouts/circuit breaking, **mTLS** + authZ, metrics/tracing, traffic splitting (canary) · language-agnostic, no code change · cost: extra hop latency, CPU/memory per pod, ops complexity

**Model answer (~90 sec):**
> "A service mesh is a dedicated **infrastructure layer** that handles service-to-service communication, so that logic is **not inside the application code**.
>
> Every service instance gets a **sidecar proxy**, usually **Envoy**, in the same pod. All inbound and outbound traffic goes **through the sidecar transparently**: service A just calls `http://service-b`; A's sidecar finds healthy B instances, load-balances, applies **timeouts, retries and circuit breaking**, encrypts with **mTLS**, and B's sidecar checks the policy and hands the request to B. The sidecars are the **data plane**; a **control plane**, like **Istio's istiod** or **Linkerd**, pushes routing rules, policies and certificates to all of them.
>
> What you get: **resilience, mutual TLS and zero-trust security, uniform metrics and tracing, and traffic shifting for canary releases**, for every service in any language, without code changes.
>
> The cost is an **extra network hop per call**, CPU and memory for a proxy in every pod, and a complex control plane to run. So it pays off with **many services, multiple languages or strict security needs**; with a handful of Spring services, libraries like **Spring Cloud LoadBalancer + Resilience4j** inside the app are simpler."

```
 Pod A                            Pod B
[service A] → [sidecar A] ──mTLS──→ [sidecar B] → [service B]
                    ↑                      ↑
                 config, certs, routing rules
               [ control plane: istiod / Linkerd ]
```

**Traps:**
- It's an **infrastructure layer**, not "putting services in an application layer"; the whole point is to move networking **out** of the app.
- "Advanced service discovery" undersells it: discovery is only one feature. Lead with **resilience + mTLS + observability + traffic control**.
- Mesh vs library approach: Resilience4j / Spring Cloud do the same things **inside each JVM**; a mesh does it **outside**, for any language.
- Mention a cost; interviewers like hearing that you know it isn't free.

### MS15 — Kafka vs RabbitMQ — 🟡 Partial (retention/replay point is right and short; RabbitMQ described wrongly, "when to pick which" missing)

**Keywords:** Kafka = log, **retained** (time/size/compaction), pull, replay, partitions, huge throughput · RabbitMQ = broker, exchanges→queues, push, **deleted after ack**, durable queues keep messages for offline consumers, rich routing, priority, TTL, delayed/DLX · pick Kafka for event streaming / many independent consumers / replay; RabbitMQ for task queues / routing / per-message control

**Model answer (~60 sec):**
> "The core difference is **what happens after a message is consumed**. **RabbitMQ** is a broker: a message sits in a **queue** until a consumer **acks** it, then it's **deleted**. If a consumer is offline, a **durable queue** keeps its messages until it comes back, but a **new** consumer can't read history; there's **no replay**. **Kafka** is a **log**: messages are **retained** for a configured period (say 7 days) whether or not anyone read them, consumers **pull** and track their own **offset**, so a new consumer group can read from the beginning and anyone can **replay**.
>
> **I'd pick Kafka** for high-throughput **event streaming**: many independent services reacting to the same events, per-key ordering, replay, event sourcing or analytics pipelines. **I'd pick RabbitMQ** for **work/task queues** and **flexible routing**: per-message acks, priorities, TTL, delayed retries, request-reply, at moderate volume."

**Traps:**
- ✗ "RabbitMQ: an inactive consumer never gets the message": with a **durable queue + persistent messages**, it gets them when it reconnects. What it can't do is **replay** or serve **history to a new consumer**.
- RabbitMQ isn't "the pub/sub one": both support pub/sub (RabbitMQ via fanout/topic exchanges, Kafka via multiple consumer groups).
- Kafka doesn't keep messages "forever" by default: **retention** is configured (`retention.ms`, size), or **log compaction** keeps the latest value per key.
- The question asks **when to use which**: always finish with the use-case line.

### MS14 — SQS vs SNS — 🟢 Strong (short, core correct, good example; add the SNS → SQS fan-out and "SQS ≠ Kafka")

**Keywords:** SQS = **queue**, point-to-point, consumers **poll**, each message → **one** consumer, deleted after processing, visibility timeout, DLQ, Standard (at-least-once, best-effort order) vs FIFO (ordered per message group, dedup) · SNS = **pub/sub topic**, **pushes** to all subscribers (SQS, Lambda, HTTP, email, SMS, mobile push), no storage · **SNS → SQS fan-out** · GCP equivalent: Pub/Sub

**Model answer (~60 sec):**
> "Both are AWS managed messaging services. **SQS** is a **queue**: producers send messages, consumers **poll** them, each message is processed by **one** consumer and then deleted. While it's being processed it's hidden by a **visibility timeout**; if processing fails it reappears, and after N attempts goes to a **dead-letter queue**. Standard queues are at-least-once with best-effort order; **FIFO** queues keep order per message group.
>
> **SNS** is **pub/sub**: you publish to a **topic** and SNS **pushes** a copy to **every subscriber**: SQS queues, Lambda, HTTP endpoints, email, SMS, mobile push. It doesn't store messages for later.
>
> The common pattern is **fan-out: SNS → several SQS queues**. An 'OrderPlaced' event goes to one SNS topic; the inventory, billing and notification services each have their own SQS queue subscribed, so each gets a copy and processes it at its own pace, with retries and DLQ. On GCP, which I use, **Pub/Sub** covers both roles."

**Traps:**
- SQS is like **RabbitMQ** (a queue), **not like Kafka**: no retention for replay, no consumer offsets, message gone once processed.
- SNS alone has no buffering: a slow or down subscriber needs SQS behind it.
- Say "**fan-out**": that's the line interviewers wait for.

### TR10 — Service discovery — 🟡 Partial (the "why" is excellent; missing registration/heartbeats, client-side vs server-side, Kubernetes DNS)

**Keywords:** instances are dynamic (autoscaling, restarts, new IPs) → no hard-coded URLs · **registry** of healthy instances · **self-registration on startup + heartbeats**, evicted when they stop · **client-side discovery** (client gets list, caches it, picks one: Eureka + Spring Cloud LoadBalancer, `lb://`) vs **server-side discovery** (client calls one stable address: LB / API gateway / **Kubernetes Service** looks up and routes) · Kubernetes = built-in DNS discovery · Consul / Eureka / etcd

**Model answer (~90 sec):**
> "In microservices, instances come and go: autoscaling, restarts, new pods with new IPs. So a service can't hard-code another's address. **Service discovery** keeps a **registry** of the healthy instances of every service.
>
> Each instance **registers itself on startup** (name, IP, port) and sends **heartbeats**; if heartbeats stop, the registry **evicts** it. In Spring Cloud that's a **Eureka server** as the registry and the **Eureka client** inside every microservice.
>
> There are two ways to use it. **Client-side discovery**: the caller fetches the instance list from the registry, **caches** it, and picks an instance itself with a client-side load balancer: e.g. `@LoadBalanced WebClient` calling `http://inventory-service`, or a Feign client by name. **Server-side discovery**: the caller hits one stable address, a load balancer or gateway, and that component looks up the registry and routes.
>
> On **Kubernetes** discovery is built in: a **Service** gets a stable DNS name like `inventory-service.prod.svc.cluster.local` and load-balances across the healthy pods, so you usually don't need Eureka there. On our platform the registry is **Consul**, and our proxy routes requests by looking up the registry: server-side discovery."

**Traps:**
- The client does **not** ask the registry on **every** request: it **caches** the list and refreshes periodically (so calls keep working briefly even if the registry is down).
- Eureka **client** = the library in **each microservice** (registers itself + fetches the registry); Eureka **server** = the registry.
- Always name **client-side vs server-side** and **Kubernetes DNS**: GKE is your daily platform.
- Registering a load balancer's address instead of instances turns it into server-side discovery; say which one you mean.

### Deep dive — Service mesh, explained from scratch (revision aid for TR15 / TR9 / MS3)

**1. The problem.** Every service-to-service call needs the same "network homework": find a healthy instance, load-balance, timeout, retry, circuit-break, encrypt, check who's calling, emit metrics and a trace span. Without a mesh, every team puts this **inside its code** (Spring Cloud LoadBalancer + Resilience4j + TLS config + Micrometer). With 50 services in Java, Go and Node, that's three implementations, configured differently by each team, and changing a retry policy means redeploying the services.

**2. The idea.** Take that homework **out of the app** and give it to a separate proxy process that sits **next to every instance**, in the same pod: the **sidecar** (usually **Envoy**). The app just makes a plain HTTP call; the sidecar does everything else.
*Analogy:* every employee (service) gets a personal assistant (sidecar). The employee says "call inventory"; the assistant looks up the number, picks a free line, retries if busy, uses an encrypted line, notes the call in the log. Head office (control plane) gives every assistant the same rule book.

**3. One call, step by step (order → inventory):**
1. `order` code calls `http://inventory:8080/stock/42`: no retry, no TLS, no discovery code.
2. The pod's network rules (iptables) silently redirect the outgoing call to **order's Envoy** on localhost.
3. Order's Envoy already knows the **healthy inventory pods** (from the control plane), picks one (**load balancing**), applies the **timeout / retry / circuit-breaker** policy.
4. It opens an **mTLS** connection to **inventory's Envoy**: both sides prove their identity with certificates.
5. Inventory's Envoy checks the **policy** ("is order allowed to call inventory?") and forwards to the inventory app on localhost.
6. The response goes back the same way. Both Envoys record **metrics and trace spans**, so you get tracing for every service for free.
If inventory pod 3 keeps failing, order's Envoy **ejects** it from the load-balancing pool (outlier detection = circuit breaking) and retries on another pod. The order code never knows.

**4. Data plane vs control plane.**
- **Data plane** = all the sidecars. They handle **every request**.
- **Control plane** = **istiod** (Istio) / Linkerd's controller. It **never touches requests**. It configures the sidecars: routing rules, retry policies, certificates, access policies.
- Example: apply a rule "send 90% of traffic to inventory v1, 10% to v2" → istiod pushes it → every sidecar starts splitting → **canary release without code changes**.

**5. Mesh vs what you already know.**
| | Library (Resilience4j, Spring Cloud) | Service mesh (Istio / Linkerd) |
|---|---|---|
| Where the logic runs | inside each app (JVM) | in the sidecar next to each app |
| Languages | Java only | any language |
| Changing a policy | code/config change + redeploy each service | one rule in the control plane |
| mTLS between services | you build it | automatic |
| Cost | none extra | extra hop per call, a proxy's CPU/RAM per pod, complex control plane |

**Mesh vs a central proxy (like mks-proxy / an API gateway):** a gateway or central proxy is **one** proxy in front of many services, mainly for **north-south** traffic (client → system). A mesh is **one proxy per instance**, for **east-west** traffic (service → service).

**6. 30-second interview version:**
> "A service mesh moves service-to-service networking out of the application into a **sidecar proxy**, usually Envoy, deployed next to every instance. The app makes a plain call; the sidecar handles discovery, load balancing, timeouts, retries, circuit breaking, **mTLS** and metrics/tracing. A **control plane** like Istio's istiod configures all sidecars centrally, so policies and canary traffic splits change without code changes. The trade-off is an extra hop and a proxy per pod, so it's worth it with many services or languages, or strict security needs."

**7. Three terms that confuse people:**
- **Envoy**: just the **name of the proxy software** used as the sidecar (open source, CNCF; like Nginx, but built for service-to-service traffic). Istio puts one Envoy in every pod.
- **Control plane**: the **central brain** that tells every sidecar what to do. The sidecars need to know: *where are the healthy inventory pods? what is the retry/timeout rule? which certificate do I use? is order allowed to call inventory?* The control plane (Istio's **istiod**) watches Kubernetes for pods coming and going, reads the rules the team writes (YAML), **issues certificates**, and **pushes all of this to every sidecar**. It **never carries a request**. Air-traffic analogy: planes (sidecars) carry passengers (requests); the control tower (control plane) only gives instructions.
- **mTLS (mutual TLS)**: normal TLS / HTTPS = only the **server** shows a certificate (your browser checks the bank is really the bank), then traffic is encrypted. **Mutual** TLS = **both sides** show certificates and verify each other, so inventory knows the caller **really is** the order service, order knows it's **really** talking to inventory, and everything is encrypted. In a mesh the control plane gives each service a certificate (its identity) and rotates it automatically; the sidecars do the handshake, the apps do nothing. This is "**zero trust**": don't assume the internal network is safe just because it's internal.

**8. Is TLS needed for internal service-to-service calls?**
TLS (Transport Layer Security, the protocol under HTTPS) gives **encryption** (nobody can read), **integrity** (nobody can modify) and **authentication** (certificate proves who the server is). It's about **how much you trust the network**, not "external vs internal provider". Kafka and Redis connections also have TLS as an **option** you switch on.
- **Edge-only TLS (perimeter model):** TLS from client to load balancer/gateway, **terminated** there; plain HTTP inside the cluster. Simpler and slightly faster, but anyone who gets inside the network can read or fake traffic.
- **TLS/mTLS everywhere (zero trust):** every hop encrypted and authenticated. Expected in regulated domains: PCI DSS requires strong encryption for card data over open/public networks, and payment companies typically encrypt internally too. The pain is managing certificates for every service, which is **exactly what a service mesh automates**.
Interview line: *"Inside a cluster it isn't technically required, but with zero trust and compliance like PCI DSS we encrypt internal traffic too, ideally with mTLS through a mesh so apps don't manage certificates."*

**9. What is a certificate (in the mesh)?** A **digital ID card for a service**: it says "I am inventory-service", carries a key, and is **stamped (signed) by a trusted authority** (a Certificate Authority, like a passport office). A sidecar checks the other side's ID card and the stamp before trusting it; without this, any pod could pretend to be inventory. In a mesh, the **control plane is the passport office**: every sidecar gets an ID card for its service automatically and it is renewed automatically. Both sides showing their ID cards = the "mutual" in mTLS.

### MS3 — How do you make a call from one service to another? (Service mesh / sidecar) — 🟡 Partial (mesh flow correct and much clearer; cut the business story, add where the pod list comes from + failure handling)

**Keywords:** app calls by **service name** (plain HTTP/gRPC) · **without mesh:** client (RestClient / Feign / WebClient) + discovery + client-side LB + Resilience4j in the app · **with mesh:** call redirected to own sidecar → sidecar has healthy-pod list from control plane → picks a pod (LB) → **mTLS** sidecar-to-sidecar → receiver checks policy → app · timeouts / retries / outlier ejection · metrics + traces for free

**Model answer (~75 sec):**
> "The order code just calls `http://inventory-service/stock/42`, by **service name**, never an IP.
>
> **Without a mesh**, the app does the work: a client like **RestClient or Feign**, **service discovery** (Eureka / Consul / Kubernetes DNS) to find inventory's instances, a **client-side load balancer** to pick one, and **Resilience4j** for timeout, retry and circuit breaker.
>
> **With a mesh**, the app does nothing extra. The call is intercepted by **order's sidecar** (Envoy). The **control plane** has already given that sidecar the list of **healthy inventory pods**, so it **picks one** (load balancing), applies the **timeout / retry / circuit-breaker** policy, and opens an **mTLS** connection to **inventory's sidecar**: both sides verify each other's certificate and the traffic is encrypted. Inventory's sidecar checks the policy, '**is order allowed to call me?**', and passes the request to the inventory app on localhost. Both sidecars record **metrics and traces**.
>
> If one inventory pod starts failing, order's sidecar **retries on another pod and ejects the bad one**, and the order code never notices."

**Traps:**
- Don't narrate the business flow (inventory check → payment); the question is about the **mechanism of the call**.
- Say **where the pod list comes from** (control plane / discovery) and **what happens on failure**: that's what separates a good answer.
- The bank's question mentions mesh / sidecar, but showing the **without-mesh** way in one sentence proves you understand what the mesh replaces.

### MS1 — API Gateway vs load balancer vs reverse proxy — 🟡 Partial (gateway as single entry point + routing, and LB algorithms are right; reverse proxy not covered, gateway's cross-cutting features missing)

**Keywords:** **reverse proxy** = general concept: sits in front of servers, clients talk to it, it forwards + hides the servers (TLS termination, caching, compression; Nginx, Envoy) · **load balancer** = reverse proxy focused on **spreading traffic across identical instances**: L4 (TCP) or L7 (HTTP), health checks, round robin / least connections / IP hash · **API gateway** = reverse proxy specialised for **APIs**: single entry point, **routes by path/header to different services** + **cross-cutting concerns**: authentication (JWT), rate limiting, TLS termination, CORS, request/response transformation, aggregation (BFF), logging/tracing · layered in practice: client → LB → gateway instances → services · Spring Cloud Gateway, Kong, AWS API Gateway, Apigee

**Model answer (~90 sec):**
> "All three sit between the client and the backend, so think of them as layers of the same idea.
>
> A **reverse proxy** is the general concept: clients talk to the proxy, it forwards to the real servers and **hides** them; it often does **TLS termination**, caching and compression. Nginx and Envoy are examples. On my platform, **mks-proxy** is a reverse proxy: it terminates TLS and routes by headers and by looking up the service registry.
>
> A **load balancer** is a reverse proxy whose job is to **spread traffic across identical instances** of one service, at **L4** (TCP) or **L7** (HTTP), with **health checks** and algorithms like round robin, least connections or IP hash.
>
> An **API gateway** is a reverse proxy specialised for **APIs**: it's the **single entry point** for clients, **routes** `/orders/**` to the order service and `/payments/**` to the payment service, and applies **cross-cutting concerns** once instead of in every service: **authentication** (validate the JWT), **rate limiting**, CORS, request transformation, aggregating several calls for one client, logging and tracing. In Spring that's **Spring Cloud Gateway**.
>
> In production they're layered: **client → cloud load balancer → several gateway instances → services**, and the gateway also load-balances across a service's pods."

| | Reverse proxy | Load balancer | API gateway |
|---|---|---|---|
| Main job | forward + hide backend servers | spread load across identical instances | single API entry point + policies |
| Routing | basic | to instances of **one** service | to **different services** by path/header |
| Extras | TLS termination, caching, compression | health checks, L4/L7, algorithms | auth/JWT, rate limiting, CORS, transformation, aggregation, monitoring |
| Examples | Nginx, Envoy, mks-proxy | GCP / AWS LB, HAProxy, K8s Service | Spring Cloud Gateway, Kong, Apigee, AWS API Gateway |

**Traps:**
- The question has **three** parts: don't skip the **reverse proxy**. It's the parent concept of the other two.
- The gateway's value isn't only "one URL": it's the **cross-cutting concerns** (auth, rate limiting). Lead with them.
- "Combine gateway and LB into one service": partly true (gateways load-balance across a service's pods), but the gateway itself runs as several instances **behind** a load balancer, or it becomes a single point of failure.
- Keep the intro short; skip the long booking/product example.

### MS11 — Reverse proxy vs forward proxy — 🔴 Weak (reverse proxy features right; forward proxy not covered, "hides the user from the backend" is the wrong direction)

**Keywords:** **forward proxy = in front of CLIENTS**, acts for clients going **out**; server sees the proxy, not the client; client is configured to use it (`HTTP_PROXY`); filtering, egress control, logging, caching, anonymity (Squid, corporate proxy) · **reverse proxy = in front of SERVERS**, acts for servers receiving **in**; client sees the proxy, not the servers; client doesn't know it exists; LB, TLS termination, caching, routing, WAF / DDoS protection (Nginx, Envoy, mks-proxy) · forward hides the client, reverse hides the servers

**Model answer (~45 sec):**
> "Both sit in the middle; the difference is **whom they represent**.
>
> A **forward proxy** sits in front of **clients** and makes **outgoing** requests on their behalf. The website sees the **proxy's** IP, not the client's. The client has to be **configured** to use it. Companies use it to **control and filter outbound traffic**, log it, cache it, or hide the client: a corporate proxy, Squid.
>
> A **reverse proxy** sits in front of **servers** and receives **incoming** requests on their behalf. The client only sees the proxy and **doesn't even know** the servers behind it. It does **load balancing, TLS termination, caching, routing and protection**: Nginx, Envoy, or our mks-proxy.
>
> One line: **a forward proxy hides the client from the server; a reverse proxy hides the servers from the client.**"

| | Forward proxy | Reverse proxy |
|---|---|---|
| Sits in front of | clients | servers |
| Traffic direction | outgoing (client → internet) | incoming (internet → servers) |
| Who is hidden | the client | the servers |
| Who knows about it | client configures it | client doesn't know it exists |
| Used for | filtering, egress control, logging, anonymity | LB, TLS termination, caching, routing, WAF |
| Examples | Squid, corporate proxy | Nginx, Envoy, mks-proxy |

**Traps:**
- The question is a **comparison**: always cover **both**.
- A reverse proxy hides the **servers**, not "the user's identity from the backend": hiding the client is what a **forward** proxy does.
- Building one page from several backends (your Amazon mobile / laptop / fridge example) is **aggregation**: an API gateway / BFF feature, not the definition of a reverse proxy.

## Monday — Resilience

### MS4 / TR5 — Circuit breaker: states, annotations, fallback — 🟡 Partial (the three states and the threshold logic are well explained; fallback strategy missing, annotation's `name` is not the URL)

**Keywords:** protects the **caller** from a failing dependency · counts failures **and slow calls** in a **sliding window** (count- or time-based) · **CLOSED** (normal, counting) → failure rate ≥ threshold → **OPEN** (**fail fast**, no calls, fallback runs) → after wait duration → **HALF_OPEN** (a few trial calls) → success → CLOSED / failure → OPEN · stops threads waiting on timeouts → prevents **cascading failure**, gives the downstream time to recover · `@CircuitBreaker(name = "inventory", fallbackMethod = "...")` · **fallback**: cached data, default value, degraded response, queue for later, clear error

**Model answer (~90 sec):**
> "A circuit breaker protects a service from a **failing dependency**. Without it, if inventory is down, every order request waits for a timeout, threads pile up, and order goes down too: a **cascading failure**.
>
> It has three states. **Closed**: calls go through and the breaker **records failures and slow calls** in a **sliding window**, say the last 10 calls or last 10 seconds. If the **failure rate crosses a threshold**, say 50%, it goes **Open**: now calls **fail immediately** without touching inventory, so no threads are wasted and inventory gets time to recover. After a **wait duration**, say 30 seconds, it goes **Half-open** and lets **a few trial calls** through: if they succeed it goes back to **Closed**, if they fail it goes back to **Open**.
>
> In Spring Boot I use **Resilience4j**: `@CircuitBreaker(name = "inventory", fallbackMethod = "stockFallback")`. The `name` points to a config block in `application.yml` with the threshold, window and wait time.
>
> The **fallback** decides what the user gets while the circuit is open: a **cached** value, a **default** ('availability unknown'), a **degraded** response, or putting the request on a **queue** to process later, instead of an error page."

```java
@CircuitBreaker(name = "inventory", fallbackMethod = "stockFallback")
public Stock getStock(Long productId) {
    return inventoryClient.getStock(productId);            // remote call
}

// same params + the exception; same return type
private Stock stockFallback(Long productId, Throwable ex) {
    return stockCache.getOrDefault(productId, Stock.unknown(productId));   // cached / default
}
```
```yaml
resilience4j.circuitbreaker.instances.inventory:
  slidingWindowType: COUNT_BASED
  slidingWindowSize: 10
  minimumNumberOfCalls: 5
  failureRateThreshold: 50              # % failures to open
  slowCallDurationThreshold: 2s
  slowCallRateThreshold: 50             # slow calls count too
  waitDurationInOpenState: 30s
  permittedNumberOfCallsInHalfOpenState: 3
  automaticTransitionFromOpenToHalfOpenEnabled: true
```

**Traps:**
- `name` is the **breaker instance name** in config, **not** the URL or the target service.
- The question asks for the **fallback strategy**: always say what the fallback returns.
- In **Open** the call **fails fast** (`CallNotPermittedException`) and the fallback runs; that's the whole benefit.
- **Slow calls** can open the circuit too, not only exceptions.
- Keep numbers consistent (don't say "50% threshold … 30% failing").

### TR6 — Resilience4j: the five annotations, config, fallback — 🟡 Partial (circuit-breaker config keys and fallback purpose recalled very well; the other four annotations not covered)

**Keywords:** `@CircuitBreaker` (stop calling a failing dependency) · `@Retry` (re-try transient failures, backoff + jitter, idempotent only) · `@TimeLimiter` (max time for a call; async / `CompletableFuture`) · `@Bulkhead` (cap concurrent calls to one dependency: semaphore or thread pool) · `@RateLimiter` (max calls per period: protect a downstream / partner quota) · config per instance in `application.yml` · shared `fallbackMethod` · needs `resilience4j-spring-boot3` + **`spring-boot-starter-aop`** · default order: **Retry( CircuitBreaker( RateLimiter( TimeLimiter( Bulkhead( call )))))**

**Model answer (~90 sec):**
> "Resilience4j gives five annotations, each configured per instance in `application.yml`, and they can share a fallback method.
> - **`@CircuitBreaker`**: stops calling a dependency that keeps failing and fails fast (MS4).
> - **`@Retry`**: retries **transient** failures (timeout, 503, connection reset) a few times with **exponential backoff and jitter**, only for **idempotent** operations.
> - **`@TimeLimiter`**: puts a **maximum time** on a call so a slow dependency can't hold the caller; used with async return types like `CompletableFuture`.
> - **`@Bulkhead`**: limits **how many concurrent calls** go to one dependency, so a slow inventory service can't use up all the threads and break unrelated features, like compartments in a ship. Semaphore-based or thread-pool-based.
> - **`@RateLimiter`**: limits **calls per time period**, e.g. to stay inside a partner API's quota of 100 requests per second.
>
> Combined, the default order is **Retry outside, then CircuitBreaker, RateLimiter, TimeLimiter, Bulkhead closest to the call**, so every retry attempt still goes through the circuit breaker."

```java
@Retry(name = "inventory")
@CircuitBreaker(name = "inventory", fallbackMethod = "stockFallback")
@Bulkhead(name = "inventory")
public Stock getStock(Long id) { return inventoryClient.getStock(id); }
```
```yaml
resilience4j:
  retry.instances.inventory:
    maxAttempts: 3
    waitDuration: 200ms
    enableExponentialBackoff: true
    exponentialBackoffMultiplier: 2
    enableRandomizedWait: true            # jitter
    retryExceptions: [java.io.IOException, java.util.concurrent.TimeoutException]
  timelimiter.instances.inventory:
    timeoutDuration: 2s
  bulkhead.instances.inventory:
    maxConcurrentCalls: 20
    maxWaitDuration: 0
  ratelimiter.instances.partnerApi:
    limitForPeriod: 100
    limitRefreshPeriod: 1s
    timeoutDuration: 0
```

**Traps:**
- The annotations only work through Spring AOP proxies: add **`spring-boot-starter-aop`**, and **self-invocation doesn't trigger them** (same rule as `@Transactional`, `@Async`).
- **Retry only idempotent operations**, or retry with an idempotency key; never blindly retry a payment POST.
- Retry + circuit breaker together: retries must stop when the circuit is open (`CallNotPermittedException` should be in `ignoreExceptions` for retry).
- `@RateLimiter` limits **your outgoing** calls (or your endpoint), it's not the same as a gateway rate limiter for incoming users.

### TR7 — Retry: exponential backoff and jitter — 🟡 Partial (backoff explained well; missed WHY jitter exists — thundering herd — and which failures to retry)

**Keywords:** retry only **transient** failures · **fixed** delay hammers a struggling service · **exponential backoff**: delay doubles (100 → 200 → 400 ms…) with a **max delay** and **max attempts** · **jitter** = random part of the delay → stops many clients retrying **at the same instant** (**thundering herd / retry storm**) · full jitter: `sleep = random(0, min(cap, base × 2^attempt))` · retry: timeouts, connection reset, 502/503/504, 429 (honour `Retry-After`) · don't retry: 400/401/403/404/409/422, business errors · only **idempotent** ops (or idempotency key) · retry budget < caller's timeout · retries at several layers multiply · combine with circuit breaker

**Model answer (~75 sec):**
> "Retries handle **transient** failures: a timeout, a dropped connection, a 503 while a pod restarts. If you retry with a **fixed** short delay, you hammer a service that is already struggling.
>
> **Exponential backoff** doubles the wait each attempt: 100 ms, 200, 400, 800, with a **cap** on the delay and a **max number of attempts**, so a recovering service gets breathing room.
>
> **Jitter** adds randomness to each wait. Why: if inventory fails for a second, **thousands of clients fail at the same moment**, and with pure backoff they all retry at exactly 100 ms, 200 ms, 400 ms: synchronized spikes that knock it down again, the **thundering herd**. Jitter spreads the retries out. The common form is **full jitter**: wait a random time between 0 and the backoff value.
>
> **What to retry:** timeouts, connection errors, **502/503/504**, and **429** respecting `Retry-After`. **Never** retry client or business errors (400, 401, 403, 404, 409, 422), they'll fail again. And only retry **idempotent** operations: GET, PUT, DELETE, or a POST that carries an **idempotency key**; otherwise a retried payment charges twice.
>
> I keep the total retry time **inside the caller's timeout**, and put a **circuit breaker** around it so we stop retrying once the dependency is clearly down."

**Traps:**
- The point of jitter is **de-synchronising many clients**, not just "adding randomness": say **thundering herd**.
- "Backoff", not "backup".
- **Retry storms**: gateway retries 3× → service A 3× → service B 3× = **27 calls** to B for one user request. Retry at **one layer** only.
- Retrying a non-idempotent POST without an idempotency key = duplicate orders or payments.

### DS2 — A slow downstream service is affecting the whole application: prevent cascading failure — 🟡 Partial (right toolbox and good fallback-with-cache idea; didn't explain WHY slowness cascades or apply each tool to the scenario)

**Keywords:** slow is worse than down · caller threads / connections **block waiting** → **pool exhausted** → caller can't serve **any** request → its callers time out → chain reaction · defenses in order: **timeouts** → **bulkhead** (isolate the slow dependency's threads) → **circuit breaker incl. slow-call threshold** (fail fast) → **fallback** (cache / default / degraded) → careful **retries** · async/queue instead of sync where possible · load shedding / rate limiting at the edge · monitor p99 latency, alerts · extra instances help crashes, not shared-cause slowness

**Model answer (~2 min):**
> "A **slow** dependency is actually more dangerous than a dead one. Say inventory starts taking 20 seconds. Every order request that calls it **holds a thread and a connection** while waiting. Within seconds order's **thread pool is exhausted**, so order can't serve *any* request, even ones that don't need inventory. Then everything calling order times out too. That's the cascade.
>
> I'd defend in layers:
> 1. **Timeouts** first: every remote call gets a tight timeout, below my own SLA. Without timeouts nothing else helps.
> 2. **Bulkhead**: cap concurrent calls to inventory (or give it its own small thread pool), so even if inventory hangs, only that slice of capacity is stuck and the rest of order keeps working.
> 3. **Circuit breaker** with a **slow-call threshold**: if, say, 50% of calls exceed 2 s, open the circuit and **fail fast** instead of waiting.
> 4. **Fallback**: serve cached stock, a default, or a degraded response ('availability confirmed at checkout').
> 5. **Retries** only a little, with backoff and jitter; retrying a slow service just adds load.
>
> Structurally, where the user doesn't need the answer immediately I'd make the call **asynchronous** through a queue, and at the edge use **rate limiting / load shedding**. And I'd **alert on p99 latency**, so we see the slowdown before it cascades."

**Traps:**
- Explain the **mechanism** (thread / connection-pool exhaustion); listing Resilience4j annotations alone doesn't answer "how".
- **Timeouts come first**: circuit breakers count slow calls only if calls actually end.
- "Add a backup instance": redundancy helps if one pod crashes, but if the slowness comes from a shared DB, a bad release or a hot partition, **all** instances are slow.
- Rate limiter protects against **overload**; it's not the main cascading-failure tool.

### DS4 — 1-second SLA, but a downstream call has a 2-second timeout — 🟡 Partial (key insight right: downstream timeout must be well below the SLA with headroom; the retry plan — 50 ms timeout, retry every 50 ms, backoff — doesn't fit)

**Keywords:** SLA = promise to callers (here: respond within 1 s, usually p99) · downstream = services you call · **timeout > SLA → SLA broken whenever downstream is slow** · client gives up at 1 s but server keeps waiting → **wasted threads, work nobody waits for** · client retries while original still running → duplicate work · **latency budget**: your work + every downstream call (sequential calls add up) + retries ≤ SLA · timeout from downstream's **p99**, not a guess · ≤ 1 fast retry only if budget remains · **deadline propagation** · parallel calls · circuit breaker + fallback · async (202) / cache · downstream's SLO must support yours

**Model answer (~90 sec):**
> "The problem is that the **downstream timeout is longer than my own SLA**. Whenever inventory is slow, say 1.8 s, I **break my 1-second promise** for sure. Worse, my client gives up at 1 s, but my service **keeps a thread waiting** for up to 2 s for an answer nobody wants, and the client may **retry**, so now the same work runs twice.
>
> I'd design with a **latency budget**: 1 s total minus my own processing and network overhead, say 150 ms, leaves about 850 ms for downstream calls. If I call two services **one after another**, they share that budget, so I'd call them **in parallel** where possible. Each **timeout comes from the dependency's real p99**: if inventory's p99 is 200 ms, a 300–400 ms timeout is sensible. A **retry** only if there's budget left for it, at most one, with jitter. Behind that a **circuit breaker** and a **fallback**, e.g. cached stock or 'availability confirmed at checkout'.
>
> Two more things: **propagate the deadline** downstream (pass the remaining time in a header, like gRPC deadlines) so inventory stops work once I've given up; and if inventory can't meet my budget, either make that step **asynchronous** (return 202 and finish later) or agree a tighter **SLO** with that team."

**Traps:**
- A **50 ms timeout** is usually *below* normal latency → most calls time out → retries **add** load. Base timeouts on measured **p99**.
- Exponential backoff inside a 1-second budget barely fits: the **retries must fit inside the budget**.
- Name the hidden costs: **wasted threads** on abandoned requests and **duplicate work** from client retries.
- Sequential downstream calls **add up**: 3 calls × 400 ms = 1.2 s > SLA.

## Tuesday — Observability & performance

### MS5 — Tracing and handling failures across microservices (Zipkin, correlation ID) — 🟡 Partial (purpose and the "one ID follows the request" idea are right; trace vs span missing, tools mixed up, failure-handling part thin)

**Keywords:** one request crosses many services → need the whole path · **trace ID** (one per request) + **span ID** per hop/operation (+ parent span ID) · propagated in headers (**W3C `traceparent`**; older custom `X-Correlation-ID`), also through **Kafka headers** · **Micrometer Tracing** (Spring Boot 3 facade, replaced Sleuth) → bridge to **OpenTelemetry** (or Brave) → export spans to a backend: **Zipkin / Jaeger / Grafana Tempo** → view in UI (Zipkin UI, Grafana) · trace ID in every **log line via MDC** · **3 pillars: logs, metrics, traces** · sampling in prod · failures: error span + logs by trace ID + alerts + return trace ID to the client

**Model answer (~2 min):**
> "With one request crossing many services, I need to see its **whole path**: which services it touched, how long each took, and where it failed. That's **distributed tracing**.
>
> When a request enters (gateway or first service), it gets a **trace ID**. Each unit of work, a call into a service, a DB query, an outgoing HTTP call, is a **span** with its own span ID, its parent's ID, a start time and a duration. The IDs travel to the next service in a header, the W3C **`traceparent`** header, and through **Kafka message headers** for async hops. Older systems do the same thing by hand with an **`X-Correlation-ID`** header.
>
> In Spring Boot 3, **Micrometer Tracing** with the **OpenTelemetry bridge** does this automatically for RestClient, WebClient, Feign and Kafka. Spans are exported to a backend like **Zipkin, Jaeger or Grafana Tempo**, and I open the trace by ID to see a timeline (a waterfall) of every hop, with the **slow or failed span highlighted**.
>
> Micrometer also puts **`traceId` and `spanId` into the logging MDC**, so every log line carries the trace ID and in Grafana / Kibana I can jump from a slow span to the exact logs. For **handling** failures: the failing span records the exception, **alerts** fire on error rate and p99 latency, and I return the **trace ID in the error response** so support can find the request instantly. In production I **sample** a percentage of traces to control cost."

```yaml
# pom: spring-boot-starter-actuator, micrometer-tracing-bridge-otel, opentelemetry-exporter-otlp (or zipkin)
management:
  tracing.sampling.probability: 0.1          # 1.0 in dev
logging.pattern.level: "%5p [${spring.application.name},%X{traceId},%X{spanId}]"
```

**Traps:**
- **Trace ID ≠ span ID**: one trace per request, many spans inside it.
- OpenTelemetry isn't "a tool inside Micrometer": it's a **vendor-neutral standard + SDK**; Micrometer Tracing **bridges** to it.
- Traces are **timed spans**, not logs. Request counts per second are **metrics** (Micrometer + Prometheus → Grafana): a different pillar.
- Grafana is the **UI**; the trace storage behind it is usually **Tempo** (or Jaeger / Zipkin). Zipkin isn't obsolete, just one backend option.

### TR13 — MDC: how the trace / correlation ID gets into every log line — 🔴 Weak (name wrong, and MDC was confused with header propagation)

**Keywords:** **Mapped Diagnostic Context** · feature of **SLF4J / Logback / Log4j2**, not a separate library · **per-thread key-value map (ThreadLocal)** · log pattern `%X{traceId}` prints it on **every** log line automatically · MDC is **logging inside one service only**; passing the ID to the next service is done by the **HTTP / Kafka header** (`traceparent`, `X-Correlation-ID`) · filter: read header or create ID → `MDC.put` → `finally MDC.remove` · thread pools reuse threads → must clear · lost on `@Async` / CompletableFuture / reactive unless propagated (TaskDecorator, Micrometer context-propagation) · Micrometer Tracing does all this automatically

**Model answer (~60 sec):**
> "MDC, **Mapped Diagnostic Context**, is a feature of the logging framework, SLF4J with Logback or Log4j2. It's a **per-thread key-value map**: I put `traceId` into it at the start of a request, and the log pattern `%X{traceId}` prints it on **every log line** that thread writes, without passing it to each log call.
>
> MDC itself only works **inside one service**. To carry the ID to the next service it goes in a **header**, `traceparent` or `X-Correlation-ID`, and the next service's filter reads that header and puts it into **its** MDC. Then in Splunk or Kibana I search one ID and get the logs from all services for that request.
>
> Two gotchas: MDC is a **ThreadLocal**, so I must **clear it in `finally`** because thread pools reuse threads; and it **doesn't follow** `@Async` or `CompletableFuture` threads unless I copy it with a `TaskDecorator` or context propagation. In Spring Boot 3, **Micrometer Tracing does all of this automatically**."

```java
@Component
public class CorrelationIdFilter extends OncePerRequestFilter {
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String id = Optional.ofNullable(req.getHeader("X-Correlation-ID"))
                            .orElse(UUID.randomUUID().toString());
        MDC.put("correlationId", id);
        res.setHeader("X-Correlation-ID", id);          // return it to the client
        try { chain.doFilter(req, res); }
        finally { MDC.remove("correlationId"); }       // thread is reused by the pool
    }
}
// logback pattern: %d %5p [%X{correlationId}] %logger - %msg%n
// outgoing calls: a RestClient / Feign interceptor copies MDC.get("correlationId") into the header
```

**Traps:**
- **Mapped**, not "Mem".
- MDC does **not** put anything in a request header: header = between services, MDC = within one service's logs.
- Key is a **name** (`traceId`, `correlationId`), the value is the ID.
- Forgetting `MDC.remove` → the **next request on that pooled thread logs the old ID** (same leak you demoed with ThreadLocal in MT2).

### TR14 — Distributed (centralised) logging: Splunk, Datadog — 🟡 Partial (the problem is explained well; mechanism missing, "distributed lock" and "sidecar tracing" are wrong terms here)

**Keywords:** logs scattered across many services and pods · **pods are ephemeral** (restart / scale-down → local logs gone) · can't SSH into 50 pods · apps log to **stdout** in **structured JSON** (timestamp, level, service, **traceId** from MDC) · **log agent / shipper** collects them (Fluent Bit / Fluentd as a K8s **DaemonSet**, Splunk Forwarder, Datadog Agent; sometimes a sidecar) · central indexed store + UI: **Splunk, ELK (Elasticsearch-Logstash-Kibana), Datadog, Grafana Loki, GCP Cloud Logging** · search by traceId, filter by service / level / time, dashboards, **alerts** on error spikes · cost control: log levels, sampling, retention · **never log secrets / PII / card numbers** (mask PANs, PCI DSS)

**Model answer (~75 sec):**
> "In microservices one request writes logs in many services and many pods, and **pods are short-lived**: when one restarts or scales down, its local logs are gone. Nobody can SSH into fifty pods and grep. So logs are **centralised**.
>
> Each service logs to **stdout** in **structured JSON**: timestamp, level, service name, and the **traceId** from MDC. A **log agent** on every node, like **Fluent Bit** running as a Kubernetes DaemonSet, or the Splunk forwarder or Datadog agent, picks those logs up and ships them to a **central, indexed store**: **Splunk, Datadog, the ELK stack, or GCP Cloud Logging** on GKE.
>
> There I can **search by traceId** to see one request across all services, filter by service, level and time, build dashboards, and set **alerts**, e.g. when ERROR logs for the payment service spike.
>
> Two rules: control cost with log levels and **retention**, and **never log secrets or card data**; in payments, card numbers must be masked for PCI DSS."

**Traps:**
- "Distributed **lock**" is a different topic (DS5: coordinating work across instances); here you mean a **log agent / shipper**.
- The agent **ships logs**, it doesn't **trace**: tracing is MS5. Logs, metrics, traces = three pillars, linked by **traceId**.
- Tools mostly **search, dashboard and alert**: don't claim they "identify the bug automatically".
- Plain text logs are hard to query: say **structured / JSON logging**.

### DS1 — A request flows through 7 microservices: trace it end-to-end in production — 🟡 Partial (right building blocks + real detail "we use Splunk"; MDC/header mix-up repeated, timing attributed to logs, no investigation steps)

**Keywords:** **setup**: trace ID created at the **entry** (gateway / first service) → propagated in **headers** (`traceparent`) and **Kafka headers** → **every** service instrumented (Micrometer Tracing / OpenTelemetry), one missing service breaks the chain → **spans** (timing per hop) exported to Tempo / Jaeger / Zipkin → traceId in every log via **MDC** → logs centralised (Splunk) → metrics (Prometheus / Grafana) · **investigation**: get the traceId (error response, alert, log) → open the **trace waterfall** → find the **slow / red span** → jump to that service's **logs by traceId** → check its **metrics** (CPU, GC, pool, DB) · sampling: always keep error / slow traces

**Model answer (~2 min):**
> "I'd split it into **setup** and **investigation**.
>
> **Setup.** The **gateway or first service creates a trace ID**, and every hop passes it on in the **`traceparent` header**, including **async hops through Kafka message headers**. **Every one of the seven services** must be instrumented, with **Micrometer Tracing / OpenTelemetry**; if one service drops the header, the trace breaks there. Each service records **spans**, the timing of its own work and its outgoing calls, and exports them to a tracing backend like **Grafana Tempo or Jaeger**. The trace ID also goes into **every log line through MDC**, and logs are shipped to **Splunk**, which is what we use on my project. Metrics go to Prometheus / Grafana.
>
> **Investigation.** I get the **trace ID**: from the error response, an alert, or a log line. In Grafana I open the trace and see a **waterfall of all seven services**: which call was **slow** and which **failed** is obvious, say service 5 spent 3 seconds in a DB query. Then I search **Splunk by the same trace ID** to read service 5's logs for that request, and check **service 5's metrics**: CPU, GC pauses, connection pool, DB latency.
>
> In production we **sample** traces to save cost, but configure it to **always keep errors and slow requests**, so the ones we need to debug are never dropped."

**Traps:**
- **MDC doesn't create the ID or put it in the header** (same mistake as TR13): the **gateway / tracing library creates it**, the **header carries it between services**, **MDC only puts it into that service's logs**.
- **Timing per service comes from spans (traces), not from logs.**
- Skip authorization and the business flow: the question is about **observability**.
- Mention **async hops (Kafka)** and **"every service must propagate"**: that's what makes it end-to-end.

### DS7 — Memory leak in production, fine locally: how to troubleshoot — 🔴 Weak → 🟡 Partial on 2nd try (profiler + "map holding unused values" are right; "unreferenced objects cause leaks" is the opposite of true, no step-by-step method, profilers don't clean up)

**Keywords:** leak = objects **still referenced** (reachable from a GC root) but no longer needed; unreferenced objects are collected by GC · why prod only: real **volume and duration**, real data variety (many unique keys), real concurrency + thread pools, different heap / JVM config, real dependencies · **confirm**: heap after each full GC keeps **rising** (rising floor of the sawtooth), GC time up, metrics `jvm.memory.used`, GC logs · **capture**: `-XX:+HeapDumpOnOutOfMemoryError`, `jcmd <pid> GC.heap_dump`, **two dumps hours apart**, take the pod out of the LB first · **analyse**: Eclipse MAT (**leak suspects**, **dominator tree**, retained size, **path to GC roots**), JProfiler, VisualVM; **JFR** is low-overhead and prod-safe · container OOMKilled with normal heap → **off-heap**: direct buffers, metaspace, thread stacks → Native Memory Tracking · **usual causes**: unbounded caches / static maps, ThreadLocal not removed, listeners not unregistered, unclosed streams / connections, huge Hibernate session in batch jobs · reproduce with a load test in staging → fix → verify

**Model answer (~2 min):**
> "A leak in Java means objects that are **still referenced**, so GC can't free them, but that we'll never use again. It shows up only in production because only production has the **volume, the uptime and the variety of data**: locally I run for ten minutes with a handful of keys.
>
> 1. **Confirm it's a leak**, not just high load: in Grafana, the **heap after each full GC keeps rising** and GC time grows. If the pod is **OOMKilled while the heap looks normal**, it's **off-heap** (direct buffers, metaspace, thread stacks) and I'd turn on Native Memory Tracking.
> 2. **Capture evidence**: the JVM runs with `-XX:+HeapDumpOnOutOfMemoryError`; I also take a heap dump with `jcmd` from one pod (after taking it out of the load balancer, since a dump pauses the JVM), and a **second dump a few hours later** to compare. For live data, **Java Flight Recorder** has very low overhead.
> 3. **Analyse** in **Eclipse MAT** or JProfiler: the **leak suspects** report and **dominator tree** show which objects hold the most **retained memory**, and **path to GC roots** shows *who* is holding them: e.g. a static `HashMap` used as a cache with no eviction.
> 4. **Fix and verify**: typical fixes are a **bounded cache with eviction** (Caffeine with max size / TTL), `ThreadLocal.remove()` in `finally`, closing resources with try-with-resources, clearing the Hibernate session in batch jobs. Then a **load test in staging** to prove the heap stays flat."

**Traps:**
- **Unreferenced objects do NOT leak**: GC frees them. A leak is **referenced but useless**.
- **Profilers diagnose; they don't clean up.** The fix is a code change.
- Attaching a heavy profiler to a live prod JVM has overhead: prefer **heap dumps + JFR**.
- Mock-1 feedback applies: answer as someone who has done it, in steps, ~2 min.

**DS7 — 2nd attempt notes (much better structure; trim to 4 steps):**
- ✓ Added a strong first step: **check JVM vs container memory**. `-Xms` / `-Xmx`, `-XX:MaxMetaspaceSize`, and **heap + metaspace + off-heap (thread stacks, direct buffers, code cache) must fit inside the container limit**, otherwise Kubernetes **OOMKills** the pod even though Java never threw OutOfMemoryError. In containers prefer **`-XX:MaxRAMPercentage=75`** over a fixed `-Xmx`.
- ✓ GC logs (old gen not shrinking, frequent full GCs), heap dump (retained size, reference chain), and the cause list (caches, statics, collections, listeners, ThreadLocal) + fixes (max size, eviction) were all right.
- ✗ **"Increase the heap size"** only **delays** a leak; say it's a temporary mitigation, not the fix.
- ✗ "Stop unneeded processes in JProfiler" isn't a leak fix; drop it.
- Missing: **compare two dumps over time** and **load-test in staging to verify** the fix.
- Length: 6 points ≈ 3 minutes → merge into **confirm → capture → analyse → fix & verify** (~2 min).

### MS10 — Designing an API for low latency: techniques — 🔴 Weak → 🟡 Partial on 2nd try (timeouts / async / fallback mentioned, but caching, DB, parallel calls, payload and connection techniques missing; ~3.5 min narrating the add-to-cart flow)

**Keywords:** **measure first** (p95 / p99, tracing → find the slow hop) · **caching** (local Caffeine / near cache, distributed Redis / Hazelcast, HTTP caching, CDN) · **database** (indexes, no N+1, projections, connection pool, read replicas, denormalise / precompute) · **fewer & parallel hops** (call independent services in parallel: CompletableFuture / WebClient `zip`; aggregate; avoid chatty APIs) · **async for non-critical work** (return early, side effects via Kafka) · **payload** (pagination, small DTOs, gzip, protobuf / gRPC) · **connections** (keep-alive, HTTP/2, pooled clients, no per-request TLS handshakes) · **runtime** (warm-up, GC tuning, autoscaling, same region) · timeouts + fallback protect the tail · retries **add** latency

**Model answer (~2 min):**
> "First I **measure**: p95 and p99 latency per endpoint, and a trace to see **which hop** is slow. Then the usual levers:
> 1. **Caching** is the biggest one: a local in-memory cache (Caffeine / Hazelcast near cache) for hot reads, a **distributed cache** like Redis or Hazelcast shared across instances, HTTP cache headers and a CDN for static or public data.
> 2. **Database**: right **indexes**, no N+1 (fetch joins / projections), a properly sized **connection pool**, **read replicas** for reads, and **precomputed / denormalised** views for heavy queries.
> 3. **Fewer and parallel hops**: avoid long chains of synchronous calls; call **independent services in parallel** (CompletableFuture or WebClient `zip`), and give the client one aggregated endpoint instead of many chatty calls.
> 4. **Do less on the request path**: anything the user doesn't need to wait for, like emails, analytics or audit, goes **async through Kafka**, and I return as soon as the critical part is done.
> 5. **Smaller and cheaper transfers**: pagination, lean DTOs, gzip, **gRPC / protobuf** between internal services, **keep-alive / HTTP/2 connection reuse** so we don't pay a TCP + TLS handshake per call.
> 6. **Protect the tail**: tight **timeouts**, circuit breaker and fallback, so one slow dependency can't blow the SLA.
>
> On my platform the target was **15,000 RPS with P95 under 1.2 s**; the biggest win was a **two-tier cache**: a local near cache in front of a distributed Hazelcast cluster."

**Traps:**
- The question asks for **techniques**: give a **grouped list**, not a narrated user flow.
- **Caching** must be the first technique you say.
- **Retries increase latency**: they're for reliability, not speed.
- WebClient = **non-blocking I/O, request-response style**. Don't say "non-blocking, blocking, synchronous".
- Use your own numbers (15k RPS, P95 < 1.2 s, Hazelcast two-tier cache): that's your strongest story.

**MS10 — 2nd attempt notes (much wider coverage; still ~3.5 min, and three claims contradict earlier answers):**
- ✓ Measure p95 / p99 first, DB indexes, fewer DB calls, read replicas for read-heavy, small payloads, parallel calls, non-blocking calls, async via Kafka for things the user doesn't wait for.
- ✗ **"Service mesh improves latency"**: a mesh **adds** a hop per call (two proxies). It's for consistency, security, control, not speed.
- ✗ **"A load balancer per cluster costs more than sidecars"**: the opposite. Sidecars in every pod usually cost **more** in total.
- ✗ **"Multiple instances to avoid a single point of failure"**: that's **availability**. More instances help throughput under load, not the latency of one request.
- ✗ **"Use views to avoid joins"**: a normal view **still runs the join**. Say **materialized views / denormalised read models**, and that joins with proper indexes are fine.
- ⚠ **Caching** is still only "static data in memory": lead with **local + distributed cache (Redis / Hazelcast)** and your **15k RPS / P95 < 1.2 s** story.
- ⚠ Read replicas bring **replication lag** (reads may be slightly stale): name the trade-off.
- Wording: Kafka = **asynchronous**; WebClient = **non-blocking I/O, request-response style**.
- Length: pick the **six levers** (measure → cache → DB → parallel / fewer hops → async → payload & connections), one line each, then your Hazelcast example. ~2 min.

## Wednesday — Data consistency

### DS3 (+ TR3 Saga, TR4 2PC) — A distributed transaction fails midway: ensure consistency — 🟡 Partial (2PC explained well; **orchestration and choreography swapped**; "compensating transaction" missing; ~5 min)

**Keywords:** no single ACID transaction across services (each owns its DB) · **2PC**: coordinator → **prepare** (each participant does the work, **locks**, votes yes/no) → **commit** if all yes, else **abort**; **avoided in microservices**: blocking (coordinator dies → locks held), all services must be up, slow, many stores (Kafka, NoSQL) don't support XA · **Saga** = sequence of **local transactions**, each commits immediately; on failure run **compensating transactions** in reverse (refund, release stock, cancel order) → **eventual consistency** · **Orchestration** = a **central orchestrator** tells each service what to do and triggers compensations (Temporal, Camunda, a saga class / state machine) · **Choreography** = **no central coordinator**; services react to each other's **events** via Kafka · steps idempotent + retryable, reliable events via **outbox**, order state machine (PENDING → CONFIRMED / CANCELLED)

**Model answer (~2 min):**
> "Each service owns its own database, so there's no single ACID transaction across order, inventory and payment. There are two approaches.
>
> **Two-phase commit**: a coordinator asks every participant to **prepare**: do the work, **hold the locks** and vote yes or no. If all vote yes, it sends **commit**; if any says no, **abort** and everyone rolls back. It gives strong consistency, but it's **blocking**: if the coordinator dies after prepare, participants sit on their locks. Every service must be up at the same time, it's slow, and Kafka or most NoSQL stores don't support it. So microservices normally avoid it.
>
> The usual answer is a **Saga**: a sequence of **local transactions**, each **committed immediately** in its own service. If a later step fails, we run **compensating transactions** for the steps already done, in reverse: payment fails → **release the reserved stock** → **mark the order CANCELLED**. Consistency is **eventual**, and the order goes through states like PENDING → CONFIRMED or CANCELLED.
>
> Two styles:
> - **Choreography**: **no coordinator**. Order publishes `OrderCreated`; inventory reserves stock and publishes `StockReserved`; payment charges and publishes `PaymentFailed`; inventory listens and releases stock; order listens and cancels. Loosely coupled, but the flow is spread across services and hard to follow as it grows.
> - **Orchestration**: a **central orchestrator** (a saga service, or a workflow engine like **Temporal / Camunda**) calls each step and, on failure, calls the compensations. The flow is in one place and easy to monitor; the orchestrator holds the logic.
>
> Either way the steps must be **idempotent and retryable**, and events must be published reliably, using the **transactional outbox**."

| | 2PC | Saga |
|---|---|---|
| Commit | all together at the end | each local step commits immediately |
| On failure | abort → rollback | **compensating transactions** |
| Consistency | strong | eventual |
| Locks | held across services | none across services |
| Fits microservices | poorly | yes |

| | Orchestration | Choreography |
|---|---|---|
| Who decides the next step | a **central orchestrator** | each service, reacting to **events** |
| Coupling | services coupled to the orchestrator | loose |
| Visibility of the flow | one place | spread across services |
| Good for | complex, many steps | simple, few steps |

**Traps:**
- **Choreography = events, no coordinator. Orchestration = central coordinator.** (Easy trick: an *orchestra* has a **conductor**.)
- Saga steps are already **committed**: you don't "roll back", you **compensate** (refund, release, cancel).
- Name the **outbox**: without it, "DB saved but event not sent" breaks the saga (KAFKA4).
- Don't describe the steps as parallel: a saga is usually **sequential**.

### KAFKA4 (+ TR11) — DB commit succeeds but Kafka publish fails: transactional outbox — 🟢 Strong (problem, same-DB outbox table, single local transaction, relay + retry all correct; add at-least-once / idempotent consumers, CDC alternative, correct status flow)

**Keywords:** **dual-write problem**: DB and Kafka can't share one transaction · **outbox table in the SAME database** · business row + outbox event in **one local transaction** (both or neither) · **relay**: poller (every N ms, batches, `FOR UPDATE SKIP LOCKED` / single leader) **or CDC (Debezium reads the transaction log)** → publish to Kafka → on **broker ack** mark **SENT** (or delete) · Kafka down → rows wait, relay retries, **user requests are not blocked** · **at-least-once**: relay may crash after publish, before marking → duplicate → **idempotent consumers** (event ID) · aggregate ID as Kafka key for ordering · clean up old rows

**Model answer (~90 sec):**
> "This is the **dual-write problem**: saving to the database and publishing to Kafka are two separate systems, so there's no single transaction; if Kafka is down after the DB commit, the event is lost and downstream services never hear about it.
>
> The fix is the **transactional outbox**. In the **same database** I have an `outbox` table. In **one local transaction** I insert the business row, the post, **and** an outbox row with the event (event ID, type, payload, status PENDING). Either both are saved or neither is.
>
> A **relay** then moves outbox rows to Kafka. Either a **poller** that reads PENDING rows in batches every few hundred milliseconds, publishes them, and **marks them SENT once Kafka acknowledges**; or, better, **CDC with Debezium**, which reads the database's transaction log and publishes new outbox rows with no polling. If Kafka is down, rows simply wait and the relay retries; the user's request was never blocked.
>
> This gives **at-least-once** delivery: if the relay crashes after publishing but before marking SENT, it publishes again. So consumers must be **idempotent**, deduplicating by event ID. I also use the **aggregate ID as the Kafka key** to keep per-entity ordering."

**Traps:**
- Name it: **dual-write problem**.
- Outbox is a **table in the same DB**, not a separate database; otherwise you're back to two systems.
- Mark **SENT after the broker ack**, not "in progress" before publishing.
- Always say **at-least-once → idempotent consumer**.
- **Debezium / CDC** is the production-grade relay (your bank also asks "Why use Debezium?").
- Several relay instances → `SELECT … FOR UPDATE SKIP LOCKED` or one leader, or you publish everything twice.

### DS5 — A distributed lock expires before the business operation completes: what problems occur? — 🔴 Weak (answered a different question: DB shared/exclusive locks + read anomalies are TX4 / TX3, and that content was correct; nothing on distributed-lock lease expiry)

**Keywords:** distributed lock = one lock **across many instances** (Redis/Redisson, ZooKeeper, etcd, **Hazelcast CP `FencedLock`**) · held with a **lease / TTL** so a crashed holder doesn't block forever · holder still working when the lease expires (long **GC pause**, slow DB / network) → **another instance acquires it** → **two holders at once** → double processing (double charge, double booking, oversold stock), **lost updates / stale writes** · old holder may **release the new holder's lock** · fixes: **fencing tokens** (increasing number per lock grant, storage rejects older token), **release only if owner matches**, **lease renewal / watchdog**, TTL > worst-case duration, **DB-level guard** (optimistic `@Version`, unique constraint, conditional update `WHERE status = 'PENDING'`), **idempotent** operation, consensus-based lock for correctness-critical work

**Model answer (~90 sec):**
> "A distributed lock is held with a **lease**, a TTL, so that if the holder crashes the lock frees itself. The danger: the holder is **still alive but slow**, a long GC pause or a slow DB call, and the lease **expires**. Another instance now acquires the lock, and **two instances believe they hold it at the same time**. Both process the same job: a customer is **charged twice**, a seat is **booked twice**, or the slower one writes **stale data over** the newer one. And when the first one finishes, it may **release the lock that now belongs to the second**, letting a third in.
>
> How I protect against it:
> 1. **Fencing tokens**: every time the lock is granted it comes with an increasing number; the database or downstream service **rejects writes carrying an older token**. Hazelcast's CP subsystem `FencedLock` gives exactly this.
> 2. **Release only if I'm the owner**: store a unique value in the lock and check it before deleting.
> 3. **Renew the lease** while working (Redisson's watchdog) and size the TTL above the worst-case duration.
> 4. **Never rely only on the lock**: make the operation **idempotent** and add a **DB-level guard**, optimistic locking with `@Version`, a unique constraint, or `UPDATE … WHERE status = 'PENDING'`, so even if two instances run, only one write wins."

**Traps:**
- **Distributed lock ≠ DB shared / exclusive lock.** DB locks live inside one database and end with the transaction; a distributed lock coordinates **instances** and ends with a **lease timeout**.
- Dirty / non-repeatable / phantom reads are **isolation-level anomalies** (TX3), not lock-expiry problems.
- The key phrase is **"two holders at the same time"**; the key fix is **fencing tokens**.
- You worked with **Hazelcast CP subsystem distributed locks**: use it as your example.

### DS8 — After scaling to multiple pods, a scheduled job runs on every instance: prevent duplicates — 🟡 Partial (right instinct: "an ID per execution, skip if already run"; but the check must live in a **shared** store with an atomic claim; standard tools not named; answer unfinished)

**Keywords:** `@Scheduled` runs in **every** pod · each pod has its own memory → a local map can't coordinate · **ShedLock**: `@SchedulerLock(name, lockAtMostFor, lockAtLeastFor)` + shared lock table / Redis → first pod to claim runs, others skip · **Kubernetes CronJob** (one pod per run, `concurrencyPolicy: Forbid`) · **leader election** (only the leader schedules: K8s Lease, ZooKeeper, Hazelcast) · **Quartz clustered** (JDBC job store) · DIY: Redis `SET job:report:2026-11-25 NX EX 600` · make the job **idempotent** / claim rows with `FOR UPDATE SKIP LOCKED` so pods can even share the work safely · `lockAtMostFor` is a lease → DS5 risks apply

**Model answer (~75 sec):**
> "`@Scheduled` runs in every JVM, so with five pods the job runs five times. The pods don't share memory, so the coordination has to go through a **shared store**.
>
> In Spring Boot the standard fix is **ShedLock**: I annotate the job with `@SchedulerLock(name = "dailySettlement", lockAtMostFor = "10m", lockAtLeastFor = "1m")` and give it a lock table in the shared DB (or Redis). When the schedule fires, every pod tries to **claim the lock row**; one succeeds and runs, the others **skip**. `lockAtMostFor` is a lease, so if the pod dies the lock frees itself.
>
> Alternatives: move the job out of the service into a **Kubernetes CronJob**, which starts exactly one pod per schedule; use **leader election** so only the leader pod runs schedules; or **Quartz in clustered mode**.
>
> And I still make the job **idempotent**: each record is claimed with `UPDATE … SET status = 'PROCESSING' WHERE status = 'PENDING'` or `SELECT … FOR UPDATE SKIP LOCKED`, so even an accidental second run can't process the same record twice. That even lets several pods **share** a big job safely."

```java
@Scheduled(cron = "0 0 2 * * *")
@SchedulerLock(name = "dailySettlement", lockAtMostFor = "10m", lockAtLeastFor = "1m")
public void runSettlement() { ... }
// + @EnableSchedulerLock(defaultLockAtMostFor = "10m") and a JdbcTemplateLockProvider bean
```

**Traps:**
- A **local** `Map` / `ConcurrentHashMap` doesn't help: each pod has its own copy. Say **shared DB / Redis**, and an **atomic** claim (insert-if-absent / `SETNX`), not "check then write".
- Name at least **ShedLock** and **Kubernetes CronJob**.
- `lockAtMostFor` too short → the lease-expiry problem from DS5 (two runs at once).
- (MDC and mTLS are unrelated here: MDC = logging context, mTLS = mutual TLS.)

### TR8 — CQRS (Command Query Responsibility Segregation) — 🟢 Strong (read-heavy motivation, separate write/read stores, sync via Kafka, eventual consistency, different DB types, even replay; name event sourcing, stress "different models", add when NOT to use)

**Keywords:** separate **command side** (writes: validation, business rules, transactions, normalised, often relational) from **query side** (reads: **denormalised read models / views** built for each screen: Elasticsearch, MongoDB, Redis) · read side updated from **events** (Kafka, via **outbox / CDC**) → **eventual consistency** · scale reads and writes independently · ≠ read replicas (same model) · often paired with **event sourcing** (store events, not state; replay to rebuild read models) · cost: complexity, duplicated data, **read-your-own-writes** lag · use for read-heavy, complex domains; not for simple CRUD

**Model answer (~90 sec):**
> "CQRS splits an application into a **command side**, which handles **writes** (placing an order, changing a price) with all the validation and business rules, and a **query side**, which handles **reads** from **separate read models** shaped exactly for each screen.
>
> The point isn't only load: it's **different models**. The write model is normalised and transactional, usually relational. The read model is **denormalised**: a product-page document with price, stock, rating and reviews already joined, in **Elasticsearch, MongoDB or Redis**, so a read is one fast lookup. Whenever the write side changes, it publishes an **event** through Kafka (reliably, with the **outbox** or CDC), and the query side updates its views. That makes reads **eventually consistent**, and lets me scale and tune each side independently: an e-commerce site has far more product views than purchases.
>
> It's often combined with **event sourcing**: the write side stores the **sequence of events** instead of the current state, and read models can be rebuilt by **replaying** them.
>
> The cost: more moving parts, duplicated data, and **lag**, so a user may not see their own change instantly. So I'd use it for **read-heavy, complex domains**, not for simple CRUD services."

**Traps:**
- CQRS ≠ **read replicas**: replicas copy the **same** schema; CQRS has **different models** for reads and writes.
- "Storing every write as a log and replaying it" = **event sourcing**: name it; it's a separate pattern often used with CQRS.
- Sync with **events via outbox / CDC**, not database triggers.
- Always give **when not to use it**.

## Thursday — REST API & Deployment

### DB4 (+ REST3, REST5) — Payment processed twice due to client retries: make the API idempotent — 🔴 Weak (right instinct: "store a key, check if already processed"; but the **server** generating the key can't detect a retry — the **client** must send it)

**Keywords:** **client** generates an **`Idempotency-Key`** (UUID) **once per logical operation** and sends the **same key on every retry** · server: **atomic** insert into an idempotency table with a **UNIQUE** key (or Redis `SET NX`) → first request processes · repeat key + **COMPLETED** → return the **stored original response** (same status + body), not an error · repeat key + **IN_PROGRESS** → `409 Conflict` (retry later) · same key, **different body** → `422` · store response + mark COMPLETED with the payment · keys expire (e.g. 24 h) · pass the key to the **payment gateway** too · second safety net: **unique constraint on the business key** (order ID / payment intent)

**Model answer (~90 sec):**
> "A retry is a new HTTP request, so the server can't tell it's a duplicate unless the **client** says so. The client generates an **idempotency key**, a UUID, **once per payment attempt**, and sends the **same key** in an `Idempotency-Key` header on every retry.
>
> On the server, before charging anything, I **atomically** insert the key into an idempotency table with a **unique constraint** (or Redis `SET NX`), together with a hash of the request and status IN_PROGRESS.
> - If the insert **succeeds**, this is the first time: process the payment, then **save the response** and mark the key COMPLETED.
> - If the key **already exists and is COMPLETED**, I don't charge again: I **return the stored original response**, the same 200 and body, so the client sees exactly what it would have seen the first time.
> - If it's **still IN_PROGRESS**, return **409 Conflict** so the client retries later.
> - Same key with a **different body**: reject with **422**.
>
> I also pass the key to the external **payment gateway**, which supports idempotency keys too, so the real charge is never duplicated, and keep a **unique constraint on the order / payment-intent ID** as a second safety net. Keys expire after about 24 hours."

```java
@PostMapping("/payments")
public ResponseEntity<PaymentResponse> pay(@RequestHeader("Idempotency-Key") String key,
                                           @RequestBody PaymentRequest req) {
    return idempotencyService.execute(key, req, () -> paymentService.charge(req, key));
}

@Transactional
public ResponseEntity<PaymentResponse> execute(String key, PaymentRequest req,
                                                Supplier<PaymentResponse> work) {
    Optional<IdemRecord> existing = repo.findById(key);
    if (existing.isPresent()) {
        IdemRecord r = existing.get();
        if (!r.getRequestHash().equals(hash(req))) throw new UnprocessableEntityException();   // 422
        if (r.getStatus() == IN_PROGRESS) throw new ConflictException();                        // 409
        return ResponseEntity.status(r.getHttpStatus()).body(r.getResponse());                 // replay
    }
    repo.saveAndFlush(new IdemRecord(key, hash(req), IN_PROGRESS));   // unique PK: a racing duplicate fails here
    PaymentResponse res = work.get();
    repo.save(new IdemRecord(key, hash(req), COMPLETED, 200, res));
    return ResponseEntity.ok(res);
}
```

**Traps:**
- **Server-generated key = useless**: every retry would get a new key. The key comes from the **client**.
- A duplicate isn't an error: return the **same original response**.
- Insert-if-absent must be **atomic** (unique constraint / `SETNX`), not "check, then insert".
- "Item pattern" = **idempotent**.

### MS12 — Have you used Swagger? — 🟡 Partial (auto-generated endpoint page + try-it-out + vs Postman are right; OpenAPI, the real purpose (contract / docs / codegen) and the Spring Boot setup missing)

**Keywords:** **OpenAPI Specification** (OAS 3) = the **standard**: a JSON/YAML **contract** describing endpoints, params, request/response schemas, auth · **Swagger** = the **tools** around it: Swagger UI, Editor, Codegen · purpose: **API documentation / contract** for consumers (frontend, other teams, partners), **client & server code generation** (openapi-generator), try-it-out · Spring Boot 3: **springdoc-openapi** (`springdoc-openapi-starter-webmvc-ui`) → `/swagger-ui.html`, `/v3/api-docs`; annotations `@Operation`, `@Tag`, `@Schema`, `@ApiResponse`; configure the **JWT bearer** scheme · **springfox is outdated** (no Boot 3) · code-first vs **contract-first** · **disable or protect** Swagger UI in production

**Model answer (~60 sec):**
> "Yes. Swagger is the tooling around the **OpenAPI specification**, a standard JSON/YAML **contract** that describes every endpoint: paths, parameters, request and response schemas, error codes and authentication.
>
> In Spring Boot 3 I add **springdoc-openapi**; it scans the controllers and generates the spec at `/v3/api-docs` and the interactive **Swagger UI** at `/swagger-ui.html`. I enrich it with annotations like `@Operation`, `@Schema` and `@ApiResponse`, and configure the **JWT bearer** scheme so 'Try it out' works on secured endpoints.
>
> The main value is that it's the **single source of truth for consumers**: frontend and other teams read it instead of asking us, and they can **generate client code** from it with openapi-generator. Some teams go **contract-first**: write the OpenAPI file, then generate the server interfaces. Unlike Postman, nobody has to add endpoints by hand; it stays in sync with the code. In **production** I disable or secure the UI."

```xml
<dependency>
  <groupId>org.springdoc</groupId>
  <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
</dependency>
```
```yaml
springdoc.swagger-ui.enabled: false   # in the prod profile
```

**Traps:**
- Swagger isn't mainly a **testing** tool: it's **documentation + contract + codegen**; try-it-out is a bonus.
- Say **OpenAPI** (the spec) vs **Swagger** (the tools).
- **springdoc**, not springfox, for Spring Boot 3.
- It uses **example values from schemas**: no "random data / batch testing".

### MS16 — GraphQL vs REST: where is GraphQL beneficial? — 🟡 Partial (REST = architectural style over HTTP, over-fetching, single `/graphql` endpoint, query vs mutation all right; under-fetching, the "where beneficial" cases and the drawbacks missing)

**Keywords:** REST = **architectural style** (resources + HTTP methods, stateless, cacheable), many endpoints, server decides response shape → **over-fetching** and **under-fetching** (several round trips: user → orders → items) · GraphQL = **query language over one endpoint**, **client chooses fields**, nested related data **in one request**, **strongly typed schema** + introspection, **query / mutation / subscription** · beneficial: **many clients needing different shapes** (web vs mobile), **mobile / slow networks**, **aggregating many microservices** behind one API (BFF / federation), fast-changing frontends · drawbacks: **HTTP / CDN caching hard** (one URL, POST), **N+1 in resolvers** (DataLoader / `@BatchMapping`), expensive queries need **depth / complexity limits**, errors in `errors[]` with 200, harder rate limiting · simple CRUD / public cacheable APIs → REST · Spring for GraphQL: `@QueryMapping`, `@MutationMapping`, `@SchemaMapping`

**Model answer (~90 sec):**
> "**REST** is an architectural style: resources at URLs, HTTP methods for actions, stateless and **cacheable**. The server decides the shape of each response, so clients often **over-fetch**, getting the whole user with images when they only need the name, or **under-fetch**, needing `/users/1`, then `/users/1/orders`, then each order's items: several round trips.
>
> **GraphQL** exposes **one endpoint** with a **strongly typed schema**. The client sends a **query listing exactly the fields it needs, including nested data**, and gets it in **one round trip**. Writes are **mutations**, and **subscriptions** push real-time updates.
>
> It's most beneficial when **many different clients need different shapes** of the same data (a mobile app wants 3 fields, the web dashboard 30), on **slow mobile networks**, and as a **single API layer over many microservices**, a BFF or federated gateway.
>
> The trade-offs: **HTTP and CDN caching** is much harder because everything goes to one URL; resolvers easily cause **N+1 queries**, so you batch with a **DataLoader** (`@BatchMapping` in Spring for GraphQL); clients can send **very expensive queries**, so you need depth and complexity limits; and errors come back as 200 with an `errors` array. For simple CRUD or public, cache-friendly APIs, I'd stay with REST."

**Traps:**
- Don't stop at "less payload": mention **under-fetching / multiple round trips**, that's GraphQL's strongest point.
- Always give **drawbacks** (caching, N+1, query cost); interviewers expect a balanced answer.
- GraphQL queries can also be sent with **GET**; POST is just the common default.

### MS6 (+ DEP2) — Deployment strategies: rolling vs canary vs blue-green — 🟢 Strong (all three correct, real experience "we use canary", good staged percentages; name Recreate, add readiness probes / backward compatibility / cost; trim the downtime story)

**Keywords:** **Recreate** = stop all v1, start v2 → downtime (K8s `strategy: Recreate`) · **Rolling** (K8s default `RollingUpdate`): replace pods a few at a time (`maxSurge`, `maxUnavailable`), **readiness probe** gates traffic, near-zero downtime, no extra infra, **v1 and v2 serve at the same time**, rollback = `kubectl rollout undo` (also gradual) · **Canary**: small % of real traffic to v2 (5 → 10 → 25 → 50 → 100), **promote or roll back by metrics** (error rate, p99, business KPIs), least blast radius; Istio / Argo Rollouts / Flagger · **Blue-green**: two full environments, test green, **switch the LB / router at once**, **instant rollback** by switching back, **double infra cost** during release · all: **backward-compatible APIs and DB schema** (expand-contract migrations)

**Model answer (~2 min):**
> "There are three common strategies, plus the naive **Recreate**: stop all v1 pods, then start v2, which means downtime.
>
> **Rolling update** is Kubernetes' default: it replaces pods **a few at a time**, controlled by `maxSurge` and `maxUnavailable`, and a new pod only gets traffic once its **readiness probe** passes. No extra infrastructure and near-zero downtime; but for a while **v1 and v2 serve together**, and rollback (`kubectl rollout undo`) is also gradual.
>
> **Canary** sends a **small percentage of real traffic** to v2, say 10%, watches **error rate, latency and business metrics** for a set period, then increases step by step: 10, 30, 50, 100%. If metrics get worse, all traffic goes back to v1 immediately. On my project we use **canary**; the bake time at each step can be hours, sometimes a day, because we're dealing with payment traffic. Tools like **Istio, Argo Rollouts or Flagger** automate the traffic split and analysis.
>
> **Blue-green** runs a **full copy** of the environment: blue is live, green has the new version. We test green, then **switch the load balancer** all at once; rollback is **instant**, switching back. The cost is **double infrastructure** during the release.
>
> Whatever the strategy, old and new versions overlap, so **APIs and database schema changes must be backward-compatible**: we use expand-then-contract migrations."

| | Rolling | Canary | Blue-green |
|---|---|---|---|
| How | replace pods gradually | small % of traffic → increase | full parallel env, switch at once |
| Extra infra | none / small | small | **double** |
| Rollback | gradual (`rollout undo`) | fast: route back | **instant**: switch back |
| Risk exposure | all users get v2 progressively | only the canary % | all users at switch time (after testing green) |
| Best for | default, low cost | risky changes, real-traffic validation | instant cutover + instant rollback |

**Traps:**
- "Stop all v1, then start v2" is **Recreate**, not rolling.
- Canary decisions should be **metric-driven**, not just time-based.
- Mention **backward compatibility** (API + DB): it's the follow-up interviewers ask.
- Blue = current live, green = new.

### MS9 (+ DEP1) — CI/CD: basic understanding — 🟡 Partial (push → automatic build on Jenkins → Docker image → registry → deploy is right, and "we use Jenkins" is a good real detail; tests / quality gates, Delivery vs Deployment, environments and the Kubernetes deploy step missing)

**Keywords:** **CI** = every push / PR is automatically **built and tested** and merged often → integration bugs caught early · **CD** = **Continuous Delivery** (every green build is *deployable*; prod needs a **manual approval**) vs **Continuous Deployment** (every green build goes to prod automatically) · stages: build (Maven) → **unit tests** → **quality & security gates** (SonarQube, dependency / image scans) → **Docker image tagged with the commit SHA** → push to **registry** (GCP Artifact Registry / ECR / Docker Hub) → deploy **dev → integration tests → staging → approval → prod** with **canary / rolling** → monitor + **auto-rollback** · **build once, promote the same image** · on Kubernetes the pipeline **updates the Deployment's image tag** (kubectl / Helm / GitOps Argo CD); K8s pulls the image and rolls it out · config per environment, secrets from **Vault**

**Model answer (~90 sec):**
> "**CI, continuous integration**, means every push or pull request is **automatically built and tested**, so the main branch is always in a working state and integration problems show up within minutes. **CD** is either **continuous delivery**, where every green build is ready to release and production needs a manual approval, or **continuous deployment**, where it goes to production automatically.
>
> On my project the pipeline runs on **Jenkins**. On a push it does a Maven **build**, runs **unit tests**, then **quality and security checks**, static analysis and dependency scanning, and fails the build if any gate fails. Then it builds a **Docker image tagged with the commit SHA** and pushes it to the **container registry**.
>
> Deployment is to **Kubernetes on GKE**: the pipeline updates the Deployment to the new image tag, and Kubernetes pulls the image and rolls it out. The **same image** is promoted **dev → staging → production**: integration tests in dev, end-to-end and performance checks in staging, an **approval** before prod, and in prod a **canary** release while we watch error rate and latency, with rollback if they get worse. Environment config comes from config maps, and secrets from **Vault**."

**Traps:**
- CI isn't "automate everything": it's **automatic build + tests on every change**, with **quality gates**.
- Know **Continuous Delivery vs Continuous Deployment** (manual prod approval or not).
- You **build an image**; a **container** is a running instance of an image ("load the image to the container" is wrong).
- On Kubernetes you **don't run containers on instances one by one**: you update the Deployment and K8s does the rollout.
- Tag images with the **commit SHA / version**, never just `latest`; build once, promote the same artifact.

### MS7 — HPA and Ingress in Kubernetes — 🟡 Ingress / 🔴 HPA (Ingress motivation right: one entry instead of a load balancer per service; Ingress Controller, TLS, routing-to-Services missing; HPA not answered)

**Keywords:** **HPA (HorizontalPodAutoscaler)** = automatically changes the **number of pod replicas** of a Deployment from metrics: **CPU / memory utilisation** (needs **resource requests** + metrics-server) or **custom / external metrics** (requests per second, **Kafka consumer lag** via Prometheus adapter / KEDA) · `minReplicas`, `maxReplicas`, target e.g. 70% CPU · control loop ~15 s · **scale-down stabilisation window** (default 5 min) avoids flapping · ≠ **VPA** (bigger pods) · **Cluster Autoscaler** adds **nodes** when pods can't be scheduled · Kafka consumers: no gain beyond the **partition count** · Java: start-up / JIT warm-up → readiness probes · **Ingress** = K8s object with **L7 HTTP(S) routing rules**: **host- and path-based** → **Services** · needs an **Ingress Controller** to work (NGINX Ingress, **GKE Ingress → Google Cloud HTTP(S) Load Balancer**, Traefik) · **one external LB for many services** · **TLS termination** (cert in a Secret) · not a full API gateway (no auth / rate limiting unless the controller adds it) · successor: **Gateway API**

**Model answer (~90 sec):**
> "**HPA**, the Horizontal Pod Autoscaler, automatically **adds or removes pod replicas** of a Deployment based on metrics. Most commonly **CPU utilisation**: I set min 3, max 20 replicas and a target of 70% CPU, and every ~15 seconds it compares actual usage, calculated against the pods' **resource requests**, with the target and scales. It can also use **custom metrics** like requests per second or **Kafka consumer lag**. It scales **down slowly**, with a stabilisation window, to avoid flapping. If the new pods don't fit on the nodes, the **Cluster Autoscaler** adds nodes.
>
> **Ingress** is the **HTTP entry point** into the cluster. Instead of a separate cloud load balancer for every service, one Ingress holds **routing rules**: `api.example.com/orders` → order Service, `/payments` → payment Service, and does **TLS termination**. The rules are only data; an **Ingress Controller** implements them: NGINX, or on GKE the controller provisions a **Google Cloud HTTP(S) Load Balancer**. Each Service then load-balances across its pods. It's L7 routing and TLS, not a full API gateway: auth or rate limiting need the controller's extras or a real gateway."

```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata: { name: order-hpa }
spec:
  scaleTargetRef: { apiVersion: apps/v1, kind: Deployment, name: order-service }
  minReplicas: 3
  maxReplicas: 20
  metrics:
    - type: Resource
      resource: { name: cpu, target: { type: Utilization, averageUtilization: 70 } }
---
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata: { name: api }
spec:
  tls: [{ hosts: [api.example.com], secretName: api-tls }]
  rules:
    - host: api.example.com
      http:
        paths:
          - { path: /orders,   pathType: Prefix, backend: { service: { name: order-service,   port: { number: 80 } } } }
          - { path: /payments, pathType: Prefix, backend: { service: { name: payment-service, port: { number: 80 } } } }
```

**Traps:**
- HPA needs **resource requests** on the containers, or CPU % can't be calculated.
- **Horizontal** (more pods) ≠ **vertical** (bigger pods, VPA) ≠ **Cluster Autoscaler** (more nodes).
- Ingress routes to **Services**, not directly to nodes / pods / ports; it does nothing without an **Ingress Controller**.
- Ingress ≈ gateway-lite: routing + TLS, not auth / rate limiting by default.

### MS8 — Have you worked with AWS / GCP? How does deployment happen? — 🟡 Partial (real, concrete GCP flow: tests → GitHub → Jenkins → Docker image → instance group → canary; mixes pods / instances, and the canary description blends in rolling; load balancer, health checks, monitoring and rollback missing)

**Keywords:** say what you use: **GCP** · compute = **GKE** (pods, Deployments) **or Compute Engine Managed Instance Groups** (VMs from an **instance template** that references the container image); pick the right word: **pods** = Kubernetes, **instances** = VMs · image registry: **Artifact Registry** (or Docker Hub) · **Cloud Load Balancing** with **health checks** in front · MIG / HPA **autoscaling** · **canary** = shift **traffic %** to a v2 group while v1 keeps running; **rolling** = replace v1 instances one by one · MIG canary: `rolling-action start-update --canary-version=template=v2,target-size=10%` · watch error rate / latency in **Cloud Monitoring / Grafana**, logs in Cloud Logging / Splunk · rollback = shift traffic / template back · AWS equivalents: **EKS** (≈GKE), **ECS** (managed containers), **EC2 Auto Scaling groups** (≈MIG), **ECR** (≈Artifact Registry), **ALB** (≈Cloud LB)

**Model answer (~90 sec):**
> "Yes, I work on **GCP**. A change goes through **Jenkins**: build, unit tests and quality checks, then a **Docker image** tagged with the commit SHA is pushed to the **registry**.
>
> The services run behind a **Google Cloud load balancer** with **health checks**, on [**GKE** / **Compute Engine managed instance groups**, say which]. In the instance-group setup, the **instance template** points to the image version; deploying v2 means creating a new template. We release it as a **canary**: a small number of v2 instances take about **10% of the traffic** while v1 keeps serving the rest; we watch **error rate and latency** in our dashboards for hours, then go to 30, 50 and 100%. If anything looks wrong, we **send traffic back to v1** and remove the canary. Once v2 is at 100% and stable, the v1 instances are removed. The group **autoscales** on CPU, and logs go to Splunk.
>
> On AWS the same pieces would be **ECR** for images, **EKS or ECS** for running containers, **EC2 Auto Scaling groups**, and an **Application Load Balancer**."

**Traps:**
- **Pods** (Kubernetes) and **instances** (VMs) are different things: don't mix them in one answer.
- Canary = **traffic percentage to the new version**; replacing old instances one at a time is **rolling**.
- Mention the **load balancer + health checks** and **how you watch and roll back**.
- "GC2" = **GCP**.

### MS13 — What AWS services have you used? — 🟡 Partial (good list; **ECS ≠ registry**; be ready to say *where and for what* you used each)

**Keywords (one line each, be ready for follow-ups):** **EC2** = virtual machines · **Auto Scaling group** = keeps N EC2 instances, scales on metrics, replaces unhealthy ones · **ALB** = L7 (HTTP) load balancer, path / host routing, health checks · **EKS** = managed Kubernetes · **ECS** = AWS's own container orchestrator (with **Fargate** = serverless containers) · **ECR** = container **image registry** · **S3** = object storage (files, backups, static assets, lifecycle rules) · **Lambda** = serverless functions triggered by events (S3, SQS, API Gateway); cold starts, 15-min limit · also common: **RDS**, **SQS / SNS**, **CloudWatch** (logs / metrics / alarms), **IAM roles**, **API Gateway**, **Route 53**, **VPC** · GCP mapping: Compute Engine ≈ EC2, MIG ≈ ASG, Cloud LB ≈ ALB, GKE ≈ EKS, Artifact Registry ≈ ECR, Cloud Storage ≈ S3, Cloud Functions / Cloud Run ≈ Lambda, Pub/Sub ≈ SQS + SNS

**Model answer (~60 sec):**
> "My current project is on **GCP**, but the building blocks map one-to-one, and I've worked with the AWS ones: **EC2** with **Auto Scaling groups** behind an **ALB**, **EKS** for Kubernetes workloads, **ECR** for container images, **S3** for storage, and **Lambda** for small event-driven jobs [say the real project / use: e.g. 'a Lambda triggered by S3 uploads to process files']. On GCP I use the equivalents daily: Compute Engine / GKE, Cloud Load Balancing, Artifact Registry, Cloud Storage, Cloud Logging and Monitoring."

**Traps:**
- **ECR** = Elastic Container **Registry** (images). **ECS** = Elastic Container **Service** (runs containers).
- Only list services you can answer **"what did you use it for?"** on: interviewers always follow up on Lambda and S3.
- Saying "we're on GCP, here's the AWS equivalent" is a strong, honest answer.

### MS17 — AWS Elastic Load Balancing: resiliency, fault tolerance, limitations — 🔴 Weak ("elastic = scales automatically, managed by AWS" is right; "creates a backup LB and moves traffic" is not how it works; mechanisms and limitations missing)

**Keywords:** types: **ALB** (L7 HTTP: path / host routing), **NLB** (L4 TCP / UDP, static IPs, very low latency), GWLB, Classic (legacy) · **resiliency**: LB nodes run in **every enabled Availability Zone** (≥ 2 AZs), DNS name points to them; AZ fails → DNS stops returning it · **health checks** remove unhealthy targets automatically; with an **Auto Scaling group** they're also **replaced** · **cross-zone load balancing** spreads load evenly across AZs · LB **capacity scales automatically** · **deregistration delay / connection draining** lets in-flight requests finish during deploys · **limitations**: scales **gradually** → a sudden huge spike can see 503s (load-test / pre-warm before big events) · **regional** → multi-region needs **Route 53** failover / latency routing or Global Accelerator · ALB IPs change (use the DNS name; NLB for static IPs) · **idle timeout** (default 60 s) for long connections · not a full API gateway (auth / rate limiting → API Gateway / WAF) · health check can pass while the app is broken (use a real `/health` endpoint)

**Model answer (~90 sec):**
> "ELB is a managed load balancer, mainly **ALB** for HTTP with path- and host-based routing, and **NLB** for TCP with static IPs and very low latency.
>
> For **fault tolerance**: I enable it in **at least two Availability Zones**; AWS runs load-balancer nodes in each, and if a whole AZ goes down, traffic goes to the others. **Health checks** constantly probe the targets and **stop sending traffic to unhealthy ones**, and when the targets are in an **Auto Scaling group**, those instances are **replaced** automatically. **Cross-zone load balancing** spreads requests evenly over all targets, the LB's own capacity **scales automatically**, and **deregistration delay** lets in-flight requests finish when a target is removed during a deploy.
>
> **Limitations**: it scales **gradually**, so a sudden, massive spike, a flash sale, can hit 503s until it catches up; you load-test or pre-warm before big events. It's **regional**: surviving a region outage needs **Route 53** failover or Global Accelerator in front. ALB IPs change, so clients use the DNS name, or NLB if they need static IPs. There's an **idle timeout** to tune for long-lived connections. And it's not an API gateway: auth and rate limiting need API Gateway or WAF."

**Traps:**
- It doesn't "create a backup load balancer": resilience comes from **multi-AZ nodes + health checks + auto scaling**.
- "Ingress" is a Kubernetes concept, not part of ELB (though the AWS Load Balancer Controller can create an ALB **from** a K8s Ingress).
- The question has two halves: give **limitations** too.

### MS18 — Design a multi-service architecture in AWS — 🟡 Partial (right layers: gateway, multiple instances, sync / async, SNS + SQS, observability; too skeletal: no network, data, security, compute choice or deployment, few AWS names)

**Keywords:** walk **top → bottom**: **Route 53** (DNS) → **CloudFront** (CDN) + **WAF** → **API Gateway** or **ALB** (auth with Cognito / JWT, rate limiting) → services on **EKS** or **ECS Fargate**, **multi-AZ**, autoscaling → **VPC** with public subnets (LB only) and **private subnets** (services, DBs) · **database per service**: **RDS / Aurora** (relational), **DynamoDB** (key-value), **ElastiCache Redis** (cache) · sync: REST / gRPC via service discovery (K8s DNS / Cloud Map) + timeouts, retries, circuit breakers · async: **SNS → SQS fan-out**, or **MSK (Kafka)**, outbox · **IAM role per service** (least privilege), **Secrets Manager / Parameter Store** · observability: **CloudWatch** logs / metrics / alarms, **X-Ray** or OpenTelemetry tracing · CI/CD: pipeline → **ECR** → EKS with canary / rolling · DR: backups, multi-AZ DBs, multi-region only if the SLA needs it

**Model answer (~2 min):**
> "I'd describe it from the user inward.
>
> **Edge**: **Route 53** for DNS, **CloudFront** for static content, **WAF** against attacks, then **API Gateway** or an **ALB** as the single entry point, validating **JWTs** (Cognito or our auth service) and **rate-limiting**.
>
> **Compute**: each service is containerised and runs on **EKS** (or ECS Fargate if we don't want to manage Kubernetes), with **several replicas across at least two Availability Zones** and **autoscaling** on CPU or request rate, all inside a **VPC**: only the load balancer is in public subnets, services and databases are in **private subnets**.
>
> **Data**: **database per service**: **Aurora / RDS** for transactional data like orders and payments, **DynamoDB** where key-value access fits, **ElastiCache Redis** for caching. No service reads another's database.
>
> **Communication**: synchronous calls (REST, or **gRPC** internally) only where an immediate answer is needed, with timeouts, retries and circuit breakers; everything else **asynchronous** through **SNS → SQS fan-out** or **Amazon MSK (Kafka)**, with the **outbox** pattern for reliable events.
>
> **Security & config**: an **IAM role per service** with least privilege, secrets in **Secrets Manager**, TLS everywhere.
>
> **Observability**: **CloudWatch** logs, metrics and alarms, distributed tracing with **X-Ray / OpenTelemetry**, trace IDs in every log.
>
> **Delivery**: CI pipeline → image in **ECR** → deploy to EKS with **canary**, plus automated rollback."

```
Users → Route 53 → CloudFront + WAF → API Gateway / ALB
                                         │
                ┌────────────── VPC (multi-AZ, private subnets) ──────────────┐
                │  order-svc   payment-svc   inventory-svc   (EKS / ECS, HPA)  │
                │     │             │              │                          │
                │  Aurora        Aurora        DynamoDB      ElastiCache Redis │
                │        └── SNS → SQS / MSK (events, outbox) ──┘             │
                └──────────────────────────────────────────────────────────────┘
        CloudWatch · X-Ray / OTel · Secrets Manager · IAM roles · ECR + CI/CD
```

**Traps:**
- Don't stop at categories ("API gateway, observability"): **name the AWS service** for each layer.
- Always include **network (VPC, private subnets, multi-AZ)**, **database per service** and **security (IAM, secrets)**: they're what "architecture in AWS" is about.
- "Easy-to-instance" = **EC2 instance**; SNS + SQS, not "EQS".

### DS6 — Different regions return different results for the same request: investigate — 🟡 Partial (right instincts: trace and compare the paths, X-Ray / ADOT, check DNS / CDN, config drift; answer cut off, causes not organised)

**Keywords:** reproduce from both regions, compare **full responses + headers** (served region, app version, `X-Cache`, `Age`) · **routing / edge**: which region did DNS send it to (Route 53 latency / geo routing), **CDN serving a stale cached copy** in one region (TTL, cache key, invalidation not done everywhere) · **deployment drift**: different **app version** per region (canary only in one), different **feature flags** · **config drift**: env vars, secrets, downstream endpoints differ · **data**: **cross-region replication lag** (eventual consistency: Aurora Global, DynamoDB global tables), stale **regional cache** (Redis), or **region-specific data by design** (pricing, data residency) · time zone / locale / currency · third-party dependency differs per region · tools: per-region **traces** (X-Ray / ADOT / OTel), version metrics, config diff (GitOps), replication-lag metrics, CDN logs · prevent: same artifact + config via GitOps, drift detection, version + region headers, replication-lag alerts

**Model answer (~90 sec):**
> "First I **reproduce** it: send the same request to both regions and compare the **full responses and headers**: which region and **app version** served it, and cache headers like `X-Cache` and `Age`. Then I work through the usual causes:
> 1. **Edge and caching**: is **Route 53** routing the user where I think, and is the **CDN or a regional Redis serving a stale copy** in one region because an invalidation didn't reach it?
> 2. **Deployment drift**: is a **different version** running, say a canary that only went to one region, or a **feature flag** that's on in one region only?
> 3. **Config drift**: different environment variables, secrets or downstream endpoints per region.
> 4. **Data**: regional databases replicate **asynchronously**, so one region can lag behind; I check the **replication-lag** metrics. Or the difference is **by design**: regional pricing or data-residency rules.
> 5. Smaller things: **time zones, locale and currency**, or a third-party API that behaves differently per region.
>
> I compare **traces from both regions** side by side in X-Ray / OpenTelemetry to see where the paths diverge. To prevent it: deploy the **same artifact and config through GitOps** with drift detection, return **version and region headers**, and alert on replication lag."

**Traps:**
- Have a **structured list of causes** (edge / cache, version, config, data, locale): interviewers want a method, not one guess.
- Mention **replication lag**: it's the most common real cause.
- "Configuration draft" = **configuration drift**; ADOT = **AWS Distro for OpenTelemetry**.

### REST1 + REST2 + REST4 — Why GET / PUT / DELETE are idempotent but POST isn't; is PATCH? — 🔴 Weak (idempotency was confused with "targeting one row by ID"; PATCH = "not guaranteed" was right)

**Keywords:** **idempotent** = sending the **same request N times leaves the server in the same state as sending it once** (the response may differ) · nothing to do with how many rows are affected · **safe** = no state change at all (GET, HEAD, OPTIONS) · GET: read-only → safe + idempotent · **PUT** /users/1 {full body}: **replace with this exact state** → repeat = same state → idempotent · **DELETE** /users/1: gone after the first call, still gone after the 2nd (204 then 404, same state) → idempotent · **POST** /payments: **creates a new resource each time** (new ID) → 2 calls = 2 payments → **not idempotent** → needs an **idempotency key** (DB4) · **PATCH**: depends on the operation: "set status = SHIPPED" is idempotent; "increment balance by 100" / "append item" is not → **not guaranteed** · why it matters: clients, proxies and gateways can **safely retry** idempotent requests automatically

**Model answer (~60 sec):**
> "**Idempotent** means that sending the **same request several times leaves the server in the same state** as sending it once.
> - **GET** only reads, so it's **safe** and idempotent.
> - **PUT** replaces the resource with the exact state I send: `PUT /users/1` with the full user sets it to that state; sending it again sets the same state.
> - **DELETE** `/users/1` removes it; repeating it changes nothing more. The response may change from 204 to 404, but the state is the same.
> - **POST** usually **creates a new resource** each time, so two identical POSTs create **two orders or two payments**: not idempotent, which is why we add an **idempotency key**.
> - **PATCH** depends on what it does: `{"status": "SHIPPED"}` is idempotent, but 'increase the balance by 100' or 'add an item' is not, so PATCH is **not guaranteed** idempotent.
>
> It matters for **retries**: idempotent requests can be retried safely after a timeout; POST can't, without a key."

| Method | Safe | Idempotent |
|---|---|---|
| GET | ✓ | ✓ |
| PUT | ✗ | ✓ |
| DELETE | ✗ | ✓ |
| POST | ✗ | ✗ (unless idempotency key) |
| PATCH | ✗ | depends on the operation |

**Traps:**
- Idempotency is **not** about "updating / deleting one row vs the whole table": that's the WHERE clause / ID in the URL.
- **Safe ≠ idempotent**: DELETE is idempotent but not safe.
- DELETE's **response** can change (204 → 404); idempotency is about the **state**.
