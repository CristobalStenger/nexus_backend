-- ============================================================
-- NexusTech - Preparación de la base de datos (Neon / PostgreSQL)
-- Ejecutar UNA vez como dueño de la base (neondb_owner en Neon),
-- desde el SQL Editor de Neon o con psql.
--
-- Cada microservicio tiene su propio schema y su propio rol:
--   usuario-service  -> schema usuario  (rol usuario_app)
--   producto-service -> schema producto (rol producto_app)
--   pedido-service   -> schema pedido   (rol pedido_app)
-- Un rol solo puede usar su schema, así ningún servicio lee
-- ni modifica las tablas de otro.
--
-- Las tablas NO se crean aquí: cada servicio las crea al arrancar
-- con sus migraciones Flyway (src/main/resources/db/migration).
-- ============================================================

-- 1) Roles de conexión. Cambia las contraseñas por unas largas y aleatorias
--    (Neon rechaza las débiles; sirve SELECT gen_random_uuid();).
--    Créalos con este SQL, no desde la consola de Neon (Roles > New role):
--    esos roles quedan en neon_superuser y podrían leer los schemas de los demás.
CREATE ROLE usuario_app  LOGIN PASSWORD 'cambia-esta-clave-usuario';
CREATE ROLE producto_app LOGIN PASSWORD 'cambia-esta-clave-producto';
CREATE ROLE pedido_app   LOGIN PASSWORD 'cambia-esta-clave-pedido';

-- 2) Un schema por microservicio
CREATE SCHEMA IF NOT EXISTS usuario;
CREATE SCHEMA IF NOT EXISTS producto;
CREATE SCHEMA IF NOT EXISTS pedido;

-- 3) Cada rol puede crear y usar tablas solo en su schema
GRANT USAGE, CREATE ON SCHEMA usuario  TO usuario_app;
GRANT USAGE, CREATE ON SCHEMA producto TO producto_app;
GRANT USAGE, CREATE ON SCHEMA pedido   TO pedido_app;
