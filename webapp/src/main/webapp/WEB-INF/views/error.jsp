<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<ui:layout title="Vinyland | Error ${errorCode}">
    <div class="error-page">
        <div class="error-vinyl"></div>
        <div class="error-code"><c:out value="${errorCode}" default="Error" /></div>
        <h2 class="error-message"><c:out value="${errorMessage}" default="Ocurrio un error inesperado" /></h2>
        <p class="error-description">Lo sentimos, no pudimos completar tu solicitud. Por favor intenta nuevamente mas tarde.</p>
        <a href="<c:url value='/'/>" class="btn btn-retro btn-retro-dark">
            <i class="bi bi-house" aria-hidden="true"></i> Volver al inicio
        </a>
    </div>
</ui:layout>
