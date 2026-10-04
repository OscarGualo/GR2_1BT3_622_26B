package ec.edu.epn.polidinner.controlador;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ec.edu.epn.polidinner.modelo.PersonalComedor;
import ec.edu.epn.polidinner.persistencia.PersonalComedorDAO;

/**
 * Controlador técnico de inicio de sesión del personal del comedor (no modelado en el diagrama de robustez).
 * Trazabilidad: Diagrama de secuencia CU01 - nota "personal obtenido de la sesión (precondición: login)".
 * Deja el PersonalComedor autenticado en la sesión con el atributo "personal".
 */
@WebServlet("/personal/login")
public class LoginPersonalServlet extends HttpServlet {

    private static final String VISTA = "/vistas/personal/login.jsp";
    private final PersonalComedorDAO personalDAO = new PersonalComedorDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession sesion = request.getSession(false);
        if (sesion != null && sesion.getAttribute("personal") != null) {
            response.sendRedirect(request.getContextPath() + "/vistas/personal/panel.jsp");
            return;
        }
        request.getRequestDispatcher(VISTA).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String cedula = request.getParameter("cedula");
        String clave = request.getParameter("clave");

        PersonalComedor personal = personalDAO.buscarPorCedula(cedula);
        if (personal == null || !personal.verificarClave(clave)) {
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
        HttpSession sesion = request.getSession(true);
        sesion.setAttribute("personal", personal);
        response.sendRedirect(request.getContextPath() + "/vistas/personal/panel.jsp");
    }
}
