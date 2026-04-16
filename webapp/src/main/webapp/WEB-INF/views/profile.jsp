<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<ui:layout title="Vinyland | Perfil">

    <ui:header showHeaderActions="true"/>

    <div class="container py-4">

        <nav aria-label="breadcrumb">
            <ol class="breadcrumb">
                <li class="breadcrumb-item">
                    <a href="<c:url value="/"/>">Inicio</a>
                </li>
                <li class="breadcrumb-item active" aria-current="page">Perfil</li>
            </ol>
        </nav>

        <div class="row">

            <div class="col-md-6">
                <h1 class="h2 fw-bold mb-2"><c:out value="${user.username}" /></h1>
                <h2 class="h4 text-muted mb-2"><c:out value="${user.email}" /></h2>

                <br/>

                <h3 class="h4 fw-bold mb-2">Tus publicaciones</h2>
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
                </div>
            </div>
        </div>
    </div>
</ui:layout>
