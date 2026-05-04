# Vinyland

Aplicación móvil para compra y venta de vinilos. Se encuentra en producción en la URL 
http://pawserver.it.itba.edu.ar/paw-2026a-02/.

## Usuarios para probar la aplicación

Ambas credenciales son válidas tanto para Vinyland como para Proton.

### Comprador

Email: comprador-vinyland@proton.me
Contraseña: Compradorvinyland1234$

### Vendedor

Email: vendedor-vinyland@proton.me
Contraseña: Vendedorvinyland1234$

### Admin 

Email: admin-vinyland@proton.me
Contraseña: Adminvinyland1234$

## Entorno local

### Requerimientos

- Java 21
- Maven 3.9+
- Una base de datos PostgreSQL

### Instalación

1. Clonar el repositorio:

```bash
git clone https://bitbucket.org/itba/paw-2026a-02
cd paw-2026a-02
```

2. Crear una base de datos PostgreSQL

3. Crear el archivo `webapp/src/main/resources/application.properties`, completando los datos que sean necesarios:

```properties
auth.rememberme=mysupersecretwarriorkeythatnobodyknowsabout

db.url=jdbc:postgresql://[url de la base de datos]
db.username=[nombre de usuario]
db.password=[contraseña]

mail.host=smtp.gmail.com
mail.port=587
mail.username=vinyland67@gmail.com
mail.password=nnvgmfajysuwjsbk
```

4. Dentro del directorio de proyecto, se debe compilar la aplicación con el siguiente comando:

```bash
mvn clean install
```

5. Iniciar la applicacion:

```bash
mvn jetty:run -pl webapp
```

6. Abrir `http://localhost:8000` en el navegador.

