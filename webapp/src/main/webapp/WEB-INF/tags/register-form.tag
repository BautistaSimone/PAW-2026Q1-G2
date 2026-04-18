<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="action" required="true" %>
<%@ attribute name="method" required="true" %>
<%@ attribute name="buttonLabel" required="true" %>

<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<div class="container py-5" style="min-height: 70vh; display: flex; align-items: center;">
    <div class="row justify-content-center w-100">
        <div class="col-12 col-sm-10 col-md-7 col-lg-5 col-xl-4">
            <div class="auth-card">
                <div class="auth-card-header">
                    <h2><i class="bi bi-vinyl" aria-hidden="true"></i> Crear cuenta</h2>
                    <p>Unite a la comunidad de coleccionistas</p>
                </div>
                <div class="auth-card-body">
                    <form:form modelAttribute="registerForm"
                            action="${action}"
                            method="${method}">

                        <!-- Username -->
                        <div class="mb-3">
                            <label for="username" class="form-label">Nombre de usuario</label>
                            <form:input path="username"
                                        id="username"
                                        cssClass="form-control"
                                        placeholder="Tu nombre de usuario"
                                        autocomplete="username" />
                            <form:errors path="username" cssClass="text-danger small"/>
                        </div>

                        <!-- Email -->
                        <div class="mb-3">
                            <label for="email" class="form-label">Email</label>
                            <form:input path="email"
                                        id="email"
                                        cssClass="form-control"
                                        placeholder="tu@email.com"
                                        autocomplete="email" />
                            <form:errors path="email" cssClass="text-danger small"/>
                        </div>

                        <!-- Password -->
                        <div class="mb-3">
                            <label for="password" class="form-label">Contraseña</label>
                            <div class="password-toggle-wrapper">
                                <form:password path="password"
                                            id="password"
                                            cssClass="form-control"
                                            placeholder="Crea una contraseña"
                                            autocomplete="new-password" />
                                <button type="button" class="password-toggle-btn" aria-label="Mostrar contraseña" onclick="togglePassword('password', this)">
                                    <i class="bi bi-eye"></i>
                                </button>
                            </div>
                            <form:errors path="password" cssClass="text-danger small"/>
                        </div>

                        <!-- Confirm Password -->
                        <div class="mb-3">
                            <label for="confirmPassword" class="form-label">Confirmar contraseña</label>
                            <div class="password-toggle-wrapper">
                                <form:password path="confirmPassword"
                                            id="confirmPassword"
                                            cssClass="form-control"
                                            placeholder="Repeti la contraseña"
                                            autocomplete="new-password" />
                                <button type="button" class="password-toggle-btn" aria-label="Mostrar contraseña" onclick="togglePassword('confirmPassword', this)">
                                    <i class="bi bi-eye"></i>
                                </button>
                            </div>
                            <form:errors path="confirmPassword" cssClass="text-danger small"/>
                        </div>

                        <script>
                            function togglePassword(inputId, btn) {
                                const input = document.getElementById(inputId);
                                const icon = btn.querySelector('i');
                                if (input.type === 'password') {
                                    input.type = 'text';
                                    icon.classList.remove('bi-eye');
                                    icon.classList.add('bi-eye-slash');
                                    btn.setAttribute('aria-label', 'Ocultar contraseña');
                                } else {
                                    input.type = 'password';
                                    icon.classList.remove('bi-eye-slash');
                                    icon.classList.add('bi-eye');
                                    btn.setAttribute('aria-label', 'Mostrar contraseña');
                                }
                            }
                        </script>

                        <!-- Submit -->
                        <div class="d-grid">
                            <button type="submit" class="btn-accent">
                                ${buttonLabel}
                            </button>
                        </div>
                    </form:form>
                </div>

                <div class="auth-card-footer">
                    <a href="${pageContext.request.contextPath}/login">
                        Ya tengo cuenta, iniciar sesion
                    </a>
                </div>
            </div>
        </div>
    </div>
</div>
