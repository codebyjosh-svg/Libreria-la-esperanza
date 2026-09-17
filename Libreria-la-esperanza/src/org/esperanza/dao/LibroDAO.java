package org.esperanza.dao;

import org.esperanza.model.Libro; 
import java.util.List;

public interface LibroDAO {

    Libro buscarPorIsbn(String isbn);

    List<Libro> buscarPorTitulo(String titulo);

    List<Libro> buscarPorAutor(String autor);

    List<Libro> listarTodos();

    // T3.3: Obtener libros con stock crítico (stock_actual <= stock_minimo)
    List<Libro> obtenerStockCritico();

    List<Libro> obtenerTodos();

    // Método requerido para el registro de entradas y movimientos de inventario
    boolean registrarMovimiento(String isbn, String tipoMovimiento, int cantidad, int idUsuario, String observacion);
}







