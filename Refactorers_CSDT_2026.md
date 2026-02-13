# LabToDo Refactoring Readout (2026)

## Overview

- The application couples JSF controllers, Spring services, and persistence logic so tightly that any UI change risks breaking core workflows.
- Navigation, dialogs, and task widgets are duplicated across pages, inflating maintenance costs and making accessibility fixes inconsistent.
- Exception handling is largely silent, which hides production failures and complicates on-call diagnostics.

## Functional Capabilities

### Backend Surface (Spring Boot + JSF Managed Beans)

- **Authentication & account lifecycle:** `LoginController` handles `/login.xhtml`, user registration with default Monitor role, forgotten-password requests/approvals, and role-based redirects toward `dashboard.xhtml`, `admindashboard.xhtml`, or `settings.xhtml`.
- **Task lifecycle management:** `TaskController`/`TaskService` expose creation and edition of tasks, assignment of monitors or administrators, status transitions (Pending → In Process → Review → Finished), and retrieval of related comments via `CommentService`.
- **Administrative bulk actions:** `AdminController` allows multi-selection state changes, role/account-type updates, and user deletions from the admin dashboard, coordinating with `UserService` and `PrimeFacesWrapper` for UI updates.
- **Academic period management:** `SemesterController`/`SemesterService` create and update semester windows, validate start/end dates, and supply dropdown data for dashboard filters.
- **Infrastructure helpers:** `SecurityConfig` publishes `BCryptPasswordEncoder`, while `UserService` injects business rules such as automatically assigning administrator tasks to newly promoted admins.

### Frontend Surface (Facelets + PrimeFaces)

- **login.xhtml:** Public landing page with login form, new-account dialog, and an "I forgot my password" wizard that enforces password-strength hints.
- **dashboard.xhtml:** Monitor-facing board with carousels for personal and lab tasks, status filters, quick actions to advance a task, and dialogs for new task creation.
- **admindashboard.xhtml:** Administrative table view with filtering, multi-row selection, bulk state updates, and access to the "Create New Semester" dialog.
- **task.xhtml:** Detail page for a single task showing metadata, assignees, transition buttons, and the paginated comment feed.
- **settings.xhtml and shared dialogs:** Configuration page plus reusable PrimeFaces dialogs (logout, semester creation, comment management) styled through the CSS assets under `META-INF/resources/css`.

## Detailed Reports

- [docs/backend-technical-debt.md](docs/backend-technical-debt.md) — Business logic, data, and security issues observed in the Spring/JSF backend.
- [docs/frontend-technical-debt.md](docs/frontend-technical-debt.md) — UI/UX, accessibility, and maintainability problems across the XHTML views and CSS assets.

## Cross-Cutting Risks

1. **Lack of layering discipline:** Controllers run database updates directly, preventing automated tests and alternative interfaces (REST, CLI) from reusing logic.
2. **Stateful session beans:** Session-scoped managed beans keep mutable collections without guards, so a single bad request can corrupt the in-memory state for an entire session.
3. **Operational blind spots:** Catch-all `Exception` blocks and missing logging obscure the root cause of failures, while duplicated UI code makes visual regressions almost impossible to detect early.
4. **Missing responsive/accessibility strategy:** Fixed-width layouts, unsupported CSS selectors, and POST-based navigation block mobile adoption and accessibility compliance.
