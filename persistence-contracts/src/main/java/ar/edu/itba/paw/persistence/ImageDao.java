package ar.edu.itba.paw.persistence;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import ar.edu.itba.paw.models.Image;

public interface ImageDao {
    Image createImage(Long productId, byte[] data, String contentType);

    Optional<Image> findById(Long imageId);

    Optional<Image> findByProductId(Long productId);

    List<Image> findAllByProductId(Long productId);

    boolean existsByProductId(Long productId);

    Set<Long> findProductIdsWithImages(List<Long> productIds);

    int deleteByProductId(Long productId);
}
