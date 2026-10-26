-- Google puede devolver URLs de avatar de mas de 1000 caracteres.
-- Ampliacion sin truncar los datos existentes; ejecutar una vez por entorno.
BEGIN;
SET LOCAL lock_timeout = '5s';
ALTER TABLE public.usuario ALTER COLUMN avatar_url TYPE text;
COMMIT;