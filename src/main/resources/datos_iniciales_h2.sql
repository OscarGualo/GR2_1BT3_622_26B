-- PoliDinner - datos iniciales para el contenedor Docker (base H2 embebida).
-- Equivale a datos_iniciales.sql (SQL Server). Lo ejecuta la aplicación al arrancar (CargaDatosIniciales)
-- cuando existe la variable POLIDINNER_DATOS_INICIALES; no hay que correrlo a mano.
-- Se puede repetir sin duplicar datos y sin reiniciar saldos ni stock ya modificados.
-- Cada sentencia termina en punto y coma; no usar punto y coma dentro de los textos.
-- ============================================================================================================
-- Corrección de productos persistentes (Paquete 3): transición de una base H2 existente (volumen Docker).
-- En una instalación limpia estas sentencias no cambian nada. Todas se pueden repetir.
-- Antes: cada ItemMenu pertenecía a un solo menú (columna menu_id). Ahora el menú ofrece ítems mediante
-- oferta_menu; menu_id queda como columna heredada que ya no se usa (no se borra).
-- ============================================================================================================
-- Columna heredada: existe en las bases anteriores; en una instalación limpia se agrega vacía para que las
-- sentencias siguientes funcionen. Es DDL: H2 confirma la transacción en curso (va primero, antes de los datos).
ALTER TABLE ItemMenu ADD COLUMN IF NOT EXISTS menu_id INTEGER;

-- 1. Una oferta por cada vínculo antiguo ítem-menú (se conservan los de menús cerrados para el historial).
--    Un ítem "Agotado" que aún tenía unidades fue un retiro manual: su oferta queda retirada.
INSERT INTO oferta_menu (menu_id, item_id, retirada, version)
SELECT i.menu_id, i.id, CASE WHEN i.estado = 'Agotado' AND i.stockActual > 0 THEN TRUE ELSE FALSE END, 0
FROM ItemMenu i
WHERE i.menu_id IS NOT NULL
  AND EXISTS (SELECT 1 FROM Menu m WHERE m.id = i.menu_id)
  AND NOT EXISTS (SELECT 1 FROM oferta_menu o WHERE o.menu_id = i.menu_id AND o.item_id = i.id);

-- 2. El retiro ya vive en la oferta: esos ítems vuelven a "Disponible" (sus unidades no cambian). Un ítem
--    "Agotado" con 0 unidades conserva su estado (no se puede saber si además se retiró: sigue sin venderse).
UPDATE ItemMenu SET estado = 'Disponible'
WHERE estado = 'Agotado' AND stockActual > 0 AND menu_id IS NOT NULL
  AND id IN (SELECT o.item_id FROM oferta_menu o WHERE o.retirada = TRUE);

-- 3. Cada plato antiguo es de la jornada de su menú.
UPDATE Plato p SET fecha_preparacion = (SELECT m.fecha FROM ItemMenu i JOIN Menu m ON m.id = i.menu_id WHERE i.id = p.id)
WHERE p.fecha_preparacion IS NULL
  AND EXISTS (SELECT 1 FROM ItemMenu i JOIN Menu m ON m.id = i.menu_id WHERE i.id = p.id);

-- 4. Solo los productos del menú de hoy aún no cerrado siguen habilitados (se están vendiendo con esas
--    unidades). Los de menús anteriores quedan como históricos: atienden sus pedidos y cancelaciones, pero no
--    se vuelven a ofrecer. No se suman ni se fusionan por nombre.
UPDATE Producto SET habilitado = TRUE
WHERE habilitado = FALSE
  AND id IN (SELECT i.id FROM ItemMenu i JOIN Menu m ON m.id = i.menu_id
             WHERE m.fecha = CURRENT_DATE AND m.estado <> 'Cerrado');

-- Personal del comedor de prueba (inicio de sesión: cédula 1700000001, clave comedor123)
INSERT INTO PersonalComedor (cedula, nombres, apellidos, rol, clave)
SELECT '1700000001', 'María', 'Pérez', 'Administradora del comedor', 'comedor123' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM PersonalComedor WHERE cedula = '1700000001');

-- Clienta Andrea Gómez con PoliCuenta 2024-0153 y saldo $10.00 (cédula 1750000001, clave cliente123)
INSERT INTO Policuenta (numero, saldo, version)
SELECT '2024-0153', 10.00, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM Policuenta WHERE numero = '2024-0153');

INSERT INTO ClienteUniversitario (cedula, nombres, apellidos, clave, policuenta_numero)
SELECT '1750000001', 'Andrea', 'Gómez', 'cliente123', '2024-0153' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ClienteUniversitario WHERE cedula = '1750000001');

-- Clientes Luis Torres y Carla Ruiz para demostrar HU-01 a HU-03 (cédulas 1750000002 y 1750000003, clave cliente123)
INSERT INTO Policuenta (numero, saldo, version)
SELECT '2024-0201', 8.00, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM Policuenta WHERE numero = '2024-0201');

INSERT INTO ClienteUniversitario (cedula, nombres, apellidos, clave, policuenta_numero)
SELECT '1750000002', 'Luis', 'Torres', 'cliente123', '2024-0201' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ClienteUniversitario WHERE cedula = '1750000002');

INSERT INTO Policuenta (numero, saldo, version)
SELECT '2024-0318', 5.00, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM Policuenta WHERE numero = '2024-0318');

INSERT INTO ClienteUniversitario (cedula, nombres, apellidos, clave, policuenta_numero)
SELECT '1750000003', 'Carla', 'Ruiz', 'cliente123', '2024-0318' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ClienteUniversitario WHERE cedula = '1750000003');

-- Mesas del comedor (HU-01 a HU-03)
INSERT INTO Mesa (numero) SELECT 1 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM Mesa WHERE numero = 1);
INSERT INTO Mesa (numero) SELECT 2 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM Mesa WHERE numero = 2);
INSERT INTO Mesa (numero) SELECT 3 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM Mesa WHERE numero = 3);

-- Menú del día publicado (fecha de hoy en la zona del comedor: el contenedor arranca con America/Guayaquil).
-- Solo si hoy no hay NINGÚN menú: si la venta de hoy ya se cerró, reiniciar no debe crear otro (CU 05).
-- Los platos y la oferta siguientes solo se agregan a un menú de hoy que no esté cerrado.
INSERT INTO Menu (fecha, estado, personal_cedula)
SELECT CURRENT_DATE, 'Publicado', '1700000001' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM Menu WHERE fecha = CURRENT_DATE);

-- Platos del día (se preparan para una sola jornada): "Arroz con pollo" ($3.50) y, para el escenario de
-- Luis Torres del Examen 1B (HU16 Esc. 3.1), "Seco de pollo" ($4.00). Uno nuevo por cada día con menú abierto.
INSERT INTO ItemMenu (nombre, precio, estado, descripcion, stockActual, version)
SELECT 'Arroz con pollo', 3.50, 'Disponible', 'Arroz, pollo jugoso y ensalada', 20, 0 FROM DUAL
WHERE EXISTS (SELECT 1 FROM Menu WHERE fecha = CURRENT_DATE AND estado <> 'Cerrado')
  AND NOT EXISTS (SELECT 1 FROM ItemMenu i JOIN Plato p ON p.id = i.id
                  WHERE i.nombre = 'Arroz con pollo' AND p.fecha_preparacion = CURRENT_DATE)
  AND NOT EXISTS (SELECT 1 FROM ItemMenu i WHERE i.nombre = 'Arroz con pollo'
                  AND NOT EXISTS (SELECT 1 FROM Plato p WHERE p.id = i.id)
                  AND NOT EXISTS (SELECT 1 FROM Producto p WHERE p.id = i.id));

INSERT INTO Plato (id, fecha_preparacion)
SELECT i.id, CURRENT_DATE FROM ItemMenu i
WHERE i.nombre = 'Arroz con pollo'
  AND NOT EXISTS (SELECT 1 FROM Plato p WHERE p.id = i.id)
  AND NOT EXISTS (SELECT 1 FROM Producto p WHERE p.id = i.id);

INSERT INTO ItemMenu (nombre, precio, estado, descripcion, stockActual, version)
SELECT 'Seco de pollo', 4.00, 'Disponible', 'Pollo guisado con arroz y maduro', 20, 0 FROM DUAL
WHERE EXISTS (SELECT 1 FROM Menu WHERE fecha = CURRENT_DATE AND estado <> 'Cerrado')
  AND NOT EXISTS (SELECT 1 FROM ItemMenu i JOIN Plato p ON p.id = i.id
                  WHERE i.nombre = 'Seco de pollo' AND p.fecha_preparacion = CURRENT_DATE)
  AND NOT EXISTS (SELECT 1 FROM ItemMenu i WHERE i.nombre = 'Seco de pollo'
                  AND NOT EXISTS (SELECT 1 FROM Plato p WHERE p.id = i.id)
                  AND NOT EXISTS (SELECT 1 FROM Producto p WHERE p.id = i.id));

INSERT INTO Plato (id, fecha_preparacion)
SELECT i.id, CURRENT_DATE FROM ItemMenu i
WHERE i.nombre = 'Seco de pollo'
  AND NOT EXISTS (SELECT 1 FROM Plato p WHERE p.id = i.id)
  AND NOT EXISTS (SELECT 1 FROM Producto p WHERE p.id = i.id);

-- Existencia de demostración "Jugo de naranja" ($0.75, producto): se crea UNA sola vez, en una instalación
-- limpia (sin productos), con 30 unidades y caducidad a 7 días desde ese arranque. Su identidad es la clave
-- de semilla, no el nombre. Si se agota o caduca no se recarga ni se extiende su fecha.
INSERT INTO ItemMenu (nombre, precio, estado, descripcion, stockActual, version)
SELECT 'Jugo de naranja', 0.75, 'Disponible', 'Vaso de 300 ml', 30, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM Producto)
  AND NOT EXISTS (SELECT 1 FROM ItemMenu i WHERE i.nombre = 'Jugo de naranja'
                  AND NOT EXISTS (SELECT 1 FROM Plato p WHERE p.id = i.id)
                  AND NOT EXISTS (SELECT 1 FROM Producto p WHERE p.id = i.id));

INSERT INTO Producto (id, fechaCaducidad, habilitado, clave_semilla)
SELECT i.id, DATEADD('DAY', 7, CURRENT_DATE), TRUE, 'demo-jugo-naranja' FROM ItemMenu i
WHERE i.nombre = 'Jugo de naranja'
  AND NOT EXISTS (SELECT 1 FROM Plato p WHERE p.id = i.id)
  AND NOT EXISTS (SELECT 1 FROM Producto p WHERE p.id = i.id)
  AND NOT EXISTS (SELECT 1 FROM Producto p WHERE p.clave_semilla = 'demo-jugo-naranja');

-- Oferta de hoy: los platos de hoy y la existencia de demostración (la MISMA de días anteriores, si sigue
-- vigente y con unidades) en el menú de hoy no cerrado.
INSERT INTO oferta_menu (menu_id, item_id, retirada, version)
SELECT m.id, p.id, FALSE, 0
FROM Menu m JOIN Plato p ON p.fecha_preparacion = m.fecha
JOIN ItemMenu i ON i.id = p.id
WHERE m.id = (SELECT MAX(id) FROM Menu WHERE fecha = CURRENT_DATE AND estado <> 'Cerrado')
  AND i.nombre IN ('Arroz con pollo', 'Seco de pollo')
  AND NOT EXISTS (SELECT 1 FROM oferta_menu o WHERE o.item_id = p.id);

INSERT INTO oferta_menu (menu_id, item_id, retirada, version)
SELECT m.id, p.id, FALSE, 0
FROM Menu m, Producto p JOIN ItemMenu i ON i.id = p.id
WHERE m.id = (SELECT MAX(id) FROM Menu WHERE fecha = CURRENT_DATE AND estado <> 'Cerrado')
  AND p.clave_semilla = 'demo-jugo-naranja' AND p.habilitado = TRUE
  AND p.fechaCaducidad >= CURRENT_DATE AND i.stockActual > 0 AND i.estado = 'Disponible'
  AND NOT EXISTS (SELECT 1 FROM oferta_menu o WHERE o.menu_id = m.id AND o.item_id = p.id);
