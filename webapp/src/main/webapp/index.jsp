<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

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
    	<h1 id="title">VINYLAND</h1>
    </header>

	<main>

        <div id="index">
            <h3>Botones!</h3>

            <div id="index_list">
                <ul>
                    <li><vl:button text="Ofertas"/></li>
                    <li><vl:button text="Catálogo"/></li>
                    <li><vl:button text="Mis pedidos"/></li>
                    <li><vl:button text="Acerca de"/></li>
                </ul>   
            </div>
        </div>

        <div id="main_content">

            <h3>Vinilos que cuentan historias</h3>

            <p>Descubrí nuestra colección de vinilos clásicos y ediciones especiales. 
            Cada disco es un viaje al pasado, listo para girar en tu tocadiscos.</p>
            
        </div>

        <hr>

	</main>

	<footer>
		<p>&copy; 2026 Vinyland - <a>vinyland@vinyland.com</a></p>
	</footer>
</body>

</html>
