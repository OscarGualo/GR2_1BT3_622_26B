-- PoliDinner - SOLO PARA PRUEBAS: permite crear otro menú hoy después de "Cerrar venta del día" (CU 05)
-- El CU 05 no deja crear otro menú el mismo día en que se cerró la venta. Este script pasa el menú cerrado
-- de hoy al día anterior; los pedidos conservan su fecha, así que el reporte de hoy los sigue mostrando.
-- No usar DELETE: los ítems del menú están ligados a los detalles de los pedidos. Se puede repetir.
-- sqlcmd -S localhost -U polidinner -P <clave> -d PoliDinner -C -f 65001 -i src/main/resources/reiniciar_venta_pruebas.sql

UPDATE Menu
SET fecha = DATEADD(day, -1, fecha)
WHERE estado = 'Cerrado'
  AND fecha = CAST(GETDATE() AS date);

SELECT TOP 5 id, fecha, estado FROM Menu ORDER BY id DESC;
