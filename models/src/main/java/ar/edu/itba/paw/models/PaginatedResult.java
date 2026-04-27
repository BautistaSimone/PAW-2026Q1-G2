package ar.edu.itba.paw.models;

import java.util.List;

public class PaginatedResult<T> {
    private final List<T> results;
    private final int currentPage;
    private final int totalPages;
    private final long totalCount;

    public PaginatedResult(List<T> results, int currentPage, int pageSize, long totalCount) {
        this.results = results;
        this.currentPage = currentPage;
        this.totalCount = totalCount;
        this.totalPages = (int) Math.ceil((double) totalCount / pageSize);
    }

    public List<T> getResults() {
        return results;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public boolean isHasNextPage() {
        return currentPage < totalPages;
    }

    public boolean isHasPreviousPage() {
        return currentPage > 1;
    }
}
