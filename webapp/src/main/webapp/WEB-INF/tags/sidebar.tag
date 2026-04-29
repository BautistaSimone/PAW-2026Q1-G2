<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<div class="sidebar-overlay"></div>

<div class="sidebar-menu">
    <div class="sidebar-header">
        <div class="sidebar-title">
            <i class="bi bi-vinyl" aria-hidden="true"></i>
            <h3><spring:message code="Sidebar.categories" /></h3>
        </div>
        <button class="sidebar-close" data-action="close-sidebar" aria-label="<spring:message code='Sidebar.close.ariaLabel' />">
            <i class="bi bi-x-lg" aria-hidden="true"></i>
        </button>
    </div>

    <div class="sidebar-content">
        <nav class="category-nav">
            <button class="category-item">
                <i class="bi bi-collection category-icon" aria-hidden="true"></i>
                <span class="category-name"><spring:message code="Sidebar.category.all" /></span>
            </button>
            <button class="category-item">
                <i class="bi bi-globe2 category-icon" aria-hidden="true"></i>
                <span class="category-name"><spring:message code="Sidebar.category.intl" /></span>
            </button>
            <button class="category-item">
                <i class="bi bi-person category-icon" aria-hidden="true"></i>
                <span class="category-name"><spring:message code="Sidebar.category.male" /></span>
            </button>
            <button class="category-item">
                <i class="bi bi-person-hearts category-icon" aria-hidden="true"></i>
                <span class="category-name"><spring:message code="Sidebar.category.female" /></span>
            </button>
            <button class="category-item">
                <i class="bi bi-music-note-beamed category-icon" aria-hidden="true"></i>
                <span class="category-name"><spring:message code="Sidebar.category.jazz" /></span>
            </button>
            <button class="category-item">
                <i class="bi bi-flag category-icon" aria-hidden="true"></i>
                <span class="category-name"><spring:message code="Sidebar.category.national" /></span>
            </button>
            <button class="category-item">
                <i class="bi bi-tropical-storm category-icon" aria-hidden="true"></i>
                <span class="category-name"><spring:message code="Sidebar.category.brazilian" /></span>
            </button>
            <button class="category-item">
                <i class="bi bi-three-dots category-icon" aria-hidden="true"></i>
                <span class="category-name"><spring:message code="Sidebar.category.others" /></span>
            </button>
        </nav>
    </div>
</div>
