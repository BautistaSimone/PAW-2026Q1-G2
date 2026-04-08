<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<ui:layout title="Vinyland | Publicar vinilo">
    <div class="sell-page">
        <div class="container py-5">
            <div class="sell-form-card">
                <div class="sell-form-header">
                    <span class="sell-form-eyebrow">Panel de vendedor</span>
                    <h1>Publicar un vinilo</h1>
                    <p>
                        Carga la información principal del disco para que quede disponible en el catálogo.
                    </p>
                </div>

                <form action="<c:url value="/products"/>" method="post" enctype="multipart/form-data" class="sell-form">
                    <div class="row g-4">
                        <div class="col-md-6">
                            <label for="title" class="form-label">Título del álbum *</label>
                            <input id="title" name="title" type="text" class="form-control" required />
                        </div>

                        <div class="col-md-6">
                            <label for="artist" class="form-label">Artista *</label>
                            <input id="artist" name="artist" type="text" class="form-control" required />
                        </div>

                        <div class="col-12">
                            <label class="form-label">Sello – Número de catálogo *</label>
                            <div class="input-group">
                                <input id="recordLabel" name="recordLabel" type="text" class="form-control"
                                       placeholder="Ej: Sony Music, Rough Trade" required />
                                <span class="input-group-text">–</span>
                                <input id="catalogNumber" name="catalogNumber" type="text" class="form-control"
                                       placeholder="Ej: EPC 85930" required />
                            </div>
                        </div>

                        <div class="col-md-6">
                            <label for="editionCountry" class="form-label">País de la Edición *</label>
                            <input id="editionCountry" name="editionCountry" type="text" class="form-control"
                                   placeholder="Ej: Argentina, USA, UK" required />
                        </div>

                        <div class="col-md-6">
                            <label class="form-label">Géneros *</label>
                            <div class="genre-checkboxes d-flex flex-wrap gap-2">
                                <c:forEach items="${categories}" var="cat">
                                    <div class="form-check genre-check">
                                        <input class="form-check-input" type="checkbox"
                                               name="categories" value="${cat.id}"
                                               id="cat-${cat.id}" />
                                        <label class="form-check-label" for="cat-${cat.id}">
                                            <c:out value="${cat.name}" />
                                        </label>
                                    </div>
                                </c:forEach>
                            </div>
                        </div>

                        <div class="col-12">
                            <label for="sellerEmail" class="form-label">Email del vendedor (asociado a tu cuenta) *</label>
                            <input id="sellerEmail" name="sellerEmail" type="email" class="form-control" 
                                   placeholder="ejemplo@mail.com" required />
                            <div class="form-text">Usaremos este email para contactarte hasta que tengamos sistema de usuarios completo.</div>
                        </div>

                        <div class="col-md-6">
                            <label for="sleeveCondition" class="form-label">Estado de la tapa (1 a 10) *</label>
                            <input id="sleeveCondition" name="sleeveCondition" type="number" min="1" max="10" step="0.1" 
                                   class="form-control" placeholder="Ej: 8.5" required />
                        </div>

                        <div class="col-md-6">
                            <label for="recordCondition" class="form-label">Estado del disco (1 a 10) *</label>
                            <input id="recordCondition" name="recordCondition" type="number" min="1" max="10" step="0.1" 
                                   class="form-control" placeholder="Ej: 9.0" required />
                        </div>

                        <div class="col-md-6">
                            <label for="neighborhood" class="form-label">Barrio / Ciudad *</label>
                            <input id="neighborhood" name="neighborhood" type="text" class="form-control" 
                                   placeholder="Ej: Palermo" required />
                        </div>

                        <div class="col-md-6">
                            <label for="province" class="form-label">Provincia *</label>
                            <input id="province" name="province" type="text" class="form-control" 
                                   placeholder="Ej: Buenos Aires" required />
                        </div>

                        <div class="col-md-6">
                            <label for="price" class="form-label">Precio *</label>
                            <input id="price" name="price" type="number" min="0" step="0.01" class="form-control" required />
                        </div>

                        <div class="col-12">
                            <label for="images" class="form-label">Imágenes del vinilo *</label>
                            <input id="images" name="images" type="file" class="form-control sell-images-input" accept="image/*" multiple="multiple" required />
                            <div class="form-text">Podés cargar varias fotos del disco. La imagen principal es la primera; tocá las miniaturas de abajo para elegir otra como portada.</div>
                            <div id="sell-images-preview" class="sell-images-preview" hidden>
                                <div class="sell-img-main-wrap">
                                    <img id="sell-img-main" class="sell-img-main" alt="Vista previa principal del vinilo" />
                                </div>
                                <div id="sell-img-thumbs" class="sell-img-thumbs" role="group" aria-label="Miniaturas para cambiar la imagen principal"></div>
                            </div>
                        </div>

                        <div class="col-12">
                            <label for="description" class="form-label">Descripción</label>
                            <textarea id="description" name="description" class="form-control" rows="5"
                                      placeholder="Contá detalles adicionales del disco, edición, etc."></textarea>
                            <div class="form-text">Opcional.</div>
                        </div>

                    </div>

                    <div class="sell-form-actions">
                        <a href="<c:url value="/"/>" class="btn btn-outline-secondary">Volver al catálogo</a>
                        <button type="submit" class="btn btn-dark">Publicar vinilo</button>
                    </div>
                </form>
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
