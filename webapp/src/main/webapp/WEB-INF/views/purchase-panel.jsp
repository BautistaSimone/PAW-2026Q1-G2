<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<ui:layout title="Vinyland | Gestionar Compra">
    <div class="purchase-page">
        <div class="container">
            <div class="row justify-content-center">
                <div class="col-lg-8 col-xl-7">
                    <nav aria-label="breadcrumb" class="mb-3">
                        <ol class="breadcrumb">
                            <li class="breadcrumb-item"><a href="<c:url value='/'/>">Inicio</a></li>
                            <c:if test="${isBuyer}">
                                <li class="breadcrumb-item"><a href="<c:url value='/profile'/>">Mi Perfil</a></li>
                            </c:if>
                            <li class="breadcrumb-item active" aria-current="page">Pedido #<c:out value="${purchase.purchaseId}"/></li>
                        </ol>
                    </nav>
                    <c:if test="${param.updated eq '1'}">
                        <div class="alert-retro alert-retro-success mb-3">
                            <i class="bi bi-check-circle" aria-hidden="true"></i> Estado actualizado correctamente.
                        </div>
                    </c:if>
                    <c:if test="${param.reviewed eq '1'}">
                        <div class="alert-retro alert-retro-success mb-3">
                            <i class="bi bi-star-fill" aria-hidden="true"></i> Tu reseña fue enviada. ¡Gracias!
                        </div>
                    </c:if>

                    <div class="purchase-card">
                        <div class="purchase-card-header">
                            <h4><i class="bi bi-receipt" aria-hidden="true"></i> Pedido #<c:out value="${purchase.purchaseId}"/></h4>
                            <span class="purchase-status-badge">${purchase.status.description}</span>
                        </div>
                        <ui:purchase-stepper status="${purchase.status}" />
                        <div class="purchase-card-body">

                            <div class="purchase-product-row">
                                <img src="<c:url value='/images/product/${product.id}'/>"
                                     alt="Item"
                                     class="purchase-product-img"
                                     onerror="this.src='https://via.placeholder.com/150?text=Sin+Imagen';"/>
                                <div class="purchase-product-info">
                                    <h5><c:out value="${product.title}"/></h5>
                                    <p class="purchase-artist"><c:out value="${product.artist}"/></p>
                                    <div class="purchase-price">$<c:out value="${product.price}"/></div>
                                </div>
                            </div>

                            <div class="d-flex flex-column gap-3">
                                <!-- Buyer Actions -->
                                <c:if test="${isBuyer}">
                                    <h5 class="purchase-actions-section" style="color: var(--color-accent);">
                                        <i class="bi bi-person-fill" aria-hidden="true"></i> Panel de Comprador
                                    </h5>
                                    <c:choose>
                                        <c:when test="${purchase.status eq 'PENDING'}">
                                            <div class="alert-retro alert-retro-info">
                                                <i class="bi bi-info-circle" aria-hidden="true"></i>
                                                Abona la suma de $<c:out value="${product.price}"/> acordandolo con el vendedor. Una vez transferido, haz clic abajo.
                                            </div>
                                            <c:url var="statusPostUrl" value='/purchases/${purchase.purchaseId}/status'/>
                                            <form:form modelAttribute="purchaseStatusForm" method="POST" action="${statusPostUrl}">
                                                <input type="hidden" name="token" value="${token}" />
                                                <input type="hidden" name="newStatus" value="PAID" />
                                                <button type="submit" class="btn btn-retro btn-retro-primary w-100 btn-lg">
                                                    <i class="bi bi-credit-card" aria-hidden="true"></i> Notificar que ya he Pagado
                                                </button>
                                                <form:errors path="newStatus" cssClass="text-danger mt-2" element="div" />
                                            </form:form>
                                        </c:when>
                                        <c:when test="${purchase.status eq 'SHIPPED'}">
                                            <div class="alert-retro alert-retro-warning">
                                                <i class="bi bi-truck" aria-hidden="true"></i>
                                                El vendedor ya envio el pedido. Cuando tengas tu vinilo en tus manos, confirmalo.
                                            </div>
                                            <c:url var="statusPostUrl" value='/purchases/${purchase.purchaseId}/status'/>
                                            <form:form modelAttribute="purchaseStatusForm" method="POST" action="${statusPostUrl}">
                                                <input type="hidden" name="token" value="${token}" />
                                                <input type="hidden" name="newStatus" value="DELIVERED" />
                                                <button type="submit" class="btn btn-retro btn-retro-primary w-100 btn-lg" style="background: #2e7d32;">
                                                    <i class="bi bi-check2-circle" aria-hidden="true"></i> Marcar como Recibido
                                                </button>
                                                <form:errors path="newStatus" cssClass="text-danger mt-2" element="div" />
                                            </form:form>
                                        </c:when>
                                        <c:when test="${purchase.status eq 'DELIVERED'}">
                                            <div class="alert-retro alert-retro-success text-center" style="padding: 1.5rem;">
                                                <i class="bi bi-check-circle-fill" style="font-size: 1.5rem;" aria-hidden="true"></i>
                                                <br/>Compra completada exitosamente!
                                            </div>
                                            <c:if test="${not hasReview}">
                                                <a href="<c:url value='/purchases/${purchase.purchaseId}/review?token=${token}'/>"
                                                   class="btn btn-retro btn-retro-primary w-100 btn-lg mt-3">
                                                    <i class="bi bi-star" aria-hidden="true"></i> Dejar reseña al vendedor
                                                </a>
                                            </c:if>
                                        </c:when>
                                        <c:otherwise>
                                            <p style="color: var(--color-text-muted); text-align: center;">Esperando que el vendedor actualice el estado...</p>
                                        </c:otherwise>
                                    </c:choose>
                                </c:if>

                                <!-- Seller Actions -->
                                <c:if test="${isSeller}">
                                    <h5 class="purchase-actions-section" style="color: #2e7d32;">
                                        <i class="bi bi-shop" aria-hidden="true"></i> Panel de Vendedor
                                    </h5>
                                    <c:choose>
                                        <c:when test="${purchase.status eq 'PAID'}">
                                            <div class="alert-retro alert-retro-info">
                                                <i class="bi bi-info-circle" aria-hidden="true"></i>
                                                El comprador dice que ya pago. Verifica en tu cuenta bancaria y confirma recepcion.
                                            </div>
                                            <c:url var="statusPostUrl" value='/purchases/${purchase.purchaseId}/status'/>
                                            <form:form modelAttribute="purchaseStatusForm" method="POST" action="${statusPostUrl}">
                                                <input type="hidden" name="token" value="${token}" />
                                                <input type="hidden" name="newStatus" value="SHIPPED" />
                                                <button type="submit" class="btn btn-retro btn-retro-primary w-100 btn-lg">
                                                    <i class="bi bi-truck" aria-hidden="true"></i> Confirmar Pago y Marcar Enviado
                                                </button>
                                                <form:errors path="newStatus" cssClass="text-danger mt-2" element="div" />
                                            </form:form>
                                        </c:when>
                                        <c:when test="${purchase.status eq 'DELIVERED'}">
                                            <div class="alert-retro alert-retro-success text-center" style="padding: 1.5rem;">
                                                <i class="bi bi-check-circle-fill" style="font-size: 1.5rem;" aria-hidden="true"></i>
                                                <br/>El comprador confirmo recepcion. Finita!
                                            </div>
                                        </c:when>
                                        <c:otherwise>
                                            <p style="color: var(--color-text-muted); text-align: center;">Esperando accion del comprador...</p>
                                        </c:otherwise>
                                    </c:choose>
                                </c:if>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</ui:layout>
