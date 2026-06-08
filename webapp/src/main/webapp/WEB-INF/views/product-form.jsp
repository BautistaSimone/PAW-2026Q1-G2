<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<c:choose>
    <c:when test="${isEditing}">
        <spring:message code="ProductForm.title.edit" var="productFormTitle" />
    </c:when>
    <c:otherwise>
        <spring:message code="ProductForm.title" var="productFormTitle" />
    </c:otherwise>
</c:choose>
<ui:layout title="${productFormTitle}">

    <ui:header />

    <div class="sell-page">
        <div class="container py-4">
            <div class="sell-form-card">
                <div class="sell-form-header">
                    <span class="sell-form-eyebrow"><i class="bi bi-vinyl" aria-hidden="true"></i> <spring:message code="ProductForm.eyebrow" /></span>
                    <c:choose>
                        <c:when test="${isEditing}">
                            <h1><spring:message code="ProductForm.heading.edit" /></h1>
                            <p><spring:message code="ProductForm.subtitle.edit" /></p>
                        </c:when>
                        <c:otherwise>
                            <h1><spring:message code="ProductForm.heading" /></h1>
                            <p><spring:message code="ProductForm.subtitle" /></p>
                        </c:otherwise>
                    </c:choose>
                </div>

                <%-- CSRF on query string: CommonsMultipartResolver parses the body after the security chain, so a hidden _csrf inside multipart is invisible to CsrfFilter. --%>
                <c:choose>
                    <c:when test="${isEditing}">
                        <c:url var="postUrl" value="/products/${editingProductId}/edit">
                            <c:param name="${_csrf.parameterName}" value="${_csrf.token}" />
                        </c:url>
                    </c:when>
                    <c:otherwise>
                        <c:url var="postUrl" value="/products">
                            <c:param name="${_csrf.parameterName}" value="${_csrf.token}" />
                        </c:url>
                    </c:otherwise>
                </c:choose>
                <form:form modelAttribute="productForm" action="${postUrl}" method="post" enctype="multipart/form-data" cssClass="sell-form" novalidate="novalidate">
                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
                    <c:url var="artistAutocompleteUrl" value="/products/autocomplete/artists" />
                    <c:url var="recordLabelAutocompleteUrl" value="/products/autocomplete/record-labels" />
                    <div class="row g-4">
                        <div class="col-md-6">
                            <label for="title" class="form-label"><spring:message code="ProductForm.albumTitle.label" /> <span class="text-danger">*</span></label>
                            <form:input path="title" cssClass="form-control" required="required" />
                            <form:errors path="title" cssClass="text-danger" element="div" />
                        </div>

                        <div class="col-md-6">
                            <label for="artist" class="form-label"><spring:message code="ProductForm.artist.label" /> <span class="text-danger">*</span></label>
                            <div class="vinyland-autocomplete-field">
                                <form:input path="artist" cssClass="form-control vinyland-autocomplete-input" required="required" autocomplete="off"
                                            data-autocomplete-url="${artistAutocompleteUrl}" data-autocomplete-list="artist-autocomplete-list"
                                            role="combobox" aria-autocomplete="list" aria-expanded="false" aria-controls="artist-autocomplete-list" />
                                <div class="vinyland-autocomplete-menu" id="artist-autocomplete-list" role="listbox" hidden></div>
                            </div>
                            <form:errors path="artist" cssClass="text-danger" element="div" />
                        </div>

                        <div class="col-12">
                            <label class="form-label"><spring:message code="ProductForm.recordLabelCatalog.label" /> <span class="text-danger">*</span></label>
                            <div class="input-group">
                                <spring:message code="ProductForm.recordLabel.placeholder" var="labelPlaceholder" />
                                <div class="vinyland-autocomplete-field vinyland-autocomplete-field-group">
                                    <form:input path="recordLabel" cssClass="form-control vinyland-autocomplete-input"
                                           placeholder="${labelPlaceholder}" required="required" autocomplete="off"
                                           data-autocomplete-url="${recordLabelAutocompleteUrl}" data-autocomplete-list="record-label-autocomplete-list"
                                           role="combobox" aria-autocomplete="list" aria-expanded="false" aria-controls="record-label-autocomplete-list" />
                                    <div class="vinyland-autocomplete-menu" id="record-label-autocomplete-list" role="listbox" hidden></div>
                                </div>
                                <span class="input-group-text product-form-span-1" >–</span>
                                <spring:message code="ProductForm.catalogNumber.placeholder" var="catalogPlaceholder" />
                                <form:input path="catalogNumber" cssClass="form-control"
                                       placeholder="${catalogPlaceholder}" required="required" />
                            </div>
                            <form:errors path="recordLabel" cssClass="text-danger" element="div" />
                            <form:errors path="catalogNumber" cssClass="text-danger" element="div" />
                        </div>

                        <div class="col-md-6">
                            <label for="editionCountry" class="form-label"><spring:message code="ProductForm.editionCountry.label" /> <span class="text-danger">*</span></label>
                            <spring:message code="ProductForm.editionCountry.placeholder" var="editionPlaceholder" />
                            <form:input path="editionCountry" cssClass="form-control"
                                   placeholder="${editionPlaceholder}" required="required" />
                            <form:errors path="editionCountry" cssClass="text-danger" element="div" />
                        </div>

                        <div class="col-md-6">
                            <label class="form-label"><spring:message code="ProductForm.genres.label" /> <span class="text-danger">*</span></label>
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
                            <label for="sleeveCondition" class="form-label"><spring:message code="ProductForm.sleeveCondition.label" /> <span class="text-danger">*</span></label>
                            <spring:message code="ProductForm.sleeveCondition.placeholder" var="sleevePlaceholder" />
                            <form:input type="number" path="sleeveCondition" min="1" max="10" step="0.01"
                                   cssClass="form-control" placeholder="${sleevePlaceholder}" required="required" />
                            <form:errors path="sleeveCondition" cssClass="text-danger" element="div" />
                        </div>

                        <div class="col-md-6">
                            <label for="recordCondition" class="form-label"><spring:message code="ProductForm.recordCondition.label" /> <span class="text-danger">*</span></label>
                            <spring:message code="ProductForm.recordCondition.placeholder" var="recordPlaceholder" />
                            <form:input type="number" path="recordCondition" min="1" max="10" step="0.01"
                                   cssClass="form-control" placeholder="${recordPlaceholder}" required="required" />
                            <form:errors path="recordCondition" cssClass="text-danger" element="div" />
                        </div>

                        <div class="col-md-6">
                            <label for="priceDisplay" class="form-label"><spring:message code="ProductForm.price.label" /> <span class="text-danger">*</span></label>
                            <form:hidden path="price" id="price" />
                            <div class="input-group">
                                <span class="input-group-text product-form-span-1" >$</span>
                                <spring:message code="ProductForm.price.placeholder" var="priceFormPlaceholder" />
                                <input type="text" id="priceDisplay" class="form-control" inputmode="decimal"
                                       autocomplete="off" placeholder="${priceFormPlaceholder}" required="required" />
                            </div>
                            <form:errors path="price" cssClass="text-danger" element="div" />
                        </div>

                        <div class="col-md-6">
                            <label for="stock" class="form-label"><spring:message code="ProductForm.stock.label" /> <span class="text-danger">*</span></label>
                            <form:input type="number" path="stock" min="1" step="1"
                                   cssClass="form-control" required="required" id="stock" />
                            <div class="form-text"><spring:message code="ProductForm.stock.help" /></div>
                            <form:errors path="stock" cssClass="text-danger" element="div" />
                        </div>

                        <div class="col-12">
                            <label for="images" class="form-label">
                                <spring:message code="ProductForm.images.label" />
                                <c:choose>
                                    <c:when test="${isEditing and hasExistingProductImages}">
                                        <span class="text-muted product-form-span-2" >(<spring:message code="ProductForm.images.optionalMark" />)</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="text-danger">*</span>
                                    </c:otherwise>
                                </c:choose>
                            </label>
                            <c:choose>
                                <c:when test="${isEditing and hasExistingProductImages}">
                                    <form:input type="file" path="images" cssClass="form-control sell-images-input" accept="image/*" multiple="true" id="images" />
                                </c:when>
                                <c:otherwise>
                                    <form:input type="file" path="images" cssClass="form-control sell-images-input" accept="image/*" multiple="true" id="images" required="required" />
                                </c:otherwise>
                            </c:choose>
                            <div class="form-text">
                                <c:choose>
                                    <c:when test="${isEditing}">
                                        <spring:message code="ProductForm.images.help.edit" />
                                    </c:when>
                                    <c:otherwise>
                                        <spring:message code="ProductForm.images.help" />
                                    </c:otherwise>
                                </c:choose>
                            </div>
                            <c:if test="${isEditing and hasExistingProductImages}">
                                <form:hidden path="imageLayout" id="imageLayout" />
                                <c:url var="ctxPath" value="/" />
                                <script type="application/json" id="existing-image-ids-json" data-context="<c:out value='${ctxPath}' />">[<c:forEach items="${existingProductImageIds}" var="iid" varStatus="st"><c:if test="${!st.first}">,</c:if><c:out value="${iid}" /></c:forEach>]</script>
                            </c:if>
                            <form:errors path="images" cssClass="text-danger" element="div" />
                            <span id="sellNoImagesMsg" class="d-none"><spring:message code="ProductForm.images.error.none" /></span>
                            <span id="sellAriaUseMainEl" class="d-none"><spring:message code="ProductForm.images.useAsMain.ariaLabel" /></span>
                            <span id="sellAriaRemoveEl" class="d-none"><spring:message code="ProductForm.images.remove.ariaLabel" /></span>
                            <div id="sell-images-preview" class="sell-images-preview" hidden>
                                <div class="sell-img-main-wrap">
                                    <img id="sell-img-main" class="sell-img-main" alt="<spring:message code='ProductForm.images.mainPreview.alt' />" />
                                    <button type="button" class="sell-img-remove sell-img-remove-main" id="sell-img-main-remove" hidden="hidden" aria-label="<spring:message code='ProductForm.images.remove.ariaLabel' />">
                                        <i class="bi bi-x-lg" aria-hidden="true"></i>
                                    </button>
                                </div>
                                <div id="sell-img-thumbs" class="sell-img-thumbs" role="group" aria-label="<spring:message code='ProductForm.images.thumbs.ariaLabel' />"></div>
                            </div>
                        </div>

                        <div class="col-12">
                            <label for="description" class="form-label"><spring:message code="ProductForm.description.label" /> <span class="text-danger">*</span></label>
                            <spring:message code="ProductForm.description.placeholder" var="descriptionPlaceholder" />
                            <form:textarea path="description" cssClass="form-control" rows="5"
                                      placeholder="${descriptionPlaceholder}" required="required" />
                            <form:errors path="description" cssClass="text-danger" element="div" />
                        </div>

                    </div>

                    <div class="sell-form-actions">
                        <c:choose>
                            <c:when test="${isEditing}">
                                <a href="<c:url value='/profile'/>" class="btn btn-retro btn-retro-outline">
                                    <i class="bi bi-arrow-left" aria-hidden="true"></i> <spring:message code="ProductForm.backToProfile" />
                                </a>
                            </c:when>
                            <c:otherwise>
                                <a href="<c:url value="/"/>" class="btn btn-retro btn-retro-outline">
                                    <i class="bi bi-arrow-left" aria-hidden="true"></i> <spring:message code="ProductForm.backToCatalog" />
                                </a>
                            </c:otherwise>
                        </c:choose>
                        <button type="submit" class="btn btn-retro btn-retro-primary" id="publishBtn">
                            <i class="bi bi-vinyl" aria-hidden="true"></i>
                            <c:choose>
                                <c:when test="${isEditing}">
                                    <spring:message code="ProductForm.submit.edit" />
                                </c:when>
                                <c:otherwise>
                                    <spring:message code="ProductForm.submit" />
                                </c:otherwise>
                            </c:choose>
                        </button>
                    </div>
                </form:form>
            </div>
        </div>
    </div>
    <c:choose>
        <c:when test="${isEditing}">
            <span id="sellFormSubmittingText" class="d-none"><spring:message code="ProductForm.submitting.edit" /></span>
        </c:when>
        <c:otherwise>
            <span id="sellFormSubmittingText" class="d-none"><spring:message code="ProductForm.submitting" /></span>
        </c:otherwise>
    </c:choose>
    <script src="<c:url value="/assets/js/autocomplete.js"/>"></script>
    <script>
    (function () {
        var form = document.querySelector('form.sell-form');
        var publishBtn = document.getElementById('publishBtn');
        var submittingTextEl = document.getElementById('sellFormSubmittingText');
        if (form && publishBtn) {
            form.addEventListener('submit', function () {
                publishBtn.disabled = true;
                var msg = submittingTextEl ? submittingTextEl.textContent : '';
                publishBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span> ' + msg;
            });
        }
    })();

    (function () {
        var priceDisplay = document.getElementById('priceDisplay');
        var price = document.getElementById('price');
        var form = document.querySelector('form.sell-form');
        if (!priceDisplay || !price || !form || !window.VinylandPriceFormat) {
            return;
        }

        var syncPrice = window.VinylandPriceFormat.attachFormattedPriceInput(priceDisplay, price);
        form.addEventListener('submit', function () {
            syncPrice();
        });
    })();

    (function () {
        var input = document.getElementById('images');
        var previewEl = document.getElementById('sell-images-preview');
        var mainImg = document.getElementById('sell-img-main');
        var thumbsEl = document.getElementById('sell-img-thumbs');
        var mainRemoveBtn = document.getElementById('sell-img-main-remove');
        var form = document.querySelector('form.sell-form');
        var layoutInput = document.getElementById('imageLayout');
        var idsJsonEl = document.getElementById('existing-image-ids-json');
        var noImagesMsgEl = document.getElementById('sellNoImagesMsg');
        var useMainAriaEl = document.getElementById('sellAriaUseMainEl');
        var removeAriaEl = document.getElementById('sellAriaRemoveEl');
        var useMainAria = useMainAriaEl ? useMainAriaEl.textContent : '';
        var removeAria = removeAriaEl ? removeAriaEl.textContent : '';
        if (!input || !previewEl || !mainImg || !thumbsEl || !form) {
            return;
        }

        var isEditWithExisting = !!(layoutInput && idsJsonEl);
        var slots = [];
        var objectUrls = [];

        function revokeObjectUrls() {
            objectUrls.forEach(function (u) { URL.revokeObjectURL(u); });
            objectUrls = [];
        }

        function syncFilesInput() {
            var dt = new DataTransfer();
            slots.filter(function (s) { return s.kind === 'n'; }).forEach(function (s) {
                dt.items.add(s.file);
            });
            input.files = dt.files;
        }

        function buildLayout() {
            return slots.map(function (s) {
                return s.kind === 'e' ? ('e:' + s.id) : 'n';
            }).join(',');
        }

        function urlForSlot(slot) {
            if (slot.kind === 'e') {
                return slot.url;
            }
            var u = URL.createObjectURL(slot.file);
            objectUrls.push(u);
            return u;
        }

        function render() {
            revokeObjectUrls();
            thumbsEl.innerHTML = '';

            if (slots.length === 0) {
                previewEl.hidden = true;
                mainImg.removeAttribute('src');
                if (layoutInput) {
                    layoutInput.value = '';
                }
                if (mainRemoveBtn) {
                    mainRemoveBtn.hidden = true;
                }
                return;
            }

            previewEl.hidden = false;
            mainImg.src = urlForSlot(slots[0]);

            if (mainRemoveBtn) {
                mainRemoveBtn.hidden = false;
                mainRemoveBtn.onclick = function () {
                    slots.splice(0, 1);
                    render();
                };
            }

            for (var i = 1; i < slots.length; i++) {
                (function (idx) {
                    var wrap = document.createElement('div');
                    wrap.className = 'sell-img-thumb-wrap';

                    var btnMain = document.createElement('button');
                    btnMain.type = 'button';
                    btnMain.className = 'sell-img-thumb';
                    btnMain.setAttribute('aria-label', useMainAria);
                    var thumbUrl = urlForSlot(slots[idx]);
                    btnMain.addEventListener('click', function () {
                        var picked = slots[idx];
                        slots.splice(idx, 1);
                        slots.unshift(picked);
                        render();
                    });

                    var im = document.createElement('img');
                    im.src = thumbUrl;
                    im.alt = '';
                    btnMain.appendChild(im);

                    var rm = document.createElement('button');
                    rm.type = 'button';
                    rm.className = 'sell-img-remove';
                    rm.setAttribute('aria-label', removeAria);
                    rm.innerHTML = '<i class="bi bi-x-lg" aria-hidden="true"></i>';
                    rm.addEventListener('click', function (ev) {
                        ev.stopPropagation();
                        slots.splice(idx, 1);
                        render();
                    });

                    wrap.appendChild(btnMain);
                    wrap.appendChild(rm);
                    thumbsEl.appendChild(wrap);
                })(i);
            }

            if (!isEditWithExisting) {
                syncFilesInput();
            }
        }

        if (isEditWithExisting) {
            try {
                var ctx = (idsJsonEl.getAttribute('data-context') || '').replace(/\/$/, '');
                var ids = JSON.parse(idsJsonEl.textContent || '[]');
                ids.forEach(function (id) {
                    slots.push({ kind: 'e', id: id, url: ctx + '/images/' + id });
                });
            } catch (ignore) {
                slots = [];
            }
            render();
        }

        input.addEventListener('change', function () {
            var picked = Array.prototype.slice.call(input.files || [], 0);
            if (picked.length === 0) {
                return;
            }
            if (isEditWithExisting) {
                for (var j = 0; j < picked.length && slots.length < 8; j++) {
                    slots.push({ kind: 'n', file: picked[j] });
                }
                input.value = '';
                render();
                return;
            }
            slots = picked.slice(0, 8).map(function (f) {
                return { kind: 'n', file: f };
            });
            render();
        });

        form.addEventListener('submit', function (ev) {
            if (isEditWithExisting) {
                if (slots.length === 0) {
                    ev.preventDefault();
                    var t = noImagesMsgEl ? noImagesMsgEl.textContent : '';
                    window.alert(t);
                    return;
                }
                layoutInput.value = buildLayout();
            }
            syncFilesInput();
        });
    })();
    </script>
</ui:layout>
