package ar.edu.itba.paw.persistence;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

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
public class ImageJpaDaoTest {

    @Autowired
    private ImageJpaDao imageDao;

    @PersistenceContext
    private EntityManager em;

    private User insertUser(final String email, final String username) {
        final User user = new User(
                email,
                "password",
                username,
                false,
                true,
                false,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);
        em.persist(user);
        em.flush();
        return user;
    }

    private Product insertProduct(final Long userId) {
        final Product product = new Product(
                userId,
                "Dynamo",
                "Soda Stereo",
                "Sony Music",
                "EPC 85930",
                "Argentina",
                Collections.emptyList(),
                "Edicion original",
                BigDecimal.valueOf(9.0),
                BigDecimal.valueOf(9.0),
                LocalDate.now(),
                BigDecimal.valueOf(32000),
                1);
        em.persist(product);
        em.flush();
        return product;
    }

    private Image insertImage(final Long productId, final byte[] data, final String contentType) {
        final Image image = new Image(productId, data, contentType);
        em.persist(image);
        em.flush();
        return image;
    }

    @Test
    public void testFindImageById() {

        // Arrange
        final User user = insertUser("image@test.com", "seller");
        final Product product = insertProduct(user.getId());
        final byte[] imageData = "fake-image".getBytes(StandardCharsets.UTF_8);
        final Image createdImage = insertImage(product.getId(), imageData, "image/jpeg");
        em.clear();

        // Act
        final Optional<Image> imageById = imageDao.findById(createdImage.getImageId());

        // Assert
        Assertions.assertTrue(imageById.isPresent());
        Assertions.assertArrayEquals(imageData, imageById.get().getData());
        Assertions.assertEquals("image/jpeg", imageById.get().getContentType());
    }

    @Test
    public void testFindImageByProductId() {

        // Arrange
        final User user = insertUser("image@test.com", "seller");
        final Product product = insertProduct(user.getId());
        final byte[] imageData = "fake-image".getBytes(StandardCharsets.UTF_8);
        final Image createdImage = insertImage(product.getId(), imageData, "image/jpeg");
        em.clear();

        // Act
        final Optional<Image> imageByProduct = imageDao.findByProductId(product.getId());

        // Assert
        Assertions.assertTrue(imageByProduct.isPresent());
        Assertions.assertArrayEquals(imageData, imageByProduct.get().getData());
        Assertions.assertEquals("image/jpeg", imageByProduct.get().getContentType());
    }

    @Test
    public void testDeleteImageByProductId() {

        // Arrange
        final User user = insertUser("image@test.com", "seller");
        final Product product = insertProduct(user.getId());
        final byte[] imageData = "fake-image".getBytes(StandardCharsets.UTF_8);
        insertImage(product.getId(), imageData, "image/jpeg");
        em.clear();

        // Act
        final Integer deleted = imageDao.deleteByProductId(product.getId());

        // Assert
        Assertions.assertEquals(1, deleted);

        em.flush();
        em.clear();

        final Long remaining = em.createQuery(
                "SELECT COUNT(i) FROM Image i WHERE i.productId = :productId",
                Long.class).setParameter("productId", product.getId()).getSingleResult();
        Assertions.assertEquals(0L, remaining.longValue());
    }

    @Test
    public void testCreateImage() {

        // Arrange
        final User user = insertUser("image@test.com", "seller");
        final Product product = insertProduct(user.getId());
        final byte[] imageData = "fake-image".getBytes(StandardCharsets.UTF_8);

        // Act
        final Image createdImage = imageDao.createImage(product.getId(), imageData, "image/jpeg");

        // Assert
        Assertions.assertNotNull(createdImage);

        em.flush();
        em.clear();

        final Long count = em.createQuery(
                "SELECT COUNT(i) FROM Image i WHERE i.productId = :productId AND i.contentType = :contentType",
                Long.class)
                .setParameter("productId", product.getId())
                .setParameter("contentType", "image/jpeg")
                .getSingleResult();
        Assertions.assertEquals(1L, count.longValue());
    }
}
