# Vinyland - Project Context

## Project Overview
Vinyland is a multi-module Java/Spring web application designed as an e-commerce platform for vinyl records. It utilizes a layered architecture with clear separation of concerns through contract-implementation modules.

### Core Modules
- **`models`**: Domain entities (e.g., `User`, `Product`).
- **`service-contracts`**: Interfaces for the business logic layer.
- **`services`**: Implementations of the service interfaces.
- **`persistence-contracts`**: Interfaces for the data access layer.
- **`persistence`**: Hibernate/JPA-based implementations of the repository interfaces.
- **`webapp`**: Presentation layer containing Spring MVC controllers, JSPs, and application configuration.

## Tech Stack
- **Language**: Java 21
- **Framework**: Spring 5.3.33 (WebMVC, ORM/JPA, Context)
- **Database**: PostgreSQL 42.2.5 (Local), HSQLDB (Testing)
- **Template Engine**: JSP (JavaServer Pages)
- **Frontend**: Bootstrap 5, CSS, Vanilla JS
- **Build Tool**: Maven 4.0.0
- **Testing**: JUnit 5.11.0, Mockito 5.23.0
- **Server**: Eclipse Jetty 9.4.58 (via Maven plugin)

## Building and Running

### Build the entire project
```powershell
mvn clean install
```

### Run the web application
From the root directory:
```powershell
mvn jetty:run -pl webapp
```
Alternatively, from the `webapp` directory:
```powershell
mvn jetty:run
```
The application will be available at `http://localhost:8000/`.

### Testing
Run all tests in the project:
```powershell
mvn test
```

## Development Conventions

### Architecture
- **Layered approach**: Controller -> Service -> DAO.
- **Dependency Injection**: Managed by Spring's `@ComponentScan` and `@Bean` definitions in `ar.edu.itba.paw.webapp.config.WebConfig`.
- **Interface-driven**: Services and DAOs should always have a corresponding interface in the `-contracts` modules.

### DTOs, Projections, and JSON Boundaries
- Keep the `models` module limited to domain entities, value objects, enums, and domain-level helper objects that are independent from any specific view, endpoint, or serialization format.
- Do not place presentation DTOs, JSON response objects, controller-specific request/response shapes, or admin-panel/view-only structures in `models`.
- Do not add Jackson or serialization annotations such as `@JsonIgnore`, `@JsonProperty`, `@JsonFormat`, `@JsonInclude`, or similar to classes in `models`. Formatting, field hiding, and JSON naming are presentation concerns.
- JSON endpoint responses should use DTOs/response classes in the `webapp` layer, for example under a `dto`, `response`, or controller-local package/class when the shape is only used by that controller.
- Persistence-only aggregate query results should live in `persistence-contracts` as projections, and service-facing aggregate results should live in `service-contracts` as business summaries when they must cross the service boundary.
- Controllers should normally return objects through `@ResponseBody` or `ResponseEntity<?>` and let Spring/Jackson serialize them. Avoid manually using `ObjectMapper` in production controllers unless there is a specific, documented need.
- The Jackson dependency, when needed for Spring MVC JSON conversion, belongs in `webapp` and root dependency management only. Do not add a Jackson dependency to `models`, `services`, `service-contracts`, `persistence`, or `persistence-contracts`.

### Database
- The database schema is defined in `persistence/src/main/resources/schema.sql` (DDL + seed data). Hibernate also manages the schema via `hbm2ddl.auto=update`.
- `WebConfig.java` uses `DataSourceInitializer` to execute `schema.sql` on startup (the `CREATE TABLE IF NOT EXISTS` statements are safe alongside Hibernate's auto-update).
- Local PostgreSQL credentials (default):
  - **Host**: `localhost`
  - **Database**: `paw`
  - **User**: `postgres`
  - **Password**: `postgres`

### Frontend
- JSPs are located in `webapp/src/main/webapp/WEB-INF/views/`.
- Static assets (CSS, images, JS) are in `webapp/src/main/webapp/assets/`.
- Custom tags are used for UI components in `webapp/src/main/webapp/WEB-INF/tags/`.

### Coding Style
- Ensure proper use of Spring annotations (`@Controller`, `@Service`, `@Repository`, `@Autowired`).
- Use domain entities from the `models` module for transferring data between layers.
- All persistence DAOs use JPA `EntityManager` with `@PersistenceContext`. Use JPQL (entity field names, not SQL column names) for queries.

### Validation and Query Efficiency
- Controller-level validation should be limited to binding flow (`@Valid` + `BindingResult`) and request orchestration. Validations that depend on a specific form/use case must be implemented as custom form validators, especially image upload checks on `MultipartFile` fields.
- File/image upload rules (size, logical MIME type, required vs. optional image, dimensions when applicable) belong in reusable validators attached to the form, not as ad hoc controller conditionals.
- Do not build maps or lists by looping through items and issuing one query per item (N+1 queries). Add DAO/service methods that batch-load the needed records with JPQL `IN` queries or equivalent bulk queries, then assemble the map/list in memory from the bulk result.

### Maven & Dependency Management
- **Centralized Versioning**: All dependency versions MUST be defined in the root `pom.xml` within the `<properties>` section.
- **Dependency Management**: The root `pom.xml` must use `<dependencyManagement>` to declare all project dependencies, including their versions and scopes.
- **Module Dependencies**: Individual modules should reference dependencies without specifying the `<version>`. This ensures alignment across the entire project and simplifies version upgrades.
- **Library Sets**: For library sets with multiple artifacts (e.g., Spring), use a single property in the root `pom.xml` to align versions across all related dependencies.

## Security Guidelines

When generating new code, always strictly adhere to the following rules to ensure the application's robustness and security:

### 1. Cross-Site Scripting (XSS) Prevention
- In the frontend (JSP and tag files), never print model objects or String variables directly using `${variable}` inside HTML structures. 
- ALWAYS wrap dynamic evaluations that print text or attributes with `<c:out value="${variable}" />`.

### 2. File Upload Validations
- Ensure validation logic runs on uploaded `MultipartFile`.
- Check file sizes strictly (e.g., limit to 5MB max).
- Perform logical MIME type verification (e.g., `image/jpeg` or `image/png`), never trust extension names solely. 
- For form submissions, implement upload validation as custom form validators instead of inline controller checks.
- Reject unexpected payloads with `IllegalArgumentException`.

### 3. Business Logic Validation
- Always validate business rules in the `Service` layer (e.g., positive numbers for prices/stock, boundary checking for enums or thresholds).
- Perform strict string sanitation: no empty strings or nulls unless conditionally supported by DB constraints, use `.trim()` appropriately.

### 4. Exception Handling
- The Web layer uses a `GlobalExceptionHandler` (`@ControllerAdvice`) to intercept `RuntimeExceptions` globally.
- Throw custom exceptions like `ResourceNotFoundException` (mapped to 404) or `IllegalArgumentException`/`IllegalStateException` (mapped to 400).
- Never return stacktraces or framework defaults to the browser. Provide generic messages to avoid disclosing DB engines or table names.

### 5. Secure Identity Matching
- Tokens or non-password secrets should be compared using `MessageDigest.isEqual(...)` in the service layer if used for authorization rules. Ensure constant-time comparisons when validating unpredictable input buffers.

### 6. SQL Injection Prevention
- All user-submitted text evaluated within `LIKE` wildcard searches must be escaped upstream before parameterization to prevent wildcards like `%` and `_` from being exploited.
- Use JPQL named parameters (`:paramName`) instead of positional parameters or string concatenation.

## Internationalization (i18n)

The application uses **Spring MessageSource**. The default locale is **Spanish (es_AR)**, and the Spanish message bundle is the only maintained source of user-facing text. `messages_en.properties` is intentionally empty and must remain empty.

### Configuration (WebConfig.java)
- **`MessageSource`**: A `ReloadableResourceBundleMessageSource` bean loads message bundles from `classpath:messages` with UTF-8 encoding.
- **`LocaleResolver`**: A `SessionLocaleResolver` stores the user's chosen locale in their session (default: `es_AR`).
- **`LocaleChangeInterceptor`**: Registered in `addInterceptors()`, listens for the `lang` request parameter to switch locale dynamically.

### Message Files
- **`webapp/src/main/resources/messages.properties`** — Default (Spanish). This is the **primary** file.
- **`webapp/src/main/resources/messages_en.properties`** — Must stay empty. Do not add English translations, duplicated Spanish keys, or placeholder keys.
- When adding new user-facing text, add the key only to **`messages.properties`**.

### Key Naming Convention
Keys follow a hierarchical `PageName.element.property` pattern. Examples:
```properties
Login.subtitle=Inicia sesión para comprar y vender vinilos
Login.email.label=Email
Login.email.placeholder=tu@email.com
Login.password.label=Contraseña
Login.password.placeholder=Tu contraseña
Login.password.show.ariaLabel=Mostrar contraseña
Login.rememberMe.label=Recordarme
ProductForm.albumTitle.label=Título del álbum
Header.search.placeholder=Buscar vinilos, artistas, sellos...
Footer.copyright=Copyright Vinyland - 2026. Todos los derechos reservados.
```

### Usage in JSPs and Tag Files
Every JSP and tag file that uses i18n must declare the Spring taglib:
```jsp
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
```

#### Inline text replacement
Replace hardcoded Spanish text with `<spring:message>`:
```jsp
<%-- WRONG: hardcoded text --%>
<p>Inicia sesión para comprar y vender vinilos</p>

<%-- CORRECT: internationalized --%>
<p><spring:message code="Login.subtitle" /></p>
```

#### Attributes (placeholder, aria-label, title)
For tag attributes that don't accept JSP tags inline, use a `var` to capture the message first:
```jsp
<%-- Store message in a variable --%>
<spring:message code="Login.email.placeholder" var="emailPlaceholder" />

<%-- Use the variable in the attribute --%>
<form:input path="email" placeholder="${emailPlaceholder}" />
```

#### Parameterized messages
Use `{0}`, `{1}`, etc. for dynamic values and `arguments` attribute:
```properties
PurchasePanel.order.id=Pedido #{0}
```
```jsp
<spring:message code="PurchasePanel.order.id" arguments="${purchase.purchaseId}" />
```

#### Page titles
Page titles must also be internationalized using a `var`:
```jsp
<spring:message code="ProductForm.title" var="pageTitle" />
<ui:layout title="${pageTitle}">
```

### Critical Rules
1. **NEVER hardcode user-facing text** in JSPs or tag files. Always use `<spring:message>` keys.
2. **Always add keys to** `messages.properties` (Spanish) **only**. Keep `messages_en.properties` empty.
3. **Keep key names consistent** with the `PageName.element.property` convention.
4. **Aria-labels and accessibility text** must also be internationalized.
5. **The `<c:out>` XSS rule still applies** — when printing dynamic model data, use `<c:out>`. The `<spring:message>` tag is only for static translatable text from the message bundles.
6. **For every new user-facing text** (labels, buttons, alerts, placeholders, titles, `aria-label`s, etc.), add the corresponding message key to **`messages.properties`** in the same change — do not defer internationalization and do not populate `messages_en.properties`.
