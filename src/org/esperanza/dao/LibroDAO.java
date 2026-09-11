package org.esperanza.dao;

import java.util.List;
import org.esperanza.model.Libro;

public interface LibroDAO {
    List<Libro> listarTodos();
    Libro buscarLibro(String isbn);
    boolean insertar(Libro libro);
    boolean actualizar(Libro libro);
    boolean eliminar(String isbn); 
}













