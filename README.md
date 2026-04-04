# Vinyland

Vinyl trading web application. Follows the **convention over configuration** paradigm 
using the **Spring** framework.

## Requirements

- Java 21
- Maven 3.9+
- PostgreSQL running locally

## Quick start

1. Clone the repository:

```bash
git clone https://github.com/BautistaSimone/PAW-2026Q1-G2
cd PAW-2026Q1-G2
```

2. Create a PostgreSQL database called `vinyland` (or another name, but then update `application.properties` accordingly).

3. Create `webapp/src/main/resources/application.properties` using the example below:

```properties
db.url=jdbc:postgresql://localhost:5432/vinyland
db.username=postgres
db.password=postgres
```

4. Build and install all modules from the repository root:

```bash
mvn clean install -DskipTests
```

5. Start the web application:

```bash
mvn -f webapp/pom.xml org.eclipse.jetty:jetty-maven-plugin:9.4.58.v20250814:run
```

On Windows PowerShell you can also use:

```powershell
.\compAndRun.ps1
```

6. Open `http://localhost:8000`.

On startup, the application initializes the schema automatically from
`persistence/src/main/resources/schema.sql`.

## Common issues

- `class path resource [application.properties] cannot be opened because it does not exist`
  Create `webapp/src/main/resources/application.properties`.
- `503 Service Unavailable` after changing shared interfaces or implementations
  Run `mvn clean install -DskipTests` from the repository root and restart Jetty. Running only `mvn compile` can leave stale jars in the local Maven repository when the webapp is launched on its own, and stale classes in `target/` can also break Jetty hot reloads.
- `mvn jetty:run` does not start the app
  Use `mvn -f webapp/pom.xml org.eclipse.jetty:jetty-maven-plugin:9.4.58.v20250814:run`.
- Jetty says `Address already in use: bind`
  Another instance is already running on port `8000`. Stop that process first, or use `.\compAndRun.ps1` on Windows to restart cleanly.
- Database connection errors
  Check that PostgreSQL is running, the `vinyland` database exists, and the credentials in `application.properties` are correct.

## Structure

The project consists of six modules:

- `models` - Where the data classes reside
- `persistence-contracts` - In charge of interfacing the PostgreSQL database
- `persistence` - Implementation of `persistence-contracts`
- `service-contracts` - Responsible of business logic
- `services` - Implementation of `service-contracts`
- `webapp` - Holds controllers, web config and webpage

See [ARCHITECTURE.md](ARCHITECTURE.md) for diagrams of the module layout, runtime request flow and application bootstrap.

Note that webapp can only see the contracts and not the actual implementation, trying to 
import something from `services/` will fail. Same thing with the persistence module 
for the service module. 

## Testing

To run the tests simply do: 

```bash
mvn test
```

Each test can be found in the `test/` folder inside each module, e.g, 
`persistence/src/test/java/.../UserJdbcDaoTest.java`.
