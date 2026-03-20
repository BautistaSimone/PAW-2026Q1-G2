<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="text" required="true" %>
<%@ attribute name="type" required="false" %>
<%@ attribute name="href" required="false" %>
<%@ attribute name="target" required="false" %>
<%@ attribute name="cssClass" required="false" %>
<%@ attribute name="disabled" required="false" type="java.lang.Boolean" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="btnType" value="${not empty size ? type : 'primary'}"/>
<c:set var="btnCssClass" value="${not empty cssClass ? cssClass : ''}" />
<c:set var="btnDisabled" value="${disabled ne null ? disabled : false}" />
<c:set var="classes" value="btn btn-${btnType} ${btnCssClass}" />

<button 
    type="button" 
    class="${classes}" 
    data-bs-toggle="modal" 
    data-bs-target="${target}"
    href="${href}"
    <c:if test="${btnDisabled}">disabled</c:if>
    >
    ${text}
</button>
