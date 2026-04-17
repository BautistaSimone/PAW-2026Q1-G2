package ar.edu.itba.paw.models;

public class SellerRatingSummary {

    private final double avgScore;
    private final int count;

    public SellerRatingSummary(final double avgScore, final int count) {
        this.avgScore = avgScore;
        this.count = count;
    }

    public double getAvgScore() {
        return avgScore;
    }

    public int getCount() {
        return count;
    }

    public String getFormattedAvg() {
        if (count == 0) {
            return "—";
        }
        return String.format("%.1f", avgScore);
    }
}
