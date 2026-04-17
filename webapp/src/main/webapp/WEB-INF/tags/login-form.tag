<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="action" required="true" %>
<%@ attribute name="method" required="true" %>
<%@ attribute name="buttonLabel" required="true" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix = "spring" uri = "http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<div class="container py-5" style="min-height: 70vh; display: flex; align-items: center;">
    <div class="row justify-content-center w-100">
        <div class="col-12 col-sm-10 col-md-7 col-lg-5 col-xl-4">
            <div class="auth-card">
                <div class="auth-card-header">
                    <h2><i class="bi bi-vinyl" aria-hidden="true"></i> Vinyland</h2>
                    <p>Inicia sesion para comprar y vender vinilos</p>
                </div>
                <div class="auth-card-body">
                    <c:if test="${param.moderated eq '1'}">
                        <div class="alert-retro alert-retro-success mb-3" role="alert">
                            <i class="bi bi-check-circle" aria-hidden="true"></i>
                            La publicación fue ocultada del catálogo.
                        </div>
                    </c:if>
                    <form:form modelAttribute="loginForm"
                            action="${action}"
                            method="${method}">

                        <!-- Email -->
                        <div class="mb-3">
                            <label for="email" class="form-label">Email</label>
                            <form:input
                                path="email"
                                id="email"
                                cssClass="form-control"
                                placeholder="tu@email.com"
                                autocomplete="email" />
                            <form:errors path="email" cssClass="text-danger small"/>
                        </div>

                        <!-- Password -->
                        <div class="mb-3">
                            <label for="password" class="form-label">Contraseña</label>
                            <form:password
                                path="password"
                                id="password"
                                cssClass="form-control"
                                placeholder="Tu contraseña"
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
                                Recordarme
                            </label>
                        </div>

                        <!-- Error message -->
                        <c:if test="${param.error != null}">
                            <div class="alert-retro alert-retro-warning mb-3" role="alert">
                                <i class="bi bi-exclamation-triangle" aria-hidden="true"></i>
                                <spring:message code = "Error.authForm.input" />
                            </div>
                        </c:if>

                        <!-- Submit -->
                        <div class="d-grid">
                            <button type="submit" class="btn-accent">
                                ${buttonLabel}
                            </button>
                        </div>
                    </form:form>
                </div>

                <!-- Not registered link -->
                <div class="auth-card-footer">
                    <a href="${pageContext.request.contextPath}/register">
                        <spring:message code = "NotRegistered.loginForm" />
                    </a>
                </div>
            </div>
        </div>
    </div>
</div>
