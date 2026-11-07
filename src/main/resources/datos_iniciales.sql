-- PoliDinner - datos iniciales de los Incrementos 1 y 2
-- Ejecutar DESPUÉS de desplegar la aplicación una vez (Hibernate crea las tablas con hbm2ddl.auto=update).
-- Si la base viene del Incremento 1, ejecutar antes migracion_incremento2.sql; si es anterior a la corrección de
-- productos persistentes, ejecutar antes migracion_productos.sql. Se puede repetir sin duplicar datos.
-- sqlcmd -S localhost -U polidinner -P <clave> -d PoliDinner -C -f 65001 -i src/main/resources/datos_iniciales.sql

-- Personal del comedor de prueba (inicio de sesión: cédula 1700000001, clave comedor123)
IF NOT EXISTS (SELECT 1 FROM PersonalComedor WHERE cedula = '1700000001')
    INSERT INTO PersonalComedor (cedula, nombres, apellidos, rol, clave)
    VALUES ('1700000001', N'María', N'Pérez', N'Administradora del comedor', 'comedor123');

-- Incremento 2 - caso de prueba del CU 03: clienta Andrea Gómez con PoliCuenta 2024-0153 y saldo $10.00
-- (inicio de sesión del cliente: cédula 1750000001, clave cliente123). PoliCuenta se simula con esta tabla.
IF NOT EXISTS (SELECT 1 FROM Policuenta WHERE numero = '2024-0153')
    INSERT INTO Policuenta (numero, saldo, version) VALUES ('2024-0153', 10.00, 0);

IF NOT EXISTS (SELECT 1 FROM ClienteUniversitario WHERE cedula = '1750000001')
    INSERT INTO ClienteUniversitario (cedula, nombres, apellidos, clave, policuenta_numero)
    VALUES ('1750000001', N'Andrea', N'Gómez', 'cliente123', '2024-0153');

-- Menú del día publicado con "Arroz con pollo" ($3.50), "Seco de pollo" ($4.00) y la existencia de
-- demostración "Jugo de naranja" ($0.75). Corrección de productos persistentes: los ítems se ofrecen con
-- oferta_menu (ejecutar antes migracion_productos.sql si la base es anterior a esta corrección).
DECLARE @hoy date = CAST(GETDATE() AS date);
-- Solo si hoy no hay NINGÚN menú: si la venta de hoy ya se cerró, no se crea otro ni se le agregan ítems (CU 05)
DECLARE @menu int = (SELECT TOP 1 id FROM Menu WHERE fecha = @hoy AND estado <> 'Cerrado' ORDER BY id DESC);
IF @menu IS NULL AND NOT EXISTS (SELECT 1 FROM Menu WHERE fecha = @hoy)
BEGIN
    INSERT INTO Menu (fecha, estado, personal_cedula) VALUES (@hoy, 'Publicado', '1700000001');
    SET @menu = SCOPE_IDENTITY();
END

-- Platos de hoy (se preparan para una sola jornada)
DECLARE @item int;
IF @menu IS NOT NULL AND NOT EXISTS (SELECT 1 FROM ItemMenu i JOIN Plato p ON p.id = i.id
                                     WHERE i.nombre = N'Arroz con pollo' AND p.fecha_preparacion = @hoy)
BEGIN
    INSERT INTO ItemMenu (nombre, precio, estado, descripcion, stockActual, version)
    VALUES (N'Arroz con pollo', 3.50, 'Disponible', N'Arroz, pollo jugoso y ensalada', 20, 0);
    SET @item = SCOPE_IDENTITY();
    INSERT INTO Plato (id, fecha_preparacion) VALUES (@item, @hoy);
    INSERT INTO oferta_menu (menu_id, item_id, retirada, version) VALUES (@menu, @item, 0, 0);
END

-- Examen 1B (HU16 Esc. 3.1): "Seco de pollo" ($4.00, plato) para el escenario de Luis Torres
IF @menu IS NOT NULL AND NOT EXISTS (SELECT 1 FROM ItemMenu i JOIN Plato p ON p.id = i.id
                                     WHERE i.nombre = N'Seco de pollo' AND p.fecha_preparacion = @hoy)
BEGIN
    INSERT INTO ItemMenu (nombre, precio, estado, descripcion, stockActual, version)
    VALUES (N'Seco de pollo', 4.00, 'Disponible', N'Pollo guisado con arroz y maduro', 20, 0);
    SET @item = SCOPE_IDENTITY();
    INSERT INTO Plato (id, fecha_preparacion) VALUES (@item, @hoy);
    INSERT INTO oferta_menu (menu_id, item_id, retirada, version) VALUES (@menu, @item, 0, 0);
END

-- Existencia de demostración: se crea UNA sola vez, en una base sin productos, con 30 unidades y caducidad a
-- 7 días. Su identidad es la clave de semilla, no el nombre; si se agota o caduca no se recarga.
IF NOT EXISTS (SELECT 1 FROM Producto)
BEGIN
    INSERT INTO ItemMenu (nombre, precio, estado, descripcion, stockActual, version)
    VALUES (N'Jugo de naranja', 0.75, 'Disponible', N'Vaso de 300 ml', 30, 0);
    INSERT INTO Producto (id, fechaCaducidad, habilitado, clave_semilla)
    VALUES (SCOPE_IDENTITY(), DATEADD(day, 7, @hoy), 1, 'demo-jugo-naranja');
END

-- La MISMA existencia de demostración se ofrece en el menú de hoy si sigue vigente y con unidades
IF @menu IS NOT NULL
    INSERT INTO oferta_menu (menu_id, item_id, retirada, version)
    SELECT @menu, p.id, 0, 0
    FROM Producto p JOIN ItemMenu i ON i.id = p.id
    WHERE p.clave_semilla = 'demo-jugo-naranja' AND p.habilitado = 1
      AND p.fechaCaducidad >= @hoy AND i.stockActual > 0 AND i.estado = 'Disponible'
      AND NOT EXISTS (SELECT 1 FROM oferta_menu o WHERE o.menu_id = @menu AND o.item_id = p.id);

-- Incremento de reservas (HU-01 a HU-03): mesas del comedor
IF NOT EXISTS (SELECT 1 FROM Mesa WHERE numero = 1) INSERT INTO Mesa (numero) VALUES (1);
IF NOT EXISTS (SELECT 1 FROM Mesa WHERE numero = 2) INSERT INTO Mesa (numero) VALUES (2);
IF NOT EXISTS (SELECT 1 FROM Mesa WHERE numero = 3) INSERT INTO Mesa (numero) VALUES (3);

   -- Incremento de reservas: clientes Luis Torres y Carla Ruiz para demostrar HU-01 a HU-03
-- (inicio de sesión: cédulas 1750000002 y 1750000003, clave cliente123)
IF NOT EXISTS (SELECT 1 FROM Policuenta WHERE numero = '2024-0201')
    INSERT INTO Policuenta (numero, saldo, version) VALUES ('2024-0201', 8.00, 0);
IF NOT EXISTS (SELECT 1 FROM ClienteUniversitario WHERE cedula = '1750000002')
    INSERT INTO ClienteUniversitario (cedula, nombres, apellidos, clave, policuenta_numero)
    VALUES ('1750000002', N'Luis', N'Torres', 'cliente123', '2024-0201');

IF NOT EXISTS (SELECT 1 FROM Policuenta WHERE numero = '2024-0318')
    INSERT INTO Policuenta (numero, saldo, version) VALUES ('2024-0318', 5.00, 0);
IF NOT EXISTS (SELECT 1 FROM ClienteUniversitario WHERE cedula = '1750000003')
    INSERT INTO ClienteUniversitario (cedula, nombres, apellidos, clave, policuenta_numero)
    VALUES ('1750000003', N'Carla', N'Ruiz', 'cliente123', '2024-0318');