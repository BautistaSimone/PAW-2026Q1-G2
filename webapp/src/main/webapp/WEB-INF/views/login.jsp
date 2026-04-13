<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html>
<head>
    <title>Login</title>

    <!-- Bootstrap CDN -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">

    <meta name="viewport" content="width=device-width, initial-scale=1">
</head>

<body class="bg-light">

<div class="container d-flex justify-content-center align-items-center" style="min-height: 100vh;">

    <div class="card shadow-sm" style="width: 380px;">

        <div class="card-body">

            <h4 class="text-center mb-4">Login</h4>

            <!-- Error message (Spring Security) -->
            <c:if test="${param.error != null}">
                <div class="alert alert-danger" role="alert">
                    Invalid username or password
                </div>
            </c:if>

            <!-- Logout message -->
            <c:if test="${param.logout != null}">
                <div class="alert alert-success" role="alert">
                    You have been logged out
                </div>
            </c:if>

            <form method="post" action="${pageContext.request.contextPath}/login">

                <!-- CSRF token (IMPORTANT for Spring Security) -->
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />

                <div class="mb-3">
                    <label class="form-label">Username</label>
                    <input type="text"
                           name="username"
                           class="form-control"
                           placeholder="Enter username"
                           required />
                </div>

                <div class="mb-3">
                    <label class="form-label">Password</label>
                    <input type="password"
                           name="password"
                           class="form-control"
                           placeholder="Enter password"
                           required />
                </div>

                <div class="form-check mb-3">
                    <input class="form-check-input"
                           type="checkbox"
                           name="remember-me"
                           id="remember-me">
                    <label class="form-check-label" for="remember-me">
                        Remember me
                    </label>
                </div>

                <button type="submit" class="btn btn-primary w-100">
                    Sign in
                </button>

            </form>

        </div>
    </div>

</div>

</body>
</html>