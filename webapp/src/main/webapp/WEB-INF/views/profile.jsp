<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<ui:layout title="Vinyland | Perfil">

    <ui:header showHeaderActions="true"/>

    <div class="profile-page">
        <div class="container py-4">

            <nav aria-label="breadcrumb">
                <ol class="breadcrumb">
                    <li class="breadcrumb-item">
                        <a href="<c:url value="/"/>">Inicio</a>
                    </li>
                    <li class="breadcrumb-item active" aria-current="page">Perfil</li>
                </ol>
            </nav>

            <div class="profile-user-card">
                <div class="profile-avatar">
                    <c:out value="${fn:substring(user.username, 0, 1)}" />
                </div>
                <div class="profile-user-info">
                    <h1><c:out value="${user.username}" /></h1>
                    <h2><c:out value="${user.email}" /></h2>
                </div>
            </div>

            <h3 class="profile-section-title">
                <i class="bi bi-vinyl" aria-hidden="true"></i> Tus publicaciones
            </h3>

            <c:choose>
                <c:when test="${not empty userProducts}">
                    <div class="products-grid">
                        <c:forEach items="${userProducts}" var="product">
                            <div class="products-grid-item">
                                <c:url value="/products/${product.id}" var="productUrl"/>
                                <ui:productCard
                                        title="${product.title}"
                                        artist="${product.artist}"
                                        price="${product.price}"
                                        installments="${product.installmentPrice}"
                                        imageUrl="${productImageUrls[product.id]}"
                                        href="${productUrl}"/>
                            </div>
                        </c:forEach>
                    </div>
                </c:when>

                <c:otherwise>
                    <div class="empty-products-state">
                        <i class="bi bi-vinyl" style="font-size: 2.5rem; color: var(--color-border);"></i>
                        <p style="color: var(--color-text-muted); font-size: 1rem; margin: 0;">Todavia no publicaste ningun vinilo.</p>
                        <a href="<c:url value='/products/new'/>" class="btn btn-retro btn-retro-primary" style="justify-self: center;">
                            <i class="bi bi-plus-lg" aria-hidden="true"></i> Publicar tu primer vinilo
                        </a>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</ui:layout>
