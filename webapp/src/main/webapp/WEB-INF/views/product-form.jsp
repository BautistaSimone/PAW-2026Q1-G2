<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<ui:layout title="Vinyland | Publicar vinilo">

    <ui:header />

    <div class="sell-page">
        <div class="container py-4">
            <div class="sell-form-card">
                <div class="sell-form-header">
                    <span class="sell-form-eyebrow"><i class="bi bi-vinyl" aria-hidden="true"></i> Panel de vendedor</span>
                    <h1>Publicar un vinilo</h1>
                    <p>
                        Carga la informacion principal del disco para que quede disponible en el catalogo.
                    </p>
                </div>

                <c:url var="postUrl" value="/products"/>
                <form:form modelAttribute="productForm" action="${postUrl}" method="post" enctype="multipart/form-data" cssClass="sell-form" novalidate="novalidate">
                    <div class="row g-4">
                        <div class="col-md-6">
                            <label for="title" class="form-label">Titulo del album *</label>
                            <form:input path="title" cssClass="form-control" required="required" />
                            <form:errors path="title" cssClass="text-danger" element="div" />
                        </div>

                        <div class="col-md-6">
                            <label for="artist" class="form-label">Artista *</label>
                            <form:input path="artist" cssClass="form-control" required="required" />
                            <form:errors path="artist" cssClass="text-danger" element="div" />
                        </div>

                        <div class="col-12">
                            <label class="form-label">Sello – Numero de catalogo *</label>
                            <div class="input-group">
                                <form:input path="recordLabel" cssClass="form-control"
                                       placeholder="Ej: Sony Music, Rough Trade" required="required" />
                                <span class="input-group-text" style="border-color: var(--color-border);">–</span>
                                <form:input path="catalogNumber" cssClass="form-control"
                                       placeholder="Ej: EPC 85930" required="required" />
                            </div>
                            <form:errors path="recordLabel" cssClass="text-danger" element="div" />
                            <form:errors path="catalogNumber" cssClass="text-danger" element="div" />
                        </div>

                        <div class="col-md-6">
                            <label for="editionCountry" class="form-label">Pais de la Edicion *</label>
                            <form:input path="editionCountry" cssClass="form-control"
                                   placeholder="Ej: Argentina, USA, UK" required="required" />
                            <form:errors path="editionCountry" cssClass="text-danger" element="div" />
                        </div>

                        <div class="col-md-6">
                            <label class="form-label">Generos *</label>
                            <div class="genre-checkboxes d-flex flex-wrap gap-2">
                                <c:forEach items="${categories}" var="cat">
                                    <div class="form-check genre-check">
                                        <form:checkbox path="categories" value="${cat.id}" cssClass="form-check-input" id="cat-${cat.id}" />
                                        <label class="form-check-label" for="cat-${cat.id}">
                                            <c:out value="${cat.name}" />
                                        </label>
                                    </div>
                                </c:forEach>
                            </div>
                            <form:errors path="categories" cssClass="text-danger" element="div" />
                        </div>

                        <div class="col-md-6">
                            <label for="sleeveCondition" class="form-label">Estado de la tapa (1 a 10) *</label>
                            <form:input type="number" path="sleeveCondition" min="1" max="10" step="0.1"
                                   cssClass="form-control" placeholder="Ej: 8.5" required="required" />
                            <form:errors path="sleeveCondition" cssClass="text-danger" element="div" />
                        </div>

                        <div class="col-md-6">
                            <label for="recordCondition" class="form-label">Estado del disco (1 a 10) *</label>
                            <form:input type="number" path="recordCondition" min="1" max="10" step="0.1"
                                   cssClass="form-control" placeholder="Ej: 9.0" required="required" />
                            <form:errors path="recordCondition" cssClass="text-danger" element="div" />
                        </div>

                        <div class="col-md-6">
                            <label for="neighborhood" class="form-label">Barrio / Ciudad *</label>
                            <form:input path="neighborhood" cssClass="form-control"
                                   placeholder="Ej: Palermo" required="required" />
                            <form:errors path="neighborhood" cssClass="text-danger" element="div" />
                        </div>

                        <div class="col-md-6">
                            <label for="province" class="form-label">Provincia *</label>
                            <form:input path="province" cssClass="form-control"
                                   placeholder="Ej: Buenos Aires" required="required" />
                            <form:errors path="province" cssClass="text-danger" element="div" />
                        </div>

                        <div class="col-md-6">
                            <label for="price" class="form-label">Precio *</label>
                            <form:input type="number" path="price" min="0" step="0.01" cssClass="form-control" required="required" />
                            <form:errors path="price" cssClass="text-danger" element="div" />
                        </div>

                        <div class="col-12">
                            <label for="images" class="form-label">Imagenes del vinilo *</label>
                            <form:input type="file" path="images" cssClass="form-control sell-images-input" accept="image/*" multiple="true" id="images" />
                            <div class="form-text">Podes cargar varias fotos del disco (hasta 5 MB cada una). La imagen principal es la primera; toca las miniaturas de abajo para elegir otra como portada.</div>
                            <form:errors path="images" cssClass="text-danger" element="div" />
                            <div id="sell-images-preview" class="sell-images-preview" hidden>
                                <div class="sell-img-main-wrap">
                                    <img id="sell-img-main" class="sell-img-main" alt="Vista previa principal del vinilo" />
                                </div>
                                <div id="sell-img-thumbs" class="sell-img-thumbs" role="group" aria-label="Miniaturas para cambiar la imagen principal"></div>
                            </div>
                        </div>

                        <div class="col-12">
                            <label for="description" class="form-label">Descripcion</label>
                            <form:textarea path="description" cssClass="form-control" rows="5"
                                      placeholder="Conta detalles adicionales del disco, edicion, etc." />
                            <div class="form-text">Opcional.</div>
                            <form:errors path="description" cssClass="text-danger" element="div" />
                        </div>

                    </div>

                    <div class="sell-form-actions">
                        <a href="<c:url value="/"/>" class="btn btn-retro btn-retro-outline">
                            <i class="bi bi-arrow-left" aria-hidden="true"></i> Volver al catalogo
                        </a>
                        <button type="submit" class="btn btn-retro btn-retro-primary">
                            <i class="bi bi-vinyl" aria-hidden="true"></i> Publicar vinilo
                        </button>
                    </div>
                </form:form>
            </div>
        </div>
    </div>
    <script>
    (function () {
        var input = document.getElementById('images');
        var previewEl = document.getElementById('sell-images-preview');
        var mainImg = document.getElementById('sell-img-main');
        var thumbsEl = document.getElementById('sell-img-thumbs');
        var form = document.querySelector('form.sell-form');
        if (!input || !previewEl || !mainImg || !thumbsEl || !form) {
            return;
        }

        var filesOrder = [];
        var objectUrls = [];

        function revokeAll() {
            objectUrls.forEach(function (u) { URL.revokeObjectURL(u); });
            objectUrls = [];
        }

        function syncInput() {
            var dt = new DataTransfer();
            filesOrder.forEach(function (f) {
                dt.items.add(f);
            });
            input.files = dt.files;
        }

        function render() {
            revokeAll();
            thumbsEl.innerHTML = '';

            if (filesOrder.length === 0) {
                previewEl.hidden = true;
                mainImg.removeAttribute('src');
                return;
            }

            previewEl.hidden = false;
            var mainUrl = URL.createObjectURL(filesOrder[0]);
            objectUrls.push(mainUrl);
            mainImg.src = mainUrl;

            filesOrder.slice(1).forEach(function (file, idx) {
                var url = URL.createObjectURL(file);
                objectUrls.push(url);
                var btn = document.createElement('button');
                btn.type = 'button';
                btn.className = 'sell-img-thumb';
                btn.setAttribute('aria-label', 'Usar como imagen principal');
                var realIndex = idx + 1;
                btn.addEventListener('click', function () {
                    var picked = filesOrder[realIndex];
                    filesOrder.splice(realIndex, 1);
                    filesOrder.unshift(picked);
                    render();
                });
                var im = document.createElement('img');
                im.src = url;
                im.alt = '';
                btn.appendChild(im);
                thumbsEl.appendChild(btn);
            });
            syncInput();
        }

        input.addEventListener('change', function () {
            filesOrder = Array.prototype.slice.call(input.files, 0);
            render();
        });

        form.addEventListener('submit', function () {
            syncInput();
        });
    })();
    </script>
</ui:layout>
