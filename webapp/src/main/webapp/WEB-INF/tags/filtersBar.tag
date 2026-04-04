<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<style>
/* Category filter chips (toggle pills, wrap on one row when space allows) */
.filter-options.filter-category-chips {
    flex-direction: row;
    flex-wrap: wrap;
    align-items: flex-start;
    gap: 0.35rem;
    padding-top: 0.35rem;
}

.filter-category-chip {
    position: relative;
    display: inline-flex;
    align-items: center;
    margin: 0;
    padding: 0;
    cursor: pointer;
    border-radius: 999px;
    user-select: none;
}

.filter-category-chip:hover input:not(:checked) + .filter-chip-text {
    border-color: var(--color-accent);
    background-color: #fff;
}

.filter-category-chip:hover input:checked + .filter-chip-text {
    filter: brightness(0.94);
}

.filter-category-chip input {
    position: absolute;
    width: 1px;
    height: 1px;
    padding: 0;
    margin: -1px;
    overflow: hidden;
    clip: rect(0, 0, 0, 0);
    white-space: nowrap;
    border: 0;
}

.filter-category-chip .filter-chip-text {
    display: inline-block;
    padding: 0.32rem 0.7rem;
    border-radius: 999px;
    border: 1px solid #dee2e6;
    background-color: #f1f3f5;
    font-size: 0.78rem;
    font-weight: 500;
    line-height: 1.25;
    color: var(--color-text-main);
    transition: background-color 0.15s ease, border-color 0.15s ease, color 0.15s ease, box-shadow 0.15s ease, filter 0.15s ease;
}

.filter-category-chip input:focus-visible + .filter-chip-text {
    outline: none;
    box-shadow: 0 0 0 2px #fff, 0 0 0 4px var(--color-accent);
}

.filter-category-chip input:checked + .filter-chip-text {
    background-color: var(--color-accent);
    border-color: var(--color-accent);
    color: #fff;
}

.filter-category-chip input:checked:focus-visible + .filter-chip-text {
    box-shadow: 0 0 0 2px #fff, 0 0 0 4px rgba(231, 111, 81, 0.45);
}
</style>

<form class="filters-bar" method="get" action="<c:url value="/"/>" novalidate>
    <div class="filters-header">
        <h3 class="filters-title">Filtros</h3>
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
<script src="<c:url value="${pageContext.request.contextPath}/assets/js/filters.js"/>"></script> 