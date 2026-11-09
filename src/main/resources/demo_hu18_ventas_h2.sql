-- PoliDinner - HU18 Consultar ventas de productos: datos de la demostración (base H2 del contenedor).
-- Se carga junto con los datos iniciales, por ejemplo:
--   POLIDINNER_DATOS_INICIALES=datos_iniciales_h2.sql,demo_hu18_ventas_h2.sql
-- Las fechas son relativas al día de la primera carga. Se puede repetir sin duplicar datos; para volver a
-- fechar los pedidos, crear el contenedor con un volumen nuevo. Cliente: Andrea Gómez (PoliCuenta 2024-0153).
-- Cada sentencia termina en punto y coma; no usar punto y coma dentro de los textos.

-- Registros de producto (las referencias son sus id)
INSERT INTO ItemMenu (id, nombre, precio, estado, descripcion, stockActual, version)
SELECT 101, 'Agua 500 ml', 0.50, 'Disponible', 'Botella', 3, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ItemMenu WHERE id = 101);
INSERT INTO Producto (id, fechaCaducidad, habilitado, clave_semilla)
SELECT 101, DATEADD('DAY', 1, CURRENT_DATE), TRUE, 'demo-hu18-101' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM Producto WHERE id = 101);

INSERT INTO ItemMenu (id, nombre, precio, estado, descripcion, stockActual, version)
SELECT 102, 'Agua 500 ml', 0.50, 'Disponible', 'Botella', 7, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ItemMenu WHERE id = 102);
INSERT INTO Producto (id, fechaCaducidad, habilitado, clave_semilla)
SELECT 102, DATEADD('DAY', 4, CURRENT_DATE), TRUE, 'demo-hu18-102' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM Producto WHERE id = 102);

INSERT INTO ItemMenu (id, nombre, precio, estado, descripcion, stockActual, version)
SELECT 103, 'Galletas 100 g', 0.75, 'Agotado', 'Paquete', 0, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ItemMenu WHERE id = 103);
INSERT INTO Producto (id, fechaCaducidad, habilitado, clave_semilla)
SELECT 103, DATEADD('DAY', 1, CURRENT_DATE), TRUE, 'demo-hu18-103' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM Producto WHERE id = 103);

INSERT INTO ItemMenu (id, nombre, precio, estado, descripcion, stockActual, version)
SELECT 104, 'Galletas 200 g', 1.25, 'Disponible', 'Paquete', 2, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ItemMenu WHERE id = 104);
INSERT INTO Producto (id, fechaCaducidad, habilitado, clave_semilla)
SELECT 104, DATEADD('DAY', 4, CURRENT_DATE), TRUE, 'demo-hu18-104' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM Producto WHERE id = 104);

INSERT INTO ItemMenu (id, nombre, precio, estado, descripcion, stockActual, version)
SELECT 201, 'Yogur 150 g', 0.90, 'Disponible', 'Vaso', 4, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ItemMenu WHERE id = 201);
INSERT INTO Producto (id, fechaCaducidad, habilitado, clave_semilla)
SELECT 201, DATEADD('DAY', 2, CURRENT_DATE), TRUE, 'demo-hu18-201' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM Producto WHERE id = 201);

-- Pedidos y comprobantes: la fecha de pago es Comprobante.fechaGeneracion
-- 18.1.1: dos pedidos Pagados hoy de la referencia 101 (6 y 4 unidades)
INSERT INTO Pedido (id, fecha, estado, total, cliente_cedula, policuenta_numero, version)
SELECT 9181, DATEADD('DAY', 0, LOCALTIMESTAMP), 'Pagado', 3.00, '1750000001', '2024-0153', 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM Pedido WHERE id = 9181);
INSERT INTO DetallePedido (cantidad, precioUnitario, item_id, pedido_id)
SELECT 6, 0.50, 101, 9181 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM DetallePedido WHERE pedido_id = 9181);
INSERT INTO Comprobante (idComprobante, codigo, fechaGeneracion, usado, pedido_id, version)
SELECT 'demo-hu18-9181', 'PD-9181', DATEADD('DAY', 0, LOCALTIMESTAMP), FALSE, 9181, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM Comprobante WHERE pedido_id = 9181);

INSERT INTO Pedido (id, fecha, estado, total, cliente_cedula, policuenta_numero, version)
SELECT 9182, DATEADD('DAY', 0, LOCALTIMESTAMP), 'Pagado', 2.00, '1750000001', '2024-0153', 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM Pedido WHERE id = 9182);
INSERT INTO DetallePedido (cantidad, precioUnitario, item_id, pedido_id)
SELECT 4, 0.50, 101, 9182 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM DetallePedido WHERE pedido_id = 9182);
INSERT INTO Comprobante (idComprobante, codigo, fechaGeneracion, usado, pedido_id, version)
SELECT 'demo-hu18-9182', 'PD-9182', DATEADD('DAY', 0, LOCALTIMESTAMP), FALSE, 9182, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM Comprobante WHERE pedido_id = 9182);

-- 18.1.2: 4 unidades de la referencia 102 pagadas hoy
INSERT INTO Pedido (id, fecha, estado, total, cliente_cedula, policuenta_numero, version)
SELECT 9183, DATEADD('DAY', 0, LOCALTIMESTAMP), 'Pagado', 2.00, '1750000001', '2024-0153', 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM Pedido WHERE id = 9183);
INSERT INTO DetallePedido (cantidad, precioUnitario, item_id, pedido_id)
SELECT 4, 0.50, 102, 9183 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM DetallePedido WHERE pedido_id = 9183);
INSERT INTO Comprobante (idComprobante, codigo, fechaGeneracion, usado, pedido_id, version)
SELECT 'demo-hu18-9183', 'PD-9183', DATEADD('DAY', 0, LOCALTIMESTAMP), FALSE, 9183, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM Comprobante WHERE pedido_id = 9183);

-- 18.3.1: referencia 103 agotada: 2 Pagadas, 3 Entregadas y 4 Canceladas, todas pagadas hoy
INSERT INTO Pedido (id, fecha, estado, total, cliente_cedula, policuenta_numero, version)
SELECT 9184, DATEADD('DAY', 0, LOCALTIMESTAMP), 'Pagado', 1.50, '1750000001', '2024-0153', 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM Pedido WHERE id = 9184);
INSERT INTO DetallePedido (cantidad, precioUnitario, item_id, pedido_id)
SELECT 2, 0.75, 103, 9184 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM DetallePedido WHERE pedido_id = 9184);
INSERT INTO Comprobante (idComprobante, codigo, fechaGeneracion, usado, pedido_id, version)
SELECT 'demo-hu18-9184', 'PD-9184', DATEADD('DAY', 0, LOCALTIMESTAMP), FALSE, 9184, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM Comprobante WHERE pedido_id = 9184);

INSERT INTO Pedido (id, fecha, estado, total, cliente_cedula, policuenta_numero, version)
SELECT 9185, DATEADD('DAY', 0, LOCALTIMESTAMP), 'Entregado', 2.25, '1750000001', '2024-0153', 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM Pedido WHERE id = 9185);
INSERT INTO DetallePedido (cantidad, precioUnitario, item_id, pedido_id)
SELECT 3, 0.75, 103, 9185 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM DetallePedido WHERE pedido_id = 9185);
INSERT INTO Comprobante (idComprobante, codigo, fechaGeneracion, usado, pedido_id, version)
SELECT 'demo-hu18-9185', 'PD-9185', DATEADD('DAY', 0, LOCALTIMESTAMP), TRUE, 9185, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM Comprobante WHERE pedido_id = 9185);

INSERT INTO Pedido (id, fecha, estado, total, cliente_cedula, policuenta_numero, version)
SELECT 9186, DATEADD('DAY', 0, LOCALTIMESTAMP), 'Cancelado', 3.00, '1750000001', '2024-0153', 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM Pedido WHERE id = 9186);
INSERT INTO DetallePedido (cantidad, precioUnitario, item_id, pedido_id)
SELECT 4, 0.75, 103, 9186 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM DetallePedido WHERE pedido_id = 9186);
INSERT INTO Comprobante (idComprobante, codigo, fechaGeneracion, usado, pedido_id, version)
SELECT 'demo-hu18-9186', 'PD-9186', DATEADD('DAY', 0, LOCALTIMESTAMP), FALSE, 9186, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM Comprobante WHERE pedido_id = 9186);

-- 18.2.2: referencia 104: 1 unidad pagada hace 6 días y 4 hace 7 días (fuera de «Últimos 7 días»)
INSERT INTO Pedido (id, fecha, estado, total, cliente_cedula, policuenta_numero, version)
SELECT 9187, DATEADD('DAY', -6, LOCALTIMESTAMP), 'Pagado', 1.25, '1750000001', '2024-0153', 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM Pedido WHERE id = 9187);
INSERT INTO DetallePedido (cantidad, precioUnitario, item_id, pedido_id)
SELECT 1, 1.25, 104, 9187 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM DetallePedido WHERE pedido_id = 9187);
INSERT INTO Comprobante (idComprobante, codigo, fechaGeneracion, usado, pedido_id, version)
SELECT 'demo-hu18-9187', 'PD-9187', DATEADD('DAY', -6, LOCALTIMESTAMP), FALSE, 9187, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM Comprobante WHERE pedido_id = 9187);

INSERT INTO Pedido (id, fecha, estado, total, cliente_cedula, policuenta_numero, version)
SELECT 9188, DATEADD('DAY', -7, LOCALTIMESTAMP), 'Pagado', 5.00, '1750000001', '2024-0153', 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM Pedido WHERE id = 9188);
INSERT INTO DetallePedido (cantidad, precioUnitario, item_id, pedido_id)
SELECT 4, 1.25, 104, 9188 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM DetallePedido WHERE pedido_id = 9188);
INSERT INTO Comprobante (idComprobante, codigo, fechaGeneracion, usado, pedido_id, version)
SELECT 'demo-hu18-9188', 'PD-9188', DATEADD('DAY', -7, LOCALTIMESTAMP), FALSE, 9188, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM Comprobante WHERE pedido_id = 9188);

-- 18.2.1: referencia 201: pedido creado hace 10 días, pagado hace 2 días y Entregado
INSERT INTO Pedido (id, fecha, estado, total, cliente_cedula, policuenta_numero, version)
SELECT 9189, DATEADD('DAY', -10, LOCALTIMESTAMP), 'Entregado', 1.80, '1750000001', '2024-0153', 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM Pedido WHERE id = 9189);
INSERT INTO DetallePedido (cantidad, precioUnitario, item_id, pedido_id)
SELECT 2, 0.90, 201, 9189 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM DetallePedido WHERE pedido_id = 9189);
INSERT INTO Comprobante (idComprobante, codigo, fechaGeneracion, usado, pedido_id, version)
SELECT 'demo-hu18-9189', 'PD-9189', DATEADD('DAY', -2, LOCALTIMESTAMP), TRUE, 9189, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM Comprobante WHERE pedido_id = 9189);