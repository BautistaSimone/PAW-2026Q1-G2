<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="status" required="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:choose>
    <c:when test="${status eq 'CANCELLED'}">
        <c:set var="stepIndex" value="-1" />
    </c:when>
    <c:when test="${status eq 'PENDING'}">
        <c:set var="stepIndex" value="0" />
    </c:when>
    <c:when test="${status eq 'PAID'}">
        <c:set var="stepIndex" value="1" />
    </c:when>
    <c:when test="${status eq 'SHIPPED'}">
        <c:set var="stepIndex" value="2" />
    </c:when>
    <c:otherwise>
        <c:set var="stepIndex" value="3" />
    </c:otherwise>
</c:choose>

<div class="purchase-stepper" role="group" aria-label="<spring:message code='PurchaseStepper.ariaLabel'/>">
    <%-- Step 0: Pendiente --%>
    <div class="purchase-step ${0 < stepIndex ? 'purchase-step--completed' : 0 == stepIndex ? 'purchase-step--current' : 'purchase-step--pending'}">
        <div class="purchase-step-icon" aria-hidden="true">
            <c:choose>
                <c:when test="${0 < stepIndex}"><i class="bi bi-check-lg"></i></c:when>
                <c:otherwise><i class="bi bi-hourglass-split"></i></c:otherwise>
            </c:choose>
        </div>
        <span class="purchase-step-label"><spring:message code="PurchaseStepper.status.pending"/></span>
    </div>

    <div class="purchase-step-connector ${1 <= stepIndex ? 'purchase-step-connector--filled' : ''}" aria-hidden="true"></div>

    <%-- Step 1: Pagado --%>
    <div class="purchase-step ${1 < stepIndex ? 'purchase-step--completed' : 1 == stepIndex ? 'purchase-step--current' : 'purchase-step--pending'}">
        <div class="purchase-step-icon" aria-hidden="true">
            <c:choose>
                <c:when test="${1 < stepIndex}"><i class="bi bi-check-lg"></i></c:when>
                <c:otherwise><i class="bi bi-credit-card"></i></c:otherwise>
            </c:choose>
        </div>
        <span class="purchase-step-label"><spring:message code="PurchaseStepper.status.paid"/></span>
    </div>

    <div class="purchase-step-connector ${2 <= stepIndex ? 'purchase-step-connector--filled' : ''}" aria-hidden="true"></div>

    <%-- Step 2: Enviado --%>
    <div class="purchase-step ${2 < stepIndex ? 'purchase-step--completed' : 2 == stepIndex ? 'purchase-step--current' : 'purchase-step--pending'}">
        <div class="purchase-step-icon" aria-hidden="true">
            <c:choose>
                <c:when test="${2 < stepIndex}"><i class="bi bi-check-lg"></i></c:when>
                <c:otherwise><i class="bi bi-truck"></i></c:otherwise>
            </c:choose>
        </div>
        <span class="purchase-step-label"><spring:message code="PurchaseStepper.status.shipped"/></span>
    </div>

    <div class="purchase-step-connector ${3 <= stepIndex ? 'purchase-step-connector--filled' : ''}" aria-hidden="true"></div>

    <%-- Step 3: Entregado --%>
    <div class="purchase-step ${3 < stepIndex ? 'purchase-step--completed' : 3 == stepIndex ? 'purchase-step--current' : 'purchase-step--pending'}">
        <div class="purchase-step-icon" aria-hidden="true">
            <c:choose>
                <c:when test="${3 < stepIndex}"><i class="bi bi-check-lg"></i></c:when>
                <c:otherwise><i class="bi bi-check-circle"></i></c:otherwise>
            </c:choose>
        </div>
        <span class="purchase-step-label"><spring:message code="PurchaseStepper.status.delivered"/></span>
    </div>
</div>
