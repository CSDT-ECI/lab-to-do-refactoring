# Architectural Smells - LabToDo (version unificada)

Fecha de analisis: 2026-04-18  
Proyecto: LabToDo Refactoring (Spring Boot + JSF/PrimeFaces + JoinFaces)


### Hallazgos relevantes del paper para este analisis

- Un smell arquitectonico es un problema estructural de alto nivel que impacta principalmente mantenibilidad.
- La deteccion tardia incrementa el costo de correccion.
- No todos los smells conocidos son detectados por herramientas automaticamente.
- Se recomienda combinar herramientas + revision arquitectonica manual con evidencia.

---

##  Criterio de evaluacion usado

Se hizo inspeccion de codigo y vistas del proyecto unificado, buscando:

- Acoplamiento entre capas y entre componentes.
- Concentracion de responsabilidades.
- Violaciones de limites de contexto (UI, dominio, seguridad, infraestructura).
- Duplicacion de reglas de negocio cross-cutting.
- Dependencias a framework que dificulten evolucion.

---

## Architectural smells identificados

## Smell A - Architecture by Implication / No Layers / Strict Layers Violated

**Taxonomia relacionada (Sharma):** Architecture by Implication, No Layers, Strict Layers Violated.  
**Severidad:** Alta.

**Evidencia en el proyecto:**

- La vista ejecuta logica de carga y negocio directamente sobre controllers:
  - `src/main/resources/META-INF/resources/dashboard.xhtml` linea 8 (`preRenderView` con `taskController.onDatabaseLoaded(...)`).
  - `src/main/resources/META-INF/resources/admindashboard.xhtml` linea 13 (`taskController.onControlLoaded()`).
  - `src/main/resources/META-INF/resources/dashboard.xhtml` linea 213 y `admindashboard.xhtml` linea 201 (`actionListener` a `taskController.saveTask`).
- Los controllers son `@SessionScope`, mezclando estado de UI, flujo y negocio:
  - `src/main/java/edu/eci/labinfo/labtodo/controller/TaskController.java` linea 31.
  - `src/main/java/edu/eci/labinfo/labtodo/controller/LoginController.java` linea 25.

**Impacto arquitectonico:**

- Baja separacion de responsabilidades entre presentacion y aplicacion.
- Mayor complejidad para pruebas aisladas y evolucion a API-first.

**Accion:**

- Definir fronteras explicitas: UI adapter -> application service -> domain service -> repository.
- Reducir logica en backing beans a orquestacion de UI.
- Mover reglas de negocio a servicios de aplicacion/dominio.

---

## Smell B - Feature Concentration (God Controllers)

**Taxonomia relacionada (Sharma):** Feature Concentration, Too Large Subsystems.  
**Severidad:** Alta.

**Evidencia en el proyecto:**

- Tamano de clases controlador (medido en el workspace):
  - `src/main/java/edu/eci/labinfo/labtodo/controller/LoginController.java`: 389 lineas.
  - `src/main/java/edu/eci/labinfo/labtodo/controller/AdminController.java`: 280 lineas.
  - `src/main/java/edu/eci/labinfo/labtodo/controller/TaskController.java`: 275 lineas.
- Metodos con multiples responsabilidades:
  - `TaskController.saveTask()` en linea 81.
  - `LoginController.login()` en linea 100.
  - `AdminController.modifyStateTaks()` en linea 43.

**Impacto arquitectonico:**

- Alto costo de cambio (un cambio de regla impacta controllers grandes).
- Mayor probabilidad de regresiones por acoplamiento interno.
- Menor testabilidad por metodos de alto contexto.

**Accion:**

- Particionar por casos de uso (p. ej., `CreateTaskUseCase`, `ChangeTaskStateUseCase`, `AuthenticateUserUseCase`).
- Mantener controllers como adaptadores delgados.

---

## Smell C - Implicit Cross-Module Dependency (controller -> controller)

**Taxonomia relacionada (Sharma):** Implicit Cross-Module Dependency, Subsystem-API Bypassed.  
**Severidad:** Alta.

**Evidencia en el proyecto:**

- `TaskController` depende directamente de `LoginController`:
  - `src/main/java/edu/eci/labinfo/labtodo/controller/TaskController.java` linea 47.
- `TaskController` usa datos de autenticacion para reglas de negocio en `saveTask()`:
  - `src/main/java/edu/eci/labinfo/labtodo/controller/TaskController.java` linea 81+.

**Impacto arquitectonico:**

- Acoplamiento transversal no declarado entre modulos de tarea y autenticacion.
- Dificulta sustituir mecanismo de auth o mover a API stateless.

**Accion:**

- Introducir un `CurrentUserContext`/`AuthFacade` en capa de aplicacion.
- Eliminar referencias directas controller -> controller.

---

## Smell D - Vendor Lock-In a PrimeFaces + Framework Leakage

**Taxonomia relacionada (Sharma):** Vendor Lock-In.  
**Severidad:** Media-Alta.

**Evidencia en el proyecto:**

- Uso directo masivo de `PrimeFaces.current()` desde controller:
  - `src/main/java/edu/eci/labinfo/labtodo/controller/LoginController.java` (muchas invocaciones, p. ej. lineas 76, 87, 92, 106, 115, 124, etc.).
- Wrapper sin desacople real:
  - `src/main/java/edu/eci/labinfo/labtodo/service/PrimeFacesWrapper.java` linea 8.
- La vista depende del contrato de componentes PrimeFaces (`PF(...)`, `p:commandButton`, `p:dialog`) en todo el flujo.

**Impacto arquitectonico:**

- Alto costo de migracion a otra tecnologia de UI.
- Mezcla de decisiones de framework con logica de aplicacion.

**Accion:**

- Introducir puertos de UI (notificaciones/comandos de interfaz) y adaptadores.
- Encapsular side-effects de UI fuera de reglas de negocio.

---

## Smell E - Scattered Parasitic Functionality (reglas duplicadas)

**Taxonomia relacionada (Sharma):** Scattered Parasitic Functionality.  
**Severidad:** Media-Alta.

**Evidencia en el proyecto:**

- Regla de estado/usuarios comentadores repetida en dos controladores:
  - `src/main/java/edu/eci/labinfo/labtodo/controller/TaskController.java` linea 134 (`completedMessage`).
  - `src/main/java/edu/eci/labinfo/labtodo/controller/AdminController.java` lineas 43 y 54 (`modifyStateTaks`).
- Logica de asignacion por tipo de tarea repartida entre controlador y servicio:
  - `TaskController.saveTask()` (linea 81+).
  - `UserService.assignAdminTasksToUser(...)` (desde `src/main/java/edu/eci/labinfo/labtodo/service/UserService.java`).

**Impacto arquitectonico:**

- Riesgo de inconsistencia funcional ante cambios de reglas.
- Mayor costo de mantenimiento por actualizaciones en varios puntos.

**Accion:**

- Centralizar transiciones de estado en un unico servicio de dominio.
- Implementar estrategia/maquina de estados para transiciones.

---

## Smell F - Jumble / Packages Not Clearly Named

**Taxonomia relacionada (Sharma):** Jumble, Packages Not Clearly Named.  
**Severidad:** Media.

**Evidencia en el proyecto:**

- Artefactos de infraestructura en paquete `service`:
  - `src/main/java/edu/eci/labinfo/labtodo/service/SecurityConfig.java` (linea 8).
  - `src/main/java/edu/eci/labinfo/labtodo/service/PrimeFacesWrapper.java` (linea 8).
- Modelo de dominio con metodos de presentacion:
  - `src/main/java/edu/eci/labinfo/labtodo/model/Task.java` lineas 83 (`getDateText`) y 88 (`getAllUsers`).
  - Uso en vista: `src/main/resources/META-INF/resources/admindashboard.xhtml` linea 129 (`filterBy="#{task.getAllUsers()}"`).

**Impacto arquitectonico:**

- Limites de contexto difusos entre dominio, infraestructura y presentacion.
- Menor claridad para evolucion modular.

**Accion:**

- Reorganizar paquetes por responsabilidad (application/domain/infrastructure/ui).
- Sacar formato/presentacion fuera de entidades.

---

## Smell G - Ambiguous/Unsafe Service Contracts

**Taxonomia relacionada (Sharma):** Ambiguous Interfaces, Unstable Interface.  
**Severidad:** Media.

**Evidencia en el proyecto:**

- Contratos de servicio con `Optional.get()` sin manejo explicito de ausencia:
  - `src/main/java/edu/eci/labinfo/labtodo/service/TaskService.java` linea 30.
  - `src/main/java/edu/eci/labinfo/labtodo/service/SemesterService.java` linea 28.
  - `src/main/java/edu/eci/labinfo/labtodo/service/CommentService.java` linea 27.
- Metodos `update...` que retornan `null` en fallo (semantica ambigua) en varios servicios.

**Impacto arquitectonico:**

- Propaga contratos implicitos y comportamiento no uniforme entre capas.
- Incrementa acoplamiento a detalles de persistencia y manejo de errores ad hoc.

**Accion:**

- Contratos explicitos: excepciones de dominio, tipos de resultado claros, sin `null` sentinela.

---
