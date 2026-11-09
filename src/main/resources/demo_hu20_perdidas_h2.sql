-- PoliDinner - HU20 Registrar pérdidas por caducidad: datos de la demostración (base H2 del contenedor).
-- Se carga después de demo_hu18_ventas_h2.sql (usa la referencia 101 de ese script como existencia vigente).
-- Referencia 202: Yogur 150 g con 6 unidades que caducaron ayer, habilitado y sin pérdidas registradas.

INSERT INTO ItemMenu (id, nombre, precio, estado, descripcion, stockActual, version)
SELECT 202, 'Yogur 150 g', 0.90, 'Disponible', 'Vaso', 6, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ItemMenu WHERE id = 202);
INSERT INTO Producto (id, fechaCaducidad, habilitado, clave_semilla)
SELECT 202, DATEADD('DAY', -1, CURRENT_DATE), TRUE, 'demo-hu20-202' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM Producto WHERE id = 202);
