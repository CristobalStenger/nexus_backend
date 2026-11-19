-- Boletas de ejemplo (las mismas órdenes del frontend) para el dashboard y los reportes
INSERT INTO boleta (numero, fecha, estado, motivo, usuario_id, cliente_run, cliente_nombre, cliente_apellidos, cliente_correo,
                    entrega_tipo, entrega_calle, entrega_departamento, entrega_region, entrega_comuna, entrega_indicaciones,
                    pago_tarjeta, cupon_codigo, cupon_descuento, subtotal, descuento, envio, total) VALUES
    (1001, '2026-09-14T15:20:00.000Z', 'pagada', '', 3, '21456789K', 'Pedro', 'Soto Fuentes', 'cliente@gmail.com', 'despacho', 'Los Crisantemos 789, Edificio Norte', 'Depto 603', 'Región Metropolitana', 'Providencia', 'Si no estoy, dejar con el conserje.', '**** 4242', NULL, NULL, 529970, 0, 3990, 533960),
    (1002, '2026-09-21T19:05:00.000Z', 'pagada', '', NULL, '', 'Camila', 'Herrera Díaz', 'camila.herrera@gmail.com', 'despacho', 'Av. Libertad 1020', '', 'Región de Valparaíso', 'Viña del Mar', '', '**** 4242', 'FELIZ18', 10, 602483, 60248, 3990, 546225),
    (1003, '2026-09-28T13:45:00.000Z', 'rechazada', 'La tarjeta fue rechazada por el banco emisor.', 3, '21456789K', 'Pedro', 'Soto Fuentes', 'cliente@gmail.com', 'retiro', 'Los Crisantemos 789, Edificio Norte', 'Depto 603', 'Región Metropolitana', 'Providencia', 'Si no estoy, dejar con el conserje.', '**** 0000', NULL, NULL, 48986, 0, 0, 48986),
    (1004, '2026-09-28T13:52:00.000Z', 'pagada', '', 3, '21456789K', 'Pedro', 'Soto Fuentes', 'cliente@gmail.com', 'retiro', 'Los Crisantemos 789, Edificio Norte', 'Depto 603', 'Región Metropolitana', 'Providencia', 'Si no estoy, dejar con el conserje.', '**** 4242', NULL, NULL, 198976, 0, 0, 198976),
    (1005, '2026-10-03T22:10:00.000Z', 'pagada', '', NULL, '', 'Ignacio', 'Morales Vera', 'ignacio.morales@duoc.cl', 'despacho', 'Pasaje Los Olmos 55', '', 'Región del Biobío', 'Concepción', 'Casa con reja verde.', '**** 4242', NULL, NULL, 1305972, 0, 3990, 1309962);

INSERT INTO detalle_boleta (boleta_numero, producto_id, codigo, nombre, categoria, imagen, precio, cantidad) VALUES
    (1001, 1, 'CPU001', 'AMD Ryzen 5 7600X', 'Componentes', 'https://cdn11.bigcommerce.com/s-7g81zn9sor/images/stencil/original/products/139/439/1761310-amd-ryzen-5-7000-series-PIB-angle__21045.1677596471.png', 249990, 1),
    (1001, 9, 'RAM001', 'Corsair Vengeance DDR5 32GB 6000MHz', 'Componentes', 'https://media.solotodo.com/media/products/1717480_picture_1675657811.png', 139990, 2),
    (1002, 29, 'CON001', 'Sony PlayStation 5 Slim Digital', 'Consolas', 'https://i5.walmartimages.com/asr/e6002cfc-9c71-43d2-af68-33c8ef81e7f0.5db413053bba324109830cfa411e97e7.png', 549990, 1),
    (1002, 18, 'MOU002', 'Razer DeathAdder V3', 'Periféricos', 'https://assets2.razerzone.com/images/pnx.assets/9b36c7dabe13e3f551263136d54d0632/razer-deathadder-v3-pro-500x500-v2.webp', 52493, 1),
    (1003, 21, 'TEC003', 'Redragon Kumara K552 RGB', 'Periféricos', 'https://media.spdigital.cl/thumbnails/products/7jkz7rkg_9191c3b4_thumbnail_4096.png', 24493, 2),
    (1004, 21, 'TEC003', 'Redragon Kumara K552 RGB', 'Periféricos', 'https://media.spdigital.cl/thumbnails/products/7jkz7rkg_9191c3b4_thumbnail_4096.png', 24493, 2),
    (1004, 17, 'MOU001', 'Logitech G Pro X Superlight 2', 'Periféricos', 'https://cl-cenco-pim-resizer.ecomm.cencosud.com/unsafe/adaptive-fit-in/3840x0/filters:quality(75)/prd-cl/product-medias/774323ed-a910-40a9-a4ae-dc25f53e0164/MKKSUKF5F2/MKKSUKF5F2-1/1781096423615-MKKSUKF5F2-1-0.png', 149990, 1),
    (1005, 35, 'NBG001', 'ASUS TUF Gaming F15 RTX 4060', 'Notebooks', 'https://dlcdnwebimgs.asus.com/gain/3a36a12f-9a0d-488c-bef3-05431f041b96//w184', 899990, 1),
    (1005, 24, 'MON001', 'LG UltraGear 27GP850 27 QHD 180Hz', 'Periféricos', 'https://img.evetech.co.za/repository/components/lg-ultragear-27-qhd-gaming-monitor-600px-v1-01.webp?width=600', 349990, 1),
    (1005, 28, 'WEB001', 'Logitech C920s HD Pro', 'Periféricos', 'https://www.alcaplus.cl/media/2020/08/Logitech-Webcam-C920S-Pro-HD2-pdp.png', 55992, 1);

SELECT setval(pg_get_serial_sequence('boleta', 'numero'), (SELECT MAX(numero) FROM boleta));
