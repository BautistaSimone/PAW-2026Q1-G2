<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<spring:message code="SearchUsers.title" var="searchUsersTitle" />
<ui:layout title="${searchUsersTitle}">

    <ui:header showHeaderActions="true" />

    <div class="products-section">
        <div class="container-fluid products-shell">

            <div class="search-users-header">
                <h1 class="search-users-heading">
                    <i class="bi bi-people" aria-hidden="true"></i>
                    <spring:message code="SearchUsers.heading" />
                </h1>

                <form class="search-users-form" method="get" action="<c:url value='/search-users'/>" novalidate>
                    <spring:message code="SearchUsers.placeholder" var="searchPlaceholder" />
                    <div class="search-users-input-group">
                        <input name="q" type="text" class="form-control search-users-input"
                            placeholder="${searchPlaceholder}"
                            value="<c:out value='${searchQuery}' />"
                            aria-label="${searchPlaceholder}" />
                        <button type="submit" class="btn btn-retro btn-retro-primary">
                            <i class="bi bi-search" aria-hidden="true"></i>
                            <spring:message code="SearchUsers.button" />
                        </button>
                    </div>
                </form>
            </div>

            <c:if test="${not showingSearchResults and not empty users}">
                <h2 class="search-users-section-title">
                    <spring:message code="SearchUsers.mostFollowed" />
                </h2>
            </c:if>

            <c:if test="${showingSearchResults}">
                <h2 class="search-users-section-title">
                    <spring:message code="SearchUsers.resultsFor" />
                    "<c:out value='${searchQuery}' />"
                </h2>
            </c:if>

            <c:choose>
                <c:when test="${not empty users}">
                    <div class="search-users-grid">
                        <c:forEach items="${users}" var="u">
                            <div class="search-user-card">
                                <a href="<c:url value='/profile?userId=${u.id}'/>" class="search-user-card-link">
                                    <div class="search-user-card-avatar">
                                        <c:out value="${fn:substring(u.username, 0, 1)}" />
                                    </div>
                                    <div class="search-user-card-info">
                                        <div class="search-user-card-username">
                                            <c:out value="${u.username}" />
                                        </div>
                                        <c:if test="${not empty u.firstName or not empty u.lastName}">
                                            <div class="search-user-card-name">
                                                <c:out value="${u.firstName}" />
                                                <c:out value="${u.lastName}" />
                                            </div>
                                        </c:if>
                                        <div class="search-user-card-followers">
                                            <i class="bi bi-people" aria-hidden="true"></i>
                                            <c:out value="${userFollowerCounts[u.id]}" />
                                            <spring:message code="Profile.followers" />
                                        </div>
                                    </div>
                                </a>
                                <sec:authorize access="isAuthenticated()">
                                    <c:if test="${u.id != currentUserId}">
                                        <form action="<c:url value='/profile/follow' />" method="post" class="search-user-card-action">
                                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
                                            <input type="hidden" name="userId" value="${u.id}" />
                                            <c:choose>
                                                <c:when test="${followStatusMap[u.id]}">
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
                    </div>

                    <c:if test="${showingSearchResults and not empty usersPage}">
                        <ui:pagination result="${usersPage}" />
                    </c:if>
                </c:when>
                <c:otherwise>
                    <div class="empty-products-state">
                        <i class="bi bi-people search-users-empty-icon"></i>
                        <p class="search-users-empty-text">
                            <c:choose>
                                <c:when test="${showingSearchResults}">
                                    <spring:message code="SearchUsers.noResults" />
                                </c:when>
                                <c:otherwise>
                                    <spring:message code="SearchUsers.empty" />
                                </c:otherwise>
                            </c:choose>
                        </p>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>

</ui:layout>
