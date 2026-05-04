# Reporte de Violaciones al Checklist PAW

> **Generado automáticamente** — Revisión completa del proyecto contra `PAW_CHECKLIST.md`.
> Los ítems marcados con **⚠️ GRAVE** son explícitamente penalizados por la cátedra.

---

## Índice

1. [Arquitectura y Modelo de Capas](#1-arquitectura-y-modelo-de-capas)
2. [Maven y Gestión de Dependencias](#2-maven-y-gestión-de-dependencias)
3. [Spring MVC y Controllers](#3-spring-mvc-y-controllers)
4. [JSP, JSTL y Frontend](#4-jsp-jstl-y-frontend)
5. [Base de Datos y Persistencia](#5-base-de-datos-y-persistencia)
6. [Seguridad (XSS / SQL Injection)](#6-seguridad)
7. [Formularios y Validación](#7-formularios-y-validación)
8. [Internacionalización (i18n)](#8-internacionalización-i18n)
9. [Spring Security](#9-spring-security)
10. [Unit Testing y Mocking](#10-unit-testing-y-mocking)
11. [Logging](#11-logging)
12. [Mailing](#12-mailing)
13. [Imágenes](#13-imágenes)
14. [AOP y Transacciones](#14-aop-y-transacciones)
15. [Correcciones Frecuentes de TPs](#15-correcciones-frecuentes-de-tps)

---

## 1. Arquitectura y Modelo de Capas

### ⚠️ GRAVE — Lógica de negocio en controllers

- **Archivo**: `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/HomeController.java` - LISTO
- **Línea**: 56–189
- **Código**: `parsePriceParam`, normalización de precios, buckets de condición, armado de `ProductSearchCriteria`, intercambio min/max, deduplicación de sellers/ratings
- **Regla violada**: [1] Sin lógica de negocio en controllers / [15.1] Servicios como Facades

- **Archivo**: `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/ProductController.java` - LISTO
- **Línea**: 403–463
- **Código**: Construcción de criterios, filtros de listas (`stream().filter(...).limit(10)`), armado de mapas de ratings
- **Regla violada**: [1] Sin lógica de negocio en controllers

- **Archivo**: `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/UserController.java` - PARECE FALSO, MODEL VIEW ES DE CONTROLLER
- **Línea**: 242–331
- **Código**: `enrichProfileModel` (paginación, mapas de imágenes, compras/ventas, reseñas, productos borrados)
- **Regla violada**: [1] Sin lógica de negocio en controllers

- **Archivo**: `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/UserController.java` - LISTO
- **Línea**: 349–362
- **Código**: `userService.ban(userId); for (Product p : userProducts) { productService.hideProductByAdmin(...); reportService.deleteByProductId(...); }`
- **Regla violada**: [1] Sin lógica de negocio en controllers / [15.1] Controller NO debe orquestar múltiples servicios

### ⚠️ GRAVE — Envío de mail desde controller

- **Archivo**: `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/ProductController.java` - LISTO
- **Línea**: 486
- **Código**: `emailService.sendProductReportEmail(product, authUser.getUser(), seller);`
- **Regla violada**: [1] / [12] Mails enviados SIEMPRE desde servicios, NUNCA desde controllers

### Controller orquesta múltiples servicios

- **Archivo**: `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/ProductController.java` - LISTO
- **Línea**: 481–486
- **Código**: `reportService.report(...); userService.findById(...); emailService.sendProductReportEmail(...)`
- **Regla violada**: [15.1] Servicios deben ser Facades — un método de alto nivel por caso de uso

### Servicio sin interfaz en service-contracts - LISTO

- **Archivo**: `services/src/main/java/ar/edu/itba/paw/services/ProductReportRemovalTokenService.java`
- **Línea**: clase completa
- **Código**: `public class ProductReportRemovalTokenService { ... }` (sin interfaz correspondiente)
- **Regla violada**: [1] Interfaces obligatorias para servicios

### Servicio con @Component en vez de @Service - LISTO

- **Archivo**: `services/src/main/java/ar/edu/itba/paw/services/ProductReportRemovalTokenService.java`
- **Línea**: 21
- **Código**: `@Component`
- **Regla violada**: [3] Anotaciones correctas: `@Service` para servicios

### Modelo con SQL acoplado

- **Archivo**: `models/src/main/java/ar/edu/itba/paw/models/ProductSortOrder.java`
- **Línea**: 7–14
- **Código**: `NEWEST(..., "p.published DESC, p.product_id DESC")` — fragmentos ORDER BY en el enum del modelo
- **Regla violada**: [1] Sin lógica de presentación en servicios / modelos no deben conocer SQL

---

## 2. Maven y Gestión de Dependencias

### Versiones no centralizadas en `<properties>` del POM raíz

- **Archivo**: `pom.xml` (raíz)
- **Línea**: 203–207
- **Código**: `<artifactId>javax.servlet-api</artifactId> <version>4.0.1</version>`
- **Regla violada**: [2] Versiones centralizadas en `<properties>`

- **Archivo**: `webapp/pom.xml`
- **Línea**: 67–72
- **Código**: `<artifactId>jstl</artifactId> <version>1.2</version>` (tiene un TODO reconociendo el problema)
- **Regla violada**: [2] `<dependencyManagement>` en padre, hijos sin `<version>`

- **Archivo**: `webapp/pom.xml`
- **Línea**: 182–191
- **Código**: `<artifactId>jetty-maven-plugin</artifactId> <version>9.4.58.v20250814</version>`
- **Regla violada**: [2] Versiones centralizadas

### Versiones de plugins repetidas en módulos hijos

- **Archivos**: `models/pom.xml`, `persistence/pom.xml`, `persistence-contracts/pom.xml`, `service-contracts/pom.xml`, `services/pom.xml`, `webapp/pom.xml`
- **Código**: `<artifactId>maven-clean-plugin</artifactId> <version>3.4.0</version>` (y otros plugins con versión fija repetida en cada hijo)
- **Regla violada**: [2] Versiones centralizadas en POM raíz

### Scope incorrecto

- **Archivo**: `pom.xml` (raíz)
- **Línea**: 74–78
- **Código**: `<artifactId>postgresql</artifactId>` sin `<scope>runtime</scope>`
- **Regla violada**: [2] Scopes correctos (driver JDBC debe ser `runtime`)

### Dependencia de transitividad

- **Archivos**: `persistence/pom.xml`, `services/pom.xml`
- **Código**: Solo `junit-jupiter-api` declarado, sin `junit-jupiter-engine`
- **Regla violada**: [2] No depender de transitividad

---

## 3. Spring MVC y Controllers

### Controller con 300+ líneas

- **Archivo**: `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/ProductController.java`
- **Línea**: ~557 líneas
- **Regla violada**: [15.2] No controllers con +300 líneas

### ErrorController en lugar de @ControllerAdvice

- **Archivo**: `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/ErrorController.java`
- **Línea**: 9–20
- **Código**: `@Controller public class ErrorController { @RequestMapping("/403") ... }`
- **Regla violada**: [15.2] Usar `@ControllerAdvice` para manejo de errores, no un ErrorController

### try-catch de excepciones en controllers para validación

- **Archivo**: `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/ProductController.java`
- **Línea**: 118–127, 226–236, 243–248
- **Código**: `try { validatedImages = ImageUploadValidator.validateAll(...); } catch (InvalidImageUploadException e) { errors.rejectValue(...) }`
- **Regla violada**: [15.2] / [15.4] No try-catch para validación — usar BindingResult y custom validators

- **Archivo**: `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/PurchaseController.java`
- **Línea**: 74–81, 135–141
- **Código**: `try { purchase = purchaseService.createPurchase(...); } catch (IllegalStateException e) { ... }`
- **Regla violada**: [15.2] / [15.4] No try-catch de excepciones para errores de validación/negocio

### Falta @Valid en GET con query params

- **Archivo**: `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/HomeController.java`
- **Línea**: 76–86
- **Código**: `@RequestParam(value = "search-text", ...) final String searchText, @RequestParam(value = "categories", ...) final List<Long> categoryIds`
- **Regla violada**: [15.2] `@Valid` tanto en POST como en GET para parámetros de búsqueda/filtros

- **Archivo**: `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/UserController.java`
- **Línea**: 154–159
- **Código**: `@RequestParam(value = "page", defaultValue = "1") final int page`
- **Regla violada**: [15.2] `@Valid` en GET para parámetros de listado/paginación

### Regex faltante para IDs numéricos

- **Archivo**: `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/ImageController.java`
- **Línea**: 28–36
- **Código**: `@RequestMapping(value = "/images/{imageId}", method = RequestMethod.GET)` sin `{imageId:\\d+}`
- **Regla violada**: [3] Regex para IDs numéricos: `@RequestMapping("/{id:\\d+}")`

### Verbos HTTP incorrectos

- **Archivo**: `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/ProductController.java`
- **Línea**: 512–521
- **Código**: `@RequestMapping(value = "/products/{id:\\d+}/moderate-hide", method = RequestMethod.GET)` — mutación con GET
- **Regla violada**: [15.2] HTTP verbs correctos

- **Archivo**: `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/ProductController.java`
- **Línea**: 490–509
- **Código**: `method = RequestMethod.POST` para borrado en vez de DELETE
- **Regla violada**: [15.2] DELETE para borrar

### Falta redirect después de POST

- **Archivo**: `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/PasswordController.java`
- **Línea**: 76–98
- **Código**: `return new ModelAndView("login")` (en POST sin `redirect:`)
- **Regla violada**: [15.2] Redirect después de POST (PRG pattern)

### Constantes de paginación hardcodeadas en controllers

- **Archivo**: `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/HomeController.java`
- **Línea**: 125
- **Código**: `new ProductSearchCriteria(..., page, 12)`
- **Regla violada**: [15.2] Constantes de paginación fuera de controllers

- **Archivo**: `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/UserController.java`
- **Línea**: 259–260, 278, 284, 300, 320, 353–356
- **Código**: `10`, `1000000` como tamaños de página literal
- **Regla violada**: [15.2] Constantes de paginación fuera de controllers

- **Archivo**: `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/ProductController.java`
- **Línea**: 432–437
- **Código**: `1, 11` en ProductSearchCriteria
- **Regla violada**: [15.2] Constantes de paginación fuera de controllers

### SecurityContextHolder repetido en controllers

- **Archivo**: `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/ProductController.java`
- **Línea**: 524–529
- **Código**: `SecurityContextHolder.getContext().getAuthentication()`
- **Regla violada**: [15.2] Extraer getCurrentUser() a `@ModelAttribute` o `@ControllerAdvice`

- **Archivo**: `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/UserController.java`
- **Línea**: 148, 223–238
- **Código**: `SecurityContextHolder.getContext().setAuthentication(auth)`
- **Regla violada**: [15.2] Misma regla

- **Archivo**: `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/VerificationController.java`
- **Línea**: 139–155
- **Código**: `SecurityContextHolder.getContext().getAuthentication()` ... `.setAuthentication(...)`
- **Regla violada**: [15.2] Misma regla

### Chequeo manual de permisos en controller

- **Archivo**: `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/UserController.java`
- **Línea**: 313–317
- **Código**: `if (authUser != null && authUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")))`
- **Regla violada**: [15.3] No hacer chequeos de permisos manuales en controllers

### 403 tratado como 404

- **Archivo**: `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/ProductController.java`
- **Línea**: 174–179, 212–214
- **Código**: `if (!product.getUserId().equals(authUser.getUser().getId())) { throw new ResourceNotFoundException(); }`
- **Regla violada**: [15.3] 403 vs 404 — recurso existente no propio devuelve 404 en vez de 403

### Optional como parámetro

- **Archivo**: `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/ImageController.java`
- **Línea**: 40
- **Código**: `private ResponseEntity<byte[]> buildImageResponse(final Optional<Image> image)`
- **Regla violada**: [15.4] No usar Optional como parámetro

### Parámetros no usados en controllers

- **Archivo**: `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/PurchaseController.java`
- **Línea**: 85–90
- **Código**: `@ModelAttribute("purchaseStatusForm") final PurchaseStatusForm form` — no se usa
- **Regla violada**: [15.2] No definir parámetros que no se usan

- **Archivo**: `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/ReviewController.java`
- **Línea**: 49–54
- **Código**: `@ModelAttribute("reviewForm") final ReviewForm form` — no se usa
- **Regla violada**: [15.2] No definir parámetros que no se usan

- **Archivo**: `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/ProductController.java`
- **Línea**: 403–408
- **Código**: `@ModelAttribute("purchaseCreateForm") ... purchaseForm` — no se usa
- **Regla violada**: [15.2] No definir parámetros que no se usan

---

## 4. JSP, JSTL y Frontend

### ⚠️ GRAVE — `${variable}` sin `<c:out>` en JSPs/tags (XSS)

**Hallazgos representativos** (hay muchos más en todo el proyecto):

- **Archivo**: `webapp/src/main/webapp/WEB-INF/tags/header.tag`
- **Línea**: 43
- **Código**: `<span><sec:authentication property="principal.user.username" /></span>` — salida dinámica sin escape
- **Regla violada**: [4] / [6] `<c:out>` obligatorio

- **Archivo**: `webapp/src/main/webapp/WEB-INF/tags/button.tag`
- **Línea**: 17–23
- **Código**: `class="${classes}" data-bs-target="${target}" href="${href}" ${text}`
- **Regla violada**: [4] / [6] `<c:out>` obligatorio — texto y atributos dinámicos sin escape

- **Archivo**: `webapp/src/main/webapp/WEB-INF/views/home.jsp`
- **Línea**: 70
- **Código**: `<option value="${opt.name()}" ${opt.name() eq selectedSort ? 'selected' : ''}>`
- **Regla violada**: [4] / [6] `<c:out>` obligatorio

- **Archivo**: `webapp/src/main/webapp/WEB-INF/views/home.jsp`
- **Línea**: 87–95
- **Código**: `title="${product.title}" artist="${product.artist}" href="${productUrl}"`
- **Regla violada**: [4] / [6] `<c:out>` obligatorio

- **Archivo**: `webapp/src/main/webapp/WEB-INF/views/product-detail.jsp`
- **Línea**: 191–192
- **Código**: `action="${purchasePostUrl}" name="${_csrf.parameterName}" value="${_csrf.token}"`
- **Regla violada**: [4] / [6] `<c:out>` obligatorio

- **Archivo**: `webapp/src/main/webapp/WEB-INF/views/purchase-panel.jsp`
- **Línea**: 136
- **Código**: `<a href="mailto:${orderSeller.email}" ...>` — email en href sin escape
- **Regla violada**: [4] / [6] `<c:out>` obligatorio

- **Archivo**: `webapp/src/main/webapp/WEB-INF/tags/layout.tag`
- **Línea**: 9, 29
- **Código**: `<html lang="${pageContext.response.locale.language}">` y `<body class="${...}">`
- **Regla violada**: [4] / [6] `<c:out>` obligatorio

- **Archivo**: `webapp/src/main/webapp/WEB-INF/tags/productCard.tag`
- **Línea**: 24, 27, 58
- **Código**: `aria-label="${...}"`, `href="${href}"`, `+${fn:length(categories) - 3}`
- **Regla violada**: [4] / [6] `<c:out>` obligatorio

- **Archivo**: `webapp/src/main/webapp/WEB-INF/tags/text.tag`
- **Línea**: 14–34
- **Código**: `class="${textClass}"`
- **Regla violada**: [4] / [6] `<c:out>` obligatorio

- **Archivo**: `webapp/src/main/webapp/WEB-INF/tags/modal.tag`
- **Línea**: 16
- **Código**: `id="${id}" class="modal fade ${classes}"`
- **Regla violada**: [4] / [6] `<c:out>` obligatorio

- **Archivo**: `webapp/src/main/webapp/WEB-INF/tags/login-form.tag`
- **Línea**: 21–23
- **Código**: `action="${action}" method="${method}" name="${_csrf.parameterName}" value="${_csrf.token}"`
- **Regla violada**: [4] / [6] `<c:out>` obligatorio

- **Archivo**: `webapp/src/main/webapp/WEB-INF/tags/register-form.tag`
- **Línea**: 20–22
- **Código**: `action="${action}" method="${method}" ${_csrf...}`
- **Regla violada**: [4] / [6] `<c:out>` obligatorio

- **Archivo**: `webapp/src/main/webapp/WEB-INF/views/profile.jsp`
- **Línea**: 65, 123–164, 176–184, 193, 314–321, 353–355, 461–470, 473–474
- **Código**: Múltiples `${...}` en atributos HTML (CSRF, tokens, valores dinámicos)
- **Regla violada**: [4] / [6] `<c:out>` obligatorio

- **Archivo**: `webapp/src/main/webapp/WEB-INF/views/product-form.jsp`
- **Línea**: 39–41, 49–51, 102
- **Código**: `${_csrf...}`, `action="${postUrl}"`, `value="${cat.id}"`
- **Regla violada**: [4] / [6] `<c:out>` obligatorio

- **Archivo**: `webapp/src/main/webapp/WEB-INF/views/review-form.jsp`
- **Línea**: 52–53, 61–62, 64
- **Código**: `action="${reviewPostUrl}"`, `id="star${i}" value="${i}"`, `title="${starTitle}"`
- **Regla violada**: [4] / [6] `<c:out>` obligatorio

- **Archivo**: `webapp/src/main/webapp/WEB-INF/views/account-not-verified.jsp`
- **Línea**: 57–59
- **Código**: `name="${_csrf.parameterName}" value="${_csrf.token}"`
- **Regla violada**: [4] / [6] `<c:out>` obligatorio

- **Archivo**: `webapp/src/main/webapp/WEB-INF/tags/pagination.tag`
- **Línea**: 27, 43, 57, 75
- **Código**: `href="${prevUrl}"`, `${i}` como texto, `href="${pageUrl}"`, `href="${nextUrl}"`
- **Regla violada**: [4] / [6] `<c:out>` obligatorio

- **Archivo**: `webapp/src/main/webapp/WEB-INF/tags/category-card.tag`
- **Línea**: 12–14
- **Código**: `class="${classes}"`
- **Regla violada**: [4] / [6] `<c:out>` obligatorio

- **Archivo**: `webapp/src/main/webapp/WEB-INF/tags/filtersBar.tag`
- **Línea**: 28–29, 88–89
- **Código**: `value="${cat.id}"`, `${selectedCategoryIds.contains(cat.id) ? 'checked' : ''}`, `${selectedLabels.contains(lbl) ? ...}`
- **Regla violada**: [4] / [6] `<c:out>` obligatorio

### ⚠️ GRAVE — `${pageContext.request.contextPath}` en vez de `<c:url>`

- **Archivo**: `webapp/src/main/webapp/WEB-INF/views/account-not-verified.jsp`
- **Línea**: 44
- **Código**: `action="${pageContext.request.contextPath}/sendVerificationEmail"`
- **Regla violada**: [4] / [15.12] No usar `${pageContext.request.contextPath}` — usar `<c:url>`

- **Archivo**: `webapp/src/main/webapp/WEB-INF/views/forgot-password.jsp`
- **Línea**: 25
- **Código**: `action="<c:out value='${pageContext.request.contextPath}'/>/resetPassword"`
- **Regla violada**: [4] / [15.12] Misma regla

- **Archivo**: `webapp/src/main/webapp/WEB-INF/views/login.jsp`
- **Línea**: 13
- **Código**: `action="${pageContext.request.contextPath}/login"`
- **Regla violada**: [4] / [15.12] Misma regla

- **Archivo**: `webapp/src/main/webapp/WEB-INF/views/register.jsp`
- **Línea**: 12
- **Código**: `action="${pageContext.request.contextPath}/register"`
- **Regla violada**: [4] / [15.12] Misma regla

- **Archivo**: `webapp/src/main/webapp/WEB-INF/views/update-password.jsp`
- **Línea**: 22
- **Código**: `action="${pageContext.request.contextPath}/changePassword"`
- **Regla violada**: [4] / [15.12] Misma regla

- **Archivo**: `webapp/src/main/webapp/WEB-INF/views/product-form.jsp`
- **Línea**: 172
- **Código**: `data-context="<c:out value='${pageContext.request.contextPath}' />"`
- **Regla violada**: [4] / [15.12] Misma regla

### CSS inline en JSPs/tags

Hay **CSS inline (style="...")** masivo en casi todas las vistas. Ejemplos representativos:

- `views/account-not-verified.jsp` — líneas 12, 25
- `views/forgot-password.jsp` — línea 11
- `views/home.jsp` — líneas 103–106
- `views/product-detail.jsp` — líneas 46–52, 59–110, 195–198, 229–351, 269
- `views/product-form.jsp` — línea 75
- `views/profile.jsp` — líneas 33–34, 64–65, 123–164, 187–196, 206–214, 226–277
- `views/purchase-panel.jsp` — líneas 98–99, 109, 116, 153–155, 160–162, 177–178, 185, 225–231
- `views/review-form.jsp` — líneas 43, 56–57, 74, 79–80, 90–93, 103–142 (bloque `<style>` embebido)
- `views/trash.jsp` — líneas 13–17, 20, 55–56, 66–68
- `views/update-password.jsp` — línea 12
- `views/verification-email-sent.jsp` — líneas 12, 26
- `views/verification-status.jsp` — líneas 11, 26, 41
- `tags/login-form.tag` — línea 10
- `tags/register-form.tag` — líneas 10, 94, 95
- `tags/pagination.tag` — líneas 43, 57
- `tags/productCard.tag` — líneas 30–37
- `tags/sellerRatingStars.tag` — líneas 13–31
- `tags/category-card.tag` — línea 14
- **Regla violada**: [4] CSS separado, no inline

### Falta Spring Form taglib en formularios

- **Archivo**: `webapp/src/main/webapp/WEB-INF/views/forgot-password.jsp`
- **Línea**: 25–51
- **Código**: `<form ...>` manual con `<input>` en vez de `<form:form>` / `<form:input>` / `<form:errors>`
- **Regla violada**: [4] / [7] Spring Form taglib para formularios

### Falta pageEncoding UTF-8

- **Archivo**: `webapp/src/main/webapp/WEB-INF/views/home.jsp`
- **Línea**: 1–3
- **Código**: Falta `pageEncoding="UTF-8"` en directiva `page`
- **Regla violada**: [15.11] UTF-8 en toda la aplicación

### Confirmación destructiva con confirm() nativo

- **Archivo**: `webapp/src/main/webapp/WEB-INF/views/profile.jsp`
- **Línea**: 187–196, 473–474, 532–536, 546
- **Código**: `onsubmit="return confirm('${confirmDelete}');"`
- **Regla violada**: [15.4] / [15.11] Confirmación antes de acciones destructivas con modal JS

- **Archivo**: `webapp/src/main/webapp/WEB-INF/views/trash.jsp`
- **Línea**: 54
- **Código**: `onsubmit="return confirm('${confirmRestore}');"`
- **Regla violada**: [15.4] / [15.11] Misma regla

### Contenido de usuario sin white-space: pre-wrap

- **Archivo**: `webapp/src/main/webapp/WEB-INF/views/product-detail.jsp`
- **Línea**: 279, 306
- **Código**: `<c:out value="${review.comment}" />` y `<c:out value="${product.description}" />`
- **Regla violada**: [15.11] Respetar saltos de línea en texto generado por usuarios

- **Archivo**: `webapp/src/main/webapp/WEB-INF/views/profile.jsp`
- **Línea**: 415–417
- **Código**: `<p style="..."><c:out value="${rev.text}"/></p>`
- **Regla violada**: [15.11] Misma regla

### Paginación sin primera/última página

- **Archivo**: `webapp/src/main/webapp/WEB-INF/tags/pagination.tag`
- **Línea**: bloque 10–87
- **Código**: Solo enlaces anterior/siguiente y páginas numeradas; sin enlaces a primera y última página
- **Regla violada**: [15.11] Paginación: mostrar primera/última página

---

## 5. Base de Datos y Persistencia

### N+1 queries (JOINs en Java)

- **Archivo**: `persistence/src/main/java/ar/edu/itba/paw/persistence/ProductJdbcDao.java`
- **Línea**: 86–105
- **Código**: `final List<Category> categories = findCategoriesByProductId(productId);` dentro de `mapProduct`
- **Regla violada**: [5] / [15.6] JOINs en SQL, no N+1 queries en Java

### RowMapper no como private static final

- **Archivo**: `persistence/src/main/java/ar/edu/itba/paw/persistence/ProductJdbcDao.java`
- **Línea**: 258–271
- **Código**: Rows mapeados con `Map` + métodos de instancia en vez de `RowMapper` como constante
- **Regla violada**: [5] RowMapper reutilizable como `private static final`

- **Archivo**: `persistence/src/main/java/ar/edu/itba/paw/persistence/ProductJdbcDao.java`
- **Línea**: 315–321, 325–331
- **Código**: `(rs, rowNum) -> rs.getString(1)` — lambdas anónimas
- **Regla violada**: [5] RowMapper como `private static final`

- **Archivo**: `persistence/src/main/java/ar/edu/itba/paw/persistence/ReviewJdbcDao.java`
- **Línea**: 107–111
- **Código**: `(ResultSet rs, int rowNum) -> new SellerRatingSummary(...)` — lambda anónima
- **Regla violada**: [5] RowMapper como `private static final`

### Orden de modificadores (menor)

- **Archivo**: `persistence/src/main/java/ar/edu/itba/paw/persistence/UserJdbcDao.java`
- **Línea**: 21
- **Código**: `private final static RowMapper<User>` — debería ser `private static final`
- **Regla violada**: [5] Convención de modificadores

- **Archivo**: `persistence/src/main/java/ar/edu/itba/paw/persistence/PurchaseJdbcDao.java`
- **Línea**: 45
- **Código**: `private final static RowMapper<Purchase>` — mismo problema
- **Regla violada**: [5] Convención de modificadores

### ORDER BY concatenado en SQL

- **Archivo**: `persistence/src/main/java/ar/edu/itba/paw/persistence/ProductJdbcDao.java`
- **Línea**: 264
- **Código**: `selectSql.append(" ORDER BY ").append(criteria.getSortOrder().getSqlOrderBy())`
- **Regla violada**: [5] Placeholders `?` siempre — fragmento SQL concatenado (aunque de enum, bypass bound params)

- **Archivo**: `persistence/src/main/java/ar/edu/itba/paw/persistence/ProductJdbcDao.java`
- **Línea**: 303–304
- **Código**: `"ORDER BY " + ProductSortOrder.NEWEST.getSqlOrderBy() + " LIMIT ? OFFSET ?"`
- **Regla violada**: [5] Misma regla

### ON DELETE CASCADE faltante en schema.sql

- **Archivo**: `persistence/src/main/resources/schema.sql`
- **Línea**: 52–57 (images), 65–71 (products_categories), 73–83 (purchases), 99–104 (wishlist_products)
- **Código**: `ON DELETE NO ACTION` en todas las FKs
- **Regla violada**: [15.6] `ON DELETE CASCADE` donde corresponda

### Inconsistencia schema.sql vs test_schema.sql

- **Archivo**: `persistence/src/main/resources/test_schema.sql`
- **Línea**: 52–57
- **Código**: `data BLOB NOT NULL` en test vs `BYTEA` en producción
- **Regla violada**: [5] Consistencia entre esquemas de producción y test

---

## 6. Seguridad

> Las violaciones de XSS (`${...}` sin `<c:out>`) están detalladas en la sección 4.
> Las violaciones de SQL injection (concatenación) están en la sección 5.

---

## 7. Formularios y Validación

### Paquete incorrecto para forms

- **Archivo**: `webapp/src/main/java/ar/edu/itba/paw/webapp/form/`
- **Código**: Paquete `form` en vez de `forms`
- **Regla violada**: [7] Form beans en paquete `forms`

### Campos de formulario sin JSR-303

- **Archivo**: `webapp/src/main/java/ar/edu/itba/paw/webapp/form/LoginForm.java`
- **Línea**: 17–24
- **Código**: Contraseña con `@NotBlank` comentado
- **Regla violada**: [7] JSR-303 annotations

- **Archivo**: `webapp/src/main/java/ar/edu/itba/paw/webapp/form/UpdatePasswordForm.java`
- **Línea**: 29
- **Código**: `private String token;` sin `@NotBlank` ni anotaciones
- **Regla violada**: [7] JSR-303 annotations

- **Archivo**: `webapp/src/main/java/ar/edu/itba/paw/webapp/form/PurchaseStatusForm.java`
- **Línea**: 10–11
- **Código**: `@NotBlank private String newStatus;` sin `@Pattern` para restringir a valores de enum
- **Regla violada**: [7] JSR-303 / validación en form vs try-catch en controller

- **Archivo**: `webapp/src/main/java/ar/edu/itba/paw/webapp/form/ProductForm.java`
- **Línea**: 60–63
- **Código**: `private MultipartFile[] images; private String imageLayout;` sin anotaciones
- **Regla violada**: [7] JSR-303 annotations en campos relevantes

### Validación de negocio faltante en servicios

- **Archivo**: `services/src/main/java/ar/edu/itba/paw/services/UserServiceImpl.java`
- **Línea**: 38–71
- **Código**: `createUser(...)` sin validar email/contraseña/username no nulos/vacíos
- **Regla violada**: [7] / [15.4] Validación de negocio en servicios

- **Archivo**: `services/src/main/java/ar/edu/itba/paw/services/ReviewServiceImpl.java`
- **Línea**: 36–57
- **Código**: `create(..., int score, String text)` sin validar rango de `score`
- **Regla violada**: [7] / [15.4] Validación de negocio en servicios

---

## 8. Internacionalización (i18n)

### ⚠️ GRAVE — Texto hardcodeado en JSPs/tags

- **Archivos**: `account-not-verified.jsp` (L18), `forgot-password.jsp` (L16), `update-password.jsp` (L17), `verification-email-sent.jsp` (L17), `verification-status.jsp` (L17), `tags/header.tag` (L16), `tags/login-form.tag` (L15)
- **Código**: `Vinyland` hardcodeado
- **Regla violada**: [8] `<spring:message>` para TODO texto estático visible

- **Archivo**: `webapp/src/main/webapp/WEB-INF/views/product-detail.jsp`
- **Línea**: 171
- **Código**: `ARS` hardcodeado
- **Regla violada**: [8] / [15.10] Símbolos de moneda internacionalizados

- **Archivo**: `webapp/src/main/webapp/WEB-INF/views/product-form.jsp`
- **Línea**: 132
- **Código**: `$` símbolo de moneda hardcodeado
- **Regla violada**: [8] / [15.10] Misma regla

- **Archivo**: `webapp/src/main/webapp/WEB-INF/tags/price.tag`
- **Línea**: 1
- **Código**: `$<c:out value="${formattedPrice}" />` — símbolo `$` hardcodeado
- **Regla violada**: [8] / [15.10] Misma regla

- **Archivo**: `webapp/src/main/webapp/WEB-INF/tags/footer.tag`
- **Línea**: 8–17
- **Código**: `Vinyland`, `vinyland67@gmail.com`, `Buenos Aires, Argentina`
- **Regla violada**: [8] Texto visible hardcodeado

- **Archivo**: `webapp/src/main/webapp/WEB-INF/tags/purchase-stepper.tag`
- **Línea**: 7, 16, 29, 41, 55
- **Código**: `aria-label="Progreso de la compra"`, `Pendiente`, `Pagado`, `Enviado`, `Entregado`
- **Regla violada**: [8] / [15.10] Texto visible hardcodeado

- **Archivo**: `webapp/src/main/webapp/WEB-INF/tags/sellerRatingStars.tag`
- **Línea**: 26, 35
- **Código**: `reseña`, `Sin reseñas`
- **Regla violada**: [8] Texto visible hardcodeado

- **Archivo**: `webapp/src/main/webapp/WEB-INF/views/review-form.jsp`
- **Línea**: 37
- **Código**: `alt="Item"` — texto alternativo en inglés
- **Regla violada**: [8] / [15.10] Alt text internacionalizado

- **Archivo**: `webapp/src/main/webapp/WEB-INF/tags/modal.tag`
- **Línea**: 14, 21
- **Código**: `primaryBtn : 'Aceptar'` (default hardcodeado) y `aria-label="Close"`
- **Regla violada**: [8] / [15.10] Texto y accesibilidad hardcodeados

- **Archivo**: `webapp/src/main/webapp/WEB-INF/tags/header.tag`
- **Línea**: 15
- **Código**: `alt=""` en logo sin texto alternativo
- **Regla violada**: [15.10] Alt text internacionalizado y significativo

### Mezcla de mensaje i18n con texto alrededor

- **Archivo**: `webapp/src/main/webapp/WEB-INF/views/home.jsp`
- **Línea**: 62
- **Código**: `<spring:message code="Home.searchResultsFor" />` seguido de `<c:out value="${activeSearchText}" />`
- **Regla violada**: [8] Mensajes parametrizados con `{0}` — NUNCA mezclar tag con texto alrededor

- **Archivo**: `webapp/src/main/webapp/WEB-INF/views/product-detail.jsp`
- **Línea**: 362–365
- **Código**: `<spring:message code="ProductDetail.moreFrom" /><c:out value="${seller.username}" />`
- **Regla violada**: [8] Misma regla — usar mensaje parametrizado

### Archivo messages_es.properties vacío

- **Archivo**: `webapp/src/main/resources/messages_es.properties`
- **Línea**: archivo completo
- **Código**: (vacío)
- **Regla violada**: [8] Keys en AMBOS archivos — faltan todas las 440+ claves

### Claves duplicadas en .properties

- **Archivo**: `webapp/src/main/resources/messages.properties`
- **Línea**: 25 y 245
- **Código**: `notVerified.title=Cuenta no verificada` y `notVerified.title=¡Tu cuenta aún no está verificada!`
- **Regla violada**: [8] Clave duplicada (el último valor pisa al anterior)

- **Archivo**: `webapp/src/main/resources/messages_en.properties`
- **Línea**: 25 y 244
- **Código**: `notVerified.title` duplicada
- **Regla violada**: [8] Misma clave duplicada

### Convención de claves no respetada

- **Archivo**: `webapp/src/main/resources/messages.properties`
- **Línea**: 25–31
- **Código**: `notVerified.title` (minúsculas en vez de `NotVerified.title`)
- **Regla violada**: [8] Convención `PageName.element.property` (PascalCase)

### Literales de moneda/formato hardcodeados en arguments

- **Archivo**: `webapp/src/main/webapp/WEB-INF/tags/filtersBar.tag`
- **Línea**: 52–56
- **Código**: `arguments="$15.000"` dentro de `<spring:message .../>`
- **Regla violada**: [15.10] No usar literales hardcodeados en argumentos de i18n

---

## 9. Spring Security

### PawAuthUser: enabled siempre true

- **Archivo**: `webapp/src/main/java/ar/edu/itba/paw/webapp/auth/PawAuthUser.java`
- **Línea**: 17–26
- **Código**: `super(user.getEmail(), user.getPassword(), true, ...)` — `enabled` fijado a `true`
- **Regla violada**: [9] / [15.3] Usuarios no verificados: usar parámetro `enabled` de UserDetails según el modelo

### Roles como List en vez de Set

- **Archivo**: `webapp/src/main/java/ar/edu/itba/paw/webapp/auth/PawAuthUser.java`
- **Línea**: 57–66
- **Código**: `List<GrantedAuthority> authorities = new ArrayList<>();`
- **Regla violada**: [9] Roles como Set

### WebAuthConfig no extiende WebSecurityConfigurerAdapter

- **Archivo**: `webapp/src/main/java/ar/edu/itba/paw/webapp/config/WebAuthConfig.java`
- **Línea**: 19–72
- **Código**: `public class WebAuthConfig {` — sin `extends WebSecurityConfigurerAdapter`
- **Regla violada**: [9] Config de seguridad extiende `WebSecurityConfigurerAdapter`

---

## 10. Unit Testing y Mocking

### ⚠️ GRAVE — Uso de Mockito.verify() - LISTO

- **Archivo**: `services/src/test/java/ar/edu/itba/paw/services/PurchaseServiceImplTest.java`
- **Línea**: 91–101, 127–135, 145–146
- **Código**: `inOrder.verify(productService).findById(...)`, `Mockito.verify(emailService)...`, `Mockito.verifyNoInteractions(emailService)`
- **Regla violada**: [10] / [15.8] NO usar `Mockito.verify()` ni `verifyNoInteractions` — verificar output, no implementación

### DAOs sin tests

- `persistence/src/main/java/ar/edu/itba/paw/persistence/CategoryJdbcDao.java` — sin tests
- `persistence/src/main/java/ar/edu/itba/paw/persistence/PurchaseJdbcDao.java` — sin tests
- `persistence/src/main/java/ar/edu/itba/paw/persistence/ReportJdbcDao.java` — sin tests
- **Regla violada**: [10] / [15.8] Cobertura adecuada — DAOs sin tests

### Servicios sin tests

- `services/src/main/java/ar/edu/itba/paw/services/CategoryServiceImpl.java` — sin tests
- `services/src/main/java/ar/edu/itba/paw/services/ImageServiceImpl.java` — sin tests
- `services/src/main/java/ar/edu/itba/paw/services/ProductServiceImpl.java` — sin tests
- `services/src/main/java/ar/edu/itba/paw/services/ReportServiceImpl.java` — sin tests
- `services/src/main/java/ar/edu/itba/paw/services/EmailServiceImpl.java` — sin tests
- `services/src/main/java/ar/edu/itba/paw/services/ProductReportRemovalTokenService.java` — sin tests- LISTO
- **Regla violada**: [10] / [15.8] Tests de servicios obligatorios si tienen lógica de negocio

### Tests de DAO usando otros DAOs para setup

- **Archivo**: `persistence/src/test/java/ar/edu/itba/paw/persistence/ImageJdbcDaoTest.java`
- **Línea**: 27–34
- **Código**: `@Autowired private ProductJdbcDao productDao; @Autowired private UserJdbcDao userDao;`
- **Regla violada**: [15.8] No usar otros DAOs para setup — usar JdbcTemplate o SQL scripts

- **Archivo**: `persistence/src/test/java/ar/edu/itba/paw/persistence/ProductJdbcDaoTest.java`
- **Línea**: 33–36
- **Código**: `@Autowired private UserJdbcDao userDao;`
- **Regla violada**: [15.8] Misma regla

### Tests con más de una llamada de ejercicio

- **Archivo**: `persistence/src/test/java/ar/edu/itba/paw/persistence/ImageJdbcDaoTest.java`
- **Línea**: 54–66
- **Código**: `createImage`, `findById`, `findByProductId`, `existsByProductId`, `deleteByProductId` — todo en un solo test
- **Regla violada**: [10] 3 etapas: Setup → Ejercitar (1 llamada) → Validaciones

- **Archivo**: `persistence/src/test/java/ar/edu/itba/paw/persistence/ProductJdbcDaoTest.java`
- **Línea**: 195–219
- **Código**: `markAsUserDeleted` ×2, `findProductsByUserIdAndState`, `findByIdIfAvailable`, `restoreUserDeletedProduct` ×2 — en un solo test
- **Regla violada**: [10] Misma regla

### Tests con más de 4 assertions

- **Archivo**: `persistence/src/test/java/ar/edu/itba/paw/persistence/ImageJdbcDaoTest.java`
- **Línea**: 58–66
- **Código**: 6+ aserciones
- **Regla violada**: [10] Validaciones: máx. 4 assertions

- **Archivo**: `persistence/src/test/java/ar/edu/itba/paw/persistence/ProductJdbcDaoTest.java`
- **Línea**: 212–219
- **Código**: 7 aserciones seguidas
- **Regla violada**: [10] Misma regla

### Falta validación BD con JdbcTestUtils.countRowsInTable()

Falta en la mayoría de tests de persistencia:
- `ImageJdbcDaoTest.java`, `ReviewJdbcDaoTest.java` (tests de find/summary), `ProductJdbcDaoTest.java` (múltiples tests)
- **Regla violada**: [15.8] Validar estado final de la BD con `JdbcTestUtils.countRowsInTable()`

### Test schema en ubicación incorrecta

- **Archivo**: `persistence/src/test/java/ar/edu/itba/paw/persistence/TestConfiguration.java`
- **Línea**: 50
- **Código**: `populator.addScript(new ClassPathResource("test_schema.sql"));` — vive en `src/main/resources`
- **Regla violada**: [10] Schema test en `src/test/resources/schema.sql`

### Falta @InjectMocks en test de servicio

- **Archivo**: `services/src/test/java/ar/edu/itba/paw/services/PurchaseServiceImplTest.java`
- **Línea**: 42–47
- **Código**: Instanciación manual `purchaseService = new PurchaseServiceImpl(...)` en `@BeforeEach`
- **Regla violada**: [10] `@Mock` + `@InjectMocks`

### Cobertura insuficiente en tests existentes

- **Archivo**: `services/src/test/java/ar/edu/itba/paw/services/UserServiceImplTest.java`
- **Código**: Solo `findById` cubierto; falta `createUser`, `updateUserProfile`, etc.
- **Regla violada**: [15.8] Testear happy paths Y edge cases

- **Archivo**: `services/src/test/java/ar/edu/itba/paw/services/PasswordTokenServiceImplTest.java`
- **Código**: Solo `isValidPasswordResetToken`; sin tests de token inexistente ni `createPasswordResetTokenForUser`
- **Regla violada**: [15.8] Misma regla

---

## 11. Logging

### ⚠️ GRAVE — System.out.println y printStackTrace

- **Archivo**: `services/src/main/java/ar/edu/itba/paw/services/EmailServiceImpl.java`
- **Línea**: 119
- **Código**: `System.out.println("Product report email sent for product: " + product.getId());`
- **Regla violada**: [11] / [15.5] NUNCA System.out.println — usar SLF4J; NUNCA concatenar strings en logs

- **Archivo**: `services/src/main/java/ar/edu/itba/paw/services/EmailServiceImpl.java`
- **Línea**: 242
- **Código**: `System.out.println("Email effectively sent to: " + to + " | Action URL: " + actionUrl);`
- **Regla violada**: [11] / [15.5] Misma regla

- **Archivo**: `services/src/main/java/ar/edu/itba/paw/services/EmailServiceImpl.java`
- **Línea**: 121, 151, 179, 245
- **Código**: `e.printStackTrace();`
- **Regla violada**: [11] / [15.5] NUNCA printStackTrace() — usar SLF4J con throwable como último argumento

### Excepciones suprimidas (catch sin log/rethrow)

- **Archivo**: `services/src/main/java/ar/edu/itba/paw/services/EmailServiceImpl.java`
- **Línea**: 120–122, 150–152, 178–180, 244–246
- **Código**: `} catch (MessagingException e) { e.printStackTrace(); }` — sin logger ni rethrow
- **Regla violada**: [11] / [15.5] No suprimir excepciones

### Falta logger en EmailServiceImpl

- **Archivo**: `services/src/main/java/ar/edu/itba/paw/services/EmailServiceImpl.java`
- **Código**: Clase sin `private static final Logger LOGGER = LoggerFactory.getLogger(EmailServiceImpl.class)`
- **Regla violada**: [11] Logger por clase

### ⚠️ GRAVE — DEBUG en producción - LISTO

- **Archivo**: `webapp/src/main/resources/logback.xml`
- **Línea**: 30–32
- **Código**: `<logger name="ar.edu.itba.paw.services" level="DEBUG" additivity="false">`
- **Regla violada**: [11] Producción NO en DEBUG (GRAVE)

### Falta additivity="false" - LISTO

- **Archivo**: `webapp/src/main/resources/logback.xml`
- **Línea**: 26–28
- **Código**: `<logger name="ar.edu.itba.paw" level="INFO">` sin `additivity="false"`
- **Regla violada**: [11] `additivity="false"` en loggers específicos

---

## 12. Mailing

### ⚠️ GRAVE — Mail enviado desde controller

(ya documentado en sección 1)

### Subjects de mail hardcodeados

- **Archivo**: `services/src/main/java/ar/edu/itba/paw/services/EmailServiceImpl.java`
- **Línea**: 111
- **Código**: `messageHelper.setSubject("Vinyland - Publicación reportada");`
- **Regla violada**: [12] / [15.9] Subjects internacionalizados

- **Archivo**: `services/src/main/java/ar/edu/itba/paw/services/EmailServiceImpl.java`
- **Línea**: 141
- **Código**: `messageHelper.setSubject("Vinyland - Recuperar contraseña");`
- **Regla violada**: [12] / [15.9] Misma regla

- **Archivo**: `services/src/main/java/ar/edu/itba/paw/services/EmailServiceImpl.java`
- **Línea**: 170
- **Código**: `messageHelper.setSubject("Vinyland - Verificar cuenta");`
- **Regla violada**: [12] / [15.9] Misma regla

- **Archivo**: `services/src/main/java/ar/edu/itba/paw/services/EmailServiceImpl.java`
- **Línea**: 234
- **Código**: `messageHelper.setSubject("Vinyland - " + title);`
- **Regla violada**: [12] / [15.9] Misma regla

### Locale del emisor, no del destinatario

- **Archivo**: `services/src/main/java/ar/edu/itba/paw/services/EmailServiceImpl.java`
- **Línea**: 86–106, 131, 160, 194
- **Código**: `new Context(LocaleContextHolder.getLocale())` — usa locale del thread actual (emisor)
- **Regla violada**: [12] / [15.9] Locale del destinatario, no del emisor

- **Archivo**: `services/src/main/java/ar/edu/itba/paw/services/EmailServiceImpl.java`
- **Línea**: 31
- **Código**: `private static final Locale PRICE_LOCALE = Locale.forLanguageTag("es-AR");`
- **Regla violada**: [12] / [15.9] Formato de precio fijado al locale del sistema

### Templates de mail referenciados desde servicios

- **Archivo**: `services/src/main/java/ar/edu/itba/paw/services/EmailServiceImpl.java`
- **Línea**: 115, 145, 174, 238
- **Código**: `templateEngine.process("product-report-notification", ctx)`, `"password-reset"`, `"verification"`, `"order-notification"`
- **Regla violada**: [15.1] Templates de mail NO deben ser referenciados desde servicios

### Contenido de correo hardcodeado en servicios

- **Archivo**: `services/src/main/java/ar/edu/itba/paw/services/PurchaseServiceImpl.java`
- **Línea**: 86–88
- **Código**: `"Confirmación de compra — datos para pagar"`, `"Reservamos el vinilo para vos..."`
- **Regla violada**: [12] / [15.9] Contenido internacionalizado

- **Archivo**: `services/src/main/java/ar/edu/itba/paw/services/PurchaseServiceImpl.java`
- **Línea**: 145–148, 161–163
- **Código**: `"El comprador ha pagado"`, `"Tu vinilo ha sido enviado"`
- **Regla violada**: [12] / [15.9] Misma regla

### Recursos i18n de mail no integrados

- **Archivo**: `services/src/main/resources/i18n/mailMessages.properties` y `mailMessages_es.properties`
- **Código**: Claves definidas pero NO consumidas por `EmailServiceImpl` / `PurchaseServiceImpl`
- **Regla violada**: [12] / [15.9] Recursos de i18n existentes pero no usados

---

## 13. Imágenes

### Carga masiva de binarios

- **Archivo**: `persistence/src/main/java/ar/edu/itba/paw/persistence/ImageJdbcDao.java`
- **Línea**: 76–82, 96–102
- **Código**: `findAllByProductId` y `listImages()` seleccionan columna `data` (BYTEA) para todas las imágenes
- **Regla violada**: [13] Cargar solo cuando se necesite — loading de binarios en batch

---

## 14. AOP y Transacciones

### ⚠️ GRAVE — Falta @Transactional en servicios

**Servicios SIN `@Transactional` en NINGÚN método:**
- `ReportServiceImpl.java` — todos los métodos (líneas 27, 43, 48, 53)
- `ReviewServiceImpl.java` — todos los métodos (líneas 36, 61, 66, 71)
- `EmailServiceImpl.java` — todos los métodos (líneas 55, 70, 85, 127, 157)
- `ImageServiceImpl.java` — todos los métodos (líneas 23, 32, 37, 42, 47, 52, 57)
- `CategoryServiceImpl.java` — método `findAll` (línea 22)
- `ProductServiceImpl.java` — todos los métodos (líneas 35, 86, 91, 96, 101, 106, 111, 116, 121, 126, 131, 141, 146, 187)
- `UserServiceImpl.java` — todos los métodos (líneas 38, 74, 97, 102, 110, 115, 120, 125, 130)
- **Regla violada**: [14] / [15.7] `@Transactional` en TODOS los métodos de servicio (GRAVE)

**Servicios con `@Transactional` PARCIAL (solo algunos métodos):**
- `PasswordTokenServiceImpl.java` — solo `createPasswordResetTokenForUser` tiene `@Transactional`; faltan: `isValidPasswordResetToken` (L44), `findByUserId` (L75), `findByToken` (L80)
- `VerificationTokenServiceImpl.java` — solo `createVerificationTokenForUser`; faltan: `isValidVerificationToken` (L46), `findByUserId` (L79), `findByToken` (L84)
- `PurchaseServiceImpl.java` — solo `createPurchase`; faltan: `findById` (L112), `updateStatus` (L117), `findByBuyerId` (L181), `findBySellerId` (L186)
- **Regla violada**: [14] / [15.7] `@Transactional` en TODOS los métodos; `@Transactional(readOnly = true)` en lecturas

---

## 15. Correcciones Frecuentes de TPs

### 15.5 — Modelos y Java

#### Java Beans format (falta no-arg constructor y setters)

Todos los modelos usan solo constructores con argumentos y no tienen no-arg constructor ni setters:
- `User.java`, `Product.java`, `Purchase.java`, `Review.java`, `Image.java`, `Report.java`, `Category.java`, `Token.java`, `PaginatedResult.java`, `SellerRatingSummary.java`, `ReportedProduct.java`, `ProductSearchCriteria.java`, `ConditionBucket.java`
- **Regla violada**: [4] / [15.5] Java Beans format

#### Boxed primitives donde no son necesarios

- **Archivo**: `models/src/main/java/ar/edu/itba/paw/models/User.java`
- **Línea**: 8–11
- **Código**: `private final Boolean mod; private final Boolean enabled; private final Boolean banned;`
- **Regla violada**: [15.5] No usar boxed primitives sin necesidad (`Boolean` → `boolean`)

- **Archivos**: `User.java`, `Product.java`, `Purchase.java`, `Review.java`, `Image.java`, `Report.java`, `Category.java`, `Token.java`, `ReportedProduct.java`
- **Código**: `private final Long id` / `private final Long productId` / etc. en todos los modelos
- **Regla violada**: [15.5] No usar boxed primitives donde no son necesarios (`Long` → `long`)

#### Token con estado mutable sin sincronización

- **Archivo**: `models/src/main/java/ar/edu/itba/paw/models/Token.java`
- **Línea**: 7–10
- **Código**: `private Long tokenId; private String token; private Long userId; private Instant expirationDate;` — campos no `final`, mutables
- **Regla violada**: [1] / [15.5] Thread-safe — sin estado mutable compartido

### 15.12 — Repositorio y Configuración

#### Archivos IDE en el repositorio

- **Archivos**: `.idea/compiler.xml`, `.idea/encodings.xml`, `.idea/jarRepositories.xml`, `.idea/misc.xml`, `.idea/vcs.xml`
- **Código**: Versionados en Git
- **Regla violada**: [15.12] No pushear archivos del IDE

- **Archivo**: `.gitignore`
- **Código**: Falta `.idea/` en el `.gitignore` raíz
- **Regla violada**: [15.12] Agregar `.idea/` a `.gitignore`

#### Exploded WAR en el repositorio

- **Archivos**: `WEB-INF/`, `META-INF/`, `assets/` (raíz del repo)
- **Código**: Carpetas de deploy/WAR explodido versionadas en el repo
- **Regla violada**: [15.12] No pushear archivos generados/compilados

### 15.3 — Spring Security (complemento)

#### Lógica de autologin en controllers

- **Archivo**: `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/UserController.java`
- **Línea**: 223–238
- **Código**: `SecurityContextHolder.getContext().setAuthentication(...)` en controller
- **Regla violada**: [15.11] Lógica de autologin fuera del controller — debe estar en servicios

- **Archivo**: `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/VerificationController.java`
- **Línea**: 139–155
- **Código**: `SecurityContextHolder.getContext().setAuthentication(...)` en controller
- **Regla violada**: [15.11] Misma regla

---

## Resumen de Errores GRAVES

| Error | Cantidad de ocurrencias | Secciones |
|---|---|---|
| Lógica de negocio en controllers | 4+ controllers | 1, 15.1 |
| `${variable}` sin `<c:out>` (XSS) | 50+ instancias en JSPs/tags | 4, 6 |
| `${pageContext.request.contextPath}` en vez de `<c:url>` | 6 archivos | 4, 15.12 |
| Texto hardcodeado sin i18n | 30+ instancias | 8, 15.10 |
| Falta `@Transactional` en servicios | 9 servicios afectados | 14, 15.7 |
| Mail enviado desde controller | 1 | 1, 12 |
| `System.out.println` / `printStackTrace()` | 6 ocurrencias | 11, 15.5 |
| DEBUG en logback de producción | 1 | 11 |
| `Mockito.verify()` en tests | 7+ usos | 10, 15.8 |
| DAOs sin tests | 3 DAOs | 10, 15.8 |
| Servicios sin tests | 6 servicios | 10, 15.8 |
| CSS inline masivo | Casi todas las vistas | 4 |
| Archivos IDE en repo | 5+ archivos | 15.12 |
