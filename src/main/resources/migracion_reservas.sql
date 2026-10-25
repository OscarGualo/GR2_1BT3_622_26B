-- PoliDinner - incremento de reservas: la base garantiza las reglas de HU-01 aunque lleguen dos peticiones a la vez.
--   * Una mesa solo puede tener una reserva "Activa" por fecha y franja (HU-01 / Escenario 1.2).
--   * Un cliente solo puede tener una reserva "Activa" por fecha (HU-01 / Escenario 1.3).
-- Ejecutar UNA vez, después de desplegar (Hibernate ya creó la tabla Reserva con hbm2ddl.auto=update).
-- Si falla porque ya hay duplicados, ejecutar antes reiniciar_reservas_pruebas.sql.
-- sqlcmd -S localhost -U polidinner -P <clave> -d PoliDinner -C -I -f 65001 -i src/main/resources/migracion_reservas.sql
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'UX_Reserva_Mesa_Franja_Activa')
CREATE UNIQUE INDEX UX_Reserva_Mesa_Franja_Activa ON Reserva (mesa_numero, fecha, franja) WHERE estado = N'Activa';

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'UX_Reserva_Cliente_Activa')
CREATE UNIQUE INDEX UX_Reserva_Cliente_Activa ON Reserva (cliente_cedula, fecha) WHERE estado = N'Activa';