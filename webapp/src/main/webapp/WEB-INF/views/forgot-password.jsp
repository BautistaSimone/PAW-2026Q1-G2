<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<ui:layout title="Vinyland | Reset Password" bodyClass="auth-page-bg">

    <ui:header showHeaderActions="false"/>

    <div class="container d-flex justify-content-center align-items-center" style="min-height: 70vh;">
        <div class="card shadow-sm p-4" style="width: 100%; max-width: 420px;">

            <h4 class="text-center mb-4">Recuperar contraseña</h4>

            <p class="text-muted text-center">
                Ingresa tu email y te enviaremos un enlace para restablecer tu contraseña.
            </p>

            <form action="${pageContext.request.contextPath}/resetPassword" method="POST">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
                <div class="mb-3">
                    <label class="form-label">Email</label>
                    <input type="email"
                           name="email"
                           value="${userEmail}"
                           class="form-control"
                           placeholder="tu@email.com"
                           required />
                </div>

                <!-- Error message -->
                <c:if test="${not empty error}">
                    <div class="alert-retro alert-retro-warning mb-3" role="alert">
                        <i class="bi bi-exclamation-triangle" aria-hidden="true"></i>
                        <spring:message code = "UserNotFound.authForm.email" />
                    </div>
                </c:if>

                <button type="submit" class="btn-accent w-100">
                    Enviar enlace
                </button>

            </form>

            <div class="text-center mt-3">
                <a href="${pageContext.request.contextPath}/login">
                    Volver
                </a>
            </div>

        </div>
    </div>

</ui:layout>