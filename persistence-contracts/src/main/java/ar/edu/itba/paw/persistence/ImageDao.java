package ar.edu.itba.paw.persistence;

import java.math.BigDecimal;
import java.util.List;
import java.time.LocalDate;
import java.io.File;

import ar.edu.itba.paw.models.Image;

public interface ImageDao {
    Image createImage(Long product_id, byte[] data);

    List<Image> listImages();
}
