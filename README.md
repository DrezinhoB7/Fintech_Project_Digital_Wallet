# 💳 FinTech • Billetera Digital y Motor de Riesgos

Plataforma financiera tipo neobanco desarrollada como proyecto académico para la asignatura de **Estructuras de Datos** (Universidad del Quindío). El sistema integra una interfaz web moderna en **Angular** empaquetada dentro de un backend en **Spring Boot**, operando bajo una arquitectura híbrida que sincroniza persistencia relacional en disco (**H2 Database**) con **7 Tipos de Datos Abstractos (TDAs) implementados desde cero en memoria RAM**.

---

## 🎯 Descripción del Problema

Las plataformas financieras digitales enfrentan el reto de procesar múltiples operaciones concurrentes entre cuentas —recargas, transferencias, pagos programados y programas de fidelización— manteniendo tiempos de respuesta bajos y detectando comportamientos fraudulentos en tiempo real.

Cuando las búsquedas de titulares, el rastreo de transferencias circulares (triangulación) o el ordenamiento dinámico de usuarios por puntaje dependen únicamente de recorridos secuenciales, el rendimiento se degrada y se dificulta la auditoría preventiva. **FinTech** resuelve esta problemática combinando persistencia relacional con estructuras de datos personalizadas en memoria RAM (`TablaHash`, `ArbolBST`, `Grafo`, `ColaPrioridad`, `Cola`, `Pila` y `Lista`) para indexar entidades en tiempo constante, analizar redes transaccionales y ejecutar alertas y reversiones de manera eficiente.

---

## 🏗️ Arquitectura del Sistema

La aplicación funciona de manera unificada en un único servidor (`localhost:8080`), sirviendo el compilado estático del frontend directamente desde `src/main/resources/static/` en Spring Boot y manteniendo sincronizadas las estructuras en memoria con la base de datos en cada operación transaccional.

### Stack Tecnológico
* **Backend:** Java 21, Spring Boot 4, Spring Data JPA, H2 Database.
* **Frontend:** Angular (arquitectura *Zoneless* con *Signals*), TypeScript, HTML5/CSS3.
* **Pruebas:** JUnit 5 (validación aislada de las estructuras de datos propias).

---

## 📐 Diagrama de Clases (UML)

Debido a la extensión de la arquitectura, el diagrama de clases completo del sistema (entidades, servicios, controladores y los 7 TDAs propios) se encuentra disponible en el siguiente documento:

👉 **[Ver Diagrama de Clases Completo (PDF)](docs/uml_plataforma_fintech_springboot.pdf)**



---

## 🧠 Estructuras de Datos Implementadas y Justificación

Todas las estructuras fueron construidas desde cero sin utilizar colecciones nativas de Java (`java.util.*`) para su lógica interna:

| Estructura (TDA) | Complejidad Temporal | Justificación y Función en FinTech |
| :--- | :--- | :--- |
| **`TablaHash<K, V>`** | Búsqueda e inserción promedio en $O(1)$ | Permite localizar usuarios, billeteras y beneficios por ID o número de cuenta en tiempo constante durante validaciones transaccionales. |
| **`ArbolBST<T>`** | Inserción promedio en $O(\log n)$ y recorrido *in-order* en $O(n)$ | Mantiene ordenados dinámicamente a los usuarios por puntos acumulados para generar el ranking de fidelidad (`BRONCE`, `PLATA`, `ORO`) sin reordenamientos costosos. |
| **`Grafo<T>`** | Recorrido de rutas y ciclos en $O(V + E)$ | Modela las billeteras como vértices y las transferencias como aristas dirigidas ponderadas, permitiendo detectar triangulaciones circulares sospechosas. |
| **`ColaPrioridad<T>`** | Extracción del más próximo en $O(1)$ e inserción ordenada en $O(n)$ | Garantiza que las operaciones programadas se despachen estrictamente según su marca de tiempo más cercana (`fechaEjecucion`). |
| **`Cola<T>`** | Encolar y desencolar en $O(1)$ | Asegura un procesamiento justo bajo política FIFO (*First-In, First-Out*) para las notificaciones y alertas de seguridad enviadas a los usuarios. |
| **`Pila<T>`** | Apilar y desapilar en $O(1)$ | Implementa comportamiento LIFO (*Last-In, First-Out*) sobre el historial reciente para revertir (*undo*) la última transacción elegible de forma inmediata. |
| **`Lista<T>`** | Inserción en $O(1)$ y recorrido secuencial en $O(n)$ | Almacena colecciones dinámicas de tamaño variable como historiales de movimientos, beneficios canjeados y registros de auditoría. |

---

## 🛡️ Motor Antifraude y Reglas de Negocio

Cada transferencia o recarga pasa por un análisis de riesgo en tiempo real calibrado a pesos colombianos (**COP**) mediante parámetros configurables en `application.properties`:

* **Umbrales por Monto (COP):** Clasificación automática de riesgo en `BAJO`, `MEDIO` ($> \$1.000.000$), `ALTO` ($> \$3.000.000$) y `CRITICO` ($> \$10.000.000$).
* **Detección de Ciclos en Grafo:** Al registrar una transferencia, se verifica si cierra un camino dirigido en el `Grafo` (ej. `A → B → C → A`), escalando inmediatamente la operación a riesgo **`CRITICO`** y registrando el evento en la auditoría.
* **Control de Velocidad y Anomalías:** Detección de múltiples operaciones en ventanas cortas de tiempo (`max-tx-por-ventana=3` en `5 minutos`) y desviaciones respecto al historial promedio del titular (`factor-monto-inusual=3.0`).
* **Club de Puntos y Fidelización:** Acumulación automática de puntos por operación, ascenso de categoría (`BRONCE`, `PLATA`, `ORO`) y canje de recompensas.

---

## 🚀 Instalación y Ejecución

Dado que el compilado de Angular ya se encuentra integrado dentro de `src/main/resources/static/`, **no se requiere tener Node.js ni Angular CLI instalados** para ejecutar el sistema completo.

1. **Clonar el repositorio:**
   ```bash
   git clone https://github.com/DrezinhoB7/Fintech_Project_Digital_Wallet.git
   cd Fintech_Project_Digital_Wallet
   ```

2. **Ejecutar la aplicación con Maven Wrapper (o desde IntelliJ IDEA):**
   ```bash
   ./mvnw spring-boot:run
   ```
   *(En Windows PowerShell o CMD usar `mvnw.cmd spring-boot:run`)*

3. **Acceder a la plataforma:**
    * **Aplicación Web FinTech:** [http://localhost:8080](http://localhost:8080)
    * **Consola Base de Datos H2:** [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
        * **JDBC URL:** `jdbc:h2:file:./data/fintech_db`
        * **User Name:** `sa`
        * **Password:** *(dejar vacío)*

---

## 🧪 Pruebas Unitarias

El proyecto incluye la suite `EstructurasDatosTest` con **JUnit 5** para verificar de forma aislada las operaciones fundamentales (inserción, búsqueda, ordenamiento, extracción y casos borde) de los **7 TDAs propios**:

```bash
./mvnw test
```

---

## 📊 Escenarios Precargados (`DataInitializer`)

Al iniciar por primera vez con la base de datos vacía, el sistema puebla automáticamente un entorno de pruebas listo para demostración:

| Titular | Nivel / Puntos | Billeteras Asignadas | Saldo Inicial (COP) | Escenario Preparado |
| :--- | :--- | :--- | ---: | :--- |
| **Robinson Ronaldo** | `ORO` (2.500 pts) | `BIL-1001` (Gastos Diarios)<br>`BIL-1004` (Inversión) | `$2.500.000`<br>`$15.000.000` | Canje VIP y saldo alto en `BIL-1004` para disparar umbral `CRITICO` ($> \$10.000.000$). |
| **Pacho Gonzales** | `PLATA` (850 pts) | `BIL-1002` (Gastos Diarios) | `$1.400.000` | Nodo intermedio en la red de transferencias y beneficios de nivel Plata. |
| **Andrés Gómez** | `BRONCE` (120 pts) | `BIL-1003` (Ahorro) | `$650.000` | Cierre de triangulación (`BIL-1001 → BIL-1002 → BIL-1003 → BIL-1001`) visible en el Grafo. |
| **Sofía Martínez** | `PLATINO` (10.000 pts) | `BIL-1005` (Inversión) | `$25.000.000` | Nivel máximo en el `ArbolBST`, transferencias de alto valor (`ALTO`) y acceso a beneficios Platino. |

---

## 👥 Autores

* **Angel David Diaz Pinilla** – Ingeniería de Sistemas y Computación, Universidad del Quindío.
* **Carlos Andres Molina Leal** – Ingeniería de Sistemas y Computación, Universidad del Quindío.
* **Tomas Fernandez Gómez** – Ingeniería de Sistemas y Computación, Universidad del Quindío.