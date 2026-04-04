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
            <button class="menu-btn" aria-label="Menu" data-action="open-sidebar">
                <i class="bi bi-list" aria-hidden="true"></i>
            </button>
        </div>

        <div class="header-center">
            <div class="search-container">
                <input
                        type="text"
                        class="search-input"
                        placeholder="Buscar vinilos..."
                        aria-label="Buscar"
                />
                <button class="search-btn" aria-label="Buscar">
                    <i class="bi bi-search" aria-hidden="true"></i>
                </button>
            </div>
        </div>

        
    </div>
</header>
