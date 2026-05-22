<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<spring:message code="UpdatePassword.title" var="updatePasswordTitle" />
<ui:layout title="${updatePasswordTitle}" bodyClass="auth-page-bg">

    <ui:header showHeaderActions="false"/>

    <div class="container py-5 auth-page-container">
        <div class="row justify-content-center w-100">
            <div class="col-12 col-sm-10 col-md-7 col-lg-5 col-xl-4">
                <div class="auth-card">
                    <div class="auth-card-header">
                        <h2><i class="bi bi-vinyl" aria-hidden="true"></i> <spring:message code="Global.brand"/></h2>
                        <p><spring:message code="UpdatePassword.heading" /></p>
                    </div>
                    <div class="auth-card-body">

                        <c:url var="changePasswordUrl" value="/changePassword" />
                        <form:form modelAttribute="updatePasswordForm" action="${changePasswordUrl}" method="POST">

                            <!-- Token hidden field -->
                            <form:hidden path="token" />

                            <div class="mb-3">
                                <label class="form-label"><spring:message code="UpdatePassword.newPassword.label" /></label>
                                <div class="password-toggle-wrapper">
                                    <spring:message code="UpdatePassword.newPassword.placeholder" var="newPasswordPlaceholder" />
                                    <form:password path="newPassword"
                                                   id="newPassword"
                                                   cssClass="form-control"
                                                   placeholder="${newPasswordPlaceholder}"
                                                   required="required"/>
                                    <button type="button" class="password-toggle-btn" aria-label="<spring:message code='Login.password.show.ariaLabel' />" onclick="togglePassword('newPassword', this)">
                                        <i class="bi bi-eye"></i>
                                    </button>
                                </div>
                                <form:errors path="newPassword" cssClass="text-danger small"/>
                            </div>

                            <div class="mb-3">
                                <label class="form-label"><spring:message code="UpdatePassword.confirmPassword.label" /></label>
                                <div class="password-toggle-wrapper">
                                    <spring:message code="UpdatePassword.confirmPassword.placeholder" var="confirmPasswordPlaceholder" />
                                    <form:password path="newPasswordConfirm"
                                                   id="newPasswordConfirm"
                                                   cssClass="form-control"
                                                   placeholder="${confirmPasswordPlaceholder}"
                                                   required="required"/>
                                    <button type="button" class="password-toggle-btn" aria-label="<spring:message code='Login.password.show.ariaLabel' />" onclick="togglePassword('newPasswordConfirm', this)">
                                        <i class="bi bi-eye"></i>
                                    </button>
                                </div>
                                <form:errors path="newPasswordConfirm" cssClass="text-danger small"/>
                            </div>

                            <c:if test="${not empty error}">
                                <div class="alert-retro alert-retro-warning mb-3" role="alert">
                                    <i class="bi bi-exclamation-triangle" aria-hidden="true"></i>
                                    <c:out value="${error}" />
                                </div>
                            </c:if>

                            <div class="d-grid">
                                <button type="submit" class="btn-accent">
                                    <spring:message code="UpdatePassword.submit" />
                                </button>
                            </div>

                        </form:form>

                        <script>
                            function togglePassword(inputId, btn) {
                                const input = document.getElementById(inputId);
                                const icon = btn.querySelector('i');
                                if (input.type === 'password') {
                                    input.type = 'text';
                                    icon.classList.remove('bi-eye');
                                    icon.classList.add('bi-eye-slash');
                                    btn.setAttribute('aria-label', '<spring:message code="Login.password.hide.ariaLabel" />');
                                } else {
                                    input.type = 'password';
                                    icon.classList.remove('bi-eye-slash');
                                    icon.classList.add('bi-eye');
                                    btn.setAttribute('aria-label', '<spring:message code="Login.password.show.ariaLabel" />');
                                }
                            }
                        </script>
                    </div>

                    <div class="auth-card-footer">

                        <c:url var="productDetailBackHref" value="${productDetailBackUrl}" />
                                    <a href="<c:out value='${productDetailBackHref}'/>" class="btn btn-retro btn-retro-outline w-100">
                            <i class="bi bi-arrow-left" aria-hidden="true"></i> <spring:message code="UpdatePassword.back" />
                        </a>
                    </div>
                </div>
            </div>
        </div>
    </div>

</ui:layout>