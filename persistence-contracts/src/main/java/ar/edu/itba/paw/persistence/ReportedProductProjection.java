package ar.edu.itba.paw.persistence;

/**
 * Persistence projection for products grouped by report count.
 */
public class ReportedProductProjection {

    private final Long productId;
    private final Long ownerUserId;
    private final int reportCount;
    private final String productTitle;
    private final String productArtist;
    private final String ownerUsername;

    public ReportedProductProjection(
            final Long productId,
            final Long ownerUserId,
            final int reportCount,
            final String productTitle,
            final String productArtist,
            final String ownerUsername) {
        this.productId = productId;
        this.ownerUserId = ownerUserId;
        this.reportCount = reportCount;
        this.productTitle = productTitle;
        this.productArtist = productArtist;
        this.ownerUsername = ownerUsername;
    }

    public Long getProductId() {
        return productId;
    }

    public Long getOwnerUserId() {
        return ownerUserId;
    }

    public int getReportCount() {
        return reportCount;
    }

    public String getProductTitle() {
        return productTitle;
    }

    public String getProductArtist() {
        return productArtist;
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }
}
