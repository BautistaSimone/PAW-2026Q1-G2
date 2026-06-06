# Checklist de Errores TP2 — Extraído de Devoluciones de Años Anteriores

> [!IMPORTANT]
> Este documento lista **todos los patrones de error recurrentes** detectados en las devoluciones de TP2 de la materia PAW (Plataformas de Aplicaciones Web) de **dos cuatrimestres anteriores**. Está diseñado para que una IA lo use como guía para escanear un repositorio Java/Spring MVC en busca de estos problemas.

> [!CAUTION]
> Los items marcados como 🔴 **CRÍTICO** o 🟠 **GRAVE** son los que los correctores consideran **errores conceptuales graves** y tienen el mayor impacto negativo en la nota. Priorizarlos.

---

## Niveles de Severidad

| Icono | Nivel | Significado |
|-------|-------|-------------|
| 🔴 | **CRÍTICO** | Error conceptual grave. Mencionado repetidamente como tal por los correctores. Resta mucho puntaje. |
| 🟠 | **GRAVE** | Error importante que aparece en múltiples correcciones. Tiene impacto significativo. |
| 🟡 | **MODERADO** | Problema de calidad que resta puntos pero no es conceptual. |
| 🔵 | **MENOR** | Detalle de estilo, UX o buena práctica. Resta poco pero suma profesionalismo. |

---

## 1. ARQUITECTURA — Servicios como Facades

### 🔴 1.1 Lógica de negocio en Controllers (CRÍTICO — EL MÁS REPETIDO)

**Frecuencia**: Aparece en **más de 15 grupos** entre ambas devoluciones. Es el error conceptual grave más común.

**Qué buscar**:

- Controllers que hacen **más de una llamada a servicios** para completar una operación (orquestación de casos de uso en el controller).
- Controllers que hacen `if/else`, cálculos, transformaciones de datos, o decisiones de negocio.
- Controllers que llaman a un servicio, luego otro, y luego combinan resultados.
- Métodos del controller como `editProfile` que llaman primero a un servicio para cambiar la imagen y luego a otro para cambiar la password — **esto debe ser un único método del servicio**.
- Controllers que obtienen entidades con `findById` y luego chequean condiciones sobre ellas antes de actuar.

**Lo correcto**: Los servicios deben ser **verdaderas Facades** que encapsulan y orquestan los casos de uso completos. El controller solo debe recibir la request, validar binding/forms, llamar **un** método del servicio, y retornar la vista/redirect.

**Patrón a detectar en código**:

```java
// MAL: múltiples llamadas a servicios desde el controller
@PostMapping("/edit")
public ModelAndView editProfile(...) {
    userService.updateImage(userId, image);    // llamada 1
    userService.updatePassword(userId, pwd);   // llamada 2
    // Esto debería ser: userService.editProfile(userId, image, pwd);
}
```

---

### 🔴 1.2 Servicios con concepto de frontend (CRÍTICO)

**Qué buscar**:

- Servicios llamados `HomeService`, `HomeDisplayService`, o similares que representan conceptos de la capa de presentación.
- Servicios que devuelven `Map<String, Object>` para popular vistas.
- Servicios que acoplan el comportamiento de `ModelAndView` al service.

**Lo correcto**: Los servicios deben definirse en función de **casos de uso y del dominio**, no de páginas específicas del frontend.

---

### 🔴 1.3 Control de acceso en Controllers en lugar de Spring Security (CRÍTICO)

**Frecuencia**: Aparece en **más de 10 grupos**.

**Qué buscar**:

- Controllers que hacen chequeos manuales del tipo `if (user.getId() != resource.getOwnerId()) throw ...`.
- Controllers que verifican roles manualmente en lugar de usar `@PreAuthorize`, `.access()`, o la taglib de Spring Security.
- Métodos con nombres genéricos como `canAccessPage(...)` para control de acceso.
- Uso de `SecurityContextHolder` directamente en controllers para obtener el usuario y verificar permisos manualmente.
- Controllers que verifican si el usuario está verificado, si es dueño del recurso, etc.

**Lo correcto**: Usar `@PreAuthorize`, `@Secured`, `.access()` en la configuración de Spring Security, o `AccessDecisionVoter`. Los chequeos finos de ownership deben hacerse vía Spring Security con expresiones SpEL o custom evaluators.

**Patrón a detectar**:

```java
// MAL: chequeo manual de acceso en controller
if (user == null) {
    throw new UnauthorizedException("User not found");
}
// MAL: verificar ownership en controller
if (!resource.getOwner().equals(currentUser)) {
    throw new ForbiddenException();
}
```

---

### 🟠 1.4 Schema.sql en módulo incorrecto (GRAVE)

**Qué buscar**:

- El archivo `schema.sql` debe estar en el módulo de **persistence** (`persistence/src/main/resources/schema.sql`), **NO** en `webapp`.

---

### 🟠 1.5 Templates de email en módulo incorrecto (GRAVE)

**Qué buscar**:

- Templates de email que están en el módulo `webapp` pero son referenciados desde la capa de servicios → viola el modelo de capas.
- Los templates de email deben estar accesibles desde la capa de servicios sin dependencia en webapp.

---

### 🟠 1.6 Dependencias indebidas entre módulos (GRAVE)

**Qué buscar**:

- Módulos de interfaces/contracts que dependan de `javax.servlet`, `javax.mail`, o Jackson.
- Dependencias de `javax.servlet.http.HttpServletResponse` en la capa de servicios.
- La capa de servicios NO debe escribir directamente en `HttpServletResponse`.

---

## 2. PERSISTENCIA / DAOs

### 🔴 2.1 No usar paginación modelo 1+1 (CRÍTICO — MUY REPETIDO)

**Frecuencia**: Aparece en **más de 12 grupos**. Es el segundo error conceptual grave más común.

**Qué buscar**:

- Queries paginadas que **NO** usan el modelo 1+1 (una query para los IDs paginados, otra query para traer las entidades completas con sus relaciones).
- Queries paginadas que potencialmente traen la tabla entera para paginar en memoria.
- `setFirstResult`/`setMaxResults` aplicados directamente a queries con JOINs o FETCH JOINs (esto puede devolver resultados incorrectos con colecciones).

**Lo correcto**:

1. **Query 1**: Obtener solo los IDs de las entidades paginadas (con `setFirstResult`/`setMaxResults`).
2. **Query 2 ("+1")**: Traer las entidades completas con sus relaciones usando `WHERE id IN (:ids)` con FETCH JOINs.

---

### 🔴 2.2 Colecciones de alta cardinalidad mapeadas con @OneToMany/@ManyToMany (CRÍTICO)

**Frecuencia**: Aparece en **más de 8 grupos**.

**Qué buscar**:

- Entidades que mapean relaciones `@OneToMany` o `@ManyToMany` donde la colección **puede crecer indefinidamente** (ej: reviews de un usuario, favoritos, pedidos, archivos).
- Uso de `FetchType.EAGER` en relaciones `@OneToMany` o `@ManyToMany`.
- Acceso a colecciones mapeadas solo para hacer `.size()` (esto carga toda la colección en memoria).

**Lo correcto**: Las relaciones de alta cardinalidad no deben mapearse como colecciones en la entidad. Se deben consultar mediante queries paginadas en el DAO. Para contar, usar `@Formula` o una query COUNT.

---

### 🟠 2.3 N+1 queries (GRAVE)

**Qué buscar**:

- Loops en código que hacen un `findById` por cada elemento de una lista.
- DAOs que traen una lista y luego por cada elemento hacen otra query para obtener relaciones.
- Servicios que iteran sobre una lista y llaman al DAO por cada item.

**Lo correcto**: Usar queries `IN` para batch-load, o FETCH JOINs.

---

### 🟠 2.4 Retornar colecciones sin paginar (GRAVE)

**Qué buscar**:

- Métodos de DAO/Service que retornan `List<T>` sin paginación para colecciones que pueden crecer (todas las materias, todos los doctores, todos los usuarios, etc.).
- Métodos como `getAll*()` sin parámetros de paginación.

---

### 🟡 2.5 Uso de `java.sql` en modelos (MODERADO)

**Frecuencia**: Aparece en **más de 5 grupos**.

**Qué buscar**:

- Imports de `java.sql.Date`, `java.sql.Timestamp`, etc. en la capa de modelos.
- La existencia de una base de datos es un detalle de implementación del DAO; los modelos deben usar `java.time.LocalDateTime`, `LocalDate`, etc.

---

### 🟡 2.6 `Optional` mal usado (MODERADO)

**Qué buscar**:

- DAOs que retornan `T` en lugar de `Optional<T>`, forzando al que llama a hacer `return instance.orElse(null)`.
- Llamadas a `Optional.get()` sin chequear `isPresent()` primero → riesgo de `NoSuchElementException`.
- DAOs de create que retornan `Optional` cuando la implementación nunca devuelve empty.
- Uso de `orElseThrow()` sin excepción custom (lanza `NoSuchElementException` genérico).

---

### 🟡 2.7 Tests de persistencia que no validan estado final de la DB (MODERADO)

**Qué buscar**:

- Tests de DAOs que hacen operaciones (create, update, delete) pero no verifican el estado resultante en la base de datos.
- Tests que usan `assertTrue(true)` para verificar ausencia de error — mal diseño.

---

### 🟡 2.8 Tests de persistencia que insertan datos con EntityManager en lugar de SQL (MODERADO)

**Qué buscar**:

- Tests que en el setup/`@BeforeEach` usan `EntityManager.persist()` o autowirean otros DAOs para insertar datos de prueba, en lugar de insertar con un script SQL o `@Sql`.
- Lo correcto es usar un `.sql` para inicializar el estado de la DB en los tests.

---

## 3. CAPA DE SERVICIOS

### 🟠 3.1 Métodos de email de bajo nivel como públicos (GRAVE)

**Frecuencia**: Aparece en **más de 5 grupos**.

**Qué buscar**:

- Servicios de email que exponen métodos como `sendHtmlMessage()`, `sendSimpleMessage()`, `sendMessageWithAttachment()` como públicos.
- El caller no debería tener que conocer qué templates existen ni qué variables acepta cada uno.

**Lo correcto**: El servicio de email debe exponer métodos de alto nivel como `sendWelcomeEmail(User user)`, `sendPurchaseConfirmation(Purchase purchase)`, etc. El servicio internamente decide template, subject, variables.

---

### 🟠 3.2 Email: acoplamiento excesivo (GRAVE)

**Qué buscar**:

- Servicios que reciben `Map<String, Object>` para popular templates de email.
- Servicios (no el de email) que son responsables de rellenar subject, elegir template, y armar variables del mail.
- Otros servicios que tienen **muchas líneas** dedicadas a preparar datos para el envío de mail (ej: 10+ líneas solo para armar el mail).

**Lo correcto**: El servicio de email se encarga internamente de todo: obtener los datos necesarios, elegir template, popular variables.

---

### 🟠 3.3 Falta de `@Transactional` (GRAVE)

**Qué buscar**:

- Métodos de servicio que hacen operaciones de escritura (create, update, delete) **sin** `@Transactional`.
- Métodos de lectura **sin** `@Transactional(readOnly = true)`.
- Servicios donde solo algunos métodos tienen `@Transactional` y otros no.
- Métodos que coordinan varias operaciones de DAOs sin estar englobados en una transacción → puede dejar estados inválidos.

---

### 🟡 3.4 Estado compartido en servicios (MODERADO)

**Qué buscar**:

- Servicios que tienen campos de instancia mutables (propiedades que se setean con métodos públicos antes de ejecutar la operación) → esto lleva a **condiciones de carrera** en un entorno multi-thread.
- Patrón builder aplicado a servicios (ej: `service.setFilter(...).setSort(...).execute()`).

**Lo correcto**: Los parámetros de una operación deben ser argumentos del método, no estado del servicio.

---

### 🟡 3.5 Marcar métodos de mailing como `@Transactional` innecesariamente (MODERADO)

**Qué buscar**:

- Métodos que envían emails marcados como `@Transactional` cuando no realizan operaciones de base de datos.

---

### 🟡 3.6 Locale del mail incorrecto (MODERADO)

**Qué buscar**:

- Mails que se envían usando `LocaleContextHolder` (locale del usuario activo/request actual) en lugar del locale del **destinatario** del mail.

---

## 4. CONTROLLERS

### 🟠 4.1 Validación de formularios en el Controller o JSP en lugar de Custom Validators (GRAVE)

**Frecuencia**: Aparece en **más de 8 grupos**.

**Qué buscar**:

- Controllers que hacen validaciones manuales con `if/else` sobre campos del formulario.
- Validaciones de formulario en el JSP en lugar del backend.
- Controllers que hacen `try/catch` de `Exception` para manejar errores de validación.
- Falta de validators custom para `MultipartFile` (validación de tamaño, tipo MIME).

**Lo correcto**: Usar `@Valid` + `BindingResult` + Custom Validators (implementando `Validator` de Spring). Las validaciones de negocio van en los forms/validators, no en el controller.

---

### 🟠 4.2 Try-catch de Exception en Controllers (GRAVE)

**Qué buscar**:

- Controllers que hacen `try { ... } catch (Exception e) { ... }` para manejar errores.
- Esto debe manejarse con `@ControllerAdvice` / `@ExceptionHandler`.

---

### 🟠 4.3 No validar parámetros de paginación (GRAVE)

**Qué buscar**:

- Endpoints paginados que NO validan que `page` y `size` no sean negativos o cero.
- `page=-1` que arroja un 500 en lugar de un 400 o redirect.

**Lo correcto**: Validar siempre que page >= 1 (o >= 0 según convención) y size > 0 con límites máximos.

---

### 🟠 4.4 No hacer redirect después de POST (Post-Redirect-Get) (GRAVE)

**Qué buscar**:

- Controllers que después de un POST retornan directamente una vista (forward) en lugar de hacer redirect → si el usuario recarga la página, se reenvía el form.

---

### 🟡 4.5 Métodos con parámetros sin usar (MODERADO)

**Qué buscar**:

- Métodos que definen parámetros que nunca se utilizan en el cuerpo del método.

---

## 5. SEGURIDAD

### 🔴 5.1 XSS por uso de `${}` sin `<c:out>` en JSP (CRÍTICO)

**Frecuencia**: Aparece en **más de 8 grupos** y se marca como **error grave reincidente**.

**Qué buscar**:

- En archivos JSP y tag files: uso de `${variable}` directamente en HTML **sin** envolver con `<c:out value="${variable}" />`.
- Especialmente peligroso en: nombres de usuario, descripciones, reviews, mensajes, títulos, cualquier contenido ingresado por usuarios.

**Patrón a detectar**:

```jsp
<%-- MAL: vulnerable a XSS --%>
<p>${user.name}</p>
<input value="${product.title}" />

<%-- BIEN: escapado --%>
<p><c:out value="${user.name}" /></p>
<spring:message code="..." var="x" />
<input value="<c:out value='${product.title}' />" />
```

> [!CAUTION]
> **Este es uno de los errores más graves y más fácilmente detectables**. Cada `${}` en un archivo JSP/tag que imprima datos del modelo y no esté envuelto en `<c:out>` es un potencial XSS. Buscar exhaustivamente.

---

### 🟠 5.2 Falta de `<c:url>` para URLs (GRAVE)

**Qué buscar**:

- URLs en JSPs construidas manualmente con `${pageContext.request.contextPath}/path` en lugar de usar `<c:url value="/path" />`.
- Variables custom como `webapp_base_url` para construir URLs.

---

### 🟠 5.3 Falta validación de imágenes/archivos subidos (GRAVE)

**Qué buscar**:

- Forms con `MultipartFile` que no validan: tamaño máximo (ej: 5MB), tipo MIME (verificar que sea realmente `image/jpeg`, `image/png`, etc.).
- Imágenes grandes que causan error 500 al subirse.
- ContentType incorrecto al devolver imágenes (ej: siempre devolver `image/jpeg` sin importar el tipo real).
- Validaciones de archivo hechas inline en el controller en lugar de en un custom form validator.

---

### 🟡 5.4 Mensajes de excepción no internacionalizados (MODERADO)

**Qué buscar**:

- Excepciones lanzadas con mensajes en texto plano (ej: `throw new BadRequestException("User not found")`) que luego se muestran al usuario sin pasar por i18n.
- Mensajes i18n en la capa de servicios (deben estar en la capa de presentación).

---

## 6. TESTING

### 🟠 6.1 Tests no unitarios (GRAVE)

**Frecuencia**: Aparece en **más de 6 grupos**.

**Qué buscar**:

- Tests de persistencia que autowirean **todos** los DAOs en lugar de solo el que están testeando.
- Tests de servicios que usan la misma class under test para el setup (ej: llamar a `create()` en el setup para luego testear `findById()`).
- Tests que usan múltiples métodos de distintas clases sin mockear.

---

### 🟠 6.2 Cobertura de tests pobre (GRAVE)

**Frecuencia**: Mencionado en **la mayoría de los grupos**.

**Qué buscar**:

- Servicios enteros sin ni un solo test.
- Solo tests de happy path, faltan tests de **casos de error** y edge cases.
- Falta de tests para: edit, delete, search, y queries complejas del DAO.
- Tests que solo cubren métodos "pasamanos" (que no tienen lógica) en lugar de cubrir los que sí tienen lógica de negocio.
- Funciones con lógica pesada de negocio sin cobertura de tests.

---

### 🟡 6.3 Uso de `Mockito.verify` para testear implementación (MODERADO)

**Qué buscar**:

- Tests que usan `Mockito.verify()` extensivamente para verificar que se llamaron ciertos métodos internos → testea **implementación** en lugar de **comportamiento**.

---

### 🟡 6.4 Tests vacíos o con `assertTrue(true)` (MODERADO)

**Qué buscar**:

- Tests que solo verifican que no hay excepción (`assertTrue(true)` después de llamar al método).
- Tests vacíos o placeholder.

---

## 7. FRONTEND / UX

### 🟠 7.1 Páginas de error incompletas (GRAVE)

**Qué buscar**:

- Falta de página de error 404 custom.
- Falta de página de error 500 custom.
- Falta de página de error 403 custom.
- Páginas de error que no muestran la navbar/header del usuario.
- Páginas de error que muestran stacktraces o mensajes técnicos.
- Páginas de error sin CTA (Call To Action) para orientar al usuario.

---

### 🟠 7.2 Falta de confirmación en acciones destructivas (GRAVE)

**Qué buscar**:

- Acciones de eliminar/borrar/cancelar sin modal de confirmación.
- Cualquier acción irreversible que se ejecuta con un solo click sin pedir confirmación.

---

### 🟠 7.3 Falta de feedback al usuario (GRAVE)

**Frecuencia**: Aparece en **más de 10 grupos**.

**Qué buscar**:

- Acciones que no muestran mensaje de éxito/error después de completarse (ej: aceptar invitación, enviar formulario).
- Login fallido que no muestra mensaje de error o muestra uno genérico como "Login failed" en lugar de algo más descriptivo.
- Listas vacías que no muestran un mensaje indicando que no hay elementos.
- Acciones que causan 500 sin feedback al usuario.

---

### 🟡 7.4 Paginación — UX (MODERADO)

**Qué buscar**:

- Mostrar TODOS los números de página posibles en los controles de paginación (ej: 1, 2, 3, ..., 500).
- Si no hay más páginas, no mostrar controles de "siguiente".
- Permitir al usuario seleccionar cuántos elementos por página.
- Páginas que muestran "Página 55/3" cuando se pide una página que excede el máximo.
- Paginación rota en responsive.

---

### 🟡 7.5 Textos hardcodeados / falta i18n (MODERADO)

**Qué buscar**:

- Textos en la UI que no están internacionalizados (directamente en español/inglés en el JSP).
- Enums que se muestran con su nombre técnico (ej: `FRONT_END`) en lugar de un texto legible internacionalizado.
- Alt texts de imágenes sin internacionalizar.
- Mensajes que muestran la key del i18n en lugar del texto (ej: `invite.friends.send`).

---

### 🟡 7.6 No redirigir al flujo que inició después de login/register (MODERADO)

**Qué buscar**:

- Tras registrarse, el usuario es siempre redirigido a la home en lugar de retomar el flujo que inició.
- Tras loguearse, no volver a la página donde estaba.

---

### 🟡 7.7 Register no loguea al usuario automáticamente (MODERADO)

**Qué buscar**:

- Después de registrarse (y verificar email si aplica), el usuario tiene que loguearse manualmente. Lo correcto es loguearlo automáticamente.

---

### 🔵 7.8 Responsive roto (MENOR)

**Qué buscar**:

- Vistas que se rompen en mobile/tablet.
- Elementos que se solapan, textos que se cortan, botones inaccesibles en responsive.

---

### 🔵 7.9 Aprovechar el espacio de pantalla (MENOR)

**Qué buscar**:

- Secciones muy estrechas que desaprovechan el ancho de pantalla.
- Layouts con mucho espacio desperdiciado.

---

## 8. MAILING

### 🟡 8.1 Mails sin CTA (Call To Action) a la página principal (MODERADO)

**Qué buscar**:

- Mails que no tienen un link/botón que lleve de vuelta a la página principal de la aplicación.
- Headers de mail que no son clickeables.

---

### 🟡 8.2 Mails apuntando a localhost (MODERADO)

**Qué buscar**:

- URLs en los mails que apuntan a `localhost` en lugar de la URL de producción.

---

### 🟡 8.3 Falta templates en los mails (MODERADO)

**Qué buscar**:

- Mails que se envían como texto plano sin template HTML.
- Mails cuya paleta de colores no coincide con la aplicación.

---

### 🔵 8.4 SMTP configurado para debug en producción (MENOR)

**Qué buscar**:

- Configuración de la conexión SMTP con debug habilitado en el entorno de producción.

---

## 9. CÓDIGO / ESTILO

### 🟡 9.1 Loguear a salida estándar sin logger (MODERADO)

**Frecuencia**: Aparece en **más de 5 grupos**. Marcado como error conceptual grave en algunos casos.

**Qué buscar**:

- Uso de `System.out.println()` o `System.err.println()` para logging.
- Falta de un framework de logging (SLF4J + Logback/Log4j).
- `hibernate.show_sql=true` en producción.

---

### 🟡 9.2 Boxed primitives sin sentido (MODERADO)

**Frecuencia**: Aparece en **más de 6 grupos**.

**Qué buscar**:

- Campos como `Long id` donde `long` sería suficiente (y null no tiene sentido semántico).
- Funciones que devuelven `Boolean` en mayúscula cuando devuelven `int` (0 o 1) para indicar si/no.
- Auto-unboxing de valores boxed sin chequeo de null previo.

---

### 🟡 9.3 Modificadores de acceso omitidos (MODERADO)

**Frecuencia**: Aparece en **más de 6 grupos**.

**Qué buscar**:

- Clases, métodos, o campos sin modificador de acceso explícito (package-private por default sin que parezca ser por diseño).
- Métodos que deberían ser `private` pero son `public` o package-private.

---

### 🟡 9.4 Magic numbers (MODERADO)

**Qué buscar**:

- Números literales en el código sin constantes con nombre (ej: `if (size > 5242880)` en lugar de `if (size > MAX_FILE_SIZE)`).

---

### 🟡 9.5 Uso de strings donde corresponden enums (MODERADO)

**Qué buscar**:

- Campos como `orderBy`, `status`, `role`, `day` que son `String` en lugar de un `enum`.
- Parámetros de ordenamiento que se pasan como strings sin validar.

---

### 🟡 9.6 Naming — clases con prefijo `Custom` (MODERADO)

**Qué buscar**:

- Clases nombradas `CustomX` — por definición, toda implementación de `X` es "custom". El nombre debería reflejar **qué hace de especial**, no que es custom.

---

### 🟡 9.7 Clases de utilidades instanciables (MODERADO)

**Qué buscar**:

- Clases de utilidades (solo métodos estáticos) que no tienen constructor privado y por tanto se pueden instanciar.

---

### 🔵 9.8 Repositorio con archivos innecesarios (MENOR)

**Qué buscar**:

- Archivos del IDE (`.iml`, `.idea/`, `.vscode/`).
- Archivos autogenerados.
- Código viejo comentado (más de 1 archivo).
- TODOs en el código, especialmente los que mencionan no entender qué hace el código.
- JARs o fonts embebidas en el repositorio que deberían venir de CDN o Maven.

---

### 🔵 9.9 Commits con mensajes poco claros (MENOR)

**Qué buscar**:

- Commits con mensajes como "aaaaaa", "fix", "update", sin descripción clara.

---

### 🔵 9.10 Convención de nombres (MENOR)

**Qué buscar**:

- Mezcla de `snake_case` y `camelCase` en Java (debe ser `camelCase`).
- Paquetes con nombres en mayúscula.
- Campos como `image_content` en lugar de `imageContent` en forms.

---

## 10. HIBERNATE / JPA ESPECÍFICO

### 🟠 10.1 Relaciones EAGER en @ManyToOne innecesarias (GRAVE)

**Qué buscar**:

- `@ManyToOne(fetch = FetchType.EAGER)` donde la relación no siempre se necesita.
- Especialmente problemático cuando la entidad relacionada tiene a su vez otras relaciones eager → cascada de carga innecesaria.

---

### 🟠 10.2 Relaciones @OneToMany sin FetchType.LAZY (GRAVE)

**Qué buscar**:

- `@OneToMany` sin `FetchType.LAZY` explícito (por default JPA usa LAZY para colecciones, pero Hibernate puede variar).
- Relaciones que cargan datos innecesarios en cada query.

---

### 🟡 10.3 Entidades mal mapeadas — relaciones por ID pelado (MODERADO)

**Qué buscar**:

- Entidades que mapean relaciones usando `Long userId` en lugar de `@ManyToOne User user`.
- Falta de foreign keys y constraints semánticas en el mapeo.

---

### 🟡 10.4 Filtrado en memoria en lugar de en la DB (MODERADO)

**Qué buscar**:

- Métodos que traen toda una colección de la DB y luego filtran con `.stream().filter(...)` en Java.
- Esto debe hacerse con una query JPQL que filtre directamente en la base de datos.

---

## 11. EXCEPTION HANDLING

### 🟠 11.1 ExceptionHandler repetitivos (GRAVE)

**Qué buscar**:

- Múltiples métodos `@ExceptionHandler` que hacen lo mismo para distintas excepciones → deberían agruparse con un solo método que reciba múltiples tipos.

---

### 🟡 11.2 Try-catch solo para loguear (MODERADO)

**Qué buscar**:

- `try { ... } catch (Exception e) { log.error(...); }` que captura la excepción solo para loguearla y se la traga sin re-lanzar ni manejar.

---

### 🟡 11.3 Excepciones genéricas (MODERADO)

**Qué buscar**:

- Catch de `Exception` genérica en lugar de excepciones específicas.
- Uso de `IllegalArgumentException` para todo en lugar de excepciones custom del dominio.
- Catch de `NoSuchElementException` o `NullPointerException` que indica un bug de programación en lugar de una validación explícita.

---

### 🟡 11.4 No hacer catch en controllers — usar ControllerAdvice (MODERADO)

**Qué buscar**:

- Controllers que hacen `try/catch` para manejar excepciones. Esto debe delegarse al `@ControllerAdvice` con `@ExceptionHandler`.

---

## 12. CONFIGURACIÓN

### 🟡 12.1 `hibernate.show_sql=true` en producción (MODERADO)

**Qué buscar**: `hibernate.show_sql` seteado en `true` en la configuración de producción.

---

### 🟡 12.2 Logback/Log4j mal configurado (MODERADO)

**Qué buscar**:

- Nivel de log `DEBUG` para paquetes de la aplicación (`ar.edu.itba.paw`) en producción.
- Falta de configuración de logging.

---

### 🟡 12.3 Versiones de dependencias no alineadas (MODERADO)

**Qué buscar**:

- Dependencias de Spring, Hibernate, etc. que usan versiones diferentes entre sí cuando deberían usar la misma.
- Versiones definidas directamente en módulos hijos en lugar del `<dependencyManagement>` del root pom.

---

### 🔵 12.4 ConcurrentMapCacheManager sin eviction (MENOR)

**Qué buscar**:

- Uso de `ConcurrentMapCacheManager` para cachear imágenes u otros datos sin estrategia de eviction → la memoria puede crecer indefinidamente.

---

### 🔵 12.5 TaskExecutor con queueCapacity limitado arbitrariamente (MENOR)

**Qué buscar**:

- Configuración de `ThreadPoolTaskExecutor` con `queueCapacity` muy bajo sin justificación.

---

## RESUMEN — TOP 5 ERRORES MÁS CRÍTICOS (por frecuencia y gravedad)

| # | Error | Sección | Frecuencia |
|---|-------|---------|------------|
| 1 | Lógica de negocio en Controllers (servicios no son Facades) | 1.1 | 15+ grupos |
| 2 | No usar paginación modelo 1+1 | 2.1 | 12+ grupos |
| 3 | XSS por `${}` sin `<c:out>` en JSPs | 5.1 | 8+ grupos |
| 4 | Control de acceso en controllers sin Spring Security | 1.3 | 10+ grupos |
| 5 | Colecciones de alta cardinalidad mapeadas en entidades | 2.2 | 8+ grupos |

---

> [!TIP]
> **Para el escaneo automatizado del repo**, priorizar en este orden:
>
> 1. Buscar `${}` en archivos `.jsp` y `.tag` que no estén dentro de `<c:out>` ni `<spring:message>` ni atributos de taglibs.
> 2. Revisar todos los métodos de Controllers que hacen más de una llamada a servicios.
> 3. Verificar que todas las queries paginadas usen el modelo 1+1.
> 4. Buscar `@OneToMany` y `@ManyToMany` en entidades y verificar que no sean de alta cardinalidad.
> 5. Buscar chequeos de permisos manuales en controllers.
> 6. Verificar `@Transactional` en todos los métodos de servicio.
> 7. Buscar `System.out.println` y `System.err.println`.
> 8. Verificar validación de `page`/`size` en endpoints paginados.
> 9. Revisar cobertura de tests de servicios y DAOs.
> 10. Buscar `try/catch` en controllers.
