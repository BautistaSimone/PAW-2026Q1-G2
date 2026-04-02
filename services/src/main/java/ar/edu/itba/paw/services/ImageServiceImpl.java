package ar.edu.itba.paw.services;

import java.math.BigDecimal;
import java.util.List;
import java.io.File;

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
        final Long product_id,
        final byte[] data
    ) {
        return imageDao.createImage(product_id, data);
    }

    @Override
    public List<Image> listImages() {
        return imageDao.listImages();
    }
}
