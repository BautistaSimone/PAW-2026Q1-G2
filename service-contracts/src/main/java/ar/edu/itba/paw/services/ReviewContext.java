package ar.edu.itba.paw.services;

import ar.edu.itba.paw.models.Product;
import ar.edu.itba.paw.models.Purchase;
import ar.edu.itba.paw.models.User;

public class ReviewContext {

    private final Purchase purchase;
    private final Product product;
    private final User seller;

    public ReviewContext(final Purchase purchase, final Product product, final User seller) {
        this.purchase = purchase;
        this.product = product;
        this.seller = seller;
    }

    public Purchase getPurchase() {
        return purchase;
    }

    public Product getProduct() {
        return product;
    }

    public User getSeller() {
        return seller;
    }
}
