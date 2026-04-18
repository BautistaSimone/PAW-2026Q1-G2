<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="status" required="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="stepIndex" value="${status eq 'PENDING' ? 0 : status eq 'PAID' ? 1 : status eq 'SHIPPED' ? 2 : 3}" />

<div class="purchase-stepper" role="group" aria-label="Progreso de la compra">
    <%-- Step 0: Pendiente --%>
    <div class="purchase-step ${0 < stepIndex ? 'purchase-step--completed' : 0 == stepIndex ? 'purchase-step--current' : 'purchase-step--pending'}">
        <div class="purchase-step-icon" aria-hidden="true">
            <c:choose>
                <c:when test="${0 < stepIndex}"><i class="bi bi-check-lg"></i></c:when>
                <c:otherwise><i class="bi bi-hourglass-split"></i></c:otherwise>
            </c:choose>
        </div>
        <span class="purchase-step-label">Pendiente</span>
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
        <span class="purchase-step-label">Pagado</span>
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
        <span class="purchase-step-label">Enviado</span>
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
        <span class="purchase-step-label">Entregado</span>
    </div>
</div>
