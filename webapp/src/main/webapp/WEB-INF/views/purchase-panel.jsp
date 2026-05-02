<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<spring:message code="PurchasePanel.title" var="purchaseTitle" />
<ui:layout title="${purchaseTitle}">
    <ui:header showHeaderActions="true"/>
    <div class="purchase-page">
        <div class="container">
            <div class="row justify-content-center">
                <div class="col-lg-8 col-xl-7">
                    <nav aria-label="breadcrumb" class="mb-3">
                        <ol class="breadcrumb">
                            <li class="breadcrumb-item"><a href="<c:url value='/'/>"><spring:message code="PurchasePanel.breadcrumb.home" /></a></li>
                            <li class="breadcrumb-item"><a href="<c:url value='/profile'/>"><spring:message code="PurchasePanel.breadcrumb.profile" /></a></li>
                            <c:choose>
                                <c:when test="${isBuyer}">
                                    <li class="breadcrumb-item"><a href="<c:url value='/profile#purchases'/>"><spring:message code="PurchasePanel.breadcrumb.purchases" /></a></li>
                                </c:when>
                                <c:when test="${isSeller}">
                                    <li class="breadcrumb-item"><a href="<c:url value='/profile#sales'/>"><spring:message code="PurchasePanel.breadcrumb.sales" /></a></li>
                                </c:when>
                            </c:choose>
                            <li class="breadcrumb-item active" aria-current="page"><spring:message code="PurchasePanel.order.id" arguments="${purchase.purchaseId}" /></li>
                        </ol>
                    </nav>
                    <c:if test="${param.updated eq '1'}">
                        <div class="alert-retro alert-retro-success mb-3">
                            <i class="bi bi-check-circle" aria-hidden="true"></i> <spring:message code="PurchasePanel.alert.updated" />
                        </div>
                    </c:if>
                    <c:if test="${param.reviewed eq '1'}">
                        <div class="alert-retro alert-retro-success mb-3">
                            <i class="bi bi-star-fill" aria-hidden="true"></i> <spring:message code="PurchasePanel.alert.reviewed" />
                        </div>
                    </c:if>

                    <div class="purchase-card">
                        <div class="purchase-card-header">
                            <h4><i class="bi bi-receipt" aria-hidden="true"></i> <spring:message code="PurchasePanel.order.id" arguments="${purchase.purchaseId}" /></h4>
                            <span class="purchase-status-badge"><c:out value="${purchase.status.description}"/></span>
                        </div>
                        <ui:purchase-stepper status="${purchase.status}" />
                        <div class="purchase-card-body">

                            <div class="purchase-product-row">
                                <img src="<c:url value='/images/product/${product.id}'/>"
                                     alt=""
                                     class="purchase-product-img"
                                     onerror="this.onerror=null;this.src='data:image/svg+xml,%3Csvg xmlns=\'http://www.w3.org/2000/svg\' width=\'150\' height=\'150\' viewBox=\'0 0 150 150\'%3E%3Crect width=\'150\' height=\'150\' fill=\'%23e9e4dc\'/%3E%3Ctext x=\'75\' y=\'80\' text-anchor=\'middle\' font-size=\'40\' fill=\'%23b0a898\'%3E♪%3C/text%3E%3C/svg%3E';"/>
                                <div class="purchase-product-info">
                                    <h5><c:out value="${product.title}"/></h5>
                                    <p class="purchase-artist"><c:out value="${product.artist}"/></p>
                                    <div class="purchase-price"><ui:price value="${product.price}" /></div>
                                </div>
                            </div>

                            <div class="purchase-card-inset">
                                <dl class="purchase-summary-dl">
                                    <div>
                                        <dt><spring:message code="PurchasePanel.summary.date" /></dt>
                                        <dd><c:out value="${purchase.date}"/></dd>
                                    </div>

                                </dl>
                            </div>

                            <c:if test="${not empty product.recordLabel or not empty product.catalogNumber or not empty product.editionCountry or product.sleeveCondition ne null or product.recordCondition ne null}">
                                <div class="purchase-card-inset">
                                    <p class="purchase-inset-title"><spring:message code="PurchasePanel.details.title" /></p>
                                    <ul class="purchase-inset-list mb-0">
                                        <c:if test="${not empty product.recordLabel or not empty product.catalogNumber}">
                                            <li><strong><spring:message code="PurchasePanel.details.labelCatalog" /></strong>
                                                <c:out value="${product.recordLabel}"/>
                                                <c:if test="${not empty product.recordLabel and not empty product.catalogNumber}"> — </c:if>
                                                <c:out value="${product.catalogNumber}"/>
                                            </li>
                                        </c:if>
                                        <c:if test="${not empty product.editionCountry}">
                                            <li><strong><spring:message code="PurchasePanel.details.origin" /></strong> <c:out value="${product.editionCountry}"/></li>
                                        </c:if>
                                        <c:if test="${product.sleeveCondition ne null or product.recordCondition ne null}">
                                            <li><strong><spring:message code="PurchasePanel.details.status" /></strong>
                                                <c:if test="${product.sleeveCondition ne null}"><c:out value="${product.sleeveCondition}"/>/10 <spring:message code="PurchasePanel.details.status.sleeve" /></c:if>
                                                <c:if test="${product.sleeveCondition ne null and product.recordCondition ne null}"> · </c:if>
                                                <c:if test="${product.recordCondition ne null}"><c:out value="${product.recordCondition}"/>/10 <spring:message code="PurchasePanel.details.status.record" /></c:if>
                                            </li>
                                        </c:if>
                                    </ul>
                                </div>
                            </c:if>

                            <div class="d-flex flex-column gap-3">
                                <c:if test="${isBuyer}">
                                    <h5 class="purchase-actions-section" style="color: var(--color-accent);">
                                        <i class="bi bi-person-fill" aria-hidden="true"></i> <spring:message code="PurchasePanel.buyer.panel" />
                                    </h5>
                                    <c:choose>
                                        <c:when test="${purchase.status eq 'PENDING'}">
                                            <div class="alert-retro alert-retro-info">
                                                <p class="mb-2"><i class="bi bi-info-circle" aria-hidden="true"></i>
                                                    <spring:message code="PurchasePanel.buyer.pending.transfer" arguments="${product.price}" /></p>
                                                <ul class="purchase-inset-list mb-0">
                                                    <li><strong><spring:message code="PurchasePanel.buyer.pending.user" /></strong> <c:out value="${orderSeller.username}" default="—"/></li>
                                                    <li><strong><spring:message code="PurchasePanel.buyer.pending.email" /></strong>
                                                        <a href="mailto:<c:out value='${orderSeller.email}'/>" style="color: inherit; font-weight: 600;"><c:out value="${orderSeller.email}"/></a>
                                                    </li>
                                                    <c:choose>
                                                        <c:when test="${not empty orderSeller.cbuCvu}">
                                                            <li><strong><spring:message code="PurchasePanel.buyer.pending.cbu" /></strong> <code class="purchase-mono"><c:out value="${orderSeller.cbuCvu}"/></code></li>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <li style="color: #b45309;"><strong><spring:message code="PurchasePanel.buyer.pending.cbu" /></strong> <spring:message code="PurchasePanel.buyer.pending.cbuEmpty" /></li>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </ul>
                                            </div>
                                            <c:url var="statusPostUrl" value='/purchases/${purchase.purchaseId}/status'/>
                                            <form:form modelAttribute="purchaseStatusForm" method="POST" action="${statusPostUrl}" data-single-submit="true">
                                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
                                                <input type="hidden" name="token" value="${token}" />
                                                <input type="hidden" name="newStatus" value="PAID" />
                                                <button type="submit" class="btn btn-retro btn-retro-primary w-100 btn-lg">
                                                    <i class="bi bi-credit-card" aria-hidden="true"></i> <spring:message code="PurchasePanel.buyer.pending.notifyPaid" />
                                                </button>
                                                <form:errors path="newStatus" cssClass="text-danger mt-2" element="div" />
                                            </form:form>
                                        </c:when>
                                        <c:when test="${purchase.status eq 'PAID'}">
                                            <div class="alert-retro alert-retro-info">
                                                <i class="bi bi-hourglass-split" aria-hidden="true"></i>
                                                <spring:message code="PurchasePanel.buyer.paid.waiting" />
                                                <a href="mailto:${orderSeller.email}" style="color: inherit; font-weight: 600;"><c:out value="${orderSeller.email}"/></a>
                                                <c:if test="${not empty orderSeller.username}"> (<c:out value="${orderSeller.username}"/>)</c:if>.
                                            </div>
                                        </c:when>
                                        <c:when test="${purchase.status eq 'SHIPPED'}">
                                            <div class="alert-retro alert-retro-warning">
                                                <i class="bi bi-truck" aria-hidden="true"></i>
                                                <spring:message code="PurchasePanel.buyer.shipped.waiting" />
                                                <c:if test="${fn:length(orderBuyer.formattedShippingAddress) gt 0}">
                                                    <span class="d-block mt-2 small" style="opacity: 0.95;"><strong><spring:message code="PurchasePanel.buyer.shipped.address" /></strong> <c:out value="${orderBuyer.formattedShippingAddress}"/></span>
                                                </c:if>
                                            </div>
                                            <c:url var="statusPostUrl" value='/purchases/${purchase.purchaseId}/status'/>
                                            <form:form modelAttribute="purchaseStatusForm" method="POST" action="${statusPostUrl}" data-single-submit="true">
                                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
                                                <input type="hidden" name="token" value="${token}" />
                                                <input type="hidden" name="newStatus" value="DELIVERED" />
                                                <button type="submit" class="btn btn-retro btn-retro-primary w-100 btn-lg" style="background: #2e7d32;">
                                                    <i class="bi bi-check2-circle" aria-hidden="true"></i> <spring:message code="PurchasePanel.buyer.shipped.confirmDelivery" />
                                                </button>
                                                <form:errors path="newStatus" cssClass="text-danger mt-2" element="div" />
                                            </form:form>
                                        </c:when>
                                        <c:when test="${purchase.status eq 'DELIVERED'}">
                                            <div class="alert-retro alert-retro-success text-center" style="padding: 1.5rem;">
                                                <i class="bi bi-check-circle-fill" style="font-size: 1.5rem;" aria-hidden="true"></i>
                                                <br/><spring:message code="PurchasePanel.buyer.delivered.success" />
                                            </div>
                                            <c:if test="${not hasReview}">
                                                <a href="<c:url value='/purchases/${purchase.purchaseId}/review?token=${token}'/>"
                                                   class="btn btn-retro btn-retro-primary w-100 btn-lg mt-3">
                                                    <i class="bi bi-star" aria-hidden="true"></i> <spring:message code="PurchasePanel.buyer.delivered.review" />
                                                </a>
                                            </c:if>
                                        </c:when>
                                        <c:otherwise>
                                            <p style="color: var(--color-text-muted); text-align: center;"><spring:message code="PurchasePanel.buyer.waitingSeller" /></p>
                                        </c:otherwise>
                                    </c:choose>
                                </c:if>

                                <c:if test="${isSeller}">
                                    <h5 class="purchase-actions-section" style="color: #2e7d32;">
                                        <i class="bi bi-shop" aria-hidden="true"></i> <spring:message code="PurchasePanel.seller.panel" />
                                    </h5>
                                    <c:choose>
                                        <c:when test="${purchase.status eq 'PENDING'}">
                                            <div class="purchase-card-inset">
                                                <p class="purchase-inset-title"><spring:message code="PurchasePanel.seller.pending.title" /></p>
                                                <p class="mb-2" style="margin:0 0 0.5rem;font-size:0.95rem;"><spring:message code="PurchasePanel.seller.pending.amount" /> <strong style="color: var(--color-accent);"><ui:price value="${product.price}" /></strong></p>
                                                <ul class="purchase-inset-list mb-2">
                                                    <li><strong><spring:message code="PurchasePanel.seller.pending.buyer" /></strong> <c:out value="${orderBuyer.username}" default="—"/></li>
                                                    <li><strong><spring:message code="PurchasePanel.buyer.pending.email" /></strong> <a href="mailto:<c:out value='${orderBuyer.email}'/>" style="color: var(--color-accent); font-weight: 600;"><c:out value="${orderBuyer.email}"/></a></li>
                                                </ul>
                                                <p class="small text-muted mb-0"><spring:message code="PurchasePanel.seller.pending.help" /></p>
                                            </div>
                                        </c:when>
                                        <c:when test="${purchase.status eq 'PAID'}">
                                            <div class="purchase-card-inset purchase-card-inset--ship">
                                                <p class="purchase-inset-title"><spring:message code="PurchasePanel.seller.paid.sendTo" /></p>
                                                <ul class="purchase-inset-list mb-0">
                                                    <li><strong><spring:message code="PurchasePanel.seller.paid.name" /></strong> <c:out value="${orderBuyer.fullName}"/></li>
                                                    <c:choose>
                                                        <c:when test="${fn:length(orderBuyer.formattedShippingAddress) gt 0}">
                                                            <li><strong><spring:message code="PurchasePanel.seller.paid.address" /></strong> <c:out value="${orderBuyer.formattedShippingAddress}"/></li>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <li style="color: #b45309;"><spring:message code="PurchasePanel.seller.paid.addressEmpty" /></li>
                                                        </c:otherwise>
                                                    </c:choose>
                                                    <li><strong><spring:message code="PurchasePanel.buyer.pending.email" /></strong> <a href="mailto:<c:out value='${orderBuyer.email}'/>" style="color: inherit; font-weight: 600;"><c:out value="${orderBuyer.email}"/></a></li>
                                                </ul>
                                            </div>
                                            <div class="alert-retro alert-retro-info">
                                                <i class="bi bi-info-circle" aria-hidden="true"></i>
                                                <spring:message code="PurchasePanel.seller.paid.verify" />
                                            </div>
                                            <c:url var="statusPostUrl" value='/purchases/${purchase.purchaseId}/status'/>
                                            <form:form modelAttribute="purchaseStatusForm" method="POST" action="${statusPostUrl}" data-single-submit="true">
                                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
                                                <input type="hidden" name="token" value="${token}" />
                                                <input type="hidden" name="newStatus" value="SHIPPED" />
                                                <button type="submit" class="btn btn-retro btn-retro-primary w-100 btn-lg">
                                                    <i class="bi bi-truck" aria-hidden="true"></i> <spring:message code="PurchasePanel.seller.paid.confirmShip" />
                                                </button>
                                                <form:errors path="newStatus" cssClass="text-danger mt-2" element="div" />
                                            </form:form>
                                        </c:when>
                                        <c:when test="${purchase.status eq 'DELIVERED'}">
                                            <div class="alert-retro alert-retro-success text-center" style="padding: 1.5rem;">
                                                <i class="bi bi-check-circle-fill" style="font-size: 1.5rem;" aria-hidden="true"></i>
                                                <br/><spring:message code="PurchasePanel.seller.delivered.success" />
                                            </div>
                                        </c:when>
                                        <c:when test="${purchase.status eq 'SHIPPED'}">
                                            <p style="color: var(--color-text-muted); text-align: center; margin: 0;"><spring:message code="PurchasePanel.seller.shipped.waitingBuyer" /></p>
                                        </c:when>
                                        <c:otherwise>
                                            <p style="color: var(--color-text-muted); text-align: center;"><spring:message code="PurchasePanel.seller.waitingBuyer" /></p>
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
