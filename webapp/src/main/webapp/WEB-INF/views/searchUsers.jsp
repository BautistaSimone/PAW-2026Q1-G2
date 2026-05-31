<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<spring:message code="SearchUsers.title" var="searchUsersTitle" />
<spring:message code="SearchUsers.placeholder" var="searchPlaceholder" />
<spring:message code="SearchUsers.carousel.prev.ariaLabel" var="carouselPrevAria" />
<spring:message code="SearchUsers.carousel.next.ariaLabel" var="carouselNextAria" />
<spring:message code="SearchUsers.carousel.empty" var="carouselEmptyMessage" />
<spring:message code="SearchUsers.carousel.error" var="carouselErrorMessage" />
<spring:message code="SearchUsers.carousel.loading" var="carouselLoadingMessage" />
<spring:message code="SearchUsers.carousel.pageLabel" var="carouselPageLabel" />
<spring:message code="SearchUsers.carousel.ofLabel" var="carouselOfLabel" />

<ui:layout title="${searchUsersTitle}">

    <ui:header showHeaderActions="true" />

    <div class="products-section community-page">
        <div class="container-fluid products-shell">

            <div class="community-hero">
                <div class="community-hero-copy">
                    <span class="community-eyebrow">
                        <i class="bi bi-people" aria-hidden="true"></i>
                        <spring:message code="SearchUsers.eyebrow" />
                    </span>
                    <h1 class="community-heading">
                        <spring:message code="SearchUsers.heading" />
                    </h1>
                    <p class="community-subtitle">
                        <spring:message code="SearchUsers.subtitle" />
                    </p>
                </div>

                <form class="search-users-form" method="get" action="<c:url value='/search-users'/>" novalidate>
                    <div class="search-users-input-group">
                        <input name="q" type="text" class="form-control search-users-input"
                            placeholder="${searchPlaceholder}"
                            value="<c:out value='${searchQuery}' />"
                            aria-label="${searchPlaceholder}" />
                        <button type="submit" class="btn btn-retro btn-retro-primary community-search-button">
                            <i class="bi bi-search" aria-hidden="true"></i>
                            <spring:message code="SearchUsers.button" />
                        </button>
                    </div>
                </form>
            </div>

            <div class="community-section-heading">
                <c:choose>
                    <c:when test="${showingSearchResults}">
                        <h2>
                            <spring:message code="SearchUsers.resultsFor" />
                            <span>"<c:out value='${searchQuery}' />"</span>
                        </h2>
                    </c:when>
                    <c:otherwise>
                        <h2><spring:message code="SearchUsers.mostFollowed" /></h2>
                    </c:otherwise>
                </c:choose>
            </div>

            <c:choose>
                <c:when test="${not empty users}">
                    <div class="community-seller-list">
                        <c:forEach items="${users}" var="u">
                            <c:url value="/profile" var="profileUrl">
                                <c:param name="userId" value="${u.id}" />
                            </c:url>
                            <c:url value="/search-users/${u.id}/products" var="communityProductsEndpoint" />
                            <c:set var="userProductsPage" value="${communityProductsByUserId[u.id]}" />
                            <c:set var="userProducts" value="${userProductsPage.results}" />

                            <article class="community-seller-row">
                                <aside class="community-user-panel">
                                    <a href="<c:out value='${profileUrl}' />" class="community-user-link">
                                        <div class="community-avatar">
                                            <c:out value="${fn:substring(u.username, 0, 1)}" />
                                        </div>
                                        <div class="community-user-main">
                                            <h3 class="community-username">
                                                <c:out value="${u.username}" />
                                            </h3>
                                            <c:if test="${not empty u.firstName or not empty u.lastName}">
                                                <p class="community-user-name">
                                                    <c:out value="${u.firstName}" />
                                                    <c:out value="${u.lastName}" />
                                                </p>
                                            </c:if>
                                        </div>
                                    </a>

                                    <div class="community-stats" aria-label="<spring:message code='SearchUsers.stats.ariaLabel' />">
                                        <div class="community-stat">
                                            <i class="bi bi-people" aria-hidden="true"></i>
                                            <strong><c:out value="${userFollowerCounts[u.id]}" /></strong>
                                            <span><spring:message code="SearchUsers.followers.label" /></span>
                                        </div>
                                        <div class="community-stat">
                                            <i class="bi bi-vinyl" aria-hidden="true"></i>
                                            <strong><c:out value="${userPublicationCounts[u.id]}" /></strong>
                                            <span><spring:message code="SearchUsers.publications.label" /></span>
                                        </div>
                                    </div>

                                    <div class="community-user-actions">
                                        <a href="<c:out value='${profileUrl}' />" class="btn btn-retro btn-retro-outline community-profile-link">
                                            <i class="bi bi-person" aria-hidden="true"></i>
                                            <spring:message code="SearchUsers.viewProfile" />
                                        </a>
                                        <sec:authorize access="isAuthenticated()">
                                            <c:if test="${u.id != currentUserId}">
                                                <form action="<c:url value='/profile/follow' />" method="post" class="community-follow-form">
                                                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
                                                    <input type="hidden" name="userId" value="${u.id}" />
                                                    <c:choose>
                                                        <c:when test="${followStatusMap[u.id]}">
                                                            <button type="submit" class="btn btn-retro btn-retro-secondary community-follow-button">
                                                                <i class="bi bi-person-dash" aria-hidden="true"></i>
                                                                <spring:message code="Profile.unfollow" />
                                                            </button>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <button type="submit" class="btn btn-retro btn-retro-primary community-follow-button">
                                                                <i class="bi bi-person-plus" aria-hidden="true"></i>
                                                                <spring:message code="Profile.follow" />
                                                            </button>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </form>
                                            </c:if>
                                        </sec:authorize>
                                    </div>
                                </aside>

                                <section class="community-carousel"
                                    data-community-carousel
                                    data-endpoint="<c:out value='${communityProductsEndpoint}' />"
                                    data-current-page="${userProductsPage.currentPage}"
                                    data-total-pages="${userProductsPage.totalPages}"
                                    data-empty-message="${carouselEmptyMessage}"
                                    data-error-message="${carouselErrorMessage}"
                                    data-loading-message="${carouselLoadingMessage}"
                                    data-page-label="${carouselPageLabel}"
                                    data-of-label="${carouselOfLabel}">
                                    <div class="community-carousel-header">
                                        <div>
                                            <h2 class="community-carousel-title">
                                                <spring:message code="SearchUsers.publishedVinyls" />
                                            </h2>
                                            <p class="community-carousel-count">
                                                <spring:message code="SearchUsers.carousel.total" arguments="${userProductsPage.totalCount}" />
                                            </p>
                                        </div>
                                        <div class="community-carousel-controls">
                                            <button type="button" class="community-carousel-button"
                                                data-carousel-prev
                                                aria-label="${carouselPrevAria}"
                                                ${userProductsPage.hasPreviousPage ? '' : 'disabled'}>
                                                <i class="bi bi-chevron-left" aria-hidden="true"></i>
                                            </button>
                                            <span class="community-carousel-status" data-carousel-status>
                                                <spring:message code="SearchUsers.carousel.pageStatus" arguments="${userProductsPage.currentPage},${userProductsPage.totalPages}" />
                                            </span>
                                            <button type="button" class="community-carousel-button"
                                                data-carousel-next
                                                aria-label="${carouselNextAria}"
                                                ${userProductsPage.hasNextPage ? '' : 'disabled'}>
                                                <i class="bi bi-chevron-right" aria-hidden="true"></i>
                                            </button>
                                        </div>
                                    </div>

                                    <div class="community-carousel-loading" data-carousel-loading hidden>
                                        <span class="community-spinner" aria-hidden="true"></span>
                                        <span><spring:message code="SearchUsers.carousel.loading" /></span>
                                    </div>

                                    <div class="community-carousel-track" data-carousel-track>
                                        <c:forEach items="${userProducts}" var="product">
                                            <c:url value="/products/${product.id}" var="productUrl" />
                                            <a href="<c:out value='${productUrl}' />" class="community-product-tile">
                                                <div class="community-product-cover">
                                                    <c:choose>
                                                        <c:when test="${not empty productImageUrls[product.id]}">
                                                            <img src="<c:url value='${productImageUrls[product.id]}' />"
                                                                alt="<c:out value='${product.artist}' /> - <c:out value='${product.title}' />" />
                                                        </c:when>
                                                        <c:otherwise>
                                                            <i class="bi bi-vinyl" aria-hidden="true"></i>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </div>
                                                <div class="community-product-body">
                                                    <h3><c:out value="${product.title}" /></h3>
                                                    <p><c:out value="${product.artist}" /></p>
                                                    <span><ui:price value="${product.price}" /></span>
                                                </div>
                                            </a>
                                        </c:forEach>
                                    </div>
                                </section>
                            </article>
                        </c:forEach>
                    </div>

                    <ui:pagination result="${usersPage}" />
                </c:when>
                <c:otherwise>
                    <div class="empty-products-state community-empty-state">
                        <i class="bi bi-people search-users-empty-icon"></i>
                        <p class="search-users-empty-text">
                            <c:choose>
                                <c:when test="${showingSearchResults}">
                                    <spring:message code="SearchUsers.noResults" />
                                </c:when>
                                <c:otherwise>
                                    <spring:message code="SearchUsers.empty" />
                                </c:otherwise>
                            </c:choose>
                        </p>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>

    <script src="<c:url value='/assets/js/community.js'/>"></script>
</ui:layout>
