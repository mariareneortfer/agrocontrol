# agrocontrol-backend

Backend real de AgroControl con Spring Boot, Maven y Java 21. Desde el Capítulo 05 está conectado a PostgreSQL con JPA/Hibernate.

| Dato | Respuesta |
|---|---|
| Package base | `com.agrocontrol` |
| Entidad padre 1:N | Predio (tabla `predio`), módulo `farm` |
| Entidad dependiente N | Parcela (tabla `parcela`), módulo `plot` |
| Relación | 1 predio tiene muchas parcelas (FK `parcela.predio_id`) |

## Capítulo 05 - PostgreSQL + JPA/Hibernate

Desde este capítulo los predios ya no viven en memoria: se guardan en la tabla `agrocontrol.predio` de PostgreSQL.

### Ficha
| Dato | Respuesta |
|---|---|
| Nombre del proyecto | AgroControl (`agrocontrol-backend`) |
| Base de datos | `agrocontrol` |
| Schema | `agrocontrol` |
| Usuario de la base | `agrocontrol_admin` |
| Tabla padre | `predio` |
| PK de la tabla padre | `predio_id` (BIGINT, generado por PostgreSQL con IDENTITY) |
| Tabla dependiente | `parcela` |
| FK hacia la tabla padre | `parcela.predio_id` → `predio.predio_id` (`fk_parcela_predio`) |
| UNIQUE importantes | `uq_predio_nombre` (nombre); `uq_parcela_predio_codigo` (predio_id, codigo) |

Comprobación en DataGrip, con la conexión `AgroControl`:
```sql
SELECT current_database() AS base_actual,
       current_schema()   AS schema_actual,
       current_user       AS usuario_actual;
-- agrocontrol | agrocontrol | agrocontrol_admin
```

### Columnas reales de `predio`
Obtenidas de `information_schema.columns` antes de escribir la entidad JPA:

| Columna | Tipo | NULL | Mapeo en `PredioJpaEntity` |
|---|---|---|---|
| `predio_id` | bigint | NO | `id`, `@Id` + `@GeneratedValue(IDENTITY)` |
| `nombre` | character varying(100) | NO | `nombre`, `nullable = false`, `length = 100` |
| `ubicacion` | character varying(200) | SÍ | `ubicacion`, `length = 200` |
| `descripcion` | text | SÍ | `descripcion`, `columnDefinition = "text"` |
| `created_at` | timestamp with time zone | NO | no se mapea: lo llena PostgreSQL con `DEFAULT CURRENT_TIMESTAMP` |
| `updated_at` | timestamp with time zone | NO | no se mapea: lo llena PostgreSQL con `DEFAULT CURRENT_TIMESTAMP` |

### Dependencias agregadas al `pom.xml`
- `spring-boot-starter-data-jpa`: trae JPA, Hibernate y Spring Data JPA.
- `org.postgresql:postgresql` (scope `runtime`): el driver JDBC que habla con PostgreSQL.

### Configuración (`application.yml`)
- `url: jdbc:postgresql://localhost:5432/agrocontrol` y `username: agrocontrol_admin`.
- `ddl-auto: validate`: al arrancar, Hibernate **comprueba** que las tablas y columnas mapeadas existen en la base. Nunca crea ni modifica tablas: el esquema lo define la migración V1, y Java se adapta a ese contrato.
- `hibernate.default_schema: agrocontrol`: las entidades no escriben el schema; lo toman de aquí.
- `logging.level.org.hibernate.SQL: debug`: muestra en la consola el SQL que genera Hibernate.
- **La contraseña no está en `application.yml`.** Se lee de `config/db-local.yml`, que está en `.gitignore` y no se sube a GitHub. El archivo `config/db-local.example.yml` muestra el formato.

### Estructura nueva
```text
farm
├── domain
│   └── model
│       └── Predio.java                      ← dominio, SIN anotaciones JPA
├── application
│   └── PredioService.java                   ← ahora usa el repositorio Spring Data
└── infrastructure
    └── adapter
        ├── in/web                           ← PredioController y DTO (Capítulo 04)
        └── out
            └── persistence
                ├── entity
                │   └── PredioJpaEntity.java ← @Entity de la tabla predio
                ├── mapper
                │   └── PredioPersistenceMapper.java
                └── repository
                    └── SpringDataPredioRepository.java
plot
└── infrastructure/adapter/out/persistence/entity
    └── ParcelaJpaEntity.java                ← @ManyToOne hacia PredioJpaEntity
```

### Por qué el dominio no tiene `@Entity`
`Predio` representa el concepto del negocio y no debe depender de una tecnología. `PredioJpaEntity` representa la fila de la tabla. `PredioPersistenceMapper` traduce de una a otra. Si mañana cambia la forma de guardar, el dominio no se toca.

### Relación 1:N
La FK está en la tabla `parcela` (el lado N). Por eso `@ManyToOne` y `@JoinColumn(name = "predio_id")` están en `ParcelaJpaEntity`. No se agregó una colección `@OneToMany` en `PredioJpaEntity`, porque ningún caso de uso necesita todavía cargar todas las parcelas desde el predio.

### JDBC, JPA, Hibernate y Spring Data JPA
| Concepto | Significado |
|---|---|
| JDBC | La forma básica de Java de hablar con una base de datos: conexiones y SQL escrito a mano. |
| JPA | La especificación estándar (las reglas y anotaciones `@Entity`, `@Column`) para mapear objetos a tablas. |
| Hibernate | La implementación de JPA que usa Spring Boot: genera el SQL y usa JDBC por debajo. |
| Spring Data JPA | Capa encima de JPA: basta con declarar la interfaz `SpringDataPredioRepository` y Spring crea la implementación. |
| ORM | Mapeo objeto-relacional: traducir entre objetos Java y filas de tablas. |

### Pruebas
Con la aplicación encendida, desde PowerShell en la carpeta `agrocontrol-backend`:

| Prueba | Resultado esperado |
|---|---|
| `GET /api/farms` | 200 con los predios que ya estaban en la base (San Isidro y La Esperanza, de la Clase 06) |
| `POST /api/farms` con `http/predio-valido.json` (Santa Rosa) | 201; en la consola se ve el `insert into agrocontrol.predio` |
| `POST /api/farms` con `http/predio-duplicado.json` (San Isidro) | 409: PostgreSQL lo rechaza por `uq_predio_nombre` |
| `POST /api/farms` con `http/predio-sin-nombre.json` | 400: `@Valid` lo rechaza antes de llegar a la base (`nombre` es NOT NULL) |
| `GET /api/farms/999` | 404 |

Comprobación en DataGrip de que el dato quedó guardado:
```sql
SELECT *
FROM agrocontrol.predio
ORDER BY predio_id DESC;
```

Pruebas de configuración (se hacen y luego se deshacen):
- **Schema incorrecto**: si en `application.yml` se cambia `default_schema: agrocontrol` por `default_schema: public`, la aplicación no arranca, porque Hibernate no encuentra la tabla `public.predio`.
- **Columna incorrecta**: si en `PredioJpaEntity` se cambia `@Column(name = "nombre", ...)` por `@Column(name = "nombre_predio", ...)`, la aplicación no arranca, porque esa columna no existe.

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

### Datos en memoria (solo en el Capítulo 04)
En este capítulo `PredioService` guardaba los predios en una `List` y genera los ids con un `AtomicLong`. Al reiniciar la aplicación se perdían todos los predios. En el Capítulo 05 la lista se reemplazó por la tabla `predio` de PostgreSQL.

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
