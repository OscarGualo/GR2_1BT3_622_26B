-- PoliDinner - reinicia las reservas de mesas de HOY para repetir la demostración de HU-01 a HU-03.
-- Solo para pruebas: borra las reservas de la fecha actual (activas y canceladas).
-- QUOTED_IDENTIFIER debe estar activo porque la tabla Reserva tiene índices filtrados (migracion_reservas.sql).
-- sqlcmd -S localhost -U polidinner -P <clave> -d PoliDinner -C -I -f 65001 -i src/main/resources/reiniciar_reservas_pruebas.sql
SET QUOTED_IDENTIFIER ON;
DELETE FROM Reserva WHERE fecha = CAST(GETDATE() AS date);