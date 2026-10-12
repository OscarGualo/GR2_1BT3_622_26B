package ec.edu.epn.polidinner.controlador;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * Soporte técnico NO modelado – Refactorización: Extract Class
 */
public class FormularioItemMenu {

    private final String tipo;
    private final String nombre;
    private final String descripcion;
    private final double precio;
    private final int stock;
    private final Date fechaCaducidad;
    private final Map<String, String> errores;

    private FormularioItemMenu(String tipo, String nombre, String descripcion, double precio, int stock,
                               Date fechaCaducidad, Map<String, String> errores) {
        this.tipo = tipo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.stock = stock;
        this.fechaCaducidad = fechaCaducidad;
        this.errores = errores;
    }

    public static FormularioItemMenu desde(HttpServletRequest request) {
        String tipo = "Producto".equals(request.getParameter("tipo")) ? "Producto" : "Plato";
        String nombre = recortar(request.getParameter("nombre"));
        String descripcion = recortar(request.getParameter("descripcion"));
        String textoPrecio = recortar(request.getParameter("precio"));
        String textoStock = recortar(request.getParameter("stock"));
        String textoFecha = recortar(request.getParameter("fechaCaducidad"));

        Map<String, String> errores = new LinkedHashMap<>();
        if (nombre.isEmpty()) {
            errores.put("nombre", "Ingrese el nombre del ítem.");
        } else if (nombre.length() > 100) {
            errores.put("nombre", "El nombre admite máximo 100 caracteres.");
        }
        if (descripcion.length() > 255) {
            errores.put("descripcion", "La descripción admite máximo 255 caracteres.");
        }

        double precio = 0;
        try {
            precio = Double.parseDouble(textoPrecio.replace(',', '.'));
            if (!(precio > 0)) {
                errores.put("precio", "El precio debe ser mayor que 0.");
            }
        } catch (NumberFormatException e) {
            errores.put("precio", "Ingrese un precio numérico, por ejemplo 2.50.");
        }

        // Plato: porciones disponibles; Producto: unidades en stock (ItemMenu.stockActual, Fig. 15)
        String nombreStock = "Producto".equals(tipo) ? "stock" : "número de porciones";
        int stock = 0;
        try {
            stock = Integer.parseInt(textoStock);
            if (stock < 1) {
                errores.put("stock", "El " + nombreStock + " debe ser al menos 1.");
            } else if (stock > 10000) {
                errores.put("stock", "El " + nombreStock + " admite máximo 10000.");
            }
        } catch (NumberFormatException e) {
            errores.put("stock", "Ingrese el " + nombreStock + " como número entero.");
        }
        Date fechaCaducidad = null;
        if ("Producto".equals(tipo)) {
            try {
                SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd");
                formato.setLenient(false);
                fechaCaducidad = formato.parse(textoFecha);
                if (fechaCaducidad.before(inicioDeHoy())) {
                    errores.put("fechaCaducidad", "La fecha de caducidad no puede ser anterior a hoy.");
                }
            } catch (ParseException e) {
                errores.put("fechaCaducidad", "Seleccione la fecha de caducidad del producto.");
            }
        }

        return new FormularioItemMenu(tipo, nombre, descripcion, precio, stock, fechaCaducidad, errores);
    }

    public boolean esValido() {
        return errores.isEmpty();
    }

    public String getTipo() {
        return tipo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public double getPrecio() {
        return precio;
    }

    public int getStock() {
        return stock;
    }

    public Date getFechaCaducidad() {
        return fechaCaducidad;
    }

    public Map<String, String> getErrores() {
        return errores;
    }

    private static String recortar(String valor) {
        return valor == null ? "" : valor.trim();
    }

    private static Date inicioDeHoy() {
        Calendar hoy = Calendar.getInstance();
        hoy.set(Calendar.HOUR_OF_DAY, 0);
        hoy.set(Calendar.MINUTE, 0);
        hoy.set(Calendar.SECOND, 0);
        hoy.set(Calendar.MILLISECOND, 0);
        return hoy.getTime();
    }
}
