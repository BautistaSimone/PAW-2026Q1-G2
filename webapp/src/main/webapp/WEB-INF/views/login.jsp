<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>

<spring:message code="Login.title" var="loginTitle" />
<spring:message code="Login.button" var="loginButton" />
<ui:layout title="${loginTitle}" bodyClass="auth-page-bg">

    <ui:header showHeaderActions="false"/>
    <ui:login-form
        action="${pageContext.request.contextPath}/login"
        method="POST"
        buttonLabel="${loginButton}"
    />
</ui:layout>
