<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<ui:layout title="Vinyland | Productos">

    <ui:sidebar />
    <ui:header showHeaderActions="true"/>

    <div class="products-section">
        <div class="container-fluid products-shell">
            <c:if test="${param.created eq '1'}">
                <div class="alert-retro alert-retro-success mb-3" role="alert">
                    <i class="bi bi-check-circle" aria-hidden="true"></i>
                    El vinilo se publico correctamente.
                </div>
            </c:if>
            <c:if test="${param.moderated eq '1'}">
                <div class="alert-retro alert-retro-success mb-3" role="alert">
                    <i class="bi bi-check-circle" aria-hidden="true"></i>
                    La publicación fue ocultada del catálogo.
                </div>
            </c:if>


            <div class="products-layout-grid">
                <aside class="products-filters-column">
                    <ui:filtersBar />
                </aside>

                <section class="products-content-column">
                    <div class="products-header">
                        <div class="products-header-titles">
                            <h2 class="products-count m-0"><c:out value="${fn:length(products)}" /> productos</h2>
                            <c:if test="${not empty activeSearchText}">
                                <p class="products-search-context m-0" role="status">
                                    Resultados para <span class="products-search-query">"<c:out value="${activeSearchText}" />"</span>
                                </p>
                            </c:if>
                        </div>
                        <div class="products-header-actions">
                            <select id="sortSelect" class="sort-select" aria-label="Ordenar por">
                                <c:forEach items="${sortOptions}" var="opt">
                                    <option value="${opt.name()}" ${opt.name() eq selectedSort ? 'selected' : ''}>
                                        <c:out value="${opt.label}" />
                                    </option>
                                </c:forEach>
                            </select>
                            <a href="<c:url value='/products/new'/>" class="btn btn-retro btn-retro-primary">
                                <i class="bi bi-plus-lg" aria-hidden="true"></i> Publicar vinilo
                            </a>
                        </div>
                    </div>

                    <c:choose>
                        <c:when test="${not empty products}">
                            <div class="products-grid" id="productsGrid">
                                <c:forEach items="${products}" var="product" varStatus="loop">
                                    <div class="products-grid-item" data-product-index="${loop.index}" style="${loop.index >= 12 ? 'display:none' : ''}">
                                        <c:url value="/products/${product.id}" var="productUrl"/>
                                        <ui:productCard
                                                title="${product.title}"
                                                artist="${product.artist}"
                                                price="${product.price}"
                                                installments="${product.installmentPrice}"
                                                imageUrl="${productImageUrls[product.id]}"
                                                categories="${product.categories}"
                                                href="${productUrl}"/>
                                    </div>
                                </c:forEach>
                            </div>
                            <c:if test="${fn:length(products) > 12}">
                                <div style="text-align:center; margin-top:2rem;">
                                    <button id="showMoreBtn" class="btn btn-retro btn-retro-outline" type="button">
                                        Mostrar m&aacute;s
                                    </button>
                                </div>
                            </c:if>
                        </c:when>
                        <c:otherwise>
                            <div class="empty-products-state">
                                <i class="bi bi-vinyl" style="font-size: 3rem; color: var(--color-border);"></i>
                                <c:choose>
                                    <c:when test="${noProductsMatchFilters}">
                                        <p style="color: var(--color-text-muted); font-size: 1.05rem;">No hay productos que coincidan con tu busqueda o filtros.</p>
                                    </c:when>
                                    <c:otherwise>
                                        <p style="color: var(--color-text-muted); font-size: 1.05rem;">Todavia no hay productos cargados. Publica un vinilo para probar el alta.</p>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </section>
            </div>
        </div>
    </div>

    <script>
    (function () {
        var sortSelect = document.getElementById('sortSelect');
        if (sortSelect) {
            sortSelect.addEventListener('change', function () {
                if (window.updateFiltersSort) {
                    window.updateFiltersSort(sortSelect.value);
                } else {
                    // Fallback
                    var params = new URLSearchParams(window.location.search);
                    params.set('sort', sortSelect.value);
                    window.location.search = params.toString();
                }
            });
        }

        var showMoreBtn = document.getElementById('showMoreBtn');
        if (showMoreBtn) {
            var BATCH = 12;
            var visible = BATCH;
            var items = document.querySelectorAll('#productsGrid .products-grid-item');
            showMoreBtn.addEventListener('click', function () {
                var next = visible + BATCH;
                for (var i = visible; i < next && i < items.length; i++) {
                    items[i].style.display = '';
                }
                visible = next;
                if (visible >= items.length) {
                    showMoreBtn.style.display = 'none';
                }
            });
        }

        var filterBar = document.querySelector('.filters-bar');
        if (filterBar) {
            var adjustSticky = function() {
                var viewportHeight = window.innerHeight;
                var filterHeight = filterBar.offsetHeight;
                if (filterHeight > viewportHeight - 120) {
                    var topVal = viewportHeight - filterHeight - 20;
                    filterBar.style.top = topVal + 'px';
                } else {
                    filterBar.style.top = '90px';
                }
            };
            window.addEventListener('resize', adjustSticky);
            if (window.ResizeObserver) {
                new ResizeObserver(adjustSticky).observe(filterBar);
            }
            adjustSticky();
        }
    })();
    </script>
</ui:layout>
