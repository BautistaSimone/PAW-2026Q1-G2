<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<style>
.header-left {
    gap: 1rem;
}
.header-logo-link {
    display: flex;
    align-items: center;
    line-height: 0;
    flex-shrink: 0;
    border-radius: 10px;
    transition: opacity 0.2s ease, transform 0.2s ease;
}
.header-logo-link:hover {
    opacity: 0.88;
    transform: scale(1.04);
}
.header-logo-img {
    display: block;
    width: 44px;
    height: 44px;
    object-fit: contain;
}
@media (max-width: 991.98px) {
    .header-logo-img {
        width: 38px;
        height: 38px;
    }
}
</style>

<header class="header-bbdiscos">
    <div class="header-content">
        <div class="header-left">
            <a href="<c:url value="/"/>" class="header-logo-link" aria-label="Vinyland - Ir al inicio">
                <img src="<c:url value="/assets/images/vinyl_disk.png"/>" alt="" class="header-logo-img" width="44" height="44" decoding="async" />
            </a>
        </div>

        <div class="header-center">
            <div class="search-container">
                <form class="d-flex" method="get" action="<c:url value='/'/>" novalidate>
                    <input id="search-input" name="search-text" 
                        class="search-input" 
                        type="text" 
                        placeholder="Buscar vinilos..."
                        aria-label="Search">
                    <button id="search-button" class="search-btn" type="submit" aria-label="Buscar">
                        <i class="bi bi-search" aria-hidden="true"></i>
                    </button>
                </form>
            </div>
        </div>

        
    </div>
</header>

<!-- Scripts -->
<script src="<c:url value="${pageContext.request.contextPath}/assets/js/search.js"/>"></script> 
