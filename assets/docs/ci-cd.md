# CI/CD (GitHub Actions) — LabToDo

Resumen de la configuración añadida al pipeline de GitHub Actions:

- Build: compila el proyecto con `./mvnw -DskipTests clean compile` y guarda artefactos compilados.
- OWASP Dependency-Check: ejecuta el plugin Maven `dependency-check` y sube el reporte XML.
- Test & Coverage: ejecuta `./mvnw verify` para correr tests y generar informes JaCoCo.
- SonarCloud: análisis de código y cobertura (requiere secretos de Sonar en el repo).
- CodeQL: análisis estático de seguridad (job `codeql`).
- Notificaciones: paso `notify` que puede enviar a Slack (si `SLACK_WEBHOOK` está configurado) y crea un issue resumido si la ejecución no es exitosa.

Secrets requeridos (configurar en Settings → Secrets):

- `SONAR_TOKEN` — token de SonarCloud.
- `SONAR_PROJECT_KEY` — clave del proyecto en SonarCloud.
- `SONAR_ORGANIZATION` — organización en SonarCloud.
- `GITHUB_TOKEN` — ya disponible por GitHub Actions (usado por algunos pasos).
- `SLACK_WEBHOOK` — (opcional) webhook para notificaciones Slack.

Artefactos que se suben automáticamente:

- `compiled-classes` — directorio `target/` compilado.
- `jacoco-report` — informe HTML de JaCoCo en `target/site/jacoco/`.
- `dependency-check-report` — reporte OWASP en `target/dependency-check-report`.
- `labtodo-jar` — artifact empaquetado en `target/labtodo.jar` (deploy step simulado).

Ejecución local rápida (para reproducir lo que hace CI):

```bash
# Compilar (sin tests)
./mvnw -DskipTests clean compile

# Ejecutar tests y generar cobertura
./mvnw verify

# Ejecutar OWASP Dependency-Check
./mvnw org.owasp:dependency-check-maven:check -Dformat=XML -DoutputDirectory=target/dependency-check-report

# Ejecutar Sonar (localmente necesita token y configuración adecuada)
./mvnw sonar:sonar -Dsonar.projectKey=... -Dsonar.organization=... -Dsonar.host.url=https://sonarcloud.io
```

Notas y recomendaciones:

- Ajusté `dependency-check` para no fallar el build automáticamente (`-DfailBuildOnCVSS=0`) y evitar romper CI por vulnerabilidades informadas; si prefieren bloquear merges por vulnerabilidades elevadas, pueden ajustar ese parámetro.
- Añadí un job `codeql` separado para mantener el análisis de seguridad aislado y con permisos adecuados.
- La notificación a Slack está condicionada a la existencia del secreto `SLACK_WEBHOOK`.

Próximos pasos sugeridos:

- Revisar y añadir los secretos en el repositorio.
- Ejecutar una rama de prueba para verificar artefactos y reportes subidos en Actions.
- Ajustar reglas de Sonar y umbral de cobertura si es necesario.

Registro de cambios (bitácora):

- 2026-03-26: Añadida pipeline mejorada (build, dependency-check, tests, SonarCloud, CodeQL, notify). — Automático
