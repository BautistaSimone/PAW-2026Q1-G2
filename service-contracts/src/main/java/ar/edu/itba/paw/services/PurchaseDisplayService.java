package ar.edu.itba.paw.services;

import ar.edu.itba.paw.models.Purchase;

public interface PurchaseDisplayService {

    PurchaseDisplay getPurchaseDisplay(Long purchaseId, Long userId);

    final class PurchaseDisplay {

        private final Purchase purchase;
        private final ar.edu.itba.paw.models.Product product;
        private final ar.edu.itba.paw.models.User orderBuyer;
        private final ar.edu.itba.paw.models.User orderSeller;
        private final boolean isBuyer;
        private final boolean isSeller;
        private final Long remainingSeconds;
        private final boolean hasPaymentProof;
        private final String paymentProofFileName;
        private final boolean hasReview;

        public PurchaseDisplay(
                final Purchase purchase,
                final ar.edu.itba.paw.models.Product product,
                final ar.edu.itba.paw.models.User orderBuyer,
                final ar.edu.itba.paw.models.User orderSeller,
                final boolean isBuyer,
                final boolean isSeller,
                final Long remainingSeconds,
                final boolean hasPaymentProof,
                final String paymentProofFileName,
                final boolean hasReview) {
            this.purchase = purchase;
            this.product = product;
            this.orderBuyer = orderBuyer;
            this.orderSeller = orderSeller;
            this.isBuyer = isBuyer;
            this.isSeller = isSeller;
            this.remainingSeconds = remainingSeconds;
            this.hasPaymentProof = hasPaymentProof;
            this.paymentProofFileName = paymentProofFileName;
            this.hasReview = hasReview;
        }

        public Purchase getPurchase() {
            return purchase;
        }

        public ar.edu.itba.paw.models.Product getProduct() {
            return product;
        }

        public ar.edu.itba.paw.models.User getOrderBuyer() {
            return orderBuyer;
        }

        public ar.edu.itba.paw.models.User getOrderSeller() {
            return orderSeller;
        }

        public boolean isBuyer() {
            return isBuyer;
        }

        public boolean isSeller() {
            return isSeller;
        }

        public Long getRemainingSeconds() {
            return remainingSeconds;
        }

        public boolean isHasPaymentProof() {
            return hasPaymentProof;
        }

        public String getPaymentProofFileName() {
            return paymentProofFileName;
        }

        public boolean isHasReview() {
            return hasReview;
        }
    }
}