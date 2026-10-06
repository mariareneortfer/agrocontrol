# Diccionario de datos v0.1 — AgroControl

Complementa a `der-logico-v0.1.md`. Describe qué significa cada atributo, si es obligatorio y por qué, su papel estructural, su dominio y la regla o requisito de la ficha que lo origina. Todavía no define tipos de datos; eso corresponde a la Clase 05.

Cambio respecto a la primera versión: una campaña abarca varias parcelas; se agrega la tabla `campana_parcela`.

Lectura de las columnas:
- Obligatorio: "Sí" significa que no puede existir una fila válida sin ese dato; "No" significa que puede faltar legítimamente.
- PK/FK/UQ: papel estructural del atributo. "UQ con ..." indica que forma parte de una unicidad compuesta.
- Origen: regla de negocio (RN), requisito funcional (RF) o sección de la ficha.

## rol

Una fila representa una responsabilidad dentro del sistema.

| Atributo | Significado | Obligatorio | PK/FK/UQ | Dominio/regla | Origen |
|---|---|---|---|---|---|
| rol_id | Identificador interno del rol. | Sí: identifica la fila. | PK | Generado por el sistema. | Sección F |
| nombre | Nombre de la responsabilidad. | Sí: sin nombre no se sabe qué rol es. | UQ | Administrador, Jefe de campo, Operario, Almacenero, Supervisor. | Sección C |
| descripcion | Explicación de lo que puede hacer el rol. | No: es sólo aclaratoria. | — | Texto libre. | Sección C |

## usuario

Una fila representa a una persona con cuenta en el sistema.

| Atributo | Significado | Obligatorio | PK/FK/UQ | Dominio/regla | Origen |
|---|---|---|---|---|---|
| usuario_id | Identificador interno del usuario. | Sí: identifica la fila. | PK | Generado por el sistema. | Sección F |
| rol_id | Rol que define qué puede hacer la persona. | Sí: sin rol no se puede decidir sus permisos. | FK -> rol | Debe existir en rol. | Sección C, RN-05 |
| nombre_completo | Nombre y apellido de la persona. | Sí: se muestra en asignaciones y registros. | — | Texto no vacío. | RF-05, RF-18 |
| correo | Correo con el que la persona se identifica. | Sí: es su clave natural. | UQ | No se repite en todo el sistema. | RF-18 |
| activo | Indica si la cuenta puede usarse. | Sí: siempre se sabe si está habilitada. | — | Sí o no. Un usuario con historial se desactiva, no se elimina. | RN-09, RF-18 |

## predio

Una fila representa una propiedad agrícola.

| Atributo | Significado | Obligatorio | PK/FK/UQ | Dominio/regla | Origen |
|---|---|---|---|---|---|
| predio_id | Identificador interno del predio. | Sí: identifica la fila. | PK | Generado por el sistema. | Sección F |
| nombre | Nombre con que el cliente conoce el predio. | Sí: es como se lo distingue. | UQ | No se repite en todo el sistema. | RF-01 |
| ubicacion | Referencia de dónde se encuentra el predio. | No: puede registrarse el predio y completarse después. | — | Texto libre. | RF-01 |
| descripcion | Notas generales sobre el predio. | No: informativa. | — | Texto libre. | RF-01 |

## parcela

Una fila representa un lote agrícola dentro de un predio.

| Atributo | Significado | Obligatorio | PK/FK/UQ | Dominio/regla | Origen |
|---|---|---|---|---|---|
| parcela_id | Identificador interno de la parcela. | Sí: identifica la fila. | PK | Generado por el sistema. | Sección F |
| predio_id | Predio al que pertenece la parcela. | Sí: una parcela no existe sin predio. | FK -> predio; UQ con codigo | Debe existir en predio. | RF-01 |
| codigo | Código corto con que se nombra la parcela en campo. | Sí: es su clave natural dentro del predio. | UQ con predio_id | No se repite dentro del mismo predio. | RF-01 |
| nombre | Nombre descriptivo de la parcela. | Sí: se muestra en listados y bitácora. | — | Texto no vacío. | RF-01, RF-15 |
| superficie | Tamaño de la parcela. | Sí: dato básico del lote. | — | Mayor que cero. | RN-07 |
| unidad_superficie | Unidad en que se expresa la superficie. | Sí: una cantidad sin unidad no es válida. | — | Por ejemplo, hectárea o metro cuadrado. | RN-07 |
| ubicacion_referencial | Referencia para ubicar la parcela en el mapa o listado. | No: puede no conocerse al registrarla. | — | Texto libre. | Sección H |

## cultivo

Una fila representa un cultivo del catálogo.

| Atributo | Significado | Obligatorio | PK/FK/UQ | Dominio/regla | Origen |
|---|---|---|---|---|---|
| cultivo_id | Identificador interno del cultivo. | Sí: identifica la fila. | PK | Generado por el sistema. | Sección F |
| nombre | Nombre del cultivo. | Sí: sin nombre no se sabe qué se siembra. | UQ con variedad | Texto no vacío. | RF-02 |
| variedad | Variedad específica del cultivo. | No: no todos los cultivos la distinguen. | UQ con nombre | Texto libre. | RF-02 |
| descripcion | Notas sobre el cultivo. | No: informativa. | — | Texto libre. | RF-02 |

## campana

Una fila representa un ciclo productivo de un cultivo sobre una o varias parcelas.

| Atributo | Significado | Obligatorio | PK/FK/UQ | Dominio/regla | Origen |
|---|---|---|---|---|---|
| campana_id | Identificador interno de la campaña. | Sí: identifica la fila. | PK | Generado por el sistema. | Sección F |
| cultivo_id | Cultivo que se trabaja en la campaña. | Sí: la campaña necesita saber qué se cultiva. | FK -> cultivo | Debe existir en cultivo. | RF-03 |
| nombre | Nombre con que el equipo identifica la campaña. | Sí: se muestra en agenda, bitácora y dashboard. | — | Texto no vacío. | RF-03 |
| fecha_inicio | Día en que empieza la campaña. | Sí: sin ella no se puede controlar la superposición. | — | Menor o igual que fecha_fin_prevista. | RN-01 |
| fecha_fin_prevista | Día en que se espera terminar. | Sí: define el período que ocupa la parcela. | — | Mayor o igual que fecha_inicio. | RN-01 |
| fecha_cierre | Día en que la campaña se dio por finalizada. | No: no existe mientras la campaña está activa. | — | Obligatoria cuando estado es finalizada. | RN-06, RN-09 |
| estado | Situación actual de la campaña. | Sí: decide si admite labores y cosecha. | — | activa o finalizada. | RN-06, RN-09 |

## campana_parcela

Una fila representa que una parcela participa en una campaña.

| Atributo | Significado | Obligatorio | PK/FK/UQ | Dominio/regla | Origen |
|---|---|---|---|---|---|
| campana_parcela_id | Identificador interno de la participación. | Sí: identifica la fila y es a lo que apunta la labor. | PK | Generado por el sistema. | Diseño |
| campana_id | Campaña que abarca la parcela. | Sí: la fila sólo existe para unir una campaña con una parcela. | FK -> campana; UQ con parcela_id | Debe existir en campana. | RN-01, flujo J |
| parcela_id | Parcela que participa en la campaña. | Sí: misma razón. | FK -> parcela; UQ con campana_id | Debe existir en parcela. Una parcela no se repite dentro de la misma campaña ni participa en dos campañas con períodos superpuestos. | RN-01 |

## labor

Una fila representa un trabajo de campo planificado en una parcela de una campaña.

| Atributo | Significado | Obligatorio | PK/FK/UQ | Dominio/regla | Origen |
|---|---|---|---|---|---|
| labor_id | Identificador interno de la labor. | Sí: identifica la fila. | PK | Generado por el sistema. | Sección F |
| campana_parcela_id | Parcela de una campaña en la que se realiza la labor. | Sí: toda labor pertenece a una campaña y parcela. | FK -> campana_parcela | Debe existir en campana_parcela. De ahí se obtienen la campaña y la parcela. | RN-02 |
| tipo_labor | Clase de trabajo: siembra, riego, fertilización, etc. | Sí: dice qué se va a hacer. | — | Texto controlado. | RF-04 |
| descripcion | Detalle o indicaciones para ejecutar la labor. | No: el tipo puede ser suficiente. | — | Texto libre. | RF-04 |
| fecha_programada | Día en que debe realizarse la labor. | Sí: se necesita para la agenda de campo. | — | Fecha válida. | RF-04, RF-06 |
| fecha_inicio_real | Momento en que el operario la inició. | No: no existe hasta que se inicia. | — | Menor o igual que fecha_fin_real. | RF-07 |
| fecha_fin_real | Momento en que el operario la completó. | No: no existe hasta que se completa. | — | Mayor o igual que fecha_inicio_real. | RF-07 |
| estado | Situación actual de la labor. | Sí: gobierna qué se puede hacer con ella. | — | planificada, asignada, en_ejecucion, completada, cancelada. | RN-03 |

## asignacion_labor

Una fila representa que un operario fue designado para una labor.

| Atributo | Significado | Obligatorio | PK/FK/UQ | Dominio/regla | Origen |
|---|---|---|---|---|---|
| asignacion_labor_id | Identificador interno de la asignación. | Sí: identifica la fila. | PK | Generado por el sistema. | Sección F |
| labor_id | Labor que se asigna. | Sí: la asignación sólo existe para una labor. | FK -> labor; UQ con operario_id | Debe existir en labor. | RF-05 |
| operario_id | Usuario que ejecutará la labor. | Sí: la asignación nombra a una persona. | FK -> usuario; UQ con labor_id | Debe ser un usuario con rol Operario. | RN-05 |
| asignado_por_id | Usuario que hizo la asignación. | Sí: debe saberse quién asignó. | FK -> usuario | Debe existir en usuario. | RF-05, RF-18 |
| fecha_asignacion | Momento en que se hizo la asignación. | Sí: forma parte de la trazabilidad. | — | Fecha válida. | RF-18 |

## insumo

Una fila representa un producto de almacén.

| Atributo | Significado | Obligatorio | PK/FK/UQ | Dominio/regla | Origen |
|---|---|---|---|---|---|
| insumo_id | Identificador interno del insumo. | Sí: identifica la fila. | PK | Generado por el sistema. | Sección F |
| codigo | Código con que el almacén distingue el insumo. | Sí: es su clave natural. | UQ | No se repite en todo el sistema. | RF-09 |
| nombre | Nombre comercial o común del insumo. | Sí: se muestra al registrar consumos. | — | Texto no vacío. | RF-09 |
| categoria | Tipo de insumo: fertilizante, semilla, etc. | No: la ficha no la exige. | — | Texto controlado. | RF-09 |
| unidad_medida | Unidad en que se mide el insumo. | Sí: toda cantidad debe tener unidad explícita. | — | Por ejemplo, kg o litro. Los consumos y movimientos se expresan en esta unidad. | RN-07 |
| stock_disponible | Cantidad que hay actualmente en almacén. | Sí: se necesita para validar cada consumo. | — | Mayor o igual que cero. Sólo cambia mediante movimientos. | RN-04, RF-12 |

## movimiento_insumo

Una fila representa una entrada o una salida de un insumo del almacén.

| Atributo | Significado | Obligatorio | PK/FK/UQ | Dominio/regla | Origen |
|---|---|---|---|---|---|
| movimiento_insumo_id | Identificador interno del movimiento. | Sí: identifica la fila. | PK | Generado por el sistema. | Sección F |
| insumo_id | Insumo que entra o sale. | Sí: no hay movimiento sin insumo. | FK -> insumo | Debe existir en insumo. | RF-10 |
| registrado_por_id | Usuario que registró el movimiento. | Sí: debe saberse quién lo hizo. | FK -> usuario | Debe existir en usuario. | RF-10, RF-18 |
| consumo_labor_id | Consumo que originó la salida. | No: las entradas y las salidas sin labor no tienen consumo. | FK -> consumo_labor; UQ | Sólo puede tener valor si tipo es salida. Un consumo tiene como máximo una salida. | RF-11 |
| tipo | Dirección del movimiento. | Sí: define si suma o resta stock. | — | entrada o salida. | RF-10 |
| cantidad | Cuánto entra o sale, en la unidad del insumo. | Sí: es el dato central del movimiento. | — | Mayor que cero. | RN-07 |
| fecha_movimiento | Momento en que ocurrió el movimiento. | Sí: permite reconstruir el stock en el tiempo. | — | Fecha válida. | RF-10 |
| motivo | Explicación del movimiento: compra, ajuste, devolución. | No: no siempre hace falta aclararlo. | — | Texto libre. | RF-10 |

## consumo_labor

Una fila representa una cantidad de un insumo usada en una labor.

| Atributo | Significado | Obligatorio | PK/FK/UQ | Dominio/regla | Origen |
|---|---|---|---|---|---|
| consumo_labor_id | Identificador interno del consumo. | Sí: identifica la fila. Una labor puede repetir el mismo insumo. | PK | Generado por el sistema. | Sección F |
| labor_id | Labor en la que se usó el insumo. | Sí: el consumo siempre se asocia a una labor. | FK -> labor | Debe existir en labor. | RF-11 |
| insumo_id | Insumo que se usó. | Sí: no hay consumo sin insumo. | FK -> insumo | Debe existir en insumo. | RF-11 |
| registrado_por_id | Usuario que registró el consumo. | Sí: debe saberse quién lo hizo. | FK -> usuario | Debe existir en usuario. | RN-05, RF-18 |
| cantidad | Cuánto se usó, en la unidad del insumo. | Sí: es el dato central del consumo. | — | Mayor que cero y no mayor que el stock disponible. | RN-04, RN-07 |
| fecha_consumo | Momento en que se registró el uso. | Sí: ordena la bitácora de la parcela. | — | Fecha válida. | RF-15 |

## bitacora_campo

Una fila representa una observación de campo anotada en la bitácora de una parcela.

| Atributo | Significado | Obligatorio | PK/FK/UQ | Dominio/regla | Origen |
|---|---|---|---|---|---|
| bitacora_campo_id | Identificador interno de la entrada. | Sí: identifica la fila. | PK | Generado por el sistema. | Sección F |
| parcela_id | Parcela sobre la que se hace la observación. | Sí: la bitácora se consulta por parcela. | FK -> parcela | Debe existir en parcela. | RF-15 |
| campana_id | Campaña a la que se refiere la observación. | No: puede anotarse fuera de una campaña. | FK -> campana | Si tiene valor, la parcela debe participar en esa campaña. | RF-08 |
| labor_id | Labor durante la que se hizo la observación. | No: no toda observación ocurre en una labor. | FK -> labor | Si tiene valor, la labor debe ser de esa campaña y de esa parcela. | RF-08 |
| autor_id | Usuario que escribió la observación. | Sí: debe saberse quién la anotó. | FK -> usuario | Debe existir en usuario. | RF-08, RF-18 |
| fecha_registro | Momento en que se anotó. | Sí: ordena la bitácora. | — | Fecha válida. | RF-15 |
| descripcion | Texto de la observación. | Sí: una entrada vacía no aporta nada. | — | Texto no vacío. | RF-08 |

## incidencia

Una fila representa un hecho excepcional ocurrido en campo.

| Atributo | Significado | Obligatorio | PK/FK/UQ | Dominio/regla | Origen |
|---|---|---|---|---|---|
| incidencia_id | Identificador interno de la incidencia. | Sí: identifica la fila. | PK | Generado por el sistema. | Sección F |
| parcela_id | Parcela donde ocurrió. | Sí: toda incidencia queda vinculada a una parcela. | FK -> parcela | Debe existir en parcela. | RN-08 |
| campana_id | Campaña afectada. | No: puede ocurrir fuera de una campaña. | FK -> campana | Si tiene valor, la parcela debe participar en esa campaña. | RN-08 |
| labor_id | Labor durante la que ocurrió. | No: no siempre hay una labor concreta. | FK -> labor | Si tiene valor, la labor debe ser de esa campaña y de esa parcela. | RN-08 |
| reportado_por_id | Usuario que reportó la incidencia. | Sí: debe saberse quién la reportó. | FK -> usuario | Debe existir en usuario. | RF-13, RF-18 |
| fecha_incidencia | Momento en que ocurrió o se reportó. | Sí: forma parte de la trazabilidad. | — | Fecha válida. | RF-13 |
| descripcion | Relato de lo ocurrido. | Sí: es el contenido de la incidencia. | — | Texto no vacío. | RF-13 |
| clasificacion | Etiqueta que agrupa la incidencia por tipo. | No: puede asignarse después, incluso con apoyo de IA. | — | Texto controlado. Sólo clasifica; no recomienda tratamientos. | RN-10, sección M |

## cosecha

Una fila representa una entrega de producción de una campaña.

| Atributo | Significado | Obligatorio | PK/FK/UQ | Dominio/regla | Origen |
|---|---|---|---|---|---|
| cosecha_id | Identificador interno de la cosecha. | Sí: identifica la fila. Una campaña puede tener varias. | PK | Generado por el sistema. | Sección F |
| campana_id | Campaña de la que proviene la producción. | Sí: la cosecha se registra contra una campaña. | FK -> campana | La campaña debe estar activa al registrar. | RN-06 |
| registrado_por_id | Usuario que capturó la cosecha. | Sí: debe saberse quién la registró. | FK -> usuario | Debe existir en usuario. | RF-14, RF-18 |
| fecha_cosecha | Día en que se cosechó. | Sí: ubica la producción en el tiempo. | — | Fecha válida. | RF-14 |
| cantidad | Cuánto se cosechó. | Sí: es el dato central. | — | Mayor que cero. | RN-07 |
| unidad_medida | Unidad en que se expresa la cantidad. | Sí: una cantidad sin unidad no es válida. | — | Por ejemplo, kg, quintal o caja. | RN-07 |
| observacion | Comentario sobre la cosecha. | No: informativo. | — | Texto libre. | RF-14 |

## auditoria

Una fila representa un cambio crítico realizado por un usuario.

| Atributo | Significado | Obligatorio | PK/FK/UQ | Dominio/regla | Origen |
|---|---|---|---|---|---|
| auditoria_id | Identificador interno del registro. | Sí: identifica la fila. | PK | Generado por el sistema. | Sección F |
| usuario_id | Usuario que hizo el cambio. | Sí: la auditoría responde quién. | FK -> usuario | Debe existir en usuario. | RF-18 |
| fecha_evento | Momento del cambio. | Sí: la auditoría responde cuándo. | — | Fecha válida. | RF-18 |
| accion | Qué se hizo. | Sí: la auditoría responde qué. | — | crear, modificar o cambiar_estado. | RF-18 |
| tabla_afectada | Sobre qué concepto se hizo el cambio. | Sí: sin ella no se ubica el registro. | — | Nombre de una tabla del modelo. | RF-18 |
| registro_id | Identificador de la fila afectada. | Sí: señala el registro exacto. | — | No es FK porque puede apuntar a distintas tablas. | RF-18 |
| detalle | Descripción del cambio: valor anterior y nuevo. | No: no todas las acciones lo necesitan. | — | Texto libre. | RF-18 |
