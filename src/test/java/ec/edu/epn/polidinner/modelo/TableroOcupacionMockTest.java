package ec.edu.epn.polidinner.modelo;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import ec.edu.epn.polidinner.persistencia.MesaDAO;
import ec.edu.epn.polidinner.persistencia.ReservaDAO;

/**
 * HU-03 Ver la ocupación de mesas del día - prueba con mocks PU-05 (tareas T3.1, T3.2 y T3.3).
 * Se prueba un TableroOcupacion real; MesaDAO y ReservaDAO son objetos simulados con Mockito,
 * para no depender de la base de datos.
 */
class TableroOcupacionMockTest {

    /** PU-05 (HU-03 / Escenarios 1.1 y 2.2): el tablero muestra titulares y cuenta mesas libres por franja. */
    @Test
    void construirTableroMuestraTitularesYCuentaMesasLibresPorFranja() {
        LocalDate hoy = LocalDate.of(2026, 10, 26);
        ClienteUniversitario andrea = new ClienteUniversitario("1712345678", "Andrea", "Gómez", "clave",
                new Policuenta("2024-0153", 10.00));
        ClienteUniversitario luis = new ClienteUniversitario("1798765432", "Luis", "Torres", "clave",
                new Policuenta("2024-0201", 8.00));
        Mesa mesa1 = new Mesa(1);
        Mesa mesa2 = new Mesa(2);
        Mesa mesa3 = new Mesa(3);

        // Objetos simulados (mocks) del acceso a datos
        MesaDAO mesaDAO = Mockito.mock(MesaDAO.class);
        ReservaDAO reservaDAO = Mockito.mock(ReservaDAO.class);

        // Respuestas preparadas: 3 mesas y 2 reservas activas de hoy
        Mockito.when(mesaDAO.listarTodos()).thenReturn(Arrays.asList(mesa1, mesa2, mesa3));
        Mockito.when(reservaDAO.buscarActivasPorFecha(hoy)).thenReturn(Arrays.asList(
                new Reserva(andrea, mesa1, hoy, Franja.F12_30),
                new Reserva(luis, mesa3, hoy, Franja.F13_00)));

        TableroOcupacion tablero = new TableroOcupacion(hoy, mesaDAO, reservaDAO);

        // Escenario 1.1: celdas reservadas con su titular y celdas libres
        assertEquals("Reservada · Andrea Gómez", tablero.obtenerEstado(1, Franja.F12_30));
        assertEquals("Reservada · Luis Torres", tablero.obtenerEstado(3, Franja.F13_00));
        assertEquals("Libre", tablero.obtenerEstado(2, Franja.F12_00));
        assertEquals("Libre", tablero.obtenerEstado(1, Franja.F13_00));   // misma mesa, otra franja

        // Escenario 2.2: conteo de mesas libres por franja
        assertEquals(3, tablero.contarMesasLibres(Franja.F12_00));
        assertEquals(2, tablero.contarMesasLibres(Franja.F12_30));
        assertEquals(2, tablero.contarMesasLibres(Franja.F13_00));

        // El tablero pidió al repositorio solo las reservas activas de hoy
        Mockito.verify(reservaDAO).buscarActivasPorFecha(hoy);
    }
}
