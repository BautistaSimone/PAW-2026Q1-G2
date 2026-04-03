package ar.edu.itba.paw.services;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ar.edu.itba.paw.models.Product;
import ar.edu.itba.paw.persistence.ProductDao;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductDao productDao;
    private final UserService userService;

    @Autowired
    public ProductServiceImpl(final ProductDao productDao, final UserService userService) {
        this.productDao = productDao;
        this.userService = userService;
    }

    @Override
    public Product createProduct(
        final String sellerEmail,
        final String title,
        final String artist,
        final List<Long> categoryIds,
        final String description,
        final BigDecimal sleeveCondition,
        final BigDecimal recordCondition,
        final String neighborhood,
        final String province,
        final BigDecimal price
    ) {
        final Optional<ar.edu.itba.paw.models.User> maybeUser = userService.findByEmail(sellerEmail);
        final ar.edu.itba.paw.models.User user = maybeUser.orElseGet(() ->
            userService.createUser(sellerEmail, "password", sellerEmail.split("@")[0], false)
        );

        return productDao.createProduct(
            user.getId(), title, artist, categoryIds, description,
            sleeveCondition, recordCondition, neighborhood, province, price
        );
    }


    @Override
    public List<Product> listProducts() {
        return productDao.listProducts();
    }

    @Override
    public Optional<Product> findById(final Long id) {
        return productDao.findById(id);
    }
}

