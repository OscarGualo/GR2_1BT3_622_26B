package ec.edu.epn.polidinner.persistencia;

import ec.edu.epn.polidinner.modelo.ClienteUniversitario;

/**
 * Soporte técnico de acceso a datos para ClienteUniversitario (inicio de sesión del cliente y
 * consulta de saldo actualizado en el CU 03).
 */
public class ClienteUniversitarioDAO extends GenericDAO<ClienteUniversitario, String> {

    public ClienteUniversitarioDAO() {
        super(ClienteUniversitario.class);
    }

    /** La cédula es la clave primaria de ClienteUniversitario. */
    public ClienteUniversitario buscarPorCedula(String cedula) {
        if (cedula == null || cedula.trim().isEmpty()) {
            return null;
        }
        return buscarPorId(cedula.trim());
    }
}
