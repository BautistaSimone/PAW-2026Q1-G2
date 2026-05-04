<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<spring:message code="Error.title" arguments="${errorCode}" var="errorTitle" />
<ui:layout title="${errorTitle}">
    <div class="error-page">
        <div class="error-vinyl"></div>
        <div class="error-code"><c:out value="${errorCode}" default="Error" /></div>
        <spring:message code="${not empty errorMessageCode ? errorMessageCode : 'Error.defaultMessage'}" var="msg" />
        <h2 class="error-message"><c:out value="${msg}" /></h2>
        <spring:message code="${not empty errorDescriptionCode ? errorDescriptionCode : 'Error.defaultDescription'}" var="desc" />
        <p class="error-description"><c:out value="${desc}" /></p>
        <a href="<c:url value='/'/>" class="btn btn-retro btn-retro-primary">
            <i class="bi bi-house" aria-hidden="true"></i> <spring:message code="Error.backToHome" />
        </a>
    </div>
</ui:layout>
