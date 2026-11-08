package ec.edu.epn.polidinner.modelo;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

/**
 * HU19 (criterios 19.1 a 19.3): existencias de la franja elegida, por caducidad más próxima y referencia.
 * Origen TDD: FiltroCaducidadTest (PU05 y PU06).
 */
public class FiltroCaducidad {

    private final ClasificadorFranja clasificador;

    public FiltroCaducidad(ClasificadorFranja clasificador) {
        this.clasificador = clasificador;
    }

    public List<ExistenciaProducto> filtrar(List<ExistenciaProducto> existencias, FranjaCaducidad franja,
                                            LocalDate hoy) {
        return existencias.stream()
                .filter(existencia -> existencia.getCaducidad() != null)
                .filter(existencia -> clasificador.clasificar(existencia.getCaducidad(), hoy) == franja)
                .sorted(Comparator.comparing(ExistenciaProducto::getCaducidad)
                        .thenComparingInt(ExistenciaProducto::getReferencia))
                .toList();
    }
}