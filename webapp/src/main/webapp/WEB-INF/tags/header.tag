<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ attribute name="showHeaderActions" required="false" type="java.lang.Boolean" %>

<c:set var="headerSearchText" value="${param['search-text']}" />

<link rel="stylesheet" href="<c:url value="/assets/css/style.css"/>">

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
                            placeholder="Buscar vinilos..."
                            aria-label="Buscar vinilos"
                            value="<c:out value='${headerSearchText}' />">
                        <button id="search-button" class="search-btn" type="submit" aria-label="Buscar">
                            <i class="bi bi-search" aria-hidden="true"></i>
                        </button>
                    </form>
                </div>
            </div>

            <div class="header-right">
                <!-- Space for future navigation items or balancing -->
                <a href="<c:url value='/profile'/>" class="profile-btn" aria-label="Ver perfil">
                    <i class="bi bi-person" aria-hidden="true"></i>
                    <span>Perfil</span>
                </a>
            </div>
        </c:if>
    </div>
</header>

<!-- Scripts -->
<script src="<c:url value="/assets/js/search.js"/>"></script>
