<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<spring:message code="Status.verification.titlePage" var="statusTitle" />
<ui:layout title="${statusTitle}" bodyClass="auth-page-bg">

    <ui:header showHeaderActions="false"/>

    <div class="container py-5 auth-page-container">
        <div class="row justify-content-center w-100">
            <div class="col-12 col-sm-10 col-md-7 col-lg-5 col-xl-4">
                <div class="auth-card">
                    
                    <div class="auth-card-header">
                        <h2><i class="bi bi-vinyl" aria-hidden="true"></i> Vinyland</h2>
                        <p><spring:message code="Status.verification.title"/></p>
                    </div>

                    <div class="auth-card-body text-center">

                        <!-- SUCCESS -->
                        <c:if test="${verificationSuccessful}">
                            <div class="mb-4">
                                <i class="bi bi-check-circle text-success auth-icon-large"></i>
                            </div>

                            <h5 class="mb-3">
                                <spring:message code="Status.verification.success.heading"/>
                            </h5>

                            <p class="text-muted">
                                <spring:message code="Status.verification.success.message"/>
                            </p>
                        </c:if>

                        <!-- ERROR -->
                        <c:if test="${not verificationSuccessful}">
                            <div class="mb-4">
                                <i class="bi bi-x-circle text-danger auth-icon-large"></i>
                            </div>

                            <h5 class="mb-3">
                                <spring:message code="Status.verification.error.heading"/>
                            </h5>

                            <p class="text-muted">
                                <spring:message code="Status.verification.error.message"/>
                            </p>
                        </c:if>

                    </div>

                    <div class="auth-card-footer">
                        <a href="<c:url value='/'/>" class="btn btn-retro btn-retro-outline w-100">
                            <i class="bi bi-arrow-left" aria-hidden="true"></i>
                            <spring:message code="Status.verification.back"/>
                        </a>
                    </div>

                </div>
            </div>
        </div>
    </div>

</ui:layout>