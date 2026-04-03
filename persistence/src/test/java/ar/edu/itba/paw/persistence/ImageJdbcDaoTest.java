package ar.edu.itba.paw.persistence;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.itba.paw.models.Image;
import ar.edu.itba.paw.models.Product;
import ar.edu.itba.paw.models.User;

@Rollback
@Transactional
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfiguration.class)
public class ImageJdbcDaoTest {

    @Autowired
    private ImageJdbcDao imageDao;

    @Autowired
    private ProductJdbcDao productDao;

    @Autowired
    private UserJdbcDao userDao;

    @Test
    public void testCreateImageStoresBinaryDataAndContentType() {
        final User user = userDao.createUser("image@test.com", "password", "seller", false);
        final Product product = productDao.createProduct(
            user.getId(),
            "Dynamo",
            "Soda Stereo",
            Collections.emptyList(),
            "Edicion original",
            BigDecimal.valueOf(9.0),
            BigDecimal.valueOf(9.0),
            "Palermo",
            "CABA",
            BigDecimal.valueOf(32000)
        );
        final byte[] imageData = "fake-image".getBytes(StandardCharsets.UTF_8);

        final Image createdImage = imageDao.createImage(product.getId(), imageData, "image/jpeg");
        final Optional<Image> imageById = imageDao.findById(createdImage.getImageId());
        final Optional<Image> imageByProduct = imageDao.findByProductId(product.getId());

        Assertions.assertNotNull(createdImage);
        Assertions.assertTrue(imageById.isPresent());
        Assertions.assertTrue(imageByProduct.isPresent());
        Assertions.assertArrayEquals(imageData, imageById.get().getData());
        Assertions.assertEquals("image/jpeg", imageById.get().getContentType());
        Assertions.assertTrue(imageDao.existsByProductId(product.getId()));
    }
}
