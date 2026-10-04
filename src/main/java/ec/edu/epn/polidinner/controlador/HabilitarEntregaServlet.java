package ec.edu.epn.polidinner.controlador;

import java.io.IOException;
import java.text.SimpleDateFormat;

import javax.persistence.OptimisticLockException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.hibernate.StaleStateException;

import ec.edu.epn.polidinner.modelo.Comprobante;
import ec.edu.epn.polidinner.modelo.PersonalComedor;
import ec.edu.epn.polidinner.persistencia.ComprobanteDAO;
import ec.edu.epn.polidinner.persistencia.PersonalComedorDAO;
import ec.edu.epn.polidinner.persistencia.Transaccion;

/**
 * Trazabilidad: Diagrama de robustez (incremento 2) - controlador "Habilitar entrega de comida".
 * Diagrama de secuencia CU04 - participante "Controlador (Habilitar entrega de comida)".
 * doGet muestra el formulario de la "Interfaz del sistema de comida"; doPost recibe el mensaje 2
 * habilitarEntrega(codigo) y envía el resultado a la frontera "Salida" (mensajes 12-15).
 */
@WebServlet("/personal/entrega")
public class HabilitarEntregaServlet extends HttpServlet {

    private static final String VISTA = "/vistas/personal/habilitarEntrega.jsp";
    private static final String VISTA_SALIDA = "/vistas/comun/salida.jsp";
    /** Atributo de sesión con el resultado de la última validación (patrón POST-redirect-GET). */
    private static final String RESULTADO = "resultadoEntrega";

    private final ComprobanteDAO comprobanteDAO = new ComprobanteDAO();
    private final PersonalComedorDAO personalDAO = new PersonalComedorDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession sesion = request.getSession();
        ResultadoEntrega resultado = (ResultadoEntrega) sesion.getAttribute(RESULTADO);
        if ("resultado".equals(request.getParameter("vista")) && resultado != null) {
            sesion.removeAttribute(RESULTADO);
            mostrarSalida(request, response, resultado);
            return;
        }
        request.getRequestDispatcher(VISTA).forward(request, response);
    }

    /** Trazabilidad: Diagrama de secuencia CU04 - mensajes 1 (ingresa el código) y 2 habilitarEntrega(codigo). */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String codigo = normalizarCodigo(request.getParameter("codigo"));
        if (codigo == null) {
            request.setAttribute("errores", java.util.Collections.singletonMap("codigo",
                    "Ingrese el código del comprobante con el formato PD-0000."));
            request.getRequestDispatcher(VISTA).forward(request, response);
            return;
        }
        PersonalComedor personal = (PersonalComedor) request.getSession().getAttribute("personal");
        request.getSession().setAttribute(RESULTADO, habilitarEntrega(codigo, personal.getCedula()));
        response.sendRedirect(request.getContextPath() + "/personal/entrega?vista=resultado");
    }

    /**
     * Trazabilidad: Diagrama de secuencia CU04 - mensaje 2 habilitarEntrega(codigo), en una sola transacción:
     * 3-4 ComprobanteDAO.buscarPorCodigo(codigo) / comprobante;
     * alt [existe]: 5-8 personal.validarCodigo(codigo, comprobante) → comprobante.validarCodigoEntrega();
     *   alt [válido]: 9 personal.entregarPedido(pedido), 10 comprobante.marcarCodigoUsado(),
     *     11 ComprobanteDAO.actualizar(comprobante), commit y 12 "Pedido autorizado para despacho";
     *   alt [usado / de otro día / no pagado]: rollback y 13 motivo del rechazo;
     * alt [inexistente]: 14 "El código no existe".
     */
    private ResultadoEntrega habilitarEntrega(String codigo, String cedulaPersonal) {
        try (Transaccion tx = Transaccion.iniciar()) {
            Comprobante comprobante = comprobanteDAO.buscarPorCodigo(codigo, tx);
            if (comprobante == null) {
                return new ResultadoEntrega(false, codigo,
                        "El código " + codigo + " no existe. Verifique el comprobante que presenta el cliente.");
            }
            PersonalComedor personal = personalDAO.buscarPorId(cedulaPersonal, tx);
            if (!personal.validarCodigo(codigo, comprobante)) {
                return new ResultadoEntrega(false, codigo, motivoRechazo(comprobante));
            }
            personal.entregarPedido(comprobante.getPedido());
            comprobante.marcarCodigoUsado();
            comprobanteDAO.actualizar(comprobante, tx);
            tx.confirmar();
            return new ResultadoEntrega(true, codigo,
                    "Entregue el pedido N.º " + String.format("%04d", comprobante.getPedido().getId())
                            + " al cliente. El código " + codigo + " ya no se puede volver a usar.");
        } catch (RuntimeException e) {
            if (!esConflictoDeVersion(e)) {
                throw e;
            }
            return new ResultadoEntrega(false, codigo,
                    "Otro miembro del personal acaba de validar el código " + codigo + ". El pedido ya fue entregado.");
        }
    }

    /**
     * Trazabilidad: Diagrama de secuencia CU04 - mensajes 12-14 (la Salida recibe el resultado) y
     * 15 (la Salida muestra el resultado al personal).
     */
    private void mostrarSalida(HttpServletRequest request, HttpServletResponse response, ResultadoEntrega resultado)
            throws ServletException, IOException {
        Comprobante comprobante = comprobanteDAO.buscarPorCodigo(resultado.codigo);
        request.setAttribute("tipoSalida", "entrega");
        request.setAttribute("exitoSalida", resultado.exito);
        request.setAttribute("mensajeSalida", resultado.mensaje);
        if (comprobante != null) {
            request.setAttribute("pedido", comprobante.getPedido());
        }
        request.getRequestDispatcher(VISTA_SALIDA).forward(request, response);
    }

    /** Mensaje 13: motivo por el que validarCodigoEntrega() devolvió false. */
    private static String motivoRechazo(Comprobante comprobante) {
        if (comprobante.isUsado()) {
            return "El código " + comprobante.getCodigo() + " ya fue usado: ese pedido ya se entregó.";
        }
        if (!comprobante.esDeHoy()) {
            return "El código " + comprobante.getCodigo() + " es del "
                    + new SimpleDateFormat("dd/MM/yyyy").format(comprobante.getFechaGeneracion())
                    + "; solo se aceptan comprobantes del día.";
        }
        return "El pedido del código " + comprobante.getCodigo() + " no está pagado.";
    }

    /** Acepta "pd-0001", " PD-1 " o solo el número ("1"), y lo lleva al formato PD-0000. */
    static String normalizarCodigo(String valor) {
        if (valor == null) {
            return null;
        }
        String limpio = valor.trim().toUpperCase().replace(" ", "");
        String numero = limpio.startsWith("PD-") ? limpio.substring(3) : limpio;
        if (!numero.matches("\\d{1,6}")) {
            return null;
        }
        return "PD-" + String.format("%04d", Integer.parseInt(numero));
    }

    private static boolean esConflictoDeVersion(Throwable e) {
        for (Throwable causa = e; causa != null; causa = causa.getCause()) {
            if (causa instanceof OptimisticLockException || causa instanceof StaleStateException) {
                return true;
            }
        }
        return false;
    }

    /** Resultado de la validación que se lleva a la Salida (soporte técnico del patrón POST-redirect-GET). */
    private static final class ResultadoEntrega implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        private final boolean exito;
        private final String codigo;
        private final String mensaje;

        private ResultadoEntrega(boolean exito, String codigo, String mensaje) {
            this.exito = exito;
            this.codigo = codigo;
            this.mensaje = mensaje;
        }
    }
}
