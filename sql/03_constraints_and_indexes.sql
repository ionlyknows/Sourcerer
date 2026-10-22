-- The keys, foreign keys, validation checks, and indexes are declared alongside
-- their tables in 02_create_tables.sql so table creation is repeatable and self-contained.
-- This companion step intentionally has no ALTER statements to avoid duplicate
-- constraints when a beginner reruns the setup scripts.
USE sourcerer_crusade;

SELECT TABLE_NAME, CONSTRAINT_NAME, CONSTRAINT_TYPE
FROM information_schema.TABLE_CONSTRAINTS
WHERE CONSTRAINT_SCHEMA = DATABASE()
ORDER BY TABLE_NAME, CONSTRAINT_NAME;
