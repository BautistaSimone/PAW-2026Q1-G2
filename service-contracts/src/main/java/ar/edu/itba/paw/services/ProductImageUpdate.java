package ar.edu.itba.paw.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ProductImageUpdate {

    public enum EntryKind {
        EXISTING,
        NEW
    }

    public static final class Entry {
        private final EntryKind kind;
        private final Long existingImageId;
        private final ProductImageData imageData;

        private Entry(final EntryKind kind, final Long existingImageId, final ProductImageData imageData) {
            this.kind = kind;
            this.existingImageId = existingImageId;
            this.imageData = imageData;
        }

        public EntryKind getKind() {
            return kind;
        }

        public Long getExistingImageId() {
            return existingImageId;
        }

        public ProductImageData getImageData() {
            return imageData;
        }
    }

    private final boolean replace;
    private final List<Entry> entries;

    private ProductImageUpdate(final boolean replace, final List<Entry> entries) {
        this.replace = replace;
        this.entries = entries == null ? Collections.emptyList() : Collections.unmodifiableList(new ArrayList<>(entries));
    }

    public static ProductImageUpdate unchanged() {
        return new ProductImageUpdate(false, Collections.emptyList());
    }

    public static ProductImageUpdate replaceWith(final List<Entry> entries) {
        return new ProductImageUpdate(true, entries);
    }

    public static ProductImageUpdate replaceWithNewImages(final List<ProductImageData> images) {
        final List<Entry> entries = new ArrayList<>();
        if (images != null) {
            for (ProductImageData image : images) {
                entries.add(newImage(image));
            }
        }
        return replaceWith(entries);
    }

    public static Entry existingImage(final Long imageId) {
        if (imageId == null) {
            throw new IllegalArgumentException("Existing image id is required");
        }
        return new Entry(EntryKind.EXISTING, imageId, null);
    }

    public static Entry newImage(final ProductImageData imageData) {
        if (imageData == null) {
            throw new IllegalArgumentException("Image data is required");
        }
        return new Entry(EntryKind.NEW, null, imageData);
    }

    public boolean isReplace() {
        return replace;
    }

    public List<Entry> getEntries() {
        return entries;
    }
}
