---
layout: home
title: "LabToDo — Enterprise Refactoring Project"
description: "Enterprise-grade refactoring initiative transforming a legacy Spring Boot JSF monolith into a clean, testable system. CSDT_M · Universidad ECI · 2026."
list_title: "📰 Technical Blog"
---

<div class="hero-banner">
  <h1>🔧 LabToDo — Enterprise Refactoring</h1>
  <p class="hero-subtitle">
    Transforming a legacy <strong>Spring Boot + JSF/PrimeFaces</strong> monolith into a maintainable,
    testable, and professionally architected system — through seven progressive engineering disciplines.
  </p>
  <div class="hero-badges">
    <img src="https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 17">
    <img src="https://img.shields.io/badge/Spring_Boot-3.2.0-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" alt="Spring Boot 3.2">
    <img src="https://img.shields.io/badge/JaCoCo-88%25_Coverage-2dba4e?style=for-the-badge" alt="88% Coverage">
    <img src="https://img.shields.io/badge/SonarCloud-Monitored-F3702A?style=for-the-badge&logo=sonarcloud&logoColor=white" alt="SonarCloud">
    <img src="https://img.shields.io/badge/CI%2FCD-GitHub_Actions-2088FF?style=for-the-badge&logo=githubactions&logoColor=white" alt="GitHub Actions">
    <img src="https://img.shields.io/badge/License-CC_BY--SA_4.0-blue?style=for-the-badge" alt="CC BY-SA 4.0">
  </div>
  <a href="https://github.com/CSDT-ECI/lab-to-do-refactoring" class="hero-cta">View on GitHub →</a>
</div>

## 🌟 Project Overview

**LabToDo** is a web application built for the computer science laboratory at *Universidad Escuela Colombiana de Ingeniería Julio Garavito*. It manages tasks, user assignments, and semester tracking with a role-based authentication system (*Administrator*, *Monitor*, *Student*).

This repository represents an **enterprise-grade refactoring initiative** developed throughout the *Calidad de Software y Deuda Técnica* (CSDT_M) course. Seven progressively advanced engineering disciplines were applied to measure, document, and reduce the technical debt accumulated in the original codebase.

The starting point was stark: **0% test coverage**, **89 code smells**, **47 SOLID violations**, **156 magic strings**, and a `TaskController.java` of 527 lines mixing 15+ unrelated responsibilities. Each deliverable addressed a measurable dimension of that debt.

## 📈 Key Metrics

<div class="metrics-grid">
  <div class="metric-card">
    <div class="metric-icon">🧪</div>
    <span class="metric-value">88%</span>
    <span class="metric-label">JaCoCo Line Coverage</span>
  </div>
  <div class="metric-card">
    <div class="metric-icon">✅</div>
    <span class="metric-value">187</span>
    <span class="metric-label">Unit Tests Written</span>
  </div>
  <div class="metric-card">
    <div class="metric-icon">🔒</div>
    <span class="metric-value">6</span>
    <span class="metric-label">CI/CD Pipeline Stages</span>
  </div>
  <div class="metric-card">
    <div class="metric-icon">📚</div>
    <span class="metric-value">7</span>
    <span class="metric-label">CSDT_M Deliverables</span>
  </div>
</div>

## 📚 CSDT_M Course Deliverables

| # | Deliverable | Key Topics | Status | Report |
|---|---|---|---|---|
| **1** | 🐛 **Code Smells & Refactoring** | 10 code smells identified, 8 refactoring patterns, SOLID principles, design patterns | ✅ Complete | [View](https://github.com/CSDT-ECI/lab-to-do-refactoring/blob/main/weekly-reports/1-code-smells-and-refactoring.md) |
| **2** | ✨ **Clean Code & XP Practices** | YAGNI, DRY, KISS principles; XP practices scorecard; phased implementation roadmap | ✅ Complete | [View](https://github.com/CSDT-ECI/lab-to-do-refactoring/blob/main/weekly-reports/2-clean-code-and-xp-practices.md) |
| **3** | 🧪 **Testing Debt & Code Quality** | TDD from 0% → 88% coverage, 187 unit tests, JaCoCo gate, SonarCloud integration | ✅ Complete | [View](https://github.com/CSDT-ECI/lab-to-do-refactoring/blob/main/weekly-reports/3-testing-debt.md) |
| **4** | 🧑‍💻 **Developer Experience** | DevEx framework (Feedback Loops, Cognitive Load, Flow State), SPACE methodology | ✅ Complete | [View](https://github.com/CSDT-ECI/lab-to-do-refactoring/blob/main/weekly-reports/4-dev-ex-and-space-frameworks.md) |
| **5** | 🔒 **DevSecOps / CI-CD** | OWASP Dependency-Check, Semgrep SAST, SonarCloud, 6-stage diamond pipeline | ✅ Complete | [View](https://github.com/CSDT-ECI/lab-to-do-refactoring/blob/main/weekly-reports/5-ci-cd-pipeline.md) |
| **6** | 🤖 **AI in SDLC** | Spec-Driven Development (SDD), AIDLC guardrails, vibe coding pitfalls, AI red teaming | ✅ Complete | [View](https://github.com/CSDT-ECI/lab-to-do-refactoring/blob/main/weekly-reports/6-sdd-analysis.md) |
| **7** | 🏛️ **Architectural Smells** | 7 architectural smells, Sharma taxonomy, ISO/IEC 25010, manual inspection + SonarCloud | ✅ Complete | [View](https://github.com/CSDT-ECI/lab-to-do-refactoring/blob/main/weekly-reports/7-architectural-smells.md) |

## 🛠️ Technology Stack

| Layer | Technology | Version |
|---|---|---|
| **Backend Framework** | Spring Boot | 3.2.0 |
| **View Layer** | JavaServer Faces *(JSF)* | 3.0 |
| **UI Components** | PrimeFaces | 13.0.3 |
| **JSF–Spring Integration** | JoinFaces | 5.2.0 |
| **Persistence** | Spring Data JPA + Hibernate | 3.2.0 |
| **Database** | MySQL | 8.2.0 |
| **Security** | Spring Security Crypto | 6.2.0 |
| **Build Tool** | Apache Maven | 3.9.5 |
| **Language** | Java (OpenJDK Temurin) | 17 |
| **Test Framework** | JUnit 5 + Mockito | — |
| **Coverage** | JaCoCo | 0.8.11 |
| **Static Analysis** | SonarCloud + Semgrep SAST + OWASP | — |
| **CI/CD** | GitHub Actions | — |
| **Containerization** | Docker + Docker Compose | — |

## 📊 System Diagrams

### Use Case Diagram

<div class="diagram-container">
  <img src="{{ site.baseurl }}/assets/images/casos-de-uso.png" alt="Use Case Diagram — LabToDo role-based task management" width="85%">
  <p class="diagram-caption">Role-based task management: Administrator, Monitor, and Student interactions with the system.</p>
</div>

### Domain Concept Diagram

<div class="diagram-container">
  <img src="{{ site.baseurl }}/assets/images/conceptos.png" alt="Domain Concept Diagram — LabToDo core entities" width="85%">
  <p class="diagram-caption">Core domain entities: Task, User, Comment, and Semester — and their relationships.</p>
</div>

### Deployment Diagram

<div class="diagram-container">
  <img src="{{ site.baseurl }}/assets/images/despliegue.png" alt="Deployment Diagram — Docker-based infrastructure" width="85%">
  <p class="diagram-caption">Docker-based deployment: Spring Boot application container connected to a MySQL database.</p>
</div>

## 👥 Authors

### 🎓 CSDT_M Refactoring Team

<div class="author-grid">
  <div class="author-card">
    <a href="https://github.com/andresserrato2004">
      <img src="https://github.com/andresserrato2004.png" alt="Andrés Serrato">
      <span class="author-name">Andrés Serrato</span>
    </a>
    <span class="author-role">Full Stack Developer</span>
  </div>
  <div class="author-card">
    <a href="https://github.com/JAPV-X2612">
      <img src="https://github.com/JAPV-X2612.png" alt="Jesús Alfonso Pinzón Vega">
      <span class="author-name">Jesús Pinzón</span>
    </a>
    <span class="author-role">Full Stack Developer</span>
  </div>
  <div class="author-card">
    <a href="https://github.com/SergioBejarano">
      <img src="https://github.com/SergioBejarano.png" alt="Sergio Bejarano">
      <span class="author-name">Sergio Bejarano</span>
    </a>
    <span class="author-role">Full Stack Developer</span>
  </div>
</div>

### 🏛️ Original Project — ECI Lab Monitors (2023-2)

| Name | GitHub | Role |
|---|---|---|
| Daniel Santanilla | [@ELS4NTA](https://github.com/ELS4NTA) | Original Developer |
| Andrés Oñate | [@AndresOnate](https://github.com/AndresOnate) | Original Developer |
| David Valencia | [@DavidVal6](https://github.com/DavidVal6) | Original Developer |
| Angie Mojica | [@An6ie02](https://github.com/An6ie02) | Original Developer |

## 🔗 Additional Resources

- [Original LabToDo Repository](https://github.com/Laboratorio-de-Informatica/LabToDo) — Source project by ECI lab monitors
- [Refactoring: Improving the Design of Existing Code](https://martinfowler.com/books/refactoring.html) — Martin Fowler
- [Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html) — Robert C. Martin
- [ISO/IEC 25010 — Software Product Quality](https://iso25000.com/index.php/en/iso-25000-standards/iso-25010)
- [JaCoCo — Java Code Coverage](https://www.jacoco.org/jacoco/)
- [SonarCloud — Continuous Code Quality](https://sonarcloud.io/)
- [AIDLC Lab — Spec-Driven Development](https://eci-arcn.github.io/AIDLC/laboratorio.html)

---

*Universidad Escuela Colombiana de Ingeniería Julio Garavito · Calidad de Software y Deuda Técnica — CSDT_M · 2026*
