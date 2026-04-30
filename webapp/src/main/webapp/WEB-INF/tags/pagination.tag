<%@ tag language="java" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<%@ attribute name="result" required="true" type="ar.edu.itba.paw.models.PaginatedResult" %>
<%@ attribute name="pageParamName" required="false" %>

<c:set var="pp" value="${empty pageParamName ? 'page' : pageParamName}" />

<c:if test="${result.totalPages > 1}">
    <nav aria-label="<spring:message code='Pagination.nav.ariaLabel' />" class="mt-4 mb-2">
        <ul class="pagination justify-content-center">
            
            <c:choose>
                <c:when test="${result.hasPreviousPage}">
                    <c:url var="prevUrl" value="">
                            <c:forEach items="${param}" var="p">
                            <c:if test="${p.key ne pp}">
                                <c:forEach items="${paramValues[p.key]}" var="val">
                                    <c:param name="${p.key}" value="${val}"/>
                                </c:forEach>
                            </c:if>
                        </c:forEach>
                        <c:param name="${pp}" value="${result.currentPage - 1}"/>
                    </c:url>
                    <li class="page-item">
                        <a class="page-link" href="${prevUrl}" aria-label="<spring:message code='Pagination.prev.ariaLabel' />">
                            <span aria-hidden="true">&laquo;</span>
                        </a>
                    </li>
                </c:when>
                <c:otherwise>
                    <li class="page-item disabled">
                        <span class="page-link" aria-hidden="true">&laquo;</span>
                    </li>
                </c:otherwise>
            </c:choose>

            <c:forEach begin="1" end="${result.totalPages}" var="i">
                <c:choose>
                    <c:when test="${i == result.currentPage}">
                        <li class="page-item active" aria-current="page">
                            <span class="page-link" style="background-color: var(--color-accent); border-color: var(--color-accent);">${i}</span>
                        </li>
                    </c:when>
                    <c:otherwise>
                        <c:url var="pageUrl" value="">
                            <c:forEach items="${param}" var="p">
                                <c:if test="${p.key ne pp}">
                                    <c:forEach items="${paramValues[p.key]}" var="val">
                                        <c:param name="${p.key}" value="${val}"/>
                                    </c:forEach>
                                </c:if>
                            </c:forEach>
                            <c:param name="${pp}" value="${i}"/>
                        </c:url>
                        <li class="page-item"><a class="page-link" style="color: var(--color-accent);" href="${pageUrl}">${i}</a></li>
                    </c:otherwise>
                </c:choose>
            </c:forEach>

            <c:choose>
                <c:when test="${result.hasNextPage}">
                    <c:url var="nextUrl" value="">
                            <c:forEach items="${param}" var="p">
                            <c:if test="${p.key ne pp}">
                                <c:forEach items="${paramValues[p.key]}" var="val">
                                    <c:param name="${p.key}" value="${val}"/>
                                </c:forEach>
                            </c:if>
                        </c:forEach>
                        <c:param name="${pp}" value="${result.currentPage + 1}"/>
                    </c:url>
                    <li class="page-item">
                        <a class="page-link" href="${nextUrl}" aria-label="<spring:message code='Pagination.next.ariaLabel' />">
                            <span aria-hidden="true">&raquo;</span>
                        </a>
                    </li>
                </c:when>
                <c:otherwise>
                    <li class="page-item disabled">
                        <span class="page-link" aria-hidden="true">&raquo;</span>
                    </li>
                </c:otherwise>
            </c:choose>
        </ul>
    </nav>
</c:if>
