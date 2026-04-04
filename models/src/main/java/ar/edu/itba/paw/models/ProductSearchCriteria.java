package ar.edu.itba.paw.models;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class ProductSearchCriteria {

    public static final ProductSearchCriteria EMPTY = new ProductSearchCriteria(
        Collections.emptyList(),
        null,
        null,
        Collections.emptyList(),
        Collections.emptyList()
    );

    private final List<Long> categoryIds;
    private final BigDecimal minPrice;
    private final BigDecimal maxPrice;
    private final List<String> recordLabels;
    private final List<ConditionBucket> conditionBuckets;

    public ProductSearchCriteria(
        final List<Long> categoryIds,
        final BigDecimal minPrice,
        final BigDecimal maxPrice,
        final List<String> recordLabels,
        final List<ConditionBucket> conditionBuckets
    ) {
        this.categoryIds = categoryIds == null ? Collections.emptyList() : List.copyOf(categoryIds);
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.recordLabels = recordLabels == null
            ? Collections.emptyList()
            : recordLabels.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .collect(Collectors.toUnmodifiableList());
        this.conditionBuckets = conditionBuckets == null ? Collections.emptyList() : List.copyOf(conditionBuckets);
    }

    public static ProductSearchCriteria empty() {
        return EMPTY;
    }

    public List<Long> getCategoryIds() {
        return categoryIds;
    }

    public BigDecimal getMinPrice() {
        return minPrice;
    }

    public BigDecimal getMaxPrice() {
        return maxPrice;
    }

    public List<String> getRecordLabels() {
        return recordLabels;
    }

    public List<ConditionBucket> getConditionBuckets() {
        return conditionBuckets;
    }
}
