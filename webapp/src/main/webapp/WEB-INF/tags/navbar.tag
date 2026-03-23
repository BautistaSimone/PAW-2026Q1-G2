<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="cssClass" required="false" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="classes" value="${not empty cssClass ? cssClass : ''}"/>

<nav class="navbar navbar-expand-lg navbar-light bg-light ${classes}">
    <div class="container-fluid">
        <a class="navbar-brand">Vinyland</a>
        <form class="d-flex">
            <input class="rounded-pill form-control me-2" type="search" placeholder="Search" aria-label="Search">
            <button class="btn btn-outline-success" type="submit">Search</button>
        </form>
    </div>
</nav>