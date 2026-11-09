package ec.edu.epn.polidinner.modelo;

import java.time.LocalDate;

/** Soporte técnico NO modelado - HU19: decide la franja de una fecha de caducidad respecto de hoy. */
public interface ClasificadorFranja {

    FranjaCaducidad clasificar(LocalDate caducidad, LocalDate hoy);
}