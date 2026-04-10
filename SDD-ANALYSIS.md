# 📐 Spec-Driven Development in LabToDo Refactoring

<div align="center">

![SDD](https://img.shields.io/badge/Methodology-Spec--Driven_Development-8A2BE2?style=for-the-badge)
![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.0-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![AI Assisted](https://img.shields.io/badge/AI_Assisted-AIDLC-FF6F00?style=for-the-badge)

</div>

> _"The Specification is the new code. The engineer designs the architecture; the AI Copilot executes it."_
> — [AIDLC Lab, ECI-ARCN](https://eci-arcn.github.io/AIDLC/laboratorio.html)

---

## 📋 **Table of Contents**

- [Overview](#-overview)
- [What is Spec-Driven Development?](#-what-is-spec-driven-development)
- [SDD Applied to LabToDo](#-sdd-applied-to-labtodo)
    - [Mega-Prompt Structure](#mega-prompt-structure)
    - [User Stories as Architectural Contracts](#user-stories-as-architectural-contracts)
    - [Acceptance Criteria & BDD](#acceptance-criteria--bdd)
- [Best Practices Identified](#-best-practices-identified)
- [Advantages in This Project](#-advantages-in-this-project)
- [Challenges & Limitations](#-challenges--limitations)
- [SDD vs. Vibe Coding — Applied to LabToDo](#-sdd-vs-vibe-coding--applied-to-labtodo)
- [AI Red Teaming as a Quality Gate](#-ai-red-teaming-as-a-quality-gate)
- [Impact on Technical Debt](#-impact-on-technical-debt)
- [Lessons Learned](#-lessons-learned)
- [Authors](#-authors)
- [License](#-license)
- [Additional Resources](#-additional-resources)

---

## 🌟 **Overview**

This document analyzes the application of **Spec-Driven Development (SDD)** to the _LabToDo_ enterprise refactoring project — a **Spring Boot 3.2.0** task management system for _Universidad Escuela Colombiana de Ingeniería Julio Garavito_. The analysis covers how SDD methodology, introduced in the **AI Driven Development Life Cycle (AIDLC)** framework, can guide, constrain, and accelerate an AI-assisted refactoring initiative of a legacy codebase that accumulated significant technical debt.

The original system had **47 SOLID violations**, **89 code smells**, **156 magic strings**, and a `TaskController.java` of 527 lines mixing 15+ responsibilities. SDD provides the guardrails to reverse that systematically — rather than accidentally.

---

## 🧠 **What is Spec-Driven Development?**

**Spec-Driven Development** is an engineering methodology where development begins with structured *Mega-Prompts* acting as architectural contracts. Instead of issuing vague instructions to an AI assistant (*Vibe Coding*), the engineer explicitly defines four components before any code is generated:

| Component | Description | Example in LabToDo |
|---|---|---|
| **① Context** | Technology stack, existing architecture, team constraints | Spring Boot 3.2 + JSF/PrimeFaces + MySQL, layered architecture |
| **② Constraints** | Security rules, forbidden patterns, mandatory frameworks | No business logic in controllers; use `orElseThrow`, not `.get()` |
| **③ Acceptance Criteria** | Verifiable, measurable requirements (BDD/Gherkin) | _Given_ a valid task ID, _When_ `getTask()` is called, _Then_ it returns the task or throws `TaskNotFoundException` |
| **④ Edge Cases** | Failure scenarios: invalid input, null states, concurrency | Empty `Optional`, duplicate usernames, null `taskId` |

The result is that the AI assistant does not _design_ — it _implements_ what the engineer already specified.

---

## 🏗️ **SDD Applied to LabToDo**

### Mega-Prompt Structure

Every significant refactoring task in LabToDo can be framed as a Mega-Prompt. Below is a real example applied to the **`TaskService` unsafe Optional handling** (one of the P0 issues identified in the README):

```
ROLE: You are a Senior Java Engineer specialized in Spring Boot and Clean Code.

TECHNOLOGICAL CONTEXT: Spring Boot 3.2.0, Java 17, Spring Data JPA.
The project uses a layered architecture: Controller → Service → Repository.
Group ID: edu.eci.labinfo.

ARCHITECTURAL CONSTRAINTS:
- Never use Optional.get() without isPresent() validation.
- All not-found scenarios must throw a custom domain exception (e.g., TaskNotFoundException).
- Custom exceptions must extend RuntimeException and include the missing resource ID in the message.
- Do not add logging, metrics, or any other concern not requested.

USER STORY: As a system component, I want the TaskService.getTask(Long taskId) method
to safely retrieve a task by ID. If the task does not exist, it must throw
TaskNotFoundException with a descriptive message. Do not modify any other method.

ACCEPTANCE CRITERIA:
- Given a valid taskId, when getTask() is called, then it returns the Task object.
- Given an invalid taskId, when getTask() is called, then it throws TaskNotFoundException.

EDGE CASES:
- taskId is null → throw IllegalArgumentException before querying the repository.
- Repository returns an empty Optional → throw TaskNotFoundException.
```

This prompt produces a deterministic, reviewable, bounded output — not a speculative rewrite of unrelated code.

---

### User Stories as Architectural Contracts

In the LabToDo refactoring, each identified code smell maps directly to a bounded User Story. This creates a **one-to-one traceability** between technical debt items and specification units:

| Technical Debt Item | User Story | Constraint | Acceptance Criterion |
|---|---|---|---|
| Unsafe `Optional.get()` (8 locations) | Refactor all unsafe Optional calls in service layer | Use `orElseThrow` + custom domain exception | No `.get()` call exists without a guard |
| God Class `TaskController` (527 lines) | Extract business logic into `TaskDomainService` | SRP: one reason to change per class | Controller delegates; service contains logic |
| Magic strings (`"Administradores"`, `"inactivo"`) | Create `AppConstants` class | No string literal used more than once | Zero magic strings remain in non-constant files |
| String concatenation in loops | Replace with `StringBuilder` or `Stream.collect(joining())` | O(n) max complexity | No `+=` on `String` inside any loop |
| Business logic in `Task` entity | Move `getAllUsers()` to a mapper or service | Entities contain only state and identity | `Task.java` has no formatting or presentation logic |

---

### Acceptance Criteria & BDD

SDD enforces **Gherkin-style acceptance criteria** as the bridge between specification and test. For the LabToDo project, this manifests directly in the JUnit 5 test strategy:

```gherkin
Feature: Safe Task Retrieval

  Scenario: Task exists
    Given a task with ID 42 exists in the repository
    When getTask(42) is called
    Then the service returns the Task with ID 42

  Scenario: Task does not exist
    Given no task with ID 99 exists in the repository
    When getTask(99) is called
    Then TaskNotFoundException is thrown
    And the exception message contains "Task not found with ID: 99"

  Scenario: Null ID supplied
    When getTask(null) is called
    Then IllegalArgumentException is thrown
```

These scenarios translate directly to `@Test` methods using **JUnit 5 + Mockito + AssertJ** — the exact testing stack documented in the project README.

---

## ✅ **Best Practices Identified**

### 🔒 Constraining the AI Output Surface

The single most effective SDD practice is **restricting scope explicitly**. Phrases like _"Do not modify any other method"_ or _"refactor only the affected part"_ prevent the AI from performing unrequested rewrites that introduce regressions.

Applied to LabToDo:
- When extracting `TaskDomainService`, constrain the prompt to **only** move logic — never redesign the data model simultaneously.
- When fixing magic strings, specify **which file** to create the constants in and **which files** to update references from.

---

### 📦 One User Story, One Prompt, One Review

Each Spec unit should produce a diff small enough to be **reviewed in a single PR**. In a codebase with 47 SOLID violations, attempting to fix all of them in one AI prompt produces Frankenstein Code — exactly the anti-pattern observed in Phase 1 of the AIDLC lab.

**Recommended cadence for LabToDo:**

1. Write the Mega-Prompt for one User Story
2. Generate the code
3. Run `mvn test` — all pre-existing tests must remain green
4. Submit a focused PR with the Spec as part of the PR description
5. Repeat for the next item

---

### 🧪 Test-First Specification

Writing the acceptance criteria *before* generating implementation code serves a dual purpose: it defines the AI's success condition and it produces the test suite as a natural byproduct. For LabToDo, this means the **85% JaCoCo coverage target** is not chased retroactively — it is designed into each Spec.

---

### 📌 Versioning Specs Alongside Code

The Mega-Prompt for each refactoring unit should be committed to the repository (e.g., in `docs/specs/`) alongside the code it generated. This creates an **auditable trail** — future contributors can understand *why* a design decision was made, not just *what* was changed.

---

### 🚫 Explicit Prohibition of Anti-Patterns

Each Spec should enumerate what the AI must **not** do, not just what it must do. For LabToDo this means:

```
PROHIBITED:
- Do not use field injection (@Autowired on fields); use constructor injection.
- Do not add try-catch blocks that swallow exceptions silently.
- Do not introduce new dependencies not already in pom.xml.
- Do not modify entity classes when refactoring service logic.
```

---

## 🚀 **Advantages in This Project**

### 🎯 Precision Targeting of Technical Debt

The LabToDo README catalogs debt at three priority levels (P0, P1, P2). SDD maps naturally to this priority queue: each Spec unit targets one debt item, with no collateral changes. The result is a refactoring history that is **linear, traceable, and reversible**.

### 🤝 Team Coordination Without Merge Conflicts

With three contributors working on a shared Java codebase, unspecified AI-assisted changes are a merge conflict factory. SDD enforces **file-level boundaries per User Story**: if Andrés owns the `TaskService` Spec and Sergio owns the `UserService` Spec, there is no structural ambiguity about who changes which file.

### 📊 Measurable Progress Against Targets

The project README defines quantitative targets (e.g., cyclomatic complexity < 5, code duplication < 5%). Because each Spec produces a bounded, testable change, **SonarCloud and JaCoCo metrics update incrementally** after each merged PR — rather than fluctuating unpredictably with large, uncontrolled AI-generated rewrites.

### 🔐 Security Constraint Enforcement

`SecurityConfig.java` is explicitly excluded from JaCoCo coverage and is a sensitive file. An SDD constraint stating _"Do not modify `SecurityConfig.java` or any `@Configuration` class"_ ensures no AI session accidentally weakens the security posture while working on adjacent service logic.

### ⚡ Reduced Token Consumption

When the AI does not need to infer the stack, the architecture, the naming conventions, or the scope from ambiguous phrasing, it generates significantly fewer speculative tokens. For a project with **Spring Boot + JSF + JoinFaces + MySQL** — an uncommon combination — this matters: without explicit context, the AI defaults to REST + React patterns it has seen more often in training data.

---

## ⚠️ **Challenges & Limitations**

### 📝 Spec Authoring Overhead

Writing a complete Mega-Prompt for every refactoring unit requires upfront investment. For a project with 89 code smells, producing 89 well-formed Specs is non-trivial — especially for students balancing coursework. The practical mitigation is **Spec templates**: reusable structures where only the User Story and constraints change between iterations.

### 🧠 Domain Knowledge Required

SDD does not eliminate the need for deep domain understanding — it *concentrates* it at the specification phase. To write accurate constraints for LabToDo (e.g., which JSF lifecycle methods cannot be moved, how `@ViewScoped` interacts with Spring beans), the engineer must already understand the framework. The AI cannot compensate for gaps in that knowledge.

### 🔄 Spec Drift in Evolving Codebases

As refactoring progresses, earlier Specs become outdated. A Spec written when `TaskController` had 527 lines must be revised once `TaskDomainService` has been extracted. Without a discipline of **updating Specs after each merge**, the specification repository diverges from reality — defeating its purpose as an architectural contract.

### 🤖 AI Context Window Limitations

For the most complex God Classes (`TaskController` at 527 lines, `LoginController` at 448 lines), the full context — existing code, dependencies, and the Spec — may approach or exceed the effective AI context window for coherent generation. The mitigation is **vertical slicing**: extract one method group per Spec rather than one class per Spec.

### ⚖️ Over-Specification Risk

Extremely prescriptive Specs can prevent the AI from applying valid, superior solutions that the engineer did not anticipate. For example, specifying _"use `StringBuilder` for string joining"_ when `String.join()` or `Collectors.joining()` is more idiomatic in Java 17 produces technically compliant but suboptimal code. Specs should constrain **what**, not always **how**.

---

## 📊 **SDD vs. Vibe Coding — Applied to LabToDo**

### Architecture & Control

| Dimension | ❌ Vibe Coding in LabToDo | ✅ SDD in LabToDo |
|---|---|---|
| **Prompt** | "Refactor TaskController, it's too big" | Role + Stack + Constraints + one User Story at a time |
| **Architecture ownership** | Delegated to AI — unpredictable layer choices | Defined by team; AI executes within boundaries |
| **Dependency management** | AI may introduce new libraries or Spring modules | Explicitly prohibited unless already in `pom.xml` |
| **Scope per change** | Potentially the entire codebase | One class, one method group, one debt item |
| **Merge conflict risk** | High — multiple files changed unpredictably | Low — file-level boundaries per contributor |
| **Rollback difficulty** | High — changes are entangled | Low — each PR is a bounded, reversible unit |

### Quality & Traceability

| Dimension | ❌ Vibe Coding | ✅ SDD |
|---|---|---|
| **Test coverage tracking** | Unpredictable — coverage may drop or spike | Incremental — each Spec includes acceptance criteria |
| **SonarCloud stability** | Metrics fluctuate wildly between PRs | Metrics improve monotonically per debt item resolved |
| **PR description quality** | "Refactored some stuff" | Spec + User Story + acceptance criteria embedded |
| **Auditability** | Low — intent not documented | High — Spec is the audit trail |
| **Onboarding new contributors** | Must read the code to infer intent | Can read the Spec to understand every design decision |

---

## 🛡️ **AI Red Teaming as a Quality Gate**

The AIDLC cycle requires a **verification checkpoint** before any production deployment. For LabToDo, this translates to a structured QA prompt sent after each Spec unit is implemented:

```
CHANGE OF ROLE: You are now a Senior QA Engineer and Security Auditor.

Review the refactored TaskService code I just generated.
Identify:
1. Any remaining unsafe Optional usage.
2. Any exception handling that swallows errors silently.
3. Any SRP violation introduced (new responsibilities added unintentionally).
4. Any test case missing from the acceptance criteria provided.

Refactor ONLY the affected parts. Do not touch anything not listed above.
```

This mirrors the QA pass performed in Phase 3 of the AIDLC lab, where the AI detected the `NaN` validation gap — but only within the scope of the prompt. The lesson applies directly to LabToDo:

> **The AI Red Teaming pass is as thorough as the scope defined in the prompt.** A vague QA prompt produces a vague audit. A targeted QA prompt produces actionable, bounded corrections.

---

## 📉 **Impact on Technical Debt**

Applying SDD systematically to LabToDo is projected to produce the following outcomes, grounded in the metrics targets established in the project README:

| Metric | Before Refactoring | SDD Target | Mechanism |
|---|---|---|---|
| **Cyclomatic Complexity** (avg) | 12.4 | < 5 | One extraction per Spec; complexity verified per PR |
| **Code Duplication** | 23% | < 5% | Constants Spec + DRY constraint in every prompt |
| **SOLID Violations** | 47 | 0 | One violation resolved per User Story |
| **Code Smells** | 89 | < 10 | P0 → P1 → P2 prioritized Spec queue |
| **Magic Strings** | 156 | 0 | Single `AppConstants` Spec; global constraint applied thereafter |
| **Test Coverage** | 0% | ≥ 85% | Acceptance criteria → JUnit tests generated per Spec |

The critical insight is that SDD converts an **overwhelming** debt backlog (89 smells, 47 violations) into a **manageable queue** of bounded, specifiable, verifiable units. The debt does not shrink faster — but it shrinks *predictably*.

---

## 💡 **Lessons Learned**

### 1. 🏛️ Architecture First, Code Never

The most expensive lesson from the original LabToDo codebase is that architectural decisions made without specification — mixing concerns in `TaskController`, embedding presentation logic in `Task.java` — compound over time. SDD enforces the discipline of _deciding architecture before writing a single line of code_.

### 2. 📏 Small Specs Beat Large Specs

A Spec that tries to fix three debt items simultaneously produces code that is harder to review, harder to test, and harder to revert. The optimal Spec unit for LabToDo is **one method group in one class**, verified by **two to five acceptance criteria**.

### 3. 🔁 The Spec is a Living Document

Each Spec must be updated when the code it generated is subsequently modified. A Spec that describes code that no longer exists is worse than no Spec — it is actively misleading.

### 4. 🤖 The AI Executes; the Engineer Decides

SDD does not reduce the engineer's responsibility — it redirects it. The cognitive effort moves from _writing code_ to _writing specifications_. For LabToDo, that means the team spends more time reasoning about layer boundaries, exception hierarchies, and naming conventions — and less time debugging AI-generated spaghetti.

### 5. 🧩 SDD and SOLID are Naturally Aligned

Every SOLID principle can be encoded as an SDD constraint:

- **SRP** → `"This class may have only one reason to change. Do not add responsibilities."`
- **OCP** → `"Extend behavior via new classes; do not modify existing ones."`
- **LSP** → `"Any subclass must be substitutable for its parent without altering behavior."`
- **ISP** → `"Do not add methods to this interface that are not used by all its implementors."`
- **DIP** → `"Depend on abstractions (interfaces); never on concrete implementations directly."`

---

## 👥 **Authors**

<table>
  <tr>
    <td align="center">
      <a href="https://github.com/andresserrato2004">
        <img src="https://github.com/andresserrato2004.png" width="100px;" alt="Andrés Serrato"/>
        <br />
        <sub><b>Andrés Serrato</b></sub>
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
        <sub><b>Sergio Bejarano</b></sub>
      </a>
      <br />
      <sub>Full Stack Developer</sub>
    </td>
  </tr>
</table>

*Software Engineering Students — Universidad Escuela Colombiana de Ingeniería Julio Garavito*
*Course: Contemporary Software Development Techniques (CSDT_M) — 2025*

---

## 📄 **License**

This project is licensed under the **Apache License, Version 2.0**.

See the [LICENSE](LICENSE) file for details.

---

## 🔗 **Additional Resources**

### 🤖 Spec-Driven Development & AIDLC

- [AIDLC — AI Driven Development Life Cycle (ECI-ARCN)](https://eci-arcn.github.io/AIDLC/index.html)
- [AIDLC Practical Lab — The Butterfly Effect of Code](https://eci-arcn.github.io/AIDLC/laboratorio.html)
- [GitHub Next — Copilot Workspace (Spec to PR)](https://githubnext.com/projects/copilot-workspace)
- [Specmatic — Contract-Driven API Testing](https://specmatic.io/)
- [Cursor Rules — Project-Level AI Specifications](https://www.cursor.com/blog/cursor-rules)

### 🧱 SOLID Principles & Clean Architecture

- [SOLID Principles in Java — Baeldung](https://www.baeldung.com/solid-principles)
- [Uncle Bob — SOLID Relevance (2020)](https://blog.cleancoder.com/uncle-bob/2020/10/18/Solid-Relevance.html)
- [Clean Architecture — Robert C. Martin](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Refactoring Guru — Design Patterns](https://refactoring.guru/design-patterns)

### 🧪 BDD, Testing & Acceptance Criteria

- [Cucumber — BDD with Gherkin](https://cucumber.io/docs/gherkin/reference/)
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [AssertJ — Fluent Assertions for Java](https://assertj.github.io/doc/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)

### 📊 Technical Debt & Refactoring

- [Managing Technical Debt — Martin Fowler](https://www.martinfowler.com/bliki/TechnicalDebt.html)
- [Technical Debt Quadrant — Martin Fowler](https://martinfowler.com/bliki/TechnicalDebtQuadrant.html)
- [Refactoring: Improving the Design of Existing Code — Martin Fowler](https://martinfowler.com/books/refactoring.html)
- [Working Effectively with Legacy Code — Michael Feathers](https://www.amazon.com/Working-Effectively-Legacy-Michael-Feathers/dp/0131177052)

### 🔍 Code Quality Tools

- [SonarCloud — Continuous Code Quality](https://sonarcloud.io/)
- [JaCoCo — Java Code Coverage](https://www.jacoco.org/jacoco/)
- [SourceMaking — Code Smells Reference](https://sourcemaking.com/refactoring/smells)

---

<div align="center">
  <p>Made with 🏗️ for Software Engineering Education</p>
  <p><i>Universidad Escuela Colombiana de Ingeniería Julio Garavito</i></p>
</div>
