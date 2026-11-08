-- PoliDinner - corrección de productos persistentes: migración de una base SQL Server EXISTENTE.
-- Antes cada ItemMenu pertenecía a un solo menú (columna ItemMenu.menu_id). Ahora el menú ofrece ítems con la
-- tabla oferta_menu: un producto conserva su identidad y sus existencias entre jornadas.
--
-- ORDEN DE APLICACIÓN (base existente):
--   1. Detener la venta (sin estudiantes pagando) y RESPALDAR la base (paso 0 de este script o a mano).
--   2. Desplegar el WAR nuevo y arrancarlo una vez: Hibernate (hbm2ddl.auto=update) crea la tabla oferta_menu
--      y las columnas Producto.habilitado, Producto.clave_semilla y Plato.fecha_preparacion.
--      Esos ALTER/CREATE de Hibernate NO se pueden deshacer con ROLLBACK: la recuperación es restaurar el respaldo.
--   3. Ejecutar ESTE script de inmediato (hasta hacerlo, el menú de hoy se ve sin ítems):
--      sqlcmd -S localhost -U polidinner -P <clave> -d PoliDinner -C -I -f 65001 -v RUTA_RESPALDO="C:\respaldos\PoliDinner_antes_productos.bak" -i src/main/resources/migracion_productos.sql
--   4. Revisar los dos informes del final y luego, si hace falta, ejecutar datos_iniciales.sql.
-- INSTALACIÓN LIMPIA: no hace falta este script (solo desplegar y ejecutar datos_iniciales.sql).
-- Se puede repetir: no duplica ofertas ni vuelve a cambiar lo ya migrado. menu_id queda como columna heredada
-- (ya no la usa la aplicación) y NO se borra.
-- Recuperación: RESTORE DATABASE PoliDinner FROM DISK = N'<ruta del respaldo>' WITH REPLACE;
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
SET NOCOUNT ON;
SET XACT_ABORT ON;

-- 0. Respaldo previo (ruta como parámetro de sqlcmd: -v RUTA_RESPALDO="...")
BACKUP DATABASE PoliDinner TO DISK = N'$(RUTA_RESPALDO)' WITH COPY_ONLY, INIT;

-- Requisitos: el WAR nuevo ya se desplegó (paso 2)
IF OBJECT_ID('oferta_menu', 'U') IS NULL OR COL_LENGTH('Producto', 'habilitado') IS NULL
   OR COL_LENGTH('Producto', 'clave_semilla') IS NULL OR COL_LENGTH('Plato', 'fecha_preparacion') IS NULL
BEGIN
    RAISERROR(N'Primero despliegue y arranque el WAR nuevo (crea oferta_menu y las columnas nuevas).', 16, 1);
    SET NOEXEC ON;
END

-- Foto antes: lo que la migración NO debe cambiar
SELECT (SELECT COUNT(*) FROM ItemMenu) AS items, (SELECT COALESCE(SUM(stockActual), 0) FROM ItemMenu) AS unidades,
       (SELECT COALESCE(SUM(precio), 0) FROM ItemMenu) AS precios, (SELECT COALESCE(SUM(saldo), 0) FROM Policuenta) AS saldos,
       (SELECT COUNT(*) FROM Pedido) AS pedidos, (SELECT COUNT(*) FROM Comprobante) AS comprobantes
INTO #antes;

BEGIN TRANSACTION;

IF COL_LENGTH('ItemMenu', 'menu_id') IS NOT NULL
BEGIN
    -- 1. Una oferta por cada vínculo antiguo ítem-menú (incluye los menús cerrados, para el historial).
    --    Un ítem "Agotado" que aún tenía unidades fue un retiro manual: su oferta queda retirada.
    EXEC sp_executesql N'
        INSERT INTO oferta_menu (menu_id, item_id, retirada, version)
        SELECT i.menu_id, i.id, CASE WHEN i.estado = ''Agotado'' AND i.stockActual > 0 THEN 1 ELSE 0 END, 0
        FROM ItemMenu i WITH (TABLOCKX, HOLDLOCK)
        WHERE i.menu_id IS NOT NULL
          AND EXISTS (SELECT 1 FROM Menu m WHERE m.id = i.menu_id)
          AND NOT EXISTS (SELECT 1 FROM oferta_menu o WHERE o.menu_id = i.menu_id AND o.item_id = i.id)';

    -- 2. El retiro ya vive en la oferta: esos ítems vuelven a "Disponible" (sus unidades no cambian).
    --    "Agotado" con 0 unidades se conserva (no se sabe si además se retiró): ver el informe de ambiguos.
    EXEC sp_executesql N'
        UPDATE ItemMenu SET estado = ''Disponible''
        WHERE estado = ''Agotado'' AND stockActual > 0 AND menu_id IS NOT NULL
          AND id IN (SELECT o.item_id FROM oferta_menu o WHERE o.retirada = 1)';

    -- 3. Cada plato antiguo es de la jornada de su menú.
    EXEC sp_executesql N'
        UPDATE p SET fecha_preparacion = m.fecha
        FROM Plato p JOIN ItemMenu i ON i.id = p.id JOIN Menu m ON m.id = i.menu_id
        WHERE p.fecha_preparacion IS NULL';

    -- 4. Solo los productos del menú de hoy aún no cerrado siguen habilitados (se están vendiendo con esas
    --    unidades). Los de menús anteriores quedan como históricos: atienden sus pedidos y cancelaciones, pero
    --    no se vuelven a ofrecer. No se suman ni se fusionan por nombre.
    EXEC sp_executesql N'
        UPDATE pr SET habilitado = 1
        FROM Producto pr JOIN ItemMenu i ON i.id = pr.id JOIN Menu m ON m.id = i.menu_id
        WHERE pr.habilitado = 0 AND m.fecha = CAST(GETDATE() AS date) AND m.estado <> ''Cerrado''';
END

-- 5. Unicidad: una oferta por menú e ítem; una sola existencia por clave de semilla (índice filtrado: SQL Server
--    admite un solo NULL en una restricción UNIQUE simple y los productos del personal no tienen clave).
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID('oferta_menu') AND is_unique = 1
               AND name IN ('uk_oferta_menu_item'))
   AND NOT EXISTS (SELECT 1 FROM sys.key_constraints WHERE name = 'uk_oferta_menu_item')
    CREATE UNIQUE INDEX uk_oferta_menu_item ON oferta_menu (menu_id, item_id);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'uk_producto_clave_semilla')
    CREATE UNIQUE INDEX uk_producto_clave_semilla ON Producto (clave_semilla) WHERE clave_semilla IS NOT NULL;

-- Foto después: si algo de lo protegido cambió, se deshace TODO
IF EXISTS (SELECT 1 FROM #antes a
           WHERE a.items <> (SELECT COUNT(*) FROM ItemMenu)
              OR a.unidades <> (SELECT COALESCE(SUM(stockActual), 0) FROM ItemMenu)
              OR a.precios <> (SELECT COALESCE(SUM(precio), 0) FROM ItemMenu)
              OR a.saldos <> (SELECT COALESCE(SUM(saldo), 0) FROM Policuenta)
              OR a.pedidos <> (SELECT COUNT(*) FROM Pedido)
              OR a.comprobantes <> (SELECT COUNT(*) FROM Comprobante))
BEGIN
    ROLLBACK TRANSACTION;
    RAISERROR(N'La migración alteró ítems, unidades, precios, saldos o pedidos: se deshizo completa.', 16, 1);
END
ELSE
    COMMIT TRANSACTION;

SELECT 'antes' AS foto, * FROM #antes
UNION ALL
SELECT 'despues', (SELECT COUNT(*) FROM ItemMenu), (SELECT COALESCE(SUM(stockActual), 0) FROM ItemMenu),
       (SELECT COALESCE(SUM(precio), 0) FROM ItemMenu), (SELECT COALESCE(SUM(saldo), 0) FROM Policuenta),
       (SELECT COUNT(*) FROM Pedido), (SELECT COUNT(*) FROM Comprobante);

-- Informe 1 (revisar): ítems antiguos "Agotado" con 0 unidades. Siguen sin venderse; no se puede reconstruir
-- si además se retiraron. No se reactivan automáticamente.
IF COL_LENGTH('ItemMenu', 'menu_id') IS NOT NULL
    EXEC sp_executesql N'
        SELECT i.id, i.nombre, i.menu_id, m.fecha, m.estado AS estado_menu
        FROM ItemMenu i JOIN Menu m ON m.id = i.menu_id
        WHERE i.estado = ''Agotado'' AND i.stockActual = 0
        ORDER BY m.fecha DESC, i.id';

-- Informe 2 (conciliar antes de volver a registrar unidades): pedidos Pagados pendientes de retiro con
-- productos históricos. Esas unidades están comprometidas; una cancelación repone sobre el registro antiguo.
-- (sp_executesql: así, si el WAR nuevo aún no se desplegó, solo aparece el mensaje de requisitos)
EXEC sp_executesql N'
    SELECT p.id AS pedido, p.estado, d.cantidad, pr.id AS producto, i.nombre, i.stockActual
    FROM Pedido p
    JOIN DetallePedido d ON d.pedido_id = p.id
    JOIN Producto pr ON pr.id = d.item_id
    JOIN ItemMenu i ON i.id = pr.id
    WHERE p.estado = ''Pagado'' AND pr.habilitado = 0
    ORDER BY p.id';

DROP TABLE #antes;
SET NOEXEC OFF;
