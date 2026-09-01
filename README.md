# AgroControl

Sistema académico full-stack para la gestión de lotes agrícolas, campañas, labores,
insumos y cosecha.

## 1. Problema

Una unidad productiva agrícola administra sus parcelas, campañas, cultivos, labores de
campo, uso de insumos, responsables y cosechas mediante registros manuales. Esto
dificulta saber qué se hizo en cada parcela, quién lo ejecutó, qué insumos se
consumieron y en qué estado está cada campaña.

## 2. Objetivo del MVP

Construir un sistema web y móvil que permita planificar campañas sobre parcelas,
planificar y asignar labores, registrar el consumo de insumos validando stock,
capturar cosechas e incidencias, y ofrecer una bitácora completa y trazable por
parcela y campaña.

## 3. Actores principales

- Administrador
- Jefe de campo
- Operario
- Almacenero
- Supervisor

## 4. Alcance inicial

- Predios y parcelas
- Cultivos
- Campañas
- Labores y sus estados
- Asignaciones de labor a operarios
- Insumos y movimientos de insumos
- Consumo de insumos asociado a labores
- Bitácora de campo
- Cosecha
- Incidencias
- Dashboard e indicadores operativos

## 5. Fuera de alcance

- Recomendaciones agronómicas, dosis, fertilización o tratamientos fitosanitarios
- Sensores IoT, estaciones meteorológicas y automatización de riego
- Imágenes satelitales, drones y georreferenciación avanzada
- GPS en tiempo real de maquinaria o personal
- Contabilidad formal, facturación fiscal y pasarela bancaria real
- Gestión de nómina y liquidación de jornales
- IA que decida labores, dosis o tratamientos

## 6. Stack objetivo del semestre

- Backend: Java 21 + Spring Boot
- Base de datos: PostgreSQL + Flyway
- Web: React + TypeScript
- Móvil: React Native + TypeScript
- Pruebas API: Postman
- Contenedores: Docker / Docker Compose
- Versionado: Git + GitHub
- CI: GitHub Actions
- IA: Spring AI, únicamente como capacidad complementaria

## 7. Estado actual

Clase 01: comprensión del problema, alcance, lenguaje inicial del dominio y backlog
v0.1. Todavía no existe código de aplicación.

## 8. Documentación

- `docs/01-vision/vision-v0.1.md`
- `docs/01-vision/glossary-v0.1.md`
- `docs/02-requirements/backlog-v0.1.md`
- `docs/03-decisions/`

## 9. Regla de trabajo

Cada cambio importante debe ser comprensible, trazable y defendible. El repositorio es
la fuente de verdad del proyecto.