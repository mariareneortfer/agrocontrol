# Plan de Migración V1 — AgroControl

Plan de la primera migración de base de datos.

Actualización de la Clase 06: la guía pide implementar primero entre 4 y 6 tablas núcleo. Por eso V1 se redujo a 6 tablas y el resto del flujo crítico pasa a V2. El script está en `agrocontrol-backend/src/main/resources/db/migration/V1__init_core.sql`.

## Objetivo
Crear el núcleo que permite representar el inicio del flujo crítico de la ficha: el jefe crea una campaña sobre una o varias parcelas y planifica labores.

## Tablas incluidas y orden

Una tabla que tiene una FK se crea después de la tabla a la que apunta.

1. predio (sin dependencias / raíz)
2. parcela (depende de predio)
3. cultivo (sin dependencias / raíz)
4. campana (depende de cultivo)
5. campana_parcela (depende de campana y parcela)
6. labor (depende de campana_parcela)

Puntos del orden que conviene poder explicar:
- `campana_parcela` va después de `campana` y de `parcela`, porque apunta a las dos, y antes de `labor`, porque la labor apunta a ella.
- `predio` y `cultivo` no dependen de nadie y podrían crearse en cualquier orden entre sí.

## Restricciones previstas

- PK: una por tabla, columna `<tabla>_id` de tipo BIGINT autogenerado (6 en total).
- FK:
  - parcela.predio_id -> predio
  - campana.cultivo_id -> cultivo
  - campana_parcela.campana_id -> campana; campana_parcela.parcela_id -> parcela
  - labor.campana_parcela_id -> campana_parcela
  - Ninguna borra en cascada (RN-09).
- NOT NULL: todas las columnas marcadas "NO" en `modelo-fisico-v0.1.md`. Quedan opcionales sólo: descripciones, predio.ubicacion, parcela.ubicacion_referencial, cultivo.variedad, campana.fecha_cierre, labor.fecha_inicio_real y labor.fecha_fin_real.
- UNIQUE:
  - uq_predio_nombre
  - uq_parcela_predio_codigo (predio_id, codigo)
  - uq_cultivo_nombre_variedad (nombre, variedad)
  - uq_campana_parcela_campana_parcela (campana_id, parcela_id)
- CHECK:
  - ck_parcela_superficie_positiva: superficie > 0
  - ck_campana_estado: estado en (activa, finalizada)
  - ck_campana_fechas: fecha_inicio <= fecha_fin_prevista
  - ck_campana_cierre: si estado = finalizada, fecha_cierre no es nula
  - ck_labor_estado: estado en (planificada, asignada, en_ejecucion, completada, cancelada)
  - ck_labor_fechas_reales: fecha_inicio_real <= fecha_fin_real

## Reglas que requerirán lógica posterior

No se resuelven con un constraint simple porque dependen de otras filas o de estados previos. Se implementarán en el backend.

- RN-01: dos campañas que comparten una parcela no pueden tener períodos superpuestos.
- Toda campaña debe tener al menos una parcela.
- RN-03: transiciones permitidas entre estados de la labor.
- RN-09: no se eliminan campañas finalizadas ni sus registros.

## Fuera de V1

V2, resto del flujo crítico, en este orden:
1. rol (raíz)
2. usuario (depende de rol)
3. asignacion_labor (depende de labor y usuario)
4. insumo (raíz)
5. consumo_labor (depende de labor, insumo y usuario)
6. movimiento_insumo (depende de insumo, usuario y consumo_labor)
7. cosecha (depende de campana y usuario)
8. bitacora_campo (depende de parcela, campana, labor y usuario)

Reglas que llegarán con V2: stock no negativo y consumo no mayor que el stock (RN-04), sólo el operario asignado reporta (RN-05), cosecha sólo contra campaña activa (RN-06).

V3 y posteriores:
- Tablas `incidencia` y `auditoria`.
- Credenciales de acceso del usuario, con el módulo de autenticación.
- Datos semilla: los cinco roles, un usuario administrador, cultivos e insumos de ejemplo.
- Índices de rendimiento, cuando haya consultas que los justifiquen.
- Posibles catálogos propios para tipo de labor, categoría de insumo y unidades de medida.

## Decisiones que el docente debe confirmar

- Si la cosecha debe indicar de cuál de las parcelas de la campaña proviene.
- Si la salida del almacenero y el consumo del operario son un solo registro o dos.

## Criterio de salida
Podemos escribir el DDL de V1 sin tomar decisiones nuevas importantes. Cumplido en la Clase 06: el script se ejecuta completo sobre una base vacía.
