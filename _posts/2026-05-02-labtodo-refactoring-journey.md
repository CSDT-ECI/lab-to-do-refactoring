---
layout: post
title: "From 0% Coverage to Clean Architecture: The LabToDo Refactoring Journey"
date: 2026-01-15
permalink: /journey/
categories: [refactoring, software-quality]
tags: [spring-boot, clean-code, tdd, devsecops, solid, architecture]
description: >-
  A technical retrospective on applying seven software quality disciplines —
  Code Smells, Clean Code, TDD, DevEx, DevSecOps, AI in SDLC, and Architectural
  Smells — to transform a legacy Spring Boot JSF monolith into a professionally
  engineered system.
---

This post documents the complete technical journey of the **LabToDo Enterprise Refactoring** project, developed throughout the *Calidad de Software y Deuda Técnica* (CSDT_M) course at *Universidad Escuela Colombiana de Ingeniería Julio Garavito* in 2026. Seven progressive engineering disciplines were applied to a real legacy codebase, each one addressing a different dimension of software quality debt.

---

## The Starting Point — A Legacy in Crisis

The original **LabToDo** application was a task management system built for the university's computer science laboratory. Functionally it worked: users could log in, create tasks by semester, assign them to monitors, and track progress from *Pending* → *In Progress* → *Review* → *Done*. But beneath that surface, the codebase had accumulated years of unchecked debt.

The inventory before refactoring began was sobering:

| Dimension | Measurement |
|---|---|
| Test coverage | **0%** (infrastructure existed, no `@Test` methods) |
| Code smells | **89** (SonarCloud) |
| SOLID violations | **47** (manual inspection) |
| Magic strings | **156+** |
| Cyclomatic complexity (avg) | **12.4** |
| `TaskController.java` size | **527 lines, 15+ responsibilities** |
| `LoginController.java` size | **389 lines** |

The goal was not to rewrite from scratch — it was to apply disciplined engineering practices to reduce that debt measurably, deliverable by deliverable.

---

## Deliverable 1 — Code Smells & Refactoring

The first step was to *name the problem*. Code smells are symptoms: they don't break the build, but they silently erode maintainability. Ten high-impact smells were documented with precise file and line evidence:

- **Unsafe `Optional.get()`** — 8+ locations across all services called `.get()` without checking `.isPresent()`, each a potential `NoSuchElementException` in production.
- **God Classes** — `TaskController` (527 lines, 15 responsibilities), `LoginController` (389 lines), `AdminController` (280 lines).
- **Magic Strings** — 156 raw literals like `"Administradores"`, `"inactivo"`, `"sin verificar"` scattered throughout the codebase, making renaming or localization impossible.
- **String Concatenation in Loops** — O(n²) complexity in `Task.getAllUsers()`, building a single display string via repeated `+` operations on a mutable collection.
- **Business Logic in Entities** — Domain objects performing display formatting, violating SRP.
- **Silent Exception Swallowing** — Empty `catch` blocks in `AdminController` (lines 58–62) hiding real failures.

Eight refactoring patterns were applied — **Extract Method**, **Extract Class**, **Replace Magic Literal**, **Introduce Parameter Object**, **Replace Conditional with Polymorphism**, **Introduce Null Object**, **Replace Error Code with Exception**, and **Decompose Conditional** — each paired with before/after code comparisons demonstrating the improvement.

The canonical fix for the most dangerous smell:

```java
// ❌ Before — potential crash on every call
public Task getTask(Long taskId) {
    return taskRepository.findById(taskId).get();
}

// ✅ After — explicit, informative failure
public Task getTask(Long taskId) {
    return taskRepository.findById(taskId)
        .orElseThrow(() -> new TaskNotFoundException(taskId));
}
```

---

## Deliverable 2 — Clean Code & XP Practices

Deliverable 2 evaluated the codebase against three core Clean Code principles and scored Extreme Programming (XP) practice adoption.

**YAGNI** (*You Aren't Gonna Need It*): The domain model showed good restraint — `Task` entities contained only fields required by actual features. However, `TaskRepository` accumulated speculative query methods never called by any service, a direct YAGNI violation tracked for removal.

**DRY** (*Don't Repeat Yourself*): Session attribute retrieval logic appeared independently in `TaskController`, `AdminController`, and `LoginController`. Role-checking conditions were duplicated across view XHTML files. A centralized `AuthService` and shared constants class were identified as the correct remediation path.

**KISS** (*Keep It Simple, Stupid*): Password validation used a nested regex with 6 simultaneous conditions in a single `if` block. Task state transition logic duplicated the ordinal check across three separate methods. Guard clauses and the `Status.next()` enum method were the prescribed simplifications.

The XP scorecard identified **Test-Driven Development** and **Continuous Integration** as the highest-priority practices to adopt — both directly addressed by subsequent deliverables.

---

## Deliverable 3 — Testing Debt & Code Quality

This was the most impactful deliverable in measurable terms. The project went from **0% effective test coverage** to **88% line coverage** — surpassing the 85% JaCoCo gate — through 187 new unit tests organized across three layers.

### The Six Testing Debt Anti-Patterns

Six specific testing debt practices were identified and addressed:

1. **Dead Scaffolding** — Base test classes existed with zero `@Test` methods, creating a false sense of security in CI.
2. **No Negative-Path Tests** — Only happy paths were considered, leaving exception branches completely uncovered.
3. **Controller Coupling to Static APIs** — `FacesContext.getCurrentInstance()` and `PrimeFaces.current()` made controllers untestable without a running JSF container.
4. **`@Test`-less Integration Test Classes** — 4 `*RepositoryIT` classes had full H2 + Spring setup but no test scenarios.
5. **Object Construction Duplication** — Test classes each rebuilt domain objects independently, making refactoring fragile.
6. **No Coverage Gate** — The build never enforced a minimum coverage threshold.

### The Solutions

**For controller testability**, a `PrimeFacesWrapper` injectable service was introduced — a thin delegation class around `PrimeFaces.current()`. Tests could then mock the wrapper via Mockito instead of starting a real Faces container. Remaining static calls were handled with `MockedStatic` from `mockito-inline`.

**For test organization**, three support classes were created:
- `BaseUnitTest` — Mockito-only base, no Spring context loaded
- `BaseIntegrationTest` — `@SpringBootTest` + H2 + `@Transactional`
- `TestDataBuilders` — Centralized factory for all domain objects

**Coverage results:**

| Layer | Tests Written | Coverage Achieved |
|---|---|---|
| Model (entities, enums) | 28 | > 95% |
| Service (business logic) | 54 | > 92% |
| Controller (JSF beans) | 105 | > 85% |
| **Total** | **187** | **88%** |

<img src="{{ site.baseurl }}/assets/images/ci-cd/01-jacoco-unit-test-coverage.png" alt="JaCoCo coverage report showing 88% line coverage" width="90%">

<img src="{{ site.baseurl }}/assets/images/ci-cd/02-sonar-cloud-project-metrics.png" alt="SonarCloud project quality metrics dashboard" width="90%">

---

## Deliverable 4 — Developer Experience

Deliverable 4 evaluated the project through two complementary productivity frameworks.

### DevEx — Three Core Dimensions

The **DevEx framework** (Noda, Storey, Forsgren & Greiler, 2023) measures developer productivity across three dimensions:

<img src="{{ site.baseurl }}/assets/images/dev-ex/01-devex-three-dimensions.png" alt="DevEx Three Core Dimensions: Feedback Loops, Cognitive Load, Flow State" width="80%">

**Feedback Loops** improved dramatically: the CI pipeline with JaCoCo gate, SonarCloud, and automated test execution gives developers quantifiable quality signals within minutes of each push — down from a multi-day manual review cycle.

**Cognitive Load** was reduced through structural conventions: `BaseUnitTest`, `BaseIntegrationTest`, and `TestDataBuilders` eliminated repetitive setup scaffolding. The mirrored package structure (`unit/model`, `unit/service`, `unit/controller`) makes navigating the test suite predictable.

**Flow State** remains partially blocked: JSF's tight coupling to `FacesContext` still requires `MockedStatic` ceremony in controller tests, creating friction that a REST-first architecture would eliminate.

### SPACE — Five Productivity Dimensions

<img src="{{ site.baseurl }}/assets/images/dev-ex/02-space-five-dimensions.png" alt="SPACE Framework: Satisfaction, Performance, Activity, Communication, Efficiency" width="80%">

The **SPACE framework** (Forsgren et al., 2021) confirmed that raw *Activity* metrics (commits, PRs) are insufficient measures of productivity. The most significant finding was that *Efficiency* — measured as pipeline runtime — averaged **~2m 5s**, well within the sub-5-minute target for fast feedback loops.

<img src="{{ site.baseurl }}/assets/images/dev-ex/03-pipeline-feedback-loop.png" alt="CI/CD pipeline feedback loop timing diagram" width="90%">

---

## Deliverable 5 — DevSecOps / CI-CD

The pipeline was transformed from a single-stage compilation check into a **6-stage diamond dependency graph** enforcing security and quality at every layer.

```
           ┌──────────┐
           │ 1. Build │
           └────┬─────┘
     ┌──────────┼──────────┬──────────┐
     ▼          ▼          ▼          ▼
  2. Test   3. Sonar  4. OWASP  5. Semgrep
     │          │          │          │
     └──────────┴──────────┴──────────┘
                      │
                      ▼
               6. Deploy
               (gates on Test ✅)
```

Jobs 2–5 execute in **parallel** after a shared build artifact is produced by Job 1, maximizing throughput. The entire pipeline completes in **under 5 minutes** on average.

**Tools integrated:**

| Stage | Tool | Purpose |
|---|---|---|
| 3 | SonarCloud | Code quality, security hotspots, duplications |
| 4 | OWASP Dependency-Check | Known CVEs in Maven dependencies |
| 5 | Semgrep SAST | Pattern-based static security analysis |

<img src="{{ site.baseurl }}/assets/images/ci-cd/04-full-pipeline-execution.png" alt="GitHub Actions pipeline execution — all 6 stages" width="90%">

<img src="{{ site.baseurl }}/assets/images/ci-cd/06-owasp-dependency-check.png" alt="OWASP Dependency Check results" width="90%">

<img src="{{ site.baseurl }}/assets/images/ci-cd/07-sast-semgrep-summary.png" alt="Semgrep SAST scan summary" width="90%">

**Concurrency control** was added to cancel in-progress runs when a new push arrives on the same branch — preventing stale results from reaching reviewers and saving GitHub Actions minutes.

---

## Deliverable 6 — AI in SDLC

Deliverable 6 analyzed the application of **Spec-Driven Development (SDD)**, the engineering methodology introduced by the AIDLC framework, to the refactoring initiative.

The central contrast was between *Vibe Coding* — issuing vague instructions to an AI assistant and accepting whatever comes out — and *Spec-Driven Development*, where the engineer defines four explicit components before any code is generated:

| Component | Role |
|---|---|
| **Context** | Technology stack, architecture, constraints |
| **Constraints** | Forbidden patterns, mandatory frameworks, naming rules |
| **Acceptance Criteria** | Verifiable, measurable requirements in BDD/Gherkin |
| **Edge Cases** | Failure scenarios, invalid inputs, null states |

### The Practical Lab — Vibe Coding vs. SDD

To make the contrast tangible, the deliverable included a hands-on laboratory: building an **Agile Carbon Footprint Calculator** using pure HTML, CSS, and JavaScript — first through unguided Vibe Coding, then through structured Spec-Driven Development. The artifact is small by design; the point is the *process*, not the product.

#### Phase 1: Vibe Coding — The Butterfly Effect

The lab began with a deliberately vague prompt: *"Make me a nice web page for a carbon footprint calculator. Make it calculate things and look modern."* The initial output looked polished — a textbook example of why Vibe Coding is seductive.

![Vibe Coding — initial lazy prompt result: looks functional, but structurally fragile]({{ site.baseurl }}/aidlc-practical-lab/assets/gifs/01-lazy-prompt.gif)

Three unguided follow-up requests — adding a chart, a PDF export, and switching to monthly calculations — triggered the *Butterfly Effect*: each prompt forced the AI to make drastic architectural decisions in isolation, stacking incoherent layers on top of each other. The PDF export silently injected `html2pdf.js` from an external CDN without authorization, and exported an invisible document because the dark CSS styles were captured as-is against a white PDF background. The chart was eventually removed, but the broken export logic persisted untouched as unresolved debt.

![The Collapse — three unguided iterations produced Frankenstein code with five mixed responsibilities at global scope]({{ site.baseurl }}/aidlc-practical-lab/assets/gifs/02-collapse.gif)

The final `script.js` mixed five distinct responsibilities — calculation, DOM manipulation, localStorage persistence, PDF export, and visual theming — at global scope with no module boundaries. Every new requirement had destroyed a piece of the prior architecture.

#### Phase 2: Spec-Driven Development — Controlled Growth

The second attempt started with a strict Mega-Prompt: role, technological constraints (no frameworks, no CDN libraries, one file), and a single bounded User Story with explicit acceptance criteria. The result was minimal, correct, and verifiable.

![SDD result — light mode: minimal, correct, and matching the User Story contract exactly]({{ site.baseurl }}/aidlc-practical-lab/assets/images/01-light-carbon-footprint-calculator.png)

A second User Story was added in the same chat — dark mode via a CSS class on the `body`. Crucially, the AI added the feature *additively*: the pre-existing calculation logic was untouched, and the dark mode handler was exactly one event listener and a handful of CSS rules — nothing more.

![SDD result — dark mode added non-destructively: prior logic intact, scope bounded by the Spec]({{ site.baseurl }}/aidlc-practical-lab/assets/images/02-dark-carbon-footprint-calculator.png)

In this project, the `CLAUDE.md` file served as the primary mega-prompt for the entire LabToDo refactoring: it defined the allowed technology stack, coding standards (e.g., "never call `Optional.get()` directly"), test writing rules (AAA blocks, `BaseUnitTest` extension, `TestDataBuilders` usage), and explicit guardrails (no `@SpringBootTest` in unit tests, no magic strings, no speculative methods).

**AI red teaming** was applied as a quality gate: after generating each refactored component, a second AI pass stress-tested the design against the documented edge cases — catching gaps before they reached the test suite.

The key finding: SDD does not reduce engineering judgment — it *preserves and amplifies* it by ensuring the AI executes a pre-designed specification rather than improvising one.

---

## Deliverable 7 — Architectural Smells

The final deliverable applied the **Sharma Architectural Smell Taxonomy** and the **ISO/IEC 25010** quality model to perform a full architectural inspection — combining SonarCloud automated findings with manual structural analysis.

Seven architectural smells were identified:

| Smell | Severity | Evidence |
|---|---|---|
| **A — Layers Violated** | 🔴 High | View layer calls business logic directly via `preRenderView` |
| **B — God Controllers** | 🔴 High | `LoginController` (389L), `AdminController` (280L), `TaskController` (275L) |
| **C — Implicit Cross-Module Dependency** | 🔴 High | `TaskController` directly injects `LoginController` |
| **D — Vendor Lock-In / Framework Leakage** | 🟡 Medium | JSF/PrimeFaces APIs in business logic |
| **E — Scattered Parasitic Functionality** | 🟡 Medium | Role-checking logic duplicated across layers |
| **F — Ambiguous Packages (Jumble)** | 🟠 Medium-Low | Flat package structure with no architectural intent |
| **G — Unsafe Service Contracts** | 🟡 Medium | `Optional.get()` returns null-risky contracts |

The most impactful finding — **Smell A** — reveals that the JSF architecture couples the *when* (view lifecycle events) with the *what* (business logic execution) in a way that cannot be untangled without a layer boundary. `@SessionScope` controllers collapse UI state, flow control, and business rules into a single object with no separation point.

The recommended target architecture follows a clean four-layer structure:
```
Presentation  →  Application  →  Domain  →  Infrastructure
(REST/JSF)       (Use cases)    (Entities)   (JPA/Security)
```

---

## Key Takeaways

Seven deliverables, one conclusion: **software quality is a continuous, measurable discipline — not a one-time cleanup**.

- **Name the debt first.** You cannot reduce what you have not measured. Code smell catalogues, SOLID violation counts, and cyclomatic complexity averages are not bureaucracy — they are the baseline without which refactoring has no direction.
- **Tests are load-bearing.** The move from 0% to 88% coverage was not just a metric improvement; it created the safety net that makes every future refactoring reversible.
- **Shift security left.** Embedding OWASP, Semgrep, and SonarCloud into every push means security findings surface at development time, not at deployment time.
- **Developer experience is an engineering concern.** Pipeline speed, cognitive load from test scaffolding, and flow interruptions are measurable and improvable — the DevEx and SPACE frameworks give language and metrics to a problem teams usually address informally.
- **Architecture smells are the most expensive debts.** God classes are a code smell. A god class that is also `@SessionScope` and injects another `@SessionScope` controller is an architectural smell — the remediation cost is an order of magnitude higher, and it compounds with every new feature built on top.

---

## 👥 Authors

**CSDT_M Refactoring Team — Universidad ECI · 2026**

- [Andrés Serrato](https://github.com/andresserrato2004)
- [Jesús Alfonso Pinzón Vega](https://github.com/JAPV-X2612)
- [Sergio Bejarano](https://github.com/SergioBejarano)

**Original Project — ECI Lab Monitors (2023-2)**

- [Daniel Santanilla](https://github.com/ELS4NTA), [Andrés Oñate](https://github.com/AndresOnate), [David Valencia](https://github.com/DavidVal6), [Angie Mojica](https://github.com/An6ie02)

## 🔗 Additional Resources

- [Full deliverable reports](https://github.com/CSDT-ECI/lab-to-do-refactoring/tree/main/weekly-reports)
- [Refactoring Guru — Design Patterns & Code Smells](https://refactoring.guru/)
- [SPACE Framework paper — Forsgren et al. (2021)](https://queue.acm.org/detail.cfm?id=3454124)
- [AIDLC Lab — Spec-Driven Development](https://eci-arcn.github.io/AIDLC/laboratorio.html)
- [Sharma Architectural Smell Taxonomy](https://doi.org/10.1007/s10664-019-09786-9)
