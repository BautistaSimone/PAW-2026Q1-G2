<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="title" required="true" %>
<%@ attribute name="text" required="false" %>
<%@ attribute name="id" required="true" %>
<%@ attribute name="primaryBtn" required="false" %>
<%@ attribute name="secondaryBtn" required="false" %>
<%@ attribute name="cssClass" required="false" %>
<%@ attribute name="primaryBtnId" required="false" %>
<%@ attribute name="primaryBtnOnClick" required="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<%@ taglib prefix="vl" tagdir="/WEB-INF/tags" %>

<spring:message code="Modal.button.primary" var="defaultPrimary" />
<spring:message code="Modal.button.close.ariaLabel" var="closeLabel" />
<c:set var="modalCssClass" value="${not empty cssClass ? cssClass : ''}" />
<c:set var="primary" value="${not empty primaryBtn ? primaryBtn : defaultPrimary}" />
<c:set var="modalText" value="${not empty text ? text : ''}" />
<c:set var="secondary" value="${secondaryBtn}" />
<c:set var="primaryBtnId"
       value="${not empty primaryBtnId ? primaryBtnId : ''}" />
<c:set var="primaryBtnOnClick"
       value="${not empty primaryBtnOnClick ? primaryBtnOnClick : ''}" />

<div class="modal fade <c:out value='${modalCssClass}' />" id="<c:out value='${id}' />" tabindex="-1" aria-labelledby="<c:out value='${id}' />Label" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title" id="<c:out value='${id}' />Label"><c:out value="${title}"/></h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="<c:out value='${closeLabel}' />"></button>
            </div>
            <div class="modal-body">
                <c:out value="${modalText}"/>
            </div>
            <div class="modal-footer">

                <c:if test="${secondaryBtn ne null}">
                    <button type="button" class="btn-accent-secondary" data-bs-dismiss="modal"><c:out value="${secondary}"/></button>
                </c:if>

                <button type="button"
                    id="<c:out value='${primaryBtnId}' />"
                    class="btn-accent"
                    data-bs-dismiss="modal"
                    onclick="<c:out value='${primaryBtnOnClick}' />">

                    <c:out value="${primary}"/>

                </button>
            </div>
        </div>
    </div>
</div>
