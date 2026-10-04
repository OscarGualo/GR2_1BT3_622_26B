package ec.edu.epn.polidinner.controlador;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ec.edu.epn.polidinner.modelo.ClienteUniversitario;
import ec.edu.epn.polidinner.persistencia.ClienteUniversitarioDAO;

/**
 * Controlador técnico de inicio de sesión del cliente universitario (no modelado en el diagrama de robustez).
 * Trazabilidad: Diagrama de secuencia CU03 - nota "cliente obtenido de la sesión (precondición: login)".
 * Deja el ClienteUniversitario autenticado en la sesión con el atributo "cliente".
 */
@WebServlet("/cliente/login")
public class LoginClienteServlet extends HttpServlet {

    private static final String VISTA = "/vistas/cliente/login.jsp";
    private final ClienteUniversitarioDAO clienteDAO = new ClienteUniversitarioDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession sesion = request.getSession(false);
        if (sesion != null && sesion.getAttribute("cliente") != null) {
            response.sendRedirect(request.getContextPath() + "/cliente/pedido");
            return;
        }
        request.getRequestDispatcher(VISTA).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String cedula = request.getParameter("cedula");
        String clave = request.getParameter("clave");

        ClienteUniversitario cliente = clienteDAO.buscarPorCedula(cedula);
        if (cliente == null || !cliente.verificarClave(clave)) {
            request.setAttribute("error", "Cédula o clave incorrecta. Verifique sus datos e intente de nuevo.");
            request.setAttribute("cedula", cedula);
            request.getRequestDispatcher(VISTA).forward(request, response);
            return;
        }

        // Nueva sesión tras autenticarse (evita fijación de sesión)
        HttpSession anterior = request.getSession(false);
        if (anterior != null) {
            anterior.invalidate();
        }
        request.getSession(true).setAttribute("cliente", cliente);
        response.sendRedirect(request.getContextPath() + "/cliente/pedido");
    }
}
