<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

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
                <a href="<c:url value='/profile'/>" class="profile-btn" aria-label="Ver perfil">
                    <i class="bi bi-person-fill" aria-hidden="true"></i>
                    <sec:authorize access="isAuthenticated()">
                        <span><sec:authentication property="principal.user.username" /></span>
                    </sec:authorize>
                    <sec:authorize access="!isAuthenticated()">
                        <span>Mi Perfil</span>
                    </sec:authorize>
                </a>
            </div>
        </c:if>
    </div>
</header>

<!-- Scripts -->
<script src="<c:url value="/assets/js/search.js"/>"></script>
