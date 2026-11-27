# Spring Boot — Revision Notes (Q&A)

← [Back to README](README.md) · [Java](java-notes.md) · [Spring Boot](spring-boot-notes.md) · [Microservices](microservices-notes.md)

Spring Boot core (SB1–SB36), Transactions & JPA annotations (TX1–TX14), Spring Data JPA & Hibernate, Spring Security (Q1–Q6).
Each entry: **Keywords** → **Model answer** → **Traps**. Tags: 🟢 Strong · 🟡 Partial · 🔴 Weak.

## Spring Boot & Spring Data JPA

### SB4 + SB5 — @SpringBootApplication and what it contains

**Keywords:** = @SpringBootConfiguration + @EnableAutoConfiguration + @ComponentScan · auto-config list in AutoConfiguration.imports (Boot 3; spring.factories in Boot 2) · each class guarded by @Conditional: OnClass, OnMissingBean (your bean wins), OnProperty · `--debug` = conditions report · exclude = ... · scan covers main package + sub-packages only

**Model answer:** "@SpringBootApplication combines three annotations. @SpringBootConfiguration is a specialised @Configuration, so the class can define beans. @ComponentScan scans the main class's package and its sub-packages for components, services, repositories and controllers. @EnableAutoConfiguration makes Boot configure beans from what's on the classpath: it loads the auto-configuration classes listed in the AutoConfiguration.imports file, and each one is guarded by conditions such as @ConditionalOnClass and @ConditionalOnMissingBean — for example a DataSource is created only if a JDBC driver is present and I haven't defined my own. I can see what was applied with the --debug conditions report and switch one off with `exclude`."

| Annotation | Does |
| --- | --- |
| @SpringBootConfiguration | Marks a @Configuration class (bean definitions) |
| @EnableAutoConfiguration | Classpath-driven auto-configuration via @Conditional |
| @ComponentScan | Scans main package + sub-packages |

**Trap:** a bean in a package outside the main class's package isn't scanned — keep the main class in the root package.

### SB1 — DispatcherServlet and request routing

**Keywords:** front controller, auto-configured by Boot, mapped to "/" · Tomcat → filters (Spring Security auth happens HERE, before DispatcherServlet) → DispatcherServlet → HandlerMapping (find method by URL + HTTP verb) → interceptors → HandlerAdapter (invoke, resolve @PathVariable / @RequestBody via Jackson) → HttpMessageConverter → JSON · exceptions → HandlerExceptionResolver → @RestControllerAdvice

**Model answer:** "Yes — Boot auto-configures a DispatcherServlet as the front controller. A request passes through embedded Tomcat and the servlet filter chain, where Spring Security authenticates it, then reaches the DispatcherServlet. It asks the HandlerMapping which controller method matches the URL and HTTP method, runs any interceptors, and the HandlerAdapter invokes the method — resolving path variables and deserialising the body with Jackson. Because it's a @RestController, the return value is written as JSON by an HttpMessageConverter. Any exception goes to a HandlerExceptionResolver, which is where @RestControllerAdvice plugs in."

```
Client → Tomcat → Filters (Security, CORS) → DispatcherServlet
   → HandlerMapping → Interceptor.preHandle → HandlerAdapter → @RestController method
   → HttpMessageConverter (Jackson) → JSON response
   (exception → HandlerExceptionResolver → @RestControllerAdvice)
```

**Trap:** authentication is not done by the DispatcherServlet — the Spring Security filter chain runs before it.

### SB7 + SB8 — Bean lifecycle and scopes

**Keywords:** instantiate → inject → Aware → BeanPostProcessor before → @PostConstruct / afterPropertiesSet / init-method → BeanPostProcessor after (proxies for @Transactional, @Async) → ready → shutdown: @PreDestroy / destroy / destroy-method · @PostConstruct = one-time setup on a service; @PreDestroy = cleanup at shutdown · entities are NOT beans (use @PrePersist) · scopes: singleton (default), prototype, request, session, application, websocket · singleton must be stateless · prototype in singleton → ObjectProvider / @Lookup

**Model answer:** "A bean is an object created and managed by the Spring container. Spring instantiates it, injects its dependencies, calls any Aware callbacks, runs BeanPostProcessors, then the initialisation callbacks — @PostConstruct, afterPropertiesSet, a custom init method — and post-processors run again, which is where proxies for @Transactional or @Async are created. The bean is then ready. At shutdown Spring calls @PreDestroy and the destroy methods. I use @PostConstruct for one-time setup on a service, like warming a cache, and @PreDestroy for cleanup like closing connections. The default scope is singleton — one instance per container, so it must be stateless. Prototype creates a new instance on every injection and Spring doesn't manage its destruction. Web applications add request, session, application and websocket scopes. Injecting a prototype into a singleton only happens once, so I use ObjectProvider when I need a fresh instance each time."

```
1. Instantiate (constructor)
2. Inject dependencies
3. Aware callbacks (BeanNameAware, ApplicationContextAware)
4. BeanPostProcessor.postProcessBeforeInitialization
5. @PostConstruct → afterPropertiesSet() → init-method
6. BeanPostProcessor.postProcessAfterInitialization   <- proxies created here
7. Ready
8. Shutdown: @PreDestroy → destroy() → destroy-method
```

| Scope | Instances |
| --- | --- |
| singleton (default) | One per container |
| prototype | New per injection / getBean; no @PreDestroy |
| request | One per HTTP request |
| session | One per HTTP session |
| application | One per ServletContext |
| websocket | One per WebSocket session |

**Traps:**

- @PostConstruct / @PreDestroy don't run on JPA entities — they aren't Spring beans. Use @PrePersist / @CreationTimestamp for createdAt.
- @PreDestroy runs at application shutdown, not when a record is deleted.
- Singletons are shared across requests: no mutable fields.

### SB9 + SB10 — @RestController vs @Controller

**Keywords:** @RestController = @Controller + @ResponseBody on every method · @Controller returns a view name → ViewResolver → HTML (Thymeleaf / JSP) · @RestController return value → HttpMessageConverter → JSON · switching only safe if methods return data; otherwise "home" is sent as literal text · @ResponseBody per method also works

**Model answer:** "@RestController is @Controller plus @ResponseBody on every method. With @Controller, a method normally returns a view name that a ViewResolver renders into an HTML page, such as a Thymeleaf template or JSP. With @RestController, the return value is written straight to the response body, serialised to JSON by an HttpMessageConverter. I can change a @Controller to @RestController only if its methods return data — if they return view names, the client would receive the literal string instead of the rendered page. For a mixed controller I keep @Controller and add @ResponseBody to the methods that return JSON."

**Trap:** changing the annotation on a page-rendering controller returns the text "home" instead of home.html.

### SB3 + SB11 + SB16 + SB20 — Spring vs Spring Boot; why Boot

**Keywords:** DI / IoC = core Spring Framework (not a Boot feature) · Boot = built ON Spring, not a replacement · Boot adds: auto-configuration, starters (curated compatible dependencies), embedded server + `java -jar`, opinionated defaults, application.properties/yml + profiles, Actuator · plain Spring: manual XML/Java config, DispatcherServlet setup, WAR on external Tomcat, version juggling · singletons created eagerly at startup (fail fast); @Lazy for on-demand

**Model answer:** "Spring Framework provides the core — the IoC container, dependency injection, Spring MVC, transactions, Spring Data. But a plain Spring application needs a lot of manual setup: configuring the DispatcherServlet, data sources and view resolvers, choosing compatible library versions, and deploying a WAR to an external Tomcat. Spring Boot is built on top of Spring to remove that work. Auto-configuration sets up beans based on the classpath, starters bring in a tested set of compatible dependencies, there's an embedded server so the app runs as a single jar, configuration lives in application.properties or YAML with profiles, and Actuator adds health checks and metrics. Legacy Spring still exists — Boot uses it underneath."

|  | Spring Framework | Spring Boot |
| --- | --- | --- |
| DI / IoC | Yes (this is where it comes from) | Uses Spring's |
| Configuration | Manual (XML / Java config) | Auto-configuration + properties |
| Dependencies | Choose versions yourself | Starters with managed versions |
| Server | Deploy WAR to external server | Embedded Tomcat, `java -jar` |
| Production features | Add yourself | Actuator |

**Traps:**

- Don't call DI Spring Boot's USP — it's core Spring.
- Singleton beans are created and stored at startup, not on demand (unless @Lazy).

### SB22 — @Primary vs @Qualifier (two beans of one type)

SB13 / SB36 (what DI is, types of DI) → see CJ32.

**Keywords:** two implementations of one interface → NoUniqueBeanDefinitionException at startup · @Qualifier("beanName") at the injection point = explicit choice · @Primary on one implementation = default · @Qualifier wins over @Primary · or inject the concrete class, or List / Map\<String, T> of all implementations for runtime choice (strategy pattern)

**Model answer:** "If two beans implement the same interface, like CarService and TruckService both implementing VehicleService, Spring can't decide which to inject and startup fails with NoUniqueBeanDefinitionException. @Primary marks one implementation as the default whenever that type is injected. @Qualifier, placed at the injection point with the bean name, selects a specific one and overrides @Primary. When the choice depends on the request, I inject a Map\<String, VehicleService> of all implementations and pick at runtime — the strategy pattern, which is how I'd select a payment processor."

```java
@Primary
@Service
class CarService implements VehicleService {}
@Service
class TruckService implements VehicleService {}

@RestController
class TruckController {
    TruckController(@Qualifier("truckService") VehicleService service) { }   // overrides @Primary
}

@Service
class Router {
    private final Map<String, VehicleService> services;   // all beans, keyed by name
    Router(Map<String, VehicleService> services) { this.services = services; }
}
```

|  | @Primary | @Qualifier |
| --- | --- | --- |
| Placed on | The bean class / @Bean method | The injection point (also on the bean) |
| Meaning | Default when ambiguous | Exactly this bean |
| Precedence | Lower | Wins |

**Trap:** Spring does not pick an implementation for you — with two candidates and no hint, the app fails to start.

### SB14 — POST vs PUT vs PATCH

**Keywords:** POST = create / action (login, pay), NOT idempotent, 201 + Location · PUT = replace whole resource, idempotent, missing fields become null · PATCH = partial update, not necessarily idempotent · GET, PUT, DELETE idempotent · idempotent = repeat gives same final state → safe to retry · POST retries: Idempotency-Key header · @PostMapping / @PutMapping / @PatchMapping

**Model answer:** "POST creates a resource or triggers an action like login or a payment; it isn't idempotent, so calling it twice can create two records, and it usually returns 201 Created with a Location header. PUT replaces the whole resource at a known URI — fields I omit are reset — and it's idempotent, so repeating it gives the same result. PATCH applies a partial update, changing only the fields sent, and isn't guaranteed to be idempotent. Idempotency matters for retries after timeouts: PUT and DELETE are safe to retry, while for POST, payment APIs accept an Idempotency-Key header so the server can return the original result instead of processing the request twice."

| Method | Purpose | Idempotent | Typical status |
| --- | --- | --- | --- |
| GET | Read | Yes | 200 |
| POST | Create / action | No | 201 + Location |
| PUT | Replace whole resource | Yes | 200 / 204 |
| PATCH | Partial update | Not guaranteed | 200 |
| DELETE | Delete | Yes | 204 |

**Trap:** PUT with only one field set wipes the others — use PATCH for partial updates.

### SB15 — @Service vs @Controller vs @Repository

**Keywords:** all are @Component specialisations, picked up by component scan · layers: controller (HTTP) → service (business logic, @Transactional) → repository (DB) · @Controller: needed for request-mapping detection · @Service: semantic only · @Repository: exception translation → DataAccessException · Spring Data JPA interfaces don't need @Repository

**Model answer:** "All three are specialisations of @Component, so component scanning registers them as beans, and they mark the layers of the application: controllers handle HTTP requests, services hold the business logic and transaction boundaries, and repositories talk to the database. Two of them add behaviour. Spring MVC only looks for request mappings in @Controller classes. @Repository enables exception translation, converting SQLException and JPA exceptions into Spring's DataAccessException hierarchy so the service layer isn't tied to a database vendor. @Service adds nothing technical — it documents intent. Spring Data JPA repository interfaces don't need @Repository, because Spring generates and registers their implementations."

| Annotation | Layer | Extra behaviour |
| --- | --- | --- |
| @Controller | Web | Request-mapping detection by HandlerMapping |
| @Service | Business logic | None (semantic, AOP-friendly) |
| @Repository | Data access | Exception translation to DataAccessException |

**Trap:** saying they're "only names" — @Repository and @Controller do add behaviour.

### SB12 + SB35 — Global exception handling; why one global handler

**Keywords:** error DTO per endpoint → duplication → one @RestControllerAdvice (= @ControllerAdvice + @ResponseBody) with @ExceptionHandler methods · most specific handler wins · map exceptions to HTTP status (404, 400, 409, 500) · MethodArgumentNotValidException → 400 with field errors · catch-all Exception → 500, log details, never leak stack traces · consistent body: timestamp, status, errorCode, message, path, traceId · Boot 3: ProblemDetail (RFC 7807/9457)

**Model answer:** "Rather than try/catch in every controller or an error DTO built in each endpoint, I use one @RestControllerAdvice class — @ControllerAdvice plus @ResponseBody — with @ExceptionHandler methods. Exceptions thrown anywhere in a controller are routed there, so every API returns the same error shape and the mapping lives in one place. I map each exception to the right status: a custom PaymentNotFoundException to 404, validation failures to 400 with the field errors, and a catch-all to 500 that logs the full exception but returns only a safe message, never a stack trace. The most specific handler wins. The body is consistent — status, error code, message, path and a trace ID — and in Boot 3 I can use the standard ProblemDetail format."

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<ErrorResponse> notFound(PaymentNotFoundException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse("PAYMENT_NOT_FOUND", ex.getMessage(), req.getRequestURI()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> invalid(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
            .map(f -> f.getField() + ": " + f.getDefaultMessage())
            .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest().body(new ErrorResponse("VALIDATION_FAILED", msg, req.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> other(Exception ex, HttpServletRequest req) {
        log.error("Unexpected error", ex);           // details to logs only
        return ResponseEntity.internalServerError()
            .body(new ErrorResponse("INTERNAL_ERROR", "Something went wrong", req.getRequestURI()));
    }
}
```

**Traps:**

- Returning 500 for every error — map business errors to 4xx.
- Leaking stack traces or SQL messages to clients is a security issue.

## Transactions, JPA annotations & tokens

### TX1 + TX2 + TX3 — @Transactional, propagation, isolation, read anomalies

From the Spring documentation; not run here (Maven unavailable).

**Keywords:** all-or-nothing · works via PROXY created at bean post-processing → begin, call, commit / rollback through the transaction manager; connection bound to the thread (ThreadLocal) · traps: self-invocation skips proxy, rollback only on unchecked by default (rollbackFor), public methods called from outside · propagation: REQUIRED default (join), REQUIRES\_NEW (suspend + separate — audit log), NESTED (savepoint), SUPPORTS, MANDATORY, NOT\_SUPPORTED, NEVER · isolation: READ\_UNCOMMITTED, READ\_COMMITTED (Postgres default), REPEATABLE\_READ (MySQL default), SERIALIZABLE · dirty = uncommitted data; non-repeatable = same row changes; phantom = new / missing rows

**Model answer:** "@Transactional makes a method all-or-nothing: if any step fails, everything rolls back — essential for something like a payment that debits, credits and records history. It works through a proxy: Spring wraps the bean at startup, and the proxy starts a transaction before the method, commits after it, and rolls back on an exception. The connection is bound to the current thread, so all repository calls inside share it. Because it's a proxy, a call from another method in the same class bypasses it; by default only unchecked exceptions trigger rollback, so I set rollbackFor for checked ones. Propagation decides what happens when a transaction already exists: REQUIRED, the default, joins it; REQUIRES\_NEW suspends it and runs separately — I'd use that for an audit record that must be saved even if the payment fails; NESTED uses a savepoint; MANDATORY requires an existing one. Isolation decides what concurrent transactions see: READ\_COMMITTED prevents dirty reads, REPEATABLE\_READ also prevents non-repeatable reads, SERIALIZABLE also prevents phantoms. Higher isolation is safer but means more locking."

| Propagation | Transaction exists | None exists | Use |
| --- | --- | --- | --- |
| REQUIRED (default) | Join | Create | Most service methods |
| REQUIRES\_NEW | Suspend, start separate | Create | Audit log that must survive rollback |
| NESTED | Savepoint | Create | Partial rollback |
| SUPPORTS | Join | Run without | Read helpers |
| MANDATORY | Join | Throw | Must be called inside a transaction |
| NOT\_SUPPORTED | Suspend | Run without | Non-transactional work |
| NEVER | Throw | Run without | Must never run in a transaction |

| Isolation | Dirty read | Non-repeatable read | Phantom read |
| --- | --- | --- | --- |
| READ\_UNCOMMITTED | Possible | Possible | Possible |
| READ\_COMMITTED (Postgres, Oracle default) | Prevented | Possible | Possible |
| REPEATABLE\_READ (MySQL InnoDB default) | Prevented | Prevented | Possible (InnoDB mostly prevents) |
| SERIALIZABLE | Prevented | Prevented | Prevented |

```java
@Service
class PaymentService {
    private final AuditService audit;
    @Transactional(rollbackFor = Exception.class)
    public void pay(PaymentRequest r) {
        debit(r); credit(r);
        audit.record(r);                 // other bean → proxy applies
    }
}

@Service
class AuditService {
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(PaymentRequest r) { /* saved even if pay() rolls back */ }
}
```

**Traps:**

- `this.method()` inside the same class bypasses the proxy — the annotation is ignored.
- A checked exception commits by default unless rollbackFor is set.
- Dirty = uncommitted data; non-repeatable = same row, different value; phantom = same query, different rows.

### TX1 extras — ACID; declarative vs programmatic transactions

**Keywords:** ACID = Atomicity (all or nothing), Consistency (valid state to valid state), Isolation (no in-progress interference), Durability (survives crash after commit) · declarative = @Transactional via proxy · programmatic = TransactionTemplate / PlatformTransactionManager, for partial or runtime-decided transactions · MANDATORY throws if NO transaction; NEVER throws if one EXISTS; SUPPORTS joins or runs without

| ACID | Meaning |
| --- | --- |
| Atomicity | All or nothing |
| Consistency | Valid state to valid state; constraints hold |
| Isolation | Concurrent transactions don't see each other's in-progress work |
| Durability | Committed data survives a crash |

```java
// programmatic
transactionTemplate.execute(status -> {
    debit(); credit();
    if (fraudDetected) status.setRollbackOnly();
    return null;
});
```

**Trap:** MANDATORY doesn't "join and throw" — it joins an existing transaction, or throws when there is none.

### SB17 — The @Configuration annotation

**Keywords:** marks a class as a source of bean definitions · @Bean methods return objects Spring registers as beans · used for library classes you can't annotate (RestTemplate, ObjectMapper, DataSource) or to choose an implementation · CGLIB proxy: a @Bean method calling another returns the same singleton (in a plain @Component it would create a new object)

**Model answer:** "@Configuration marks a class whose @Bean methods define beans: each method returns an object that Spring registers in the container. I use it for classes I can't annotate myself, like RestTemplate, ObjectMapper or a DataSource, and when I want to control exactly how a bean is built or which implementation of an interface is used. Spring proxies @Configuration classes, so if one @Bean method calls another, it gets the existing singleton rather than a new instance."

```java
@Configuration
public class AppConfig {
    @Bean
    public RestTemplate restTemplate() { return new RestTemplate(); }

    @Bean
    public VehicleService vehicleService() { return new CarService(); }
}
```

### SB18 — Loading resource / properties files (@PropertySource)

**Keywords:** application.properties / .yml load automatically · custom file → `@PropertySource("classpath:x.properties")` on a @Configuration class · read with `@Value("${key:default}")` · @PropertySource doesn't read YAML by default · groups of settings → @ConfigurationProperties(prefix) typed binding · file contents → `@Value("classpath:file.json") Resource`

**Model answer:** "Boot loads application.properties or application.yml automatically. For an extra properties file in src/main/resources I add @PropertySource with its classpath location on a configuration class, then read values with @Value using ${key}, optionally with a default after a colon. @PropertySource only handles .properties files out of the box, not YAML. For a group of related settings I prefer @ConfigurationProperties, which binds them into one typed, validated class. To read a file's contents, like a JSON file, I inject it as a Resource with @Value("classpath:...")."

```java
@Configuration
@PropertySource("classpath:payment.properties")
public class PaymentConfig {
    @Value("${payment.timeout:30}")
    private int timeout;
}

@ConfigurationProperties(prefix = "payment")
public record PaymentProperties(int timeout, String gatewayUrl) {}

@Value("classpath:countries.json")
private Resource countries;     // countries.getInputStream()
```

### SB19 — Profiles and setting the active profile

**Keywords:** profile = per-environment config (dev, test, prod) · application-{profile}.properties / .yml · base application.properties always loads; profile file overrides it · set active: spring.profiles.active in properties, `--spring.profiles.active=prod`, `-Dspring.profiles.active`, env var SPRING\_PROFILES\_ACTIVE (Kubernetes) · @Profile("dev") on classes or @Bean methods, "!dev" = not dev · tests: @ActiveProfiles

**Model answer:** "Profiles let one application run with different configuration per environment. I keep shared settings in application.properties and environment-specific ones in files like application-dev.properties or application-prod.yml; the active profile's file is loaded on top of the base file and overrides it. I set the active profile with spring.profiles.active — in the properties file for local development, or as a command-line argument, JVM property or the SPRING\_PROFILES\_ACTIVE environment variable in Kubernetes. @Profile on a class or @Bean method means that bean is only created when the profile is active, for example a mock payment gateway in dev and the real one everywhere else with "!dev". In tests I use @ActiveProfiles."

```bash
java -jar app.jar --spring.profiles.active=prod
SPRING_PROFILES_ACTIVE=prod      # environment variable, e.g. in a Kubernetes deployment
```

```java
@Configuration
public class PaymentGatewayConfig {
    @Bean
    @Profile("dev")
    PaymentGateway mockGateway() { return new MockGateway(); }
    @Bean
    @Profile("!dev")
    PaymentGateway realGateway() { return new CyberSourceGateway(); }
}
```

### SB21 — Spring Boot starters

**Keywords:** one dependency = curated, compatible set of libraries for a feature · versions managed by spring-boot-starter-parent / BOM → no version numbers · puts libraries on the classpath → triggers auto-configuration · starter-web brings MVC + Jackson + Tomcat (they're not separate starters) · official spring-boot-starter-\*, third-party \*-spring-boot-starter

**Model answer:** "A starter is a single dependency that brings in a tested, compatible set of libraries for one feature. spring-boot-starter-web, for example, pulls in Spring MVC, Jackson and embedded Tomcat. Their versions are managed by Spring Boot's parent POM, so I don't specify versions and don't get conflicts. Because they put libraries on the classpath, they also trigger auto-configuration — adding the JPA starter is enough for Boot to configure a DataSource and EntityManager. Common ones are web, data-jpa, security, test, actuator and validation."

| Starter | Brings |
| --- | --- |
| spring-boot-starter-web | Spring MVC, Jackson, embedded Tomcat |
| spring-boot-starter-data-jpa | Hibernate, Spring Data JPA, HikariCP |
| spring-boot-starter-security | Spring Security |
| spring-boot-starter-test | JUnit 5, Mockito, AssertJ, Spring Test |
| spring-boot-starter-actuator | Health checks, metrics |

**Trap:** Jackson and Tomcat aren't starters themselves — they come inside starter-web.

### SB23 — Multiple data sources

**Keywords:** auto-config handles ONE DataSource and backs off when you define your own (@ConditionalOnMissingBean) · separate property prefixes · one @Configuration per DB with 4 pieces: DataSource, EntityManagerFactory, TransactionManager, @EnableJpaRepositories(basePackages, entityManagerFactoryRef, transactionManagerRef) · separate packages for each DB's repositories and entities · @Primary on one set · @Transactional("auditTxManager") for the second DB

**Model answer:** "Spring Boot auto-configures a single DataSource, and it backs off once I define my own, so for two databases I configure both explicitly. Each gets its own property prefix, and its own configuration class defining a DataSource bound with @ConfigurationProperties, an EntityManagerFactory scanning that database's entity package, and a JpaTransactionManager. @EnableJpaRepositories on each class points a repository package at the matching entity manager and transaction manager, so repositories are split by package. I mark one set @Primary so unqualified injections work. For methods that write to the secondary database I name its transaction manager in @Transactional; otherwise those writes aren't covered by the transaction."

```properties
spring.datasource.payments.url=jdbc:postgresql://host1/payments
spring.datasource.audit.url=jdbc:mysql://host2/audit
```

```java
@Configuration
@EnableJpaRepositories(basePackages = "com.app.payments.repo",
    entityManagerFactoryRef = "paymentsEntityManager",
    transactionManagerRef = "paymentsTxManager")
public class PaymentsDbConfig {

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.payments")
    public DataSource paymentsDataSource() { return DataSourceBuilder.create().build(); }

    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean paymentsEntityManager(
            EntityManagerFactoryBuilder builder, @Qualifier("paymentsDataSource") DataSource ds) {
        return builder.dataSource(ds).packages("com.app.payments.entity").build();
    }

    @Bean
    @Primary
    public PlatformTransactionManager paymentsTxManager(
            @Qualifier("paymentsEntityManager") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}
// AuditDbConfig: same three beans without @Primary, pointing at the audit packages
```

**Trap:** a @Transactional method writing to the second DB must name its transaction manager, or it runs under the primary one.

### SB24 — @Value: reading properties, multi-line values and lists

**Keywords:** annotation is @Value (not @PropertyValue) · `@Value("${key}")`, default `${key:default}` · multi-line: properties → backslash at line end; YAML → `|` keeps line breaks · comma list → String\[\] directly, List via SpEL `#{'${key}'.split(',')}` · groups / lists → @ConfigurationProperties

**Model answer:** "@Value injects a property with ${key} syntax, optionally with a default after a colon. For a multi-line value, in a .properties file I end each line with a backslash, and in YAML I use the | block scalar, which keeps the line breaks; @Value reads both the same way. A comma-separated value can be injected straight into a String array, or into a List with a SpEL split. For lists or groups of related settings I prefer @ConfigurationProperties, which binds them into a typed class."

```properties
payment.welcome-message=Thank you for your payment. \
  Your transaction is being processed.
payment.currencies=INR,USD,EUR
```

```yaml
payment:
  welcome-message: |
    Thank you for your payment.
    Your transaction is being processed.
```

```java
@Value("${payment.timeout:30}")                  private int timeout;
@Value("${payment.welcome-message}")             private String welcome;
@Value("${payment.currencies}")                  private String[] currencies;
@Value("#{'${payment.currencies}'.split(',')}")  private List<String> currencyList;
```

**Trap:** the annotation is @Value; @PropertySource is for loading a file, not reading a value.

### SB25 (+ Security Q2, Q3) — Restricting API access by permission

**Keywords:** this is AUTHORIZATION (what you may do), not authentication (who you are) · 401 = not authenticated, 403 = authenticated but forbidden · URL rules in SecurityFilterChain: requestMatchers(...).hasRole("ADMIN") · method level: @EnableMethodSecurity + @PreAuthorize("hasRole('ADMIN')") · hasRole adds ROLE\_ prefix; hasAuthority = exact string (JWT scopes) · Spring Security 6 — no WebSecurityConfigurerAdapter

**Model answer:** "Restricting what a logged-in user can do is authorization; authentication is establishing who they are. An unauthenticated request gets 401, an authenticated user without permission gets 403. In Spring Security 6 I define a SecurityFilterChain bean and add URL rules, for example only users with the ADMIN role may call DELETE on /payments. For finer control I enable method security and put @PreAuthorize on methods, which can also use method arguments — like letting users read only their own payments. hasRole checks for an authority with the ROLE\_ prefix, while hasAuthority checks the exact value, which suits JWT scopes."

```java
@Bean
SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(auth -> auth
            .requestMatchers("/public/**").permitAll()
            .requestMatchers(HttpMethod.DELETE, "/payments/**").hasRole("ADMIN")
            .anyRequest().authenticated())
        .oauth2ResourceServer(oauth -> oauth.jwt(Customizer.withDefaults()));
    return http.build();
}

@EnableMethodSecurity
@Configuration
class MethodSecurityConfig {}

@DeleteMapping("/payments/{id}")
@PreAuthorize("hasRole('ADMIN')")
public void delete(@PathVariable Long id) { }

@PreAuthorize("#userId == authentication.name")
public List<Payment> paymentsOf(String userId) { return List.of(); }
```

|  | Authentication | Authorization |
| --- | --- | --- |
| Question | Who are you? | What may you do? |
| Example | Login, JWT validation | Only ADMIN can delete |
| Failure | 401 Unauthorized | 403 Forbidden |

**Trap:** calling role checks "authentication".

### SB29 — Circular dependency

**Keywords:** A needs B and B needs A (or A → B → C → A) · constructor injection → startup fails ("form a cycle") — good, fail fast · Boot 2.6+ blocks circular references by default (spring.main.allow-circular-references=true to allow) · fixes, best first: refactor shared logic into a third bean → events (ApplicationEventPublisher + @EventListener) → @Lazy on one constructor parameter (proxy) → setter/field injection as last resort · a cycle is a design smell

**Model answer:** "A circular dependency is when beans depend on each other directly or through a chain, like OrderService needing PaymentService and PaymentService needing OrderService. With constructor injection Spring can't create either one, so startup fails with a cycle error, and since Boot 2.6 cycles are rejected by default even with field or setter injection. I treat it as a design smell: the best fix is extracting the shared logic into a third bean both can depend on, or decoupling them with application events. As a quick fix, @Lazy on one constructor parameter makes Spring inject a proxy that resolves the real bean on first use. Switching to setter or field injection and allowing circular references hides the problem, so I'd only use it as a last resort."

```java
// quick fix
public OrderService(@Lazy PaymentService paymentService) { this.paymentService = paymentService; }

// real fix: OrderService ⇄ PaymentService  becomes  OrderService → PricingService ← PaymentService
```

**Trap:** recommending field injection as "the best way" contradicts constructor injection being preferred (CJ32).

### SB33 — Building a custom annotation in Spring (with AOP)

Java side (`@interface`, `@Retention`, `@Target`, validation annotations) → see CJ42.

**Keywords:** annotation with @Retention(RUNTIME) + @Target(METHOD) · behaviour comes from an @Aspect @Component with `@Around("@annotation(pkg.Name)")` · ProceedingJoinPoint.proceed() runs the real method · same proxy mechanism as @Transactional → self-invocation skips it · needs spring-boot-starter-aop · examples: @LogExecutionTime, @AuditLog, @RateLimited, @Retry

**Model answer:** "An annotation on its own is only metadata, so in Spring I pair it with an aspect. I declare the annotation with RUNTIME retention and a METHOD target, then write an @Aspect component with an @Around advice matching methods that carry it. The advice runs code before and after calling proceed() on the join point — for example timing the method and logging the duration. It works through the same proxies as @Transactional, so it needs the AOP starter, and a call from inside the same class bypasses it."

```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface LogExecutionTime {}

@Aspect
@Component
public class ExecutionTimeAspect {
    private static final Logger log = LoggerFactory.getLogger(ExecutionTimeAspect.class);

    @Around("@annotation(com.app.LogExecutionTime)")
    public Object logTime(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            return pjp.proceed();
        } finally {
            log.info("{} took {} ms", pjp.getSignature(), System.currentTimeMillis() - start);
        }
    }
}

@LogExecutionTime
public PaymentResponse pay(PaymentRequest r) { return null; }
```

**Traps:**

- Retention must be RUNTIME, not "scope".
- `this.pay()` inside the same bean skips the aspect.

### SB2 + SB6 — BeanFactory vs ApplicationContext

**Keywords:** both are IoC containers; ApplicationContext EXTENDS BeanFactory (superset) · BeanFactory: basic, lazy (creates on getBean), no automatic BeanPostProcessors → @Autowired / @Transactional / AOP don't work out of the box · ApplicationContext: eager singletons at startup (fail fast), auto-registers BeanPostProcessors, AOP, events (ApplicationEventPublisher), i18n (MessageSource), resource loading, Environment / profiles, all scopes · Boot always uses an ApplicationContext

**Model answer:** "Both are Spring IoC containers, and ApplicationContext extends BeanFactory, so it's a superset. BeanFactory is the basic container: it creates beans lazily when getBean is called and doesn't automatically register BeanPostProcessors, so annotation-driven features like @Autowired, @Transactional and AOP proxies don't work without manual setup. ApplicationContext creates singletons eagerly at startup, so wiring errors fail fast, registers the post-processors automatically, and adds event publishing, internationalisation, resource loading and environment and profile support. Spring Boot always runs on an ApplicationContext — a plain BeanFactory is rarely used directly today."

|  | BeanFactory | ApplicationContext |
| --- | --- | --- |
| Relationship | Base interface | Extends BeanFactory |
| Singleton creation | Lazy (on getBean) | Eager at startup (fail fast) |
| BeanPostProcessors | Register manually | Automatic (@Autowired, @Transactional, AOP work) |
| Events, i18n, Environment | No | Yes |
| Used in Boot | Underneath only | Yes |

**Trap:** "global session" scope was portlet-only; current web scopes are request, session, application, websocket.

## Spring Data JPA & Hibernate

### SB26 — JPA architecture — 🟡 Partial (✓ on 2nd try)

**Keywords:** JPA = spec · Hibernate = implementation · Spring Data JPA = repository layer · JDBC = driver · EntityManagerFactory → EntityManager → Persistence Context · entity states · dirty checking

**Model answer:** JPA (Jakarta Persistence API) is only a **specification**: annotations (`@Entity`, `@Id`, `@OneToMany`…) plus interfaces (`EntityManager`, `EntityManagerFactory`, JPQL). It has no code that talks to a DB. **Hibernate** is the **implementation** (the ORM provider) that turns entity operations into SQL. Hibernate itself sends that SQL through **JDBC** + the DB driver, using a pooled connection (HikariCP in Boot). **Spring Data JPA** sits on top: you write `interface UserRepo extends JpaRepository<User, Long>` and Spring generates the implementation (`SimpleJpaRepository`), which internally calls `EntityManager`.

| Layer | Role |
| --- | --- |
| Your code / `JpaRepository` | Spring Data JPA: CRUD, derived queries, paging |
| `EntityManager` | JPA API: persist / find / merge / remove / JPQL |
| Hibernate (`Session`) | ORM engine: SQL generation, dirty checking, caching |
| JDBC + HikariCP + driver | actual connection and SQL execution |
| Database | tables |

**EntityManagerFactory:** heavyweight, thread-safe, one per persistence unit (datasource); created at startup. **EntityManager:** lightweight, **not thread-safe**, one per transaction; Spring injects a shared proxy that binds the real one to the current transaction.

**Persistence context** = the first-level cache inside an EntityManager. It holds managed entities (one Java object per DB row per context). Changes to a managed entity are detected by **dirty checking** and flushed as UPDATE at commit, with no `save()` needed.

**Entity states:** Transient (`new`, unknown to JPA) → `persist()` → **Managed** → transaction closes / `detach()` → **Detached** → `merge()` → back to Managed copy; `remove()` → **Removed** (DELETE on flush).

**Traps:** `EntityManager` is an **interface**, not a class, and it comes from JPA, not Spring. ORM = Object-Relational **Mapping**. `merge()` returns a *new* managed copy; the object you passed stays detached.

**SB26 extras (say these too):** JPA is now **Jakarta Persistence** (Boot 3 → `jakarta.persistence.*`, not `javax`). DB portability comes from Hibernate's **Dialect** + writing **JPQL** against entities instead of native SQL; switching DB is mostly a config change, *unless* you used native queries / DB-specific features. Mapping is: class → table, object → row, field → column, association → foreign key. Spring Data JPA sits between **your code** and JPA (not "between Spring Boot and JPA").

**One-line flow to memorise:** `Repository → EntityManager (JPA) → Hibernate → JDBC → DB`.

### SB27 (+ TX6, TX11) — Hibernate Session, first-level & second-level cache, cache region — 🟡 Partial (L2 mixed up with flush)

**Keywords:** Session = Hibernate's EntityManager · L1 = persistence context, per session, always on · L2 = SessionFactory-wide, off by default, needs provider · lookup L1 → L2 → DB · region = named bucket · query cache separate · flush ≠ L2

**Session:** Hibernate's native API for one unit of work. JPA's `EntityManager` is implemented by Hibernate's `Session` (`em.unwrap(Session.class)` gives it). Lightweight, **not thread-safe**, one per transaction. Created by `SessionFactory` (= `EntityManagerFactory`), which is heavyweight, thread-safe, one per DB.

|  | First-level (L1) | Second-level (L2) |
| --- | --- | --- |
| What | the persistence context | shared cache of entity data |
| Scope | one Session / EntityManager (one transaction) | whole SessionFactory: all sessions, all threads |
| Default | **always on, cannot disable** | **off**; must enable |
| Stores | managed Java objects | "dehydrated" field values, not objects |
| Cleared by | `clear()`, `detach()`, session close | eviction / TTL / `cache.evict...` |
| Needs provider? | no | yes: Ehcache / Caffeine (via JCache), Hazelcast, Infinispan |

**Lookup order on `find(id)`:** L1 → L2 → DB; the result is then put into both. L1 also guarantees **one object per row** inside a transaction. Note: JPQL/derived queries always hit the DB (only `find`/lookup-by-id uses the caches), unless the **query cache** is on.

**Enable L2 (Spring Boot):**

```properties
spring.jpa.properties.hibernate.cache.use_second_level_cache=true
spring.jpa.properties.hibernate.cache.region.factory_class=jcache
spring.jpa.properties.jakarta.persistence.sharedCache.mode=ENABLE_SELECTIVE
# optional, separate: spring.jpa.properties.hibernate.cache.use_query_cache=true
```

```java
@Entity
@Cacheable                                                   // jakarta.persistence
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "products")
public class Product { ... }
```

Concurrency strategies: `READ_ONLY` (never changes, fastest), `NONSTRICT_READ_WRITE`, `READ_WRITE` (soft locks), `TRANSACTIONAL` (JTA). Good for read-mostly reference data (countries, products, config), bad for frequently updated data.

**Cache region (TX11):** a **named bucket inside the L2 cache** with its own settings (TTL, max size, eviction). By default one region per entity class (named by FQCN), one per collection, plus a query-cache region. Set with `@Cache(region = "...")` and configure TTL/size per region in the provider (e.g. ehcache.xml).

**Flush (what was confused with L2):** `flush()` sends the pending SQL from the persistence context to the DB (found by **dirty checking** against a snapshot). It does not commit. It happens automatically before commit and before a JPQL query (`FlushMode.AUTO`), or manually with `em.flush()`. This is **L1 behaviour, not L2**.

**Traps:** L2 is **not** "writing pending changes to the DB"; that's flush. L1 cannot be turned off. L2 is not on just because you added a dependency; you need the properties + `@Cacheable`. L2 does not cache query results; that's the separate query cache.

### SB28 — Cascading in JPA — 🟡 Partial (refresh backwards, ALL missing)

**Keywords:** propagate EntityManager operation parent → child · PERSIST, MERGE, REMOVE, REFRESH, DETACH, ALL · put on parent side · not DB `ON DELETE CASCADE` · never REMOVE on ManyToMany/ManyToOne · vs orphanRemoval

**Model answer:** Cascade means: when an `EntityManager` operation is applied to the **parent**, apply the same operation to the associated **child** entities. It's declared on the parent side of the association: `@OneToMany(mappedBy = "student", cascade = CascadeType.ALL)`. It is JPA-level (Hibernate issues the extra SQL); it is **not** the database's `ON DELETE CASCADE`.

| Type | When parent is… | Child gets… | Example |
| --- | --- | --- | --- |
| `PERSIST` | `persist()`ed (new) | inserted too | save Student with new Addresses in one call |
| `MERGE` | `merge()`d (detached → managed) | its state merged too | REST PUT: update Order + its OrderLines from a DTO |
| `REMOVE` | `remove()`d | deleted too | delete Order → delete its OrderLines |
| `REFRESH` | `refresh()`ed | **reloaded from DB** too (unflushed changes discarded) | re-read Order + lines after a DB trigger changed them |
| `DETACH` | `detach()`ed | detached too (no longer tracked) | hand a whole object graph to another layer without auto-updates |
| `ALL` | any of the above | all of the above | strong ownership/composition (Order–OrderLine) |

**Without cascade:** saving a Student with a new Address in it throws `TransientPropertyValueException` ("object references an unsaved transient instance"), or the child is just ignored, depending on the mapping; you'd have to save the Address yourself.

**Traps:** `refresh` goes **DB → memory** (overwrites the entity with DB values); it never writes to the DB. Don't use `REMOVE`/`ALL` on `@ManyToMany` or `@ManyToOne`: deleting one Student would delete a shared Course/Department. `CascadeType.REMOVE` deletes children only when the **parent** is deleted; removing a child from the collection needs `orphanRemoval = true` (TX10). Use `ALL` only for true parent-owns-child relationships.

### SB30 — Composite primary key — 🟢 Strong

**Keywords:** `@EmbeddedId` + `@Embeddable` · `@IdClass` + multiple `@Id` · key class is NOT an entity · Serializable + no-arg ctor + equals/hashCode · `JpaRepository<E, KeyClass>` · `@MapsId`

**Model answer:** When one column can't uniquely identify a row, the PK is a combination of columns. JPA needs a **separate key class** (not an entity) in both approaches:

**1. `@EmbeddedId` (preferred):** key fields live only in the key class; the entity holds one key object.

```java
@Embeddable
public class EnrollmentId implements Serializable {
    private Long studentId;
    private Long courseId;
    // no-arg ctor, equals(), hashCode()
}

@Entity
public class Enrollment {
    @EmbeddedId private EnrollmentId id;
    private LocalDate enrolledOn;
}
// query: e.id.studentId
```

**2. `@IdClass`:** key fields are repeated in the entity, each marked `@Id`; the key class must have the **same field names and types**.

```java
public class EnrollmentId implements Serializable { Long studentId; Long courseId; /* equals/hashCode */ }

@Entity
@IdClass(EnrollmentId.class)
public class Enrollment {
    @Id private Long studentId;
    @Id private Long courseId;
}
// query: e.studentId (flatter)
```

**Key class rules (both):** `implements Serializable`, public no-arg constructor, **`equals()` + `hashCode()`** over all key fields (the persistence context and L2 cache look entities up by id), and not annotated `@Entity`.

**Repository:** `interface EnrollmentRepo extends JpaRepository<Enrollment, EnrollmentId>`.

**Which to prefer:** `@EmbeddedId`: cleaner, single id object, no duplicated fields, reusable. `@IdClass`: flatter queries, handy for legacy schemas. If part of the key is also a FK, use `@MapsId("studentId") @ManyToOne Student student;`.

**Traps:** the key class is **not** an entity. Missing `equals/hashCode` causes duplicate/missed lookups. With `@IdClass` the field names must match exactly. `@GeneratedValue` is not used on composite key parts.

### SB31 — JPA relationship mappings — 🔴 Weak (owning side / mappedBy / fetch defaults)

**Keywords:** owning side = has the FK (`@JoinColumn`) · inverse side = `mappedBy` · ToOne = EAGER, ToMany = LAZY by default · ManyToOne owns · ManyToMany → `@JoinTable` · sync both sides with helper methods · make everything LAZY

**Model answer:** Four annotations: `@OneToOne`, `@OneToMany`, `@ManyToOne`, `@ManyToMany`. Each can be **unidirectional** (only one entity has the field) or **bidirectional** (both do).

**Owning side vs `mappedBy`:** in a bidirectional relationship only **one** side controls the FK column; that's the **owning side** (it has `@JoinColumn` / `@JoinTable`). The other side says `mappedBy = "<field name on owner>"` and is **read-only for the FK**: changing only the inverse collection writes nothing to the DB.

| Mapping | Example | Owning side / FK | Default fetch |
| --- | --- | --- | --- |
| `@OneToOne` | User – Profile | side with `@JoinColumn(name="user_id")` (or `@MapsId` to share PK) | **EAGER** |
| `@ManyToOne` | many Orders → one Customer | **always the owner**, FK in orders table | **EAGER** |
| `@OneToMany` | Customer → Orders | inverse: `mappedBy = "customer"` | **LAZY** |
| `@ManyToMany` | Student – Course | one side with `@JoinTable(student_course)`, other side `mappedBy` | **LAZY** |

```java
@Entity
class Order {
    @ManyToOne(fetch = FetchType.LAZY)          // override EAGER default
    @JoinColumn(name = "customer_id")
    private Customer customer;                   // OWNER
}
@Entity 
class Customer {
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Order> orders = new ArrayList<>();   // INVERSE

    public void addOrder(Order o) { orders.add(o); o.setCustomer(this); }   // keep both sides in sync
    public void removeOrder(Order o) { orders.remove(o); o.setCustomer(null); }
}
```

**Best practices:** make every association `LAZY` (change the EAGER ToOne defaults), fetch what you need with `JOIN FETCH` / `@EntityGraph` (see N+1). Prefer a unidirectional `@ManyToOne`; add the `@OneToMany` side only if you navigate it. Use `Set` for `@ManyToMany`. If the link table needs extra columns (enrolledOn, grade), replace `@ManyToMany` with a **join entity** (two `@ManyToOne`s + composite key, see SB30).

**Traps:** bidirectional is *not* "recommended for error handling"; it's for navigation, and it costs you keeping both sides in sync. A unidirectional `@OneToMany` without `@JoinColumn` makes Hibernate create an extra join table. Setting only `customer.getOrders().add(o)` without `o.setCustomer(customer)` saves a NULL FK. Bidirectional + `toString()`/Jackson causes infinite recursion (`@JsonIgnore` / DTOs).

**SB31 examples (yours, refined):** OneToOne: Person – Passport / User – Profile (phone number is weak: real users have many phones). ManyToOne: many Users → one joint BankAccount (owner = User, FK `account_id`). OneToMany: Course → Subjects (`mappedBy = "course"` on Course). ManyToMany: Students ↔ Courses via `student_course` join table; if you need `enrolledOn`/`grade`, make it an `Enrollment` entity.

### SB32 / TX12 — N+1 select problem — 🟡 Partial (1+N mechanism, detection missing)

**Keywords:** 1 query for parents + N lazy queries for children · detect: SQL log / statistics / query-count test · fix: `JOIN FETCH`, `@EntityGraph`, batch fetching, DTO projection · EAGER does NOT fix it

**Model answer:** You run **1** query to load N parents (`findAll()` → 100 students), then accessing a lazy association on each one (`s.getAddress()` in a loop / during JSON serialization) fires **one more query per parent** → 1 + N = 101 round-trips instead of 1. It's slow because of DB round-trips, not data volume.

```sql
select * from student;                       -- 1
select * from address where student_id = 1;  -- N times
select * from address where student_id = 2;
...
```

**Detect:** turn on SQL logging (`logging.level.org.hibernate.SQL=DEBUG` or `spring.jpa.show-sql=true`) and look for the repeated select; `spring.jpa.properties.hibernate.generate_statistics=true` (prints query count per session); assert query counts in tests (datasource-proxy / Hypersistence Utils); APM traces in prod.

**Fix:**

| Fix | How | Note |
| --- | --- | --- |
| `JOIN FETCH` | `@Query("select s from Student s join fetch s.address")` | one query; use `distinct` for collections |
| `@EntityGraph` | `@EntityGraph(attributePaths = "address") List<Student> findAll();` | same effect, no JPQL; declarative |
| Batch fetching | `@BatchSize(size = 50)` or `hibernate.default_batch_fetch_size=50` | 1 + N/50 queries using `IN (...)`; works with pagination |
| DTO projection | `select new com.x.StudentDto(s.name, a.city) from Student s join s.address a` | fetch only needed columns, read-only |

**Traps:** switching to `FetchType.EAGER` does **not** fix it: `findAll()`/JPQL still loads parents first, then fires N eager selects, and now on every query. `JOIN FETCH` a collection + pagination → Hibernate paginates **in memory** (warning HHH90003004); use batch fetching or two queries (page IDs, then fetch). Fetching two `List` collections at once → `MultipleBagFetchException` (use `Set` or separate queries). It also happens with `@ManyToOne` (EAGER by default).

### SB34 — Event streaming with Kafka in Spring Boot (vs Spring application events) — ⚪ Skipped (not in this interview)

**Keywords:** Spring events = in-JVM · Kafka = cross-service, durable log · `KafkaTemplate` + `@KafkaListener` · key → partition → ordering · consumer group: 1 partition → 1 consumer · at-least-once → idempotent consumer · retry + DLT · outbox

**Spring application events (what was answered):** `ApplicationEventPublisher.publishEvent(new OrderCreatedEvent(...))` + `@EventListener void on(OrderCreatedEvent e)`. Works, but **inside one JVM only**: synchronous by default (`@Async` to decouple), not durable (lost on crash), no replay, other services can't consume. `@TransactionalEventListener(phase = AFTER_COMMIT)` fires only after the DB commit. Use for decoupling modules within a service.

**Kafka (what the question wants):** a distributed, durable, replayable **log**. Producers write to a **topic**; a topic is split into **partitions** (unit of parallelism + ordering). Messages with the **same key go to the same partition → ordered per key** (e.g. key = paymentId).

```java
// Producer
@Service
@RequiredArgsConstructor
class PaymentEventProducer {
    private final KafkaTemplate<String, PaymentEvent> kafka;
    void publish(PaymentEvent e) {
        kafka.send("payment-events", e.paymentId(), e);   // key = paymentId → ordering per payment
    }
}

// Consumer
@Component
class PaymentEventConsumer {
    @KafkaListener(topics = "payment-events", groupId = "settlement-service", concurrency = "3")
    void on(PaymentEvent e) { if (alreadyProcessed(e.eventId())) return; settle(e); }
}
```

```yaml
spring.kafka:
  bootstrap-servers: localhost:9092
  producer:
    acks: all
    properties.enable.idempotence: true
    value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
  consumer:
    auto-offset-reset: earliest
    enable-auto-commit: false
    value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
```

**Consumer groups:** within one group, each partition is read by **exactly one** consumer → parallelism ≤ number of partitions (extra consumers sit idle). **Different groups each get every message** (pub-sub): settlement-service and notification-service both read payment-events. Kafka tracks progress via committed **offsets** per group; rebalance happens when consumers join/leave.

**Failure handling:**

- Producer: `acks=all`, `enable.idempotence=true`, retries → no loss, no duplicates from retries.
- Consumer: delivery is **at-least-once** → make processing **idempotent** (dedupe by eventId / unique constraint).
- Retries + DLT: `DefaultErrorHandler(new DeadLetterPublishingRecoverer(template), new ExponentialBackOff())` → after N retries send to `payment-events.DLT`; or `@RetryableTopic` for non-blocking retry topics.
- Commit the offset only after successful processing (manual ack / `AckMode.RECORD`).
- DB write + publish atomically: **transactional outbox** (write the event to an outbox table in the same DB transaction; a relay/Debezium publishes it).
- Schema evolution: Avro/Protobuf + Schema Registry.

**Traps:** `@EventListener` is not event streaming across microservices. Ordering is only guaranteed **within a partition**, not across a topic. More consumers than partitions gives no extra throughput. "Exactly-once" in Kafka only covers Kafka-to-Kafka (transactions); with external side effects you still need idempotency.

### TX4 — Shared vs exclusive lock — 🟢 Strong

**Keywords:** S = read, many holders · X = write, one holder · S+S ok, S+X / X+X block · held till commit · `FOR SHARE` / `FOR UPDATE` · `PESSIMISTIC_READ` / `PESSIMISTIC_WRITE` · MVCC reads take no lock

**Model answer:** A **shared (S / read) lock** lets many transactions read the same row at once but blocks anyone from writing it. An **exclusive (X / write) lock** is held by one transaction only; nobody else can take an S or X lock on that row until it commits/rolls back. Locks are held till the transaction ends, so other transactions **wait** (serialized, not parallel).

| Held \\ Requested | Shared | Exclusive |
| --- | --- | --- |
| **Shared** | ✓ allowed | ✗ waits |
| **Exclusive** | ✗ waits | ✗ waits |

**SQL:** `SELECT ... FOR SHARE` (MySQL: `LOCK IN SHARE MODE`) = shared; `SELECT ... FOR UPDATE` and every `UPDATE`/`DELETE` = exclusive.

**JPA:**

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)        // SELECT ... FOR UPDATE  (exclusive)
@Query("select a from Account a where a.id = :id")
Optional<Account> findForUpdate(Long id);
// PESSIMISTIC_READ → shared lock
// Optimistic alternative: @Version field → no DB lock, OptimisticLockException on conflict
```

**Use:** exclusive lock for read-modify-write on money (debit account balance) so two transactions can't both read 100 and both write 50.

**Traps:** in MVCC databases (PostgreSQL, MySQL InnoDB) a **plain SELECT takes no shared lock**; it reads a snapshot. Locks only apply when you ask (`FOR SHARE`/`FOR UPDATE`) or write. Two transactions locking rows in opposite order → **deadlock** (DB kills one); lock in a consistent order and keep transactions short. Pessimistic = lock first; optimistic (`@Version`) = check at commit, better for low contention.

### TX5 — `@Async` — 🔴 Weak → 🟡 Partial on 2nd try (async ≠ non-blocking, not Mono/Flux)

**Keywords:** runs method on another thread from a TaskExecutor · `@EnableAsync` · returns `void` or `CompletableFuture<T>` · proxy → no self-invocation, public · configure own executor · transaction & ThreadLocal don't propagate · NOT Mono/Flux

**Model answer:** `@Async` makes Spring run the method on a **different thread** from a `TaskExecutor`, so the caller returns immediately. Enable with `@EnableAsync` on a config class. Return type: `void` (fire-and-forget) or `CompletableFuture<T>` (caller can join/combine results).

```java
@Configuration
@EnableAsync
class AsyncConfig {
    @Bean(name = "ioExecutor")
    Executor ioExecutor() {
        var ex = new ThreadPoolTaskExecutor();
        ex.setCorePoolSize(10); ex.setMaxPoolSize(20); ex.setQueueCapacity(500);
        ex.setThreadNamePrefix("io-"); ex.initialize();
        return ex;
    }
}

@Service
class PriceClient {
    @Async("ioExecutor")
    public CompletableFuture<Price> fetchPrice(String sku) {
        return CompletableFuture.completedFuture(restClient.get(sku));
    }
}
// caller: run 3 calls in parallel
var a = client.fetchPrice("A"); var b = client.fetchPrice("B");
CompletableFuture.allOf(a, b).join();
```

**Use cases:** send email/notification, audit logging, calling several slow external APIs in parallel.

**Exceptions:** `void` methods → handled by `AsyncUncaughtExceptionHandler` (otherwise just logged); `CompletableFuture` → handle with `exceptionally()` / `handle()`.

**Traps:** `@Async` is **not** reactive. `Mono`/`Flux` are Project Reactor (WebFlux), a different, non-blocking model; `@Async` just moves blocking code to another thread. It doesn't return `Optional`. It's proxy-based like `@Transactional`: **self-invocation doesn't work**, method must be public. The new thread has **no transaction** from the caller (`@Transactional` context doesn't cross threads) and no `SecurityContext`/MDC/ThreadLocal unless you add a `TaskDecorator`. Boot's default executor has an unbounded queue; define your own for production.

**TX5 extra — asynchronous ≠ non-blocking (say this precisely):** `@Async` is **asynchronous for the caller** (caller thread returns at once) but **still blocking**: the pool thread sits blocked while waiting for the other service's response; you just moved the wait to another thread. **Non-blocking** (WebFlux `WebClient` + Mono/Flux, Vert.x event loop) means **no thread is held** while waiting for I/O; a few event-loop threads serve thousands of requests. One-liner: *"@Async moves blocking work to a thread pool; reactive/Vert.x avoids blocking threads at all."* `@EnableAsync` goes on a `@Configuration`/main class, `@Async` on public methods of another bean.

### TX7 — `@Table` and `@Column` — 🟢 Strong

**Keywords:** `jakarta.persistence` · map class → table, field → column · default name: Boot converts camelCase → snake\_case · `nullable`/`unique`/`length` only affect DDL · `@Transient` to skip a field · Bean Validation for runtime checks

**Model answer:** Both are JPA annotations (`jakarta.persistence.*`; Jakarta EE is the renamed Java EE *specification*). `@Table` maps the entity class to a specific table; `@Column` maps a field to a specific column. Both are optional: without them the names are derived from the class/field name.

```java
@Entity
@Table(name = "customers", schema = "sales",
       uniqueConstraints = @UniqueConstraint(columnNames = {"email"}),
       indexes = @Index(name = "idx_customer_city", columnList = "city"))
public class Customer {
    @Id
    @GeneratedValue
    Long id;

    @Column(name = "email_address", nullable = false, unique = true, length = 100)
    private String email;

    @Column(precision = 12, scale = 2)       // BigDecimal money
    private BigDecimal creditLimit;

    @Column(updatable = false)                // set once on insert
    private Instant createdAt;

    @Transient                                // not stored at all
    private String displayName;
}
```

| `@Table` attrs | `@Column` attrs |
| --- | --- |
| `name`, `schema`, `catalog`, `uniqueConstraints`, `indexes` | `name`, `nullable`, `unique`, `length` (default 255), `precision`/`scale`, `insertable`/`updatable`, `columnDefinition` |

**If omitted:** in **Spring Boot** the default naming strategy (`CamelCaseToUnderscoresNamingStrategy`) converts names to snake\_case: class `OrderItem` → table `order_item`, field `firstName` → column `first_name`. (Plain Hibernate without Boot keeps the name as-is.)

**Traps:** `nullable`, `unique`, `length` only influence **schema generation** (`ddl-auto`); they don't validate data at runtime; use Bean Validation (`@NotNull`, `@Size`, `@Email`) for that. Every non-static, non-transient field is persisted by default; mark helpers `@Transient`. Enums: use `@Enumerated(EnumType.STRING)`, since the default ORDINAL breaks when you reorder constants.

### TX8 — `@Id` and `@GeneratedValue` — 🟡 Partial (strategy names, MySQL vs Postgres)

**Keywords:** `@Id` = primary key (mandatory) · strategies AUTO, IDENTITY, SEQUENCE, TABLE, UUID · MySQL → IDENTITY · PostgreSQL/Oracle → SEQUENCE + allocationSize · IDENTITY disables batch inserts · avoid AUTO & TABLE

**Model answer:** `@Id` marks the field that is the entity's **primary key**; every entity must have one (or `@EmbeddedId`/`@IdClass`, see SB30). `@GeneratedValue(strategy = ...)` tells JPA the value is generated rather than set by you.

| Strategy | How the id is produced | Use with |
| --- | --- | --- |
| `IDENTITY` | DB auto-increment column (`AUTO_INCREMENT`, `SERIAL`/identity) | **MySQL** |
| `SEQUENCE` | DB sequence object; Hibernate pre-fetches ids in blocks (`allocationSize`) | **PostgreSQL**, Oracle |
| `TABLE` | a separate table simulates a sequence (row lock per increment) | avoid: slow |
| `AUTO` (default) | provider chooses; Hibernate 6 → SEQUENCE, on MySQL falls back to a `hibernate_sequence` table | avoid: be explicit |
| `UUID` (JPA 3.1) | generated in the app, no DB round-trip | distributed systems, ids exposed in URLs |

```java
// MySQL
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;

// PostgreSQL
@Id
@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order_seq")
@SequenceGenerator(name = "order_seq", sequenceName = "order_seq", allocationSize = 50)
private Long id;

// UUID
@Id
@GeneratedValue(strategy = GenerationType.UUID)
private UUID id;
```

**Why SEQUENCE is better when available:** Hibernate gets the id *before* the INSERT and can reserve 50 ids per DB call, so it can **batch inserts** (`hibernate.jdbc.batch_size`). With `IDENTITY` the id is only known *after* the INSERT, so Hibernate must insert immediately on `persist()` and **JDBC batching is disabled**.

**Traps:** there is no "random" strategy; it's `UUID`. Don't rely on `AUTO` (it creates a `hibernate_sequence` table on MySQL). `allocationSize` must match the DB sequence's `INCREMENT BY`, or you get duplicate/gapped ids. Random UUIDv4 primary keys fragment B-tree indexes; prefer time-ordered UUIDs (v7) or keep a Long PK plus a public UUID column. Use wrapper `Long`, not `long`: `null` means "new entity".

### TX9 — `@JoinColumn` / `@JoinColumns` — 🟢 Strong

**Keywords:** names the FK column · on the owning side (`@ManyToOne`, `@OneToOne`) · `name`, `referencedColumnName` (default = PK), `nullable`, `foreignKey` · default name `field_pk` · `@JoinColumns` = composite FK to a composite PK · ManyToMany uses `@JoinTable`

**Model answer:** `@JoinColumn` defines the **foreign-key column** for an association. It goes on the **owning side**, normally `@ManyToOne` or `@OneToOne` (the side whose table holds the FK). Without it the default name is `<field>_<referenced PK>`, e.g. `customer_id`.

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "customer_id",              // FK column in orders table
            referencedColumnName = "id",       // default: target's PK
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_order_customer"))
private Customer customer;
```

`referencedColumnName` is only needed when the FK points to a **non-PK unique column** (e.g. `customer_code`).

**`@JoinColumns`:** when the target entity has a **composite primary key** (SB30), the FK is also several columns, so you list one `@JoinColumn` per column, **each with its own** `referencedColumnName`:

```java
@ManyToOne
@JoinColumns({
    @JoinColumn(name = "student_id", referencedColumnName = "student_id"),
    @JoinColumn(name = "course_id",  referencedColumnName = "course_id")
})
private Enrollment enrollment;
// Java 8+: repeatable, so two @JoinColumn annotations directly also works
```

**ManyToMany:** uses `@JoinTable`, which contains `joinColumns` (FK to this entity) and `inverseJoinColumns` (FK to the other):

```java
@ManyToMany
@JoinTable(name = "student_course",
           joinColumns = @JoinColumn(name = "student_id"),
           inverseJoinColumns = @JoinColumn(name = "course_id"))
private Set<Course> courses;
```

**Traps:** `@JoinColumn` goes on the owner, never on the `mappedBy` side. A `@OneToMany` + `@JoinColumn` (unidirectional) puts the FK in the *child* table and Hibernate updates it with extra UPDATE statements. `insertable = false, updatable = false` makes a read-only duplicate mapping of the same FK column.

### TX10 — `orphanRemoval` vs `CascadeType.REMOVE` — 🟢 Strong

**Keywords:** orphan = child no longer referenced by its parent · removed from collection / parent set to null → DELETE · REMOVE only on parent delete · orphanRemoval covers both · only `@OneToMany`/`@OneToOne` · exclusive ownership · don't replace the collection

**Model answer:** `orphanRemoval = true` deletes a child row as soon as it is **disconnected from its parent**: removed from the parent's collection, or the `@OneToOne` reference set to `null`. `CascadeType.REMOVE` deletes children **only when the parent itself is deleted**.

| Action | `CascadeType.REMOVE` | `orphanRemoval = true` |
| --- | --- | --- |
| `em.remove(order)` (delete parent) | children deleted | children deleted |
| `order.getLines().remove(line)` | nothing; row stays (FK may be set null) | **line deleted** |
| `order.setLines(null)` / `profile = null` | nothing | child deleted |

```java
@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
private List<OrderLine> lines = new ArrayList<>();

public void removeLine(OrderLine l) { lines.remove(l); l.setOrder(null); }   // → DELETE order_line
```

**Use when:** the child has no meaning without that parent (OrderLine, Address of a User, Comment of a Post); that's composition.

**Traps:** "remove from the collection" is not JPA `detach()`; detach just stops tracking and deletes nothing. Only valid on `@OneToMany` and `@OneToOne`. You can't move a child to another parent: removing it from the first collection deletes it. Never replace the collection (`setLines(new ArrayList<>())`): Hibernate throws "A collection with cascade=all-delete-orphan was no longer referenced"; use `lines.clear()` + `addAll()`. Don't use it when children are shared or must be kept for audit.

### TX11 extra — cache region in **Spring Cache** terms (`@Cacheable`) — 🟡 Partial

**Keywords:** cache name = logical bucket · `@EnableCaching` · `@Cacheable` / `@CachePut` / `@CacheEvict` · key = method args (SpEL) · provider: Caffeine / Redis / Hazelcast · TTL per cache name · proxy → no self-invocation

**Model answer:** In Spring's cache abstraction a "region" is the **cache name**: a named logical bucket ("products", "customers") inside the cache store, each with its own TTL/size config in the provider. Enable with `@EnableCaching`; Spring Boot auto-configures a `CacheManager` for the provider on the classpath (Caffeine, Redis, Hazelcast…; default is an in-memory ConcurrentHashMap).

```java
@Service
class ProductService {
    @Cacheable(cacheNames = "products", key = "#id")        // miss → run method, store; hit → skip method
    public Product find(Long id) { return repo.findById(id).orElseThrow(); }

    @CachePut(cacheNames = "products", key = "#p.id")       // always run, refresh cache
    public Product update(Product p) { return repo.save(p); }

    @CacheEvict(cacheNames = "products", key = "#id")       // remove entry (allEntries = true clears bucket)
    public void delete(Long id) { repo.deleteById(id); }
}
```

**Spring Cache vs Hibernate L2 region:** Spring Cache caches **method return values** in the service layer (any object, any source: DB, REST call). Hibernate L2 caches **entity data** inside JPA, transparently for `find(id)`. Different layers; both use named regions/buckets.

**Traps:** `@Cacheable` is proxy-based: self-invocation bypasses the cache. Without `@EnableCaching` the annotations do nothing. The default simple cache has no TTL/eviction; use Caffeine/Redis in production. Don't put it on the *repository* method you also update without `@CacheEvict`/`@CachePut`, or you serve stale data.

### TX13 — `@Modifying` — 🟡 Partial (said AOP; @Transactional missing)

**Keywords:** Spring Data JPA (not AOP) · on `@Query` that does UPDATE/DELETE (or native INSERT) · needs `@Transactional` · returns `int` rows affected · bulk query bypasses persistence context → `clearAutomatically` / `flushAutomatically`

**Model answer:** `@Query` (Spring Data JPA) lets you write your own **JPQL** or native SQL (`nativeQuery = true`) on a repository method. By default Spring treats it as a SELECT. For an **UPDATE / DELETE** (or native INSERT) you must add `@Modifying`, so Spring calls `executeUpdate()` instead of `getResultList()`. Without it: `InvalidDataAccessApiUsageException` ("Not supported for DML operations" / "Query executed via getResultList…").

```java
public interface AccountRepo extends JpaRepository<Account, Long> {

    @Transactional                                            // required
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Account a set a.status = :status where a.lastLogin < :cutoff")
    int deactivateInactive(@Param("status") Status status, @Param("cutoff") Instant cutoff);
}
```

**Also needed:** `@Transactional`: repository methods are read-only by default (`SimpleJpaRepository` is `@Transactional(readOnly = true)`), and a DML query needs a read-write transaction (else `TransactionRequiredException`). Usually put it on the service method that calls the repo. Return type `int`/`void` (number of rows changed).

**`clearAutomatically` / `flushAutomatically`:** a bulk JPQL update goes **straight to the DB, bypassing the persistence context**. `flushAutomatically = true` flushes pending changes *before* the update; `clearAutomatically = true` clears the context *after* it so later `find()` calls don't return stale managed entities.

**Traps:** `@Modifying` / `@Query` are **Spring Data JPA**, not Spring AOP (AOP = Aspect-Oriented Programming: `@Aspect`, `@Around`). `@Query` is JPQL by default, not SQL. Bulk updates skip entity lifecycle: no `@Version` check, no `@PreUpdate`, no cascade, no L2 cache update. Derived `deleteBy...` methods don't need `@Modifying` but load and delete each entity one by one; a bulk `@Modifying @Query("delete ...")` is one statement.

### TX14 — Access token vs ID token vs refresh token — 🟡 Partial (ID vs access token mixed)

**Keywords:** ID token = OIDC, *who* the user is, for the client · access token = OAuth2, *what* it may access, sent to APIs as Bearer, short-lived · refresh token = long-lived, only to auth server, gets new access token · JWT is a *format*, not a 4th type · rotation · HttpOnly cookie

**Model answer:**

|  | ID token | Access token | Refresh token |
| --- | --- | --- | --- |
| Comes from | OpenID Connect (login) | OAuth 2.0 | OAuth 2.0 |
| Purpose | **authentication**: proves who logged in | **authorization**: proves what the caller may do (scopes/roles) | get a new access token without re-login |
| Read by | the **client app** (show name, email) | the **resource server / API** | only the **authorization server** |
| Sent how | never sent to APIs | `Authorization: Bearer <token>` on every request | POST to `/token` with `grant_type=refresh_token` |
| Format | always JWT (`sub`, `email`, `aud` = client id) | JWT or opaque | usually opaque |
| Lifetime | short (minutes) | short: 5–15 / 15–60 min | long: days/weeks; revocable |

**JWT** = `header.payload.signature`, Base64URL-encoded and **signed, not encrypted** (anyone can read the payload, nobody can change it). The API validates signature (via JWKS public key), `exp`, `iss`, `aud`; no DB lookup, so it is stateless.

**Refresh flow:**

1. Login → auth server returns access token (15 min) + refresh token (7 days) (+ ID token with OIDC).
2. Client calls APIs with the access token.
3. API returns **401** when it has expired.
4. Client sends the **refresh token** to the auth server's `/token` endpoint.
5. Auth server validates it and returns a **new access token** + (with **rotation**) a **new refresh token**; the old refresh token is invalidated. If an old one is reused → treat as theft, revoke the whole token family.
6. Client retries the original request.

**Spring Boot:** API = resource server: `http.oauth2ResourceServer(o -> o.jwt(Customizer.withDefaults()))` + `spring.security.oauth2.resourceserver.jwt.issuer-uri=...`. Login app = `oauth2Login()` (OIDC client).

**Storage:** access token in memory (or HttpOnly cookie); refresh token in an **HttpOnly, Secure, SameSite cookie**. Not `localStorage`: any XSS script can read it. Safer still: BFF pattern (tokens stay on the server).

**Traps:** JWT is not a separate token type; access and ID tokens are often JWTs. Don't send the ID token to APIs for authorization. Short access-token TTL exists because a JWT can't be revoked before it expires. Don't put secrets in a JWT payload.

## Spring Security

### Security Q1 + Q2 — Authentication vs Authorization (+ how Spring Security implements them) — 🟡 Partial ("role-based authentication", internals missing)

**Keywords:** AuthN = *who are you* (verify identity) → 401 · AuthZ = *what may you do* (permissions) → 403 · AuthN first · filter chain → AuthenticationManager → DaoAuthenticationProvider → UserDetailsService + PasswordEncoder (BCrypt) → SecurityContextHolder · RBAC = role-based *authorization*

**Model answer:** **Authentication** verifies the caller's **identity**: username + password, OTP, JWT, OAuth2 login. Fails → **401 Unauthorized**. **Authorization** decides, for an already-authenticated user, **what they are allowed to do** (roles/permissions: ADMIN can delete, USER can only read). Fails → **403 Forbidden**. Authentication always happens first. (Signup/login/logout are *flows around* authentication, not its definition.)

|  | Authentication | Authorization |
| --- | --- | --- |
| Question | Who are you? | What can you do? |
| Input | credentials / token | roles, authorities, scopes |
| Failure | 401 | 403 |
| Spring pieces | `AuthenticationManager`, `UserDetailsService`, `PasswordEncoder` | `authorizeHttpRequests`, `@PreAuthorize`, `hasRole` |

**How Spring Security authenticates a login:**

1. Request passes through the **security filter chain** (`DelegatingFilterProxy` → `FilterChainProxy` → your `SecurityFilterChain`).
2. Credentials go to `AuthenticationManager` (`ProviderManager`) → `DaoAuthenticationProvider`.
3. It calls `UserDetailsService.loadUserByUsername()` (your user repository) and checks the password with `PasswordEncoder` (**BCrypt**; passwords are stored hashed, never plain).
4. On success, an `Authentication` object (user + authorities) is stored in the `SecurityContextHolder` (ThreadLocal) → available to the rest of the request.
5. Stateless REST: login endpoint returns a **JWT**; later requests send it and a `OncePerRequestFilter` (or `oauth2ResourceServer().jwt()`) validates it and fills the SecurityContext.

```java
@Configuration
@EnableMethodSecurity
class SecurityConfig {
    @Bean
    SecurityFilterChain api(HttpSecurity http) throws Exception {
        return http
            .csrf(c -> c.disable())                                   // stateless JWT API
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(a -> a
                .requestMatchers("/auth/signup", "/auth/login").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")       // authorization
                .anyRequest().authenticated())                         // authentication required
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }
    @Bean
    PasswordEncoder encoder() { return new BCryptPasswordEncoder(); }
}

@PreAuthorize("hasRole('ADMIN')")      // method-level authorization
public void deleteUser(Long id) { ... }
```

**Traps:** it's role-based **authorization** (RBAC), not "role-based authentication". `hasRole("ADMIN")` checks authority `ROLE_ADMIN` (prefix added automatically); `hasAuthority` doesn't add it. Never store plain passwords. Logout with JWT can't "delete" the token server-side: keep TTL short, delete the refresh token, or keep a deny-list.

### Security Q3 — Restrict API access for users without permission — 🟢 Strong

**URL-level authorization in `SecurityFilterChain` (details):** rules are checked **top to bottom, first match wins**: put specific paths first, `anyRequest()` last. Options per matcher: `permitAll()`, `authenticated()`, `hasRole("ADMIN")`, `hasAnyRole("ADMIN","SUPER_USER")`, `hasAuthority("orders:write")`, `denyAll()`; can also match by HTTP method: `requestMatchers(HttpMethod.DELETE, "/orders/**").hasRole("ADMIN")`. Several chains (e.g. `/api/**` JWT, `/admin/**` form login) = multiple `SecurityFilterChain` beans with `securityMatcher(...)` + `@Order`. For data-based rules URL matching can't express ("user can only see their own order"), use method security: `@PreAuthorize("#userId == authentication.principal.id or hasRole('ADMIN')")`.

```java
.authorizeHttpRequests(a -> a
    .requestMatchers("/auth/**", "/actuator/health").permitAll()
    .requestMatchers(HttpMethod.GET, "/products/**").hasAnyRole("USER", "ADMIN")
    .requestMatchers("/admin/**").hasRole("ADMIN")
    .anyRequest().authenticated())      // must be LAST
```

**Trap:** putting `.anyRequest()` before other matchers fails at startup; a broad matcher like `/**` placed first silently shadows the specific rules below it.

### Security Q4 — JWT structure, OAuth 2.0 and OpenID Connect — 🟡 Partial (signed ≠ encrypted)

**Keywords:** JWT = header.payload.signature · **signed, not encrypted** (Base64URL, readable) · header `alg` + `typ` · payload = claims (`sub`, `iss`, `aud`, `exp`, `iat`, roles) · signature = integrity · HS256 shared secret vs RS256 private/public key (JWKS) · OAuth2 = delegated **authorization** (scopes, access token) · OIDC = **authentication** layer on OAuth2 (ID token, `openid` scope)

**JWT:**

| Part | Contains |
| --- | --- |
| Header | `{"alg": "RS256", "typ": "JWT", "kid": "key-1"}`: **signing** algorithm, token type, key id |
| Payload | **claims**: registered `sub` (user id), `iss` (issuer), `aud` (audience), `exp` (expiry), `iat` (issued at), `jti` (id) + custom `roles`, `email` |
| Signature | `sign(base64url(header) + "." + base64url(payload), key)` |

The signature proves **integrity + who issued it**: nobody can change the payload without the key. It does **not** hide the payload; anyone can Base64-decode it. So: no passwords/secrets/PII you wouldn't show. **HS256** = one shared secret signs and verifies (fine for a single service). **RS256/ES256** = auth server signs with a private key, every microservice verifies with the public key from the JWKS endpoint (preferred for microservices). The API validates: signature, `exp`, `iss`, `aud`.

**OAuth 2.0 = delegated authorization.** The user lets a **client app** access their data at a provider, limited to **scopes** (the consent screen: "allow read email, profile"), without giving the app their password. Roles: resource owner (user), client (your app), authorization server (Google), resource server (Google API). Result: **access token** (+ refresh token). Main flow: **Authorization Code + PKCE** (redirect to Google → user logs in & consents → code back to app → app exchanges code for tokens). Machine-to-machine: Client Credentials.

**OpenID Connect = identity layer on top of OAuth 2.0.** Add scope `openid` and you also get an **ID token** (always a JWT) with who the user is (`sub`, `email`, `name`) + a `/userinfo` endpoint. OAuth alone says *"this app may access X"*; OIDC adds *"this is user Y"*. **"Login with Google/GitHub" = OIDC** (GitHub is plain OAuth2 + user API).

**Spring Boot:** login app → `spring-boot-starter-oauth2-client` + `http.oauth2Login()` + `spring.security.oauth2.client.registration.google.client-id/secret`. API → `spring-boot-starter-oauth2-resource-server` + `http.oauth2ResourceServer(o -> o.jwt(...))`.

**Traps:** header holds the *signing* algorithm, not "encryption" (encrypted JWT = JWE, rare). Roles in a JWT are for **authorization**. OAuth2 by itself is not an authentication protocol; that's why OIDC exists. Reject `alg: none`. Don't put an ID token in `Authorization` headers to call APIs.

### Security Q5 — OAuth2 / OIDC login flow ("Login with Google") — Authorization Code + PKCE — 🟡 Partial (backend steps missing)

**Keywords:** redirect to /authorize (client\_id, redirect\_uri, scope, state, code\_challenge) · user logs in **at Google** · consent to scopes · redirect back with **code** + state · back-channel exchange code → tokens (client\_secret + code\_verifier) · validate ID token → find/create local user → issue own session/JWT

**Model answer:**

1. User clicks **Login with Google**. The app redirects the browser to Google's `/authorize` with `client_id`, `redirect_uri`, `response_type=code`, `scope=openid email profile`, a random **`state`** (CSRF protection) and a **PKCE `code_challenge`**.
2. User authenticates **on Google's page**; the app never sees the Google password.
3. Google shows the **consent screen** listing the scopes ("share your email and profile"); user approves.
4. Google redirects back to `redirect_uri?code=XYZ&state=...`. App checks `state` matches.
5. App's **backend** calls Google's `/token` endpoint with the `code` + `client_secret` + PKCE `code_verifier` → receives **access token + ID token** (+ refresh token).
6. App validates the **ID token** (signature, `iss`, `aud` = its client id, `exp`), reads `sub`/`email`, **finds or creates the local user** (first login = automatic signup), and starts its own session or issues its own JWT.
7. If needed, uses the Google access token to call Google APIs (e.g. Calendar) within the granted scopes.

**Why a code, not the token, in the redirect:** the URL passes through the browser (history, logs); the short-lived, one-time code is useless without the client secret / PKCE verifier, and the real tokens travel only server-to-server.

**Spring Boot:** `oauth2Login()` performs steps 1–6 automatically; callback URL is `/login/oauth2/code/{registrationId}`; register client id/secret under `spring.security.oauth2.client.registration.google.*`; customize user mapping with a custom `OidcUserService`, and issue your own JWT in an `AuthenticationSuccessHandler`.

**Benefits:** no password stored by you, the provider's MFA and security, faster signup/login, verified email. **Downsides:** dependency on the provider, account-linking when the same email signs up locally.

**Traps:** the user *consents* (authorizes the app); calling the last step "giving authentication" mixes terms. Without `state` → login CSRF. Without PKCE, an intercepted code can be redeemed (mandatory for mobile/SPA). Implicit flow (token in URL) is deprecated.

### Security Q6 — CORS (Cross-Origin Resource Sharing) — 🔴 Weak (called it a vulnerability)

**Keywords:** browser's Same-Origin Policy · origin = scheme + host + port · CORS = server *relaxes* SOP via response headers · enforced by the **browser** · preflight `OPTIONS` · `Access-Control-Allow-*` · not a vulnerability, not server protection · `http.cors()` + `CorsConfigurationSource`

**Model answer:** Browsers apply the **Same-Origin Policy**: JavaScript loaded from `https://app.com` may not read responses from a different **origin** (scheme + host + port), e.g. `https://api.com` or `http://localhost:8080` vs `:3000`. **CORS is the mechanism that lets the server tell the browser which other origins are allowed** to read its responses, using response headers. The **browser** enforces it; the server only declares the policy.

**Flow:** for "non-simple" requests (PUT/DELETE/PATCH, `Content-Type: application/json`, custom headers like `Authorization`) the browser first sends a **preflight** `OPTIONS` request with `Origin`, `Access-Control-Request-Method`, `Access-Control-Request-Headers`. The server answers with:

- `Access-Control-Allow-Origin: https://app.com`
- `Access-Control-Allow-Methods: GET, POST, PUT, DELETE`
- `Access-Control-Allow-Headers: Authorization, Content-Type`
- `Access-Control-Allow-Credentials: true` (if cookies are sent)
- `Access-Control-Max-Age: 3600` (cache the preflight)

If they match, the browser sends the real request; otherwise it blocks the response ("CORS error" in the console).

**Spring Boot + Spring Security:**

```java
@Bean
SecurityFilterChain api(HttpSecurity http) throws Exception {
    return http.cors(Customizer.withDefaults())          // use the bean below; handles preflight before auth
               .authorizeHttpRequests(a -> a.anyRequest().authenticated())
               .build();
}

@Bean
CorsConfigurationSource corsConfigurationSource() {
    var c = new CorsConfiguration();
    c.setAllowedOrigins(List.of("https://app.example.com"));
    c.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
    c.setAllowedHeaders(List.of("Authorization", "Content-Type"));
    c.setAllowCredentials(true);
    var src = new UrlBasedCorsConfigurationSource();
    src.registerCorsConfiguration("/api/**", c);
    return src;
}
```

Without Security: `@CrossOrigin(origins = "https://app.example.com")` on a controller, or global `WebMvcConfigurer.addCorsMappings()`. With Security you need `http.cors()`, otherwise the preflight `OPTIONS` (which carries no token) is rejected with 401 before MVC's CORS config runs.

**Traps:** CORS is **not a vulnerability** and **not protection for the server**: Postman/curl/other servers ignore it; it only controls what a browser lets JS read. Misconfigured CORS (`*` or reflecting any Origin with credentials) *creates* a vulnerability. `allowedOrigins("*")` + `allowCredentials(true)` is rejected; list origins or use `allowedOriginPatterns`. CORS ≠ CSRF protection.
