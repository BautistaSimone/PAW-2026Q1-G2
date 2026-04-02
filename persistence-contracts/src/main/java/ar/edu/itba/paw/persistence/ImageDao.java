package ar.edu.itba.paw.persistence;

import java.util.List;
import java.util.Optional;

import ar.edu.itba.paw.models.Image;

public interface ImageDao {
    Image createImage(Long productId, byte[] data, String contentType);

    Optional<Image> findById(Long imageId);

    Optional<Image> findByProductId(Long productId);

    boolean existsByProductId(Long productId);

    List<Image> listImages();
}
