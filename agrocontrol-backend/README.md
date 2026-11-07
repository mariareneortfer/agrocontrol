# agrocontrol-backend

Backend real de AgroControl con Spring Boot, Maven y Java 21. Todavía no usa JPA ni se conecta a PostgreSQL: los datos viven en memoria.

| Dato | Respuesta |
|---|---|
| Package base | `com.agrocontrol` |
| Entidad padre 1:N | Predio (tabla `predio`), módulo `farm` |
| Entidad dependiente N | Parcela (tabla `parcela`), módulo `plot` |
| Relación | 1 predio tiene muchas parcelas (FK `parcela.predio_id`) |

## Capítulo 04 - API REST de Predio con DTO y validación

### Por qué Predio es la entidad padre
Es la raíz del modelo: sin predio no hay parcelas, y la FK `parcela.predio_id` apunta a él. Además es la tabla con la que empieza la migración V1.

### URL base
`/api/farms`. Se usa el plural en inglés para seguir el estilo de la API de la ficha del proyecto: `/plots`, `/campaigns`, `/tasks`. `farm` es también el nombre del módulo.

### Estructura
```text
com.agrocontrol
├── AgrocontrolBackendApplication.java
├── shared
│   ├── application
│   │   └── ProjectInfoService.java
│   └── web
│       ├── HealthController.java              ← GET /api/health
│       └── ValidacionExceptionHandler.java    ← arma la respuesta 400
├── farm                                       ← módulo Predio
│   ├── domain
│   │   └── Predio.java
│   ├── application
│   │   └── PredioService.java                 ← lista en memoria + AtomicLong
│   └── infrastructure
│       └── adapter
│           └── in
│               └── web                        ← adaptador de entrada
│                   ├── PredioController.java
│                   └── dto
│                       ├── CrearPredioRequest.java
│                       └── PredioResponse.java
└── plot                                       ← módulo Parcela (siguientes capítulos)
```

El endpoint demo del Capítulo 03 (`/api/farms/demo`) se retiró: lo reemplaza la API real de predios.

### Contrato HTTP
| Verbo | Ruta | Entrada | Salida | Status |
|---|---|---|---|---|
| POST | `/api/farms` | JSON `CrearPredioRequest` | `PredioResponse` | 201 / 400 |
| GET | `/api/farms` | Query param opcional `nombre` | Lista de `PredioResponse` | 200 |
| GET | `/api/farms/{id}` | Path variable `id` | `PredioResponse` | 200 / 404 |

### Request y Response
- `CrearPredioRequest`: `nombre`, `ubicacion`, `descripcion`. Es lo que el cliente envía. **No lleva `id`**, porque lo genera el sistema.
- `PredioResponse`: `id`, `nombre`, `ubicacion`, `descripcion`. Es lo que la API devuelve. El `id` **sale pero no entra**.
- La clase de dominio `Predio` nunca se expone directamente: si mañana cambia por dentro (por ejemplo, al conectar JPA), el contrato de la API no cambia.

### Validaciones
| Campo | Validación | Regla que protege |
|---|---|---|
| `nombre` | `@NotBlank` | `predio.nombre NOT NULL`: un predio sin nombre no es válido. |
| `nombre` | `@Size(max = 100)` | `predio.nombre VARCHAR(100)`: PostgreSQL rechazaría un nombre más largo. |
| `ubicacion` | `@Size(max = 200)` | `predio.ubicacion VARCHAR(200)`. Es opcional. |
| `descripcion` | `@Size(max = 1000)` | Es `TEXT` en la base; se pone un límite razonable para la API. Es opcional. |

Si una validación falla, `@Valid` detiene la petición antes de llegar al servicio y `ValidacionExceptionHandler` responde 400 con el campo y el mensaje:

```json
{"status":400,"error":"Bad Request","errores":{"nombre":"El nombre del predio es obligatorio"},"timestamp":"..."}
```

### Anotaciones
| Anotación | Qué hace |
|---|---|
| `@RestController` | La clase atiende peticiones HTTP y sus métodos devuelven datos que Spring convierte a JSON. |
| `@RequestMapping("/api/farms")` | Fija la ruta base que comparten todos los métodos del controller. |
| `@PostMapping` | El método responde a peticiones POST, que crean un recurso. |
| `@GetMapping` | El método responde a peticiones GET, que consultan sin modificar. |
| `@RequestBody` | Toma el JSON del cuerpo de la petición y lo convierte en el record `CrearPredioRequest`. |
| `@Valid` | Revisa las validaciones del DTO (`@NotBlank`, `@Size`) antes de ejecutar el método. |
| `@PathVariable` | Toma un valor que es parte de la ruta: el `1` de `/api/farms/1`. Identifica un recurso concreto. |
| `@RequestParam` | Toma un valor opcional después del `?`: el `isidro` de `/api/farms?nombre=isidro`. Sirve para filtrar. |

### Códigos de respuesta
- **201 Created**: el POST creó un predio nuevo. Es más preciso que 200, que solo dice "salió bien".
- **200 OK**: la consulta encontró lo pedido.
- **400 Bad Request**: el JSON enviado no cumple las validaciones.
- **404 Not Found**: no existe un predio con ese id.

### Datos en memoria
`PredioService` guarda los predios en una `List` y genera los ids con un `AtomicLong`. **Si se reinicia la aplicación, todos los predios se pierden** y los ids vuelven a empezar en 1. En el próximo capítulo la lista se reemplazará por la tabla `predio` de PostgreSQL.

### Pruebas
Las peticiones están en `requests.http`. Como el cliente HTTP de IntelliJ requiere licencia, las pruebas se ejecutan con `curl.exe` desde PowerShell, dentro de la carpeta `agrocontrol-backend`. Los cuerpos JSON están en la carpeta `http/`.

| Prueba | Comando | Resultado esperado |
|---|---|---|
| POST válido | `curl.exe -i -X POST http://localhost:8080/api/farms -H "Content-Type: application/json" -d "@http/predio-valido.json"` | 201 + JSON con `id` 1 |
| POST válido 2 | igual, con `@http/predio-valido-2.json` | 201 + JSON con `id` 2 |
| POST sin nombre | igual, con `@http/predio-sin-nombre.json` | 400, error en `nombre` |
| POST nombre de más de 100 caracteres | igual, con `@http/predio-nombre-largo.json` | 400, error en `nombre` |
| GET lista | `curl.exe -i http://localhost:8080/api/farms` | 200 + arreglo con 2 predios |
| GET con filtro | `curl.exe -i "http://localhost:8080/api/farms?nombre=isidro"` | 200 + solo San Isidro |
| GET por id existente | `curl.exe -i http://localhost:8080/api/farms/1` | 200 + San Isidro |
| GET por id inexistente | `curl.exe -i http://localhost:8080/api/farms/999` | 404 |

Las capturas de PowerShell con estas respuestas son la evidencia del capítulo.

### Adaptador de entrada
`PredioController` y sus DTO están en `farm/infrastructure/adapter/in/web`. Es el **adaptador de entrada**: traduce HTTP y JSON al lenguaje del servicio. El controller no tiene lógica de negocio: valida la forma de los datos, delega en `PredioService` y convierte el resultado en `PredioResponse`.

## Capítulo 03 - Spring Boot base (resumen)
- Proyecto creado con Spring Initializr desde IntelliJ: Maven, Java 21, Jar, dependencias Spring Web y Validation, sin JPA ni driver de PostgreSQL.
- `GET /api/health` responde el estado de la aplicación usando el Bean `ProjectInfoService`, inyectado por constructor.
- Spring crea los Beans (`@Service`, `@RestController`) e inyecta las dependencias: inversión de control e inyección de dependencias.
- El script de la base de datos está en `src/main/resources/db/migration/V1__init_core.sql`, donde lo buscará Flyway más adelante.
