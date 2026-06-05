
## 1. Errores Graves (🟠 GRAVE)

### 1.1 Cobertura de tests pobre (Sección 6.2 del checklist)

* **Ubicación:** Carpeta `services/src/test/java/ar/edu/itba/paw/services`
* **Descripción del checklist:** Servicios enteros sin ni un solo test. Falta de tests de casos de error y edge cases.
* **Detalle en el proyecto:** Los siguientes servicios carecen por completo de una clase de prueba unitaria/de integración:
  * `EmailServiceImpl`
  * `ReportServiceImpl`
  * `NotificationServiceImpl`
  * `ImageServiceImpl`
  * `CategoryServiceImpl`
  * `PendingNotificationServiceImpl`
  * Los schedulers `NewVinylDigestScheduler` y `PurchaseExpirationScheduler`.

### 1.2 Email: acoplamiento excesivo (Sección 3.2 del checklist)

* **Ubicación:** [PurchaseServiceImpl.java](file:///c:/Users/bauti/Projects/PAW-2026Q1-G2/services/src/main/java/ar/edu/itba/paw/services/PurchaseServiceImpl.java#L95-L115)
* **Descripción del checklist:** Servicios (no el de email) que son responsables de rellenar subject, elegir template, y armar variables del mail. Otros servicios que tienen muchas líneas dedicadas a preparar datos para el envío de mail.
* **Detalle en el proyecto:** `PurchaseServiceImpl` obtiene directamente los mensajes traducidos (e.g. `messageSource.getMessage("Email.purchase.buyer.confirmed.title", null, locale)`) y los pasa como strings al `EmailService` (`sendBuyerEmail`, `sendSellerEmail`). La resolución de subjects y mensajes de mail debería estar encapsulada dentro de `EmailService` o `EmailServiceImpl` para no acoplar la lógica de negocio con los detalles de presentación del mail.

### 1.3 Retornar colecciones sin paginar (Sección 2.4 del checklist)

* **Ubicación:** [ProductService.java](file:///c:/Users/bauti/Projects/PAW-2026Q1-G2/service-contracts/src/main/java/ar/edu/itba/paw/services/ProductService.java#L54-L56) / [ProductServiceImpl.java](file:///c:/Users/bauti/Projects/PAW-2026Q1-G2/services/src/main/java/ar/edu/itba/paw/services/ProductServiceImpl.java#L203-L228)
* **Descripción del checklist:** Métodos de DAO/Service que retornan `List<T>` sin paginación para colecciones que pueden crecer.
* **Detalle en el proyecto:** Los métodos `listProductsNotByUser`, `listProductsByUserExcept` y `listProductsByArtistExcept` retornan listas (`List<Product>`) sin aceptar parámetros de paginación (`page`, `pageSize`). Aunque limitan los resultados a 10 elementos, realizan búsquedas ineficientes y filtrados in-memory.

---

## 2. Errores Moderados (🟡 MODERADO)

### 2.2 Locale del mail incorrecto (Sección 3.6 del checklist)

* **Ubicación:**
  * [PendingNotificationServiceImpl.java](file:///c:/Users/bauti/Projects/PAW-2026Q1-G2/services/src/main/java/ar/edu/itba/paw/services/PendingNotificationServiceImpl.java#L96)
  * [VerificationTokenServiceImpl.java](file:///c:/Users/bauti/Projects/PAW-2026Q1-G2/services/src/main/java/ar/edu/itba/paw/services/VerificationTokenServiceImpl.java#L72)
  * [PasswordTokenServiceImpl.java](file:///c:/Users/bauti/Projects/PAW-2026Q1-G2/services/src/main/java/ar/edu/itba/paw/services/PasswordTokenServiceImpl.java#L69)
  * [ReportServiceImpl.java](file:///c:/Users/bauti/Projects/PAW-2026Q1-G2/services/src/main/java/ar/edu/itba/paw/services/ReportServiceImpl.java#L56)
  * [PurchaseServiceImpl.java](file:///c:/Users/bauti/Projects/PAW-2026Q1-G2/services/src/main/java/ar/edu/itba/paw/services/PurchaseServiceImpl.java#L94) (y líneas 104, 115, 194, 200, 205, 215)
* **Descripción del checklist:** Mails que se envían usando `LocaleContextHolder` (locale del usuario activo/request actual) en lugar del locale del destinatario del mail.
* **Detalle en el proyecto:**
  * En `PendingNotificationServiceImpl.java` (digest de nuevos vinilos), se usa `LocaleContextHolder.getLocale()` dentro del flujo ejecutado por el scheduler (`NewVinylDigestScheduler`). Esto se ejecuta en un hilo de background sin contexto HTTP, por lo que usará el locale por defecto del servidor en lugar del correspondiente a cada usuario destinatario.
  * En `PurchaseServiceImpl.java`, cuando el comprador realiza una acción, la notificación al vendedor se envía utilizando `LocaleContextHolder.getLocale()`, lo que significa que el vendedor recibirá el mail en el idioma del comprador.
  * De igual manera, los correos de verificación y reseteo de contraseña se envían basados en el contexto de la request en lugar de la configuración de locale del usuario.

### 2.4 Boxed primitives sin sentido (Sección 9.2 del checklist) [SOLUCIONADO]
* **Ubicación:** [UserService.java](file:///c:/Users/bauti/Projects/PAW-2026Q1-G2/service-contracts/src/main/java/ar/edu/itba/paw/services/UserService.java#L47-L56) / [UserServiceImpl.java](file:///c:/Users/bauti/Projects/PAW-2026Q1-G2/services/src/main/java/ar/edu/itba/paw/services/UserServiceImpl.java#L149-L191)
* **Descripción del checklist:** Funciones que devuelven `Boolean` en mayúscula cuando devuelven `int` (0 o 1) para indicar si/no, o donde `boolean` primitivo es suficiente.
* **Detalle en el proyecto (Solucionado):** Los métodos `isPasswordEmpty`, `isVerified` e `isProductInWishlist` (tanto en la capa de servicios como de persistencia/DAOs en `UserDao` y `UserJpaDao`) han sido modificados para retornar el tipo primitivo `boolean` en lugar de la clase envolvente `Boolean`, evitando el auto-boxing innecesario.

### 2.5 Uso de strings donde corresponden enums (Sección 9.5 del checklist)

* **Ubicación:** [PendingNotificationServiceImpl.java](file:///c:/Users/bauti/Projects/PAW-2026Q1-G2/services/src/main/java/ar/edu/itba/paw/services/PendingNotificationServiceImpl.java#L87)
* **Descripción del checklist:** Campos como `orderBy`, `status`, `role`, `day` que son `String` en lugar de un `enum`.
* **Detalle en el proyecto:** La comprobación del estado del producto se realiza mediante la cadena de texto `"ACTIVE".equals(p.getState())`. El método `getState()` de `Product` devuelve un `String` en lugar del enum `ProductState` correspondiente.

---
