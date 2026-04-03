# El Efecto Mariposa del Código

Bienvenidos al laboratorio. Aquí aprenderán por qué "Vibe Coding" (programar solo con corazonadas y prompts vagos) es peligroso, y cómo aplicar el Spec-Driven Development los salvará de la deuda técnica extrema.

## 🎯 Su Misión (El Reto)

Van a construir un componente web puramente Frontend (HTML, CSS y JavaScript) que no requiere Bases de Datos ni backend. El componente es una "Calculadora Ágil de Huella de Carbono".

Solo necesitan tener abierto un bloc de notas, Visual Studio Code o el navegador, y su asistente de IA favorito (ChatGPT, Copilot, Cursor o Gemini).

---

## Fase 1: Vibe Coding

### La Trampa del "Vibe Coding"

El Vibe Coding ocurre cuando le hablan a la IA como si fuera magia, sin darle especificaciones de arquitectura. Vamos a intentarlo para ver cómo falla.

### Paso 1: El Prompt Perezoso

Copien este texto exacto en su IA y generen la página:

"Hazme una página web bonita para una calculadora de huella de carbono. Que calcule cosas y se vea moderna."

### Paso 2: Prueben el Código

Copien el código en un archivo index.html y ábranlo en el navegador. Seguramente se verá bien a la primera. "Parece mágico", ¿verdad?

Resultado obtenido:

[Ver video: Prompt Perezoso](videos/promptPerezoso.mp4)

![Preview Prompt Perezoso](videos/promptPerezoso.gif)

### Paso 3: El "Giro" Inesperado (El Efecto Mariposa) 🦋💥

Ahora vamos a pedirle un cambio que parece simple, pero que forzará a la IA a tomar decisiones de arquitectura drásticas. Pídanle esto en el mismo chat:

"Ahora haz que los cálculos se guarden en una tabla abajo, añade una gráfica interactiva súper profesional y haz que los colores dependan del resultado. Ah, y que se pueda descargar como PDF profesional."

Resultado obtenido:

[Ver video: Efecto Mariposa](videos/efectoMariposa.mp4)

![Preview Efecto Mariposa](videos/efectoMariposa.gif)

No se visualiza la gráfica.
No hay colores que dependan del resultado, y como se puede ver en el video, al descargar el PDF, este sí se genera bien pero su contenido no es visible correctamente.

### Paso 4: El Colapso (Frankenstein Code) 🧟

Si aún no se ha roto, este último prompt ambiguo causará el desastre. Pidan esto:

"No me gusta el gráfico, quítalo y haz que sea minimalista en una sola tarjeta, pero mantén la descarga de PDF. Además, ahora los cálculos deben ser mensuales estimamos por 12 meses, no por año. Arréglalo rápido."

Resultado obtenido:

[Ver video: Colapso](videos/colapso.mp4)

![Preview Colapso](videos/colapso.gif)

Se eliminó correctamente el gráfico. Lo demás se mantuvo, pero al generar el PDF se mantiene el mismo problema con el contenido del documento, no es visible correctamente su contenido.

---

## 🤔 Análisis de Desastre

**Inyección de Librerías:** Sí ocurrió. La IA inyectó `html2pdf.js` desde un CDN externo sin que formara parte del diseño original:

```html
<script
  src="https://cdn.jsdelivr.net/npm/html2pdf.js@0.10.1/dist/html2pdf.bundle.min.js"
  defer
></script>
```

Esta librería funciona tomando una "foto" del DOM con `html2canvas`. En la exportación el PDF queda con fondo blanco, pero el texto se mantiene en blanco por los estilos originales, así que el contenido se pierde por falta de contraste. La función de descarga generada luce así:

```js
const downloadPdf = () => {
  if (!window.html2pdf || !reportRoot) return;

  const options = {
    margin: 10,
    filename: "reporte-huella-carbono.pdf",
    image: { type: "jpeg", quality: 0.98 },
    html2canvas: { scale: 2, useCORS: true },
    jsPDF: { unit: "mm", format: "a4", orientation: "portrait" },
  };

  html2pdf().set(options).from(reportRoot).save();
};
```

Nótese que `html2canvas` captura el DOM tal como está, incluyendo el fondo oscuro del CSS. No se aplicó ninguna transformación de estilos antes de exportar, por eso el texto blanco queda invisible sobre el fondo blanco del PDF generado.

---

**Pérdida de Contexto:** Ocurrió de forma parcial. El gráfico sí se eliminó correctamente al pedirlo, pero la integración rota de `html2pdf` persistió sin corrección. La IA no revisó si la lógica de exportación seguía funcionando tras la reestructuración; simplemente arrastró el código defectuoso como deuda técnica al siguiente prompt.

Para el cálculo anual, la IA introdujo la constante `monthsEstimate` y la aplicó correctamente en las emisiones:

```js
const monthsEstimate = 12;

const emissions = Object.fromEntries(
  Object.entries(emissionsMonthly).map(([key, value]) => [
    key,
    value * monthsEstimate,
  ]),
);
```

Sin embargo esa corrección no se propagó al módulo de PDF, dejando el bug de contraste intacto en ambos contextos.

---

**Alucinaciones de Arquitectura:** Sí ocurrió. El archivo `script.js` mezcla múltiples responsabilidades sin ninguna separación. Comparen estas cinco funciones que conviven en el mismo nivel global sin ningún módulo ni clase que las agrupe:

```js
// Responsabilidad 1: Cálculo de emisiones
const calculate = () => { ... };

// Responsabilidad 2: Manipulación del DOM
const buildBreakdown = (entries) => { ... };
const renderHistoryTable = (entries) => { ... };

// Responsabilidad 3: Persistencia en localStorage
const loadHistory = () => {
  try {
    const stored = JSON.parse(localStorage.getItem(storageKey));
    return Array.isArray(stored) ? stored : [];
  } catch (error) {
    return [];
  }
};
const saveHistory = (entries) => {
  localStorage.setItem(storageKey, JSON.stringify(entries));
};

// Responsabilidad 4: Exportación a PDF
const downloadPdf = () => { ... };

// Responsabilidad 5: Estado visual y temas de color
const updateTheme = (ratio) => {
  document.body.classList.remove("state-low", "state-mid", "state-high");
  if (ratio <= 80) document.body.classList.add("state-low");
  else if (ratio <= 120) document.body.classList.add("state-mid");
  else document.body.classList.add("state-high");
};
const updateRing = (percent) => { ... };
```

Cada nuevo prompt fue apilando capas sobre las mismas funciones globales, produciendo exactamente la "sopa" de código donde ya no es claro dónde termina una responsabilidad y empieza la siguiente.

**Lección:** Sin un "Spec" previo, cada nuevo requerimiento es una tirada de dados que puede destruir la arquitectura.

---

## Fase 2: Spec-Driven Development

### Escribiendo Especificaciones de Ingeniería

Abran un CHAT NUEVO. Esta vez no le pediremos "algo bonito". Le entregaremos Arquitectura, Restricciones e Historias de Usuario claras guiando las decisiones de diseño.

### Paso 1: El Mega-Prompt (Contexto + Restricciones)

Utilicen esta estructura estricta para el desarrollo inicial:

```
ROL: Eres un Ingeniero Frontend Senior.
CONTEXTO TECNOLÓGICO: Aplicación de una sola página (SPA). NO uses React, NO uses Vue,
NO uses CDN de librerías de estilos. Usa única y exclusivamente HTML semántico, CSS puro
(Vanilla) dentro de un bloque <style> y JavaScript puro en un bloque <script>.
RESTRICCIONES ARQUITECTÓNICAS: Todo el código debe venir en un solo archivo index.html
fácil de copiar. Separa visualmente la lógica de JS del maquetado HTML.
HISTORIA DE USUARIO 1: Como usuario, quiero ver 3 campos numéricos (Kilómetros en auto,
Horas de vuelo, Gasto en electricidad). Quiero un botón que al pulsarlo tome esos 3 valores,
los sume y multiplique por 0.5, y muestre el "Total de Huella de Carbono en KG" en pantalla
usando JavaScript, validando que los datos no estén vacíos. No añadas nada más.
```

### Paso 2: Comparen Control vs Caos

Copien el código en su `index.html`. Ejecútenlo. Pídanle a la IA que añada funcionalidad paso a paso leyendo la siguiente historia de usuario en el chat:

```
HISTORIA DE USUARIO 2: Ahora implementa un botón para "Cambiar a Modo Oscuro", que
únicamente agregue la clase CSS ".dark-mode" al body. Usa colores oscuros estándar (#222 y #fff).
```

Resultado obtenido:

![Resultado Historia de Usuario 2 - Modo oscuro](images/image2.png)

![Resultado Historia de Usuario 1 - Modo claro con calculo](images/image1.png)

---

### 🤔 Reflexión

**Crecimiento modular sin roturas:** Al comparar las dos capturas, se puede confirmar que la Historia de Usuario 2 se implementó de forma aditiva y no destructiva. El cálculo ya existente (100 km + 2 horas + 340 kWh = 221 kg) siguió funcionando idénticamente después de agregar el modo oscuro. La IA no tocó la lógica de cálculo para implementar el nuevo requerimiento, porque el Spec le indicó con precisión dónde intervenir: solo agregar una clase CSS al `body`.

El handler del modo oscuro es exactamente eso, nada más:

```js
darkModeBtn.addEventListener("click", () => {
  document.body.classList.add("dark-mode");
});
```

Y el CSS correspondiente respeta el contrato de la historia de usuario usando exactamente los colores especificados (`#222` y `#fff`), sin tocar ninguna otra regla existente:

```css
body.dark-mode {
  background: #222;
  color: #fff;
}

body.dark-mode main,
body.dark-mode input,
body.dark-mode .result {
  background: #222;
  color: #fff;
  border-color: #fff;
}

body.dark-mode button {
  background: #fff;
  color: #222;
}
```

---

**Por qué el Spec evita el Token Sprawl:** En el Vibe Coding, la IA asume el diseño, la tecnología, la estructura y los casos borde por su cuenta, generando tokens extras para rellenar ambigüedades. Con el Mega-Prompt, esas decisiones ya estaban tomadas por el equipo antes de empezar: sin frameworks, sin CDNs, un solo archivo, separación visual de HTML y JS.

Comparen la estructura del HTML generado en cada enfoque:

```html
<!-- Con Vibe Coding: dependencias externas inyectadas sin pedirlas -->
<link
  href="https://fonts.googleapis.com/css2?family=Fraunces..."
  rel="stylesheet"
/>
<script
  src="https://cdn.jsdelivr.net/npm/html2pdf.js@0.10.1/..."
  defer
></script>
```

```html
<!-- Con Spec: HTML semántico, sin CDNs, estructura predecible -->
<main>
  <header>
    <h1>Calculadora de Huella de Carbono</h1>
    <p class="subtitle">
      Ingresa los tres datos solicitados y calcula tu total.
    </p>
  </header>
  <section aria-label="Formulario de calculo">
    <form id="carbonForm">
      <div class="field">
        <label for="carKm">Kilometros en auto</label>
        <input id="carKm" type="number" min="0" step="any" required />
      </div>
      ...
      <button type="button" id="calculateBtn">Calcular</button>
    </form>
  </section>
</main>
<script src="script.js"></script>
```

La IA no tuvo que inventar nada, solo ejecutar. Eso reduce drásticamente el número de tokens consumidos en suposiciones y reescrituras.

**Lección:** La Especificación ES el Nuevo Código. Tú diseñas, el Copiloto programa.

---

## Fase 3: Quality Gate (Orquestación)

### La IA también es Inspectora

El ciclo AIDLC exige una Verificación antes del pase a producción (Deploy). Vamos a pedirle a la IA que se audite a sí misma.

### Paso Final: AI Red Teaming

Envíenle el código completo que acaban de generar junto con esta orden:

```
Cambia de rol. Ahora eres un experto en Aseguramiento de Calidad (QA) y Experiencia de
Usuario (UX). Analiza mi código anterior. Encuentra posibles bugs si el usuario ingresa
letras en vez de números, o si hay problemas de accesibilidad (contraste). Dime qué
corregirías y refactoriza solo la parte afectada.
```

Resultado:

- Se validó el ingreso numérico para evitar `NaN` cuando el usuario escribe letras.
- Se ajustó el mensaje de error en modo oscuro para garantizar contraste con `#222` y `#fff`.

---

### 🤔 Reflexión

**Validación de entrada — ✅ Cumplido:** La IA refactorizó únicamente la lógica de lectura de inputs sin tocar el HTML ni los estilos. Antes del QA, cualquier texto no numérico producía un `NaN` silencioso que se mostraba en pantalla sin aviso. Después, se introdujeron dos funciones auxiliares con responsabilidad única:

```js
// Antes del QA: sin validación, cualquier texto producía NaN silencioso
const getValue = (id) => Number(document.getElementById(id).value || 0);

// Después del QA: detecta vacío y verifica que el valor sea un número finito
const getNumber = (id) => {
  const value = document.getElementById(id).value;
  return value === "" ? null : Number(value);
};

const isValidNumber = (value) => Number.isFinite(value);

const calculate = () => {
  const carKm = getNumber("carKm");
  const flightHours = getNumber("flightHours");
  const electricitySpend = getNumber("electricitySpend");

  if (carKm === null || flightHours === null || electricitySpend === null) {
    errorMessage.textContent = "Completa los tres campos antes de calcular.";
    return;
  }

  if (!isValidNumber(carKm) || !isValidNumber(flightHours) || !isValidNumber(electricitySpend)) {
    errorMessage.textContent = "Usa solo números válidos en los tres campos.";
    return;
  }
  ...
};
```

---

**Contraste en modo oscuro — ⚠️ Cumplido parcialmente:** El mensaje de error sí recibió su regla de contraste, y el elemento HTML recibió `role="alert"` para lectores de pantalla, una mejora de accesibilidad que no estaba en el Spec original:

```css
body.dark-mode .error {
  color: #fff;
}
```

```html
<p class="error" id="errorMessage" role="alert"></p>
```

Sin embargo, el botón "Calcular" en modo oscuro mantiene `background: var(--accent)` (`#0f6c5c`) con texto `#fff`, un ratio de contraste de ~4.5:1 que queda justo en el límite WCAG AA. El QA no lo detectó ni lo corrigió porque el prompt solo pedía revisar contraste del mensaje de error, no de los botones:

```css
/* Este caso quedó sin corregir — contraste borderline en modo oscuro */
button {
  background: var(--accent); /* #0f6c5c sobre #fff: ratio ~4.5:1 */
  color: #fff;
}
```

Esto demuestra que el AI Red Teaming cubre exactamente los casos descritos en el prompt, pero no realiza una auditoría exhaustiva por iniciativa propia. El alcance del QA es tan bueno como la especificación que lo define.

---

### 🏅 Conclusión del Cierre

La IA es sumamente eficiente y rápida, pero necesita un marco de reglas (Guardrails) claras. El Vibe Coding es útil para prototipar rápido sin dolor, pero en entornos de ingeniería reales, el Spec-Driven Development es el estándar para asegurar el control sobre la arquitectura técnica.
