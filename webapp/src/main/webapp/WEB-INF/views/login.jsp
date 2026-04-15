<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>

<ui:layout title="Vinyland | Login">
    <ui:login-form 
        action="${pageContext.request.contextPath}/login"
        method="POST"
        buttonLabel="Login"
    />
</ui:layout>