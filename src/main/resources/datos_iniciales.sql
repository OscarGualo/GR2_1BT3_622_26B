-- PoliDinner - datos iniciales de los Incrementos 1 y 2
-- Ejecutar DESPUÉS de desplegar la aplicación una vez (Hibernate crea las tablas con hbm2ddl.auto=update).
-- Si la base viene del Incremento 1, ejecutar antes migracion_incremento2.sql. Se puede repetir sin duplicar datos.
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

-- Menú del día publicado con "Arroz con pollo" ($3.50) y "Jugo de naranja" ($0.75) con stock
DECLARE @hoy date = CAST(GETDATE() AS date);
DECLARE @menu int = (SELECT TOP 1 id FROM Menu WHERE fecha = @hoy AND estado <> 'Cerrado' ORDER BY id DESC);
IF @menu IS NULL
BEGIN
    INSERT INTO Menu (fecha, estado, personal_cedula) VALUES (@hoy, 'Publicado', '1700000001');
    SET @menu = SCOPE_IDENTITY();
END

IF NOT EXISTS (SELECT 1 FROM ItemMenu WHERE menu_id = @menu AND nombre = N'Arroz con pollo')
BEGIN
    INSERT INTO ItemMenu (nombre, precio, estado, descripcion, stockActual, version, menu_id)
    VALUES (N'Arroz con pollo', 3.50, 'Disponible', N'Arroz, pollo jugoso y ensalada', 20, 0, @menu);
    INSERT INTO Plato (id) VALUES (SCOPE_IDENTITY());
END

IF NOT EXISTS (SELECT 1 FROM ItemMenu WHERE menu_id = @menu AND nombre = N'Jugo de naranja')
BEGIN
    INSERT INTO ItemMenu (nombre, precio, estado, descripcion, stockActual, version, menu_id)
    VALUES (N'Jugo de naranja', 0.75, 'Disponible', N'Vaso de 300 ml', 30, 0, @menu);
    INSERT INTO Producto (id, fechaCaducidad) VALUES (SCOPE_IDENTITY(), DATEADD(day, 7, @hoy));
END
