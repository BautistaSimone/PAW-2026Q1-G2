<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<%@ page contentType="text/html; charset=UTF-8" language="java" %>

<!-- vl prefix = VinyLand -->
<%@ taglib prefix="vl" tagdir="/WEB-INF/tags" %>

<html>

<head>
    <!-- Required meta tags -->
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    
    <title>Vinyland</title>
    <link rel="stylesheet" type="text/css" href="<c:url value="/assets/css/style.css"/>"/>
    <link rel="stylesheet" type="text/css" href="<c:url value="/assets/css/components.css"/>"/>
</head>
<body>
	<header>
        <vl:text type="h1" value="VINYLAND"/>
    </header>

	<main>

        <div id="index">
            <vl:text type="h3" value="Botones >:)"/>

            <div id="index_list">
                <ul>
                    <li><vl:button text="Ofertas" img="/assets/images/vinyl_disk.png"/></li>
                    <li><vl:button text="Catálogo" img="/assets/images/shopping_cart.png"/></li>
                    <li><vl:button text="Mis pedidos"/></li>
                    <li><vl:button text="Acerca de"/></li>
                </ul>   
            </div>
        </div>

        <div id="main_content">
            <vl:text type="h3" value="Vinilos que cuentan historias"/>

            <vl:text type="p" value="Descubrí nuestra colección de vinilos clásicos y ediciones especiales.
            Cada disco es un viaje al pasado, listo para girar en tu tocadiscos."
            />
            
            <hr>

            <div id="categories">

                <vl:text type="h2" value="Algunas de nuestras categorias"/>

                <vl:category-card 
                    title="Artistas alternativos" 
                    text="Bla bla, hacen algo diferente"
                    img="/assets/images/album.jpg"/>
                <vl:category-card 
                    title="Rock nacional" 
                    text="Para los que quieren algo mas cercano"
                    img="/assets/images/album.jpg"/>
                <vl:category-card 
                    title="Jazz" 
                    text="???"
                    img="/assets/images/album.jpg"/>
            </div>
        </div>

        <hr>

	</main>

	<footer>
        <vl:text type="p" value="© 2026 Vinyland - vinyland@vinyland.com"/>
	</footer>
</body>

</html>
