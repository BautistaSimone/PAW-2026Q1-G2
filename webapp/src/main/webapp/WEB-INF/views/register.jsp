<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>

<ui:layout title="Vinyland | Register">
    <div class="container">
        <div class="row justify-content-center">
            <div class="col-12 col-md-6 col-lg-4">
                <ui:register-form 
                    action="${pageContext.request.contextPath}/register"
                    method="POST"
                    buttonLabel="Register"
                />
            </div>
        </div>
    </div>
</ui:layout>