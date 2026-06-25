-- =============================================================================
-- Migración: un solo usuario ACTIVO por rol único (Administrator / Coordinator)
-- Base real: MySQL
-- Fecha: 2026-06-24
--
-- Cierra la condición de carrera del patrón "SELECT y luego INSERT": la regla de
-- "un solo administrador/coordinador activo" deja de depender solo de la
-- verificación en la aplicación y queda garantizada por la base de datos.
--
-- Cómo funciona: una columna generada (virtual) que vale el rol SOLO cuando el
-- usuario está activo y su rol es Administrator o Coordinator, y NULL en cualquier
-- otro caso. Un índice UNIQUE sobre ella permite a lo más una fila activa por rol
-- (los NULL no chocan entre sí en un índice único de MySQL).
-- =============================================================================

-- -----------------------------------------------------------------------------
-- PASO 1 (OBLIGATORIO antes de migrar): comprobar que NO existan ya duplicados
-- activos. Si esta consulta devuelve filas, hay que inactivar los sobrantes a
-- mano ANTES de aplicar el ALTER, o este fallará.
-- -----------------------------------------------------------------------------
SELECT role_name, COUNT(*) AS activos
FROM user
WHERE status = 'Active'
  AND role_name IN ('Administrator', 'Coordinator')
GROUP BY role_name
HAVING COUNT(*) > 1;

-- -----------------------------------------------------------------------------
-- PASO 2: añadir la columna generada y el índice único.
-- -----------------------------------------------------------------------------
ALTER TABLE user
    ADD COLUMN active_singleton_role VARCHAR(50)
        GENERATED ALWAYS AS (
            CASE
                WHEN status = 'Active' AND role_name IN ('Administrator', 'Coordinator')
                    THEN role_name
            END
        ) VIRTUAL,
    ADD CONSTRAINT uq_user_active_singleton_role UNIQUE (active_singleton_role);

-- =============================================================================
-- ROLLBACK (si necesitas revertir):
--
-- ALTER TABLE user DROP INDEX uq_user_active_singleton_role;
-- ALTER TABLE user DROP COLUMN active_singleton_role;
-- =============================================================================
