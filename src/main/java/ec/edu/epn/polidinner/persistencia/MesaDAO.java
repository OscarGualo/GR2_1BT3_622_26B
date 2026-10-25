package ec.edu.epn.polidinner.persistencia;

import ec.edu.epn.polidinner.modelo.Mesa;

/**
 * Trazabilidad: Diagramas de robustez HU-01 y HU-03 - acceso a la entidad Mesa (T1.1).
 * listarTodos() se hereda de GenericDAO.
 */
public class MesaDAO extends GenericDAO<Mesa, Integer> {

    public MesaDAO() {
        super(Mesa.class);
    }
}