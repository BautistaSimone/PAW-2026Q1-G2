<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="action" required="true" %>
<%@ attribute name="method" required="true" %>
<%@ attribute name="buttonLabel" required="true" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix = "spring" uri = "http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<div class="container">
    <div class="row justify-content-center">
        <div class="col-12 col-md-6 col-lg-4">
            <form:form modelAttribute="loginForm"
                    action="${action}"
                    method="${method}"
                    class="p-4 border rounded shadow-sm bg-light">

                <!-- Email -->
                <div class="mb-3">
                    <label for="email" class="form-label">Email</label>

                    <form:input
                        path="email"
                        id="email"
                        cssClass="form-control"
                        placeholder="Enter email"
                        autocomplete="email" />

                    <form:errors path="email" cssClass="text-danger small"/>
                </div>

                <!-- Password -->
                <div class="mb-3">
                    <label for="password" class="form-label">Password</label>

                    <form:password
                        path="password"
                        id="password"
                        cssClass="form-control"
                        placeholder="Enter password"
                        autocomplete="current-password" />

                    <form:errors path="password" cssClass="text-danger small"/>
                </div>

                <!-- Remember Me -->
                <div class="form-check mb-3">
                    <form:checkbox
                        path="rememberMe"
                        id="rememberMe"
                        cssClass="form-check-input" />

                    <label class="form-check-label" for="rememberMe">
                        Remember me
                    </label>
                </div>

                <!-- Error message -->
                <c:if test="${param.error != null}">
                    <div class="text-danger">
                        <spring:message code = "Error.authForm.input" />
                    </div>
                </c:if>

                <!-- Submit -->
                <div class="d-grid">
                    <button type="submit" class="btn btn-primary">
                        ${buttonLabel}
                    </button>
                </div>

                <!-- Not registered link -->
                <div class="text-center mt-3">
                    <a href="${pageContext.request.contextPath}/register">
                        <spring:message code = "NotRegistered.loginForm" />
                    </a>
                </div>

            </form:form>
        </div>
    </div>
</div>