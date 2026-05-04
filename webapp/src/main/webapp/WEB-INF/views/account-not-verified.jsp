<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<spring:message code="notVerified.titlePage" var="notVerifiedTitle" />
<ui:layout title="${notVerifiedTitle}" bodyClass="auth-page-bg">

    <ui:header showHeaderActions="false"/>

    <div class="container py-5 auth-page-container">
        <div class="row justify-content-center w-100">
            <div class="col-12 col-sm-10 col-md-7 col-lg-5 col-xl-4">
                <div class="auth-card">

                    <div class="auth-card-header">
                        <h2><i class="bi bi-vinyl"></i> Vinyland</h2>
                        <p><spring:message code="notVerified.title"/></p>
                    </div>

                    <div class="auth-card-body text-center">

                        <div class="mb-4">
                            <i class="bi bi-envelope-exclamation text-warning auth-icon-large"></i>
                        </div>

                        <h5 class="mb-3">
                            <spring:message code="notVerified.heading"/>
                        </h5>

                        <p class="text-muted">
                            <spring:message code="notVerified.message"/>
                        </p>

                        <c:if test="${not empty message}">
                            <div class="alert-retro alert-retro-info mt-3" role="alert">
                                <i class="bi bi-info-circle"></i>
                                <spring:message code="${message}"/>
                            </div>
                        </c:if>

                        <!-- Resend verification email -->
                        <c:url var="sendVerificationUrl" value="/sendVerificationEmail" />
                        <form:form action="${sendVerificationUrl}" method="POST">
                            <div class="d-grid mt-4">
                                <button type="submit" class="btn-accent">
                                    <i class="bi bi-envelope"></i>
                                    <spring:message code="notVerified.resend"/>
                                </button>
                            </div>
                        </form:form>

                    </div>

                    <div class="auth-card-footer">

                        <form action="<c:url value='/logout' />" method="post" style="margin-top: 1rem;">
                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
                            <button type="submit" class="btn btn-retro btn-retro-outline w-100">
                                <i class="bi bi-arrow-left"></i>
                                <spring:message code="notVerified.back"/>
                            </button>
                        </form>
                    </div>

                </div>
            </div>
        </div>
    </div>

</ui:layout>