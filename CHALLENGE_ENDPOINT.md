# Challenge Endpoint Implementation

## Overview
A new `/api/v1/challenge/start` endpoint that generates unique SHA256 hashes with TTL timestamps and persists them to the database.

## Files Created

### Core Implementation
- **Entity**: `src/main/kotlin/com/openpad/entity/Challenge.kt`
  - JPA entity with auto-generated ID, SHA, createdAt, expiresAt fields
  - Database-agnostic (works with H2, PostgreSQL, MSSQL)

- **Repository**: `src/main/kotlin/com/openpad/repository/ChallengeRepository.kt`
  - Spring Data JPA interface for database abstraction
  - Methods: `save()`, `findBySha()`, `findById()`, etc. (auto-generated)

- **Service**: `src/main/kotlin/com/openpad/service/ChallengeService.kt`
  - Business logic for generating SHA256 hashes
  - Manages TTL (default: 300 seconds / 5 minutes)
  - Persists to database via repository

- **Controller**: `src/main/kotlin/com/openpad/controller/ChallengeController.kt`
  - REST endpoint: `POST /api/v1/challenge/start`
  - Calls service and returns JSON response with HTTP 201 CREATED

- **DTO**: `src/main/kotlin/com/openpad/dto/ChallengeResponseDto.kt`
  - API response object (decoupled from entity)
  - Contains: sha, createdAt, expiresAt, ttlSeconds

### Tests
- **Service Tests**: `src/test/kotlin/com/openpad/service/ChallengeServiceTest.kt`
  - 6 unit tests covering SHA generation, timestamps, TTL, persistence
  - No Spring context (pure JUnit 5)
  - Tests: SHA validity, uniqueness, timestamp accuracy, repository calls

- **Controller Tests**: `src/test/kotlin/com/openpad/controller/ChallengeControllerTest.kt`
  - 5 integration tests for HTTP plumbing
  - WebMvcTest (Spring MVC slice test)
  - Tests: 201 CREATED status, JSON structure, content type

### Configuration
- **build.gradle.kts**: Added JPA, H2, PostgreSQL, MSSQL drivers
- **application.properties**: Database configuration with easy switching instructions

## API Usage

### Endpoint
```
POST /api/v1/challenge/start
Content-Type: application/json
```

### Example Request
```bash
curl -X POST http://localhost:8081/api/v1/challenge/start
```

### Example Response (201 CREATED)
```json
{
  "sha": "a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0u1v2w3x4y5z6a7b8c9d0e1f2",
  "createdAt": "2026-03-28T10:30:00Z",
  "expiresAt": "2026-03-28T10:35:00Z",
  "ttlSeconds": 300
}
```

## Database Configuration

### Default (H2 - In-Memory)
```properties
spring.datasource.url=jdbc:h2:mem:openpad
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
```
No external setup needed. Useful for development and testing.

### Switch to PostgreSQL
In `application.properties`, uncomment the PostgreSQL section and comment out H2:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/openpad
spring.datasource.driverClassName=org.postgresql.Driver
spring.datasource.username=postgres
spring.datasource.password=your_password
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```

### Switch to MSSQL
In `application.properties`, uncomment the MSSQL section and comment out H2:
```properties
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=openpad
spring.datasource.driverClassName=com.microsoft.sqlserver.jdbc.SQLServerDriver
spring.datasource.username=sa
spring.datasource.password=your_password
spring.jpa.database-platform=org.hibernate.dialect.SQLServerDialect
```

## Architecture Highlights

### Database-Agnostic Design
- **Spring Data JPA** eliminates database-specific SQL
- **Hibernate** handles dialect differences (H2 vs PostgreSQL vs MSSQL)
- **No raw SQL** — repositories use method names to derive queries
- **Minimal changes** to switch databases (just update `application.properties`)

### Clean Layering
```
Controller (HTTP plumbing)
    ↓
Service (business logic)
    ↓
Repository (data access)
    ↓
Entity (database model)
```
Each layer has a single responsibility, making it easy to test and modify.

### DTO Pattern
- API contract (DTO) is separate from database entity
- Clients never see internal database fields
- Entity schema changes don't break the API

## Running Tests

```bash
# Run all tests
./gradlew test

# Run only service tests
./gradlew test --tests ChallengeServiceTest

# Run only controller tests
./gradlew test --tests ChallengeControllerTest

# Run with detailed output
./gradlew test --info
```

## Running the Application

```bash
# Start the application
./gradlew bootRun

# The API will be available at:
# POST http://localhost:8081/api/v1/challenge/start

# View H2 console (default):
# http://localhost:8081/h2-console
# Driver: org.h2.Driver
# JDBC URL: jdbc:h2:mem:openpad
# User: sa
# Password: (blank)
```

## Summary
✅ Endpoint created with SHA + TTL support
✅ Persisted to database
✅ Database-agnostic (H2, PostgreSQL, MSSQL switchable with minimal changes)
✅ Complete unit and integration test coverage
✅ Clean architecture with separation of concerns
