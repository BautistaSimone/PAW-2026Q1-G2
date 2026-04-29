<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>

<%@ taglib prefix = "spring" uri = "http://www.springframework.org/tags" %>

<spring:message code="Sent.verification.titlePage" var="sentTitle" />
<ui:layout title="${sentTitle}" bodyClass="auth-page-bg">

    <ui:header showHeaderActions="false"/>

    <div class="container py-5" style="min-height: 70vh; display: flex; align-items: center;">
        <div class="row justify-content-center w-100">
            <div class="col-12 col-sm-10 col-md-7 col-lg-5 col-xl-4">
                <div class="auth-card">
                    <div class="auth-card-header">
                        <h2><i class="bi bi-vinyl" aria-hidden="true"></i> Vinyland</h2>
                        <p>
                            <spring:message code = "Sent.verification.title" />
                        </p>
                    </div>

                    <div class="auth-card-body text-center">

                        <div class="mb-4">
                            <i class="bi bi-envelope-check" style="font-size: 3rem;"></i>
                        </div>

                        <h5 class="mb-3">
                            <spring:message code = "Sent.verification.check" />
                        </h5>

                        <p class="text-muted">
                            <spring:message code = "Sent.verification.text" />
                        </p>

                        <c:if test="${not empty email}">
                            <p class="small text-muted">
                                <spring:message code = "Sent.verification.sentTo" /> <strong><c:out value="${email}" /></strong>
                            </p>
                        </c:if>

                    </div>

                    <div class="auth-card-footer">
                        <a href="<c:url value='/login'/>" class="btn btn-retro btn-retro-outline w-100">
                            <i class="bi bi-arrow-left" aria-hidden="true"></i> 
                            <spring:message code = "Sent.verification.backToLogin" />
                        </a>
                    </div>
                </div>
            </div>
        </div>
    </div>

</ui:layout>