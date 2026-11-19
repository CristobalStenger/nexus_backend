-- Usuarios de prueba (los mismos del frontend). Contraseñas: admin123, vende123 y cliente123
INSERT INTO usuario (id, run, nombre, apellidos, correo, clave, fecha_nacimiento, tipo, region, comuna, direccion) VALUES
    (1, '190110222', 'Admin', 'NexusTech', 'admin@duoc.cl',
     '$2a$10$BzpGH7fPAVLlzSifkE8yL.5mVlpFDRxuboYs24T8XlDeUSlVPHGMW', NULL, 'Administrador',
     'Región Metropolitana', 'Santiago', 'Av. Principal 1234'),
    (2, '123456785', 'Valentina', 'Rojas Muñoz', 'vendedor@duoc.cl',
     '$2a$10$dfncbVG.jBEFS7YUlXSFZOp3C9oiDdCsym2daoiKErT7LOPjSfd7G', '1998-04-12', 'Vendedor',
     'Región de Valparaíso', 'Viña del Mar', 'Calle Valparaíso 456'),
    (3, '21456789K', 'Pedro', 'Soto Fuentes', 'cliente@gmail.com',
     '$2a$10$o65b0UhJ8UuMkBeTbDORxu3CHE1iqzTYGDmzMrHNka5.sP4UrQFR.', '2002-09-30', 'Cliente',
     'Región Metropolitana', 'Providencia', 'Los Crisantemos 789, Edificio Norte');

SELECT setval(pg_get_serial_sequence('usuario', 'id'), (SELECT MAX(id) FROM usuario));
