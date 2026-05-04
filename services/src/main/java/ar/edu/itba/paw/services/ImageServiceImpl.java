package ar.edu.itba.paw.services;

import org.springframework.transaction.annotation.Transactional;

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
    @Transactional
    public Image createImage(
        final Long productId,
        final byte[] data,
        final String contentType
    ) {
        return imageDao.createImage(productId, data, contentType);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Image> findById(final Long imageId) {
        return imageDao.findById(imageId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Image> findByProductId(final Long productId) {
        return imageDao.findByProductId(productId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Image> findAllByProductId(final Long productId) {
        return imageDao.findAllByProductId(productId);
    }

    @Override
    @Transactional
    public boolean existsByProductId(final Long productId) {
        return imageDao.existsByProductId(productId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Image> listImages() {
        return imageDao.listImages();
    }

    @Override
    @Transactional
    public void deleteImagesByProductId(final Long productId) {
        imageDao.deleteByProductId(productId);
    }
}
