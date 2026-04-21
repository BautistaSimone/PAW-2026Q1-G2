<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>

<ui:layout title="Vinyland | Reset Password" bodyClass="auth-page-bg">

    <ui:header showHeaderActions="false"/>

    <div class="container d-flex justify-content-center align-items-center" style="min-height: 70vh;">
        <div class="card shadow-sm p-4" style="width: 100%; max-width: 420px;">

            <h4 class="text-center mb-4">Restablecer contraseña</h4>

            <form action="${pageContext.request.contextPath}/changePassword" method="POST">

                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />

                <!-- token hidden field -->
                <input type="hidden" name="token" value="${token}" />

                <div class="mb-3">
                    <label class="form-label">Nueva contraseña</label>
                    <input type="password"
                            name="newPassword"
                            class="form-control"
                            placeholder="Nueva contraseña"
                            required />

                    <form:errors path="newPassword" cssClass="text-danger small"/>
                </div>

                <div class="mb-3">
                    <label class="form-label">Confirmar contraseña</label>
                    <input type="password"
                           name="newPasswordConfirm"
                           class="form-control"
                           placeholder="Confirmar contraseña"
                           required />

                    <form:errors path="newPasswordConfirm" cssClass="text-danger small"/>
                </div>

                <c:if test="${not empty error}">
                    <div class="alert alert-danger py-2">
                        <c:out value="${error}" />
                    </div>
                </c:if>

                <button type="submit" class="btn-accent w-100">
                    Cambiar contraseña
                </button>

            </form>

            <div class="text-center mt-3">
                <a href="${pageContext.request.contextPath}/login">Volver al login</a>
            </div>

        </div>
    </div>

</ui:layout>