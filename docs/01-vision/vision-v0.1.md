# Visión v0.1 — AgroControl

## 1. Contexto

Una unidad productiva agrícola gestiona varios predios divididos en parcelas, sobre
las cuales se ejecutan campañas de cultivo con labores planificadas, insumos
consumidos y cosechas registradas. Hoy esa información vive en registros manuales
dispersos.

## 2. Problema

Los registros manuales impiden saber con certeza qué labores se ejecutaron sobre cada
parcela, quién fue el responsable, qué insumos se consumieron y en qué cantidades, y
cuál es el avance real de cada campaña. No existe una bitácora confiable ni
trazabilidad operativa por lote agrícola.

## 3. Objetivo

Centralizar la operación de campo y mantener una bitácora trazable por parcela y
campaña, desde la planificación de labores hasta el registro de la cosecha.

## 4. Actores

### Administrador
Configura predios, parcelas, usuarios y catálogos del sistema.

### Jefe de campo
Crea campañas, planifica labores y asigna operarios.

### Operario
Consulta las labores que tiene asignadas, las inicia y completa, registra consumos,
observaciones e incidencias desde la aplicación móvil.

### Almacenero
Registra entradas y salidas de insumos y entrega insumos para las labores.

### Supervisor
Consulta el avance de campañas, la bitácora y los indicadores operativos.

## 5. Alcance del MVP

1. Gestión de predios y parcelas.
2. Gestión de cultivos.
3. Creación y seguimiento de campañas.
4. Planificación de labores y sus estados.
5. Asignación de labores a operarios.
6. Gestión de insumos y movimientos de stock.
7. Registro de consumo de insumos asociado a labores, validando disponibilidad.
8. Bitácora de campo por parcela.
9. Registro de incidencias.
10. Registro de cosecha por campaña.
11. Dashboard e indicadores operativos.

## 6. Exclusiones

No incluye recomendaciones agronómicas ni cálculo de dosis, sensores IoT, estaciones
meteorológicas, imágenes satelitales o drones, GPS en tiempo real, contabilidad
formal, facturación fiscal, pasarela bancaria real, gestión de nómina, ni decisiones
automáticas de negocio mediante IA.

## 7. Éxito inicial del proyecto

El proyecto será exitoso cuando pueda demostrar, con datos persistidos, un flujo
coherente desde la creación de una campaña sobre una parcela hasta la ejecución
reportada de una labor con consumo de insumos y el registro de la cosecha, consumido
por web y móvil sobre el mismo backend.