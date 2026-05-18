<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
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

            <c:choose>
                <c:when test="${not hasFollowing}">
                    <div class="empty-products-state">
                        <i class="bi bi-people foryou-empty-icon"></i>
                        <p class="foryou-empty-text">
                            <spring:message code="ForYou.empty.noFollowing" />
                        </p>
                        <a href="<c:url value='/search-users'/>" class="btn btn-retro btn-retro-primary">
                            <i class="bi bi-search" aria-hidden="true"></i>
                            <spring:message code="ForYou.empty.cta" />
                        </a>
                    </div>
                </c:when>
                <c:when test="${empty products}">
                    <div class="empty-products-state">
                        <i class="bi bi-vinyl foryou-empty-icon"></i>
                        <p class="foryou-empty-text">
                            <spring:message code="ForYou.empty.noProducts" />
                        </p>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="products-grid" id="productsGrid">
                        <c:forEach items="${products}" var="product" varStatus="loop">
                            <div class="products-grid-item" data-product-index="${loop.index}">
                                <c:url value="/products/${product.id}" var="productUrl"/>
                                <ui:productCard
                                    title="${product.title}"
                                    artist="${product.artist}"
                                    price="${product.price}"
                                    installments="${product.installmentPrice}"
                                    imageUrl="${productImageUrls[product.id]}"
                                    categories="${product.categories}"
                                    sellerRating="${sellerRatingByUserId[product.userId]}"
                                    href="${productUrl}"/>
                            </div>
                        </c:forEach>
                    </div>
                    <ui:pagination result="${productsPage}" />
                </c:otherwise>
            </c:choose>
        </div>
    </div>

</ui:layout>
