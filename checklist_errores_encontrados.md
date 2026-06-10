# Reporte de Errores Encontrados (Basado en el Checklist del TP2)

A continuación, se listan los errores conceptuales encontrados en el repositorio basándose en la guía proporcionada (`checklist_errores_tp2.md`), con su ubicación y descripción.

> [!WARNING]
> La mayoría de los errores detectados pertenecen a las categorías **CRÍTICO** y **GRAVE**, especialmente relacionados con el acoplamiento en los controladores (falta del patrón Facade) y el manejo manual de excepciones/validaciones.

---

## 1. ARQUITECTURA

### 🔴 1.1 Lógica de negocio en Controllers (CRÍTICO)

**Descripción del error:** Los Controllers no deben orquestar llamadas a múltiples servicios. Deben delegar en un servicio Facade que resuelva el caso de uso completo y devuelva los datos consolidados.
**Ubicaciones encontradas:**

- **`ProductController.java` (método `productDetail`):** El controlador obtiene el producto y luego realiza **múltiples llamadas independientes** a `imageService.findAllByProductId`, `reviewService.summaryForSeller`, `userService.findById`, `reviewService.findBySellerId`, y `productService.listProductsByUserExcept` para armar el `ModelAndView`. Esta lógica de orquestación de presentación pertenece a la capa de servicios (como un `ProductDisplayFacade`).
- **`NotificationPanelAdvice.java` (método `addNotificationPanel`):** Actuando como un controlador transversal, obtiene una página de notificaciones llamando a `notificationService`, y luego itera manualmente sobre ellas para extraer los IDs de usuarios y productos, haciendo llamadas subsiguientes a `userService.findByIds` y `productService.findByIds`.
- **`PurchaseController.java` (método `buildPurchaseView`):** Llama a `purchaseService.getPurchaseDetailsForUser` y luego condicionalmente hace otra llamada a `reviewService.findByPurchaseId`.

---

## 2. PERSISTENCIA / DAOs

### 🔴 2.2 Colecciones de alta cardinalidad mapeadas con @OneToMany/@ManyToMany (CRÍTICO)

**Descripción del error:** Mapear relaciones que pueden crecer indefinidamente como colecciones en las entidades, lo que carga todos los elementos en memoria o genera múltiples queries.
**Ubicaciones encontradas:**

- **`User.java` (líneas 65 y 74):**
  - La relación `wishlistProducts` está mapeada mediante `@ManyToMany`. La "wishlist" de un usuario puede crecer indefinidamente y no debería mapearse como una colección directa en el modelo `User`. Debería consultarse a través de un DAO paginado.

---

## 3. CONTROLLERS

### 🟢 4.1 Validación de formularios en el Controller en lugar de Custom Validators (GRAVE)

**Descripción del error:** La lógica de validación de negocio (especialmente de archivos subidos) se realiza manualmente llamando a validadores estáticos o métodos manuales desde el controlador, en lugar de integrarse con `@Valid` y custom Validators de Spring (por ej. `@FileValidator`).
**Ubicaciones encontradas:**

- **`ProductController.java` (método `createProduct` / `imageDataFrom`):** Llama directamente a `ImageUploadValidator.readAll(files)` dentro del flujo del controlador para validar e instanciar las imágenes en lugar de validar el `MultipartFile[]` a través de anotaciones de Spring en el `ProductForm`.
- **`PurchaseController.java` (método `updateStatus`):** Verifica manualmente `PaymentProofValidator.validate(form.getProofFile())` si el status es `PAID`. Esto es validación de reglas de negocio atadas a la vista y debería estar encapsulada en un Constraint de validación propio del formulario.
