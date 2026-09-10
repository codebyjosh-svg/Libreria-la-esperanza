package org.esperanza.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.esperanza.dao.VentaDao;

public class CarritoVenta {

    private final Map<String, DetalleVenta> productos =
            new LinkedHashMap<>();

    public void agregarProducto(
            String isbn,
            int cantidad,
            BigDecimal precioUnitario) {

        if (isbn == null || isbn.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "El ISBN es obligatorio"
            );
        }

        BigDecimal precio =
                Objects.requireNonNull(
                        precioUnitario
                ).setScale(
                        2,
                        RoundingMode.UNNECESSARY
                );

        if (precio.signum() < 0) {

            throw new IllegalArgumentException(
                    "El precio no puede ser negativo"
            );
        }

        String isbnLimpio =
                isbn.trim();

        DetalleVenta nuevo =
                new DetalleVenta(
                        0,
                        0,
                        isbnLimpio,
                        cantidad,
                        precio
                );

        DetalleVenta actual =
                productos.get(isbnLimpio);

        if (actual != null) {

            if (actual
                    .getPrecioUnitario()
                    .compareTo(precio) != 0) {

                throw new IllegalArgumentException(
                        "El libro ya tiene otro precio"
                );
            }

            nuevo.setCantidad(
                    Math.addExact(
                            actual.getCantidad(),
                            cantidad
                    )
            );
        }

        productos.put(
                isbnLimpio,
                nuevo
        );
    }

    public void cambiarCantidad(
            String isbn,
            int cantidad) {

        DetalleVenta detalle =
                productos.get(isbn);

        if (detalle == null) {

            throw new IllegalArgumentException(
                    "El libro no esta en el carrito"
            );
        }

        detalle.setCantidad(cantidad);
    }

    public boolean quitarProducto(
            String isbn) {

        return productos.remove(isbn) != null;
    }

    public boolean estaVacio() {

        return productos.isEmpty();
    }

    public void vaciar() {

        productos.clear();
    }

    public List<DetalleVenta> getDetalles() {

        List<DetalleVenta> copia =
                new ArrayList<>();

        for (DetalleVenta detalle
                : productos.values()) {

            copia.add(
                    new DetalleVenta(
                            0,
                            0,
                            detalle.getIsbn(),
                            detalle.getCantidad(),
                            detalle.getPrecioUnitario()
                    )
            );
        }

        return Collections.unmodifiableList(
                copia
        );
    }

    public BigDecimal getTotal() {

        return productos
                .values()
                .stream()
                .map(
                        DetalleVenta::getSubtotal
                )
                .reduce(
                        new BigDecimal("0.00"),
                        BigDecimal::add
                );
    }

    public Venta confirmarVenta(
            long cuiCliente,
            int idUsuario,
            VentaDao ventaDao)
            throws SQLException {

        Venta venta =
                Objects.requireNonNull(
                        ventaDao
                ).registrar(
                        cuiCliente,
                        idUsuario,
                        getDetalles()
                );

        vaciar();

        return venta;
    }
}