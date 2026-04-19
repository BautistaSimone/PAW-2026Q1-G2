<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<ui:layout title="Vinyland | Perfil">

    <ui:header showHeaderActions="true"/>

    <div class="profile-page">
        <div class="container py-4">
            <div class="profile-user-card d-flex justify-content-between">
                <div class="d-flex justify-content-between profile-data">
                    <div class="profile-avatar">
                        <c:out value="${fn:substring(user.username, 0, 1)}" />
                    </div>
                    <div class="profile-user-info">
                        <h1><c:out value="${user.username}" /></h1>
                        <c:if test="${isOwnProfile}">
                            <h2><c:out value="${user.email}" /></h2>
                        </c:if>
                        <c:if test="${sellerRating.count > 0}">
                            <div class="profile-rating-row" aria-label="Valoración como vendedor">
                                <span class="profile-rating-stars" aria-hidden="true">
                                    <c:forEach begin="1" end="5" var="i">
                                        <c:choose>
                                            <c:when test="${i <= sellerRating.avgScore}"><i class="bi bi-star-fill"></i></c:when>
                                            <c:when test="${i - 0.5 <= sellerRating.avgScore}"><i class="bi bi-star-half"></i></c:when>
                                            <c:otherwise><i class="bi bi-star"></i></c:otherwise>
                                        </c:choose>
                                    </c:forEach>
                                </span>
                                <span class="profile-rating-caption">
                                    <c:out value="${sellerRating.formattedAvg}"/> (<c:out value="${sellerRating.count}"/> reseña<c:if test="${sellerRating.count != 1}">s</c:if>)
                                </span>
                            </div>
                        </c:if>
                    </div>
                </div>

                <c:if test="${isOwnProfile}">
                    <form action="<c:url value='/logout' />" method="post" style="margin-top: 1rem;">
                        <button type="submit" class="btn btn-retro btn-retro-secondary">
                            <i class="bi bi-box-arrow-right" aria-hidden="true"></i> Cerrar sesión
                        </button>
                    </form>
                </c:if>
            </div>

            <!-- Tabs -->
            <ul class="nav nav-tabs mt-4" id="profileTabs" role="tablist" style="border-bottom: 2px solid var(--color-border);">
                <li class="nav-item" role="presentation">
                    <button class="nav-link active" id="publications-tab" data-bs-toggle="tab" data-bs-target="#publications" type="button" role="tab" aria-controls="publications" aria-selected="true" style="font-weight: 600;">
                        <i class="bi bi-vinyl" aria-hidden="true"></i> Publicaciones
                    </button>
                </li>
                <c:if test="${isOwnProfile}">
                    <li class="nav-item" role="presentation">
                        <button class="nav-link" id="purchases-tab" data-bs-toggle="tab" data-bs-target="#purchases" type="button" role="tab" aria-controls="purchases" aria-selected="false" style="font-weight: 600;">
                            <i class="bi bi-bag" aria-hidden="true"></i> Mis compras
                        </button>
                    </li>
                    <li class="nav-item" role="presentation">
                        <button class="nav-link" id="sales-tab" data-bs-toggle="tab" data-bs-target="#sales" type="button" role="tab" aria-controls="sales" aria-selected="false" style="font-weight: 600;">
                            <i class="bi bi-shop" aria-hidden="true"></i> Mis ventas
                        </button>
                    </li>
                </c:if>
                <li class="nav-item" role="presentation">
                    <button class="nav-link" id="reviews-tab" data-bs-toggle="tab" data-bs-target="#reviews" type="button" role="tab" aria-controls="reviews" aria-selected="false" style="font-weight: 600;">
                        <i class="bi bi-star" aria-hidden="true"></i> Reseñas recibidas
                    </button>
                </li>
            </ul>

            <div class="tab-content mt-3" id="profileTabContent">
                <!-- Tab: Publicaciones -->
                <div class="tab-pane fade show active" id="publications" role="tabpanel" aria-labelledby="publications-tab">
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
                                                categories="${product.categories}"
                                                href="${productUrl}"/>
                                    </div>
                                </c:forEach>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="empty-products-state">
                                <i class="bi bi-vinyl" style="font-size: 2.5rem; color: var(--color-border);"></i>
                                <p style="color: var(--color-text-muted); font-size: 1rem; margin: 0;">
                                    <c:choose>
                                        <c:when test="${isOwnProfile}">Todavía no publicaste ningún vinilo.</c:when>
                                        <c:otherwise>Este usuario aún no publicó vinilos.</c:otherwise>
                                    </c:choose>
                                </p>
                                <c:if test="${isOwnProfile}">
                                    <a href="<c:url value='/products/new'/>" class="btn btn-retro btn-retro-primary" style="justify-self: center;">
                                        <i class="bi bi-plus-lg" aria-hidden="true"></i> Publicar tu primer vinilo
                                    </a>
                                </c:if>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>

                <!-- Tab: Mis compras (only own profile) -->
                <c:if test="${isOwnProfile}">
                    <div class="tab-pane fade" id="purchases" role="tabpanel" aria-labelledby="purchases-tab">
                        <c:choose>
                            <c:when test="${not empty purchases}">
                                <div class="d-flex flex-column gap-3">
                                    <c:forEach items="${purchases}" var="purchase">
                                        <c:set var="pProduct" value="${purchaseProducts[purchase.purchaseId]}"/>
                                        <div style="background: #fff; border-radius: 16px; padding: 1.25rem; border: 1px solid var(--color-border); box-shadow: 0 2px 8px rgba(0,0,0,0.04); display: flex; align-items: center; gap: 1rem;">
                                            <c:if test="${pProduct != null}">
                                                <img src="<c:url value='/images/product/${pProduct.id}'/>"
                                                     alt="" style="width: 60px; height: 60px; border-radius: 10px; object-fit: cover;"
                                                     onerror="this.src='https://via.placeholder.com/60?text=—';"/>
                                            </c:if>
                                            <div style="flex: 1; min-width: 0;">
                                                <c:if test="${pProduct != null}">
                                                    <div style="font-weight: 600; font-size: 1rem; color: var(--color-text-main); white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">
                                                        <c:out value="${pProduct.title}"/>
                                                    </div>
                                                    <div style="font-size: 0.85rem; color: var(--color-text-muted);">
                                                        <c:out value="${pProduct.artist}"/> · $<c:out value="${pProduct.price}"/>
                                                    </div>
                                                </c:if>
                                                <div style="font-size: 0.8rem; color: var(--color-text-muted); margin-top: 0.2rem;">
                                                    <c:out value="${purchase.date}"/> · <span style="font-weight: 600;">${purchase.status.description}</span>
                                                </div>
                                            </div>
                                            <div style="display: flex; gap: 0.5rem; align-items: center; flex-shrink: 0;">
                                                <a href="<c:url value='/purchases/${purchase.purchaseId}?token=${purchase.buyerToken}'/>"
                                                   class="btn btn-retro btn-retro-secondary" style="font-size: 0.8rem; padding: 0.4rem 0.8rem;">
                                                    <i class="bi bi-eye" aria-hidden="true"></i> Ver
                                                </a>
                                                <c:if test="${purchase.status eq 'DELIVERED' and not purchaseHasReview[purchase.purchaseId]}">
                                                    <a href="<c:url value='/purchases/${purchase.purchaseId}/review?token=${purchase.buyerToken}'/>"
                                                       class="btn btn-retro btn-retro-primary" style="font-size: 0.8rem; padding: 0.4rem 0.8rem;">
                                                        <i class="bi bi-star" aria-hidden="true"></i> Reseñar
                                                    </a>
                                                </c:if>
                                            </div>
                                        </div>
                                    </c:forEach>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="empty-products-state">
                                    <i class="bi bi-bag" style="font-size: 2.5rem; color: var(--color-border);"></i>
                                    <p style="color: var(--color-text-muted); font-size: 1rem; margin: 0;">Aún no realizaste ninguna compra.</p>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </c:if>

                <!-- Tab: Mis ventas (only own profile) -->
                <c:if test="${isOwnProfile}">
                    <div class="tab-pane fade" id="sales" role="tabpanel" aria-labelledby="sales-tab">
                        <c:choose>
                            <c:when test="${not empty sales}">
                                <div class="d-flex flex-column gap-3">
                                    <c:forEach items="${sales}" var="sale">
                                        <c:set var="sProduct" value="${saleProducts[sale.purchaseId]}"/>
                                        <div style="background: #fff; border-radius: 16px; padding: 1.25rem; border: 1px solid var(--color-border); box-shadow: 0 2px 8px rgba(0,0,0,0.04); display: flex; align-items: center; gap: 1rem;">
                                            <c:if test="${sProduct != null}">
                                                <img src="<c:url value='/images/product/${sProduct.id}'/>"
                                                     alt="" style="width: 60px; height: 60px; border-radius: 10px; object-fit: cover;"
                                                     onerror="this.src='https://via.placeholder.com/60?text=—';"/>
                                            </c:if>
                                            <div style="flex: 1; min-width: 0;">
                                                <c:if test="${sProduct != null}">
                                                    <div style="font-weight: 600; font-size: 1rem; color: var(--color-text-main); white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">
                                                        <c:out value="${sProduct.title}"/>
                                                    </div>
                                                    <div style="font-size: 0.85rem; color: var(--color-text-muted);">
                                                        <c:out value="${sProduct.artist}"/> · $<c:out value="${sProduct.price}"/>
                                                    </div>
                                                </c:if>
                                                <div style="font-size: 0.8rem; color: var(--color-text-muted); margin-top: 0.2rem;">
                                                    <c:out value="${sale.date}"/> · <span style="font-weight: 600;">${sale.status.description}</span>
                                                </div>
                                            </div>
                                            <div style="display: flex; gap: 0.5rem; align-items: center; flex-shrink: 0;">
                                                <a href="<c:url value='/purchases/${sale.purchaseId}?token=${sale.sellerToken}'/>"
                                                   class="btn btn-retro btn-retro-secondary" style="font-size: 0.8rem; padding: 0.4rem 0.8rem;">
                                                    <i class="bi bi-eye" aria-hidden="true"></i> Ver
                                                </a>
                                            </div>
                                        </div>
                                    </c:forEach>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="empty-products-state">
                                    <i class="bi bi-shop" style="font-size: 2.5rem; color: var(--color-border);"></i>
                                    <p style="color: var(--color-text-muted); font-size: 1rem; margin: 0;">Aún no realizaste ninguna venta.</p>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </c:if>

                <!-- Tab: Reseñas recibidas -->
                <div class="tab-pane fade" id="reviews" role="tabpanel" aria-labelledby="reviews-tab">
                    <c:choose>
                        <c:when test="${not empty receivedReviews}">
                            <div class="d-flex flex-column gap-3">
                                <c:forEach items="${receivedReviews}" var="rev">
                                    <div style="background: #fff; border-radius: 16px; padding: 1.25rem; border: 1px solid var(--color-border); box-shadow: 0 2px 8px rgba(0,0,0,0.04);">
                                        <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 0.5rem;">
                                            <div style="display: flex; align-items: center; gap: 0.5rem;">
                                                <div style="width: 36px; height: 36px; background: var(--color-accent); color: #fff; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-weight: 700; font-size: 0.9rem;">
                                                    <c:out value="${fn:substring(rev.buyerUsername, 0, 1)}"/>
                                                </div>
                                                <span style="font-weight: 600; color: var(--color-text-main);"><c:out value="${rev.buyerUsername}"/></span>
                                            </div>
                                            <div style="color: var(--color-accent); font-size: 1rem;">
                                                <c:forEach begin="1" end="5" var="i">
                                                    <c:choose>
                                                        <c:when test="${i <= rev.score}"><i class="bi bi-star-fill"></i></c:when>
                                                        <c:otherwise><i class="bi bi-star"></i></c:otherwise>
                                                    </c:choose>
                                                </c:forEach>
                                            </div>
                                        </div>
                                        <c:if test="${not empty rev.text}">
                                            <p style="color: var(--color-text-main); margin: 0; font-size: 0.95rem; line-height: 1.6;">
                                                <c:out value="${rev.text}"/>
                                            </p>
                                        </c:if>
                                    </div>
                                </c:forEach>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="empty-products-state">
                                <i class="bi bi-star" style="font-size: 2.5rem; color: var(--color-border);"></i>
                                <p style="color: var(--color-text-muted); font-size: 1rem; margin: 0;">
                                    <c:choose>
                                        <c:when test="${isOwnProfile}">Aún no recibiste reseñas como vendedor.</c:when>
                                        <c:otherwise>Este vendedor aún no tiene reseñas.</c:otherwise>
                                    </c:choose>
                                </p>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </div>
    </div>
</ui:layout>
