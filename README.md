

## 📋 Prerequisites

- Java 17
- Maven 3.x

## 🚀 Quick Start

### Build and Run

```bash
# Build the project
mvn clean install

# Run the application
mvn spring-boot:run

# Run with specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

The application will start on `http://localhost:8080`

### Run Tests

```bash
# Run all tests
mvn test

# Run tests with coverage report
mvn clean test jacoco:report

# View coverage report
open target/site/jacoco/index.html
```

## 📚 API Documentation

### Task 1: Text Replacement API

**Endpoint:** `GET /replace?text={value}`

**Business Rules:**
- Length < 2: Returns `400 Bad Request`
- Length = 2: Returns `200 OK` with empty body
- Length > 2: Replaces first character with `*` and last character with `$`

**Examples:**

```bash
# Example 1: Simple word
curl "http://localhost:8080/replace?text=elephant"
# Response: *lephan$

# Example 2: With numbers and special chars
curl "http://localhost:8080/replace?text=abc#20xyz"
# Response: *bc#20xy$

# Example 3: Long text
curl "http://localhost:8080/replace?text=TestingCodeAssignmentProject"
# Response: *estingCodeAssignmentProjec$

# Example 4: Length = 2 (empty response)
curl "http://localhost:8080/replace?text=ab"
# Response: 200 OK (empty body)

# Example 5: Length < 2 (error)
curl "http://localhost:8080/replace?text=a"
# Response: 400 Bad Request
```

### Task 2: Forecast API

**Endpoint:** `POST /api/v1/forcast` (also supports `/forcast`)

**Request Body:**
```json
{
  "date": "2026-06-15",
  "addTemprature": true,
  "addHumidity": true,
  "addWindSpeed": false
}
```

**Response:**
```json
{
  "date": "2026-06-15",
  "maxTemperature": 22.7,
  "maxHumidity": 69.0,
  "maxWindSpeed": null
}
```

**Examples:**

```bash
# Example 1: Full forecast
curl -X POST http://localhost:8080/api/v1/forcast \
  -H "Content-Type: application/json" \
  -d '{
    "date": "2026-06-15",
    "addTemprature": true,
    "addHumidity": true,
    "addWindSpeed": true
  }'

# Example 2: Only temperature
curl -X POST http://localhost:8080/api/v1/forcast \
  -H "Content-Type: application/json" \
  -d '{
    "date": "2026-06-15",
    "addTemprature": true,
    "addHumidity": false,
    "addWindSpeed": false
  }'
```

**Error Responses:**

```json
// 400 Bad Request - Missing fields
{
  "timestamp": "2026-06-13T10:30:00",
  "status": 400,
  "error": "Missing or invalid mandatory fields",
  "errorCode": "VAL001",
  "message": "One or more required fields are missing or contain invalid values",
  "path": "/api/v1/forcast"
}

// 502 Bad Gateway - API unreachable
{
  "timestamp": "2026-06-13T10:30:00",
  "status": 502,
  "error": "Upstream API Unreachable",
  "errorCode": "FC001",
  "message": "Connection to the upstream is unreachable",
  "path": "/api/v1/forcast"
}
```


### Project Structure

```
screenplay-assignment/
├── src/main/java/com/example/screenplay/
│   ├── config/                    # Configuration classes
│   │   ├── ApiConfig.java        # API endpoint mappings (from YAML)
│   │   ├── ErrorMessagesConfig.java # Error messages (from YAML)
│   │   ├── ForecastApiConfig.java   # External API config
│   │   └── RestClientConfig.java    # HTTP client configuration
│   ├── controller/                # REST controllers
│   │   ├── ForcastController.java
│   │   └── TextReplaceController.java
│   ├── dto/                       # Data Transfer Objects
│   │   ├── ErrorResponse.java
│   │   ├── ForecastRequest.java
│   │   └── OpenMeteoResponse.java
│   ├── exception/                 # Custom exceptions
│   │   ├── ExternalApiException.java
│   │   └── GlobalExceptionHandler.java
│   ├── model/                     # JPA entities
│   │   └── ForecastRecord.java
│   ├── repository/                # Spring Data repositories
│   │   └── ForecastRepository.java
│   └── service/                   # Business logic
│       ├── ForecastService.java
│       └── TextReplaceService.java
├── src/main/resources/
│   ├── application.yml            # Main configuration
│   └── error-messages.yml         # Error codes and messages
└── src/test/java/                 # Comprehensive tests
```

## 🧪 Testing Strategy

### Test Coverage
- **Unit Tests**: Service, Controller, Repository layers
- **Integration Tests**: End-to-end API flows with WireMock
- **Coverage Target**: 95%+ (enforced by JaCoCo)

### Test Patterns (Following agentdesktop)
```java
@ExtendWith(MockitoExtension.class)
class ForecastServiceTest {
    
    @Mock
    private ForecastRepository repository;
    
    @Mock
    private RestClient restClient;
    
    // Tests following AAA pattern (Arrange-Act-Assert)
}
```

### Running Specific Tests
```bash
# Run specific test class
mvn test -Dtest=ForecastServiceTest

# Run integration tests only
mvn test -Dtest=*IntegrationTest

# Skip tests
mvn clean install -DskipTests
```

## 🔧 Configuration

### Database (H2)
- In-memory database for development
- Automatic schema creation via JPA
- H2 Console: `http://localhost:8080/h2-console`
  - JDBC URL: `jdbc:h2:mem:screenplaydb`
  - Username: `sa`
  - Password: (empty)

### External API Configuration
Configured in `application.yml`:
```yaml
forecast:
  api:
    base-url: https://api.open-meteo.com
    endpoints:
      current-forecast: /v1/forecast
    params:
      latitude: 52.52
      longitude: 13.41
    timeout:
      connect: 5000
      read: 10000
```

### Build Issues
```bash
# Clean and rebuild
mvn clean install -U

# Clear local Maven cache
rm -rf ~/.m2/repository/com/example/screenplay-assignment
```
**Author:** Screenplay Assignment Project
**Version:** 0.0.1-SNAPSHOT
**Spring Boot:** 3.5.5
**Java:** 17

