<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<!-- vl prefix = VinyLand -->
<%@ taglib prefix="vl" tagdir="/WEB-INF/tags" %>

<html>

<head>
    <!-- Required meta tags -->
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    
    <title>Vinyland</title>
</head>
<body>
	<header>
    	<h1 id="title">VINYLAND</h1>
    </header>

	<main>

		<h2><c:out value="${message}" /></h2>
		<vl:button text="Primary"/>

	</main>

	<footer>
		<p>&copy; 2026 Vinyland - <a>vinyland@vinyland.com</a></p>
	</footer>
</body>

</html>
