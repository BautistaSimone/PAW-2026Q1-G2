package ar.edu.itba.paw.models;

import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.SequenceGenerator;
import javax.persistence.Id;
import javax.persistence.Column;
import javax.persistence.Transient;
import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

@Entity
@Table(name = "purchases")
public class Purchase {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "purchases_purchase_id_seq")
    @SequenceGenerator(sequenceName = "purchases_purchase_id_seq", name = "purchases_purchase_id_seq", allocationSize = 1)
    @Column(name = "purchase_id")
    private Long purchaseId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "buyer_user_id", nullable = false)
    private Long buyerId;

    @Column(name = "seller_user_id", nullable = false)
    private Long sellerId;

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "payment_method", nullable = false)
    private String paymentMethod;

    @Column(nullable = false)
    private boolean confirmed;

    @Column(name = "reserved_until")
    private LocalDateTime reservedUntil;
    //TODO: fijarse que el @Lob rompia todo aca, hacia error cuando se empezaba una purchase
    @Column(name = "payment_proof")
    private byte[] paymentProof;

    @Column(name = "payment_proof_content_type")
    private String paymentProofContentType;

    @Column(name = "payment_proof_file_name")
    private String paymentProofFileName;

    @Transient
    private PurchaseStatus status;

    @Transient
    private String buyerToken;

    @Transient
    private String sellerToken;

    Purchase() {
        // Just for Hibernate, we love you!
    }

    public Purchase(
        final Long purchaseId,
        final Long productId,
        final Long buyerId,
        final Long sellerId,
        final LocalDate date,
        final PurchaseStatus status,
        final String buyerToken,
        final String sellerToken
    ) {
        this.purchaseId = purchaseId;
        this.productId = productId;
        this.buyerId = buyerId;
        this.sellerId = sellerId;
        this.date = date;
        this.status = status;
        this.buyerToken = buyerToken;
        this.sellerToken = sellerToken;
        this.confirmed = (status == PurchaseStatus.DELIVERED);
        encodePaymentMethod();
    }

    public Purchase(
        final Long productId,
        final Long buyerId,
        final Long sellerId,
        final LocalDate date,
        final PurchaseStatus status,
        final String buyerToken,
        final String sellerToken
    ) {
        this.productId = productId;
        this.buyerId = buyerId;
        this.sellerId = sellerId;
        this.date = date;
        this.status = status;
        this.buyerToken = buyerToken;
        this.sellerToken = sellerToken;
        this.confirmed = (status == PurchaseStatus.DELIVERED);
        encodePaymentMethod();
    }

    @PostLoad
    private void decodePaymentMethod() {
        if (paymentMethod != null) {
            final String[] parts = paymentMethod.split("\\|", 3);
            this.status = PurchaseStatus.valueOf(parts[0]);
            this.buyerToken = parts.length > 1 ? parts[1] : "";
            this.sellerToken = parts.length > 2 ? parts[2] : "";
        }
    }

    @PrePersist
    @PreUpdate
    private void encodePaymentMethod() {
        this.paymentMethod = status.name() + "|" + buyerToken + "|" + sellerToken;
    }

    public Long getPurchaseId() {
        return purchaseId;
    }

    public Long getProductId() {
        return productId;
    }

    public Long getBuyerId() {
        return buyerId;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public LocalDate getDate() {
        return date;
    }

    public PurchaseStatus getStatus() {
        return status;
    }

    public void setStatus(PurchaseStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Purchase status is required");
        }
        this.status = status;
        this.confirmed = (status == PurchaseStatus.DELIVERED);
        encodePaymentMethod();
    }

    public String getBuyerToken() {
        return buyerToken;
    }

    public String getSellerToken() {
        return sellerToken;
    }

    public boolean getConfirmed() {
        return confirmed;
    }

    public LocalDateTime getReservedUntil() {
        return reservedUntil;
    }

    public void setReservedUntil(final LocalDateTime reservedUntil) {
        this.reservedUntil = reservedUntil;
    }

    public byte[] getPaymentProof() {
        return paymentProof;
    }

    public String getPaymentProofContentType() {
        return paymentProofContentType;
    }

    public String getPaymentProofFileName() {
        return paymentProofFileName;
    }

    public void setPaymentProof(final byte[] paymentProof, final String contentType, final String fileName) {
        this.paymentProof = paymentProof;
        this.paymentProofContentType = contentType;
        this.paymentProofFileName = fileName;
    }
}
