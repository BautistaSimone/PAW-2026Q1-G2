<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<c:set var="activeMyData" value="${isOwnProfile and param.tab eq 'mydata'}" />
<c:set var="activeWishlistProducts" value="${param.tab eq 'wishlist'}" />
<c:set var="activePurchases" value="${isOwnProfile and param.tab eq 'purchases'}" />
<c:set var="activeSales" value="${isOwnProfile and param.tab eq 'sales'}" />
<c:set var="activeReviews" value="${param.tab eq 'reviews'}" />
<c:set var="activeTrash" value="${isOwnProfile and param.tab eq 'trash'}" />
<c:set var="activeFollowers" value="${param.tab eq 'followers'}" />
<c:set var="activeFollowing" value="${param.tab eq 'following'}" />
<sec:authorize access="hasRole('ADMIN')" var="isAdmin" />
<c:set var="activeReports"
    value="${isOwnProfile and isAdmin and param.tab eq 'reports'}" />
<c:set var="activePublications"
    value="${not activeWishlistProducts and not activeMyData and not activePurchases and not activeSales and not activeReviews and not activeTrash and not activeReports and not activeFollowers and not activeFollowing}" />


<spring:message code="Profile.title" var="profileTitle" />
<ui:layout title="${profileTitle}">

    <ui:header showHeaderActions="true" />

    <spring:message code="Profile.Confirm.title" var="modalTitle" />
    <spring:message code="Profile.Confirm.text" var="modalText" />
    <spring:message code="Global.cancel" var="cancelText" />
    <spring:message code="Global.confirm" var="confirmText" />
    <ui:modal
        id="confirmationModal"
        title="${modalTitle}"
        text="${modalText}"
        primaryBtn="${confirmText}"
        secondaryBtn="${cancelText}"
        primaryBtnId="confirmRemoveBtn" />

    <div class="profile-page">
        <div class="container py-4">
            <div class="profile-hero">
                <div class="profile-hero-main">
                    <div class="profile-avatar-lg">
                        <c:out value="${fn:substring(user.username, 0, 1)}" />
                    </div>
                    <div class="profile-hero-info">
                        <div class="profile-hero-header">
                            <h1 class="profile-username">
                                <c:out value="${user.username}" />
                            </h1>
                            <c:if
                                test="${not empty user.firstName or not empty user.lastName}">
                                <div class="profile-fullname">
                                    <c:out value="${user.firstName}" />
                                    <c:out value="${user.lastName}" />
                                </div>
                            </c:if>
                        </div>
                        <div class="profile-bio">
                            <c:if test="${sellerRating.count > 0}">
                                <div class="profile-rating-row"
                                    aria-label="<spring:message code='Profile.rating.ariaLabel' />">
                                    <span class="profile-rating-stars"
                                        aria-hidden="true">
                                        <c:forEach begin="1" end="5" var="i">
                                            <c:choose>
                                                <c:when
                                                    test="${i <= sellerRating.avgScore}">
                                                    <i class="bi bi-star-fill"></i>
                                                </c:when>
                                                <c:when
                                                    test="${i - 0.5 <= sellerRating.avgScore}">
                                                    <i class="bi bi-star-half"></i>
                                                </c:when>
                                                <c:otherwise><i class="bi bi-star"></i>
                                                </c:otherwise>
                                            </c:choose>
                                        </c:forEach>
                                    </span>
                                    <span class="profile-rating-caption">
                                        <c:out value="${sellerRating.formattedAvg}" /> (
                                        <c:out value="${sellerRating.count}" />
                                        <spring:message
                                            code="${sellerRating.count == 1 ? 'Profile.rating.reviews.singular' : 'Profile.rating.reviews.plural'}" />
                                        )
                                    </span>
                                </div>
                            </c:if>
                            <c:if test="${not empty user.location}">
                                <div class="profile-location">
                                    <i class="bi bi-geo-alt" aria-hidden="true"></i>
                                    <c:out value="${user.location}" />
                                </div>
                            </c:if>
                        </div>
                        <div class="profile-stats">
                            <div class="profile-stat">
                                <span class="profile-stat-value">
                                    <c:out value="${userProductsPage.totalCount}" />
                                </span>
                                <span class="profile-stat-label">
                                    <spring:message code="Profile.tabs.publications" />
                                </span>
                            </div>
                            <button type="button" class="profile-stat profile-stat-btn"
                                data-bs-toggle="modal" data-bs-target="#followersModal">
                                <span class="profile-stat-value">
                                    <c:out value="${followerCount}" />
                                </span>
                                <span class="profile-stat-label">
                                    <spring:message code="Profile.followers" />
                                </span>
                            </button>
                            <button type="button" class="profile-stat profile-stat-btn"
                                data-bs-toggle="modal" data-bs-target="#followingModal">
                                <span class="profile-stat-value">
                                    <c:out value="${followingCount}" />
                                </span>
                                <span class="profile-stat-label">
                                    <spring:message code="Profile.following" />
                                </span>
                            </button>
                        </div>
                    </div>
                </div>
                <div class="profile-hero-actions">
                    <c:choose>
                        <c:when test="${isOwnProfile}">
                            <a href="<c:url value='/changePassword'/>"
                                class="btn btn-retro btn-retro-secondary" role="button">
                                <spring:message code="Profile.changePassword" />
                            </a>
                            <button type="button"
                                class="btn btn-retro btn-retro-secondary"
                                data-bs-toggle="modal"
                                data-bs-target="#logoutConfirmModal">
                                <i class="bi bi-box-arrow-right"
                                    aria-hidden="true"></i>
                                <spring:message code="Profile.logout" />
                            </button>
                            <c:url var="logoutUrl" value="/logout" />
                            <spring:message code="Profile.logout.confirm.title"
                                var="logoutConfirmTitle" />
                            <spring:message code="Profile.logout.confirm.text"
                                var="logoutConfirmText" />
                            <spring:message code="Profile.logout" var="logoutBtnText" />
                            <ui:confirmFormModal
                                id="logoutConfirmModal"
                                title="${logoutConfirmTitle}"
                                message="${logoutConfirmText}"
                                confirmBtnText="${logoutBtnText}"
                                cancelBtnText="${cancelText}"
                                actionUrl="${logoutUrl}"
                                confirmBtnClass="btn-retro-primary" />
                        </c:when>
                        <c:otherwise>
                            <sec:authorize access="isAuthenticated()">
                                <form action="<c:url value='/profile/follow' />"
                                    method="post">
                                    <input type="hidden" name="${_csrf.parameterName}"
                                        value="${_csrf.token}" />
                                    <input type="hidden" name="userId"
                                        value="${user.id}" />
                                    <c:choose>
                                        <c:when test="${isFollowing}">
                                            <button type="submit"
                                                class="btn btn-retro btn-retro-secondary btn-follow">
                                                <i class="bi bi-person-dash"
                                                    aria-hidden="true"></i>
                                                <spring:message
                                                    code="Profile.unfollow" />
                                            </button>
                                        </c:when>
                                        <c:otherwise>
                                            <button type="submit"
                                                class="btn btn-retro btn-retro-primary btn-follow">
                                                <i class="bi bi-person-plus"
                                                    aria-hidden="true"></i>
                                                <spring:message code="Profile.follow" />
                                            </button>
                                        </c:otherwise>
                                    </c:choose>
                                </form>
                            </sec:authorize>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>

            <c:if test="${isOwnProfile}">
                <c:if test="${param.updated eq '1'}">
                    <div class="alert-retro alert-retro-success mt-3" role="alert">
                        <i class="bi bi-check-circle" aria-hidden="true"></i>
                        <spring:message code="Profile.alert.updated" />
                    </div>
                </c:if>
                <c:if test="${param.missingData eq 'purchase'}">
                    <div class="alert-retro alert-retro-warning mt-3" role="alert">
                        <i class="bi bi-exclamation-triangle" aria-hidden="true"></i>
                        <spring:message code="Profile.alert.missingData.purchase" />
                        <c:if test="${not empty param.productId}">
                            <a href="<c:url value='/products/${param.productId}'/>"
                                class="alert-link" style="margin-left: 0.5rem;">
                                <spring:message code="Profile.alert.backToProduct" />
                            </a>
                        </c:if>
                    </div>
                </c:if>
                <c:if test="${param.missingData eq 'publish'}">
                    <div class="alert-retro alert-retro-warning mt-3" role="alert">
                        <i class="bi bi-exclamation-triangle" aria-hidden="true"></i>
                        <spring:message code="Profile.alert.missingData.publish" />
                    </div>
                </c:if>
                <c:if test="${deleted}">
                    <div class="alert-retro alert-retro-success mt-3" role="alert">
                        <i class="bi bi-check-circle" aria-hidden="true"></i>
                        <spring:message code="Profile.alert.deleted" />
                    </div>
                </c:if>
                <c:if test="${deleteError eq 'forbidden'}">
                    <div class="alert-retro alert-retro-warning mt-3" role="alert">
                        <i class="bi bi-exclamation-triangle" aria-hidden="true"></i>
                        <spring:message code="Profile.alert.deleteForbidden" />
                    </div>
                </c:if>
            </c:if>

            <c:if test="${isOwnProfile and hidden}">
                <div class="alert-retro alert-retro-success mt-3" role="alert">
                    <i class="bi bi-check-circle" aria-hidden="true"></i>
                    <spring:message code="Profile.alert.hidden" />
                </div>
            </c:if>
            <c:if test="${isOwnProfile and banned}">
                <div class="alert-retro alert-retro-success mt-3" role="alert">
                    <i class="bi bi-check-circle" aria-hidden="true"></i>
                    <spring:message code="Profile.alert.banned" />
                </div>
            </c:if>

            <c:url var="profileTabWishlistUrl" value="/profile">
                <c:param name="tab" value="wishlist" />
                <c:if test="${not empty param.userId}">
                    <c:param name="userId" value="${param.userId}" />
                </c:if>
            </c:url>

            <c:url var="profileTabPublicationsUrl" value="/profile">
                <c:if test="${not empty param.userId}">
                    <c:param name="userId" value="${param.userId}" />
                </c:if>
            </c:url>
            <c:url var="profileTabMydataUrl" value="/profile">
                <c:param name="tab" value="mydata" />
            </c:url>
            <c:url var="profileTabPurchasesUrl" value="/profile">
                <c:param name="tab" value="purchases" />
            </c:url>
            <c:url var="profileTabSalesUrl" value="/profile">
                <c:param name="tab" value="sales" />
            </c:url>
            <c:url var="profileTabReviewsUrl" value="/profile">
                <c:param name="tab" value="reviews" />
                <c:if test="${not empty param.userId}">
                    <c:param name="userId" value="${param.userId}" />
                </c:if>
            </c:url>
            <c:url var="profileTabTrashUrl" value="/profile">
                <c:param name="tab" value="trash" />
            </c:url>
            <c:url var="profileTabReportsUrl" value="/profile">
                <c:param name="tab" value="reports" />
            </c:url>
            <c:url var="profileTabFollowersUrl" value="/profile">
                <c:param name="tab" value="followers" />
                <c:if test="${not empty param.userId}">
                    <c:param name="userId" value="${param.userId}" />
                </c:if>
            </c:url>
            <c:url var="profileTabFollowingUrl" value="/profile">
                <c:param name="tab" value="following" />
                <c:if test="${not empty param.userId}">
                    <c:param name="userId" value="${param.userId}" />
                </c:if>
            </c:url>

            <!-- Tabs -->
            <ul class="nav nav-tabs mt-4 profile-ul-2" id="profileTabs" role="tablist">
                <li class="nav-item" role="presentation">
                    <a class="nav-link<c:if test='${activePublications}'> active</c:if>"
                        id="publications-tab"
                        href="<c:out value='${profileTabPublicationsUrl}'/>" role="tab"
                        aria-controls="publications"
                        aria-selected="${activePublications}" style="font-weight: 600;">
                        <i class="bi bi-vinyl" aria-hidden="true"></i>
                        <spring:message code="Profile.tabs.publications" />
                    </a>
                </li>
                <li class="nav-item" role="presentation">
                    <a class="nav-link<c:if test='${activeWishlistProducts}'> active</c:if>"
                        id="wishlist-tab" href="<c:out value='${profileTabWishlistUrl}'/>"
                        role="tab" aria-controls="wishlist"
                        aria-selected="${activeWishlistProducts}" style="font-weight: 600;">
                        <i class="bi bi-heart-fill" aria-hidden="true"></i>
                        <spring:message code="Profile.tabs.wishlist" />
                    </a>
                </li>
                <c:if test="${isOwnProfile}">
                    <li class="nav-item" role="presentation">
                        <a class="nav-link<c:if test='${activeMyData}'> active</c:if>"
                            id="mydata-tab"
                            href="<c:out value='${profileTabMydataUrl}'/>" role="tab"
                            aria-controls="mydata" aria-selected="${activeMyData}"
                            style="font-weight: 600;">
                            <i class="bi bi-person-lines-fill" aria-hidden="true"></i>
                            <spring:message code="Profile.tabs.myData" />
                        </a>
                    </li>
                    <li class="nav-item" role="presentation">
                        <a class="nav-link<c:if test='${activePurchases}'> active</c:if>"
                            id="purchases-tab"
                            href="<c:out value='${profileTabPurchasesUrl}'/>" role="tab"
                            aria-controls="purchases" aria-selected="${activePurchases}"
                            style="font-weight: 600;">
                            <i class="bi bi-bag" aria-hidden="true"></i>
                            <spring:message code="Profile.tabs.purchases" />
                        </a>
                    </li>
                    <li class="nav-item" role="presentation">
                        <a class="nav-link<c:if test='${activeSales}'> active</c:if>"
                            id="sales-tab" href="<c:out value='${profileTabSalesUrl}'/>"
                            role="tab" aria-controls="sales"
                            aria-selected="${activeSales}" style="font-weight: 600;">
                            <i class="bi bi-shop" aria-hidden="true"></i>
                            <spring:message code="Profile.tabs.sales" />
                        </a>
                    </li>
                </c:if>
                <li class="nav-item" role="presentation">
                    <a class="nav-link<c:if test='${activeReviews}'> active</c:if>"
                        id="reviews-tab" href="<c:out value='${profileTabReviewsUrl}'/>"
                        role="tab" aria-controls="reviews"
                        aria-selected="${activeReviews}" style="font-weight: 600;">
                        <i class="bi bi-star" aria-hidden="true"></i>
                        <spring:message code="Profile.tabs.reviews" />
                    </a>
                </li>
                <c:if test="${isOwnProfile}">
                    <li class="nav-item" role="presentation">
                        <a class="nav-link<c:if test='${activeTrash}'> active</c:if>"
                            id="trash-tab" href="<c:out value='${profileTabTrashUrl}'/>"
                            role="tab" aria-controls="trash"
                            aria-selected="${activeTrash}" style="font-weight: 600;">
                            <i class="bi bi-trash3" aria-hidden="true"></i>
                            <spring:message code="Profile.tabs.trash" />
                        </a>
                    </li>
                </c:if>
                <c:if test="${isOwnProfile and isAdmin}">
                    <li class="nav-item" role="presentation">
                        <a class="nav-link<c:if test='${activeReports}'> active</c:if>"
                            id="reports-tab"
                            href="<c:out value='${profileTabReportsUrl}'/>" role="tab"
                            aria-controls="reports" aria-selected="${activeReports}"
                            style="font-weight: 600;">
                            <i class="bi bi-flag" aria-hidden="true"></i>
                            <spring:message code="Profile.tabs.reports" />
                        </a>
                    </li>
                </c:if>
            </ul>

            <div class="tab-content mt-3" id="profileTabContent">
                <!-- Tab: Publicaciones -->
                <div class="tab-pane fade<c:if test='${activePublications}'> show active</c:if>"
                    id="publications" role="tabpanel"
                    aria-labelledby="publications-tab">
                    <c:choose>
                        <c:when test="${not empty userProducts}">
                            <div class="profile-publications-stack">
                                <div class="products-grid profile-listings-grid">
                                    <c:forEach items="${userProducts}" var="product">
                                        <div class="products-grid-item">
                                            <c:url value="/products/${product.id}"
                                                var="productUrl" />
                                            <ui:productCard title="${product.title}"
                                                artist="${product.artist}"
                                                price="${product.price}"
                                                installments="${product.installmentPrice}"
                                                imageUrl="${productImageUrls[product.id]}"
                                                categories="${product.categories}"
                                                sellerRating="${sellerRating}"
                                                href="${productUrl}" />
                                            <c:if test="${isOwnProfile}">
                                                <c:url var="editProductUrl"
                                                    value="/products/${product.id}/edit" />
                                                <a href="<c:out value='${editProductUrl}'/>"
                                                    class="btn btn-retro btn-retro-primary w-100 mt-2"
                                                    style="font-size: 0.85rem; padding: 0.4rem 0.75rem;">
                                                    <i class="bi bi-pencil-square"
                                                        aria-hidden="true"></i>
                                                    <spring:message
                                                        code="Profile.publications.editButton" />
                                                </a>
                                                <c:url var="deleteProductUrl"
                                                    value="/products/${product.id}/delete" />
                                                <spring:message
                                                    code="Profile.publications.deleteConfirm"
                                                    var="confirmDelete" />
                                                <spring:message
                                                    code="Profile.publications.deleteButton"
                                                    var="deleteListingText" />
                                                <button type="button"
                                                    class="btn btn-retro btn-retro-secondary w-100 profile-button-3 mt-2"
                                                    data-bs-toggle="modal"
                                                    data-bs-target="#deleteListingModal-${product.id}">
                                                    <i class="bi bi-trash"
                                                        aria-hidden="true"></i>
                                                    <c:out value="${deleteListingText}" />
                                                </button>
                                                <ui:confirmFormModal
                                                    id="deleteListingModal-${product.id}"
                                                    title="${deleteListingText}"
                                                    message="${confirmDelete}"
                                                    confirmBtnText="${deleteListingText}"
                                                    cancelBtnText="${cancelText}"
                                                    actionUrl="${deleteProductUrl}"
                                                    confirmBtnClass="btn-retro-danger" />
                                            </c:if>
                                        </div>
                                    </c:forEach>
                                </div>
                                <ui:pagination result="${userProductsPage}" />
                                <c:if test="${isOwnProfile}">
                                    <div class="d-flex justify-content-center mt-3">
                                        <a href="<c:url value='/products/new'/>"
                                            class="btn btn-retro btn-retro-primary">
                                            <i class="bi bi-plus-lg" aria-hidden="true"></i>
                                            <spring:message
                                                code="Profile.publications.publishVinyl" />
                                        </a>
                                    </div>
                                </c:if>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="empty-products-state">
                                <i class="bi bi-vinyl profile-i-4"></i>
                                <p class="profile-p-5">
                                    <c:choose>
                                        <c:when test="${isOwnProfile}">
                                            <spring:message
                                                code="Profile.publications.empty.own" />
                                        </c:when>
                                        <c:otherwise>
                                            <spring:message
                                                code="Profile.publications.empty.other" />
                                        </c:otherwise>
                                    </c:choose>
                                </p>
                                <c:if test="${isOwnProfile}">
                                    <a href="<c:url value='/products/new'/>"
                                        class="btn btn-retro btn-retro-primary"
                                        style="justify-self: center;">
                                        <i class="bi bi-plus-lg" aria-hidden="true"></i>
                                        <spring:message
                                            code="Profile.publications.publishFirst" />
                                    </a>
                                </c:if>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>

                <!-- Tab: Wishlist -->
                <div class="tab-pane fade<c:if test='${activeWishlistProducts}'> show active</c:if>"
                    id="wishlist" role="tabpanel"
                    aria-labelledby="wishlists-tab">
                    <c:choose>
                        <c:when test="${not empty wishlistProducts}">
                            <div class="profile-wishlist-stack">
                                <div class="products-grid profile-wishlist-grid">
                                    <c:forEach items="${wishlistProducts}" var="product">
                                        <div class="products-grid-item">
                                            <c:url value="/products/${product.id}"
                                                var="productUrl" />
                                            <ui:productCard title="${product.title}"
                                                artist="${product.artist}"
                                                price="${product.price}"
                                                installments="${product.installmentPrice}"
                                                imageUrl="${wishlistProductImageUrls[product.id]}"
                                                categories="${product.categories}"
                                                sellerRating="${sellerRating}"
                                                href="${productUrl}" />

                                            <c:if test="${isOwnProfile}">
                                                <c:url var="removeFromWishlistUrl"
                                                    value="/toggle-wishlist-product" />
                                                <spring:message
                                                    code="Wishlist.remove"
                                                    var="confirmRemove" />

                                                <form action="${removeFromWishlistUrl}"
                                                    method="post"
                                                    class="mt-2"
                                                    onsubmit="return showConfirmationModal(event);">

                                                    <input type="hidden"
                                                        name="productId"
                                                        value="<c:out value='${product.id}'/>" />
                                                    <input type="hidden"
                                                        name="${_csrf.parameterName}"
                                                        value="${_csrf.token}" />

                                                    <button type="submit"
                                                        class="btn btn-retro btn-retro-secondary w-100 profile-button-3">
                                                        <i class="bi bi-trash"
                                                            aria-hidden="true"></i>
                                                        <spring:message
                                                            code="Wishlist.remove" />
                                                    </button>
                                                </form>

                                            </c:if>
                                        </div>
                                    </c:forEach>
                                </div>
                                <ui:pagination result="${wishlistProductsPage}" />
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="empty-products-state">
                                <i class="bi bi-vinyl profile-i-4"></i>
                                    <c:choose>
                                        <c:when test="${isOwnProfile}">
                                            <p class="profile-p-5">
                                                <spring:message
                                                    code="Profile.wishlist.empty.own" />
                                            </p>
                                            <a href="<c:url value='/'/>"
                                                class="btn btn-retro btn-retro-primary"
                                                style="justify-self: center;">
                                                <i class="bi bi-plus-lg" aria-hidden="true"></i>
                                                <spring:message
                                                    code="Profile.wishlist.empty.checkProducts" />
                                            </a>
                                        </c:when>    
                                        <c:otherwise>

                                            <p class="profile-p-5">
                                                <spring:message
                                                    code="Profile.wishlist.empty.other" />
                                            </p>
                                        </c:otherwise>
                                    </c:choose>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
                
                <!-- Tab: Mis datos (solo perfil propio) -->
                <c:if test="${isOwnProfile}">
                    <div class="tab-pane fade<c:if test='${activeMyData}'> show active</c:if>"
                        id="mydata" role="tabpanel" aria-labelledby="mydata-tab">
                        <div class="row g-4">
                            <div class="col-md-6">
                                <div class="profile-div-6">
                                    <h4 class="mb-3"><spring:message code="Profile.tabs.myData" text="Mis Datos" /></h4>
                                    <p class="profile-p-7">
                                        <spring:message code="Profile.myData.help" />
                                    </p>
                                    <c:url var="profileUpdateUrl" value="/profile/update" />
                                    <form:form modelAttribute="userProfileForm"
                                        action="${profileUpdateUrl}" method="post"
                                        cssClass="user-profile-form" id="profileForm"
                                        novalidate="true">
                                        <input type="hidden" name="${_csrf.parameterName}"
                                            value="${_csrf.token}" />
                                        <div class="row g-2">
                                            <div class="col-md-6 mb-3">
                                                <label class="form-label" for="pfFirstName">
                                                    <spring:message
                                                        code="Profile.myData.firstName" /> <span
                                                        class="text-danger">*</span>
                                                </label>
                                                <form:input path="firstName" id="pfFirstName"
                                                    cssClass="form-control"
                                                    autocomplete="given-name" />
                                                <form:errors path="firstName"
                                                    cssClass="text-danger small d-block" />
                                            </div>
                                            <div class="col-md-6 mb-3">
                                                <label class="form-label" for="pfLastName">
                                                    <spring:message
                                                        code="Profile.myData.lastName" /> <span
                                                        class="text-danger">*</span>
                                                </label>
                                                <form:input path="lastName" id="pfLastName"
                                                    cssClass="form-control"
                                                    autocomplete="family-name" />
                                                <form:errors path="lastName"
                                                    cssClass="text-danger small d-block" />
                                            </div>
                                        </div>
                                        <div class="mb-3">
                                            <label class="form-label" for="pfStreet">
                                                <spring:message code="Profile.myData.street" />
                                            </label>
                                            <spring:message code="Common.optional"
                                                var="optionalPlaceholder" />
                                            <form:input path="streetName" id="pfStreet"
                                                cssClass="form-control"
                                                placeholder="${optionalPlaceholder}" />
                                            <form:errors path="streetName"
                                                cssClass="text-danger small d-block" />
                                        </div>
                                        <div class="mb-3">
                                            <label class="form-label" for="pfStreetNum">
                                                <spring:message code="Profile.myData.number" />
                                            </label>
                                            <form:input type="number" path="streetNumber"
                                                id="pfStreetNum" cssClass="form-control"
                                                placeholder="${optionalPlaceholder}" min="1" />
                                            <form:errors path="streetNumber"
                                                cssClass="text-danger small d-block" />
                                        </div>
                                        <div class="row g-2">
                                            <div class="col-md-6 mb-3">
                                                <label class="form-label" for="pfNeighborhood">
                                                    <spring:message
                                                        code="Profile.myData.neighborhood" />
                                                </label>
                                                <form:input path="neighborhood"
                                                    id="pfNeighborhood" cssClass="form-control"
                                                    placeholder="${optionalPlaceholder}" />
                                                <form:errors path="neighborhood"
                                                    cssClass="text-danger small d-block" />
                                            </div>
                                            <div class="col-md-6 mb-3">
                                                <label class="form-label" for="pfProvince">
                                                    <spring:message
                                                        code="Profile.myData.province" />
                                                </label>
                                                <form:input path="province" id="pfProvince"
                                                    cssClass="form-control"
                                                    placeholder="${optionalPlaceholder}" />
                                                <form:errors path="province"
                                                    cssClass="text-danger small d-block" />
                                            </div>
                                        </div>
                                        <div class="mb-3">
                                            <label class="form-label" for="pfExtra">
                                                <spring:message
                                                    code="Profile.myData.extraInfo" />
                                            </label>
                                            <form:input path="extraAddressInfo" id="pfExtra"
                                                cssClass="form-control"
                                                placeholder="${optionalPlaceholder}" />
                                            <form:errors path="extraAddressInfo"
                                                cssClass="text-danger small d-block" />
                                        </div>
                                        <div class="mb-3">
                                            <label class="form-label" for="pfCbu">
                                                <spring:message code="Profile.myData.cbuCvu" />
                                            </label>
                                            <form:input path="cbuCvu" id="pfCbu"
                                                cssClass="form-control"
                                                placeholder="${optionalPlaceholder}"
                                                inputmode="numeric" maxlength="22" />
                                            <form:errors path="cbuCvu"
                                                cssClass="text-danger small d-block" />
                                        </div>
                                        <div class="mb-3">
                                            <label class="form-label" for="pfLanguage">
                                                <spring:message code="Profile.myData.language" />
                                            </label>
                                            <form:select path="language" id="pfLanguage" cssClass="form-select">
                                                <form:option value="es" label="Español" />
                                                <form:option value="en" label="English" />
                                            </form:select>
                                            <form:errors path="language"
                                                cssClass="text-danger small d-block" />
                                        </div>
                                        <button type="submit"
                                            class="btn btn-retro btn-retro-primary"
                                            id="profileSaveBtn" disabled="true">
                                            <i class="bi bi-save" aria-hidden="true"></i>
                                            <spring:message code="Profile.myData.save" />
                                        </button>
                                    </form:form>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="profile-div-6 profile-favorite-categories-card" id="favorite-categories">
                                    <h4 class="mb-3"><spring:message code="Profile.tabs.favoriteGenres" /></h4>
                                    <p class="profile-p-7">
                                        <spring:message code="Profile.favoriteGenres.help" />
                                    </p>
                                    <c:url var="profileUpdateGenresUrl" value="/profile/update-genres" />
                                    <form action="${profileUpdateGenresUrl}" method="post" id="genresForm">
                                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
                                        <div class="mb-4 d-flex flex-column gap-2" style="max-height: 400px; overflow-y: auto;">
                                            <c:forEach items="${allCategories}" var="category">
                                                <div class="form-check">
                                                    <input class="form-check-input form-check-input-retro" type="checkbox" name="favoriteCategories" value="${category.id}" id="favCat_${category.id}" 
                                                        <c:if test="${fn:contains(userFavoriteCategoryIds, category.id)}">checked</c:if>
                                                    >
                                                    <label class="form-check-label" for="favCat_${category.id}">
                                                        <c:out value="${category.name}" />
                                                    </label>
                                                </div>
                                            </c:forEach>
                                        </div>
                                        <button type="submit" class="btn btn-retro btn-retro-primary">
                                            <i class="bi bi-save" aria-hidden="true"></i>
                                            <spring:message code="Profile.myData.save" />
                                        </button>
                                    </form>
                                </div>
                            </div>
                        </div>
                    </div>
                </c:if>

                <!-- Tab: Mis compras (only own profile) -->
                <c:if test="${isOwnProfile}">
                    <div class="tab-pane fade<c:if test='${activePurchases}'> show active</c:if>"
                        id="purchases" role="tabpanel" aria-labelledby="purchases-tab">
                        <div class="products-layout-grid"
                            style="align-items: start; margin-top: 1rem;">
                            <aside class="products-filters-column">
                                <ui:purchaseFiltersBar activeTab="purchases" />
                            </aside>
                            <section class="products-content-column"
                                style="width: 100%;">
                                <c:choose>
                                    <c:when test="${not empty purchases}">
                                        <div class="d-flex flex-column gap-3">
                                            <c:forEach items="${purchases}"
                                                var="purchase">
                                                <c:set var="pProduct"
                                                    value="${purchaseProducts[purchase.purchaseId]}" />
                                                <div class="profile-div-8">
                                                    <c:if test="${pProduct != null}">
                                                        <img src="<c:url value='/images/product/${pProduct.id}'/>"
                                                            alt=""
                                                            style="width: 60px; height: 60px; border-radius: 10px; object-fit: cover;"
                                                            onerror="this.onerror=null;this.src='data:image/svg+xml,%3Csvg xmlns=\'http://www.w3.org/2000/svg\' width=\'60\' height=\'60\' viewBox=\'0 0 60 60\'%3E%3Crect width=\'60\' height=\'60\' fill=\'%23e9e4dc\'/%3E%3Ctext x=\'30\' y=\'38\' text-anchor=\'middle\' font-size=\'22\' fill=\'%23b0a898\'%3E♪%3C/text%3E%3C/svg%3E';" />
                                                    </c:if>
                                                    <div class="profile-div-9">
                                                        <c:if
                                                            test="${pProduct != null}">
                                                            <div class="profile-div-10">
                                                                <c:out
                                                                    value="${pProduct.title}" />
                                                            </div>
                                                            <div class="profile-div-11">
                                                                <c:out
                                                                    value="${pProduct.artist}" />
                                                                ·
                                                                <ui:price
                                                                    value="${pProduct.price}" />
                                                            </div>
                                                        </c:if>
                                                        <div class="profile-div-12">
                                                            <c:out
                                                                value="${purchase.date}" />
                                                            ·
                                                            <span
                                                                class="profile-span-13">
                                                                <spring:message
                                                                    code="PurchaseStatus.${purchase.status}" />
                                                            </span>
                                                        </div>
                                                    </div>
                                                    <div class="profile-div-14">
                                                        <a href="<c:url value='/purchases/${purchase.purchaseId}'/>"
                                                            class="btn btn-retro btn-retro-secondary"
                                                            style="font-size: 0.8rem; padding: 0.4rem 0.8rem;">
                                                            <i class="bi bi-eye"
                                                                aria-hidden="true"></i>
                                                            <spring:message
                                                                code="Profile.reports.view" />
                                                        </a>
                                                        <c:if
                                                            test="${purchase.status eq 'DELIVERED' and not purchaseHasReview[purchase.purchaseId]}">
                                                            <a href="<c:url value='/purchases/${purchase.purchaseId}/review'/>"
                                                                class="btn btn-retro btn-retro-primary"
                                                                style="font-size: 0.8rem; padding: 0.4rem 0.8rem;">
                                                                <i class="bi bi-star"
                                                                    aria-hidden="true"></i>
                                                                <spring:message
                                                                    code="PurchasePanel.buyer.delivered.review" />
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
                                            <i class="bi bi-bag profile-i-4"></i>
                                            <p class="profile-p-5">
                                                <spring:message
                                                    code="Profile.purchases.empty" />
                                            </p>
                                            <a href="<c:url value='/'/>"
                                                class="btn btn-retro btn-retro-primary"
                                                style="justify-self: center;">
                                                <i class="bi bi-search"
                                                    aria-hidden="true"></i>
                                                <spring:message
                                                    code="Profile.purchases.explore" />
                                            </a>
                                        </div>
                                    </c:otherwise>
                                </c:choose>
                            </section>
                        </div>
                    </div>
                </c:if>

                <!-- Tab: Mis ventas (only own profile) -->
                <c:if test="${isOwnProfile}">
                    <div class="tab-pane fade<c:if test='${activeSales}'> show active</c:if>"
                        id="sales" role="tabpanel" aria-labelledby="sales-tab">
                        <div class="products-layout-grid"
                            style="align-items: start; margin-top: 1rem;">
                            <aside class="products-filters-column">
                                <ui:purchaseFiltersBar activeTab="sales" />
                            </aside>
                            <section class="products-content-column"
                                style="width: 100%;">
                                <c:choose>
                                    <c:when test="${not empty sales}">
                                        <div class="d-flex flex-column gap-3">
                                            <c:forEach items="${sales}" var="sale">
                                                <c:set var="sProduct"
                                                    value="${saleProducts[sale.purchaseId]}" />
                                                <div class="profile-div-8">
                                                    <c:if test="${sProduct != null}">
                                                        <img src="<c:url value='/images/product/${sProduct.id}'/>"
                                                            alt=""
                                                            style="width: 60px; height: 60px; border-radius: 10px; object-fit: cover;"
                                                            onerror="this.onerror=null;this.src='data:image/svg+xml,%3Csvg xmlns=\'http://www.w3.org/2000/svg\' width=\'60\' height=\'60\' viewBox=\'0 0 60 60\'%3E%3Crect width=\'60\' height=\'60\' fill=\'%23e9e4dc\'/%3E%3Ctext x=\'30\' y=\'38\' text-anchor=\'middle\' font-size=\'22\' fill=\'%23b0a898\'%3E♪%3C/text%3E%3C/svg%3E';" />
                                                    </c:if>
                                                    <div class="profile-div-9">
                                                        <c:if
                                                            test="${sProduct != null}">
                                                            <div class="profile-div-10">
                                                                <c:out
                                                                    value="${sProduct.title}" />
                                                            </div>
                                                            <div class="profile-div-11">
                                                                <c:out
                                                                    value="${sProduct.artist}" />
                                                                ·
                                                                <ui:price
                                                                    value="${sProduct.price}" />
                                                            </div>
                                                        </c:if>
                                                        <div class="profile-div-12">
                                                            <c:out
                                                                value="${sale.date}" />
                                                            ·
                                                            <span
                                                                class="profile-span-13">
                                                                <spring:message
                                                                    code="PurchaseStatus.${sale.status}" />
                                                            </span>
                                                        </div>
                                                    </div>
                                                    <div class="profile-div-14">
                                                        <a href="<c:url value='/purchases/${sale.purchaseId}'/>"
                                                            class="btn btn-retro btn-retro-secondary"
                                                            style="font-size: 0.8rem; padding: 0.4rem 0.8rem;">
                                                            <i class="bi bi-eye"
                                                                aria-hidden="true"></i>
                                                            <spring:message
                                                                code="Profile.reports.view" />
                                                        </a>
                                                    </div>
                                                </div>
                                            </c:forEach>
                                        </div>
                                        <ui:pagination result="${salesPage}" />
                                    </c:when>
                                    <c:otherwise>
                                        <div class="empty-products-state">
                                            <i class="bi bi-shop profile-i-4"></i>
                                            <p class="profile-p-5">
                                                <spring:message
                                                    code="Profile.sales.empty" />
                                            </p>
                                            <a href="<c:url value='/products/new'/>"
                                                class="btn btn-retro btn-retro-primary"
                                                style="justify-self: center;">
                                                <i class="bi bi-plus-lg"
                                                    aria-hidden="true"></i>
                                                <spring:message
                                                    code="Profile.sales.publishVinyl" />
                                            </a>
                                        </div>
                                    </c:otherwise>
                                </c:choose>
                            </section>
                        </div>
                    </div>
                </c:if>
                <!-- Tab: Reseñas recibidas -->
                <div class="tab-pane fade<c:if test='${activeReviews}'> show active</c:if>"
                    id="reviews" role="tabpanel" aria-labelledby="reviews-tab">
                    <c:choose>
                        <c:when test="${not empty receivedReviews}">
                            <div class="d-flex flex-column gap-3">
                                <c:forEach items="${receivedReviews}" var="rev">
                                    <div class="profile-div-15">
                                        <div class="profile-div-16">
                                            <div class="profile-div-17">
                                                <div class="profile-div-18">
                                                    <c:out
                                                        value="${fn:substring(rev.buyerUsername, 0, 1)}" />
                                                </div>
                                                <span class="profile-span-19">
                                                    <c:out
                                                        value="${rev.buyerUsername}" />
                                                </span>
                                            </div>
                                            <div class="profile-div-20">
                                                <c:forEach begin="1" end="5" var="i">
                                                    <c:choose>
                                                        <c:when
                                                            test="${i <= rev.score}"><i
                                                                class="bi bi-star-fill"></i>
                                                        </c:when>
                                                        <c:otherwise><i
                                                                class="bi bi-star"></i>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </c:forEach>
                                            </div>
                                        </div>
                                        <c:if test="${not empty rev.text}">
                                            <p class="profile-p-21">
                                                <c:out value="${rev.text}" />
                                            </p>
                                        </c:if>
                                    </div>
                                </c:forEach>
                            </div>
                            <ui:pagination result="${receivedReviewsPage}" />
                        </c:when>
                        <c:otherwise>
                            <div class="empty-products-state">
                                <i class="bi bi-star profile-i-4"></i>
                                <p class="profile-p-5">
                                    <c:choose>
                                        <c:when test="${isOwnProfile}">
                                            <spring:message
                                                code="Profile.reviews.empty.own" />
                                        </c:when>
                                        <c:otherwise>
                                            <spring:message
                                                code="Profile.reviews.empty.other" />
                                        </c:otherwise>
                                    </c:choose>
                                </p>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>

                <div class="modal fade profile-modal" id="followersModal" tabindex="-1"
                    aria-labelledby="followersModalLabel" aria-hidden="true"
                    data-follow-list="followers"
                    data-follow-search-url="<c:url value='/profile/followers/search' />"
                    data-follow-target-id="${user.id}">
                    <div class="modal-dialog modal-dialog-centered modal-lg">
                        <div class="modal-content">
                            <div class="modal-header">
                                <h5 class="modal-title" id="followersModalLabel">
                                    <spring:message code="Profile.tabs.followers" />
                                </h5>
                                <button type="button" class="btn-close"
                                    data-bs-dismiss="modal" aria-label="Close"></button>
                            </div>
                            <div class="modal-body">
                                <div class="mb-3">
                                    <spring:message code="FollowModal.search.placeholder" var="searchPlaceholder" />
                                    <input type="text"
                                        class="form-control follow-modal-search-input"
                                        placeholder="${searchPlaceholder}"
                                        aria-label="${searchPlaceholder}" />
                                </div>
                                <div class="d-flex flex-column gap-3 follow-modal-list">
                                    <c:choose>
                                        <c:when test="${not empty followers}">
                                            <c:forEach items="${followers}" var="followerUser">
                                                <div class="user-card-row" data-user-id="${followerUser.id}">
                                                    <a href="<c:url value='/profile?userId=${followerUser.id}'/>" class="user-card-link">
                                                        <div class="user-card-avatar">
                                                            <c:out value="${fn:substring(followerUser.username, 0, 1)}" />
                                                        </div>
                                                        <div class="user-card-info">
                                                            <div class="user-card-username">
                                                                <c:out value="${followerUser.username}" />
                                                            </div>
                                                            <c:if test="${not empty followerUser.firstName or not empty followerUser.lastName}">
                                                                <div class="user-card-name">
                                                                    <c:out value="${followerUser.firstName}" />
                                                                    <c:out value="${followerUser.lastName}" />
                                                                </div>
                                                            </c:if>
                                                        </div>
                                                    </a>
                                                    <sec:authorize access="isAuthenticated()">
                                                        <sec:authentication property="principal.user.id" var="currentUserId" />
                                                        <c:if test="${followerUser.id != currentUserId}">
                                                            <form action="<c:url value='/profile/follow' />" method="post">
                                                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
                                                                <input type="hidden" name="userId" value="${followerUser.id}" />
                                                                <c:choose>
                                                                    <c:when test="${followStatusMap[followerUser.id]}">
                                                                        <button type="submit" class="btn btn-retro btn-retro-secondary btn-follow-sm">
                                                                            <spring:message code="Profile.unfollow" />
                                                                        </button>
                                                                    </c:when>
                                                                    <c:otherwise>
                                                                        <button type="submit" class="btn btn-retro btn-retro-primary btn-follow-sm">
                                                                            <spring:message code="Profile.follow" />
                                                                        </button>
                                                                    </c:otherwise>
                                                                </c:choose>
                                                            </form>
                                                        </c:if>
                                                    </sec:authorize>
                                                </div>
                                            </c:forEach>
                                        </c:when>
                                        <c:otherwise>
                                            <div class="empty-products-state follow-modal-empty">
                                                <i class="bi bi-people profile-i-4"></i>
                                                <p class="profile-p-5">
                                                    <spring:message code="Profile.followers.empty" />
                                                </p>
                                            </div>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                                <div class="follow-modal-pagination">
                                    <ui:pagination result="${followersPage}" />
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="modal fade profile-modal" id="followingModal" tabindex="-1"
                    aria-labelledby="followingModalLabel" aria-hidden="true"
                    data-follow-list="following"
                    data-follow-search-url="<c:url value='/profile/following/search' />"
                    data-follow-target-id="${user.id}">
                    <div class="modal-dialog modal-dialog-centered modal-lg">
                        <div class="modal-content">
                            <div class="modal-header">
                                <h5 class="modal-title" id="followingModalLabel">
                                    <spring:message code="Profile.tabs.following" />
                                </h5>
                                <button type="button" class="btn-close"
                                    data-bs-dismiss="modal" aria-label="Close"></button>
                            </div>
                            <div class="modal-body">
                                <div class="mb-3">
                                    <spring:message code="FollowModal.search.placeholder" var="searchPlaceholder" />
                                    <input type="text"
                                        class="form-control follow-modal-search-input"
                                        placeholder="${searchPlaceholder}"
                                        aria-label="${searchPlaceholder}" />
                                </div>
                                <div class="d-flex flex-column gap-3 follow-modal-list">
                                    <c:choose>
                                        <c:when test="${not empty followingUsers}">
                                            <c:forEach items="${followingUsers}" var="followedUser">
                                                <div class="user-card-row" data-user-id="${followedUser.id}">
                                                    <a href="<c:url value='/profile?userId=${followedUser.id}'/>" class="user-card-link">
                                                        <div class="user-card-avatar">
                                                            <c:out value="${fn:substring(followedUser.username, 0, 1)}" />
                                                        </div>
                                                        <div class="user-card-info">
                                                            <div class="user-card-username">
                                                                <c:out value="${followedUser.username}" />
                                                            </div>
                                                            <c:if test="${not empty followedUser.firstName or not empty followedUser.lastName}">
                                                                <div class="user-card-name">
                                                                    <c:out value="${followedUser.firstName}" />
                                                                    <c:out value="${followedUser.lastName}" />
                                                                </div>
                                                            </c:if>
                                                        </div>
                                                    </a>
                                                    <sec:authorize access="isAuthenticated()">
                                                        <sec:authentication property="principal.user.id" var="currentUserId" />
                                                        <c:if test="${followedUser.id != currentUserId}">
                                                            <form action="<c:url value='/profile/follow' />" method="post">
                                                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
                                                                <input type="hidden" name="userId" value="${followedUser.id}" />
                                                                <c:choose>
                                                                    <c:when test="${followStatusMap[followedUser.id]}">
                                                                        <button type="submit" class="btn btn-retro btn-retro-secondary btn-follow-sm">
                                                                            <spring:message code="Profile.unfollow" />
                                                                        </button>
                                                                    </c:when>
                                                                    <c:otherwise>
                                                                        <button type="submit" class="btn btn-retro btn-retro-primary btn-follow-sm">
                                                                            <spring:message code="Profile.follow" />
                                                                        </button>
                                                                    </c:otherwise>
                                                                </c:choose>
                                                            </form>
                                                        </c:if>
                                                    </sec:authorize>
                                                </div>
                                            </c:forEach>
                                        </c:when>
                                        <c:otherwise>
                                            <div class="empty-products-state follow-modal-empty">
                                                <i class="bi bi-person-check profile-i-4"></i>
                                                <p class="profile-p-5">
                                                    <spring:message code="Profile.following.empty" />
                                                </p>
                                            </div>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                                <div class="follow-modal-pagination">
                                    <ui:pagination result="${followingPage}" />
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                
                <!-- Tab: Papelera (solo perfil propio) -->
                <c:if test="${isOwnProfile}">
                    <div class="tab-pane fade<c:if test='${activeTrash}'> show active</c:if>"
                        id="trash" role="tabpanel" aria-labelledby="trash-tab">
                <c:if test="${restored}">
                    <div class="alert-retro alert-retro-success mb-3"
                        role="alert">
                        <i class="bi bi-check-circle" aria-hidden="true"></i>
                        <spring:message code="Trash.alert.restored" />
                    </div>
                </c:if>
                <c:if test="${restoreError}">
                    <div class="alert-retro alert-retro-warning mb-3"
                        role="alert">
                        <i class="bi bi-exclamation-triangle"
                            aria-hidden="true"></i>
                        <spring:message code="Trash.alert.restoreError" />
                    </div>
                </c:if>
                        <h2 class="h5 mb-3 profile-h2-22">
                            <i class="bi bi-trash3" aria-hidden="true"></i>
                            <spring:message code="Trash.heading" />
                        </h2>
                        <c:choose>
                            <c:when test="${not empty deletedProducts}">
                                <div class="profile-publications-stack deleted-products-stack">
                                    <div class="products-grid profile-listings-grid">
                                        <c:forEach items="${deletedProducts}" var="product">
                                            <div class="products-grid-item">
                                                <ui:productCard title="${product.title}"
                                                    artist="${product.artist}"
                                                    price="${product.price}"
                                                    installments="${product.installmentPrice}"
                                                    imageUrl="${deletedProductImageUrls[product.id]}"
                                                    categories="${product.categories}"
                                                    sellerRating="${sellerRating}"
                                                    href="#" linkDisabled="true" />

                                                <c:url var="restoreProductUrl"
                                                    value="/products/${product.id}/restore" />

                                                <spring:message
                                                    code="Trash.restore.confirm"
                                                    var="confirmRestore" />
                                                    
                                                <form action="${restoreProductUrl}"
                                                    method="post"
                                                    class="mt-2 flex-shrink-0"
                                                    onsubmit="return showConfirmationModal(event);">
                                                    <input type="hidden"
                                                        name="${_csrf.parameterName}"
                                                        value="${_csrf.token}" />
                                                    <button type="submit"
                                                        class="btn btn-retro btn-retro-primary w-100 profile-button-3">
                                                        <i class="bi bi-arrow-counterclockwise"
                                                            aria-hidden="true"></i>
                                                        <spring:message
                                                            code="Trash.restore.button" />
                                                    </button>
                                                </form>
                                            </div>
                                        </c:forEach>
                                    </div>
                                    <ui:pagination result="${deletedProductsPage}"
                                        pageParamName="trashPage" />
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="empty-products-state profile-div-23">
                                    <i class="bi bi-trash3 profile-i-4"></i>
                                    <p class="profile-p-24">
                                        <spring:message code="Trash.empty" />
                                    </p>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </c:if>

                <!-- Tab: Reportes (solo moderadores en perfil propio) -->
                <c:if test="${isOwnProfile and isAdmin}">
                    <div class="tab-pane fade<c:if test='${activeReports}'> show active</c:if>"
                        id="reports" role="tabpanel" aria-labelledby="reports-tab">
                        <c:choose>
                            <c:when test="${not empty reportedProducts}">
                                <div class="d-flex flex-column gap-3">
                                    <c:forEach items="${reportedProducts}" var="rp">
                                        <div class="profile-div-8">
                                            <img src="<c:url value='/images/product/${rp.productId}'/>"
                                                alt=""
                                                style="width: 70px; height: 70px; border-radius: 12px; object-fit: cover; flex-shrink: 0;"
                                                onerror="this.onerror=null;this.src='data:image/svg+xml,%3Csvg xmlns=\'http://www.w3.org/2000/svg\' width=\'70\' height=\'70\' viewBox=\'0 0 70 70\'%3E%3Crect width=\'70\' height=\'70\' fill=\'%23e9e4dc\'/%3E%3Ctext x=\'35\' y=\'44\' text-anchor=\'middle\' font-size=\'26\' fill=\'%23b0a898\'%3E&#9834;%3C/text%3E%3C/svg%3E';" />
                                            <div class="profile-div-9">
                                                <div class="profile-div-10">
                                                    <c:out value="${rp.productTitle}" />
                                                </div>
                                                <div class="profile-div-11">
                                                    <c:out
                                                        value="${rp.productArtist}" />
                                                </div>
                                                <div class="profile-div-25">
                                                    <span class="profile-span-26">
                                                        <i class="bi bi-flag-fill"
                                                            aria-hidden="true"></i>
                                                        <spring:message
                                                            code="Profile.reports.count">
                                                            <spring:argument
                                                                value="${rp.reportCount}" />
                                                            <spring:argument
                                                                value="${rp.reportCount}" />
                                                        </spring:message>
                                                    </span>
                                                    <span class="profile-span-27">
                                                        <spring:message
                                                            code="Profile.reports.publishedBy" />
                                                        <a href="<c:url value='/profile?userId=${rp.ownerUserId}'/>"
                                                            style="color: var(--color-accent); text-decoration: none; font-weight: 600;">
                                                            <c:out
                                                                value="${rp.ownerUsername}" />
                                                        </a>
                                                    </span>
                                                </div>
                                            </div>
                                            <div class="profile-div-28">
                                                <spring:message
                                                    code="Profile.reports.view"
                                                    var="viewText" />
                                                <a href="<c:url value='/products/${rp.productId}'/>"
                                                    class="btn btn-retro btn-retro-secondary"
                                                    style="font-size: 0.78rem; padding: 0.35rem 0.7rem;"
                                                    title="${viewText}">
                                                    <i class="bi bi-eye"
                                                        aria-hidden="true"></i>
                                                    <c:out value="${viewText}" />
                                                </a>
                                                <c:url var="hideUrl"
                                                    value="/profile/admin/hide-product" />
                                                <spring:message
                                                    code="Profile.reports.hideConfirm"
                                                    var="confirmHide" />
                                                <spring:message
                                                    code="Profile.reports.hide"
                                                    var="hideText" />
                                                <button type="button"
                                                    data-bs-toggle="modal"
                                                    data-bs-target="#hideModal-${rp.productId}"
                                                    class="btn btn-retro btn-retro-danger-outline"
                                                    style="font-size: 0.78rem; padding: 0.35rem 0.7rem;"
                                                    title="${hideText}">
                                                    <i class="bi bi-x-circle"
                                                        aria-hidden="true"></i>
                                                    <c:out value="${hideText}" />
                                                </button>

                                                <spring:message
                                                    code="Profile.reports.hideConfirm"
                                                    var="hideConfirmText" />
                                                <spring:message code="Global.cancel"
                                                    var="cancelText" />
                                                <ui:confirmFormModal
                                                    id="hideModal-${rp.productId}"
                                                    title="${hideText}"
                                                    message="${hideConfirmText}"
                                                    confirmBtnText="${hideText}"
                                                    cancelBtnText="${cancelText}"
                                                    actionUrl="${hideUrl}"
                                                    hiddenInputName="productId"
                                                    hiddenInputValue="${rp.productId}"
                                                    confirmBtnClass="btn-retro-danger" />
                                                <spring:message
                                                    code="Profile.reports.profile"
                                                    var="profileText" />
                                                <a href="<c:url value='/profile?userId=${rp.ownerUserId}'/>"
                                                    class="btn btn-retro btn-retro-secondary"
                                                    style="font-size: 0.78rem; padding: 0.35rem 0.7rem;"
                                                    title="${profileText}">
                                                    <i class="bi bi-person"
                                                        aria-hidden="true"></i>
                                                    <c:out value="${profileText}" />
                                                </a>
                                                <c:url var="banUrl"
                                                    value="/profile/admin/ban-user" />
                                                <spring:message
                                                    code="Profile.reports.banConfirm"
                                                    var="confirmBan" />
                                                <spring:message
                                                    code="Profile.reports.ban"
                                                    var="banText" />
                                                <button type="button"
                                                    data-bs-toggle="modal"
                                                    data-bs-target="#banModal-${rp.productId}"
                                                    class="btn btn-retro btn-retro-danger"
                                                    style="font-size: 0.78rem; padding: 0.35rem 0.7rem;"
                                                    title="${banText}">
                                                    <i class="bi bi-person-x"
                                                        aria-hidden="true"></i>
                                                    <c:out value="${banText}" />
                                                </button>

                                                <spring:message
                                                    code="Profile.reports.banConfirm"
                                                    var="banConfirmText" />
                                                <ui:confirmFormModal
                                                    id="banModal-${rp.productId}"
                                                    title="${banText}"
                                                    message="${banConfirmText}"
                                                    confirmBtnText="${banText}"
                                                    cancelBtnText="${cancelText}"
                                                    actionUrl="${banUrl}"
                                                    hiddenInputName="userId"
                                                    hiddenInputValue="${rp.ownerUserId}"
                                                    confirmBtnClass="btn-retro-danger" />
                                            </div>
                                        </div>
                                    </c:forEach>
                                </div>
                                <ui:pagination result="${reportsPage}" />
                            </c:when>
                            <c:otherwise>
                                <div class="empty-products-state">
                                    <i class="bi bi-flag profile-i-4"></i>
                                    <p class="profile-p-5">
                                        <spring:message code="Profile.reports.empty" />
                                    </p>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </c:if>
            </div>
        </div>
    </div>
    <meta name="is-authenticated" content="<sec:authorize access='isAuthenticated()'>true</sec:authorize><sec:authorize access='!isAuthenticated()'>false</sec:authorize>" />
    <meta name="current-user-id" content="<sec:authorize access='isAuthenticated()'><sec:authentication property='principal.user.id' /></sec:authorize><sec:authorize access='!isAuthenticated()'>-1</sec:authorize>" />
    <script>window.contextPath = '<c:out value="${pageContext.request.contextPath}" />';</script>
    <script src="<c:url value="/assets/js/profile-follow-modals.js" />"></script>
    <script>
        (function () {

           let pendingForm = null;

            window.showConfirmationModal = function(event) {
                event.preventDefault();

                pendingForm = event.target;

                const modalEl = document.getElementById('confirmationModal');

                if (modalEl) {
                    const modal = new bootstrap.Modal(modalEl);
                    modal.show();
                }

                return false;
            };

            document.addEventListener("DOMContentLoaded", function () {

                const confirmBtn = document.getElementById("confirmRemoveBtn");

                if (confirmBtn) {
                    confirmBtn.addEventListener("click", function () {
                        if (pendingForm) {
                            pendingForm.submit();
                        }
                    });
                }
            });

            var form = document.getElementById('profileForm');
            var saveBtn = document.getElementById('profileSaveBtn');
            if (!form || !saveBtn) return;


            function getSerializedState() {
                var formData = new FormData(form);
                var params = new URLSearchParams();
                Array.from(formData.entries()).sort().forEach(function (pair) {
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
            form.addEventListener('input', function (e) {
                if (e.target.tagName === 'INPUT' || e.target.tagName === 'TEXTAREA' || e.target.tagName === 'SELECT') {
                    checkChanges();
                }
            });
        })();
    </script>
</ui:layout>
