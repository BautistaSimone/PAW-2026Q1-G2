<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="action" required="true" %>
<%@ attribute name="method" required="true" %>
<%@ attribute name="buttonLabel" required="true" %>

<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

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

    <!-- Global errors -->
    <div class="mb-3">
        <form:errors path="*" cssClass="text-danger"/>
    </div>

    <!-- Submit -->
    <div class="d-grid">
        <button type="submit" class="btn btn-primary">
            ${buttonLabel}
        </button>
    </div>

    <!-- Not registered link -->
    <div class="text-center mt-3">
        <a href="${pageContext.request.contextPath}/register">
            Not registered? Create an account
        </a>
    </div>

</form:form>