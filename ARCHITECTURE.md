# Vinyland Architecture

This document captures the current architecture of the application as implemented in the repository.

## 1. Module Architecture

```mermaid
flowchart LR
    webapp["webapp<br/>Controllers, forms, JSP, tags, WebConfig"]
    serviceContracts["service-contracts<br/>Service interfaces"]
    services["services<br/>Service implementations"]
    persistenceContracts["persistence-contracts<br/>DAO interfaces"]
    persistence["persistence<br/>JDBC DAO implementations"]
    models["models<br/>Domain entities"]
    postgres["PostgreSQL"]

    webapp --> serviceContracts
    serviceContracts --> models

    services --> serviceContracts
    services --> persistenceContracts

    persistence --> persistenceContracts
    persistenceContracts --> models

    services -. runtime .-> persistence
    webapp -. runtime .-> services

    persistence --> postgres
```

### Reading the diagram

- `webapp` is the presentation layer. It contains Spring MVC controllers, form objects, JSP views, tags and the main Spring configuration.
- `service-contracts` defines the interfaces consumed by the web layer.
- `services` implements business use cases and delegates persistence concerns to DAO contracts.
- `persistence-contracts` defines the repository/DAO interfaces.
- `persistence` implements those DAOs using Spring JDBC and PostgreSQL.
- `models` contains the shared domain classes: `User`, `Product`, `Image`.

## 2. Runtime Request Flow

```mermaid
flowchart TD
    browser["Browser"]
    dispatcher["DispatcherServlet"]
    controllers["Controllers<br/>HomeController<br/>UserController<br/>ProductController<br/>ImageController"]
    services["Services<br/>UserService<br/>ProductService<br/>ImageService"]
    daos["DAOs<br/>UserDao<br/>ProductDao<br/>ImageDao"]
    jdbc["Spring JDBC<br/>JdbcTemplate / SimpleJdbcInsert"]
    db["PostgreSQL"]
    views["JSP Views + Tags"]
    assets["Static assets<br/>CSS / Bootstrap / images"]

    browser --> dispatcher
    dispatcher --> controllers
    controllers --> services
    services --> daos
    daos --> jdbc
    jdbc --> db

    controllers --> views
    views --> browser

    browser --> assets
```

## 3. Application Bootstrap

```mermaid
flowchart TD
    webxml["web.xml"]
    spring["WebConfig"]
    resolver["InternalResourceViewResolver"]
    multipart["CommonsMultipartResolver"]
    datasource["DataSource"]
    init["DataSourceInitializer"]
    schema["schema.sql"]
    db["PostgreSQL"]

    webxml --> spring
    spring --> resolver
    spring --> multipart
    spring --> datasource
    spring --> init
    init --> schema
    schema --> db
```

## 4. Notes About the Current State

- The architecture is layered: `Controller -> Service -> DAO -> Database`.
- The repo is also split by contracts and implementations, which helps keep module boundaries explicit.
- `ImageController`, `ImageService` and `ImageDao` are already part of the design, so the architecture now covers users, products and images.
- `WebConfig` wires the main beans, resource handling, multipart upload support and database startup initialization.
- The view layer is server-rendered with JSP and custom tags, not a separate SPA frontend.

## 5. Suggested Presentation Version

If you need a short version for a report or slide, this one usually works well:

```mermaid
flowchart LR
    Client["Client / Browser"] --> Web["Spring MVC Web Layer"]
    Web --> Service["Service Layer"]
    Service --> Persistence["Persistence Layer"]
    Persistence --> DB["PostgreSQL"]
    Web --> View["JSP + Tags + Assets"]
```
