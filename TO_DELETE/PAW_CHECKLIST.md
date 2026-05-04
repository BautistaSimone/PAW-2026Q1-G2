# PAW — Checklist de Cumplimiento del Proyecto (Unidades 1–8)

> **Propósito**: Este documento contiene todas las reglas, convenciones y buenas prácticas extraídas de los apuntes de la materia *72.38 — Proyecto de Aplicaciones Web* (Unidades 1 a 8). Está diseñado para que una IA revise el repositorio completo y verifique que el proyecto cumple con cada punto.

---

## Índice

1. [Arquitectura y Modelo de Capas](#1-arquitectura-y-modelo-de-capas)
2. [Maven y Gestión de Dependencias](#2-maven-y-gestión-de-dependencias)
3. [Spring MVC y Controllers](#3-spring-mvc-y-controllers)
4. [JSP, JSTL y Frontend](#4-jsp-jstl-y-frontend)
5. [Base de Datos y Persistencia (JDBC)](#5-base-de-datos-y-persistencia-jdbc)
6. [Seguridad](#6-seguridad)
7. [Formularios y Validación](#7-formularios-y-validación)
8. [Internacionalización (i18n)](#8-internacionalización-i18n)
9. [Spring Security](#9-spring-security)
10. [Unit Testing y Mocking](#10-unit-testing-y-mocking)
11. [Logging](#11-logging)
12. [Mailing](#12-mailing)
13. [Imágenes](#13-imágenes)
14. [AOP y Transacciones](#14-aop-y-transacciones)

---

## 1. Arquitectura y Modelo de Capas

El proyecto sigue un **modelo de capas estricto** (estilo cebolla). Cada capa solo puede comunicarse con la capa inmediatamente inferior. **No se puede bypassear capas.**

### Módulos obligatorios

| Módulo | Responsabilidad |
|---|---|
| `models` | Entidades del dominio (POJOs). |
| `service-contracts` | Interfaces de la capa de servicios. |
| `services` | Implementaciones de los servicios (lógica de negocio). |
| `persistence-contracts` | Interfaces de la capa de persistencia (DAOs). |
| `persistence` | Implementaciones JDBC/JPA de los DAOs. |
| `webapp` | Capa de presentación: controllers, JSPs, configuración Spring. |

### Reglas

- [ ] **Flujo estricto**: `Controller → Service → DAO`. Un controller **NUNCA** debe invocar un DAO directamente.
- [ ] **Sin lógica de negocio en controllers**: La capa web NO debe contener lógica de aplicación. Solo recibe input, delega al servicio y elige una vista. Tener lógica en esta capa **se penaliza en las entregas**.
- [ ] **Sin lógica de presentación en servicios**: Los servicios no deben conocer nada de HTTP, JSP ni Spring MVC.
- [ ] **Creación de entidades solo en persistencia**: Nadie debería poder crear una instancia de una entidad de dominio en memoria si no es a través de la capa de persistencia. Si existe en runtime, es porque existe en la BD.
- [ ] **Interfaces obligatorias**: Servicios y DAOs siempre deben tener una interfaz correspondiente en los módulos `-contracts`.
- [ ] **Sin estados compartidos en clases**: Las clases Java NO deben tener estado mutable compartido. Deben ser **thread-safe** (idealmente inmutables).

---

## 2. Maven y Gestión de Dependencias

- [ ] **Versiones centralizadas**: Todas las versiones en `<properties>` del `pom.xml` raíz.
- [ ] **`<dependencyManagement>` en el padre**: Declarar todas las dependencias con versiones y scopes.
- [ ] **Módulos sin `<version>`**: Los hijos referencian dependencias sin especificar versión.
- [ ] **Versiones alineadas para Spring**: Una única property (ej: `<spring.version>`) para todos los artefactos Spring.
- [ ] **Spring Security versión separada**: Tiene su propia versión independiente.
- [ ] **Scopes correctos**: `compile` (default), `provided` (provista por el container), `runtime` (no en compilación), `test` (solo tests).
- [ ] **No depender de transitividad**: Cada módulo debe declarar explícitamente lo que necesita.

---

## 3. Spring MVC y Controllers

- [ ] **Anotaciones correctas**: `@Controller`, `@Service`, `@Repository`.
- [ ] **`@ComponentScan` configurado**: `WebConfig` apunta a los paquetes correctos.
- [ ] **`@Autowired` para inyección**: Inyección de dependencias, no instanciación manual.
- [ ] **Regex para IDs numéricos**: `@RequestMapping("/{id:\\\\d+}")` para path variables numéricas (devuelve 404 en vez de 400 para no-numéricos).
- [ ] **ViewResolver configurado**: `InternalResourceViewResolver` con prefix `/WEB-INF/jsp/` y suffix `.jsp`.
- [ ] **JSPs privados**: Todos dentro de `WEB-INF/` para que no sean accesibles por URL directa.
- [ ] **ResourceHandlers**: Configurar en `WebConfig` para servir assets estáticos.
- [ ] **`Optional<T>` en DAOs**: Nunca devolver `null` desde `findById`. Usar `Optional<T>`.
- [ ] **Excepciones ante ausencia**: Lanzar `ResourceNotFoundException` (404) en vez de devolver null.
- [ ] **`@ControllerAdvice`**: Manejo global de excepciones con `GlobalExceptionHandler`.

---

## 4. JSP, JSTL y Frontend

- [ ] **Formato Bean**: Objetos del model deben ser Java Beans (propiedades privadas, getters/setters, constructor sin args).
- [ ] **Sin scriptlets**: Los JSPs NO deben contener código Java (`<% %>`). Usar JSTL.
- [ ] **`<c:out>` para todo output dinámico**: SIEMPRE usar `<c:out value="${variable}" />`. NUNCA `${variable}` directo (previene XSS).
- [ ] **`<c:url>` para todas las URLs**: Garantiza funcionamiento correcto con web context en deploy.
- [ ] **`<c:if>` y `<c:forEach>`**: Para condicionales e iteraciones.
- [ ] **CSS separado, no inline**: Nunca estilos inline en HTML.
- [ ] **Flexbox/Grid para layout**: Usar primitivas nativas de CSS.
- [ ] **Tags reutilizables**: Componentes UI en `WEB-INF/tags/`.

---

## 5. Base de Datos y Persistencia (JDBC)

- [ ] **`JdbcTemplate` de Spring**: No usar JDBC primitivo.
- [ ] **`RowMapper` reutilizable**: Definir como constante `static final` de la clase DAO.
- [ ] **Placeholders `?` siempre**: NUNCA concatenar parámetros en queries (error GRAVE por SQL injection).
- [ ] **`SimpleJdbcInsert`**: Para inserts, con `usingGeneratedKeyColumns()` para IDs auto.
- [ ] **`DataSource` en `WebConfig`**: Configurar como bean.
- [ ] **`DataSourceInitializer`**: Para ejecutar `schema.sql` al arranque.
- [ ] **Escapar wildcards en LIKE**: Pre-escapar `%` y `_` del input del usuario.

---

## 6. Seguridad

### XSS
- [ ] **`<c:out>` obligatorio**: NUNCA `${variable}` directo en HTML de JSPs/tags.

### SQL Injection
- [ ] **Placeholders `?`**: En TODAS las queries. NUNCA concatenar.
- [ ] **Escapar wildcards LIKE**: Pre-escapar `%` y `_`.

### Uploads
- [ ] **Validar tamaño** (ej: max 5MB).
- [ ] **Verificar MIME type** lógico, no solo extensión.
- [ ] **Rechazar payloads inválidos** con `IllegalArgumentException`.

### Errores
- [ ] **NUNCA exponer stacktraces** al navegador.
- [ ] **Excepciones semánticas**: `ResourceNotFoundException` (404), `IllegalArgumentException` (400).

### Tokens
- [ ] **`MessageDigest.isEqual()`**: Comparación en tiempo constante para tokens/secretos.

---

## 7. Formularios y Validación

- [ ] **Form beans**: Clases Form en el paquete `forms` de webapp.
- [ ] **JSR-303 annotations**: `@Size`, `@Email`, `@NotNull`, `@Pattern`, etc.
- [ ] **`@Valid` + `BindingResult`**: En la firma del método del controller, `BindingResult` justo después del form bean.
- [ ] **Verificar `hasErrors()`**: Re-renderizar formulario si hay errores.
- [ ] **`@ModelAttribute`**: Para bindear campos automáticamente.
- [ ] **Spring Form taglib**: `<form:form>`, `<form:input>`, `<form:errors>`.
- [ ] **Validación de negocio en servicios**: Precios positivos, strings no vacíos, bounds de enums → capa de servicios.

---

## 8. Internacionalización (i18n)

### Configuración
- [ ] **`MessageSource`**: `ReloadableResourceBundleMessageSource` con basename `classpath:messages`.
- [ ] **`SessionLocaleResolver`**: Default `es_AR`.
- [ ] **`LocaleChangeInterceptor`**: Parámetro `?lang=` para cambio dinámico.

### Archivos
- [ ] **`messages.properties`**: Default (español), archivo primario.
- [ ] **`messages_en.properties`**: Traducciones inglés.
- [ ] **Keys en AMBOS archivos**: Siempre agregar a los dos.
- [ ] **Convención**: `PageName.element.property`.

### Uso en JSPs
- [ ] **Taglib declarado**: `<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>`.
- [ ] **`<spring:message>`**: Para TODO texto estático visible. NUNCA hardcodear.
- [ ] **`var` para atributos**: Capturar en variable para usar en `placeholder`, `aria-label`, `title`.
- [ ] **Mensajes parametrizados**: `{0}`, `{1}` con `arguments`. NUNCA mezclar tag con texto hardcodeado alrededor.
- [ ] **Títulos internacionalizados**: Usar `<spring:message>` con `var` para títulos de página.
- [ ] **Accesibilidad internacionalizada**: Aria-labels también usan i18n.
- [ ] **Errores de validación custom**: `Annotation.formName.field` en `.properties` (ej: `Size.registerForm.password`).

---

## 9. Spring Security

- [ ] **Versión separada**: `<spring-security.version>` independiente.
- [ ] **Config separada**: `WebAuthConfig` extendiendo `WebSecurityConfigurerAdapter`.
- [ ] **Filter**: `DelegatingFilterProxy` con nombre `springSecurityFilterChain` en `web.xml`.
- [ ] **`UserDetailsService` propio**: `PawUserDetailsService` en paquete `auth` de webapp.
- [ ] **Roles como Set**: Un usuario puede tener múltiples roles. No asumir herencia entre roles.
- [ ] **Prefijo `ROLE_`**: Convención obligatoria de Spring Security.
- [ ] **`BCryptPasswordEncoder`**: NUNCA `PlainTextPasswordEncoder` en producción.
- [ ] **Encodear al crear usuario**: Usar `PasswordEncoder` en `UserServiceImpl`.
- [ ] **Reglas por roles**: ACL define permisos sobre roles, no sobre usuarios individuales.

---

## 10. Unit Testing y Mocking

### Estructura
- [ ] **Tests en `src/test/java`**: Convención Maven.
- [ ] **Schema test en `src/test/resources/schema.sql`**.
- [ ] **3 etapas**: Setup → Ejercitar (1 llamada) → Validaciones (max 4 assertions).

### Persistencia
- [ ] **HSQLDB embebida**: BD en memoria para tests. Garantiza condiciones idénticas.
- [ ] **`TestConfig`**: Con `DataSource` embebido y `DataSourceInitializer`.
- [ ] **Spring Test runner**: `@RunWith(SpringJUnit4ClassRunner.class)` + `@ContextConfiguration`.
- [ ] **No manipular objeto bajo test en setup**: Solo tocarlo en la ejercitación.

### Servicios (Mocking)
- [ ] **Mockito**: Mockear DAOs al testear servicios.
- [ ] **`@Mock` + `@InjectMocks`**: Simplificar setup.
- [ ] **`MockitoJUnitRunner`**: Runner de Mockito para tests de servicios.
- [ ] **NO usar `Mockito.verify()`**: Verificar comportamiento (output), no implementación. Esto se desaconseja explícitamente.

---

## 11. Logging

### Configuración
- [ ] **SLF4J + Logback**: Usar `org.slf4j.Logger` y `LoggerFactory`. Independiente de implementación.
- [ ] **`logback-test.xml`**: Desarrollo → ConsoleAppender, DEBUG.
- [ ] **`logback.xml`**: Producción → FileAppender con rotación, INFO o superior.
- [ ] **Excluir `logback-test.xml` del WAR**: Configurar `maven-war-plugin`.
- [ ] **Producción NO a consola ni en DEBUG**.

### Uso
- [ ] **Logger por clase**: `private static final Logger LOGGER = LoggerFactory.getLogger(MiClase.class);`.
- [ ] **Placeholders `{}`**: NUNCA concatenar strings. Usar `LOGGER.info("User: {}", email);`.
- [ ] **Throwable como último arg**: Para stack traces completos.
- [ ] **Niveles apropiados**: ERROR, WARN, INFO, DEBUG usados correctamente.
- [ ] **`additivity="false"`**: En loggers específicos para evitar duplicados.

---

## 12. Mailing

- [ ] **Enviar SIEMPRE desde servicios**: NUNCA desde controllers. Error conceptual clave.
- [ ] **`@Async`**: Mails no bloqueantes. Habilitar `@EnableAsync`.
- [ ] **Contenido informativo**: Info que el usuario no conozca.
- [ ] **Formato**: Header (branding) → Body (info) → CTA (link) → Footer (opcional).
- [ ] **URLs dinámicas**: No hardcodear `localhost` en links de mails.
- [ ] **Internacionalizados**: Respetar idioma del usuario. Probar en todos los idiomas.
- [ ] **Thymeleaf**: Motor de templates recomendado para mails.

---

## 13. Imágenes

- [ ] **Tabla separada**: Tabla con `id` y `content` (bytea). Entidad referencia por FK.
- [ ] **NUNCA Base64**: Almacenar de forma plana.
- [ ] **NUNCA JOIN con tabla de imágenes**: Equivale a tenerlas en la misma tabla. Cargar solo cuando se necesite.
- [ ] **Endpoint dedicado**: Controller específico para servir imágenes con content-type correcto.

---

## 14. AOP y Transacciones

- [ ] **`@Transactional` en servicios**: Nivel recomendado. Operaciones compuestas atómicas.
- [ ] **Consistencia**: Elegir un nivel y mantenerlo.
- [ ] **Solo métodos de interfaz**: AOP funciona solo en métodos expuestos por la interfaz (proxies).
- [ ] **Solo invocaciones externas**: `this.metodo()` NO pasa por el proxy. AOP no aplica a llamadas internas.
- [ ] **Anotar la clase es válido**: Porque invocaciones internas no pasan por proxy de todas formas.

---

## Errores GRAVES según la Cátedra

> [!CAUTION]
> Estos errores son explícitamente mencionados como **graves** o **penalizados**:

| Error | Unidad |
|---|---|
| Lógica de negocio en controllers | U4 |
| Concatenar parámetros en SQL (injection) | U4 |
| Devolver `null` en vez de `Optional<T>` en DAOs | U4 |
| `${variable}` sin `<c:out>` en JSPs (XSS) | U3/U4 |
| No usar `<c:url>` para URLs | U3 |
| `PlainTextPasswordEncoder` en producción | U7 |
| Enviar mails fuera de la capa de servicios | U2 |
| JOIN con tabla de imágenes | U2 |
| Base64 para imágenes | U2 |
| STDOUT/DEBUG en producción (logging) | U7 |
| Concatenar strings en logs | U7 |
| `Mockito.verify()` para validar implementación | U5 |
| Hardcodear texto en JSPs (sin i18n) | U6 |
| Estados compartidos en clases (thread-safety) | U2 |

---

## 15. Correcciones Frecuentes de TPs Anteriores

> [!IMPORTANT]
> Esta sección recopila **todos los errores recurrentes** encontrados en las devoluciones de TPs de años anteriores (2024 y 2025). Son correcciones reales de la cátedra sobre ~28 grupos. Los errores marcados como "**error conceptual grave**" son los que más impactan en la nota.

### 15.1 Arquitectura y Modelo de Capas

- [ ] **Los servicios deben ser verdaderos Facades**: Un servicio debe exponer un método de alto nivel por caso de uso (ej: `register(email, password)`) y orquestar internamente llamadas a DAOs, mails, etc. El **controller NO debe orquestar múltiples servicios** para completar un caso de uso. Esto fue el error **más repetido** en todas las devoluciones.
- [ ] **No implementar servicios en la capa web**: La capa webapp solo contiene controllers, configuración y auth. Nunca crear `@Service` en webapp (excepto `UserDetailsService` de Spring Security).
- [ ] **Templates de mail NO deben ser referenciados desde servicios**: Si los templates de Thymeleaf viven en webapp, los servicios no deben conocer sus nombres ni sus variables. Solución: que cada servicio tenga métodos de alto nivel (`sendWelcomeEmail(user)`) y que el mail service encapsule el detalle de templates internamente.
- [ ] **`EmailService.sendEmail()` NO debe ser público**: Métodos genéricos como `sendHtmlMessage`, `sendSimpleMessage` obligan al llamador a conocer detalles de templates. Crear un método público por cada tipo de mail.
- [ ] **`schema.sql` debe estar en `persistence/src/main/resources/`**: No en webapp. La existencia de una BD es detalle de implementación de persistencia.
- [ ] **No usar `java.sql.*` fuera de persistencia**: Clases como `java.sql.Timestamp`, `java.sql.Date` son detalles de implementación del DAO. Usar `LocalDate`, `LocalDateTime`, `Instant` en modelos.
- [ ] **No pasar `MultipartFile` a los DAOs**: El DAO debe recibir `byte[]`. `MultipartFile` es un concepto de la capa web.
- [ ] **Un DAO no debe insertar en tablas que no le corresponden**: Cada DAO maneja su tabla principal. Si se necesitan inserts en otras tablas, delegarlo al servicio que coordina.

### 15.2 Controllers

- [ ] **No tener controllers con +300 líneas**: Dividir responsabilidades en múltiples controllers. Un controller con ~700 líneas denota mala separación.
- [ ] **Usar `@ControllerAdvice` para manejo de errores**: No crear un "ErrorController" — es un `@ControllerAdvice`. Centralizar exception handlers ahí, no repetirlos en cada controller.
- [ ] **No hacer `try-catch` de `Exception` en controllers**: Los errores deben ser manejados por el `@ControllerAdvice`. Si se necesita validación de negocio, usar custom validators.
- [ ] **`@Valid` tanto en POST como en GET**: Validar forms en GET también, especialmente cuando se reciben query parameters de búsqueda/filtros.
- [ ] **No definir parámetros que no se usan** en métodos de controllers.
- [ ] **Constantes de paginación fuera de controllers**: Mover a una clase utilitaria o constante centralizada. La lógica de paginación (cálculo de páginas, offset) también debería estar en una clase wrapper `Page<T>`.
- [ ] **Usar `page` en vez de `offset`**: La paginación a nivel web debe expresarse como número de página, no como offset. El offset es detalle de implementación de SQL.
- [ ] **Redirect después de POST**: Después de procesar un POST (crear, editar, borrar), hacer `redirect:` para evitar que la URL quede apuntando al endpoint POST.
- [ ] **Extraer `getCurrentUser()` a un `@ModelAttribute` o `ControllerAdvice`**: No repetir `SecurityContextHolder.getContext().getAuthentication()...` en cada controller.
- [ ] **HTTP verbs correctos**: DELETE para borrar, POST para crear, no usar POST para ambos.

### 15.3 Seguridad y Spring Security

- [ ] **Control de acceso por ownership**: No alcanza con verificar roles. Si un usuario solo puede editar **sus propios** recursos, verificar ownership en Spring Security (ej: `@PreAuthorize` con SpEL o `AccessDecisionVoter`), no con `if` en controllers.
- [ ] **No hacer chequeos de permisos manuales en controllers**: Todo control de acceso debe estar en `WebAuthConfig` o usando anotaciones de Spring Security. Los controllers NO deben tener `if (user.isMod())`.
- [ ] **Usar `spring-security-taglibs` en JSPs**: Para mostrar/ocultar elementos según roles, usar `<sec:authorize access="hasRole('ADMIN')">` en vez de `<c:if test="${user.mod}">`. Esto evita duplicar lógica.
- [ ] **Remember-me key segura**: La key de remember-me NO debe ser un string corto hardcodeado como "REMEMBER ME". Usar una key larga y aleatoria, idealmente desde properties.
- [ ] **Usuarios no verificados**: Usar el parámetro `enabled` de `UserDetails.User` para bloquear usuarios no verificados. No hacer verificación manual en controllers.
- [ ] **Usuarios bloqueados**: Usar `accountNonLocked` de `UserDetails`. Capturar `LockedException` para mostrar pantalla custom.
- [ ] **403 vs 404**: Retornar 403 (Forbidden) cuando el usuario no tiene permisos, 404 cuando el recurso no existe. No mezclarlos.

### 15.4 Validación y Forms

- [ ] **No validar forms manualmente en controllers**: Usar Bean Validation (JSR-303) con anotaciones y custom validators.
- [ ] **Custom Validators para validaciones de negocio complejas**: Por ejemplo, que un email no esté duplicado.
- [ ] **No hacer `try-catch` de excepciones para mostrar errores de validación**: Usar `BindingResult` y custom validators en su lugar.
- [ ] **Validar query parameters de GET**: Los parámetros de búsqueda y filtros también deben validarse con `@Valid`.
- [ ] **No usar `Optional` como parámetro**: No recibir `Optional<Integer>` como parámetro de método. Usar valores default en su lugar.
- [ ] **Confirmación antes de acciones destructivas**: Pedir confirmación al usuario antes de borrar (modal de confirmación JS).

### 15.5 Modelos y Java

- [ ] **Utility classes no instanciables**: Clases utilitarias deben tener constructor privado (item 4 de Effective Java).
- [ ] **Overridear `equals()` implica overridear `hashCode()`**: Item 11 de Effective Java.
- [ ] **No usar boxed primitives sin necesidad**: No usar `Long`, `Integer` donde `long`, `int` bastan. Si se usa boxed, siempre verificar null antes de unboxing para evitar `NullPointerException`.
- [ ] **Usar enums en vez de magic strings**: Roles, estados, tipos de servicio, días de la semana, etc. deben ser enums, no strings hardcodeados.
- [ ] **No usar `Optional<List>`**: Retornar `Collections.emptyList()` en vez de `Optional.empty()` para colecciones.
- [ ] **No hacer `optional.get()` sin `isPresent()`**: Usar `orElseThrow()`, `orElse()`, `ifPresent()` en su lugar.
- [ ] **No usar `Optional` como campo de clase**: Optional es solo para retorno de funciones, nunca como field.
- [ ] **No pasar `Optional` como parámetro entre capas**: Usar valores default o sobrecarga de métodos.
- [ ] **`@Override` en implementaciones**: Todos los métodos que implementan una interfaz deben tener `@Override`.
- [ ] **Convención de nombres Java**: Paquetes en minúsculas, clases en PascalCase, modelos en singular (`User`, no `Users`).
- [ ] **No nombrar clases `CustomX`**: Toda implementación es "custom" por definición. Usar nombres descriptivos.
- [ ] **No usar `Collections.EMPTY_LIST`**: Usar `Collections.emptyList()` (type-safe).
- [ ] **No usar `StringBuilder` para queries estáticas**: Si la query no cambia, usar un `String` constante (`static final`).
- [ ] **Usar `name()` en vez de `toString()` para enums**: `toString()` puede ser overridden, `name()` no.
- [ ] **Consistencia en inyección de dependencias**: Elegir una forma (constructor, field, setter) y ser consistente en todo el proyecto.
- [ ] **Modificadores de acceso explícitos**: No omitir `private`, `public`, etc. (package-private por default puede ser confuso).
- [ ] **No suprimir excepciones**: Nunca hacer catch y no loguear/relanzar. Esto oculta errores.
- [ ] **Excepciones custom deben extender `RuntimeException` o `Exception`**: Nunca extender `Throwable` directamente.
- [ ] **Evitar `System.out.println` / `printStackTrace()`**: SIEMPRE usar loggers de SLF4J. `printStackTrace()` loguea a STDERR sin contexto.

### 15.6 Persistencia

- [ ] **`SimpleJdbcInsert` inicializado UNA sola vez**: Inicializar en el constructor del DAO, no en cada llamada a método. Inicializarlo repetidamente no es thread-safe.
- [ ] **RowMappers como `private static final`**: No instanciarlos en cada ejecución.
- [ ] **No llamar otro DAO dentro de un RowMapper**: Usar JOINs SQL para obtener datos relacionados en una sola query.
- [ ] **No hacer paginación en Java**: Siempre paginar a nivel SQL (`LIMIT`/`OFFSET`). La paginación en Java implica cargar todos los datos en memoria.
- [ ] **No hacer sorting en Java**: Usar `ORDER BY` en SQL.
- [ ] **No hacer JOINs en Java**: Si se necesitan datos de varias tablas, hacer JOIN en SQL, no iterar con N queries (problema N+1).
- [ ] **Relaciones N:M con tablas intermedias**: Usar junction tables, no arrays ni duplicación de datos.
- [ ] **Usar `ON DELETE CASCADE` donde corresponda**: No borrar relaciones manualmente en Java si la BD puede manejar cascadas.
- [ ] **Borrados lógicos vs físicos**: Considerar borrado lógico (campo `active`/`deleted`) para preservar integridad referencial.
- [ ] **`@Rollback` en tests de persistencia**: Usar la anotación para que cada test deje la BD limpia sin borrado manual.

### 15.7 `@Transactional`

- [ ] **`@Transactional` en TODOS los métodos de servicio**: No solo en los de escritura.
- [ ] **`@Transactional(readOnly = true)` en métodos de lectura**: Optimiza performance y comunica la intención del método.
- [ ] **No tener `@Transactional` en ningún lugar es error grave**: Fue marcado explícitamente como tal.

### 15.8 Testing

- [ ] **Tests UNITARIOS deben ser unitarios**: En tests de DAO, no usar OTROS DAOs para setup. Usar `JdbcTemplate` directamente o SQL scripts.
- [ ] **No manipular el objeto bajo test en precondiciones/postcondiciones**: Solo tocarlo en la ejercitación.
- [ ] **No usar `Mockito.verify()` ni `Mockito.spy()`**: Verificar comportamiento (output), no implementación.
- [ ] **Orden correcto de assertions**: `assertEquals(expected, actual)`. Invertirlos genera mensajes de error confusos.
- [ ] **No hacer `assertNotNull` de primitivos**: `long` nunca es null, el test siempre pasa.
- [ ] **Cobertura adecuada**: Testear happy paths Y edge cases / error cases. DAOs sin tests o con solo 1 test de create es insuficiente.
- [ ] **Tests de servicios obligatorios**: Si el servicio tiene lógica de negocio, debe tener tests. Es requerimiento del enunciado.
- [ ] **Validar estado final de la BD en tests de persistencia**: Usar `JdbcTestUtils.countRowsInTable()` para verificar que los datos se insertaron/actualizaron correctamente.
- [ ] **No testear utilidades de test en vez del DAO**: El test debe probar el DAO, no el helper que armaste para setup.

### 15.9 Mailing

- [ ] **`@Async` en TODOS los métodos de envío de mail**: No olvidarse en ninguno.
- [ ] **Subjects de mails internacionalizados**: No hardcodear strings. Usar keys de `messages.properties`.
- [ ] **Locale del destinatario, no del emisor**: Al enviar un mail, usar el locale del usuario que lo **recibe**, no del que realizó la acción.
- [ ] **No tener templates de mail duplicados por idioma**: Usar Thymeleaf con internacionalización (same template, different messages).
- [ ] **No hardcodear credenciales SMTP en código**: Usar properties file.
- [ ] **SMTP debug deshabilitado en producción**: Configuración de mail no debe estar en debug en producción.
- [ ] **BaseURL inyectada desde properties**: No hardcodear `localhost:8080` ni la URL de producción. Usar `@Value("${app.baseUrl}")`.
- [ ] **CTA (Call to Action) claro en mails**: Todo mail debe tener un botón/link que lleve al usuario a la acción relevante.
- [ ] **CTA con URL correcta**: El link debe funcionar (no apuntar a rutas rotas, no usar el web context incorrecto).

### 15.10 Internacionalización (Complemento)

- [ ] **Todo texto visible internacionalizado**: Incluye botones, labels, dropdowns, mensajes de error, mensajes vacíos ("No hay resultados"), categorías, estados.
- [ ] **No usar interpolaciones en i18n con valores hardcodeados**: Usar `{0}`, `{1}` correctamente en los `.properties`.
- [ ] **Fechas y horas formateadas con i18n**: No formatear fechas en el modelo como strings. Usar `<fmt:formatDate>` en JSTL o el equivalente de Thymeleaf.
- [ ] **Símbolos de moneda internacionalizados**: `$`, `€`, etc. también dependen del locale.
- [ ] **Alt text de imágenes internacionalizado**: Para accesibilidad (a11y).
- [ ] **No concatenar textos en la vista**: No hacer `${nombre} + " - " + ${apellido}`. Usar mensajes parametrizados.
- [ ] **Singular/plural correcto**: No usar "1 unidad/es". Configurar correctamente plurales con `ChoiceFormat` o mensajes separados.

### 15.11 UX y Frontend (Correcciones de Demo)

- [ ] **Feedback al usuario tras cada acción**: Mostrar mensajes de éxito/error después de crear, editar, borrar, enviar.
- [ ] **Mantener valores del form al haber error**: Si un formulario falla validación, NO borrar los campos ya ingresados.
- [ ] **Navegación anónima**: Permitir explorar contenido sin estar logueado donde tenga sentido. No redirigir todo al login.
- [ ] **Paginación correcta**: No mostrar controles de paginación si hay una sola página. Links de paginación no deben estar rotos. Mostrar primera/última página.
- [ ] **Filtros persistentes**: Al paginar, mantener los filtros activos. Al cambiar filtros, volver a la página 1.
- [ ] **Saltos de línea**: Si el usuario escribe texto multilinea (textarea), mostrarlo respetando los saltos de línea (`white-space: pre-wrap`).
- [ ] **Cursor pointer en botones**: Todos los elementos clickeables deben mostrar `cursor: pointer`.
- [ ] **Favicon**: Tener un favicon configurado en todas las páginas.
- [ ] **UTF-8**: Usar UTF-8 en toda la aplicación. Nunca ISO-8859-1.
- [ ] **Contraste adecuado**: Los textos deben ser legibles. Verificar relación de contraste.
- [ ] **CTA en listas vacías**: Cuando una lista está vacía, mostrar un mensaje amigable y un call-to-action.
- [ ] **No permitir acciones sobre uno mismo**: No poder enviarse un mensaje a sí mismo, crear una reserva con uno mismo, etc.
- [ ] **Pedir contraseña actual al cambiarla**: Para evitar sesiones robadas.
- [ ] **No escapar `%` y `_` en búsquedas**: Escapar wildcards de SQL LIKE en el input del usuario.
- [ ] **Autologin tras verificar cuenta**: Después de verificar email o cambiar contraseña, loguear al usuario automáticamente.
- [ ] **Lógica de autologin fuera del controller**: Debe estar en la capa de servicios.

### 15.12 Repositorio y Configuración

- [ ] **No pushear archivos del IDE**: No incluir `.idea/`, `.vscode/`, `.mvn/` en el repositorio. Agregarlos al `.gitignore`.
- [ ] **No pushear archivos vacíos**: Como `jvm.config`, `maven.config`.
- [ ] **Schema.sql NO comentado**: El schema.sql debe generar las tablas necesarias al deployar. Si está comentado, la app no funciona.
- [ ] **No tener vistas en carpeta "helloworld"**: Organizar las vistas por funcionalidad (ej: `views/user/`, `views/product/`).
- [ ] **`@ExceptionHandler` con `@ResponseStatus`**: Todo exception handler debe setear el status code HTTP correspondiente.
- [ ] **Consistencia en snake_case vs camelCase**: Elegir una convención y mantenerla en todo el proyecto.
- [ ] **No usar `${pageContext.request.contextPath}`**: Usar `<c:url>` que ya resuelve el context path.
- [ ] **`@Qualifier` innecesario**: No usar `@Qualifier` si hay un solo bean de ese tipo.

---

> [!TIP]
> **Para la revisión del repo**: Una IA debe buscar cada punto de esta lista en el código fuente. Los puntos de la sección 15 son los más comunes en las correcciones reales de la cátedra y los que más bajan la nota.
