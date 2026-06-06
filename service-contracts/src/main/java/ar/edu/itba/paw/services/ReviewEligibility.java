package ar.edu.itba.paw.services;

public class ReviewEligibility {

    public enum Status {
        AVAILABLE,
        NOT_BUYER,
        NOT_DELIVERED,
        ALREADY_REVIEWED
    }

    private final Status status;
    private final ReviewContext context;

    private ReviewEligibility(final Status status, final ReviewContext context) {
        this.status = status;
        this.context = context;
    }

    public static ReviewEligibility available(final ReviewContext context) {
        return new ReviewEligibility(Status.AVAILABLE, context);
    }

    public static ReviewEligibility unavailable(final Status status) {
        if (status == Status.AVAILABLE) {
            throw new IllegalArgumentException("Available review eligibility requires context");
        }
        return new ReviewEligibility(status, null);
    }

    public Status getStatus() {
        return status;
    }

    public ReviewContext getContext() {
        return context;
    }
}
