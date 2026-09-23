package org.esperanza.Model;

import java.math.BigDecimal;

public class StockValorizado {

    private String isbn;
    private String titulo;
    private int stockActual;
    private BigDecimal precio;
    private BigDecimal valorInventario;

    public StockValorizado(
            String isbn,
            String titulo,
            int stockActual,
            BigDecimal precio,
            BigDecimal valorInventario) {

        this.isbn = isbn;
        this.titulo = titulo;
        this.stockActual = stockActual;
        this.precio = precio;
        this.valorInventario = valorInventario;
    }

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

    public int getStockActual() {
        return stockActual;
    }

    public void setStockActual(int stockActual) {
        this.stockActual = stockActual;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public BigDecimal getValorInventario() {
        return valorInventario;
    }

    public void setValorInventario(BigDecimal valorInventario) {
        this.valorInventario = valorInventario;
    }
}