package org.esperanza.Model;

import java.math.BigDecimal;

public class LibroMasVendido {

    private String isbn;
    private String titulo;
    private int cantidadVendida;
    private BigDecimal totalVendido;

    public LibroMasVendido(
            String isbn,
            String titulo,
            int cantidadVendida,
            BigDecimal totalVendido) {

        this.isbn = isbn;
        this.titulo = titulo;
        this.cantidadVendida = cantidadVendida;
        this.totalVendido = totalVendido;
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

    public int getCantidadVendida() {
        return cantidadVendida;
    }

    public void setCantidadVendida(int cantidadVendida) {
        this.cantidadVendida = cantidadVendida;
    }

    public BigDecimal getTotalVendido() {
        return totalVendido;
    }

    public void setTotalVendido(BigDecimal totalVendido) {
        this.totalVendido = totalVendido;
    }
}