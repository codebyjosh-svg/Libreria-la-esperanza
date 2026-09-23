package org.esperanza.dao;

import java.util.List;
import org.esperanza.model.Libro;
import java.math.BigDecimal;

public interface LibroDAO {

    List<Libro> listarTodos();

    Libro buscarLibro(String isbn);

    Libro buscarPorIsbn(String isbn);

    List<Libro> buscarPorTitulo(String titulo);

    List<Libro> buscarPorAutor(String autor);

    List<Libro> obtenerStockCritico();

    boolean insertar(Libro libro);

    boolean actualizar(Libro libro);

    boolean eliminar(String isbn);
    
    boolean actualizarPrecio(String isbn, BigDecimal nuevoPrecio);
}