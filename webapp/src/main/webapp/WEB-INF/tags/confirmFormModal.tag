<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="id" required="true" type="java.lang.String" %>
<%@ attribute name="title" required="true" type="java.lang.String" %>
<%@ attribute name="message" required="true" type="java.lang.String" %>
<%@ attribute name="confirmBtnText" required="true" type="java.lang.String" %>
<%@ attribute name="cancelBtnText" required="false" type="java.lang.String" %>
<%@ attribute name="actionUrl" required="true" type="java.lang.String" %>
<%@ attribute name="method" required="false" type="java.lang.String" %>
<%@ attribute name="hiddenInputName" required="false" type="java.lang.String" %>
<%@ attribute name="hiddenInputValue" required="false" type="java.lang.String" %>
<%@ attribute name="confirmBtnClass" required="false" type="java.lang.String" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<spring:message code="Global.cancel" var="defaultCancelText" />
<c:set var="cancelText" value="${not empty cancelBtnText ? cancelBtnText : defaultCancelText}" />
<c:set var="submitMethod" value="${not empty method ? method : 'post'}" />
<c:set var="btnClass" value="${not empty confirmBtnClass ? confirmBtnClass : 'btn-retro-danger'}" />

<div class="modal fade confirm-form-modal" id="<c:out value='${id}' />" tabindex="-1" aria-labelledby="<c:out value='${id}' />Label" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content" style="border: 2px solid var(--color-border); border-radius: 12px; box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15); background-color: var(--color-card-bg, #ffffff);">
            <div class="modal-header" style="border-bottom: 2px solid var(--color-border); background-color: var(--color-card-bg, #ffffff);">
                <h5 class="modal-title" id="<c:out value='${id}' />Label" style="font-weight: bold; color: var(--color-text-main, #264653);">
                    <c:out value="${title}"/>
                </h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="<spring:message code='Modal.button.close.ariaLabel' />"></button>
            </div>
            <div class="modal-body" style="color: var(--color-text-muted, #6c757d); background-color: var(--color-card-bg, #ffffff);">
                <p style="margin: 0;"><c:out value="${message}"/></p>
            </div>
            <div class="modal-footer" style="border-top: none; background-color: var(--color-card-bg, #ffffff);">
                <button type="button" class="btn btn-retro btn-retro-secondary" data-bs-dismiss="modal">
                    <c:out value="${cancelText}"/>
                </button>
                <form action="<c:out value='${actionUrl}'/>" method="${submitMethod}" style="margin: 0;">
                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
                    <c:if test="${not empty hiddenInputName}">
                        <input type="hidden" name="<c:out value='${hiddenInputName}' />" value="<c:out value='${hiddenInputValue}' />" />
                    </c:if>
                    <button type="submit" class="btn btn-retro ${btnClass}">
                        <c:out value="${confirmBtnText}"/>
                    </button>
                </form>
            </div>
        </div>
    </div>
</div>
