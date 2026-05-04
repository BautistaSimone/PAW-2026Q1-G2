<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="title" required="true" %>
<%@ attribute name="artist" required="true" %>
<%@ attribute name="price" required="true" type="java.lang.Number" %>
<%@ attribute name="installments" required="true" type="java.lang.Number" %>
<%@ attribute name="href" required="true" %>
<%@ attribute name="imageUrl" required="false" %>
<%@ attribute name="onSale" required="false" type="java.lang.Boolean" %>
<%@ attribute name="discountPercentage" required="false" type="java.lang.Integer" %>
<%@ attribute name="categories" required="false" rtexprvalue="true" type="java.util.List" %>
<%@ attribute name="sellerRating" required="false" rtexprvalue="true" type="ar.edu.itba.paw.models.SellerRatingSummary" %>
<%@ attribute name="linkDisabled" required="false" rtexprvalue="true" type="java.lang.Boolean" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="isOnSale" value="${onSale ne null ? onSale : false}" />
<c:set var="isLinkDisabled" value="${linkDisabled ne null ? linkDisabled : false}" />
<spring:message code="ProductCard.linkDisabled.ariaLabel" var="productCardNoLinkAria" />

<c:choose>
    <c:when test="${isLinkDisabled}">
        <div class="card product-card text-decoration-none product-card-div-1"  role="group" aria-label="${productCardNoLinkAria}">
    </c:when>
    <c:otherwise>
        <a href="${href}" class="card product-card text-decoration-none product-card-a-2" >
    </c:otherwise>
</c:choose>
    <div class="product-image-placeholder product-card-div-3" >
        <c:choose>
            <c:when test="${not empty imageUrl}">
                <img
                        src="<c:url value='${imageUrl}'/>"
                        alt="<c:out value='${artist}'/> - <c:out value='${title}'/>"
                        style="position: absolute; inset: 0; width: 100%; height: 100%; object-fit: cover;"
                />
            </c:when>
            <c:otherwise>
                <i class="bi bi-vinyl product-card-i-4" aria-hidden="true" ></i>
            </c:otherwise>
        </c:choose>

        <c:if test="${isOnSale && discountPercentage ne null && discountPercentage gt 0}">
            <div class="offer-badge">-${discountPercentage}%</div>
        </c:if>
    </div>

    <div class="card-body product-card-div-5" >
        <h6 class="product-title"><c:out value='${title}'/></h6>
        <p class="product-artist"><c:out value='${artist}'/></p>
        <c:if test="${not empty categories}">
            <div class="product-card-categories" role="list" aria-label="<spring:message code='Product.categories.ariaLabel' />">
                <c:forEach items="${categories}" var="cat" varStatus="st" end="2">
                    <span class="product-card-category-pill"><c:out value="${cat.name}"/></span>
                </c:forEach>
                <c:if test="${fn:length(categories) > 3}">
                    <span class="product-card-category-overflow">+${fn:length(categories) - 3}</span>
                </c:if>
            </div>
        </c:if>
        <c:choose>
            <c:when test="${isOnSale && discountPercentage ne null && discountPercentage gt 0}">
                <div class="price-wrapper">
                    <span class="album-price-original"><ui:price value="${price}" /></span>
                    <span class="product-price product-card-span-6" ><ui:price value="${price}" /></span>
                </div>
            </c:when>
            <c:otherwise>
                <div class="product-price"><ui:price value="${price}" /></div>
            </c:otherwise>
        </c:choose>
    </div>
<c:choose>
    <c:when test="${isLinkDisabled}">
        </div>
    </c:when>
    <c:otherwise>
        </a>
    </c:otherwise>
</c:choose>
