package ar.edu.itba.paw.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ar.edu.itba.paw.models.Image;
import ar.edu.itba.paw.persistence.ImageDao;

@Service
public class ImageServiceImpl implements ImageService {

    private final ImageDao imageDao;

    @Autowired
    public ImageServiceImpl(final ImageDao imageDao) {
        this.imageDao = imageDao;
    }

    @Override
    public Image createImage(
        final Long productId,
        final byte[] data,
        final String contentType
    ) {
        return imageDao.createImage(productId, data, contentType);
    }

    @Override
    public Optional<Image> findById(final Long imageId) {
        return imageDao.findById(imageId);
    }

    @Override
    public Optional<Image> findByProductId(final Long productId) {
        return imageDao.findByProductId(productId);
    }

    @Override
    public boolean existsByProductId(final Long productId) {
        return imageDao.existsByProductId(productId);
    }

    @Override
    public List<Image> listImages() {
        return imageDao.listImages();
    }
}
