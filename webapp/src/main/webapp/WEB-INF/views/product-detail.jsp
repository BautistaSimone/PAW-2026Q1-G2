<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
        <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
            <%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
                <%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
                    <%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

                        <ui:layout title="Vinyland | ${product.title}">

                            <ui:header showHeaderActions="true" />

                            <div class="container py-4">
                                <c:if test="${param.created eq '1'}">
                                    <div class="alert-retro alert-retro-success mb-3" role="alert">
                                        <i class="bi bi-check-circle" aria-hidden="true"></i>
                                        <spring:message code="Home.alert.productCreated" />
                                    </div>
                                </c:if>
                                <c:if test="${param.updated eq '1'}">
                                    <div class="alert-retro alert-retro-success mb-3" role="alert">
                                        <i class="bi bi-check-circle" aria-hidden="true"></i>
                                        <spring:message code="ProductDetail.alert.updated" />
                                    </div>
                                </c:if>
                                <c:if test="${param.reported eq '1'}">
                                    <div class="alert-retro alert-retro-success mb-3" role="alert">
                                        <i class="bi bi-check-circle" aria-hidden="true"></i>
                                        <spring:message code="ProductDetail.alert.reported" />
                                    </div>
                                </c:if>
                                <c:if test="${param.alreadyReported eq '1'}">
                                    <div class="alert-retro alert-retro-warning mb-3" role="alert">
                                        <i class="bi bi-exclamation-triangle" aria-hidden="true"></i>
                                        <spring:message code="ProductDetail.alert.alreadyReported" />
                                    </div>
                                </c:if>
                                <c:if test="${param.purchaseError eq '1'}">
                                    <div class="alert-retro alert-retro-warning mb-3" role="alert">
                                        <i class="bi bi-exclamation-triangle" aria-hidden="true"></i>
                                        <spring:message code="ProductDetail.alert.purchaseError" />
                                    </div>
                                </c:if>

                                <div class="mb-4">
                                    <c:url var="productDetailBackHref" value="${productDetailBackUrl}" />
                                    <a href="<c:out value='${productDetailBackHref}'/>"
                                        style="display: inline-flex; align-items: center; gap: 0.25rem; color: var(--color-text-muted); font-weight: 500; text-decoration: none; transition: color 0.2s;"
                                        onmouseover="this.style.color='var(--color-accent)';"
                                        onmouseout="this.style.color='var(--color-text-muted)';">
                                        <i class="bi bi-arrow-left product-detail-i-1" aria-hidden="true" ></i>
                                        <span><spring:message code="ProductDetail.back" /></span>
                                    </a>
                                </div>

                                <div class="row g-5">
                                    <!-- Left Side: Image Gallery -->
                                    <div class="col-lg-6 mb-4">
                                        <div id="productDetailGallery" class="product-detail-gallery">
                                            <div class="product-gallery-main product-detail-div-2"
                                                >
                                                <c:choose>
                                                    <c:when test="${not empty productImageUrl}">
                                                        <img id="productGalleryMain"
                                                            src="<c:url value='${productImageUrl}'/>"
                                                            alt="<c:out value='${product.artist}'/> — <c:out value='${product.title}'/>"
                                                            class="product-gallery-main-img"
                                                            style="border-radius: 12px; box-shadow: 0 4px 12px rgba(0,0,0,0.1);" />
                                                    </c:when>
                                                    <c:otherwise>
                                                        <img id="productGalleryMain"
                                                            src="https://via.placeholder.com/600x600?text=Sin+imagen"
                                                            alt="<c:out value='${product.artist}'/> — <c:out value='${product.title}'/>"
                                                            class="product-gallery-main-img"
                                                            style="border-radius: 12px;" />
                                                    </c:otherwise>
                                                </c:choose>
                                            </div>
                                            <c:if test="${not empty productImages}">
                                                <div class="product-gallery-thumbs mt-4" role="group"
                                                    aria-label="<spring:message code='ProductDetail.images.ariaLabel' />">
                                                    <c:forEach items="${productImages}" var="img" varStatus="st">
                                                        <c:url var="galleryImgUrl" value="/images/${img.imageId}" />
                                                        <button type="button"
                                                            class="product-gallery-thumb<c:if test='${st.first}'> is-active</c:if>"
                                                            data-full-src="<c:out value='${galleryImgUrl}'/>"
                                                            style="border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); transition: transform 0.2s;"
                                                            onmouseover="this.style.transform='scale(1.05)'"
                                                            onmouseout="this.style.transform='scale(1)'"
                                                            aria-label="<spring:message code='ProductDetail.images.item.ariaLabel' arguments='${st.index + 1},${fn:length(productImages)}' />"
                                                            aria-pressed="${st.first}">
                                                            <img src="<c:out value='${galleryImgUrl}'/>" alt=""
                                                                loading="lazy" style="border-radius: 8px;" />
                                                        </button>
                                                    </c:forEach>
                                                </div>
                                            </c:if>
                                        </div>


                                    </div>

                                    <!-- Right Side: Details -->
                                    <div class="col-lg-6">
                                        <!-- Main Info Card -->
                                        <div class="product-detail-div-3"
                                            >
                                            <!-- Decorative accent top -->
                                            <div class="product-detail-div-4"
                                                >
                                            </div>

                                            <h1 class="product-detail-h1-5"
                                                >
                                                <c:out value="${product.title}" />
                                            </h1>
                                            <h2 class="product-detail-h2-6"
                                                >
                                                <i class="bi bi-mic product-detail-i-7"
                                                    ></i>
                                                <span class="product-detail-artist-text"><c:out value="${product.artist}" /></span>
                                            </h2>


                                            <c:if
                                                test="${not empty product.recordLabel or not empty product.catalogNumber or not empty product.editionCountry}">
                                                <div class="product-detail-div-8"
                                                    >
                                                    <c:if
                                                        test="${not empty product.recordLabel or not empty product.catalogNumber}">
                                                        <div>
                                                            <span class="product-detail-span-9"
                                                                ><spring:message code="ProductDetail.labelCatalog" /></span>
                                                            <span class="product-detail-span-10"
                                                                >
                                                                <c:choose>
                                                                    <c:when
                                                                        test="${not empty product.recordLabel and not empty product.catalogNumber}">
                                                                        <c:out value="${product.recordLabel}" /> -
                                                                        <c:out value="${product.catalogNumber}" />
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
                                                            <span class="product-detail-span-9"
                                                                ><spring:message code="ProductDetail.origin" /></span>
                                                            <span class="product-detail-span-10"
                                                                >
                                                                <c:out value="${product.editionCountry}" />
                                                            </span>
                                                        </div>
                                                    </c:if>
                                                </div>
                                            </c:if>

                                            <div class="product-detail-div-11"
                                                >
                                                <span class="product-detail-span-12"
                                                    >
                                                    <ui:price value="${product.price}" />
                                                </span>
                                                <span class="product-detail-span-13"
                                                    ><spring:message code="Global.currency.ars"/></span>
                                            </div>

                                            <c:choose>
                                                <c:when test="${isOwnProduct}">
                                                    <div class="alert-retro alert-retro-warning mb-0 product-detail-div-14" role="status" >
                                                        <i class="bi bi-info-circle" aria-hidden="true"></i>
                                                        <spring:message code="ProductDetail.isOwnProduct" />
                                                    </div>
                                                    <div class="d-grid gap-2 mt-3">
                                                        <a href="<c:url value='/products/${product.id}/edit'/>" class="btn btn-retro btn-retro-primary w-100">
                                                            <i class="bi bi-pencil-square" aria-hidden="true"></i> <spring:message code="Profile.publications.editButton" />
                                                        </a>
                                                        <button type="button" class="btn w-100 product-detail-button-15" disabled
                                                            >
                                                            <i class="bi bi-cart-fill product-detail-i-16" aria-hidden="true"
                                                                ></i> <spring:message code="ProductDetail.purchase.button" />
                                                        </button>
                                                    </div>
                                                </c:when>
                                                <c:otherwise>
                                                    <div class="d-grid gap-3">
                                                        <!-- Purchase -->
                                                        <c:url var="purchasePostUrl" value='/purchases' />
                                                        <form:form modelAttribute="purchaseCreateForm"
                                                            action="${purchasePostUrl}"
                                                            method="POST"
                                                            cssClass="w-100"
                                                            data-single-submit="true">

                                                            <input type="hidden"
                                                                name="${_csrf.parameterName}"
                                                                value="${_csrf.token}" />

                                                            <input type="hidden"
                                                                name="productId"
                                                                value="<c:out value='${product.id}'/>'" />

                                                            <button type="submit"
                                                                    class="btn w-100 product-detail-button-17">

                                                                <i class="bi bi-cart-fill product-detail-i-16"
                                                                aria-hidden="true"></i>

                                                                <spring:message code="ProductDetail.purchase.button" />
                                                            </button>
                                                        </form:form>

                                                        <!-- Wishlist -->
                                                        <c:url var="wishlistUrl" value='/add-wishlist-product' />

                                                        <form:form action="${wishlistUrl}"
                                                                method="POST"
                                                                cssClass="w-100">

                                                            <input type="hidden"
                                                                name="${_csrf.parameterName}"
                                                                value="${_csrf.token}" />

                                                            <input type="hidden"
                                                                name="productId"
                                                                value="<c:out value='${product.id}'/>" />

                                                            <button type="submit"
                                                                    class="btn w-100 product-detail-button-18">

                                                                <i class="bi ${isWishlisted ? 'bi-heart-fill' : 'bi-heart'}"></i>

                                                                <c:choose>
                                                                    <c:when test="${isWishlisted}">
                                                                        Remove from wishlist
                                                                    </c:when>
                                                                    <c:otherwise>
                                                                        Add to wishlist
                                                                    </c:otherwise>
                                                                </c:choose>
                                                            </button>
                                                        </form:form>

                                                    </div>
                                                    <sec:authorize access="isAuthenticated()">
                                                        <div class="d-grid mt-3">
                                                            <c:url var="reportPostUrl" value="/products/${product.id}/report" />
                                                            <form action="<c:out value='${reportPostUrl}'/>" method="POST" class="w-100">
                                                                <input type="hidden" name="<c:out value='${_csrf.parameterName}'/>" value="<c:out value='${_csrf.token}'/>" />
                                                                <button type="submit" class="btn w-100 product-detail-button-18"
                                                                    
                                                                    onmouseover="this.style.background='rgba(231, 111, 81, 0.08)'; this.style.borderColor='var(--color-accent)';"
                                                                    onmouseout="this.style.background='transparent'; this.style.borderColor='rgba(231, 111, 81, 0.5)';">
                                                                    <i class="bi bi-flag" aria-hidden="true"></i> <spring:message code="ProductDetail.report.button" />
                                                                </button>
                                                            </form>
                                                        </div>
                                                    </sec:authorize>
                                                </c:otherwise>
                                            </c:choose>
                                        </div> <!-- End Main Info Card -->

                                    </div>
                                </div>

                                <div class="row g-5 mt-2">
                                    <div class="col-lg-6 mb-4">
                                        <!-- Seller Information Card -->
                                        <c:if test="${seller != null}">
                                            <div class="product-detail-div-19" >
                                                <h5 class="product-detail-h5-20" >
                                                    <i class="bi bi-person-badge product-detail-i-21" ></i> <spring:message code="ProductDetail.seller.title" />
                                                </h5>

                                                <div class="product-detail-div-22" >
                                                    <div class="product-detail-div-23" >
                                                        <c:out value="${fn:substring(seller.username, 0, 1)}" />
                                                    </div>
                                                    <div>
                                                        <a href="<c:url value='/profile?userId=${seller.id}'/>"
                                                            style="font-weight: 700; color: var(--color-text-main); text-decoration: none; font-size: 1.1rem; display: block; margin-bottom: 0.2rem;"
                                                            onmouseover="this.style.color='var(--color-accent)';"
                                                            onmouseout="this.style.color='var(--color-text-main)';">
                                                            <c:out value="${seller.username}" />
                                                        </a>
                                                        <a href="<c:url value='/profile?userId=${seller.id}'/>"
                                                            style="text-decoration: none; color: inherit; display: inline-block;">
                                                            <ui:sellerRatingStars summary="${sellerRating}" compact="false"/>
                                                        </a>
                                                    </div>
                                                </div>

                                                <div class="product-detail-div-24" >
                                                    <div class="product-detail-div-25" >
                                                        <i class="bi bi-geo-alt-fill product-detail-i-26" aria-hidden="true" ></i>
                                                    </div>
                                                    <div>
                                                        <span class="product-detail-span-27" ><spring:message code="ProductDetail.location" /></span>
                                                        <span class="product-detail-span-28" >
                                                            <c:out value="${seller.location}" />
                                                        </span>
                                                    </div>
                                                </div>

                                                <div class="product-detail-div-29" >
                                                    <c:choose>
                                                        <c:when test="${not empty sellerReviews}">
                                                            <div class="product-detail-div-30" >
                                                                <c:forEach items="${sellerReviews}" var="review" varStatus="st">
                                                                    <div style="padding-bottom: ${st.last ? '0' : '1rem'}; border-bottom: ${st.last ? 'none' : '1px solid rgba(0,0,0,0.05)'};">
                                                                        <div class="product-detail-div-31" >
                                                                            <c:forEach begin="1" end="${review.score}">
                                                                                <i class="bi bi-star-fill"></i>
                                                                            </c:forEach>
                                                                            <c:forEach begin="${review.score + 1}" end="5">
                                                                                <i class="bi bi-star product-detail-i-32" ></i>
                                                                            </c:forEach>
                                                                        </div>
                                                                        <c:if test="${not empty review.text}">
                                                                            <p class="product-detail-p-33" >"<c:out value="${review.text}" />"</p>
                                                                        </c:if>
                                                                    </div>
                                                                </c:forEach>
                                                            </div>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <p class="product-detail-p-34" ><spring:message code="ProductDetail.seller.reviews.empty" /></p>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </div>
                                            </div>
                                        </c:if>
                                    </div>
                                    <div class="col-lg-6 mb-4">
                                        <!-- Details & Description Card -->
                                        <div class="product-detail-div-19"
                                            >
                                            <h5 class="product-detail-h5-20"
                                                >
                                                <i class="bi bi-music-note-list product-detail-i-21"
                                                    ></i> <spring:message code="ProductDetail.details.title" />
                                            </h5>

                                            <div class="product-detail-div-35" >
                                                <p class="product-detail-p-36"
                                                    >
                                                    <c:out value="${product.description}" />
                                                </p>
                                            </div>

                                            <c:if test="${not empty product.categories}">
                                                <div class="product-detail-div-37"
                                                    >
                                                    <c:forEach items="${product.categories}" var="cat"
                                                        varStatus="status">
                                                        <c:url var="catFilterUrl" value="/">
                                                            <c:param name="categories" value="${cat.id}" />
                                                        </c:url>
                                                        <a href="<c:out value='${catFilterUrl}'/>"
                                                            style="background: #fff; color: var(--color-accent); font-weight: 600; font-size: 0.8rem; padding: 0.4rem 1rem; border-radius: 50px; border: 1.5px solid rgba(231, 111, 81, 0.2); transition: all 0.25s; box-shadow: 0 2px 8px rgba(231,111,81,0.05); text-decoration: none;"
                                                            onmouseover="this.style.background='var(--color-accent)'; this.style.color='#fff'; this.style.transform='translateY(-2px)';"
                                                            onmouseout="this.style.background='#fff'; this.style.color='var(--color-accent)'; this.style.transform='none';">
                                                            <c:out value="${cat.name}" />
                                                        </a>
                                                    </c:forEach>
                                                </div>
                                            </c:if>

                                            <div class="row g-3 mt-2 mb-4">
                                                <div class="col-6">
                                                    <div class="product-detail-div-38"
                                                        >
                                                        <span class="product-detail-span-39"
                                                            ><spring:message code="ProductDetail.recordCondition" /></span>
                                                        <span class="product-detail-span-40"
                                                            >
                                                            <c:out value="${product.recordConditionDisplay}" />
                                                        </span><span class="product-detail-span-41"
                                                            >/10</span>
                                                    </div>
                                                </div>
                                                <div class="col-6">
                                                    <div class="product-detail-div-38"
                                                        >
                                                        <span class="product-detail-span-39"
                                                            ><spring:message code="ProductDetail.sleeveCondition" /></span>
                                                        <span class="product-detail-span-40"
                                                            >
                                                            <c:out value="${product.sleeveConditionDisplay}" />
                                                        </span><span class="product-detail-span-41"
                                                            >/10</span>
                                                    </div>
                                                </div>
                                            </div>
                                        </div> <!-- End Details Card -->
                                    </div>
                                </div>

                                <!-- Carousels: Más de y También te podría interesar -->
                                <div class="mt-5">
                                <div class="recommendations-wrapper">
                                    <div class="mb-5">
                                        <h4 class="recommendations-title">
                                            <spring:message code="ProductDetail.moreFrom" arguments="${seller.username}" />
                                        </h4>
                                        <c:choose>
                                            <c:when test="${not empty sellerProducts}">
                                                <div id="sellerCarousel" class="recommendations-carousel">
                                                    <div class="carousel-fade-edge left"></div>
                                                    <div class="carousel-fade-edge right"></div>

                                                    <button class="carousel-control-btn prev" aria-label="<spring:message code='ProductDetail.carousel.prev' />">
                                                        <i class="bi bi-chevron-left"></i>
                                                    </button>

                                                    <div class="carousel-track">
                                                    <c:forEach items="${sellerProducts}" var="sp">
                                                            <div class="carousel-item-wrapper">
                                                                <c:url value="/products/${sp.id}" var="spUrl" />
                                                                <ui:productCard title="${sp.title}"
                                                                    artist="${sp.artist}" price="${sp.price}"
                                                                    installments="${sp.installmentPrice}"
                                                                    imageUrl="/images/product/${sp.id}"
                                                                    categories="${sp.categories}"
                                                                    sellerRating="${sellerRatings[sp.userId]}"
                                                                    href="${spUrl}" />
                                                            </div>
                                                        </c:forEach>
                                                    </div>

                                                    <button class="carousel-control-btn next" aria-label="<spring:message code='ProductDetail.carousel.next' />">
                                                        <i class="bi bi-chevron-right"></i>
                                                    </button>
                                                </div>
                                            </c:when>
                                            <c:otherwise>
                                                <div class="product-detail-div-42"
                                                    >
                                                    <p class="product-detail-p-43"
                                                        >
                                                        <spring:message code="ProductDetail.moreFrom.empty" />
                                                    </p>
                                                </div>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>

                                    <div class="mb-5">
                                        <h4 class="recommendations-title"><spring:message code="ProductDetail.related.title" /></h4>
                                        <c:choose>
                                            <c:when test="${not empty relatedProducts}">
                                                <div id="relatedCarousel" class="recommendations-carousel">
                                                    <div class="carousel-fade-edge left"></div>
                                                    <div class="carousel-fade-edge right"></div>

                                                    <button class="carousel-control-btn prev" aria-label="<spring:message code='ProductDetail.carousel.prev' />">
                                                        <i class="bi bi-chevron-left"></i>
                                                    </button>

                                                    <div class="carousel-track">
                                                        <c:forEach items="${relatedProducts}" var="rp">
                                                            <div class="carousel-item-wrapper">
                                                                <c:url value="/products/${rp.id}" var="rpUrl" />
                                                                <ui:productCard title="${rp.title}"
                                                                    artist="${rp.artist}" price="${rp.price}"
                                                                    installments="${rp.installmentPrice}"
                                                                    imageUrl="/images/product/${rp.id}"
                                                                    categories="${rp.categories}"
                                                                    sellerRating="${sellerRatings[rp.userId]}"
                                                                    href="${rpUrl}" />
                                                            </div>
                                                        </c:forEach>
                                                    </div>

                                                    <button class="carousel-control-btn next" aria-label="<spring:message code='ProductDetail.carousel.next' />">
                                                        <i class="bi bi-chevron-right"></i>
                                                    </button>
                                                </div>
                                            </c:when>
                                            <c:otherwise>
                                                <div class="product-detail-div-42"
                                                    >
                                                    <p class="product-detail-p-43"
                                                        >
                                                        <spring:message code="ProductDetail.related.empty" />
                                                    </p>
                                                </div>
                                            </c:otherwise>
                                        </c:choose>
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

                            <script>
                                (function () {
                                    function initCarousel(id) {
                                        const carousel = document.getElementById(id);
                                        if (!carousel) return;

                                        const track = carousel.querySelector('.carousel-track');
                                        const prevBtn = carousel.querySelector('.carousel-control-btn.prev');
                                        const nextBtn = carousel.querySelector('.carousel-control-btn.next');
                                        const fadeLeft = carousel.querySelector('.carousel-fade-edge.left');
                                        const fadeRight = carousel.querySelector('.carousel-fade-edge.right');

                                        if (!track || !prevBtn || !nextBtn) return;

                                        const updateControls = () => {
                                            const scrollLeft = track.scrollLeft;
                                            const maxScrollLeft = track.scrollWidth - track.clientWidth;

                                            if (prevBtn) {
                                                prevBtn.style.opacity = scrollLeft > 10 ? '1' : '0';
                                                prevBtn.style.pointerEvents = scrollLeft > 10 ? 'auto' : 'none';
                                            }

                                            if (nextBtn) {
                                                nextBtn.style.opacity = scrollLeft < maxScrollLeft - 10 ? '1' : '0';
                                                nextBtn.style.pointerEvents = scrollLeft < maxScrollLeft - 10 ? 'auto' : 'none';
                                            }

                                            if (fadeLeft) fadeLeft.style.opacity = scrollLeft > 20 ? '1' : '0';
                                            if (fadeRight) fadeRight.style.opacity = scrollLeft < maxScrollLeft - 20 ? '1' : '0';
                                        };

                                        prevBtn.addEventListener('click', () => {
                                            track.scrollBy({ left: -(track.clientWidth * 0.8), behavior: 'smooth' });
                                        });

                                        nextBtn.addEventListener('click', () => {
                                            track.scrollBy({ left: (track.clientWidth * 0.8), behavior: 'smooth' });
                                        });

                                        track.addEventListener('scroll', updateControls);
                                        window.addEventListener('resize', updateControls);

                                        // Initial check
                                        setTimeout(updateControls, 150);
                                    }

                                    initCarousel('sellerCarousel');
                                    initCarousel('relatedCarousel');
                                })();
                            </script>
                        </ui:layout>
