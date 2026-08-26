
package org.esperanza.Dao;

import modelo.Libro;
import java.util.List;

public interface Dao {

    boolean insertar(Libro libro);

    boolean actualizar(Libro libro);

    boolean eliminar(int id);

    Libro buscarPorId(int id);

    List<Libro> listarTodos();
}
