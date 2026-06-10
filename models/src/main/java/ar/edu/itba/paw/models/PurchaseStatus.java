package ar.edu.itba.paw.models;

public enum PurchaseStatus {
    PENDING("PurchaseStatus.PENDING"),
    PAID("PurchaseStatus.PAID"),
    SHIPPED("PurchaseStatus.SHIPPED"),
    DELIVERED("PurchaseStatus.DELIVERED"),
    CANCELLED("PurchaseStatus.CANCELLED");

    private final String messageCode;

    PurchaseStatus(final String messageCode) {
        this.messageCode = messageCode;
    }

    public String getMessageCode() {
        return messageCode;
    }
}
