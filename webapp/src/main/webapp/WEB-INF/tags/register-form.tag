<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="action" required="true" %>
<%@ attribute name="method" required="true" %>
<%@ attribute name="buttonLabel" required="true" %>

<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<form:form modelAttribute="registerForm"
           action="${action}"
           method="${method}"
           class="p-4 border rounded shadow-sm bg-light">

    <!-- Username -->
    <div class="mb-3">
        <label for="username" class="form-label">Username</label>
        <form:input path="username"
                    id="username"
                    cssClass="form-control"
                    placeholder="Enter username"
                    autocomplete="username" />
        <form:errors path="username" cssClass="text-danger small"/>
    </div>

    <!-- Email -->
    <div class="mb-3">
        <label for="email" class="form-label">Email</label>
        <form:input path="email"
                    id="email"
                    cssClass="form-control"
                    placeholder="Enter email"
                    autocomplete="email" />
        <form:errors path="email" cssClass="text-danger small"/>
    </div>

    <!-- Password -->
    <div class="mb-3">
        <label for="password" class="form-label">Password</label>
        <form:password path="password"
                       id="password"
                       cssClass="form-control"
                       placeholder="Enter password"
                       autocomplete="new-password" />
        <form:errors path="password" cssClass="text-danger small"/>
    </div>

    <!-- Confirm Password -->
    <div class="mb-3">
        <label for="confirmPassword" class="form-label">Confirm Password</label>
        <form:password path="confirmPassword"
                       id="confirmPassword"
                       cssClass="form-control"
                       placeholder="Repeat password"
                       autocomplete="new-password" />
        <form:errors path="confirmPassword" cssClass="text-danger small"/>
    </div>

    <!-- Global errors (like PasswordMatches) -->
    <div class="mb-3">
        <form:errors path="*" cssClass="text-danger"/>
    </div>

    <!-- Submit -->
    <div class="d-grid">
        <button type="submit" class="btn btn-success">
            ${buttonLabel}
        </button>
    </div>

</form:form>