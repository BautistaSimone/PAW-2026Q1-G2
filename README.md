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

2. Create a PostgreSQL database called `paw`.

3. Create `webapp/src/main/resources/application.properties` using the example below:

```properties
db.url=jdbc:postgresql://localhost:5432/paw
db.username=postgres
db.password=postgres
```

4. Build the whole project from the repository root:

```bash
mvn install
```

5. Start the web application:

```bash
mvn -f webapp/pom.xml jetty:run
```

6. Open `http://localhost:8000`.

On startup, the application initializes the schema automatically from
`persistence/src/main/resources/schema.sql`.

## Common issues

- `class path resource [application.properties] cannot be opened because it does not exist`
  Create `webapp/src/main/resources/application.properties`.
- `mvn jetty:run` does not start the app
  Run it from the repository root with `mvn -f webapp/pom.xml jetty:run`.
- Database connection errors
  Check that PostgreSQL is running, the `paw` database exists, and the credentials in `application.properties` are correct.

## Structure

The project consists of six modules:

- `models` - Where the data classes reside
- `persistence-contracts` - In charge of interfacing the PostgreSQL database
- `persistence` - Implementation of `persistence-contracts`
- `service-contracts` - Responsible of business logic
- `services` - Implementation of `service-contracts`
- `webapp` - Holds controllers, web config and webpage

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
