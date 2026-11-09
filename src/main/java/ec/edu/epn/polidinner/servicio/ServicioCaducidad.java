package ec.edu.epn.polidinner.servicio;

import java.time.LocalDate;
import java.util.List;

import ec.edu.epn.polidinner.modelo.ClasificadorCaducidad;
import ec.edu.epn.polidinner.modelo.ExistenciaProducto;
import ec.edu.epn.polidinner.modelo.FiltroCaducidad;
import ec.edu.epn.polidinner.modelo.FranjaCaducidad;
import ec.edu.epn.polidinner.persistencia.ProductoDAO;

/**
 * Soporte técnico NO modelado - HU19: existencias de la franja elegida a partir de los registros de
 * Producto (habilitados o no; el filtro decide).
 */
public class ServicioCaducidad {

    private final ProductoDAO productoDAO;
    private final FiltroCaducidad filtro;

    public ServicioCaducidad() {
        this(new ProductoDAO(), new FiltroCaducidad(new ClasificadorCaducidad()));
    }

    ServicioCaducidad(ProductoDAO productoDAO, FiltroCaducidad filtro) {
        this.productoDAO = productoDAO;
        this.filtro = filtro;
    }

    public List<ExistenciaProducto> consultar(FranjaCaducidad franja, LocalDate hoy) {
        return filtro.filtrar(consultarExistenciasGuardadas(), franja, hoy);
    }

    private List<ExistenciaProducto> consultarExistenciasGuardadas() {
        return productoDAO.listarTodos().stream().map(ExistenciaProducto::desde).toList();
    }
}