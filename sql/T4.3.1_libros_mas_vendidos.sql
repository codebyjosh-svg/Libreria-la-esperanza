SELECT
    l.isbn,
    l.titulo,
    SUM(dv.cantidad) AS cantidad_vendida,
    COALESCE(SUM(dv.subtotal), 0) AS total_vendido
FROM detalle_venta dv
INNER JOIN ventas v
    ON v.id_venta = dv.id_venta
INNER JOIN libros l
    ON l.isbn = dv.isbn
WHERE v.estado = 'COMPLETADA'
GROUP BY
    l.isbn,
    l.titulo
ORDER BY
    cantidad_vendida DESC,
    total_vendido DESC,
    l.titulo ASC;