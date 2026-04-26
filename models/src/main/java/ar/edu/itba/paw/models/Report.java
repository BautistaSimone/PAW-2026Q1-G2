package ar.edu.itba.paw.models;

import java.time.LocalDateTime;

public class Report {

    private final Long reportId;
    private final Long productId;
    private final Long ownerUserId;
    private final Long reporterUserId;
    private final LocalDateTime createdAt;

    public Report(
            final Long reportId,
            final Long productId,
            final Long ownerUserId,
            final Long reporterUserId,
            final LocalDateTime createdAt) {
        this.reportId = reportId;
        this.productId = productId;
        this.ownerUserId = ownerUserId;
        this.reporterUserId = reporterUserId;
        this.createdAt = createdAt;
    }

    public Long getReportId() {
        return reportId;
    }

    public Long getProductId() {
        return productId;
    }

    public Long getOwnerUserId() {
        return ownerUserId;
    }

    public Long getReporterUserId() {
        return reporterUserId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
