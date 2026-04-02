# Vinyland

Vinyl trading web application. Follows the **convention over configuration** paradigm 
using the **Spring** framework.

## Installation

First, make sure to have Java and Maven installed, then clone the github repository using:

```
git clone https://github.com/BautistaSimone/PAW-2026Q1-G2
```

> Make sure to have the `.env` with the proper credentianls before the next step.

Then, inside the root of the project run:

```
mvn install
```

Once it finishes compiling just enter the `webapp/` module and run:

```
mvn jetty:run
```

## Structure

The project consists of five modules:

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

To run the test simply do: 

```
mvn test
```

Each test can be found in the `test/` folder inside each module, e.g, 
`persistence/src/test/java/.../UserJdbcDaoTest.java`.



