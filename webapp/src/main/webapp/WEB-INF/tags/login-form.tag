<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="action" required="true" %>
<%@ attribute name="method" required="true" %>
<%@ attribute name="buttonLabel" required="true" %>

<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>

<form action="${action}" method="${method}" class="p-4 border rounded shadow-sm bg-light">

    <!-- email -->
    <div class="mb-3">
        <label for="email" class="form-label">email / Email</label>
        <input
            type="text"
            name="email"
            id="email"
            class="form-control"
            placeholder="Enter email or email"
            autocomplete="email"
            required />
    </div>

    <!-- Password -->
    <div class="mb-3">
        <label for="password" class="form-label">Password</label>
        <input
            type="password"
            name="password"
            id="password"
            class="form-control"
            placeholder="Enter password"
            autocomplete="current-password"
            required />
    </div>

    <!-- Remember Me -->
    <div class="form-check mb-3">
        <input
            class="form-check-input"
            type="checkbox"
            value="true"
            id="rememberMe"
            name="rememberMe">
        <label class="form-check-label" for="rememberMe">
            Remember me
        </label>
    </div>

    <!-- Submit -->
    <div class="d-grid">
        <button type="submit" class="btn btn-primary">
            ${buttonLabel}
        </button>
    </div>

</form>