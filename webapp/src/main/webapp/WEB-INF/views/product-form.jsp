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
                        Carga la informacion principal del disco para que quede disponible en el catalogo.
                    </p>
                </div>

                <form action="<c:url value="/products"/>" method="post" enctype="multipart/form-data" class="sell-form">
                    <div class="row g-4">
                        <div class="col-md-6">
                            <label for="title" class="form-label">Titulo del album</label>
                            <input id="title" name="title" type="text" class="form-control" required />
                        </div>

                        <div class="col-md-6">
                            <label for="artist" class="form-label">Artista</label>
                            <input id="artist" name="artist" type="text" class="form-control" required />
                        </div>

                        <div class="col-12">
                            <label class="form-label">Generos</label>
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
                            <label for="sellerEmail" class="form-label">Email del vendedor (asociado a tu cuenta)</label>
                            <input id="sellerEmail" name="sellerEmail" type="email" class="form-control" 
                                   placeholder="ejemplo@mail.com" required />
                            <div class="form-text">Usaremos este email para contactarte hasta que tengamos sistema de usuarios completo.</div>
                        </div>

                        <div class="col-md-6">
                            <label for="sleeveCondition" class="form-label">Estado de la tapa (1 a 10)</label>
                            <input id="sleeveCondition" name="sleeveCondition" type="number" min="1" max="10" step="0.1" 
                                   class="form-control" placeholder="Ej: 8.5" required />
                        </div>

                        <div class="col-md-6">
                            <label for="recordCondition" class="form-label">Estado del disco (1 a 10)</label>
                            <input id="recordCondition" name="recordCondition" type="number" min="1" max="10" step="0.1" 
                                   class="form-control" placeholder="Ej: 9.0" required />
                        </div>

                        <div class="col-md-6">
                            <label for="neighborhood" class="form-label">Barrio / Ciudad</label>
                            <input id="neighborhood" name="neighborhood" type="text" class="form-control" 
                                   placeholder="Ej: Palermo" required />
                        </div>

                        <div class="col-md-6">
                            <label for="province" class="form-label">Provincia</label>
                            <input id="province" name="province" type="text" class="form-control" 
                                   placeholder="Ej: Buenos Aires" required />
                        </div>

                        <div class="col-md-6">
                            <label for="price" class="form-label">Precio</label>
                            <input id="price" name="price" type="number" min="0" step="0.01" class="form-control" required />
                        </div>

                        <div class="col-md-6">
                            <label for="image" class="form-label">Imagen del vinilo</label>
                            <input id="image" name="image" type="file" class="form-control" accept="image/*" />
                        </div>

                        <div class="col-12">
                            <label for="description" class="form-label">Descripcion</label>
                            <textarea id="description" name="description" class="form-control" rows="5"
                                      placeholder="Contá detalles adicionales del disco, edición, etc."></textarea>
                        </div>

                    </div>

                    <div class="sell-form-actions">
                        <a href="<c:url value="/"/>" class="btn btn-outline-secondary">Volver al catalogo</a>
                        <button type="submit" class="btn btn-dark">Publicar vinilo</button>
                    </div>
                </form>
            </div>
        </div>
    </div>
</ui:layout>
