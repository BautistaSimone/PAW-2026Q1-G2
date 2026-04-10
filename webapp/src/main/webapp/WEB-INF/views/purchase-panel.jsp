<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<ui:layout title="Vinyland | Gestionar Compra">
    <div class="container py-5">
        <div class="row justify-content-center">
            <div class="col-md-8">
                <c:if test="${param.updated eq '1'}">
                    <div class="alert alert-success mt-3">Estado actualizado correctamente.</div>
                </c:if>

                <div class="card shadow-sm">
                    <div class="card-header bg-dark text-white d-flex justify-content-between align-items-center">
                        <h4 class="mb-0">Pedido #<c:out value="${purchase.purchaseId}"/></h4>
                        <span class="badge bg-secondary">${purchase.status.description}</span>
                    </div>
                    <div class="card-body p-4">

                        <div class="row mb-4">
                            <div class="col-md-3">
                                <img src="<c:url value='/images/product/${product.id}'/>" 
                                     alt="Item" 
                                     class="img-fluid rounded border" 
                                     onerror="this.src='https://via.placeholder.com/150?text=Sin+Imagen';"/>
                            </div>
                            <div class="col-md-9">
                                <h5><c:out value="${product.title}"/></h5>
                                <p class="text-muted mb-1"><c:out value="${product.artist}"/></p>
                                <h4 class="fw-bold">$<c:out value="${product.price}"/></h4>
                            </div>
                        </div>

                        <hr>

                        <div class="d-flex flex-column gap-3 mt-4">
                            <!-- Buyer Actions -->
                            <c:if test="${isBuyer}">
                                <h5 class="text-primary"><i class="bi bi-person-fill"></i> Panel de Comprador</h5>
                                <c:choose>
                                    <c:when test="${purchase.status eq 'PENDING'}">
                                        <div class="alert alert-info">
                                            Abona la suma de $<c:out value="${product.price}"/> acordandolo con el vendedor. Una vez transferido, haz clic abajo.
                                        </div>
                                        <c:url var="statusPostUrl" value='/purchases/${purchase.purchaseId}/status'/>
                                        <form:form modelAttribute="purchaseStatusForm" method="POST" action="${statusPostUrl}">
                                            <input type="hidden" name="token" value="${token}" />
                                            <input type="hidden" name="newStatus" value="PAID" />
                                            <button type="submit" class="btn btn-primary w-100 btn-lg">Notificar que ya he Pagado</button>
                                            <form:errors path="newStatus" cssClass="text-danger mt-2" element="div" />
                                        </form:form>
                                    </c:when>
                                    <c:when test="${purchase.status eq 'SHIPPED'}">
                                        <div class="alert alert-warning">
                                            El vendedor ya envíó el pedido. Cuando tengas tu vinilo en tus manos, confirmalo.
                                        </div>
                                        <c:url var="statusPostUrl" value='/purchases/${purchase.purchaseId}/status'/>
                                        <form:form modelAttribute="purchaseStatusForm" method="POST" action="${statusPostUrl}">
                                            <input type="hidden" name="token" value="${token}" />
                                            <input type="hidden" name="newStatus" value="DELIVERED" />
                                            <button type="submit" class="btn btn-success w-100 btn-lg">Marcar como Recibido</button>
                                            <form:errors path="newStatus" cssClass="text-danger mt-2" element="div" />
                                        </form:form>
                                    </c:when>
                                    <c:when test="${purchase.status eq 'DELIVERED'}">
                                        <div class="alert alert-success mb-0 text-center">
                                            <i class="bi bi-check-circle-fill"></i> ¡Compra completada exitosamente!
                                        </div>
                                    </c:when>
                                    <c:otherwise>
                                        <p class="text-muted text-center mb-0">Esperando que el vendedor actualice el estado...</p>
                                    </c:otherwise>
                                </c:choose>
                            </c:if>

                            <!-- Seller Actions -->
                            <c:if test="${isSeller}">
                                <h5 class="text-success"><i class="bi bi-shop"></i> Panel de Vendedor</h5>
                                <c:choose>
                                    <c:when test="${purchase.status eq 'PAID'}">
                                        <div class="alert alert-info">
                                            El comprador dice que ya pagó. Verifica en tu cuenta bancaria y confirma recepción.
                                        </div>
                                        <c:url var="statusPostUrl" value='/purchases/${purchase.purchaseId}/status'/>
                                        <form:form modelAttribute="purchaseStatusForm" method="POST" action="${statusPostUrl}">
                                            <input type="hidden" name="token" value="${token}" />
                                            <input type="hidden" name="newStatus" value="SHIPPED" />
                                            <button type="submit" class="btn btn-primary w-100 btn-lg">Confirmar Pago y Marcar Enviado</button>
                                            <form:errors path="newStatus" cssClass="text-danger mt-2" element="div" />
                                        </form:form>   
                                    </c:when>
                                    <c:when test="${purchase.status eq 'DELIVERED'}">
                                        <div class="alert alert-success mb-0 text-center">
                                            <i class="bi bi-check-circle-fill"></i> ¡El comprador confirmó recepción. Finita!
                                        </div>
                                    </c:when>
                                    <c:otherwise>
                                        <p class="text-muted text-center mb-0">Esperando accion del comprador...</p>
                                    </c:otherwise>
                                </c:choose>
                            </c:if>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</ui:layout>
