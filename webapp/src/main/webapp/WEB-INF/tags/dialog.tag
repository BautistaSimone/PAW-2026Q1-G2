<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="title" required="true" %>
<%@ attribute name="text" required="true" %>
<%@ attribute name="cssClass" required="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ taglib prefix="vl" tagdir="/WEB-INF/tags" %>

<c:set var="dialogCssClass" value="${not empty cssClass ? cssClass : ''}" />
<c:set var="classes" value="dialog ${catCardCssClass}" />

<div class="${classes}">
    <div class="dialog-content">
        <h3>${title}</h3>
        <p>${text}</p>

        <div class="dialog-buttons">
            <vl:button text="Cancelar"/>
            <vl:button text="Aceptar"/>
        </div>
    </div>
</div>