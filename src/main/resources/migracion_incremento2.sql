-- PoliDinner - migración de una base del Incremento 1 al Incremento 2
-- Trazabilidad: Diagrama de clases del incremento 2 (Fig. 15): ItemMenu gana stockActual y Producto pierde stock.
-- Ejecutar UNA vez ANTES de desplegar el Incremento 2 sobre una base que ya tenía datos del Incremento 1
-- (en una base vacía no hace falta: Hibernate crea las tablas directamente). Se puede repetir sin efectos.
-- sqlcmd -S localhost -U polidinner -P <clave> -d PoliDinner -C -f 65001 -i src/main/resources/migracion_incremento2.sql

-- 1. ItemMenu.stockActual (0 por defecto para las filas existentes)
IF COL_LENGTH('ItemMenu', 'stockActual') IS NULL
    ALTER TABLE ItemMenu ADD stockActual int NOT NULL CONSTRAINT DF_ItemMenu_stockActual DEFAULT 0;
GO

-- 2. El stock de los productos pasa a ItemMenu.stockActual y se elimina la columna antigua (era NOT NULL
--    y haría fallar el registro de productos nuevos).
IF COL_LENGTH('Producto', 'stock') IS NOT NULL
BEGIN
    EXEC('UPDATE i SET i.stockActual = p.stock FROM ItemMenu i JOIN Producto p ON p.id = i.id');
    ALTER TABLE Producto DROP COLUMN stock;
END
GO
