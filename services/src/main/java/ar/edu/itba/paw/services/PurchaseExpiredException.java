package ar.edu.itba.paw.services;

public class PurchaseExpiredException extends IllegalStateException {

    public PurchaseExpiredException(final String message) {
        super(message);
    }
}
