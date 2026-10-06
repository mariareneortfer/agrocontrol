# Decisiones de integridad v0.1 — AgroControl

Relaciona las reglas de negocio y requisitos de la ficha oficial con el mecanismo que las protegerá. Complementa a `der-logico-v0.1.md` y `diccionario-datos-v0.1.md`.

Mecanismos: PK, FK, UQ (unicidad), NN (obligatorio), CHECK (regla simple sobre una fila) y backend (regla que depende de varias filas, de estados previos o de permisos).

## 1. Reglas de negocio

| RN/RF | Regla | Protección prevista | Justificación |
|---|---|---|---|
| RN-01 | Una parcela participa en campañas distintas en el tiempo, no superpuestas. | Tabla puente campana_parcela con FK + NN y UQ (campana_id, parcela_id); NN en fecha_inicio y fecha_fin_prevista; CHECK fecha_inicio <= fecha_fin_prevista; backend para la superposición. | La superposición compara las campañas que comparten una parcela, así que no se resuelve con una restricción simple. La UQ impide repetir una parcela dentro de la misma campaña. |
| RN-02 | Toda labor pertenece a una campaña y parcela. | FK + NN en labor.campana_parcela_id. | La labor apunta a una parcela de una campaña, así que no puede quedar en una parcela que no participa en esa campaña. |
| RN-03 | Una labor tiene estado planificada, asignada, en ejecución, completada o cancelada. | NN + CHECK de dominio en labor.estado; backend para las transiciones. | El conjunto de valores es una regla simple; el orden permitido entre estados depende del estado anterior. |
| RN-04 | El consumo de insumos no puede superar el stock disponible. | CHECK stock_disponible >= 0; CHECK cantidad > 0; backend dentro de una transacción. | Comparar el consumo con el stock involucra dos tablas y debe hacerse junto con el descuento, para que dos consumos simultáneos no pasen ambos. |
| RN-05 | Los operarios sólo reportan labores asignadas. | FK + NN en asignacion_labor.operario_id; UQ (labor_id, operario_id); backend para verificar el rol y la asignación. | Que el usuario tenga rol Operario y que esté asignado a esa labor depende de otras filas y de quién está autenticado. |
| RN-06 | La cosecha se registra contra una campaña activa/finalizable. | FK + NN en cosecha.campana_id; CHECK de dominio en campana.estado; backend para verificar el estado al registrar. | La FK garantiza que la campaña existe; que esté activa depende del estado de otra fila. |
| RN-07 | Las cantidades y unidades de medida deben estar explícitas. | NN en toda cantidad y en unidad_medida / unidad_superficie; CHECK cantidad > 0 y superficie > 0. | El consumo y el movimiento usan la unidad del insumo, que es obligatoria, así que nunca hay una cantidad sin unidad. |
| RN-08 | Las incidencias quedan vinculadas a parcela/campaña/labor. | FK + NN en incidencia.parcela_id; FK opcionales en campana_id y labor_id; backend para la coherencia. | Parcela es obligatoria; campaña y labor pueden faltar. Que la labor sea de esa campaña y la campaña de esa parcela depende de otras filas. |
| RN-09 | No se elimina historial de campañas terminadas. | FK sin borrado en cascada; backend que impide eliminar. | Al no borrar en cascada, la base rechaza eliminar una campaña con labores, consumos o cosechas. El backend además no ofrece la operación. |
| RN-10 | La IA no prescribe dosis ni tratamientos agronómicos. | Ninguna restricción de datos; límite funcional en backend. | No es una regla sobre la estructura. incidencia.clasificacion sólo guarda una etiqueta. |

## 2. Requisitos funcionales con impacto en la integridad

| RN/RF | Regla | Protección prevista | Justificación |
|---|---|---|---|
| RF-01 | Gestionar predios y parcelas. | FK + NN en parcela.predio_id; UQ (predio_id, codigo). | Unicidad contextual: el código sólo debe ser único dentro de su predio. |
| RF-05 | Asignar operarios. | Tabla puente asignacion_labor con UQ (labor_id, operario_id). | Evita asignar dos veces al mismo operario a la misma labor. |
| RF-09 | Gestionar insumos. | UQ en insumo.codigo. | Unicidad global: el almacén no puede tener dos insumos con el mismo código. |
| RF-10 | Registrar entradas/salidas de insumos. | NN + CHECK de dominio en movimiento_insumo.tipo; CHECK cantidad > 0. | Un movimiento sólo puede sumar o restar. |
| RF-11 | Asociar consumo a labor. | FK + NN en consumo_labor.labor_id e insumo_id; UQ en movimiento_insumo.consumo_labor_id. | Un consumo siempre tiene labor e insumo, y como máximo una salida de almacén. |
| RF-12 | Validar stock antes de consumo. | Backend en transacción (ver RN-04). | Misma razón que RN-04. |
| RF-18 | Auditar cambios críticos. | FK + NN en auditoria.usuario_id; NN en fecha_evento, accion, tabla_afectada y registro_id; FK + NN de usuario responsable en consumo, movimiento, cosecha, incidencia, bitácora y asignación. | Siempre debe poder responderse quién hizo qué y cuándo. |

## 3. Constraints estructurales frente a reglas transaccionales

Se resuelven con la estructura (PK, FK, UQ, NN, CHECK):
- Toda labor tiene campaña y parcela, y esa parcela participa en la campaña (RN-02).
- Toda cosecha tiene campaña (RN-06, parte estructural).
- Toda incidencia tiene parcela (RN-08, parte estructural).
- Los estados pertenecen a un conjunto cerrado (RN-03, parte estructural).
- Las cantidades son positivas y tienen unidad (RN-07).
- El stock nunca es negativo (RN-04, parte estructural).
- No se repiten correos, códigos de insumo ni códigos de parcela dentro de un predio.

Requieren backend porque dependen de varias filas, de estados previos o de permisos:
- No superposición de campañas en una parcela (RN-01).
- Toda campaña tiene al menos una parcela (flujo J).
- Transiciones de estado de la labor (RN-03).
- Consumo no mayor que el stock, con descuento en la misma transacción (RN-04).
- Sólo el operario asignado reporta la labor (RN-05).
- Cosecha sólo contra campaña activa (RN-06).
- Coherencia parcela — campaña — labor en incidencias y bitácora (RN-08).
- No eliminar historial de campañas finalizadas (RN-09).
- La IA no prescribe (RN-10).

## 4. Prueba de contradicción

| Regla de tu proyecto | Estado inválido posible | Protección prevista |
|---|---|---|
| RN-04 | El insumo "Urea" tiene 350 kg y se registra un consumo de 500 kg; el stock quedaría en -150. | El backend compara la cantidad con stock_disponible antes de guardar y rechaza la operación. Como segunda barrera, CHECK stock_disponible >= 0 impide que la base guarde un valor negativo. |
| RN-02 | Se crea la labor "Riego" de la campaña "Maíz verano 2026" en la parcela P-09, que no participa en esa campaña. | labor.campana_parcela_id es FK obligatoria hacia campana_parcela: como no existe la fila que une esa campaña con P-09, la base rechaza la labor. |
| RN-01 | La parcela P-03 tiene una campaña del 1/10/2026 al 28/2/2027 y se crea otra del 15/1/2027 al 30/6/2027. | El backend busca campañas de esa parcela cuyo período se cruce con el nuevo y rechaza la creación. |
| RN-05 | El operario Juan reporta como completada una labor asignada a Pedro. | El backend verifica que exista una fila en asignacion_labor con esa labor y ese operario antes de permitir el cambio de estado. |
| RN-07 | Se registra una cosecha de "1200" sin decir si son kg o quintales. | cosecha.unidad_medida es obligatoria: la base rechaza la fila sin unidad. |

## 5. Decisiones tomadas en esta clase

- Se cierra D-01 / P-01: una campaña abarca varias parcelas. Se agrega la tabla puente campana_parcela; campana ya no tiene parcela_id y labor apunta a campana_parcela.
- Se cierra P-09 de la Clase 03: quién creó la campaña y quién planificó la labor se obtiene de `auditoria`; no se agregan columnas propias.
- Se mantiene P-03: el consumo y el movimiento no guardan unidad; usan la del insumo.
- Se mantiene P-02: insumo.stock_disponible se guarda y sólo cambia mediante movimientos.
- labor.descripcion, insumo.categoria y predio.ubicacion pasan a ser opcionales, porque la ficha no las exige.
- campana.fecha_fin_prevista es obligatoria, porque sin ella no se puede validar RN-01.

## 6. Decisiones que siguen pendientes

- Cosecha por parcela: la cosecha se registra contra la campaña (RN-06). Falta confirmar si además debe indicar de cuál de las parcelas proviene.
- Relación entre la salida del almacenero y el consumo del operario (D-03 / P-05). Hoy cada consumo se respalda con una salida.
- UQ (labor_id, operario_id) frente a la necesidad de reasignar conservando historial (P-04).
- UQ (cultivo.nombre, cultivo.variedad): como variedad es opcional, hay que definir cómo se evita repetir un cultivo sin variedad. Se resuelve en la Clase 05.
- Catálogos: tipo_labor, categoria de insumo y unidades de medida, ¿texto controlado o tabla propia? (P-06)
- Un rol por usuario o varios (P-07).
- Estados de campaña: ¿hacen falta planificada y cancelada? (P-08)
- Credenciales de acceso del usuario: no se modelan todavía; corresponden al módulo de autenticación.
