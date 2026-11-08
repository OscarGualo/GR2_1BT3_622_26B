-- PoliDinner - HU19 Consultar productos por franja de caducidad: datos de la demostración (base H2).
-- Se carga después de los scripts de HU18 y HU20 (usa sus referencias 101, 102, 103, 201 y 202).
-- 105 caduca pasado mañana, 301 caduca hoy, 302 dentro de 3 días y 401 es un histórico no habilitado.

INSERT INTO ItemMenu (id, nombre, precio, estado, descripcion, stockActual, version)
SELECT 105, 'Agua 500 ml', 0.50, 'Disponible', 'Botella', 4, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ItemMenu WHERE id = 105);
INSERT INTO Producto (id, fechaCaducidad, habilitado, clave_semilla)
SELECT 105, DATEADD('DAY', 2, CURRENT_DATE), TRUE, 'demo-hu19-105' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM Producto WHERE id = 105);

INSERT INTO ItemMenu (id, nombre, precio, estado, descripcion, stockActual, version)
SELECT 301, 'Pastel porción', 1.50, 'Disponible', 'Porción', 2, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ItemMenu WHERE id = 301);
INSERT INTO Producto (id, fechaCaducidad, habilitado, clave_semilla)
SELECT 301, DATEADD('DAY', 0, CURRENT_DATE), TRUE, 'demo-hu19-301' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM Producto WHERE id = 301);

INSERT INTO ItemMenu (id, nombre, precio, estado, descripcion, stockActual, version)
SELECT 302, 'Empanada unidad', 1.00, 'Disponible', 'Unidad', 2, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ItemMenu WHERE id = 302);
INSERT INTO Producto (id, fechaCaducidad, habilitado, clave_semilla)
SELECT 302, DATEADD('DAY', 3, CURRENT_DATE), TRUE, 'demo-hu19-302' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM Producto WHERE id = 302);

INSERT INTO ItemMenu (id, nombre, precio, estado, descripcion, stockActual, version)
SELECT 401, 'Jugo 250 ml', 0.80, 'Disponible', 'Caja', 5, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ItemMenu WHERE id = 401);
INSERT INTO Producto (id, fechaCaducidad, habilitado, clave_semilla)
SELECT 401, DATEADD('DAY', 1, CURRENT_DATE), FALSE, 'demo-hu19-401' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM Producto WHERE id = 401);
