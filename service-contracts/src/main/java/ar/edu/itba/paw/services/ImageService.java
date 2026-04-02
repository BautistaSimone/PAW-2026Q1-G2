package ar.edu.itba.paw.services;

import java.math.BigDecimal;
import java.util.List;
import java.time.LocalDate;
import java.io.File;

import ar.edu.itba.paw.models.Image;

public interface ImageService {
    Image createImage(
        final Long product_id,
        final byte[] data
    );

    List<Image> listImages();
}
