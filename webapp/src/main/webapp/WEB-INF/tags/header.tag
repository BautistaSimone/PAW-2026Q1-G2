<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix = "spring" uri = "http://www.springframework.org/tags" %>

<%@ attribute name="showHeaderActions" required="false" type="java.lang.Boolean" %>
<%@ attribute name="searchMode" required="false" type="java.lang.String" %>
<%@ attribute name="searchValue" required="false" type="java.lang.String" %>
<%@ attribute name="activeSection" required="false" type="java.lang.String" %>

<c:set var="activeSearchMode" value="${not empty searchMode ? searchMode : 'vinyls'}" />
<c:set var="headerSearchText" value="${param['search-text']}" />
<c:if test="${activeSearchMode eq 'users' and not empty searchValue}">
    <c:set var="headerSearchText" value="${searchValue}" />
</c:if>

<spring:message code="Header.search.placeholder.vinyls" var="vinylsPlaceholder" />
<spring:message code="Header.search.placeholder.users" var="usersPlaceholder" />
<spring:message code="Header.search.mode.vinyls" var="modeVinyls" />
<spring:message code="Header.search.mode.users" var="modeUsers" />

<header class="header-bbdiscos">

    <div class="header-content">
        <div class="header-left">
            <a href="<c:url value="/"/>" class="header-logo-link" aria-label="<spring:message code='Header.logo.ariaLabel' />">
                <img src="<c:url value="/assets/images/vinyl_disk.png"/>" alt="<spring:message code='Header.logo.alt'/>" class="header-logo-img" width="44" height="44" decoding="async" />
                <span class="brand-name"><spring:message code="Global.brand"/></span>
            </a>
        </div>

        <c:if test="${showHeaderActions != false}">
            <div class="header-center">
                <div class="search-container">
                    <form class="search-form" id="unified-search-form" method="get" novalidate
                          action="<c:url value="${activeSearchMode eq 'users' ? '/search-users' : '/'}"/>"
                          data-vinyls-action="<c:url value='/'/>"
                          data-users-action="<c:url value='/search-users'/>"
                          data-vinyls-param="search-text"
                          data-users-param="q">
                        <input id="search-input" name="${activeSearchMode eq 'users' ? 'q' : 'search-text'}"
                            class="search-input"
                            type="text"
                            placeholder="${activeSearchMode eq 'users' ? usersPlaceholder : vinylsPlaceholder}"
                            aria-label="<spring:message code='Header.search.ariaLabel' />"
                            data-placeholder-vinyls="${vinylsPlaceholder}"
                            data-placeholder-users="${usersPlaceholder}"
                            value="<c:out value='${headerSearchText}' />">
                        <div class="search-mode-group">
                            <button id="search-mode-toggle" class="search-mode-btn" type="button"
                                    aria-haspopup="listbox" aria-expanded="false"
                                    aria-label="<spring:message code='Header.search.mode.ariaLabel' />">
                                <span id="search-mode-label"><c:out value="${activeSearchMode eq 'users' ? modeUsers : modeVinyls}" /></span>
                                <i class="bi bi-chevron-down search-mode-chevron" aria-hidden="true"></i>
                            </button>
                            <ul id="search-mode-menu" class="search-mode-dropdown" role="listbox" aria-hidden="true">
                                <li role="option" data-mode="vinyls" class="search-mode-option ${activeSearchMode eq 'vinyls' ? 'is-selected' : ''}">
                                    <i class="bi bi-vinyl" aria-hidden="true"></i>
                                    <c:out value="${modeVinyls}" />
                                </li>
                                <li role="option" data-mode="users" class="search-mode-option ${activeSearchMode eq 'users' ? 'is-selected' : ''}">
                                    <i class="bi bi-people" aria-hidden="true"></i>
                                    <c:out value="${modeUsers}" />
                                </li>
                            </ul>
                            <button id="search-button" class="search-btn" type="submit" aria-label="<spring:message code='Header.search.button.ariaLabel' />">
                                <i class="bi bi-search" aria-hidden="true"></i>
                            </button>
                        </div>
                    </form>
                </div>
            </div>

            <div class="header-right">
                <nav class="header-nav-links">
                    <a href="<c:url value='/'/>" class="header-nav-link ${activeSection eq 'vinyls' ? 'is-active' : ''}" aria-label="<spring:message code='Header.nav.vinyls.ariaLabel' />">
                        <i class="bi bi-vinyl" aria-hidden="true"></i>
                        <span><spring:message code="Header.nav.vinyls" /></span>
                    </a>
                    <a href="<c:url value='/search-users'/>" class="header-nav-link ${activeSection eq 'community' ? 'is-active' : ''}" aria-label="<spring:message code='Header.searchUsers.ariaLabel' />">
                        <i class="bi bi-people" aria-hidden="true"></i>
                        <span><spring:message code="Header.searchUsers" /></span>
                    </a>
                    <sec:authorize access="isAuthenticated()">
                        <a href="<c:url value='/for-you'/>" class="header-nav-link ${activeSection eq 'forYou' ? 'is-active' : ''}" aria-label="<spring:message code='Header.forYou.ariaLabel' />">
                            <i class="bi bi-heart" aria-hidden="true"></i>
                            <span><spring:message code="Header.forYou" /></span>
                        </a>
                    </sec:authorize>
                </nav>

                <sec:authorize access="isAuthenticated()">
                    <div class="notifications-wrapper">
                        <spring:message code="Header.notifications.ariaLabel" var="notificationsAriaLabel" />
                        <button type="button" class="notifications-btn" id="notificationsToggle" aria-label="<c:out value='${notificationsAriaLabel}'/>">
                            <i class="bi bi-bell" aria-hidden="true"></i>
                            <c:if test="${notificationPanelUnreadCount > 0}">
                                <span class="notifications-badge"><c:out value="${notificationPanelUnreadCount}" /></span>
                            </c:if>
                        </button>

                        <div class="notifications-panel" id="notificationsPanel" aria-hidden="true">
                            <div class="notifications-panel-header">
                                <span class="notifications-title"><spring:message code="Notifications.title" /></span>
                                <c:if test="${notificationPanelUnreadCount > 0}">
                                    <form method="post" action="<c:url value='/notifications/read-all'/>" class="notifications-markall-form">
                                        <input type="hidden" name="<c:out value='${_csrf.parameterName}'/>" value="<c:out value='${_csrf.token}'/>" />
                                        <button type="submit" class="notifications-markall-btn">
                                            <spring:message code="Notifications.markAll" />
                                        </button>
                                    </form>
                                </c:if>
                            </div>

                            <div class="notifications-filters">
                                <c:set var="notifFilterAllClass" value="notifications-filter ${notificationPanelFilter eq 'ALL' ? 'is-active' : ''}" />
                                <button type="button" class="<c:out value='${notifFilterAllClass}'/>" data-notif-filter="ALL">
                                    <spring:message code="Notifications.filter.all" />
                                </button>
                                <c:set var="notifFilterFollowClass" value="notifications-filter ${notificationPanelFilter eq 'FOLLOW' ? 'is-active' : ''}" />
                                <button type="button" class="<c:out value='${notifFilterFollowClass}'/>" data-notif-filter="FOLLOW">
                                    <spring:message code="Notifications.filter.follow" />
                                </button>
                                <c:set var="notifFilterNewProductClass" value="notifications-filter ${notificationPanelFilter eq 'NEW_PRODUCT' ? 'is-active' : ''}" />
                                <button type="button" class="<c:out value='${notifFilterNewProductClass}'/>" data-notif-filter="NEW_PRODUCT">
                                    <spring:message code="Notifications.filter.newProduct" />
                                </button>
                                <c:set var="notifFilterPurchaseClass" value="notifications-filter ${notificationPanelFilter eq 'PURCHASE_STATUS' ? 'is-active' : ''}" />
                                <button type="button" class="<c:out value='${notifFilterPurchaseClass}'/>" data-notif-filter="PURCHASE_STATUS">
                                    <spring:message code="Notifications.filter.purchase" />
                                </button>
                                <c:set var="notifFilterReviewClass" value="notifications-filter ${notificationPanelFilter eq 'REVIEW_RECEIVED' ? 'is-active' : ''}" />
                                <button type="button" class="<c:out value='${notifFilterReviewClass}'/>" data-notif-filter="REVIEW_RECEIVED">
                                    <spring:message code="Notifications.filter.review" />
                                </button>
                            </div>

                            <div class="notifications-list">
                                <c:choose>
                                    <c:when test="${not empty notificationPanelNotifications}">
                                        <c:forEach items="${notificationPanelNotifications}" var="notification">
                                            <c:set var="actorUser" value="${notificationPanelUsersById[notification.actorUserId]}" />
                                            <c:set var="product" value="${notificationPanelProductsById[notification.productId]}" />

                                            <c:set var="notificationItemClass" value="notification-item ${notification.readAt == null ? 'is-unread' : ''}" />
                                            <div class="<c:out value='${notificationItemClass}'/>">
                                                <div class="notification-text">
                                                    <c:choose>
                                                        <c:when test="${notification.type == 'FOLLOW'}">
                                                            <span class="notification-actor">
                                                                <c:choose>
                                                                    <c:when test="${actorUser != null}">
                                                                        <c:out value="${actorUser.username}" />
                                                                    </c:when>
                                                                    <c:otherwise>
                                                                        <spring:message code="Notifications.actor.unknown" />
                                                                    </c:otherwise>
                                                                </c:choose>
                                                            </span>
                                                            <spring:message code="Notifications.item.follow" />
                                                        </c:when>
                                                        <c:when test="${notification.type == 'NEW_PRODUCT'}">
                                                            <span class="notification-actor">
                                                                <c:choose>
                                                                    <c:when test="${actorUser != null}">
                                                                        <c:out value="${actorUser.username}" />
                                                                    </c:when>
                                                                    <c:otherwise>
                                                                        <spring:message code="Notifications.actor.unknown" />
                                                                    </c:otherwise>
                                                                </c:choose>
                                                            </span>
                                                            <spring:message code="Notifications.item.newProduct" />
                                                            <span class="notification-product">
                                                                <c:choose>
                                                                    <c:when test="${product != null}">
                                                                        <c:out value="${product.title}" />
                                                                    </c:when>
                                                                    <c:otherwise>
                                                                        <spring:message code="Notifications.product.unknown" />
                                                                    </c:otherwise>
                                                                </c:choose>
                                                            </span>
                                                        </c:when>
                                                        <c:when test="${notification.type == 'PURCHASE_STATUS'}">
                                                            <span class="notification-actor">
                                                                <c:choose>
                                                                    <c:when test="${actorUser != null}">
                                                                        <c:out value="${actorUser.username}" />
                                                                    </c:when>
                                                                    <c:otherwise>
                                                                        <spring:message code="Notifications.actor.system" />
                                                                    </c:otherwise>
                                                                </c:choose>
                                                            </span>
                                                            <spring:message code="Notifications.item.purchase" />
                                                            <span class="notification-status">
                                                                <c:set var="purchaseStatusKey" value="PurchaseStatus.${notification.purchaseStatus}" />
                                                                <spring:message code="${purchaseStatusKey}" />
                                                            </span>
                                                            <c:if test="${product != null}">
                                                                <spring:message code="Notifications.item.purchase.suffix" />
                                                                <span class="notification-product"><c:out value="${product.title}" /></span>
                                                            </c:if>
                                                        </c:when>
                                                        <c:when test="${notification.type == 'REVIEW_RECEIVED'}">
                                                            <span class="notification-actor">
                                                                <c:choose>
                                                                    <c:when test="${actorUser != null}">
                                                                        <c:out value="${actorUser.username}" />
                                                                    </c:when>
                                                                    <c:otherwise>
                                                                        <spring:message code="Notifications.actor.unknown" />
                                                                    </c:otherwise>
                                                                </c:choose>
                                                            </span>
                                                            <spring:message code="Notifications.item.review" />
                                                            <span class="notification-product">
                                                                <c:choose>
                                                                    <c:when test="${product != null}">
                                                                        <c:out value="${product.title}" />
                                                                    </c:when>
                                                                    <c:otherwise>
                                                                        <spring:message code="Notifications.product.unknown" />
                                                                    </c:otherwise>
                                                                </c:choose>
                                                            </span>
                                                        </c:when>
                                                    </c:choose>
                                                </div>

                                                <div class="notification-meta">
                                                    <span class="notification-time"><c:out value="${notification.createdAt}" /></span>
                                                    <c:if test="${notification.readAt == null}">
                                                        <form method="post" action="<c:url value='/notifications/read'/>" class="notification-read-form">
                                                            <input type="hidden" name="<c:out value='${_csrf.parameterName}'/>" value="<c:out value='${_csrf.token}'/>" />
                                                            <input type="hidden" name="id" value="<c:out value='${notification.notificationId}'/>" />
                                                            <button type="submit" class="notification-read-btn">
                                                                <spring:message code="Notifications.markRead" />
                                                            </button>
                                                        </form>
                                                    </c:if>
                                                </div>

                                                <div class="notification-links">
                                                    <c:if test="${notification.productId != null}">
                                                        <c:url value="/products/${notification.productId}" var="productUrl" />
                                                        <a class="notification-link" href="<c:out value='${productUrl}'/>">
                                                            <spring:message code="Notifications.link.product" />
                                                        </a>
                                                    </c:if>
                                                    <c:if test="${notification.purchaseId != null}">
                                                        <c:url value="/purchases/${notification.purchaseId}" var="purchaseUrl" />
                                                        <a class="notification-link" href="<c:out value='${purchaseUrl}'/>">
                                                            <spring:message code="Notifications.link.purchase" />
                                                        </a>
                                                    </c:if>
                                                </div>
                                            </div>
                                        </c:forEach>
                                    </c:when>
                                    <c:otherwise>
                                        <div class="notifications-empty">
                                            <spring:message code="Notifications.empty" />
                                        </div>
                                    </c:otherwise>
                                </c:choose>
                            </div>

                            <c:if test="${notificationPanelPage.hasNextPage}">
                                <button type="button" class="notifications-more" data-notif-page="<c:out value='${notificationPanelPage.currentPage + 1}'/>">
                                    <spring:message code="Notifications.loadMore" />
                                </button>
                            </c:if>
                        </div>
                    </div>

                    <a href="<c:url value='/profile'/>" class="profile-btn" aria-label="<spring:message code='Header.profile.ariaLabel' />">
                        <i class="bi bi-person-fill" aria-hidden="true"></i>
                        <span><sec:authentication property="principal.user.username" /></span>
                    </a>
                </sec:authorize>

                <sec:authorize access="!isAuthenticated()">
                    <div class="header-auth-actions">
                        <a href="<c:url value='/login'/>" class="profile-btn" aria-label="<spring:message code='Header.login.button' />">
                            <i class="bi bi-box-arrow-in-right" aria-hidden="true"></i>
                            <span><spring:message code="Header.login.button" /></span>
                        </a>
                        <a href="<c:url value='/register'/>" class="profile-btn profile-btn-primary" aria-label="<spring:message code='Header.register.button' />">
                            <i class="bi bi-person-plus-fill" aria-hidden="true"></i>
                            <span><spring:message code="Header.register.button" /></span>
                        </a>
                    </div>
                </sec:authorize>
            </div>
        </c:if>
    </div>

</header>

<!-- Messages -->
<c:if test="${message != null}">
    <div class="container mt-3">
        <div class="alert-retro alert-retro-info text-break d-flex align-items-start" role="alert">
            <div>
                <spring:message code="${message}" />
            </div>
        </div>
    </div>
</c:if>

<!-- Scripts -->
<script src="<c:url value="/assets/js/header.js"/>"></script>
