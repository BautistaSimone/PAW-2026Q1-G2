package ar.edu.itba.paw.services;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import ar.edu.itba.paw.models.Image;
import ar.edu.itba.paw.persistence.ImageDao;

@ExtendWith(MockitoExtension.class)
public class ImageServiceImplTest {

    @InjectMocks
    private ImageServiceImpl imageService;

    @Mock
    private ImageDao imageDao;

    @Test
    public void testCreateImage() {
        // Arrange
        byte[] data = new byte[]{1, 2, 3};
        Image image = new Image(1L, 10L, data, "image/png");
        Mockito.when(imageDao.createImage(10L, data, "image/png")).thenReturn(image);

        // Act
        Image result = imageService.createImage(10L, data, "image/png");

        // Assert
        Assertions.assertEquals(1L, result.getImageId());
        Assertions.assertEquals(10L, result.getProductId());
        Assertions.assertArrayEquals(data, result.getData());
        Assertions.assertEquals("image/png", result.getContentType());
    }

    @Test
    public void testFindById() {
        // Arrange
        Image image = new Image(1L, 10L, new byte[]{1}, "image/jpeg");
        Mockito.when(imageDao.findById(1L)).thenReturn(Optional.of(image));

        // Act
        Optional<Image> result = imageService.findById(1L);

        // Assert
        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(10L, result.get().getProductId());
    }

    @Test
    public void testFindByProductId() {
        // Arrange
        Image image = new Image(1L, 10L, new byte[]{1}, "image/jpeg");
        Mockito.when(imageDao.findByProductId(10L)).thenReturn(Optional.of(image));

        // Act
        Optional<Image> result = imageService.findByProductId(10L);

        // Assert
        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(1L, result.get().getImageId());
    }

    @Test
    public void testFindAllByProductId() {
        // Arrange
        Image image1 = new Image(1L, 10L, new byte[]{1}, "image/jpeg");
        Image image2 = new Image(2L, 10L, new byte[]{2}, "image/png");
        Mockito.when(imageDao.findAllByProductId(10L)).thenReturn(Arrays.asList(image1, image2));

        // Act
        List<Image> result = imageService.findAllByProductId(10L);

        // Assert
        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals(1L, result.get(0).getImageId());
        Assertions.assertEquals(2L, result.get(1).getImageId());
    }

    @Test
    public void testExistsByProductId() {
        // Arrange
        Mockito.when(imageDao.existsByProductId(10L)).thenReturn(true);

        // Act
        boolean result = imageService.existsByProductId(10L);

        // Assert
        Assertions.assertTrue(result);
    }

    @Test
    public void testFindProductIdsWithImages() {
        // Arrange
        List<Long> productIds = Arrays.asList(10L, 20L);
        Mockito.when(imageDao.findProductIdsWithImages(productIds)).thenReturn(Collections.singleton(10L));

        // Act
        Set<Long> result = imageService.findProductIdsWithImages(productIds);

        // Assert
        Assertions.assertEquals(1, result.size());
        Assertions.assertTrue(result.contains(10L));
    }

    @Test
    public void testListImages() {
        // Arrange
        Image image = new Image(1L, 10L, new byte[]{1}, "image/jpeg");
        Mockito.when(imageDao.listImages()).thenReturn(Collections.singletonList(image));

        // Act
        List<Image> result = imageService.listImages();

        // Assert
        Assertions.assertEquals(1, result.size());
    }

    @Test
    public void testDeleteImagesByProductId() {
        // Act
        imageService.deleteImagesByProductId(10L);

        // Assert
        Mockito.verify(imageDao, Mockito.times(1)).deleteByProductId(10L);
    }
}
