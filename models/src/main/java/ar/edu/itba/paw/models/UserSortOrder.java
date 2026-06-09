package ar.edu.itba.paw.models;

public enum UserSortOrder {
    FOLLOWERS_DESC,
    FOLLOWERS_ASC,
    PRODUCTS_DESC,
    PRODUCTS_ASC,
    RATING_DESC,
    RATING_ASC;

    public static UserSortOrder parse(final String raw) {
        if (raw == null || raw.isBlank()) {
            return FOLLOWERS_DESC;
        }
        try {
            return UserSortOrder.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return FOLLOWERS_DESC;
        }
    }

    public String getParamValue() {
        return this.name().toLowerCase();
    }
}