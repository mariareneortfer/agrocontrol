# Modelo conceptual v0.1 — AgroControl

Gestión de Lotes Agrícolas, Campañas, Labores, Insumos y Cosecha

## 1. Objetivo
Este documento identifica los conceptos principales del dominio de AgroControl, sus atributos conceptuales, relaciones, cardinalidades y primeras reglas de integridad. Todavía no define tablas, tipos de datos, claves foráneas ni entidades JPA.

> Cambio respecto a la primera versión: el equipo confirmó que una campaña abarca varias parcelas. La relación campaña — parcela pasó de 1:N a N:M.

## 2. Fuente analizada
- Proyecto asignado: 16. AgroControl (Banco Oficial de Proyectos II, Programación Aplicada 2026-2).
- Secciones revisadas: A, B, C, D, E, F, G y J de la ficha oficial.
- Apoyo secundario: secciones I y K, sólo para confirmar qué registra el operario móvil y si una campaña admite varias cosechas.

## 3. Candidatos analizados

| Concepto | Clasificación | Justificación | Fuente |
|---|---|---|---|
| Predio | Entidad | Existe físicamente, se distingue de otros predios y agrupa parcelas. | Sección D, F, RF-01 |
| Parcela | Entidad | Es el lote agrícola sobre el que se pide la trazabilidad; tiene identidad, superficie e historial propio. | Sección A, F, RN-01, RF-15 |
| Cultivo | Entidad (catálogo) | Se gestiona por separado y se reutiliza en muchas campañas. | Sección D, F, RF-02 |
| Campaña | Entidad | Tiene período, estado, cultivo y una o varias parcelas; concentra labores, cosechas e historial que no se elimina. | Sección F, RN-01, RN-06, RN-09, RF-03 |
| Labor | Entidad | Trabajo de campo planificado con identidad, fechas y ciclo de estados. | Sección F, RN-02, RN-03, RF-04, RF-07 |
| AsignacionLabor | Entidad | Registra qué operario fue asignado a qué labor, cuándo y por quién; permite más de un operario y reasignaciones. | Sección F, RN-05, RF-05 |
| Insumo | Entidad | Producto de almacén con identidad, unidad de medida y stock. | Sección F, RN-04, RF-09 |
| MovimientoInsumo | Entidad (evento) | Cada entrada o salida debe conservarse con cantidad, fecha y responsable. | Sección F, RF-10 |
| ConsumoLabor | Entidad | Une una labor con un insumo y guarda la cantidad usada; la relación tiene datos propios. | Sección F, RN-04, RF-11, RF-12 |
| BitacoraCampo | Entidad | Entrada de bitácora (observación de campo) con fecha, autor y texto propios. | Sección F, RF-08, RF-15 |
| Incidencia | Entidad | Hecho excepcional que debe quedar registrado y vinculado a parcela, campaña o labor. | Sección F, RN-08, RF-13 |
| Cosecha | Entidad | Registro de producción con cantidad, unidad y fecha, asociado a una campaña. | Sección F, RN-06, RF-14 |
| Usuario | Entidad | Persona con cuenta; se necesita saber quién hizo qué y cuándo. | Sección C, F, RF-18 |
| Rol | Entidad (catálogo) | Determina qué puede hacer cada usuario. | Sección C, F |
| Auditoria | Entidad (registro transversal) | Guarda los cambios críticos con usuario y fecha. | Sección F, RF-18 |
| Administrador, Jefe de campo, Operario, Almacenero, Supervisor | Actor / rol | Son responsabilidades frente al sistema. Se representan como valores de Rol asignados a un Usuario, no como cinco entidades separadas. | Sección C |
| Responsable | Papel dentro de una relación | No es una cosa distinta: es el Usuario que planifica, asigna, registra o reporta. | Sección A |
| Planificada, asignada, en ejecución, completada, cancelada | Estado (de Labor) | No existen por sí solos; son situaciones por las que pasa una labor. | RN-03 |
| Activa, finalizada | Estado (de Campaña) | Condición de la campaña que habilita o impide registrar cosecha y que protege el historial. | RN-06, RN-09 |
| Entrada, salida | Atributo (tipo de MovimientoInsumo) | Sólo describen la dirección del movimiento. | RF-10 |
| Cantidad, unidad de medida | Atributo | Describen a un consumo, un movimiento, una cosecha o una superficie; no tienen vida propia. | RN-07 |
| Stock disponible | Atributo de Insumo o resultado derivado | Es un dato del insumo que cambia con los movimientos. Ver D-07. | RN-04, RF-12 |
| Agenda de campo | Resultado derivado | Es una consulta de las labores asignadas a un operario. | RF-06 |
| Bitácora por parcela | Resultado derivado | Es la vista cronológica de lo ocurrido en una parcela. Ver D-04. | RF-15, sección B |
| Avance por campaña, KPIs, Dashboard | Resultado derivado | Se calculan a partir de labores, consumos, cosechas e incidencias. | RF-16, RF-17 |
| Unidad productiva | Contexto (duda) | La ficha describe un solo cliente; no se modela como entidad. Ver D-09. | Sección A |

## 4. Entidades núcleo v0.1

### Predio
Responsabilidad: representar una propiedad agrícola que agrupa parcelas.
Atributos conceptuales: nombre, ubicación, descripción (opcional).
Identificador de negocio candidato: nombre del predio.

### Parcela
Responsabilidad: representar el lote agrícola donde se desarrollan campañas y sobre el que se consulta la bitácora.
Atributos conceptuales: código, nombre, superficie, unidad de la superficie, ubicación referencial (opcional).
Identificador de negocio candidato: código de parcela dentro de su predio.

### Cultivo
Responsabilidad: catálogo de lo que se puede sembrar en una campaña.
Atributos conceptuales: nombre, variedad (opcional), descripción (opcional).
Identificador de negocio candidato: nombre + variedad.

### Campaña
Responsabilidad: representar un ciclo productivo de un cultivo sobre una o varias parcelas durante un período.
Atributos conceptuales: nombre, fechaInicio, fechaFinPrevista, fechaCierre (opcional), estado.
Identificador de negocio candidato: nombre de la campaña + fechaInicio.
Nota: la ficha la escribe "Campana" en la sección F; en este documento se usa el término del dominio, "Campaña".

### Labor
Responsabilidad: representar un trabajo de campo planificado en una parcela concreta de una campaña, y su avance.
Atributos conceptuales: tipo de labor, descripción, fechaProgramada, fechaInicioReal (opcional), fechaFinReal (opcional), estado.
Identificador de negocio candidato: campaña + parcela + tipo de labor + fechaProgramada.

### AsignacionLabor
Responsabilidad: registrar que un operario fue designado para ejecutar una labor.
Atributos conceptuales: fechaAsignacion, operario asignado, usuario que asignó.
Identificador de negocio candidato: labor + operario + fechaAsignacion.

### Insumo
Responsabilidad: representar un producto de almacén que se usa en las labores.
Atributos conceptuales: código, nombre, categoría, unidadMedida, stockDisponible.
Identificador de negocio candidato: código de insumo.

### MovimientoInsumo
Responsabilidad: registrar cada entrada o salida de un insumo del almacén.
Atributos conceptuales: tipo (entrada o salida), cantidad, unidadMedida, fecha, motivo (opcional), usuario que registró.
Identificador de negocio candidato: insumo + fecha y hora + tipo.

### ConsumoLabor
Responsabilidad: registrar cuánto de un insumo se usó en una labor.
Atributos conceptuales: cantidad, unidadMedida, fecha, usuario que registró.
Identificador de negocio candidato: labor + insumo + fecha y hora.

### BitacoraCampo
Responsabilidad: guardar una observación de campo dentro de la bitácora de una parcela.
Atributos conceptuales: fecha y hora, descripción, autor.
Identificador de negocio candidato: parcela + fecha y hora + autor.

### Incidencia
Responsabilidad: dejar constancia de un hecho excepcional ocurrido en campo.
Atributos conceptuales: fecha, descripción, clasificación (opcional), usuario que reportó.
Identificador de negocio candidato: parcela + fecha y hora + usuario que reportó.

### Cosecha
Responsabilidad: registrar la producción obtenida en una campaña.
Atributos conceptuales: fecha, cantidad, unidadMedida, observación (opcional), usuario que registró.
Identificador de negocio candidato: campaña + fecha.

### Usuario
Responsabilidad: identificar a la persona que opera el sistema.
Atributos conceptuales: nombre completo, correo, estado (activo o inactivo).
Identificador de negocio candidato: correo.

### Rol
Responsabilidad: definir la responsabilidad de un usuario (Administrador, Jefe de campo, Operario, Almacenero, Supervisor).
Atributos conceptuales: nombre, descripción.
Identificador de negocio candidato: nombre del rol.

### Auditoria
Responsabilidad: conservar quién hizo qué cambio crítico y cuándo.
Atributos conceptuales: fecha y hora, usuario, acción, concepto afectado, referencia al registro afectado, detalle.
Identificador de negocio candidato: ninguno natural; se decidirá en Clase 03.

## 5. Relaciones
- Un Predio puede tener muchas Parcelas. Cada Parcela pertenece a un único Predio.
- Una Parcela puede participar en muchas Campañas a lo largo del tiempo. Cada Campaña se desarrolla sobre una o varias Parcelas.
- Un Cultivo puede sembrarse en muchas Campañas. Cada Campaña trabaja un único Cultivo.
- Una Campaña puede tener muchas Labores. Cada Labor pertenece a una única Campaña.
- Una Parcela puede tener muchas Labores. Cada Labor se realiza en una única Parcela, que debe ser una de las parcelas de su Campaña.
- Una Labor puede tener varias Asignaciones. Cada AsignacionLabor corresponde a una única Labor.
- Un Usuario con rol Operario puede recibir muchas Asignaciones. Cada AsignacionLabor designa a un único Operario.
- Una Labor puede registrar varios Consumos. Cada ConsumoLabor corresponde a una única Labor.
- Un Insumo puede aparecer en muchos Consumos. Cada ConsumoLabor se refiere a un único Insumo.
- Un Insumo puede tener muchos Movimientos. Cada MovimientoInsumo afecta a un único Insumo.
- Un ConsumoLabor se respalda con una salida de almacén. Un MovimientoInsumo de tipo salida puede corresponder a un ConsumoLabor; las entradas no corresponden a ninguno.
- Una Parcela puede tener muchas entradas de BitacoraCampo. Cada entrada pertenece a una única Parcela y puede referirse además a una Campaña y a una Labor.
- Una Parcela puede tener muchas Incidencias. Cada Incidencia se vincula a una Parcela y puede referirse además a una Campaña y a una Labor.
- Una Campaña puede tener varias Cosechas. Cada Cosecha se registra contra una única Campaña.
- Un Rol puede estar asignado a muchos Usuarios. Cada Usuario tiene un Rol.
- Un Usuario puede originar muchos registros de Auditoria. Cada registro de Auditoria corresponde a un único Usuario.
- Un Usuario registra movimientos, consumos, entradas de bitácora, incidencias y cosechas. Cada uno de esos registros tiene un único Usuario responsable.

## 6. Cardinalidades

Lectura de las columnas: "Para una A" indica cuántas B puede tener una A; "Para una B" indica cuántas A puede tener una B.

| Relación (A — B) | Cardinalidad | Para una A | Para una B | Justificación |
|---|---|---|---|---|
| Predio — Parcela | 1:N | 0..* | 1 | Un predio recién creado aún no tiene parcelas; una parcela no existe sin predio (RF-01). |
| Parcela — Campaña | N:M | 0..* | 1..* | RN-01: la parcela participa en campañas distintas en el tiempo. Una campaña abarca una o varias parcelas (D-01, confirmado por el equipo). |
| Cultivo — Campaña | 1:N | 0..* | 1 | Un cultivo del catálogo puede no haberse usado aún; una campaña necesita saber qué se cultiva. Ver D-02. |
| Campaña — Labor | 1:N | 0..* | 1 | RN-02: toda labor pertenece a una campaña. Una campaña recién creada todavía no tiene labores. |
| Parcela — Labor | 1:N | 0..* | 1 | RN-02: toda labor pertenece a una campaña y parcela. |
| Labor — AsignacionLabor | 1:N | 0..* | 1 | Una labor planificada aún no tiene asignación; RF-05 habla de "asignar operarios" en plural. |
| Usuario (Operario) — AsignacionLabor | 1:N | 0..* | 1 | Un operario puede no tener labores; cada asignación nombra a un operario. |
| Labor — Operario | N:M | 0..* | 0..* | Es la relación que resuelve AsignacionLabor: una labor con varios operarios y un operario con varias labores. |
| Labor — ConsumoLabor | 1:N | 0..* | 1 | No toda labor usa insumos; RF-11 asocia cada consumo a una labor. |
| Insumo — ConsumoLabor | 1:N | 0..* | 1 | Un insumo puede no haberse usado aún; cada consumo es de un insumo. |
| Labor — Insumo | N:M | 0..* | 0..* | Es la relación que resuelve ConsumoLabor, que además guarda la cantidad. |
| Insumo — MovimientoInsumo | 1:N | 0..* | 1 | RF-10: entradas y salidas por insumo. |
| ConsumoLabor — MovimientoInsumo | 1:1 | 1 | 0..1 | Cada consumo descuenta stock mediante una salida; una entrada no tiene consumo. Ver D-03. |
| Parcela — BitacoraCampo | 1:N | 0..* | 1 | RF-15: la bitácora se consulta por parcela. |
| Campaña — BitacoraCampo | 1:N | 0..* | 0..1 | La observación puede hacerse dentro de una campaña o fuera de ella. Ver D-04. |
| Labor — BitacoraCampo | 1:N | 0..* | 0..1 | El operario puede anotar una observación al ejecutar una labor (RF-08). |
| Parcela — Incidencia | 1:N | 0..* | 1 | RN-08. Ver D-05. |
| Campaña — Incidencia | 1:N | 0..* | 0..1 | RN-08. |
| Labor — Incidencia | 1:N | 0..* | 0..1 | RN-08. |
| Campaña — Cosecha | 1:N | 0..* | 1 | RN-06: la cosecha se registra contra una campaña; puede haber cosechas parciales. |
| Rol — Usuario | 1:N | 0..* | 1 | Sección C asigna una responsabilidad por actor. Ver D-06. |
| Usuario — Auditoria | 1:N | 0..* | 1 | RF-18: debe saberse quién hizo el cambio. |

## 7. Reglas iniciales de integridad
- RI-01. Toda parcela pertenece a exactamente un predio. (RF-01)
- RI-02. Toda campaña se define sobre al menos una parcela, con un cultivo y una fecha de inicio. (Flujo J, RF-03)
- RI-03. Dos campañas que comparten una parcela no pueden tener períodos superpuestos. (RN-01; ver D-08)
- RI-04. Toda labor pertenece a una campaña y a una parcela; esa parcela debe ser una de las parcelas de la campaña. (RN-02)
- RI-05. El estado de una labor sólo puede ser planificada, asignada, en ejecución, completada o cancelada. (RN-03)
- RI-06. Una labor sólo puede estar en estado asignada o posterior si tiene al menos una asignación. (RN-03, RF-05)
- RI-07. Sólo un usuario con rol Operario puede recibir una asignación, y sólo un operario asignado a la labor puede iniciarla, completarla o reportar sobre ella. (RN-05, RF-07)
- RI-08. La cantidad de un consumo o de una salida no puede superar el stock disponible del insumo en ese momento. (RN-04, RF-12)
- RI-09. Toda cantidad (consumo, movimiento, cosecha, superficie) es mayor que cero y lleva su unidad de medida explícita; el consumo de un insumo se expresa en la unidad de ese insumo. (RN-07)
- RI-10. Una cosecha sólo puede registrarse contra una campaña activa o finalizable. (RN-06)
- RI-11. Toda incidencia queda vinculada a una parcela; si además indica campaña o labor, estas deben corresponder a esa misma parcela. (RN-08)
- RI-12. Una campaña finalizada y todo lo que cuelga de ella (labores, consumos, bitácora, incidencias, cosechas) no se elimina. (RN-09)
- RI-13. Todo movimiento, consumo, entrada de bitácora, incidencia y cosecha registra el usuario responsable y la fecha. (RF-18, criterio L de trazabilidad)
- RI-14. Los cambios críticos generan un registro de auditoría que no se modifica ni elimina. (RF-18)

RN-10 (la IA no prescribe dosis ni tratamientos) no produce una regla de integridad de datos: limita una funcionalidad, no la estructura del modelo.

## 8. Dudas y decisiones
- D-01. ¿Una campaña abarca una sola parcela o varias? Resuelta: el equipo confirmó que una campaña abarca varias parcelas. La relación Campaña — Parcela es N:M y cada labor indica en cuál de las parcelas de la campaña se realiza, como dice RN-02 ("a una campaña y parcela"). Alternativa descartada: una sola parcela por campaña, que fue el supuesto inicial.
- D-02. La ficha no dice explícitamente que cada campaña tenga un único cultivo. Decisión v0.1: un cultivo por campaña. Pendiente de confirmar.
- D-03. ¿La salida que registra el almacenero y el consumo que reporta el operario son el mismo hecho o dos hechos distintos (entregado frente a realmente usado)? Decisión v0.1: cada consumo se respalda con una salida y lo entregado se considera consumido; una devolución sería una entrada. Pendiente de confirmar.
- D-04. ¿BitacoraCampo guarda sólo observaciones de campo, o una entrada por cada evento (labor completada, consumo, incidencia, cosecha)? Decisión v0.1: guarda las observaciones (RF-08) y la bitácora por parcela (RF-15) se arma como consulta que reúne observaciones, labores, consumos, incidencias y cosechas. Pendiente de confirmar.
- D-05. En RN-08, "parcela/campaña/labor" ¿significa las tres obligatorias o al menos una? Decisión v0.1: parcela obligatoria; campaña y labor opcionales.
- D-06. ¿Un usuario puede tener más de un rol (por ejemplo, jefe de campo que también es supervisor)? Decisión v0.1: un rol por usuario. Si se permite más de uno, la relación pasa a N:M.
- D-07. ¿El stock disponible se guarda como dato del insumo o se calcula sumando entradas y restando salidas? Se decide en Clase 03; conceptualmente es un dato del insumo que sólo cambia mediante movimientos.
- D-08. RN-01 condiciona la no superposición a "si la regla de ocupación lo impide". La ficha no define esa regla. Decisión v0.1: no se permite superposición en ningún caso.
- D-09. ¿El sistema atiende una sola unidad productiva o varias empresas? Decisión v0.1: una sola; no se modela una entidad para la empresa.
- D-10. La ficha fija los estados de la labor pero no los de la campaña ni las transiciones permitidas. Propuesta v0.1 para campaña: activa y finalizada (los únicos que nombra la ficha); queda pendiente si hacen falta planificada y cancelada. Propuesta de transiciones de labor: planificada → asignada → en ejecución → completada, con cancelación posible antes de completarse.
- D-11. ¿El tipo de labor (siembra, riego, fumigación, etc.) y la unidad de medida son texto libre o catálogos administrados? Decisión v0.1: atributos. Se revisa en Clase 03.
- D-12. Usuario se mantiene como entidad a pesar de que los actores no lo son automáticamente, porque RN-05, RF-05 y RF-18 exigen identificar a la persona concreta que fue asignada o que hizo un cambio.

## 9. Trazabilidad inicial

| Concepto/relación | RN/RF asociado |
|---|---|
| Predio, Parcela, Predio — Parcela | RF-01 |
| Cultivo | RF-02 |
| Campaña, Parcela — Campaña | RN-01, RN-09, RF-03, RF-16 |
| Labor, Campaña — Labor, Parcela — Labor | RN-02, RN-03, RF-04, RF-07 |
| AsignacionLabor, Labor — Operario | RN-05, RF-05, RF-06 |
| Insumo | RN-04, RN-07, RF-09 |
| MovimientoInsumo | RF-10 |
| ConsumoLabor, Labor — Insumo | RN-04, RN-07, RF-11, RF-12 |
| BitacoraCampo | RF-08, RF-15 |
| Incidencia | RN-08, RF-13 |
| Cosecha, Campaña — Cosecha | RN-06, RN-07, RF-14 |
| Usuario, Rol | Sección C, RN-05 |
| Auditoria | RF-18 |
| Dashboard, KPIs (derivados) | RF-16, RF-17 |

## 10. Verificación con el flujo crítico

| Paso del flujo (sección J) | Entidad(es) involucradas | Relación/regla que lo soporta |
|---|---|---|
| Jefe crea campaña sobre parcela | Usuario, Parcela, Cultivo, Campaña | Parcela — Campaña, Cultivo — Campaña; RI-02, RI-03 |
| Planifica labores | Campaña, Labor | Campaña — Labor; RI-04; la labor nace en estado planificada (RI-05) |
| Asigna operario | Labor, AsignacionLabor, Usuario | Labor — AsignacionLabor — Operario; RI-06, RI-07; la labor pasa a asignada |
| Almacén entrega insumo | Insumo, MovimientoInsumo | Insumo — MovimientoInsumo; RI-08, RI-09 |
| Operario ejecuta y reporta desde móvil | Labor, ConsumoLabor, Usuario | Labor — ConsumoLabor — Insumo; RI-07, RI-08; la labor pasa a en ejecución y luego a completada |
| Se actualiza bitácora | BitacoraCampo, Parcela, Campaña, Labor | Parcela — BitacoraCampo; RI-13; D-04 |
| Se registra cosecha | Campaña, Cosecha | Campaña — Cosecha; RI-09, RI-10 |
| Supervisor revisa indicadores | Campaña, Labor, ConsumoLabor, Incidencia, Cosecha | Resultados derivados; no requieren entidad nueva |

Resultado: el flujo puede recorrerse completo con las entidades y relaciones de la versión 0.1. Los puntos frágiles son D-03 (entrega frente a consumo) y D-04 (cómo se construye la bitácora).

## 11. Pendientes para Clase 03
- Confirmar con el docente D-03 y D-04, que son las dudas que más cambian el modelo.
- Revisar identificadores de negocio.
- Transformar el modelo conceptual en modelo relacional.
- Definir PK, FK y opcionalidad física.
- Resolver las relaciones N:M (Campaña — Parcela, Labor — Operario y Labor — Insumo).
- Decidir si el stock se guarda o se calcula (D-07).
- Revisar normalización inicial.
