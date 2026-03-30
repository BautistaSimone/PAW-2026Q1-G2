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
The application will be available at `http://localhost:8000/vinyland/`.

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
