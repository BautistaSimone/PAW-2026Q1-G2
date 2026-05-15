package ar.edu.itba.paw.persistence;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import ar.edu.itba.paw.models.Image;

@Repository
public class ImageJpaDao implements ImageDao {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Image createImage(final Long productId, final byte[] data, final String contentType) {
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
        final TypedQuery<Image> query = em.createQuery(
            "FROM Image WHERE productId = :productId", Image.class
        );
        query.setParameter("productId", productId);
        return query.getResultList().stream().findFirst();
    }

    @Override
    public List<Image> findAllByProductId(final Long productId) {
        return em.createQuery("FROM Image WHERE productId = :productId", Image.class)
            .setParameter("productId", productId)
            .getResultList();
    }

    @Override
    public boolean existsByProductId(final Long productId) {
        return em.createQuery(
            "SELECT COUNT(i) FROM Image i WHERE i.productId = :productId", Long.class
        ).setParameter("productId", productId)
        .getSingleResult() > 0;
    }

    @Override
    public List<Image> listImages() {
        return em.createQuery("FROM Image", Image.class).getResultList();
    }

    @Override
    public int deleteByProductId(final Long productId) {
        return em.createQuery("DELETE FROM Image WHERE productId = :productId")
            .setParameter("productId", productId)
            .executeUpdate();
    }
}
