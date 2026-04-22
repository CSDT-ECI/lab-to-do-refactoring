# 🏛️ Architectural Smells Analysis

> **Weekly Report — LabToDo Refactoring Project**
> Analysis date: 2026-04-18 · Project: *LabToDo Refactoring* (Spring Boot + JSF/PrimeFaces + JoinFaces)
> Related to: [README.md](../README.md) · Branch: `feature/architecture-sells`

---

## Table of Contents

- [Overview](#-overview)
- [Evaluation Criteria](#-evaluation-criteria)
- [Architectural Smells Identified](#-architectural-smells-identified)
  - [Smell A — Architecture by Implication / Strict Layers Violated](#smell-a--architecture-by-implication--strict-layers-violated)
  - [Smell B — Feature Concentration (God Controllers)](#smell-b--feature-concentration-god-controllers)
  - [Smell C — Implicit Cross-Module Dependency](#smell-c--implicit-cross-module-dependency)
  - [Smell D — Vendor Lock-In & Framework Leakage](#smell-d--vendor-lock-in--framework-leakage)
  - [Smell E — Scattered Parasitic Functionality](#smell-e--scattered-parasitic-functionality)
  - [Smell F — Jumble / Packages Not Clearly Named](#smell-f--jumble--packages-not-clearly-named)
  - [Smell G — Ambiguous / Unsafe Service Contracts](#smell-g--ambiguous--unsafe-service-contracts)
- [Summary Scorecard](#-summary-scorecard)
- [Authors](#-authors)
- [Additional Resources](#-additional-resources)

---

## 🌟 Overview

An **architectural smell** is a structural, high-level design problem that primarily impacts **maintainability**, **evolvability**, and **testability** — qualities that compound over time. Unlike code smells (which affect individual methods or classes), architectural smells affect entire layers, modules, or cross-cutting concerns. Late detection significantly increases the cost of correction.

This analysis inspects the LabToDo codebase — a JSF monolith built with Spring Boot, PrimeFaces, and JoinFaces — against the *Sharma Architectural Smell Taxonomy* and the *ISO/IEC 25010* quality model. The approach combines **automated static analysis** (SonarCloud) with **manual architectural inspection**, since not all known smells can be detected by tools alone.

Seven architectural smells were identified, spanning four severity levels. Each finding includes file-level evidence, architectural impact, and a concrete remediation action.

---

## 🔍 Evaluation Criteria

The inspection examined the unified project view for the following structural signals:

- **Cross-layer coupling** — dependencies that bypass declared architectural boundaries.
- **Responsibility concentration** — components accumulating multiple unrelated concerns.
- **Context boundary violations** — UI logic leaking into domain, or infrastructure leaking into services.
- **Cross-cutting rule duplication** — the same business rule enforced in multiple, disconnected locations.
- **Framework entanglement** — application logic that cannot be exercised without a running framework container.

---

## 🏚️ Architectural Smells Identified

---

### Smell A — Architecture by Implication / Strict Layers Violated

**Related taxonomy (Sharma)**: *Architecture by Implication*, *No Layers*, *Strict Layers Violated*
**Severity**: 🔴 **High**

#### Evidence

The view layer invokes business logic and lifecycle callbacks directly on controllers, bypassing any application service boundary:

| Location | Violation |
|---|---|
| `META-INF/resources/dashboard.xhtml` line 8 | `preRenderView` calls `taskController.onDatabaseLoaded(...)` — data loading in the view event |
| `META-INF/resources/admindashboard.xhtml` line 13 | `taskController.onControlLoaded()` invoked at render time |
| `dashboard.xhtml` line 213 / `admindashboard.xhtml` line 201 | `actionListener` bound directly to `taskController.saveTask` |

Controllers are declared `@SessionScope`, collapsing UI state, flow control, and business logic into a single component:

```java
// TaskController.java — line 31
@Component
@SessionScope   // ← UI lifecycle, flow state, and business rules co-located
public class TaskController { ... }

// LoginController.java — line 25
@Component
@SessionScope
public class LoginController { ... }
```

#### Architectural Impact

- Presentation and application concerns are inseparable, making isolated testing of business rules impossible without a running JSF container.
- Migration to an API-first or REST architecture requires rewriting the entire boundary.

#### Recommended Action

Define explicit architectural boundaries: *UI adapter → application service → domain service → repository*. Reduce backing beans to UI orchestration only and relocate all business rules to dedicated application or domain services.

---

### Smell B — Feature Concentration (God Controllers)

**Related taxonomy (Sharma)**: *Feature Concentration*, *Too Large Subsystems*
**Severity**: 🔴 **High**

#### Evidence

Three controllers accumulate multiple unrelated responsibilities, far exceeding a single-purpose design:

| Class | Lines | Responsibilities mixed |
|---|---|---|
| `controller/LoginController.java` | **389** | Authentication, account creation, password change, role-based redirect, UI messaging |
| `controller/AdminController.java` | **280** | Task state management, user role assignment, account type transitions, bulk operations |
| `controller/TaskController.java` | **275** | Task CRUD, semester loading, state transitions, comment handling, role-based rendering |

Methods with multiple distinct responsibilities:

```java
// TaskController.java — line 81
public void saveTask() {
    // 1. Determines task type
    // 2. Assigns users based on type and role
    // 3. Sets semester
    // 4. Persists via service
    // 5. Resets UI state
    // → five responsibilities in one method
}

// LoginController.java — line 100
public String login() {
    // 1. Validates existence
    // 2. Checks account status
    // 3. Verifies password
    // 4. Updates session
    // 5. Emits UI messages
    // 6. Redirects by role
}
```

#### Architectural Impact

- A single rule change in any of these responsibilities requires modifying a large, high-risk class, increasing regression probability.
- Cyclomatic complexity remains elevated (average CC of 12.4 across controllers), raising the cognitive cost of every code review.

#### Recommended Action

Partition controllers by use case: `CreateTaskUseCase`, `ChangeTaskStateUseCase`, `AuthenticateUserUseCase`, `ChangePasswordUseCase`. Maintain controllers as thin adapters that delegate to single-responsibility application services.

---

### Smell C — Implicit Cross-Module Dependency

**Related taxonomy (Sharma)**: *Implicit Cross-Module Dependency*, *Subsystem-API Bypassed*
**Severity**: 🔴 **High**

#### Evidence

`TaskController` holds a direct compile-time dependency on `LoginController` to access authentication context:

```java
// TaskController.java — line 47
@Autowired
private LoginController loginController;   // ← controller depends on controller

// TaskController.java — line 81+
public void saveTask() {
    String username = loginController.getLoggedUser().getUsername();
    // Business rule (task assignment) is coupled to UI session state
}
```

#### Architectural Impact

- The task management and authentication modules are coupled through an undeclared, cross-cutting dependency. A change to `LoginController`'s session model directly breaks `TaskController`.
- Migrating to a stateless REST API requires simultaneously refactoring both controllers, multiplying the scope and risk of the change.

#### Recommended Action

Introduce a `CurrentUserContext` or `AuthFacade` in the application layer that exposes the authenticated user identity independently of the JSF session mechanism. Remove all direct controller-to-controller references.

---

### Smell D — Vendor Lock-In & Framework Leakage

**Related taxonomy (Sharma)**: *Vendor Lock-In*
**Severity**: 🟠 **Medium-High**

#### Evidence

Application and controller logic is tightly coupled to *PrimeFaces*-specific static APIs, bypassing any abstraction boundary:

```java
// LoginController.java — multiple invocations (lines 76, 87, 92, 106, 115, 124, ...)
PrimeFaces.current().executeScript("PF('loginDialog').show()");
PrimeFaces.current().ajax().update("messages");
```

Although `PrimeFacesWrapper` was introduced to enable mocking in tests, its abstraction is shallow — it still delegates to the static singleton without hiding the PrimeFaces API contract:

```java
// service/PrimeFacesWrapper.java — line 8
@Component
public class PrimeFacesWrapper {
    public void executeScript(String script) {
        PrimeFaces.current().executeScript(script);   // thin delegation, no port defined
    }
}
```

The view layer is also structurally dependent on PrimeFaces-specific component contracts (`PF(...)`, `<p:commandButton>`, `<p:dialog>`) throughout all XHTML templates.

#### Architectural Impact

- Replacing or upgrading the PrimeFaces version requires changes in controller logic, not only view templates.
- UI side-effects (script execution, AJAX partial updates) are embedded inside application logic, making the boundary between UI behaviour and business rules invisible.

#### Recommended Action

Define UI notification ports (interfaces such as `UINotificationService`) in the application layer and implement them as PrimeFaces adapters. Business logic should call the port contract, not the framework implementation directly.

---

### Smell E — Scattered Parasitic Functionality

**Related taxonomy (Sharma)**: *Scattered Parasitic Functionality*
**Severity**: 🟠 **Medium-High**

#### Evidence

The business rule for task state transitions and participant tracking (when a `LABORATORIO` task reaches `TERMINADO`, its user list is overwritten with the users who commented) is implemented in at least two disconnected locations:

```java
// TaskController.java — line 134 (completedMessage)
public String completedMessage(Task task) {
    // Re-implements state transition check
    // Re-implements commentor enrichment logic
}

// AdminController.java — lines 43 and 54 (modifyStateTaks)
public void modifyStateTaks() {
    // Duplicate state machine check
    // Duplicate enrichment of task user list
}
```

Task assignment rules are additionally split between the controller and a service:

```java
// TaskController.saveTask() — line 81+
// Determines assignment strategy based on TypeTask

// UserService.assignAdminTasksToUser(...) — UserService.java
// Handles ADMINISTRADORES-type assignment separately
```

#### Architectural Impact

- A change to the `LABORATORIO` task completion rule must be replicated in at least two controllers and one service, creating a high risk of functional inconsistency.
- The state machine has no canonical, authoritative location — its logic is observable only by reading all controllers simultaneously.

#### Recommended Action

Centralise all state transitions in a single domain service (e.g., `TaskStateTransitionService`). Implement the `Status` progression using the *Strategy* or *State* pattern, making each transition an explicit, independently testable unit.

---

### Smell F — Jumble / Packages Not Clearly Named

**Related taxonomy (Sharma)**: *Jumble*, *Packages Not Clearly Named*
**Severity**: 🟡 **Medium**

#### Evidence

Infrastructure and configuration artefacts are placed inside the `service` package, obscuring the boundary between domain services and technical infrastructure:

```
service/
├── CommentService.java       — domain service ✅
├── TaskService.java          — domain service ✅
├── UserService.java          — domain service ✅
├── SemesterService.java      — domain service ✅
├── SecurityConfig.java       — infrastructure configuration ⚠️ misplaced
└── PrimeFacesWrapper.java    — UI adapter / infrastructure ⚠️ misplaced
```

Domain entities also carry presentation-layer methods, violating the *Single Responsibility Principle* at the model boundary:

```java
// model/Task.java — lines 83–88
public String getDateText() { ... }    // ← formatting concern in a domain entity
public String getAllUsers() { ... }    // ← UI display string in a domain entity

// Used directly in the view:
// admindashboard.xhtml — line 129: filterBy="#{task.getAllUsers()}"
```

#### Architectural Impact

- Package boundaries do not reflect architectural intent, increasing the cognitive overhead of navigation and onboarding.
- Domain entities carry formatting responsibilities that will need to be duplicated or adapted if a non-JSF interface (REST, CLI) is added.

#### Recommended Action

Reorganise packages to reflect explicit architectural layers: `domain/`, `application/`, `infrastructure/`, `api/`. Extract formatting and display concerns from entities into dedicated *Presenter* or *DTO* classes consumed only by the view layer.

---

### Smell G — Ambiguous / Unsafe Service Contracts

**Related taxonomy (Sharma)**: *Ambiguous Interfaces*, *Unstable Interface*
**Severity**: 🟡 **Medium**

#### Evidence

Service methods call `Optional.get()` without guarding against the empty case, creating implicit contracts that callers cannot reason about statically:

```java
// TaskService.java — line 30
public Task getTaskById(Long id) {
    return taskRepository.findById(id).get();   // ← throws NoSuchElementException silently
}

// SemesterService.java — line 28
public Semester getSemesterByName(String name) {
    return semesterRepository.findByName(name).get();   // ← same anti-pattern

// CommentService.java — line 27
public Comment getCommentById(Long id) {
    return commentRepository.findById(id).get();   // ← same anti-pattern
}
```

Additionally, several `update*` methods return `null` when the target entity does not exist, creating an ambiguous contract that forces callers to perform null-checks without any documented semantics:

```java
// Across multiple services:
public Task updateTask(Task task) {
    if (taskRepository.existsById(task.getTaskId())) {
        return taskRepository.save(task);
    }
    return null;   // ← null sentinel: meaning is implicit, not expressed in the signature
}
```

#### Architectural Impact

- Runtime `NoSuchElementException` exceptions propagate to the controller layer with no typed, catchable contract, making error handling ad hoc and inconsistent.
- `null`-returning update methods force each caller to re-implement presence checks independently, scattering defensive logic across the codebase.

#### Recommended Action

Replace all `Optional.get()` calls with `.orElseThrow(() -> new EntityNotFoundException(...))`. Replace `null`-returning methods with `Optional<T>` return types or typed domain exceptions, establishing an explicit and uniform error contract across the service layer.

---

## 📊 Summary Scorecard

| # | Smell | Taxonomy (Sharma) | Severity | Affected Files | Priority |
|---|---|---|---|---|---|
| **A** | Architecture by Implication / No Layers | *Architecture by Implication*, *Strict Layers Violated* | 🔴 High | `TaskController`, `LoginController`, all XHTML views | 1 — Foundational |
| **B** | Feature Concentration (God Controllers) | *Feature Concentration*, *Too Large Subsystems* | 🔴 High | `LoginController` (389 lines), `AdminController` (280), `TaskController` (275) | 2 — Daily developer cost |
| **C** | Implicit Cross-Module Dependency | *Implicit Cross-Module Dependency*, *Subsystem-API Bypassed* | 🔴 High | `TaskController` → `LoginController` (line 47) | 3 — Blocks API migration |
| **D** | Vendor Lock-In & Framework Leakage | *Vendor Lock-In* | 🟠 Medium-High | `LoginController`, `PrimeFacesWrapper`, all XHTML views | 4 — Migration risk |
| **E** | Scattered Parasitic Functionality | *Scattered Parasitic Functionality* | 🟠 Medium-High | `TaskController`, `AdminController`, `UserService` | 5 — Consistency risk |
| **F** | Jumble / Packages Not Clearly Named | *Jumble*, *Packages Not Clearly Named* | 🟡 Medium | `service/SecurityConfig`, `service/PrimeFacesWrapper`, `model/Task` | 6 — Onboarding friction |
| **G** | Ambiguous / Unsafe Service Contracts | *Ambiguous Interfaces*, *Unstable Interface* | 🟡 Medium | `TaskService`, `SemesterService`, `CommentService` (8 `Optional.get()` calls) | 7 — Runtime reliability |

> **Key finding**: Smells A, B, and C form a compound structural problem — the absence of defined layers (A) enables God Controllers to form (B), which in turn creates implicit cross-module couplings (C). Resolving A creates the precondition for resolving B and C with significantly lower risk.

---

## 👥 Authors

<table>
  <tr>
    <td align="center">
      <a href="https://github.com/andresserrato2004">
        <img src="https://github.com/andresserrato2004.png" width="100px;" alt="Andrés Serrato"/>
        <br />
        <sub><b>Andrés Serrato Camero</b></sub>
      </a>
      <br />
      <sub>Full Stack Developer</sub>
    </td>
    <td align="center">
      <a href="https://github.com/JAPV-X2612">
        <img src="https://github.com/JAPV-X2612.png" width="100px;" alt="Jesús Alfonso Pinzón Vega"/>
        <br />
        <sub><b>Jesús Alfonso Pinzón Vega</b></sub>
      </a>
      <br />
      <sub>Full Stack Developer</sub>
    </td>
    <td align="center">
      <a href="https://github.com/SergioBejarano">
        <img src="https://github.com/SergioBejarano.png" width="100px;" alt="Sergio Bejarano"/>
        <br />
        <sub><b>Sergio Andrés Bejarano Rodríguez</b></sub>
      </a>
      <br />
      <sub>Full Stack Developer</sub>
    </td>
  </tr>
</table>

---

## 🔗 Additional Resources

### Architectural Smell Taxonomies

- [A Taxonomy of Software Smells](https://tusharma.in/smells/) — Tushar Sharma, *tusharma.in*
- [Designite — Architectural Smell Detection](https://www.designite-tools.com/) — designite-tools.com
- [CAST Highlight — Rapid Application Portfolio Analysis](https://www.castsoftware.com/products/highlight) — castsoftware.com

### Architecture Frameworks & Patterns

- [TOGAF — System Engineering Checklist](https://www.opengroup.org/architecture/togaf7-doc/arch/p4/comp/clists/syseng.htm) — opengroup.org
- [Improving Application Architecture — Microsoft Patterns & Practices](https://docs.microsoft.com/en-us/previous-versions/msp-n-p/ff647464(v=pandp.10)) — microsoft.com
- [Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html) — Robert C. Martin
- [Hexagonal Architecture (Ports & Adapters)](https://alistair.cockburn.us/hexagonal-architecture/) — Alistair Cockburn

### Static Analysis

- [SonarCloud — Continuous Code Quality](https://sonarcloud.io/) — sonarcloud.io
- [ISO/IEC 25010 — Software Product Quality](https://iso25000.com/index.php/en/iso-25000-standards/iso-25010) — iso25000.com

### Refactoring

- [Refactoring: Improving the Design of Existing Code](https://martinfowler.com/books/refactoring.html) — Martin Fowler
- [Refactoring Guru — Architectural Patterns](https://refactoring.guru/design-patterns) — refactoring.guru
