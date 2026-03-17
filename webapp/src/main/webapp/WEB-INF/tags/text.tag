<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="value" required="true" %>
<%@ attribute name="type" required="false" %>
<%@ attribute name="cssClass" required="false" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="textType" value="${not empty type ? type : 'p'}"/>
<c:set var="textClass" value="${not empty cssClass ? cssClass : ''}"/>

<c:choose>

    <c:when test="${textType eq 'h1'}">
        <h1 class="${textClass}">${value}</h1>
    </c:when>

    <c:when test="${textType eq 'h2'}">
        <h2 class="${textClass}">${value}</h2>
    </c:when>

    <c:when test="${textType eq 'h3'}">
        <h3 class="${textClass}">${value}</h3>
    </c:when>

    <c:when test="${textType eq 'h4'}">
        <h4 class="${textClass}">${value}</h4>
    </c:when>

    <c:when test="${textType eq 'span'}">
        <span class="${textClass}">${value}</span>
    </c:when>

    <c:otherwise>
        <p class="${textClass}">${value}</p>
    </c:otherwise>

</c:choose>