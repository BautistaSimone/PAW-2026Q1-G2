<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<ui:layout title="Vinyland | ${product.title}">

    <ui:header showHeaderActions="true"/>

    <div class="container py-4">
        <c:if test="${param.created eq '1'}">
            <div class="alert-retro alert-retro-success mb-3" role="alert">
                <i class="bi bi-check-circle" aria-hidden="true"></i>
                El vinilo se publico correctamente.
            </div>
        </c:if>
        <c:if test="${param.reported eq '1'}">
            <div class="alert-retro alert-retro-success mb-3" role="alert">
                <i class="bi bi-check-circle" aria-hidden="true"></i>
                Gracias por el reporte. Nuestro equipo lo revisará.
            </div>
        </c:if>

        <div class="mb-4">
            <a href="<c:url value='/'/>" style="display: inline-flex; align-items: center; gap: 0.25rem; color: var(--color-text-muted); font-weight: 500; text-decoration: none; transition: color 0.2s;" onmouseover="this.style.color='var(--color-accent)';" onmouseout="this.style.color='var(--color-text-muted)';">
                <i class="bi bi-arrow-left" aria-hidden="true" style="font-size: 1.1rem;"></i> <span>Volver</span>
            </a>
        </div>

        <div class="row g-5">
            <!-- Left Side: Image Gallery -->
            <div class="col-lg-6 mb-4">
                <div id="productDetailGallery" class="product-detail-gallery">
                    <div class="product-gallery-main" style="background: linear-gradient(135deg, #ffffff 0%, #f4f1ec 100%); border-radius: 20px; box-shadow: 0 15px 35px rgba(0,0,0,0.08); border: 1px solid rgba(255,255,255,1); padding: 1rem; position: relative;">
                        <c:choose>
                            <c:when test="${not empty productImageUrl}">
                                <img id="productGalleryMain"
                                     src="<c:url value='${productImageUrl}'/>"
                                     alt="<c:out value='${product.artist}'/> — <c:out value='${product.title}'/>"
                                     class="product-gallery-main-img" style="border-radius: 12px; box-shadow: 0 4px 12px rgba(0,0,0,0.1);" />
                            </c:when>
                            <c:otherwise>
                                <img id="productGalleryMain"
                                     src="https://via.placeholder.com/600x600?text=Sin+imagen"
                                     alt="<c:out value='${product.artist}'/> — <c:out value='${product.title}'/>"
                                     class="product-gallery-main-img" style="border-radius: 12px;" />
                            </c:otherwise>
                        </c:choose>
                    </div>
                    <c:if test="${not empty productImages}">
                        <div class="product-gallery-thumbs mt-4" role="group" aria-label="Galeria de imagenes del producto">
                            <c:forEach items="${productImages}" var="img" varStatus="st">
                                <c:url var="galleryImgUrl" value="/images/${img.imageId}" />
                                <button type="button"
                                        class="product-gallery-thumb<c:if test='${st.first}'> is-active</c:if>"
                                        data-full-src="${galleryImgUrl}"
                                        style="border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); transition: transform 0.2s;"
                                        onmouseover="this.style.transform='scale(1.05)'"
                                        onmouseout="this.style.transform='scale(1)'"
                                        aria-label="Ver imagen ${st.index + 1} de ${fn:length(productImages)}"
                                        aria-pressed="${st.first}">
                                    <img src="<c:url value='${galleryImgUrl}'/>" alt="" loading="lazy" style="border-radius: 8px;" />
                                </button>
                            </c:forEach>
                        </div>
                    </c:if>
                </div>
            </div>

            <!-- Right Side: Details -->
            <div class="col-lg-6">
                <!-- Main Info Card -->
                <div style="background: #ffffff; border-radius: 24px; padding: 2.5rem; box-shadow: 0 10px 40px rgba(0,0,0,0.04); border: 1px solid rgba(0,0,0,0.03); position: relative; overflow: hidden; margin-bottom: 2rem;">
                    <!-- Decorative accent top -->
                    <div style="position: absolute; top: 0; left: 0; right: 0; height: 6px; background: linear-gradient(90deg, var(--color-accent) 0%, #ff9f43 100%);"></div>

                    <h1 style="font-family: var(--font-heading); font-size: 2.5rem; font-weight: 700; margin-bottom: 0.25rem; color: var(--color-text-main); line-height: 1.1;"><c:out value="${product.title}"/></h1>
                    <h2 style="font-size: 1.25rem; font-weight: 500; color: var(--color-text-muted); margin-bottom: 0.75rem; display: flex; align-items: center; gap: 0.5rem;">
                        <i class="bi bi-mic" style="color: var(--color-accent); opacity: 0.7;"></i> <c:out value="${product.artist}"/>
                    </h2>

                    <c:if test="${seller != null}">
                        <div style="display: flex; align-items: center; gap: 0.75rem; margin-bottom: 2rem; padding: 0.75rem 1rem; background: #fcfaf8; border-radius: 12px; border: 1px solid rgba(0,0,0,0.04);">
                            <div style="width: 32px; height: 32px; background: var(--color-accent); color: #fff; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-weight: 700; font-size: 0.8rem; flex-shrink: 0;">
                                <c:out value="${fn:substring(seller.username, 0, 1)}"/>
                            </div>
                            <div style="flex: 1;">
                                <a href="<c:url value='/profile?userId=${seller.id}'/>" style="font-weight: 600; color: var(--color-text-main); text-decoration: none; font-size: 0.95rem;" onmouseover="this.style.color='var(--color-accent)';" onmouseout="this.style.color='var(--color-text-main)';">
                                    <c:out value="${seller.username}"/>
                                </a>
                            </div>
                            <c:if test="${sellerRating.count > 0}">
                                <a href="<c:url value='/profile?userId=${seller.id}'/>" style="display: flex; align-items: center; gap: 0.35rem; text-decoration: none; color: var(--color-accent); font-weight: 600; font-size: 0.9rem;">
                                    <i class="bi bi-star-fill"></i>
                                    <c:out value="${sellerRating.formattedAvg}"/>
                                    <span style="color: var(--color-text-muted); font-weight: 400;">(<c:out value="${sellerRating.count}"/>)</span>
                                </a>
                            </c:if>
                        </div>
                    </c:if>

                    <c:if test="${not empty product.recordLabel or not empty product.catalogNumber or not empty product.editionCountry}">
                        <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: 1rem; margin-bottom: 2.5rem; background: #fcfaf8; padding: 1.5rem; border-radius: 16px; border: 1px dashed rgba(231,111,81,0.2);">
                            <c:if test="${not empty product.recordLabel or not empty product.catalogNumber}">
                                <div>
                                    <span style="display: block; font-size: 0.75rem; text-transform: uppercase; letter-spacing: 0.05em; color: var(--color-text-muted); margin-bottom: 0.2rem; font-weight: 700;">Sello - Nro. de catálogo</span>
                                    <span style="font-weight: 600; color: var(--color-text-main); font-size: 0.95rem;">
                                        <c:choose>
                                            <c:when test="${not empty product.recordLabel and not empty product.catalogNumber}">
                                                <c:out value="${product.recordLabel}" /> - <c:out value="${product.catalogNumber}" />
                                            </c:when>
                                            <c:when test="${not empty product.recordLabel}">
                                                <c:out value="${product.recordLabel}" />
                                            </c:when>
                                            <c:otherwise>
                                                <c:out value="${product.catalogNumber}" />
                                            </c:otherwise>
                                        </c:choose>
                                    </span>
                                </div>
                            </c:if>
                            <c:if test="${not empty product.editionCountry}">
                                <div>
                                    <span style="display: block; font-size: 0.75rem; text-transform: uppercase; letter-spacing: 0.05em; color: var(--color-text-muted); margin-bottom: 0.2rem; font-weight: 700;">Origen</span>
                                    <span style="font-weight: 600; color: var(--color-text-main); font-size: 0.95rem;"><c:out value="${product.editionCountry}" /></span>
                                </div>
                            </c:if>
                        </div>
                    </c:if>

                    <div style="margin-bottom: 2rem; display: flex; align-items: flex-end; gap: 0.5rem;">
                        <span style="font-family: var(--font-mono); font-size: 3rem; font-weight: 700; color: var(--color-accent); line-height: 1; letter-spacing: -1px;">$<c:out value='${product.price}'/></span>
                        <span style="font-size: 1rem; color: var(--color-text-muted); font-weight: 600; padding-bottom: 0.4rem;">ARS</span>
                    </div>

                    <div class="d-grid">
                        <c:url var="purchasePostUrl" value='/purchases'/>
                        <form:form modelAttribute="purchaseCreateForm" action="${purchasePostUrl}" method="POST" cssClass="w-100">
                            <input type="hidden" name="productId" value="${product.id}" />
                            <button type="submit" class="btn w-100" style="background: var(--color-accent); color: #fff; font-size: 1.15rem; padding: 1.1rem; border-radius: 99px; box-shadow: 0 8px 24px rgba(231, 111, 81, 0.4); border: none; transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1); display: flex; justify-content: center; align-items: center; gap: 0.75rem; font-weight: 700;" onmouseover="this.style.transform='translateY(-3px)'; this.style.boxShadow='0 12px 28px rgba(231, 111, 81, 0.5)'; this.style.background='var(--color-accent-hover)';" onmouseout="this.style.transform='none'; this.style.boxShadow='0 8px 24px rgba(231, 111, 81, 0.4)'; this.style.background='var(--color-accent)';">
                                <i class="bi bi-cart-fill" aria-hidden="true" style="font-size: 1.3rem;"></i> Iniciar Compra
                            </button>
                        </form:form>
                    </div>
                    <sec:authorize access="isAuthenticated()">
                        <div class="d-grid mt-3">
                            <c:url var="reportPostUrl" value="/products/${product.id}/report"/>
                            <form action="${reportPostUrl}" method="POST" class="w-100">
                                <button type="submit" class="btn w-100" style="background: transparent; color: var(--color-accent); font-size: 1rem; padding: 0.8rem; border-radius: 99px; border: 1px solid rgba(231, 111, 81, 0.5); transition: all 0.2s ease; display: flex; justify-content: center; align-items: center; gap: 0.5rem; font-weight: 600;" onmouseover="this.style.background='rgba(231, 111, 81, 0.08)'; this.style.borderColor='var(--color-accent)';" onmouseout="this.style.background='transparent'; this.style.borderColor='rgba(231, 111, 81, 0.5)';">
                                    <i class="bi bi-flag" aria-hidden="true"></i> Reportar publicación
                                </button>
                            </form>
                        </div>
                    </sec:authorize>
                </div> <!-- End Main Info Card -->

                <!-- Details & Description Card -->
                <div style="background: rgba(255, 255, 255, 0.5); backdrop-filter: blur(12px); border-radius: 20px; padding: 2rem; border: 1px solid rgba(255,255,255,0.8); box-shadow: 0 8px 32px rgba(0,0,0,0.03);">
                    <h5 style="font-family: var(--font-heading); font-weight: 700; margin-bottom: 1.5rem; font-size: 1.3rem; color: var(--color-text-main); display: flex; align-items: center; gap: 0.5rem; border-bottom: 1px solid rgba(0,0,0,0.05); padding-bottom: 1rem;">
                        <i class="bi bi-music-note-list" style="color: var(--color-accent);"></i> Más detalles
                    </h5>
                    
                    <div style="margin-bottom: 1.5rem;">
                        <p style="margin-bottom: 0; line-height: 1.8; font-size: 0.98rem; color: var(--color-text-main);">
                            <c:out value="${product.description}" />
                        </p>
                    </div>
                    
                    <c:if test="${not empty product.categories}">
                        <div style="margin-bottom: 2rem; display: flex; flex-wrap: wrap; gap: 0.6rem; align-items: center;">
                            <c:forEach items="${product.categories}" var="cat" varStatus="status">
                                <span style="background: #fff; color: var(--color-accent); font-weight: 600; font-size: 0.8rem; padding: 0.4rem 1rem; border-radius: 50px; border: 1.5px solid rgba(231, 111, 81, 0.2); transition: all 0.25s; box-shadow: 0 2px 8px rgba(231,111,81,0.05);" onmouseover="this.style.background='var(--color-accent)'; this.style.color='#fff'; this.style.transform='translateY(-2px)';" onmouseout="this.style.background='#fff'; this.style.color='var(--color-accent)'; this.style.transform='none';"><c:out value="${cat.name}" /></span>
                            </c:forEach>
                        </div>
                    </c:if>

                    <div class="row g-3 mt-2 mb-4">
                        <div class="col-6">
                            <div style="background: #fff; padding: 1.25rem 1rem; border-radius: 16px; border: 1px solid rgba(0,0,0,0.04); text-align: center; box-shadow: 0 4px 15px rgba(0,0,0,0.03);">
                                <span style="display: block; font-size: 0.72rem; font-weight: 700; letter-spacing: 0.12em; text-transform: uppercase; color: var(--color-text-muted); margin-bottom: 0.5rem;">Estado Disco</span>
                                <span style="font-family: var(--font-mono); font-size: 1.85rem; font-weight: 700; color: var(--color-text-main);"><c:out value="${product.recordCondition}" /></span><span style="font-family: var(--font-mono); font-size: 0.95rem; color: #a0a0a0; font-weight: 600;">/10</span>
                            </div>
                        </div>
                        <div class="col-6">
                            <div style="background: #fff; padding: 1.25rem 1rem; border-radius: 16px; border: 1px solid rgba(0,0,0,0.04); text-align: center; box-shadow: 0 4px 15px rgba(0,0,0,0.03);">
                                <span style="display: block; font-size: 0.72rem; font-weight: 700; letter-spacing: 0.12em; text-transform: uppercase; color: var(--color-text-muted); margin-bottom: 0.5rem;">Estado Tapa</span>
                                <span style="font-family: var(--font-mono); font-size: 1.85rem; font-weight: 700; color: var(--color-text-main);"><c:out value="${product.sleeveCondition}" /></span><span style="font-family: var(--font-mono); font-size: 0.95rem; color: #a0a0a0; font-weight: 600;">/10</span>
                            </div>
                        </div>
                    </div>

                    <div style="display: flex; align-items: center; gap: 1rem; background: #fff; padding: 1.25rem; border-radius: 16px; border: 1px solid rgba(0,0,0,0.04); box-shadow: 0 4px 15px rgba(0,0,0,0.03);">
                        <div style="width: 44px; height: 44px; background: rgba(231, 111, 81, 0.1); border-radius: 50%; display: flex; align-items: center; justify-content: center; flex-shrink: 0;">
                            <i class="bi bi-geo-alt-fill" aria-hidden="true" style="color: var(--color-accent); font-size: 1.2rem;"></i> 
                        </div>
                        <div>
                            <span style="display: block; font-size: 0.75rem; font-weight: 700; color: var(--color-text-muted); text-transform: uppercase; letter-spacing: 0.08em; margin-bottom: 0.2rem;">Ubicación</span>
                            <span style="font-size: 1rem; font-weight: 600; color: var(--color-text-main);"><c:out value="${product.location}" /></span>
                        </div>
                    </div>
                </div> <!-- End Details Card -->
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
