# Vinyland - Project Context

## Project Overview
Vinyland is a multi-module Java/Spring web application designed as an e-commerce platform for vinyl records. It utilizes a layered architecture with clear separation of concerns through contract-implementation modules.

### Core Modules
- **`models`**: Domain entities (e.g., `User`, `Product`).
- **`service-contracts`**: Interfaces for the business logic layer.
- **`services`**: Implementations of the service interfaces.
- **`persistence-contracts`**: Interfaces for the data access layer.
- **`persistence`**: JDBC-based implementations of the repository interfaces.
- **`webapp`**: Presentation layer containing Spring MVC controllers, JSPs, and application configuration.

## Tech Stack
- **Language**: Java 21
- **Framework**: Spring 5.3.33 (WebMVC, JDBC, Context)
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

### Database
- The database schema is defined in `persistence/src/main/resources/schema.sql`.
- `WebConfig.java` uses `DataSourceInitializer` to automatically populate the schema on application startup.
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
- All user-submitted text evaluated within `LIKE` wildcard searches (e.g., `CONCAT('%', ?, '%')`) must be escaped upstream before parameterization to prevent wildcards like `%` and `_` from being exploited.
