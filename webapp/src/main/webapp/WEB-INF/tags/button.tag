<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="text" required="true" %>
<%@ attribute name="img" required="false" %>
<%@ attribute name="size" required="false" %>
<%@ attribute name="cssClass" required="false" %>
<%@ attribute name="disabled" required="false" type="java.lang.Boolean" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>


<c:set var="btnSize" value="${not empty size ? size : 'md'}"/>
<c:set var="btnCssClass" value="${not empty cssClass ? cssClass : ''}" />
<c:set var="btnDisabled" value="${disabled ne null ? disabled : false}" />
<c:set var="btnImage" value="${img ne null ? true : false}" />
<c:set var="classes" value="btn btn-${btnSize} ${btnCssClass}" />

<button type="button"
    class="${classes}"
    <c:if test="${btnDisabled}">disabled</c:if>
    >

    <c:if test="${btnImage}"><img src="<c:url value="${img}"/>" alt="Button image"></c:if>
    <p>${text}</p>

</button>