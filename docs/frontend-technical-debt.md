# 🔴 TECHNICAL DEBT - FRONTEND

## Project: LabToDo - Task Management System

---

## 📊 EXECUTIVE SUMMARY

**Technologies:** JSF 4.0 (Jakarta Faces), PrimeFaces, XHTML, CSS, Vanilla JavaScript  
**Architecture:** Server-Side Rendering (JSF)  
**Technical Debt Level:** **VERY HIGH** 🔴🔴🔴

---

## 🔴 CRITICAL ISSUES

### 1. **Obsolete Technology (JSF)**

**Location:** Entire frontend  
**Severity:** 🔴🔴🔴 CRITICAL

**Problem:**

- JSF is a 2004 technology with server-side rendering paradigm
- Doesn't follow modern web development standards
- Declining community, difficult to find developers
- Not compatible with modern approaches (SPA, PWA, Mobile)

**Impact:**

```
❌ Impossible to create native mobile application
❌ Cannot be used as API for other clients
❌ Poor performance (full page reloads)
❌ Inferior user experience
❌ Difficult integration with modern tools
❌ Limited library ecosystem
```

**Problematic Code:**

```xml
<!-- dashboard.xhtml - Lines 1-6 -->
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
  "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:h="jakarta.faces.html"    <!-- ❌ JSF 2004 approach -->
      xmlns:p="http://primefaces.org/ui">
```

**Comparison with modern technologies:**

| Feature        | JSF (current) | React/Vue (modern) |
| -------------- | ------------- | ------------------ |
| Rendering      | Server-side   | Client-side        |
| Performance    | Low           | High               |
| Mobile         | No            | Yes (React Native) |
| Developer Pool | Small         | Large              |
| Ecosystem      | Limited       | Huge               |
| Type Safety    | No            | Yes (TypeScript)   |

**Recommendation:**

- Fully migrate to SPA (React, Vue, or Angular)
- Backend exposes REST API
- Frontend consumes API with fetch/axios
- Consider PWA for mobile experience

---

### 2. **Mixed Responsibilities (Frontend + Backend)**

**Location:** All `.xhtml` files  
**Severity:** 🔴 CRITICAL

**Problem:**

```xml
<!-- dashboard.xhtml - Line 8 -->
<f:metadata>
  <!-- ❌ Backend logic in frontend -->
  <f:event type="preRenderView"
           listener="#{taskController.onDatabaseLoaded(loginController.userName)}" />
</f:metadata>

<!-- Line 69 -->
<p:commandButton value="Create Task"
                 actionListener="#{taskController.openNew}"   <!-- ❌ Backend call -->
                 update=":dialogs:manage-task-content"        <!-- ❌ Server update -->
                 oncomplete="PF('managetaskDialog').show()">  <!-- ⚠️ Client script -->
```

**Impact:**

```
❌ Frontend and backend inseparable
❌ Impossible to reuse backend for mobile app
❌ No documented REST API
❌ Extremely difficult testing
❌ Each change requires complete rebuild
```

**How it should be (React + REST API):**

```javascript
// TaskDashboard.jsx
const TaskDashboard = () => {
  const [tasks, setTasks] = useState([]);

  useEffect(() => {
    fetch("/api/tasks")
      .then((res) => res.json())
      .then((data) => setTasks(data));
  }, []);

  return (
    <div>
      {tasks.map((task) => (
        <TaskCard task={task} />
      ))}
    </div>
  );
};
```

---

### 3. **Business Logic in View**

**Location:** `.xhtml` files  
**Severity:** 🔴 CRITICAL

**Problem:**

```xml
<!-- dashboard.xhtml - Lines 194-207 -->
<p:outputPanel rendered="#{taskController.currentTask.typeTask == 'Monitor'}">
  <!-- ❌ Business logic in template -->
  <p:outputLabel for="multiple">Assign Monitor(s)</p:outputLabel>
  <p:selectCheckboxMenu id="multiple"
                        value="#{taskController.selectedUsers}">
    <f:selectItems value="#{loginController.userNames}" />
  </p:selectCheckboxMenu>
</p:outputPanel>

<p:outputPanel rendered="#{taskController.currentTask.typeTask == 'Administradores'}">
  <!-- ❌ Different components by type (should be in component) -->
  <h:outputText value="This task will be automatically assigned to all users with Administrator role." />
</p:outputPanel>
```

**Impact:**

```
❌ Duplicated logic across templates
❌ Difficult maintenance
❌ Not reusable
❌ Impossible to test
```

**How it should be:**

```jsx
// TaskAssignmentSelector.jsx
const TaskAssignmentSelector = ({ taskType, selectedUsers, onChange }) => {
  if (taskType === TaskType.MONITOR) {
    return <MultiUserSelector users={selectedUsers} onChange={onChange} />;
  }

  if (taskType === TaskType.ADMIN) {
    return <AutoAssignmentInfo message="Will be auto-assigned..." />;
  }

  return null;
};
```

---

### 4. **Poorly Managed Global State**

**Location:** All backing beans with `@SessionScope`  
**Severity:** 🔴 CRITICAL

**Problem:**

```java
// TaskController.java
@Component
@SessionScope  // ❌ State in server session
@Data
public class TaskController {
    private List<Task> tasks;          // ❌ Global state
    private Task currentTask;          // ❌ Shared across tabs
    private String commentary;         // ❌ Server memory
}
```

**Impact:**

```
🔴 Consumes server memory per user
🔴 Doesn't scale horizontally (sticky sessions)
🔴 Problems with multiple tabs/windows
🔴 State can become inconsistent
🔴 Difficult debugging
```

**Comparison:**

| JSF @SessionScope       | React/Redux (modern) |
| ----------------------- | -------------------- |
| State on server         | State on client      |
| Server memory           | Browser memory       |
| Not multi-tab           | Multi-tab friendly   |
| Requires sticky session | Stateless backend    |
| Difficult debug         | Excellent DevTools   |

**Recommendation:**

```javascript
// Redux store (modern example)
const taskSlice = createSlice({
  name: "tasks",
  initialState: { items: [], loading: false },
  reducers: {
    setTasks: (state, action) => {
      state.items = action.payload;
    },
  },
});
```

---

### 5. **Non-Modularized CSS**

**Location:** `/css/*.css`  
**Severity:** 🟠 HIGH

**Problem:**

```css
/* taskdashboard.css - No scope, global selectors */
body {
  font-family: Helvetica, sans-serif; /* ❌ Affects entire app */
}

.user-wrapper {
  /* ❌ Generic name, can collide */
  display: flex;
  align-items: center;
}

.link {
  /* ❌ VERY generic */
  display: flex;
}

/* admindashboard.css - Duplication */
body {
  font-family: Helvetica, sans-serif; /* ❌ Repeated */
}
```

**Problems:**

```
❌ No CSS methodology (BEM, SMACSS)
❌ Global selectors cause collisions
❌ Duplicated code across files
❌ Doesn't use CSS variables
❌ Doesn't use preprocessors (Sass, Less)
❌ No CSS modules or CSS-in-JS
❌ Responsive not well structured
```

**Recommendation:**

```scss
// taskdashboard.module.scss (CSS Modules)
@use 'variables' as *;

.dashboard {
  font-family: $font-primary;

  &__header {
    display: flex;
    align-items: center;
  }

  &__userWrapper {
    padding: $spacing-md;
  }
}

// Or with Styled Components (CSS-in-JS)
const Dashboard = styled.div`
  font-family: ${props => props.theme.fonts.primary};
`;
```

---

### 6. **Vanilla JavaScript Without Structure**

**Location:** `login-validation.js`  
**Severity:** 🟠 HIGH

**Problem:**

```javascript
// login-validation.js
(function () {
  // ❌ Unnecessary IIFE in modern modules
  function validatePasswordAndConfirm(newPwClientId, confirmPwClientId) {
    try {
      var newPwEl = document.getElementById(newPwClientId); // ❌ var
      var confirmPwEl = document.getElementById(confirmPwClientId);

      if (!newPwEl) {
        // ❌ Fragile selector
        newPwEl = document.querySelector("[id$='new-password']");
      }

      var newPw = newPwEl ? newPwEl.value : ""; // ❌ Manual checking
      // ... 40 more lines of imperative code
    } catch (e) {
      return true; // ❌ Silencing errors
    }
  }

  // ❌ Polling every 500ms
  var attachInterval = setInterval(function () {
    attachLiveValidation();
  }, 500);

  // ❌ Expose to global scope
  window.validatePasswordAndConfirm = validatePasswordAndConfirm;
})();
```

**Problems:**

```
❌ Doesn't use ES6+ (const, let, arrow functions)
❌ Doesn't use modules (import/export)
❌ Imperative instead of declarative logic
❌ Polling instead of event-driven
❌ Direct DOM manipulation
❌ No type checking (TypeScript)
❌ No bundling or minification
❌ No dependency management (npm)
```

**Modern equivalent code:**

```typescript
// passwordValidation.ts
import { z } from 'zod';

const passwordSchema = z.string()
  .min(8, 'Minimum 8 characters')
  .regex(/[A-Z]/, 'Requires uppercase')
  .regex(/[a-z]/, 'Requires lowercase')
  .regex(/[0-9]/, 'Requires number')
  .regex(/[^A-Za-z0-9]/, 'Requires special character');

export const validatePassword = (password: string): ValidationResult => {
  try {
    passwordSchema.parse(password);
    return { valid: true };
  } catch (error) {
    return { valid: false, errors: error.errors };
  }
};

// In React component
const PasswordInput = () => {
  const [password, setPassword] = useState('');
  const validation = usePasswordValidation(password);

  return (
    <input
      value={password}
      onChange={(e) => setPassword(e.target.value)}
      aria-invalid={!validation.valid}
    />
  );
};
```

---

### 7. **No Component System**

**Location:** All `.xhtml` files  
**Severity:** 🟠 HIGH

**Problem:**

```xml
<!-- dashboard.xhtml - Code repeated in task.xhtml -->
<div class="user-wrapper">
  <div class="user-description">
    <p:outputLabel value="#{loginController.getCurrentFullName(loginController.userName)}" />
    <p:outputLabel value="#{loginController.getCurrentUserProfile(loginController.userName)}" />
  </div>
  <img src="https://www.w3schools.com/howto/img_avatar.png" />
</div>

<!-- Same code copied in admindashboard.xhtml -->
<!-- Same code copied in settings.xhtml -->
```

**Impact:**

```
❌ Code duplicated across multiple files
❌ Changes require modifying N files
❌ Inconsistencies between pages
❌ No reusability
```

**How it should be:**

```jsx
// components/UserBadge.jsx
const UserBadge = ({ user }) => (
  <div className={styles.userWrapper}>
    <div className={styles.userInfo}>
      <span className={styles.name}>{user.fullName}</span>
      <span className={styles.role}>{user.role}</span>
    </div>
    <Avatar src={user.avatarUrl} />
  </div>
);

// Used in multiple pages
<UserBadge user={currentUser} />;
```

---

### 8. **Poor Accessibility (a11y)**

**Location:** All files  
**Severity:** 🟠 HIGH

**Problems:**

```xml
<!-- No ARIA attributes -->
<p:commandButton value="Create Task" icon="pi pi-plus" />
<!-- ❌ No aria-label for screen readers -->

<div class="task-item">
  <!-- ❌ No semantic roles -->
  <h4>#{task.title}</h4>
</div>

<img src="img/logo.png" alt="TeamLogo" />
<!-- ⚠️ Generic alt -->

<!-- ❌ No skip navigation links -->
<!-- ❌ No focus management in modals -->
<!-- ❌ No keyboard navigation -->
```

**Recommendation:**

```jsx
<button
  aria-label="Create new task"
  aria-haspopup="dialog"
>
  <PlusIcon aria-hidden="true" />
  Create Task
</button>

<article role="article" aria-labelledby="task-title-123">
  <h4 id="task-title-123">{task.title}</h4>
</article>
```

---

## 🟠 HIGH PRIORITY ISSUES

### 9. **No Client-Side State Management**

**Severity:** 🟠 HIGH

```xml
<!-- State managed via server backing beans -->
<p:selectOneButton value="#{taskController.status}">
  <!-- ❌ Each change = server request -->
  <p:ajax listener="#{taskController.onDatabaseLoaded(loginController.userName)}"
          update="dt-task dt-task-lab" />
</p:selectOneButton>
```

**Should be:**

```javascript
// Local client state
const [filterStatus, setFilterStatus] = useState("pending");
const filteredTasks = tasks.filter((t) => t.status === filterStatus);
```

---

### 10. **No Load Optimization**

**Severity:** 🟠 HIGH

```xml
<!-- dashboard.xhtml -->
<!-- ❌ Everything loads on a single page -->
<link rel="stylesheet" href="https://unpkg.com/primeflex@latest/primeflex.css" />
<!-- ❌ CDN without specific version -->
<!-- ❌ No lazy loading of components -->
<!-- ❌ No code splitting -->
```

**Recommendation:**

```javascript
// Lazy loading with React
const TaskDashboard = lazy(() => import("./TaskDashboard"));
const AdminPanel = lazy(() => import("./AdminPanel"));

// Automatic code splitting
```

---

### 11. **External CDN Dependencies**

**Severity:** 🟡 MEDIUM

```xml
<link rel="stylesheet" href="https://unpkg.com/primeflex@latest/primeflex.css" />
<!-- ❌ @latest without version control -->
<!-- ❌ Uncontrolled external dependency -->
<!-- ❌ Can break in production -->
```

---

### 12. **No UI Error Handling**

**Severity:** 🟠 HIGH

```xml
<!-- No error boundaries -->
<!-- No loading fallbacks -->
<!-- No empty state handling -->

<p:carousel id="dt-task" var="task" value="#{taskController.tasks}">
  <!-- ❌ What if tasks is null? -->
  <!-- ❌ What if error loading? -->
  <!-- ❌ No loading state -->
</p:carousel>
```

**Should have:**

```jsx
<ErrorBoundary>
  <Suspense fallback={<Loading />}>
    <TaskList tasks={tasks} emptyState={<EmptyTasksMessage />} error={error} />
  </Suspense>
</ErrorBoundary>
```

---

## 🟡 MEDIUM PRIORITY ISSUES

### 13. **No Design System**

**Problem:**

- Hardcoded colors in CSS
- Inconsistent spacing
- Typography without system
- No design tokens

```css
/* Hardcoded values everywhere */
background: linear-gradient(to bottom right, #7dcc7f, #3dcc63, #2b9440);
padding: 1em;
margin: 10px;
```

**Recommendation:**

```scss
// design-tokens.scss
$color-primary: #3dcc63;
$color-primary-dark: #2b9440;
$spacing-sm: 0.5rem;
$spacing-md: 1rem;
```

---

### 14. **No Internationalization (i18n)**

```xml
<!-- Hardcoded texts in Spanish -->
<p:commandButton value="Crear Tarea" />
<p:outputLabel value="Monitor(es) a asignar" />
```

**Should be:**

```jsx
import { useTranslation } from "react-i18next";

const { t } = useTranslation();
<button>{t("tasks.create")}</button>;
```

---

### 15. **No Robust Form Validation**

```xml
<!-- Basic validation only with required -->
<p:inputTextarea id="title" required="true" />
<!-- ❌ No custom messages -->
<!-- ❌ No async validation -->
<!-- ❌ No cross-field validation -->
```

---

### 16. **Limited Responsive Design**

```css
/* No well-structured media queries */
/* No mobile-first approach */
/* No consistent breakpoints */
```

---

### 17. **No Animations/Transitions**

```xml
<!-- PrimeFaces has some, but limited -->
<p:dialog showEffect="fade">
  <!-- ❌ Limited animation options -->
</p:dialog>
```

---

## 🎨 UX/UI ISSUES

### 18. **Outdated User Experience**

```
❌ Full page reloads (not SPA)
❌ No optimistic updates
❌ Slow action feedback
❌ No modern drag & drop
❌ No keyboard shortcuts
❌ No dark mode
❌ No user preferences saved locally
```

---

### 19. **No PWA Capabilities**

```
❌ Doesn't work offline
❌ Cannot be installed as app
❌ No push notifications
❌ No service workers
❌ No manifest.json
```

---

### 20. **Performance Issues**

```xml
<!-- Full server rendering on each action -->
<p:ajax update="dt-task dt-task-lab" />
<!-- ❌ Re-renders complete components -->
<!-- ❌ No virtual scrolling for large lists -->
<!-- ❌ No memoization -->
```

---

## 📋 RECOMMENDED ARCHITECTURE

### Proposed Modern Stack

```
Frontend:
├── React 18+ or Vue 3+
├── TypeScript
├── Vite (build tool)
├── TailwindCSS or Styled Components
├── React Query or SWR (data fetching)
├── Redux Toolkit or Zustand (state)
├── React Router v6 (routing)
├── React Hook Form (forms)
├── Zod (validation)
└── Vitest + Testing Library (testing)

Backend API:
├── Spring Boot 3.2+
├── Spring Web (REST Controllers)
├── Spring Security (JWT)
├── OpenAPI/Swagger
└── CORS configured
```

---

## 🔄 MIGRATION PLAN

### Option 1: Big Bang (Not Recommended)

- Rewrite everything at once
- Very high risk
- Time: 4-6 months

### Option 2: Strangler Fig Pattern (Recommended)

Gradual migration by modules:

**Phase 1: Backend API**

```
✅ Create REST API endpoints
✅ Keep JSF working
✅ Both systems coexist
```

**Phase 2: Authentication**

```
✅ Migrate login to React
✅ Implement JWT
✅ Proxy requests to backend
```

**Phase 3: Dashboard**

```
✅ Migrate task view
✅ Consume REST API
✅ Deprecate JSF view
```

**Phase 4: Administration**

```
✅ Migrate admin panel
✅ Complex forms to React
```

**Phase 5: Cleanup (Sprint 8)**

```
✅ Remove JSF completely
✅ Optimize and polish
```

---

## 📊 BEFORE/AFTER COMPARISON

| Aspect                   | JSF (Current) | React + API (Proposed) |
| ------------------------ | ------------- | ---------------------- |
| **Performance**          | ⚠️ Slow       | ✅ Fast                |
| **Mobile**               | ❌ No         | ✅ Yes + React Native  |
| **Developer Experience** | ❌ Poor       | ✅ Excellent           |
| **Testing**              | ❌ Difficult  | ✅ Easy                |
| **Hiring**               | ❌ Difficult  | ✅ Easy                |
| **Ecosystem**            | ⚠️ Limited    | ✅ Huge                |
| **Offline**              | ❌ No         | ✅ Yes (PWA)           |
| **SEO**                  | ⚠️ Regular    | ✅ Good (SSR)          |
| **Bundle Size**          | ⚠️ Large      | ✅ Optimized           |
| **Hot Reload**           | ❌ No         | ✅ Yes                 |

---

## 🎯 PRIORITIZATION

### 🔥 Critical - Do Now

1. Plan migration to modern stack
2. Create REST API (can coexist with JSF)
3. Document current components for migration
4. Setup React/Vue project

### ⚠️ High - Next Sprint

1. Migrate authentication module
2. Implement JWT
3. Create base components in React
4. Setup testing

### 📌 Medium - 2-3 Months

1. Migrate main dashboard
2. Migrate forms
3. Optimize performance
4. PWA capabilities

---

## 💰 COST VS BENEFIT

### Maintain JSF

```
Costs:
- Difficult to hire developers
- Technical limitations
- Growing technical debt
- Impossible mobile app

Benefits:
- No immediate investment required
- Current system functional
```

### Migrate to Modern Stack

```
Costs:
- 4-6 months development
- Team training
- Risk of initial bugs

Benefits:
- Solid foundation for 5+ years
- Easy hiring
- Better UX/performance
- Mobile app possible
- Active community
- Modern ecosystem
```

**Estimated ROI:** Positive in 12-18 months

---

## 📈 SUCCESS METRICS

| Metric                 | Current | Target |
| ---------------------- | ------- | ------ |
| Time to Interactive    | ~5s     | <2s    |
| Bundle Size            | N/A     | <300kb |
| Lighthouse Score       | ~60     | >90    |
| Accessibility Score    | ~50     | >95    |
| Test Coverage          | 0%      | >70%   |
| Developer Satisfaction | ⚠️      | ✅     |

---

## 🏁 CONCLUSION

LabToDo's frontend is built on **obsolete technology** (JSF) that:

### Main Problems:

1. ❌ **Doesn't allow modern scalability** (mobile, PWA)
2. ❌ **User experience inferior** to current standards
3. ❌ **Difficult hiring** developers
4. ❌ **Backend-frontend coupling** prevents evolution
5. ❌ **No ecosystem** of modern tools

### Final Recommendation:

**Gradual migration** (Strangler Fig Pattern) to:

- **Frontend:** React + TypeScript + TailwindCSS
- **Backend:** Spring Boot REST API
- **Priority:** HIGH

This investment is **critical** for the project's long-term viability.

---

**Analysis date:** February 2026
