<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<ui:layout title="Vinyland | Productos">
    <div class="products-section">
        <div class="container">
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

            <div class="row">
                <div class="col-lg-3 col-md-4 col-12">
                    <ui:filtersBar />
                </div>

                <div class="col-lg-9 col-md-8 col-12">
                    <div class="products-header d-flex justify-content-between align-items-center">
                        <h2 class="products-count m-0">${fn:length(products)} productos</h2>
                        <a href="<c:url value='/products/new'/>" class="btn btn-dark">Publicar vinilo</a>
                    </div>

                    <c:choose>
                        <c:when test="${not empty products}">
                            <div class="row">
                                <c:forEach items="${products}" var="product">
                                    <div class="col-lg-4 col-md-6 col-12 mb-4">
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
                </div>
            </div>
        </div>
    </div>
</ui:layout>
