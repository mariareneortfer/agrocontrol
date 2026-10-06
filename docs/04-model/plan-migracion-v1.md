# Plan de Migración V1 — AgroControl

Plan de la primera migración de base de datos. Todavía no se escribe ni se ejecuta SQL; eso corresponde a la Clase 06.

## Objetivo
Crear el núcleo que permite recorrer el flujo crítico de la ficha de principio a fin: el jefe crea una campaña sobre una parcela, planifica labores, asigna operarios, el almacén entrega insumos, el operario ejecuta y reporta, se actualiza la bitácora y se registra la cosecha.

## Tablas incluidas y orden

Una tabla que tiene una FK se crea después de la tabla a la que apunta.

1. rol (sin dependencias / raíz)
2. usuario (depende de rol)
3. predio (sin dependencias / raíz)
4. parcela (depende de predio)
5. cultivo (sin dependencias / raíz)
6. campana (depende de parcela y cultivo)
7. labor (depende de campana)
8. asignacion_labor (depende de labor y usuario)
9. insumo (sin dependencias / raíz)
10. consumo_labor (depende de labor, insumo y usuario)
11. movimiento_insumo (depende de insumo, usuario y consumo_labor)
12. cosecha (depende de campana y usuario)
13. bitacora_campo (depende de parcela, campana, labor y usuario)

Puntos del orden que conviene poder explicar:
- `consumo_labor` va antes que `movimiento_insumo`, porque el movimiento tiene la FK `consumo_labor_id`.
- `usuario` va al principio, porque casi todas las tablas de eventos registran quién hizo la operación.
- `rol`, `predio`, `cultivo` e `insumo` no dependen de nadie y podrían crearse en cualquier orden entre sí.

## Restricciones previstas

- PK: una por tabla, columna `<tabla>_id` de tipo BIGINT autogenerado (13 en total).
- FK:
  - usuario.rol_id -> rol
  - parcela.predio_id -> predio
  - campana.parcela_id -> parcela; campana.cultivo_id -> cultivo
  - labor.campana_id -> campana
  - asignacion_labor.labor_id -> labor; operario_id y asignado_por_id -> usuario
  - consumo_labor.labor_id -> labor; insumo_id -> insumo; registrado_por_id -> usuario
  - movimiento_insumo.insumo_id -> insumo; registrado_por_id -> usuario; consumo_labor_id -> consumo_labor
  - cosecha.campana_id -> campana; registrado_por_id -> usuario
  - bitacora_campo.parcela_id -> parcela; campana_id -> campana; labor_id -> labor; autor_id -> usuario
  - Ninguna borra en cascada (RN-09).
- NOT NULL: todas las columnas marcadas "NO" en `modelo-fisico-v0.1.md`. Quedan opcionales sólo: descripciones y observaciones, predio.ubicacion, parcela.ubicacion_referencial, cultivo.variedad, campana.fecha_cierre, labor.fecha_inicio_real, labor.fecha_fin_real, insumo.categoria, movimiento_insumo.motivo, movimiento_insumo.consumo_labor_id, bitacora_campo.campana_id y bitacora_campo.labor_id.
- UNIQUE:
  - uq_rol_nombre
  - uq_usuario_correo
  - uq_predio_nombre
  - uq_parcela_predio_codigo (predio_id, codigo)
  - uq_cultivo_nombre_variedad (nombre, variedad)
  - uq_campana_parcela_fecha_inicio (parcela_id, fecha_inicio)
  - uq_asignacion_labor_labor_operario (labor_id, operario_id)
  - uq_insumo_codigo
  - uq_movimiento_insumo_consumo (consumo_labor_id)
- CHECK:
  - ck_parcela_superficie_positiva: superficie > 0
  - ck_campana_estado: estado en (activa, finalizada)
  - ck_campana_fechas: fecha_inicio <= fecha_fin_prevista
  - ck_campana_cierre: si estado = finalizada, fecha_cierre no es nula
  - ck_labor_estado: estado en (planificada, asignada, en_ejecucion, completada, cancelada)
  - ck_labor_fechas_reales: fecha_inicio_real <= fecha_fin_real
  - ck_insumo_stock_no_negativo: stock_disponible >= 0
  - ck_consumo_labor_cantidad_positiva: cantidad > 0
  - ck_movimiento_insumo_tipo: tipo en (entrada, salida)
  - ck_movimiento_insumo_cantidad_positiva: cantidad > 0
  - ck_movimiento_insumo_consumo_salida: consumo_labor_id sólo tiene valor si tipo = salida
  - ck_cosecha_cantidad_positiva: cantidad > 0

## Reglas que requerirán lógica posterior

No se resuelven con un constraint simple porque dependen de otras filas, de estados previos o de permisos. Se implementarán en el backend.

- RN-01: dos campañas de la misma parcela no pueden tener períodos superpuestos.
- RN-03: transiciones permitidas entre estados de la labor.
- RN-04 / RF-12: el consumo no puede superar el stock disponible; la validación y el descuento van en la misma transacción.
- RN-05: sólo un usuario con rol Operario puede ser asignado, y sólo el operario asignado puede iniciar, completar o reportar la labor.
- RN-06: sólo se registra cosecha contra una campaña activa.
- RN-08: en la bitácora, la labor indicada debe ser de la campaña indicada, y la campaña debe ser de la parcela indicada.
- RN-09: no se eliminan campañas finalizadas ni sus registros.
- El stock de un insumo sólo cambia cuando se registra un movimiento.

## Fuera de V1

- Tabla `incidencia`: no participa en el flujo crítico. Entra en V2.
- Tabla `auditoria`: entra en V2, junto con la identificación real de usuarios.
- Credenciales de acceso del usuario (contraseña): entran con el módulo de autenticación.
- Datos semilla (los cinco roles, un usuario administrador, cultivos e insumos de ejemplo): migración separada, V2.
- Índices de rendimiento sobre FK y consultas del dashboard: se agregan cuando haya consultas que los justifiquen.
- Posibles catálogos propios para tipo de labor, categoría de insumo y unidades de medida.

## Decisiones que el docente debe confirmar antes de escribir el DDL

- Si una campaña abarca una o varias parcelas. Si son varias, V1 cambia: aparece `campana_parcela` y `labor` necesita `parcela_id`.
- Si la salida del almacenero y el consumo del operario son un solo registro o dos.

## Criterio de salida
Podemos escribir el DDL de V1 sin tomar decisiones nuevas importantes.
