package ar.edu.itba.paw.models;

public enum PurchaseStatus {
    PENDING("Pendiente de pago"),
    PAID("Pagado"),
    SHIPPED("Enviado"),
    DELIVERED("Entregado");

    private final String description;

    PurchaseStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
