<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="title" required="true" %>
<%@ attribute name="bodyClass" required="false" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<!doctype html>
<html lang="es">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title><c:out value='${title}'/></title>

    <link rel="icon" type="image/x-icon" href="<c:url value="/assets/images/vinyl_disk.png"/>">

    <link rel="stylesheet" type="text/css" href="<c:url value="/assets/css/style.css"/>"/>

    <!-- Bootstrap -->
    <link rel="stylesheet" href="<c:url value="/assets/bootstrap/css/bootstrap.min.css"/>">
    <!-- TODO: Hacer que saque de los assets, no del internet, lo de bootstrap -->
    <link
            rel="stylesheet"
            href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css"
    />
</head>
<body class="${not empty bodyClass ? bodyClass : ''}">
<div class="app-container">
    <main class="page-content">
        <jsp:doBody />
    </main>
    <ui:footer />
</div>

<!-- FIXME: Que haria este script? -->
<script src="${pageContext.request.contextPath}/assets/app.js"></script>
</body>
</html>
