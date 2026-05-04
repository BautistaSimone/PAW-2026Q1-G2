<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="summary" required="true" type="ar.edu.itba.paw.models.SellerRatingSummary" rtexprvalue="true" %>
<%@ attribute name="compact" required="false" type="java.lang.Boolean" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="isCompact" value="${compact ne null and compact}" />

<c:set var="starSize" value="${isCompact ? '0.72rem' : '0.9rem'}"/>
<c:set var="captionSize" value="${isCompact ? '0.72rem' : '0.85rem'}"/>

<c:choose>
    <c:when test="${summary.count > 0}">
        <span class="seller-rating-stars-wrap seller-rating-stars-span-1" >
            <span style="color: var(--color-accent); letter-spacing: 0.02em; font-size: ${starSize};" aria-hidden="true">
                <c:forEach begin="1" end="5" var="i">
                    <c:choose>
                        <c:when test="${i <= summary.avgScore}"><i class="bi bi-star-fill"></i></c:when>
                        <c:when test="${i - 0.5 <= summary.avgScore}"><i class="bi bi-star-half"></i></c:when>
                        <c:otherwise><i class="bi bi-star"></i></c:otherwise>
                    </c:choose>
                </c:forEach>
            </span>
            <span style="font-size: ${captionSize}; color: var(--color-text-muted); font-weight: 600;">
                <c:out value="${summary.formattedAvg}"/>
                <c:if test="${!isCompact}">
                    <span class="product-form-span-2" > (<c:out value="${summary.count}"/> <spring:message code="SellerRating.reviews"/><c:if test="${summary.count != 1}">s</c:if>)</span>
                </c:if>
                <c:if test="${isCompact}">
                    <span class="product-form-span-2" > (<c:out value="${summary.count}"/>)</span>
                </c:if>
            </span>
        </span>
    </c:when>
    <c:otherwise>
        <span style="font-size: ${captionSize}; color: var(--color-text-muted); font-style: italic;"><spring:message code="SellerRating.noReviews"/></span>
    </c:otherwise>
</c:choose>
