-- PoliDinner - datos iniciales del Incremento 1
-- Ejecutar DESPUÉS de desplegar la aplicación una vez (Hibernate crea las tablas con hbm2ddl.auto=update).
-- sqlcmd -S localhost -E -d PoliDinner -f 65001 -i src/main/resources/datos_iniciales.sql

-- Personal del comedor de prueba (inicio de sesión: cédula 1700000001, clave comedor123)
IF NOT EXISTS (SELECT 1 FROM PersonalComedor WHERE cedula = '1700000001')
    INSERT INTO PersonalComedor (cedula, nombres, apellidos, rol, clave)
    VALUES ('1700000001', N'María', N'Pérez', N'Administradora del comedor', 'comedor123');
