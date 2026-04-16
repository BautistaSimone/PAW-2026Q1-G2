<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<ui:layout title="Vinyland | ${product.title}">

    <ui:header showHeaderActions="true"/>

    <div class="container py-4">
        <c:if test="${param.created eq '1'}">
            <div class="alert-retro alert-retro-success mb-3" role="alert">
                <i class="bi bi-check-circle" aria-hidden="true"></i>
                El vinilo se publico correctamente.
            </div>
        </c:if>

        <nav aria-label="breadcrumb" class="mb-3">
            <ol class="breadcrumb">
                <li class="breadcrumb-item">
                    <a href="<c:url value='/'/>">Inicio</a>
                </li>
                <li class="breadcrumb-item active" aria-current="page"><c:out value='${product.title}'/></li>
            </ol>
        </nav>

        <div class="product-detail-header">
            <a class="product-detail-back" href="<c:url value='/'/>">
                <i class="bi bi-arrow-left" aria-hidden="true"></i>
                Volver
            </a>
        </div>

        <div class="row g-4">
            <div class="col-lg-6 mb-4">
                <div id="productDetailGallery" class="product-detail-gallery">
                    <div class="product-gallery-main">
                        <c:choose>
                            <c:when test="${not empty productImageUrl}">
                                <img id="productGalleryMain"
                                     src="<c:url value='${productImageUrl}'/>"
                                     alt="<c:out value='${product.artist}'/> — <c:out value='${product.title}'/>"
                                     class="product-gallery-main-img" />
                            </c:when>
                            <c:otherwise>
                                <img id="productGalleryMain"
                                     src="https://via.placeholder.com/600x600?text=Sin+imagen"
                                     alt="<c:out value='${product.artist}'/> — <c:out value='${product.title}'/>"
                                     class="product-gallery-main-img" />
                            </c:otherwise>
                        </c:choose>
                    </div>
                    <c:if test="${not empty productImages}">
                        <div class="product-gallery-thumbs" role="group" aria-label="Galeria de imagenes del producto">
                            <c:forEach items="${productImages}" var="img" varStatus="st">
                                <c:url var="galleryImgUrl" value="/images/${img.imageId}" />
                                <button type="button"
                                        class="product-gallery-thumb<c:if test='${st.first}'> is-active</c:if>"
                                        data-full-src="${galleryImgUrl}"
                                        aria-label="Ver imagen ${st.index + 1} de ${fn:length(productImages)}"
                                        aria-pressed="${st.first}">
                                    <img src="<c:url value='${galleryImgUrl}'/>" alt="" loading="lazy" />
                                </button>
                            </c:forEach>
                        </div>
                    </c:if>
                </div>
            </div>

            <div class="col-lg-6">
                <div style="background: var(--color-card-bg); border-radius: var(--radius-xl); padding: 2rem; border: 1.5px solid var(--color-border); box-shadow: var(--shadow-sm);">
                    <h1 style="font-family: var(--font-heading); font-size: 1.75rem; font-weight: 700; margin-bottom: 0.25rem;"><c:out value="${product.title}"/></h1>
                    <h2 style="font-size: 1.1rem; font-weight: 400; color: var(--color-text-muted); margin-bottom: 1rem;"><c:out value="${product.artist}"/></h2>

                    <div style="margin-bottom: 1.25rem;">
                        <c:if test="<c:out value='${not empty product.recordLabel}'/>">
                            <p style="color: var(--color-text-muted); margin-bottom: 0.3rem; font-size: 0.92rem;">
                                <span style="font-weight: 600;">Sello:</span>
                                <c:out value="${product.recordLabel}" />
                            </p>
                        </c:if>
                        <c:if test="<c:out value='${not empty product.catalogNumber}'/>">
                            <p style="color: var(--color-text-muted); margin-bottom: 0.3rem; font-size: 0.92rem;">
                                <span style="font-weight: 600;">Catalogo:</span>
                                <c:out value="${product.catalogNumber}" />
                            </p>
                        </c:if>
                        <c:if test="<c:out value='${not empty product.editionCountry}'/>">
                            <p style="color: var(--color-text-muted); margin-bottom: 0.3rem; font-size: 0.92rem;">
                                <span style="font-weight: 600;">Pais:</span>
                                <c:out value="${product.editionCountry}" />
                            </p>
                        </c:if>
                    </div>

                    <div style="margin-bottom: 1.5rem; padding: 1rem 0; border-top: 1.5px solid var(--color-border); border-bottom: 1.5px solid var(--color-border);">
                        <div style="font-family: var(--font-mono); font-size: 2.25rem; font-weight: 700; color: var(--color-accent);">$<c:out value='${product.price}'/></div>
                    </div>

                    <div style="margin-bottom: 1.5rem;">
                        <h5 style="font-family: var(--font-heading); font-weight: 700; margin-bottom: 0.75rem;">Descripcion</h5>
                        <div style="background: var(--color-bg-light); padding: 1.25rem; border-radius: var(--radius-md); border: 1px solid var(--color-border);">
                            <p style="margin-bottom: 0.75rem; line-height: 1.6; font-size: 0.95rem;">
                                <c:out value="${product.description}" />
                            </p>
                            <c:if test="<c:out value='${not empty product.categories}'/>">
                                <div style="margin-bottom: 0.75rem;">
                                    <strong style="font-size: 0.85rem;">Generos:</strong>
                                    <c:forEach items="<c:out value='${product.categories}'/>" var="cat" varStatus="status">
                                        <span class="badge" style="background: var(--color-accent); color: #fff; font-size: 0.78rem; padding: 0.25rem 0.6rem; border-radius: var(--radius-pill);"><c:out value="<c:out value='${cat.name}'/>" /></span>
                                    </c:forEach>
                                </div>
                            </c:if>
                            <div class="row g-2 mt-2">
                                <div class="col-6">
                                    <div class="condition-badge">
                                        <span class="condition-badge-label">Estado Disco</span>
                                        <span class="condition-badge-value"><c:out value="${product.recordCondition}" /></span><span class="condition-badge-max">/10</span>
                                    </div>
                                </div>
                                <div class="col-6">
                                    <div class="condition-badge">
                                        <span class="condition-badge-label">Estado Tapa</span>
                                        <span class="condition-badge-value"><c:out value="${product.sleeveCondition}" /></span><span class="condition-badge-max">/10</span>
                                    </div>
                                </div>
                            </div>
                            <p style="margin-top: 1rem; margin-bottom: 0; color: var(--color-text-muted); font-size: 0.9rem;">
                                <i class="bi bi-geo-alt-fill" aria-hidden="true" style="color: var(--color-accent);"></i> <c:out value="${product.location}" />
                            </p>
                        </div>
                    </div>

                    <div class="d-grid gap-2">
                        <c:url var="purchasePostUrl" value='/purchases'/>
                        <form:form modelAttribute="purchaseCreateForm" action="${purchasePostUrl}" method="POST" cssClass="d-flex w-100 flex-column gap-2">
                            <input type="hidden" name="productId" value="${product.id}" />
                            <form:errors path="buyerEmail" cssClass="text-danger" element="div" />
                            <button type="submit" class="btn btn-retro btn-retro-dark btn-lg w-100">
                                <i class="bi bi-cart-plus" aria-hidden="true"></i> Iniciar Compra
                            </button>
                        </form:form>
                        <a class="btn btn-retro btn-retro-outline btn-lg" href="<c:url value='/'/>">
                            <i class="bi bi-arrow-left" aria-hidden="true"></i> Seguir comprando
                        </a>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <c:if test="${not empty productImages}">
    <script>
    (function () {
        var root = document.getElementById('productDetailGallery');
        var main = document.getElementById('productGalleryMain');
        if (!root || !main) {
            return;
        }
        var thumbs = root.querySelectorAll('.product-gallery-thumb');
        thumbs.forEach(function (btn) {
            btn.addEventListener('click', function () {
                var src = btn.getAttribute('data-full-src');
                if (src) {
                    main.src = src;
                }
                thumbs.forEach(function (b) {
                    b.classList.remove('is-active');
                    b.setAttribute('aria-pressed', 'false');
                });
                btn.classList.add('is-active');
                btn.setAttribute('aria-pressed', 'true');
            });
        });
    })();
    </script>
    </c:if>
</ui:layout>
