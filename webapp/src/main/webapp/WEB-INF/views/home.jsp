<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<spring:message code="Home.title" var="homeTitle" />
<ui:layout title="${homeTitle}">

    <ui:sidebar />
    <ui:header showHeaderActions="true"/>

    <!-- Tell user to change its password -->
    <c:if test="${changePsswdModal}">
        <spring:message code="Home.modal.password.title" var="psswdTitle" />
        <spring:message code="Home.modal.password.text" var="psswdText" />
        <ui:modal 
            id="psswd" 
            title="${psswdTitle}" 
            text="${psswdText}" />
    </c:if>

    <div class="products-section">
        <div class="container-fluid products-shell">
            <c:if test="${param.created eq '1'}">
                <div class="alert-retro alert-retro-success mb-3" role="alert">
                    <i class="bi bi-check-circle" aria-hidden="true"></i>
                    <spring:message code="Home.alert.productCreated" />
                </div>
            </c:if>
            <c:if test="${param.moderated eq '1'}">
                <div class="alert-retro alert-retro-success mb-3" role="alert">
                    <i class="bi bi-check-circle" aria-hidden="true"></i>
                    <spring:message code="Home.alert.productHidden" />
                </div>
            </c:if>
            <c:if test="${param.purchaseUnavailable eq '1'}">
                <div class="alert-retro alert-retro-warning mb-3" role="alert">
                    <i class="bi bi-exclamation-triangle" aria-hidden="true"></i>
                    <spring:message code="Home.alert.purchaseUnavailable" />
                </div>
            </c:if>
            <c:if test="${param.purchaseError eq '1'}">
                <div class="alert-retro alert-retro-warning mb-3" role="alert">
                    <i class="bi bi-exclamation-triangle" aria-hidden="true"></i>
                    <spring:message code="Home.alert.purchaseError" />
                </div>
            </c:if>


            <div class="products-layout-grid">
                <aside class="products-filters-column">
                    <ui:filtersBar />
                </aside>

                <section class="products-content-column">
                    <div class="products-header">
                        <div class="products-header-titles">
                            <h2 class="products-count m-0"><spring:message code="Home.productsCount" arguments="${fn:length(products)}" /></h2>
                            <c:if test="${not empty activeSearchText}">
                                <p class="products-search-context m-0" role="status">
                                    <spring:message code="Home.searchResultsFor" /> <span class="products-search-query">"<c:out value="${activeSearchText}" />"</span>
                                </p>
                            </c:if>
                        </div>
                        <div class="products-header-actions">
                            <spring:message code="Home.sort.ariaLabel" var="sortAriaLabel" />
                            <select id="sortSelect" class="sort-select" aria-label="<c:out value='${sortAriaLabel}' />">
                                <c:forEach items="${sortOptions}" var="opt">
                                    <option value="<c:out value='${opt.name()}' />" ${opt.name() eq selectedSort ? 'selected' : ''}>
                                        <spring:message code="ProductSortOrder.${opt.name()}" />
                                    </option>
                                </c:forEach>
                            </select>
                            <a href="<c:url value='/products/new'/>" class="btn btn-retro btn-retro-primary">
                                <i class="bi bi-plus-lg" aria-hidden="true"></i> <spring:message code="Home.publish.button" />
                            </a>
                        </div>
                    </div>

                    <c:choose>
                        <c:when test="${not empty products}">
                            <div class="products-grid" id="productsGrid">
                                <c:forEach items="${products}" var="product" varStatus="loop">
                                    <div class="products-grid-item" data-product-index="${loop.index}">
                                        <c:url value="/products/${product.id}" var="productUrl"/>
                                        <ui:productCard
                                                title="<c:out value='${product.title}' />"
                                                artist="<c:out value='${product.artist}' />"
                                                price="${product.price}"
                                                installments="${product.installmentPrice}"
                                                imageUrl="<c:out value='${productImageUrls[product.id]}' />"
                                                categories="${product.categories}"
                                                sellerRating="${sellerRatingByUserId[product.userId]}"
                                                href="<c:out value='${productUrl}' />"/>
                                    </div>
                                </c:forEach>
                            </div>
                            <ui:pagination result="${productsPage}" />
                        </c:when>
                        <c:otherwise>
                            <div class="empty-products-state">
                                <i class="bi bi-vinyl home-i-1" ></i>
                                <c:choose>
                                    <c:when test="${noProductsMatchFilters}">
                                        <p class="home-p-2" ><spring:message code="Home.empty.filtered" /></p>
                                    </c:when>
                                    <c:otherwise>
                                        <p class="home-p-2" ><spring:message code="Home.empty.none" /></p>
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
        document.addEventListener("DOMContentLoaded", function () {
            var modalEl = document.getElementById('psswd');
            if (modalEl) {
                var modal = new bootstrap.Modal(modalEl);
                modal.show();
            }
        });
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
