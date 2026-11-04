-- PoliDinner - Release 2 (Examen 1B): pedidos programados con turnos de retiro (HU11 a HU17).
-- Ejecutar UNA vez, después de desplegar (Hibernate ya creó las tablas franja_retiro y configuracion_cupos
-- y las columnas franja_retiro_id y version de Pedido con hbm2ddl.auto=update). Es seguro repetirlo.
-- sqlcmd -S localhost -U polidinner -P <clave> -d PoliDinner -C -I -f 65001 -i src/main/resources/migracion_release2.sql
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;

-- Pedidos del Release 1 (compra inmediata) guardados antes de existir el control de versión: versión 0, sin franja
IF COL_LENGTH('Pedido', 'version') IS NOT NULL
    EXEC sp_executesql N'UPDATE Pedido SET version = 0 WHERE version IS NULL';

-- HU15 / Control de cupos: una sola fila por fecha y hora de inicio. Evita que dos solicitudes simultáneas
-- creen la misma franja dos veces (ServicioFranjas la crea la primera vez que se pide)
IF OBJECT_ID('franja_retiro', 'U') IS NOT NULL
   AND NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'uk_franja_fecha_hora')
    CREATE UNIQUE INDEX uk_franja_fecha_hora ON franja_retiro (fecha, hora_inicio);

-- HU15 Esc. 1.1: cupo general inicial de 15 pedidos por franja (fila única, id = 1)
IF OBJECT_ID('configuracion_cupos', 'U') IS NOT NULL
   AND NOT EXISTS (SELECT 1 FROM configuracion_cupos WHERE id = 1)
    INSERT INTO configuracion_cupos (id, cupo_general) VALUES (1, 15);

-- Comprobación de que el contador de cada franja coincide con sus pedidos (debe devolver 0 filas)
IF OBJECT_ID('franja_retiro', 'U') IS NOT NULL AND COL_LENGTH('Pedido', 'franja_retiro_id') IS NOT NULL
    EXEC sp_executesql N'SELECT f.id, f.fecha, f.hora_inicio, f.asignados, COUNT(p.id) AS pedidos_reales
        FROM franja_retiro f
        LEFT JOIN Pedido p ON p.franja_retiro_id = f.id AND p.estado IN (N''Pagado'', N''Entregado'')
        GROUP BY f.id, f.fecha, f.hora_inicio, f.asignados
        HAVING f.asignados <> COUNT(p.id)';
