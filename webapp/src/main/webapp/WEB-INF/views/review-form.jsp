<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<ui:layout title="Vinyland | Dejar Reseña">
    <div class="purchase-page">
        <div class="container">
            <div class="row justify-content-center">
                <div class="col-lg-7 col-xl-6">
                    <div class="mb-3 d-flex flex-wrap align-items-center gap-3">
                        <a href="<c:url value='/purchases/${purchase.purchaseId}?token=${token}'/>"
                           style="display: inline-flex; align-items: center; gap: 0.25rem; color: var(--color-text-muted); font-weight: 500; text-decoration: none; transition: color 0.2s;"
                           onmouseover="this.style.color='var(--color-accent)';" onmouseout="this.style.color='var(--color-text-muted)';">
                            <i class="bi bi-arrow-left" aria-hidden="true" style="font-size: 1.1rem;"></i>
                            <span>Volver al pedido</span>
                        </a>
                        <span style="color: var(--color-border);">|</span>
                        <a href="<c:url value='/'/>"
                           style="display: inline-flex; align-items: center; gap: 0.25rem; color: var(--color-text-muted); font-weight: 500; text-decoration: none; transition: color 0.2s;"
                           onmouseover="this.style.color='var(--color-accent)';" onmouseout="this.style.color='var(--color-text-muted)';">
                            <i class="bi bi-house" aria-hidden="true" style="font-size: 1rem;"></i>
                            <span>Inicio</span>
                        </a>
                    </div>

                    <div class="purchase-card">
                        <div class="purchase-card-header">
                            <h4><i class="bi bi-star" aria-hidden="true"></i> Reseñar al vendedor</h4>
                        </div>
                        <div class="purchase-card-body">

                            <div class="purchase-product-row mb-4">
                                <img src="<c:url value='/images/product/${product.id}'/>"
                                     alt="Item"
                                     class="purchase-product-img"
                                     onerror="this.src='https://via.placeholder.com/150?text=Sin+Imagen';"/>
                                <div class="purchase-product-info">
                                    <h5><c:out value="${product.title}"/></h5>
                                    <p class="purchase-artist"><c:out value="${product.artist}"/></p>
                                    <p style="color: var(--color-text-muted); margin: 0; font-size: 0.9rem;">
                                        Vendedor: <strong><c:out value="${seller.username}"/></strong>
                                    </p>
                                </div>
                            </div>

                            <c:url var="reviewPostUrl" value="/purchases/${purchase.purchaseId}/review">
                                <c:param name="token" value="${token}"/>
                            </c:url>
                            <form:form modelAttribute="reviewForm" method="POST" action="${reviewPostUrl}">

                                <div class="mb-4">
                                    <label class="form-label" style="font-weight: 600; color: var(--color-text-main); font-size: 1rem;">
                                        Puntuación
                                    </label>
                                    <div class="star-rating-input" role="radiogroup" aria-label="Puntuación del vendedor">
                                        <c:forEach begin="1" end="5" var="i">
                                            <input type="radio" name="score" id="star${i}" value="${i}"
                                                   class="star-radio" ${reviewForm.score != null && reviewForm.score == i ? 'checked' : ''}/>
                                            <label for="star${i}" class="star-label" title="${i} estrella${i > 1 ? 's' : ''}">
                                                <i class="bi bi-star-fill"></i>
                                            </label>
                                        </c:forEach>
                                    </div>
                                    <form:errors path="score" cssClass="text-danger mt-1" element="div" />
                                </div>

                                <div class="mb-4">
                                    <label for="reviewText" class="form-label" style="font-weight: 600; color: var(--color-text-main); font-size: 1rem;">
                                        Comentario <span style="color: var(--color-text-muted); font-weight: 400;">(opcional)</span>
                                    </label>
                                    <form:textarea path="text" id="reviewText" cssClass="form-control"
                                                   rows="4" maxlength="2000"
                                                   placeholder="Contá tu experiencia con este vendedor..."
                                                   style="resize: vertical; border-radius: 12px; border: 1.5px solid var(--color-border); padding: 1rem;"/>
                                    <form:errors path="text" cssClass="text-danger mt-1" element="div" />
                                </div>

                                <button type="submit" class="btn btn-retro btn-retro-primary w-100 btn-lg">
                                    <i class="bi bi-send" aria-hidden="true"></i> Enviar reseña
                                </button>
                            </form:form>

                            <div class="text-center mt-3">
                                <a href="<c:url value='/purchases/${purchase.purchaseId}?token=${token}'/>"
                                   style="color: var(--color-text-muted); text-decoration: none; font-size: 0.9rem;">
                                    Omitir por ahora
                                </a>
                            </div>
                        </div>
                    </div>

                </div>
            </div>
        </div>
    </div>

    <style>
        .star-rating-input {
            display: flex;
            gap: 0.25rem;
            flex-direction: row;
        }
        .star-radio {
            position: absolute;
            opacity: 0;
            width: 0;
            height: 0;
        }
        .star-label {
            cursor: pointer;
            font-size: 2rem;
            color: #d4d0cb;
            transition: color 0.15s ease, transform 0.15s ease;
            line-height: 1;
        }
        .star-label:hover,
        .star-label:hover ~ .star-label {
            color: #d4d0cb;
        }
        .star-radio:checked ~ .star-label {
            color: #d4d0cb;
        }
        .star-radio:checked + .star-label,
        .star-label:hover {
            color: var(--color-accent, #e76f51);
            transform: scale(1.15);
        }
        /* Highlight all stars up to and including hovered/checked */
        .star-rating-input:has(.star-radio:checked) .star-label {
            color: #d4d0cb;
        }
        .star-rating-input .star-radio:checked + .star-label,
        .star-rating-input .star-radio:checked + .star-label ~ .star-label {
            color: #d4d0cb;
        }
    </style>

    <script>
    (function() {
        var radios = document.querySelectorAll('.star-radio');
        var labels = document.querySelectorAll('.star-label');

        function updateStars() {
            var checkedVal = 0;
            radios.forEach(function(r) { if (r.checked) checkedVal = parseInt(r.value); });
            labels.forEach(function(lbl, idx) {
                lbl.style.color = (idx < checkedVal) ? 'var(--color-accent, #e76f51)' : '#d4d0cb';
                lbl.style.transform = '';
            });
        }

        radios.forEach(function(r) { r.addEventListener('change', updateStars); });

        labels.forEach(function(lbl) {
            lbl.addEventListener('mouseenter', function() {
                var idx = Array.prototype.indexOf.call(labels, lbl);
                labels.forEach(function(l, i) {
                    l.style.color = (i <= idx) ? 'var(--color-accent, #e76f51)' : '#d4d0cb';
                });
                lbl.style.transform = 'scale(1.15)';
            });
            lbl.addEventListener('mouseleave', function() {
                lbl.style.transform = '';
                updateStars();
            });
        });

        updateStars();
    })();
    </script>
</ui:layout>
