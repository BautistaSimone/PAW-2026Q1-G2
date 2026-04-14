package ar.edu.itba.paw.models;

import java.util.Optional;

public enum ProductSortOrder {

    NEWEST("Más recientes", "p.published DESC, p.product_id DESC"),
    OLDEST("Más antiguos", "p.published ASC, p.product_id ASC"),
    PRICE_ASC("Precio: menor a mayor", "p.price ASC"),
    PRICE_DESC("Precio: mayor a menor", "p.price DESC"),
    NAME_ASC("Nombre: A - Z", "LOWER(p.title) ASC"),
    NAME_DESC("Nombre: Z - A", "LOWER(p.title) DESC"),
    CONDITION_DESC("Estado: mejor a peor", "(p.sleeve_condition + p.record_condition) / 2.0 DESC"),
    CONDITION_ASC("Estado: peor a mejor", "(p.sleeve_condition + p.record_condition) / 2.0 ASC");

    private final String label;
    private final String sqlOrderBy;

    ProductSortOrder(final String label, final String sqlOrderBy) {
        this.label = label;
        this.sqlOrderBy = sqlOrderBy;
    }

    public String getLabel() {
        return label;
    }

    public String getSqlOrderBy() {
        return sqlOrderBy;
    }

    public static Optional<ProductSortOrder> parse(final String raw) {
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(valueOf(raw.trim().toUpperCase()));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }
}
