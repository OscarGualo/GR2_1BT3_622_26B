package ec.edu.epn.polidinner.modelo;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import ec.edu.epn.polidinner.persistencia.MesaDAO;
import ec.edu.epn.polidinner.persistencia.ReservaDAO;

/**
 * HU-03 Ver la ocupación de mesas del día - estados generales del tablero (tarea T3.3).
 * Pruebas adicionales del incremento, fuera de las seis pruebas TDD (PU-01 a PU-06).
 * MesaDAO y ReservaDAO se simulan con Mockito, como en PU-05.
 */
class TableroEstadosTest {

    private final LocalDate hoy = LocalDate.of(2026, 10, 26);
    private final List<Mesa> mesas = Arrays.asList(new Mesa(1), new Mesa(2), new Mesa(3));

    private TableroOcupacion tableroCon(List<Reserva> reservasActivas) {
        MesaDAO mesaDAO = Mockito.mock(MesaDAO.class);
        ReservaDAO reservaDAO = Mockito.mock(ReservaDAO.class);
        Mockito.when(mesaDAO.listarTodos()).thenReturn(mesas);
        Mockito.when(reservaDAO.buscarActivasPorFecha(hoy)).thenReturn(reservasActivas);
        return new TableroOcupacion(hoy, mesaDAO, reservaDAO);
    }

    private ClienteUniversitario cliente(String cedula, String nombres, String apellidos) {
        return new ClienteUniversitario(cedula, nombres, apellidos, "clave", new Policuenta("P-" + cedula, 5.00));
    }

    /** HU-03 / Escenario 2.1: sin reservas, el tablero lo indica y ninguna franja está completa. */
    @Test
    void sinReservasNoHayReservasYNingunaFranjaEstaCompleta() {
        TableroOcupacion tablero = tableroCon(new ArrayList<>());

        assertFalse(tablero.hayReservas());
        for (Franja franja : Franja.values()) {
            assertEquals(3, tablero.contarMesasLibres(franja));
            assertFalse(tablero.estaCompleta(franja));
        }
    }

    /** HU-03 / Escenario 2.3: con las tres mesas reservadas a las 13:00, esa franja está completa. */
    @Test
    void franjaConTodasLasMesasReservadasEstaCompleta() {
        TableroOcupacion tablero = tableroCon(Arrays.asList(
                new Reserva(cliente("1712345678", "Andrea", "Gómez"), mesas.get(0), hoy, Franja.F13_00),
                new Reserva(cliente("1798765432", "Luis", "Torres"), mesas.get(1), hoy, Franja.F13_00),
                new Reserva(cliente("1755555555", "Carla", "Ruiz"), mesas.get(2), hoy, Franja.F13_00)));

        assertTrue(tablero.hayReservas());
        assertTrue(tablero.estaCompleta(Franja.F13_00));
        assertEquals(0, tablero.contarMesasLibres(Franja.F13_00));
        assertFalse(tablero.estaCompleta(Franja.F12_00));
    }
}