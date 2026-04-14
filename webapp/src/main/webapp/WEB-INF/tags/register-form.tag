<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="action" required="true" %>
<%@ attribute name="method" required="true" %>
<%@ attribute name="buttonLabel" required="true" %>

<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>

<form action="${action}" method="${method}" class="p-4 border rounded shadow-sm bg-light">

    <!-- Username -->
    <div class="mb-3">
        <label for="username" class="form-label">Username</label>
        <input
            type="text"
            name="username"
            id="username"
            class="form-control"
            placeholder="Enter username"
            autocomplete="username"
            required />
    </div>

    <!-- Email -->
    <div class="mb-3">
        <label for="email" class="form-label">Email</label>
        <input
            type="email"
            name="email"
            id="email"
            class="form-control"
            placeholder="Enter email"
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
            autocomplete="new-password"
            required />
    </div>

    <!-- Confirm Password -->
    <div class="mb-3">
        <label for="confirmPassword" class="form-label">Confirm Password</label>
        <input
            type="password"
            name="confirmPassword"
            id="confirmPassword"
            class="form-control"
            placeholder="Repeat password"
            autocomplete="new-password"
            required />
    </div>

    <!-- Submit -->
    <div class="d-grid">
        <button type="submit" class="btn btn-success">
            ${buttonLabel}
        </button>
    </div>

</form>