<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<ui:layout title="Vinyland | Error ${errorCode}">
    <div class="container py-5" style="min-height: 50vh; display: flex; flex-direction: column; justify-content: center; align-items: center; text-align: center;">
        <h1 class="display-1 fw-bold text-muted mb-2"><c:out value="${errorCode}" default="Error" /></h1>
        <h2 class="h3 mb-4"><c:out value="${errorMessage}" default="Ocurrió un error inesperado" /></h2>
        
        <p class="text-muted mb-4">Lo sentimos, no pudimos completar tu solicitud. Por favor intenta nuevamente más tarde.</p>
        
        <a href="<c:url value='/'/>" class="btn btn-dark px-4 py-2">Volver al inicio</a>
    </div>
</ui:layout>
