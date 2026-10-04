package ec.edu.epn.polidinner.controlador;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Filtro técnico (no modelado): exige que el personal del comedor haya iniciado sesión
 * para usar los controladores (/personal/*) y las vistas (/vistas/personal/*).
 * Trazabilidad: precondición "login" del Diagrama de secuencia CU01.
 */
@WebFilter({"/personal/*", "/vistas/personal/*"})
public class FiltroPersonal implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain cadena)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String ruta = request.getRequestURI().substring(request.getContextPath().length());
        boolean esLogin = ruta.equals("/personal/login") || ruta.equals("/vistas/personal/login.jsp");

        HttpSession sesion = request.getSession(false);
        boolean autenticado = sesion != null && sesion.getAttribute("personal") != null;

        if (esLogin || autenticado) {
            response.setHeader("Cache-Control", "no-store");
            cadena.doFilter(req, res);
        } else {
            response.sendRedirect(request.getContextPath() + "/personal/login");
        }
    }
}
