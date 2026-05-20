<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
        <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
            <%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
                <%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
                    <%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

                        <spring:message code="ForYou.title" var="forYouTitle" />
                        <ui:layout title="${forYouTitle}">

                            <ui:header showHeaderActions="true" />

                            <div class="products-section">
                                <div class="container-fluid products-shell">

                                    <div class="foryou-header">
                                        <h1 class="foryou-heading">
                                            <i class="bi bi-heart" aria-hidden="true"></i>
                                            <spring:message code="ForYou.heading" />
                                        </h1>
                                        <p class="foryou-subheading">
                                            <spring:message code="ForYou.subheading" />
                                        </p>
                                    </div>

                                    <div class="foryou-section">
                                        <div class="foryou-section-header">
                                            <h2 class="foryou-section-title">
                                                <i class="bi bi-stars" aria-hidden="true"></i>
                                                <spring:message code="ForYou.section.suggestedUsers" />
                                            </h2>
                                        </div>
                                        <c:choose>
                                            <c:when test="${not empty suggestedUsers}">
                                                <div class="foryou-user-carousel">
                                                    <c:forEach items="${suggestedUsers}" var="u">
                                                        <div class="foryou-user-card">
                                                            <a href="<c:url value='/profile?userId=${u.id}'/>"
                                                                class="foryou-user-link">
                                                                <div class="foryou-user-avatar">
                                                                    <c:out value="${fn:substring(u.username, 0, 1)}" />
                                                                </div>
                                                                <div class="foryou-user-info">
                                                                    <div class="foryou-user-username">
                                                                        <c:out value="${u.username}" />
                                                                    </div>
                                                                    <c:if
                                                                        test="${not empty u.firstName or not empty u.lastName}">
                                                                        <div class="foryou-user-name">
                                                                            <c:out value="${u.firstName}" />
                                                                            <c:out value="${u.lastName}" />
                                                                        </div>
                                                                    </c:if>
                                                                    <div class="foryou-user-followers">
                                                                        <i class="bi bi-people" aria-hidden="true"></i>
                                                                        <c:out
                                                                            value="${suggestedFollowerCounts[u.id]}" />
                                                                        <spring:message code="Profile.followers" />
                                                                    </div>
                                                                </div>
                                                            </a>
                                                            <sec:authorize access="isAuthenticated()">
                                                                <c:if test="${u.id != currentUserId}">
                                                                    <form action="<c:url value='/profile/follow' />"
                                                                        method="post" class="foryou-user-action">
                                                                        <input type="hidden"
                                                                            name="${_csrf.parameterName}"
                                                                            value="${_csrf.token}" />
                                                                        <input type="hidden" name="userId"
                                                                            value="${u.id}" />
                                                                        <c:choose>
                                                                            <c:when
                                                                                test="${suggestedFollowStatusMap[u.id]}">
                                                                                <button type="submit"
                                                                                    class="btn btn-retro btn-retro-secondary btn-follow-sm">
                                                                                    <spring:message
                                                                                        code="Profile.unfollow" />
                                                                                </button>
                                                                            </c:when>
                                                                            <c:otherwise>
                                                                                <button type="submit"
                                                                                    class="btn btn-retro btn-retro-primary btn-follow-sm">
                                                                                    <spring:message
                                                                                        code="Profile.follow" />
                                                                                </button>
                                                                            </c:otherwise>
                                                                        </c:choose>
                                                                    </form>
                                                                </c:if>
                                                            </sec:authorize>
                                                        </div>
                                                    </c:forEach>
                                                </div>
                                            </c:when>
                                            <c:otherwise>
                                                <div class="empty-products-state">
                                                    <i class="bi bi-people foryou-empty-icon"></i>
                                                    <p class="foryou-empty-text">
                                                        <spring:message code="SearchUsers.empty" />
                                                    </p>
                                                </div>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>

                                    <div class="foryou-section">
                                        <div class="foryou-section-header">
                                            <h2 class="foryou-section-title">
                                                <i class="bi bi-heart" aria-hidden="true"></i>
                                                <spring:message code="ForYou.section.wishlist" />
                                            </h2>
                                        </div>
                                        <c:choose>
                                            <c:when test="${not empty wishlistProducts}">
                                                <div class="products-grid">
                                                    <c:forEach items="${wishlistProducts}" var="product"
                                                        varStatus="loop">
                                                        <div class="products-grid-item"
                                                            data-product-index="${loop.index}">
                                                            <c:url value="/products/${product.id}" var="productUrl" />
                                                            <ui:productCard title="${product.title}"
                                                                artist="${product.artist}" price="${product.price}"
                                                                installments="${product.installmentPrice}"
                                                                imageUrl="${wishlistProductImageUrls[product.id]}"
                                                                categories="${product.categories}"
                                                                sellerRating="${wishlistSellerRatingByUserId[product.userId]}"
                                                                href="${productUrl}" />
                                                        </div>
                                                    </c:forEach>
                                                </div>
                                            </c:when>
                                            <c:otherwise>
                                                <div class="empty-products-state">
                                                    <i class="bi bi-heart foryou-empty-icon"></i>
                                                    <p class="foryou-empty-text">
                                                        <spring:message code="ForYou.empty.wishlist" />
                                                    </p>
                                                </div>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>

                                    <div class="foryou-section">
                                        <div class="foryou-section-header">
                                            <h2 class="foryou-section-title">
                                                <i class="bi bi-vinyl" aria-hidden="true"></i>
                                                <spring:message code="ForYou.section.following" />
                                            </h2>
                                        </div>
                                        <c:choose>
                                            <c:when test="${hasFollowing and not empty products}">
                                                <div class="products-grid" id="productsGrid">
                                                    <c:forEach items="${products}" var="product" varStatus="loop">
                                                        <div class="products-grid-item"
                                                            data-product-index="${loop.index}">
                                                            <c:url value="/products/${product.id}" var="productUrl" />
                                                            <ui:productCard title="${product.title}"
                                                                artist="${product.artist}" price="${product.price}"
                                                                installments="${product.installmentPrice}"
                                                                imageUrl="${productImageUrls[product.id]}"
                                                                categories="${product.categories}"
                                                                sellerRating="${sellerRatingByUserId[product.userId]}"
                                                                href="${productUrl}" />
                                                        </div>
                                                    </c:forEach>
                                                </div>
                                                <ui:pagination result="${productsPage}" />
                                            </c:when>
                                            <c:when test="${hasFollowing}">
                                                <div class="empty-products-state">
                                                    <i class="bi bi-vinyl foryou-empty-icon"></i>
                                                    <p class="foryou-empty-text">
                                                        <spring:message code="ForYou.empty.noProducts" />
                                                    </p>
                                                </div>
                                            </c:when>
                                            <c:otherwise>
                                                <div class="empty-products-state">
                                                    <i class="bi bi-people foryou-empty-icon"></i>
                                                    <p class="foryou-empty-text">
                                                        <spring:message code="ForYou.empty.noFollowing" />
                                                    </p>
                                                    <a href="<c:url value='/search-users'/>"
                                                        class="btn btn-retro btn-retro-primary">
                                                        <i class="bi bi-search" aria-hidden="true"></i>
                                                        <spring:message code="ForYou.empty.cta" />
                                                    </a>
                                                </div>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                </div>
                            </div>

                        </ui:layout>