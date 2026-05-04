<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="title" required="true" %>
<%@ attribute name="text" required="true" %>
<%@ attribute name="img" required="true" %>
<%@ attribute name="cssClass" required="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="catCardCssClass" value="${not empty cssClass ? cssClass : ''}" />
<c:set var="catCardImage" value="${img ne null ? true : false}" />
<c:set var="classes" value="category-card ${catCardCssClass}" />

<button type="button" class="<c:out value='${classes}' />">
    <img src="<c:url value="${img}"/>" alt="" class="category-card-bg-img" />
    
    <h3><c:out value="${title}"/></h3>
    <p><c:out value="${text}"/></p>

</button>