
package org.esperanza.Dao;

import org.esperanza.Model.Libro; 
import java.util.List;

public interface LibroDAO {

    Libro buscarPorIsbn(String isbn);
    

    List<Libro> buscarPorTitulo(String titulo);
    

    List<Libro> buscarPorAutor(String autor);
    

    List<Libro> listarTodos();
}