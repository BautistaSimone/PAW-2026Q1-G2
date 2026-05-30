package ar.edu.itba.paw.persistence;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
    public Set<Long> findProductIdsWithImages(final List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Set.of();
        }
        final List<Long> distinctIds = productIds.stream()
            .filter(id -> id != null)
            .distinct()
            .collect(Collectors.toList());
        if (distinctIds.isEmpty()) {
            return Set.of();
        }
        return em.createQuery(
                "SELECT DISTINCT i.productId FROM Image i WHERE i.productId IN :productIds",
                Long.class)
            .setParameter("productIds", distinctIds)
            .getResultList()
            .stream()
            .collect(Collectors.toSet());
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
