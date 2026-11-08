package ec.edu.epn.polidinner.modelo;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

/**
 * HU19 (criterios 19.1 a 19.3): existencias de la franja elegida.
 * Contrato de la iteración: pendiente de PU05 y PU06 (Jean).
 */
public class FiltroCaducidad {

    private final ClasificadorFranja clasificador;

    public FiltroCaducidad(ClasificadorFranja clasificador) {
        this.clasificador = clasificador;
    }

    public List<ExistenciaProducto> filtrar(List<ExistenciaProducto> existencias, FranjaCaducidad franja,
                                            LocalDate hoy) {
        return Collections.emptyList();
    }
}