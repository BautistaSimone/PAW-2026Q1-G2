<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="action" required="true" %>
<%@ attribute name="method" required="true" %>
<%@ attribute name="buttonLabel" required="true" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<div class="container py-5 auth-page-container">
    <div class="row justify-content-center w-100">
        <div class="col-12 col-sm-11 col-md-9 col-lg-7 col-xl-6">
            <div class="auth-card">
                <div class="auth-card-header">
                    <h2><i class="bi bi-vinyl" aria-hidden="true"></i> <spring:message code="Register.button" /></h2>
                    <p><spring:message code="Register.subtitle" /></p>
                </div>
                <div class="auth-card-body">
                    <form:form modelAttribute="registerForm"
                            action="${action}"
                            method="${method}">
                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />

                        <div class="row g-2">
                            <div class="col-md-6 mb-3">
                                <label for="firstName" class="form-label"><spring:message code="Register.firstName.label" /> <span class="text-danger">*</span></label>
                                <spring:message code="Register.firstName.placeholder" var="firstNamePlaceholder" />
                                <form:input path="firstName" id="firstName" cssClass="form-control" placeholder="${firstNamePlaceholder}" autocomplete="given-name" />
                                <form:errors path="firstName" cssClass="text-danger small"/>
                            </div>
                            <div class="col-md-6 mb-3">
                                <label for="lastName" class="form-label"><spring:message code="Register.lastName.label" /> <span class="text-danger">*</span></label>
                                <spring:message code="Register.lastName.placeholder" var="lastNamePlaceholder" />
                                <form:input path="lastName" id="lastName" cssClass="form-control" placeholder="${lastNamePlaceholder}" autocomplete="family-name" />
                                <form:errors path="lastName" cssClass="text-danger small"/>
                            </div>
                        </div>

                        <div class="mb-3">
                            <label for="username" class="form-label"><spring:message code="Register.username.label" /> <span class="text-danger">*</span></label>
                            <spring:message code="Register.username.placeholder" var="usernamePlaceholder" />
                            <form:input path="username"
                                        id="username"
                                        cssClass="form-control"
                                        placeholder="${usernamePlaceholder}"
                                        autocomplete="username" />
                            <form:errors path="username" cssClass="text-danger small"/>
                        </div>

                        <div class="mb-3">
                            <label for="email" class="form-label"><spring:message code="Register.email.label" /> <span class="text-danger">*</span></label>
                            <spring:message code="Register.email.placeholder" var="registerEmailPlaceholder" />
                            <form:input path="email"
                                        id="email"
                                        cssClass="form-control"
                                        placeholder="${registerEmailPlaceholder}"
                                        autocomplete="email" />
                            <form:errors path="email" cssClass="text-danger small"/>
                        </div>

                        <div class="mb-3">
                            <label for="password" class="form-label"><spring:message code="Register.password.label" /> <span class="text-danger">*</span></label>
                            <div class="password-toggle-wrapper">
                                <spring:message code="Register.password.placeholder" var="registerPasswordPlaceholder" />
                                <form:password path="password"
                                            id="password"
                                            cssClass="form-control"
                                            placeholder="${registerPasswordPlaceholder}"
                                            autocomplete="new-password" />
                                <button type="button" class="password-toggle-btn" aria-label="<spring:message code='Login.password.show.ariaLabel' />" onclick="togglePassword('password', this)">
                                    <i class="bi bi-eye"></i>
                                </button>
                            </div>
                            <div class="form-text"><spring:message code="Register.password.help" /></div>
                            <form:errors path="password" cssClass="text-danger small"/>
                        </div>

                        <div class="mb-3">
                            <label for="confirmPassword" class="form-label"><spring:message code="Register.confirmPassword.label" /> <span class="text-danger">*</span></label>
                            <div class="password-toggle-wrapper">
                                <spring:message code="Register.confirmPassword.placeholder" var="confirmPasswordPlaceholder" />
                                <form:password path="confirmPassword"
                                            id="confirmPassword"
                                            cssClass="form-control"
                                            placeholder="${confirmPasswordPlaceholder}"
                                            autocomplete="new-password" />
                                <button type="button" class="password-toggle-btn" aria-label="<spring:message code='Login.password.show.ariaLabel' />" onclick="togglePassword('confirmPassword', this)">
                                    <i class="bi bi-eye"></i>
                                </button>
                            </div>
                            <form:errors path="confirmPassword" cssClass="text-danger small"/>
                        </div>

                        <hr class="my-4 auth-divider" />
                        <p class="small text-muted mb-3 auth-extra-data-heading"><spring:message code="Register.extraData.heading" /></p>

                        <div class="mb-3">
                            <label for="streetName" class="form-label"><spring:message code="Register.streetName.label" /></label>
                            <spring:message code="Register.streetName.placeholder" var="streetNamePlaceholder" />
                            <form:input path="streetName" id="streetName" cssClass="form-control" placeholder="${streetNamePlaceholder}" />
                            <form:errors path="streetName" cssClass="text-danger small"/>
                        </div>
                        <div class="mb-3">
                            <label for="streetNumber" class="form-label"><spring:message code="Register.streetNumber.label" /></label>
                            <spring:message code="Register.streetNumber.placeholder" var="streetNumberPlaceholder" />
                            <form:input type="number" path="streetNumber" id="streetNumber" cssClass="form-control" placeholder="${streetNumberPlaceholder}" min="1" />
                            <form:errors path="streetNumber" cssClass="text-danger small"/>
                        </div>
                        <div class="row g-2">
                            <div class="col-md-6 mb-3">
                                <label for="neighborhood" class="form-label"><spring:message code="Register.neighborhood.label" /></label>
                                <spring:message code="Register.neighborhood.placeholder" var="neighborhoodPlaceholder" />
                                <form:input path="neighborhood" id="neighborhood" cssClass="form-control" placeholder="${neighborhoodPlaceholder}" />
                                <form:errors path="neighborhood" cssClass="text-danger small"/>
                            </div>
                            <div class="col-md-6 mb-3">
                                <label for="province" class="form-label"><spring:message code="Register.province.label" /></label>
                                <spring:message code="Register.province.placeholder" var="provincePlaceholder" />
                                <form:input path="province" id="province" cssClass="form-control" placeholder="${provincePlaceholder}" />
                                <form:errors path="province" cssClass="text-danger small"/>
                            </div>
                        </div>
                        <div class="mb-3">
                            <label for="extraAddressInfo" class="form-label"><spring:message code="Register.extraAddressInfo.label" /></label>
                            <spring:message code="Register.extraAddressInfo.placeholder" var="extraAddressInfoPlaceholder" />
                            <form:input path="extraAddressInfo" id="extraAddressInfo" cssClass="form-control" placeholder="${extraAddressInfoPlaceholder}" />
                            <form:errors path="extraAddressInfo" cssClass="text-danger small"/>
                        </div>
                        <div class="mb-3">
                            <label for="cbuCvu" class="form-label"><spring:message code="Register.cbuCvu.label" /></label>
                            <spring:message code="Register.cbuCvu.placeholder" var="cbuPlaceholder" />
                            <form:input path="cbuCvu" id="cbuCvu" cssClass="form-control" placeholder="${cbuPlaceholder}" inputmode="numeric" maxlength="22" />
                            <form:errors path="cbuCvu" cssClass="text-danger small"/>
                        </div>

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

                        <div class="d-grid">
                            <button type="submit" class="btn-accent">
                                <c:out value="${buttonLabel}" />
                            </button>
                        </div>
                    </form:form>
                </div>

                <div class="auth-card-footer">
                    <a href="<c:url value='/login'/>">
                        <spring:message code="Register.alreadyHasAccount" />
                    </a>
                </div>
            </div>
        </div>
    </div>
</div>
