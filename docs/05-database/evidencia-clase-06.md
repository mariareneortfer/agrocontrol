# Evidencia Clase 06 — AgroControl

Primera implementación física del esquema en PostgreSQL.

## 1. Base de datos creada
- Nombre: `agrocontrol`
- Propietario: rol `agrocontrol_admin`
- Esquema del proyecto: `agrocontrol` (las tablas no están en `public`)
- Codificación: UTF8
- Servidor: PostgreSQL 17 local, puerto 5432.
- Herramienta: DataGrip.
- Diferencia entre ambos: PostgreSQL es el motor que guarda los datos y hace cumplir las restricciones; DataGrip es sólo el programa cliente con el que nos conectamos para enviarle SQL.

Pasos seguidos, según la guía "DataGrip + PostgreSQL: crear usuario administrador y base de datos":

1. Conectados como `postgres` a la base `postgres`, se creó el rol de laboratorio y la base:

```sql
CREATE ROLE agrocontrol_admin
WITH
    LOGIN
    PASSWORD '********'
    SUPERUSER
    CREATEDB
    CREATEROLE
    INHERIT;

CREATE DATABASE agrocontrol
    WITH
    OWNER = agrocontrol_admin
    ENCODING = 'UTF8'
    TEMPLATE = template0;
```

2. Se creó en DataGrip una conexión nueva llamada `AgroControl`, con usuario `agrocontrol_admin` y base `agrocontrol`.
3. Antes de ejecutar el script se comprobó la conexión:

```sql
SELECT current_user AS usuario_actual,
       current_database() AS base_actual;
-- Resultado: agrocontrol_admin | agrocontrol
```

La contraseña real no se guarda en el repositorio. `SUPERUSER` se usa sólo en este laboratorio local; el backend de la aplicación deberá usar un rol con privilegios mínimos.

## 1.1 Base de datos y esquema

Jerarquía, siguiendo la guía "Base de Datos vs. Esquema en PostgreSQL":

```text
Servidor PostgreSQL (localhost:5432)
└── agrocontrol              ← BASE DE DATOS
    ├── public               ← ESQUEMA por defecto (queda vacío)
    └── agrocontrol          ← ESQUEMA del proyecto
        ├── predio           ← TABLA
        ├── parcela
        ├── cultivo
        ├── campana
        ├── campana_parcela
        └── labor
```

- La base `agrocontrol` es el contenedor del sistema; la conexión de DataGrip apunta a ella.
- El esquema `agrocontrol` agrupa las tablas del proyecto. El nombre completo de una tabla es, por ejemplo, `agrocontrol.parcela`.
- La base y el esquema se llaman igual, pero son objetos distintos: la base se crea con `CREATE DATABASE` y el esquema con `CREATE SCHEMA`.
- `V1__init_core.sql` empieza con `CREATE SCHEMA IF NOT EXISTS agrocontrol;` y `SET search_path TO agrocontrol, public;`. Así, las tablas sin esquema escrito se crean dentro de `agrocontrol`.
- Para no tener que escribir el esquema en cada consulta, se configuró el `search_path` del rol en esta base:

```sql
ALTER ROLE agrocontrol_admin IN DATABASE agrocontrol
    SET search_path TO agrocontrol, public;
```

Nota: en la primera ejecución las tablas se habían creado en `public`. Se movieron al esquema `agrocontrol` con `ALTER TABLE ... SET SCHEMA agrocontrol`, sin perder datos ni restricciones, y el script V1 se corrigió para crearlas directamente allí.

Comprobación:

```sql
SELECT table_schema, table_name
FROM information_schema.tables
WHERE table_schema = 'agrocontrol'
  AND table_type = 'BASE TABLE'
ORDER BY table_name;
-- Resultado: 6 tablas, todas con table_schema = agrocontrol
```

## 2. Tablas incluidas en V1 y por qué

La guía pide entre 4 y 6 tablas núcleo. Se eligieron las 6 que permiten representar la primera mitad del flujo crítico: "Jefe crea campaña sobre parcela → planifica labores".

| Pregunta | Respuesta del equipo |
|---|---|
| ¿Cuál es la entidad raíz o catálogo principal? | predio |
| ¿Qué tabla depende directamente de ella? | parcela |
| ¿Qué segunda relación 1:N puedes implementar? | cultivo → campana, y campana_parcela → labor |
| ¿Qué entidad de actor o recurso necesita persistencia? | parcela (el recurso sobre el que se trabaja). El actor usuario entra en V2. |
| ¿Qué tabla transaccional conviene dejar para después? | consumo_labor, movimiento_insumo y cosecha |

| Tabla | Por qué está en V1 |
|---|---|
| predio | Raíz del modelo; sin predio no hay parcelas. |
| parcela | Es el lote agrícola sobre el que la ficha pide trazabilidad. |
| cultivo | Catálogo que toda campaña necesita. |
| campana | Centro del dominio: agrupa parcelas, labores y cosechas. |
| campana_parcela | Tabla puente: una campaña abarca varias parcelas. |
| labor | Primera tabla del flujo operativo; depende de todo lo anterior. |

## 3. Orden de creación
1. predio (raíz)
2. parcela (depende de predio)
3. cultivo (raíz)
4. campana (depende de cultivo)
5. campana_parcela (depende de campana y parcela)
6. labor (depende de campana_parcela)

Script: `agrocontrol-backend/src/main/resources/db/migration/V1__init_core.sql`

Nota: en la Clase 06 el script estaba en `backend/src/main/resources/db/migration/`. En el Capítulo 03 se movió dentro del proyecto Spring Boot `agrocontrol-backend`, que es el backend real del sistema.

## 4. PK y FK

| Tabla | PK | FK |
|---|---|---|
| predio | pk_predio (predio_id) | — |
| parcela | pk_parcela (parcela_id) | fk_parcela_predio: predio_id -> predio |
| cultivo | pk_cultivo (cultivo_id) | — |
| campana | pk_campana (campana_id) | fk_campana_cultivo: cultivo_id -> cultivo |
| campana_parcela | pk_campana_parcela (campana_parcela_id) | fk_campana_parcela_campana: campana_id -> campana; fk_campana_parcela_parcela: parcela_id -> parcela |
| labor | pk_labor (labor_id) | fk_labor_campana_parcela: campana_parcela_id -> campana_parcela |

Total: 6 PK y 5 FK. Todas las PK son BIGINT autogenerado (identity).

## 5. UNIQUE y CHECK implementados

| Restricción | Tipo | Qué protege | Regla |
|---|---|---|---|
| uq_predio_nombre | UNIQUE global | No hay dos predios con el mismo nombre. | RF-01 |
| uq_parcela_predio_codigo | UNIQUE contextual | El código de parcela no se repite dentro del mismo predio. | RF-01 |
| uq_cultivo_nombre_variedad | UNIQUE compuesto | No se repite un cultivo con la misma variedad, tampoco cuando la variedad está vacía. | RF-02 |
| uq_campana_parcela_campana_parcela | UNIQUE compuesto | Una parcela no figura dos veces en la misma campaña. | RN-01 |
| ck_parcela_superficie_positiva | CHECK | La superficie es mayor que cero. | RN-07 |
| ck_campana_estado | CHECK | El estado sólo puede ser activa o finalizada. | RN-06, RN-09 |
| ck_campana_fechas | CHECK | La campaña no termina antes de empezar. | RN-01 |
| ck_campana_cierre | CHECK | Una campaña finalizada tiene fecha de cierre. | RN-09 |
| ck_labor_estado | CHECK | El estado sólo puede ser uno de los cinco de la ficha. | RN-03 |
| ck_labor_fechas_reales | CHECK | La labor no se completa antes de iniciarse. | RF-07 |

Valores por defecto: `campana.estado = 'activa'` y `labor.estado = 'planificada'`, porque son los estados con los que nace cada registro.

## 6. Dataset mínimo insertado

Comandos en `docs/05-database/clase-06-datos-y-pruebas.sql`, parte A.

- 2 predios: San Isidro y La Esperanza.
- 3 parcelas: P-01 y P-02 en San Isidro; P-01 en La Esperanza.
- 1 cultivo: Maíz, variedad Amarillo duro.
- 1 campaña: Maíz verano 2026, del 1/10/2026 al 28/2/2027, que nace en estado activa.
- 2 filas en campana_parcela: la campaña abarca P-01 y P-02 de San Isidro.
- 1 labor: riego programado para el 12/10/2026 en P-01, que nace en estado planificada.

Caso positivo de unicidad contextual: el código P-01 se insertó en dos predios distintos y PostgreSQL lo aceptó.

## 7. Errores de integridad provocados

Comandos en `docs/05-database/clase-06-datos-y-pruebas.sql`, parte B. El equipo ejecutó en DataGrip las pruebas B1 a B4, una por vez, y PostgreSQL rechazó todas. Las pruebas B5 a B7 quedan en el archivo como pruebas adicionales.

| # | Qué se intentó | Restricción que se activó | Por qué se activó |
|---|---|---|---|
| B1 | Insertar una parcela sin nombre. | NOT NULL en parcela.nombre | Una parcela no es válida sin nombre; se muestra en listados y bitácora. |
| B2 | Insertar otra parcela P-01 en San Isidro. | uq_parcela_predio_codigo | El código ya existe en ese predio. |
| B3 | Unir la campaña con la parcela 9999. | fk_campana_parcela_parcela | No existe ninguna parcela con ese identificador. |
| B4 | Insertar una labor con estado "terminada". | ck_labor_estado | "terminada" no es uno de los cinco estados de RN-03. |
| B5 | Insertar una campaña del 1/6/2027 al 1/1/2027. | ck_campana_fechas | La fecha de fin es anterior a la de inicio. |
| B6 | Insertar una parcela con superficie 0. | ck_parcela_superficie_positiva | La superficie debe ser mayor que cero. |
| B7 | Agregar P-02 por segunda vez a la campaña. | uq_campana_parcela_campana_parcela | Esa parcela ya participa en la campaña. |

Mensajes obtenidos en DataGrip:

```text
B1  [23502] ERROR: el valor nulo en la columna «nombre» de la relación «parcela» viola la restricción de no nulo
    Detail: La fila que falla contiene (4, 1, P-03, null, 1.00, hectarea, null, ...).
B2  [23505] ERROR: llave duplicada viola restricción de unicidad «uq_parcela_predio_codigo»
    Detail: Ya existe la llave (predio_id, codigo)=(1, P-01).
B3  Violación de la llave foránea «fk_campana_parcela_parcela»: no existe una parcela con identificador 9999.
B4  Violación de la restricción check «ck_labor_estado»: "terminada" no es un estado permitido.
```

Capturas de pantalla: ver las imágenes guardadas por el equipo de cada error.

## 8. Por qué el script es reproducible
- Todo el esquema está en un solo archivo versionado, `V1__init_core.sql`; no se creó ninguna tabla desde la interfaz de DataGrip.
- Las tablas están en orden de dependencias, así que el archivo se ejecuta completo de una sola vez sobre una base vacía.
- El script de tablas no contiene usuarios ni contraseñas.

Para reconstruir desde cero, conectados a la base `postgres`:

```sql
DROP DATABASE agrocontrol;
CREATE DATABASE agrocontrol WITH OWNER = postgres ENCODING = 'UTF8';
```

Luego se ejecuta `V1__init_core.sql` conectados a `agrocontrol`; el propio script crea el esquema `agrocontrol`.

## 9. Tablas que no se crearon todavía y por qué
- rol, usuario y asignacion_labor: entran en V2, cuando se trabaje la asignación de operarios.
- insumo, consumo_labor y movimiento_insumo: entran en V2; son la parte transaccional del almacén.
- cosecha y bitacora_campo: entran en V2; cierran el flujo crítico.
- incidencia y auditoria: entran en V3; no participan en el flujo crítico.

## 10. Decisiones pendientes para la siguiente clase
- Si la cosecha debe indicar de cuál de las parcelas de la campaña proviene.
- Si la salida del almacenero y el consumo del operario son un solo registro o dos.
- Si tipo de labor y unidades de medida serán texto controlado o catálogos con tabla propia.
- `updated_at` hoy sólo recibe valor al insertar; falta definir quién lo actualiza en cada modificación (backend o trigger).
- Reglas que siguen en backend y aún no están protegidas: no superposición de campañas en una parcela (RN-01), transiciones de estado de la labor (RN-03) y que toda campaña tenga al menos una parcela.
