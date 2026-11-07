# AgroControl — Laboratorio de Java 21

Laboratorio de Java 21 puro, sin Spring Boot, sin JPA y sin conexión a PostgreSQL. Representa en Java dos tablas del diseño físico de AgroControl y la relación 1:N entre ellas.

- Proyecto oficial: 16. AgroControl — Gestión de Lotes Agrícolas, Campañas, Labores, Insumos y Cosecha
- Proyecto IntelliJ: `agrocontrol-backend-lab`, con JDK 21
- Package base: `com.agrocontrol`

## Capítulo 02 - Java 21

### Entidad padre
`Predio` (tabla `predio`, PK `predio_id`), en el módulo `farm`. Atributo UNIQUE: `nombre` (`uq_predio_nombre`).

### Entidad dependiente
`Parcela` (tabla `parcela`, PK `parcela_id`), en el módulo `plot`. Atributo UNIQUE dentro del predio: `codigo` (`uq_parcela_predio_codigo`).

### Relación
1:N porque un predio tiene muchas parcelas y cada parcela pertenece a un solo predio. En PostgreSQL está en la FK `parcela.predio_id`. En Java aparece de dos formas:
- En el hijo: `Parcela` guarda `predioId`, igual que la FK.
- En el padre: `Predio` tiene una `List<Parcela>` privada y el método `agregarParcela()`, que comprueba que la parcela apunte a ese predio y que su código no se repita.

### Estructura
```text
src/com/agrocontrol
├── Main.java
├── farm
│   ├── domain
│   │   ├── Predio.java
│   │   ├── exception
│   │   │   ├── PredioNoEncontradoException.java
│   │   │   ├── NombrePredioDuplicadoException.java
│   │   │   └── CodigoParcelaDuplicadoException.java
│   │   └── port
│   │       └── PredioRepository.java
│   ├── application
│   │   └── PredioService.java
│   └── infrastructure
│       └── memory
│           └── PredioRepositoryEnMemoria.java
└── plot
    ├── domain
    │   └── Parcela.java
    └── application
        └── command
            └── RegistrarParcelaCommand.java
```

Respecto al Capítulo 01, `PredioRepository` se movió a `domain/port` y las excepciones se movieron a `domain/exception`, siguiendo la estructura de la guía.

### Interfaz (contrato)
`PredioRepository` declara `guardar`, `buscarPorId`, `listarTodos` y `existePorNombre`. No dice dónde se guardan los datos: no menciona PostgreSQL, SQL ni JPA. La tecnología se decide fuera, en `infrastructure`.

`PredioService` depende de la interfaz `PredioRepository`, no de `PredioRepositoryEnMemoria`. Cuando llegue PostgreSQL se escribirá otra implementación del mismo contrato, y el servicio no cambiará.

### Colección elegida
- **Map** en `PredioRepositoryEnMemoria` (`LinkedHashMap<Long, Predio>`), porque la operación principal es buscar un predio por su id: el id es la clave, como la PK en PostgreSQL. `LinkedHashMap` conserva el orden de registro.
- **List** en `Predio` para sus parcelas, porque importa el orden en que se agregaron. Los duplicados no se evitan con la colección, sino con la regla de `agregarParcela()`, que compara códigos.
- **Set** no se usó: ninguna operación necesita solo un conjunto de valores distintos sin orden ni clave.

### Optional
`buscarPorId` puede no encontrar el predio, así que devuelve `Optional<Predio>` en vez de `null`. El servicio lo resuelve con `orElseThrow`, que lanza `PredioNoEncontradoException` cuando el predio no existe.

### Excepciones propias
- `PredioNoEncontradoException`: se pide un predio con un id que no existe. Por ejemplo, `obtener(999L)` o registrar una parcela en el predio 999.
- `NombrePredioDuplicadoException`: se registra un predio con un nombre que ya existe, sin importar mayúsculas (regla `uq_predio_nombre`).
- `CodigoParcelaDuplicadoException`: se agrega a un predio una parcela con un código que ya tiene (regla `uq_parcela_predio_codigo`).

Los datos inválidos de un solo objeto, como una superficie 0 o un nombre vacío, siguen usando `IllegalArgumentException` en los constructores.

### Enum
No se creó un enum en este capítulo. Las tablas `predio` y `parcela` no tienen ningún CHECK con un conjunto cerrado de valores, y la guía pide no inventar estados. Los estados reales de AgroControl están en otras tablas y serán enums cuando se modelen:
- `EstadoCampana`: `ACTIVA`, `FINALIZADA` (`ck_campana_estado`)
- `EstadoLabor`: `PLANIFICADA`, `ASIGNADA`, `EN_EJECUCION`, `COMPLETADA`, `CANCELADA` (`ck_labor_estado`, RN-03)

### Record
`RegistrarParcelaCommand` agrupa solo los datos que necesita la operación "registrar parcela": `predioId`, `codigo`, `nombre`, `superficie` y `unidadSuperficie`. No lleva `id`, porque lo genera el sistema, ni `ubicacion_referencial`, porque es opcional. Es inmutable y Java genera su constructor y sus métodos de lectura.

### Relación con PostgreSQL
| PostgreSQL | Java en este capítulo |
|---|---|
| PRIMARY KEY `predio_id` | `id` del objeto y clave del `Map` |
| UNIQUE `predio.nombre` | `existePorNombre()` antes de guardar, en `PredioService.registrar()` |
| UNIQUE `(predio_id, codigo)` | regla de `Predio.agregarParcela()` |
| FOREIGN KEY `parcela.predio_id` | `predioId` dentro de `Parcela` |
| CHECK `superficie > 0` | validación del constructor de `Parcela` |
| NOT NULL | validaciones de los constructores |
| Tabla | clase de dominio; todavía no es `@Entity` |

### Qué cambiará con PostgreSQL
Se agregará una nueva implementación de `PredioRepository` en `infrastructure` y `Main` dejará de usar `PredioRepositoryEnMemoria`. El dominio, el contrato y el servicio no tienen que cambiar. El contador temporal de ids de parcela del servicio desaparecerá, porque los ids los generará la columna IDENTITY.

### Evidencia
Salida de consola al ejecutar `Main`:

```text
=== Pruebas positivas ===
Predios registrados: 2
Obtener(1): Predio{id=1, nombre='San Isidro', parcelas=0}
Predio{id=1, nombre='San Isidro', parcelas=2}
  - Parcela{id=1, predioId=1, codigo='P-01', nombre='Lote Norte', superficie=4.50 hectarea}
  - Parcela{id=2, predioId=1, codigo='P-02', nombre='Lote Sur', superficie=3.25 hectarea}
Predio{id=2, nombre='La Esperanza', parcelas=1}
  - Parcela{id=3, predioId=2, codigo='P-01', nombre='Lote Unico', superficie=2.00 hectarea}

=== Pruebas negativas (errores controlados) ===
ERROR CONTROLADO: No existe el predio con id: 999
ERROR CONTROLADO: Ya existe un predio con el nombre: san isidro
ERROR CONTROLADO: Ya existe la parcela P-01 en el predio San Isidro
ERROR CONTROLADO: No existe el predio con id: 999
ERROR CONTROLADO: La superficie debe ser mayor que cero

Parcelas en San Isidro: 2
```

## Capítulo 01 - Java esencial (resumen)
- Se eligió la relación `predio` 1:N `parcela` porque es la más directa del diseño: `labor` no apunta a `campana`, sino a la tabla puente `campana_parcela`.
- Atributos `private` y `final`, validados en el constructor, sin setters: un objeto no puede quedar en un estado inválido.
- `getParcelas()` devuelve una lista de solo lectura, para que nadie se salte la regla de `agregarParcela()`.
- `superficie` es `BigDecimal` porque en PostgreSQL es `NUMERIC(10,2)`, un valor exacto.
