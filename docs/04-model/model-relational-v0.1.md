# AgroControl — Modelo relacional v0.1

Este documento transforma el modelo conceptual en tablas candidatas. Todavía no contiene SQL, tipos de datos ni entidades JPA.

## 1. Fuente
- Proyecto oficial: 16. AgroControl — Gestión de Lotes Agrícolas, Campañas, Labores, Insumos y Cosecha.
- Modelo conceptual base: `model-conceptual-v0.1.md`.
- Flujo crítico utilizado (sección J de la ficha): Jefe crea campaña sobre parcela → planifica labores → asigna operario → almacén entrega insumo → operario ejecuta y reporta desde móvil → se actualiza bitácora → se registra cosecha → supervisor revisa indicadores.

## 2. Criterios de transformación
- Relaciones 1:N: FK en el lado N.
- Relaciones N:M: tabla puente.
- Optionalidad registrada antes de decidir NULL/NOT NULL.
- Claves naturales relevantes registradas como candidatas a UNIQUE.
- Nombres en español, en singular y en snake_case.
- PK técnica con el patrón `<tabla>_id` en todas las tablas; las claves naturales se protegen con UNIQUE.
- Cuando una tabla tiene dos FK hacia `usuario`, el nombre de la columna indica el papel (`operario_id`, `asignado_por_id`, `registrado_por_id`).

## 3. Tablas candidatas núcleo

Se eligieron 8 tablas porque son las que permiten recorrer el flujo crítico de principio a fin.

### parcela
Propósito: una fila representa un lote agrícola concreto dentro de un predio.
Ejemplo de fila: la parcela "P-03 Lote Norte" de 4,5 hectáreas del predio "San Isidro".
- parcela_id [PK]
- predio_id [FK -> predio.predio_id]
- codigo
- nombre
- superficie
- unidad_superficie
- ubicacion_referencial (opcional)

Reglas relacionadas: RN-01, RN-07 / RF-01, RF-15

### campana
Propósito: una fila representa un ciclo productivo de un cultivo sobre una parcela durante un período.
Ejemplo de fila: la campaña "Maíz verano 2026" en la parcela P-03, del 1/10/2026 al 28/2/2027, activa.
- campana_id [PK]
- parcela_id [FK -> parcela.parcela_id]
- cultivo_id [FK -> cultivo.cultivo_id]
- nombre
- fecha_inicio
- fecha_fin_prevista
- fecha_cierre (opcional)
- estado

Reglas relacionadas: RN-01, RN-06, RN-09 / RF-03, RF-16

### labor
Propósito: una fila representa un trabajo de campo planificado dentro de una campaña.
Ejemplo de fila: la labor "Riego" programada para el 12/10/2026 en la campaña "Maíz verano 2026", en estado asignada.
- labor_id [PK]
- campana_id [FK -> campana.campana_id]
- tipo_labor
- descripcion
- fecha_programada
- fecha_inicio_real (opcional)
- fecha_fin_real (opcional)
- estado

Reglas relacionadas: RN-02, RN-03 / RF-04, RF-07

### usuario
Propósito: una fila representa a una persona con cuenta en el sistema.
Ejemplo de fila: Juan Mamani, correo juan@agro.bo, rol Operario, activo.
- usuario_id [PK]
- rol_id [FK -> rol.rol_id]
- nombre_completo
- correo
- activo

Reglas relacionadas: RN-05 / RF-05, RF-18, sección C

### asignacion_labor
Propósito: una fila representa que un operario fue designado para ejecutar una labor.
Ejemplo de fila: el 10/10/2026 la jefa de campo asignó a Juan Mamani la labor "Riego" del 12/10/2026.
- asignacion_labor_id [PK]
- labor_id [FK -> labor.labor_id]
- operario_id [FK -> usuario.usuario_id]
- asignado_por_id [FK -> usuario.usuario_id]
- fecha_asignacion

Reglas relacionadas: RN-05 / RF-05, RF-06

### insumo
Propósito: una fila representa un producto de almacén que se usa en las labores.
Ejemplo de fila: el insumo "INS-014 Urea", categoría fertilizante, medido en kg, con 350 kg disponibles.
- insumo_id [PK]
- codigo
- nombre
- categoria
- unidad_medida
- stock_disponible

Reglas relacionadas: RN-04, RN-07 / RF-09, RF-12

### consumo_labor
Propósito: una fila representa una cantidad de un insumo usada en una labor.
Ejemplo de fila: en la labor "Fertilización" del 15/10/2026 se usaron 40 de "INS-014 Urea", registrado por Juan Mamani.
- consumo_labor_id [PK]
- labor_id [FK -> labor.labor_id]
- insumo_id [FK -> insumo.insumo_id]
- registrado_por_id [FK -> usuario.usuario_id]
- cantidad
- fecha_consumo

Reglas relacionadas: RN-04, RN-07 / RF-11, RF-12

### cosecha
Propósito: una fila representa una entrega de producción obtenida en una campaña.
Ejemplo de fila: el 20/2/2027 se cosecharon 1 200 kg en la campaña "Maíz verano 2026".
- cosecha_id [PK]
- campana_id [FK -> campana.campana_id]
- registrado_por_id [FK -> usuario.usuario_id]
- fecha_cosecha
- cantidad
- unidad_medida
- observacion (opcional)

Reglas relacionadas: RN-06, RN-07 / RF-14

### Tablas de apoyo referenciadas por el núcleo

Son catálogos sencillos. Se definen aquí en forma breve porque el núcleo tiene FK hacia ellas.

**predio** — una fila representa una propiedad agrícola.
- predio_id [PK]
- nombre
- ubicacion
- descripcion (opcional)

**cultivo** — una fila representa un cultivo del catálogo.
- cultivo_id [PK]
- nombre
- variedad (opcional)
- descripcion (opcional)

**rol** — una fila representa una responsabilidad (Administrador, Jefe de campo, Operario, Almacenero, Supervisor).
- rol_id [PK]
- nombre
- descripcion (opcional)

### Tablas que quedan para la Clase 04
movimiento_insumo, bitacora_campo, incidencia y auditoria. Existen en el modelo conceptual y son obligatorias por la sección F, pero no se detallan todavía porque dependen de dudas abiertas (ver sección 9).

## 4. Relaciones

| Relación | Dónde queda la FK | Justificación |
|---|---|---|
| predio 1:N parcela | parcela.predio_id | Un predio tiene muchas parcelas; cada parcela pertenece a un predio. RF-01 |
| parcela 1:N campana | campana.parcela_id | Una parcela participa en muchas campañas en el tiempo; cada campaña se hace sobre una parcela. RN-01, flujo J |
| cultivo 1:N campana | campana.cultivo_id | Un cultivo se usa en muchas campañas; cada campaña trabaja un cultivo. RF-02, RF-03 |
| campana 1:N labor | labor.campana_id | Una campaña tiene muchas labores; toda labor pertenece a una campaña. RN-02 |
| labor 1:N asignacion_labor | asignacion_labor.labor_id | Una labor puede tener varias asignaciones. RF-05 |
| usuario 1:N asignacion_labor (operario) | asignacion_labor.operario_id | Un operario recibe muchas asignaciones. RN-05 |
| usuario 1:N asignacion_labor (quien asigna) | asignacion_labor.asignado_por_id | Un jefe de campo realiza muchas asignaciones. RF-05, RF-18 |
| labor 1:N consumo_labor | consumo_labor.labor_id | Una labor registra varios consumos. RF-11 |
| insumo 1:N consumo_labor | consumo_labor.insumo_id | Un insumo aparece en muchos consumos. RF-11 |
| usuario 1:N consumo_labor | consumo_labor.registrado_por_id | Debe saberse quién registró el consumo. RF-18 |
| campana 1:N cosecha | cosecha.campana_id | Una campaña puede tener varias cosechas; cada cosecha es de una campaña. RN-06 |
| usuario 1:N cosecha | cosecha.registrado_por_id | Debe saberse quién registró la cosecha. RF-18 |
| rol 1:N usuario | usuario.rol_id | Un rol lo tienen muchos usuarios; cada usuario tiene un rol. Sección C |

## 5. Relaciones N:M

- labor N:M usuario (operario) → **asignacion_labor**
  - Una labor puede asignarse a varios operarios y un operario puede tener varias labores (RF-05 dice "asignar operarios").
  - Atributos propios de la relación: fecha_asignacion, asignado_por_id.
- labor N:M insumo → **consumo_labor**
  - Una labor puede usar varios insumos y un insumo se usa en muchas labores (RF-11).
  - Atributos propios de la relación: cantidad, fecha_consumo, registrado_por_id.
  - Lleva PK técnica y no PK compuesta (labor_id, insumo_id), porque una misma labor puede registrar el mismo insumo más de una vez.

No se detectó otra N:M en el núcleo. Parcela — campaña queda como 1:N por la decisión D-01 del modelo conceptual (ver P-01).

## 6. Claves naturales / UNIQUE candidatas

- usuario.correo — identifica a la persona al iniciar sesión; RN-05 y RF-18 exigen saber exactamente quién actúa. No se usa como PK porque un correo puede cambiar.
- insumo.codigo — el almacén necesita distinguir un insumo de otro sin ambigüedad (RF-09, RF-10). Supuesto a confirmar: la ficha no menciona un código.
- (parcela.predio_id, parcela.codigo) — el código de parcela no debe repetirse dentro del mismo predio (RF-01). Es UNIQUE compuesta porque la unicidad depende del predio.
- (asignacion_labor.labor_id, asignacion_labor.operario_id) — un operario no debe quedar asignado dos veces a la misma labor (RF-05). Ver P-04.
- (campana.parcela_id, campana.fecha_inicio) — dos campañas de una parcela no pueden empezar el mismo día (consecuencia de RN-01). No reemplaza la regla de no superposición.
- rol.nombre — no pueden existir dos roles con el mismo nombre (sección C).
- predio.nombre — supuesto a confirmar.
- (cultivo.nombre, cultivo.variedad) — evita cultivos duplicados en el catálogo (RF-02). Supuesto a confirmar.

No son UNIQUE: (consumo_labor.labor_id, consumo_labor.insumo_id) ni (cosecha.campana_id, cosecha.fecha_cosecha), porque el negocio permite varios registros.

## 7. Optionalidad

FK del núcleo:

- parcela.predio_id — obligatoria: una parcela no existe sin predio (RF-01).
- campana.parcela_id — obligatoria: la campaña se crea sobre una parcela (flujo J).
- campana.cultivo_id — obligatoria: una campaña necesita saber qué se cultiva (D-02 del modelo conceptual).
- labor.campana_id — obligatoria: RN-02.
- usuario.rol_id — obligatoria: sin rol no se sabe qué puede hacer el usuario (sección C).
- asignacion_labor.labor_id — obligatoria: la asignación sólo existe para una labor.
- asignacion_labor.operario_id — obligatoria: la asignación nombra a un operario.
- asignacion_labor.asignado_por_id — obligatoria: debe saberse quién asignó (RF-18).
- consumo_labor.labor_id — obligatoria: RF-11 asocia el consumo a una labor.
- consumo_labor.insumo_id — obligatoria: no hay consumo sin insumo.
- consumo_labor.registrado_por_id — obligatoria: trazabilidad (RF-18).
- cosecha.campana_id — obligatoria: RN-06.
- cosecha.registrado_por_id — obligatoria: trazabilidad (RF-18).

Todas las FK del núcleo resultaron obligatorias. Las FK opcionales aparecerán en las tablas de la Clase 04:

- incidencia.campana_id e incidencia.labor_id — opcionales: una incidencia siempre tiene parcela, pero puede ocurrir fuera de una campaña o sin una labor concreta (RN-08, D-05). En el modelo físico admitirán NULL.
- bitacora_campo.campana_id y bitacora_campo.labor_id — opcionales por la misma razón (RF-08).

Atributos opcionales del núcleo (no son FK): campana.fecha_cierre, labor.fecha_inicio_real, labor.fecha_fin_real, parcela.ubicacion_referencial, cosecha.observacion.

## 8. Reglas iniciales de integridad

Restricciones candidatas sobre columnas:

- campana.fecha_inicio debe ser anterior o igual a campana.fecha_fin_prevista.
- labor.fecha_inicio_real debe ser anterior o igual a labor.fecha_fin_real.
- consumo_labor.cantidad, cosecha.cantidad y parcela.superficie deben ser mayores que cero (RN-07).
- insumo.stock_disponible no puede ser negativo (RN-04).
- labor.estado sólo admite planificada, asignada, en_ejecucion, completada o cancelada (RN-03).
- campana.estado sólo admite activa o finalizada (RN-06, RN-09; ver P-08).
- insumo.unidad_medida, cosecha.unidad_medida y parcela.unidad_superficie son obligatorias (RN-07).

Reglas que no se resuelven con una restricción simple y se validarán en el backend:

- Dos campañas de la misma parcela no pueden tener períodos superpuestos (RN-01).
- La cantidad de un consumo no puede superar insumo.stock_disponible en ese momento (RN-04, RF-12).
- asignacion_labor.operario_id debe apuntar a un usuario con rol Operario (RN-05).
- Sólo un operario asignado a la labor puede iniciarla, completarla o registrar consumos (RN-05).
- Una labor en estado asignada o posterior debe tener al menos una asignación (RN-03).
- Sólo se registra cosecha contra una campaña activa (RN-06).
- No se eliminan campañas finalizadas ni sus labores, consumos y cosechas (RN-09).

## 9. Decisiones pendientes

- P-01. Campaña sobre una o varias parcelas (D-01). Hoy es 1:N con campana.parcela_id. Si el docente confirma que una campaña abarca varias parcelas, se crea la tabla puente campana_parcela y labor necesitará además parcela_id.
- P-02. insumo.stock_disponible: ¿se guarda o se calcula a partir de movimiento_insumo? (D-07). Hoy se guarda, y es una redundancia controlada que se revisará al modelar movimiento_insumo.
- P-03. Unidad de medida del consumo. Hoy no se guarda en consumo_labor: se toma de insumo.unidad_medida. Confirmar si RN-07 exige guardarla en cada registro.
- P-04. Reasignaciones. Con UNIQUE (labor_id, operario_id) no se puede quitar y volver a asignar al mismo operario conservando el historial. Si se necesita ese historial, la UNIQUE se retira o se agrega un estado a la asignación.
- P-05. Relación entre consumo_labor y movimiento_insumo (D-03): ¿cada consumo genera una salida de almacén, o la salida del almacenero y el consumo del operario son registros distintos?
- P-06. labor.tipo_labor, insumo.categoria y las unidades de medida: ¿texto controlado o catálogos con tabla propia? (D-11)
- P-07. Un rol por usuario (D-06). Si se permite más de uno, aparece la tabla puente usuario_rol.
- P-08. Estados de campaña: la ficha sólo nombra activa y finalizada; falta confirmar si hacen falta planificada y cancelada (D-10).
- P-09. Quién creó la campaña y quién planificó la labor: ¿columna propia en cada tabla o se obtiene de auditoria? Se decide en la Clase 04 junto con los campos de auditoría.

## 10. Revisión de normalización básica

- Listas multivaluadas detectadas/corregidas: los operarios de una labor no se guardan como lista dentro de labor ("3, 5, 9"); cada asignación es una fila de asignacion_labor. Los insumos usados en una labor tampoco se guardan como lista; cada uno es una fila de consumo_labor.
- Columnas repetitivas detectadas/corregidas: no existen columnas como operario1/operario2 ni insumo1/insumo2/cantidad1/cantidad2. Una campaña con varias cosechas parciales usa varias filas de cosecha, no columnas cosecha1/cosecha2.
- Datos redundantes detectados/corregidos:
  - labor no tiene parcela_id: la parcela de una labor se obtiene por labor → campana → parcela. Así se cumple RN-02 sin riesgo de que la labor apunte a una parcela distinta a la de su campaña.
  - consumo_labor no copia el nombre ni la unidad del insumo: se obtienen por insumo_id.
  - asignacion_labor no copia el nombre del operario; campana no copia el nombre de la parcela ni del cultivo; usuario no copia el nombre del rol.
- Atributos que dependen de otra entidad: unidad_medida del consumo depende del insumo y no del consumo, por eso está en insumo. El estado es una columna de labor y de campana, no una tabla.
- Redundancia que se mantiene a propósito: insumo.stock_disponible (ver P-02).
