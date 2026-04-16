<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="title" required="true" %>
<%@ attribute name="artist" required="true" %>
<%@ attribute name="price" required="true" %>
<%@ attribute name="installments" required="true" %>
<%@ attribute name="href" required="true" %>
<%@ attribute name="imageUrl" required="false" %>
<%@ attribute name="onSale" required="false" type="java.lang.Boolean" %>
<%@ attribute name="discountPercentage" required="false" type="java.lang.Integer" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="isOnSale" value="${onSale ne null ? onSale : false}" />

<a href="${href}" class="card product-card text-decoration-none" style="cursor: pointer;">
    <div class="product-image-placeholder" style="position: relative;">
        <c:choose>
            <c:when test="${not empty imageUrl}">
                <img
                        src="<c:url value='${imageUrl}'/>"
                        alt="<c:out value='${artist}'/> - <c:out value='${title}'/>"
                        style="position: absolute; inset: 0; width: 100%; height: 100%; object-fit: cover;"
                />
            </c:when>
            <c:otherwise>
                <i class="bi bi-vinyl" aria-hidden="true" style="font-size: 2.5rem; color: #ccc;"></i>
            </c:otherwise>
        </c:choose>

        <c:if test="${isOnSale && discountPercentage ne null && discountPercentage gt 0}">
            <div class="offer-badge">-${discountPercentage}%</div>
        </c:if>
    </div>

    <div class="card-body" style="padding: 1rem 1.15rem;">
        <h6 class="product-title"><c:out value='${title}'/></h6>
        <p class="product-artist"><c:out value='${artist}'/></p>
        <c:choose>
            <c:when test="${isOnSale && discountPercentage ne null && discountPercentage gt 0}">
                <div class="price-wrapper">
                    <span class="album-price-original">$<c:out value='${price}'/></span>
                    <span class="product-price" style="margin: 0;">$<c:out value='${price}'/></span>
                </div>
            </c:when>
            <c:otherwise>
                <div class="product-price">$<c:out value='${price}'/></div>
            </c:otherwise>
        </c:choose>
    </div>
</a>
