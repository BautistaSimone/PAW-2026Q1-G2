<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<spring:message code="ForgotPassword.title" var="forgotPasswordTitle" />
<ui:layout title="${forgotPasswordTitle}" bodyClass="auth-page-bg">

    <ui:header showHeaderActions="false"/>

    <div class="container py-5 auth-page-container">
        <div class="row justify-content-center w-100">
            <div class="col-12 col-sm-10 col-md-7 col-lg-5 col-xl-4">
                <div class="auth-card">
                    <div class="auth-card-header">
                        <h2><i class="bi bi-vinyl" aria-hidden="true"></i> Vinyland</h2>
                        <p><spring:message code="ForgotPassword.heading" /></p>
                    </div>
                    <div class="auth-card-body">

                        <p class="text-muted small mb-3">
                            <spring:message code="ForgotPassword.message" />
                        </p>

                        <c:url var="resetPasswordUrl" value="/resetPassword" />
                        <form action="<c:out value='${resetPasswordUrl}' />" method="POST">
                            <input type="hidden" name="<c:out value='${_csrf.parameterName}'/>" value="<c:out value='${_csrf.token}'/>" />
                            <div class="mb-3">
                                <label class="form-label"><spring:message code="ForgotPassword.email.label" /></label>
                                <spring:message code="ForgotPassword.email.placeholder" var="forgotEmailPlaceholder" />
                                <input type="email"
                                       name="email"
                                       value="<c:out value='${userEmail}'/>"
                                       class="form-control"
                                       placeholder="<c:out value='${forgotEmailPlaceholder}' />"
                                       required />
                            </div>

                            <!-- Error message -->
                            <c:if test="${not empty error}">
                                <div class="alert-retro alert-retro-warning mb-3" role="alert">
                                    <i class="bi bi-exclamation-triangle" aria-hidden="true"></i>
                                    <spring:message code="UserNotFound.authForm.email" />
                                </div>
                            </c:if>

                            <div class="d-grid">
                                <button type="submit" class="btn-accent">
                                    <spring:message code="ForgotPassword.submit" />
                                </button>
                            </div>
                        </form>
                    </div>

                    <div class="auth-card-footer">
                        <a href="<c:url value='/'/>" class="btn btn-retro btn-retro-outline w-100">
                            <i class="bi bi-arrow-left" aria-hidden="true"></i> <spring:message code="ForgotPassword.back" />
                        </a>
                    </div>
                </div>
            </div>
        </div>
    </div>

</ui:layout>