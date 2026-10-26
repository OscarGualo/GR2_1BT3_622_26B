-- PoliDinner - datos iniciales para el contenedor Docker (base H2 embebida).
-- Equivale a datos_iniciales.sql (SQL Server). Lo ejecuta la aplicación al arrancar (CargaDatosIniciales)
-- cuando existe la variable POLIDINNER_DATOS_INICIALES; no hay que correrlo a mano.
-- Se puede repetir sin duplicar datos y sin reiniciar saldos ni stock ya modificados.
-- Cada sentencia termina en punto y coma; no usar punto y coma dentro de los textos.

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

-- Menú del día publicado (fecha de hoy en la zona del comedor: el contenedor arranca con America/Guayaquil)
INSERT INTO Menu (fecha, estado, personal_cedula)
SELECT CURRENT_DATE, 'Publicado', '1700000001' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM Menu WHERE fecha = CURRENT_DATE AND estado <> 'Cerrado');

-- "Arroz con pollo" ($3.50, plato) y "Jugo de naranja" ($0.75, producto) en el menú de hoy
INSERT INTO ItemMenu (nombre, precio, estado, descripcion, stockActual, version, menu_id)
SELECT 'Arroz con pollo', 3.50, 'Disponible', 'Arroz, pollo jugoso y ensalada', 20, 0, m.id FROM Menu m
WHERE m.id = (SELECT MAX(id) FROM Menu WHERE fecha = CURRENT_DATE AND estado <> 'Cerrado')
  AND NOT EXISTS (SELECT 1 FROM ItemMenu i WHERE i.menu_id = m.id AND i.nombre = 'Arroz con pollo');

INSERT INTO Plato (id)
SELECT i.id FROM ItemMenu i
WHERE i.nombre = 'Arroz con pollo'
  AND NOT EXISTS (SELECT 1 FROM Plato p WHERE p.id = i.id)
  AND NOT EXISTS (SELECT 1 FROM Producto p WHERE p.id = i.id);

INSERT INTO ItemMenu (nombre, precio, estado, descripcion, stockActual, version, menu_id)
SELECT 'Jugo de naranja', 0.75, 'Disponible', 'Vaso de 300 ml', 30, 0, m.id FROM Menu m
WHERE m.id = (SELECT MAX(id) FROM Menu WHERE fecha = CURRENT_DATE AND estado <> 'Cerrado')
  AND NOT EXISTS (SELECT 1 FROM ItemMenu i WHERE i.menu_id = m.id AND i.nombre = 'Jugo de naranja');

INSERT INTO Producto (id, fechaCaducidad)
SELECT i.id, DATEADD('DAY', 7, CURRENT_DATE) FROM ItemMenu i
WHERE i.nombre = 'Jugo de naranja'
  AND NOT EXISTS (SELECT 1 FROM Producto p WHERE p.id = i.id)
  AND NOT EXISTS (SELECT 1 FROM Plato p WHERE p.id = i.id);
