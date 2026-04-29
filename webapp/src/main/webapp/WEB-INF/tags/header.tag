<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix = "spring" uri = "http://www.springframework.org/tags" %>

<%@ attribute name="showHeaderActions" required="false" type="java.lang.Boolean" %>

<c:set var="headerSearchText" value="${param['search-text']}" />

<header class="header-bbdiscos">

    <div class="header-content">
        <div class="header-left">
            <a href="<c:url value="/"/>" class="header-logo-link" aria-label="Vinyland - Ir al inicio">
                <img src="<c:url value="/assets/images/vinyl_disk.png"/>" alt="" class="header-logo-img" width="44" height="44" decoding="async" />
                <span class="brand-name">Vinyland</span>
            </a>
        </div>

        <c:if test="${showHeaderActions != false}">
            <div class="header-center">
                <div class="search-container">
                    <form class="search-form" method="get" action="<c:url value='/'/>" novalidate>
                        <input id="search-input" name="search-text"
                            class="search-input"
                            type="text"
                            placeholder="Buscar vinilos, artistas, sellos..."
                            aria-label="Buscar vinilos"
                            value="<c:out value='${headerSearchText}' />">
                        <button id="search-button" class="search-btn" type="submit" aria-label="Buscar">
                            <i class="bi bi-search" aria-hidden="true"></i>
                        </button>
                    </form>
                </div>
            </div>

            <div class="header-right">

                <sec:authorize access="isAuthenticated()">
                    <a href="<c:url value='/profile'/>" class="profile-btn" aria-label="Ver perfil">
                        <i class="bi bi-person-fill" aria-hidden="true"></i>
                        <span><sec:authentication property="principal.user.username" /></span>
                    </a>
                </sec:authorize>

                <!-- Link to login if not authenticated -->
                <sec:authorize access="!isAuthenticated()">
                    <a href="<c:url value='/login'/>" class="profile-btn" aria-label="<spring:message code='Profile.login.button' />">
                        <span><spring:message code="Profile.login.button" /></span>
                    </a>
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
