# 🔧 LabToDo — Enterprise Refactoring Project

<div align="center">

![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.0-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-3.9.5-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.2.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![PrimeFaces](https://img.shields.io/badge/PrimeFaces-13.0.3-F7B500?style=for-the-badge)
![JSF](https://img.shields.io/badge/JSF-3.0-007396?style=for-the-badge&logo=java&logoColor=white)
![JoinFaces](https://img.shields.io/badge/JoinFaces-5.2.0-FF6F00?style=for-the-badge)
![License](https://img.shields.io/badge/License-CC_BY--SA_4.0-blue?style=for-the-badge)

</div>

---

## 📋 Table of Contents

- [Project Overview](#-project-overview)
- [Application Screenshots](#️-application-screenshots)
- [Technology Stack](#️-technology-stack)
- [System Diagrams](#-system-diagrams)
- [Getting Started](#-getting-started)
- [CSDT\_M Course Deliverables](#-csdt_m-course-deliverables)
- [Authors](#-authors)
- [License](#-license)
- [Additional Resources](#-additional-resources)

---

## 🌟 Project Overview

**LabToDo** is a web application designed to manage tasks for the computer science laboratory at the *Universidad Escuela Colombiana de Ingeniería Julio Garavito*. It enables users to create and track pending tasks, mark them as completed, and add comments to each one. Every task records its creation date and the user who registered it.

The application features a built-in **authentication system** with role-based access control, allowing users to log in with accounts whose permissions are determined by their assigned roles (*Administrator*, *Monitor*, *Student*). This role structure provides flexible and secure task management tailored to the needs of different user types.

This repository represents an **enterprise-grade refactoring initiative** developed as part of the *Calidad de Software y Deuda Técnica* (CSDT_M) course, progressively transforming the original legacy codebase into a maintainable, testable, and professionally architected system through the application of **Clean Code principles**, **SOLID design patterns**, **TDD**, **DevSecOps**, and **architectural analysis** techniques.

---

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
| **Language** | Java (OpenJDK) | 17.0.9 |
| **Utilities** | Lombok | 1.18.30 |
| **Server** | Embedded Apache Tomcat | 10.1.16 |

> [!TIP]
> To use a different dependency version, update the corresponding entry in `pom.xml` at the project root and rebuild with `mvn clean install`.

---

## 📊 System Diagrams

### Use Case Diagram

<img src="assets/images/casos-de-uso.png" alt="Use Case Diagram" width="80%">

### Concept Diagram

<img src="assets/images/conceptos.png" alt="Concept Diagram" width="80%">

### Deployment Diagram

<img src="assets/images/despliegue.png" alt="Deployment Diagram" width="80%">

---

## 🚀 Getting Started

### ✅ Prerequisites

> [!IMPORTANT]
> Git, Java 17, Maven 3.9+, and Docker must be installed before running the project locally.

- **[Git](https://git-scm.com/)** — Version control system
- **[Java 17](https://www.oracle.com/java/technologies/downloads/#java17)** — Runtime and development kit
- **[Apache Maven 3.9+](https://maven.apache.org/)** — Build and dependency manager
- **[Docker](https://www.docker.com/)** — Container runtime (for the MySQL database)

---

### 🔧 Installation

#### 1. Clone the repository

```bash
git clone https://github.com/Laboratorio-de-Informatica/LabToDo.git
cd LabToDo
```

#### 2. Start the MySQL database

**Option A — Docker** (recommended):

```bash
docker run -p 3306:3306 --name labtodo-mysql \
  -e MYSQL_ROOT_PASSWORD=my-secret-pw \
  -d mysql:latest
```

**Option B — Existing MySQL instance**: update `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/labtodo?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=my-secret-pw
```

#### 3. Build and run the application

```bash
mvn clean compile spring-boot:run
```

The application will be available at `http://localhost:8080/login.xhtml`.

> [!NOTE]
> After the first start, register a user and grant it the `Administrator` role with `Active` account status using a database client such as [DBeaver](https://dbeaver.io/).

#### 4. Package as JAR (optional)

```bash
mvn clean package
java -jar target/labtodo.jar
```

---

## 📚 CSDT\_M Course Deliverables

This project evolves progressively through the *Calidad de Software y Deuda Técnica* (CSDT_M) course. Each deliverable applies a specific technique or methodology to identify, document, and reduce software quality issues and technical debt accumulated in the original codebase.

### 🗂️ Deliverables Index

| #     | Deliverable                        | Topics Covered | Branch | Report | Status |
|-------|------------------------------------|---|---|---|---|
| **1** | 🐛 **Code Smells & Refactoring**   | Technical debt analysis, 10 code smells, 8 refactoring patterns, SOLID principles, Clean Code, design patterns, architecture improvements | `feature/code-smells-identification` | [📄 View](weekly-reports/1-code-smells-and-refactoring.md) | ✅ Complete |
| **2** | ✨ **Clean Code & XP Practices**    | YAGNI, DRY, KISS principles; XP practices scorecard and phased implementation roadmap | `feature/clean-code-and-xp-practices` | [📄 View](weekly-reports/2-clean-code-and-xp-practices.md) | ✅ Complete |
| **3** | 🧪 **Testing Debt & Code Quality** | TDD, 187 unit tests, <u>JaCoCo 88% coverage</u>, SonarCloud integration, 3-stage CI/CD pipeline | `feature/test-scaffolding` | [📄 View](weekly-reports/3-testing-debt.md) | ✅ Complete |
| **4** | 📐 **Developer Experience**        | DevEx framework (Feedback Loops, Cognitive Load, Flow State) and SPACE methodology analysis | `docs/dev-ex-framework` | 🔄 In Review | 🔄 In Review |
| **5** | 🔒 **DevSecOps / CI-CD**           | Static analysis tools: OWASP Dependency-Check, Semgrep SAST, SonarCloud, Checkstyle, PMD | `feature/ci-enhancements` | [📄 View](weekly-reports/5-ci-cd-pipeline.md) | ✅ Complete |
| **6** | 🤖 **AI in SDLC**                  | Spec-Driven Development (SDD), AIDLC guardrails, vibe coding pitfalls, AI red teaming as quality gate | `feature/aidlc-lab` | 🔄 In Review | 🔄 In Review |
| **7** | 🏛️ **Architectural Smells**       | Architecture debt, quality attributes, functional requirements, business constraints, manual inspection | `feature/architecture-sells` | 🔄 In Review | 🔄 In Review |

---

## 👥 Authors

### 🎓 CSDT\_M Refactoring Team

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
        <sub><b>Jesús Pinzón</b></sub>
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

### 🏛️ Original Project Authors

*ECI Laboratory Monitors — 2023-2*

- **Daniel Santanilla** — [ELS4NTA](https://github.com/ELS4NTA)
- **Andrés Oñate** — [AndresOnate](https://github.com/AndresOnate)
- **David Valencia** — [DavidVal6](https://github.com/DavidVal6)
- **Angie Mojica** — [An6ie02](https://github.com/An6ie02)

---

## 📄 License

This project is licensed under the **Creative Commons Attribution-ShareAlike 4.0 International License** (CC BY-SA 4.0).

[![License: CC BY-SA 4.0](https://licensebuttons.net/l/by-sa/4.0/88x31.png)](https://creativecommons.org/licenses/by-sa/4.0/)

**You are free to**:
- ✅ **Share** — copy and redistribute the material in any medium or format
- ✅ **Adapt** — remix, transform, and build upon the material for any purpose, even commercially

**Under the following terms**:
- 📝 **Attribution** — You must give appropriate credit, provide a link to the license, and indicate if changes were made
- 🔄 **ShareAlike** — If you remix, transform, or build upon the material, you must distribute your contributions under the same license as the original

See the [LICENSE](LICENSE) file for the full license text.

---

## 🔗 Additional Resources

### 📘 Original Project

- [Original LabToDo Repository](https://github.com/Laboratorio-de-Informatica/LabToDo) — Source project by ECI lab monitors

### 🏗️ Refactoring & Architecture

- [Refactoring: Improving the Design of Existing Code](https://martinfowler.com/books/refactoring.html) — Martin Fowler
- [Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html) — Robert C. Martin
- [Refactoring Guru — Design Patterns & Code Smells](https://refactoring.guru/)
- [Hexagonal Architecture](https://alistair.cockburn.us/hexagonal-architecture/) — Alistair Cockburn

### ☕ Spring Boot & Java

- [Spring Boot Reference Documentation](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/)
- [Spring Data JPA Reference](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)
- [Baeldung — Spring Boot Tutorials](https://www.baeldung.com/spring-boot)
- [Java Design Patterns](https://java-design-patterns.com/)

### 🧪 Testing & Code Quality

- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://site.mockito.org/)
- [JaCoCo — Java Code Coverage](https://www.jacoco.org/jacoco/)
- [SonarCloud — Continuous Code Quality](https://sonarcloud.io/)
- [Test-Driven Development: By Example](https://www.amazon.com/Test-Driven-Development-Kent-Beck/dp/0321146530) — Kent Beck

### 📐 Standards & Methodologies

- [ISO/IEC 25010 — Software Product Quality](https://iso25000.com/index.php/en/iso-25000-standards/iso-25010)
- [Technical Debt](https://martinfowler.com/bliki/TechnicalDebt.html) — Martin Fowler
- [SOLID Principles in Java](https://www.baeldung.com/solid-principles) — Baeldung
- [Extreme Programming Explained](https://www.amazon.com/Extreme-Programming-Explained-Embrace-Change/dp/0321278658) — Kent Beck

---

<div align="center">
  <p><i>Universidad Escuela Colombiana de Ingeniería Julio Garavito</i></p>
  <p><i>Calidad de Software y Deuda Técnica — CSDT_M · 2026</i></p>
</div>
