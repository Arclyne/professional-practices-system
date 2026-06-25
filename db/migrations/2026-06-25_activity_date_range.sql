-- =============================================================================
-- Migración: la actividad de bitácora pasa de una fecha única a un rango
-- Base real: MySQL
-- Fecha: 2026-06-25
--
-- Reemplaza la columna activity_date por start_date y end_date. El tope de horas
-- por actividad pasa a calcularse como (end_date - start_date + 1) * 12, por lo
-- que cada actividad necesita ambos extremos del rango. Se respalda el valor
-- existente copiándolo a las dos columnas nuevas antes de eliminar la original.
-- =============================================================================

-- PASO 1: agregar las columnas nuevas (temporalmente nulas para el backfill).
ALTER TABLE activity
    ADD COLUMN start_date DATE NULL AFTER description,
    ADD COLUMN end_date   DATE NULL AFTER start_date;

-- PASO 2: respaldar la fecha actual en ambos extremos del rango.
UPDATE activity
SET start_date = activity_date,
    end_date   = activity_date
WHERE activity_date IS NOT NULL;

-- PASO 3: hacer obligatorias las columnas nuevas una vez completado el backfill.
ALTER TABLE activity
    MODIFY COLUMN start_date DATE NOT NULL,
    MODIFY COLUMN end_date   DATE NOT NULL;

-- PASO 4: eliminar la columna anterior.
ALTER TABLE activity
    DROP COLUMN activity_date;

-- =============================================================================
-- ROLLBACK:
-- ALTER TABLE activity ADD COLUMN activity_date DATE NULL AFTER description;
-- UPDATE activity SET activity_date = start_date;
-- ALTER TABLE activity MODIFY COLUMN activity_date DATE NOT NULL;
-- ALTER TABLE activity DROP COLUMN end_date, DROP COLUMN start_date;
-- =============================================================================
