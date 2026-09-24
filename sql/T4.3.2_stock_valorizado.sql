SELECT
    isbn,
    titulo,
    stock_actual,
    precio,
    CAST(
        stock_actual * precio
        AS DECIMAL(14,2)
    ) AS valor_inventario
FROM libros
WHERE activo = TRUE
ORDER BY
    valor_inventario DESC,
    titulo ASC;