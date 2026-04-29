<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>

<spring:message code="Register.title" var="registerTitle" />
<spring:message code="Register.button" var="registerButton" />
<ui:layout title="${registerTitle}" bodyClass="auth-page-bg">
    <ui:header showHeaderActions="false"/>
    <ui:register-form
        action="${pageContext.request.contextPath}/register"
        method="POST"
        buttonLabel="${registerButton}"
    />
</ui:layout>
