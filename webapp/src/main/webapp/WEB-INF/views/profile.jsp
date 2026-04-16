<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<ui:layout title="Vinyland | Productos">
    <div class="profile-section">
        <div class="container-fluid profile-shell">

            <c:if test="${param.updated eq '1'}">
                <div class="alert alert-success" role="alert">
                    El perfil se actualizó correctamente.
                </div>
            </c:if>

            <nav aria-label="breadcrumb">
                <ol class="breadcrumb">
                    <li class="breadcrumb-item">
                        <a href="<c:url value="/"/>">Inicio</a>
                    </li>
                    <li class="breadcrumb-item active" aria-current="page">Mi perfil</li>
                </ol>
            </nav>

            <div class="profile-layout-grid">
                <!-- Sidebar -->
                <aside class="profile-sidebar-column">
                    <div class="profile-card">
                        <div class="profile-avatar">
                            <img src="${user.avatarUrl}" alt="Avatar" class="img-fluid rounded-circle"/>
                        </div>
                        <div class="profile-info">
                            <h3 class="profile-name m-0">
                                <c:out value="${user.username}" />
                            </h3>
                            <p class="profile-email m-0">
                                <c:out value="${user.email}" />
                            </p>
                        </div>

                        <div class="profile-actions">
                            <a href="<c:url value='/profile/edit'/>" class="btn btn-outline-dark btn-sm">
                                Editar perfil
                            </a>
                        </div>
                    </div>
                </aside>

                <!-- Main content -->
                <section class="profile-content-column">
                    <div class="profile-header">
                        <div class="profile-header-titles">
                            <h2 class="profile-count m-0">
                                <c:out value="${fn:length(userProducts)}" /> publicaciones
                            </h2>
                        </div>
                        <div class="profile-header-actions">
                            <a href="<c:url value='/products/new'/>" class="btn btn-dark">
                                Publicar vinilo
                            </a>
                        </div>
                    </div>

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
                            <div class="alert alert-secondary" role="alert">
                                Todavía no publicaste ningún vinilo.
                            </div>
                        </c:otherwise>
                    </c:choose>
                </section>
            </div>
        </div>
    </div>
</ui:layout>
