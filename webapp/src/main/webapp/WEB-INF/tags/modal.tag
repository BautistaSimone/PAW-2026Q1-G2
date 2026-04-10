<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="title" required="true" %>
<%@ attribute name="text" required="true" %>
<%@ attribute name="id" required="true" %>
<%@ attribute name="primaryBtn" required="false" %>
<%@ attribute name="secondaryBtn" required="false" %>
<%@ attribute name="cssClass" required="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ taglib prefix="vl" tagdir="/WEB-INF/tags" %>

<c:set var="modalCssClass" value="${not empty cssClass ? cssClass : ''}" />
<c:set var="classes" value="modal ${modalCssClass}" />
<c:set var="primary" value="${primaryBtn ne null ? primaryBtn : 'Aceptar'}" />
<c:set var="secondary" value="${secondaryBtn ne null ? secondaryBtn : 'Cancelar'}" />

<div class="modal fade ${classes}" id="${id}" tabindex="-1" aria-labelledby="${id}Label" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title" id="exampleModalLabel"><c:out value="${title}"/></h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <div class="modal-body">
                <c:out value="${text}"/>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal"><c:out value="${secondary}"/></button>
                <button type="button" class="btn btn-primary"><c:out value="${primary}"/></button>
            </div>
        </div>
    </div>
</div>