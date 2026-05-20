<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<spring:message code="Global.currency.symbol" var="currencySymbol"/>
<c:url var="recordLabelAutocompleteUrl" value="/products/autocomplete/record-labels" />
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
                            <input type="checkbox" name="categories" value="${cat.id}"
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
                <button type="button" class="price-preset-btn" data-min="" data-max="15000"><spring:message code="Filters.price.lessThan" arguments="${currencySymbol}15.000" /></button>
                <button type="button" class="price-preset-btn" data-min="15000" data-max="30000"><spring:message code="Filters.price.range" arguments="${currencySymbol}15.000,${currencySymbol}30.000" /></button>
                <button type="button" class="price-preset-btn" data-min="30000" data-max="60000"><spring:message code="Filters.price.range" arguments="${currencySymbol}30.000,${currencySymbol}60.000" /></button>
                <button type="button" class="price-preset-btn" data-min="60000" data-max="120000"><spring:message code="Filters.price.range" arguments="${currencySymbol}60.000,${currencySymbol}120.000" /></button>
                <button type="button" class="price-preset-btn" data-min="120000" data-max=""><spring:message code="Filters.price.moreThan" arguments="${currencySymbol}120.000" /></button>
            </div>
            <div class="price-inputs filter-options pt-2">
                <div class="price-input-group">
                    <label for="filterMinPrice" class="price-label"><spring:message code="Filters.price.from" /></label>
                    <spring:message code="Filters.price.minPlaceholder" var="minPlaceholder" />
                    <input id="filterMinPrice" name="minPrice" type="text" inputmode="numeric" class="price-input"
                           placeholder="${minPlaceholder}" value="<c:out value="${filterMinPrice}" />" />
                </div>
                <div class="price-input-group">
                    <label for="filterMaxPrice" class="price-label"><spring:message code="Filters.price.to" /></label>
                    <spring:message code="Filters.price.maxPlaceholder" var="maxPlaceholder" />
                    <input id="filterMaxPrice" name="maxPrice" type="text" inputmode="numeric" class="price-input"
                           placeholder="${maxPlaceholder}" value="<c:out value="${filterMaxPrice}" />" />
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
            <div class="record-label-filter"
                 data-autocomplete-url="<c:out value='${recordLabelAutocompleteUrl}' />"
                 data-min-query-length="2"
                 data-max-results="7"
                 data-debounce-ms="250">
                <spring:message code="Filters.labels.search.placeholder" var="labelSearchPlaceholder" />
                <input id="recordLabelSearch" type="text" class="record-label-search-input"
                       placeholder="<c:out value='${labelSearchPlaceholder}' />" autocomplete="off" />
                <div class="record-label-selected" hidden>
                    <p class="record-label-group-title"><spring:message code="Filters.labels.selected" /></p>
                    <div class="record-label-selected-options"></div>
                </div>
                <div class="record-label-results"></div>
                <p class="record-label-no-results filter-empty-hint mb-0" hidden>
                    <spring:message code="Filters.labels.noResults" />
                </p>
            </div>
            <select id="recordLabelFilterSource" class="record-label-filter-source" hidden="hidden" aria-hidden="true" tabindex="-1">
                <c:forEach items="${selectedLabels}" var="selectedLabel">
                    <option value="<c:out value='${selectedLabel}' />" data-selected="true"><c:out value="${selectedLabel}" /></option>
                </c:forEach>
            </select>
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

    function normalizeLabel(value) {
        return (value || '').trim().toLowerCase();
    }

    function parsePositiveInt(value, fallback) {
        var parsed = parseInt(value, 10);
        return parsed > 0 ? parsed : fallback;
    }

    function buildAutocompleteUrl(endpoint, query) {
        var separator = endpoint.indexOf('?') === -1 ? '?' : '&';
        return endpoint + separator + 'q=' + encodeURIComponent(query);
    }

    function initRecordLabelFilter() {
        var labelFilter = form.querySelector('.record-label-filter');
        var source = document.getElementById('recordLabelFilterSource');
        if (!labelFilter || !source) {
            return;
        }

        var endpoint = labelFilter.getAttribute('data-autocomplete-url');
        var searchInput = document.getElementById('recordLabelSearch');
        var selectedBlock = labelFilter.querySelector('.record-label-selected');
        var selectedOptions = labelFilter.querySelector('.record-label-selected-options');
        var resultsOptions = labelFilter.querySelector('.record-label-results');
        var noResults = labelFilter.querySelector('.record-label-no-results');
        var minQueryLength = parsePositiveInt(labelFilter.getAttribute('data-min-query-length'), 2);
        var maxResults = parsePositiveInt(labelFilter.getAttribute('data-max-results'), 7);
        var debounceMs = parsePositiveInt(labelFilter.getAttribute('data-debounce-ms'), 250);
        var labelsByKey = Object.create(null);
        var labels = [];
        var selectedKeys = Object.create(null);
        var suggestions = [];
        var debounceTimer = null;
        var requestCounter = 0;
        var activeController = null;
        var completedQuery = '';
        var isLoading = false;

        function currentSearchTerm() {
            return searchInput ? (searchInput.value || '').trim() : '';
        }

        function addLabel(value) {
            var name = (value || '').trim();
            var key = normalizeLabel(name);
            if (!key) {
                return null;
            }
            if (!labelsByKey[key]) {
                labelsByKey[key] = { key: key, name: name };
                labels.push(labelsByKey[key]);
            }
            return labelsByKey[key];
        }

        function sanitizeSuggestions(values) {
            var seen = Object.create(null);
            var result = [];

            if (!Array.isArray(values)) {
                return result;
            }

            values.some(function (value) {
                if (typeof value !== 'string') {
                    return false;
                }
                var label = addLabel(value);
                if (!label || Object.prototype.hasOwnProperty.call(seen, label.key)) {
                    return false;
                }
                seen[label.key] = true;
                result.push(label);
                return result.length >= maxResults;
            });

            return result;
        }

        function clearPendingRequest() {
            if (debounceTimer) {
                window.clearTimeout(debounceTimer);
                debounceTimer = null;
            }
            if (activeController) {
                activeController.abort();
                activeController = null;
            }
        }

        Array.prototype.slice.call(source.options).forEach(function (option) {
            var label = addLabel(option.value);
            if (label && option.getAttribute('data-selected') === 'true') {
                selectedKeys[label.key] = true;
            }
        });

        function selectedLabelObjects() {
            return labels.filter(function (label) {
                return !!selectedKeys[label.key];
            });
        }

        function visibleSuggestionObjects() {
            return suggestions.filter(function (label) {
                return !selectedKeys[label.key];
            });
        }

        function createLabelOption(label) {
            var optionLabel = document.createElement('label');
            optionLabel.className = 'filter-option record-label-option';

            var checkbox = document.createElement('input');
            checkbox.type = 'checkbox';
            checkbox.name = 'label';
            checkbox.value = label.name;
            checkbox.checked = !!selectedKeys[label.key];

            var text = document.createElement('span');
            text.className = 'filter-record-label';
            text.title = label.name;
            text.textContent = label.name;

            checkbox.addEventListener('change', function () {
                if (checkbox.checked) {
                    selectedKeys[label.key] = true;
                } else {
                    delete selectedKeys[label.key];
                }
                renderLabels();
                checkChanges();
            });

            optionLabel.appendChild(checkbox);
            optionLabel.appendChild(text);
            return optionLabel;
        }

        function renderLabels() {
            var selected = selectedLabelObjects();
            var visible = visibleSuggestionObjects();
            var normalizedSearch = normalizeLabel(currentSearchTerm());
            var shouldShowNoResults = normalizedSearch.length >= minQueryLength &&
                !isLoading &&
                completedQuery === normalizedSearch &&
                visible.length === 0;

            selectedOptions.textContent = '';
            resultsOptions.textContent = '';

            selected.forEach(function (label) {
                selectedOptions.appendChild(createLabelOption(label));
            });
            visible.forEach(function (label) {
                resultsOptions.appendChild(createLabelOption(label));
            });

            selectedBlock.hidden = selected.length === 0;
            noResults.hidden = !shouldShowNoResults;
        }

        function requestSuggestions(delay) {
            var query = currentSearchTerm();
            var normalizedQuery = normalizeLabel(query);
            requestCounter += 1;
            clearPendingRequest();

            if (!endpoint || typeof window.fetch !== 'function' || normalizedQuery.length < minQueryLength) {
                suggestions = [];
                completedQuery = '';
                isLoading = false;
                renderLabels();
                return;
            }

            isLoading = true;
            completedQuery = '';
            suggestions = [];
            renderLabels();

            var requestId = requestCounter;
            debounceTimer = window.setTimeout(function () {
                var options = {
                    headers: {
                        'Accept': 'application/json'
                    },
                    credentials: 'same-origin'
                };

                if (typeof window.AbortController === 'function') {
                    activeController = new AbortController();
                    options.signal = activeController.signal;
                }

                window.fetch(buildAutocompleteUrl(endpoint, query), options)
                    .then(function (response) {
                        if (!response.ok) {
                            throw new Error('Record label autocomplete request failed');
                        }
                        return response.json();
                    })
                    .then(function (values) {
                        if (requestId !== requestCounter) {
                            return;
                        }
                        activeController = null;
                        isLoading = false;
                        completedQuery = normalizedQuery;
                        suggestions = sanitizeSuggestions(values);
                        renderLabels();
                    })
                    .catch(function (error) {
                        if (error && error.name === 'AbortError') {
                            return;
                        }
                        if (requestId === requestCounter) {
                            activeController = null;
                            isLoading = false;
                            completedQuery = '';
                            suggestions = [];
                            renderLabels();
                        }
                    });
            }, delay);
        }

        if (searchInput) {
            searchInput.addEventListener('input', function () {
                requestSuggestions(debounceMs);
            });
            searchInput.addEventListener('keydown', function (ev) {
                if (ev.key === 'Enter') {
                    ev.preventDefault();
                }
            });
        }
        renderLabels();
    }

    initRecordLabelFilter();

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
