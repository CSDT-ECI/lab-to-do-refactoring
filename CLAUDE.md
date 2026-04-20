# CLAUDE.md — LabToDo Enterprise Refactoring

This file provides essential context for Claude Code sessions working on this repository.
Read it fully before making any changes to the codebase or documentation.

---

## Project Identity

**Project**: LabToDo — Enterprise Refactoring Initiative

**Course**: *Calidad de Software y Deuda Técnica* (CSDT_M) — *Universidad Escuela Colombiana de Ingeniería Julio Garavito*, 2026

**Team**:
- Andrés Serrato ([andresserrato2004](https://github.com/andresserrato2004))
- Jesús Pinzón ([JAPV-X2612](https://github.com/JAPV-X2612))
- Sergio Bejarano ([SergioBejarano](https://github.com/SergioBejarano))

**Original project**: [github.com/Laboratorio-de-Informatica/LabToDo](https://github.com/Laboratorio-de-Informatica/LabToDo)

---

## Essential Commands

```bash
# Build (compile only, skip tests)
./mvnw clean compile -B

# Run unit tests only (fast, no Spring context required)
./mvnw test

# Run all tests + enforce 85% coverage gate (fails build if below)
./mvnw clean verify

# Run the application locally (requires MySQL or Docker first)
./mvnw spring-boot:run

# Package as JAR
./mvnw clean package -DskipTests

# Start MySQL via Docker (required before running the application)
docker run -p 3306:3306 --name labtodo-mysql \
  -e MYSQL_ROOT_PASSWORD=my-secret-pw \
  -d mysql:latest

# Start everything with Docker Compose (app + DB)
docker compose up --build

# Generate JaCoCo coverage report and check gate
./mvnw clean test verify
# HTML report: target/site/jacoco/index.html

# Quick line-coverage value in terminal
awk -F, 'NR>1{m+=$8; c+=$9} END{printf "LINE_COVERAGE=%.2f%%\n", (c*100)/(m+c)}' \
  target/site/jacoco/jacoco.csv

# Run SonarCloud analysis locally (requires SONAR_TOKEN env variable)
./mvnw sonar:sonar \
  -Dsonar.projectKey=CSDT-ECI_lab-to-do-refactoring \
  -Dsonar.organization=csdt-eci \
  -Dsonar.host.url=https://sonarcloud.io
```

---

## Repository Structure

```
lab-to-do-refactoring/
├── src/
│   ├── main/java/edu/eci/labinfo/labtodo/
│   │   ├── LabtodoApplication.java         — Spring Boot entry point
│   │   ├── controller/                     — JSF backing beans (@SessionScope)
│   │   │   ├── AdminController.java        — Admin task/user management
│   │   │   ├── LoginController.java        — Authentication & account mgmt
│   │   │   ├── SemesterController.java     — Semester/period CRUD
│   │   │   └── TaskController.java         — Task CRUD + state transitions
│   │   ├── data/                           — Spring Data JPA repositories
│   │   │   ├── CommentRepository.java
│   │   │   ├── SemesterRepository.java
│   │   │   ├── TaskRepository.java
│   │   │   └── UserRepository.java
│   │   ├── model/                          — Domain entities and enums
│   │   │   ├── AccountType.java            — Enum: ACTIVO, INACTIVO, SIN_VERIFICAR
│   │   │   ├── Comment.java
│   │   │   ├── LabToDoExeption.java        — Base custom exception (note: typo in name)
│   │   │   ├── Role.java                   — Enum: ADMINISTRADOR, MONITOR, ESTUDIANTE
│   │   │   ├── Semester.java
│   │   │   ├── Status.java                 — Enum: POR_HACER, EN_PROGRESO, EN_REVISION, TERMINADO
│   │   │   ├── Task.java
│   │   │   ├── TopicTask.java              — Enum: task topic categories
│   │   │   ├── TypeTask.java               — Enum: LABORATORIO, USUARIO, ADMINISTRADORES
│   │   │   └── User.java
│   │   └── service/                        — Business logic services
│   │       ├── CommentService.java
│   │       ├── PrimeFacesWrapper.java      — Injectable PrimeFaces wrapper (enables mocking)
│   │       ├── SecurityConfig.java         — Spring Security password encoder config
│   │       ├── SemesterService.java
│   │       ├── TaskService.java
│   │       └── UserService.java
│   ├── main/resources/
│   │   ├── META-INF/resources/             — JSF/XHTML views (PrimeFaces frontend)
│   │   ├── application.properties          — Main DB and server config
│   │   └── application-test.properties     — H2 in-memory config for tests
│   └── test/java/edu/eci/labinfo/labtodo/
│       ├── support/
│       │   ├── BaseUnitTest.java           — Mockito-only base class (no Spring context)
│       │   ├── BaseIntegrationTest.java    — @SpringBootTest + H2 + @Transactional
│       │   └── TestDataBuilders.java       — Centralized domain object factory
│       ├── unit/
│       │   ├── model/                      — 28 entity tests
│       │   ├── service/                    — 54 service tests (mocked repos)
│       │   └── controller/                 — 105 JSF controller tests (MockedStatic)
│       └── integration/data/               — 4 *RepositoryIT classes (infra ready, no @Test yet)
├── assets/
│   ├── diagrams/                           — Architecture diagrams (.asta)
│   ├── docs/                               — Supplementary technical debt docs
│   └── images/                             — Screenshots and report images
├── weekly-reports/                         — CSDT_M detailed analysis reports
│   ├── code-smells-and-refactoring.md      — Deliverable 1: 10 smells, 8 patterns, SOLID
│   ├── testing-debt.md                     — Deliverable 3: 6 testing debt practices, 187 tests
│   └── code-quality.md                     — Deliverable 3: ISO 25010, JaCoCo, SonarCloud
├── CLAUDE.md                               — This file
├── CSDT_PrimeraEntrega2026.md              — Consolidated deliverable 1+3 report
├── CSDT-2026.md                            — Deliverable 2: YAGNI, DRY, KISS, XP practices
├── README.md                               — Project index with all 7 CSDT_M deliverables
├── sonar-project.properties               — SonarCloud project configuration
├── docker-compose.yml                      — App + DB container setup
├── Dockerfile                              — Application container definition
└── pom.xml                                 — Maven build configuration
```

---

## Technology Stack

| Layer | Technology | Version | Notes |
|---|---|---|---|
| Backend Framework | Spring Boot | 3.2.0 | Entry point: `LabtodoApplication.java` |
| View Layer | JSF (JavaServer Faces) | 3.0 | XHTML views in `META-INF/resources/` |
| UI Components | PrimeFaces | 13.0.3 | Via JoinFaces integration |
| JSF–Spring Bridge | JoinFaces | 5.2.0 | Enables `@SessionScope` in Spring |
| Persistence | Spring Data JPA + Hibernate | 3.2.0 | Repositories in `data/` package |
| Database (prod) | MySQL | 8.2.0 | Also supports MariaDB |
| Database (tests) | H2 (in-memory) | — | Configured in `application-test.properties` |
| Security | Spring Security Crypto | 6.2.0 | BCrypt password encoding only |
| Build Tool | Apache Maven | 3.9.5 | Use `./mvnw` (wrapper included) |
| Language | Java (OpenJDK Temurin) | 17 | LTS — do not upgrade without team decision |
| Utilities | Lombok | 1.18.30 | `@Data`, `@Builder`, `@Value` on entities |
| Test Framework | JUnit 5 + Mockito | — | `mockito-inline` for `MockedStatic` |
| Assertion Library | AssertJ | — | Prefer over JUnit native assertions |
| Coverage | JaCoCo | 0.8.11 | Gate: **85% line coverage** |
| Static Analysis | SonarCloud | — | `sonar.organization=csdt-eci` |
| CI/CD | GitHub Actions | — | `.github/workflows/ci-cd.yml` |
| Containerization | Docker + Docker Compose | — | `Dockerfile` + `docker-compose.yml` |

---

## Branch Strategy

Each CSDT_M deliverable lives in its own branch.

**Never push directly to `main`**. Always open a PR from the feature branch.

---

## Business Rules

These rules reflect the actual behavior of the LabToDo application domain. Respect them in all refactoring decisions.

### User Roles and Permissions

- **ADMINISTRADOR**: Can create and assign any task type; can change any user's role and account status; can view all tasks.
- **MONITOR**: Can create tasks of type `LABORATORIO` and `USUARIO`; cannot create `ADMINISTRADORES` tasks; can view and update tasks assigned to them.
- **ESTUDIANTE**: Read-only access; cannot create or modify tasks.

### Task Types (`TypeTask`)

- **`LABORATORIO`**: Assigned to all active monitors in a semester. On `TERMINADO` transition, the task's user list is replaced with the list of users who commented on it (participation tracking).
- **`USUARIO`**: Assigned to specific selected users.
- **`ADMINISTRADORES`**: Assigned automatically to all active administrators; only admins can create this type.

### Task Status Machine (`Status`)

Valid transitions only move forward. There is no rollback.

```
POR_HACER → EN_PROGRESO → EN_REVISION → TERMINADO
```

- Transitioning a `LABORATORIO` task to `TERMINADO` triggers enrichment: the task's user list is overwritten with the users who commented.
- The `Status.next()` method advances to the next ordinal state.

### Account Types (`AccountType`)

- **`ACTIVO`**: User can log in and perform actions according to their role.
- **`INACTIVO`**: User cannot log in. Active admin tasks must be reassigned before deactivation.
- **`SIN_VERIFICAR`**: New registrations — blocked until an admin activates them.

### Semester / Period Rules

- A semester has a `startDate` and `endDate`; the active semester is determined by `LocalDate.now()` falling within this range.
- `startDate` must be strictly before `endDate` — this is validated in `SemesterController`.
- Only one semester is considered active at any point (no overlapping ranges in valid data).

### Password Rules

- Passwords are encoded with **BCrypt** via `PasswordEncoder` from `SecurityConfig`.
- Password change flow requires: current password verification + new password confirmation matching.
- A "strong password" validator is enforced in `LoginController`.

---

## Architecture Context

### Current State (Legacy — Being Refactored)

The codebase is a **JSF monolith** with known architectural problems. Do not assume it follows Clean Architecture — it does not yet.

- Controllers are `@Component @SessionScope` JSF backing beans, **not** REST controllers.
- Business logic is mixed with UI concerns inside controllers.
- There is no DTO layer — entities are passed directly to the view.
- `PrimeFacesWrapper` is an injectable wrapper around `PrimeFaces.current()` introduced specifically to allow mocking in tests.
- `SecurityConfig` contains only a `PasswordEncoder` bean — there is **no `SecurityFilterChain`**, which is a tracked SonarCloud Security E finding.

### Target Architecture (Refactoring Goal)

```
Presentation Layer  — REST controllers, DTOs, exception handlers
Application Layer   — @Transactional use case services, events
Domain Layer        — Entities, domain services, repository interfaces, custom exceptions
Infrastructure Layer — JPA implementations, security, configuration
```

**Target package structure** (not yet implemented):

```
edu.eci.labinfo.labtodo/
├── api/             — REST controllers, DTOs, global exception handler
├── application/     — Use case services, mappers, domain events
├── domain/          — Entities, domain services, repository interfaces, exceptions
├── infrastructure/  — JPA, security, external config
└── shared/          — Constants, utilities
```

---

## Coding Standards

### Language

- **All code must be in English**: class names, method names, variable names, comments, Javadoc, exception messages, log messages, and constants.
- Spanish is only permitted in user-facing display strings that match domain data already stored in the database (e.g., enum display values like `"Pendiente"`).

### Naming Conventions

- Classes: `PascalCase`
- Methods and variables: `camelCase`
- Constants: `UPPER_SNAKE_CASE`
- Test methods: `shouldDoSomethingWhenCondition` / `shouldNotDoSomethingWhenCondition`
- Integration test classes: `*RepositoryIT` (picked up by Maven Failsafe)
- Unit test classes: `*Test` (picked up by Maven Surefire)

### Method Constraints

- Maximum **20 lines** per method.
- Maximum **Cyclomatic Complexity of 5** per method. The target is to bring the current average from 12.4 down to 3.2.
- Maximum **3 parameters** per method. Use a parameter object (`@Value @Builder` record) for more.
- Use **guard clauses** (early returns) instead of nested conditionals.

### Dependency and Safety Rules

- **Never call `Optional.get()` directly.** Always use `.orElseThrow(() -> new EntityNotFoundException(...))` or `.orElse(defaultValue)`.
- **Never use magic strings.** All string literals that represent domain values must become enum constants or `static final` fields in a constants class.
- **Never return `null` from a service method.** Use `Optional<T>` for nullable lookups, or throw a typed exception. Use the Null Object pattern for entities.
- **Never swallow exceptions** with empty `catch` blocks.
- **Never leave `@Test`-less test classes.** A test class with only infrastructure setup and no `@Test` methods is testing debt (anti-pattern #4).

### SOLID Enforcement

Apply these before adding any new code:

- **SRP**: One class, one reason to change. If a class has more than one responsibility, extract.
- **OCP**: Use the Strategy Pattern for variant behavior — never add a new `if/else` branch to existing switch-like logic.
- **LSP**: Subtypes must honor parent contracts. `NullSemester` must not throw where `Semester` returns a value.
- **ISP**: Repositories must be segregated by client need. Do not inject a repository with 20 methods when a service only needs 2.
- **DIP**: Depend on interfaces, not concrete classes. No service should depend on `PrimeFacesWrapper` directly — only on `UINotificationService`.

---

## Testing Requirements

### Coverage Gate

The JaCoCo gate is set to **85% line coverage**. `./mvnw verify` fails the build if coverage drops below this threshold. Do not lower the gate.

### Test Writing Rules

1. Every test must follow **AAA**: `// Given`, `// When`, `// Then` comment blocks.
2. Every service operation must have at least one **negative-path test** (`shouldNotDoSomethingWhen...`).
3. Use `TestDataBuilders` to construct domain objects in the Arrange block — never duplicate object construction across tests.
4. Extend `BaseUnitTest` for unit tests (Mockito, no Spring context).
5. Extend `BaseIntegrationTest` for integration tests (H2, `@SpringBootTest`, `@Transactional`).
6. Use `MockedStatic` from `mockito-inline` to mock `FacesContext.getCurrentInstance()` and `PrimeFaces.current()` in controller tests — **never** start a real Faces container.
7. **Do not use `@SpringBootTest` in unit test classes.** It loads the full application context and slows down the suite.

### Test Coverage Targets by Layer

| Layer | Target |
|---|---|
| Domain services | 95% |
| Application services | 90% |
| REST / JSF controllers | 80% |
| JPA repositories | 70% |
| Mappers / converters | 85% |

### SonarCloud Exclusions

The following classes are excluded from both JaCoCo and SonarCloud analysis (configured in `sonar-project.properties`):

- `LabtodoApplication.java` — framework entry point
- `SecurityConfig.java` — infrastructure configuration
- `PrimeFacesWrapper.java` — thin delegation wrapper

---

## CI/CD Pipeline

The pipeline is defined in `.github/workflows/ci-cd.yml` and triggers on push/PR to `main` and `develop`.

| Stage | Job name | What it does |
|---|---|---|
| 1 | `🔨 Build` | `./mvnw clean compile -B` — fails if compilation errors |
| 2 | `🧪 Test & Coverage` | `./mvnw verify -B` + SonarCloud scan — fails if coverage < 85% |
| 3 | `🚀 Deploy (simulated)` | `./mvnw package -DskipTests` + echo deployment confirmation |

Each stage depends on (`needs:`) the previous one — a failure blocks all downstream stages.

**Required GitHub Secrets** (Settings → Secrets → Actions):

- `SONAR_TOKEN` — SonarCloud API token
- `SONAR_PROJECT_KEY` — `CSDT-ECI_lab-to-do-refactoring`
- `SONAR_ORGANIZATION` — `csdt-eci`
- `GITHUB_TOKEN` — provided automatically by GitHub Actions

---

## Known Technical Debt (Tracked)

These are pre-existing issues, **not regressions**. They are tracked in SonarCloud and in `assets/docs/`.

| Item | Severity | SonarCloud Rating | Location | Remediation |
|---|---|---|---|---|
| `Optional.get()` without `isPresent()` | Critical | Reliability D | `TaskService`, `CommentService`, `SemesterService`, `UserService` | Replace with `.orElseThrow()` |
| No `SecurityFilterChain` configured | Blocker | Security E | `SecurityConfig.java` | Add Spring Security config |
| Hardcoded BCrypt string in SQL seed | Blocker | Security E | Init SQL script | Move to env variable |
| God class — `LoginController` | High | Maintainability | `LoginController.java` (389 lines) | Extract auth, account, redirect concerns |
| God class — `AdminController` | High | Maintainability | `AdminController.java` (280 lines) | Extract per use case |
| `TaskController` direct dependency on `LoginController` | High | Architecture | Line 47 | Route through `SecurityContext` or `AuthService` |
| Session-scoped controllers mixing UI state + business state | High | Architecture | All controllers | Migrate to REST + stateless services |
| Integration test classes with no `@Test` methods | Medium | Testing debt | `*RepositoryIT.java` (4 classes) | Write test scenarios + SQL seed scripts |
| No mutation testing configured | Medium | Testing quality | — | Add `pitest-maven` plugin for service layer |

Do not remove items from this table without resolving them and verifying the SonarCloud rating improves.

---

## Documentation Rules

When creating or editing Markdown files in this repository (especially in `weekly-reports/` and root-level deliverable files), apply the following conventions:

- **Language**: All content in formal English.
- **Headings**: Use all four levels (`#`, `##`, `###`, `####`). Reserve `#` for document title only.
- **Emphasis**: Bold (`**text**`) for key concepts and technologies; italic (`*text*`) for foreign-language terms and technology names used in prose; HTML underline (`<u>text</u>`) for critical data values (metrics, thresholds).
- **Lists**: Use nested bullet points (up to 3 levels) for readability. Use numbered lists only for sequential steps.
- **Emojis**: Add emojis to `##` and `###` headings, and to table entries where they meaningfully categorize content. Avoid overuse in body text.
- **Code**: Use backticks for inline commands, file paths, class names, method names. Use fenced code blocks with explicit language identifier for all multi-line code.
- **Math**: Use `$...$` for inline LaTeX and `$$...$$` for block equations.
- **Images**: Use `<img src="assets/images/..." alt="Description" width="70%">` for local images. Use `![alt](url)` only for externally hosted images that cannot be stored locally.
- **Hyperlinks**: `[Display Name](URL)` — always include descriptive display text, never bare URLs.
- **Tables**: Use tables for comparative data, metrics, and structured lists only when they improve readability over bullet points.
- **Authors section**: Every document must end with the standard `## 👥 Authors` HTML table (3 team members).
- **Additional Resources**: Every document must end with `## 🔗 Additional Resources` linking to relevant authoritative sources.
- **License section**: Include `## 📄 License` (with CC BY-SA 4.0 content) **only in `README.md`**, not in report files.

---

## Guardrails — What Claude Must Not Do

- **Do not merge or switch branches** without explicit user instruction. Each deliverable branch is a separate PR under professor review.
- **Do not modify test configuration** (JaCoCo gate threshold, SonarCloud exclusions, Surefire/Failsafe config) without explicit user approval.
- **Do not add new dependencies** to `pom.xml` without explicit user approval. Justify any addition against the existing tech stack.
- **Do not refactor production code** while working on a documentation task, and vice versa. Keep scopes clean.
- **Do not create documentation files** (`.md`) unless the user explicitly requests them.
- **Do not commit or push** any changes — always stage and present the diff for user review first.
- **Do not use Spanish** in any code, comments, test names, constants, or log messages, even when replicating legacy patterns.
- **Do not lower the JaCoCo coverage gate** below 85% under any circumstances.
- **Do not use `Optional.get()`** anywhere in new or modified service code.
- **Do not introduce new magic strings** — use or create enum constants.
- **Do not use `@SpringBootTest` in unit tests** — use `BaseUnitTest` (Mockito only).
- **Do not mock the database** in integration tests — use the H2 in-memory database configured in `application-test.properties`.
- **Do not add speculative code** (YAGNI) — no unused repository methods, no "might be useful later" abstractions.
- **Do not silence exceptions** — empty `catch` blocks are forbidden; rethrow, wrap, or log with context.

---

## Environment Variables

The application reads these from environment or `.env` file (for Docker Compose):

| Variable | Description | Example |
|---|---|---|
| `DB_HOST` | MySQL host | `localhost` |
| `DB_PORT` | MySQL port | `3306` |
| `DB_NAME` | Database name | `labtodo` |
| `DB_USERNAME` | DB user | `root` |
| `DB_PASSWORD` | DB password | `my-secret-pw` |
| `DB_CREATE_IF_NOT_EXISTS` | Auto-create DB | `true` |
| `HOST_PORT` | Host port mapped to container 8080 | `8081` |
| `SPRING_PROFILES_ACTIVE` | Active profile | `prod` or `test` |
| `JAVA_OPTS` | JVM options | `-Xmx512m` |

> Never hardcode credentials in source files. The SonarCloud Security E rating is partly caused by a hardcoded BCrypt string — do not repeat this pattern.

---

## Quick Reference — Key File Locations

| What you need | Where to find it |
|---|---|
| Application entry point | `src/main/java/.../LabtodoApplication.java` |
| Task business logic | `src/main/java/.../service/TaskService.java` |
| User business logic | `src/main/java/.../service/UserService.java` |
| JSF UI controllers | `src/main/java/.../controller/` |
| Domain entities & enums | `src/main/java/.../model/` |
| JPA repositories | `src/main/java/.../data/` |
| Test base classes | `src/test/java/.../support/` |
| Test data factory | `src/test/java/.../support/TestDataBuilders.java` |
| CI/CD pipeline | `.github/workflows/ci-cd.yml` |
| SonarCloud config | `sonar-project.properties` |
| Coverage gate config | `pom.xml` → `jacoco-maven-plugin` → `check` execution |
| Docker setup | `docker-compose.yml` + `Dockerfile` |
| Deliverable 1 full report | `weekly-reports/code-smells-and-refactoring.md` |
| Deliverable 2 full report | `CSDT-2026.md` |
| Deliverable 3 full report | `weekly-reports/testing-debt.md` + `weekly-reports/code-quality.md` |
| Consolidated deliverable report | `CSDT_PrimeraEntrega2026.md` |
| Technical debt backlog | `assets/docs/backend-technical-debt.md` |
