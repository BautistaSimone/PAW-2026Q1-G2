<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>

<ui:layout title="Vinyland | Registro" bodyClass="auth-page-bg">
    <ui:header showHeaderActions="false"/>
    <ui:register-form
        action="${pageContext.request.contextPath}/register"
        method="POST"
        buttonLabel="Crear cuenta"
    />
</ui:layout>
