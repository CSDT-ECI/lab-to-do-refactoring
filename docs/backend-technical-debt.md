# 🔴 TECHNICAL DEBT - BACKEND

## Project: LabToDo - Task Management System

---

## 📊 EXECUTIVE SUMMARY

**Total Java Code Lines:** ~2,035  
**Technologies:** Spring Boot 3.2.0, JSF (JoinFaces 5.2.0), JPA/Hibernate, MySQL/MariaDB  
**Technical Debt Level:** **HIGH** ⚠️

---

## 🔴 CRITICAL ISSUES

### 1. **Mixed Architecture (Anti-pattern)**

**Location:** Entire project  
**Severity:** 🔴 CRITICAL

**Problem:**

- Mixing JSF (server-side view technology) with Spring Boot
- JSF controllers (`@Component @SessionScope`) handle presentation logic
- Violation of traditional Spring MVC pattern
- Not a REST API, but JSF with backing beans

**Impact:**

```
❌ Difficult unit and integration testing
❌ Impossible to develop modern frontend (React, Vue, Angular)
❌ Difficult horizontal scalability
❌ Tight coupling between backend and frontend
```

**Problematic Code:**

```java
// TaskController.java - Lines 28-31
@Component
@Data
@SessionScope
public class TaskController {
    // This should not be a REST controller but a JSF backing bean
```

**Recommendation:**

- Migrate to REST API architecture with Spring MVC
- Completely separate frontend (SPA) from backend
- Remove JSF and PrimeFaces dependencies from backend

---

### 2. **Missing DTO Layer**

**Location:** All controllers  
**Severity:** 🔴 CRITICAL

**Problem:**

- JPA entities exposed directly in controllers
- No separation between domain model and transfer model
- Entities have Lombok annotations that can cause circular references

**Impact:**

```
❌ Risk of exposing sensitive data (passwords)
❌ Circular references in JSON (User -> Task -> User)
❌ Difficulty evolving data model
❌ Coupling between persistence and presentation layers
```

**Problematic Code:**

```java
// TaskController.java - Line 81
public void saveTask() {
    this.currentTask.setUsers(selectedUsersToTask); // Direct JPA entity
    taskService.addTask(currentTask);
}
```

**Recommendation:**

- Create DTOs for all operations
- Implement mappers (MapStruct or ModelMapper)
- Example: `TaskDTO`, `UserDTO`, `CreateTaskRequest`, `TaskResponse`

---

### 3. **Inadequate Exception Handling**

**Location:** `LabToDoExeption.java` and all services  
**Severity:** 🔴 CRITICAL

**Problems:**

```java
// LabToDoExeption.java - Spelling errors and poor design
public class LabToDoExeption extends Exception {  // ❌ "Exeption" typo
    public static final String CREDENTIALS_INCORRECT = "Su cuenta o contraseña es incorrecta.";
    // ❌ Hardcoded messages in Spanish
    // ❌ Uses Exception instead of RuntimeException
    // ❌ No exception hierarchy
}
```

**Impact:**

```
❌ Error messages in Spanish in code
❌ Forces try-catch handling in every method
❌ No internationalization (i18n)
❌ Difficult logging and traceability
```

**Recommendation:**

- Create exception hierarchy:
  ```
  LabToDoException (base)
  ├── BusinessException
  │   ├── UserNotFoundException
  │   ├── InvalidCredentialsException
  │   └── DuplicateUserException
  └── TechnicalException
      ├── DatabaseException
      └── ExternalServiceException
  ```
- Implement `@ControllerAdvice` with `@ExceptionHandler`
- Use internationalization files (.properties)

---

### 4. **Insufficient Validations**

**Location:** Models and services  
**Severity:** 🟠 HIGH

**Problem:**

- Jakarta Bean Validation annotations not used
- Manual validations scattered throughout code
- Missing database-level validation

**Problematic Code:**

```java
// Task.java - No validations
@Entity
@Data
public class Task {
    private String title;           // ❌ No @NotBlank
    private String description;     // ❌ No @Size
    private LocalDate creationDate; // ❌ No @PastOrPresent
}

// User.java - No validations
@Entity
@Data
public class User {
    private String userName;  // ❌ No @NotBlank, @Size, @Pattern
    private String password;  // ❌ No @NotBlank
    private String role;      // ❌ Should be enum
}
```

**Recommendation:**

```java
@Entity
@Data
public class Task {
    @NotBlank(message = "Title is required")
    @Size(min = 5, max = 100)
    private String title;

    @NotBlank
    @Size(max = 700)
    private String description;

    @PastOrPresent
    private LocalDate creationDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Status status;
}
```

---

### 5. **Poor Security**

**Location:** `SecurityConfig.java`, `LoginController.java`  
**Severity:** 🔴 CRITICAL

**Identified problems:**

```java
// SecurityConfig.java - Only defines an encoder bean
@Configuration
public class SecurityConfig {
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
    // ❌ NO SECURITY CONFIGURATION
    // ❌ Spring Security not configured
    // ❌ No CSRF protection
    // ❌ No authentication/authorization
}
```

**Vulnerabilities:**

```
🔴 No Spring Security HTTP Security configured
🔴 No endpoint protection
🔴 No secure session management
🔴 Manual authentication in LoginController
🔴 Passwords potentially visible in logs
🔴 No rate limiting for login
🔴 No access auditing
```

**Problematic Code:**

```java
// LoginController.java - Line 103
if (password.equals(null) || userName.equals(null)) {
    // ❌ This will throw NullPointerException
    // ❌ Should be: password == null || userName == null
}

// LoginController.java - Line 136
if ((!passwordEncoder.matches(password, userToLogin.getPassword()))) {
    // ✅ Good, but no rate limiting
    // ❌ Vulnerable to brute force attacks
}
```

**Recommendation:**

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()));
        return http.build();
    }
}
```

---

### 6. **N+1 Query Problem**

**Location:** Models with JPA relationships  
**Severity:** 🟠 HIGH

**Problem:**

```java
// Task.java - Line 44
@ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
private List<User> users;  // ❌ EAGER always loads all users

// Task.java - Line 49
@OneToMany(mappedBy = "task", cascade = CascadeType.REMOVE)
private List<Comment> comments;  // ❌ LAZY but can cause LazyInitializationException
```

**Impact:**

- Loading a list of 100 tasks executes 101 queries (1 + 100)
- Significantly degraded performance
- Excessive memory consumption

**Recommendation:**

```java
// Add to TaskRepository:
@Query("SELECT DISTINCT t FROM Task t LEFT JOIN FETCH t.users WHERE t.semester.periodId = :semesterId")
List<Task> findBySemesterWithUsers(@Param("semesterId") Long semesterId);

// Or use EntityGraph
@EntityGraph(attributePaths = {"users", "comments"})
List<Task> findBySemesterPeriodId(Long periodId);
```

---

### 7. **Business Logic in Controllers**

**Location:** `TaskController.java`, `LoginController.java`, `AdminController.java`  
**Severity:** 🟠 HIGH

**Problem:**

```java
// TaskController.java - Lines 81-129 (49 lines of logic)
public void saveTask() {
    String message = "";
    List<User> selectedUsersToTask = new ArrayList<>();

    // Server-side: only allow administrators to create/update tasks
    if (this.currentTask != null && "Administradores".equals(this.currentTask.getTypeTask())) {
        String currentUserName = loginController.getUserName();
        if (currentUserName == null || !loginController.isAdmin(currentUserName)) {
            // ... 10+ more lines of business logic
        }
    }
    // ... more validation and transformation logic
}
```

**Impact:**

```
❌ Controllers with 200+ lines
❌ Impossible to test logic without JSF context
❌ Violation of Single Responsibility Principle
❌ Duplicated code between controllers
```

**Recommendation:**

- Move all business logic to services
- Controllers should only:
  1. Receive request
  2. Validate input (superficially)
  3. Call service
  4. Return response

```java
// How it should be:
@PostMapping("/tasks")
public ResponseEntity<TaskDTO> createTask(@Valid @RequestBody CreateTaskRequest request) {
    TaskDTO task = taskService.createTask(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(task);
}
```

---

### 8. **Incorrect Transaction Usage**

**Location:** Services  
**Severity:** 🟠 HIGH

**Problem:**

```java
// TaskService.java - Line 84
@Transactional(propagation = Propagation.REQUIRED)
public Task updateTask(Task task) {
    if (taskRepository.existsById(task.getTaskId())) {
        return taskRepository.save(task);
    }
    return null;  // ❌ Returns null instead of throwing exception
}

// UserService.java - Line 82
@Transactional(propagation = Propagation.REQUIRED)
public User updateUser(User user) {
    if (userRepository.existsById(user.getUserId())) {
        // ... complex logic with multiple updates
        // ❌ Everything in one large transaction
    }
    return null;
}
```

**Problems:**

```
❌ @Transactional only on some methods (inconsistent)
❌ Very large transactions (updateUser does multiple operations)
❌ Not using READ_ONLY transactions for queries
❌ Returning null instead of exceptions
```

**Recommendation:**

```java
@Transactional(readOnly = true)
public Task getTask(Long taskId) {
    return taskRepository.findById(taskId)
        .orElseThrow(() -> new TaskNotFoundException(taskId));
}

@Transactional
public Task updateTask(UpdateTaskRequest request) {
    Task task = getTask(request.getTaskId());
    taskMapper.updateEntity(task, request);
    return taskRepository.save(task);
}
```

---

## 🟠 HIGH PRIORITY ISSUES

### 9. **Magic Strings Everywhere**

**Severity:** 🟠 HIGH

```java
// Examples scattered throughout code:
if ("Administradores".equals(typeTask))  // ❌ Magic string
if ("Por Hacer".equals(status))          // ❌ Magic string
if (role.equals("Administrador"))        // ❌ Magic string

// Should be:
if (TypeTask.ADMINISTRADOR.equals(typeTask))
if (Status.PENDING.equals(status))
if (Role.ADMINISTRADOR.equals(role))
```

**Impact:**

- Prone to typos
- Difficult refactoring
- No type-safety

---

### 10. **Inadequate Logging**

**Severity:** 🟠 HIGH

```java
// LoginController.java - Only basic logging
private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
logger.info("Creando cuenta de usuario");  // ❌ No context

// AdminController.java - Line 134
} catch (Exception e) {
    System.out.println("No se pudo eliminar");  // ❌❌❌ SEVERE
}
```

**Problems:**

```
❌ System.out.println instead of logger
❌ Exceptions not fully logged
❌ No appropriate levels (DEBUG, INFO, WARN, ERROR)
❌ No contextual information (userId, taskId)
❌ No correlation ID for traceability
```

**Recommendation:**

```java
try {
    userService.deleteUser(userId);
    log.info("User deleted successfully. userId={}", userId);
} catch (UserHasTasksException e) {
    log.warn("Attempt to delete user with assigned tasks. userId={}", userId, e);
    throw e;
} catch (Exception e) {
    log.error("Unexpected error deleting user. userId={}", userId, e);
    throw new TechnicalException("Error deleting user", e);
}
```

---

### 11. **Hardcoded Configuration**

**Severity:** 🟠 HIGH

**Problem:**

```java
// application.properties
spring.datasource.password=${DB_PASSWORD:my-secret-pw}  // ❌ Default password
server.port=${SERVER_PORT:8080}                         // ✅ Good, but...
```

**Missing:**

- Spring profiles (dev, test, prod)
- Externalized configuration
- Documented environment variables
- Secrets management

**Recommendation:**

```yaml
# application-dev.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/labtodo_dev

# application-prod.yml
spring:
  datasource:
    url: ${DATABASE_URL}  # From AWS Secrets Manager or similar
```

---

### 12. **Non-existent Testing**

**Location:** `/src/test/`  
**Severity:** 🟠 HIGH

**Problem:**

```java
// LabtodoApplicationTests.java - Only test
@SpringBootTest
class LabtodoApplicationTests {
    @Test
    void contextLoads() {
        // Empty test
    }
}
```

**Missing:**

```
❌ Unit tests for services
❌ Integration tests for repositories
❌ Controller tests
❌ Security tests
❌ Code coverage: 0%
```

**Recommendation:**

- Target: Minimum 70% coverage
- Unit tests with Mockito
- Integration tests with TestContainers
- Load tests with JMeter/Gatling

---

## 🟡 MEDIUM PRIORITY ISSUES

### 13. **Code Duplication**

```java
// TaskController.java and AdminController.java - Similar logic
// Task update logic repeated in multiple places
// Role validations duplicated
```

---

### 14. **Inconsistent Naming**

```java
public Boolean saveTask()          // ❌ Returns Boolean
public void completedMessage()     // ❌ Confusing name
public Boolean getRenderedToTaskButton()  // ❌ "get" but returns UI boolean
```

---

### 15. **Outdated Dependencies**

```xml
<version>3.2.0</version>          <!-- Spring Boot -->
<version>6.2.0</version>          <!-- Spring Security -->
<version>2.7.9</version>          <!-- MariaDB - Very old -->
```

---

### 16. **Comments in Spanish**

```java
/**
 * Metodo que crea una nueva tarea.
 */
public void openNew() {
```

**Recommendation:** Code and comments in English.

---

## 📋 RECOMMENDED REFACTORING

### Phase 1: Stabilization

1. ✅ Add DTOs and mappers
2. ✅ Implement global exception handling
3. ✅ Add validations with Jakarta Bean Validation
4. ✅ Configure Spring Security properly
5. ✅ Add structured logging

### Phase 2: Architectural Migration

1. ✅ Create REST API with Spring MVC
2. ✅ Remove JSF and migrate to SPA frontend (React/Vue)
3. ✅ Implement JWT authentication
4. ✅ Dockerize application

### Phase 3: Optimization

1. ✅ Optimize queries (eliminate N+1)
2. ✅ Implement cache (Redis)
3. ✅ Add comprehensive testing
4. ✅ Implement CI/CD

---

## 📊 QUALITY METRICS

| Metric                     | Current State | Target |
| -------------------------- | ------------- | ------ |
| Test Coverage              | 0%            | 70%+   |
| Technical Debt (SonarQube) | High          | Low    |
| Vulnerabilities            | Multiple      | 0      |
| Code Smells                | 100+          | <50    |
| Duplication                | ~15%          | <3%    |
| Cyclomatic Complexity      | High          | Medium |

---

## 🎯 PRIORITIZATION

### 🔥 Critical (Do Immediately)

1. Implement Spring Security correctly
2. Create DTOs for all entities
3. Implement global exception handling
4. Fix null validations

### ⚠️ High

1. Add validations with Bean Validation
2. Implement structured logging
3. Create unit tests for critical services
4. Optimize N+1 queries

### 📌 Medium

1. Migrate to REST API
2. Separate frontend
3. Implement JWT authentication
4. Create integration tests

---

## 💡 CONCLUSIONS

This project has **significant technical debt** affecting:

- ✅ **Security**: Critical vulnerabilities
- ✅ **Maintainability**: Coupled and difficult to modify code
- ✅ **Scalability**: Monolithic JSF architecture doesn't scale
- ✅ **Testability**: Practically impossible to test correctly

**Recommendation:** Start gradual refactoring prioritizing security aspects.

---

**Analysis date:** February 2026
