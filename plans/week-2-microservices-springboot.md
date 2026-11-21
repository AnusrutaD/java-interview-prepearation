# Week 2 plan — Microservices, REST API & Deployment, then Spring Boot revision
**Sun 22 Nov → Sat 28 Nov 2026** · 77 questions · printable version: `Week2_Microservices_SpringBoot_Plan.pdf`

**Tick only when you can answer out loud in 2 minutes:** definition → 2–3 points → Visa example → *"Want me to go deeper?"*

**Prefixes:** MS / DS / KAFKA / DB = question bank · TR / REST / DEP = ProgressTracker (tracker order) · SB / TX / SEC = same numbers as last week

## Sun 22 Nov — Foundations & communication
*About 6 hours · Start with your own story: every microservices round opens with it*

**Your project & the basics**
- [ ] **TR1** Have you worked on microservices? (your Visa / CyberSource answer)
- [ ] **TR2** Difference between monolithic vs microservice application
- [ ] **TR12** Strangler fig (monolith to microservice migration)

**How services talk to each other**
- [ ] **MS2** How do two microservices communicate with each other (sync vs async)?
- [ ] **TR16** Sync communication: RestTemplate, WebClient
- [ ] **TR17** Async communication: message queue, Kafka, RabbitMQ
- [ ] **TR15** How two microservices communicate: service mesh proxy, sidecar

**Messaging choices**
- [ ] **MS15** Kafka vs RabbitMQ
- [ ] **MS14** SQS vs SNS

**Finding and reaching services**
- [ ] **TR10** Service discovery
- [ ] **TR9** Service mesh
- [ ] **MS3** How do you make a call from one service to another? (Service mesh / sidecar)
- [ ] **MS1** What is an API Gateway? Difference between a load balancer, an API Gateway, and a reverse proxy
- [ ] **MS11** Reverse proxy vs forward proxy

- [ ] Say out loud, 2 min each: TR1 (your project), MS2, MS3, MS1

## Mon 23 Nov — Resilience
*7:30–10:30 pm · Know the three circuit-breaker states and one Resilience4j YAML block*

**Circuit breaker & Resilience4j**
- [ ] **MS4** What is a circuit breaker? Explain its annotations and fallback strategy.
- [ ] **TR5** Circuit breaker
- [ ] **TR6** Resilience4j: annotations, config, fallback
- [ ] **TR7** Retry: exponential backoff and jitter

**Scenarios**
- [ ] **DS2** One downstream service becomes slow and begins affecting the entire application. How would you prevent cascading failures?
- [ ] **DS4** Your API has a 1-second SLA, but one downstream service has a 2-second timeout. What design problems do you see?

- [ ] Say out loud, 2 min each: MS4 with Resilience4j, DS2

## Tue 24 Nov — Observability & performance
*7:30–10:30 pm · DS7: reuse the OOM answer from Mock 1 (heap dump, MAT / JProfiler, retained size)*

**Tracing & logging**
- [ ] **MS5** How do you trace and handle failures across microservices? (Distributed tracing via Zipkin, correlation ID)
- [ ] **TR13** Distributed tracing: Zipkin, unique ID, MDC
- [ ] **TR14** Distributed logging (Splunk, Datadog)

**Scenarios**
- [ ] **DS1** A request flows through seven microservices. How would you trace it end-to-end in production?
- [ ] **DS7** Your Spring Boot application has a memory leak in production but works perfectly in local environments. How would you troubleshoot it?

**Performance**
- [ ] **MS10** How would you design an API for low latency? What techniques would you use?

- [ ] Say out loud, 2 min each: MS5, DS1, MS10

## Wed 25 Nov — Data consistency
*7:30–10:30 pm · The core of the round: 2PC vs Saga, Outbox, idempotent consumers*

**Distributed transactions**
- [ ] **TR4** 2PC (two-phase commit)
- [ ] **TR3** Saga pattern: orchestrator vs choreography
- [ ] **DS3** A distributed transaction spans multiple microservices, and one service fails midway. How would you ensure data consistency?

**Keeping the DB and Kafka in sync**
- [ ] **TR11** Transactional outbox
- [ ] **KAFKA4** A database transaction succeeds, but the corresponding Kafka publish fails. How do you keep the database and Kafka consistent?

**Read/write split**
- [ ] **TR8** CQRS (Command Query Responsibility Segregation)

**Locks & scheduled jobs**
- [ ] **DS5** A distributed lock expires before the business operation completes. What problems can occur?
- [ ] **DS8** After scaling to multiple pods, a scheduled job executes on every instance. How would you prevent duplicate execution?

- [ ] Say out loud, 2 min each: 2PC vs Saga (DS3), Outbox (KAFKA4)

## Thu 26 Nov — REST API & Deployment
*7:30–10:30 pm · Write the idempotent POST in Spring Boot · If short on time, skip MS13 / MS17 / MS18*

**REST API**
- [ ] **REST1** GET, PUT, DELETE are idempotent: why?
- [ ] **REST2** POST is non-idempotent: why?
- [ ] **REST4** PATCH
- [ ] **REST5** Idempotent API
- [ ] **REST3** How to make a POST API idempotent (concept and coding)
- [ ] **DB4** The same payment request is processed twice due to client retries. How would you make the API idempotent?
- [ ] **MS12** Have you used Swagger?
- [ ] **MS16** GraphQL vs REST API — where is GraphQL beneficial?

**Deployment**
- [ ] **DEP1** What do you understand by CI/CD?
- [ ] **DEP2** Deployment strategies: canary, blue-green, rolling
- [ ] **MS6** Deployment strategies — canary vs blue-green
- [ ] **MS9** Basic understanding of CI/CD
- [ ] **MS7** HPA and Ingress in Kubernetes
- [ ] **MS8** Have you worked with AWS/GCP? How does deployment happen? (EKS, ECS)

**Cloud (answer from your GCP experience)**
- [ ] **MS13** What AWS services have you used?
- [ ] **MS17** Explain AWS Elastic Load Balancing — how is resiliency and fault tolerance managed, and what are its limitations?
- [ ] **MS18** How would you design a multi-service architecture in AWS?
- [ ] **DS6** Different geographic regions return different results for the same request. How would you investigate?

- [ ] Say out loud, 2 min each: REST3 (idempotent POST), DEP2 (canary vs blue-green vs rolling)

## Fri 27 Nov — Spring Boot revision: weak & partial first
*7:30–10:30 pm · Use the red / amber tags in the README · 2-minute answers*

**<font color="#dc2626">●</font> Weak**
- [ ] **SB31** Hibernate entity mappings (1:1, 1:N, N:1, M:N)
- [ ] **TX5** The @Async annotation
- [ ] **SEC6** How do you configure CORS in Spring Security?

**<font color="#d97706">●</font> Partial**
- [ ] **SB27** What is Hibernate?
  - [ ] The Hibernate session and its impact on concurrent systems
  - [ ] First-level vs second-level caching, and their effect on scalability and resiliency
- [ ] **SB28** What are the types of cascades in JPA?
- [ ] **SB32** What is the N+1 problem, and how do you fix it?
- [ ] **TX8** @Id and @GeneratedValue annotations
- [ ] **TX13** The @Modifying annotation
- [ ] **TX14** Access token vs ID token vs refresh token
- [ ] **SEC1** How did you implement authentication in your project?
- [ ] **SEC2** Authentication vs authorization
- [ ] **SEC4** Explain JWT, OAuth2, and OpenID Connect.
- [ ] **SEC5** How do you authenticate a user via OAuth2.0 for APIs? (WebSecurityConfigurerAdapter for authentication/authorization)

- [ ] Say out loud, 2 min each: SB31, TX5, SEC6

## Sat 28 Nov — Spring Boot core + full recap
*About 5 hours · Finish with a timed self-mock*

**Spring Boot core**
- [ ] **SB1** In Spring Boot, is DispatcherServlet used? How does it route a URL to a controller?
- [ ] **SB4** What is the @SpringBootApplication annotation?
- [ ] **SB5** What does @SpringBootApplication actually contain?
- [ ] **SB7** What is the bean lifecycle, and what bean scopes exist?
- [ ] **SB12** How do you handle exceptions? Explain global exception handling.
- [ ] **SB13** What is dependency injection?
- [ ] **SB19** How do you use different profiles in Spring, and how do you set the active profile?
- [ ] **SB21** What are Spring Boot starters, and why are they useful?
- [ ] **SB22** Difference between @Primary and @Qualifier
- [ ] **SB29** What is a circular dependency, and how do you resolve it? (lazy initialization or field injection)
- [ ] **TX1** The @Transactional annotation
- [ ] **TX2** Propagation and isolation levels in transactions

**Recap & self-mock**
- [ ] Microservices + REST + Deployment: one-page keyword recap from this sheet
- [ ] Timed self-mock: 10 random questions from this sheet, 2 minutes each, out loud

---
> If the mock falls **before** Saturday, move Friday's list to the evening before the mock and skip MS13 / MS17 / MS18.
