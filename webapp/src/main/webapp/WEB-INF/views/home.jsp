<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<ui:layout title="Vinyland | Productos">
    <div class="products-section">
        <div class="container">
            <nav aria-label="breadcrumb">
                <ol class="breadcrumb">
                    <li class="breadcrumb-item">
                        <a href="<c:url value="#"/>">Inicio</a>
                    </li>
                    <li class="breadcrumb-item active" aria-current="page">Productos</li>
                </ol>
            </nav>

            <div class="row">
                <div class="col-lg-3 col-md-4 col-12">
                    <ui:filtersBar />
                </div>

                <div class="col-lg-9 col-md-8 col-12">
                    <div class="products-header">
                        <h2 class="products-count">12 productos</h2>
                    </div>

                    <div class="row">
                        <div class="col-lg-4 col-md-6 col-12 mb-4">
                            <ui:productCard title="Dynamo" artist="Soda Stereo" price="32000"
                                            installments="10666" href="${pageContext.request.contextPath}/producto.jsp?id=1"
                                            imageUrl="https://res.cloudinary.com/dzjik8puv/image/upload/v1771457200/WhatsApp_Image_2026-02-18_at_10.33.21_AM_1_a6m7kw.jpg" />
                        </div>
                        <div class="col-lg-4 col-md-6 col-12 mb-4">
                            <ui:productCard title="The Game" artist="Queen" price="28500" installments="9500"
                                            href="${pageContext.request.contextPath}/producto.jsp?id=2"
                                            imageUrl="https://res.cloudinary.com/dzjik8puv/image/upload/v1771457184/WhatsApp_Image_2026-02-18_at_10.24.46_AM_cyeqfk.jpg" />
                        </div>
                        <div class="col-lg-4 col-md-6 col-12 mb-4">
                            <ui:productCard title="The Wall" artist="Pink Floyd" price="35000"
                                            installments="11666" href="${pageContext.request.contextPath}/producto.jsp?id=3"
                                            imageUrl="https://res.cloudinary.com/dzjik8puv/image/upload/v1771457184/WhatsApp_Image_2026-02-18_at_10.21.58_AM_lxupq1.jpg" />
                        </div>
                        <div class="col-lg-4 col-md-6 col-12 mb-4">
                            <ui:productCard title="Abbey Road" artist="The Beatles" price="38000"
                                            installments="12666" href="${pageContext.request.contextPath}/producto.jsp?id=4"
                                            imageUrl="https://res.cloudinary.com/dzjik8puv/image/upload/v1771457183/WhatsApp_Image_2026-02-18_at_10.21.58_AM_1_iaipmr.jpg" />
                        </div>
                        <div class="col-lg-4 col-md-6 col-12 mb-4">
                            <ui:productCard title="Kind of Blue" artist="Miles Davis" price="26500"
                                            installments="8833" href="${pageContext.request.contextPath}/producto.jsp?id=5"
                                            imageUrl="https://res.cloudinary.com/dzjik8puv/image/upload/v1771457185/WhatsApp_Image_2026-02-18_at_10.29.22_AM_1_bzqxea.jpg" />
                        </div>
                        <div class="col-lg-4 col-md-6 col-12 mb-4">
                            <ui:productCard title="Transa" artist="Caetano Veloso" price="29900"
                                            installments="9966" href="${pageContext.request.contextPath}/producto.jsp?id=6"
                                            imageUrl="https://res.cloudinary.com/dzjik8puv/image/upload/v1771457193/WhatsApp_Image_2026-02-18_at_10.32.05_AM_1_qedzgb.jpg" />
                        </div>
                        <div class="col-lg-4 col-md-6 col-12 mb-4">
                            <ui:productCard title="Clics Modernos" artist="Charly Garcia" price="24000"
                                            installments="8000" href="${pageContext.request.contextPath}/producto.jsp?id=7"
                                            imageUrl="https://res.cloudinary.com/dzjik8puv/image/upload/v1771457204/WhatsApp_Image_2026-02-18_at_10.33.21_AM_fhqsp6.jpg"
                                            onSale="true" discountPercentage="20" />
                        </div>
                        <div class="col-lg-4 col-md-6 col-12 mb-4">
                            <ui:productCard title="Low" artist="David Bowie" price="23300" installments="7766"
                                            href="${pageContext.request.contextPath}/producto.jsp?id=8"
                                            imageUrl="https://res.cloudinary.com/dzjik8puv/image/upload/v1771457193/WhatsApp_Image_2026-02-18_at_10.24.45_AM_1_qiqhbp.jpg"
                                            onSale="true" discountPercentage="15" />
                        </div>
                        <div class="col-lg-4 col-md-6 col-12 mb-4">
                            <ui:productCard title="Lady Soul" artist="Aretha Franklin" price="23250"
                                            installments="7750" href="${pageContext.request.contextPath}/producto.jsp?id=9"
                                            imageUrl="https://res.cloudinary.com/dzjik8puv/image/upload/v1771457184/WhatsApp_Image_2026-02-18_at_10.27.59_AM_b0rhoz.jpg"
                                            onSale="true" discountPercentage="25" />
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</ui:layout>
