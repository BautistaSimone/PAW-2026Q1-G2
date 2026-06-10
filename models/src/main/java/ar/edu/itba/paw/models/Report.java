package ar.edu.itba.paw.models;

import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.SequenceGenerator;
import javax.persistence.Id;
import javax.persistence.Column;
import javax.persistence.PrePersist;

@Entity
@Table(name = "reports")
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "reports_report_id_seq")
    @SequenceGenerator(sequenceName = "reports_report_id_seq", name = "reports_report_id_seq", allocationSize = 1)
    @Column(name = "report_id")
    private Long reportId;

    @Column(name = "product_id", nullable = false)
    private long productId;

    @Column(name = "owner_user_id", nullable = false)
    private long ownerUserId;

    @Column(name = "reporter_user_id", nullable = false)
    private long reporterUserId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    Report() {
        // Just for Hibernate, we love you!
    }

    public Report(
            final Long reportId,
            final long productId,
            final long ownerUserId,
            final long reporterUserId,
            final LocalDateTime createdAt) {
        this.reportId = reportId;
        this.productId = productId;
        this.ownerUserId = ownerUserId;
        this.reporterUserId = reporterUserId;
        this.createdAt = createdAt;
    }

    public Report(
            final long productId,
            final long ownerUserId,
            final long reporterUserId) {
        this.productId = productId;
        this.ownerUserId = ownerUserId;
        this.reporterUserId = reporterUserId;
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    public Long getReportId() {
        return reportId;
    }

    public long getProductId() {
        return productId;
    }

    public long getOwnerUserId() {
        return ownerUserId;
    }

    public long getReporterUserId() {
        return reporterUserId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
