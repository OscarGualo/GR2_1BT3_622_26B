package ec.edu.epn.polidinner.modelo;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;

/**
 * Soporte técnico NO modelado - HU18 (regla 5): periodo de consulta con las fechas inicial y final
 * completas. Los límites se calculan con DiaComedor en la zona del comedor (America/Guayaquil).
 */
public final class PeriodoVentas {

    private final LocalDate inicio;
    private final LocalDate fin;

    public PeriodoVentas(LocalDate inicio, LocalDate fin) {
        if (inicio == null || fin == null) {
            throw new IllegalArgumentException("Seleccione la fecha inicial y la final");
        }
        if (fin.isBefore(inicio)) {
            throw new IllegalArgumentException("La fecha final no puede ser anterior a la inicial");
        }
        this.inicio = inicio;
        this.fin = fin;
    }

    public static PeriodoVentas hoy(LocalDate hoy) {
        return new PeriodoVentas(hoy, hoy);
    }

    /** «Últimos 7 días»: hoy y los seis días anteriores. Origen TDD: PeriodoVentasTest (PU04). */
    public static PeriodoVentas ultimos7Dias(LocalDate hoy) {
        LocalDate primerDiaDelPeriodo = hoy.minusDays(6);
        return new PeriodoVentas(primerDiaDelPeriodo, hoy);
    }

    /** 00:00 del día inicial en la zona del comedor (incluido). */
    public Date desde(ZoneId zona) {
        return new DiaComedor(inicio, zona).inicio();
    }

    /** 00:00 del día siguiente al final en la zona del comedor (excluido). */
    public Date hastaExclusivo(ZoneId zona) {
        return new DiaComedor(fin, zona).fin();
    }

    public LocalDate getInicio() {
        return inicio;
    }

    public LocalDate getFin() {
        return fin;
    }

    @Override
    public boolean equals(Object otro) {
        if (this == otro) {
            return true;
        }
        if (!(otro instanceof PeriodoVentas)) {
            return false;
        }
        PeriodoVentas periodo = (PeriodoVentas) otro;
        return inicio.equals(periodo.inicio) && fin.equals(periodo.fin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(inicio, fin);
    }

    @Override
    public String toString() {
        return "PeriodoVentas[" + inicio + " al " + fin + "]";
    }
}