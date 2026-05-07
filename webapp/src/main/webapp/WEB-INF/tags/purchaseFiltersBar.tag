<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ attribute name="activeTab" required="true" type="java.lang.String" %>

<form class="filters-bar" method="get" action="<c:url value="/profile"/>" novalidate>
    <input type="hidden" name="tab" value="<c:out value="${activeTab}" />" />
    <div class="filters-header">
        <h3 class="filters-title"><i class="bi bi-sliders2" aria-hidden="true"></i> <spring:message code="Filters.title" /></h3>
        <a href="<c:url value="/profile?tab=${activeTab}"/>" class="clear-filters-btn"><spring:message code="Filters.clearAll" /></a>
    </div>

    <details class="filter-section" open>
        <summary class="filter-section-header">
            <div class="filter-section-title">
                <i class="bi bi-truck" aria-hidden="true"></i>
                <span><spring:message code="Filters.purchaseStatus.title" /></span>
            </div>
            <i class="bi bi-chevron-up filter-section-chevron" aria-hidden="true"></i>
        </summary>
        <div class="filter-options">
            <label class="filter-option">
                <input type="checkbox" name="status" value="PENDING" ${selectedStatuses.contains('PENDING') ? 'checked' : ''} />
                <span><spring:message code="PurchaseStatus.PENDING" /></span>
            </label>
            <label class="filter-option">
                <input type="checkbox" name="status" value="PAID" ${selectedStatuses.contains('PAID') ? 'checked' : ''} />
                <span><spring:message code="PurchaseStatus.PAID" /></span>
            </label>
            <label class="filter-option">
                <input type="checkbox" name="status" value="SHIPPED" ${selectedStatuses.contains('SHIPPED') ? 'checked' : ''} />
                <span><spring:message code="PurchaseStatus.SHIPPED" /></span>
            </label>
            <label class="filter-option">
                <input type="checkbox" name="status" value="DELIVERED" ${selectedStatuses.contains('DELIVERED') ? 'checked' : ''} />
                <span><spring:message code="PurchaseStatus.DELIVERED" /></span>
            </label>
        </div>
    </details>

    <div class="filters-actions-sticky">
        <button type="submit" class="btn-retro filters-apply-btn" id="applyPurchaseFiltersBtn-${activeTab}" disabled>
            <i class="bi bi-check2-all" aria-hidden="true"></i> <spring:message code="Filters.apply" />
        </button>
    </div>

</form>

<script>
(function () {
    var form = document.querySelector('form.filters-bar:has(#applyPurchaseFiltersBtn-${activeTab})');
    var applyBtn = document.getElementById('applyPurchaseFiltersBtn-${activeTab}');
    if (!form || !applyBtn) {
        return;
    }

    // Function to serialize form state for comparison
    function getSerializedState() {
        var formData = new FormData(form);
        // Sort keys to ensure consistent comparison regardless of order
        var params = new URLSearchParams();
        Array.from(formData.entries()).sort().forEach(function(pair) {
            params.append(pair[0], pair[1]);
        });
        return params.toString();
    }

    // Store initial state to detect changes
    var initialState = getSerializedState();
    
    function checkChanges() {
        var currentState = getSerializedState();
        if (currentState !== initialState) {
            applyBtn.disabled = false;
            applyBtn.classList.add('is-active');
        } else {
            applyBtn.disabled = true;
            applyBtn.classList.remove('is-active');
        }
    }

    form.addEventListener('change', function () {
        checkChanges();
    });

})();
</script>
