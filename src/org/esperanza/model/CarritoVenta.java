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

        String isbnLimpio =
                validarIsbn(isbn);

        validarCantidad(cantidad);

        BigDecimal precio =
                validarPrecio(precioUnitario);

        DetalleVenta actual =
                productos.get(isbnLimpio);

        if (actual != null) {

            if (actual
                    .getPrecioUnitario()
                    .compareTo(precio) != 0) {

                throw new IllegalArgumentException(
                        "El libro ya existe en el carrito con otro precio."
                );
            }

            int nuevaCantidad;

            try {

                nuevaCantidad =
                        Math.addExact(
                                actual.getCantidad(),
                                cantidad
                        );

            } catch (ArithmeticException e) {

                throw new IllegalArgumentException(
                        "La cantidad ingresada es demasiado grande."
                );
            }

            actual.setCantidad(
                    nuevaCantidad
            );

            return;
        }

        DetalleVenta nuevo =
                new DetalleVenta(
                        0,
                        0,
                        isbnLimpio,
                        cantidad,
                        precio
                );

        productos.put(
                isbnLimpio,
                nuevo
        );
    }

    public void cambiarCantidad(
            String isbn,
            int cantidad) {

        String isbnLimpio =
                validarIsbn(isbn);

        validarCantidad(cantidad);

        DetalleVenta detalle =
                productos.get(
                        isbnLimpio
                );

        if (detalle == null) {

            throw new IllegalArgumentException(
                    "El libro no está en el carrito."
            );
        }

        detalle.setCantidad(
                cantidad
        );
    }

    public boolean quitarProducto(
            String isbn) {

        String isbnLimpio =
                validarIsbn(isbn);

        return productos.remove(
                isbnLimpio
        ) != null;
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

            if (detalle == null) {
                continue;
            }

            DetalleVenta copiaDetalle =
                    new DetalleVenta(
                            0,
                            0,
                            detalle.getIsbn(),
                            detalle.getCantidad(),
                            detalle.getPrecioUnitario()
                    );

            copia.add(
                    copiaDetalle
            );
        }

        return Collections.unmodifiableList(
                copia
        );
    }

    public BigDecimal getTotal() {

        BigDecimal total =
                BigDecimal.ZERO;

        for (DetalleVenta detalle
                : productos.values()) {

            if (detalle == null) {
                continue;
            }

            BigDecimal subtotal =
                    detalle.getSubtotal();

            if (subtotal != null) {

                total =
                        total.add(
                                subtotal
                        );
            }
        }

        return total.setScale(
                2,
                RoundingMode.HALF_UP
        );
    }

    public Venta confirmarVenta(
            long cuiCliente,
            int idUsuario,
            VentaDao ventaDao)
            throws SQLException {

        return confirmarVenta(
                cuiCliente,
                idUsuario,
                ventaDao,
                DescuentoVenta.sinDescuento()
        );
    }

    public Venta confirmarVenta(
            long cuiCliente,
            int idUsuario,
            VentaDao ventaDao,
            DescuentoVenta descuento)
            throws SQLException {

        if (estaVacio()) {

            throw new IllegalArgumentException(
                    "No hay productos en el carrito."
            );
        }

        if (cuiCliente <= 0) {

            throw new IllegalArgumentException(
                    "El CUI del cliente no es válido."
            );
        }

        if (idUsuario <= 0) {

            throw new IllegalArgumentException(
                    "El usuario no es válido."
            );
        }

        if (ventaDao == null) {

            throw new IllegalArgumentException(
                    "No se pudo acceder al registro de ventas."
            );
        }

        /*
         * Creamos la copia antes de guardar.
         * Si falla la base de datos,
         * el carrito conserva sus productos.
         */
        List<DetalleVenta> detalles =
                getDetalles();

        DescuentoVenta descuentoAplicado =
                Objects.requireNonNull(
                        descuento,
                        "El descuento no puede ser nulo."
                );

        Venta venta =
                ventaDao.registrar(
                        cuiCliente,
                        idUsuario,
                        detalles,
                        descuentoAplicado
                );

        if (venta == null) {

            throw new SQLException(
                    "No fue posible registrar la venta."
            );
        }

        /*
         * El carrito solamente se vacía
         * después de registrar correctamente.
         */
        vaciar();

        return venta;
    }

    private String validarIsbn(
            String isbn) {

        if (isbn == null
                || isbn.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "El ISBN es obligatorio."
            );
        }

        return isbn.trim();
    }

    private void validarCantidad(
            int cantidad) {

        if (cantidad <= 0) {

            throw new IllegalArgumentException(
                    "La cantidad debe ser mayor que cero."
            );
        }
    }

    private BigDecimal validarPrecio(
            BigDecimal precioUnitario) {

        if (precioUnitario == null) {

            throw new IllegalArgumentException(
                    "El precio es obligatorio."
            );
        }

        if (precioUnitario.compareTo(
                BigDecimal.ZERO
        ) <= 0) {

            throw new IllegalArgumentException(
                    "El precio debe ser mayor que cero."
            );
        }

        if (precioUnitario.scale() > 2) {

            throw new IllegalArgumentException(
                    "El precio puede tener máximo dos decimales."
            );
        }

        return precioUnitario.setScale(
                2,
                RoundingMode.HALF_UP
        );
    }
}