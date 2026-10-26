# Day 1 Notes: customer-service (Spring Boot basics, REST, validation, Docker start)

Living document. Updated after each step. Read the Q&A out loud before an interview.

---

## 1. What I built so far

`customer-service` on port **8081**, in layers:

| Layer | Class | Job (bank analogy) |
|---|---|---|
| Controller | `CustomerController` | Receptionist: takes the web request, passes it on, makes no decisions |
| Service | `CustomerService` | Manager: applies the business rules |
| Repository | `CustomerRepository` (interface) + `InMemoryCustomerRepository` | Vault clerk: stores and fetches |
| Model | `Customer`, `KycStatus` (enum) | The data |
| Exception | `CustomerAlreadyExistsException`, `CustomerNotFoundException`, `GlobalExceptionHandler` | Complaints desk |

Request flow: **Postman → Controller → Service → Repository → (notebook) and back.**

Endpoints:

| Method | Path | Result |
|---|---|---|
| GET | /customers | list of customers (`[]` if none) |
| GET | /customers/{id} | one customer, or 404 |
| POST | /customers | create (starts as PENDING), 409 if email exists, 400 if invalid |
| PATCH | /customers/{id}/kyc?status=VERIFIED | change KYC status |

Package name is `com.cbs.customer_service` (underscore, because the artifact name had a hyphen).

---

## 2. Commands

### Git
| Command | What it does |
|---|---|
| `git status` | What changed, which branch I'm on |
| `git checkout -b feature/customer-service` | Create and switch to a new branch |
| `git add .` | Stage all changes (put in the frame) |
| `git commit -m "feat(customer): ..."` | Save a snapshot with a label |
| `git push -u origin <branch>` | First push of a branch; links it to GitHub |
| `git push` | Later pushes |
| `git log` | History (press `q` to exit) |

Commit message format: `feat`, `fix`, `docs`, `test`, `refactor`, `chore`, then `(service)`, then what changed.

### Maven / running
- Run the app: green play button on `CustomerServiceApplication`
- Health check: `http://localhost:8081/actuator/health` → `{"status":"UP"}`
- IntelliJ: Settings → Build Tools → Maven → Maven home path → **Bundled (Maven 3)**

### Docker
| Command | What it does |
|---|---|
| `docker --version` | Is the Docker program installed? (does NOT prove the engine runs) |
| `docker run hello-world` | Proves the engine, WSL 2 and terminal work end to end |
| `docker compose up -d` | Start everything in `docker-compose.yml` in the background |
| `docker ps` | List running containers |

Port mapping `"5432:5432"` = **laptop port : container port**.

---

## 3. Concepts in plain words

- **Microservice:** a separate bank department with its own register (database). Departments talk by REST calls.
- **Spring Boot:** a ready-made kitchen. Auto-configuration sees the libraries in `pom.xml` and sets things up (for example Tomcat).
- **`pom.xml`:** the shopping list of libraries for Maven.
- **IoC / Dependency Injection:** I don't write `new` for my dependencies. Spring creates them and hands them in through the constructor. It creates the clerk first, then the manager, then the controller, because each constructor asks for the previous one.
- **Bean:** an object Spring creates and manages (`@Component`, `@Service`, `@Repository`, `@RestController` all register beans).
- **Injection is by type.** If two beans implement the same interface, Spring can't choose; fix with `@Primary`, `@Qualifier` or `@Profile`.
- **Interface + polymorphism + loose coupling:** the manager knows only `CustomerRepository` (the job description). I can swap the in-memory clerk for a database clerk without changing the manager.
- **Encapsulation:** private fields, public getters and setters. Rules can later live in one place.
- **Inheritance:** `CustomerAlreadyExistsException extends RuntimeException` gets message storage, stack trace and "throwability" for free.
- **Checked vs unchecked:** `RuntimeException` is unchecked (Java doesn't force `try/catch` or `throws`). `IOException` is checked.
- **Enum:** a fixed list of allowed values. Bad values are rejected at compile time (in code) and by Spring (in requests: `?status=APPROVED` gives 400).
- **`Optional`:** a box that holds a value or is empty. Forces callers to handle "not found". `ofNullable` wraps something that may be null; `of` for sure-non-null; `empty()` for the empty box.
- **`ConcurrentHashMap` and `AtomicLong`:** many requests run on many threads. A plain `HashMap` can corrupt; `id = id + 1` is read-add-write and can give two customers the same id (race condition). Atomic does it as one unbreakable step.
- **DRY:** `updateKycStatus` reuses `getCustomerById`, so "not found" logic lives in one place.
- **Never return null for a list:** return an empty list.
- **Shallow copy:** `new ArrayList<>(store.values())` is a new list, but the same `Customer` objects inside.
- **Never trust the caller:** the manager forces `PENDING` on create, so nobody can self-approve KYC.
- **Validation (Bean Validation):** labels on the model (`@NotBlank`, `@Email`), switched on by `@Valid` on the controller parameter. Checked before the controller body runs, so the service is never called for bad data.
- **Exception handling in one place:** `@RestControllerAdvice` + `@ExceptionHandler`. The exception **reports** the problem; the handler **decides** the HTTP status.
- **Map:** key to value pairs. Starts empty; my loop fills it. For validation errors: field name to message, so a UI can highlight the right field.
- **Docker:** an **image** is the ready-made package, a **container** is it running, **Docker Hub** is the public store, a **volume** keeps data outside the container so it survives removal (durability).

---

## 4. Annotation cheat sheet

| Annotation | Meaning |
|---|---|
| `@SpringBootApplication` | Configuration + auto-configuration + component scan |
| `@RestController` | Class handles web requests; return values become JSON |
| `@RequestMapping("/customers")` | Base address for the class |
| `@GetMapping`, `@PostMapping`, `@PatchMapping` | Method handles that HTTP action |
| `@PathVariable` | Takes `{id}` from the address |
| `@RequestParam` | Takes `?status=...` from the address |
| `@RequestBody` | Converts incoming JSON into an object |
| `@Valid` | Run the validation rules on this object |
| `@Service`, `@Repository` | Register a bean with a role |
| `@NotBlank`, `@Email` | Validation rules on fields |
| `@RestControllerAdvice` | Global safety net for all controllers |
| `@ExceptionHandler(X.class)` | Run this method when X is thrown |
| `@Override` | This method implements or replaces a parent's |

---

## 5. HTTP status codes I used

| Code | Meaning | When |
|---|---|---|
| 200 | OK | Successful read or update |
| 400 | Bad Request | Validation failed, or invalid enum value |
| 404 | Not Found | Customer id doesn't exist |
| 409 | Conflict | Email already registered |
| 500 | Server error | Unhandled exception (what we saw before the handler) |

Families: 2xx success, 4xx caller's mistake, 5xx server's mistake.

---

## 6. Bugs I hit and how I fixed them (great interview stories)

| Problem | Cause | Fix |
|---|---|---|
| Maven error `ArtifactHandlerManager was bound` | IntelliJ used a broken manual Maven install | Use IntelliJ's bundled Maven |
| `store.put(Customer.getId(), ...)` red | Capital `Customer` is the class; I needed the object `customer` | Call the method on the object |
| `Optional.ofNullable()` with nothing inside | Wanted the empty box | `Optional.empty()` |
| `List.of(store.values())` wrong | Put the whole collection in as ONE item | `new ArrayList<>(store.values())` |
| `@GetMapping("/")` | Made the path `/customers/`, which Spring treats as different | Use `@GetMapping` with nothing |
| Handler method didn't run | Forgot `@ExceptionHandler(...)` label; no compile warning | Add the label |
| KYC always stayed PENDING after PATCH | The setter ignored its parameter and always wrote `PENDING` | Setter stores what it receives; the PENDING rule lives in `createCustomer` |
| Duplicate email gave 500 with no message | No handler for my exception | `GlobalExceptionHandler` returning 409 |
| `docker run` failed | WSL 2 needs the Virtual Machine Platform Windows feature | Enable it in admin PowerShell, restart Windows |

---

## 7. Interview Q&A (answer in 3-5 sentences)

**Spring Boot and design**

1. **What is the difference between Spring and Spring Boot?**
   Spring is the core framework (IoC, DI, MVC, data access). Spring Boot adds auto-configuration, starters and an embedded server so an app runs with minimal setup.
2. **What does `@SpringBootApplication` do?**
   It combines `@Configuration`, `@EnableAutoConfiguration` and `@ComponentScan`: it marks the config class, turns on auto-configuration from the libraries on the classpath, and scans the package for beans.
3. **What is Dependency Injection and why use it?**
   Objects receive their dependencies instead of creating them. It gives loose coupling and easy testing, because I can inject a fake. Spring does the injection from its IoC container, and constructor injection is preferred.
4. **What is a bean? What is the IoC container?**
   A bean is an object Spring creates and manages. The IoC container is where Spring creates, stores and wires them.
5. **How does Spring choose which bean to inject if an interface has two implementations?**
   By type first. With two candidates it fails, and I resolve it with `@Primary`, `@Qualifier` or `@Profile`.
6. **What happens if two beans depend on each other?**
   Circular dependency. With constructor injection Spring refuses to start. The fix is redesigning so the dependency goes one way.
7. **Explain your layered architecture. Why separate the layers?**
   Controller handles HTTP, service holds business rules, repository handles storage. Each can change or be tested alone, and rules live in one place no matter who calls the service.
8. **Why is the repository an interface?**
   So the service depends on a contract. I started with an in-memory implementation and can swap in a JPA one without touching the service. That is polymorphism and loose coupling.
9. **Interface vs abstract class?**
   An interface is a contract (a class can implement many); an abstract class can hold shared state and partial code (a class extends one). Use an interface for a capability, an abstract class for a shared base.
10. **What is encapsulation? Example from your project?**
    Hiding fields behind methods. `Customer` fields are private with getters and setters, so rules can be added in one place.

**Java**

11. **Why `Optional`?** It makes "might be missing" explicit and avoids `NullPointerException`. The caller must decide what to do when it's empty.
12. **`HashMap` vs `ConcurrentHashMap`?** `HashMap` isn't thread-safe. `ConcurrentHashMap` is safe for concurrent access without locking the whole map.
13. **Why `AtomicLong` for ids?** `id++` is read, add, write, so two threads can read the same value (race condition). Atomic operations do it as one step.
14. **Checked vs unchecked exceptions?** Checked must be declared or caught (`IOException`); unchecked (`RuntimeException`) need not. Spring business code mostly uses unchecked ones.
15. **Shallow vs deep copy?** Shallow copies the container but shares the objects inside; deep also copies the objects.
16. **Why an enum instead of a String?** Fixed values, checked at compile time, no typos.

**REST and errors**

17. **GET vs POST vs PUT vs PATCH?** GET reads, POST creates, PUT replaces, PATCH changes part of a resource.
18. **`@PathVariable` vs `@RequestParam` vs `@RequestBody`?** Path variable is part of the address (`/customers/5`), request param is after `?`, request body is the JSON payload.
19. **Why 409 for a duplicate email, not 400 or 500?** The request is well formed (not 400) and the server is fine (not 500); it conflicts with existing data, which is 409.
20. **How does validation work in Spring Boot?** Labels like `@NotBlank` on the model, `@Valid` on the parameter. Spring checks before the method runs and throws `MethodArgumentNotValidException`, which I turn into a 400 with field messages.
21. **How do you handle exceptions globally?** `@RestControllerAdvice` with `@ExceptionHandler` methods. An uncaught exception travels up to the `DispatcherServlet`, which finds the matching handler. This avoids `try/catch` in every controller.
22. **Why validate in the controller but enforce business rules in the service?** Input shape is checked at the door; business rules (unique email, starting KYC status) must hold whoever calls the service.
23. **Why don't you trust `kycStatus` from the JSON?** A caller could self-approve. The service forces `PENDING` on create.
24. **How do you read a stack trace?** First line is the exception and message; the first `com.cbs...` lines show where my code failed; framework lines can be ignored.

**Docker**

25. **Image vs container?** Image is the packaged software; a container is a running instance of it.
26. **What does `"5432:5432"` mean?** Laptop port : container port.
27. **Why use a volume for the database?** Container files vanish when the container is removed; a volume keeps the data (durability).
28. **Why Docker for PostgreSQL, Redis, Kafka?** No local installs, the same setup on every machine, one command to start and stop everything.

---

## 8. Still open (next steps)

- Replace the in-memory repository with a JPA/PostgreSQL one (Docker container is already running)
- Spring Data JPA, entities, `@Entity`, `@Id`, connecting through `application.properties`
- Unit tests with JUnit and Mockito
- Then account-service, Redis, security, gateway, Kafka
