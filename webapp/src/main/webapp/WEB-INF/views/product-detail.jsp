<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<ui:layout title="Vinyland | <c:out value='${product.title}'/>">
    <div class="container py-4">
        <c:if test="${param.created eq '1'}">
            <div class="alert alert-success" role="alert">
                El vinilo se publicó correctamente.
            </div>
        </c:if>

        <nav aria-label="breadcrumb" class="mb-4">
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

        <div class="row">
            <div class="col-md-6 mb-4">
                <div id="productDetailGallery" class="product-detail-gallery">
                    <div class="product-gallery-main">
                        <c:choose>
                            <c:when test="<c:out value='${not empty productImageUrl}'/>">
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
                        <div class="product-gallery-thumbs" role="group" aria-label="Galería de imágenes del producto">
                            <c:forEach items="<c:out value='${productImages}'/>" var="img" varStatus="st">
                                <c:url var="galleryImgUrl" value="/images/<c:out value='${img.imageId}'/>" />
                                <button type="button"
                                        class="product-gallery-thumb<c:if test='${st.first}'> is-active</c:if>"
                                        data-full-src="<c:out value='${galleryImgUrl}'/>"
                                        aria-label="Ver imagen ${st.index + 1} de ${fn:length(productImages)}"
                                        aria-pressed="${st.first}">
                                    <img src="<c:out value='${galleryImgUrl}'/>" alt="" loading="lazy" />
                                </button>
                            </c:forEach>
                        </div>
                    </c:if>
                </div>
            </div>

            <div class="col-md-6">
                <h1 class="h2 fw-bold mb-2">${product.title}</h1>
                <h2 class="h4 text-muted mb-2">${product.artist}</h2>
                <div class="product-metadata mb-4">
                    <c:if test="<c:out value='${not empty product.recordLabel}'/>">
                        <p class="text-muted mb-1">
                            <span class="fw-semibold">Sello:</span>
                            <c:out value="${product.recordLabel}" />
                        </p>
                    </c:if>
                    <c:if test="<c:out value='${not empty product.catalogNumber}'/>">
                        <p class="text-muted mb-1">
                            <span class="fw-semibold">Número de catálogo:</span>
                            <c:out value="${product.catalogNumber}" />
                        </p>
                    </c:if>
                    <c:if test="<c:out value='${not empty product.editionCountry}'/>">
                        <p class="text-muted mb-1">
                            <span class="fw-semibold">País de la edición:</span>
                            <c:out value="${product.editionCountry}" />
                        </p>
                    </c:if>
                </div>

                <div class="mb-4">
                    <div class="h1 fw-bold" style="color: var(--color-accent);">$<c:out value='${product.price}'></div>
                </div>

                <div class="mb-4">
                    <h5 class="fw-bold">Descripción:</h5>
                    <div class="bg-light p-3 rounded">
                        <p class="mb-3">
                            <c:out value="${product.description}" />
                        </p>
                        <c:if test="<c:out value='${not empty product.categories}'/>">
                            <p class="mb-1">
                                <strong>Géneros:</strong>
                                <c:forEach items="<c:out value='${product.categories}'/>" var="cat" varStatus="status">
                                    <span class="badge bg-secondary"><c:out value="<c:out value='${cat.name}'/>" /></span>
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
                    <form action="<c:url value='/purchases'/>" method="POST" class="d-flex w-100 flex-column gap-2">
                        <input type="hidden" name="productId" value="${product.id}" />
                        <input type="email" name="buyerEmail" class="form-control form-control-lg" placeholder="Ingresa tu email para comprar" required />
                        <button type="submit" class="btn btn-dark btn-lg w-100">Iniciar Compra</button>
                    </form>
                    <a class="btn btn-outline-secondary btn-lg" href="<c:url value='/'/>">Seguir comprando</a>
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
