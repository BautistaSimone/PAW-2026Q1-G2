package ar.edu.itba.paw.webapp.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Parses the {@code imageLayout} field from the product edit form.
 * Tokens are comma-separated: {@code e:123} for an existing image id, {@code n} for a new upload slot
 * (matched in order with non-empty multipart files).
 */
public final class ProductImageLayoutParser {

    public enum SlotKind {
        EXISTING,
        NEW
    }

    public static final class Slot {
        private final SlotKind kind;
        private final Long existingImageId;

        private Slot(final SlotKind kind, final Long existingImageId) {
            this.kind = kind;
            this.existingImageId = existingImageId;
        }

        public static Slot existing(final long imageId) {
            return new Slot(SlotKind.EXISTING, imageId);
        }

        public static Slot newFile() {
            return new Slot(SlotKind.NEW, null);
        }

        public SlotKind getKind() {
            return kind;
        }

        public long getExistingImageId() {
            if (kind != SlotKind.EXISTING || existingImageId == null) {
                throw new IllegalStateException();
            }
            return existingImageId;
        }
    }

    private ProductImageLayoutParser() {
        throw new AssertionError();
    }

    public static List<Slot> parse(final String raw) {
        if (raw == null) {
            return Collections.emptyList();
        }
        final String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            return Collections.emptyList();
        }
        final String[] parts = trimmed.split(",");
        final List<Slot> out = new ArrayList<>(parts.length);
        for (String part : parts) {
            final String p = part.trim();
            if (p.isEmpty()) {
                continue;
            }
            if ("n".equals(p)) {
                out.add(Slot.newFile());
            } else if (p.startsWith("e:")) {
                final String idPart = p.substring(2).trim();
                if (idPart.isEmpty()) {
                    throw new IllegalArgumentException("empty id");
                }
                out.add(Slot.existing(Long.parseLong(idPart)));
            } else {
                throw new IllegalArgumentException("bad token");
            }
        }
        return out;
    }
}