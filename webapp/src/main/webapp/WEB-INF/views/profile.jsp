<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<c:set var="activeMyData" value="${isOwnProfile and param.tab eq 'mydata'}"/>

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
                        <c:if test="${not empty user.firstName or not empty user.lastName}">
                            <p style="margin: 0.15rem 0 0.35rem; color: var(--color-text-muted); font-size: 1rem; font-weight: 500;">
                                <c:out value="${user.firstName}"/> <c:out value="${user.lastName}"/>
                            </p>
                        </c:if>
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
                    <div>
                        <a href="<c:url value='/resetPassword'/>" class="btn btn-retro btn-retro-secondary" role="button">
                            Cambiar contraseña
                        </a>
                        <form action="<c:url value='/logout' />" method="post" style="margin-top: 1rem;">
                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
                            <button type="submit" class="btn btn-retro btn-retro-secondary">
                                <i class="bi bi-box-arrow-right" aria-hidden="true"></i> Cerrar sesión
                            </button>
                        </form>
                    </div>
                </c:if>
            </div>

            <c:if test="${isOwnProfile}">
                <c:if test="${param.updated eq '1'}">
                    <div class="alert-retro alert-retro-success mt-3" role="alert">
                        <i class="bi bi-check-circle" aria-hidden="true"></i> Tus datos se guardaron correctamente.
                    </div>
                </c:if>
                <c:if test="${param.missingData eq 'purchase'}">
                    <div class="alert-retro alert-retro-warning mt-3" role="alert">
                        <i class="bi bi-exclamation-triangle" aria-hidden="true"></i>
                        Para comprar necesitás completar tu nombre, apellido y dirección de envío (calle, número, barrio y provincia).
                        <c:if test="${not empty param.productId}">
                            <a href="<c:url value='/products/${param.productId}'/>" class="alert-link" style="margin-left: 0.5rem;">Volver al producto</a>
                        </c:if>
                    </div>
                </c:if>
                <c:if test="${param.missingData eq 'publish'}">
                    <div class="alert-retro alert-retro-warning mt-3" role="alert">
                        <i class="bi bi-exclamation-triangle" aria-hidden="true"></i>
                        Para publicar un vinilo necesitás cargar tu CBU/CVU (22 dígitos) y completar barrio y provincia en Mis datos (se usan como ubicación de tus publicaciones).
                    </div>
                </c:if>
                <c:if test="${param.deleted eq '1'}">
                    <div class="alert-retro alert-retro-success mt-3" role="alert">
                        <i class="bi bi-check-circle" aria-hidden="true"></i>
                        La publicación se eliminó correctamente.
                    </div>
                </c:if>
                <c:if test="${param.deleteError eq 'forbidden'}">
                    <div class="alert-retro alert-retro-warning mt-3" role="alert">
                        <i class="bi bi-exclamation-triangle" aria-hidden="true"></i>
                        No podés eliminar una publicación que no es tuya.
                    </div>
                </c:if>
            </c:if>

            <!-- Tabs -->
            <ul class="nav nav-tabs mt-4" id="profileTabs" role="tablist" style="border-bottom: 2px solid var(--color-border);">
                <li class="nav-item" role="presentation">
                    <button class="nav-link<c:if test='${not activeMyData}'> active</c:if>" id="publications-tab" data-bs-toggle="tab" data-bs-target="#publications" type="button" role="tab" aria-controls="publications" aria-selected="${not activeMyData}" style="font-weight: 600;">
                        <i class="bi bi-vinyl" aria-hidden="true"></i> Publicaciones
                    </button>
                </li>
                <c:if test="${isOwnProfile}">
                    <li class="nav-item" role="presentation">
                        <button class="nav-link<c:if test='${activeMyData}'> active</c:if>" id="mydata-tab" data-bs-toggle="tab" data-bs-target="#mydata" type="button" role="tab" aria-controls="mydata" aria-selected="${activeMyData}" style="font-weight: 600;">
                            <i class="bi bi-person-lines-fill" aria-hidden="true"></i> Mis datos
                        </button>
                    </li>
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
                <div class="tab-pane fade<c:if test='${not activeMyData}'> show active</c:if>" id="publications" role="tabpanel" aria-labelledby="publications-tab">
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
                                                sellerRating="${sellerRating}"
                                                href="${productUrl}"/>
                                        <c:if test="${isOwnProfile}">
                                            <c:url var="deleteProductUrl" value="/products/${product.id}/delete"/>
                                            <form action="${deleteProductUrl}" method="post" class="mt-2" onsubmit="return confirm('¿Estás seguro de que querés eliminar esta publicación?');">
                                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
                                                <button type="submit" class="btn btn-retro btn-retro-secondary w-100" style="font-size: 0.85rem; padding: 0.4rem 0.75rem;">
                                                    <i class="bi bi-trash" aria-hidden="true"></i> Eliminar publicación
                                                </button>
                                            </form>
                                        </c:if>
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

                <!-- Tab: Mis datos (solo perfil propio) -->
                <c:if test="${isOwnProfile}">
                    <div class="tab-pane fade<c:if test='${activeMyData}'> show active</c:if>" id="mydata" role="tabpanel" aria-labelledby="mydata-tab">
                        <div style="background: #fff; border-radius: 16px; padding: 1.5rem 1.25rem; border: 1px solid var(--color-border); max-width: 640px;">
                            <p style="color: var(--color-text-muted); font-size: 0.95rem; margin-bottom: 1.25rem;">
                                Nombre y apellido son obligatorios. El resto es opcional; podés dejarlo vacío si preferís completarlo más tarde.
                            </p>
                            <c:url var="profileUpdateUrl" value="/profile/update"/>
                            <form:form modelAttribute="userProfileForm" action="${profileUpdateUrl}" method="post" cssClass="user-profile-form" id="profileForm">
                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
                                <div class="row g-2">
                                    <div class="col-md-6 mb-3">
                                        <label class="form-label" for="pfFirstName">Nombre <span class="text-danger">*</span></label>
                                        <form:input path="firstName" id="pfFirstName" cssClass="form-control" autocomplete="given-name" />
                                        <form:errors path="firstName" cssClass="text-danger small d-block"/>
                                    </div>
                                    <div class="col-md-6 mb-3">
                                        <label class="form-label" for="pfLastName">Apellido <span class="text-danger">*</span></label>
                                        <form:input path="lastName" id="pfLastName" cssClass="form-control" autocomplete="family-name" />
                                        <form:errors path="lastName" cssClass="text-danger small d-block"/>
                                    </div>
                                </div>
                                <div class="mb-3">
                                    <label class="form-label" for="pfStreet">Calle</label>
                                    <form:input path="streetName" id="pfStreet" cssClass="form-control" placeholder="Opcional"/>
                                    <form:errors path="streetName" cssClass="text-danger small d-block"/>
                                </div>
                                <div class="mb-3">
                                    <label class="form-label" for="pfStreetNum">Número</label>
                                    <form:input type="number" path="streetNumber" id="pfStreetNum" cssClass="form-control" placeholder="Opcional" min="1"/>
                                    <form:errors path="streetNumber" cssClass="text-danger small d-block"/>
                                </div>
                                <div class="row g-2">
                                    <div class="col-md-6 mb-3">
                                        <label class="form-label" for="pfNeighborhood">Barrio</label>
                                        <form:input path="neighborhood" id="pfNeighborhood" cssClass="form-control" placeholder="Opcional"/>
                                        <form:errors path="neighborhood" cssClass="text-danger small d-block"/>
                                    </div>
                                    <div class="col-md-6 mb-3">
                                        <label class="form-label" for="pfProvince">Provincia</label>
                                        <form:input path="province" id="pfProvince" cssClass="form-control" placeholder="Opcional"/>
                                        <form:errors path="province" cssClass="text-danger small d-block"/>
                                    </div>
                                </div>
                                <div class="mb-3">
                                    <label class="form-label" for="pfExtra">Comentario (edificio, piso, etc.)</label>
                                    <form:input path="extraAddressInfo" id="pfExtra" cssClass="form-control" placeholder="Opcional"/>
                                    <form:errors path="extraAddressInfo" cssClass="text-danger small d-block"/>
                                </div>
                                <div class="mb-3">
                                    <label class="form-label" for="pfCbu">CBU/CVU (22 dígitos)</label>
                                    <form:input path="cbuCvu" id="pfCbu" cssClass="form-control" placeholder="Opcional" inputmode="numeric" maxlength="22"/>
                                    <form:errors path="cbuCvu" cssClass="text-danger small d-block"/>
                                </div>
                                <button type="submit" class="btn btn-retro btn-retro-primary" id="profileSaveBtn" disabled="true">
                                    <i class="bi bi-save" aria-hidden="true"></i> Guardar cambios
                                </button>
                            </form:form>
                        </div>
                    </div>
                </c:if>

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
                                                        <c:out value="${pProduct.artist}"/> · <ui:price value="${pProduct.price}" />
                                                    </div>
                                                </c:if>
                                                <div style="font-size: 0.8rem; color: var(--color-text-muted); margin-top: 0.2rem;">
                                                    <c:out value="${purchase.date}"/> · <span style="font-weight: 600;"><c:out value="${purchase.status.description}"/></span>
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
                                                        <c:out value="${sProduct.artist}"/> · <ui:price value="${sProduct.price}" />
                                                    </div>
                                                </c:if>
                                                <div style="font-size: 0.8rem; color: var(--color-text-muted); margin-top: 0.2rem;">
                                                    <c:out value="${sale.date}"/> · <span style="font-weight: 600;"><c:out value="${sale.status.description}"/></span>
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
    <script>
    (function () {
        var form = document.getElementById('profileForm');
        var saveBtn = document.getElementById('profileSaveBtn');
        if (!form || !saveBtn) return;

        function getSerializedState() {
            var formData = new FormData(form);
            var params = new URLSearchParams();
            Array.from(formData.entries()).sort().forEach(function(pair) {
                params.append(pair[0], pair[1]);
            });
            return params.toString();
        }

        var initialState = getSerializedState();

        function checkChanges() {
            var currentState = getSerializedState();
            if (currentState !== initialState) {
                saveBtn.disabled = false;
            } else {
                saveBtn.disabled = true;
            }
        }

        form.addEventListener('change', checkChanges);
        form.addEventListener('input', function(e) {
            if (e.target.tagName === 'INPUT' || e.target.tagName === 'TEXTAREA' || e.target.tagName === 'SELECT') {
                checkChanges();
            }
        });
    })();
    </script>
</ui:layout>
