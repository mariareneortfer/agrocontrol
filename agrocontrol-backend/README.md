# agrocontrol-backend

Backend real de AgroControl con Spring Boot, Maven y Java 21. Desde el Capítulo 05 está conectado a PostgreSQL con JPA/Hibernate, desde el Capítulo 06 sigue la arquitectura hexagonal y desde el Capítulo 08 tiene manejo de errores único y transacciones.

| Dato | Respuesta |
|---|---|
| Package base | `com.agrocontrol` |
| Entidad padre 1:N | Predio (tabla `predio`), módulo `farm` |
| Entidad dependiente N | Parcela (tabla `parcela`), módulo `plot` |
| Relación | 1 predio tiene muchas parcelas (FK `parcela.predio_id`) |

## CRUD completo de Predio (Guía 06 de React)

Se agregaron `PUT` y `DELETE` en todas las capas hexagonales del módulo `farm`.

| Capa | Cambio |
|---|---|
| Port IN | `ActualizarPredioUseCase`, `EliminarPredioUseCase` (nuevos) |
| Port OUT `PredioRepositoryPort` | `existePorNombreEnOtroPredio(nombre, id)`, `eliminar(predio)` |
| `PredioService` | `actualizar` y `eliminar`, los dos con `@Transactional` |
| `SpringDataPredioRepository` | `existsByNombreAndIdNot` |
| `PredioPersistenceAdapter` | `eliminar`: `deleteById` + `flush`, y traduce la violación de la FK |
| Excepción de dominio | `PredioConParcelasException` → **409** en `GlobalExceptionHandler` |
| DTO | `ActualizarPredioRequest` (el id viaja en la URL, no en el body) |
| `PredioController` | `PUT /{id}`, `DELETE /{id}`. `GET /{id}` ahora responde 404 con `ApiError` (antes era un 404 sin cuerpo) |

### Contrato
| Operación | Método y ruta | Respuestas |
|---|---|---|
| Listar | `GET /api/farms` | 200 |
| Buscar | `GET /api/farms/{id}` | 200 · 404 |
| Crear | `POST /api/farms` | 201 · 400 · 409 (nombre repetido) |
| Actualizar | `PUT /api/farms/{id}` | 200 · 400 · 404 · 409 (nombre de **otro** predio) |
| Eliminar | `DELETE /api/farms/{id}` | **204** sin cuerpo · 404 · **409** (tiene parcelas) |

### ¿Qué pasa al eliminar un predio que tiene parcelas?
`fk_parcela_predio` no tiene `ON DELETE CASCADE`, así que PostgreSQL **rechaza** el `DELETE`. El adaptador hace `flush()` para que el error aparezca en ese momento, lo captura (`DataIntegrityViolationException`) y lo convierte en `PredioConParcelasException`. La transacción se deshace y la respuesta es:

```json
{ "status": 409, "message": "No se puede eliminar el predio San Isidro porque tiene parcelas asociadas. Elimine o mueva primero sus parcelas." }
```

¿Por qué no se cuentan las parcelas en `PredioService`? Porque para eso `farm` tendría que depender de `plot`, y `plot` ya depende de `farm` (`ParcelaService` usa `ConsultarPredioUseCase`). Sería una **dependencia circular**. La FK de PostgreSQL ya protege la regla; el adaptador solo la traduce a un error de dominio.

**Borrado físico vs baja lógica:** aquí se usa borrado físico (la fila desaparece). La baja lógica necesitaría una columna `activo` (`UPDATE ... SET activo = false`), que la tabla `predio` no tiene.

### Pruebas con curl.exe
```powershell
curl.exe -i http://localhost:8080/api/farms/999
curl.exe -i -X PUT http://localhost:8080/api/farms/1 -H "Content-Type: application/json" -d "{\"nombre\":\"San Isidro\",\"ubicacion\":\"Km 12 carretera al norte\",\"descripcion\":null}"
curl.exe -i -X DELETE http://localhost:8080/api/farms/1
```
Resultados: `404`; `200` (es el mismo nombre del propio predio, así que no hay conflicto); `409` (San Isidro tiene parcelas).

## Conexión con el frontend (Guía 05 de React)

Se hicieron dos cambios para que `agrocontrol-frontend` (React + Vite en `http://localhost:5173`) pueda usar esta API:

### 1. CORS: `shared/config/CorsConfig.java`
El navegador bloquea las respuestas de otro origen (puerto 8080) si el backend no autoriza explícitamente el origen de Vite (puerto 5173).
```java
registry.addMapping("/api/**")
        .allowedOrigins("http://localhost:5173")
        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
        .allowedHeaders("*");
```
- Se autoriza **un solo origen**, no `*`.
- CORS **no** autentica usuarios ni protege la base de datos: solo decide qué página web del navegador puede leer las respuestas. `curl` y Postman no son navegadores, por eso nunca tuvieron problemas de CORS.

### 2. Nuevo endpoint `GET /api/plots` (todas las parcelas)
El listado del frontend necesita todas las parcelas, y antes solo existía `GET /api/plots/farm/{predioId}`. Se agregó en todas las capas hexagonales:

| Capa | Cambio |
|---|---|
| Port IN `ConsultarParcelaUseCase` | `List<Parcela> listar()` |
| Port OUT `ParcelaRepositoryPort` | `List<Parcela> listarTodas()` |
| `ParcelaService` | `listar()` con `@Transactional(readOnly = true)` |
| `SpringDataParcelaRepository` | `findAllByOrderByIdAsc()` |
| `ParcelaPersistenceAdapter` | `listarTodas()` |
| `ParcelaController` | `@GetMapping` → `GET /api/plots` |

Prueba:
```powershell
curl.exe -i http://localhost:8080/api/plots
```
Respuesta esperada: `200` con un arreglo JSON de parcelas.

### Contrato que usa el frontend
| Método y ruta | Body | Respuesta |
|---|---|---|
| `GET /api/farms` | - | `200` con `PredioResponse[]` |
| `POST /api/farms` | `{ nombre, ubicacion, descripcion }` | `201` con `PredioResponse` · `400` · `409` |
| `GET /api/plots` | - | `200` con `ParcelaResponse[]` |
| `POST /api/plots` | `{ predioId, codigo, nombre, superficie, unidadSuperficie, ubicacionReferencial }` | `201` con `ParcelaResponse` · `400` · `404` · `409` |

Los errores siempre llegan con el formato `ApiError` del Capítulo 08 (`status`, `message`, `fieldErrors`), y el frontend muestra ese `message`.

## Capítulo 08 - Errores profesionales, validaciones y transacciones

Todos los errores de la API responden con **la misma forma** (`ApiError`). Un solo `@RestControllerAdvice` (`GlobalExceptionHandler`) decide qué código HTTP corresponde a cada error, y los controllers no tienen `try/catch`.

### Equivalencias con el ejemplo del profesor
| Profesor (ParkFlow) | AgroControl |
|---|---|
| `com.parkflow` | `com.agrocontrol` |
| Cliente / Vehiculo | Predio (`farm`) / Parcela (`plot`) |
| `ClienteNoEncontradoException` | `PredioNoEncontradoException` |
| `PlacaDuplicadaException` | `CodigoParcelaDuplicadoException` |
| `RegistrarVehiculoCommand` | `RegistrarParcelaCommand` |
| `/api/vehicles` | `/api/plots` |

### Formato único de error: `ApiError`
```json
{
  "timestamp": "2026-11-07T21:03:24Z",
  "status": 404,
  "error": "Not Found",
  "message": "No existe el predio con id: 999",
  "path": "/api/plots",
  "fieldErrors": {}
}
```
`fieldErrors` solo se llena en los errores de validación (400), con un mensaje por campo.

### Qué código devuelve cada error (`shared/web/GlobalExceptionHandler`)
| Excepción | HTTP | Cuándo pasa |
|---|---|---|
| `MethodArgumentNotValidException` | 400 | Falla una validación del DTO (`@NotNull`, `@Positive`...) |
| `HttpMessageNotReadableException` | 400 | El JSON está mal escrito o un tipo no calza (`"superficie": "mucho"`) |
| `MethodArgumentTypeMismatchException` | 400 | Un parámetro de la URL no es del tipo esperado (`/api/plots/abc`) |
| `IllegalArgumentException` | 400 | Regla del dominio (`Parcela`: superficie > 0) |
| `PredioNoEncontradoException` | 404 | El predio no existe |
| `NoResourceFoundException` | 404 | La ruta no existe |
| `HttpRequestMethodNotSupportedException` | 405 | Método HTTP no permitido |
| `PredioDuplicadoException` | 409 | Nombre de predio repetido |
| `CodigoParcelaDuplicadoException` | 409 | Código de parcela repetido en el mismo predio |
| `DataIntegrityViolationException` | 409 | PostgreSQL rechaza el dato (UNIQUE, FK o CHECK) |
| `Exception` (cualquier otra) | 500 | Error inesperado: mensaje genérico; el detalle solo va al log |

Los tres handlers anteriores (`ValidacionExceptionHandler`, `PredioExceptionHandler`, `ParcelaExceptionHandler`) se eliminaron. Si dos `@RestControllerAdvice` atienden la misma excepción, Spring no sabe cuál usar.

### Dónde se valida cada cosa
| Capa | Qué valida | Ejemplo AgroControl |
|---|---|---|
| DTO (`CrearParcelaRequest`) | Forma del dato que llega | `predioId` obligatorio, `superficie` positiva con 2 decimales, `codigo` no vacío |
| Dominio (`Parcela`) | Reglas propias del objeto | Código en mayúsculas, superficie > 0 |
| Servicio (`ParcelaService`) | Reglas que necesitan consultar datos | El predio existe y el código no se repite en ese predio |
| PostgreSQL | Última defensa | `fk_parcela_predio`, `uq_parcela_predio_codigo`, `ck_parcela_superficie_positiva` |

### `@Transactional`
- `ParcelaService.registrar` y `PredioService.registrar` son `@Transactional`. Registrar una parcela consulta el predio, revisa si el código se repite y guarda; todo eso es una sola operación. Si algo falla, no queda nada a medias en la base de datos.
- Las consultas (`buscarPorId`, `listar`, `listarPorPredio`) son `@Transactional(readOnly = true)`, porque solo leen datos.
- `@Transactional` va en el servicio (la capa de aplicación), no en el controller ni en el repositorio.

### `RegistrarParcelaCommand`
El controller ya no arma un objeto `Parcela`. Convierte el DTO en un `RegistrarParcelaCommand` (`ParcelaWebMapper.toCommand`) y se lo pasa al caso de uso. El servicio valida y recién después crea la `Parcela`.

### Pruebas (PowerShell, dentro de `agrocontrol-backend`)
| # | Caso | Comando | Esperado |
|---|---|---|---|
| 1 | Parcela válida P-05 | `curl.exe -i -X POST http://localhost:8080/api/plots -H "Content-Type: application/json" -d "@http/parcela-valida-cap08.json"` | 201 |
| 2 | Superficie 0 | `... -d "@http/parcela-superficie-invalida.json"` | 400 con `fieldErrors.superficie` |
| 3 | Predio 999 | `... -d "@http/parcela-predio-inexistente.json"` | 404 |
| 4 | Código `p-01` repetido en predio 1 | `... -d "@http/parcela-codigo-duplicado.json"` | 409 |
| 5 | Error inesperado | `curl.exe -i http://localhost:8080/api/diagnostico/error-simulado` | 500 con mensaje genérico |
| extra | JSON con texto en superficie | `... -d "@http/parcela-json-invalido.json"` | 400 |

`DiagnosticoController` existe solo para la prueba 5 del laboratorio y se debe borrar antes de producción.

### Verificación en DataGrip
```sql
SELECT parcela_id, predio_id, codigo, nombre
FROM agrocontrol.parcela
ORDER BY parcela_id;
```
Aparece P-05 una sola vez. No aparece ninguna fila de las pruebas que fallaron (P-04, P-09, P-06).

### Preguntas de defensa
1. **¿Por qué un solo `GlobalExceptionHandler`?** Así todos los errores tienen el mismo formato y la regla "excepción → código HTTP" está en un solo lugar.
2. **¿Por qué los controllers no tienen try/catch?** El controller solo recibe y responde. Si ocurre un error, la excepción sube y el handler la traduce.
3. **¿Por qué el 500 no muestra el detalle?** El detalle puede revelar la estructura interna (tablas, clases, SQL). Se guarda en el log y el cliente solo recibe un mensaje genérico.
4. **¿Por qué validar en el DTO, en el servicio y en PostgreSQL?** Cada capa revisa algo distinto: la forma del dato, las reglas del negocio y la integridad final. PostgreSQL protege incluso si alguien se salta la API.
5. **¿Qué hace `@Transactional`?** Ejecuta todo el método como una sola operación: o se guarda todo o no se guarda nada.
6. **¿Por qué `readOnly = true` en las consultas?** Indica que el método no modifica datos, y Hibernate puede optimizar la lectura.

## Capítulo 07 - Relación 1:N Predio → Parcela entre módulos

El módulo `plot` (Parcela) ahora tiene la misma arquitectura hexagonal que `farm` y se relaciona con él respetando la FK real de PostgreSQL.

### Datos de la relación
| Dato | Profesor | AgroControl |
|---|---|---|
| Entidad padre | Cliente | Predio (módulo `farm`) |
| PK padre | `cliente_id` | `predio_id` |
| Entidad dependiente | Vehiculo | Parcela (módulo `plot`) |
| PK dependiente | `vehiculo_id` | `parcela_id` |
| FK dependiente | `cliente_id` | `predio_id` (`fk_parcela_predio`) |
| Campo UNIQUE | `placa` | `codigo`, único **dentro del predio**: `uq_parcela_predio_codigo (predio_id, codigo)` |
| CHECK/enum | `tipo` | No hay enum: `parcela` no tiene un CHECK de valores cerrados. Su CHECK real es `ck_parcela_superficie_positiva` (`superficie > 0`). |
| Package base | `com.parkflow` | `com.agrocontrol` |

```text
PREDIO (1) -------------------- (N) PARCELA
predio_id [PK]                      parcela_id [PK]
nombre [UNIQUE]                     predio_id [FK → predio.predio_id]
                                    codigo [UNIQUE junto con predio_id]
                                    superficie [CHECK > 0]
```

La relación es 1:N porque un predio tiene muchas parcelas y cada parcela pertenece a un solo predio: la FK `predio_id` está en la tabla `parcela`.

### Tres niveles
| Nivel | Cómo aparece la relación |
|---|---|
| Dominio | `Parcela` tiene `Long predioId`. Sin anotaciones JPA. |
| JPA | `ParcelaJpaEntity` tiene `@ManyToOne(fetch = LAZY)` + `@JoinColumn(name = "predio_id")` hacia `PredioJpaEntity`. |
| PostgreSQL | La FK `fk_parcela_predio` sigue en la base. Si llegara un `predio_id` inexistente, PostgreSQL rechazaría el INSERT. |

### Árbol del módulo `plot`
```text
plot
├── domain
│   ├── model
│   │   └── Parcela.java
│   ├── exception
│   │   └── CodigoParcelaDuplicadoException.java
│   └── port
│       ├── in
│       │   ├── RegistrarParcelaUseCase.java
│       │   └── ConsultarParcelaUseCase.java
│       └── out
│           └── ParcelaRepositoryPort.java
├── application
│   └── service
│       └── ParcelaService.java
└── infrastructure
    └── adapter
        ├── in
        │   └── web
        │       ├── ParcelaController.java
        │       ├── ParcelaExceptionHandler.java
        │       ├── dto
        │       │   ├── CrearParcelaRequest.java
        │       │   └── ParcelaResponse.java
        │       └── mapper
        │           └── ParcelaWebMapper.java
        └── out
            └── persistence
                ├── ParcelaPersistenceAdapter.java
                ├── entity
                │   └── ParcelaJpaEntity.java
                ├── mapper
                │   └── ParcelaPersistenceMapper.java
                └── repository
                    └── SpringDataParcelaRepository.java
```

En el módulo `farm` se agregó `PredioNoEncontradoException`, que su `PredioExceptionHandler` traduce a 404.

### Cómo se valida que el predio exista
`ParcelaService` recibe `ConsultarPredioUseCase`, el Port IN **público** del módulo `farm`, y llama a `buscarPorId`. **No** usa `SpringDataPredioRepository`: ese repositorio es un detalle interno de la infraestructura de `farm`.

```text
ParcelaService
    +--> ConsultarPredioUseCase      (correcto: contrato público de farm)
    X--> SpringDataPredioRepository  (incorrecto: detalle interno de farm)
```

El único lugar fuera de `farm` que usa `SpringDataPredioRepository` es `ParcelaPersistenceAdapter`, y solo para `getReferenceById`: la referencia que exige `@ManyToOne`. Queda dentro de infraestructura; el caso de uso no la ve.

### Endpoints
| Operación | Método | Ruta | Status |
|---|---|---|---|
| Registrar parcela | POST | `/api/plots` | 201 / 400 / 404 / 409 |
| Consultar por id | GET | `/api/plots/{id}` | 200 / 404 |
| Listar por predio | GET | `/api/plots/farm/{predioId}` | 200 / 404 |

`/api/plots` es la ruta que usa la API de la ficha del proyecto.

### Pruebas
Desde PowerShell, en la carpeta `agrocontrol-backend`, con la aplicación encendida:

| Prueba | Archivo | Resultado esperado |
|---|---|---|
| Registro válido con predio existente | `http/parcela-valida.json` (predio 1, P-03) | 201 |
| Consulta por id del registro creado | `GET /api/plots/{id}` | 200 |
| Listado de parcelas del mismo predio | `GET /api/plots/farm/1` | 200: P-01, P-02 y P-03 |
| Mismo código en otro predio | `http/parcela-mismo-codigo-otro-predio.json` (predio 2, P-02) | 201: la unicidad es por predio |
| Predio inexistente | `http/parcela-predio-inexistente.json` (predio 999) | 404 "No existe el predio con id: 999" |
| Código repetido en el mismo predio | `http/parcela-codigo-duplicado.json` (predio 1, "p-01") | 409: el código se normaliza a P-01, que ya existe |
| Valor que viola el CHECK | `http/parcela-superficie-invalida.json` (superficie 0) | 400 "La superficie debe ser mayor que cero" |

### Comprobación SQL
```sql
SELECT d.parcela_id, d.codigo, d.nombre AS parcela, d.predio_id AS fk_predio_id,
       p.predio_id, p.nombre AS predio
FROM agrocontrol.parcela d
JOIN agrocontrol.predio p
  ON p.predio_id = d.predio_id
ORDER BY d.parcela_id;
```
Cada fila muestra que `fk_predio_id` coincide con un `predio_id` válido.

### Respuestas para la defensa
| Pregunta | Respuesta |
|---|---|
| ¿Tabla padre y dependiente? | `predio` es la padre; `parcela` es la dependiente. |
| ¿Dónde está la FK física? | En `parcela.predio_id`, protegida por `fk_parcela_predio`. |
| ¿Por qué `@ManyToOne` y no `@OneToMany` en la FK? | La columna FK está en `parcela`, el lado N: muchas parcelas apuntan a un predio. `@OneToMany` sería una colección en el predio, y no se necesita. |
| ¿Por qué el dominio no tiene anotaciones JPA? | Porque representa el negocio; la relación se guarda como `predioId`, igual que la columna. |
| ¿Cómo validas que el predio exista? | `ParcelaService` llama a `ConsultarPredioUseCase.buscarPorId` y lanza `PredioNoEncontradoException` (404). |
| ¿Por qué no llamas al JpaRepository del predio desde el Service? | Porque es un detalle interno de `farm`; los módulos se comunican por sus puertos públicos. |
| ¿Qué UNIQUE tiene la tabla dependiente? | `(predio_id, codigo)`: el código no se repite dentro del mismo predio, pero sí en predios distintos. |
| ¿Qué pasa si la base recibe una FK inexistente? | PostgreSQL rechaza el INSERT por `fk_parcela_predio`. El caso de uso lo evita antes, pero la FK sigue siendo la última defensa. |
| ¿Qué significa LAZY? | Que el predio relacionado no se lee de la base hasta que alguien lo necesita. Leer solo su id no hace otra consulta. |
| Recorrido de POST a PostgreSQL | `ParcelaController` → `ParcelaWebMapper` → `RegistrarParcelaUseCase` → `ParcelaService` (¿existe el predio? ¿código repetido?) → `ParcelaRepositoryPort` → `ParcelaPersistenceAdapter` (`getReferenceById` del predio) → `ParcelaPersistenceMapper` → `SpringDataParcelaRepository` → `INSERT INTO agrocontrol.parcela`. |

## Capítulo 06 - Arquitectura hexagonal en el módulo Predio

El módulo `farm` (Predio) se reorganizó para que **el caso de uso no conozca JPA ni PostgreSQL**. La funcionalidad es la misma del Capítulo 05; cambió quién depende de quién.

### Equivalencia con el proyecto del profesor
| Elemento del profesor | AgroControl |
|---|---|
| Proyecto | `agrocontrol-backend` |
| Package base | `com.agrocontrol` |
| Entidad padre | Predio (módulo `farm`) |
| Tabla PostgreSQL | `predio` |
| PK | `predio_id` |
| Campo UNIQUE 1 | `nombre` (`uq_predio_nombre`) |
| Campo UNIQUE 2 | No existe en `predio`. No se inventa: la otra unicidad del diseño, `(predio_id, codigo)`, es de la tabla `parcela` y se trabajará en el Capítulo 07. |
| Schema | `agrocontrol` |

Tampoco se agregó un atributo `activo`: la tabla `predio` no tiene esa columna.

### Árbol del módulo
```text
farm
├── domain                                   ← NÚCLEO: sin Spring Data, sin JPA
│   ├── model
│   │   └── Predio.java
│   ├── exception
│   │   └── PredioDuplicadoException.java
│   └── port
│       ├── in                               ← qué puede hacer el sistema
│       │   ├── RegistrarPredioUseCase.java
│       │   └── ConsultarPredioUseCase.java
│       └── out                              ← qué necesita el núcleo del exterior
│           └── PredioRepositoryPort.java
├── application
│   └── service
│       └── PredioService.java               ← implementa los Port IN, usa el Port OUT
└── infrastructure
    └── adapter
        ├── in
        │   └── web                          ← ADAPTER IN
        │       ├── PredioController.java
        │       ├── PredioExceptionHandler.java
        │       ├── dto
        │       │   ├── CrearPredioRequest.java
        │       │   └── PredioResponse.java
        │       └── mapper
        │           └── PredioWebMapper.java
        └── out
            └── persistence                  ← ADAPTER OUT
                ├── PredioPersistenceAdapter.java
                ├── entity
                │   └── PredioJpaEntity.java
                ├── mapper
                │   └── PredioPersistenceMapper.java
                └── repository
                    └── SpringDataPredioRepository.java
```

### Recorrido de una petición
```text
POST /api/farms
   ↓
PredioController                (adapter in: HTTP + DTO)
   ↓ PredioWebMapper: request → Predio
RegistrarPredioUseCase          (port in)
   ↓
PredioService                   (caso de uso: ¿ya existe ese nombre?)
   ↓
PredioRepositoryPort            (port out)
   ↓
PredioPersistenceAdapter        (adapter out)
   ↓ PredioPersistenceMapper: Predio → PredioJpaEntity
SpringDataPredioRepository → Hibernate → INSERT INTO agrocontrol.predio
```

### Checklist de arquitectura
- [x] `PredioController` no inyecta `JpaRepository`: recibe `RegistrarPredioUseCase` y `ConsultarPredioUseCase`.
- [x] `PredioService` depende de `PredioRepositoryPort`, una interfaz del dominio.
- [x] `PredioRepositoryPort` no extiende `JpaRepository`.
- [x] `Predio` no tiene `@Entity`.
- [x] `PredioJpaEntity` está dentro de `infrastructure`.
- [x] Hay mapper web (`PredioWebMapper`) y mapper de persistencia (`PredioPersistenceMapper`).
- [x] Ningún archivo de `domain` o `application` importa `jakarta.persistence` ni `org.springframework.data`.

### Pruebas
Desde PowerShell, en la carpeta `agrocontrol-backend`, con la aplicación encendida:

| Prueba | Resultado esperado |
|---|---|
| `POST /api/farms` con `http/predio-valido.json` (El Porvenir) | 201 y fila nueva en `agrocontrol.predio` |
| `GET /api/farms/1` | 200 (San Isidro) |
| `GET /api/farms/999` | 404 |
| `POST /api/farms` con `http/predio-sin-nombre.json` | 400 (validación HTTP) |
| `POST /api/farms` con `http/predio-duplicado.json` (San Isidro) | 409, mensaje "Ya existe un predio con el nombre: San Isidro". Lo detecta el caso de uso con `existePorNombre`, antes de llegar a PostgreSQL. |

Verificación en DataGrip:
```sql
SELECT *
FROM agrocontrol.predio
ORDER BY predio_id;
```

### Respuestas para la defensa
| Pregunta | Respuesta |
|---|---|
| ¿Cuál es tu Port IN y qué capacidad expresa? | `RegistrarPredioUseCase` (registrar un predio) y `ConsultarPredioUseCase` (buscar por id y listar). |
| ¿Cuál es tu Port OUT y por qué está en el núcleo? | `PredioRepositoryPort`. Está en `domain` porque es el núcleo quien define lo que necesita; la infraestructura se adapta a él. Esa es la inversión de dependencias. |
| ¿Qué clase implementa el Port OUT? | `PredioPersistenceAdapter`. |
| ¿Qué clase conoce `JpaRepository`? | Solo `PredioPersistenceAdapter`, a través de `SpringDataPredioRepository`. |
| ¿Qué clase conoce el nombre físico de la tabla? | `PredioJpaEntity`, con `@Table(name = "predio")`. El schema está en `application.yml`. |
| ¿Por qué tu dominio no tiene `@Entity`? | Porque representa el negocio y no debe depender de una tecnología de base de datos. |
| Si cambiáramos PostgreSQL por otra tecnología, ¿qué cambiaría? | Solo la infraestructura de salida: `PredioPersistenceAdapter`, la entidad, el repositorio y el mapper de persistencia. El dominio, los puertos, el caso de uso y el controller no cambian. |
| ¿Qué constraint respetas desde el caso de uso? | `uq_predio_nombre`: `PredioService.registrar` consulta `existePorNombre` y lanza `PredioDuplicadoException` (409) antes de intentar guardar. |

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
