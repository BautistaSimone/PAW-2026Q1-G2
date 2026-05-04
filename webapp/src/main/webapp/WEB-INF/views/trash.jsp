<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<spring:message code="Trash.title" var="trashPageTitle" />
<ui:layout title="${trashPageTitle}">

    <ui:header showHeaderActions="true" />

    <div class="container py-4">
        <div class="mb-4">
            <a href="<c:url value='/profile'><c:param name="tab" value="trash"/></c:url>"
                style="display: inline-flex; align-items: center; gap: 0.25rem; color: var(--color-text-muted); font-weight: 500; text-decoration: none;">
                <i class="bi bi-arrow-left" aria-hidden="true"></i>
                <span><spring:message code="Trash.back" /></span>
            </a>
        </div>

        <h1 class="mb-3 profile-h2-22" >
            <i class="bi bi-trash3" aria-hidden="true"></i> <spring:message code="Trash.heading" />
        </h1>

        <c:if test="${param.restored eq '1'}">
            <div class="alert-retro alert-retro-success mb-3" role="alert">
                <i class="bi bi-check-circle" aria-hidden="true"></i>
                <spring:message code="Trash.alert.restored" />
            </div>
        </c:if>
        <c:if test="${param.restoreError eq '1'}">
            <div class="alert-retro alert-retro-warning mb-3" role="alert">
                <i class="bi bi-exclamation-triangle" aria-hidden="true"></i>
                <spring:message code="Trash.alert.restoreError" />
            </div>
        </c:if>

        <c:choose>
            <c:when test="${not empty deletedProducts}">
                <div class="profile-publications-stack deleted-products-stack">
                    <div class="products-grid profile-listings-grid">
                        <c:forEach items="${deletedProducts}" var="product">
                            <div class="products-grid-item">
                                <ui:productCard
                                    title="${product.title}"
                                    artist="${product.artist}"
                                    price="${product.price}"
                                    installments="${product.installmentPrice}"
                                    imageUrl="${productImageUrls[product.id]}"
                                    categories="${product.categories}"
                                    sellerRating="${sellerRating}"
                                    href="#"
                                    linkDisabled="true" />
                                <c:url var="restoreProductUrl" value="/products/${product.id}/restore" />
                                <spring:message code="Trash.restore.confirm" var="confirmRestore" />
                                <form action="${restoreProductUrl}" method="post" class="mt-2 flex-shrink-0" onsubmit="return confirm('${confirmRestore}');">
                                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
                                    <button type="submit" class="btn btn-retro btn-retro-primary w-100 profile-button-3" >
                                        <i class="bi bi-arrow-counterclockwise" aria-hidden="true"></i> <spring:message code="Trash.restore.button" />
                                    </button>
                                </form>
                            </div>
                        </c:forEach>
                    </div>
                    <ui:pagination result="${deletedProductsPage}" />
                </div>
            </c:when>
            <c:otherwise>
                <div class="empty-products-state profile-div-23" >
                    <i class="bi bi-trash3 profile-i-4" ></i>
                    <p class="profile-p-24" >
                        <spring:message code="Trash.empty" />
                    </p>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</ui:layout>
