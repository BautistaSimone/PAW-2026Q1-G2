package ar.edu.itba.paw.models;

import java.util.Optional;

public enum ConditionBucket {
    EXCELENTE,
    MUY_BUENO,
    BUENO,
    REGULAR;

    public static Optional<ConditionBucket> parse(final String raw) {
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
