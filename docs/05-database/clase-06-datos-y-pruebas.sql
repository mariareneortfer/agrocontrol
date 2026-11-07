-- =====================================================================
-- AgroControl - Clase 06: datos minimos y pruebas de integridad
--
-- Este archivo NO es una migracion. Es el laboratorio de la clase.
-- Requisito: haber ejecutado antes V1__init_core.sql en la base agrocontrol.
--
-- Como usarlo en DataGrip:
--   - Parte A: seleccionar todo el bloque y ejecutarlo.
--   - Parte B: ejecutar UNA sentencia por vez. Cada una DEBE fallar.
--   - Parte C: ejecutar cada consulta y observar el resultado.
-- =====================================================================

-- Las tablas estan en el esquema agrocontrol.
SET search_path TO agrocontrol, public;


-- ---------------------------------------------------------------------
-- PARTE A - Dataset minimo valido (todo esto debe funcionar)
-- ---------------------------------------------------------------------

INSERT INTO predio (nombre, ubicacion)
VALUES ('San Isidro', 'Km 12 carretera al norte');

INSERT INTO parcela (predio_id, codigo, nombre, superficie, unidad_superficie)
VALUES
    ((SELECT predio_id FROM predio WHERE nombre = 'San Isidro'),
     'P-01', 'Lote Norte', 4.50, 'hectarea'),
    ((SELECT predio_id FROM predio WHERE nombre = 'San Isidro'),
     'P-02', 'Lote Sur', 3.25, 'hectarea');

INSERT INTO cultivo (nombre, variedad)
VALUES ('Maiz', 'Amarillo duro');

INSERT INTO campana (cultivo_id, nombre, fecha_inicio, fecha_fin_prevista)
VALUES
    ((SELECT cultivo_id FROM cultivo WHERE nombre = 'Maiz'),
     'Maiz verano 2026', DATE '2026-10-01', DATE '2027-02-28');

-- La campana abarca las dos parcelas.
INSERT INTO campana_parcela (campana_id, parcela_id)
SELECT c.campana_id, p.parcela_id
FROM campana c
CROSS JOIN parcela p
WHERE c.nombre = 'Maiz verano 2026'
  AND p.codigo IN ('P-01', 'P-02');

-- Una labor de riego en la parcela P-01 de esa campana.
INSERT INTO labor (campana_parcela_id, tipo_labor, fecha_programada)
SELECT cp.campana_parcela_id, 'riego', DATE '2026-10-12'
FROM campana_parcela cp
JOIN campana c ON c.campana_id = cp.campana_id
JOIN parcela p ON p.parcela_id = cp.parcela_id
WHERE c.nombre = 'Maiz verano 2026'
  AND p.codigo = 'P-01';

-- Revision de lo insertado.
SELECT * FROM predio;
SELECT * FROM parcela;
SELECT * FROM cultivo;
SELECT * FROM campana;
SELECT * FROM campana_parcela;
SELECT * FROM labor;

-- Unicidad contextual: el mismo codigo P-01 SI se permite en otro predio.
INSERT INTO predio (nombre) VALUES ('La Esperanza');

INSERT INTO parcela (predio_id, codigo, nombre, superficie, unidad_superficie)
VALUES
    ((SELECT predio_id FROM predio WHERE nombre = 'La Esperanza'),
     'P-01', 'Lote Unico', 2.00, 'hectarea');


-- ---------------------------------------------------------------------
-- PARTE B - Pruebas de violacion (cada sentencia DEBE fallar)
-- Ejecutar de una en una y leer el mensaje de PostgreSQL.
-- ---------------------------------------------------------------------

-- B1. NOT NULL: parcela sin nombre.
-- Esperado: error de valor nulo en la columna "nombre".
INSERT INTO parcela (predio_id, codigo, superficie, unidad_superficie)
VALUES
    ((SELECT predio_id FROM predio WHERE nombre = 'San Isidro'),
     'P-03', 1.00, 'hectarea');

-- B2. UNIQUE: repetir el codigo P-01 dentro del mismo predio.
-- Esperado: viola uq_parcela_predio_codigo.
INSERT INTO parcela (predio_id, codigo, nombre, superficie, unidad_superficie)
VALUES
    ((SELECT predio_id FROM predio WHERE nombre = 'San Isidro'),
     'P-01', 'Lote repetido', 1.00, 'hectarea');

-- B3. FOREIGN KEY: unir la campana con una parcela que no existe.
-- Esperado: viola fk_campana_parcela_parcela.
INSERT INTO campana_parcela (campana_id, parcela_id)
VALUES
    ((SELECT campana_id FROM campana WHERE nombre = 'Maiz verano 2026'), 9999);

-- B4. CHECK de estado: un estado que no existe en RN-03.
-- Esperado: viola ck_labor_estado.
INSERT INTO labor (campana_parcela_id, tipo_labor, fecha_programada, estado)
VALUES
    ((SELECT MIN(campana_parcela_id) FROM campana_parcela),
     'siembra', DATE '2026-10-05', 'terminada');

-- B5. CHECK de fechas: campana que termina antes de empezar.
-- Esperado: viola ck_campana_fechas.
INSERT INTO campana (cultivo_id, nombre, fecha_inicio, fecha_fin_prevista)
VALUES
    ((SELECT cultivo_id FROM cultivo WHERE nombre = 'Maiz'),
     'Campana imposible', DATE '2027-06-01', DATE '2027-01-01');

-- B6. CHECK de cantidad: parcela con superficie cero.
-- Esperado: viola ck_parcela_superficie_positiva.
INSERT INTO parcela (predio_id, codigo, nombre, superficie, unidad_superficie)
VALUES
    ((SELECT predio_id FROM predio WHERE nombre = 'San Isidro'),
     'P-04', 'Lote sin superficie', 0, 'hectarea');

-- B7. UNIQUE en la tabla puente: la misma parcela dos veces en la campana.
-- Esperado: viola uq_campana_parcela_campana_parcela.
INSERT INTO campana_parcela (campana_id, parcela_id)
SELECT c.campana_id, p.parcela_id
FROM campana c
JOIN parcela p ON p.codigo = 'P-02'
JOIN predio d ON d.predio_id = p.predio_id AND d.nombre = 'San Isidro'
WHERE c.nombre = 'Maiz verano 2026';


-- ---------------------------------------------------------------------
-- PARTE C - Inspeccion de la estructura
-- ---------------------------------------------------------------------

SELECT table_schema, table_name
FROM information_schema.tables
WHERE table_schema = 'agrocontrol'
ORDER BY table_name;

SELECT table_name, column_name, data_type, is_nullable
FROM information_schema.columns
WHERE table_schema = 'agrocontrol'
ORDER BY table_name, ordinal_position;

-- Lista de constraints por tabla (p = PK, f = FK, u = UNIQUE, c = CHECK).
SELECT rel.relname AS tabla, con.conname AS restriccion, con.contype AS tipo
FROM pg_constraint con
JOIN pg_class rel ON rel.oid = con.conrelid
JOIN pg_namespace nsp ON nsp.oid = rel.relnamespace
WHERE nsp.nspname = 'agrocontrol'
ORDER BY rel.relname, con.contype, con.conname;
