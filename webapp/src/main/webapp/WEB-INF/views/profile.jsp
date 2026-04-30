<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<c:set var="activeMyData" value="${isOwnProfile and param.tab eq 'mydata'}"/>
<c:set var="activePurchases" value="${isOwnProfile and param.tab eq 'purchases'}"/>
<c:set var="activeSales" value="${isOwnProfile and param.tab eq 'sales'}"/>
<c:set var="activeReviews" value="${param.tab eq 'reviews'}"/>
<sec:authorize access="hasRole('ADMIN')" var="isAdmin"/>
<c:set var="activeReports" value="${isOwnProfile and isAdmin and param.tab eq 'reports'}"/>
<c:set var="activePublications" value="${not activeMyData and not activePurchases and not activeSales and not activeReviews and not activeReports}"/>

<spring:message code="Profile.title" var="profileTitle" />
<ui:layout title="${profileTitle}">

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
                            <div class="profile-rating-row" aria-label="<spring:message code='Profile.rating.ariaLabel' />">
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
                                    <c:out value="${sellerRating.formattedAvg}"/> (<c:out value="${sellerRating.count}"/> <spring:message code="${sellerRating.count == 1 ? 'Profile.rating.reviews.singular' : 'Profile.rating.reviews.plural'}" />)
                                </span>
                            </div>
                        </c:if>
                    </div>
                </div>

                <c:if test="${isOwnProfile}">
                    <div>
                        <a href="<c:url value='/resetPassword'/>" class="btn btn-retro btn-retro-secondary" role="button">
                            <spring:message code="Profile.changePassword" />
                        </a>
                        <form action="<c:url value='/logout' />" method="post" style="margin-top: 1rem;">
                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
                            <button type="submit" class="btn btn-retro btn-retro-secondary">
                                <i class="bi bi-box-arrow-right" aria-hidden="true"></i> <spring:message code="Profile.logout" />
                            </button>
                        </form>
                    </div>
                </c:if>
            </div>

            <c:if test="${isOwnProfile}">
                <c:if test="${param.updated eq '1'}">
                    <div class="alert-retro alert-retro-success mt-3" role="alert">
                        <i class="bi bi-check-circle" aria-hidden="true"></i> <spring:message code="Profile.alert.updated" />
                    </div>
                </c:if>
                <c:if test="${param.missingData eq 'purchase'}">
                    <div class="alert-retro alert-retro-warning mt-3" role="alert">
                        <i class="bi bi-exclamation-triangle" aria-hidden="true"></i>
                        <spring:message code="Profile.alert.missingData.purchase" />
                        <c:if test="${not empty param.productId}">
                            <a href="<c:url value='/products/${param.productId}'/>" class="alert-link" style="margin-left: 0.5rem;"><spring:message code="Profile.alert.backToProduct" /></a>
                        </c:if>
                    </div>
                </c:if>
                <c:if test="${param.missingData eq 'publish'}">
                    <div class="alert-retro alert-retro-warning mt-3" role="alert">
                        <i class="bi bi-exclamation-triangle" aria-hidden="true"></i>
                        <spring:message code="Profile.alert.missingData.publish" />
                    </div>
                </c:if>
                <c:if test="${param.deleted eq '1'}">
                    <div class="alert-retro alert-retro-success mt-3" role="alert">
                        <i class="bi bi-check-circle" aria-hidden="true"></i>
                        <spring:message code="Profile.alert.deleted" />
                    </div>
                </c:if>
                <c:if test="${param.deleteError eq 'forbidden'}">
                    <div class="alert-retro alert-retro-warning mt-3" role="alert">
                        <i class="bi bi-exclamation-triangle" aria-hidden="true"></i>
                        <spring:message code="Profile.alert.deleteForbidden" />
                    </div>
                </c:if>
                <div class="mt-2">
                    <a href="<c:url value='/profile/trash'/>" class="btn btn-retro btn-retro-outline btn-sm">
                        <i class="bi bi-trash3" aria-hidden="true"></i> <spring:message code="Profile.trash.link" />
                    </a>
                </div>
            </c:if>

            <c:if test="${isOwnProfile and param.hidden eq '1'}">
                <div class="alert-retro alert-retro-success mt-3" role="alert">
                    <i class="bi bi-check-circle" aria-hidden="true"></i>
                    <spring:message code="Profile.alert.hidden" />
                </div>
            </c:if>
            <c:if test="${isOwnProfile and param.banned eq '1'}">
                <div class="alert-retro alert-retro-success mt-3" role="alert">
                    <i class="bi bi-check-circle" aria-hidden="true"></i>
                    <spring:message code="Profile.alert.banned" />
                </div>
            </c:if>

            <!-- Tabs -->
            <ul class="nav nav-tabs mt-4" id="profileTabs" role="tablist" style="border-bottom: 2px solid var(--color-border);">
                <li class="nav-item" role="presentation">
                    <button class="nav-link<c:if test='${activePublications}'> active</c:if>" id="publications-tab" data-bs-toggle="tab" data-bs-target="#publications" type="button" role="tab" aria-controls="publications" aria-selected="${activePublications}" style="font-weight: 600;">
                        <i class="bi bi-vinyl" aria-hidden="true"></i> <spring:message code="Profile.tabs.publications" />
                    </button>
                </li>
                <c:if test="${isOwnProfile}">
                    <li class="nav-item" role="presentation">
                        <button class="nav-link<c:if test='${activeMyData}'> active</c:if>" id="mydata-tab" data-bs-toggle="tab" data-bs-target="#mydata" type="button" role="tab" aria-controls="mydata" aria-selected="${activeMyData}" style="font-weight: 600;">
                            <i class="bi bi-person-lines-fill" aria-hidden="true"></i> <spring:message code="Profile.tabs.myData" />
                        </button>
                    </li>
                    <li class="nav-item" role="presentation">
                        <button class="nav-link<c:if test='${activePurchases}'> active</c:if>" id="purchases-tab" data-bs-toggle="tab" data-bs-target="#purchases" type="button" role="tab" aria-controls="purchases" aria-selected="${activePurchases}" style="font-weight: 600;">
                            <i class="bi bi-bag" aria-hidden="true"></i> <spring:message code="Profile.tabs.purchases" />
                        </button>
                    </li>
                    <li class="nav-item" role="presentation">
                        <button class="nav-link<c:if test='${activeSales}'> active</c:if>" id="sales-tab" data-bs-toggle="tab" data-bs-target="#sales" type="button" role="tab" aria-controls="sales" aria-selected="${activeSales}" style="font-weight: 600;">
                            <i class="bi bi-shop" aria-hidden="true"></i> <spring:message code="Profile.tabs.sales" />
                        </button>
                    </li>
                </c:if>
                <li class="nav-item" role="presentation">
                    <button class="nav-link<c:if test='${activeReviews}'> active</c:if>" id="reviews-tab" data-bs-toggle="tab" data-bs-target="#reviews" type="button" role="tab" aria-controls="reviews" aria-selected="${activeReviews}" style="font-weight: 600;">
                        <i class="bi bi-star" aria-hidden="true"></i> <spring:message code="Profile.tabs.reviews" />
                    </button>
                </li>
                <c:if test="${isOwnProfile and isAdmin}">
                    <li class="nav-item" role="presentation">
                        <button class="nav-link<c:if test='${activeReports}'> active</c:if>" id="reports-tab" data-bs-toggle="tab" data-bs-target="#reports" type="button" role="tab" aria-controls="reports" aria-selected="${activeReports}" style="font-weight: 600;">
                            <i class="bi bi-flag" aria-hidden="true"></i> <spring:message code="Profile.tabs.reports" />
                        </button>
                    </li>
                </c:if>
            </ul>

            <div class="tab-content mt-3" id="profileTabContent">
                <!-- Tab: Publicaciones -->
                <div class="tab-pane fade<c:if test='${activePublications}'> show active</c:if>" id="publications" role="tabpanel" aria-labelledby="publications-tab">
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
                                            <c:url var="editProductUrl" value="/products/${product.id}/edit"/>
                                            <a href="<c:out value='${editProductUrl}'/>" class="btn btn-retro btn-retro-primary w-100 mt-2" style="font-size: 0.85rem; padding: 0.4rem 0.75rem;">
                                                <i class="bi bi-pencil-square" aria-hidden="true"></i> <spring:message code="Profile.publications.editButton" />
                                            </a>
                                            <c:url var="deleteProductUrl" value="/products/${product.id}/delete"/>
                                            <spring:message code="Profile.publications.deleteConfirm" var="confirmDelete" />
                                            <form action="${deleteProductUrl}" method="post" class="mt-2" onsubmit="return confirm('${confirmDelete}');">
                                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
                                                <button type="submit" class="btn btn-retro btn-retro-secondary w-100" style="font-size: 0.85rem; padding: 0.4rem 0.75rem;">
                                                    <i class="bi bi-trash" aria-hidden="true"></i> <spring:message code="Profile.publications.deleteButton" />
                                                </button>
                                            </form>
                                        </c:if>
                                    </div>
                                </c:forEach>
                            </div>
                            <ui:pagination result="${userProductsPage}" />
                        </c:when>
                        <c:otherwise>
                            <div class="empty-products-state">
                                <i class="bi bi-vinyl" style="font-size: 2.5rem; color: var(--color-border);"></i>
                                <p style="color: var(--color-text-muted); font-size: 1rem; margin: 0;">
                                    <c:choose>
                                        <c:when test="${isOwnProfile}"><spring:message code="Profile.publications.empty.own" /></c:when>
                                        <c:otherwise><spring:message code="Profile.publications.empty.other" /></c:otherwise>
                                    </c:choose>
                                </p>
                                <c:if test="${isOwnProfile}">
                                    <a href="<c:url value='/products/new'/>" class="btn btn-retro btn-retro-primary" style="justify-self: center;">
                                        <i class="bi bi-plus-lg" aria-hidden="true"></i> <spring:message code="Profile.publications.publishFirst" />
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
                                <spring:message code="Profile.myData.help" />
                            </p>
                            <c:url var="profileUpdateUrl" value="/profile/update"/>
                            <form:form modelAttribute="userProfileForm" action="${profileUpdateUrl}" method="post" cssClass="user-profile-form" id="profileForm">
                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
                                <div class="row g-2">
                                    <div class="col-md-6 mb-3">
                                        <label class="form-label" for="pfFirstName"><spring:message code="Profile.myData.firstName" /> <span class="text-danger">*</span></label>
                                        <form:input path="firstName" id="pfFirstName" cssClass="form-control" autocomplete="given-name" />
                                        <form:errors path="firstName" cssClass="text-danger small d-block"/>
                                    </div>
                                    <div class="col-md-6 mb-3">
                                        <label class="form-label" for="pfLastName"><spring:message code="Profile.myData.lastName" /> <span class="text-danger">*</span></label>
                                        <form:input path="lastName" id="pfLastName" cssClass="form-control" autocomplete="family-name" />
                                        <form:errors path="lastName" cssClass="text-danger small d-block"/>
                                    </div>
                                </div>
                                <div class="mb-3">
                                    <label class="form-label" for="pfStreet"><spring:message code="Profile.myData.street" /></label>
                                    <spring:message code="Common.optional" var="optionalPlaceholder" />
                                    <form:input path="streetName" id="pfStreet" cssClass="form-control" placeholder="${optionalPlaceholder}"/>
                                    <form:errors path="streetName" cssClass="text-danger small d-block"/>
                                </div>
                                <div class="mb-3">
                                    <label class="form-label" for="pfStreetNum"><spring:message code="Profile.myData.number" /></label>
                                    <form:input type="number" path="streetNumber" id="pfStreetNum" cssClass="form-control" placeholder="${optionalPlaceholder}" min="1"/>
                                    <form:errors path="streetNumber" cssClass="text-danger small d-block"/>
                                </div>
                                <div class="row g-2">
                                    <div class="col-md-6 mb-3">
                                        <label class="form-label" for="pfNeighborhood"><spring:message code="Profile.myData.neighborhood" /></label>
                                        <form:input path="neighborhood" id="pfNeighborhood" cssClass="form-control" placeholder="${optionalPlaceholder}"/>
                                        <form:errors path="neighborhood" cssClass="text-danger small d-block"/>
                                    </div>
                                    <div class="col-md-6 mb-3">
                                        <label class="form-label" for="pfProvince"><spring:message code="Profile.myData.province" /></label>
                                        <form:input path="province" id="pfProvince" cssClass="form-control" placeholder="${optionalPlaceholder}"/>
                                        <form:errors path="province" cssClass="text-danger small d-block"/>
                                    </div>
                                </div>
                                <div class="mb-3">
                                    <label class="form-label" for="pfExtra"><spring:message code="Profile.myData.extraInfo" /></label>
                                    <form:input path="extraAddressInfo" id="pfExtra" cssClass="form-control" placeholder="${optionalPlaceholder}"/>
                                    <form:errors path="extraAddressInfo" cssClass="text-danger small d-block"/>
                                </div>
                                <div class="mb-3">
                                    <label class="form-label" for="pfCbu"><spring:message code="Profile.myData.cbuCvu" /></label>
                                    <form:input path="cbuCvu" id="pfCbu" cssClass="form-control" placeholder="${optionalPlaceholder}" inputmode="numeric" maxlength="22"/>
                                    <form:errors path="cbuCvu" cssClass="text-danger small d-block"/>
                                </div>
                                <button type="submit" class="btn btn-retro btn-retro-primary" id="profileSaveBtn" disabled="true">
                                    <i class="bi bi-save" aria-hidden="true"></i> <spring:message code="Profile.myData.save" />
                                </button>
                            </form:form>
                        </div>
                    </div>
                </c:if>

                <!-- Tab: Mis compras (only own profile) -->
                <c:if test="${isOwnProfile}">
                    <div class="tab-pane fade<c:if test='${activePurchases}'> show active</c:if>" id="purchases" role="tabpanel" aria-labelledby="purchases-tab">
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
                                                    <i class="bi bi-eye" aria-hidden="true"></i> <spring:message code="Profile.reports.view" />
                                                </a>
                                                <c:if test="${purchase.status eq 'DELIVERED' and not purchaseHasReview[purchase.purchaseId]}">
                                                    <a href="<c:url value='/purchases/${purchase.purchaseId}/review?token=${purchase.buyerToken}'/>"
                                                       class="btn btn-retro btn-retro-primary" style="font-size: 0.8rem; padding: 0.4rem 0.8rem;">
                                                        <i class="bi bi-star" aria-hidden="true"></i> <spring:message code="PurchasePanel.buyer.delivered.review" />
                                                    </a>
                                                </c:if>
                                            </div>
                                        </div>
                                    </c:forEach>
                                </div>
                                <ui:pagination result="${purchasesPage}" />
                            </c:when>
                            <c:otherwise>
                                <div class="empty-products-state">
                                    <i class="bi bi-bag" style="font-size: 2.5rem; color: var(--color-border);"></i>
                                    <p style="color: var(--color-text-muted); font-size: 1rem; margin: 0;"><spring:message code="Profile.purchases.empty" /></p>
                                    <a href="<c:url value='/'/>" class="btn btn-retro btn-retro-primary" style="justify-self: center;">
                                        <i class="bi bi-search" aria-hidden="true"></i> <spring:message code="Profile.purchases.explore" />
                                    </a>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </c:if>

                <!-- Tab: Mis ventas (only own profile) -->
                <c:if test="${isOwnProfile}">
                    <div class="tab-pane fade<c:if test='${activeSales}'> show active</c:if>" id="sales" role="tabpanel" aria-labelledby="sales-tab">
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
                                                    <i class="bi bi-eye" aria-hidden="true"></i> <spring:message code="Profile.reports.view" />
                                                </a>
                                            </div>
                                        </div>
                                    </c:forEach>
                                </div>
                                <ui:pagination result="${salesPage}" />
                            </c:when>
                            <c:otherwise>
                                <div class="empty-products-state">
                                    <i class="bi bi-shop" style="font-size: 2.5rem; color: var(--color-border);"></i>
                                    <p style="color: var(--color-text-muted); font-size: 1rem; margin: 0;"><spring:message code="Profile.sales.empty" /></p>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </c:if>

                <!-- Tab: Reseñas recibidas -->
                <div class="tab-pane fade<c:if test='${activeReviews}'> show active</c:if>" id="reviews" role="tabpanel" aria-labelledby="reviews-tab">
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
                            <ui:pagination result="${receivedReviewsPage}" />
                        </c:when>
                        <c:otherwise>
                            <div class="empty-products-state">
                                <i class="bi bi-star" style="font-size: 2.5rem; color: var(--color-border);"></i>
                                <p style="color: var(--color-text-muted); font-size: 1rem; margin: 0;">
                                    <c:choose>
                                        <c:when test="${isOwnProfile}"><spring:message code="Profile.reviews.empty.own" /></c:when>
                                        <c:otherwise><spring:message code="Profile.reviews.empty.other" /></c:otherwise>
                                    </c:choose>
                                </p>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>

                <!-- Tab: Reportes (solo moderadores en perfil propio) -->
                <c:if test="${isOwnProfile and isAdmin}">
                    <div class="tab-pane fade<c:if test='${activeReports}'> show active</c:if>" id="reports" role="tabpanel" aria-labelledby="reports-tab">
                        <c:choose>
                            <c:when test="${not empty reportedProducts}">
                                <div class="d-flex flex-column gap-3">
                                    <c:forEach items="${reportedProducts}" var="rp">
                                        <div style="background: #fff; border-radius: 16px; padding: 1.25rem; border: 1px solid var(--color-border); box-shadow: 0 2px 8px rgba(0,0,0,0.04); display: flex; align-items: center; gap: 1rem;">
                                            <img src="<c:url value='/images/product/${rp.productId}'/>" alt=""
                                                 style="width: 70px; height: 70px; border-radius: 12px; object-fit: cover; flex-shrink: 0;"
                                                 onerror="this.src='https://via.placeholder.com/70?text=%E2%80%94';"/>
                                            <div style="flex: 1; min-width: 0;">
                                                <div style="font-weight: 600; font-size: 1rem; color: var(--color-text-main); white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">
                                                    <c:out value="${rp.productTitle}"/>
                                                </div>
                                                <div style="font-size: 0.85rem; color: var(--color-text-muted);">
                                                    <c:out value="${rp.productArtist}"/>
                                                </div>
                                                <div style="margin-top: 0.35rem; display: flex; align-items: center; gap: 0.5rem;">
                                                    <span style="background: #dc3545; color: #fff; font-weight: 700; font-size: 0.75rem; padding: 0.2rem 0.6rem; border-radius: 50px;">
                                                        <i class="bi bi-flag-fill" aria-hidden="true"></i>
                                                        <spring:message code="Profile.reports.count" arguments="${rp.reportCount},${rp.reportCount}" />
                                                    </span>
                                                    <span style="font-size: 0.8rem; color: var(--color-text-muted);">
                                                        <spring:message code="Profile.reports.publishedBy" /> <a href="<c:url value='/profile?userId=${rp.ownerUserId}'/>" style="color: var(--color-accent); text-decoration: none; font-weight: 600;"><c:out value="${rp.ownerUsername}"/></a>
                                                    </span>
                                                </div>
                                            </div>
                                            <div style="display: flex; flex-wrap: wrap; gap: 0.4rem; align-items: center; flex-shrink: 0;">
                                                <spring:message code="Profile.reports.view" var="viewText" />
                                                <a href="<c:url value='/products/${rp.productId}'/>" class="btn btn-retro btn-retro-secondary" style="font-size: 0.78rem; padding: 0.35rem 0.7rem;" title="${viewText}">
                                                    <i class="bi bi-eye" aria-hidden="true"></i> <c:out value="${viewText}"/>
                                                </a>
                                                <c:url var="hideUrl" value="/profile/admin/hide-product"/>
                                                <spring:message code="Profile.reports.hideConfirm" var="confirmHide" />
                                                <spring:message code="Profile.reports.hide" var="hideText" />
                                                <form action="<c:out value='${hideUrl}'/>" method="post" style="margin: 0;" onsubmit="return confirm('${confirmHide}');">
                                                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
                                                    <input type="hidden" name="productId" value="<c:out value='${rp.productId}'/>" />
                                                    <button type="submit" class="btn btn-retro btn-retro-secondary" style="font-size: 0.78rem; padding: 0.35rem 0.7rem; color: #dc3545; border-color: #dc3545;" title="${hideText}">
                                                        <i class="bi bi-x-circle" aria-hidden="true"></i> <c:out value="${hideText}"/>
                                                    </button>
                                                </form>
                                                <spring:message code="Profile.reports.profile" var="profileText" />
                                                <a href="<c:url value='/profile?userId=${rp.ownerUserId}'/>" class="btn btn-retro btn-retro-secondary" style="font-size: 0.78rem; padding: 0.35rem 0.7rem;" title="${profileText}">
                                                    <i class="bi bi-person" aria-hidden="true"></i> <c:out value="${profileText}"/>
                                                </a>
                                                <c:url var="banUrl" value="/profile/admin/ban-user"/>
                                                <spring:message code="Profile.reports.banConfirm" var="confirmBan" />
                                                <spring:message code="Profile.reports.ban" var="banText" />
                                                <form action="<c:out value='${banUrl}'/>" method="post" style="margin: 0;" onsubmit="return confirm('${confirmBan}');">
                                                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
                                                    <input type="hidden" name="userId" value="<c:out value='${rp.ownerUserId}'/>" />
                                                    <button type="submit" class="btn btn-retro btn-retro-secondary" style="font-size: 0.78rem; padding: 0.35rem 0.7rem; color: #fff; background: #dc3545; border-color: #dc3545;" title="${banText}">
                                                        <i class="bi bi-person-x" aria-hidden="true"></i> <c:out value="${banText}"/>
                                                    </button>
                                                </form>
                                            </div>
                                        </div>
                                    </c:forEach>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="empty-products-state">
                                    <i class="bi bi-flag" style="font-size: 2.5rem; color: var(--color-border);"></i>
                                    <p style="color: var(--color-text-muted); font-size: 1rem; margin: 0;"><spring:message code="Profile.reports.empty" /></p>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </c:if>
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
