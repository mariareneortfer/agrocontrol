# Convenciones de Base de Datos — AgroControl v0.1

Reglas de nombres y tipos que el equipo aplicará al pasar el diseño a PostgreSQL. Todavía no se ejecuta ningún SQL.

## Nombres
- Tablas: en español, en singular y en snake_case, sin prefijos. Ejemplos: `parcela`, `asignacion_labor`. Se escribe `campana` sin "ñ" para evitar problemas de codificación, igual que en la sección F de la ficha.
- Columnas: en español y en snake_case, con nombre completo y sin abreviaturas. Las fechas llevan su significado: `fecha_programada`, `fecha_cosecha`.
- PK: columna `<tabla>_id`; restricción `pk_<tabla>`. Ejemplo: `labor_id`, `pk_labor`.
- FK: columna `<tabla_referenciada>_id`; restricción `fk_<tabla>_<referencia>`. Ejemplo: `campana_id`, `fk_cosecha_campana`. Cuando la tabla apunta a `usuario`, la columna indica el papel de la persona: `operario_id`, `asignado_por_id`, `registrado_por_id`, `autor_id`, `reportado_por_id`.
- UNIQUE: restricción `uq_<tabla>_<columnas>`. Ejemplo: `uq_parcela_predio_codigo`.
- CHECK: restricción `ck_<tabla>_<regla>`. Ejemplo: `ck_labor_estado`, `ck_insumo_stock_no_negativo`.
- Índices: `idx_<tabla>_<columnas>`. Ejemplo: `idx_cosecha_campana`. Sólo se crean con una consulta que los justifique.

## Tipos candidatos
- Identificador técnico: BIGINT autogenerado (identity).
- Texto corto: VARCHAR(n), con n elegido según el dato. No se usa VARCHAR(255) por costumbre.
- Texto amplio: TEXT, para descripciones y observaciones que pueden crecer libremente.
- Importes/decimales exactos: NUMERIC(p,s). El proyecto no maneja dinero, pero sí cantidades y superficies que exigen exactitud. Nunca FLOAT ni DOUBLE.
- Fecha: DATE, cuando sólo importa el día calendario.
- Instante con hora: TIMESTAMPTZ, cuando debe conservarse el momento exacto de una operación.
- Booleanos: BOOLEAN, sólo para verdadero/falso reales.
- Estados: VARCHAR(20) con un CHECK que limita los valores. Los valores se escriben en minúsculas y snake_case: `en_ejecucion`. No se usan números (1, 2, 3).

## Estrategia de identificadores

| Pregunta | Respuesta del equipo |
|---|---|
| ¿Usaremos BIGINT, UUID u otra estrategia? | BIGINT autogenerado en todas las tablas. |
| ¿Por qué? | El sistema atiende a una sola unidad productiva con una sola base de datos; no hay que generar identificadores en varios lugares a la vez ni ocultarlos. BIGINT es simple, ordenado y fácil de depurar. |
| ¿Qué claves naturales requieren además UNIQUE? | usuario.correo, insumo.codigo, rol.nombre, predio.nombre, (parcela.predio_id, parcela.codigo), (cultivo.nombre, cultivo.variedad), (campana_parcela.campana_id, campana_parcela.parcela_id). |
| ¿Existe algún dato de negocio mutable que NO debe ser PK? | Sí: el correo del usuario, el código de la parcela y el código del insumo pueden corregirse o cambiar. Por eso son UNIQUE y no PK. |

## Cantidades y mediciones

| Dato | Ejemplo máximo razonable | Decimales | Tipo candidato | Justificación |
|---|---|---|---|---|
| Cantidad de insumo (stock, movimiento, consumo) | 999 999 999,999 kg o litros | 3 | NUMERIC(12,3) | Tres decimales permiten registrar gramos o mililitros cuando la unidad es kg o litro. Debe ser exacto para que el stock cuadre (RN-04). |
| Cantidad cosechada | 9 999 999 999,99 | 2 | NUMERIC(12,2) | La producción se pesa o se cuenta con, como mucho, dos decimales. |
| Superficie de parcela | 99 999 999,99 | 2 | NUMERIC(10,2) | Las hectáreas se registran con dos decimales. |

## Fechas: DATE o TIMESTAMPTZ

| Dato | ¿Importa la hora? | Tipo candidato |
|---|---|---|
| campana.fecha_inicio, fecha_fin_prevista, fecha_cierre | No: una campaña ocupa días completos. | DATE |
| labor.fecha_programada | No: se planifica por día. | DATE |
| cosecha.fecha_cosecha | No: se registra el día de cosecha. | DATE |
| labor.fecha_inicio_real, fecha_fin_real | Sí: el operario inicia y completa desde el móvil. | TIMESTAMPTZ |
| asignacion_labor.fecha_asignacion | Sí: es el instante de una operación. | TIMESTAMPTZ |
| movimiento_insumo.fecha_movimiento, consumo_labor.fecha_consumo | Sí: el orden de los movimientos afecta el stock. | TIMESTAMPTZ |
| bitacora_campo.fecha_registro, incidencia.fecha_incidencia | Sí: ordenan la bitácora de la parcela. | TIMESTAMPTZ |
| auditoria.fecha_evento | Sí: responde "cuándo" con exactitud. | TIMESTAMPTZ |

## Auditoría mínima
- `created_at` y `updated_at` (TIMESTAMPTZ) se agregan sólo a las tablas cuyos datos se editan con el tiempo: usuario, predio, parcela, cultivo, campana, labor e insumo.
- `created_at` lo llena la base al insertar la fila; `updated_at` lo actualiza el backend en cada modificación.
- No se agregan a las tablas de eventos (asignacion_labor, movimiento_insumo, consumo_labor, bitacora_campo, incidencia, cosecha, auditoria), porque cada una ya tiene su propia fecha y su usuario responsable.
- No se agregan `created_by` ni `updated_by`: quién hizo cada cambio crítico queda en la tabla `auditoria` (RF-18).

## Otras reglas
- Borrado: ninguna FK borra en cascada. Si una fila tiene registros que dependen de ella, la base rechaza eliminarla (RN-09).
- Unicidad con columnas opcionales: `uq_cultivo_nombre_variedad` debe tratar dos variedades vacías como iguales, para que no existan dos cultivos "Maíz" sin variedad. En PostgreSQL 15 o superior se declara con NULLS NOT DISTINCT.

## Regla de equipo
Toda excepción debe quedar justificada.

Excepción vigente: `created_at` y `updated_at` se escriben en inglés aunque el resto del modelo está en español, porque son los nombres que fija el manual del curso (sección 13) para los campos temporales.
