package ar.edu.itba.paw.services;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import ar.edu.itba.paw.models.Image;

public interface ImageService {
    Image createImage(
        final Long productId,
        final byte[] data,
        final String contentType
    );

    Optional<Image> findById(final Long imageId);

    Optional<Image> findByProductId(final Long productId);

    List<Image> findAllByProductId(final Long productId);

    boolean existsByProductId(final Long productId);

    Set<Long> findProductIdsWithImages(final List<Long> productIds);

    void deleteImagesByProductId(final Long productId);
}
