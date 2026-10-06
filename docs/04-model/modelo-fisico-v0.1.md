# Modelo físico v0.1 — AgroControl

Diseño físico preparado para PostgreSQL. Deriva de `der-logico-v0.1.md` y `diccionario-datos-v0.1.md` (Clase 04) y de la ficha oficial del proyecto 16. Todavía no se ejecuta CREATE TABLE; este documento es la especificación con la que se escribirá el SQL en la Clase 06.

- Cambio respecto al DER inicial: una campaña abarca varias parcelas; se agrega la tabla puente `campana_parcela`.
- Convenciones de nombres y tipos: `convenciones-bd-v0.1.md`.
- Orden de creación y alcance de la primera migración: `plan-migracion-v1.md`.
- Columna "NULL": NO significa obligatorio (NOT NULL); SÍ significa que puede faltar.
- Todas las PK son BIGINT autogenerado. Ninguna FK borra en cascada.

## rol
- Propósito: una fila representa una responsabilidad dentro del sistema.

| Columna | Tipo candidato | NULL | Rol/Restricción | Fuente |
|---|---|---|---|---|
| rol_id | BIGINT | NO | PK | Diseño |
| nombre | VARCHAR(30) | NO | UQ uq_rol_nombre | Sección C |
| descripcion | TEXT | SÍ | — | Sección C |

### Decisiones
- VARCHAR(30) alcanza para el nombre más largo ("Jefe de campo") con margen.
- No lleva created_at/updated_at: es un catálogo fijo de cinco valores.

### Regla que NO se resuelve sólo con constraint simple
- Ninguna.

## usuario
- Propósito: una fila representa a una persona con cuenta en el sistema.

| Columna | Tipo candidato | NULL | Rol/Restricción | Fuente |
|---|---|---|---|---|
| usuario_id | BIGINT | NO | PK | Diseño |
| rol_id | BIGINT | NO | FK -> rol.rol_id | Sección C, RN-05 |
| nombre_completo | VARCHAR(120) | NO | — | RF-05, RF-18 |
| correo | VARCHAR(254) | NO | UQ uq_usuario_correo | RF-18 |
| activo | BOOLEAN | NO | Valor inicial: verdadero | RN-09 |
| created_at | TIMESTAMPTZ | NO | Auditoría mínima | Diseño |
| updated_at | TIMESTAMPTZ | NO | Auditoría mínima | Diseño |

### Decisiones
- VARCHAR(254) es la longitud máxima de una dirección de correo.
- `activo` es BOOLEAN porque sólo tiene dos valores reales. Un usuario con historial se desactiva; no se elimina.
- El correo es UNIQUE pero no PK, porque puede cambiar.

### Regla que NO se resuelve sólo con constraint simple
- Los permisos de cada rol se controlan en el backend.

## predio
- Propósito: una fila representa una propiedad agrícola.

| Columna | Tipo candidato | NULL | Rol/Restricción | Fuente |
|---|---|---|---|---|
| predio_id | BIGINT | NO | PK | Diseño |
| nombre | VARCHAR(100) | NO | UQ uq_predio_nombre | RF-01 |
| ubicacion | VARCHAR(200) | SÍ | — | RF-01 |
| descripcion | TEXT | SÍ | — | RF-01 |
| created_at | TIMESTAMPTZ | NO | Auditoría mínima | Diseño |
| updated_at | TIMESTAMPTZ | NO | Auditoría mínima | Diseño |

### Decisiones
- `ubicacion` es una referencia corta en texto, no coordenadas; por eso VARCHAR(200) y no TEXT.

### Regla que NO se resuelve sólo con constraint simple
- Ninguna.

## parcela
- Propósito: una fila representa un lote agrícola dentro de un predio.

| Columna | Tipo candidato | NULL | Rol/Restricción | Fuente |
|---|---|---|---|---|
| parcela_id | BIGINT | NO | PK | Diseño |
| predio_id | BIGINT | NO | FK -> predio.predio_id; UQ uq_parcela_predio_codigo | RF-01 |
| codigo | VARCHAR(20) | NO | UQ uq_parcela_predio_codigo | RF-01 |
| nombre | VARCHAR(100) | NO | — | RF-01, RF-15 |
| superficie | NUMERIC(10,2) | NO | CK ck_parcela_superficie_positiva: superficie > 0 | RN-07 |
| unidad_superficie | VARCHAR(20) | NO | — | RN-07 |
| ubicacion_referencial | VARCHAR(200) | SÍ | — | Sección H |
| created_at | TIMESTAMPTZ | NO | Auditoría mínima | Diseño |
| updated_at | TIMESTAMPTZ | NO | Auditoría mínima | Diseño |

### Decisiones
- La unicidad es contextual: (predio_id, codigo). El código "P-01" puede existir en dos predios distintos.
- `superficie` es NUMERIC y no FLOAT, porque es una medición que debe conservarse exacta.

### Regla que NO se resuelve sólo con constraint simple
- Ninguna propia; la ocupación de la parcela se controla desde campana (RN-01).

## cultivo
- Propósito: una fila representa un cultivo del catálogo.

| Columna | Tipo candidato | NULL | Rol/Restricción | Fuente |
|---|---|---|---|---|
| cultivo_id | BIGINT | NO | PK | Diseño |
| nombre | VARCHAR(80) | NO | UQ uq_cultivo_nombre_variedad | RF-02 |
| variedad | VARCHAR(80) | SÍ | UQ uq_cultivo_nombre_variedad | RF-02 |
| descripcion | TEXT | SÍ | — | RF-02 |
| created_at | TIMESTAMPTZ | NO | Auditoría mínima | Diseño |
| updated_at | TIMESTAMPTZ | NO | Auditoría mínima | Diseño |

### Decisiones
- Se cierra el pendiente de la Clase 04: la unicidad (nombre, variedad) tratará dos variedades vacías como iguales (NULLS NOT DISTINCT), para que no existan dos "Maíz" sin variedad.

### Regla que NO se resuelve sólo con constraint simple
- Ninguna.

## campana
- Propósito: una fila representa un ciclo productivo de un cultivo sobre una o varias parcelas.

| Columna | Tipo candidato | NULL | Rol/Restricción | Fuente |
|---|---|---|---|---|
| campana_id | BIGINT | NO | PK | Diseño |
| cultivo_id | BIGINT | NO | FK -> cultivo.cultivo_id | RF-03 |
| nombre | VARCHAR(100) | NO | — | RF-03 |
| fecha_inicio | DATE | NO | CK ck_campana_fechas | RN-01 |
| fecha_fin_prevista | DATE | NO | CK ck_campana_fechas: fecha_inicio <= fecha_fin_prevista | RN-01 |
| fecha_cierre | DATE | SÍ | CK ck_campana_cierre | RN-06, RN-09 |
| estado | VARCHAR(20) | NO | CK ck_campana_estado: activa, finalizada. Valor inicial: activa | RN-06, RN-09 |
| created_at | TIMESTAMPTZ | NO | Auditoría mínima | Diseño |
| updated_at | TIMESTAMPTZ | NO | Auditoría mínima | Diseño |

### Decisiones
- Las tres fechas son DATE: una campaña ocupa días completos; la hora no aporta.
- `fecha_cierre` es NULL mientras la campaña está activa. `ck_campana_cierre` exige que tenga valor cuando el estado es finalizada.
- `fecha_fin_prevista` es obligatoria porque sin ella no se puede comprobar la superposición.

### Regla que NO se resuelve sólo con constraint simple
- RN-01: no superposición de períodos entre campañas que comparten una parcela. Compara varias filas; va en backend.
- Toda campaña debe tener al menos una fila en campana_parcela.
- RN-09: una campaña finalizada no se elimina.

## campana_parcela
- Propósito: una fila representa que una parcela participa en una campaña.

| Columna | Tipo candidato | NULL | Rol/Restricción | Fuente |
|---|---|---|---|---|
| campana_parcela_id | BIGINT | NO | PK | Diseño |
| campana_id | BIGINT | NO | FK -> campana.campana_id; UQ uq_campana_parcela_campana_parcela | RN-01, flujo J |
| parcela_id | BIGINT | NO | FK -> parcela.parcela_id; UQ uq_campana_parcela_campana_parcela | RN-01 |

### Decisiones
- Es la tabla puente de la relación N:M entre campaña y parcela.
- Lleva PK técnica porque `labor` apunta a ella con una sola columna. La UQ (campana_id, parcela_id) impide repetir una parcela dentro de la misma campaña.
- No lleva created_at/updated_at: la fila no se edita, sólo se agrega o se quita.

### Regla que NO se resuelve sólo con constraint simple
- RN-01: una parcela no puede participar en dos campañas con períodos superpuestos. Compara varias filas; va en backend.

## labor
- Propósito: una fila representa un trabajo de campo planificado en una parcela de una campaña.

| Columna | Tipo candidato | NULL | Rol/Restricción | Fuente |
|---|---|---|---|---|
| labor_id | BIGINT | NO | PK | Diseño |
| campana_parcela_id | BIGINT | NO | FK -> campana_parcela.campana_parcela_id | RN-02 |
| tipo_labor | VARCHAR(40) | NO | — | RF-04 |
| descripcion | TEXT | SÍ | — | RF-04 |
| fecha_programada | DATE | NO | — | RF-04, RF-06 |
| fecha_inicio_real | TIMESTAMPTZ | SÍ | CK ck_labor_fechas_reales | RF-07 |
| fecha_fin_real | TIMESTAMPTZ | SÍ | CK ck_labor_fechas_reales: fecha_inicio_real <= fecha_fin_real | RF-07 |
| estado | VARCHAR(20) | NO | CK ck_labor_estado: planificada, asignada, en_ejecucion, completada, cancelada. Valor inicial: planificada | RN-03 |
| created_at | TIMESTAMPTZ | NO | Auditoría mínima | Diseño |
| updated_at | TIMESTAMPTZ | NO | Auditoría mínima | Diseño |

### Decisiones
- `fecha_programada` es DATE porque se planifica por día. Las fechas reales son TIMESTAMPTZ porque registran el instante en que el operario inicia y completa desde el móvil.
- Las fechas reales son NULL hasta que la labor se inicia o se completa.
- No hay `campana_id` y `parcela_id` por separado: `campana_parcela_id` garantiza que la parcela de la labor participa en su campaña (RN-02).

### Regla que NO se resuelve sólo con constraint simple
- RN-03: transiciones permitidas entre estados (planificada -> asignada -> en_ejecucion -> completada; cancelación antes de completarse).
- Una labor en estado asignada o posterior debe tener al menos una fila en asignacion_labor.

## asignacion_labor
- Propósito: una fila representa que un operario fue designado para una labor.

| Columna | Tipo candidato | NULL | Rol/Restricción | Fuente |
|---|---|---|---|---|
| asignacion_labor_id | BIGINT | NO | PK | Diseño |
| labor_id | BIGINT | NO | FK -> labor.labor_id; UQ uq_asignacion_labor_labor_operario | RF-05 |
| operario_id | BIGINT | NO | FK -> usuario.usuario_id; UQ uq_asignacion_labor_labor_operario | RN-05 |
| asignado_por_id | BIGINT | NO | FK -> usuario.usuario_id | RF-05, RF-18 |
| fecha_asignacion | TIMESTAMPTZ | NO | — | RF-18 |

### Decisiones
- Es la tabla puente de la relación N:M entre labor y operario.
- Lleva PK técnica y además UQ (labor_id, operario_id), para que un operario no quede asignado dos veces a la misma labor.

### Regla que NO se resuelve sólo con constraint simple
- RN-05: `operario_id` debe ser un usuario con rol Operario. Depende de otra tabla; va en backend.

## insumo
- Propósito: una fila representa un producto de almacén.

| Columna | Tipo candidato | NULL | Rol/Restricción | Fuente |
|---|---|---|---|---|
| insumo_id | BIGINT | NO | PK | Diseño |
| codigo | VARCHAR(20) | NO | UQ uq_insumo_codigo | RF-09 |
| nombre | VARCHAR(100) | NO | — | RF-09 |
| categoria | VARCHAR(40) | SÍ | — | RF-09 |
| unidad_medida | VARCHAR(20) | NO | — | RN-07 |
| stock_disponible | NUMERIC(12,3) | NO | CK ck_insumo_stock_no_negativo: stock_disponible >= 0. Valor inicial: 0 | RN-04, RF-12 |
| created_at | TIMESTAMPTZ | NO | Auditoría mínima | Diseño |
| updated_at | TIMESTAMPTZ | NO | Auditoría mínima | Diseño |

### Decisiones
- `stock_disponible` es NUMERIC(12,3): tres decimales permiten gramos o mililitros, y debe ser exacto para que el stock cuadre.
- El código es UNIQUE global pero no PK, porque el almacén puede recodificar.

### Regla que NO se resuelve sólo con constraint simple
- El stock sólo cambia cuando se registra un movimiento.

## consumo_labor
- Propósito: una fila representa una cantidad de un insumo usada en una labor.

| Columna | Tipo candidato | NULL | Rol/Restricción | Fuente |
|---|---|---|---|---|
| consumo_labor_id | BIGINT | NO | PK | Diseño |
| labor_id | BIGINT | NO | FK -> labor.labor_id | RF-11 |
| insumo_id | BIGINT | NO | FK -> insumo.insumo_id | RF-11 |
| registrado_por_id | BIGINT | NO | FK -> usuario.usuario_id | RN-05, RF-18 |
| cantidad | NUMERIC(12,3) | NO | CK ck_consumo_labor_cantidad_positiva: cantidad > 0 | RN-04, RN-07 |
| fecha_consumo | TIMESTAMPTZ | NO | — | RF-15 |

### Decisiones
- Es la tabla puente de la relación N:M entre labor e insumo.
- No hay UQ (labor_id, insumo_id): una labor puede registrar el mismo insumo más de una vez.
- No guarda unidad: la cantidad se expresa en `insumo.unidad_medida`.

### Regla que NO se resuelve sólo con constraint simple
- RN-04 / RF-12: la cantidad no puede superar `insumo.stock_disponible`. Compara dos tablas y debe hacerse en la misma transacción que descuenta el stock.
- RN-05: sólo un operario asignado a la labor puede registrar el consumo.

## movimiento_insumo
- Propósito: una fila representa una entrada o una salida de un insumo del almacén.

| Columna | Tipo candidato | NULL | Rol/Restricción | Fuente |
|---|---|---|---|---|
| movimiento_insumo_id | BIGINT | NO | PK | Diseño |
| insumo_id | BIGINT | NO | FK -> insumo.insumo_id | RF-10 |
| registrado_por_id | BIGINT | NO | FK -> usuario.usuario_id | RF-10, RF-18 |
| consumo_labor_id | BIGINT | SÍ | FK -> consumo_labor.consumo_labor_id; UQ uq_movimiento_insumo_consumo; CK ck_movimiento_insumo_consumo_salida | RF-11 |
| tipo | VARCHAR(10) | NO | CK ck_movimiento_insumo_tipo: entrada, salida | RF-10 |
| cantidad | NUMERIC(12,3) | NO | CK ck_movimiento_insumo_cantidad_positiva: cantidad > 0 | RN-07 |
| fecha_movimiento | TIMESTAMPTZ | NO | — | RF-10 |
| motivo | VARCHAR(200) | SÍ | — | RF-10 |

### Decisiones
- `consumo_labor_id` es NULL en las entradas y en las salidas que no corresponden a una labor.
- La UQ sobre `consumo_labor_id` hace que un consumo tenga como máximo una salida. Varias filas con el valor vacío sí están permitidas.
- La cantidad siempre es positiva; `tipo` indica si suma o resta.

### Regla que NO se resuelve sólo con constraint simple
- Al registrar un movimiento, el backend actualiza `insumo.stock_disponible` en la misma transacción.

## cosecha
- Propósito: una fila representa una entrega de producción de una campaña.

| Columna | Tipo candidato | NULL | Rol/Restricción | Fuente |
|---|---|---|---|---|
| cosecha_id | BIGINT | NO | PK | Diseño |
| campana_id | BIGINT | NO | FK -> campana.campana_id | RN-06 |
| registrado_por_id | BIGINT | NO | FK -> usuario.usuario_id | RF-14, RF-18 |
| fecha_cosecha | DATE | NO | — | RF-14 |
| cantidad | NUMERIC(12,2) | NO | CK ck_cosecha_cantidad_positiva: cantidad > 0 | RN-07 |
| unidad_medida | VARCHAR(20) | NO | — | RN-07 |
| observacion | TEXT | SÍ | — | RF-14 |

### Decisiones
- `fecha_cosecha` es DATE: importa el día en que se cosechó, no la hora.
- Guarda su propia unidad porque no tiene un insumo del cual tomarla.

### Regla que NO se resuelve sólo con constraint simple
- RN-06: la campaña debe estar activa al registrar la cosecha. Depende del estado de otra fila.

## bitacora_campo
- Propósito: una fila representa una observación de campo anotada en la bitácora de una parcela.

| Columna | Tipo candidato | NULL | Rol/Restricción | Fuente |
|---|---|---|---|---|
| bitacora_campo_id | BIGINT | NO | PK | Diseño |
| parcela_id | BIGINT | NO | FK -> parcela.parcela_id | RF-15 |
| campana_id | BIGINT | SÍ | FK -> campana.campana_id | RF-08 |
| labor_id | BIGINT | SÍ | FK -> labor.labor_id | RF-08 |
| autor_id | BIGINT | NO | FK -> usuario.usuario_id | RF-08, RF-18 |
| fecha_registro | TIMESTAMPTZ | NO | — | RF-15 |
| descripcion | TEXT | NO | — | RF-08 |

### Decisiones
- `campana_id` y `labor_id` son NULL cuando la observación no ocurre dentro de una campaña o de una labor.
- `descripcion` es TEXT porque una observación puede ser larga.

### Regla que NO se resuelve sólo con constraint simple
- Si se indica labor, debe ser de la campaña y la parcela indicadas; si se indica campaña, la parcela debe participar en ella.

## incidencia
- Propósito: una fila representa un hecho excepcional ocurrido en campo.

| Columna | Tipo candidato | NULL | Rol/Restricción | Fuente |
|---|---|---|---|---|
| incidencia_id | BIGINT | NO | PK | Diseño |
| parcela_id | BIGINT | NO | FK -> parcela.parcela_id | RN-08 |
| campana_id | BIGINT | SÍ | FK -> campana.campana_id | RN-08 |
| labor_id | BIGINT | SÍ | FK -> labor.labor_id | RN-08 |
| reportado_por_id | BIGINT | NO | FK -> usuario.usuario_id | RF-13, RF-18 |
| fecha_incidencia | TIMESTAMPTZ | NO | — | RF-13 |
| descripcion | TEXT | NO | — | RF-13 |
| clasificacion | VARCHAR(40) | SÍ | — | RN-10, sección M |

### Decisiones
- `clasificacion` es NULL hasta que alguien, o la IA, la etiqueta. Sólo clasifica; no recomienda tratamientos.
- Queda fuera de la migración V1.

### Regla que NO se resuelve sólo con constraint simple
- RN-08: coherencia entre parcela, campaña y labor indicadas.

## auditoria
- Propósito: una fila representa un cambio crítico realizado por un usuario.

| Columna | Tipo candidato | NULL | Rol/Restricción | Fuente |
|---|---|---|---|---|
| auditoria_id | BIGINT | NO | PK | Diseño |
| usuario_id | BIGINT | NO | FK -> usuario.usuario_id | RF-18 |
| fecha_evento | TIMESTAMPTZ | NO | — | RF-18 |
| accion | VARCHAR(20) | NO | CK ck_auditoria_accion: crear, modificar, cambiar_estado | RF-18 |
| tabla_afectada | VARCHAR(40) | NO | — | RF-18 |
| registro_id | BIGINT | NO | Sin FK | RF-18 |
| detalle | TEXT | SÍ | — | RF-18 |

### Decisiones
- `registro_id` no es FK porque apunta a filas de tablas distintas según `tabla_afectada`.
- Queda fuera de la migración V1.

### Regla que NO se resuelve sólo con constraint simple
- Las filas de auditoría no se modifican ni se eliminan.

## Nulabilidad: casos que conviene poder defender

| Tabla | Atributo | ¿Puede faltar en un estado válido? | Ejemplo de estado válido | Decisión | Regla que lo justifica |
|---|---|---|---|---|---|
| campana | fecha_cierre | Sí | Campaña activa que aún no terminó. | NULL | RN-06, RN-09 |
| campana | fecha_fin_prevista | No | — | NOT NULL | RN-01: sin ella no se valida la superposición. |
| labor | fecha_inicio_real | Sí | Labor planificada o asignada que nadie inició. | NULL | RN-03, RF-07 |
| labor | campana_parcela_id | No | — | NOT NULL | RN-02 |
| movimiento_insumo | consumo_labor_id | Sí | Entrada de insumo por compra. | NULL | RF-10 |
| incidencia | labor_id | Sí | Daño por granizo detectado sin una labor en curso. | NULL | RN-08 |
| incidencia | parcela_id | No | — | NOT NULL | RN-08 |
| cosecha | unidad_medida | No | — | NOT NULL | RN-07 |

## Estados controlados

| Entidad | Atributo | Valores permitidos | ¿Transiciones? | Fuente | Decisión física candidata |
|---|---|---|---|---|---|
| labor | estado | planificada, asignada, en_ejecucion, completada, cancelada | Sí, en backend | RN-03 | VARCHAR(20) NOT NULL + CHECK |
| campana | estado | activa, finalizada | Sí: activa -> finalizada | RN-06, RN-09 | VARCHAR(20) NOT NULL + CHECK |
| movimiento_insumo | tipo | entrada, salida | No | RF-10 | VARCHAR(10) NOT NULL + CHECK |
| auditoria | accion | crear, modificar, cambiar_estado | No | RF-18 | VARCHAR(20) NOT NULL + CHECK |

## Prueba de coherencia contra el flujo crítico

| Paso del flujo (sección J) | Tabla que se consulta o modifica | ¿Existen los datos necesarios? |
|---|---|---|
| Jefe crea campaña sobre parcela | Consulta parcela y cultivo; inserta en campana y en campana_parcela | Sí: cultivo_id, fechas y estado; una fila de campana_parcela por cada parcela. |
| Planifica labores | Inserta en labor | Sí: campana_parcela_id, tipo_labor, fecha_programada, estado planificada. |
| Asigna operario | Inserta en asignacion_labor; actualiza labor.estado | Sí: labor_id, operario_id, asignado_por_id, fecha_asignacion. |
| Almacén entrega insumo | Inserta en consumo_labor y en movimiento_insumo; actualiza insumo.stock_disponible | Sí: insumo_id, cantidad, tipo salida, registrado_por_id. |
| Operario ejecuta y reporta desde móvil | Actualiza labor.estado, fecha_inicio_real y fecha_fin_real | Sí. |
| Se actualiza bitácora | Inserta en bitacora_campo | Sí: parcela_id, campana_id, labor_id, autor_id, descripcion. |
| Se registra cosecha | Inserta en cosecha | Sí: campana_id, cantidad, unidad_medida, fecha_cosecha. |
| Supervisor revisa indicadores | Consulta campana, labor, consumo_labor y cosecha | Sí; no requiere tablas nuevas. |

Brechas registradas:
- La cosecha se registra contra la campaña (RN-06). Con varias parcelas por campaña, falta confirmar si además debe indicar de cuál parcela proviene.
- Si el docente define que la entrega del almacén y el consumo del operario son registros distintos, el paso "almacén entrega insumo" necesitará que movimiento_insumo apunte directamente a la labor.
- `incidencia` y `auditoria` no participan en el flujo crítico; son necesarias porque la ficha las exige (RF-13, RF-18, sección F) y entran en la migración V2.
