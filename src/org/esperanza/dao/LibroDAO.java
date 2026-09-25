package org.esperanza.dao;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

import org.esperanza.Model.Libro;


public interface LibroDAO {


    /**
     * Obtiene los libros disponibles para venta.
     * 
     * @return lista de libros con stock disponible
     * @throws SQLException error de conexión o consulta
     */
    List<Libro> listarDisponibles() throws SQLException;



    /**
     * Obtiene todos los libros registrados.
     * 
     * @return lista completa de libros
     */
    List<Libro> listarTodos();



    /**
     * Busca un libro por su ISBN.
     * 
     * @param isbn identificador del libro
     * @return libro encontrado o null
     */
    Libro buscarLibro(String isbn);



    /**
     * Busca un libro por ISBN.
     * 
     * @param isbn identificador del libro
     * @return libro encontrado o null
     */
    Libro buscarPorIsbn(String isbn);



    /**
     * Busca libros por título.
     * 
     * @param titulo texto a buscar
     * @return lista de libros encontrados
     */
    List<Libro> buscarPorTitulo(String titulo);



    /**
     * Busca libros por autor.
     * 
     * @param autor nombre del autor
     * @return lista de libros encontrados
     */
    List<Libro> buscarPorAutor(String autor);



    /**
     * Obtiene libros con stock crítico.
     * 
     * @return libros donde stock actual es menor o igual al mínimo
     */
    List<Libro> obtenerStockCritico();



    /**
     * Inserta un nuevo libro.
     * 
     * @param libro libro a registrar
     * @return true si se insertó correctamente
     */
    boolean insertar(Libro libro);



    /**
     * Actualiza información de un libro.
     * 
     * @param libro libro con cambios
     * @return true si se actualizó correctamente
     */
    boolean actualizar(Libro libro);



    /**
     * Elimina o desactiva un libro.
     * 
     * @param isbn identificador del libro
     * @return true si se eliminó correctamente
     */
    boolean eliminar(String isbn);



    /**
     * Actualiza únicamente el precio del libro.
     * 
     * @param isbn identificador del libro
     * @param nuevoPrecio nuevo precio
     * @return true si se actualizó correctamente
     */
    boolean actualizarPrecio(String isbn, BigDecimal nuevoPrecio);

}