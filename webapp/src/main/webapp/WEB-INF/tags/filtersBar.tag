<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<form class="filters-bar" method="get" action="<c:url value="/"/>" novalidate>
    <input type="hidden" name="sort" value="<c:out value="${selectedSort}" />" />
    <c:if test="${not empty activeSearchText}">
        <input type="hidden" name="search-text" value="<c:out value="${activeSearchText}" />" />
    </c:if>
    <div class="filters-header">
        <h3 class="filters-title"><i class="bi bi-sliders2" aria-hidden="true"></i> <spring:message code="Filters.title" /></h3>
        <a href="<c:url value="/"/>" class="clear-filters-btn"><spring:message code="Filters.clearAll" /></a>
    </div>

    <details class="filter-section" open>
        <summary class="filter-section-header">
            <div class="filter-section-title">
                <i class="bi bi-tag" aria-hidden="true"></i>
                <span><spring:message code="Filters.categories.title" /></span>
            </div>
            <i class="bi bi-chevron-up filter-section-chevron" aria-hidden="true"></i>
        </summary>
        <div class="filter-options filter-category-chips">
            <c:choose>
                <c:when test="${not empty categories}">
                    <c:forEach items="${categories}" var="cat">
                        <label class="filter-category-chip">
                            <input type="checkbox" name="categories" value="<c:out value='${cat.id}' />"
                                ${selectedCategoryIds.contains(cat.id) ? 'checked' : ''} />
                            <span class="filter-chip-text"><c:out value="${cat.name}" /></span>
                        </label>
                    </c:forEach>
                </c:when>
                <c:otherwise>
                    <p class="filter-empty-hint mb-0"><spring:message code="Filters.categories.empty" /></p>
                </c:otherwise>
            </c:choose>
        </div>
    </details>

    <details class="filter-section" open>
        <summary class="filter-section-header">
            <div class="filter-section-title">
                <i class="bi bi-currency-dollar" aria-hidden="true"></i>
                <span><spring:message code="Filters.price.title" /></span>
            </div>
            <i class="bi bi-chevron-up filter-section-chevron" aria-hidden="true"></i>
        </summary>
        <div class="filter-options">
            <p class="filter-hint mb-2"><spring:message code="Filters.price.suggested" /></p>
            <div class="filter-price-presets">
                <button type="button" class="price-preset-btn" data-min="" data-max="15000"><spring:message code="Filters.price.lessThan" arguments="$15.000" /></button>
                <button type="button" class="price-preset-btn" data-min="15000" data-max="30000"><spring:message code="Filters.price.range" arguments="$15.000,$30.000" /></button>
                <button type="button" class="price-preset-btn" data-min="30000" data-max="60000"><spring:message code="Filters.price.range" arguments="$30.000,$60.000" /></button>
                <button type="button" class="price-preset-btn" data-min="60000" data-max="120000"><spring:message code="Filters.price.range" arguments="$60.000,$120.000" /></button>
                <button type="button" class="price-preset-btn" data-min="120000" data-max=""><spring:message code="Filters.price.moreThan" arguments="$120.000" /></button>
            </div>
            <div class="price-inputs filter-options pt-2">
                <div class="price-input-group">
                    <label for="filterMinPrice" class="price-label"><spring:message code="Filters.price.from" /></label>
                    <spring:message code="Filters.price.minPlaceholder" var="minPlaceholder" />
                    <input id="filterMinPrice" name="minPrice" type="text" inputmode="numeric" class="price-input"
                           placeholder="<c:out value='${minPlaceholder}' />" value="<c:out value="${filterMinPrice}" />" />
                </div>
                <div class="price-input-group">
                    <label for="filterMaxPrice" class="price-label"><spring:message code="Filters.price.to" /></label>
                    <spring:message code="Filters.price.maxPlaceholder" var="maxPlaceholder" />
                    <input id="filterMaxPrice" name="maxPrice" type="text" inputmode="numeric" class="price-input"
                           placeholder="<c:out value='${maxPlaceholder}' />" value="<c:out value="${filterMaxPrice}" />" />
                </div>
            </div>
        </div>
    </details>

    <details class="filter-section" open>
        <summary class="filter-section-header">
            <div class="filter-section-title">
                <i class="bi bi-disc" aria-hidden="true"></i>
                <span><spring:message code="Filters.labels.title" /></span>
            </div>
            <i class="bi bi-chevron-up filter-section-chevron" aria-hidden="true"></i>
        </summary>
        <div class="filter-options">
            <c:choose>
                <c:when test="${not empty recordLabelsFilter}">
                    <c:forEach items="${recordLabelsFilter}" var="lbl">
                        <label class="filter-option">
                            <input type="checkbox" name="label" value="<c:out value="${lbl}" />"
                                ${selectedLabels.contains(lbl) ? 'checked' : ''} />
                            <span><c:out value="${lbl}" /></span>
                        </label>
                    </c:forEach>
                </c:when>
                <c:otherwise>
                    <p class="filter-empty-hint mb-0"><spring:message code="Filters.labels.empty" /></p>
                </c:otherwise>
            </c:choose>
        </div>
    </details>

    <details class="filter-section" open>
        <summary class="filter-section-header">
            <div class="filter-section-title">
                <i class="bi bi-star" aria-hidden="true"></i>
                <span><spring:message code="Filters.status.title" /></span>
            </div>
            <i class="bi bi-chevron-up filter-section-chevron" aria-hidden="true"></i>
        </summary>
        <div class="filter-options">
            <p class="filter-hint mb-2"><spring:message code="Filters.status.help" /></p>
            <label class="filter-option">
                <input type="checkbox" name="estado" value="EXCELENTE" ${selectedEstados.contains('EXCELENTE') ? 'checked' : ''} />
                <span><spring:message code="Filters.status.excellent" /></span>
            </label>
            <label class="filter-option">
                <input type="checkbox" name="estado" value="MUY_BUENO" ${selectedEstados.contains('MUY_BUENO') ? 'checked' : ''} />
                <span><spring:message code="Filters.status.veryGood" /></span>
            </label>
            <label class="filter-option">
                <input type="checkbox" name="estado" value="BUENO" ${selectedEstados.contains('BUENO') ? 'checked' : ''} />
                <span><spring:message code="Filters.status.good" /></span>
            </label>
            <label class="filter-option">
                <input type="checkbox" name="estado" value="REGULAR" ${selectedEstados.contains('REGULAR') ? 'checked' : ''} />
                <span><spring:message code="Filters.status.regular" /></span>
            </label>
        </div>
    </details>

    <div class="filters-actions-sticky">
        <button type="submit" class="btn-retro filters-apply-btn" id="applyFiltersBtn" disabled>
            <i class="bi bi-check2-all" aria-hidden="true"></i> <spring:message code="Filters.apply" />
        </button>
    </div>

</form>

<!-- Scripts -->
<script>
(function () {
    var form = document.querySelector('form.filters-bar');
    var applyBtn = document.getElementById('applyFiltersBtn');
    if (!form || !applyBtn) {
        return;
    }
    var minPriceInput = document.getElementById('filterMinPrice');
    var maxPriceInput = document.getElementById('filterMaxPrice');

    if (window.VinylandPriceFormat) {
        window.VinylandPriceFormat.attachFormattedPriceInput(minPriceInput);
        window.VinylandPriceFormat.attachFormattedPriceInput(maxPriceInput);
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

    form.addEventListener('input', function(e) {
        if (e.target.tagName === 'INPUT') {
            checkChanges();
        }
    });

    document.querySelectorAll('.price-preset-btn').forEach(function (btn) {
        btn.addEventListener('click', function () {
            var dMin = btn.getAttribute('data-min');
            var dMax = btn.getAttribute('data-max');
            if (minPriceInput) {
                minPriceInput.value = dMin !== null && dMin !== '' && window.VinylandPriceFormat
                    ? window.VinylandPriceFormat.normalizePrice(dMin).formatted
                    : (dMin || '');
            }
            if (maxPriceInput) {
                maxPriceInput.value = dMax !== null && dMax !== '' && window.VinylandPriceFormat
                    ? window.VinylandPriceFormat.normalizePrice(dMax).formatted
                    : (dMax || '');
            }
            checkChanges();
        });
    });

    // Expose sort update function for home.jsp
    window.updateFiltersSort = function(newSort) {
        var sortInput = form.querySelector('input[name="sort"]');
        if (sortInput) {
            sortInput.value = newSort;
            checkChanges();
        }
    };
})();
</script>
