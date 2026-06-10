package ar.edu.itba.paw.models;

import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "purchases")
public class Purchase {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "purchases_purchase_id_seq")
    @SequenceGenerator(sequenceName = "purchases_purchase_id_seq", name = "purchases_purchase_id_seq", allocationSize = 1)
    @Column(name = "purchase_id")
    private Long purchaseId;

    @Column(name = "product_id", nullable = false)
    private long productId;

    @Column(name = "buyer_user_id", nullable = false)
    private long buyerId;

    @Column(name = "seller_user_id", nullable = false)
    private long sellerId;

    @Column(nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(name = "purchase_status", nullable = false)
    private PurchaseStatus status;

    @Column(name = "buyer_token")
    private String buyerToken;

    @Column(name = "seller_token")
    private String sellerToken;

    @Column(nullable = false)
    private boolean confirmed;

    @Column(name = "reserved_until")
    private LocalDateTime reservedUntil;

    @Column(name = "payment_proof")
    private byte[] paymentProof;

    @Column(name = "payment_proof_content_type")
    private String paymentProofContentType;

    @Column(name = "payment_proof_file_name")
    private String paymentProofFileName;

    Purchase() {
        // Just for Hibernate, we love you!
    }

    public Purchase(
        final Long purchaseId,
        final long productId,
        final long buyerId,
        final long sellerId,
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
    }

    public Purchase(
        final long productId,
        final long buyerId,
        final long sellerId,
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
    }

    public Long getPurchaseId() {
        return purchaseId;
    }

    public long getProductId() {
        return productId;
    }

    public long getBuyerId() {
        return buyerId;
    }

    public long getSellerId() {
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