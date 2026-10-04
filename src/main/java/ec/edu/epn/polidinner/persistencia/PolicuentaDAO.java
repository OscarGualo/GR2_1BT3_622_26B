package ec.edu.epn.polidinner.persistencia;

import ec.edu.epn.polidinner.modelo.Policuenta;

/**
 * Soporte técnico de acceso a datos para Policuenta (simulación del sistema externo PoliCuenta).
 */
public class PolicuentaDAO extends GenericDAO<Policuenta, String> {

    public PolicuentaDAO() {
        super(Policuenta.class);
    }

    /** El número de PoliCuenta es la clave primaria. */
    public Policuenta buscarPorNumero(String numero) {
        if (numero == null || numero.trim().isEmpty()) {
            return null;
        }
        return buscarPorId(numero.trim());
    }
}
