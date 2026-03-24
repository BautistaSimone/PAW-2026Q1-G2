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

                <form action="<c:url value="/products"/>" method="post" class="sell-form">
                    <div class="row g-4">
                        <div class="col-md-6">
                            <label for="title" class="form-label">Titulo del album</label>
                            <input id="title" name="title" type="text" class="form-control" required />
                        </div>

                        <div class="col-md-6">
                            <label for="artist" class="form-label">Artista</label>
                            <input id="artist" name="artist" type="text" class="form-control" required />
                        </div>

                        <div class="col-md-6">
                            <label for="genre" class="form-label">Genero</label>
                            <input id="genre" name="genre" type="text" class="form-control" required />
                        </div>

                        <div class="col-md-6">
                            <label for="vinylCondition" class="form-label">Estado del vinilo</label>
                            <select id="vinylCondition" name="vinylCondition" class="form-select" required>
                                <option value="">Seleccionar</option>
                                <option value="Nuevo">Nuevo</option>
                                <option value="Como nuevo">Como nuevo</option>
                                <option value="Muy bueno">Muy bueno</option>
                                <option value="Bueno">Bueno</option>
                            </select>
                        </div>

                        <div class="col-md-6">
                            <label for="price" class="form-label">Precio</label>
                            <input id="price" name="price" type="number" min="0" step="0.01" class="form-control" required />
                        </div>

                        <div class="col-md-6">
                            <label for="imageUrl" class="form-label">URL de imagen</label>
                            <input id="imageUrl" name="imageUrl" type="url" class="form-control" placeholder="https://..." />
                        </div>

                        <div class="col-12">
                            <label for="description" class="form-label">Descripcion</label>
                            <textarea id="description" name="description" class="form-control" rows="5"
                                      placeholder="Conta detalles del disco, edicion, estado de tapa, etc."></textarea>
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
