package ec.edu.epn.polidinner.persistencia;

import ec.edu.epn.polidinner.modelo.PersonalComedor;

/**
 * Soporte técnico de acceso a datos para PersonalComedor (usado por el inicio de sesión).
 */
public class PersonalComedorDAO extends GenericDAO<PersonalComedor, String> {

    public PersonalComedorDAO() {
        super(PersonalComedor.class);
    }

    /** La cédula es la clave primaria de PersonalComedor. */
    public PersonalComedor buscarPorCedula(String cedula) {
        if (cedula == null || cedula.trim().isEmpty()) {
            return null;
        }
        return buscarPorId(cedula.trim());
    }
}
