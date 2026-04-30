package ar.edu.itba.paw.models;

/**
 * Persistence state of a product row ({@code products.state}).
 */
public enum ProductState {
    /** Listed and purchasable in the catalog. */
    ACTIVE,
    /** Reserved atomically when a purchase is created (checkout in progress). */
    RESERVED,
    /** Sale completed (delivered). */
    SOLD,
    /** Removed from the catalog by the owner; restorable from trash. */
    USER_DELETED,
    /** Hidden by an admin or moderation action. */
    ADMIN_HIDDEN;

    public String getPersistenceValue() {
        return name();
    }

    public static ProductState fromPersistenceValue(final String raw) {
        if (raw == null || raw.isBlank()) {
            return ACTIVE;
        }
        return ProductState.valueOf(raw.trim());
    }
}
