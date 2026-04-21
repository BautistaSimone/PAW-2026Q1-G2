<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="action" required="true" %>
<%@ attribute name="method" required="true" %>
<%@ attribute name="buttonLabel" required="true" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<div class="container py-5" style="min-height: 70vh; display: flex; align-items: center;">
    <div class="row justify-content-center w-100">
        <div class="col-12 col-sm-11 col-md-9 col-lg-7 col-xl-6">
            <div class="auth-card">
                <div class="auth-card-header">
                    <h2><i class="bi bi-vinyl" aria-hidden="true"></i> Crear cuenta</h2>
                    <p>Unite a la comunidad de coleccionistas</p>
                </div>
                <div class="auth-card-body">
                    <form:form modelAttribute="registerForm"
                            action="${action}"
                            method="${method}">
                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />

                        <div class="row g-2">
                            <div class="col-md-6 mb-3">
                                <label for="firstName" class="form-label">Nombre <span class="text-danger">*</span></label>
                                <form:input path="firstName" id="firstName" cssClass="form-control" placeholder="Nombre" autocomplete="given-name" />
                                <form:errors path="firstName" cssClass="text-danger small"/>
                            </div>
                            <div class="col-md-6 mb-3">
                                <label for="lastName" class="form-label">Apellido <span class="text-danger">*</span></label>
                                <form:input path="lastName" id="lastName" cssClass="form-control" placeholder="Apellido" autocomplete="family-name" />
                                <form:errors path="lastName" cssClass="text-danger small"/>
                            </div>
                        </div>

                        <div class="mb-3">
                            <label for="username" class="form-label">Nombre de usuario</label>
                            <form:input path="username"
                                        id="username"
                                        cssClass="form-control"
                                        placeholder="Tu nombre de usuario"
                                        autocomplete="username" />
                            <form:errors path="username" cssClass="text-danger small"/>
                        </div>

                        <div class="mb-3">
                            <label for="email" class="form-label">Email</label>
                            <form:input path="email"
                                        id="email"
                                        cssClass="form-control"
                                        placeholder="tu@email.com"
                                        autocomplete="email" />
                            <form:errors path="email" cssClass="text-danger small"/>
                        </div>

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

                        <hr class="my-4" style="border-color: var(--color-border); opacity: 0.6;" />
                        <p class="small text-muted mb-3" style="font-weight: 600;">Datos adicionales (opcional — podés omitirlos y completarlos después en tu perfil)</p>

                        <div class="mb-3">
                            <label for="streetName" class="form-label">Calle</label>
                            <form:input path="streetName" id="streetName" cssClass="form-control" placeholder="Nombre de la calle" />
                            <form:errors path="streetName" cssClass="text-danger small"/>
                        </div>
                        <div class="mb-3">
                            <label for="streetNumber" class="form-label">Número</label>
                            <form:input path="streetNumber" id="streetNumber" cssClass="form-control" placeholder="Número" />
                            <form:errors path="streetNumber" cssClass="text-danger small"/>
                        </div>
                        <div class="row g-2">
                            <div class="col-md-6 mb-3">
                                <label for="neighborhood" class="form-label">Barrio</label>
                                <form:input path="neighborhood" id="neighborhood" cssClass="form-control" placeholder="Barrio o localidad" />
                                <form:errors path="neighborhood" cssClass="text-danger small"/>
                            </div>
                            <div class="col-md-6 mb-3">
                                <label for="province" class="form-label">Provincia</label>
                                <form:input path="province" id="province" cssClass="form-control" placeholder="Provincia" />
                                <form:errors path="province" cssClass="text-danger small"/>
                            </div>
                        </div>
                        <div class="mb-3">
                            <label for="extraAddressInfo" class="form-label">Comentario (piso, depto, edificio…)</label>
                            <form:input path="extraAddressInfo" id="extraAddressInfo" cssClass="form-control" placeholder="Ej.: 3º B, timbre roto, etc." />
                            <form:errors path="extraAddressInfo" cssClass="text-danger small"/>
                        </div>
                        <div class="mb-3">
                            <label for="cbuCvu" class="form-label">CBU/CVU (22 dígitos)</label>
                            <form:input path="cbuCvu" id="cbuCvu" cssClass="form-control" placeholder="Opcional" inputmode="numeric" maxlength="22" />
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
                                    btn.setAttribute('aria-label', 'Ocultar contraseña');
                                } else {
                                    input.type = 'password';
                                    icon.classList.remove('bi-eye-slash');
                                    icon.classList.add('bi-eye');
                                    btn.setAttribute('aria-label', 'Mostrar contraseña');
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
                        Ya tengo cuenta, iniciar sesion
                    </a>
                </div>
            </div>
        </div>
    </div>
</div>
