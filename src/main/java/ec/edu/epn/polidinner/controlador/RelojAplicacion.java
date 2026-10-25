package ec.edu.epn.polidinner.controlador;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

/**
 * Soporte técnico (no modelado): hora que usan los controladores de reservas (HU-01 y HU-02).
 * Por defecto es la hora del sistema. Para demostrar los escenarios en cualquier momento del día,
 * se puede fijar una hora simulada escribiéndola (por ejemplo 12:20) en el archivo polidinner-hora.txt
 * de la carpeta del usuario, o en el archivo que indique la variable de entorno POLIDINNER_ARCHIVO_HORA.
 * El archivo se lee en cada petición: se puede cambiar sin reiniciar Tomcat. Si no existe, está vacío
 * o no tiene una hora válida, se usa la hora real.
 */
final class RelojAplicacion {

    private RelojAplicacion() {
    }

    static LocalTime horaActual() {
        Path archivo = archivoDeHora();
        try {
            if (Files.exists(archivo)) {
                String texto = new String(Files.readAllBytes(archivo), StandardCharsets.UTF_8).trim();
                if (!texto.isEmpty()) {
                    return LocalTime.parse(texto);
                }
            }
        } catch (IOException | DateTimeParseException e) {
            // Archivo ilegible o con formato incorrecto: se usa la hora real
        }
        return LocalTime.now();
    }

    private static Path archivoDeHora() {
        String ruta = System.getenv("POLIDINNER_ARCHIVO_HORA");
        if (ruta != null && !ruta.trim().isEmpty()) {
            return Paths.get(ruta.trim());
        }
        return Paths.get(System.getProperty("user.home"), "polidinner-hora.txt");
    }
}