package ar.edu.itba.paw.persistence;

import javax.sql.DataSource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import ar.edu.itba.paw.models.Image;

@Repository
public class ImageJpaDao implements ImageDao {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Image createImage(
        final Long productId,
        final byte[] data,
        final String contentType
    ) {
        final Image image = new Image(productId, data, contentType);

        em.persist(image);

        return image;
    }

    @Override
    public Optional<Image> findById(final Long imageId) {
        return Optional.ofNullable(em.find(Image.class, imageId));
    }

    @Override
    public Optional<Image> findByProductId(final Long productId) {
        final TypedQuery<Image> query = em.createQuery("FROM Image WHERE productId = :product_id", Image.class);
        query.setParameter("product_id", productId);
        return query.getResultList().stream().findFirst();
    }

    @Override
    public List<Image> findAllByProductId(final Long productId) {
        final TypedQuery<Image> query = em.createQuery("FROM Image WHERE productId = :product_id", Image.class);
        query.setParameter("product_id", productId);
        return query.getResultList();
    }

    @Override
    public boolean existsByProductId(final Long productId) {

        TypedQuery<Image> count = em.createQuery("FROM Image WHERE productId = :product_id", Image.class);
        count.setParameter("product_id", productId);

        final Integer imageCount = count.getResultList().size();
        
        return imageCount != null && imageCount > 0;
    }

    @Override
    public List<Image> listImages() {
        final TypedQuery<Image> query = em.createQuery("FROM Image", Image.class);
        return query.getResultList();
    }

    @Override
    public int deleteByProductId(final Long productId) {
        return em.createQuery("DELETE FROM Image WHERE productId = :product_id")
            .setParameter("product_id", productId)
            .executeUpdate();
    }
}
