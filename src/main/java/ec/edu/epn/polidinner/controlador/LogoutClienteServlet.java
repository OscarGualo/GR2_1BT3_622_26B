package ec.edu.epn.polidinner.controlador;

import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Controlador técnico de cierre de sesión del cliente universitario (no modelado en el diagrama de robustez).
 * Un pedido en armado (no pagado) vive en la sesión y se descarta al salir.
 */
@WebServlet("/cliente/logout")
public class LogoutClienteServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession sesion = request.getSession(false);
        if (sesion != null) {
            sesion.invalidate();
        }
        response.sendRedirect(request.getContextPath() + "/cliente/login");
    }
}
