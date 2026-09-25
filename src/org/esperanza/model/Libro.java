package org.esperanza.Model;

import java.util.Date;
import java.math.BigDecimal;

public class Libro {

    private String isbn;
    private String titulo;
    private int stockActual;
    private int stockMinimo;
    private Date fechaPublicacion;
    private int idCategoria;
    private String nitEditorial;
    private int idProveedor;
    private boolean activo;
    private double precio;

    public Libro() {
    }

    public Libro(String isbn, String titulo, int stockActual, int stockMinimo, Date fechaPublicacion, int idCategoria, String nitEditorial, int idProveedor, boolean activo) {
        this.isbn = isbn;
        this.titulo = titulo;
        this.stockActual = stockActual;
        this.stockMinimo = stockMinimo;
        this.fechaPublicacion = fechaPublicacion;
        this.idCategoria = idCategoria;
        this.nitEditorial = nitEditorial;
        this.idProveedor = idProveedor;
        this.activo = activo;
    }

    // --- Getters y Setters ---
    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public int getStock() {
        return stockActual;
    }

    public void setStock(int stock) {
        this.stockActual = stock;
    }

    public int getStockActual() {
        return stockActual;
    }

    public void setStockActual(int stockActual) {
        this.stockActual = stockActual;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio.doubleValue();
    }

    public int getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(int stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public Date getFechaPublicacion() {
        return fechaPublicacion;
    }

    public void setFechaPublicacion(Date fechaPublicacion) {
        this.fechaPublicacion = fechaPublicacion;
    }

    public int getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(int idCategoria) {
        this.idCategoria = idCategoria;
    }

    public String getNitEditorial() {
        return nitEditorial;
    }

    public void setNitEditorial(String nitEditorial) {
        this.nitEditorial = nitEditorial;
    }

    public int getIdProveedor() {
        return idProveedor;
    }

    public void setIdProveedor(int idProveedor) {
        this.idProveedor = idProveedor;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    // --- Métodos de Negocio ---
    public boolean esStockCritico() {
        return this.stockActual <= this.stockMinimo;
    }
}
