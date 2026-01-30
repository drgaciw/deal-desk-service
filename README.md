# Deal Desk Service

A comprehensive microservices-based deal management system with advanced rule engine capabilities, Salesforce integration, and Total Contract Value (TCV) calculation.

## Overview

The Deal Desk Service is a Spring Boot-based application designed to streamline deal lifecycle management, pricing calculations, and integration with Salesforce CPQ. It provides a robust platform for managing complex deal workflows, validating business rules, and calculating deal values.

### Key Features

- **Deal Lifecycle Management**: Complete CRUD operations with state management (DRAFT, SUBMITTED, APPROVED, REJECTED, CANCELLED)
- **Salesforce Integration**: Bidirectional sync with Salesforce opportunities and CPQ
- **Rule Engine**: Flexible, dynamic business rule execution for TCV calculations, deal validation, and status management
- **TCV Calculation**: Advanced Total Contract Value computation with support for:
  - Base pricing models
  - Discounts and adjustments
  - Contingent revenue
  - Repricing triggers
  - Contract term variations
- **Security**: OAuth2 JWT-based authentication and authorization
- **Caching**: High-performance caching with Caffeine
- **Resilience**: Circuit breaker patterns and rate limiting for external integrations
- **API Documentation**: Interactive Swagger UI for API exploration

## Technology Stack

- **Java 21**: Latest LTS version with modern language features
- **Spring Boot 3.4.3**: Enterprise-grade application framework
- **Spring Data MongoDB**: NoSQL data persistence
- **Spring Security**: OAuth2 resource server with JWT
- **MapStruct**: Type-safe bean mapping
- **Lombok**: Reduced boilerplate code
- **Easy Rules**: Business rule engine
- **Resilience4j**: Fault tolerance and resilience patterns
- **Caffeine Cache**: High-performance caching
- **SpringDoc OpenAPI**: API documentation
- **Docker**: Containerization
- **Kubernetes**: Production orchestration

### Testing

- **JUnit 5**: Unit testing framework
- **Spring Boot Test**: Integration testing support
- **Testcontainers**: Container-based integration tests
- **AssertJ**: Fluent assertion library

## Architecture

The application follows a microservices architecture with three main components:

### 1. Deal Core Service (Port 8080)
- Deal management operations
- Business logic and validation
- Salesforce integration
- RESTful API endpoints

### 2. Rules Runtime Service (Port 8081)
- Dynamic rule execution
- Rule definition management
- Rule versioning and caching
- Metrics endpoint (Port 9090)

### 3. TCV Processors Service (Port 8082)
- Total Contract Value calculations
- Batch processing for pricing
- Advanced pricing strategies

### Data Stores

- **MongoDB**: Primary data store for deals and rules
- **PostgreSQL**: Supporting data for audit and analytics

## Prerequisites

- Java 21 or higher
- Maven 3.8+
- Docker and Docker Compose (for containerized deployment)
- MongoDB 7.0+ (or use Docker Compose)
- PostgreSQL 15+ (optional, or use Docker Compose)

## Installation

### 1. Clone the Repository

```bash
git clone https://github.com/drgaciw/deal-desk-service.git
cd deal-desk-service
```

### 2. Build the Application

```bash
mvn clean install
```

This will:
- Compile the Java source code
- Process MapStruct and Lombok annotations
- Run unit tests
- Create executable JAR file in `target/`

### 3. Configuration

Create or update the `.env` file with your environment-specific values:

```bash
# MongoDB Configuration
MONGO_USERNAME=dealdesk
MONGO_PASSWORD=your-secure-password

# JWT Authentication
JWT_ISSUER_URI=https://your-auth-server/auth/realms/dealdesk

# Salesforce Configuration
SALESFORCE_CLIENT_ID=your-client-id
SALESFORCE_CLIENT_SECRET=your-client-secret
SALESFORCE_USERNAME=your-salesforce-username
SALESFORCE_PASSWORD=your-salesforce-password
SALESFORCE_SECURITY_TOKEN=your-security-token
SALESFORCE_BASE_URL=https://login.salesforce.com
```

## Running the Application

### Local Development (Standalone)

```bash
# Run with default profile
mvn spring-boot:run

# Run with specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

The application will be available at `http://localhost:8080/api`

### Docker Compose (Recommended for Local Development)

```bash
# Start all services
docker-compose up -d

# Start specific service
docker-compose up -d deal-core

# View logs
docker-compose logs -f deal-core

# Stop all services
docker-compose down
```

### Using Deployment Scripts

```bash
# Deploy locally with Docker Compose
./deploy.sh local

# Deploy specific service
./deploy.sh local deal-core

# Deploy to Kubernetes (production)
./deploy.sh prod
```

### Docker Build and Run

```bash
# Build the JAR
mvn clean package -DskipTests

# Build Docker image
docker build -t deal-desk-service:latest .

# Run container
docker run -p 8080:8080 \
  -e MONGODB_URI=mongodb://host.docker.internal:27017/dealdesk \
  -e JWT_ISSUER_URI=https://your-auth-server/auth/realms/dealdesk \
  deal-desk-service:latest
```

## API Documentation

Once the application is running, access the interactive API documentation:

- **Swagger UI**: http://localhost:8080/api/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api/api-docs

### Key Endpoints

#### Deal Management
- `POST /api/deals` - Create a new deal
- `GET /api/deals/{id}` - Get deal by ID
- `GET /api/deals` - List all deals
- `PUT /api/deals/{id}` - Update a deal
- `DELETE /api/deals/{id}` - Delete a deal
- `GET /api/deals/status/{status}` - Get deals by status
- `POST /api/deals/{id}/submit` - Submit deal for approval
- `POST /api/deals/{id}/approve` - Approve deal
- `POST /api/deals/{id}/reject` - Reject deal

#### Salesforce Integration
- `POST /api/deals/{id}/sync` - Sync deal with Salesforce
- `POST /api/deals/{id}/pricing/sync` - Sync pricing data

#### Rule Management
- `POST /api/rules` - Create rule definition
- `GET /api/rules` - List all rules
- `GET /api/rules/{id}` - Get rule by ID
- `POST /api/rules/{id}/execute` - Execute a rule

#### Health and Monitoring
- `GET /api/actuator/health` - Application health status
- `GET /api/actuator/metrics` - Application metrics
- `GET /api/actuator/info` - Application information

## Configuration

### Application Properties

Key configuration properties in `application.yml`:

```yaml
server:
  port: 8080
  servlet:
    context-path: /api

spring:
  application:
    name: deal-desk-service
  data:
    mongodb:
      uri: ${MONGODB_URI:mongodb://localhost:27017/dealdesk}

# Rule Engine Configuration
rules:
  engine:
    versioning:
      enabled: true
      keep-versions: 5
  audit:
    enabled: true
    retention-days: 30
  validation:
    expression-timeout-ms: 5000
```

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `MONGODB_URI` | MongoDB connection string | `mongodb://localhost:27017/dealdesk` |
| `JWT_ISSUER_URI` | OAuth2 JWT issuer URI | `https://localhost:8080/auth/realms/dealdesk` |
| `SALESFORCE_CLIENT_ID` | Salesforce API client ID | - |
| `SALESFORCE_CLIENT_SECRET` | Salesforce API client secret | - |
| `SALESFORCE_USERNAME` | Salesforce username | - |
| `SALESFORCE_PASSWORD` | Salesforce password | - |
| `SALESFORCE_SECURITY_TOKEN` | Salesforce security token | - |
| `SALESFORCE_BASE_URL` | Salesforce base URL | `https://test.salesforce.com` |

## Development

### Project Structure

```
deal-desk-service/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/aciworldwide/dealdesk/
│   │   │       ├── controller/       # REST controllers
│   │   │       ├── service/          # Business logic
│   │   │       ├── repository/       # Data access
│   │   │       ├── model/            # Domain models
│   │   │       ├── mapper/           # MapStruct mappers
│   │   │       ├── exception/        # Exception handling
│   │   │       ├── config/           # Configuration classes
│   │   │       └── rules/            # Rule engine components
│   │   └── resources/
│   │       ├── application.yml       # Application configuration
│   │       └── db/                   # Database migrations
│   └── test/
│       └── java/                     # Test classes
├── k8s/                              # Kubernetes manifests
├── deal-desk-rules/                  # Rule definitions
├── docker-compose.yml                # Local development setup
├── Dockerfile                        # Container image definition
└── pom.xml                           # Maven configuration
```

### Building the Project

```bash
# Clean build
mvn clean install

# Skip tests
mvn clean install -DskipTests

# Run specific test
mvn test -Dtest=DealServiceTest

# Generate sources (MapStruct)
mvn generate-sources
```

### Code Quality

The project uses several tools to maintain code quality:

- **MapStruct**: Compile-time code generation for bean mapping
- **Lombok**: Reduces boilerplate with annotations
- **Spring Boot DevTools**: Hot reload during development
- **Qodana**: Code quality analysis (see `qodana.yaml`)

### Running Tests

```bash
# Run all tests
mvn test

# Run integration tests
mvn verify

# Run with coverage
mvn clean test jacoco:report
```

## Deployment

### Kubernetes Deployment

```bash
# Deploy to Kubernetes cluster
kubectl apply -f k8s/

# Check deployment status
kubectl get pods -n dealdesk

# View logs
kubectl logs -f deployment/deal-core -n dealdesk
```

### Production Deployment

1. Build the production image:
```bash
mvn clean package -Pprod
docker build -t deal-desk-service:v1.0.0 .
```

2. Push to container registry:
```bash
docker tag deal-desk-service:v1.0.0 your-registry/deal-desk-service:v1.0.0
docker push your-registry/deal-desk-service:v1.0.0
```

3. Deploy using deployment script:
```bash
./deploy.sh prod
```

## Monitoring

### Health Checks

The application provides comprehensive health checks:

```bash
# Check application health
curl http://localhost:8080/api/actuator/health

# Detailed health information
curl http://localhost:8080/api/actuator/health | jq .
```

### Metrics

Access metrics at: `http://localhost:8080/api/actuator/metrics`

Key metrics:
- HTTP request metrics
- JVM memory usage
- Database connection pool stats
- Cache statistics
- Custom business metrics

## Troubleshooting

### Common Issues

**MongoDB Connection Issues**
```bash
# Check MongoDB is running
docker ps | grep mongodb

# Test MongoDB connection
mongosh mongodb://localhost:27017/dealdesk
```

**Build Failures with MapStruct**
```bash
# Clean and rebuild
mvn clean compile

# Check for annotation processor issues
mvn clean install -X
```

**Port Already in Use**
```bash
# Find process using port 8080
lsof -i :8080

# Change port in application.yml
server:
  port: 8081
```

## Documentation

Additional documentation is available:

- [Service Documentation](./services-documentation-complete.md) - Comprehensive service API reference
- [Deal Service Implementation](./dealserviceimpl-doc.md) - Implementation details
- [Salesforce Service](./salesforce-service-doc.md) - Salesforce integration guide
- [Rule Services](./rule-services-doc.md) - Rule engine documentation
- [Migration Notes](./migration-notes.md) - Version migration guide
- [MapStruct Configuration](./MAPSTRUCT_FIX.md) - MapStruct setup guide

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Development Guidelines

- Follow Java coding conventions and best practices
- Write unit tests for new functionality
- Update documentation for API changes
- Use meaningful commit messages
- Ensure all tests pass before submitting PR

## License

This project is proprietary software owned by ACI Worldwide.

## Support

For questions or issues:
- Open an issue in the GitHub repository
- Contact the development team
- Review the documentation in the `docs/` directory

## Acknowledgments

- Spring Boot team for the excellent framework
- Easy Rules for the flexible rule engine
- MapStruct for type-safe mapping
- All contributors to this project
