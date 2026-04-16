<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<form class="filters-bar" method="get" action="<c:url value="/"/>" novalidate>
    <input type="hidden" name="sort" value="<c:out value="${selectedSort}" />" />
    <c:if test="${not empty activeSearchText}">
        <input type="hidden" name="search-text" value="<c:out value="${activeSearchText}" />" />
    </c:if>
    <div class="filters-header">
        <h3 class="filters-title"><i class="bi bi-sliders2" aria-hidden="true"></i> Filtros</h3>
        <a href="<c:url value="/"/>" class="clear-filters-btn">Limpiar todo</a>
    </div>

    <details class="filter-section" open>
        <summary class="filter-section-header">
            <div class="filter-section-title">
                <i class="bi bi-tag" aria-hidden="true"></i>
                <span>Categorias</span>
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
                    <p class="filter-empty-hint mb-0">No hay categorias disponibles.</p>
                </c:otherwise>
            </c:choose>
        </div>
    </details>

    <details class="filter-section" open>
        <summary class="filter-section-header">
            <div class="filter-section-title">
                <i class="bi bi-currency-dollar" aria-hidden="true"></i>
                <span>Precio</span>
            </div>
            <i class="bi bi-chevron-up filter-section-chevron" aria-hidden="true"></i>
        </summary>
        <div class="filter-options">
            <p class="filter-hint mb-2">Rangos sugeridos (ARS)</p>
            <div class="filter-price-presets">
                <button type="button" class="price-preset-btn" data-min="" data-max="15000">Menos de $15.000</button>
                <button type="button" class="price-preset-btn" data-min="15000" data-max="30000">$15.000 – $30.000</button>
                <button type="button" class="price-preset-btn" data-min="30000" data-max="60000">$30.000 – $60.000</button>
                <button type="button" class="price-preset-btn" data-min="60000" data-max="120000">$60.000 – $120.000</button>
                <button type="button" class="price-preset-btn" data-min="120000" data-max="">Mas de $120.000</button>
            </div>
            <div class="price-inputs filter-options pt-2">
                <div class="price-input-group">
                    <label for="filterMinPrice" class="price-label">Desde</label>
                    <input id="filterMinPrice" name="minPrice" type="number" min="0" step="1" class="price-input"
                           placeholder="Minimo" value="<c:out value="${filterMinPrice}" />" />
                </div>
                <div class="price-input-group">
                    <label for="filterMaxPrice" class="price-label">Hasta</label>
                    <input id="filterMaxPrice" name="maxPrice" type="number" min="0" step="1" class="price-input"
                           placeholder="Maximo" value="<c:out value="${filterMaxPrice}" />" />
                </div>
            </div>
        </div>
    </details>

    <details class="filter-section" open>
        <summary class="filter-section-header">
            <div class="filter-section-title">
                <i class="bi bi-disc" aria-hidden="true"></i>
                <span>Discograficas</span>
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
                    <p class="filter-empty-hint mb-0">Cuando haya sellos cargados en publicaciones, vas a poder filtrar aca.</p>
                </c:otherwise>
            </c:choose>
        </div>
    </details>

    <details class="filter-section" open>
        <summary class="filter-section-header">
            <div class="filter-section-title">
                <i class="bi bi-star" aria-hidden="true"></i>
                <span>Estado</span>
            </div>
            <i class="bi bi-chevron-up filter-section-chevron" aria-hidden="true"></i>
        </summary>
        <div class="filter-options">
            <p class="filter-hint mb-2">Segun el promedio entre tapa y disco (escala 1 a 10).</p>
            <label class="filter-option">
                <input type="checkbox" name="estado" value="EXCELENTE" ${selectedEstados.contains('EXCELENTE') ? 'checked' : ''} />
                <span>Excelente (9 a 10)</span>
            </label>
            <label class="filter-option">
                <input type="checkbox" name="estado" value="MUY_BUENO" ${selectedEstados.contains('MUY_BUENO') ? 'checked' : ''} />
                <span>Muy bueno (8 a 9)</span>
            </label>
            <label class="filter-option">
                <input type="checkbox" name="estado" value="BUENO" ${selectedEstados.contains('BUENO') ? 'checked' : ''} />
                <span>Bueno (7 a 8)</span>
            </label>
            <label class="filter-option">
                <input type="checkbox" name="estado" value="REGULAR" ${selectedEstados.contains('REGULAR') ? 'checked' : ''} />
                <span>Regular (menos de 7)</span>
            </label>
        </div>
    </details>

</form>

<!-- Scripts -->
<script>
(function () {
    var form = document.querySelector('form.filters-bar');
    if (!form) {
        return;
    }

    function submitFilters() {
        if (typeof form.requestSubmit === 'function') {
            form.requestSubmit();
        } else {
            form.submit();
        }
    }

    form.addEventListener('change', function () {
        submitFilters();
    });

    document.querySelectorAll('.price-preset-btn').forEach(function (btn) {
        btn.addEventListener('click', function () {
            var minEl = document.getElementById('filterMinPrice');
            var maxEl = document.getElementById('filterMaxPrice');
            var dMin = btn.getAttribute('data-min');
            var dMax = btn.getAttribute('data-max');
            if (minEl) {
                minEl.value = dMin !== null && dMin !== '' ? dMin : '';
            }
            if (maxEl) {
                maxEl.value = dMax !== null && dMax !== '' ? dMax : '';
            }
            submitFilters();
        });
    });
})();
</script>
