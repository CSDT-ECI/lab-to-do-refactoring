# Testing Debt Analysis

## Table of Contents

- [Overview](#overview)
- [Testing Debt Practices Identified](#testing-debt-practices-identified)
- [Code Examples of Debt — Before State](#code-examples-of-debt--before-state)
- [How the Debt Was Addressed](#how-the-debt-was-addressed)
- [Testing Strategy](#-testing-strategy)
- [Unit Test Implementation](#-unit-test-implementation)
- [Coverage Summary](#-coverage-summary)
- [Techniques Applied to Increase Coverage](#-techniques-applied-to-increase-coverage)
- [Proposed Scenarios to Increase Coverage Further](#proposed-scenarios-to-increase-coverage-further)

---

## Overview

At the start of this lab, the project had **no unit tests implemented whatsoever**. The `src/test/` directory existed with structural scaffolding — base classes, templates, and configuration — but every test class was empty: not a single `@Test` method had been written. Effective code coverage was **0%**.

This represented a direct risk to the project's maintainability: any refactoring could silently introduce regressions with no automated safety net to catch them.

---

## Testing Debt Practices Identified

The following **testing debt** anti-patterns were identified in the project at the start of the refactoring:

### 1. Empty Test Infrastructure (Dead Scaffolding)

**Practice**: Test classes and base classes existed but contained zero `@Test` methods. The scaffolding gave a false sense of security.

**Impact**: 0% effective coverage despite the presence of a `src/test/` directory. CI pipelines would pass trivially with no assertions.

**Debt category**: _Missing tests / phantom coverage_

---

### 2. No Negative-Path Tests

**Practice**: Even after partial test additions, only happy paths were considered. Edge cases such as non-existent entity lookups, duplicate username registration, or invalid state transitions had no corresponding tests.

**Impact**: Services could throw unhandled exceptions in production that were never caught during development.

**Debt category**: _Insufficient test coverage — error paths ignored_

---

### 3. Controller Coupling to Static Platform APIs

**Practice**: JSF controllers called `FacesContext.getCurrentInstance()` and `PrimeFaces.current()` directly — static singletons that cannot be mocked with standard Mockito.

```java
// DEBT: direct static call — untestable without a running Faces container
FacesContext.getCurrentInstance().addMessage(null,
    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", message));

PrimeFaces.current().executeScript("PF('loginDialog').show()");
```

**Impact**: Controller logic was impossible to test in isolation. Any test required a full application server or was simply skipped.

**Debt category**: _Untestable design / tight coupling to framework internals_

---

### 4. Integration Tests With No `@Test` Methods

**Practice**: The four `*RepositoryIT` integration test classes had the full Spring + H2 + `@Transactional` infrastructure configured, but no actual test scenarios were written inside them.

```java
// DEBT: infrastructure ready, but zero test methods
@SpringBootTest
@Transactional
class TaskRepositoryIT extends BaseIntegrationTest {
    // nothing here
}
```

**Impact**: JPA query correctness, JPQL named queries, and cascade behaviour were never validated automatically.

**Debt category**: _Incomplete tests / infrastructure without assertions_

---

### 5. Missing Test-Data SQL Scripts

**Practice**: Integration tests referenced SQL seed scripts (e.g., `/test-data.sql`) but those scripts did not exist, making integration tests fail at startup if actually executed.

**Impact**: Any attempt to run integration tests against a real schema would fail before a single assertion, blocking CI.

**Debt category**: _Broken test environment / missing fixtures_

---

### 6. No Mutation Testing

**Practice**: The project relied solely on line coverage as a quality proxy. Mutation testing (e.g., with PIT) was never configured. Line coverage can reach 80% with weak assertions that never fail on logic errors.

**Impact**: Tests may pass even when the production code logic is subtly wrong (e.g., inverted conditions, off-by-one errors).

**Debt category**: _Inadequate test quality metrics_

---

## Code Examples of Debt — Before State

### Before: `TaskService` — no repository delegation tests

```java
// In production code (TaskService.java):
public Task updateTask(Task task) {
    return taskRepository.save(task);
}

// In test code (TaskServiceTest.java) — BEFORE:
// (empty class — no @Test methods at all)
```

### Before: `LoginController` — no testable message dispatch

```java
// DEBT — direct static API, impossible to assert in a unit test:
public void showError(String summary, String detail) {
    FacesContext context = FacesContext.getCurrentInstance();
    context.addMessage(null,
        new FacesMessage(FacesMessage.SEVERITY_ERROR, summary, detail));
}
```

---

## How the Debt Was Addressed

The decision was made to implement unit tests from scratch, building on the existing scaffolding and honouring the conventions already established in the base classes:

- **AAA pattern** (_Arrange / Act / Assert_) in every test method.
- **Naming convention** `shouldDoSomething` / `shouldNotDoSomethingWhenCondition`.
- **FIRST principles**: tests are Fast, Independent, Repeatable, Self-validating, and Timely.
- **`TestDataBuilders`**: centralised static factory for constructing domain objects without coupling tests to each other.

The controller coupling debt was resolved at the **design level** by introducing a `PrimeFacesWrapper` injectable component during the refactoring, making the static call replaceable with a mock.

---

## 🧪 Testing Strategy

The repository now includes concrete testing scaffolding for AAA, FIRST, and `should` or `shouldNot` naming conventions.

- `./mvnw test` for unit tests and fast smoke tests.
- `./mvnw verify` for unit, integration, and acceptance tests.

### Code Coverage (JaCoCo)

Generate tests + coverage report:

```bash
./mvnw clean test verify
```

Where to see the results:

- HTML report (recommended): `target/site/jacoco/index.html`
- CSV report: `target/site/jacoco/jacoco.csv`
- XML report: `target/site/jacoco/jacoco.xml`

Quick line-coverage value in terminal:

```bash
awk -F, 'NR>1{m+=$8; c+=$9} END{printf "LINE_COVERAGE=%.2f%%\n", (c*100)/(m+c)}' target/site/jacoco/jacoco.csv
```

### Test Pyramid

```
        ╱╲
       ╱  ╲ E2E Tests (10%)
      ╱────╲ Acceptance Tests
     ╱      ╲ Integration Tests (30%)
    ╱────────╲ Unit Tests (60%)
   ╱──────────╲
```

### Unit Tests

**Target**: 60% of total tests, 95% coverage of business logic

```java
@ExtendWith(MockitoExtension.class)
class TaskStateTransitionServiceTest {
    
    @Mock
    private TaskRepository taskRepository;
    
    @Mock
    private ApplicationEventPublisher eventPublisher;
    
    @InjectMocks
    private TaskStateTransitionService service;
    
    @Test
    void shouldTransitionLabTaskToFinishedAndEnrichWithContributors() {
        // Given
        Task labTask = createLabTask(Status.REVIEW);
        List<User> contributors = createMockContributors();
        
        when(commentRepository.findUsersWhoCommented(labTask.getId()))
            .thenReturn(contributors);
        when(taskRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        
        // When
        Task result = service.transitionTo(labTask, Status.FINISHED);
        
        // Then
        assertThat(result.getStatus()).isEqualTo(Status.FINISHED);
        assertThat(result.getParticipants()).hasSize(contributors.size());
        verify(eventPublisher).publishEvent(any(TaskStateChangedEvent.class));
    }
    
    @Test
    void shouldThrowExceptionWhenInvalidTransition() {
        // Given
        Task task = createTask(Status.PENDING);
        
        // When & Then
        assertThatThrownBy(() -> service.transitionTo(task, Status.FINISHED))
            .isInstanceOf(InvalidStateTransitionException.class)
            .hasMessageContaining("Cannot transition from PENDING to FINISHED");
    }
}
```

### Integration Tests

**Target**: 30% of total tests

```java
@SpringBootTest
@AutoConfigureMockMvc
@Sql("/test-data.sql")
class TaskRestControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    void shouldCreateTaskAndReturnCreated() throws Exception {
        CreateTaskRequest request = CreateTaskRequest.builder()
            .title("Integration Test Task")
            .description("Test description")
            .type(TypeTask.LABORATORY)
            .build();
        
        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.title").value("Integration Test Task"));
    }
}
```

### Test Coverage Goals

| Component | Coverage Target |
|-----------|----------------|
| Domain Services | 95% |
| Application Services | 90% |
| REST Controllers | 80% |
| Repositories | 70% |
| Mappers | 85% |

**Tools**: JUnit 5, Mockito, AssertJ, Spring Boot Test, H2 (in-memory DB for tests)

---

## ✅ Unit Test Implementation

### Implemented test structure

```
src/test/java/edu/eci/labinfo/labtodo/
├── support/
│   ├── BaseUnitTest.java           — base for unit tests (Mockito only, no Spring context)
│   ├── BaseIntegrationTest.java    — base for integration tests (@SpringBootTest + H2 + @Transactional)
│   └── TestDataBuilders.java       — reusable domain object factory
├── unit/
│   ├── model/                      — domain entity tests
│   │   ├── CommentTest.java
│   │   ├── EnumsAndExceptionTest.java
│   │   ├── SemesterTest.java
│   │   ├── TaskTest.java
│   │   └── UserTest.java
│   ├── service/                    — service tests with mocked repositories
│   │   ├── CommentServiceTest.java
│   │   ├── SemesterServiceTest.java
│   │   ├── TaskServiceTest.java
│   │   └── UserServiceTest.java
│   └── controller/                 — JSF controller tests with mocked services
│       ├── AdminControllerTest.java
│       ├── LoginControllerTest.java
│       ├── SemesterControllerTest.java
│       └── TaskControllerTest.java
└── integration/
    └── data/                       — scaffolding ready for JPA repository tests
        ├── CommentRepositoryIT.java
        ├── SemesterRepositoryIT.java
        ├── TaskRepositoryIT.java
        └── UserRepositoryIT.java
```

### Scenarios covered per layer

#### Model layer — `unit/model/` (28 tests)

The intrinsic behaviour of each domain entity was validated independently of any framework.

| Class                   | Scenarios covered                                                                                                                                          |
| ----------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `CommentTest`           | Construction with and without an associated task; date formatting; `equals`, `hashCode`, and `toString` via Lombok                                         |
| `TaskTest`              | Default status on construction (`Por Hacer`); adding users and comments; concatenated user text; `equals`, `hashCode`, and `toString`                      |
| `UserTest`              | Adding tasks to a user; all-args constructor; `equals`, `hashCode`, and `toString`                                                                         |
| `SemesterTest`          | Storing dates and name; full constructor; `equals`, `hashCode`, and `toString`                                                                             |
| `EnumsAndExceptionTest` | Case-insensitive value lookup for `Status`, `TypeTask`, `TopicTask`, `Role`, and `AccountType`; `Status.next()` transition; `LabToDoExeption` construction |

#### Service layer — `unit/service/` (54 tests)

Each service was tested in isolation by mocking its repository with Mockito. Both happy paths and error cases were covered.

| Class                 | Scenarios covered                                                                                                                                                                                                                                       |
| --------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `TaskServiceTest`     | Full CRUD; queries by user, status, type, semester, and filter combinations; retrieval of users who commented; `null` return when updating a non-existent task                                                                                          |
| `UserServiceTest`     | Registration with password encoding; rejection on duplicate username (`LabToDoExeption`); lookup by username and full name; automatic assignment of admin tasks when role changes to Administrator; skip if user already assigned; deletion by username |
| `SemesterServiceTest` | Full CRUD; lookup by name; active semester by current date (`LocalDate.now()`); `null` return when no result exists                                                                                                                                     |
| `CommentServiceTest`  | Full CRUD; listing by task; `null` return when updating a non-existent comment; `NoSuchElementException` thrown when looking up a non-existent ID                                                                                                       |

#### JSF controller layer — `unit/controller/` (105 tests)

JSF controllers present the highest testing complexity due to their dependency on `FacesContext` and `PrimeFaces` — static platform APIs. This challenge was solved by combining Mockito's `MockedStatic` with the `PrimeFacesWrapper` already introduced during the refactoring.

| Class                    | Scenarios covered                                                                                                                                                                                                                                                                                                 |
| ------------------------ | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `TaskControllerTest`     | Available task types by role; button message by task status; conditional button rendering by role and status; task loading by active or selected semester; task save and update; state advancement (`completedMessage`); comment saving; unknown username handling; null task and semester edge cases             |
| `LoginControllerTest`    | Full login flow: non-existent user, unverified account, wrong password, blocking account states, and successful access; account creation and saving with strong/weak password validation; password change (request, match, format validation); logout and redirect; `getRedirectPath` for admin and monitor roles |
| `AdminControllerTest`    | Task state changes for selected tasks (empty, monitor type, lab type on finish); user role changes; account type changes with transition validation; user deletion with error resilience; button messages based on selection count; state accessors and mutators                                                  |
| `SemesterControllerTest` | Opening a new period; active semester verification; listing as `SelectItem`; save with date validation (start > end blocked); create vs. update based on ID presence; accessors and mutators                                                                                                                      |

---

## 📊 Coverage Summary

| Layer                     | Tests implemented | What is covered                                      |
| ------------------------- | ----------------- | ---------------------------------------------------- |
| Model (entities)          | 28                | Construction, immutability, Lombok contracts         |
| Services                  | 54                | Happy paths + error cases + repository delegation    |
| JSF Controllers           | 105               | UI flows, validations, redirects, account states     |
| Integration (scaffolding) | 4 classes ready   | Infrastructure configured with H2 + `@Transactional` |
| **Total `@Test` methods** | **187**           |                                                      |

> The integration classes (`*RepositoryIT`) have the full infrastructure in place and are ready to receive `@Test` methods in future iterations, once the corresponding SQL test-data scripts are defined.

---

## 🔑 Techniques Applied to Increase Coverage

1. **`MockedStatic`** for `FacesContext` and `PrimeFaces`: allowed testing JSF controller logic without spinning up an application server or a real Faces context.

2. **`PrimeFacesWrapper`** (introduced during the refactoring): replacing the direct `PrimeFaces.current()` call with an injectable wrapper made controllers fully testable with an ordinary `@Mock`.

3. **`TestDataBuilders`**: centralises test object construction, keeps the _Arrange_ block expressive, and eliminates setup duplication across test classes.

4. **`BaseUnitTest` and `BaseIntegrationTest`**: abstract classes that encapsulate JUnit/Mockito and Spring context configuration respectively, ensuring consistency and reducing boilerplate in every test class.

5. **Explicit negative-path tests**: for every service operation or controller action, at least one `shouldNot...` scenario was written to verify behaviour on invalid inputs, non-existent entities, or disallowed state transitions.

---

## Proposed Scenarios to Increase Coverage Further

The following test scenarios are proposed for the next iteration to close the remaining gaps:

### Repository Integration Tests (High Priority)

The `*RepositoryIT` classes have full Spring + H2 + `@Transactional` infrastructure ready. Add `@Test` methods for:

| Repository | Proposed scenarios |
|------------|-------------------|
| `TaskRepositoryIT` | Find by status; find by semester; find tasks with comments; cascade delete of associated comments |
| `UserRepositoryIT` | Find by username; find by role; ensure unique username constraint is enforced at DB level |
| `CommentRepositoryIT` | Find comments by task; ordering by date; cascade behaviour when parent task is deleted |
| `SemesterRepositoryIT` | Find by name; find overlapping date ranges; active semester query with edge dates |

### Mutation Testing with PIT

Configure PIT Mutation Testing to validate the quality of existing assertions:

```xml
<!-- pom.xml -->
<plugin>
    <groupId>org.pitest</groupId>
    <artifactId>pitest-maven</artifactId>
    <version>1.15.3</version>
    <dependencies>
        <dependency>
            <groupId>org.pitest</groupId>
            <artifactId>pitest-junit5-plugin</artifactId>
            <version>1.2.1</version>
        </dependency>
    </dependencies>
    <configuration>
        <targetClasses>
            <param>edu.eci.labinfo.labtodo.service.*</param>
        </targetClasses>
        <targetTests>
            <param>edu.eci.labinfo.labtodo.unit.service.*</param>
        </targetTests>
    </configuration>
</plugin>
```

Run with: `./mvnw org.pitest:pitest-maven:mutationCoverage`

### Acceptance / E2E Tests (Future Iteration)

Once a REST API layer is introduced (as proposed in the technical debt roadmap), add Cucumber/RestAssured acceptance tests covering full user flows end-to-end.
