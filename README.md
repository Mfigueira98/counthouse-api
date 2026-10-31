#counthouse-api

REST API for Counthouse, a multi-tenant inventory back office. Built with Spring Boot and
PostgreSQL. The web app (counthouse-web) calls
it from its server side.

##Status

Early setup. The project skeleton is generated; the database, the first endpoint and CI come next.
Nothing is deployed yet.

##Stack

Java 25, Spring Boot <version>, Maven (wrapper included: ./mvnw)
PostgreSQL 17, schema managed by Flyway migrations
Spring Data JPA, Bean Validation, Spring Boot Actuator
Tests: JUnit 5 and Testcontainers (a real PostgreSQL in Docker)

##Requirements

JDK 25
Docker (for the local database and the integration tests)

##Getting started

The local database setup is not added yet, so the application does not start on its own today.
This section will list the commands once it does.

##API contract

The API follows an OpenAPI contract shared with the web app. The contract will live in this
repository and change through pull requests here.

##License

[MIT](LICENSE)