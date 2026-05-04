package ar.edu.itba.paw.models;

import java.util.Optional;

public enum ProductSortOrder {

    NEWEST,
    OLDEST,
    PRICE_ASC,
    PRICE_DESC,
    NAME_ASC,
    NAME_DESC,
    CONDITION_DESC,
    CONDITION_ASC;

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
