<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<ui:layout title="Vinyland | Productos">
    <div class="products-section">
        <div class="container-fluid products-shell">
            <c:if test="${param.created eq '1'}">
                <div class="alert alert-success" role="alert">
                    El vinilo se publico correctamente.
                </div>
            </c:if>

            <nav aria-label="breadcrumb">
                <ol class="breadcrumb">
                    <li class="breadcrumb-item">
                        <a href="<c:url value="/"/>">Inicio</a>
                    </li>
                    <li class="breadcrumb-item active" aria-current="page">Productos</li>
                </ol>
            </nav>

            <div class="products-layout-grid">
                <aside class="products-filters-column">
                    <ui:filtersBar />
                </aside>

                <section class="products-content-column">
                    <div class="products-header">
                        <h2 class="products-count m-0">${fn:length(products)} productos</h2>
                        <a href="<c:url value='/products/new'/>" class="btn btn-dark">Publicar vinilo</a>
                    </div>

                    <c:choose>
                        <c:when test="${not empty products}">
                            <div class="products-grid">
                                <c:forEach items="${products}" var="product">
                                    <div class="products-grid-item">
                                        <ui:productCard
                                                title="${product.title}"
                                                artist="${product.artist}"
                                                price="${product.price}"
                                                installments="${product.installmentPrice}"
                                                imageUrl="${productImageUrls[product.id]}"
                                                href="<c:url value='/products/${product.id}'/>"/>
                                    </div>
                                </c:forEach>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="alert alert-secondary" role="alert">
                                Todavia no hay productos cargados. Publica un vinilo para probar el alta.
                            </div>
                        </c:otherwise>
                    </c:choose>
                </section>
            </div>
        </div>
    </div>
</ui:layout>
