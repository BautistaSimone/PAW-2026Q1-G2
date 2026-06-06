<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<c:set var="resolvedErrorCode" value="${not empty errorCode ? errorCode : requestScope['javax.servlet.error.status_code']}" />

<c:choose>
    <c:when test="${not empty errorMessageCode}">
        <c:set var="resolvedMessageCode" value="${errorMessageCode}" />
        <c:set var="resolvedDescriptionCode" value="${errorDescriptionCode}" />
    </c:when>
    <c:otherwise>
        <c:choose>
            <c:when test="${resolvedErrorCode == 400}">
                <c:set var="resolvedMessageCode" value="Error.400.message.badRequest" />
                <c:set var="resolvedDescriptionCode" value="Error.400.description.badRequest" />
            </c:when>
            <c:when test="${resolvedErrorCode == 403}">
                <c:set var="resolvedMessageCode" value="Error.403.message" />
                <c:set var="resolvedDescriptionCode" value="Error.403.description" />
            </c:when>
            <c:when test="${resolvedErrorCode == 404}">
                <c:set var="resolvedMessageCode" value="Error.404.message.noHandler" />
                <c:set var="resolvedDescriptionCode" value="Error.404.description.noHandler" />
            </c:when>
            <c:when test="${resolvedErrorCode == 405}">
                <c:set var="resolvedMessageCode" value="Error.405.message" />
                <c:set var="resolvedDescriptionCode" value="Error.405.description" />
            </c:when>
            <c:when test="${resolvedErrorCode == 500}">
                <c:set var="resolvedMessageCode" value="Error.500.message.generic" />
                <c:set var="resolvedDescriptionCode" value="Error.500.description.generic" />
            </c:when>
            <c:otherwise>
                <c:set var="resolvedMessageCode" value="Error.defaultMessage" />
                <c:set var="resolvedDescriptionCode" value="Error.defaultDescription" />
            </c:otherwise>
        </c:choose>
    </c:otherwise>
</c:choose>

<spring:message code="Error.title" arguments="${resolvedErrorCode}" var="errorTitle" />
<ui:layout title="${errorTitle}">
    <div class="error-page">
        <div class="error-vinyl"></div>
        <div class="error-code"><c:out value="${resolvedErrorCode}" default="Error" /></div>
        <spring:message code="${resolvedMessageCode}" var="msg" />
        <h2 class="error-message"><c:out value="${msg}" /></h2>
        <spring:message code="${resolvedDescriptionCode}" var="desc" />
        <p class="error-description"><c:out value="${desc}" /></p>
        <a href="<c:url value='/'/>" class="btn btn-retro btn-retro-primary">
            <i class="bi bi-house" aria-hidden="true"></i> <spring:message code="Error.backToHome" />
        </a>
    </div>
</ui:layout>
