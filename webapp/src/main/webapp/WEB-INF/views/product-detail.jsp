<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<ui:layout title="Vinyland | ${product.title}">
    <div class="container py-4">
        <c:if test="${param.created eq '1'}">
            <div class="alert alert-success" role="alert">
                El vinilo se publico correctamente.
            </div>
        </c:if>

        <nav aria-label="breadcrumb" class="mb-4">
            <ol class="breadcrumb">
                <li class="breadcrumb-item">
                    <a href="<c:url value='/'/>">Inicio</a>
                </li>
                <li class="breadcrumb-item active" aria-current="page">${product.title}</li>
            </ol>
        </nav>

        <div class="product-detail-header">
            <a class="product-detail-back" href="<c:url value='/'/>">
                <i class="bi bi-arrow-left" aria-hidden="true"></i>
                Volver
            </a>
        </div>

        <div class="row">
            <div class="col-md-6 mb-4">
                <div class="product-gallery">
                    <c:choose>
                        <c:when test="${not empty productImageUrl}">
                            <img src="<c:url value='${productImageUrl}'/>" alt="${product.artist} - ${product.title}" />
                        </c:when>
                        <c:otherwise>
                            <img src="https://via.placeholder.com/600x600?text=Sin+Imagen" alt="${product.artist} - ${product.title}" />
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>

            <div class="col-md-6">
                <h1 class="h2 fw-bold mb-2">${product.title}</h1>
                <h2 class="h4 text-muted mb-4">${product.artist}</h2>

                <div class="mb-4">
                    <div class="h1 fw-bold" style="color: var(--color-accent);">$${product.price}</div>
                    <div class="text-muted"><small>Disponible en cuotas</small></div>
                </div>

                <div class="mb-4">
                    <h5 class="fw-bold">Descripcion:</h5>
                    <div class="bg-light p-3 rounded">
                        <p class="mb-3">
                            <c:out value="${product.description}" />
                        </p>
                        <c:if test="${not empty product.categories}">
                            <p class="mb-1">
                                <strong>Generos:</strong>
                                <c:forEach items="${product.categories}" var="cat" varStatus="status">
                                    <span class="badge bg-secondary"><c:out value="${cat.name}" /></span>
                                </c:forEach>
                            </p>
                        </c:if>
                        <div class="mt-3 pt-3 border-top">
                            <div class="row g-2">
                                <div class="col-6">
                                    <div class="p-2 border rounded text-center bg-white">
                                        <small class="text-muted d-block uppercase mb-1" style="font-size: 0.7rem; font-weight: 700;">ESTADO DISCO</small>
                                        <span class="h5 fw-bold mb-0"><c:out value="${product.recordCondition}" /></span><span class="text-muted small">/10</span>
                                    </div>
                                </div>
                                <div class="col-6">
                                    <div class="p-2 border rounded text-center bg-white">
                                        <small class="text-muted d-block uppercase mb-1" style="font-size: 0.7rem; font-weight: 700;">ESTADO TAPA</small>
                                        <span class="h5 fw-bold mb-0"><c:out value="${product.sleeveCondition}" /></span><span class="text-muted small">/10</span>
                                    </div>
                                </div>
                            </div>
                            <p class="mt-3 mb-0 text-muted">
                                <i class="bi bi-geo-alt-fill me-1"></i> <c:out value="${product.location}" />
                            </p>
                        </div>

                    </div>
                </div>

                <div class="d-grid gap-2">
                    <button class="btn btn-dark btn-lg">Agregar al carrito</button>
                    <a class="btn btn-outline-secondary btn-lg" href="<c:url value='/'/>">Seguir comprando</a>
                </div>
            </div>
        </div>
    </div>
</ui:layout>