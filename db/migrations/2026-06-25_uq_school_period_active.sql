-- =============================================================================
-- Migración: un solo periodo escolar ACTIVO a la vez
-- Base real: MySQL
-- Fecha: 2026-06-25
--
-- Garantiza en la base de datos que a lo más exista un school_period con
-- period_status = 'Active', mediante una columna generada que vale 1 solo para
-- el periodo activo (NULL en cualquier otro caso) y un índice UNIQUE sobre ella.
-- =============================================================================

-- PASO 1 (OBLIGATORIO): comprobar que no haya ya más de un periodo activo.
-- Si devuelve una fila con activos > 1, concluir los sobrantes ANTES del ALTER.
SELECT COUNT(*) AS activos
FROM school_period
WHERE period_status = 'Active'
HAVING COUNT(*) > 1;

-- PASO 2: columna generada + índice único.
ALTER TABLE school_period
    ADD COLUMN active_period_guard INT
        GENERATED ALWAYS AS (CASE WHEN period_status = 'Active' THEN 1 END) VIRTUAL,
    ADD CONSTRAINT uq_school_period_active UNIQUE (active_period_guard);

-- =============================================================================
-- ROLLBACK:
-- ALTER TABLE school_period DROP INDEX uq_school_period_active;
-- ALTER TABLE school_period DROP COLUMN active_period_guard;
-- =============================================================================
