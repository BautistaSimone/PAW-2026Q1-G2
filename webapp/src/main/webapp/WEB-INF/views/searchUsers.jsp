<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<spring:message code="SearchUsers.title" var="searchUsersTitle" />
<spring:message code="SearchUsers.carousel.viewMore.ariaLabel" var="carouselViewMoreAria" />
<spring:message code="SearchUsers.carousel.prev.ariaLabel" var="carouselPrevAria" />
<spring:message code="SearchUsers.carousel.next.ariaLabel" var="carouselNextAria" />

<ui:layout title="${searchUsersTitle}">

    <ui:header showHeaderActions="true" searchMode="users" searchValue="${searchQuery}" />

    <div class="products-section community-page">
        <div class="container-fluid products-shell">

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
                            <c:set var="userProductsPage" value="${sellerProductPagesByUserId[u.id]}" />
                            <c:set var="userProducts" value="${userProductsPage.results}" />

                            <article class="community-seller-row" id="seller-<c:out value='${u.id}' />">
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

                                <section class="community-carousel">
                                    <div class="community-carousel-header">
                                        <div>
                                            <h2 class="community-carousel-title">
                                                <spring:message code="SearchUsers.publishedVinyls" />
                                            </h2>
                                            <p class="community-carousel-count">
                                                <spring:message code="SearchUsers.carousel.total" arguments="${userProductsPage.totalCount}" />
                                            </p>
                                        </div>
                                    </div>

                                    <div class="community-carousel-frame">
                                        <c:if test="${not empty userProducts}">
                                            <button type="button" class="community-carousel-button community-carousel-button-prev" data-carousel-scroll-prev aria-label="${carouselPrevAria}">
                                                <i class="bi bi-chevron-left" aria-hidden="true"></i>
                                            </button>
                                            <button type="button" class="community-carousel-button community-carousel-button-next" data-carousel-scroll-next aria-label="${carouselNextAria}">
                                                <i class="bi bi-chevron-right" aria-hidden="true"></i>
                                            </button>
                                        </c:if>

                                        <div class="community-carousel-track" data-carousel-scroll-track>
                                            <c:choose>
                                                <c:when test="${not empty userProducts}">
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
                                                    <a href="<c:out value='${profileUrl}' />" class="community-carousel-more-tile" aria-label="${carouselViewMoreAria}">
                                                        <i class="bi bi-arrow-right-circle" aria-hidden="true"></i>
                                                        <span><spring:message code="SearchUsers.carousel.viewMore" /></span>
                                                    </a>
                                                </c:when>
                                                <c:otherwise>
                                                    <div class="community-carousel-message community-carousel-message-empty">
                                                        <spring:message code="SearchUsers.carousel.empty" />
                                                    </div>
                                                </c:otherwise>
                                            </c:choose>
                                        </div>
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

    <c:url value="/assets/js/community.js" var="communityJsUrl">
        <c:param name="v" value="carousel-floating-v3" />
    </c:url>
    <script src="<c:out value='${communityJsUrl}' />"></script>
</ui:layout>
