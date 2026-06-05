
### 1.2 Email: acoplamiento excesivo (Sección 3.2 del checklist)

* **Ubicación:** [PurchaseServiceImpl.java](file:///c:/Users/bauti/Projects/PAW-2026Q1-G2/services/src/main/java/ar/edu/itba/paw/services/PurchaseServiceImpl.java#L95-L115)
* **Descripción del checklist:** Servicios (no el de email) que son responsables de rellenar subject, elegir template, y armar variables del mail. Otros servicios que tienen muchas líneas dedicadas a preparar datos para el envío de mail.
* **Detalle en el proyecto:** `PurchaseServiceImpl` obtiene directamente los mensajes traducidos (e.g. `messageSource.getMessage("Email.purchase.buyer.confirmed.title", null, locale)`) y los pasa como strings al `EmailService` (`sendBuyerEmail`, `sendSellerEmail`). La resolución de subjects y mensajes de mail debería estar encapsulada dentro de `EmailService` o `EmailServiceImpl` para no acoplar la lógica de negocio con los detalles de presentación del mail.

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
