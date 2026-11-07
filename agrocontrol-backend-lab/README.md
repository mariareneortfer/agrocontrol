# Capítulo 01 - Java esencial — AgroControl

Laboratorio de Java 21 sin Spring Boot, sin JPA y sin conexión a PostgreSQL. Representa en Java dos tablas del diseño físico de AgroControl y la relación 1:N entre ellas.

## Datos del laboratorio
- Proyecto oficial: 16. AgroControl — Gestión de Lotes Agrícolas, Campañas, Labores, Insumos y Cosecha
- Nombre del proyecto IntelliJ: `agrocontrol-backend-lab`
- Package base: `com.agrocontrol`
- Tabla/entidad padre A: `predio` → clase `Predio`
- Nombre de módulo A: `farm` (predio, propiedad agrícola)
- Tabla/entidad dependiente B: `parcela` → clase `Parcela`
- Nombre de módulo B: `plot` (parcela; es el mismo término que usa la API de la ficha: `/plots`)
- FK que conecta B con A: `parcela.predio_id -> predio.predio_id`

## Entidades elegidas
- Tabla padre: `predio` (PK `predio_id`)
- Tabla dependiente: `parcela` (PK `parcela_id`)
- Relación: 1:N — un predio tiene muchas parcelas; cada parcela pertenece a un predio.
- FK: `parcela.predio_id` (`fk_parcela_predio` en `V1__init_core.sql`)

Se eligió esta pareja porque es la relación 1:N más directa del diseño: `labor` no apunta directamente a `campana`, sino a la tabla puente `campana_parcela`.

## Estructura
```text
agrocontrol-backend-lab
└── src
    └── com
        └── agrocontrol
            ├── Main.java
            ├── farm
            │   ├── domain
            │   │   ├── Predio.java
            │   │   └── PredioRepository.java
            │   └── exception
            └── plot
                ├── domain
                │   └── Parcela.java
                └── exception
```

Las carpetas `exception` están preparadas para el Capítulo 02. Tienen un `package-info.java` para que Git las conserve, porque Git no guarda carpetas vacías.

## Clases Java
- `Predio`: entidad padre. Atributos `private`, constructor que valida el nombre y una lista privada de parcelas.
- `Parcela`: entidad dependiente. Atributos `private` y constructor que valida código, nombre, superficie y unidad.
- `PredioRepository`: interface de contrato con `guardar` y `buscarPorId`.
- `Main`: crea un predio con dos parcelas, las relaciona y prueba las reglas.

## Cómo se representó el 1:N
| En PostgreSQL | En Java |
|---|---|
| La FK `parcela.predio_id` está en la tabla hija. | El padre `Predio` tiene `private final List<Parcela> parcelas`. |
| Se relacionan al insertar la parcela con su `predio_id`. | Se relacionan con el método de negocio `predio.agregarParcela(parcela)`. |
| `UNIQUE (predio_id, codigo)` impide repetir el código en el predio. | `agregarParcela()` rechaza una parcela con un código que ya existe en ese predio. |

## Reglas implementadas
- **Código único dentro del predio** (`uq_parcela_predio_codigo`, RF-01): `Predio.agregarParcela()` lanza `IllegalStateException` si el código ya existe en ese predio.
- **Superficie mayor que cero** (`ck_parcela_superficie_positiva`, RN-07): el constructor de `Parcela` lanza `IllegalArgumentException`.
- **Unidad explícita** (RN-07): el constructor de `Parcela` exige la unidad de superficie.
- **Nombre obligatorio** (NOT NULL): los constructores de `Predio` y `Parcela` lo exigen.

## Resultado de ejecutar Main
```text
=== Relacion 1:N Predio -> Parcela ===
Predio{id=1, nombre='San Isidro', parcelas=2}
  - Parcela{codigo='P-01', nombre='Lote Norte', superficie=4.50 hectarea}
  - Parcela{codigo='P-02', nombre='Lote Sur', superficie=3.25 hectarea}

=== Pruebas de reglas ===
Rechazado (codigo repetido): Ya existe la parcela P-01 en el predio San Isidro
Rechazado (superficie): La superficie debe ser mayor que cero
Rechazado (lista de solo lectura): no se puede modificar desde fuera

Total de parcelas en San Isidro: 2
```

## Comparación PostgreSQL – Java
| Pregunta | Respuesta |
|---|---|
| ¿Cuál es la tabla padre? | `predio` |
| ¿Cuál es la tabla dependiente? | `parcela` |
| ¿Dónde está la FK en PostgreSQL? | En `parcela.predio_id` |
| ¿Cómo representaste el 1:N en Java? | Con una `List<Parcela>` privada dentro de `Predio` y el método `agregarParcela()` |
| ¿Qué regla protege tu método? | Que el código de parcela no se repita dentro del mismo predio |
| ¿Qué enum creaste y de qué CHECK/regla proviene? | Ninguno en este capítulo (ver Decisiones) |

## Decisiones
- **¿Por qué no hay enum?** Las tablas `predio` y `parcela` no tienen ningún CHECK con un conjunto cerrado de valores, y la guía pide no inventar enums. Los enums reales de AgroControl son `EstadoCampana` (`ck_campana_estado`: activa, finalizada) y `EstadoLabor` (`ck_labor_estado`: planificada, asignada, en_ejecucion, completada, cancelada). Aparecerán cuando se modelen campaña y labor.
- **¿Por qué la colección es privada?** Para que nadie agregue parcelas sin pasar por `agregarParcela()`, que es donde se valida el código repetido. `getParcelas()` devuelve una lista de solo lectura: si alguien intenta modificarla, Java lanza `UnsupportedOperationException`. `Main` lo comprueba.
- **¿Por qué `BigDecimal` en la superficie?** Porque en PostgreSQL es `NUMERIC(10,2)`, un valor exacto. `double` puede introducir errores de redondeo.
- **¿Por qué no hay setters?** Los atributos son `final` y se fijan en el constructor, donde se validan. Así un objeto no puede quedar en un estado inválido.
- **¿Qué NO implementamos todavía?** Spring Boot, JPA, `@Entity`, `JpaRepository`, controladores ni conexión a PostgreSQL desde Java. `PredioRepository` solo declara el contrato; no tiene implementación.
